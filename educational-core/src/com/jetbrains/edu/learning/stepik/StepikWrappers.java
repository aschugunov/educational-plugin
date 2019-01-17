package com.jetbrains.edu.learning.stepik;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.jetbrains.edu.learning.EduVersions;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.courseFormat.EduCourse;
import com.jetbrains.edu.learning.courseFormat.Lesson;
import com.jetbrains.edu.learning.courseFormat.Section;
import com.jetbrains.edu.learning.courseFormat.tasks.Task;
import com.jetbrains.edu.learning.stepik.serialization.StepikSubmissionTaskAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StepikWrappers {

  public static class CoursesContainer {
    public List<EduCourse> courses;
    public Map meta;
  }

  public static class CourseWrapper {
    EduCourse course;

    public CourseWrapper(@NotNull Course course) {
      this.course = new EduCourse();
      this.course.setName(course.getName());
      this.course.setLanguage(course.getLanguage());
      this.course.setDescription(course.getDescription());
      this.course.setAuthors(course.getAuthors());
      if (course instanceof EduCourse && ((EduCourse)course).isRemote()) {
        this.course.setInstructors(((EduCourse)course).getInstructors());
        this.course.setPublic(((EduCourse)course).isPublic());
      }
    }
  }

  public static class LessonWrapper {
    Lesson lesson;

    public LessonWrapper(Lesson lesson) {
      this.lesson = new Lesson();
      this.lesson.setName(lesson.getName());
      this.lesson.setId(lesson.getId());
      this.lesson.steps = new ArrayList<>();
      this.lesson.setPublic(true);
    }
  }

  public static class LessonContainer {
    public List<Lesson> lessons;
  }

  public static class SectionWrapper {
    Section section;

    public void setSection(Section section) {
      this.section = section;
    }
  }

  public static class SectionContainer {
    List<Section> sections;
    public List<Section> getSections() {
      return sections;
    }
  }

  public static class Unit {
    int id;
    int section;
    int lesson;
    int position;
    @SerializedName("update_date") Date updateDate;
    List<Integer> assignments;

    public void setSection(int section) {
      this.section = section;
    }

    public void setPosition(int position) {
      this.position = position;
    }

    public int getPosition() {
      return position;
    }

    public void setLesson(int lesson) {
      this.lesson = lesson;
    }

    public int getSection() {
      return section;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public Date getUpdateDate() {
      return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
      this.updateDate = updateDate;
    }
  }

  public static class UnitContainer {
    public List<Unit> units;
  }

  public static class UnitWrapper {
    Unit unit;

    public void setUnit(Unit unit) {
      this.unit = unit;
    }
  }

  public static class AttemptWrapper {
    public static class Attempt {
      int step;
      public Dataset dataset;
      String status;
      String user;
      int id;

      public Attempt(int step) {
        this.step = step;
      }

      public boolean isActive() {
        return status.equals("active");
      }
    }

    public static class Dataset {
      public boolean is_multiple_choice;
      public List<String> options;
    }

    public AttemptWrapper(int step) {
      attempt = new Attempt(step);
    }

    Attempt attempt;
  }

  static class AttemptContainer {
    List<AttemptWrapper.Attempt> attempts;
  }

  public static class SolutionFile {
    public String name;
    public String text;

    public SolutionFile(String name, String text) {
      this.name = name;
      this.text = text;
    }
  }

  static class AuthorWrapper {
    List<StepikUserInfo> users;
  }

  static class SubmissionsWrapper {
    Submission[] submissions;
  }

  static class SubmissionWrapper {
    Submission submission;

    public SubmissionWrapper(int attemptId, String score, ArrayList<SolutionFile> files, Task task) {
      String serializedTask = new GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(Task.class, new StepikSubmissionTaskAdapter())
        .create()
        .toJson(new StepikWrappers.TaskWrapper(task));
      submission = new Submission(score, attemptId, files, serializedTask);
    }
  }

  public static class Reply {

    String score;
    List<SolutionFile> solution;
    public String language;
    public String code;
    String edu_task;
    public int version = EduVersions.JSON_FORMAT_VERSION;

    public Reply(List<SolutionFile> files, String score, String serializedTask) {
      this.score = score;
      solution = files;
      edu_task = serializedTask;
    }
  }

  public static class Submission {
    int attempt;
    public final Reply reply;

    public Submission(String score, int attemptId, ArrayList<SolutionFile> files, String serializedTask) {
      reply = new Reply(files, score, serializedTask);
      this.attempt = attemptId;
    }
  }

  static class SubmissionToPostWrapper {
    Submission submission;

    public SubmissionToPostWrapper(@NotNull String attemptId, @NotNull String language, @NotNull String code) {
      submission = new Submission(attemptId, new Submission.CodeReply(language, code));
    }

    public SubmissionToPostWrapper(@NotNull String attemptId, boolean[] choices) {
      submission = new Submission(attemptId, new Submission.ChoiceReply(choices));
    }

    static class Submission {
      String attempt;
      Reply reply;

      public Submission(String attempt, Reply reply) {
        this.attempt = attempt;
        this.reply = reply;
      }


      interface Reply {}

      static class CodeReply implements Reply {
        String language;
        String code;

        public CodeReply(String language, String code) {
          this.language = language;
          this.code = code;
        }
      }

      static class ChoiceReply implements Reply {
        boolean[] choices;

        public ChoiceReply(boolean[] choices) {
          this.choices = choices;
        }
      }
    }
  }

  static class ResultSubmissionWrapper {
    ResultSubmission[] submissions;

    static class ResultSubmission {
      int id;
      String status;
      String hint;
    }
  }

  static class AssignmentsWrapper {
    List<Assignment> assignments;
  }

  static class Assignment {
    int id;
    int step;
  }

  static class ViewsWrapper {
    View view;

    public ViewsWrapper(final int assignment, final int step) {
      this.view = new View(assignment, step);
    }
  }

  static class View {
    int assignment;
    int step;

    public View(int assignment, int step) {
      this.assignment = assignment;
      this.step = step;
    }
  }

  static class ProgressContainer {
    static class Progress {
      String id;
      boolean isPassed;
    }

    List<Progress> progresses;

  }

  public static class TaskWrapper {
    @Expose Task task;

    public TaskWrapper(Task task) {
      this.task = task;
    }
  }
}
