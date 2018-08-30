package com.jetbrains.edu.coursecreator.stepik

import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.edu.coursecreator.CCUtils
import com.jetbrains.edu.coursecreator.actions.stepik.CCPushCourse
import com.jetbrains.edu.coursecreator.actions.stepik.CCPushLesson
import com.jetbrains.edu.coursecreator.actions.stepik.CCPushSection
import com.jetbrains.edu.learning.EduUtils
import com.jetbrains.edu.learning.StudyTaskManager
import com.jetbrains.edu.learning.course
import com.jetbrains.edu.learning.courseFormat.Course
import com.jetbrains.edu.learning.courseFormat.ItemContainer
import com.jetbrains.edu.learning.courseFormat.Lesson
import com.jetbrains.edu.learning.courseFormat.Section
import com.jetbrains.edu.learning.courseGeneration.GeneratorUtils
import com.jetbrains.edu.learning.stepik.StepikConnector
import com.jetbrains.edu.learning.stepik.StepikTestCase
import com.jetbrains.edu.learning.stepik.courseFormat.StepikCourse
import com.jetbrains.edu.learning.stepik.courseFormat.ext.id
import junit.framework.TestCase


open class StepikIntegrationTest : StepikTestCase() {

  fun `test upload course`() {
    val course = courseWithFiles(courseMode = CCUtils.COURSE_MODE) {
      lesson("lesson1") {
        eduTask {
          taskFile("fizz.kt")
        }
      }
    }
    StudyTaskManager.getInstance(project).course = course

    CCStepikConnector.postCourseWithProgress(project, StudyTaskManager.getInstance(project).course!!)
    checkCourseUploaded(StudyTaskManager.getInstance(project).course as StepikCourse)
  }

  fun `test upload course with top level lesson`() {
    val courseToPost = courseWithFiles(courseMode = CCUtils.COURSE_MODE) {
      lesson("lesson1") {
        eduTask {
          taskFile("fizz.kt")
        }
      }
    }
    StudyTaskManager.getInstance(project).course = courseToPost

    CCPushCourse.doPush(project, courseToPost)

    val localCourse = StudyTaskManager.getInstance(project).course as StepikCourse
    val courseFromStepik = StepikConnector.getCourseInfo(user, localCourse.id, true)
    checkTopLevelLessons(courseFromStepik, localCourse)
  }

  fun `test upload course with top level lessons`() {
    val courseToPost = courseWithFiles(courseMode = CCUtils.COURSE_MODE) {
      lesson("lesson1") {
        eduTask {
          taskFile("fizz.kt")
        }
      }
      lesson("lesson2") {
        eduTask {
          taskFile("fizz.kt")
        }
      }
    }
    StudyTaskManager.getInstance(project).course = courseToPost

    CCPushCourse.doPush(project, courseToPost)

    val localCourse = StudyTaskManager.getInstance(project).course as StepikCourse
    val courseFromStepik = StepikConnector.getCourseInfo(user, localCourse.id, true)
    checkTopLevelLessons(courseFromStepik, localCourse)
  }

  fun `test post top level lesson`() {
    val courseToPost = courseWithFiles {
      lesson("lesson1") {
        eduTask {
          taskFile("fizz.kt")
        }
      }
      lesson("lesson2") {
        eduTask {
          taskFile("fizz.kt")
        }
      }
    }
    StudyTaskManager.getInstance(project).course = courseToPost

    CCStepikConnector.postCourseWithProgress(project, courseToPost)

    val localCourse = StudyTaskManager.getInstance(project).course as StepikCourse
    val newLesson = addNewLesson("lesson3", 3, localCourse, localCourse, EduUtils.getCourseDir(project))
    CCPushLesson.doPush(newLesson, project, localCourse)

    val courseFromStepik = StepikConnector.getCourseInfo(user, localCourse.id, true)
    checkTopLevelLessons(courseFromStepik, localCourse)
  }

  fun `test rearrange lessons`() {
    val courseToPost = courseWithFiles(courseMode = CCUtils.COURSE_MODE) {
      lesson("lesson1") {
        eduTask {
          taskFile("fizz.kt")
        }
      }
      lesson("lesson2") {
        eduTask {
          taskFile("fizz.kt")
        }
      }
    }
    StudyTaskManager.getInstance(project).course = courseToPost

    CCPushCourse.doPush(project, courseToPost)

    val localCourse = StudyTaskManager.getInstance(project).course as StepikCourse
    val lesson1 = localCourse.getLesson("lesson1")!!
    val lesson2 = localCourse.getLesson("lesson2")!!
    lesson1.index = 2
    lesson2.index = 1
    localCourse.sortItems()
    CCPushLesson.doPush(lesson1, project, localCourse)
    CCPushLesson.doPush(lesson2, project, localCourse)

    val courseFromStepik = StepikConnector.getCourseInfo(user, localCourse.id, true)
    checkTopLevelLessons(courseFromStepik, localCourse)
  }

