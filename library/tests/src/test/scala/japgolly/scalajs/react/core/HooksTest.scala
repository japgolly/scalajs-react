package japgolly.scalajs.react.core

import japgolly.scalajs.react.Hooks.UseEffectArg
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.test.DomTester
import japgolly.scalajs.react.test.ReactTestUtils._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html.Input
import utest._
import japgolly.scalajs.react.hooks.HookResult

object HooksTest extends TestSuite {

  // TODO: https://github.com/lampepfl/dotty/issues/12663
  // Should be private ↓
  def test[M, A](u: Unmounted[M])(f: DomTester => A): A =
    withRenderedIntoBody(u).withParent(root => f(new DomTester(root)))

  private val incBy1 = Reusable.byRef((_: Int) + 1)
  private val incBy5 = Reusable.byRef((_: Int) + 5)

  // TODO: https://github.com/lampepfl/dotty/issues/12663
  // Should be private or moved elsewhere ↓
  final class Counter {
    var value = 0
    def inc(by: Int = 1): Int = {
      value += by
      value
    }
    def incCB         : Callback = incCB(1)
    def incCB(by: Int): Callback = Callback{ inc(by); () }
  }

  private final class Recorder[A] {
    var values = Vector.empty[A]
    def add(as: A*): Unit = values ++= as
    def addCB(as: A*): Callback = {val x = as.toList; Callback(add(x: _*))}
  }

  // TODO: https://github.com/lampepfl/dotty/issues/12663
  // Should be private ↓
  final case class PI(pi: Int) {
    def unary_- : PI = PI(-pi)
    def *(n: Int): PI = PI(pi * n)
    def +(n: Int): PI = PI(pi + n)
    def +(n: PI): PI = PI(pi + n.pi)
  }

  private final class ReusableSetIntComponent {
    private var renders = 0
    def render(f: Reusable[Int => Callback]) = {
      renders += 1
      <.div(renders,
        <.button(^.onClick --> f(1)),
        <.button(^.onClick --> f(2)),
      )
    }
  }
  private val ReusableSetIntComponent =
    ScalaComponent.builder[Reusable[Int => Callback]]
      .renderBackend[ReusableSetIntComponent]
      .configure(Reusability.shouldComponentUpdate)
      .build

  private final class ReusableModIntComponent {
    private var renders = 0
    def render(f: Reusable[(Int => Int) => Callback]) = {
      renders += 1
      <.div(renders,
        <.button(^.onClick --> f(_ + 1)),
        <.button(^.onClick --> f(_ + 10)),
      )
    }
  }
  private val ReusableModIntComponent =
    ScalaComponent.builder[Reusable[(Int => Int) => Callback]]
      .renderBackend[ReusableModIntComponent]
      .configure(Reusability.shouldComponentUpdate)
      .build

  private final class ReusableCallbackComponent {
    private var renders = 0
    def render(p: Reusable[Callback]) = {
      renders += 1
      <.div(renders, <.button(^.onClick --> p))
    }
  }
  private val ReusableCallbackComponent =
    ScalaComponent.builder[Reusable[Callback]]
      .renderBackend[ReusableCallbackComponent]
      .configure(Reusability.shouldComponentUpdate)
      .build

  private final class ReusableStateSnapshotComponent {
    private var renders = 0
    def render(s: StateSnapshot[Int]) = {
      renders += 1
      <.div(s"${s.value}:$renders",
        <.button(^.onClick --> s.modState(_ + 1)),
        <.button(^.onClick --> s.modState(_ + 10)),
      )
    }
  }
  private val ReusableStateSnapshotComponent =
    ScalaComponent.builder[StateSnapshot[Int]]
      .renderBackend[ReusableStateSnapshotComponent]
      .configure(Reusability.shouldComponentUpdate)
      .build

  // ===================================================================================================================

  private def testCustomHook(): Unit = {
    // TODO: https://github.com/lampepfl/dotty/issues/12663
    ScalaSpecificHooksTest.testCustomHook()

  //   val counter = new Counter

  //   val hookS = CustomHook[Int].useStateBy(identity).buildReturning(_.hook1)
  //   val hookE = CustomHook[Int].useEffectBy(counter.incCB(_)).build

  //   val comp = ScalaFnComponent.withHooks[PI]
  //     .custom(hookE(10))
  //     .custom(hookS(3)) // <--------------------------------------- s1
  //     .custom(hookS.contramap[PI](_.pi)) // <---------------------- s2
  //     .customBy((p, s, _) => hookE(p.pi + s.value))
  //     .customBy($ => hookS($.props.pi + $.hook1.value + 1)) // <--- s3
  //     .customBy($ => hookE($.props.pi + $.hook1.value + 1))
  //     .render((p, s1, s2, s3) =>
  //       <.div(
  //         s"${s1.value}:${s2.value}:${s3.value}",
  //         <.button(^.onClick --> s1.modState(_ + 1))
  //       )
  //     )

  //   test(comp(PI(5))) { t =>
  //     t.assertText("3:5:9")
  //     assertEq(counter.value, 10 + (5+3) + (5+3+1))
  //     counter.value = 0
  //     t.clickButton()
  //     t.assertText("4:5:9")
  //     assertEq(counter.value, 10 + (5+4) + (5+4+1))
  //   }
  }

  private def testCustomHookComposition(): Unit = {

    locally {
      type LL = CustomHook[Long, Long]
      type II = CustomHook[Int, Int]
      type IU = CustomHook[Int, Unit]
      type UI = CustomHook[Unit, Int]
      type UU = CustomHook[Unit, Unit]
      type TF = CustomHook[3, 4]
      type FT = CustomHook[4, 3]

      assertType[(II, LL)].map(h => h._1 ++ h._2).is[CustomHook[(Int, Long), (Int, Long)]]
      assertType[(II, TF)].map(h => h._1 ++ h._2).is[CustomHook[(Int, 3), (Int, 4)]]
      assertType[(II, II)].map(h => h._1 ++ h._2).is[CustomHook[Int, (Int, Int)]]
      assertType[(II, IU)].map(h => h._1 ++ h._2).is[CustomHook[Int, Int]]
      assertType[(II, UI)].map(h => h._1 ++ h._2).is[CustomHook[Int, (Int, Int)]]
      assertType[(II, UU)].map(h => h._1 ++ h._2).is[CustomHook[Int, Int]]

      assertType[(IU, LL)].map(h => h._1 ++ h._2).is[CustomHook[(Int, Long), Long]]
      assertType[(IU, TF)].map(h => h._1 ++ h._2).is[CustomHook[(Int, 3), 4]]
      assertType[(IU, II)].map(h => h._1 ++ h._2).is[CustomHook[Int, Int]]
      assertType[(IU, IU)].map(h => h._1 ++ h._2).is[CustomHook[Int, Unit]]
      assertType[(IU, UI)].map(h => h._1 ++ h._2).is[CustomHook[Int, Int]]
      assertType[(IU, UU)].map(h => h._1 ++ h._2).is[CustomHook[Int, Unit]]

      assertType[(UI, LL)].map(h => h._1 ++ h._2).is[CustomHook[Long, (Int, Long)]]
      assertType[(UI, TF)].map(h => h._1 ++ h._2).is[CustomHook[3, (Int, 4)]]
      assertType[(UI, II)].map(h => h._1 ++ h._2).is[CustomHook[Int, (Int, Int)]]
      assertType[(UI, IU)].map(h => h._1 ++ h._2).is[CustomHook[Int, Int]]
      assertType[(UI, UI)].map(h => h._1 ++ h._2).is[CustomHook[Unit, (Int, Int)]]
      assertType[(UI, UU)].map(h => h._1 ++ h._2).is[CustomHook[Unit, Int]]

      assertType[(UU, LL)].map(h => h._1 ++ h._2).is[CustomHook[Long, Long]]
      assertType[(UU, TF)].map(h => h._1 ++ h._2).is[CustomHook[3, 4]]
      assertType[(UU, II)].map(h => h._1 ++ h._2).is[CustomHook[Int, Int]]
      assertType[(UU, IU)].map(h => h._1 ++ h._2).is[CustomHook[Int, Unit]]
      assertType[(UU, UI)].map(h => h._1 ++ h._2).is[CustomHook[Unit, Int]]
      assertType[(UU, UU)].map(h => h._1 ++ h._2).is[CustomHook[Unit, Unit]]

      assertType[(LL, LL)].map(h => h._1 ++ h._2).is[CustomHook[Long, (Long, Long)]]
      assertType[(LL, II)].map(h => h._1 ++ h._2).is[CustomHook[(Long, Int), (Long, Int)]]
      assertType[(LL, IU)].map(h => h._1 ++ h._2).is[CustomHook[(Long, Int), Long]]
      assertType[(LL, UI)].map(h => h._1 ++ h._2).is[CustomHook[Long, (Long, Int)]]
      assertType[(LL, UU)].map(h => h._1 ++ h._2).is[CustomHook[Long, Long]]

      assertType[(TF, FT)].map(h => h._1 ++ h._2).is[CustomHook[(3, 4), (4, 3)]]
      assertType[(TF, II)].map(h => h._1 ++ h._2).is[CustomHook[(3, Int), (4, Int)]]
      assertType[(TF, IU)].map(h => h._1 ++ h._2).is[CustomHook[(3, Int), 4]]
      assertType[(TF, LL)].map(h => h._1 ++ h._2).is[CustomHook[(3, Long), (4, Long)]]
      assertType[(TF, TF)].map(h => h._1 ++ h._2).is[CustomHook[3, 4]]
      assertType[(TF, UI)].map(h => h._1 ++ h._2).is[CustomHook[3, (4, Int)]]
      assertType[(TF, UU)].map(h => h._1 ++ h._2).is[CustomHook[3, 4]]

      assertType[(FT, FT)].map(h => h._1 ++ h._2).is[CustomHook[4, 3]]
      assertType[(FT, II)].map(h => h._1 ++ h._2).is[CustomHook[(4, Int), (3, Int)]]
      assertType[(FT, IU)].map(h => h._1 ++ h._2).is[CustomHook[(4, Int), 3]]
      assertType[(FT, LL)].map(h => h._1 ++ h._2).is[CustomHook[(4, Long), (3, Long)]]
      assertType[(FT, TF)].map(h => h._1 ++ h._2).is[CustomHook[(4, 3), (3, 4)]]
      assertType[(FT, UI)].map(h => h._1 ++ h._2).is[CustomHook[4, (3, Int)]]
      assertType[(FT, UU)].map(h => h._1 ++ h._2).is[CustomHook[4, 3]]
    }

    val ints = new Recorder[Int]

    val addII = CustomHook[Int].useEffectBy(ints.addCB(_)).buildReturning(identity)
    val addI_ = addII.map(_ => ()).contramap[Int](_ * 10)
    val add_S = CustomHook[Unit].useEffect(ints.addCB(100)).buildReturning(_ => "ah")

    val hook = addII ++ addI_ ++ add_S
    val _ : CustomHook[Int, (Int, String)] = hook

    val comp = ScalaFnComponent.withDisplayName("WithCustomHooks")
      .withHooks[PI]
      .customBy(p => hook(p.pi))
      .render((_, h) => h.toString)

    assertEq(comp.displayName, "WithCustomHooks")
    test(comp(PI(3))) { t =>
      t.assertText("(3,ah)")
    }
    assertEq(ints.values, Vector(3, 30, 100))
  }

