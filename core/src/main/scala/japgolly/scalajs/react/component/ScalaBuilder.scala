package japgolly.scalajs.react.component

import org.scalajs.dom
import scala.{Either => Or}
import scalajs.js
import japgolly.scalajs.react.{Callback, CallbackTo, Children, CtorType, PropsChildren, raw, vdom}
import japgolly.scalajs.react.internal._
import Scala._

object ScalaBuilder {
  import Lifecycle._

  type InitStateFnU[P, S]    = Generic.BaseUnmounted[P, _, Box[P], _]
  type InitStateArg[P, S]    = (InitStateFnU[P, S] => S) Or js.Function0[Box[S]]
  type NewBackendFn[P, S, B] = BackendScope[P, S] => B
  type RenderFn    [P, S, B] = RenderScope[P, S, B] => vdom.ReactElement

  private val InitStateUnit : Nothing Or js.Function0[Box[Unit]] =
    Right(() => Box.Unit)

  implicit def defaultToNoState  [P](b: Step1[P]): Step2[P, Unit] = b.stateless
  implicit def defaultToNoBackend[X, P, S](b: X)(implicit ev: X => Step2[P, S]): Step3[P, S, Unit] = b.noBackend

  // ===================================================================================================================

  final class Step1[P](name: String) {
    // Dealiases type aliases :(
    // type Next[S] = Step2[P, S]

    // getInitialState is how it's named in React
    def getInitialState  [S](f: InitStateFnU[P, S] => S)            : Step2[P, S] = new Step2(name, Left(f))
    def getInitialStateCB[S](f: InitStateFnU[P, S] => CallbackTo[S]): Step2[P, S] = getInitialState(f.andThen(_.runNow()))

    // More convenient methods that don't need the full CompScope
    def initialState    [S](s: => S              ): Step2[P, S] = new Step2(name, Right(() => Box(s)))
    def initialStateCB  [S](s: CallbackTo[S]     ): Step2[P, S] = initialState(s.runNow())
    def initialState_P  [S](f: P => S            ): Step2[P, S] = getInitialState[S]($ => f($.props))
    def initialStateCB_P[S](f: P => CallbackTo[S]): Step2[P, S] = getInitialState[S]($ => f($.props).runNow())

    def stateless: Step2[P, Unit] =
      new Step2(name, InitStateUnit)
  }

  // ===================================================================================================================

  final class Step2[P, S](name: String, initStateFn: InitStateArg[P, S]) {
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
    def renderBackend[B]: Step4[P, Children.None, S, B] =
      macro ComponentBuilderMacros.backendAndRender[P, S, B]

    /**
     * Shortcut for:
     *
     * {{{
     *   .backend[B](new B(_))
     *   .renderBackendWithChildren
     * }}}
     */
    def renderBackendWithChildren[B]: Step4[P, Children.Varargs, S, B] =
      macro ComponentBuilderMacros.backendAndRenderWithChildren[P, S, B]
  }

  // ===================================================================================================================

  final class Step3[P, S, B](name: String, initStateFn: InitStateArg[P, S], backendFn: NewBackendFn[P, S, B]) {
    // Dealiases type aliases :(
    // type Next[C <: Children] = Step4[P, C, S, B]

    type $ = RenderScope[P, S, B]

    def renderWith[C <: Children](r: RenderFn[P, S, B]): Step4[P, C, S, B] =
      new Step4[P, C, S, B](name, initStateFn, backendFn, r, Lifecycle.empty)

    // No args

    def renderStatic(r: vdom.ReactElement): Step4[P, Children.None, S, B] =
      renderWith(_ => r)

    def render_(r: => vdom.ReactElement): Step4[P, Children.None, S, B] =
      renderWith(_ => r)

    // No children

    def render(r: RenderFn[P, S, B]): Step4[P, Children.None, S, B] =
      renderWith(r)

    def renderPS(r: ($, P, S) => vdom.ReactElement): Step4[P, Children.None, S, B] =
       renderWith($ => r($, $.props, $.state))

     def renderP(r: ($, P) => vdom.ReactElement): Step4[P, Children.None, S, B] =
       renderWith($ => r($, $.props))

     def renderS(r: ($, S) => vdom.ReactElement): Step4[P, Children.None, S, B] =
       renderWith($ => r($, $.state))