  fun `test upload course with section`() {
    val courseToPost = courseWithFiles {
      section("section1") {
        lesson("lesson1")
        {
          eduTask {
            taskFile("fizz.kt")
          }
        }
      }
    }
    StudyTaskManager.getInstance(project).course = courseToPost

    CCPushCourse.doPush(project, courseToPost)

    val localCourse = StudyTaskManager.getInstance(project).course as StepikCourse
    val courseFromStepik = StepikConnector.getCourseInfo(user, localCourse.id, true)
    checkSections(courseFromStepik, localCourse)
  }

  fun `test rearrange sections`() {
    val courseToPost = courseWithFiles {
      section("section1") {
        lesson("lesson1")
        {
          eduTask {
            taskFile("fizz.kt")
          }
        }
      }
      section("section2") {
        lesson("lesson1")
        {
          eduTask {
            taskFile("fizz.kt")
          }
        }
      }
    }
    StudyTaskManager.getInstance(project).course = courseToPost

    CCPushCourse.doPush(project, courseToPost)

    val localCourse = StudyTaskManager.getInstance(project).course as StepikCourse
    val section1 = localCourse.getSection("section1")
    val section2 = localCourse.getSection("section2")
    section1!!.index = 2
    section2!!.index = 1
    localCourse.sortItems()
    CCPushSection.doPush(project, section1, localCourse)
    CCPushSection.doPush(project, section2, localCourse)

    val courseFromStepik = StepikConnector.getCourseInfo(user, localCourse.id, true)
    checkSections(courseFromStepik, localCourse)
  }

  fun `test post lesson into section`() {
    val courseToPost = courseWithFiles {
      section("section1") {
        lesson("lesson1") {
          eduTask {
            taskFile("fizz.kt")
          }
        }
        lesson("lesson2") {
          eduTask {
            taskFile("fizz.kt")
          }
        }
      }
    }
    courseToPost.init(null, null, false)
    StudyTaskManager.getInstance(project).course = courseToPost

    CCStepikConnector.postCourseWithProgress(project, courseToPost)

    val localCourse = StudyTaskManager.getInstance(project).course!! as StepikCourse

    val section = localCourse.getSection("section1")
    val newLesson = addNewLesson("lesson3", 3, localCourse, section!!, EduUtils.getCourseDir(project))
    CCPushLesson.doPush(newLesson, project, StudyTaskManager.getInstance(project).course as StepikCourse?)

    val courseFromStepik = StepikConnector.getCourseInfo(user, localCourse.id, true)
    checkSections(courseFromStepik, localCourse)
  }

  fun `test rearrange lessons inside section`() {
    val courseToPost = courseWithFiles {
      section("section1") {
        lesson("lesson1") {
          eduTask {
            taskFile("fizz.kt")
          }
        }
        lesson("lesson2") {
          eduTask {
            taskFile("fizz.kt")
          }
        }
      }
    }
    courseToPost.init(null, null, false)
    StudyTaskManager.getInstance(project).course = courseToPost

    CCStepikConnector.postCourseWithProgress(project, courseToPost)

    val localCourse = StudyTaskManager.getInstance(project).course!! as StepikCourse

    val section = localCourse.getSection("section1")
    val newLesson = addNewLesson("lesson3", 3, localCourse, section!!, EduUtils.getCourseDir(project))

    CCPushLesson.doPush(newLesson, project, StudyTaskManager.getInstance(project).course as StepikCourse?)

    val courseFromStepik = StepikConnector.getCourseInfo(user, localCourse.id, true)
    checkSections(courseFromStepik, localCourse)
  }

  fun `test post new section`() {
    val courseToPost = courseWithFiles {
      section("section1") {
        lesson("lesson1")
        {
          eduTask {
            taskFile("fizz.kt")
          }
        }
      }
    }
    StudyTaskManager.getInstance(project).course = courseToPost
    CCPushCourse.doPush(project, courseToPost)

    val localCourse = StudyTaskManager.getInstance(project).course as StepikCourse

    val newSection = addNewSection("section2", 2, localCourse, EduUtils.getCourseDir(project))
    CCPushSection.doPush(project, newSection, localCourse)

    val courseFromStepik = StepikConnector.getCourseInfo(user, localCourse.id, true)
    checkSections(courseFromStepik, localCourse)
  }