  private def testCustomMonadicHookComposition(): Unit = {

    locally {
      type LI = Long => HookResult[Int]
      type IU = Int => HookResult[Unit]

      assertType[(LI, IU)].map(h => h._1.andThen(_.flatMap(h._2))).is[Long => HookResult[Unit]]
    }

    val ints = new Recorder[Int]

    val addII = (i: Int) => useEffect(ints.addCB(i)).map(_ => i)
    val addI_ = addII.andThen(_.map(_ => ())).contramap[Int](_ * 10)
    val add_S = useEffect(ints.addCB(100)).map(_ => "ah")

    def hook(i: Int) =
      for {
        x <- addII(i)
        _ <- addI_(i)
        s <- add_S
      } yield (x, s)
    val _ : Int => HookResult[(Int, String)] = hook(_)

    val comp = 
      ScalaFnComponent.withDisplayName("WithCustomHooks")[PI](p => hook(p.pi).map(_.toString))

    assertEq(comp.displayName, "WithCustomHooks")
    test(comp(PI(3))) { t =>
      t.assertText("(3,ah)")
    }
    assertEq(ints.values, Vector(3, 30, 100))
  }  

  private def testLazyVal(): Unit = {
    val counter = new Counter
    val comp = ScalaFnComponent.withDisplayName("TestComponent").withHooks[PI]
      .localLazyVal(counter.inc())
      .localLazyValBy((p, _) => p.pi + counter.inc())
      .localLazyValBy($ => $.props.pi + counter.inc())
      .useState(123)
      .render { (p, f1, f2, f3, s) =>
        val v3 = f3()
        val v2 = f2()
        val v1 = f1()
        <.div(
          <.div(s"P=$p, v1=$v1, v2=$v2, v3=$v3"),
          <.button(^.onClick --> s.modState(_ + 1)))
      }

    assertEq(comp.displayName, "TestComponent")
    test(comp(PI(10))) { t =>
      t.assertText("P=PI(10), v1=3, v2=12, v3=11")
      t.clickButton(); t.assertText("P=PI(10), v1=6, v2=15, v3=14")
    }
  }

  private def testVal(): Unit = {
    val counter = new Counter
    val comp = ScalaFnComponent.withHooks[PI]
      .localVal(counter.inc())
      .localValBy((p, _) => p.pi + counter.inc())
      .localValBy($ => $.props.pi + counter.inc())
      .useState(123)
      .render { (p, v1, v2, v3, s) =>
        <.div(
          <.div(s"P=$p, v1=$v1, v2=$v2, v3=$v3"),
          <.button(^.onClick --> s.modState(_ + 1)))
      }

    test(comp(PI(10))) { t =>
      t.assertText("P=PI(10), v1=1, v2=12, v3=13")
      t.clickButton(); t.assertText("P=PI(10), v1=4, v2=15, v3=16")
    }
  }

  private def testVar(): Unit = {
    val counter = new Counter
    val comp = ScalaFnComponent.withHooks[PI]
      .localVar(counter.inc())
      .localVarBy((p, _) => p.pi + counter.inc())
      .localVarBy($ => $.props.pi + counter.inc())
      .useState(123)
      .render { (p, v1, v2, v3, s) =>
        v1.value += 100
        v2.value += 100
        v3.value += 100
        <.div(
          <.div(s"P=$p, v1=${v1.value}, v2=${v2.value}, v3=${v3.value}"),
          <.button(^.onClick --> s.modState(_ + 1)))
      }

    assertEq(comp.displayName, "HooksTest.comp (japgolly.scalajs.react.core)")
    test(comp(PI(10))) { t =>
      t.assertText("P=PI(10), v1=101, v2=112, v3=113")
      t.clickButton(); t.assertText("P=PI(10), v1=104, v2=115, v3=116")
    }
  }

  private def testUnchecked(): Unit = {
    val counterE = new Counter
    val counterS = new Counter

    val comp = ScalaFnComponent.withHooks[PI]
      .unchecked{counterE.inc(10); ()}
      .unchecked(counterS.inc(3))
      .uncheckedBy((p, _) => counterS.inc(p.pi))
      .uncheckedBy((p, s, _) => {counterE.inc(p.pi + s); ()})
      .uncheckedBy($ => counterS.inc($.props.pi + $.hook1 + 1))
      .uncheckedBy($ => {counterE.inc($.props.pi + $.hook1 + 1); ()})
      .useState(0)
      .render((_, s1, s2, s3, x) =>
        <.div(
          s"$s1:$s2:$s3",
          <.button(^.onClick --> x.modState(_ + 1))
        )
      )

    test(comp(PI(5))) { t =>
      t.assertText("3:8:17")
      assertEq(counterE.value, 10 + (5+3) + (5+3+1))
      counterE.value = 0
      t.clickButton()
      t.assertText("20:25:51") // +3 : +5 : +(5+20+1)
      assertEq(counterE.value, 10 + (5+20) + (5+20+1))
    }
  }

  private def testUseCallback(): Unit = {
    val counter = new Counter
    val comp = ScalaFnComponent.withHooks[Unit]
      .useCallback(counter.incCB(9))
      .useCallback((i: Int) => counter.incCB(i))
      .useState(0)
      .render((_, c1, c2, s) =>
        <.div(
          "S=", counter.value,
          ", R1=", ReusableCallbackComponent(c1),
          ", R2=", ReusableSetIntComponent(c2),
          <.button(^.onClick --> s.modState(_ + 1))
        )
      )

    val inc9 = 1
    val inc1 = 2
    val inc2 = 3
    val rndr = 4

    test(comp()) { t =>
      assertEq(counter.value, 0); t.assertText("S=0, R1=1, R2=1")
      t.clickButton(inc1); assertEq(counter.value, 1); t.assertText("S=0, R1=1, R2=1")
      t.clickButton(inc9); assertEq(counter.value, 10); t.assertText("S=0, R1=1, R2=1")
      t.clickButton(inc2); assertEq(counter.value, 12); t.assertText("S=0, R1=1, R2=1")
      t.clickButton(rndr); assertEq(counter.value, 12); t.assertText("S=12, R1=1, R2=1")
    }
  }

  private def testMonadicUseCallback(): Unit = {
    val counter = new Counter
    val comp = ScalaFnComponent[Unit]( _ =>
      for {
        c1 <- useCallback(counter.incCB(9))
        c2 <- useCallback((i: Int) => counter.incCB(i))
        s  <- useState(0)
      } yield 
        <.div(
          "S=", counter.value,
          ", R1=", ReusableCallbackComponent(c1),
          ", R2=", ReusableSetIntComponent(c2),
          <.button(^.onClick --> s.modState(_ + 1))
        )
    )

    val inc9 = 1
    val inc1 = 2
    val inc2 = 3
    val rndr = 4

    test(comp()) { t =>
      assertEq(counter.value, 0); t.assertText("S=0, R1=1, R2=1")
      t.clickButton(inc1); assertEq(counter.value, 1); t.assertText("S=0, R1=1, R2=1")
      t.clickButton(inc9); assertEq(counter.value, 10); t.assertText("S=0, R1=1, R2=1")
      t.clickButton(inc2); assertEq(counter.value, 12); t.assertText("S=0, R1=1, R2=1")
      t.clickButton(rndr); assertEq(counter.value, 12); t.assertText("S=12, R1=1, R2=1")
    }
  }