     def render_PS(r: (P, S) => vdom.ReactElement): Step4[P, Children.None, S, B] =
       renderWith($ => r($.props, $.state))

     def render_P(r: P => vdom.ReactElement): Step4[P, Children.None, S, B] =
       renderWith($ => r($.props))

     def render_S(r: S => vdom.ReactElement): Step4[P, Children.None, S, B] =
       renderWith($ => r($.state))

    // Has children

     def renderPCS(r: ($, P, PropsChildren, S) => vdom.ReactElement): Step4[P, Children.Varargs, S, B] =
       renderWith($ => r($, $.props, $.propsChildren, $.state))

     def renderPC(r: ($, P, PropsChildren) => vdom.ReactElement): Step4[P, Children.Varargs, S, B] =
       renderWith($ => r($, $.props, $.propsChildren))

     def renderCS(r: ($, PropsChildren, S) => vdom.ReactElement): Step4[P, Children.Varargs, S, B] =
       renderWith($ => r($, $.propsChildren, $.state))

     def renderC(r: ($, PropsChildren) => vdom.ReactElement): Step4[P, Children.Varargs, S, B] =
       renderWith($ => r($, $.propsChildren))

     def render_PCS(r: (P, PropsChildren, S) => vdom.ReactElement): Step4[P, Children.Varargs, S, B] =
       renderWith($ => r($.props, $.propsChildren, $.state))

     def render_PC(r: (P, PropsChildren) => vdom.ReactElement): Step4[P, Children.Varargs, S, B] =
       renderWith($ => r($.props, $.propsChildren))

     def render_CS(r: (PropsChildren, S) => vdom.ReactElement): Step4[P, Children.Varargs, S, B] =
       renderWith($ => r($.propsChildren, $.state))

     def render_C(r: PropsChildren => vdom.ReactElement): Step4[P, Children.Varargs, S, B] =
       renderWith($ => r($.propsChildren))

    /**
     * Use a method named `render` in the backend, automatically populating its arguments with props and state
     * where needed.
     */
    def renderBackend: Step4[P, Children.None, S, B] =
      macro ComponentBuilderMacros.renderBackend[P, S, B]

    /**
     * Use a method named `render` in the backend, automatically populating its arguments with props, state, and
     * propsChildren where needed.
     */
    def renderBackendWithChildren: Step4[P, Children.Varargs, S, B] =
      macro ComponentBuilderMacros.renderBackendWithChildren[P, S, B]
  }

  // ===================================================================================================================

  type Config[P, C <: Children, S, B] =
    Step4[P, C, S, B] => Step4[P, C, S, B]

