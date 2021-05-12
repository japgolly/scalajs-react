package japgolly.scalajs

import japgolly.scalajs.react.{raw => Raw, _}
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.console
import scala.scalajs.js
import scala.scalajs.js.|

/*
- Only call Hooks at the top level. Don’t call Hooks inside loops, conditions, or nested functions.
- Only call Hooks from React function components.
*/

trait ScalaFnComponent2[P] {
  def render: VdomNode = ???
}

object ScalaFnComponent2 {

  // final def useLayoutEffect(effect: js.Function0[js.UndefOr[js.Function0[Any]]],
  //                           deps  : js.UndefOr[HookDeps] = js.native): Unit = js.native
  // final def useContext[A <: js.Any](ctx: React.Context[A]): React.Context[A] = js.native
  // final type UseReducer[S, A] = js.Tuple2[S, js.Function1[A, Unit]]
  // final def useReducer[   S, A](reducer: js.Function2[S, A, S], initialArg: S                          ): UseReducer[S, A] = js.native
  // final def useReducer[I, S, A](reducer: js.Function2[S, A, S], initialArg: I, init: js.Function1[I, S]): UseReducer[S, A] = js.native
  // final def useCallback(callback: js.Function0[Unit], deps: js.UndefOr[HookDeps] = js.native): js.Function0[Unit] = js.native
  // final def useMemo[A](f: js.Function0[A], deps: js.UndefOr[HookDeps] = js.native): js.Function0[A] = js.native
  // final def useRef[A](f: js.Function0[A], deps: js.UndefOr[HookDeps] = js.native): HookRef[A] = js.native
  // final def useDebugValue(desc: js.Any): Unit = js.native
  // final def useDebugValue[A](value: A, desc: A => js.Any): Unit = js.native

  @inline def withHooks(f: HooksDsl => VdomNode): ScalaFnComponent2[Unit] =
    withHooks[Unit]((_, h) => f(h))

  def withHooks[P](f: (P, HooksDsl) => VdomNode): ScalaFnComponent2[P] =
    ???
}

// TODO: Add Reusability[S]
// TODO: StateSnapshot
final case class UseState[S](raw: Raw.React.UseState[S]) {

  def withReusability(implicit r: Reusability[S]): UseState[S] =
    ???

  val state: S =
    raw._1

  def setState(s: S): Callback =
    Callback(raw._2(s))

  def modState(f: S => S): Callback =
    Callback(raw._2(f: js.Function1[S, S]))

  def xmap[T](f: S => T)(g: T => S): UseState[T] = {
    val newSetState: js.Function1[T | js.Function1[T, T], Unit] =
      tOrFn => {
        if (js.typeOf(tOrFn) == "function") {
          val mod = tOrFn.asInstanceOf[js.Function1[T, T]]
          mod(f(state))
        } else {
          val t = tOrFn.asInstanceOf[T]
          g(t)
        }
      }
    UseState[T](js.Tuple2(f(state), newSetState))
  }
}

trait HooksDsl {

  def useState[S](initial: S): UseState[S] =
    UseState[Box[S]](Raw.React.useState(Box(initial)))
      .xmap(_.unbox)(Box.apply)

  def useStateLazily[S](initial: => S): UseState[S] = {
    val initJs: js.Function0[Box[S]] =
      () => Box(initial)
    UseState[Box[S]](Raw.React.useState(initJs))
      .xmap(_.unbox)(Box.apply)
  }

  // TODO: Use @implicitNotFound to explain errors
  trait UseEffectParam[A]
  object UseEffectParam {
    implicit val unit    : UseEffectParam[Unit]     = new UseEffectParam[Unit]{}
    implicit val callback: UseEffectParam[Callback] = new UseEffectParam[Callback]{}
  }

  // TODO: Clarify the sig in doc
  def useEffect[A](e: CallbackTo[A])(implicit x: UseEffectParam[A]): Unit = ???

  // TODO: rename to something clearer
  final case class HookDep(raw: js.Any)

  def useEffectWithDeps1[A](e: CallbackTo[A])(deps: HookDep*)(implicit x: UseEffectParam[A]): Unit = ???
  def useEffectWithDeps2[A](e: CallbackTo[A])(deps: js.Any*)(implicit x: UseEffectParam[A]): Unit = ???

  /*
  def useEffect(e: Callback): Unit = ???
  def useEffect(e: CallbackTo[Callback]): Unit = ???
  */

  // def useEffect(effect: js.Function0[js.UndefOr[js.Function0[Any]]],
  //               // deps  : js.UndefOr[HookDeps] = js.native
  //               ): Unit = js.native

  // What do we need for hook deps?
  //   * need to know that "hook dep comparison semantics" are what users expect

  // TODO: Put in doc somewhere: hook dependencies are compared using Object.is
  // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/is
  // Objects specifically, are compared by reference.

} // HooksDsl

object CustomHook {
  type CustomHook[A] = HooksDsl => A

  def useFriendStatus(friendID: Any): CustomHook[Boolean] =
    $ => {
      val _ = friendID
      val online = $.useState(false).withReusability
      online.state
    }
}
import CustomHook.CustomHook

object UseFriendStatus2 {
  private def apply(friendID: Any): CustomHook[Boolean] =
    $ => {
      val _ = friendID
      val online = $.useState(false)
      online.state
    }

  @inline implicit class UseFriendStatusExt(private val $: HooksDsl) extends AnyVal {
    def useFriendStatus(friendID: Any) = apply(friendID)($)
  }
}

object Test {

  final case class Props(name: String, age: Int)

  val myComponentWithProps = ScalaFnComponent2.withHooks[Props] { (props, $) =>

    val effect = Callback {
      console.log("")
    }

    // $.useEffectWithDeps1()

    <.div(s"Hello ${props.name}! I see that you're ${props.age} yrs old.")
  }

  val myComponentNoProps = ScalaFnComponent2.withHooks { $ =>
    import UseFriendStatus2._

    val counter = $.useState(0)

    val friendStatus1 = CustomHook.useFriendStatus(1234)($)
    val friendStatus2 = $.useFriendStatus(1234)

    <.div(
      "friendStatus1 = ", (if (friendStatus1) "Online" else "Offline"),
      "friendStatus2 = ", (if (friendStatus2) "Online" else "Offline"),
      "count = ", counter.state,
      <.button("+", ^.onClick --> counter.modState(_ + 1)),
      <.button("reset", ^.onClick --> counter.setState(0)),
    )
  }

}

/*

type Reusability[A] = (A, A) => Boolean
type ToHookDef[A]   = A => js.Any

case class P(name: String, x: Option[Int])

*/