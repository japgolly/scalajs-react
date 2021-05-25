package example

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._


object Example1 {

  case class P(propsInt: Int)

  // final case class Blah2[P, H1, H2](props: P, hook1: H1, hook2: H2)

  val customHook1 = CustomHook[Int]
    .useState(123)
    .buildReturning(_.hook1)

  val component1a = ScalaFnComponent.withHooks[P]
    .useStateBy(_.propsInt)
    .custom((p, s1) => customHook1(p.propsInt + s1.value))
    .useReducerBy($ => _[Int, Int]((s, a) => s + a, $.hook2.value))
    .render($ => <.div(
      <.div(s"p.propsInt = ${$.props.propsInt}"),
      <.div(s"s1 = ${$.hook1.value}"),
      <.div(s"s2 = ${$.hook2.value}"),
      <.button("Inc S1", ^.onClick --> $.hook1.modState(_ + 1)),
      <.button("Inc S2", ^.onClick --> $.hook2.modState(_ + 1)),
    ))

  val component1b = ScalaFnComponent.withHooks[P]
    .useStateBy(_.propsInt)
    .useStateBy((p, s1) => p.propsInt + s1.value)
    .render((p, s1, s2) => <.div(
      <.div(s"p.propsInt = ${p.propsInt}"),
      <.div(s"s1 = ${s1.value}"),
      <.div(s"s2 = ${s2.value}"),
      <.button("Inc S1", ^.onClick --> s1.modState(_ + 1)),
      <.button("Inc S2", ^.onClick --> s2.modState(_ + 1)),
    ))




  ScalaFnComponent.withHooks[P]
    .useStateBy(_.propsInt)

    .useEffect(Callback.empty)
    // .useEffect($ => Callback.log($.props.propsInt))
    .useEffectBy($ => _(Callback.log($.props.propsInt)))

    .useEffect(Callback.empty, 1)
    .useEffectBy($ => _(Callback.log($.props.propsInt), 1))

    .useEffectOnMount(Callback.empty)
    .useEffectOnMountBy($ => Callback.log($.props.propsInt))

    .useLayoutEffect(Callback.empty)
    // .useLayoutEffect($ => Callback.log($.props.propsInt))
    .useLayoutEffectBy($ => _(Callback.log($.props.propsInt)))

    .useLayoutEffect(Callback.empty, 1)
    .useLayoutEffectBy($ => _(Callback.log($.props.propsInt), 1))

    .useLayoutEffectOnMount(Callback.empty)
    .useLayoutEffectOnMountBy($ => Callback.log($.props.propsInt))

    .custom(customHook1(123))
    .custom(ctx => customHook1(ctx.hook1.value))

    .useCallback(Callback.empty)
    .useCallback(Callback.empty, 123)
    .useCallbackBy($ => _(Callback.log($.props.propsInt)))
    .useCallbackBy($ => _(Callback.log($.props.propsInt), $.props.propsInt))

  ScalaFnComponent.withHooks[P]
    .useCallbackBy(_ => _((i: Int) => Callback.log(i)))
    .useCallbackBy($ => _((i: Int) => Callback.log(i), $.props.propsInt))
    .useCallback((i: Int) => Callback.log(i))

    .useRef[Int]
    .useRef(123)






