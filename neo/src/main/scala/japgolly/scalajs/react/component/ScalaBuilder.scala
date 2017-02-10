package japgolly.scalajs.react.component

import scala.{Either => Or}
import scalajs.js
import japgolly.scalajs.react.{CallbackTo, ChildrenArg, CtorType, PropsChildren, raw, vdom}
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.macros.CompBuilderMacros
import Lifecycle._
import Scala._

object ScalaBuilder {

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
    def renderBackend[B]: Step4[P, ChildrenArg.None, S, B] =
      macro CompBuilderMacros.backendAndRender[P, S, B]

    /**
     * Shortcut for:
     *
     * {{{
     *   .backend[B](new B(_))
     *   .renderBackendWithChildren
     * }}}
     */
    def renderBackendWithChildren[B]: Step4[P, ChildrenArg.Varargs, S, B] =
      macro CompBuilderMacros.backendAndRenderWithChildren[P, S, B]
  }

  // ===================================================================================================================

  final class Step3[P, S, B](name: String, initStateFn: InitStateArg[P, S], backendFn: NewBackendFn[P, S, B]) {
    // Dealiases type aliases :(
    // type Next[C <: ChildrenArg] = Step4[P, C, S, B]

    type $ = RenderScope[P, S, B]

    def render[C <: ChildrenArg](r: RenderFn[P, S, B]): Step4[P, C, S, B] =
      new Step4[P, C, S, B](name, initStateFn, backendFn, r, Lifecycle.empty)

    // No children

     def renderPS(r: ($, P, S) => vdom.ReactElement): Step4[P, ChildrenArg.None, S, B] =
       render($ => r($, $.props, $.state))

     def renderP(r: ($, P) => vdom.ReactElement): Step4[P, ChildrenArg.None, S, B] =
       render($ => r($, $.props))

     def renderS(r: ($, S) => vdom.ReactElement): Step4[P, ChildrenArg.None, S, B] =
       render($ => r($, $.state))

     def render_PS(r: (P, S) => vdom.ReactElement): Step4[P, ChildrenArg.None, S, B] =
       render($ => r($.props, $.state))

     def render_P(r: P => vdom.ReactElement): Step4[P, ChildrenArg.None, S, B] =
       render($ => r($.props))

     def render_S(r: S => vdom.ReactElement): Step4[P, ChildrenArg.None, S, B] =
       render($ => r($.state))

    // Has children

     def renderPCS(r: ($, P, PropsChildren, S) => vdom.ReactElement): Step4[P, ChildrenArg.Varargs, S, B] =
       render($ => r($, $.props, $.propsChildren, $.state))

     def renderPC(r: ($, P, PropsChildren) => vdom.ReactElement): Step4[P, ChildrenArg.Varargs, S, B] =
       render($ => r($, $.props, $.propsChildren))

     def renderCS(r: ($, PropsChildren, S) => vdom.ReactElement): Step4[P, ChildrenArg.Varargs, S, B] =
       render($ => r($, $.propsChildren, $.state))

     def renderC(r: ($, PropsChildren) => vdom.ReactElement): Step4[P, ChildrenArg.Varargs, S, B] =
       render($ => r($, $.propsChildren))

     def render_PCS(r: (P, PropsChildren, S) => vdom.ReactElement): Step4[P, ChildrenArg.Varargs, S, B] =
       render($ => r($.props, $.propsChildren, $.state))

     def render_PC(r: (P, PropsChildren) => vdom.ReactElement): Step4[P, ChildrenArg.Varargs, S, B] =
       render($ => r($.props, $.propsChildren))

     def render_CS(r: (PropsChildren, S) => vdom.ReactElement): Step4[P, ChildrenArg.Varargs, S, B] =
       render($ => r($.propsChildren, $.state))

     def render_C(r: PropsChildren => vdom.ReactElement): Step4[P, ChildrenArg.Varargs, S, B] =
       render($ => r($.propsChildren))

    /**
     * Use a method named `render` in the backend, automatically populating its arguments with props and state
     * where needed.
     */
    def renderBackend: Step4[P, ChildrenArg.None, S, B] =
      macro CompBuilderMacros.renderBackend[P, S, B]

    /**
     * Use a method named `render` in the backend, automatically populating its arguments with props, state, and
     * propsChildren where needed.
     */
    def renderBackendWithChildren: Step4[P, ChildrenArg.Varargs, S, B] =
      macro CompBuilderMacros.renderBackendWithChildren[P, S, B]
  }

  // ===================================================================================================================

  final class Step4[P, C <: ChildrenArg, S, B](name       : String,
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
      lcAppend(Lifecycle.shouldComponentUpdate)(f)(Semigroup.either)

    def spec: raw.ReactComponentSpec = {
      val spec = js.Object().asInstanceOf[raw.ReactComponentSpec]

      @inline def castV($: raw.ReactComponent) = $.asInstanceOf[Vars[P, S, B]]
      @inline def castP($: raw.Props) = $.asInstanceOf[Box[P]]
      @inline def castS($: raw.State) = $.asInstanceOf[Box[S]]

      for (n <- Option(name))
        spec.displayName = n

      def withMounted[A](f: RenderScope[P, S, B] => A): js.ThisFunction0[raw.ReactComponent, A] =
        ($: raw.ReactComponent) =>
          f(RenderScope(castV($).mounted.js))

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
          val jMounted : JsMounted[P, S, B] = Js.mounted[Box[P], Box[S]]($).addFacade[Vars[P, S, B]]
          val sMountedI: Mounted  [P, S, B] = Scala.rootMounted(jMounted)
          val sMountedC: MountedCB[P, S, B] = sMountedI.withEffect
          val backend  : B                  = backendFn(sMountedC)
          jMounted.raw.mounted   = sMountedI
          jMounted.raw.mountedCB = sMountedC
          jMounted.raw.backend   = backend
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
          vars.mounted   = null
          vars.mountedCB = null
          vars.backend   = null.asInstanceOf[B]
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
          f(new ComponentWillUpdate(castV($).mounted, castP(p).unbox, castS(s).unbox)).runNow())

      lifecycle.shouldComponentUpdate.foreach(f =>
        spec.shouldComponentUpdate = ($: raw.ReactComponent, p: raw.Props, s: raw.State) =>
          f(new ShouldComponentUpdate(castV($).mounted, castP(p).unbox, castS(s).unbox)))

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
}
