package com.github.jwbargsten.envinject

import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configurations.{JavaParameters, RunConfigurationBase, RunnerSettings}
import com.intellij.notification.{NotificationGroupManager, NotificationType}
import com.intellij.openapi.diagnostic.Logger

import java.nio.file.{Files, Path}

class EnvInjectRunConfigurationExtension extends RunConfigurationExtension:

  private val logger = Logger.getInstance(getClass)

  override def getSerializationId: String = "com.github.jwbargsten.envinject"

  override def isApplicableFor(configuration: RunConfigurationBase[?]): Boolean = true

  override def isEnabledFor(
      applicableConfiguration: RunConfigurationBase[?],
      runnerSettings: RunnerSettings | Null
  ): Boolean =
    val project = applicableConfiguration.getProject
    val basePath = project.getBasePath
    basePath != null && Files.exists(Path.of(basePath, ".env-inject"))

  override def updateJavaParameters[T <: RunConfigurationBase[?]](
      configuration: T,
      params: JavaParameters,
      runnerSettings: RunnerSettings | Null
  ): Unit =
    val project = configuration.getProject
    val workingDir = project.getBasePath
    if workingDir == null then return

    val cmds = EnvInjectConfig.readCommand(project)
    if cmds.isEmpty then logger.info("env-inject: no command found in .env-inject")
    val mergedEnv = cmds
      .map { cmd =>
        val env = ExternalCommandRunner.run(cmd, workingDir)
        if env.isEmpty then
          showNotification(
            configuration,
            s"env-inject: command produced no output or failed: $cmd"
          )

        env
      }
      .reduce(_ ++ _)
    logger.info(s"env-inject: injecting ${mergedEnv.size} environment variables")
    mergedEnv.foreach { (k, v) => params.addEnv(k, v) }

  private def showNotification(
      configuration: RunConfigurationBase[?],
      message: String
  ): Unit =
    val group = NotificationGroupManager.getInstance().getNotificationGroup("env-inject")
    if group != null then
      group
        .createNotification(message, NotificationType.WARNING)
        .notify(configuration.getProject)
    else logger.warn(message)
