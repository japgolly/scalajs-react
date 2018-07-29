package japgolly.scalajs.react.component.builder

import japgolly.scalajs.react.{Callback, CallbackTo, Children, CtorType, PropsChildren, UpdateSnapshot, raw}
import japgolly.scalajs.react.component.{Js, Scala}
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.vdom.VdomNode
import Scala.{BackendScope, Vars}
import Lifecycle._

object Builder {

  type InitStateFn [-P, +S]  = Box[P] => Box[S]
  type NewBackendFn[P, S, B] = BackendScope[P, S] => B
  type RenderFn    [P, S, B] = RenderScope[P, S, B] => VdomNode

  type Config[P, C <: Children, S, B, US <: UpdateSnapshot, US2 <: UpdateSnapshot] =
    Step4[P, C, S, B, US] => Step4[P, C, S, B, US2]

  private val InitStateUnit: InitStateFn[Any, Unit] =
    _ => Box.Unit

  // ===================================================================================================================

  implicit def defaultToNoState[P](b: Step1[P]): Step2[P, Unit] =
    b.stateless

  implicit def defaultToNoBackend[X, P, S](b: X)(implicit ev: X => Step2[P, S]): Step3[P, S, Unit] =
    b.noBackend

  // ===================================================================================================================

  /** You're on step 1/4 on the way to building a component.
    *
    * If your component is to be stateful, here you must specify it's initial state.
    *
    * If your component is to be stateless, you can skip this step or explicitly use `.stateless`.
    */
  final class Step1[P](name: String) {
    // Dealiases type aliases :(
    // type Next[S] = Step2[P, S]

    def initialState[S](s: => S): Step2[P, S] =
      new Step2(name, _ => Box(s))

    def initialStateFromProps[S](f: P => S): Step2[P, S] =
      new Step2(name, p => Box(f(p.unbox)))

    def initialStateCallback[S](cb: CallbackTo[S]): Step2[P, S] =
      initialState(cb.runNow())

    def initialStateCallbackFromProps[S](f: P => CallbackTo[S]): Step2[P, S] =
      initialStateFromProps(f(_).runNow())

    def stateless: Step2[P, Unit] =
      new Step2(name, InitStateUnit)
  }

  // ===================================================================================================================

  /** You're on step 2/4 on the way to building a component.
    *
    * Here you specify whether your component has a "backend" or not.
    * A backend like a class that is created when your component mounts and remains until it is unmounted.
    *
    * If you don't need a backend, you can skip this step or explicitly use `.noBackend`.
    *
    * Making common cases convenient, you can even use `.renderBackend` or `.renderBackendWithChildren` to take care
    * of both this and the following step automatically, using macros to save you typing boring boilerplate.
    * If you have an unhealthy fear of macros you can ignore then and do it all manually too; the macros don't have any
    * special privileges.
    */
  final class Step2[P, S](name: String, initStateFn: InitStateFn[P, S]) {
    // Dealiases type aliases :(
    // type Next[B] = Step3[P, S, B]

    def backend[B](f: NewBackendFn[P, S, B]): Step3[P, S, B] =
      new Step3(name, initStateFn, f)

    def noBackend: Step3[P, S, Unit] =
      backend(_ => ())

    /**
     * Shortcut for:
     *
     * {{{
     *   .backend[B](new B(_))
     *   .renderBackend
     * }}}
     */
    def renderBackend[B]: Step4[P, Children.None, S, B, UpdateSnapshot.None] =
      macro ComponentBuilderMacros.backendAndRender[P, S, B]

    /**
     * Shortcut for:
     *
     * {{{
     *   .backend[B](new B(_))
     *   .renderBackendWithChildren
     * }}}
     */
    def renderBackendWithChildren[B]: Step4[P, Children.Varargs, S, B, UpdateSnapshot.None] =
      macro ComponentBuilderMacros.backendAndRenderWithChildren[P, S, B]
  }

  // ===================================================================================================================

