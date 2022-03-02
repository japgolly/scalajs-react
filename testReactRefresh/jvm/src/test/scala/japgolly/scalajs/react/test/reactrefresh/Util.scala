package japgolly.scalajs.react.test.reactrefresh

import java.io.{File, FileNotFoundException}
import scala.io.Source
import java.nio.file.Files
import scala.sys.process._
import scala.util.Try
import japgolly.microlibs.utils.FileUtils

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

  def debugShowContent(path: String, content: String, colour: String): Unit = {
    val batPath = "/usr/bin/bat"
    var useBat  = (new File(batPath)).exists()
    val rr      = content.contains("$RefreshSig$") || content.contains("$RefreshReg$")
    val rrDesc  = if (rr) "\u001b[102;30m[RR]" else "\u001b[101;97m[RR]"
    val sep     = "=" * (path.length + 5)

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
    println(rrDesc + Console.RESET + " " + colour + path + Console.RESET)
    println(sep)
    if (useBat) {
      val f = Files.createTempFile("sjr-reactrefresh-", ".js")
      val p = f.toFile().getAbsolutePath()
      FileUtils.write(p, content2)
      val res = Try { Seq(batPath, "-pp", "--color=always", p).! }
      Files.deleteIfExists(f)
      if (!res.toOption.contains(0))
        useBat = false
    }
    if (!useBat) {
      print(colour)
      println(content2)
    }
    println(Console.RESET + sep)
    println()
  }
}