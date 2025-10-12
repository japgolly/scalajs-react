package japgolly.scalajs.react.component.builder

import japgolly.scalajs.react.component.Scala.{BackendScope, Vars}
import japgolly.scalajs.react.component.{Js, Scala}
import japgolly.scalajs.react.internal.{Box, Lens}
import japgolly.scalajs.react.util.DefaultEffects.{Sync => DS}
import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.Semigroup
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, UpdateSnapshot, facade}

object ComponentBuilder {
  import Lifecycle._

  type NewBackendFn[P, S, B] = BackendScope[P, S] => B
  type RenderFn    [P, S, B] = RenderScope[P, S, B] => VdomNode

  type Config[P, C <: Children, S, B, US <: UpdateSnapshot, US2 <: UpdateSnapshot] =
    LastStep[P, C, S, B, US] => LastStep[P, C, S, B, US2]

  def static(transformedName: String, vdom: VdomNode) =
    new Step1[Unit](transformedName)
      .renderStatic(vdom)
      .shouldComponentUpdateConst(false)

  // ===================================================================================================================

  @inline implicit def defaultToNoState[P](b: Step1[P]): Step2[P, Unit] =
    b.stateless

  @inline implicit def defaultToNoBackend[X, P, S](b: X)(implicit ev: X => Step2[P, S]): Step3[P, S, Unit] =
    b.noBackend

  // ===================================================================================================================

  /** You're on step 1/4 on the way to building a component.
    *
    * If your component is to be stateful, here you must specify it's initial state.
    *
    * If your component is to be stateless, you can skip this step or explicitly use `.stateless`.
    */
  final class Step1[P](private val name: String) extends AnyVal {

    /** getDerivedStateFromProps is invoked right before calling the render method, both on the initial mount and on
      * subsequent updates.
      *
      * This method exists for rare use cases where the state depends on changes in props over time.
      * For example, it might be handy for implementing a Transition component that compares its previous and next
      * children to decide which of them to animate in and out.
      *
      * Deriving state leads to verbose code and makes your components difficult to think about.
      * Make sure you're familiar with simpler alternatives:
      *
      *   - If you need to perform a side effect (for example, data fetching or an animation) in response to a change in
      *     props, use componentDidUpdate lifecycle instead.
      *
      *   - If you want to re-compute some data only when a prop changes, use a memoization helper instead.
      *
      *   - If you want to "reset" some state when a prop changes, consider either making a component fully controlled
      *     or fully uncontrolled with a key instead.
      *
      * Note that this method is fired on every render, regardless of the cause.
      * This is in contrast to componentWillReceiveProps, which only fires when the parent causes a re-render and
      * not as a result of a local setState.
      */
    @deprecated("Use getDerivedStateFromPropsAndState instead. This doesn't just get called when props change, it gets called when state changes too; meaning it gets reset every time you call setState (!)", "1.7.1")
    def getDerivedStateFromProps[S](f: P => S): Step2[P, S] =
      new Step2(name, InitState.DerivedFromProps(f))

    /** This is called twice when a component is first rendered. Once with state set to `None` and then again by React
      * with state set to `Some`.
      *
      * getDerivedStateFromProps is invoked right before calling the render method, both on the initial mount and on
      * subsequent updates.
      *
      * This method exists for rare use cases where the state depends on changes in props over time.
      * For example, it might be handy for implementing a Transition component that compares its previous and next
      * children to decide which of them to animate in and out.
      *
      * Deriving state leads to verbose code and makes your components difficult to think about.
      * Make sure you're familiar with simpler alternatives:
      *
      *   - If you need to perform a side effect (for example, data fetching or an animation) in response to a change in
      *     props, use componentDidUpdate lifecycle instead.
      *
      *   - If you want to re-compute some data only when a prop changes, use a memoization helper instead.
      *
      *   - If you want to "reset" some state when a prop changes, consider either making a component fully controlled
      *     or fully uncontrolled with a key instead.
      *
      * Note that this method is fired on every render, regardless of the cause.
      * This is in contrast to componentWillReceiveProps, which only fires when the parent causes a re-render and
      * not as a result of a local setState.
      */
    def getDerivedStateFromPropsAndState[S](f: (P, Option[S]) => S): Step2[P, S] =
      new Step2(name, InitState.DerivedFromPropsAndState(f))

