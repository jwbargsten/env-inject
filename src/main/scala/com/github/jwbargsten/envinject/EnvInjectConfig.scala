package com.github.jwbargsten.envinject

import com.intellij.openapi.project.Project

import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

object EnvInjectConfig:

  private val FileName = ".env-inject"

  def readCommand(project: Project): List[String] =
    val basePath = project.getBasePath
    if basePath == null then return List.empty

    val configFile = Path.of(basePath, FileName)
    if !Files.exists(configFile) then return List.empty

    Files
      .readAllLines(configFile)
      .asScala
      .map(_.trim)
      .filter(line => line.nonEmpty && !line.startsWith("#"))
      .toList
