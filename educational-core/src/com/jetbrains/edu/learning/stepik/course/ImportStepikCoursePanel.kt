package com.jetbrains.edu.learning.stepik.course

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.ui.HyperlinkLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.jetbrains.edu.learning.EduLogInListener
import com.jetbrains.edu.learning.EduSettings
import com.jetbrains.edu.learning.EduUtils
import com.jetbrains.edu.learning.statistics.EduCounterUsageCollector
import com.jetbrains.edu.learning.stepik.StepikAuthorizer
import com.jetbrains.edu.learning.stepik.StepikNames
import com.jetbrains.edu.learning.ui.EduColors
import org.apache.commons.lang.math.NumberUtils.isDigits
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class ImportStepikCoursePanel(private val parent: Disposable) {
  private val courseLinkTextField = JTextField()
  val panel: JPanel
  private val helpLabel = JLabel("https://stepik.org/course/*")
  private var errorLabel = HyperlinkLabel()
  private var validationListener: ValidationListener? = null

  val courseLink: String
    get() = courseLinkTextField.text

  val preferredFocusedComponent: JComponent?
    get() = courseLinkTextField

  init {
    helpLabel.foreground = UIUtil.getLabelDisabledForeground()
    helpLabel.font = UIUtil.getLabelFont()
    errorLabel.setHyperlinkText("", "Log in", " to Stepik to import course")
    errorLabel.foreground = EduColors.errorTextForeground
    errorLabel.isVisible = false

    val courseLink = JLabel("Course link:")
    panel = JPanel(BorderLayout())
    val nestedPanel = JPanel()
    val layout = GroupLayout(nestedPanel)
    layout.autoCreateGaps = true
    nestedPanel.layout = layout
    layout.setHorizontalGroup(
      layout.createSequentialGroup()
        .addComponent(courseLink)
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(courseLinkTextField)
                    .addComponent(helpLabel))
    )
    layout.setVerticalGroup(
      layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(courseLink)
                    .addComponent(courseLinkTextField))
        .addComponent(helpLabel)
    )
    panel.add(nestedPanel, BorderLayout.NORTH)
    panel.add(errorLabel, BorderLayout.SOUTH)
    panel.preferredSize = JBUI.size(Dimension(400, 80))
    panel.minimumSize = panel.preferredSize
    addErrorStateListener()
  }

  fun setValidationListener(validationListener: ValidationListener?) {
    this.validationListener = validationListener
    doValidation()
  }

  private fun addErrorStateListener() {
    errorLabel.addHyperlinkListener {
      if (!EduSettings.isLoggedIn()) {
        addLoginListener()
        StepikAuthorizer.doAuthorize { EduUtils.showOAuthDialog() }
        EduCounterUsageCollector.loggedIn(StepikNames.STEPIK, EduCounterUsageCollector.AuthorizationPlace.START_COURSE_DIALOG)
      }
    }
  }

  private fun addLoginListener() {
    val busConnection = ApplicationManager.getApplication().messageBus.connect(parent)
    busConnection.subscribe(EduSettings.SETTINGS_CHANGED, object : EduLogInListener {
      override fun userLoggedIn() {
        busConnection.disconnect()
        ApplicationManager.getApplication().invokeLater({ doValidation() }, ModalityState.any())
      }

      override fun userLoggedOut() {}
    })
  }

  private fun doValidation() {
    val isLoggedIn = EduSettings.isLoggedIn()
    errorLabel.isVisible = !isLoggedIn
    if (isLoggedIn) {
      // If user is logged - make panel smaller without `Log In` message
      setPanelSize(Dimension(400, 50))
    }
    validationListener?.onLoggedIn(isLoggedIn)
  }

  private fun setPanelSize(dimension: Dimension, isMinimumSizeEqualsPreferred: Boolean = true) {
    panel.preferredSize = JBUI.size(dimension)
    if (isMinimumSizeEqualsPreferred) {
      panel.minimumSize = panel.preferredSize
    }
  }

  fun validate(): Boolean {
    val text = courseLinkTextField.text
    return text.isNotEmpty() && (isDigits(text) || isValidStepikLink(text))
  }

  private fun isValidStepikLink(text: String): Boolean {
    return StepikCourseConnector.getCourseIdFromLink(text) != -1
  }

  interface ValidationListener {
    fun onLoggedIn(isLoggedIn: Boolean)
  }
}