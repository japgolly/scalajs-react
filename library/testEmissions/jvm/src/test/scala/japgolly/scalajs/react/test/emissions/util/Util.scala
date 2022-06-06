package japgolly.scalajs.react.test.emissions.util

import japgolly.microlibs.utils.FileUtils
import java.io.{File, FileNotFoundException}
import java.nio.file.Files
import scala.annotation.tailrec
import scala.io.Source
import scala.sys.process._
import scala.util.Try

object Util {

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
