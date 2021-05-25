package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.feature.Context
import japgolly.scalajs.react.internal.{Box, NotAllowed, OptionLike}
import japgolly.scalajs.react.{Callback, React => _, Reusability, Reusable, raw => Raw, _}
import scala.annotation.implicitNotFound
import scala.scalajs.js
import scala.scalajs.js.|

object Hooks {

  trait UseCallbackArg[S] {
    type J <: js.Function
    def toJs: S => J
    def fromJs: J => Reusable[S]
  }

  object UseCallbackArg extends UseCallbackArgInstances {

    def apply[S, F <: js.Function](f: S => F)(g: F => Reusable[S]): UseCallbackArg[S] =
      new UseCallbackArg[S] {
        override type J = F
        override def toJs = f
        override def fromJs = g
      }

    implicit def c: UseCallbackArg[Callback] =
      apply[Callback, js.Function0[Unit]](
        _.toJsFn)(
        f => Reusable.byRef(f).withValue(Callback.fromJsFn(f)))
  }

  // ===================================================================================================================

  object UseContext {
    def unsafeCreate[A](ctx: Context[A]): A = {
      val rawValue = Raw.React.useContext(ctx.raw)
      ctx.jsRepr.fromJs(rawValue)
    }
  }

  // ===================================================================================================================

  object UseDebugValue {
    def unsafeCreate(desc: => Any): Unit =
      Raw.React.useDebugValue[Null](null, _ => desc)
  }

  // ===================================================================================================================

  @implicitNotFound(
    "You're attempting to provide a CallbackTo[${A}] to the useEffect family of hooks."
    + "\n  - To specify a basic effect, provide a Callback (protip: try adding .void to your callback)."
    + "\n  - To specify an effect and a clean-up effect, provide a CallbackTo[Callback] where the Callback you return is the clean-up effect."
    + "\nSee https://reactjs.org/docs/hooks-reference.html#useeffect")
  final case class UseEffectArg[A](toJs: CallbackTo[A] => Raw.React.UseEffectArg)

  object UseEffectArg {
    implicit val unit: UseEffectArg[Unit] =
      apply(_.toJsFn)

    def byCallback[A](f: A => js.UndefOr[js.Function0[Any]]): UseEffectArg[A] =
      apply(_.map(f).toJsFn)

    implicit val callback: UseEffectArg[Callback] =
      byCallback(_.toJsFn)

    implicit def optionalCallback[O[_]](implicit O: OptionLike[O]): UseEffectArg[O[Callback]] =
      byCallback(O.unsafeToJs(_).map(_.toJsFn))
  }

  object UseEffect {
    def unsafeCreate[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A]): Unit =
      Raw.React.useEffect(a.toJs(effect))

