package com.jetbrains.edu.learning.courseFormat.remote

import com.intellij.openapi.project.Project
import com.jetbrains.edu.learning.EduNames
import com.jetbrains.edu.learning.courseFormat.Course
import com.jetbrains.edu.learning.courseFormat.Tag
import com.jetbrains.edu.learning.stepik.StepikAdaptiveReactionsPanel
import java.util.*
import javax.swing.JPanel

interface RemoteInfo {

  fun getTags(): List<Tag> = listOf()
  fun isCourseValid(course: Course): Boolean = true
  fun getAdditionalDescriptionPanel(project: Project): JPanel? = null
}

class LocalInfo : RemoteInfo

class StepikRemoteInfo : RemoteInfo {
  // publish to stepik
  var isPublic: Boolean = false
  var isAdaptive = false
  var isIdeaCompatible = true
  var id: Int = 0
  var updateDate = Date(0)
  var sectionIds: List<Int> = ArrayList() // in CC mode is used to store top-level lessons section id
  var instructors: List<Int> = ArrayList()
  var additionalMaterialsUpdateDate = Date(0)

  // do not publish to stepik
  var loadSolutions = true // disabled for reset courses

  override fun isCourseValid(course: Course): Boolean {
    if (!isAdaptive) return true
    val lessons = course.lessons
    if (lessons.size == 1) {
      return !lessons[0].getTaskList().isEmpty()
    }
    return true
  }

  override fun getTags(): List<Tag> =
    if (isAdaptive) {
      listOf(Tag(EduNames.ADAPTIVE))
    }
    else listOf()

  override fun getAdditionalDescriptionPanel(project: Project): JPanel? = if (isAdaptive) StepikAdaptiveReactionsPanel(project) else null
}
