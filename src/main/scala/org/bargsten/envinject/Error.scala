package org.bargsten.envinject

import scala.util.control.NoStackTrace

object Error {

  sealed trait EnvInjectError

  /*
    extends Exception, NoStackTrace {
    def msg: String

    def cause: Option[Throwable] = None

    override def getCause: Throwable = cause.orNull

    override def getMessage: String = msg
  }

   */

}
