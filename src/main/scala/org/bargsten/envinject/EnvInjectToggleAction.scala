package org.bargsten.envinject

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class EnvInjectToggleAction extends ToggleAction:

  override def isSelected(e: AnActionEvent): Boolean =
    val project = e.getProject
    project != null && EnvInjectToggleState.getInstance(project).isEnabled

  override def setSelected(e: AnActionEvent, state: Boolean): Unit =
    val project = e.getProject
    if project != null then EnvInjectToggleState.getInstance(project).setEnabled(state)
