package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.component.{Js => JsComponent, Scala => ScalaComponent}
import japgolly.scalajs.react.feature.Context
import japgolly.scalajs.react.internal.{Box, NotAllowed, OptionLike}
import japgolly.scalajs.react.vdom.TopNode
import japgolly.scalajs.react.{Callback, CallbackTo, CtorType, React => _, Ref, Reusability, Reusable, raw => Raw}
import scala.annotation.implicitNotFound
import scala.reflect.ClassTag
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

  object UseCallback {

    private def create[A](in: A, deps: Raw.React.HookDeps)(implicit a: UseCallbackArg[A]): Reusable[A] =
      a.fromJs(Raw.React.useCallback(a.toJs(in), deps))

    def apply[A](callback: => A)(implicit a: UseCallbackArg[A]): CustomHook[Unit, Reusable[A]] =
      CustomHook.delay(create(callback, new js.Array[Any]))

    def apply[A, D](callback: => A, deps: => D)(implicit a: UseCallbackArg[A], r: Reusability[D]): CustomHook[Unit, Reusable[A]] =
      CustomHook.reusableDeps[D]
        .apply(() => deps)
        .map(rev => create(callback, js.Array[Any](rev)))
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
      Raw.React.useEffect(a.toJs(effect), new js.Array[Any])

    def unsafeCreateLayout[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A]): Unit =
      Raw.React.useLayoutEffect(a.toJs(effect))

    def unsafeCreateLayoutOnMount[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A]): Unit =
      Raw.React.useLayoutEffect(a.toJs(effect), new js.Array[Any])
  }

  object ReusableEffect {

    def useEffect[A, D](e: CallbackTo[A], deps: => D)(implicit a: UseEffectArg[A], r: Reusability[D]): CustomHook[Unit, Unit] =
      CustomHook.reusableDeps[D]
        .apply(() => deps)
        .map(rev => Raw.React.useEffect(a.toJs(e), js.Array[Any](rev)))

    def useLayoutEffect[A, D](e: CallbackTo[A], deps: => D)(implicit a: UseEffectArg[A], r: Reusability[D]): CustomHook[Unit, Unit] =
      CustomHook.reusableDeps[D]
        .apply(() => deps)
        .map(rev => Raw.React.useLayoutEffect(a.toJs(e), js.Array[Any](rev)))
  }

  // ===================================================================================================================

  object UseMemo {
    def apply[A, D](create: => A, deps: => D)(implicit r: Reusability[D]): CustomHook[Unit, Reusable[A]] =
      CustomHook.reusableByDeps[A, D](rev => Raw.React.useMemo(() => create, js.Array[Any](rev)))
        .apply(() => deps)
  }

  // ===================================================================================================================

  object UseReducer {
    @inline def unsafeCreate[S, A](reducer: (S, A) => S, initialState: => S): UseReducer[S, A] =
      unsafeCreate[Null, S, A](reducer, null, _ => initialState)

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

  final case class UseRef[A](raw: Raw.React.RefHandle[A]) {
    @inline def value: A =
      raw.current

    /** NOTE: This doesn't force an update-to/redraw-of your component. */
    @inline def value_=(a: A): Unit =
      raw.current = a

    /** NOTE: This doesn't force an update-to/redraw-of your component. */
    def set(a: A): Callback =
      Callback{ value = a }

    /** NOTE: This doesn't force an update-to/redraw-of your component. */
    def mod(f: A => A): Callback =
      Callback{ value = f(value) }
  }

  object UseRef {
    def unsafeCreate[A](initialValue: A): UseRef[A] =
      UseRef(Raw.React.useRef(initialValue))

    def unsafeCreateSimple[A](): Ref.Simple[A] =
      Ref.fromJs(Raw.React.useRef[A | Null](null))

    @inline def unsafeCreateToAnyVdom(): Ref.ToAnyVdom =
      unsafeCreateSimple()

    def unsafeCreateToVdom[N <: TopNode : ClassTag](): Ref.ToVdom[N] =
      unsafeCreateToAnyVdom().narrowOption[N]

    def unsafeCreateToScalaComponent[P, S, B](): Ref.ToScalaComponent[P, S, B] =
      unsafeCreateSimple[ScalaComponent.RawMounted[P, S, B]]()
        .map(_.mountedImpure)

    def unsafeCreateToScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]](c: ScalaComponent.Component[P, S, B, CT]): Ref.WithScalaComponent[P, S, B, CT] =
      Ref.ToComponent.inject(c, unsafeCreateToScalaComponent[P, S, B]())

    def unsafeCreateToJsComponent[P <: js.Object, S <: js.Object](): Ref.ToJsComponent[P, S, JsComponent.RawMounted[P, S]] =
      unsafeCreateSimple[JsComponent.RawMounted[P, S]]().map(JsComponent.mounted[P, S](_))

    def unsafeCreateToJsComponentWithMountedFacade[P <: js.Object, S <: js.Object, F <: js.Object](): Ref.ToJsComponent[P, S, JsComponent.RawMounted[P, S] with F] =
      unsafeCreateSimple[JsComponent.RawMounted[P, S] with F]().map(JsComponent.mounted[P, S](_).addFacade[F])

    def unsafeCreateToJsComponent[F[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]]
        (a: Ref.WithJsComponentArg[F, P1, S1, CT1, R, P0, S0])
        : Ref.WithJsComponent[F, P1, S1, CT1, R, P0, S0] =
      a.wrap(unsafeCreateToJsComponentWithMountedFacade[P0, S0, R]())
  }

  // ===================================================================================================================

  private lazy val internalReuseSafety = Reusable.byRef(new AnyRef)

  object UseState {
    def apply[S, O](r: Raw.React.UseState[S], oss: Reusable[Raw.React.UseStateSetter[O]]): UseState[S] =
      new UseState[S] {
        override val raw = r
        override type OriginalState = O
        override val originalSetState = oss
      }

    def unsafeCreate[S](initialState: => S): UseState[S] = {
      // Boxing is required because React's useState uses reflection to distinguish between {set,mod}State.
      val initialStateFn   = (() => Box(initialState)): js.Function0[Box[S]]
      val originalResult   = Raw.React.useState[Box[S]](initialStateFn)
      val originalSetState = Reusable.byRef(originalResult._2)
      UseState(originalResult, originalSetState)
        .xmap(_.unbox)(Box.apply)
    }
  }

  trait UseState[S] { self =>

    val raw: Raw.React.UseState[S]

    type OriginalState
    val originalSetState: Reusable[Raw.React.UseStateSetter[OriginalState]]

    @inline def value: S =
      raw._1

    def setState: Reusable[S => Callback] =
      originalSetState.withValue(s => Callback(raw._2(s)))

    /** WARNING: This ignores reusability of the provided function.
      * It will only work correctly if you always provide the exact same function.
      *
      * If you want to be able to provide different functions, use `.withReusableInputs.modState`.
      *
      * @param f WARNING: This must be a consistent/stable function.
      */
    def modState: Reusable[(S => S) => Callback] =
      originalSetState.withValue(f => Callback(modStateRaw(f)))

    object withReusableInputs {
      def setState: Reusable[Reusable[S] => Reusable[Callback]] = {
        val setR = self.setState
        Reusable.implicitly((setR, internalReuseSafety)).withValue(_.ap(setR))
      }

      def modState: Reusable[Reusable[S => S] => Reusable[Callback]] = {
        val modR = self.modState
        Reusable.implicitly((modR, internalReuseSafety)).withValue(_.ap(modR))
      }
    }

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
    private[Hooks] def withReusability(implicit r: Reusability[S]): UseState[S] = {

      def update(cur: S, next: S): S = {
        // println(s"$cur => $next? update=${r.updateNeeded(cur, next)}, is=${js.Object.is(cur, cur)}")
        if (r.updateNeeded(cur, next))
          next
        else
          cur // returning the exact same state as was given is how to abort an update (see hooks.js)
      }

      val givenValue: S => Unit =
        next => modStateRaw(update(_, next))

      val givenFn: js.Function1[S, S] => Unit =
        f => modStateRaw(cur => update(cur,  f(cur)))

      val newSetState = newSetStateJs(givenValue, givenFn)

      UseState(js.Tuple2(value, newSetState), originalSetState)
    }
  }

  // ===================================================================================================================

  object UseStateWithReuse {
    def unsafeCreate[S](initialState: => S)(implicit r: Reusability[S], ct: ClassTag[S]): UseStateWithReuse[S] = {
      val rr = Reusable.reusabilityInstance(r)
      val us = UseState.unsafeCreate(initialState).withReusability
      apply(us, rr)
    }
  }

  final case class UseStateWithReuse[S: ClassTag](withoutReuse: UseState[S], reusability: Reusable[Reusability[S]]) {

    @inline def value: S =
      withoutReuse.value

    def setState: Reusable[S => Reusable[Callback]] =
      withReusableInputs.setState.map(set => s => set(reusability.reusable(s)))

    /** WARNING: This ignores reusability of the provided function.
      * It will only work correctly if you always provide the exact same function.
      *
      * If you want to be able to provide different functions, use `.withReusableInputs.modState`.
      *
      * @param f WARNING: This must be a consistent/stable function.
      */
    def modState(f: S => S): Reusable[Callback] =
      withReusableInputs.modState(internalReuseSafety.withValue(f))

    @inline def withReusableInputs =
      withoutReuse.withReusableInputs

    @deprecated("The useState hook isn't powerful enough to be used as a StateSnapshot. Change your hook to StateSnapshot{,.withReuse}.hook instead.", "always")
    def stateSnapshot(na: NotAllowed): Nothing =
      na.result
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

    /** NOTE: This doesn't force an update-to/redraw-of your component. */
    def set(a: A): Callback =
      Callback{ value = a }

    /** NOTE: This doesn't force an update-to/redraw-of your component. */
    def mod(f: A => A): Callback =
      Callback{ value = f(value) }
  }

}
