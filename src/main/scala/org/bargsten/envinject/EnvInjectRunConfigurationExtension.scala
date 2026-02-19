package org.bargsten.envinject

import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configurations.{JavaParameters, RunConfigurationBase, RunnerSettings}
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration

import java.nio.file.{Files, Path}

/*
RunConfiguration                          ← base interface
  └── ExternalSystemRunConfiguration      ← for external tool tasks (e.g. "gradle run", "sbt test")
 */
class EnvInjectRunConfigurationExtension extends RunConfigurationExtension:

  override def getSerializationId: String = "org.bargsten.envinject"

  override def isApplicableFor(configuration: RunConfigurationBase[?]): Boolean = true

  override def isEnabledFor(
      applicableConfiguration: RunConfigurationBase[?],
      runnerSettings: RunnerSettings | Null
  ): Boolean =
    val project = applicableConfiguration.getProject
    EnvInjectToggleState.getInstance(project).isEnabled && EnvInjectResolver(project).configExists()

  override def updateJavaParameters[T <: RunConfigurationBase[?]](
      configuration: T,
      params: JavaParameters,
      runnerSettings: RunnerSettings | Null
  ): Unit =
    val env = EnvInjectResolver(configuration.getProject).resolve()
    env.foreach { (k, v) => params.addEnv(k, v) }

    configuration match
      case esrc: ExternalSystemRunConfiguration =>
        val settings = esrc.getSettings
        val merged = new java.util.HashMap(settings.getEnv)
        env.foreach { (k, v) => merged.put(k, v) }
        settings.setEnv(merged)
      case _ => ()
