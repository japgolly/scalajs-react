package japgolly.scalajs.react.test.emissions.util

import japgolly.microlibs.testutil.TestUtil._
import japgolly.microlibs.utils.FileUtils
import java.io.{File, FileNotFoundException}
import java.nio.file.Files
import scala.annotation._
import scala.io.Source
import scala.sys.process._
import scala.util.Try
import sourcecode.Line

object Util {

  def assertJs(actual: String, expect: String)(implicit l: Line): Unit =
    assertJs("", actual, expect)

  def assertJs(name: String, actual: String, expect: String)(implicit l: Line): Unit =
    assertJs(name, actual, expect, TestJs.Hack.forComparison)

  def assertJs(actual: String, expect: String, preCmpHack: TestJs.Hack)(implicit l: Line): Unit =
    assertJs("", actual, expect, preCmpHack)

  def assertJs(name: String, actual: String, expect: String, preCmpHack: TestJs.Hack)(implicit l: Line): Unit = {
    val a = preCmpHack.runAs(name, actual).content
    val e = preCmpHack.runAs(name, expect).content
    assertMultiline(name, actual = a, expect = e)
  }

  // TODO: Update microlibs
  // private def assertMultiline(actual: String, expect: String)(implicit q: Line): Unit =
  //   _assertMultiline(None, actual, expect)

  private def assertMultiline(name: => String, actual: String, expect: String)(implicit q: Line): Unit =
    _assertMultiline(Some(name), actual, expect)

  private def _assertMultiline(name: => Option[String], actual: String, expect: String)(implicit q: Line): Unit =
    if (actual != expect) withAtomicOutput {
import scala.io.AnsiColor._
import japgolly.microlibs.testutil.TestUtilInternals._
import japgolly.microlibs.testutil.LineDiff
      println()
      val EA = List(expect, actual).map(_.split("\n"))
      val List(es, as) = EA : @nowarn
      val lim = es.length max as.length
      val List(maxAllE,_) = EA.map(x => (0 :: x.iterator.map(_.length).toList).max) : @nowarn
      // val mismtachingLines = (es.iterator.zip(as.iterator)).filter { case (e,a) => e !=* a }.toList
      // val maxDiffE = (0 :: mismtachingLines.map(_._1.length)).max
      // val maxLimitE = 80
      // val maxE = if (maxAllE <= maxLimitE) maxAllE else if (maxLimitE >= maxDiffE) maxLimitE else maxDiffE
      val maxLimitE = 114 // TODO: Make configurable by moving into config class
      // TODO: Make colours configurable by moving into config class
      // TODO: Make diffing logic configurable by moving into config class
      // TODO: Now I now why I keep finding the colours confusing! diff:red=del,green=add, assert:red=bad,green=good
      val maxE = maxAllE min maxLimitE
      val maxL = lim.toString.length
      if (maxL == 0 || maxE == 0)
        assertEqO(name, actual, expect)
      else {
        val nameSuffix = name.fold(RESET)(s":$RESET " + _)
        val fmtWSE = GREEN_B + BLACK
        val fmtWSA = RED_B + BLACK
        val fmtKOE = BLACK_B + BOLD_BRIGHT_GREEN
        val fmtKOA = BLACK_B + BOLD_BRIGHT_RED
        val cmp    = if (as.length == es.length) "|" else if (es.length > as.length) ">" else "<"
        println(s"${BRIGHT_YELLOW}assertMultiline$nameSuffix (${fmtKOE}expect$RESET $cmp ${fmtKOA}actual$RESET)")

        if (as.length == es.length) {
          val fmtOK = s"${BRIGHT_BLACK}%${maxL}d: %-${maxE}s | | %s${RESET}\n"
          val fmtWS = s"${WHITE}%${maxL}d: ${fmtWSE}%-${maxE}s${RESET}${WHITE} |≈| ${fmtWSA}%s${RESET}\n"
          val fmtKO = s"${WHITE}%${maxL}d: ${fmtKOE}%-${maxE}s${RESET}${WHITE} |≠| ${fmtKOA}%s${RESET}\n"
          def removeWhitespace(s: String) = s.filterNot(_.isWhitespace)
          for (i <- 0 until lim) {
            val List(e, a) = EA.map(s => if (i >= s.length) "" else s(i)) : @nowarn

            // val (fmt, a2, e2) =
            //   if (a == e)
            //     (fmtOK, a.take(maxA), e.take(maxA))
            //   else if (removeWhitespace(a) == removeWhitespace(e))
            //     (fmtWS, a.take(maxA), e.take(maxA))
            //   else
            //     (fmtKO, a, e)
            // printf(fmt, i + 1, a2, e2)

            val (fmt, truncate) =
              if (a == e)
                (fmtOK, true) // TODO: Make `true` configurable by moving into config class
              else if (removeWhitespace(a) == removeWhitespace(e))
                (fmtWS, true) // TODO: Make `true` configurable by moving into config class
              else
                (fmtKO, false) // TODO: Make `true` configurable by moving into config class

            val l = i + 1
            val w = maxE
            @tailrec def go(x: String, y: String, fmt2: Option[String]): Unit = {
              fmt2 match {
                case None    => printf(fmt, l, x.take(w), y.take(w))
                case Some(f) => printf(f, x.take(w), y.take(w))
              }
              val hasMore = (x.length max y.length) > w
              if (hasMore && !truncate) {
                def newFmt = fmt.replace(s"%${maxL}d:", " " * (maxL + 1))
                go(x.drop(w), y.drop(w), fmt2.orElse(Some(newFmt)))
              }
            }
            go(e, a, None)
          }
        } else {
          println(LineDiff(expect, actual).expectActualColoured)
          println(BRIGHT_YELLOW + ("-" * 120) + RESET)
        }
        println()
        fail("assertMultiline failed.")
      }
    }

