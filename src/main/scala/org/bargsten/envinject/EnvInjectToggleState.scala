package org.bargsten.envinject

import com.intellij.openapi.components.{PersistentStateComponent, Service, State, Storage}
import com.intellij.openapi.project.Project

@State(name = "EnvInjectToggleState", storages = Array(new Storage("env-inject.xml")))
@Service(Array(Service.Level.PROJECT))
final class EnvInjectToggleState extends PersistentStateComponent[EnvInjectToggleState.State]:

  private var state = new EnvInjectToggleState.State()

  def isEnabled: Boolean = state.enabled

  def setEnabled(value: Boolean): Unit = state.enabled = value

  override def getState: EnvInjectToggleState.State = state

  override def loadState(state: EnvInjectToggleState.State): Unit = this.state = state

object EnvInjectToggleState:
  def getInstance(project: Project): EnvInjectToggleState =
    project.getService(classOf[EnvInjectToggleState])

  class State:
    var enabled: Boolean = false