    def initialState[S](s: => S): Step2[P, S] =
      new Step2(name, InitState.InitialState(_ => Box(s)))

    def initialStateFromProps[S](f: P => S): Step2[P, S] =
      new Step2(name, InitState.InitialState(p => Box(f(p.unbox))))

    def initialStateCallback[G[_], S](cb: G[S])(implicit G: Sync[G]): Step2[P, S] =
      initialState(G.runSync(cb))

    def initialStateCallbackFromProps[G[_], S](f: P => G[S])(implicit G: Sync[G]): Step2[P, S] =
      initialStateFromProps(p => G.runSync(f(p)))

    def stateless: Step2[P, Unit] =
      new Step2(name, InitState.stateless)
  }

  // ===================================================================================================================

  /** You're on step 2/4 on the way to building a component.
    *
    * Here you specify whether your component has a "backend" or not.
    * A backend like a class that is created when your component mounts and remains until it is unmounted.
    *
    * If you don't need a backend, you can skip this step or explicitly use `.noBackend`.
    */
  final class Step2[P, S](name: String, initState: InitState[P, S]) {

    def backend[B](f: NewBackendFn[P, S, B]): Step3[P, S, B] =
      new Step3(name, initState, f)

    def noBackend: Step3[P, S, Unit] =
      backend(_ => ())

    /** Shortcut for:
      *
      * {{{
      *   .backend[B](new B(_))
      *   .renderBackend
      * }}}
      */
    @deprecated("Call .backend(new B(_)) and then .render or one of its variants", "3.0.0")
    def renderBackend[B]: LastStep[P, Children.None, S, B, UpdateSnapshot.None] =
      macro ComponentBuilderMacros.backendAndRender[P, S, B]

    /** Shortcut for:
      *
      * {{{
      *   .backend[B](new B(_))
      *   .renderBackendWithChildren
      * }}}
      */
    @deprecated("Call .backend(new B(_)) and then .render or one of its variants", "3.0.0")
    def renderBackendWithChildren[B]: LastStep[P, Children.Varargs, S, B, UpdateSnapshot.None] =
      macro ComponentBuilderMacros.backendAndRenderWithChildren[P, S, B]
  }

  // ===================================================================================================================

  /** You're on step 3/4 on the way to building a component.
    *
    * Here you specify your component's render function. For convenience there are a plethora of methods you can call
    * that all do the same thing but accept the argument in different shapes; the suffixes in method names refer to the
    * argument type(s). This means you choose the method that provides the types that you need, rather than having to
    * manually specify the types for all arguments including stuff you don't need.
    *
    * If you're using a backend, then it's highly recommended that you put your render function in the backend.
    */
  final class Step3[P, S, B](name: String, initState: InitState[P, S], backendFn: NewBackendFn[P, S, B]) {

    type $ = RenderScope[P, S, B]

    def renderWith[C <: Children](r: RenderFn[P, S, B]): LastStep[P, C, S, B, UpdateSnapshot.None] = {
      var lc = Lifecycle.empty[P, S, B]
      initState match {
        case InitState.DerivedFromProps        (f) => lc = lc.copy(getDerivedStateFromProps = Some((p, _) => Some(f(p))))
        case InitState.DerivedFromPropsAndState(f) => lc = lc.copy(getDerivedStateFromProps = Some((p, s) => Some(f(p, Some(s)))))
        case _                                     => ()
      }
      new LastStep[P, C, S, B, UpdateSnapshot.None](name, initState, backendFn, r, lc)
    }

    // No args

