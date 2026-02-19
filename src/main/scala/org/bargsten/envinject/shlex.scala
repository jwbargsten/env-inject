package org.bargsten.envinject

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

object shlex {

  // taken from python's shlex module
  // https://docs.python.org/3/library/shlex.html
  // https://github.com/python/cpython/blob/3.12/Lib/shlex.py#L323
  private val unsafeCharRegex: Regex = "[^\\w@%+=:,./-]".r

  def quote(s: String): String =
    if s.isEmpty then "''"
    else if unsafeCharRegex.findFirstIn(s).isEmpty then s
    else
      /*
        use single quotes, and put single quotes into double quotes
        the string ab'c is then quoted as >>'ab'"'"'c'<<
       */
      "'" + s.replace("'", "'\"'\"'") + "'"

  sealed trait TokenizeError {
    def cmd: String
  }

  case class UnmatchedSingleQuote(position: Int, cmd: String) extends TokenizeError

  case class UnmatchedDoubleQuote(position: Int, cmd: String) extends TokenizeError

  case class TrailingEscape(position: Int, cmd: String) extends TokenizeError

  /**
   * Tokenizes a shell-style command string, following POSIX shell rules similar to Python's shlex.split().
   *
   * Supports:
   *   - Single quotes: preserve literal content, no escape processing
   *   - Double quotes: preserve content, but \\ \" \$ \` \newline are interpreted
   *   - Backslash escaping outside quotes
   *   - Whitespace splitting outside quotes
   *   - Comment stripping (# outside quotes)
   *
   * @param input
   * the command string to tokenize
   * @param comments
   * if true, treat unquoted # as a comment start (default: true)
   * @return
   * Either an error or the list of tokens
   */
  def split(input: String, comments: Boolean = true): Either[TokenizeError, Seq[String]] =
    val tokens = ListBuffer[String]()
    val current = new StringBuilder
    var i = 0
    val len = input.length
    var inToken = false

    while i < len do
      val c = input(i)
      c match
        case '\'' =>
          inToken = true
          val start = i
          i += 1
          var closed = false
          while i < len && !closed do
            if input(i) == '\'' then closed = true
            else current.append(input(i))
            i += 1
          if !closed then return Left(UnmatchedSingleQuote(start, input))

        case '"' =>
          inToken = true
          val start = i
          i += 1
          var closed = false
          while i < len && !closed do
            input(i) match
              case '"' =>
                closed = true
                i += 1
              case '\\' if i + 1 < len =>
                val next = input(i + 1)
                // inside double quotes, only these characters are special after backslash
                if "\\\"$`\n".contains(next) then
                  current.append(next)
                  i += 2
                else
                  current.append('\\')
                  current.append(next)
                  i += 2
              case '\\' =>
                return Left(TrailingEscape(i, input))
              case other =>
                current.append(other)
                i += 1
          if !closed then return Left(UnmatchedDoubleQuote(start, input))

        case '\\' =>
          if i + 1 < len then
            val next = input(i + 1)
            if next == '\n' then
              // line continuation, skip both
              i += 2
            else
              inToken = true
              current.append(next)
              i += 2
          else return Left(TrailingEscape(i, input))

        case '#' if comments && !inToken =>
          // rest of line is a comment
          i = len

        case w if w.isWhitespace =>
          if inToken then
            tokens += current.toString()
            current.clear()
            inToken = false
          i += 1

        case other =>
          inToken = true
          current.append(other)
          i += 1

    if inToken then tokens += current.toString()

    Right(tokens.toSeq)

}
