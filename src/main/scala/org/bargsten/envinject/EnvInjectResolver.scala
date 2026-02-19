package org.bargsten.envinject

import com.intellij.notification.{NotificationGroupManager, NotificationType}
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.bargsten.envinject.util.widen
import util.{commasep, spacesep}

import scala.concurrent.ExecutionContext

class EnvInjectResolver(project: Project):
  private given ec: ExecutionContext = ExecutionContext.Implicits.global

  private val logger = Logger.getInstance(getClass)
  private val config = EnvInjectConfig(project)

  def configExists(): Boolean = config.exists()

  def resolve(): Map[String, String] =
    if !config.exists() then return Map.empty

    val cmds = config.readCommands()
    if cmds.isEmpty then
      showInfo("no command found in .env-inject")
      Map.empty
    else
      val mergedEnv = cmds
        .map(_.widen[shlex.TokenizeError | CommandExecutionError])
        .map(_.flatMap(ExternalCommandRunner.run(_)))
        .map {
          case Left(err: shlex.TokenizeError) =>
            showWarning(s"could not parse cmd: ${err.cmd}")
            Map.empty
          case Left(err: CommandExecutionError) =>
            logger.warn(s"failed with ex env-inject: ${err.cmd}: ${err.stderr}", err.cause.orNull)
            if err.stderr.isBlank then showWarning(s"could not execute cmd: ${err.cmd.cmd.spacesep()}")
            else showWarning(s"could not execute cmd: ${err.cmd.cmd.spacesep()}, stderr: ${err.stderr}")
            Map.empty
          case Right(value) => value
        }
        .reduce(_ ++ _)

      val keys = mergedEnv.keys.toSet
      if keys.nonEmpty then showInfo(s"injected ${keys.commasep()}")
      mergedEnv

  private def showWarning(message: String): Unit =
    val m = s"env-inject: $message"
    val group = NotificationGroupManager.getInstance().getNotificationGroup("env-inject.warnings")
    if group != null then
      group
        .createNotification(m, NotificationType.WARNING)
        .notify(project)
    else logger.warn(m)

  private def showInfo(message: String): Unit =
    val m = s"env-inject: $message"
    val group = NotificationGroupManager.getInstance().getNotificationGroup("env-inject.info")
    if group != null then
      group
        .createNotification(m, NotificationType.INFORMATION)
        .notify(project)
    else logger.info(m)
