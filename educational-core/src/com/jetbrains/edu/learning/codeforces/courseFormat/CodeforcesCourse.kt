package com.jetbrains.edu.learning.codeforces.courseFormat

import com.jetbrains.edu.learning.codeforces.CodeforcesContestConnector.getContestURLFromID
import com.jetbrains.edu.learning.codeforces.CodeforcesNames
import com.jetbrains.edu.learning.codeforces.CodeforcesNames.CODEFORCES_COURSE_TYPE
import com.jetbrains.edu.learning.codeforces.ContestURLInfo
import com.jetbrains.edu.learning.courseFormat.CourseCompatibility
import com.jetbrains.edu.learning.courseFormat.EduCourse
import com.jetbrains.edu.learning.courseFormat.Lesson
import okhttp3.ResponseBody
import org.jsoup.Jsoup

class CodeforcesCourse : EduCourse {
  @Suppress("unused") //used for deserialization
  constructor()

  constructor(contestURLInfo: ContestURLInfo, html: ResponseBody) {
    id = contestURLInfo.id
    language = contestURLInfo.languageId
    languageCode = contestURLInfo.locale

    parseResponseToAddContent(html)
  }

  val contestUrl: String by lazy { getContestURLFromID(id) }
  val submissionUrl: String by lazy { "$contestUrl/submit?locale=${languageCode}" }

  override fun courseCompatibility(courseInfo: EduCourse): CourseCompatibility = CourseCompatibility.COMPATIBLE
  override fun getItemType(): String = CODEFORCES_COURSE_TYPE

  private fun parseResponseToAddContent(html: ResponseBody) {
    val doc = Jsoup.parse(html.string())
    name = doc.selectFirst(".caption").text()

    val problems = doc.select(".problem-statement")

    description = problems.joinToString("\n") {
      it.select("div.header").select("div.title").text()
    }

    val lesson = Lesson()
    lesson.name = CodeforcesNames.CODEFORCES_PROBLEMS
    lesson.course = this

    addLesson(lesson)
    problems.forEach { lesson.addTask(CodeforcesTask(it, lesson)) }
  }
}