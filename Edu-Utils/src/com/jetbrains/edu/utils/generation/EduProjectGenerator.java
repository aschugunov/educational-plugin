package com.jetbrains.edu.utils.generation;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.StudyTaskManager;
import com.jetbrains.edu.learning.courseGeneration.StudyProjectGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class EduProjectGenerator extends StudyProjectGenerator {
    private static final Logger LOG = Logger.getInstance(EduProjectGenerator.class);

    @Override
    public void generateProject(@NotNull Project project, @NotNull VirtualFile baseDir) {
        final Course course = getCourse(project);
        if (course == null) {
            LOG.warn("Failed to get course");
            return;
        }
        StudyTaskManager.getInstance(project).setCourse(course);
        course.setCourseDirectory(new File(OUR_COURSES_DIR, mySelectedCourseInfo.getName()).getAbsolutePath());
    }
}
