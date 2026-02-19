package org.bargsten.envinject

import com.intellij.openapi.diagnostic.Logger

import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, ExecutionContext, Future, blocking}
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try, Using}
import scala.concurrent.duration.*
import scala.util.control.NoStackTrace

final case class CommandExecutionError(cmd: CmdSpec, stderr: String = "", cause: Option[Throwable] = None)
    extends Exception(s"could not execute $cmd", cause.orNull),
      NoStackTrace

object ExternalCommandRunner:
  private val logger = Logger.getInstance(getClass)
  private val Timeout = 7.seconds

  def run(cmdSpec: CmdSpec): Either[CommandExecutionError, Map[String, String]] = {
    val result = Try {
      val pb = new ProcessBuilder(cmdSpec.cmd.asJava)
      pb.directory(new File(cmdSpec.wd))
      pb.redirectErrorStream(false)

      val process = pb.start()

      val stdoutF = Future {
        Using.resource(process.getInputStream) { is =>
          new String(is.readAllBytes())
        }
      }
      val stderrF = Future {
        Using.resource(process.getErrorStream) { is =>
          new String(is.readAllBytes())
        }
      }

      val exitF = Future(blocking(process.waitFor()))
      val combinedF = for
        exit <- exitF
        stdout <- stdoutF
        stderr <- stderrF
      yield (stdout, stderr, exit)

      Try(Await.result(combinedF, Timeout)) match {
        case Success((stdout, stderr, exitValue)) if exitValue == 0 =>
          Right(parseOutput(stdout))
        case Success((stdout, stderr, exitValue)) =>
          process.destroyForcibly()
          Left(CommandExecutionError(cmdSpec, stderr))
        case Failure(ex) =>
          Try(process.getErrorStream.close())
          Try(process.getOutputStream.close())
          process.destroyForcibly()
          Left(CommandExecutionError(cmdSpec, cause = Some(ex)))
      }
    }

    result match
      case Success(v)  => v
      case Failure(ex) => Left(CommandExecutionError(cmdSpec, cause = Some(ex)))
  }

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
