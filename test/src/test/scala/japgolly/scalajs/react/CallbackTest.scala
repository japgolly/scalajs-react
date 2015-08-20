package japgolly.scalajs.react

import utest._

object CallbackTest extends TestSuite {
  override def tests = TestSuite {
    'guard {
      def assertCompiles[A](f: => A): Unit = ()
      def assertFails(f: CompileError): Unit = assume(f.msg contains "which will discard without running it")
      def cb = Callback.empty
      def cbI = CallbackTo(3)

      "Callback(unit)"       - assertCompiles[Callback](Callback(()))
      "Callback(boolean)"    - assertCompiles[Callback](Callback(false))
      "Callback(int)"        - assertCompiles[Callback](Callback(3))
      "Callback(Callback)"   - assertFails(compileError("Callback(cb)"))
      "Callback(CallbackTo)" - assertFails(compileError("Callback(cbI)"))
    }
  }
}
