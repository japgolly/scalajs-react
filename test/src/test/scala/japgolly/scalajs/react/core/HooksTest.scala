package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html.{Button, Element}
import sourcecode.Line
import utest._

object HooksTest extends TestSuite {

  private val tagRegex = "</?[a-z]+>".r

  private def getText(e: Element): String =
    tagRegex.replaceAllIn(e.innerHTML, "").trim

  private def assertText(e: Element, expect: String)(implicit l: Line): Unit =
    assertEq(getText(e), expect)

  private def test[M, A](u: Unmounted[M])(f: Tester => A): A =
    withRenderedIntoBody(u).withParent(root => f(new Tester(root)))

  private class Tester(root: Element) {

    def assertText(expect: String)(implicit l: Line): Unit =
      HooksTest.assertText(root, expect)

    def clickButton(n: Int = 1): Unit = {
      val bs = root.querySelectorAll("button")
      Predef.assert(n > 0 && n <= bs.length, s"${bs.length} buttons found (n=$n)")
      val b = bs(n - 1).asInstanceOf[Button]
      act(Simulate.click(b))
    }
  }

  private val incBy1 = Reusable.byRef((_: Int) + 1)
  private val incBy5 = Reusable.byRef((_: Int) + 5)

  private final class Counter {
    var value = 0
    def inc(by: Int = 1): Int = {
      value += by
      value
    }
    def incCB         : Callback = incCB(1)
    def incCB(by: Int): Callback = Callback{ inc(by); () }
  }

