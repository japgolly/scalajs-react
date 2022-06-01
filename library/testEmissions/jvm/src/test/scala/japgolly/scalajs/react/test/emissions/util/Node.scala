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

  def babel(args: String*): String = {
    val _ = init
    val cmd = Seq("./node_modules/.bin/babel") ++ args
    val res = ProcessResult(cmd, dir)
    res.assertExitStatus()
    res.out
  }
}
