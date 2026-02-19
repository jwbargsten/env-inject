package org.bargsten.envinject

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ExecutionListener
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration

import java.util.concurrent.ConcurrentHashMap

class EnvInjectExecutionListener extends ExecutionListener:

  private val logger = Logger.getInstance(getClass)
  private val savedEnvs = new ConcurrentHashMap[Long, java.util.Map[String, String]]()

  override def processStartScheduled(executorId: String, env: ExecutionEnvironment): Unit =
    env.getRunProfile match
      case config: ExternalSystemRunConfiguration
          if EnvInjectToggleState.getInstance(env.getProject).isEnabled
            && EnvInjectResolver(env.getProject).configExists() =>
        val settings = config.getSettings
        val originalEnv = new java.util.HashMap(settings.getEnv)
        savedEnvs.put(env.getExecutionId, originalEnv)
        logger.info("env-inject: saved original env vars for external system config")
      case _ => ()

  override def processTerminated(
      executorId: String,
      env: ExecutionEnvironment,
      handler: ProcessHandler,
      exitCode: Int
  ): Unit =
    restoreEnv(env)

  override def processNotStarted(executorId: String, env: ExecutionEnvironment): Unit =
    restoreEnv(env)

  private def restoreEnv(env: ExecutionEnvironment): Unit =
    val original = savedEnvs.remove(env.getExecutionId)
    if original != null then
      env.getRunProfile match
        case config: ExternalSystemRunConfiguration =>
          config.getSettings.setEnv(original)
          logger.info("env-inject: restored original env vars for external system config")
        case _ => ()