  def containsRR(content: String): Boolean =
    content.contains("$RefreshSig$") || content.contains("$RefreshReg$")

  def countRR(content: String): Int =
    countSubstringOccurances(content, "$RefreshSig$()")

  def countSubstringOccurances(str: String, substr: String): Int = {
    @tailrec
    def go(s: String, n: Int): Int = {
      val i = s.indexOf(substr)
      if (i >= 0)
        go(s.drop(i + substr.length), n + 1)
      else
        n
    }
    go(str, 0)
  }

  def debugShowContent(name: String, content: String, colour: String, rrFlags: Boolean = true): Unit = {
    val batPath = "/usr/bin/bat"
    var useBat  = exists(batPath)

    val rrDesc =
      if (rrFlags) {
        val rr = containsRR(content)
        val flags = if (rr) "\u001b[102;30m[RR:Y]" else "\u001b[101;97m[RR:N]"
        flags + Console.RESET + " "
      } else
        ""

    val sep = "=" * (name.length + (if (rrFlags) 7 else 0))

    val content2 = {
      val sb = new java.lang.StringBuilder
      val ignore = """^(?:['"]use strict|import ).*""".r
      var showBlanks = false
      var show = false
      for (line <- content.trim.linesIterator) {
        show = true

        if (line.trim.isEmpty)
          show = showBlanks
        else if (ignore.matches(line))
          show = false

        if (show) {
          sb.append(line)
          sb.append('\n')
          showBlanks = true
        }
      }
      sb.toString()
    }

    println(sep)
    println(rrDesc + colour + name + Console.RESET)
    println(sep)
    if (useBat) {
      val f = writeToTempFile(".js")(content2)
      val t = Try { Seq(batPath, "-pp", "--color=always", f).! }
      if (!t.toOption.contains(0))
        useBat = false
    }
    if (!useBat) {
      print(colour)
      println(content2)
    }
    println(Console.RESET + sep)
    println()
  }

  def debugShowDiff(content1: String, content2: String): Unit = {
    var diffPath = "/usr/bin/colordiff"
    if (!exists(diffPath))
      diffPath = "diff"

    val file1 = writeToTempFile("")(content1)
    val file2 = writeToTempFile("")(content2)

    val sep = Console.YELLOW_B + Console.BLACK + ("=" * 120) + Console.RESET
    println(sep)
    Seq(diffPath, "-uw", file1, file2).#|(Seq("tail", "+3")).!
    println(sep)
  }

  def exists(filename: String): Boolean =
    (new File(filename)).exists()

  def getFileContent(path: String): Option[String] = {
    val f = new File(path)
    Option.when(f.exists())(readSource(Source.fromFile(f)))
  }

  def needFileContent(path: String): String =
    readSource(Source.fromFile(new File(path)))

  def needResourceContent(path: String): String = {
    getClass.getResourceAsStream(path) match {
      case null => throw new FileNotFoundException("cp:" + path)
      case is   => try readSource(Source.fromInputStream(is)) finally is.close()
    }
  }

  def readOrCreateFile[A](filename: String, create: => String): Option[String] =
    getFileContent(filename) match {

      // If no file exists, create it
      case None =>
        if (Props.CI)
          throw new RuntimeException(s"File not found: $filename. This file should've been checked in to git.")
        println(s"File not found, creating: $filename")
        FileUtils.write(filename, create)
        None

      // File exists
      case s@ Some(_) =>
        s
    }

  private def readSource(src: => Source): String = {
    val s = src
    try s.mkString finally s.close()
  }

  def writeToTempFile(fileSuffix: String)(content: String): String = {
    val p = Files.createTempFile("sjr-emissions-", fileSuffix)
    val f = p.toFile()
    val d = f.getAbsolutePath()
    FileUtils.write(d, content)
    f.deleteOnExit()
    d
  }

}
