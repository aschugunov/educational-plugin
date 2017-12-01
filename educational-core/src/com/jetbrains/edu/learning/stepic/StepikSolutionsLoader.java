package com.jetbrains.edu.learning.stepic;

import com.intellij.ide.SaveAndSyncHandler;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.progress.Task.WithResult;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLoadingPanel;
import com.intellij.util.messages.MessageBusConnection;
import com.jetbrains.edu.learning.EduUtils;
import com.jetbrains.edu.learning.StudyTaskManager;
import com.jetbrains.edu.learning.courseFormat.CheckStatus;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.courseFormat.TaskFile;
import com.jetbrains.edu.learning.courseFormat.tasks.EduTask;
import com.jetbrains.edu.learning.courseFormat.tasks.Task;
import com.jetbrains.edu.learning.courseFormat.tasks.TaskWithSubtasks;
import com.jetbrains.edu.learning.courseFormat.tasks.TheoryTask;
import com.jetbrains.edu.learning.editor.EduEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.jetbrains.edu.learning.stepic.StepicConnector.getLastSubmission;
import static com.jetbrains.edu.learning.stepic.StepicConnector.getSolutionForStepikAssignment;
import static com.jetbrains.edu.learning.stepic.StepicConnector.removeAllTags;

public class StepikSolutionsLoader implements Disposable{
  private static final Logger LOG = Logger.getInstance(StepikSolutionsLoader.class);
  private static final int MAX_REQUEST_PARAMS = 100; // restriction of Stepik API for multiple requests
  private static final String PROGRESS_ID_PREFIX = "77-";
  private final HashMap<Integer, Future> myFutures = new HashMap<>();
  private final Project myProject;
  private MessageBusConnection myBusConnection;
  private Task mySelectedTask;

  protected StepikSolutionsLoader(@NotNull final Project project) {
    this.myProject = project;
  }

  public static StepikSolutionsLoader getInstance(@NotNull Project project) {
    StepikSolutionsLoader service = ServiceManager.getService(project, StepikSolutionsLoader.class);
    if (service != null) {
      service.init();
    }
    return service;
  }

  private void init() {
    EduEditor selectedEduEditor = EduUtils.getSelectedStudyEditor(myProject);
    if (selectedEduEditor != null && selectedEduEditor.getTaskFile() != null) {
      mySelectedTask = selectedEduEditor.getTaskFile().getTask();
    }
    addFileOpenListener();
  }

  public void loadSolutionsInBackground() {
    ProgressManager.getInstance().run(new Backgroundable(myProject, "Getting Tasks to Update") {
      @Override
      public void run(@NotNull ProgressIndicator progressIndicator) {
        Course course = StudyTaskManager.getInstance(myProject).getCourse();
        if (course != null) {
          loadSolutions(progressIndicator, course);
        }
      }
    });
  }

  @NotNull
  public List<Task> tasksToUpdateUnderProgress() throws Exception {
    return ProgressManager.getInstance().run(new WithResult<List<Task>, Exception>(myProject, "Updating Task Statuses", true) {
      @Override
      protected List<Task> compute(@NotNull ProgressIndicator progressIndicator) {
        progressIndicator.setIndeterminate(true);
        Course course = StudyTaskManager.getInstance(myProject).getCourse();
        if (course != null) {
          return EduUtils.execCancelable(() -> tasksToUpdate(course));
        }
        return Collections.emptyList();
      }
    });
  }

  public void loadSolutionsInBackground(@NotNull List<Task> tasksToUpdate) {
    ProgressManager.getInstance().run(new Backgroundable(myProject, "Updating Solutions") {
      @Override
      public void run(@NotNull ProgressIndicator progressIndicator) {
        updateTasks(tasksToUpdate, progressIndicator);
      }
    });
  }

