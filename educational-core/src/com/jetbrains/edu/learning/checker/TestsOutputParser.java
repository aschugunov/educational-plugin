package com.jetbrains.edu.learning.checker;

import com.intellij.execution.process.ProcessOutput;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.jetbrains.edu.learning.checker.CheckUtils.*;


public class TestsOutputParser {
  public static final String TEST_OK = "test OK";
  private static final String TEST_FAILED = "FAILED + ";
  private static final String CONGRATS_MESSAGE = "CONGRATS_MESSAGE ";
  public static final String CONGRATULATIONS = "Congratulations!";

  public static class TestsOutput {
    private final boolean isSuccess;
    private final String myMessage;

    public TestsOutput(boolean isSuccess, @NotNull final String message) {
      this.isSuccess = isSuccess;
      myMessage = message;
    }

    public boolean isSuccess() {
      return isSuccess;
    }

    public String getMessage() {
      return myMessage;
    }
  }

  @NotNull
  public static TestsOutput getTestsOutput(@NotNull final ProcessOutput processOutput, final boolean isAdaptive) {
    //gradle prints compilation failures to error stream
    if (hasCompilationErrors(processOutput)) {
      return new TestsOutput(false, COMPILATION_FAILED_MESSAGE);
    }
    String congratulations = CONGRATULATIONS;
    final List<String> lines = processOutput.getStdoutLines();
    for (int i = 0; i < lines.size(); i++) {
      final String line = lines.get(i);
      if (line.startsWith(STUDY_PREFIX)) {
        if (line.contains(TEST_OK)) {
          continue;
        }

        if (line.contains(CONGRATS_MESSAGE)) {
          congratulations = line.substring(line.indexOf(CONGRATS_MESSAGE) + CONGRATS_MESSAGE.length());
        }

        if (line.contains(TEST_FAILED)) {
          if (!isAdaptive) {
            return new TestsOutput(false, line.substring(line.indexOf(TEST_FAILED) + TEST_FAILED.length()));
          }
          else {
            final StringBuilder builder = new StringBuilder(line.substring(line.indexOf(TEST_FAILED) + TEST_FAILED.length()) + "\n");
            for (int j = i + 1; j < lines.size(); j++) {
              final String failedTextLine = lines.get(j);
              if (!failedTextLine.contains(STUDY_PREFIX) || !failedTextLine.contains(CONGRATS_MESSAGE)) {
                builder.append(failedTextLine);
                builder.append("\n");
              }
              else {
                break;
              }
            }
            return new TestsOutput(false, builder.toString());
          }          
        }
      }
    }

    return new TestsOutput(true, congratulations);
  }

  private static boolean hasCompilationErrors(@NotNull ProcessOutput processOutput) {
    return ContainerUtil.find(processOutput.getStderrLines(), line -> line.contains(COMPILATION_ERROR)) != null;
  }
}
