package japgolly.scalajs.react.test.emissions.util

import java.io.File
import scala.sys.process.Process
import scala.util.Try

object Node {
  private val dir = new File(Props.testRootDir + "/node")

  private val init = Try {
    val cmd = Seq("npm", "install", "--no-audit", "--no-fund")
    print(cmd.mkString("> ", " ", ""))

    val code = Process(cmd, dir).!
    println()

    if (code != 0)
      throw new ExceptionInInitializerError(s"`${cmd.mkString(" ")}` exited with $code")
  }

  def run(cmd: String*): ProcessResult = {
    val _ = init
    ProcessResult(cmd, dir)
  }

  def babel(args: String*): String = {
    val res = run(("./node_modules/.bin/babel" +: args): _*)
    res.assertExitStatus()
    res.out
  }
}