  public void loadSolutions(@Nullable ProgressIndicator progressIndicator, @NotNull Course course) {
    List<Task> tasksToUpdate = EduUtils.execCancelable(() -> tasksToUpdate(course));
    if (tasksToUpdate != null) {
      updateTasks(tasksToUpdate, progressIndicator);
    }
    else {
      LOG.warn("Can't get a list of tasks to update");
    }
  }

  private void updateTasks(@NotNull List<Task> tasksToUpdate, @Nullable ProgressIndicator progressIndicator) {
    cancelUnfinishedTasks();
    myFutures.clear();

    CountDownLatch countDownLatch = new CountDownLatch(tasksToUpdate.size());
    for (Task task : tasksToUpdate) {
      if (progressIndicator == null || !progressIndicator.isCanceled()) {
          Future<?> future = ApplicationManager.getApplication().executeOnPooledThread(() -> {
          boolean isSolved = task.getStatus() == CheckStatus.Solved;
          if (!(task instanceof TheoryTask)) {
            loadSolution(myProject, task, isSolved);
          }
          countDownLatch.countDown();
        });
        myFutures.put(task.getStepId(), future);
      }
      else {
        countDownLatch.countDown();
      }
    }

    ApplicationManager.getApplication().invokeLater(() -> {
      if (mySelectedTask != null && tasksToUpdate.contains(mySelectedTask)) {
        EduEditor selectedEduEditor = EduUtils.getSelectedStudyEditor(myProject);
        assert selectedEduEditor != null;
        selectedEduEditor.showLoadingPanel();
        enableEditorWhenFutureDone(myFutures.get(mySelectedTask.getStepId()));
      }
    });

    try {
      countDownLatch.await();
      ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(EduUtils::synchronize));
      myBusConnection.disconnect();
    }
    catch (InterruptedException e) {
      LOG.warn(e.getCause());
    }
  }

  private void cancelUnfinishedTasks() {
    for (Future future : myFutures.values()) {
      if (!future.isDone()) {
        future.cancel(true);
      }
    }
  }

  private List<Task> tasksToUpdate(@NotNull Course course) {
    List<Task> tasksToUpdate = new ArrayList<>();
    Task[] allTasks = course.getLessons().stream().flatMap(lesson -> lesson.getTaskList().stream()).toArray(Task[]::new);
    int length = allTasks.length;
    for (int i = 0; i < length; i += MAX_REQUEST_PARAMS) {
      List<Task> sublist = Arrays.asList(allTasks).subList(i, Math.min(i + MAX_REQUEST_PARAMS, length));
      String[] progresses = sublist.stream().map(task -> PROGRESS_ID_PREFIX + String.valueOf(task.getStepId())).toArray(String[]::new);
      Boolean[] taskStatuses = StepicConnector.taskStatuses(progresses);
      if (taskStatuses == null) return tasksToUpdate;
      for (int j = 0; j < sublist.size(); j++) {
        Boolean isSolved = taskStatuses[j];
        Task task = sublist.get(j);
        boolean toUpdate = false;
        if (isSolved != null && !(task instanceof TheoryTask)) {
          toUpdate = isToUpdate(task, isSolved, task.getStatus(), task.getStepId());
        }
        if (toUpdate) {
          CheckStatus checkStatus = isSolved ? CheckStatus.Solved : CheckStatus.Failed;
          task.setStatus(checkStatus);
          tasksToUpdate.add(task);
        }
      }
    }
    return tasksToUpdate;
  }

  private void addFileOpenListener() {
    myBusConnection = ApplicationManager.getApplication().getMessageBus().connect();
    myBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
      @Override
      public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        EduEditor eduEditor = EduUtils.getSelectedStudyEditor(myProject);
        TaskFile taskFile = EduUtils.getTaskFile(myProject, file);
        if (eduEditor != null && taskFile != null) {
          mySelectedTask = taskFile.getTask();
          Task task = taskFile.getTask();
          if (myFutures.containsKey(task.getStepId())) {
            eduEditor.showLoadingPanel();
            Future future = myFutures.get(task.getStepId());
            if (!future.isDone() || !future.isCancelled()) {
              enableEditorWhenFutureDone(future);
            }
          }
        }
      }
    });
  }

  private void enableEditorWhenFutureDone(@NotNull Future future) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try {
        future.get();
        ApplicationManager.getApplication().invokeLater(() -> {
          EduEditor selectedEditor = EduUtils.getSelectedStudyEditor(myProject);
          if (selectedEditor != null && mySelectedTask.getTaskFiles().containsKey(selectedEditor.getTaskFile().name)) {
            JBLoadingPanel component = selectedEditor.getComponent();
            component.stopLoading();
            ((EditorImpl) selectedEditor.getEditor()).setViewer(false);
            selectedEditor.validateTaskFile();
          }
        });
      }
      catch (InterruptedException | ExecutionException e) {
        LOG.warn(e.getCause());
      }
    });
  }

  private static boolean isToUpdate(Task task, @NotNull Boolean isSolved, @NotNull CheckStatus currentStatus, int stepId) {
    if (isSolved && currentStatus != CheckStatus.Solved) {
      return true;
    }
    else if (!isSolved) {
      try {
        if (task instanceof EduTask) {
          List<StepicWrappers.SolutionFile> solutionFiles = getLastSubmission(String.valueOf(stepId), isSolved);
          if (!solutionFiles.isEmpty()) {
            return true;
          }
        }
        else {
          String solution = getSolutionForStepikAssignment(task, isSolved);
          if (solution != null) {
            return true;
          }

        }
      }
      catch (IOException e) {
        LOG.warn(e.getMessage());
      }
    }

    return false;
  }

  private static void loadSolution(@NotNull Project project, @NotNull Task task, boolean isSolved) {
    if (task instanceof TaskWithSubtasks) {
      return;
    }

    try {
      String solutionText = loadSolution(task, isSolved);
      if (solutionText.isEmpty()) return;
      updateFiles(project, task, solutionText);
    }
    catch (IOException e) {
      LOG.warn(e.getMessage());
    }
  }

  private static String loadSolution(@NotNull Task task, boolean isSolved) throws IOException {
    if (task instanceof EduTask) {
      List<StepicWrappers.SolutionFile> solutionFiles = getLastSubmission(String.valueOf(task.getStepId()), isSolved);
      if (solutionFiles.isEmpty()) {
        task.setStatus(CheckStatus.Unchecked);
        return "";
      }
      for (StepicWrappers.SolutionFile file : solutionFiles) {
        TaskFile taskFile = task.getTaskFile(file.name);
        if (taskFile != null) {
          task.setStatus(isSolved ? CheckStatus.Solved : CheckStatus.Failed);
          if (StepicConnector.setPlaceholdersFromTags(taskFile, file)) {
            return removeAllTags(file.text);
          }
          else {
            return file.text;
          }
        }
      }
    }
    else {
      String solution = getSolutionForStepikAssignment(task, isSolved);
      if (solution != null) {
        task.setStatus(isSolved ? CheckStatus.Solved : CheckStatus.Failed);
        return solution;
      }
    }
    return "";
  }

  private static void updateFiles(@NotNull Project project, @NotNull Task task, String solutionText) {
    VirtualFile taskDir = task.getTaskDir(project);
    if (taskDir == null) {
      return;
    }
    ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
      for (TaskFile taskFile : task.getTaskFiles().values()) {
        VirtualFile vFile = taskDir.findChild(taskFile.name);
        if (vFile != null) {
          try {
            taskFile.setTrackChanges(false);
            VfsUtil.saveText(vFile, solutionText);
            SaveAndSyncHandler.getInstance().refreshOpenFiles();
            taskFile.setTrackChanges(true);
          }
          catch (IOException e) {
            LOG.warn(e.getMessage());
          }
        }
      }
    }));
  }

  @Override
  public void dispose() {
    myBusConnection.disconnect();
    cancelUnfinishedTasks();
  }
}
