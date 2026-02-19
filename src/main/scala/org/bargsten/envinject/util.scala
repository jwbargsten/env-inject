package org.bargsten.envinject

object util {
  extension [L, R](e: Either[L, R])
    def widen[L2 >: L]: Either[L2, R] = e.asInstanceOf[Either[L2, R]]

  extension (xs: Seq[String])
    def spacesep(): String = xs.mkString(" ")
    def commasep(): String = xs.mkString(", ")

  extension (xs: Set[String])
    def spacesep(): String = xs.mkString(" ")
    def commasep(): String = xs.mkString(", ")
}