    def renderStatic(r: VdomNode): LastStep[P, Children.None, S, B, UpdateSnapshot.None] =
      renderWith(_ => r)

    def render_(r: => VdomNode): LastStep[P, Children.None, S, B, UpdateSnapshot.None] =
      renderWith(_ => r)

    // No children

    def render(r: RenderFn[P, S, B]): LastStep[P, Children.None, S, B, UpdateSnapshot.None] =
      renderWith(r)

    def renderPS(r: ($, P, S) => VdomNode): LastStep[P, Children.None, S, B, UpdateSnapshot.None] =
       renderWith($ => r($, $.props, $.state))

    def renderP(r: ($, P) => VdomNode): LastStep[P, Children.None, S, B, UpdateSnapshot.None] =
      renderWith($ => r($, $.props))

    def renderS(r: ($, S) => VdomNode): LastStep[P, Children.None, S, B, UpdateSnapshot.None] =
      renderWith($ => r($, $.state))

    def render_PS(r: (P, S) => VdomNode): LastStep[P, Children.None, S, B, UpdateSnapshot.None] =
      renderWith($ => r($.props, $.state))

    def render_P(r: P => VdomNode): LastStep[P, Children.None, S, B, UpdateSnapshot.None] =
      renderWith($ => r($.props))

    def render_S(r: S => VdomNode): LastStep[P, Children.None, S, B, UpdateSnapshot.None] =
      renderWith($ => r($.state))

    // Has children

    def renderPCS(r: ($, P, PropsChildren, S) => VdomNode): LastStep[P, Children.Varargs, S, B, UpdateSnapshot.None] =
      renderWith($ => r($, $.props, $.propsChildren, $.state))

    def renderPC(r: ($, P, PropsChildren) => VdomNode): LastStep[P, Children.Varargs, S, B, UpdateSnapshot.None] =
      renderWith($ => r($, $.props, $.propsChildren))

    def renderCS(r: ($, PropsChildren, S) => VdomNode): LastStep[P, Children.Varargs, S, B, UpdateSnapshot.None] =
      renderWith($ => r($, $.propsChildren, $.state))

    def renderC(r: ($, PropsChildren) => VdomNode): LastStep[P, Children.Varargs, S, B, UpdateSnapshot.None] =
      renderWith($ => r($, $.propsChildren))

    def render_PCS(r: (P, PropsChildren, S) => VdomNode): LastStep[P, Children.Varargs, S, B, UpdateSnapshot.None] =
      renderWith($ => r($.props, $.propsChildren, $.state))

    def render_PC(r: (P, PropsChildren) => VdomNode): LastStep[P, Children.Varargs, S, B, UpdateSnapshot.None] =
      renderWith($ => r($.props, $.propsChildren))

    def render_CS(r: (PropsChildren, S) => VdomNode): LastStep[P, Children.Varargs, S, B, UpdateSnapshot.None] =
      renderWith($ => r($.propsChildren, $.state))

    def render_C(r: PropsChildren => VdomNode): LastStep[P, Children.Varargs, S, B, UpdateSnapshot.None] =
      renderWith($ => r($.propsChildren))

    /**
     * Use a method named `render` in the backend, automatically populating its arguments with props and state
     * where needed.
     */
    @deprecated("Call .render or one of its variants", "3.0.0")
    def renderBackend: LastStep[P, Children.None, S, B, UpdateSnapshot.None] =
      macro ComponentBuilderMacros.renderBackend[P, S, B]

    /**
     * Use a method named `render` in the backend, automatically populating its arguments with props, state, and
     * propsChildren where needed.
     */
    @deprecated("Call .render or one of its variants", "3.0.0")
    def renderBackendWithChildren: LastStep[P, Children.Varargs, S, B, UpdateSnapshot.None] =
      macro ComponentBuilderMacros.renderBackendWithChildren[P, S, B]
  }

  // ===================================================================================================================