  private final case class PI(pi: Int) {
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
      .initialState(1)
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
      .initialState(1)
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
      .initialState(1)
      .renderBackend[ReusableCallbackComponent]
      .configure(Reusability.shouldComponentUpdate)
      .build

  // ===================================================================================================================

  private def testLazyVal(): Unit = {
    val counter = new Counter
    val comp = ScalaFnComponent.withHooks[PI]
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

    test(comp(PI(10))) { t =>
      t.assertText("P=PI(10), v1=101, v2=112, v3=113")
      t.clickButton(); t.assertText("P=PI(10), v1=104, v2=115, v3=116")
    }
  }

  private def testUseEffect(): Unit = {
    val counter1 = new Counter
    val counter2 = new Counter
    def state() = s"${counter1.value}:${counter2.value}"
    val comp = ScalaFnComponent.withHooks[Unit]
      .useEffect(counter1.incCB(100))
      .useEffect(counter1.incCB.ret(counter2.incCB))
      .useState(321)
      .render((_, s) => <.button(^.onClick --> s.modState(_ + 1)))

    test(comp()) { t =>
      assertEq(state(), "101:0")
      t.clickButton()
      assertEq(state(), "202:1")
    }
  }

  private def testUseEffectBy(): Unit = {
    val counter1 = new Counter
    val counter2 = new Counter
    def state() = s"${counter1.value}:${counter2.value}"
    val comp = ScalaFnComponent.withHooks[Unit]
      .useState(100)
      .useEffectBy((_, s1) => counter1.incCB(s1.value))
      .useEffectBy($ => counter1.incCB.ret(counter2.incCB($.hook1.value)))
      .render((_, s) => <.button(^.onClick --> s.modState(_ + 1)))

    test(comp()) { t =>
      assertEq(state(), "101:0")
      t.clickButton()
      assertEq(state(), "203:100")
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
    implicit val reusability = Reusability.by[PI, Int](_.pi >> 1)

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
    implicit val reusability = Reusability[PI]((x, y) => (x.pi - y.pi).abs <= 1)

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
    implicit val reusability = Reusability.by[PI, Int](_.pi >> 1)
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


  // ===================================================================================================================

  override def tests = Tests {
    "localLazyVal" - testLazyVal()
    "localVal"     - testVal()
    "localVar"     - testVar()
    "useEffect" - {
      "const" - testUseEffect()
      "by" - testUseEffectBy()
    }
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
  }
}

// final def useCallback[A, D](callback: A, deps: D)(implicit a: UseCallbackArg[A], r: Reusability[D], step: Step): step.Next[Reusable[A]] =
// final def useCallback[A](callback: A)(implicit a: UseCallbackArg[A], step: Step): step.Next[Reusable[A]] =
// final def useCallbackBy[A](f: Ctx => UseCallbackInline => HookCreated[Reusable[A]])(implicit step: Step): step.Next[Reusable[A]] =
// final def useCallbackBy[A](f: CtxFn[UseCallbackInline => HookCreated[Reusable[A]]])(implicit step: Step): step.Next[Reusable[A]] =

// final def useContext[A](ctx: Context[A])(implicit step: Step): step.Next[A] =
// final def useContextBy[A](f: Ctx => Context[A])(implicit step: Step): step.Next[A] =
// final def useContextBy[A](f: CtxFn[Context[A]])(implicit step: Step): step.Next[A] =

// final def useDebugValue(desc: => Any)(implicit step: Step): step.Self =
// final def useDebugValueBy(desc: Ctx => Any)(implicit step: Step): step.Self =
// final def useDebugValueBy(f: CtxFn[Any])(implicit step: Step): step.Self =

// final def useEffect[A, D](effect: CallbackTo[A], deps: D)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self =
// final def useEffectBy(init: Ctx => UseEffectInline => HookCreated[Unit])(implicit step: Step): step.Self =
// final def useEffectBy(init: CtxFn[UseEffectInline => HookCreated[Unit]])(implicit step: Step): step.Self =
// final def useEffectOnMount[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
// final def useEffectOnMountBy[A](effect: Ctx => CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
// final def useEffectOnMountBy[A](effect: CtxFn[CallbackTo[A]])(implicit a: UseEffectArg[A], step: Step): step.Self =

// final def useLayoutEffect[A, D](effect: CallbackTo[A], deps: D)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self =
// final def useLayoutEffect[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
// final def useLayoutEffectBy(init: Ctx => UseLayoutEffectInline => HookCreated[Unit])(implicit step: Step): step.Self =
// final def useLayoutEffectBy(init: CtxFn[UseLayoutEffectInline => HookCreated[Unit]])(implicit step: Step): step.Self =
// final def useLayoutEffectOnMount[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
// final def useLayoutEffectOnMountBy[A](effect: Ctx => CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
// final def useLayoutEffectOnMountBy[A](effect: CtxFn[CallbackTo[A]])(implicit a: UseEffectArg[A], step: Step): step.Self =

// final def useMemo[A, D](create: => A, deps: D)(implicit r: Reusability[D], step: Step): step.Next[A] =
// final def useMemoBy[A](f: Ctx => UseMemo.type => UseMemo[A])(implicit step: Step): step.Next[A] =
// final def useMemoBy[A](f: CtxFn[UseMemo.type => UseMemo[A]])(implicit step: Step): step.Next[A] =

// final def useReducer[I, S, A](reducer: (S, A) => S, initialArg: I, init: I => S)(implicit step: Step): step.Next[UseReducer[S, A]] =
// final def useReducer[S, A](reducer: (S, A) => S, initialArg: S)(implicit step: Step): step.Next[UseReducer[S, A]] =
// final def useReducerBy[S, A](init: Ctx => UseReducerInline => HookCreated[UseReducer[S, A]])(implicit step: Step): step.Next[UseReducer[S, A]] =
// final def useReducerBy[S, A](init: CtxFn[UseReducerInline => HookCreated[UseReducer[S, A]]])(implicit step: Step): step.Next[UseReducer[S, A]] =

// final def useRef[A](implicit step: Step): step.Next[Ref.Simple[A]] =
// final def useRef[A](initialValue: => A)(implicit step: Step): step.Next[Ref.NonEmpty.Simple[A]] =
// final def useRefBy[A](f: CtxFn[A])(implicit step: Step): step.Next[Ref.NonEmpty.Simple[A]] =
// final def useRefBy[A](initialValue: Ctx => A)(implicit step: Step): step.Next[Ref.NonEmpty.Simple[A]] =

// final def custom_(hook: Ctx => CustomHook[Unit, Unit])(implicit step: Step): step.Self =
// final def custom_(hook: CtxFn[CustomHook[Unit, Unit]])(implicit step: Step): step.Self =
// final def custom_[I](hook: CustomHook[I, Unit])(implicit step: Step, a: CustomHook.Arg[Ctx, I]): step.Self =
// final def custom[I, O](hook: CustomHook[I, O])(implicit step: Step, a: CustomHook.Arg[Ctx, I]): step.Next[O] =
// final def custom[O](hook: Ctx => CustomHook[Unit, O])(implicit step: Step): step.Next[O] =
// final def custom[O](hook: CtxFn[CustomHook[Unit, O]])(implicit step: Step): step.Next[O] =

// final def unchecked_(f: Ctx => Any)(implicit step: Step): step.Self =
// final def unchecked_(f: CtxFn[Any])(implicit step: Step): step.Self =
// final def unchecked[A](f: Ctx => A)(implicit step: Step): step.Next[A] =
// final def unchecked[A](f: CtxFn[A])(implicit step: Step): step.Next[A] =

// SS hook too
