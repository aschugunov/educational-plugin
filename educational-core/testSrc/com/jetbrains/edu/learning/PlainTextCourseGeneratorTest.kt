package com.jetbrains.edu.learning

import com.intellij.openapi.util.SystemInfo
import com.intellij.testFramework.LightPlatformTestCase
import com.jetbrains.edu.coursecreator.CCUtils

class PlainTextCourseGeneratorTest : EduTestCase() {

  fun `test course structure creation`() {
    courseWithFiles {
      lesson {
        eduTask {
          taskFile("Fizz.kt")
        }
        eduTask {
          taskFile("Buzz.kt")
        }
      }
      lesson {
        eduTask {
          taskFile("FizzBuzz.kt")
        }
      }
    }

    checkFileTree {
      dir("lesson1") {
        dir("task1") {
          file("Fizz.kt")
          file("task.html")
        }
        dir("task2") {
          file("Buzz.kt")
          file("task.html")
        }
      }
      dir("lesson2") {
        dir("task1") {
          file("FizzBuzz.kt")
          file("task.html")
        }
      }
    }
  }

  fun `test course with framework lesson structure creation`() {
    courseWithFiles {
      frameworkLesson {
        eduTask {
          taskFile("Fizz.kt")
        }
        eduTask {
          taskFile("Buzz.kt")
        }
      }
      lesson {
        eduTask {
          taskFile("FizzBuzz.kt")
        }
      }
    }

    checkFileTree {
      dir("lesson1") {
        dir("task") {
          file("Fizz.kt")
        }
        dir("task1") {
          file("task.html")
        }
        dir("task2") {
          file("task.html")
        }
      }
      dir("lesson2") {
        dir("task1") {
          file("FizzBuzz.kt")
          file("task.html")
        }
      }
    }
  }

  fun `test course with framework lesson structure creation in CC mode`() {
    courseWithFiles(courseMode = CCUtils.COURSE_MODE) {
      frameworkLesson {
        eduTask {
          taskFile("Fizz.kt")
        }
        eduTask {
          taskFile("Buzz.kt")
        }
      }
      lesson {
        eduTask {
          taskFile("FizzBuzz.kt")
        }
      }
    }

    checkFileTree {
      dir("lesson1") {
        dir("task1") {
          file("Fizz.kt")
          file("task.html")
        }
        dir("task2") {
          file("Buzz.kt")
          file("task.html")
        }
      }
      dir("lesson2") {
        dir("task1") {
          file("FizzBuzz.kt")
          file("task.html")
        }
      }
    }
  }

  fun `test course with sections creation`() {
    courseWithFiles {
      section {
        lesson {
          eduTask {
            taskFile("Fizz.kt")
          }
          eduTask {
            taskFile("Buzz.kt")
          }
        }
      }
      lesson {
        eduTask {
          taskFile("FizzBuzz.kt")
        }
      }
    }

    checkFileTree {
      dir("section1") {
        dir("lesson1") {
          dir("task1") {
            file("Fizz.kt")
            file("task.html")
          }
          dir("task2") {
            file("Buzz.kt")
            file("task.html")
          }
        }
      }
      dir("lesson1") {
        dir("task1") {
          file("FizzBuzz.kt")
          file("task.html")
        }
      }
    }
  }

  fun `test course creation in CC mode`() {
    courseWithFiles(courseMode = CCUtils.COURSE_MODE) {
      section {
        lesson {
          eduTask {
            taskFile("Fizz.kt")
          }
          eduTask {
            taskFile("Buzz.kt")
          }
        }
      }
      lesson {
        eduTask {
          taskFile("FizzBuzz.kt")
        }
      }
    }

    checkFileTree {
      dir("section1") {
        dir("lesson1") {
          dir("task1") {
            file("Fizz.kt")
            file("task.html")
          }
          dir("task2") {
            file("Buzz.kt")
            file("task.html")
          }
        }
      }
      dir("lesson1") {
        dir("task1") {
          file("FizzBuzz.kt")
          file("task.html")
        }
      }
    }
  }

  fun `test course with sections creation in CC mode`() {
    courseWithFiles(courseMode = CCUtils.COURSE_MODE) {
      section {
        lesson {
          eduTask {
            taskFile("Fizz.kt")
          }
          eduTask {
            taskFile("Buzz.kt")
          }
        }
      }
      lesson {
        eduTask {
          taskFile("FizzBuzz.kt")
        }
      }
    }

    checkFileTree {
      dir("section1") {
        dir("lesson1") {
          dir("task1") {
            file("Fizz.kt")
            file("task.html")
          }
          dir("task2") {
            file("Buzz.kt")
            file("task.html")
          }
        }
      }
      dir("lesson1") {
        dir("task1") {
          file("FizzBuzz.kt")
          file("task.html")
        }
      }
    }
  }

  fun `test creation of course with sections and without top level lessons in CC mode`() {
    courseWithFiles(courseMode = CCUtils.COURSE_MODE) {
      section {
        lesson {
          eduTask {
            taskFile("Fizz.kt")
          }
          eduTask {
            taskFile("Buzz.kt")
          }
        }
        lesson {
          eduTask {
            taskFile("FizzBuzz.kt")
          }
        }
      }
    }

    checkFileTree {
      dir("section1") {
        dir("lesson1") {
          dir("task1") {
            file("Fizz.kt")
            file("task.html")
          }
          dir("task2") {
            file("Buzz.kt")
            file("task.html")
          }
        }
        dir("lesson2") {
          dir("task1") {
            file("FizzBuzz.kt")
            file("task.html")
          }
        }
      }
    }
  }