  final class Step4[P, C <: Children, S, B](val name   : String,
                                            initStateFn: InitStateArg[P, S],
                                            backendFn  : NewBackendFn[P, S, B],
                                            renderFn   : RenderFn[P, S, B],
                                            lifecycle  : Lifecycle[P, S, B]) {
    type This = Step4[P, C, S, B]

    private def copy(name       : String                = this.name       ,
                     initStateFn: InitStateArg[P, S]    = this.initStateFn,
                     backendFn  : NewBackendFn[P, S, B] = this.backendFn  ,
                     renderFn   : RenderFn    [P, S, B] = this.renderFn   ,
                     lifecycle  : Lifecycle   [P, S, B] = this.lifecycle  ): This =
      new Step4(name, initStateFn, backendFn, renderFn, lifecycle)

    private def lcAppend[I, O](lens: Lens[Lifecycle[P, S, B], Option[I => O]])(g: I => O)(implicit s: Semigroup[O]): This =
      copy(lifecycle = lifecycle.append(lens)(g)(s))

    def configure(fs: Config[P, C, S, B]*): This =
      fs.foldLeft(this)((s, f) => f(s))

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
    def componentDidUpdate(f: ComponentDidUpdateFn[P, S, B]): This =
      lcAppend(Lifecycle.componentDidUpdate)(f)

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

    @deprecated("Use componentDidMountConst",         "1.0.0") def componentDidMountCB        (cb: Callback           ): This = componentDidMount        (_ => cb)
    @deprecated("Use componentDidUpdateConst",        "1.0.0") def componentDidUpdateCB       (cb: Callback           ): This = componentDidUpdate       (_ => cb)
    @deprecated("Use componentWillMountConst",        "1.0.0") def componentWillMountCB       (cb: Callback           ): This = componentWillMount       (_ => cb)
    @deprecated("Use componentWillReceivePropsConst", "1.0.0") def componentWillReceivePropsCB(cb: Callback           ): This = componentWillReceiveProps(_ => cb)
    @deprecated("Use componentWillUnmountConst",      "1.0.0") def componentWillUnmountCB     (cb: Callback           ): This = componentWillUnmount     (_ => cb)
    @deprecated("Use componentWillUpdateConst",       "1.0.0") def componentWillUpdateCB      (cb: Callback           ): This = componentWillUpdate      (_ => cb)
    @deprecated("Use shouldComponentUpdateConst",     "1.0.0") def shouldComponentUpdateCB    (cb: CallbackTo[Boolean]): This = shouldComponentUpdate    (_ => cb)

    def componentDidMountConst        (cb: Callback           ): This = componentDidMount         (_ => cb)
    def componentDidUpdateConst       (cb: Callback           ): This = componentDidUpdate        (_ => cb)
    def componentWillMountConst       (cb: Callback           ): This = componentWillMount        (_ => cb)
    def componentWillReceivePropsConst(cb: Callback           ): This = componentWillReceiveProps (_ => cb)
    def componentWillUnmountConst     (cb: Callback           ): This = componentWillUnmount      (_ => cb)
    def componentWillUpdateConst      (cb: Callback           ): This = componentWillUpdate       (_ => cb)
    def shouldComponentUpdateConst    (cb: CallbackTo[Boolean]): This = shouldComponentUpdate     (_ => cb)
    def shouldComponentUpdateConst    (b : Boolean            ): This = shouldComponentUpdateConst(CallbackTo pure b)

    def spec: raw.ReactComponentSpec = {
      val spec = js.Object().asInstanceOf[raw.ReactComponentSpec]

      @inline def castV($: raw.ReactComponent) = $.asInstanceOf[RawMounted[P, S, B]]
      @inline def castP($: raw.Props) = $.asInstanceOf[Box[P]]
      @inline def castS($: raw.State) = $.asInstanceOf[Box[S]]

      for (n <- Option(name))
        spec.displayName = n

      def withMounted[A](f: RenderScope[P, S, B] => A): js.ThisFunction0[raw.ReactComponent, A] =
        ($: raw.ReactComponent) =>
          f(new RenderScope(castV($)))

      spec.render = withMounted(renderFn.andThen(_.rawReactElement))

      spec.getInitialState =
        initStateFn match {
          case Right(fn0) => fn0
          case Left(fn) => ((rc: raw.ReactComponentElement) => {
            val js = Js.unmounted[Box[P], Box[S]](rc)
            Box(fn(js.mapUnmountedProps(_.unbox)))
          }): js.ThisFunction0[raw.ReactComponentElement, Box[S]]
        }

      val setup: raw.ReactComponent => Unit =
        $ => {
          val jMounted : JsMounted  [P, S, B] = Js.mounted[Box[P], Box[S]]($).addFacade[Vars[P, S, B]]
          val sMounted : Mounted    [P, S, B] = Scala.rootMounted(jMounted)
          val sMountedP: MountedPure[P, S, B] = sMounted.withEffect
          val backend  : B                    = backendFn(sMountedP)
          jMounted.raw.mounted     = sMounted
          jMounted.raw.mountedPure = sMountedP
          jMounted.raw.backend     = backend
        }
      spec.componentWillMount = lifecycle.componentWillMount match {
        case None    => setup
        case Some(f) =>
          ($: raw.ReactComponent) => {
            setup($)
            f(new ComponentWillMount(castV($))).runNow()
          }
      }

      val teardown: raw.ReactComponent => Unit =
        $ => {
          val vars = castV($)
          vars.mounted      = null
          vars.mountedPure = null
          vars.backend     = null.asInstanceOf[B]
        }
      spec.componentWillUnmount = lifecycle.componentWillUnmount match {
        case None    => teardown
        case Some(f) =>
          ($: raw.ReactComponent) => {
            f(new ComponentWillUnmount(castV($))).runNow()
            teardown($)
          }
      }

      lifecycle.componentDidMount.foreach(f =>
        spec.componentDidMount = ($: raw.ReactComponent) =>
          f(new ComponentDidMount(castV($))).runNow())

      lifecycle.componentDidUpdate.foreach(f =>
        spec.componentDidUpdate = ($: raw.ReactComponent, p: raw.Props, s: raw.State) =>
          f(new ComponentDidUpdate(castV($), castP(p).unbox, castS(s).unbox)).runNow())

      lifecycle.componentWillReceiveProps.foreach(f =>
        spec.componentWillReceiveProps = ($: raw.ReactComponent, p: raw.Props) =>
          f(new ComponentWillReceiveProps(castV($), castP(p).unbox)).runNow())

      lifecycle.componentWillUpdate.foreach(f =>
        spec.componentWillUpdate = ($: raw.ReactComponent, p: raw.Props, s: raw.State) =>
          f(new ComponentWillUpdate(castV($), castP(p).unbox, castS(s).unbox)).runNow())

      lifecycle.shouldComponentUpdate.foreach(f =>
        spec.shouldComponentUpdate = ($: raw.ReactComponent, p: raw.Props, s: raw.State) =>
          f(new ShouldComponentUpdate(castV($), castP(p).unbox, castS(s).unbox)).runNow())

//        if (jsMixins.nonEmpty)
//          spec("mixins") = JArray(jsMixins: _*)
//
//        lc.configureSpec.foreach(_(spec2).runNow())

      spec
    }

    def build(implicit ctorType: CtorType.Summoner[Box[P], C]): Scala.Component[P, S, B, ctorType.CT] = {
      val rc = raw.React.createClass(spec)
      Js.component[Box[P], C, Box[S]](rc)(ctorType)
        .addFacade[Vars[P, S, B]]
        .cmapCtorProps[P](Box(_))
        .mapUnmounted(_
          .mapUnmountedProps(_.unbox)
          .mapMounted(Scala.rootMounted))
    }
  }