  /** You're on step 3/4 on the way to building a component.
    *
    * Here you specify your component's render function. For convenience there are a plethoria of methods you can call
    * that all do the same thing but accept the argument in different shapes; the suffixes in method names refer to the
    * argument type(s). This means you choose the method that provides the types that you need, rather than having to
    * manually specify the types for all arguments including stuff you don't need.
    *
    * If you're using a backend, then it's highly recommended that you put your render function in the backend.
    * When you do that, make sure it's called `.render` and then here in the builder, use the `.renderBackend` or
    * `.renderBackendWithChildren` methods which will use a macro to inspect your backend's render method and provide
    * everything it needs automatically.
    */
  final class Step3[P, S, B](name: String, initStateFn: InitStateFn[P, S], backendFn: NewBackendFn[P, S, B]) {
    // Dealiases type aliases :(
    // type Next[C <: Children] = Step4[P, C, S, B]

    type $ = RenderScope[P, S, B]

    def renderWith[C <: Children](r: RenderFn[P, S, B]): Step4[P, C, S, B, UpdateSnapshot.None] =
      new Step4[P, C, S, B, UpdateSnapshot.None](name, initStateFn, backendFn, r, Lifecycle.empty)

    // No args

    def renderStatic(r: VdomNode): Step4[P, Children.None, S, B, UpdateSnapshot.None] =
      renderWith(_ => r)

    def render_(r: => VdomNode): Step4[P, Children.None, S, B, UpdateSnapshot.None] =
      renderWith(_ => r)

    // No children

    def render(r: RenderFn[P, S, B]): Step4[P, Children.None, S, B, UpdateSnapshot.None] =
      renderWith(r)

    def renderPS(r: ($, P, S) => VdomNode): Step4[P, Children.None, S, B, UpdateSnapshot.None] =
       renderWith($ => r($, $.props, $.state))

     def renderP(r: ($, P) => VdomNode): Step4[P, Children.None, S, B, UpdateSnapshot.None] =
       renderWith($ => r($, $.props))

     def renderS(r: ($, S) => VdomNode): Step4[P, Children.None, S, B, UpdateSnapshot.None] =
       renderWith($ => r($, $.state))

     def render_PS(r: (P, S) => VdomNode): Step4[P, Children.None, S, B, UpdateSnapshot.None] =
       renderWith($ => r($.props, $.state))

     def render_P(r: P => VdomNode): Step4[P, Children.None, S, B, UpdateSnapshot.None] =
       renderWith($ => r($.props))

     def render_S(r: S => VdomNode): Step4[P, Children.None, S, B, UpdateSnapshot.None] =
       renderWith($ => r($.state))

    // Has children

     def renderPCS(r: ($, P, PropsChildren, S) => VdomNode): Step4[P, Children.Varargs, S, B, UpdateSnapshot.None] =
       renderWith($ => r($, $.props, $.propsChildren, $.state))

     def renderPC(r: ($, P, PropsChildren) => VdomNode): Step4[P, Children.Varargs, S, B, UpdateSnapshot.None] =
       renderWith($ => r($, $.props, $.propsChildren))

     def renderCS(r: ($, PropsChildren, S) => VdomNode): Step4[P, Children.Varargs, S, B, UpdateSnapshot.None] =
       renderWith($ => r($, $.propsChildren, $.state))

     def renderC(r: ($, PropsChildren) => VdomNode): Step4[P, Children.Varargs, S, B, UpdateSnapshot.None] =
       renderWith($ => r($, $.propsChildren))

     def render_PCS(r: (P, PropsChildren, S) => VdomNode): Step4[P, Children.Varargs, S, B, UpdateSnapshot.None] =
       renderWith($ => r($.props, $.propsChildren, $.state))

     def render_PC(r: (P, PropsChildren) => VdomNode): Step4[P, Children.Varargs, S, B, UpdateSnapshot.None] =
       renderWith($ => r($.props, $.propsChildren))

     def render_CS(r: (PropsChildren, S) => VdomNode): Step4[P, Children.Varargs, S, B, UpdateSnapshot.None] =
       renderWith($ => r($.propsChildren, $.state))

     def render_C(r: PropsChildren => VdomNode): Step4[P, Children.Varargs, S, B, UpdateSnapshot.None] =
       renderWith($ => r($.propsChildren))

