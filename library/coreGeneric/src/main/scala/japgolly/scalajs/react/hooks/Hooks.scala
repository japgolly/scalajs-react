package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.component.{Js => JsComponent, Scala => ScalaComponent}
import japgolly.scalajs.react.feature.Context
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.Util.identityFn
import japgolly.scalajs.react.util.{DefaultEffects => D, NotAllowed, OptionLike}
import japgolly.scalajs.react.vdom.TopNode
import japgolly.scalajs.react.{CtorType, NonEmptyRef, Ref, Reusability, Reusable, facade}
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

    implicit def callback[F[_]](implicit F: Dispatch[F]): UseCallbackArg[F[Unit]] =
      apply[F[Unit], js.Function0[Unit]](
        F.dispatchFn(_))(
        f => Reusable.byRef(f).withValue(F.delay(f())))
  }

  object UseCallback {

    private def create[A](in: A, deps: facade.React.HookDeps)(implicit a: UseCallbackArg[A]): Reusable[A] =
      a.fromJs(facade.React.useCallback(a.toJs(in), deps))

    def apply[A](callback: => A)(implicit a: UseCallbackArg[A]): CustomHook[Unit, Reusable[A]] =
      CustomHook.delay(create(callback, new js.Array[Any]))

    def withDeps[D, A](deps: => D)(callback: D => A)(implicit a: UseCallbackArg[A], r: Reusability[D]): CustomHook[Unit, Reusable[A]] =
      CustomHook.reusableDeps[D]
        .apply(() => deps)
        .map { case (d, rev) => create(callback(d), js.Array[Int](rev)) }
  }

  // ===================================================================================================================

  object UseContext {
    @inline def unsafeCreate[A](ctx: Context[A]): A =
      facade.React.useContext(ctx.raw)
  }

  // ===================================================================================================================

  object UseDebugValue {
    def unsafeCreate(desc: => Any): Unit =
      facade.React.useDebugValue[Null](null, _ => desc)
  }

  // ===================================================================================================================

  @implicitNotFound(
    "\nYou're attempting to provide a ${A} to the useEffect family of hooks."
    + "\n  - To specify a basic effect, provide a Callback / F[Unit]."
    + "\n  - To specify an effect and a clean-up effect, provide a CallbackTo[Callback] / F[F[Unit]] where the inner callback is the clean-up effect."
    + "\nSee https://reactjs.org/docs/hooks-reference.html#useeffect")
  final case class UseEffectArg[A](toJs: A => facade.React.UseEffectArg) extends AnyVal

  object UseEffectArg {
    implicit def unit[F[_]](implicit F: Dispatch[F]): UseEffectArg[F[Unit]] =
      apply(F.dispatchFn(_))

    def maybeCleanupF[F[_], A](f: A => js.UndefOr[js.Function0[Any]])(implicit F: Sync[F]): UseEffectArg[F[A]] =
      apply(fa => F.toJsFn(F.map(fa)(f)))

    implicit def cleanup[F[_], G[_], A](implicit F: Sync[F], G: Dispatch[G]): UseEffectArg[F[G[A]]] =
      maybeCleanupF(G.dispatchFn(_))

    implicit def optionalCallback[F[_], G[_], O[_], A](implicit F: Sync[F], G: Dispatch[G], O: OptionLike[O]): UseEffectArg[F[O[G[A]]]] =
      maybeCleanupF(O.unsafeToJs(_).map(G.dispatchFn(_)))
  }

  object UseEffect {
    def unsafeCreate[A](effect: A)(implicit a: UseEffectArg[A]): Unit =
      facade.React.useEffect(a.toJs(effect))

    def unsafeCreateOnMount[A](effect: A)(implicit a: UseEffectArg[A]): Unit =
      facade.React.useEffect(a.toJs(effect), new js.Array[Any])

    def unsafeCreateLayout[A](effect: A)(implicit a: UseEffectArg[A]): Unit =
      facade.React.useLayoutEffect(a.toJs(effect))

    def unsafeCreateLayoutOnMount[A](effect: A)(implicit a: UseEffectArg[A]): Unit =
      facade.React.useLayoutEffect(a.toJs(effect), new js.Array[Any])
  }

  object ReusableEffect {

    def useEffect[D, A](deps: => D)(effect: D => A)(implicit a: UseEffectArg[A], r: Reusability[D]): CustomHook[Unit, Unit] =
      CustomHook.reusableDeps[D]
        .apply(() => deps)
        .map { case (d, rev) => facade.React.useEffect(a.toJs(effect(d)), js.Array[Int](rev)) }

    def useLayoutEffect[D, A](deps: => D)(effect: D => A)(implicit a: UseEffectArg[A], r: Reusability[D]): CustomHook[Unit, Unit] =
      CustomHook.reusableDeps[D]
        .apply(() => deps)
        .map { case (d, rev) => facade.React.useLayoutEffect(a.toJs(effect(d)), js.Array[Int](rev)) }
  }

  // ===================================================================================================================

  object UseMemo {
    def apply[D, A](deps: => D)(create: D => A)(implicit r: Reusability[D]): CustomHook[Unit, Reusable[A]] =
      CustomHook.reusableByDeps[D, A] { case (d, rev) => facade.React.useMemo(() => create(d), js.Array[Int](rev)) }
        .apply(() => deps)
  }

  // ===================================================================================================================

  object UseReducer {
    @inline def unsafeCreate[S, A](reducer: (S, A) => S, initialState: => S): UseReducer[S, A] =
      unsafeCreate[Null, S, A](reducer, null, _ => initialState)

    def unsafeCreate[I, S, A](reducer: (S, A) => S, initialArg: I, init: I => S): UseReducer[S, A] =
      _unsafeCreate(facade.React.useReducer[I, S, A](reducer, initialArg, init))

    private def _unsafeCreate[S, A](originalResult: facade.React.UseReducer[S, A]): UseReducer[S, A] = {
      val originalDispatch = Reusable.byRef(originalResult._2)
      UseReducer(originalResult, originalDispatch)
    }

    implicit def reusability[S, A](implicit S: Reusability[S]): Reusability[UseReducer[S, A]] = {
      val r = implicitly[Reusability[Reusable[facade.React.UseReducerDispatch[_]]]]
      Reusability((x, y) => S.test(x.value, y.value) && r.test(x.originalDispatch, y.originalDispatch))
    }
  }

  final case class UseReducer[S, A](raw: facade.React.UseReducer[S, A], originalDispatch: Reusable[facade.React.UseReducerDispatch[_]]) {

    @inline def value: S =
      raw._1

    def dispatch(a: A): Reusable[D.Sync[Unit]] =
      originalDispatch.withValue(D.Sync.delay(raw._2(a)))

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

  final case class UseRefF[F[_], A](raw: facade.React.RefHandle[A])(implicit FF: Sync[F]) extends NonEmptyRef.SimpleF[F, A] {
    protected def F = FF

    override def withEffect[G[_]](implicit G: Sync[G]): UseRefF[G, A] =
      G.subst[F, ({type L[E[_]] = UseRefF[E, A]})#L](this)(
        new UseRefF(raw))

    @inline def value: A =
      raw.current

    /** NOTE: This doesn't force an update-to/redraw-of your component. */
    @inline def value_=(a: A): Unit =
      raw.current = a

    /** NOTE: This doesn't force an update-to/redraw-of your component. */
    override def set(a: A): F[Unit] =
      F.delay { value = a }

    override def get: F[A] =
      F.delay(value)

    override def contramap[X](f: X => A): NonEmptyRef.FullF[F, X, A, A] =
      NonEmptyRef.FullF(raw, f, identityFn)

    override def map[X](f: A => X): NonEmptyRef.FullF[F, A, A, X] =
      NonEmptyRef.FullF(raw, identityFn, f)

    override def narrow[X <: A]: NonEmptyRef.FullF[F, X, A, A] =
      NonEmptyRef.FullF(raw, identityFn, identityFn)

    override def widen[X >: A]: NonEmptyRef.FullF[F, A, A, X] =
      NonEmptyRef.FullF(raw, identityFn, identityFn)
  }

  type UseRef[A] = UseRefF[D.Sync, A]

  object UseRef {
    def unsafeCreate[A](initialValue: A): UseRef[A] =
      new UseRefF(facade.React.useRef(initialValue))(D.Sync)

    def unsafeCreateSimple[A](): Ref.Simple[A] =
      Ref.fromJs(facade.React.useRef[A | Null](null))

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

    def unsafeCreateToJsComponent[F[_], A[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]]
        (a: Ref.WithJsComponentArg[F, A, P1, S1, CT1, R, P0, S0])
        : Ref.WithJsComponent[F, A, P1, S1, CT1, R, P0, S0] =
      a.wrap(unsafeCreateToJsComponentWithMountedFacade[P0, S0, R]())
  }

  // ===================================================================================================================

  private lazy val internalReuseSafety = Reusable.byRef(new AnyRef)

  type UseState[S] = UseStateF[D.Sync, S]

  object UseState {
    @inline def apply[S, O](r: facade.React.UseState[S], oss: Reusable[facade.React.UseStateSetter[O]]): UseState[S] =
      UseStateF(r, oss)(D.Sync)

    def unsafeCreate[S](initialState: => S): UseState[S] = {
      // Boxing is required because React's useState uses reflection to distinguish between {set,mod}State.
      val initialStateFn   = (() => Box(initialState)): js.Function0[Box[S]]
      val originalResult   = facade.React.useState[Box[S]](initialStateFn)
      val originalSetState = Reusable.byRef(originalResult._2)
      UseState(originalResult, originalSetState)
        .xmap(_.unbox)(Box.apply)
    }
  }

  object UseStateF {
    def apply[F[_], S, O](r: facade.React.UseState[S], oss: Reusable[facade.React.UseStateSetter[O]])(implicit f: Sync[F]): UseStateF[F, S] =
      new UseStateF[F, S] {
        override protected[hooks] implicit def F = f
        override val raw = r
        override type OriginalState = O
        override val originalSetState = oss
      }

    implicit def reusability[F[_], S: Reusability]: Reusability[UseStateF[F, S]] =
      Reusability.by(_.value)
  }

  trait UseStateF[F[_], S] { self =>
    protected[hooks] implicit def F: Sync[F]
    val raw: facade.React.UseState[S]
    type OriginalState
    val originalSetState: Reusable[facade.React.UseStateSetter[OriginalState]]

    final def withEffect[G[_]](implicit G: Sync[G]): UseStateF[G, S] =
      G.subst[F, ({type L[E[_]] = UseStateF[E, S]})#L](this)(
        UseStateF(raw, originalSetState)
      )

    @inline def value: S =
      raw._1

    def setState: Reusable[S => F[Unit]] =
      originalSetState.withValue(s => F.delay(raw._2(s)))

    /** WARNING: This ignores reusability of the provided function.
      * It will only work correctly if you always provide the exact same function.
      *
      * If you want to be able to provide different functions, use `.withReusableInputs.modState`.
      *
      * @param f WARNING: This must be a consistent/stable function.
      */
    def modState: Reusable[(S => S) => F[Unit]] =
      originalSetState.withValue(f => F.delay(modStateRaw(f)))

    object withReusableInputs {
      def setState: Reusable[Reusable[S] => Reusable[F[Unit]]] = {
        val setR = self.setState
        Reusable.implicitly((setR, internalReuseSafety)).withValue(_.ap(setR))
      }

      def modState: Reusable[Reusable[S => S] => Reusable[F[Unit]]] = {
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
                                ): facade.React.UseStateSetter[T] =
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
      UseStateWithReuseF(us, rr)
    }
  }

  type UseStateWithReuse[S] = UseStateWithReuseF[D.Sync, S]

  implicit def reusabilityUseStateWithReuseF[F[_], S]: Reusability[UseStateWithReuseF[F, S]] = {
    val r = implicitly[Reusability[Reusable[Reusability[S]]]]
    Reusability((x, y) => r.test(x.reusability, y.reusability) && x.reusability.value.test(x.value, y.value))
  }

  final case class UseStateWithReuseF[F[_], S: ClassTag](withoutReuse: UseStateF[F, S], reusability: Reusable[Reusability[S]]) {

    def withEffect[G[_]](implicit G: Sync[G]): UseStateWithReuseF[G, S] =
      G.subst[F, ({type L[E[_]] = UseStateWithReuseF[E, S]})#L](this)(
        UseStateWithReuseF(withoutReuse.withEffect[G], reusability)
      )(withoutReuse.F)

    @inline def value: S =
      withoutReuse.value

    def setState: Reusable[S => Reusable[F[Unit]]] =
      withReusableInputs.setState.map(set => s => set(reusability.reusable(s)))

    /** WARNING: This ignores reusability of the provided function.
      * It will only work correctly if you always provide the exact same function.
      *
      * If you want to be able to provide different functions, use `.withReusableInputs.modState`.
      *
      * @param f WARNING: This must be a consistent/stable function.
      */
    def modState(f: S => S): Reusable[F[Unit]] =
      withReusableInputs.modState(internalReuseSafety.withValue(f))

    @inline def withReusableInputs =
      withoutReuse.withReusableInputs

    @deprecated("The useState hook isn't powerful enough to be used as a StateSnapshot. Change your hook to StateSnapshot{,.withReuse}.hook instead.", "always")
    def stateSnapshot(na: NotAllowed): Nothing =
      na.result
  }

  // ===================================================================================================================

  type Var[A] = VarF[D.Sync, A]

  @inline def Var[A](initialValue: A): Var[A] =
    new VarF(initialValue)(D.Sync)

  final class VarF[F[_], A](initialValue: A)(implicit F: Sync[F]) {
    override def toString =
      s"Hooks.Var($value)"
      // Note: this is not just simply `value` because if a user were to rely on it (and base tests on it), it would
      // break when changing the Scala.js semantics that wipe out toString bodies.

    var value: A =
      initialValue

    def get: F[A] =
      F.delay(value)

    /** NOTE: This doesn't force an update-to/redraw-of your component. */
    def set(a: A): F[Unit] =
      F.delay { value = a }

    /** NOTE: This doesn't force an update-to/redraw-of your component. */
    def mod(f: A => A): F[Unit] =
      F.delay { value = f(value) }
  }


  // ===================================================================================================================

  object UseId {
    def apply(): CustomHook[Unit, String] =
      CustomHook.delay(facade.React.useId())
  }
}
