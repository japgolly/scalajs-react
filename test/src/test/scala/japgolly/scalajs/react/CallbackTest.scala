package japgolly.scalajs.react

import scala.concurrent._
import scala.concurrent.duration._
import utest._
import TestUtil2._

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

  def assertCompiles[A](f: => A): Unit = ()

  class IntVar {
    var i = 0
    val inc = Callback(i += 1)
    val get = CallbackTo(i)
    val incGet = inc >> get
  }

  override def tests = TestSuite {
    'guard {
      def cb = Callback.empty
      def cbI = CallbackTo(3)

      'constructor {
        def assertFails(f: CompileError): Unit = assert(f.msg contains "which will discard without running it")
        "unit"       - assertCompiles[Callback]( Callback(()))
        "boolean"    - assertCompiles[Callback]( Callback(false))
        "int"        - assertCompiles[Callback]( Callback(3))
        "Callback"   - assertFails(compileError("Callback(cb)"))
        "CallbackTo" - assertFails(compileError("Callback(cbI)"))
      }

      "map(): Callback" - {
        def assertFails(f: CompileError): Unit = assert(f.msg contains "type mismatch")
        def b = false
        def i = 1
        "unit"       - assertCompiles[Callback]( cb.map      (_ => ()) : Callback)
        "boolean"    - assertCompiles[Callback]( cb.map[Unit](_ => b)  : Callback)
        "int"        - assertCompiles[Callback]( cb.map[Unit](_ => i)  : Callback)
        "Callback"   - assertFails(compileError("cb.map      (_ => cb) : Callback"))
        "CallbackTo" - assertFails(compileError("cb.map      (_ => cbI): Callback"))
      }

      "map(): CallbackTo" - {
        "unit"       - assertCompiles[Callback                   ](cb.map(_ => ()))
        "boolean"    - assertCompiles[CallbackTo[Boolean        ]](cb.map(_ => false))
        "int"        - assertCompiles[CallbackTo[Int            ]](cb.map(_ => 3))
        "Callback"   - assertCompiles[CallbackTo[Callback       ]](cb.map(_ => cb))
        "CallbackTo" - assertCompiles[CallbackTo[CallbackTo[Int]]](cb.map(_ => cbI))
      }

    }

    'contravariance {
      def assertFails(f: CompileError): Unit = ()
      val x: CallbackTo[Seq[Int]] = CallbackTo(Nil)

      'widen  - assertCompiles(x: CallbackTo[Iterable[Int]])
      'narrow - assertFails(compileError("x: CallbackTo[List[Int]]"))
      'unit   - assertFails(compileError("x: Callback"))
    }

    'lazily -
      testEvalStrategy(Callback lazily _, 0, 1, 1)

    'byName -
      testEvalStrategy(Callback byName _, 0, 1, 2)

    'future {
      'repeatable {
        import test.RunNowEC.Implicit._
        var runs = 0
        def modState = Callback(runs += 1)
        def f = Future(modState)
        val c = Callback.future(f)
        assert(runs == 0)
        c.runNow()
        c.runNow()
        assert(runs == 2)
      }

      'toFlatFuture {
        import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
        val c = CallbackTo(Future(666))
        val f = c.toFlatFuture
        f.map(i => assert(i == 666))
      }
    }

    'flatten {
      val a = CallbackTo(CallbackTo(3)).flatten
      val b: CallbackTo[Int] = a

      val x = CallbackTo(Callback.empty).flatten
      val y: Callback = x
    }

    'when {
      val t = new IntVar; import t._
      assertEq(incGet.when(false).runNow(), None)
      assertEq(incGet.when(true ).runNow(), Some(1))
      assertEq(incGet.when(true ).runNow(), Some(2))
      assertEq(incGet.when(false).runNow(), None)
      assertEq(incGet.when(true ).runNow(), Some(3))
    }

    'unless {
      val t = new IntVar; import t._
      assertEq(incGet.unless(false).runNow(), Some(1))
      assertEq(incGet.unless(true ).runNow(), None)
      assertEq(incGet.unless(false).runNow(), Some(2))
      assertEq(incGet.unless(true ).runNow(), None)
    }

    'when_ {
      val t = new IntVar; import t._
      incGet.when_(false).runNow(); assertEq(i, 0)
      incGet.when_(true ).runNow(); assertEq(i, 1)
      incGet.when_(true ).runNow(); assertEq(i, 2)
      incGet.when_(false).runNow(); assertEq(i, 2)
      incGet.when_(true ).runNow(); assertEq(i, 3)
    }

    'unless {
      val t = new IntVar; import t._
      incGet.unless_(true ).runNow(); assertEq(i, 0)
      incGet.unless_(false).runNow(); assertEq(i, 1)
      incGet.unless_(true ).runNow(); assertEq(i, 1)
      incGet.unless_(false).runNow(); assertEq(i, 2)
      incGet.unless_(false).runNow(); assertEq(i, 3)
    }

    "Callback.when" - {
      val t = new IntVar; import t._
      Callback.when(false)(inc).runNow(); assertEq(i, 0)
      Callback.when(true )(inc).runNow(); assertEq(i, 1)
      Callback.when(true )(inc).runNow(); assertEq(i, 2)
      Callback.when(false)(inc).runNow(); assertEq(i, 2)
      Callback.when(true )(inc).runNow(); assertEq(i, 3)
    }

    "Callback.unless" - {
      val t = new IntVar; import t._
      Callback.unless(true )(inc).runNow(); assertEq(i, 0)
      Callback.unless(false)(inc).runNow(); assertEq(i, 1)
      Callback.unless(true )(inc).runNow(); assertEq(i, 1)
      Callback.unless(false)(inc).runNow(); assertEq(i, 2)
      Callback.unless(false)(inc).runNow(); assertEq(i, 3)
    }

  }
}
