package japgolly.scalajs.react.test.emissions.util

import java.io.File
import scala.sys.process._

final case class ProcessResult(cmd: Seq[String], exitStatus: Int, out: String, err: String) {

  def assertExitStatus(is: Int = 0): Unit =
    if (exitStatus != is) {
      System.out.println(out)
      System.out.flush()
      System.err.println(err)
      System.err.flush()
      throw new RuntimeException(s"`${cmd.mkString(" ")}` exited with $exitStatus")
    }
}

object ProcessResult {

  def apply(cmd: Seq[String], cwd: File): ProcessResult = {
    val p      = Process(cmd, cwd)
    val output = new Output
    val code   = p ! output.catpure
    ProcessResult(cmd, code, output.out.value(), output.err.value())
  }

  private final class Output {
    val out = new OutputStream
    val err = new OutputStream
    def catpure = ProcessLogger(out.capture, err.capture)
  }

  private final class OutputStream {
    private var sb: java.lang.StringBuilder = null

    def capture: String => Unit = line => {
      if (sb eq null)
        sb = new java.lang.StringBuilder
      else
        sb.append('\n')
      sb.append(line)
    }

    def value(): String =
      if (sb eq null) "" else sb.toString()
  }
}
