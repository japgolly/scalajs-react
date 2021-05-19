import japgolly.scalajs.react._
import japgolly.scalajs.react.feature.Hooks
import japgolly.scalajs.react.vdom.html_<^._

/*
// TODO: Only call Hooks at the top level. Don’t call Hooks inside loops, conditions, or nested functions.
//   - idea 1: macro
//   - idea 2: applic
//   - idea 3: separate static structure
//       - Hx + Hy = H(xy), (h: H) => [P]       => (P, h.Blah)    => VdomNode
//       - Hx + Hy = H(xy), (h: H) => [F[_], P] => (P, h.Blah[F]) => F[VdomNode]
//   - Really don't want to over-engineer this :/
//   - PROBLEM: custom hooks vs normal fns
//   - Value in purifying? Technically the right thing to do. More info in types = more power, more informed
//     Also covers cases where $ provided to helper class and methods execute side-effects
//     Initally thought this is just a tiny little scope safe for impurity but in reality calling outer functions
//       is for-sure gonna happen, and probably won't be long before ppl start abstracting over hook creation/init stuff
//   - Cases
//     - if
//     - lazy vals
//     - vars
//     - CallbackTo(_).when().runNow()
//     - put calls in a fn and then use those fns in if (same as custom hooks) if (cond) customHooks1() else customHooks2()
//     - many more
//   - Purpose: help avoid reasonable mistakes, not prevent malice
//   - Too restrictive if implemented? Escape hatch?
*/

sealed trait HookStructure[A, B] {
  // def ++[C, D](h: HookStructure[C, D]): HookStructure[] =
  //   ???

  val prepare: (Hooks.Dsl, A) => B
}
object HookStructure {

  def tuple2[A1,B1,A2,B2](x: HookStructure[A1, B1], y: HookStructure[A2, B2]): HookStructure[(A1, A2), (B1, B2)] =
    new HookStructure[(A1, A2), (B1, B2)] {
      override val prepare = { case (dsl, (a1, a2)) =>
        (x.prepare(dsl, a1), y.prepare(dsl, a2))
      }
    }

  final case class UseState[S]() extends HookStructure[S, Hooks.UseState[S]] {
    override val prepare = _.useState(_)
  }
final class StateSnapshot[S](val state: S) extends StateAccess.Write[CallbackTo, S] { }

  final case class UseStateSnapshot[S]() extends HookStructure[S, StateSnapshot[S]] {
    override val prepare = ???
  }

  case object UseEffect extends HookStructure[Callback, Unit] {
    override val prepare = _.useEffect(_)
  }
}

object Hooks2 {
  type Component = Any

  def apply[A, B](s: HookStructure[A, B])(hookInputs: A)(f: B => VdomNode): Component =
    ScalaFnComponent2.withHooks { dsl =>
      f(s.prepare(dsl, hookInputs))
    }

  // TODO: Gonna need to be smarter to handle composed/custom HookStructures
  def apply[A1,B1,A2,B2](h1: HookStructure[A1, B1], h2: HookStructure[A2, B2])
                         (hookInput1: A1,
                          hookInput2: B1 => A2)
                         (f: (B1,B2) => VdomNode): Component =
    ScalaFnComponent2.withHooks { dsl =>
      val b1 = h1.prepare(dsl, hookInput1)
      val b2 = h2.prepare(dsl, hookInput2(b1))
      f(b1, b2)
    }
}

object Playground2 {

  object Simple1 {
    val structure = HookStructure.UseState[Int]()
    val compo = Hooks2(structure)(123) { counter =>
      if (scala.util.Random.nextBoolean()) {
        <.div("RANDOMLY DISABLED!")
      } else {
        <.button(
          s"Current value is ${counter.state}. Press to increment",
          ^.onClick --> counter.modState(_ + 1))
      }
    }
  }