  fun `test empty course creation`() {
    courseWithFiles(courseMode = CCUtils.COURSE_MODE) {}

    checkFileTree {
      dir("lesson1") {
        dir("task1") {
          file("Task.txt")
          dir("tests") {
            file("Tests.txt")
          }
          file("task.html")
        }
      }
    }
  }

  fun `test placeholder content`() {
    courseWithFiles {
      lesson {
        eduTask {
          taskFile("Fizz.kt", "fun foo(): String = <p>TODO()</p>") {
            placeholder(0, "\"Foo\"")
          }
        }
        eduTask {
          taskFile("Buzz.kt", "fun bar(): String = <p>TODO()</p>") {
            placeholder(0, "\"Bar\"")
          }
        }
      }
      lesson {
        eduTask {
          taskFile("FizzBuzz.kt", """fun fooBar(): String = <p>""</p> + <p>""</p>""") {
            placeholder(0, "\"Foo\"")
            placeholder(1, "\"Bar\"")
          }
        }
      }
    }

    checkFileTree {
      dir("lesson1") {
        dir("task1") {
          file("Fizz.kt", code = "fun foo(): String = TODO()")
          file("task.html")
        }
        dir("task2") {
          file("Buzz.kt", code = "fun bar(): String = TODO()")
          file("task.html")
        }
      }
      dir("lesson2") {
        dir("task1") {
          file("FizzBuzz.kt", code = "fun fooBar(): String = \"\" + \"\"")
          file("task.html")
        }
      }
    }
  }

  fun `test placeholder content in CC mode`() {
    courseWithFiles(courseMode = CCUtils.COURSE_MODE) {
      lesson {
        eduTask {
          taskFile("Fizz.kt", "fun foo(): String = <p>TODO()</p>") {
            placeholder(0, "\"Foo\"")
          }
        }
        eduTask {
          taskFile("Buzz.kt", "fun bar(): String = <p>TODO()</p>") {
            placeholder(0, "\"Bar\"")
          }
        }
      }
      lesson {
        eduTask {
          taskFile("FizzBuzz.kt", """fun fooBar(): String = <p>""</p> + <p>""</p>""") {
            placeholder(0, "\"Foo\"")
            placeholder(1, "\"Bar\"")
          }
        }
      }
    }

    checkFileTree {
      dir("lesson1") {
        dir("task1") {
          file("Fizz.kt", code = "fun foo(): String = \"Foo\"")
          file("task.html")
        }
        dir("task2") {
          file("Buzz.kt", code = "fun bar(): String = \"Bar\"")
          file("task.html")
        }
      }
      dir("lesson2") {
        dir("task1") {
          file("FizzBuzz.kt", code = "fun fooBar(): String = \"Foo\" + \"Bar\"")
          file("task.html")
        }
      }
    }
  }

  fun `test placeholder content in CC mode with sections`() {
    courseWithFiles(courseMode = CCUtils.COURSE_MODE) {
      section {
        lesson {
          eduTask {
            taskFile("Fizz.kt", "fun foo(): String = <p>TODO()</p>") {
              placeholder(0, "\"Foo\"")
            }
          }
          eduTask {
            taskFile("Buzz.kt", "fun bar(): String = <p>TODO()</p>") {
              placeholder(0, "\"Bar\"")
            }
          }
        }
        lesson {
          eduTask {
            taskFile("FizzBuzz.kt", """fun fooBar(): String = <p>""</p> + <p>""</p>""") {
              placeholder(0, "\"Foo\"")
              placeholder(1, "\"Bar\"")
            }
          }
        }
      }
    }

    checkFileTree {
      dir("section1") {
        dir("lesson1") {
          dir("task1") {
            file("Fizz.kt", code = "fun foo(): String = \"Foo\"")
            file("task.html")
          }
          dir("task2") {
            file("Buzz.kt", code = "fun bar(): String = \"Bar\"")
            file("task.html")
          }
        }
        dir("lesson2") {
          dir("task1") {
            file("FizzBuzz.kt", code = "fun fooBar(): String = \"Foo\" + \"Bar\"")
            file("task.html")
          }
        }
      }
    }
  }

  fun `test invalid symbols`() {
    courseWithFiles {
      lesson("lesson/name") {
        eduTask("task?1")
        eduTask("task:2")
        eduTask("task;3")
        eduTask("task&4")
      }
    }

    if (SystemInfo.isWindows) {
      checkFileTree {
        dir("lesson name") {
          dir("task 1") {
            file("task.html")
          }
          dir("task 2") {
            file("task.html")
          }
          dir("task 3") {
            file("task.html")
          }
          dir("task 4") {
            file("task.html")
          }
        }
      }
    }
    else {
      checkFileTree {
        dir("lesson name") {
          dir("task?1") {
            file("task.html")
          }
          dir("task 2") {
            file("task.html")
          }
          dir("task;3") {
            file("task.html")
          }
          dir("task&4") {
            file("task.html")
          }
        }
      }
    }
  }

  private fun checkFileTree(block: FileTreeBuilder.() -> Unit) {
    fileTree(block).assertEquals(LightPlatformTestCase.getSourceRoot(), myFixture)
  }
}
