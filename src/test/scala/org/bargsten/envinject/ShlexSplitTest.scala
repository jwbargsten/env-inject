package org.bargsten.envinject

import munit.FunSuite

class ShlexSplitTest extends FunSuite:

  private def assertSplit(input: String, expected: Seq[String])(using munit.Location): Unit =
    assertEquals(shlex.split(input), Right(expected))

  // --- Basic splitting ---

  test("single token") {
    assertSplit("x", Seq("x"))
  }

  test("two tokens") {
    assertSplit("foo bar", Seq("foo", "bar"))
  }

  test("leading whitespace") {
    assertSplit(" foo bar", Seq("foo", "bar"))
  }

  test("trailing whitespace") {
    assertSplit(" foo bar ", Seq("foo", "bar"))
  }

  test("multiple whitespace") {
    assertSplit("foo   bar    bla     fasel", Seq("foo", "bar", "bla", "fasel"))
  }

  test("mixed whitespace widths") {
    assertSplit("x y  z              xxxx", Seq("x", "y", "z", "xxxx"))
  }

  test("empty input") {
    assertSplit("", Seq.empty)
  }

  test("whitespace only") {
    assertSplit("   ", Seq.empty)
  }

  // --- Backslash escaping outside quotes ---

  test("backslash escapes next char") {
    assertSplit("\\x bar", Seq("x", "bar"))
  }

  test("backslash-space joins into token") {
    assertSplit("\\ x bar", Seq(" x", "bar"))
  }

  test("backslash-space at start") {
    assertSplit("\\ bar", Seq(" bar"))
  }

  test("backslash escape mid-word") {
    assertSplit("foo \\x bar", Seq("foo", "x", "bar"))
  }

  test("backslash-space mid-word") {
    assertSplit("foo \\ x bar", Seq("foo", " x", "bar"))
  }

  test("backslash-space joins with next word") {
    assertSplit("foo \\ bar", Seq("foo", " bar"))
  }

  test("backslash-space inside word") {
    assertSplit("foo\\ xx", Seq("foo xx"))
  }

  test("multiple backslash escapes in word") {
    assertSplit("foo\\ x\\x", Seq("foo xx"))
  }

  test("line continuation skips newline") {
    assertSplit("foo\\\nbar", Seq("foobar"))
  }

  test("escaped double quote at top level") {
    assertSplit("\\\"", Seq("\""))
  }

  test("escaped single quote at top level") {
    assertSplit("\\'", Seq("'"))
  }

  test("escaped quote before word") {
    assertSplit("\\\"foo", Seq("\"foo"))
  }

  test("escaped quote and escaped char") {
    assertSplit("\\\"foo\\x", Seq("\"foox"))
  }

  test("backslash-space then backslash-escape then escaped-quote") {
    assertSplit("foo\\ x\\x\\\"", Seq("foo xx\""))
  }

  test("trailing escaped backslash") {
    assertSplit("foo \\\\", Seq("foo", "\\"))
  }

  // --- Double-quoted strings ---

  test("double quotes basic") {
    assertSplit("foo \"bar\" bla", Seq("foo", "bar", "bla"))
  }

  test("all double quoted") {
    assertSplit("\"foo\" \"bar\" \"bla\"", Seq("foo", "bar", "bla"))
  }

  test("double quotes mixed positions") {
    assertSplit("\"foo\" bar \"bla\"", Seq("foo", "bar", "bla"))
  }

  test("double quotes first token only") {
    assertSplit("\"foo\" bar bla", Seq("foo", "bar", "bla"))
  }

  test("empty double quotes") {
    assertSplit("\"\"", Seq(""))
  }

  test("empty double quotes between words") {
    assertSplit("foo \"\" bar", Seq("foo", "", "bar"))
  }

  test("multiple empty double quotes") {
    assertSplit("foo \"\" \"\" \"\" bar", Seq("foo", "", "", "", "bar"))
  }

  test("escaped quote inside double quotes") {
    assertSplit("\"\\\"\"", Seq("\""))
  }

  // backslash-space inside double quotes: space is NOT special, both chars kept
  test("backslash-space inside double quotes is literal") {
    assertSplit("\"foo\\ bar\"", Seq("foo\\ bar"))
  }

  // backslash-backslash inside double quotes: \\ -> single backslash
  test("backslash-backslash inside double quotes") {
    assertSplit("\"foo\\\\ bar\"", Seq("foo\\ bar"))
  }

  test("escaped backslash and escaped quote in double quotes") {
    assertSplit("\"foo\\\\ bar\\\"\"", Seq("foo\\ bar\""))
  }

  // closing double quote after escaped backslash, then more outside
  test("double quote closed after escaped backslash") {
    assertSplit("\"foo\\\\\" bar\\\"", Seq("foo\\", "bar\""))
  }

  // backslash-x inside double quotes: x is not special, both chars kept
  test("backslash with non-special char inside double quotes") {
    assertSplit("\"foo\\x\"", Seq("foo\\x"))
  }

  test("backslash-space at end inside double quotes") {
    assertSplit("\"foo\\ \"", Seq("foo\\ "))
  }

  test("backslash-space and backslash-x inside double quotes") {
    assertSplit("\"foo\\ x\\x\"", Seq("foo\\ x\\x"))
  }

  test("double quotes with escaped backslash at end") {
    assertSplit("\"foo\\ x\\x\\\\\"", Seq("foo\\ x\\x\\"))
  }

  // --- Single-quoted strings ---

  test("single quotes basic") {
    assertSplit("foo 'bar' bla", Seq("foo", "bar", "bla"))
  }

  test("all single quoted") {
    assertSplit("'foo' 'bar' 'bla'", Seq("foo", "bar", "bla"))
  }

  test("single quotes mixed") {
    assertSplit("'foo' bar 'bla'", Seq("foo", "bar", "bla"))
  }

  test("single quotes first token only") {
    assertSplit("'foo' bar bla", Seq("foo", "bar", "bla"))
  }

  test("empty single quotes") {
    assertSplit("''", Seq(""))
  }

  test("empty single quotes between words") {
    assertSplit("foo '' bar", Seq("foo", "", "bar"))
  }

  test("multiple empty single quotes") {
    assertSplit("foo '' '' '' bar", Seq("foo", "", "", "", "bar"))
  }

  // no escape processing inside single quotes
  test("backslash inside single quotes is literal") {
    assertSplit("'foo\\ bar'", Seq("foo\\ bar"))
  }

  test("double backslash inside single quotes is literal") {
    assertSplit("'foo\\\\ bar'", Seq("foo\\\\ bar"))
  }

  // --- Mixed quoting ---

  test("adjacent double quotes merge into token") {
    assertSplit("blurb foo\"bar\"bar\"fasel\" baz", Seq("blurb", "foobarbarfasel", "baz"))
  }

  test("adjacent single quotes merge into token") {
    assertSplit("blurb foo'bar'bar'fasel' baz", Seq("blurb", "foobarbarfasel", "baz"))
  }

  // no wordchar-based splitting like Python, so '' merges with )abc
  test("empty quotes followed by unquoted") {
    assertSplit("'')abc", Seq(")abc"))
  }

  test("don't idiom with escaped quote") {
    assertSplit("'don'\\''t'", Seq("don't"))
  }

  // --- Comments ---

  test("comment line") {
    assertSplit("# this is a comment", Seq.empty)
  }

  test("comment after whitespace") {
    assertSplit("  # comment", Seq.empty)
  }

  test("comment after token") {
    assertSplit("foo # comment", Seq("foo"))
  }

  test("hash inside double quotes is not a comment") {
    assertSplit("\"foo#bar\"", Seq("foo#bar"))
  }

  test("hash inside single quotes is not a comment") {
    assertSplit("'foo#bar'", Seq("foo#bar"))
  }

  // our implementation treats # as comment only when not mid-token
  test("hash adjacent to token is literal") {
    assertSplit("foo#bar", Seq("foo#bar"))
  }

  test("comments disabled") {
    assertEquals(shlex.split("foo # bar", comments = false), Right(Seq("foo", "#", "bar")))
  }

  // --- Unicode ---

  test("unicode preserved") {
    assertSplit("áéíóú", Seq("áéíóú"))
  }

  test("punctuation in tokens") {
    assertSplit(":-) ;-)", Seq(":-)", ";-)"))
  }

  // --- Error cases ---

  test("unmatched single quote") {
    val result = shlex.split("foo 'bar")
    assert(result.isLeft)
    result.left.foreach { e =>
      assert(e.isInstanceOf[shlex.UnmatchedSingleQuote])
      assertEquals(e.asInstanceOf[shlex.UnmatchedSingleQuote].position, 4)
    }
  }

  test("unmatched double quote") {
    val result = shlex.split("foo \"bar")
    assert(result.isLeft)
    result.left.foreach { e =>
      assert(e.isInstanceOf[shlex.UnmatchedDoubleQuote])
      assertEquals(e.asInstanceOf[shlex.UnmatchedDoubleQuote].position, 4)
    }
  }

  test("trailing backslash") {
    val result = shlex.split("foo\\")
    assert(result.isLeft)
    result.left.foreach { e =>
      assert(e.isInstanceOf[shlex.TrailingEscape])
    }
  }

  test("trailing backslash inside double quotes") {
    val result = shlex.split("\"foo\\")
    assert(result.isLeft)
    result.left.foreach { e =>
      assert(e.isInstanceOf[shlex.TrailingEscape])
    }
  }