  // ===================================================================================================================

  final case class Lifecycle[P, S, B](
    componentDidMount        : Option[ComponentDidMountFn        [P, S, B]],
    componentDidUpdate       : Option[ComponentDidUpdateFn       [P, S, B]],
    componentWillMount       : Option[ComponentWillMountFn       [P, S, B]],
    componentWillReceiveProps: Option[ComponentWillReceivePropsFn[P, S, B]],
    componentWillUnmount     : Option[ComponentWillUnmountFn     [P, S, B]],
    componentWillUpdate      : Option[ComponentWillUpdateFn      [P, S, B]],
    shouldComponentUpdate    : Option[ShouldComponentUpdateFn    [P, S, B]]) {

    type This = Lifecycle[P, S, B]

    def append[I, O](lens: Lens[Lifecycle[P, S, B], Option[I => O]])(g: I => O)(implicit s: Semigroup[O]): This =
      lens.mod(o => Some(o.fold(g)(f => i => s.append(f(i), g(i)))))(this)
  }

  object Lifecycle {
    def empty[P, S, B]: Lifecycle[P, S, B] =
      new Lifecycle(None, None, None, None, None, None, None)

    // Reads are untyped
    //   - Safe because of implementation in builder (creating a new Callback on demand).
    //   - Preferred because use is easier. (TODO is it really?)

    // Writes are Callbacks
    //   - All state modification from within a component should return a Callback.
    //     Consistency, type-safe, protects API & future changes.

    // Missing from all below:
    //   - def isMounted: F[Boolean]
    //   - def withEffect[G[_]](implicit t: Effect.Trans[F, G]): Props[G, P]
    //   - def mapProps[X](f: P => X): Mounted[F, X, S] =
    //   - def xmapState[X](f: S => X)(g: X => S): Mounted[F, P, X] =
    //   - def zoomState[X](get: S => X)(set: X => S => S): Mounted[F, P, X] =

    sealed trait Base[P, S, B] extends Any {
      def raw: RawMounted[P, S, B]

      final def backend      : B                    = raw.backend
      final def mountedImpure: Mounted    [P, S, B] = raw.mounted
      final def mountedPure  : MountedPure[P, S, B] = raw.mountedPure
    }

    sealed trait StateW[P, S, B] extends Any with Base[P, S, B] {
      final def setState(newState: S, cb: Callback = Callback.empty): Callback = mountedPure.setState(newState, cb)
      final def modState(mod: S => S, cb: Callback = Callback.empty): Callback = mountedPure.modState(mod, cb)