    def unsafeCreateOnMount[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A]): Unit =
      Raw.React.useEffect(a.toJs(effect), new js.Array[js.Any])

    def unsafeCreateLayout[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A]): Unit =
      Raw.React.useLayoutEffect(a.toJs(effect))

    def unsafeCreateLayoutOnMount[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A]): Unit =
      Raw.React.useLayoutEffect(a.toJs(effect), new js.Array[js.Any])
  }

  object ReusableEffect {
    private val noEffect: Raw.React.UseEffectArg =
      () => ()

    def prepare[A, D](e: CallbackTo[A], deps: D)(implicit a: UseEffectArg[A], r: Reusability[D]): CustomHook[Unit, Raw.React.UseEffectArg] =
      CustomHook[Unit]
        .useState(deps)
        .buildReturning { (_, prevDeps) =>
          if (r.updateNeeded(prevDeps.value, deps))
            a.toJs(e.finallyRun(prevDeps.setState(deps)))
          else
            noEffect
        }

    def useEffect[A, D](e: CallbackTo[A], deps: D)(implicit a: UseEffectArg[A], r: Reusability[D]): CustomHook[Unit, Unit] =
      prepare(e, deps).map(Raw.React.useEffect(_))

    def useLayoutEffect[A, D](e: CallbackTo[A], deps: D)(implicit a: UseEffectArg[A], r: Reusability[D]): CustomHook[Unit, Unit] =
      prepare(e, deps).map(Raw.React.useLayoutEffect(_))
  }

  // ===================================================================================================================

  final class UseMemo[+A](val hook: CustomHook[Unit, A]) extends AnyVal

  object UseMemo {
    def apply[A, D](create: => A, deps: D)(implicit r: Reusability[D]): UseMemo[A] = {
      val hook = CustomHook[Unit]
        .useState(0)
        .useState(deps)
        .buildReturning { (_, prevRev, prevDeps) =>
          var rev = prevRev.value
          if (r.updateNeeded(prevDeps.value, deps)) {
            rev += 1
            prevRev.setState(rev).runNow()
            prevDeps.setState(deps).runNow()
          }

          Raw.React.useMemo(() => create, js.Array[js.Any](rev))
        }
      new UseMemo(hook)
    }
  }

  // ===================================================================================================================

  object UseReducer {
    def unsafeCreate[S, A](reducer: (S, A) => S, initialArg: S): UseReducer[S, A] =
      _unsafeCreate(Raw.React.useReducer[S, A](reducer, initialArg))

    def unsafeCreate[I, S, A](reducer: (S, A) => S, initialArg: I, init: I => S): UseReducer[S, A] =
      _unsafeCreate(Raw.React.useReducer[I, S, A](reducer, initialArg, init))

    private def _unsafeCreate[S, A](originalResult: Raw.React.UseReducer[S, A]): UseReducer[S, A] = {
      val originalDispatch = Reusable.byRef(originalResult._2)
      UseReducer(originalResult, originalDispatch)
    }
  }

  final case class UseReducer[S, A](raw: Raw.React.UseReducer[S, A], originalDispatch: Reusable[Raw.React.UseReducerDispatch[_]]) {

    @inline def value: S =
      raw._1

    def dispatch(a: A): Reusable[Callback] =
      originalDispatch.withValue(Callback(raw._2(a)))

    /** WARNING: This does not affect the dispatch callback reusability. */
    def map[T](f: S => T): UseReducer[T, A] =
      UseReducer(js.Tuple2(f(value), raw._2), originalDispatch)

    /** WARNING: This does not affect the dispatch callback reusability. */
    def contramap[B](f: B => A): UseReducer[S, B] = {
      val newDispatch: js.Function1[B, Unit] = b => raw._2(f(b))
      UseReducer(js.Tuple2(value, newDispatch), originalDispatch)
    }

    @inline def widen[T >: S]: UseReducer[T, A] =
      UseReducer[T, A](raw, originalDispatch)

    @inline def narrow[B <: A]: UseReducer[S, B] =
      UseReducer[S, B](raw, originalDispatch)
  }

  // ===================================================================================================================

  object UseRef {
    def unsafeCreate[A](): Ref.Simple[A] =
      Ref.fromJs(Raw.React.useRef[A | Null](null))

    def unsafeCreate[A](initialValue: => A): Ref.NonEmpty.Simple[A] =
      Ref.NonEmpty.Simple(Raw.React.useRef((() => initialValue): js.Function0[A]))
  }

  // ===================================================================================================================

  object UseState {
    def unsafeCreate[S](initialState: => S): UseState[S] = {
      // Boxing is required because React's useState uses reflection to distinguish between {set,mod}State.
      val initialStateFn   = (() => Box(initialState)): js.Function0[Box[S]]
      val originalResult   = Raw.React.useState[Box[S]](initialStateFn)
      val originalSetState = Reusable.byRef(originalResult._2)
      UseState[Box[S]](originalResult, originalSetState)
        .xmap(_.unbox)(Box.apply)
    }
  }

  final case class UseState[S](raw: Raw.React.UseState[S], originalSetState: Reusable[Raw.React.UseStateSetter[_]]) {

    @inline def value: S =
      raw._1

    def setState(s: S): Reusable[Callback] =
      originalSetState.withValue(Callback(raw._2(s)))

    def modState(f: S => S): Reusable[Callback] =
      originalSetState.withValue(Callback(modStateRaw(f)))

    @deprecated("The useState hook isn't powerful enough to be used as a StateSnapshot. Change your hook to StateSnapshot{,.withReuse}.hook instead.", "always")
    def stateSnapshot(na: NotAllowed): Nothing =
      na.result

    @inline private def modStateRaw(f: js.Function1[S, S]): Unit =
      raw._2(f)

    /** WARNING: This does not affect the setState callback reusability. */
    private def newSetStateJs[T](givenValue: T => Unit,
                                 givenFn   : js.Function1[T, T] => Unit,
                                ): Raw.React.UseStateSetter[T] =
      tOrFn => {
        if (js.typeOf(tOrFn) == "function") {
          val mod = tOrFn.asInstanceOf[js.Function1[T, T]]
          givenFn(mod)
        } else {
          val t = tOrFn.asInstanceOf[T]
          givenValue(t)
        }
      }

    /** WARNING: This does not affect the setState callback reusability. */
    def xmap[T](f: S => T)(g: T => S): UseState[T] = {
      val newSetState = newSetStateJs[T](
        givenValue = t => raw._2(g(t)),
        givenFn    = m => raw._2((s => g(m(f(s)))): js.Function1[S, S]),
      )
      UseState(js.Tuple2(f(value), newSetState), originalSetState)
    }

    /** WARNING: This does not affect the setState callback reusability. */
    def withReusability(implicit r: Reusability[S]): UseState[S] = {
      val givenValue: S => Unit = next =>
        if (r.updateNeeded(value, next))
          raw._2(next)

      val givenFn: js.Function1[S, S] => Unit = f =>
        modStateRaw { cur =>
          val next = f(cur)
          if (r.updateNeeded(cur, next))
            next
          else
            cur // returning the exact same state as was given is how to abort an update (see hooks.js)
        }

      val newSetState = newSetStateJs(givenValue, givenFn)

      UseState(js.Tuple2(value, newSetState), originalSetState)
    }
  }

  // ===================================================================================================================

  final class Var[A](initialValue: A) {
    override def toString =
      s"Hooks.Var($value)"
      // Note: this is not just simply `value` because if a user were to rely on it (and base tests on it), it would
      // break when changing the Scala.js semantics that wipe out toString bodies.

    var value: A =
      initialValue

    def get: CallbackTo[A] =
      CallbackTo(value)

    def set(a: A): Callback =
      Callback{ value = a }

    def mod(f: A => A): Callback =
      Callback{ value = f(value) }
  }

}