  fun `test top-level lessons wrapped into section`() {
    val courseToPost = courseWithFiles {
      lesson("lesson1")
      {
        eduTask {
          taskFile("fizz.kt")
        }
      }
    }

    StudyTaskManager.getInstance(project).course = courseToPost
    courseToPost.init(null, null, false)
    CCPushCourse.doPush(project, courseToPost)
    val localCourse = StudyTaskManager.getInstance(project).course
    CCUtils.wrapIntoSection(project, localCourse!!, localCourse.lessons, "section1")
    CCPushCourse.doPush(project, localCourse)

    val courseFromStepik = StepikConnector.getCourseInfo(user, (localCourse as StepikCourse).id, true)
    checkSections(courseFromStepik, localCourse)
  }

  private fun checkSections(courseFromStepik: Course?, localCourse: Course) {
    assertNotNull("Uploaded courses not found among courses available to instructor", courseFromStepik)

    StepikConnector.fillItems(courseFromStepik as StepikCourse)
    TestCase.assertTrue("Sections number mismatch. Expected: ${localCourse.sections.size}. Actual: ${courseFromStepik.sections.size}",
                        localCourse.sections.size == courseFromStepik.sections.size)
    localCourse.sections.forEachIndexed{ index, section ->
      val sectionFromStepik = courseFromStepik.sections[index]
      TestCase.assertTrue("Sections name mismatch. Expected: ${section.name}. Actual: ${sectionFromStepik.name}",
                          section.name == sectionFromStepik.name)
      TestCase.assertTrue("Lessons number mismatch. Expected: ${section.lessons.size}. Actual: ${sectionFromStepik.lessons.size}",
                          section.lessons.size == sectionFromStepik.lessons.size)
      section.lessons.forEachIndexed { lessonIndex, lesson ->
        TestCase.assertTrue("Lessons name mismatch. Expected: ${lesson.name}. Actual: ${sectionFromStepik.lessons[lessonIndex].name}",
                            lesson.name == sectionFromStepik.lessons[lessonIndex].name)
      }
    }
  }

  private fun checkTopLevelLessons(courseFromStepik: Course?, localCourse: RemoteCourse) {
    assertNotNull("Uploaded courses not found among courses available to instructor", courseFromStepik)
    TestCase.assertTrue("Course with top-level lessons should have only one section, but has: ${localCourse.stepikRemoteInfo.sectionIds.size}",
                        localCourse.stepikRemoteInfo.sectionIds.size == 1)
    TestCase.assertTrue("Top-level lessons section id mismatch", localCourse.stepikRemoteInfo.sectionIds[0] ==
      (courseFromStepik as StepikCourse).stepikRemoteInfo.sectionIds[0])
    val section = StepikConnector.getSection(courseFromStepik.stepikRemoteInfo.sectionIds[0])
    TestCase.assertTrue("Section name mismatch. Expected: ${localCourse.name}.\n Actual: ${section.name}",
                        section.name == localCourse.name)

    val unitIds = section.stepikRemoteInfo.units.map { unit -> unit.toString() }
    val lessonsFromUnits = StepikConnector.getLessonsFromUnits(courseFromStepik, unitIds.toTypedArray(), false)

    TestCase.assertTrue("Lessons number mismatch. Expected: ${localCourse.lessons.size}. Actual: ${lessonsFromUnits.size}",
                        lessonsFromUnits.size == localCourse.lessons.size)
    localCourse.lessons.forEachIndexed { index, lesson ->
      TestCase.assertTrue("Lessons name mismatch. Expected: ${lesson.name}. Actual: ${lessonsFromUnits[index].name}",
                          lesson.name == lessonsFromUnits[index].name)
    }
  }
}

internal fun addNewLesson(name: String,
                          index: Int,
                          courseToInit: Course,
                          parent: ItemContainer,
                          virtualFile: VirtualFile): Lesson {
  val course = course {
    lesson(name) {
      eduTask {
        taskFile("fizz.kt")
      }
    }
  }

  val newLesson = course.getLesson(name)!!
  newLesson.index = index
  newLesson.init(courseToInit, parent, false)
  parent.addLesson(newLesson)
  if (parent is Course) {
    GeneratorUtils.createLesson(newLesson, virtualFile)
  }
  else {
    val sectionDir = virtualFile.findChild(parent.name)
    GeneratorUtils.createLesson(newLesson, sectionDir!!)
  }

  return newLesson
}

internal fun addNewSection(name: String,
                           index: Int,
                           courseToInit: Course,
                           virtualFile: VirtualFile): Section {
  val course = course {
    section(name) {
      lesson("lesson1") {
        eduTask {
          taskFile("fizz.kt")
        }
      }
    }
  }

  val newSection = course.getSection(name)!!
  newSection.index = index
  GeneratorUtils.createSection(newSection, virtualFile)

  courseToInit.addSection(newSection)
  courseToInit.init(null, null, false)
  return newSection
}