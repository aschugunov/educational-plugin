package com.jetbrains.edu.java.learning.stepik.alt;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.edu.learning.checker.TaskCheckerProvider;
import com.jetbrains.edu.learning.checker.gradle.GradleTaskCheckerProvider;
import com.jetbrains.edu.learning.gradle.GradleConfiguratorBase;
import com.jetbrains.edu.learning.gradle.GradleCourseBuilderBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JHyperskillConfigurator extends GradleConfiguratorBase {
  private final JHyperskillCourseBuilder myCourseBuilder = new JHyperskillCourseBuilder();

  @NotNull
  @Override
  public GradleCourseBuilderBase getCourseBuilder() {
    return myCourseBuilder;
  }

  @NotNull
  @Override
  public String getTestFileName() {
    return "";
  }

  @NotNull
  @Override
  public TaskCheckerProvider getTaskCheckerProvider() {
    return new GradleTaskCheckerProvider() {
      @Nullable
      @Override
      protected String mainClassForFile(@NotNull Project project, @NotNull VirtualFile file) {
        return null;
      }
    };
  }
}