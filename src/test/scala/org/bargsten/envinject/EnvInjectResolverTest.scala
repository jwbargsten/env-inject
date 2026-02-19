package org.bargsten.envinject

import munit.FunSuite
import org.bargsten.envinject.ExternalCommandRunner.run

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*

class EnvInjectResolverTest extends FunSuite {
  private given ExecutionContext = ExecutionContext.Implicits.global

  test("return env vars") {
    val res = run(CmdSpec.fromCmd("perl", "-E", """say "DREI=3""""), 10.seconds)
    assertEquals(res, Right(Map("DREI" -> "3")))
  }

  test("failure") {
    val res = run(CmdSpec.fromCmd("kaboom"), 10.seconds)
    assertMatches(res) {
      case Left(_) => true
    }
  }
}