  /** You're on the final step on the way to building a component.
    *
    * This is where you add lifecycle callbacks if you need to.
    *
    * When you're done, simply call `.build` to create and return your `ScalaComponent`.
    */
  final class LastStep[P, C <: Children, S, B, US <: UpdateSnapshot](
      val name: String,
      private[builder] val initState: InitState[P, S],
      private[builder] val backendFn: NewBackendFn[P, S, B],
      private[builder] val renderFn : RenderFn[P, S, B],
      private[builder] val lifecycle: Lifecycle[P, S, B, US#Value]) {

    type This = LastStep[P, C, S, B, US]

    type SnapshotValue = US#Value

    private type Lifecycle_ = Lifecycle[P, S, B, SnapshotValue]

    private def copy(name     : String                = this.name,
                     initState: InitState [P, S]      = this.initState,
                     backendFn: NewBackendFn[P, S, B] = this.backendFn,
                     renderFn : RenderFn    [P, S, B] = this.renderFn,
                     lifecycle: Lifecycle_): This =
      new LastStep(name, initState, backendFn, renderFn, lifecycle)

    private def setLC[US2 <: UpdateSnapshot](lc: Lifecycle[P, S, B, US2#Value]): LastStep[P, C, S, B, US2] =
      new LastStep(name, initState, backendFn, renderFn, lc)

    private def lcAppend[I, O](lens: Lens[Lifecycle_, Option[I => O]])(g: I => O)(implicit s: Semigroup[O]): This =
      copy(lifecycle = lifecycle.append(lens)(g)(s))

    private def lcAppendDispatch[G[_], I](lens: Lens[Lifecycle_, Option[I => DS[Unit]]])(g: I => G[Unit])(implicit G: Dispatch[G]): This =
      copy(lifecycle = lifecycle.append(lens)(DS.transDispatchFn1(g))(DS.semigroupSyncUnit))

    @inline def configure[US2 <: UpdateSnapshot](f: Config[P, C, S, B, US, US2]): LastStep[P, C, S, B, US2] =
      f(this)

    @inline def configureWhen(cond: Boolean)(f: => Config[P, C, S, B, US, US]): LastStep[P, C, S, B, US] =
      if (cond) configure(f) else this

    @inline def configureUnless(cond: Boolean)(f: => Config[P, C, S, B, US, US]): LastStep[P, C, S, B, US] =
      if (cond) this else configure(f)

    /** Error boundaries are React components that catch errors anywhere in their child component tree, log those errors,
      * and display a fallback UI instead of the component tree that crashed. Error boundaries catch errors during
      * rendering, in lifecycle methods, and in constructors of the whole tree below them.
      *
      * Note: "CHILD COMPONENT TREE". Components cannot catch errors in themselves, only their children.
      */
    def componentDidCatch[G[_]](f: ComponentDidCatch[P, S, B] => G[Unit])(implicit G: Dispatch[G]): This =
      lcAppendDispatch(LifecycleF.componentDidCatch)(f)

    /** Invoked once, only on the client (not on the server), immediately after the initial rendering occurs. At this point
      * in the lifecycle, the component has a DOM representation which you can access via `ReactDOM.findDOMNode(this)`.
      * The `componentDidMount()` method of child components is invoked before that of parent components.
      *
      * If you want to integrate with other JavaScript frameworks, set timers using `setTimeout` or `setInterval`, or send
      * AJAX requests, perform those operations in this method.
      */
    def componentDidMount[G[_]](f: ComponentDidMount[P, S, B] => G[Unit])(implicit G: Dispatch[G]): This =
      lcAppendDispatch(LifecycleF.componentDidMount)(f)

    /** Invoked immediately after the component's updates are flushed to the DOM. This method is not called for the initial
      * render.
      *
      * Use this as an opportunity to operate on the DOM when the component has been updated.
      */
    def componentDidUpdate[G[_]](f: ComponentDidUpdate[P, S, B, SnapshotValue] => G[Unit])(implicit G: Dispatch[G]): LastStep[P, C, S, B, UpdateSnapshot.Some[SnapshotValue]] =
      setLC[UpdateSnapshot.Some[SnapshotValue]](lcAppendDispatch(LifecycleF.componentDidUpdate)(f).lifecycle)

    /**
     * Invoked once, both on the client and server, immediately before the initial rendering occurs.
     * If you call `setState` within this method, `render()` will see the updated state and will be executed only once
     * despite the state change.
     */
    @deprecated(
      "Use either .initialState* on the component builder, or .componentDidMount. See https://reactjs.org/docs/react-component.html#unsafe_componentwillmount / https://reactjs.org/blog/2018/03/27/update-on-async-rendering.html",
      "scalajs-react 1.7.0 / React 16.9.0")
    def componentWillMount[G[_]](f: ComponentWillMount[P, S, B] => G[Unit])(implicit G: Dispatch[G]): This =
      lcAppendDispatch(LifecycleF.componentWillMount)(f)

    /**
     * Invoked when a component is receiving new props. This method is not called for the initial render.
     *
     * Use this as an opportunity to react to a prop transition before `render()` is called by updating the state using
     * `this.setState()`. The old props can be accessed via `this.props`. Calling `this.setState()` within this function
     * will not trigger an additional render.
     *
     * Note: There is no analogous method `componentWillReceiveState`. An incoming prop transition may cause a state
     * change, but the opposite is not true. If you need to perform operations in response to a state change, use
     * `componentWillUpdate`.
     */
    @deprecated(
      "See https://reactjs.org/docs/react-component.html#unsafe_componentwillreceiveprops / https://reactjs.org/blog/2018/03/27/update-on-async-rendering.html",
      "scalajs-react 1.7.0 / React 16.9.0")
    def componentWillReceiveProps[G[_]](f: ComponentWillReceiveProps[P, S, B] => G[Unit])(implicit G: Dispatch[G]): This =
      lcAppendDispatch(LifecycleF.componentWillReceiveProps)(f)

    /**
     * Invoked immediately before a component is unmounted from the DOM.
     *
     * Perform any necessary cleanup in this method, such as invalidating timers or cleaning up any DOM elements that were
     * created in `componentDidMount`.
     */
    def componentWillUnmount[G[_]](f: ComponentWillUnmount[P, S, B] => G[Unit])(implicit G: Dispatch[G]): This =
      lcAppendDispatch(LifecycleF.componentWillUnmount)(f)

    /**
     * Invoked immediately before rendering when new props or state are being received. This method is not called for the
     * initial render.
     *
     * Use this as an opportunity to perform preparation before an update occurs.
     *
     * Note: You *cannot* use `this.setState()` in this method. If you need to update state in response to a prop change,
     * use `componentWillReceiveProps` instead.
     */
    @deprecated(
      "Use .componentDidUpdate or .getSnapshotBeforeUpdate. See https://reactjs.org/docs/react-component.html#unsafe_componentwillupdate / https://reactjs.org/blog/2018/03/27/update-on-async-rendering.html",
      "scalajs-react 1.7.0 / React 16.9.0")
    def componentWillUpdate[G[_]](f: ComponentWillUpdate[P, S, B] => G[Unit])(implicit G: Dispatch[G]): This =
      lcAppendDispatch(LifecycleF.componentWillUpdate)(f)

    /** getDerivedStateFromProps is invoked right before calling the render method, both on the initial mount and on
      * subsequent updates.
      *
      * This method exists for rare use cases where the state depends on changes in props over time.
      * For example, it might be handy for implementing a Transition component that compares its previous and next
      * children to decide which of them to animate in and out.
      *
      * Deriving state leads to verbose code and makes your components difficult to think about.
      * Make sure you're familiar with simpler alternatives:
      *
      *   - If you need to perform a side effect (for example, data fetching or an animation) in response to a change in
      *     props, use componentDidUpdate lifecycle instead.
      *
      *   - If you want to re-compute some data only when a prop changes, use a memoization helper instead.
      *
      *   - If you want to "reset" some state when a prop changes, consider either making a component fully controlled
      *     or fully uncontrolled with a key instead.
      *
      * Note that this method is fired on every render, regardless of the cause.
      * This is in contrast to componentWillReceiveProps, which only fires when the parent causes a re-render and
      * not as a result of a local setState.
      */
    def getDerivedStateFromProps(f: (P, S) => S): This =
      getDerivedStateFromPropsOption((p, s) => Some(f(p, s)))

    /** getDerivedStateFromProps is invoked right before calling the render method, both on the initial mount and on
      * subsequent updates.
      *
      * This method exists for rare use cases where the state depends on changes in props over time.
      * For example, it might be handy for implementing a Transition component that compares its previous and next
      * children to decide which of them to animate in and out.
      *
      * Deriving state leads to verbose code and makes your components difficult to think about.
      * Make sure you're familiar with simpler alternatives:
      *
      *   - If you need to perform a side effect (for example, data fetching or an animation) in response to a change in
      *     props, use componentDidUpdate lifecycle instead.
      *
      *   - If you want to re-compute some data only when a prop changes, use a memoization helper instead.
      *
      *   - If you want to "reset" some state when a prop changes, consider either making a component fully controlled
      *     or fully uncontrolled with a key instead.
      *
      * Note that this method is fired on every render, regardless of the cause.
      * This is in contrast to componentWillReceiveProps, which only fires when the parent causes a re-render and
      * not as a result of a local setState.
      */
    def getDerivedStateFromProps(f: P => S): This =
      getDerivedStateFromProps((p, _) => f(p))

    /** getDerivedStateFromProps is invoked right before calling the render method, both on the initial mount and on
      * subsequent updates. It should return Some to update the state, or None to update nothing.
      *
      * This method exists for rare use cases where the state depends on changes in props over time.
      * For example, it might be handy for implementing a Transition component that compares its previous and next
      * children to decide which of them to animate in and out.
      *
      * Deriving state leads to verbose code and makes your components difficult to think about.
      * Make sure you're familiar with simpler alternatives:
      *
      *   - If you need to perform a side effect (for example, data fetching or an animation) in response to a change in
      *     props, use componentDidUpdate lifecycle instead.
      *
      *   - If you want to re-compute some data only when a prop changes, use a memoization helper instead.
      *
      *   - If you want to "reset" some state when a prop changes, consider either making a component fully controlled
      *     or fully uncontrolled with a key instead.
      *
      * Note that this method is fired on every render, regardless of the cause.
      * This is in contrast to componentWillReceiveProps, which only fires when the parent causes a re-render and
      * not as a result of a local setState.
      */
    def getDerivedStateFromPropsOption(f: (P, S) => Option[S]): This = {
      val update: Lifecycle_ => Lifecycle_ =
        LifecycleF.getDerivedStateFromProps.mod {
          case None => Some(f)
          case Some(prev) =>
            Some { (p, s1) =>
              prev(p, s1) match {
                case ss2@ Some(s2) => f(p, s2).orElse(ss2)
                case None          => f(p, s1)
              }
            }
        }
      copy(lifecycle = update(lifecycle))
    }

    /** getDerivedStateFromProps is invoked right before calling the render method, both on the initial mount and on
      * subsequent updates. It should return Some to update the state, or None to update nothing.
      *
      * This method exists for rare use cases where the state depends on changes in props over time.
      * For example, it might be handy for implementing a Transition component that compares its previous and next
      * children to decide which of them to animate in and out.
      *
      * Deriving state leads to verbose code and makes your components difficult to think about.
      * Make sure you're familiar with simpler alternatives:
      *
      *   - If you need to perform a side effect (for example, data fetching or an animation) in response to a change in
      *     props, use componentDidUpdate lifecycle instead.
      *
      *   - If you want to re-compute some data only when a prop changes, use a memoization helper instead.
      *
      *   - If you want to "reset" some state when a prop changes, consider either making a component fully controlled
      *     or fully uncontrolled with a key instead.
      *
      * Note that this method is fired on every render, regardless of the cause.
      * This is in contrast to componentWillReceiveProps, which only fires when the parent causes a re-render and
      * not as a result of a local setState.
      */
    def getDerivedStateFromPropsOption(f: P => Option[S]): This =
      getDerivedStateFromPropsOption((p, _) => f(p))

    /** getSnapshotBeforeUpdate() is invoked right before the most recently rendered output is committed to e.g. the
      * DOM. It enables your component to capture some information from the DOM (e.g. scroll position) before it is
      * potentially changed. Any value returned by this lifecycle will be passed as a parameter to componentDidUpdate().
      *
      * This use case is not common, but it may occur in UIs like a chat thread that need to handle scroll position in a
      * special way.
      */
    def getSnapshotBeforeUpdate[G[_], SS](f: GetSnapshotBeforeUpdate[P, S, B] => G[SS])
                                         (implicit ev: UpdateSnapshot.SafetyProof[US], G: Sync[G]): LastStep[P, C, S, B, UpdateSnapshot.Some[SS]] =
      setLC[UpdateSnapshot.Some[SS]](lifecycle.resetSnapshot(None, Some(DS.transSyncFn1(f))))

    /** getSnapshotBeforeUpdate() is invoked right before the most recently rendered output is committed to e.g. the
      * DOM. It enables your component to capture some information from the DOM (e.g. scroll position) before it is
      * potentially changed. Any value returned by this lifecycle will be passed as a parameter to componentDidUpdate().
      *
      * This use case is not common, but it may occur in UIs like a chat thread that need to handle scroll position in a
      * special way.
      */
    def getSnapshotBeforeUpdatePure[A](f: GetSnapshotBeforeUpdate[P, S, B] => A)
                                      (implicit ev: UpdateSnapshot.SafetyProof[US]): LastStep[P, C, S, B, UpdateSnapshot.Some[A]] =
      getSnapshotBeforeUpdate($ => DS.delay(f($)))(ev, DS)

    /** Invoked before rendering when new props or state are being received. This method is not called for the initial
      * render or when `forceUpdate` is used.
      *
      * Use this as an opportunity to `return false` when you're certain that the transition to the new props and state
      * will not require a component update.
      *
      * If `shouldComponentUpdate` returns false, then `render()` will be completely skipped until the next state change.
      * In addition, `componentWillUpdate` and `componentDidUpdate` will not be called.
      *
      * By default, `shouldComponentUpdate` always returns `true` to prevent subtle bugs when `state` is mutated in place,
      * but if you are careful to always treat `state` as immutable and to read only from `props` and `state` in `render()`
      * then you can override `shouldComponentUpdate` with an implementation that compares the old props and state to their
      * replacements.
      *
      * If performance is a bottleneck, especially with dozens or hundreds of components, use `shouldComponentUpdate` to
      * speed up your app.
      */
    def shouldComponentUpdate[G[_]](f: ShouldComponentUpdate[P, S, B] => G[Boolean])(implicit G: Sync[G]): This =
      lcAppend(LifecycleF.shouldComponentUpdate)(DS.transSyncFn1(f))(DS.semigroupSyncOr)

    /**
     * Invoked before rendering when new props or state are being received. This method is not called for the initial
     * render or when `forceUpdate` is used.
     *
     * Use this as an opportunity to `return false` when you're certain that the transition to the new props and state
     * will not require a component update.
     *
     * If `shouldComponentUpdate` returns false, then `render()` will be completely skipped until the next state change.
     * In addition, `componentWillUpdate` and `componentDidUpdate` will not be called.
     *
     * By default, `shouldComponentUpdate` always returns `true` to prevent subtle bugs when `state` is mutated in place,
     * but if you are careful to always treat `state` as immutable and to read only from `props` and `state` in `render()`
     * then you can override `shouldComponentUpdate` with an implementation that compares the old props and state to their
     * replacements.
     *
     * If performance is a bottleneck, especially with dozens or hundreds of components, use `shouldComponentUpdate` to
     * speed up your app.
     */
    def shouldComponentUpdatePure(f: ShouldComponentUpdate[P, S, B] => Boolean): This =
      shouldComponentUpdate($ => DS.delay(f($)))

    def componentDidCatchConst    [G[_]](f: G[Unit]   )(implicit G: Dispatch[G]): This = {val x = DS.transDispatch(f); componentDidCatch    (_ => x)}
    def componentDidMountConst    [G[_]](f: G[Unit]   )(implicit G: Dispatch[G]): This = {val x = DS.transDispatch(f); componentDidMount    (_ => x)}
    def componentDidUpdateConst   [G[_]](f: G[Unit]   )(implicit G: Dispatch[G])       = {val x = DS.transDispatch(f); componentDidUpdate   (_ => x)}
    def componentWillUnmountConst [G[_]](f: G[Unit]   )(implicit G: Dispatch[G]): This = {val x = DS.transDispatch(f); componentWillUnmount (_ => x)}
    def shouldComponentUpdateConst[G[_]](f: G[Boolean])(implicit G: Sync    [G]): This = {val x = DS.transSync    (f); shouldComponentUpdate(_ => x)}
    def shouldComponentUpdateConst      (b : Boolean  )                         : This = shouldComponentUpdateConst(DS.pure(b))

    @deprecated(
      "Use either .initialState* on the component builder, or .componentDidMount. See https://reactjs.org/docs/react-component.html#unsafe_componentwillmount / https://reactjs.org/blog/2018/03/27/update-on-async-rendering.html",
      "scalajs-react 1.7.0 / React 16.9.0")
    def componentWillMountConst[G[_]](cb: G[Unit])(implicit G: Dispatch[G]): This = componentWillMount(_ => cb)

    @deprecated(
      "See https://reactjs.org/docs/react-component.html#unsafe_componentwillreceiveprops / https://reactjs.org/blog/2018/03/27/update-on-async-rendering.html",
      "scalajs-react 1.7.0 / React 16.9.0")
    def componentWillReceivePropsConst[G[_]](cb: G[Unit])(implicit G: Dispatch[G]): This = componentWillReceiveProps(_ => cb)

    @deprecated(
      "Use .componentDidUpdate or .getSnapshotBeforeUpdate. See https://reactjs.org/docs/react-component.html#unsafe_componentwillupdate / https://reactjs.org/blog/2018/03/27/update-on-async-rendering.html",
      "scalajs-react 1.7.0 / React 16.9.0")
    def componentWillUpdateConst[G[_]](cb: G[Unit])(implicit G: Dispatch[G]): This = componentWillUpdate(_ => cb)

    /** This is the end of the road for this component builder.
      *
      * @return Your brand-new, spanking, ScalaComponent. Mmmmmmmm, new-car smell.
      */
    def build(implicit ctorType: CtorType.Summoner[Box[P], C]): Scala.Component[P, S, B, ctorType.CT] = {
      val c = ViaReactComponent(this)
      fromReactComponentClass(c)(ctorType)
    }
  }

  // ===================================================================================================================

  def fromReactComponentClass[P, C <: Children, S, B](rc: facade.React.ComponentClass[Box[P], Box[S]])
                                                    (implicit ctorType: CtorType.Summoner[Box[P], C]): Scala.Component[P, S, B, ctorType.CT] =
    Js.component[Box[P], C, Box[S]](rc)(ctorType)
      .addFacade[Vars[P, S, B]]
      .cmapCtorProps[P](Box(_))
      .mapUnmounted(_
        .mapUnmountedProps(_.unbox)
        .mapMounted(Scala.mountedRoot))
}
