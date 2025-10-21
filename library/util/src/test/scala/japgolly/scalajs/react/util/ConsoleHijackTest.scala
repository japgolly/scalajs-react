package japgolly.scalajs.react.util

import org.scalajs.dom.console
import scala.util.Try
import utest._

object ConsoleHijackTest extends TestSuite {

  override def tests = Tests {
    "catchReactWarnings" - {

      def testPass[A](a: => A): A =
        ConsoleHijack.fatalReactWarnings(a)

      def testThrows(a: => Any): Unit = {
        val t = Try(ConsoleHijack.fatalReactWarnings(a))
        println(t)
        assert(t.isFailure)
      }

      "error_hit"  - testThrows(console.error("Warning: this is a test"))
      "error_miss" - testPass(console.error("this is a test"))
      "warn_hit"  - testThrows(console.warn("Warning: this is a test"))
      "warn_miss" - testPass(console.warn("this is a test"))
    }
  }
}