    /**
     * Use a method named `render` in the backend, automatically populating its arguments with props and state
     * where needed.
     */
    def renderBackend: Step4[P, Children.None, S, B, UpdateSnapshot.None] =
      macro ComponentBuilderMacros.renderBackend[P, S, B]

    /**
     * Use a method named `render` in the backend, automatically populating its arguments with props, state, and
     * propsChildren where needed.
     */
    def renderBackendWithChildren: Step4[P, Children.Varargs, S, B, UpdateSnapshot.None] =
      macro ComponentBuilderMacros.renderBackendWithChildren[P, S, B]
  }

  // ===================================================================================================================

  /** You're on the final step on the way to building a component.
    *
    * This is where you add lifecycle callbacks if you need to.
    *
    * When you're done, simply call `.build` to create and return your `ScalaComponent`.
    */
  final class Step4[P, C <: Children, S, B, US <: UpdateSnapshot](
      val name: String,
      private[builder] val initStateFn: InitStateFn[P, S],
      private[builder] val backendFn  : NewBackendFn[P, S, B],
      private[builder] val renderFn   : RenderFn[P, S, B],
      private[builder] val lifecycle  : Lifecycle[P, S, B, US#Value]) {

    type This = Step4[P, C, S, B, US]

    type SnapshotValue = US#Value

    private type Lifecycle_ = Lifecycle[P, S, B, SnapshotValue]

    private def copy(name       : String                = this.name,
                     initStateFn: InitStateFn [P, S]    = this.initStateFn,
                     backendFn  : NewBackendFn[P, S, B] = this.backendFn,
                     renderFn   : RenderFn    [P, S, B] = this.renderFn,
                     lifecycle  : Lifecycle_            = this.lifecycle): This =
      new Step4(name, initStateFn, backendFn, renderFn, lifecycle)

    private def setLC[US2 <: UpdateSnapshot](lc: Lifecycle[P, S, B, US2#Value]): Step4[P, C, S, B, US2] =
      new Step4(name, initStateFn, backendFn, renderFn, lc)

    private def lcAppend[I, O](lens: Lens[Lifecycle_, Option[I => O]])(g: I => O)(implicit s: Semigroup[O]): This =
      copy(lifecycle = lifecycle.append(lens)(g)(s))

    private def lcAppend2[I1, I2, O](lens: Lens[Lifecycle_, Option[(I1, I2) => O]])(g: (I1, I2) => O)(implicit s: Semigroup[O]): This =
      copy(lifecycle = lifecycle.append2(lens)(g)(s))

    @inline def configure[US2 <: UpdateSnapshot](f: Config[P, C, S, B, US, US2]): Step4[P, C, S, B, US2] =
      f(this)

    @inline def configureWhen(cond: Boolean)(f: => Config[P, C, S, B, US, US]): Step4[P, C, S, B, US] =
      if (cond) configure(f) else this

    @inline def configureUnless(cond: Boolean)(f: => Config[P, C, S, B, US, US]): Step4[P, C, S, B, US] =
      if (cond) this else configure(f)

    /**
      * Error boundaries are React components that catch errors anywhere in their child component tree, log those errors,
      * and display a fallback UI instead of the component tree that crashed. Error boundaries catch errors during
      * rendering, in lifecycle methods, and in constructors of the whole tree below them.
      *
      * Note: "CHILD COMPONENT TREE". Components cannot catch errors in themselves, only their children.
      */
    def componentDidCatch(f: ComponentDidCatchFn[P, S, B]): This =
      lcAppend(Lifecycle.componentDidCatch)(f)

    /**
     * Invoked once, only on the client (not on the server), immediately after the initial rendering occurs. At this point
     * in the lifecycle, the component has a DOM representation which you can access via `ReactDOM.findDOMNode(this)`.
     * The `componentDidMount()` method of child components is invoked before that of parent components.
     *
     * If you want to integrate with other JavaScript frameworks, set timers using `setTimeout` or `setInterval`, or send
     * AJAX requests, perform those operations in this method.
     */
    def componentDidMount(f: ComponentDidMountFn[P, S, B]): This =
      lcAppend(Lifecycle.componentDidMount)(f)

    /**
     * Invoked immediately after the component's updates are flushed to the DOM. This method is not called for the initial
     * render.
     *
     * Use this as an opportunity to operate on the DOM when the component has been updated.
     */
    def componentDidUpdate(f: ComponentDidUpdateFn[P, S, B, SnapshotValue]): Step4[P, C, S, B, UpdateSnapshot.Some[SnapshotValue]] =
      setLC[UpdateSnapshot.Some[SnapshotValue]](lcAppend(Lifecycle.componentDidUpdate)(f).lifecycle)

    /**
     * Invoked once, both on the client and server, immediately before the initial rendering occurs.
     * If you call `setState` within this method, `render()` will see the updated state and will be executed only once
     * despite the state change.
     */
    def componentWillMount(f: ComponentWillMountFn[P, S, B]): This =
      lcAppend(Lifecycle.componentWillMount)(f)

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
    def componentWillReceiveProps(f: ComponentWillReceivePropsFn[P, S, B]): This =
      lcAppend(Lifecycle.componentWillReceiveProps)(f)

    /**
     * Invoked immediately before a component is unmounted from the DOM.
     *
     * Perform any necessary cleanup in this method, such as invalidating timers or cleaning up any DOM elements that were
     * created in `componentDidMount`.
     */
    def componentWillUnmount(f: ComponentWillUnmountFn[P, S, B]): This =
      lcAppend(Lifecycle.componentWillUnmount)(f)

    /**
     * Invoked immediately before rendering when new props or state are being received. This method is not called for the
     * initial render.
     *
     * Use this as an opportunity to perform preparation before an update occurs.
     *
     * Note: You *cannot* use `this.setState()` in this method. If you need to update state in response to a prop change,
     * use `componentWillReceiveProps` instead.
     */
    def componentWillUpdate(f: ComponentWillUpdateFn[P, S, B]): This =
      lcAppend(Lifecycle.componentWillUpdate)(f)

    /** getDerivedStateFromProps is invoked right before calling the render method, both on the initial mount and on
      * subsequent updates. It should return Some to update the state, or None to update nothing.
      *
      * This method exists for rare use cases where the state depends on changes in props over time.
      * For example, it might be handy for implementing a Transition component that compares its previous and next
      * children to decide which of them to animate in and out.
      *
      * Deriving state leads to verbose code and makes your components difficult to think about.
      * Make sure you’re familiar with simpler alternatives:
      *
      *   - If you need to perform a side effect (for example, data fetching or an animation) in response to a change in
      *     props, use componentDidUpdate lifecycle instead.
      *
      *   - If you want to re-compute some data only when a prop changes, use a memoization helper instead.
      *
      *   - If you want to “reset” some state when a prop changes, consider either making a component fully controlled
      *     or fully uncontrolled with a key instead.
      *
      * Note that this method is fired on every render, regardless of the cause.
      * This is in contrast to componentWillReceiveProps, which only fires when the parent causes a re-render and
      * not as a result of a local setState.
      */
    def getDerivedStateFromProps(f: (P, S) => Option[S]): This =
      lcAppend2(Lifecycle.getDerivedStateFromProps)(f)(Semigroup.optionFirst)

    /** getDerivedStateFromProps is invoked right before calling the render method, both on the initial mount and on
      * subsequent updates. It should return Some to update the state, or None to update nothing.
      *
      * This method exists for rare use cases where the state depends on changes in props over time.
      * For example, it might be handy for implementing a Transition component that compares its previous and next
      * children to decide which of them to animate in and out.
      *
      * Deriving state leads to verbose code and makes your components difficult to think about.
      * Make sure you’re familiar with simpler alternatives:
      *
      *   - If you need to perform a side effect (for example, data fetching or an animation) in response to a change in
      *     props, use componentDidUpdate lifecycle instead.
      *
      *   - If you want to re-compute some data only when a prop changes, use a memoization helper instead.
      *
      *   - If you want to “reset” some state when a prop changes, consider either making a component fully controlled
      *     or fully uncontrolled with a key instead.
      *
      * Note that this method is fired on every render, regardless of the cause.
      * This is in contrast to componentWillReceiveProps, which only fires when the parent causes a re-render and
      * not as a result of a local setState.
      */
    def getDerivedStateFromProps(f: P => Option[S]): This =
      getDerivedStateFromProps((p, _) => f(p))

    /** getSnapshotBeforeUpdate() is invoked right before the most recently rendered output is committed to e.g. the
      * DOM. It enables your component to capture some information from the DOM (e.g. scroll position) before it is
      * potentially changed. Any value returned by this lifecycle will be passed as a parameter to componentDidUpdate().
      *
      * This use case is not common, but it may occur in UIs like a chat thread that need to handle scroll position in a
      * special way.
      */
    def getSnapshotBeforeUpdate[A](f: GetSnapshotBeforeUpdateFn[P, S, B, A])
                                  (implicit ev: UpdateSnapshot.SafetyProof[US]): Step4[P, C, S, B, UpdateSnapshot.Some[A]] =
      setLC[UpdateSnapshot.Some[A]](lifecycle.resetSnapshot(None, Some(f)))

    /** getSnapshotBeforeUpdate() is invoked right before the most recently rendered output is committed to e.g. the
      * DOM. It enables your component to capture some information from the DOM (e.g. scroll position) before it is
      * potentially changed. Any value returned by this lifecycle will be passed as a parameter to componentDidUpdate().
      *
      * This use case is not common, but it may occur in UIs like a chat thread that need to handle scroll position in a
      * special way.
      */
    def getSnapshotBeforeUpdatePure[A](f: GetSnapshotBeforeUpdate[P, S, B] => A)
                                      (implicit ev: UpdateSnapshot.SafetyProof[US]): Step4[P, C, S, B, UpdateSnapshot.Some[A]] =
      getSnapshotBeforeUpdate($ => CallbackTo(f($)))(ev)

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
    def shouldComponentUpdate(f: ShouldComponentUpdateFn[P, S, B]): This =
      lcAppend(Lifecycle.shouldComponentUpdate)(f)(Semigroup.eitherCB)

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
      shouldComponentUpdate($ => CallbackTo(f($)))

    def componentDidCatchConst        (cb: Callback           ): This = componentDidCatch         (_ => cb)
    def componentDidMountConst        (cb: Callback           ): This = componentDidMount         (_ => cb)
    def componentDidUpdateConst       (cb: Callback           )       = componentDidUpdate        (_ => cb)
    def componentWillMountConst       (cb: Callback           ): This = componentWillMount        (_ => cb)
    def componentWillReceivePropsConst(cb: Callback           ): This = componentWillReceiveProps (_ => cb)
    def componentWillUnmountConst     (cb: Callback           ): This = componentWillUnmount      (_ => cb)
    def componentWillUpdateConst      (cb: Callback           ): This = componentWillUpdate       (_ => cb)
    def shouldComponentUpdateConst    (cb: CallbackTo[Boolean]): This = shouldComponentUpdate     (_ => cb)
    def shouldComponentUpdateConst    (b : Boolean            ): This = shouldComponentUpdateConst(CallbackTo pure b)

    /** This is the end of the road for this component builder.
      *
      * @return Your brand-new, spanking, ScalaComponent. Mmmmmmmm, new-car smell.
      */
    def build(implicit ctorType: CtorType.Summoner[Box[P], C], snapshotJs: JsRepr[SnapshotValue]): Scala.Component[P, S, B, ctorType.CT] = {
      val c = ViaReactComponent(this)(snapshotJs)
      fromReactComponentClass(c)(ctorType)
    }
  }

  // ===================================================================================================================

  def fromReactComponentClass[P, C <: Children, S, B](rc: raw.React.ComponentClass[Box[P], Box[S]])
                                                    (implicit ctorType: CtorType.Summoner[Box[P], C]): Scala.Component[P, S, B, ctorType.CT] =
    Js.component[Box[P], C, Box[S]](rc)(ctorType)
      .addFacade[Vars[P, S, B]]
      .cmapCtorProps[P](Box(_))
      .mapUnmounted(_
        .mapUnmountedProps(_.unbox)
        .mapMounted(Scala.mountedRoot))
}
