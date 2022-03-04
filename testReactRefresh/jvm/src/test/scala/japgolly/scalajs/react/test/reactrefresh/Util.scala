package japgolly.scalajs.react.test.reactrefresh

import japgolly.microlibs.utils.FileUtils
import java.io.{File, FileNotFoundException}
import java.nio.file.Files
import scala.annotation.tailrec
import scala.io.Source
import scala.sys.process._
import scala.util.Try

object Util {

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

  private def readSource(src: => Source): String = {
    val s = src
    try s.mkString finally s.close()
  }

  def debugShowContent(name: String, content: String, colour: String): Unit = {
    val batPath = "/usr/bin/bat"
    var useBat  = (new File(batPath)).exists()
    val rr      = containsRR(content)
    val rrDesc  = if (rr) "\u001b[102;30m[RR]" else "\u001b[101;97m[RR]"
    val sep     = "=" * (name.length + 5)

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
    println(rrDesc + Console.RESET + " " + colour + name + Console.RESET)
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

  def containsRR(content: String): Boolean =
    content.contains("$RefreshSig$") || content.contains("$RefreshReg$")

  def writeToTempFile(fileSuffix: String)(content: String): String = {
    val p = Files.createTempFile("sjr-reactrefresh-", fileSuffix)
    val f = p.toFile()
    val d = f.getAbsolutePath()
    FileUtils.write(d, content)
    f.deleteOnExit()
    d
  }

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

  def countRR(content: String): Int =
    countSubstringOccurances(content, "$RefreshSig$()")

}