  private def testUseCallbackBy(): Unit = {
    val counter = new Counter
    val comp = ScalaFnComponent.withHooks[PI]
      .useCallbackBy(p => counter.incCB(p.pi))
      .useCallbackBy((_, c1) => (i: Int) => c1 >> counter.incCB(i))
      .useCallbackBy($ => (i: Int) => $.hook1 >> counter.incCB(2 + i))
      .useState(0)
      .render((_, c1, c2, c3, s) =>
        <.div(
          "S=", counter.value,
          ", R1=", ReusableCallbackComponent(c1),
          ", R2=", ReusableSetIntComponent(c2),
          ", R3=", ReusableSetIntComponent(c3),
          <.button(^.onClick --> s.modState(_ + 1))
        )
      )

    val inc7  = 1
    val inc8  = 2 // 7 + 1
    val inc9  = 3 // 7 + 2
    val inc10 = 4 // 7 + 2 + 1
    val inc11 = 5 // 7 + 2 + 2
    val rndr  = 6

    test(comp(PI(7))) { t =>
      assertEq(counter.value, 0); t.assertText("S=0, R1=1, R2=1, R3=1")
      t.clickButton(inc9); assertEq(counter.value, 9); t.assertText("S=0, R1=1, R2=1, R3=1")
      t.clickButton(inc11); assertEq(counter.value, 20); t.assertText("S=0, R1=1, R2=1, R3=1")
      t.clickButton(inc10); assertEq(counter.value, 30); t.assertText("S=0, R1=1, R2=1, R3=1")
      t.clickButton(inc8); assertEq(counter.value, 38); t.assertText("S=0, R1=1, R2=1, R3=1")
      t.clickButton(inc7); assertEq(counter.value, 45); t.assertText("S=0, R1=1, R2=1, R3=1")
      t.clickButton(rndr); assertEq(counter.value, 45); t.assertText("S=45, R1=1, R2=1, R3=1")
    }
  }

  private def testUseCallbackWithDeps(): Unit = {
    val depA    = new Counter
    val depB    = new Counter
    val counter = new Counter
    val comp = ScalaFnComponent.withHooks[Unit]
      .useCallbackWithDeps(depA.value)(_ => counter.incCB(depA.value))
      .useCallbackWithDeps(depB.value)(_ => (i: Int) => counter.incCB(depB.value + i - 1))
      .useState(0)
      .render((_, c1, c2, s) =>
        <.div(
          "S=", counter.value,
          ", R1=", ReusableCallbackComponent(c1),
          ", R2=", ReusableSetIntComponent(c2),
          <.button(^.onClick --> s.modState(_ + 1))
        )
      )

    val incA  = 1
    val incB0 = 2
    val incB1 = 3
    val rndr  = 4

    depA.value = 10
    depB.value = 20
    test(comp()) { t =>
      assertEq(counter.value, 0); t.assertText("S=0, R1=1, R2=1")
      t.clickButton(incA); assertEq(counter.value, 10); t.assertText("S=0, R1=1, R2=1")
      t.clickButton(incB0); assertEq(counter.value, 30); t.assertText("S=0, R1=1, R2=1")
      t.clickButton(incB1); assertEq(counter.value, 51); t.assertText("S=0, R1=1, R2=1")
      t.clickButton(rndr); assertEq(counter.value, 51); t.assertText("S=51, R1=1, R2=1")
      depA.value = 100; t.clickButton(rndr); assertEq(counter.value, 51); t.assertText("S=51, R1=2, R2=1")
      t.clickButton(incA); assertEq(counter.value, 151); t.assertText("S=51, R1=2, R2=1")
      t.clickButton(incB0); assertEq(counter.value, 171); t.assertText("S=51, R1=2, R2=1")
      depB.value = 200; t.clickButton(rndr); assertEq(counter.value, 171); t.assertText("S=171, R1=2, R2=2")
      t.clickButton(incB1); assertEq(counter.value, 372); t.assertText("S=171, R1=2, R2=2")
    }
  }

  private def testUseCallbackWithDepsBy(): Unit = {
    val counter = new Counter
    val comp = ScalaFnComponent.withHooks[PI]
      .useCallbackWithDepsBy(_.pi)(p => _ => counter.incCB(p.pi)) // hook1
      .useState(10) // hook2: dep1
      .useState(20) // hook3: dep2
      .useCallbackWithDepsBy((_, _, d1, _) => d1.value)((_, _, _, _) => counter.incCB) // hook4
      .useCallbackWithDepsBy(_.hook3.value)($ => _ => (i: Int) => counter.incCB($.hook3.value + i - 1)) // hook5
      .render((_, c0, d1, d2, c1, c2) =>
        <.div(
          "S=", counter.value,
          ", C0=", ReusableCallbackComponent(c0),
          ", C1=", ReusableCallbackComponent(c1),
          ", C2=", ReusableSetIntComponent(c2),
          <.button(^.onClick --> d1.modState(_ + 100)),
          <.button(^.onClick --> d2.modState(_ + 100)),
        )
      )

    val c0  = 1 //  s += 3
    val c1  = 2 //  s += d1
    val c2a = 3 //  s += d2
    val c2b = 4 //  s += d2+1
    val d1  = 5 // d1 += 100
    val d2  = 6 // d2 += 100

    test(comp(PI(3))) { t =>
      assertEq(counter.value, 0); t.assertText("S=0, C0=1, C1=1, C2=1")
      t.clickButton(c1); assertEq(counter.value, 10); t.assertText("S=0, C0=1, C1=1, C2=1")
      t.clickButton(c0); assertEq(counter.value, 13); t.assertText("S=0, C0=1, C1=1, C2=1")
      t.clickButton(c2b); assertEq(counter.value, 34); t.assertText("S=0, C0=1, C1=1, C2=1")
      t.clickButton(c2a); assertEq(counter.value, 54); t.assertText("S=0, C0=1, C1=1, C2=1")
      t.clickButton(d2); assertEq(counter.value, 54); t.assertText("S=54, C0=1, C1=1, C2=2") // d2=120
      t.clickButton(c2a); assertEq(counter.value, 174); t.assertText("S=54, C0=1, C1=1, C2=2")
      t.clickButton(c0); assertEq(counter.value, 177); t.assertText("S=54, C0=1, C1=1, C2=2")
      t.clickButton(c1); assertEq(counter.value, 187); t.assertText("S=54, C0=1, C1=1, C2=2")
      t.clickButton(d1); assertEq(counter.value, 187); t.assertText("S=187, C0=1, C1=2, C2=2") // d1=110
      t.clickButton(c2a); assertEq(counter.value, 307); t.assertText("S=187, C0=1, C1=2, C2=2")
      t.clickButton(c1); assertEq(counter.value, 417); t.assertText("S=187, C0=1, C1=2, C2=2")
      t.clickButton(d1); assertEq(counter.value, 417); t.assertText("S=417, C0=1, C1=3, C2=2") // d1=210
      t.clickButton(d1); assertEq(counter.value, 417); t.assertText("S=417, C0=1, C1=4, C2=2") // d1=310
      t.clickButton(c1); assertEq(counter.value, 727); t.assertText("S=417, C0=1, C1=4, C2=2")
    }
  }

  private def testMonadicUseCallbackWithDeps(): Unit = {
    val counter = new Counter
    val comp = ScalaFnComponent[PI]( p =>
      for {
        c0 <- useCallbackWithDeps(p.pi)( _ => counter.incCB(p.pi))
        d1 <- useState(10)
        d2 <- useState(20)
        c1 <- useCallbackWithDeps( d1.value)(counter.incCB)
        c2 <- useCallbackWithDeps(d2.value)(_ => (i: Int) => counter.incCB(d2.value + i - 1))
      } yield
        <.div(
          "S=", counter.value,
          ", C0=", ReusableCallbackComponent(c0),
          ", C1=", ReusableCallbackComponent(c1),
          ", C2=", ReusableSetIntComponent(c2),
          <.button(^.onClick --> d1.modState(_ + 100)),
          <.button(^.onClick --> d2.modState(_ + 100)),
        )
      )

    val c0  = 1 //  s += 3
    val c1  = 2 //  s += d1
    val c2a = 3 //  s += d2
    val c2b = 4 //  s += d2+1
    val d1  = 5 // d1 += 100
    val d2  = 6 // d2 += 100

    test(comp(PI(3))) { t =>
      assertEq(counter.value, 0); t.assertText("S=0, C0=1, C1=1, C2=1")
      t.clickButton(c1); assertEq(counter.value, 10); t.assertText("S=0, C0=1, C1=1, C2=1")
      t.clickButton(c0); assertEq(counter.value, 13); t.assertText("S=0, C0=1, C1=1, C2=1")
      t.clickButton(c2b); assertEq(counter.value, 34); t.assertText("S=0, C0=1, C1=1, C2=1")
      t.clickButton(c2a); assertEq(counter.value, 54); t.assertText("S=0, C0=1, C1=1, C2=1")
      t.clickButton(d2); assertEq(counter.value, 54); t.assertText("S=54, C0=1, C1=1, C2=2") // d2=120
      t.clickButton(c2a); assertEq(counter.value, 174); t.assertText("S=54, C0=1, C1=1, C2=2")
      t.clickButton(c0); assertEq(counter.value, 177); t.assertText("S=54, C0=1, C1=1, C2=2")
      t.clickButton(c1); assertEq(counter.value, 187); t.assertText("S=54, C0=1, C1=1, C2=2")
      t.clickButton(d1); assertEq(counter.value, 187); t.assertText("S=187, C0=1, C1=2, C2=2") // d1=110
      t.clickButton(c2a); assertEq(counter.value, 307); t.assertText("S=187, C0=1, C1=2, C2=2")
      t.clickButton(c1); assertEq(counter.value, 417); t.assertText("S=187, C0=1, C1=2, C2=2")
      t.clickButton(d1); assertEq(counter.value, 417); t.assertText("S=417, C0=1, C1=3, C2=2") // d1=210
      t.clickButton(d1); assertEq(counter.value, 417); t.assertText("S=417, C0=1, C1=4, C2=2") // d1=310
      t.clickButton(c1); assertEq(counter.value, 727); t.assertText("S=417, C0=1, C1=4, C2=2")
    }
  }