      @deprecated("Renamed to setStateFn", "1.0.0")
      final def _setState[I](f: I => S, callback: Callback = Callback.empty): I => Callback =
        setStateFn(f, callback)

      @deprecated("Renamed to modStateFn", "1.0.0")
      final def _modState[I](f: I => S => S, callback: Callback = Callback.empty): I => Callback =
        modStateFn(f, callback)

      final def setStateFn[I](f: I => S, callback: Callback = Callback.empty): I => Callback =
        i => setState(f(i), callback)

      final def modStateFn[I](f: I => S => S, callback: Callback = Callback.empty): I => Callback =
        i => modState(f(i), callback)
    }

    sealed trait StateRW[P, S, B] extends Any with StateW[P, S, B] {
      final def state: S = mountedImpure.state
    }

    sealed trait ForceUpdate[P, S, B] extends Any with Base[P, S, B] {
      final def forceUpdate(cb: Callback = Callback.empty): Callback = mountedPure.forceUpdate(cb)
    }

    // ===================================================================================================================

    def componentDidMount[P, S, B] = Lens((_: Lifecycle[P, S, B]).componentDidMount)(n => _.copy(componentDidMount = n))

    type ComponentDidMountFn[P, S, B] = ComponentDidMount[P, S, B] => Callback

    final class ComponentDidMount[P, S, B](val raw: RawMounted[P, S, B])
        extends AnyVal with StateRW[P, S, B] with ForceUpdate[P, S, B] {

      def props        : P                = mountedImpure.props
      def propsChildren: PropsChildren    = mountedImpure.propsChildren
      def getDOMNode   : dom.Element      = mountedImpure.getDOMNode
    }

    // ===================================================================================================================

    def componentDidUpdate[P, S, B] = Lens((_: Lifecycle[P, S, B]).componentDidUpdate)(n => _.copy(componentDidUpdate = n))

    type ComponentDidUpdateFn[P, S, B] = ComponentDidUpdate[P, S, B] => Callback

    final class ComponentDidUpdate[P, S, B](val raw: RawMounted[P, S, B], val prevProps: P, val prevState: S)
        extends StateW[P, S, B] with ForceUpdate[P, S, B] {

      def propsChildren: PropsChildren = mountedImpure.propsChildren
      def currentProps : P             = mountedImpure.props
      def currentState : S             = mountedImpure.state
      def getDOMNode   : dom.Element   = mountedImpure.getDOMNode
    }

    // ===================================================================================================================

    def componentWillMount[P, S, B] = Lens((_: Lifecycle[P, S, B]).componentWillMount)(n => _.copy(componentWillMount = n))

    type ComponentWillMountFn[P, S, B] = ComponentWillMount[P, S, B] => Callback

    final class ComponentWillMount[P, S, B](val raw: RawMounted[P, S, B])
        extends AnyVal with StateRW[P, S, B] {

      def props        : P             = mountedImpure.props
      def propsChildren: PropsChildren = mountedImpure.propsChildren

      @deprecated("forceUpdate prohibited within the componentWillMount callback.", "")
      def forceUpdate(prohibited: Nothing = ???): Nothing = ???

      // Nope
      // def getDOMNode   : dom.Element   = raw.mounted.getDOMNode
    }

    // ===================================================================================================================

    def componentWillUnmount[P, S, B] = Lens((_: Lifecycle[P, S, B]).componentWillUnmount)(n => _.copy(componentWillUnmount = n))

    type ComponentWillUnmountFn[P, S, B] = ComponentWillUnmount[P, S, B] => Callback

    final class ComponentWillUnmount[P, S, B](val raw: RawMounted[P, S, B])
        extends AnyVal with Base[P, S, B] {

      def props        : P             = mountedImpure.props
      def propsChildren: PropsChildren = mountedImpure.propsChildren
      def state        : S             = mountedImpure.state
      def getDOMNode   : dom.Element   = mountedImpure.getDOMNode

      @deprecated("setState prohibited within the componentWillUnmount callback.", "")
      def setState(prohibited: Nothing, cb: Callback = ???): Nothing = ???

      @deprecated("modState prohibited within the componentWillUnmount callback.", "")
      def modState(prohibited: Nothing, cb: Callback = ???): Nothing = ???

      @deprecated("forceUpdate prohibited within the componentWillUnmount callback.", "")
      def forceUpdate(prohibited: Nothing = ???): Nothing = ???
    }