  //   .useEffect(Callback.log("HELLO!"))
  //   .useEffect($ => Callback.log(s"Props = ${$.props}, state = ${$.hook1.state}"))
  //   .useEffect((_, counter) => Callback.log(s"State = ${counter.state}"))
  //   // .useEffect_P(p => Callback.log(s"State = ${counter.state}"))
  //   .render { case (p, counter, ()) =>
  //     <.button(
  //       s"Current value is ${counter.state}. Press to increment",
  //       ^.onClick --> counter.modState(_ + 1))
  //   }

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

// sealed trait HookStructure[A, B] {
//   // def ++[C, D](h: HookStructure[C, D]): HookStructure[] =
//   //   ???

//   val prepare: (Hooks.Dsl, A) => B
// }
// object HookStructure {

//   def tuple2[A1,B1,A2,B2](x: HookStructure[A1, B1], y: HookStructure[A2, B2]): HookStructure[(A1, A2), (B1, B2)] =
//     new HookStructure[(A1, A2), (B1, B2)] {
//       override val prepare = { case (dsl, (a1, a2)) =>
//         (x.prepare(dsl, a1), y.prepare(dsl, a2))
//       }
//     }

//   final case class UseState[S]() extends HookStructure[S, Hooks.UseState[S]] {
//     override val prepare = _.useState(_)
//   }
// final class StateSnapshot[S](val state: S) extends StateAccess.Write[CallbackTo, S] { }

//   final case class UseStateSnapshot[S]() extends HookStructure[S, StateSnapshot[S]] {
//     override val prepare = ???
//   }

//   case object UseEffect extends HookStructure[Callback, Unit] {
//     override val prepare = _.useEffect(_)
//   }
// }

// object Hooks2 {
//   type Component = Any

//   def apply[A, B](s: HookStructure[A, B])(hookInputs: A)(f: B => VdomNode): Component =
//     ScalaFnComponent2.withHooks { dsl =>
//       f(s.prepare(dsl, hookInputs))
//     }

//   // TODO: Gonna need to be smarter to handle composed/custom HookStructures
//   def apply[A1,B1,A2,B2](h1: HookStructure[A1, B1], h2: HookStructure[A2, B2])
//                          (hookInput1: A1,
//                           hookInput2: B1 => A2)
//                          (f: (B1,B2) => VdomNode): Component =
//     ScalaFnComponent2.withHooks { dsl =>
//       val b1 = h1.prepare(dsl, hookInput1)
//       val b2 = h2.prepare(dsl, hookInput2(b1))
//       f(b1, b2)
//     }
// }

// object Playground2 {

//   object Simple1 {
//     val structure = HookStructure.UseState[Int]()
//     val compo = Hooks2(structure)(123) { counter =>
//       if (scala.util.Random.nextBoolean()) {
//         <.div("RANDOMLY DISABLED!")
//       } else {
//         <.button(
//           s"Current value is ${counter.state}. Press to increment",
//           ^.onClick --> counter.modState(_ + 1))
//       }
//     }
//   }

//   object TwoSetStates {
//     val structure = HookStructure.tuple2(
//       HookStructure.UseState[Int](),
//       HookStructure.UseState[String](),
//     )
//     def initialStates = (0, "")
//     val component = Hooks2(structure)(initialStates) { case (changeCounter, name) =>
//       val onChange: ReactEventFromInput => Callback = e => {
//         val newName = e.target.value
//         name.setState(newName) >> changeCounter.modState(_ + 1)
//       }
//       <.div(
//         <.p("Change count: ", changeCounter.state),
//         <.input.text(
//           ^.value := name.state,
//           ^.onChange ==> onChange))
//     }
//   }

//   object SetStateAndUseEffect {
//     val component = Hooks2(
//       HookStructure.UseState[Int](),
//       HookStructure.UseEffect,
//     )(
//       0, // useState
//       counter => Callback.log("State = ", counter.state),
//     ) { case (counter, ()) =>
//       <.button(
//         s"Current value is ${counter.state}. Press to increment",
//         ^.onClick --> counter.modState(_ + 1))
//     }
//   }

//   object SetStateAndUseEffect_IMAGINE1 {
//     val component = Hooks2(
//       HookStructure.UseState[Int],
//       HookStructure.UseEffect,
//     )
//       .initialiseHook1(0)
//       .initialiseHook2(counter => Callback.log("State = ", counter.state))
//       .render { case (counter, ()) =>
//         <.button(
//           s"Current value is ${counter.state}. Press to increment",
//           ^.onClick --> counter.modState(_ + 1))
//       }
//   }

//   object SetStateAndUseEffect_IMAGINE2 {
//     val component = ScalaFnComponent.buiderWithHooks[P]
//       .useState[Int](p => 0)
//       .useEffect((p, counter) => Callback.log("State = ", counter.state))
//       .render { case (p, counter, ()) =>
//         <.button(
//           s"Current value is ${counter.state}. Press to increment",
//           ^.onClick --> counter.modState(_ + 1))
//       }
//   }