  private def testUseContext(): Unit = {
    val ctx = React.createContext(100)

    val compC = ScalaFnComponent.withHooks[Unit]
      .useContext(ctx)
      .render((_, c) => c)

    val comp = ScalaFnComponent[Unit] { _ =>
      <.div(
        compC(),
        ":",
        ctx.provide(123)(compC()),
      )
    }

    test(comp()) { t =>
      t.assertText("100:123")
    }
  }

  private def testMonadicUseContext(): Unit = {
    val ctx = React.createContext(100)

    val compC = ScalaFnComponent[Unit]( _ => useContext(ctx).map(c => c))

    val comp = ScalaFnComponent[Unit] { _ =>
      <.div(
        compC(),
        ":",
        ctx.provide(123)(compC()),
      )
    }

    test(comp()) { t =>
      t.assertText("100:123")
    }
  }

  // Can't really observe this but can at least confirm that usage doesn't throw
  private def testUseDebugValue(): Unit = {
    val comp = ScalaFnComponent.withHooks[PI]
      .useDebugValue("hehe")
      .useDebugValueBy(_.pi)
      .useState(0)
      .useDebugValueBy($ => $.props.pi + $.hook1.value)
      .render($ => <.div($.props.pi))

    test(comp(PI(3))) { t =>
      t.assertText("3")
    }
  }

  trait X_UseEffect_Primary[Ctx, Step <: HooksApi.AbstractStep] {
    def X_useEffect[A](effect: A)(implicit a: UseEffectArg[A], step: Step): step.Self
    def X_useEffectBy[A](init: Ctx => A)(implicit a: UseEffectArg[A], step: Step): step.Self
    def X_useEffectOnMount[A](effect: A)(implicit a: UseEffectArg[A], step: Step): step.Self
    def X_useEffectOnMountBy[A](effect: Ctx => A)(implicit a: UseEffectArg[A], step: Step): step.Self
    def X_useEffectWithDeps[D, A](deps: => D)(effect: D => A)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self
    def X_useEffectWithDepsBy[D, A](deps: Ctx => D)(effect: Ctx => D => A)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self
  }
  trait X_UseEffect_Secondary[Ctx, CtxFn[_], Step <: HooksApi.SubsequentStep[Ctx, CtxFn]] extends X_UseEffect_Primary[Ctx, Step] {
    def X_useEffectBy[A](init: CtxFn[A])(implicit a: UseEffectArg[A], step: Step): step.Self
    def X_useEffectOnMountBy[A](effect: CtxFn[A])(implicit a: UseEffectArg[A], step: Step): step.Self
    def X_useEffectWithDepsBy[D, A](deps: CtxFn[D])(effect: CtxFn[D => A])(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self
  }

