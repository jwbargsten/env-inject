package org.bargsten.envinject

import com.intellij.openapi.project.Project
import org.bargsten.envinject.EnvInjectConfig.FileName

import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

case class CmdSpec(cmd: Seq[String], wd: String)

class EnvInjectConfig(project: Project):
  private val workingDir: Option[String] = Option(project.getBasePath)
  private val configFile = workingDir.map(wd => (wd, Path.of(wd, FileName))).filter(wdc => Files.exists(wdc._2))

  def exists(): Boolean = configFile.isDefined

  def readCommands(): Seq[Either[shlex.TokenizeError, CmdSpec]] =
    configFile match {
      case Some((wd, file)) =>
        Files
          .readAllLines(file)
          .asScala
          .map(_.trim)
          .filter(line => line.nonEmpty && !line.startsWith("#"))
          .map(shlex.split(_))
          .map(_.map(CmdSpec(_, wd)))
          .toList
      case _ => List.empty
    }

object EnvInjectConfig:
  val FileName = ".env-inject"
