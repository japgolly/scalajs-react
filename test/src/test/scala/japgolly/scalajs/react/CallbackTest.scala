package japgolly.scalajs.react

import utest._

object CallbackTest extends TestSuite {

  def testEvalStrategy(f: ( => Callback) => Callback, e1: Int, e2: Int, e3: Int): Unit = {
    var i = 9
    var j = 0
    def setJ(x: Int) = Callback(j = x)
    val cb = f(setJ(i))
    assert(j == e1)
    i = 1
    cb.runNow()
    assert(j == e2)
    j = 0
    i = 2
    cb.runNow()
    assert(j == e3)
  }

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
    'lazily -
      testEvalStrategy(Callback lazily _, 0, 1, 1)
    'byName -
      testEvalStrategy(Callback byName _, 0, 1, 2)
  }
}