  private object UseEffect extends UseEffectTests {
    override protected implicit def hooksExt1[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]): Primary[Ctx, Step] =
      new Primary(api)
    override protected implicit def hooksExt2[Ctx, CtxFn[_], Step <: HooksApi.SubsequentStep[Ctx, CtxFn]](api: HooksApi.Secondary[Ctx, CtxFn, Step]): Secondary[Ctx, CtxFn, Step] =
      new Secondary(api)
    protected class Primary[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]) extends X_UseEffect_Primary[Ctx, Step] {
        override def X_useEffect[A](effect: A)(implicit a: UseEffectArg[A], step: Step) =
          api.useEffect(effect)
        override def X_useEffectBy[A](init: Ctx => A)(implicit a: UseEffectArg[A], step: Step) =
          api.useEffectBy(init)
        override def X_useEffectOnMount[A](effect: A)(implicit a: UseEffectArg[A], step: Step) =
          api.useEffectOnMount(effect)
        override def X_useEffectOnMountBy[A](effect: Ctx => A)(implicit a: UseEffectArg[A], step: Step) =
          api.useEffectOnMountBy(effect)
        override def X_useEffectWithDeps[D, A](deps: => D)(effect: D => A)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step) =
          api.useEffectWithDeps(deps)(effect)
        override def X_useEffectWithDepsBy[D, A](deps: Ctx => D)(effect: Ctx => D => A)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step) =
          api.useEffectWithDepsBy(deps)(effect)
    }
    protected class Secondary[Ctx, CtxFn[_], Step <: HooksApi.SubsequentStep[Ctx, CtxFn]](api: HooksApi.Secondary[Ctx, CtxFn, Step]) extends Primary(api) with X_UseEffect_Secondary[Ctx, CtxFn, Step] {
        override def X_useEffectBy[A](init: CtxFn[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
          api.useEffectBy(init)
        override def X_useEffectOnMountBy[A](effect: CtxFn[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
          api.useEffectOnMountBy(effect)
        override def X_useEffectWithDepsBy[D, A](deps: CtxFn[D])(effect: CtxFn[D => A])(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self =
          api.useEffectWithDepsBy(deps)(effect)
    }
  }

  private object UseLayoutEffect extends UseEffectTests {
    override protected implicit def hooksExt1[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]): Primary[Ctx, Step] =
      new Primary(api)
    override protected implicit def hooksExt2[Ctx, CtxFn[_], Step <: HooksApi.SubsequentStep[Ctx, CtxFn]](api: HooksApi.Secondary[Ctx, CtxFn, Step]): Secondary[Ctx, CtxFn, Step] =
      new Secondary(api)
    protected class Primary[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]) extends X_UseEffect_Primary[Ctx, Step] {
        override def X_useEffect[A](effect: A)(implicit a: UseEffectArg[A], step: Step) =
          api.useLayoutEffect(effect)
        override def X_useEffectBy[A](init: Ctx => A)(implicit a: UseEffectArg[A], step: Step) =
          api.useLayoutEffectBy(init)
        override def X_useEffectOnMount[A](effect: A)(implicit a: UseEffectArg[A], step: Step) =
          api.useLayoutEffectOnMount(effect)
        override def X_useEffectOnMountBy[A](effect: Ctx => A)(implicit a: UseEffectArg[A], step: Step) =
          api.useLayoutEffectOnMountBy(effect)
        override def X_useEffectWithDeps[D, A](deps: => D)(effect: D => A)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step) =
          api.useLayoutEffectWithDeps(deps)(effect)
        override def X_useEffectWithDepsBy[D, A](deps: Ctx => D)(effect: Ctx => D => A)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step) =
          api.useLayoutEffectWithDepsBy(deps)(effect)
    }
    protected class Secondary[Ctx, CtxFn[_], Step <: HooksApi.SubsequentStep[Ctx, CtxFn]](api: HooksApi.Secondary[Ctx, CtxFn, Step]) extends Primary(api) with X_UseEffect_Secondary[Ctx, CtxFn, Step] {
        override def X_useEffectBy[A](init: CtxFn[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
          api.useLayoutEffectBy(init)
        override def X_useEffectOnMountBy[A](effect: CtxFn[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
          api.useLayoutEffectOnMountBy(effect)
        override def X_useEffectWithDepsBy[D, A](deps: CtxFn[D])(effect: CtxFn[D => A])(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self =
          api.useLayoutEffectWithDepsBy(deps)(effect)
    }
  }

  private abstract class UseEffectTests {
    protected implicit def hooksExt1[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]): X_UseEffect_Primary[Ctx, Step]
    protected implicit def hooksExt2[Ctx, CtxFn[_], Step <: HooksApi.SubsequentStep[Ctx, CtxFn]](api: HooksApi.Secondary[Ctx, CtxFn, Step]): X_UseEffect_Secondary[Ctx, CtxFn, Step]

    def testSingle(): Unit = {
      val counter1 = new Counter
      val counter2 = new Counter
      def state() = s"${counter1.value}:${counter2.value}"

      val comp = ScalaFnComponent.withHooks[Unit]
        .X_useEffect(counter1.incCB.ret(counter2.incCB))
        .X_useEffect(counter1.incCB(101))
        .X_useEffect(counter1.incCB.ret(counter2.incCB))
        .render(_ => EmptyVdom)

      test(comp()) { _ =>
        assertEq(state(), "103:0")
      }
      assertEq(state(), "103:2")
    }

    def testMonadicSingle(): Unit = {
      val counter1 = new Counter
      val counter2 = new Counter
      def state() = s"${counter1.value}:${counter2.value}"

      val comp = ScalaFnComponent[Unit]( _ =>
        for {
          _ <- useEffect(counter1.incCB.ret(counter2.incCB))
          _ <- useEffect(counter1.incCB(101))
          _ <- useEffect(counter1.incCB.ret(counter2.incCB))
        } yield EmptyVdom
      )

      test(comp()) { _ =>
        assertEq(state(), "103:0")
      }
      assertEq(state(), "103:2")
    }    

    def testConst(): Unit = {
      val counter1 = new Counter
      val counter2 = new Counter
      def state() = s"${counter1.value}:${counter2.value}"

      val comp = ScalaFnComponent.withHooks[Unit]
        .X_useEffect(counter1.incCB.ret(counter2.incCB))
        .X_useEffect(counter1.incCB(101))
        .X_useEffect(counter1.incCB.ret(counter2.incCB))
        .useState(321)
        .render((_, s) => <.button(^.onClick --> s.modState(_ + 1)))

      test(comp()) { t =>
        assertEq(state(), "103:0")
        t.clickButton(); assertEq(state(), "206:2")
      }
      assertEq(state(), "206:4")
    }

   def testMonadicConst(): Unit = {
      val counter1 = new Counter
      val counter2 = new Counter
      def state() = s"${counter1.value}:${counter2.value}"

      val comp = ScalaFnComponent[Unit]( _ =>
        for {
          _ <- useEffect(counter1.incCB.ret(counter2.incCB))
          _ <- useEffect(counter1.incCB(101))
          _ <- useEffect(counter1.incCB.ret(counter2.incCB))
          s <- useState(321)
        } yield <.button(^.onClick --> s.modState(_ + 1))
      )
      

      test(comp()) { t =>
        assertEq(state(), "103:0")
        t.clickButton(); assertEq(state(), "206:2")
      }
      assertEq(state(), "206:4")
    }

    def testConstBy(): Unit = {
      val counter1 = new Counter
      val counter2 = new Counter
      def state() = s"${counter1.value}:${counter2.value}"
      val comp = ScalaFnComponent.withHooks[Unit]
        .useState(100)
        .X_useEffectBy((_, s1) => counter1.incCB(s1.value))
        .X_useEffectBy($ => counter1.incCB.ret(counter2.incCB($.hook1.value)))
        .render((_, s) => <.button(^.onClick --> s.modState(_ + 1)))

      test(comp()) { t =>
        assertEq(state(), "101:0")
        t.clickButton(); assertEq(state(), "203:100")
      }
      assertEq(state(), "203:201")
    }

    def testOnMount(): Unit = {
      val counter1 = new Counter
      val counter2 = new Counter
      def state() = s"${counter1.value}:${counter2.value}"
      val comp = ScalaFnComponent.withHooks[Unit]
        .X_useEffectOnMount(counter1.incCB.ret(counter2.incCB))
        .X_useEffectOnMount(counter1.incCB(101))
        .X_useEffectOnMount(counter1.incCB.ret(counter2.incCB))
        .useState(321)
        .render((_, s) => <.button(^.onClick --> s.modState(_ + 1)))

      test(comp()) { t =>
        assertEq(state(), "103:0")
        t.clickButton(); assertEq(state(), "103:0")
      }
      assertEq(state(), "103:2")
    }

    def testOnMountBy(): Unit = {
      val counter1 = new Counter
      val counter2 = new Counter
      def state() = s"${counter1.value}:${counter2.value}"
      val comp = ScalaFnComponent.withHooks[Unit]
        .useState(100)
        .X_useEffectOnMountBy((_, s1) => counter1.incCB(s1.value))
        .X_useEffectOnMountBy($ => counter1.incCB.ret(counter2.incCB($.hook1.value)))
        .render((_, s) => <.button(^.onClick --> s.modState(_ + 1)))

      test(comp()) { t =>
        assertEq(state(), "101:0")
        t.clickButton(); assertEq(state(), "101:0")
      }
      assertEq(state(), "101:100")
    }

    def testMonadicOnMount(): Unit = {
      val counter1 = new Counter
      val counter2 = new Counter
      def state() = s"${counter1.value}:${counter2.value}"
      val comp = ScalaFnComponent[Unit](_ =>
        for {
          s <- useState(100)
          _ <- useEffectOnMount(counter1.incCB(s.value))
          _ <- useEffectOnMount(counter1.incCB.ret(counter2.incCB(s.value)))
        } yield <.button(^.onClick --> s.modState(_ + 1))
      )

      test(comp()) { t =>
        assertEq(state(), "101:0")
        t.clickButton(); assertEq(state(), "101:0")
      }
      assertEq(state(), "101:100")
    }    

    def testWithDeps(): Unit = {
      val dep1     = new Counter
      val dep2     = new Counter
      val dep3     = new Counter
      val counter1 = new Counter
      val counter2 = new Counter
      def state() = s"${counter1.value}:${counter2.value}"
      val comp = ScalaFnComponent.withHooks[Unit]
        .X_useEffectWithDeps(dep1.value)(_ => counter1.incCB.ret(counter2.incCB))
        .X_useEffectWithDeps(dep2.value)(_ => counter1.incCB(100))
        .X_useEffectWithDeps(dep3.value)(_ => counter1.incCB(10).ret(counter2.incCB(10)))
        .useState(321)
        .render((_, s) => <.button(^.onClick --> s.modState(_ + 1)))

      test(comp()) { t =>
        assertEq(state(), "111:0")
        t.clickButton(); assertEq(state(), "111:0")
        dep2.inc(); t.clickButton(); assertEq(state(), "211:0")
        dep1.inc(); t.clickButton(); assertEq(state(), "212:1")
        dep1.inc(); t.clickButton(); assertEq(state(), "213:2")
        dep3.inc(); t.clickButton(); assertEq(state(), "223:12")
      }
      assertEq(state(), "223:23")
    }

    def testWithDepsBy(): Unit = {
      var _state   = -1
      val dep1     = new Counter
      val dep2     = new Counter
      val dep3     = new Counter
      val counter1 = new Counter
      val counter2 = new Counter
      def state() = s"${_state})${counter1.value}:${counter2.value}"

      val comp = ScalaFnComponent.withHooks[PI]
        .X_useEffectWithDepsBy(_ => dep1.value)(p => _ => counter1.incCB(p.pi).ret(counter2.incCB(p.pi)))
        .useState(100)
        .X_useEffectWithDepsBy((_, _) => dep2.value)((_, s) => _ => counter1.incCB(s.value))
        .X_useEffectWithDepsBy(_ => dep3.value)($ => _ => counter1.incCB($.hook1.value / 10).ret(counter2.incCB($.hook1.value / 10)))
        .render((_, s) => {
          _state = s.value
          <.button(^.onClick --> s.modState(_ + 100))
        })

      test(comp(PI(1000))) { t =>
        assertEq(state(), "100)1110:0")
        t.clickButton(); assertEq(state(), "200)1110:0")
        dep2.inc(); t.clickButton(); assertEq(state(), "300)1410:0")
        dep1.inc(); t.clickButton(); assertEq(state(), "400)2410:1000")
        dep1.inc(); t.clickButton(); assertEq(state(), "500)3410:2000")
        dep3.inc(); t.clickButton(); assertEq(state(), "600)3470:2010") // s'=100, d'=10, s=600, d=60
        dep3.inc(); t.clickButton(); assertEq(state(), "700)3540:2070") // s'=600, d'=60, s=700, d=70
      }
      assertEq(state(), "700)3540:3140") // +1000 +0 +70
    }

    def testMonadicWithDeps(): Unit = {
      var _state   = -1
      val dep1     = new Counter
      val dep2     = new Counter
      val dep3     = new Counter
      val counter1 = new Counter
      val counter2 = new Counter
      def state() = s"${_state})${counter1.value}:${counter2.value}"

      val comp = ScalaFnComponent[PI]( p =>
        for {
          _ <- useEffectWithDeps(dep1.value)(_ => counter1.incCB(p.pi).ret(counter2.incCB(p.pi)))
          s <- useState(100)
          _ <- useEffectWithDeps(dep2.value)(_ => counter1.incCB(s.value))
          _ <- useEffectWithDeps(dep3.value)(_ => counter1.incCB(s.value / 10).ret(counter2.incCB(s.value / 10)))
        } yield {
          _state = s.value
          <.button(^.onClick --> s.modState(_ + 100))
        }
      )

      test(comp(PI(1000))) { t =>
        assertEq(state(), "100)1110:0")
        t.clickButton(); assertEq(state(), "200)1110:0")
        dep2.inc(); t.clickButton(); assertEq(state(), "300)1410:0")
        dep1.inc(); t.clickButton(); assertEq(state(), "400)2410:1000")
        dep1.inc(); t.clickButton(); assertEq(state(), "500)3410:2000")
        dep3.inc(); t.clickButton(); assertEq(state(), "600)3470:2010") // s'=100, d'=10, s=600, d=60
        dep3.inc(); t.clickButton(); assertEq(state(), "700)3540:2070") // s'=600, d'=60, s=700, d=70
      }
      assertEq(state(), "700)3540:3140") // +1000 +0 +70
    }    

  } // UseEffectTests

  private def testUseForceUpdate(): Unit = {
    val counter = new Counter
    val comp = ScalaFnComponent.withHooks[Unit]
      .useForceUpdate
      .render { (_, forceUpdate) =>
        val rev = counter.inc()
        <.div(
          rev, ":", ReusableCallbackComponent(forceUpdate)
        )
      }

    test(comp()) { t =>
      t.assertText("1:1")
      t.clickButton(); t.assertText("2:2")
      t.clickButton(); t.assertText("3:3")
    }
  }

  private def testUseMemo(): Unit = {
    val dep1    = new Counter
    val dep2    = new Counter
    val counter = new Counter

    val comp = ScalaFnComponent.withHooks[Unit]
      .useMemo(dep1.value)(counter.incCB)
      .useMemo(dep2.value)(counter.incCB)
      .useState(0)
      .render((_, c1, c2, s) =>
        <.div(
          "C1=", ReusableCallbackComponent(c1),
          ", C2=", ReusableCallbackComponent(c2),
          <.button(^.onClick --> s.modState(_ + 1)),
        )
      )

    val c1 = 1
    val c2 = 2
    val s  = 3

    dep1.value =  1
    dep2.value = 10
    test(comp()) { t =>
      assertEq(counter.value, 0); t.assertText("C1=1, C2=1")
      t.clickButton(c2); assertEq(counter.value, 10); t.assertText("C1=1, C2=1")
      t.clickButton(c2); assertEq(counter.value, 20); t.assertText("C1=1, C2=1")
      t.clickButton(c1); assertEq(counter.value, 21); t.assertText("C1=1, C2=1")
      t.clickButton(s); assertEq(counter.value, 21); t.assertText("C1=1, C2=1")
      t.clickButton(s); assertEq(counter.value, 21); t.assertText("C1=1, C2=1")
      dep1.value = 17; t.clickButton(s); assertEq(counter.value, 21); t.assertText("C1=2, C2=1")
      dep1.value = 7; t.clickButton(s); assertEq(counter.value, 21); t.assertText("C1=3, C2=1")
      t.clickButton(c2); assertEq(counter.value, 31); t.assertText("C1=3, C2=1")
      t.clickButton(c1); assertEq(counter.value, 38); t.assertText("C1=3, C2=1")
      t.clickButton(s); assertEq(counter.value, 38); t.assertText("C1=3, C2=1")
      dep2.value = 100; t.clickButton(s); assertEq(counter.value, 38); t.assertText("C1=3, C2=2")
      t.clickButton(c2); assertEq(counter.value, 138); t.assertText("C1=3, C2=2")
      t.clickButton(s); assertEq(counter.value, 138); t.assertText("C1=3, C2=2")
      t.clickButton(c1); assertEq(counter.value, 145); t.assertText("C1=3, C2=2")
    }
  }

  private def testUseMemoBy(): Unit = {
    val counter = new Counter
    val comp = ScalaFnComponent.withHooks[PI]
      .useMemoBy(_.pi)(p => _ => counter.incCB(p.pi))
      .useState(1)
      .useMemoBy((_, _, s) => s.value)((_, _, s) => _ => counter.incCB(s.value))
      .useState(5)
      .useMemoBy($ => $.props.pi + $.hook4.value)(_ => counter.incCB)
      .render((_, c1, s2, c2, s3, c3) =>
        <.div(
          "S2=", s2.value,
          ", S3=", s3.value,
          ", C1=", ReusableCallbackComponent(c1),
          ", C2=", ReusableCallbackComponent(c2),
          ", C3=", ReusableCallbackComponent(c3),
          <.button(^.onClick --> s2.modState(_ + 1)),
          <.button(^.onClick --> s3.modState(_ + 1)),
        )
      )

    val c1 = 1 // + p
    val c2 = 2 // + s2
    val c3 = 3 // + p + s3
    val s2 = 4
    val s3 = 5

    test(comp(PI(10))) { t =>
      assertEq(counter.value, 0); t.assertText("S2=1, S3=5, C1=1, C2=1, C3=1")
      t.clickButton(c2); assertEq(counter.value, 1); t.assertText("S2=1, S3=5, C1=1, C2=1, C3=1")
      t.clickButton(c1); assertEq(counter.value, 11); t.assertText("S2=1, S3=5, C1=1, C2=1, C3=1")
      t.clickButton(c3); assertEq(counter.value, 26); t.assertText("S2=1, S3=5, C1=1, C2=1, C3=1")
      t.clickButton(s2); assertEq(counter.value, 26); t.assertText("S2=2, S3=5, C1=1, C2=2, C3=1")
      t.clickButton(c2); assertEq(counter.value, 28); t.assertText("S2=2, S3=5, C1=1, C2=2, C3=1")
      t.clickButton(s3); assertEq(counter.value, 28); t.assertText("S2=2, S3=6, C1=1, C2=2, C3=2")
      t.clickButton(c3); assertEq(counter.value, 44); t.assertText("S2=2, S3=6, C1=1, C2=2, C3=2")
    }
  }

  private def testUseId(): Unit = {
    val comp = ScalaFnComponent.withHooks[Unit]
      .useId
      .render((_, id) => <.div(id))

    test(comp()) { t =>
      assertEq(t.getText.length, 4)
    }
  }

  private def testUseTransition(): Unit = {
   val comp = ScalaFnComponent.withHooks[Unit]
      .useTransition
      .useState(false)
      .render { (_, transition, state) =>
        <.button(
          ^.onClick --> transition.startTransition(state.modState(!_)), 
          state.value.toString()
        )
      }
        
    test(comp()) { t =>
      assertEq(t.getText, "false")
      t.clickButton()
      assertEq(t.getText, "true")
    }
  }

  private def testUseRefManual(): Unit = {
    val comp = ScalaFnComponent.withHooks[Unit]
      .useRef(100)
      .useState(0)
      .render { (_, ref, s) =>
        <.div(
          ref.value,
          <.button(^.onClick --> ref.mod(_ + 1)),
          <.button(^.onClick --> s.modState(_ + 1)),
        )
      }

    test(comp()) { t =>
      t.assertText("100")
      t.clickButton(1); t.assertText("100")
      t.clickButton(2); t.assertText("101")
      t.clickButton(1); t.assertText("101")
      t.clickButton(2); t.assertText("102")
    }
  }

  private def testUseRefManualBy(): Unit = {
    val comp = ScalaFnComponent.withHooks[PI]
      .useRefBy(_.pi + 1)
      .useRefBy($ => $.props.pi + $.hook1.value)
      .useState(0)
      .render { (_, ref1, ref2, s) =>
        <.div(
          s"${ref1.value}:${ref2.value}",
          <.button(^.onClick --> ref1.mod(_ + 1)),
          <.button(^.onClick --> ref2.mod(_ + 1)),
          <.button(^.onClick --> s.modState(_ + 1)),
        )
      }

    test(comp(PI(4))) { t =>
      t.assertText("5:9")
      t.clickButton(1); t.assertText("5:9")
      t.clickButton(3); t.assertText("6:9")
      t.clickButton(2); t.assertText("6:9")
      t.clickButton(3); t.assertText("6:10")
    }
  }

  private def testUseRefVdom(): Unit = {
    var text = "uninitialised"
    val comp = ScalaFnComponent.withHooks[Unit]
      .useRefToVdom[Input]
      .useState("x")
      .render { (_, inputRef, s) =>

        def onChange(e: ReactEventFromInput): Callback =
          s.setState(e.target.value)

        def btn: Callback =
          for {
            i <- inputRef.get.asCBO
            // _ <- Callback.log(s"i.value = [${i.value}]")
          } yield {
            text = i.value
          }

        <.div(
          <.input.text.withRef(inputRef)(^.value := s.value, ^.onChange ==> onChange),
          <.button(^.onClick --> btn)
        )
      }

    test(comp()) { t =>
      t.assertInputText("x")
      t.clickButton()
      assertEq(text, "x")

      t.setInputText("hehe")
      t.assertInputText("hehe")
      t.clickButton()
      assertEq(text, "hehe")
    }
  }

  private def testUseReducer(): Unit = {
    def add(n: Int): (Int, Int) => Int = _ + _ + n
    val comp = ScalaFnComponent.withHooks[PI]
      .useReducer(add(0), 100)
      .useReducerBy((_, s1) => add(s1.value), (p, s1) => p.pi + s1.value)
      .useReducerBy($ => add($.hook1.value), $ => $.props.pi + $.hook1.value + $.hook2.value)
      .render((p, s1, s2, s3) =>
        <.div(
          <.div(s"P=$p, s1=${s1.value}, s2=${s2.value}, s3=${s3.value}"),
          <.button(^.onClick --> s1.dispatch(1)),
          <.button(^.onClick --> s2.dispatch(10)),
          <.button(^.onClick --> s3.dispatch(100)),
      ))

    test(comp(PI(666))) { t =>
      t.assertText("P=PI(666), s1=100, s2=766, s3=1532")
      t.clickButton(1); t.assertText("P=PI(666), s1=101, s2=766, s3=1532")
      t.clickButton(2); t.assertText("P=PI(666), s1=101, s2=877, s3=1532") // +101+10
      t.clickButton(3); t.assertText("P=PI(666), s1=101, s2=877, s3=1733") // +101+100
    }
  }

  private def testUseState(): Unit = {
    val comp = ScalaFnComponent.withHooks[PI]
      .useState(100)
      .useStateBy((p, s1) => p.pi + s1.value)
      .useStateBy($ => $.props.pi + $.hook1.value + $.hook2.value)
      .render((p, s1, s2, s3) =>
        <.div(
          <.div(s"P=$p, s1=${s1.value}, s2=${s2.value}, s3=${s3.value}"),
          <.button(^.onClick --> (
            s1.modState(_ + 1) >> s2.modState(-_) >> s3.modState(_ * 10)
          ))))

    test(comp(PI(666))) { t =>
      t.assertText("P=PI(666), s1=100, s2=766, s3=1532")
      t.clickButton(); t.assertText("P=PI(666), s1=101, s2=-766, s3=15320")
    }
  }

  private def testUseStateSetStateReusability(): Unit = {
    val comp = ScalaFnComponent.withHooks[Unit]
      .useState(4)
      .render { (_, s) =>
        <.div(
          s"S=${s.value}",
          ", R1=", ReusableSetIntComponent(s.setState),
          ", R2=", ReusableSetIntComponent(s.setState),
          ", R3=", ReusableCallbackComponent(s.withReusableInputs.setState(Reusable.implicitly(if (s.value >= 30) -30 else 30))),
          ", R4=", ReusableCallbackComponent(s.withReusableInputs.setState(Reusable.implicitly(40))),
        )
      }

    val r1_1 = 1
    val r1_2 = 2
    val r2_1 = 3
    val r2_2 = 4
    val r3   = 5
    val r4   = 6

    test(comp()) { t =>
      t.assertText("S=4, R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r2_1); t.assertText("S=1, R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r1_2); t.assertText("S=2, R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r1_1); t.assertText("S=1, R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r2_2); t.assertText("S=2, R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r3); t.assertText("S=30, R1=1, R2=1, R3=2, R4=1")
      t.clickButton(r4); t.assertText("S=40, R1=1, R2=1, R3=2, R4=1")
      t.clickButton(r3); t.assertText("S=-30, R1=1, R2=1, R3=3, R4=1")
      t.clickButton(r3); t.assertText("S=30, R1=1, R2=1, R3=4, R4=1")
    }
  }

  private def testUseStateModStateReusability(): Unit = {
    val comp = ScalaFnComponent.withHooks[Unit]
      .useState(4)
      .render { (_, s) =>
        <.div(
          s"S=${s.value}",
          ", R1=", ReusableModIntComponent(s.modState),
          ", R2=", ReusableModIntComponent(s.modState),
          ", R3=", ReusableCallbackComponent(s.withReusableInputs.modState(if (s.value >= 28) incBy5 else incBy1)),
          ", R4=", ReusableCallbackComponent(s.withReusableInputs.modState(incBy1)),
        )
      }

    val r1_1  = 1
    val r1_10 = 2
    val r2_1  = 3
    val r2_10 = 4
    val r3    = 5
    val r4    = 6

    test(comp()) { t =>
      t.assertText("S=4, R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r2_1); t.assertText("S=5, R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r1_10); t.assertText("S=15, R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r1_1); t.assertText("S=16, R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r2_10); t.assertText("S=26, R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r3); t.assertText("S=27, R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r4); t.assertText("S=28, R1=1, R2=1, R3=2, R4=1")
      t.clickButton(r3); t.assertText("S=33, R1=1, R2=1, R3=2, R4=1")
    }
  }

  private def testUseStateWithReuse(): Unit = {
    implicit val reusability: Reusability[PI] = Reusability.by[PI, Int](_.pi >> 1)

    val comp = ScalaFnComponent.withHooks[PI]
      .useStateWithReuse(PI(100))
      .useStateWithReuseBy((p, s1) => p + s1.value)
      .useStateWithReuseBy($ => $.props + $.hook1.value + $.hook2.value)
      .render((p, s1, s2, s3) =>
        <.div(
          <.div(s"P=$p, s1=${s1.value}, s2=${s2.value}, s3=${s3.value}"),
          <.button(^.onClick --> (
            s1.modState(_ + 2) >> s2.modState(-_) >> s3.modState(_ * 10)
          )),
          <.button(^.onClick --> (
            s1.modState(_ + 1) >> s2.modState(-_)
          )),
      ))

    test(comp(PI(666))) { t =>
      t.assertText("P=PI(666), s1=PI(100), s2=PI(766), s3=PI(1532)")
      t.clickButton(1); t.assertText("P=PI(666), s1=PI(102), s2=PI(-766), s3=PI(15320)")
      t.clickButton(2); t.assertText("P=PI(666), s1=PI(102), s2=PI(766), s3=PI(15320)")
    }
  }

  private def testUseStateWithReuseSetStateReusability(): Unit = {
    implicit val reusability: Reusability[PI] = Reusability[PI]((x, y) => (x.pi - y.pi).abs <= 1)

    val comp = ScalaFnComponent.withHooks[Unit]
      .useStateWithReuse(PI(4))
      .render { (_, s) =>
        <.div(
          s"S=${s.value}",
          ", R1=", ReusableSetIntComponent(s.setState.map(f => (i: Int) => f(PI(i)).value)),
          ", R2=", ReusableSetIntComponent(s.setState.map(f => (i: Int) => f(PI(i)).value)),
          ", R3=", ReusableCallbackComponent(s.setState(PI(if (s.value.pi >= 30) -30 else 30))),
          ", R4=", ReusableCallbackComponent(s.setState(PI(40))),
        )
      }

    val r1_1 = 1
    val r1_2 = 2
    val r2_1 = 3
    val r2_2 = 4
    val r3   = 5
    val r4   = 6

    test(comp()) { t =>
      t.assertText("S=PI(4), R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r2_1); t.assertText("S=PI(1), R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r1_2); t.assertText("S=PI(1), R1=1, R2=1, R3=1, R4=1")
      t.clickButton(r3); t.assertText("S=PI(30), R1=1, R2=1, R3=2, R4=1")
      t.clickButton(r4); t.assertText("S=PI(40), R1=1, R2=1, R3=2, R4=1")
      t.clickButton(r2_2); t.assertText("S=PI(2), R1=1, R2=1, R3=3, R4=1")
      t.clickButton(r1_1); t.assertText("S=PI(2), R1=1, R2=1, R3=3, R4=1")
      t.clickButton(r3); t.assertText("S=PI(30), R1=1, R2=1, R3=4, R4=1")
      t.clickButton(r3); t.assertText("S=PI(-30), R1=1, R2=1, R3=5, R4=1")
      t.clickButton(r3); t.assertText("S=PI(30), R1=1, R2=1, R3=6, R4=1")
    }
  }

  private def testUseStateWithReuseModStateReusability(): Unit = {
    implicit val reusability: Reusability[PI] = Reusability.by[PI, Int](_.pi >> 1)
    val comp = ScalaFnComponent.withHooks[Unit]
      .useStateWithReuse(PI(4))
      .render { (_, s) =>
        <.div(
          s"S=${s.value}",
          ", R0=", ReusableCallbackComponent(s.modState(_ + 1)),
          ", R1=", ReusableCallbackComponent(s.modState(_ + 2)),
          ", R2=", ReusableCallbackComponent(s.modState(_ + 4)),
        )
      }

    val inc1 = 1
    val inc2 = 2
    val inc4 = 3

    test(comp()) { t =>
      t.assertText("S=PI(4), R0=1, R1=1, R2=1")
      t.clickButton(inc2); t.assertText("S=PI(6), R0=1, R1=1, R2=1")
      t.clickButton(inc4); t.assertText("S=PI(10), R0=1, R1=1, R2=1")
      t.clickButton(inc1); t.assertText("S=PI(10), R0=1, R1=1, R2=1")
      t.clickButton(inc4); t.assertText("S=PI(14), R0=1, R1=1, R2=1")
      t.clickButton(inc2); t.assertText("S=PI(16), R0=1, R1=1, R2=1")
    }
  }

  private def testUseStateSnapshot(): Unit = {
    val counter = new Counter
    var latestS3 = 0
    val comp = ScalaFnComponent.withHooks[PI]
      .useStateSnapshot(100)
      .useStateSnapshotBy((p, s1) => p.pi + s1.value)
      .useStateSnapshotBy($ => $.props.pi + $.hook1.value + $.hook2.value)
      .render { (p, s1, s2, s3) =>
        latestS3 = s3.value
        <.div(
          <.button(^.onClick --> (s1.modState(_ + 1) >> s2.modState(-_) >> s3.modState(_ * 10))),
          <.button(^.onClick --> s3.modState(_ + 1, CallbackTo(latestS3).flatMap(counter.incCB(_)))),
          <.button(^.onClick --> s3.setStateOption(None, counter.incCB)),
          s"P=$p",
          s", S1=${s1.value}:", ReusableSetIntComponent(s1.underlyingSetFn.map(f => (i: Int) => f(Some(i), Callback.empty))),
          s", S2=${s2.value}:", ReusableSetIntComponent(s1.underlyingSetFn.map(f => (i: Int) => f(Some(i), Callback.empty))),
          s", S3=${s3.value}:", ReusableSetIntComponent(s1.underlyingSetFn.map(f => (i: Int) => f(Some(i), Callback.empty))),
        )
      }

    test(comp(PI(666))) { t =>
      t.assertText("P=PI(666), S1=100:1, S2=766:1, S3=1532:1")
      t.clickButton(1); t.assertText("P=PI(666), S1=101:1, S2=-766:1, S3=15320:1")
      t.clickButton(2); t.assertText("P=PI(666), S1=101:1, S2=-766:1, S3=15321:1")
      assertEq(counter.value, 15321) // verify that the modState(cb) executes after the state update
      t.clickButton(3); t.assertText("P=PI(666), S1=101:1, S2=-766:1, S3=15321:1")
      assertEq(counter.value, 15322) // verify that the setState(None, cb) executes (and that ↖ the previous modState effect ↖ doesn't execute again)
      t.clickButton(3); t.assertText("P=PI(666), S1=101:1, S2=-766:1, S3=15321:1")
      assertEq(counter.value, 15323)
      t.clickButton(2); t.assertText("P=PI(666), S1=101:1, S2=-766:1, S3=15322:1")
      assertEq(counter.value, 15323 + 15322)
      t.clickButton(4); t.assertText("P=PI(666), S1=1:1, S2=-766:1, S3=15322:1")
      assertEq(counter.value, 15323 + 15322)
    }
  }

  private def testUseStateSnapshotWithReuse(): Unit = {
    type I = Int {type A=1}
    implicit val I = Reusability.int.contramap((_: I) >> 1)
    val counter = new Counter
    var latestS3 = 0
    val comp = ScalaFnComponent.withHooks[PI]
      .useStateSnapshotWithReuse(100)
      .useStateSnapshotWithReuseBy((p, s1) => p.pi + s1.value)
      .useStateSnapshotWithReuseBy($ => $.props.pi + $.hook1.value + $.hook2.value)
      .useStateSnapshotWithReuse(330.asInstanceOf[I])
      .render { (p, s1, s2, s3, s4) =>
        latestS3 = s3.value
        <.div(
          <.button(^.onClick --> (s1.modState(_ + 1) >> s2.modState(-_) >> s3.modState(_ * 10))),
          <.button(^.onClick --> s3.modState(_ + 1, CallbackTo(latestS3).flatMap(counter.incCB(_)))),
          <.button(^.onClick --> s3.setStateOption(None, counter.incCB)),
          s"P=$p",
          ", S1=", ReusableStateSnapshotComponent(s1), // buttons: 4 & 5
          ", S2=", ReusableStateSnapshotComponent(s2), // buttons: 6 & 7
          ", S3=", ReusableStateSnapshotComponent(s3), // buttons: 8 & 9
          s", S4=", ReusableStateSnapshotComponent(s4.asInstanceOf[StateSnapshot[Int]]), // buttons: 10 & 11
        )
      }

    test(comp(PI(666))) { t =>
      t.assertText("P=PI(666), S1=100:1, S2=766:1, S3=1532:1, S4=330:1")
      t.clickButton(1); t.assertText("P=PI(666), S1=101:2, S2=-766:2, S3=15320:2, S4=330:1")
      t.clickButton(2); t.assertText("P=PI(666), S1=101:2, S2=-766:2, S3=15321:3, S4=330:1")
      assertEq(counter.value, 15321) // verify that the modState(cb) executes after the state update
      t.clickButton(3); t.assertText("P=PI(666), S1=101:2, S2=-766:2, S3=15321:3, S4=330:1")
      assertEq(counter.value, 15322) // verify that the setState(None, cb) executes (and that ↖ the previous modState effect ↖ doesn't execute again)
      t.clickButton(3); t.assertText("P=PI(666), S1=101:2, S2=-766:2, S3=15321:3, S4=330:1")
      assertEq(counter.value, 15323)
      t.clickButton(2); t.assertText("P=PI(666), S1=101:2, S2=-766:2, S3=15322:4, S4=330:1")
      assertEq(counter.value, 15323 + 15322)
      t.clickButton(4); t.assertText("P=PI(666), S1=102:3, S2=-766:2, S3=15322:4, S4=330:1")
      t.clickButton(4); t.assertText("P=PI(666), S1=103:4, S2=-766:2, S3=15322:4, S4=330:1")
      assertEq(counter.value, 15323 + 15322)
      t.clickButton(11); t.assertText("P=PI(666), S1=103:4, S2=-766:2, S3=15322:4, S4=340:2")
      t.clickButton(10); t.assertText("P=PI(666), S1=103:4, S2=-766:2, S3=15322:4, S4=340:2") // reusability blocks update
      t.clickButton(11); t.assertText("P=PI(666), S1=103:4, S2=-766:2, S3=15322:4, S4=350:3")
    }
  }

  private def testRenderWithReuse(): Unit = {
    implicit val reusability: Reusability[PI] = Reusability.by[PI, Int](_.pi >> 1)
    var renders = 0
    var extState = 5
    val comp = ScalaFnComponent.withHooks[PI]
      .useState(20)
      .useCallback(Callback(extState += 1))
      .useForceUpdate
      .renderWithReuse { (p, s, incES, fu) =>
        renders += 1
        <.div(
          s"P=$p, S=${s.value}, ES=$extState, R=$renders",
          <.button(^.onClick --> s.modState(_ + 1)),
          <.button(^.onClick --> (incES >> fu)),
        )
      }

    val wrapper = ScalaComponent.builder[PI].render_P(comp(_)).build

    withRenderedIntoBody(wrapper(PI(3))) { (m, root) =>
      val t = new DomTester(root)
      t.assertText("P=PI(3), S=20, ES=5, R=1")
      replaceProps(wrapper, m)(PI(2)); t.assertText("P=PI(3), S=20, ES=5, R=1")
      t.clickButton(1); t.assertText("P=PI(2), S=21, ES=5, R=2")
      replaceProps(wrapper, m)(PI(2)); t.assertText("P=PI(2), S=21, ES=5, R=2")
      replaceProps(wrapper, m)(PI(3)); t.assertText("P=PI(2), S=21, ES=5, R=2")
      replaceProps(wrapper, m)(PI(4)); t.assertText("P=PI(4), S=21, ES=5, R=3")
      t.clickButton(2); t.assertText("P=PI(4), S=21, ES=6, R=4")
      replaceProps(wrapper, m)(PI(5)); t.assertText("P=PI(4), S=21, ES=6, R=4")
    }
  }

  // See https://github.com/japgolly/scalajs-react/issues/1027
  private def testRenderWithReuseNever(): Unit = {
    implicit val reusability: Reusability[PI] = Reusability.never
    var renders = 0
    val comp = ScalaFnComponent.withHooks[PI]
      .renderWithReuse { p =>
        renders += 1
        <.div(s"P=$p, R=$renders")
      }

    val wrapper = ScalaComponent.builder[PI]
      .initialStateFromProps(identity)
      .renderS { ($, s) =>
        <.div(
          comp(s),
          <.button(^.onClick --> $.modState(_ + 0)),
          <.button(^.onClick --> $.modState(_ + 1)),
        )
      }
      .build

    test(wrapper(PI(3))) { (t) =>
      t.assertText("P=PI(3), R=1")
      t.clickButton(2); t.assertText("P=PI(4), R=2")
      t.clickButton(1); t.assertText("P=PI(4), R=3")
    }
  }

  private def testRenderWithReuseAndUseRef(): Unit = {
    val comp = ScalaFnComponent.withHooks[Unit]
      .useRef(100)
      .useState(0)
      .renderWithReuse { (_, ref, s) =>
        <.div(
          ref.value,
          <.button(^.onClick --> ref.mod(_ + 1)),
          <.button(^.onClick --> s.modState(_ + 1)),
        )
      }

    test(comp()) { t =>
      t.assertText("100")
      t.clickButton(1); t.assertText("100")
      t.clickButton(2); t.assertText("101")
      t.clickButton(1); t.assertText("101")
      t.clickButton(2); t.assertText("102")
    }
  }

  private def testRenderWithReuseAndUseRefToVdom(): Unit = {
    var text = "uninitialised"
    val comp = ScalaFnComponent.withHooks[Unit]
      .useRefToVdom[Input]
      .useState("x")
      .renderWithReuse { (_, inputRef, s) =>

        def onChange(e: ReactEventFromInput): Callback =
          s.setState(e.target.value)

        def btn: Callback =
          for {
            i <- inputRef.get.asCBO
            // _ <- Callback.log(s"i.value = [${i.value}]")
          } yield {
            text = i.value
          }

        <.div(
          <.input.text.withRef(inputRef)(^.value := s.value, ^.onChange ==> onChange),
          <.button(^.onClick --> btn)
        )
      }

    test(comp()) { t =>
      t.assertInputText("x")
      t.clickButton()
      assertEq(text, "x")

      t.setInputText("hehe")
      t.assertInputText("hehe")
      t.clickButton()
      assertEq(text, "hehe")
    }
  }

  // ===================================================================================================================

  override def tests = Tests {
    "custom" - {
      "usage" - testCustomHook()
      "composition" - testCustomHookComposition()
      "monadic composition" - testCustomMonadicHookComposition()
    }
    "localLazyVal" - testLazyVal()
    "localVal" - testVal()
    "localVar" - testVar()
    "useCallback" - {
      "const" - testUseCallback()
      "constBy" - testUseCallbackBy()
      "deps" - testUseCallbackWithDeps()
      "depsBy" - testUseCallbackWithDepsBy()
    }
    "useCallback (monadic)" - {
      "const" - testMonadicUseCallback()
      "deps" - testMonadicUseCallbackWithDeps()
    }
    "unchecked" - testUnchecked()
    "useContext" - testUseContext()
    "useContext (monadic)" - testMonadicUseContext()
    "useDebugValue" - testUseDebugValue()
    "useEffect" - {
      import UseEffect._
      "single" - testSingle()
      "const" - testConst()
      "constBy" - testConstBy()
      "deps" - testWithDeps()
      "depsBy" - testWithDepsBy()
      "mount" - testOnMount()
      "mountBy" - testOnMountBy()
    }
    "useEffect (monadic)" - {
      import UseEffect._
      "single" - testMonadicSingle()
      "const" - testMonadicConst()
      "deps" - testMonadicWithDeps()
      "mount" - testMonadicOnMount()
    } 
    "useForceUpdate" - testUseForceUpdate()
    "useLayoutEffect" - {
      import UseLayoutEffect._
      "single" - testSingle()
      "const" - testConst()
      "constBy" - testConstBy()
      "deps" - testWithDeps()
      "depsBy" - testWithDepsBy()
      "mount" - testOnMount()
      "mountBy" - testOnMountBy()
    }
    "useMemo" - {
      "deps" - testUseMemo()
      "depsBy" - testUseMemoBy()
    }
    "useRef" - {
      "manual" - testUseRefManual()
      "manualBy" - testUseRefManualBy()
      "vdom" - testUseRefVdom()
    }
    "useReducer" - testUseReducer()
    "useState" - {
      "state" - testUseState()
      "reusability" - {
        "set" - testUseStateSetStateReusability()
        "mod" - testUseStateModStateReusability()
      }
    }
    "useStateWithReuse" - {
      "state" - testUseStateWithReuse()
      "reusability" - {
        "set" - testUseStateWithReuseSetStateReusability()
        "mod" - testUseStateWithReuseModStateReusability()
      }
    }
    "useStateSnapshot" - testUseStateSnapshot()
    "useStateSnapshotWithReuse" - testUseStateSnapshotWithReuse()

    "useId" - testUseId()

    "useTransition" - testUseTransition()

    "renderWithReuse" - {
      "main" - testRenderWithReuse()
      "never" - testRenderWithReuseNever()
      "useRef" - testRenderWithReuseAndUseRef()
      "useRefToVdom" - testRenderWithReuseAndUseRefToVdom()
    }
  }
}