  object TwoSetStates {
    val structure = HookStructure.tuple2(
      HookStructure.UseState[Int](),
      HookStructure.UseState[String](),
    )
    def initialStates = (0, "")
    val component = Hooks2(structure)(initialStates) { case (changeCounter, name) =>
      val onChange: ReactEventFromInput => Callback = e => {
        val newName = e.target.value
        name.setState(newName) >> changeCounter.modState(_ + 1)
      }
      <.div(
        <.p("Change count: ", changeCounter.state),
        <.input.text(
          ^.value := name.state,
          ^.onChange ==> onChange))
    }
  }

  object SetStateAndUseEffect {
    val component = Hooks2(
      HookStructure.UseState[Int](),
      HookStructure.UseEffect,
    )(
      0, // useState
      counter => Callback.log("State = ", counter.state),
    ) { case (counter, ()) =>
      <.button(
        s"Current value is ${counter.state}. Press to increment",
        ^.onClick --> counter.modState(_ + 1))
    }
  }

  object SetStateAndUseEffect_IMAGINE1 {
    val component = Hooks2(
      HookStructure.UseState[Int],
      HookStructure.UseEffect,
    )
      .initialiseHook1(0)
      .initialiseHook2(counter => Callback.log("State = ", counter.state))
      .render { case (counter, ()) =>
        <.button(
          s"Current value is ${counter.state}. Press to increment",
          ^.onClick --> counter.modState(_ + 1))
      }
  }

  object SetStateAndUseEffect_IMAGINE2 {
    val component = ScalaFnComponent.buiderWithHooks[P]
      .useState[Int](p => 0)
      .useEffect((p, counter) => Callback.log("State = ", counter.state))
      .render { case (p, counter, ()) =>
        <.button(
          s"Current value is ${counter.state}. Press to increment",
          ^.onClick --> counter.modState(_ + 1))
      }
  }

  object SetStateAndUseEffect_IMAGINE2b {

    case class P()

    final case class Blah2[P, H1, H2](props: P, hook1: H1, hook2: H2)

    val component = ScalaFnComponent.buiderWithHooks[P]
      .useState[Int](p => 0)
      .useEffect(Callback.log("HELLO!"))
      .useEffect($ => Callback.log(s"Props = ${$.props}, state = ${$.hook1.state}"))
      .useEffect((_, counter) => Callback.log(s"State = ${counter.state}"))
      // .useEffect_P(p => Callback.log(s"State = ${counter.state}"))
      .render { case (p, counter, ()) =>
        <.button(
          s"Current value is ${counter.state}. Press to increment",
          ^.onClick --> counter.modState(_ + 1))
      }

      // .useEffect(p => Callback.log(s"Props = $p"))
      // .useEffect((_, counter) => Callback.log(s"State = ${counter.state}"))
      // .useEffect.withHook2(counter => Callback.log(s"State = ${counter.state}"))
      // .render { ($: Blah2[P, Hooks.UseState[Int], Unit]) =>
      //   import $.{props, hook1 => counter}
      //   <.button(
      //     s"Props = $props, Current value is ${counter.state}. Press to increment",
      //     ^.onClick --> counter.modState(_ + 1))
      // }
  }

}

















































/* AGENDA:
// TODO: useState set callbacks are Reusable.byRef + plus add SSnap
*/

object ScalaFnComponent2 {
  @inline def withHooks(f: Hooks.Dsl => VdomNode) =
    ??? // withHooks[Unit]((_, h) => f(h))

  // def withHooks[P](f: (P, HooksDsl) => VdomNode): ScalaFnComponent2[P] =
  //   ???
}

object Playground {

  val blah = ScalaFnComponent2.withHooks { hooks =>

    val counter = hooks.useState(0).withReusability

    counter.setState(123)

    ???
  }

  object IncButton {
    final case class Props(update: Reusable[Callback])

    implicit val reusability = Reusability.derive[Props]

    def render(p: Props): VdomNode =
      <.button("Increment", ^.onClick --> p.update)

    val Component = ScalaComponent.builder[Props]
      .render_P(render)
      .configure(Reusability.shouldComponentUpdate)
      .build
  }


}