package org.bargsten.envinject

import munit.FunSuite

class ShlexTest extends FunSuite:

    test("shell escape") {
      val script = "echo 'test'"
      val escaped = shlex.quote(script)
      assertEquals(escaped, "'echo '\"'\"'test'\"'\"''")
    }
    test("nested shell escape") {
      val password = "s3'cr3t!"
      val cmd = s"echo ${shlex.quote(password)}"
      val shCmd = s"sh -c ${shlex.quote(cmd)}"

      // yes, it looks awful, but it's correct
      assertEquals(shCmd, """sh -c 'echo '"'"'s3'"'"'"'"'"'"'"'"'cr3t!'"'"''""")
    }

