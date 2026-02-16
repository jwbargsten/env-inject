package com.github.jwbargsten.envinject

import com.intellij.openapi.diagnostic.Logger

import java.io.File
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.*
import scala.util.{Try, Using}

object ExternalCommandRunner:

  private val logger = Logger.getInstance(getClass)
  private val TimeoutSeconds = 10L

  def run(command: String, workingDir: String): Map[String, String] =
    Try {
      val parts = command.split("\\s+").toList
      val pb = new ProcessBuilder(parts.asJava)
      pb.directory(new File(workingDir))
      pb.redirectErrorStream(false)

      val process = pb.start()

      val stdout = Using.resource(process.getInputStream) { is =>
        new String(is.readAllBytes())
      }

      val finished = process.waitFor(TimeoutSeconds, TimeUnit.SECONDS)
      if !finished then
        process.destroyForcibly()
        logger.warn(s"env-inject: command timed out after ${TimeoutSeconds}s: $command")
        Map.empty
      else
        val exitCode = process.exitValue()
        if exitCode != 0 then
          logger.warn(s"env-inject: command exited with code $exitCode: $command")
          Map.empty
        else parseOutput(stdout)
    }.recover { case e: Exception =>
      logger.warn(s"env-inject: failed to run command: $command", e)
      Map.empty
    }.get

  private def parseOutput(output: String): Map[String, String] =
    output.linesIterator
      .map(_.trim)
      .filter(line => line.nonEmpty && !line.startsWith("#"))
      .flatMap { line =>
        line.indexOf('=') match
          case -1 => None
          case i  => Some(line.substring(0, i) -> line.substring(i + 1))
      }
      .toMap