    // ===================================================================================================================

    def componentWillReceiveProps[P, S, B] = Lens((_: Lifecycle[P, S, B]).componentWillReceiveProps)(n => _.copy(componentWillReceiveProps = n))

    type ComponentWillReceivePropsFn[P, S, B] = ComponentWillReceiveProps[P, S, B] => Callback

    final class ComponentWillReceiveProps[P, S, B](val raw: RawMounted[P, S, B], val nextProps: P)
        extends StateRW[P, S, B] with ForceUpdate[P, S, B] {

      def propsChildren: PropsChildren = mountedImpure.propsChildren
      def currentProps : P             = mountedImpure.props
      def getDOMNode   : dom.Element   = mountedImpure.getDOMNode
    }

    // ===================================================================================================================

    def componentWillUpdate[P, S, B] = Lens((_: Lifecycle[P, S, B]).componentWillUpdate)(n => _.copy(componentWillUpdate = n))

    type ComponentWillUpdateFn[P, S, B] = ComponentWillUpdate[P, S, B] => Callback

    final class ComponentWillUpdate[P, S, B](val raw: RawMounted[P, S, B], val nextProps: P, val nextState: S)
        extends Base[P, S, B] {

      def propsChildren: PropsChildren = mountedImpure.propsChildren
      def currentProps : P             = mountedImpure.props
      def currentState : S             = mountedImpure.state
      def getDOMNode   : dom.Element   = mountedImpure.getDOMNode

      @deprecated("setState prohibited within the componentWillUpdate callback. Use componentWillReceiveProps instead.", "")
      def setState(prohibited: Nothing, cb: Callback = ???): Nothing = ???

      @deprecated("modState prohibited within the componentWillUpdate callback. Use componentWillReceiveProps instead.", "")
      def modState(prohibited: Nothing, cb: Callback = ???): Nothing = ???

      @deprecated("forceUpdate prohibited within the componentWillUpdate callback. Use componentWillReceiveProps instead.", "")
      def forceUpdate(prohibited: Nothing = ???): Nothing = ???
    }

    // ===================================================================================================================

    def shouldComponentUpdate[P, S, B] = Lens((_: Lifecycle[P, S, B]).shouldComponentUpdate)(n => _.copy(shouldComponentUpdate = n))

    type ShouldComponentUpdateFn[P, S, B] = ShouldComponentUpdate[P, S, B] => CallbackTo[Boolean]

    final class ShouldComponentUpdate[P, S, B](val raw: RawMounted[P, S, B], val nextProps: P, val nextState: S)
        extends Base[P, S, B] {

      def propsChildren: PropsChildren = mountedImpure.propsChildren
      def currentProps : P             = mountedImpure.props
      def currentState : S             = mountedImpure.state
      def getDOMNode   : dom.Element   = mountedImpure.getDOMNode

      def cmpProps(cmp: (P, P) => Boolean): Boolean = cmp(currentProps, nextProps)
      def cmpState(cmp: (S, S) => Boolean): Boolean = cmp(currentState, nextState)

      @deprecated("setState prohibited within the shouldComponentUpdate callback.", "")
      def setState(prohibited: Nothing, cb: Callback = ???): Nothing = ???

      @deprecated("modState prohibited within the shouldComponentUpdate callback.", "")
      def modState(prohibited: Nothing, cb: Callback = ???): Nothing = ???

      @deprecated("forceUpdate prohibited within the shouldComponentUpdate callback.", "")
      def forceUpdate(prohibited: Nothing = ???): Nothing = ???
    }

    // ===================================================================================================================

    final class RenderScope[P, S, B](val raw: RawMounted[P, S, B])
        extends StateRW[P, S, B] with ForceUpdate[P, S, B] {

      def isMounted    : Boolean       = mountedImpure.isMounted
      def props        : P             = mountedImpure.props
      def propsChildren: PropsChildren = mountedImpure.propsChildren
      def getDOMNode   : dom.Element   = mountedImpure.getDOMNode
    }

  }
}
