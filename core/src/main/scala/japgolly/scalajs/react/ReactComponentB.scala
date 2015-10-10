package japgolly.scalajs.react

import scala.scalajs.js.{Any => JAny, Array => JArray, _}
import Internal._
import CompScope._
import macros.CompBuilderMacros

abstract class LifecycleInput[P, S, +$ <: HasProps[P] with HasState[S]] {
  val $: $
  @inline final def component: $ = $
  @inline final def currentProps: P = $._props.v
  @inline final def currentState: S = $._state.v
}
case class ComponentWillUpdate      [P, S, +B, +N <: TopNode]($: WillUpdate[P, S, B, N],      nextProps: P, nextState: S) extends LifecycleInput[P, S, WillUpdate[P, S, B, N]]
case class ComponentDidUpdate       [P, S, +B, +N <: TopNode]($: DuringCallbackM[P, S, B, N], prevProps: P, prevState: S) extends LifecycleInput[P, S, DuringCallbackM[P, S, B, N]]
case class ShouldComponentUpdate    [P, S, +B, +N <: TopNode]($: DuringCallbackM[P, S, B, N], nextProps: P, nextState: S) extends LifecycleInput[P, S, DuringCallbackM[P, S, B, N]]
case class ComponentWillReceiveProps[P, S, +B, +N <: TopNode]($: DuringCallbackM[P, S, B, N], nextProps: P) extends LifecycleInput[P, S, DuringCallbackM[P, S, B, N]]

object ReactComponentB {

  @inline def apply[Props](name: String) = new P[Props](name)

  // ======
  // Stages
  // ======
  //
  // 1 = P
  // 2 = PS
  // 3 = PSB
  //     .render() mandatory
  // 4 = PSBR
  // 5 = ReactComponentB
  // 6 = ReactComponentB#Builder

  implicit def defaultStateless    [p](x: P[p])                                           = x.stateless
  implicit def defaultNoBackend    [p,s,X <% PS[p, s]](x: X)                              = x.noBackend
  implicit def defaultTopNode      [p,s,b](x: PSBR[p, s, b])                              = x.domType[TopNode]
  implicit def defaultPropsRequired[P,S,B,N<:TopNode,X <% ReactComponentB[P,S,B,N]](x: X) = x.propsRequired

  type InitStateFn[P, S] = DuringCallbackU[P, S, Any] => CallbackTo[S]
  type RenderFn[P, S, -B] = DuringCallbackU[P, S, B] => ReactElement

  // ===================================================================================================================
  // Convenience

  /**
   * Create a component that always displays the same content, never needs to be redrawn, never needs vdom diffing.
   */
  def static(name: String, content: ReactElement) =
    staticN[TopNode](name, content)

  def staticN[N <: TopNode](name: String, content: ReactElement) =
    ReactComponentB[Unit](name)
      .render(_ => content)
      .domType[N]
      .shouldComponentUpdateCB(_ => alwaysFalse)

  private val alwaysFalse = CallbackTo.pure(false)

  // ===================================================================================================================
  final class P[Props] private[ReactComponentB](name: String) {

    // getInitialState is how it's named in React
    def getInitialState  [State](f: DuringCallbackU[Props, State, Any] => State)             = getInitialStateCB[State]($ => CallbackTo(f($)))
    def getInitialStateCB[State](f: DuringCallbackU[Props, State, Any] => CallbackTo[State]) = new PS[Props, State](name, f)

    // More convenient methods that don't need the full CompScope
    def initialState    [State](s: => State                  ) = initialStateCB(CallbackTo(s))
    def initialStateCB  [State](s: CallbackTo[State]         ) = getInitialStateCB[State](_ => s)
    def initialState_P  [State](f: Props => State            ) = getInitialStateCB[State]($ => CallbackTo(f($.props)))
    def initialStateCB_P[State](f: Props => CallbackTo[State]) = getInitialStateCB[State]($ => f($.props))

    def stateless = initialStateCB(Callback.empty)
  }

  // ===================================================================================================================
  final class PS[P, S] private[ReactComponentB](name: String, initF: InitStateFn[P, S]) {
    def noBackend: PSB[P, S, Unit] =
      backend(_ => ())

    def backend[B](f: BackendScope[P, S] => B): PSB[P, S, B] =
      new PSB(name, initF, f)

    /**
     * Shortcut for:
     *
     * {{{
     *   .backend[B](new B(_))
     *   .renderBackend
     * }}}
     */
    def renderBackend[B]: PSBR[P, S, B] =
      macro CompBuilderMacros.backendAndRender[P, S, B]
  }

  // ===================================================================================================================
  final class PSB[P, S, B] private[ReactComponentB](name: String, initF: InitStateFn[P, S], backF: BackendScope[P, S] => B) {

    def render(f: DuringCallbackU[P, S, B] => ReactElement): PSBR[P, S, B] =
      new PSBR(name, initF, backF, f)

    def renderPCS(f: (DuringCallbackU[P, S, B], P, PropsChildren, S) => ReactElement): PSBR[P, S, B] =
      render($ => f($, $.props, $.propsChildren, $.state))

    def renderPC(f: (DuringCallbackU[P, S, B], P, PropsChildren) => ReactElement): PSBR[P, S, B] =
      render($ => f($, $.props, $.propsChildren))

    def renderPS(f: (DuringCallbackU[P, S, B], P, S) => ReactElement): PSBR[P, S, B] =
      render($ => f($, $.props, $.state))

    def renderP(f: (DuringCallbackU[P, S, B], P) => ReactElement): PSBR[P, S, B] =
      render($ => f($, $.props))

    def renderCS(f: (DuringCallbackU[P, S, B], PropsChildren, S) => ReactElement): PSBR[P, S, B] =
      render($ => f($, $.propsChildren, $.state))

    def renderC(f: (DuringCallbackU[P, S, B], PropsChildren) => ReactElement): PSBR[P, S, B] =
      render($ => f($, $.propsChildren))

    def renderS(f: (DuringCallbackU[P, S, B], S) => ReactElement): PSBR[P, S, B] =
      render($ => f($, $.state))

    def render_P(f: P => ReactElement): PSBR[P, S, B] =
      render($ => f($.props))

    def render_C(f: PropsChildren => ReactElement): PSBR[P, S, B] =
      render($ => f($.propsChildren))

    def render_S(f: S => ReactElement): PSBR[P, S, B] =
      render($ => f($.state))

    /**
     * Use a method named `render` in the backend, automatically populating its arguments with props, state,
     * propsChildren where needed.
     */
    def renderBackend: PSBR[P, S, B] =
      macro CompBuilderMacros.renderBackend[P, S, B]
  }

  // ===================================================================================================================
  final class PSBR[P, S, B] private[ReactComponentB](name: String, initF: InitStateFn[P, S], backF: BackendScope[P, S] => B, rendF: RenderFn[P, S, B]) {
    def domType[N <: TopNode]: ReactComponentB[P, S, B, N] =
      new ReactComponentB(name, initF, backF, rendF, emptyLifeCycle, Vector.empty)
  }

  // ===================================================================================================================
  private[react] case class LifeCycle[P,S,B,N <: TopNode](
    configureSpec            : UndefOr[ReactComponentSpec       [P, S, B, N] => Callback],
    getDefaultProps          : UndefOr[                                         CallbackTo[P]],
    componentWillMount       : UndefOr[DuringCallbackU          [P, S, B]    => Callback],
    componentDidMount        : UndefOr[DuringCallbackM          [P, S, B, N] => Callback],
    componentWillUnmount     : UndefOr[DuringCallbackM          [P, S, B, N] => Callback],
    componentWillUpdate      : UndefOr[ComponentWillUpdate      [P, S, B, N] => Callback],
    componentDidUpdate       : UndefOr[ComponentDidUpdate       [P, S, B, N] => Callback],
    componentWillReceiveProps: UndefOr[ComponentWillReceiveProps[P, S, B, N] => Callback],
    shouldComponentUpdate    : UndefOr[ShouldComponentUpdate    [P, S, B, N] => CallbackB])

  private[react] def emptyLifeCycle[P,S,B,N <: TopNode] =
    LifeCycle[P,S,B,N](undefined, undefined, undefined, undefined, undefined, undefined, undefined, undefined, undefined)
}

import ReactComponentB.{InitStateFn, RenderFn, LifeCycle}

final class ReactComponentB[P,S,B,N <: TopNode](val name: String,
                                                initF   : InitStateFn[P, S],
                                                backF   : BackendScope[P, S] => B,
                                                rendF   : RenderFn[P, S, B],
                                                lc      : LifeCycle[P, S, B, N],
                                                jsMixins: Vector[JAny]) {

  @inline private def copy(name    : String                  = name,
                           initF   : InitStateFn[P, S]       = initF,
                           backF   : BackendScope[P, S] => B = backF,
                           rendF   : RenderFn[P, S, B]       = rendF,
                           lc      : LifeCycle[P, S, B, N]   = lc,
                           jsMixins: Vector[JAny]            = jsMixins): ReactComponentB[P, S, B, N] =
    new ReactComponentB(name, initF, backF, rendF, lc, jsMixins)

  @inline private implicit def lcmod(a: LifeCycle[P, S, B, N]): ReactComponentB[P, S, B, N] =
    copy(lc = a)

  def configureSpec(modify: ReactComponentSpec[P, S, B, N] => Callback): ReactComponentB[P, S, B, N] =
       lc.copy(configureSpec = modify)

  def configure(fs: (ReactComponentB[P, S, B, N] => ReactComponentB[P, S, B, N])*): ReactComponentB[P, S, B, N] =
    fs.foldLeft(this)((a,f) => f(a))


  def getDefaultProps(p: => P): ReactComponentB[P, S, B, N] =
    getDefaultPropsCB(CallbackTo(p))

  def getDefaultPropsCB(p: CallbackTo[P]): ReactComponentB[P, S, B, N] =
    lc.copy(getDefaultProps = p)


  /**
   * Invoked once, both on the client and server, immediately before the initial rendering occurs.
   * If you call `setState` within this method, `render()` will see the updated state and will be executed only once
   * despite the state change.
   */
  def componentWillMount(f: DuringCallbackU[P, S, B] => Callback): ReactComponentB[P, S, B, N] =
    lc.copy(componentWillMount = fcUnit(lc.componentWillMount, f))

  /**
   * Invoked once, only on the client (not on the server), immediately after the initial rendering occurs. At this point
   * in the lifecycle, the component has a DOM representation which you can access via `ReactDOM.findDOMNode(this)`.
   * The `componentDidMount()` method of child components is invoked before that of parent components.
   *
   * If you want to integrate with other JavaScript frameworks, set timers using `setTimeout` or `setInterval``, or send
   * AJAX requests, perform those operations in this method.
   */
  def componentDidMount(f: DuringCallbackM[P, S, B, N] => Callback): ReactComponentB[P, S, B, N] =
    lc.copy(componentDidMount = fcUnit(lc.componentDidMount, f))

  /**
   * Invoked immediately before a component is unmounted from the DOM.
   *
   * Perform any necessary cleanup in this method, such as invalidating timers or cleaning up any DOM elements that were
   * created in `componentDidMount`.
   */
  def componentWillUnmount(f: DuringCallbackM[P, S, B, N] => Callback): ReactComponentB[P, S, B, N] =
    lc.copy(componentWillUnmount = fcUnit(lc.componentWillUnmount, f))

  /**
   * Invoked immediately before rendering when new props or state are being received. This method is not called for the
   * initial render.
   *
   * Use this as an opportunity to perform preparation before an update occurs.
   *
   * Note: You *cannot* use `this.setState()` in this method. If you need to update state in response to a prop change,
   * use `componentWillReceiveProps` instead.
   */
  def componentWillUpdate(f: ComponentWillUpdate[P, S, B, N] => Callback): ReactComponentB[P, S, B, N] =
    lc.copy(componentWillUpdate = fcUnit(lc.componentWillUpdate, f))

  /**
   * Invoked immediately after the component's updates are flushed to the DOM. This method is not called for the initial
   * render.
   *
   * Use this as an opportunity to operate on the DOM when the component has been updated.
   */
  def componentDidUpdate(f: ComponentDidUpdate[P, S, B, N] => Callback): ReactComponentB[P, S, B, N] =
    lc.copy(componentDidUpdate = fcUnit(lc.componentDidUpdate, f))

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
  def componentWillReceiveProps(f: ComponentWillReceiveProps[P, S, B, N] => Callback): ReactComponentB[P, S, B, N] =
    lc.copy(componentWillReceiveProps = fcUnit(lc.componentWillReceiveProps, f))

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
  def shouldComponentUpdate(f: ShouldComponentUpdate[P, S, B, N] => Boolean): ReactComponentB[P, S, B, N] =
    shouldComponentUpdateCB(f andThen CallbackTo.pure)

  def componentWillMountCB(cb: Callback): ReactComponentB[P, S, B, N] =
    componentWillMount(_ => cb)

  def componentDidMountCB(cb: Callback): ReactComponentB[P, S, B, N] =
    componentDidMount(_ => cb)

  def componentWillUnmountCB(cb: Callback): ReactComponentB[P, S, B, N] =
    componentWillUnmount(_ => cb)

  def componentWillUpdateCB(cb: Callback): ReactComponentB[P, S, B, N] =
    componentWillUpdate(_ => cb)

  def componentDidUpdateCB(cb: Callback): ReactComponentB[P, S, B, N] =
    componentDidUpdate(_ => cb)

  def componentWillReceivePropsCB(cb: Callback): ReactComponentB[P, S, B, N] =
    componentWillReceiveProps(_ => cb)

  def shouldComponentUpdateCB(f: ShouldComponentUpdate[P, S, B, N] => CallbackB): ReactComponentB[P, S, B, N] =
    lc.copy(shouldComponentUpdate = fcEither(lc.shouldComponentUpdate, f))

  /**
   * Install a pure-JS React mixin.
   *
   * Beware: There will be mixins that won't work correctly as they make assumptions that don't hold for Scala.
   * If a mixin expects to inspect your props or state, forget about it; Scala-land owns that data.
   */
  def mixinJS(mixins: JAny*): ReactComponentB[P, S, B, N] =
    copy(jsMixins = jsMixins ++ mixins)

  /**
   * Modify the render function.
   */
  def reRender(f: RenderFn[P, S, B] => RenderFn[P, S, B]): ReactComponentB[P, S, B, N] =
    copy(rendF = f(rendF))

  // ===================================================================================================================
  import ReactComponentC.{ConstProps, ReqProps, UnitPropProof}

  type BuildFn[Output] = ReqProps[P,S,B,N] => Output

  @inline private def builder[Output](buildFn: BuildFn[Output]) = new Builder(buildFn)

  def propsRequired         = builder(identity)
  def propsDefault(p: => P) = builder(_ withDefaultProps p)
  def propsConst  (p: => P) = builder(_ withProps p)

  def propsUnit(implicit ev: UnitPropProof[P]) = builder(_.noProps)

  def buildU(implicit ev: UnitPropProof[P]): ConstProps[P, S, B, N] =
    propsUnit.build

  final class Builder[Output] private[ReactComponentB](buildFn: BuildFn[Output]) {

    def buildSpec: ReactComponentSpec[P, S, B, N] = {
      val spec = Dynamic.literal(
        "displayName" -> name,
        "backend" -> 0,
        "render" -> (rendF: ThisFunction)
      )

      @inline def setFnPS[$, A, R](a: ($, P, S) => A)(fn: UndefOr[A => CallbackTo[R]], name: String): Unit =
        fn.foreach { f =>
          val g = ($: $, p: WrapObj[P], s: WrapObj[S]) =>
            f(a($, p.v, s.v)).runNow()
          spec.updateDynamic(name)(g: ThisFunction)
        }

      @inline def setFnP[$, A, R](a: ($, P) => A)(fn: UndefOr[A => CallbackTo[R]], name: String): Unit =
        fn.foreach { f =>
          val g = ($: $, p: WrapObj[P], s: WrapObj[S]) =>
            f(a($, p.v)).runNow()
          spec.updateDynamic(name)(g: ThisFunction)
        }

      @inline def setThisFn1[A](fn: UndefOr[A => Callback], name: String): Unit =
        fn.foreach { f =>
          val g = (a: A) => f(a).runNow()
          spec.updateDynamic(name)(g: ThisFunction)
        }

      val componentWillMount2 = (t: DuringCallbackU[P, S, B]) => {
        val scopeB = t.asInstanceOf[BackendScope[P, S]]
        t.asInstanceOf[Dynamic].updateDynamic("backend")(backF(scopeB).asInstanceOf[JAny])
        lc.componentWillMount.foreach(g => g(t).runNow())
      }
      spec.updateDynamic("componentWillMount")(componentWillMount2: ThisFunction)

      val initStateFn: DuringCallbackU[P, S, B] => WrapObj[S] = $ => WrapObj(initF($).runNow())
      spec.updateDynamic("getInitialState")(initStateFn: ThisFunction)

      lc.getDefaultProps.flatMap(_.toJsCallback).foreach(f => spec.updateDynamic("getDefaultProps")(f))
      setThisFn1(lc.componentWillUnmount,  "componentWillUnmount")
      setThisFn1(lc.componentDidMount,     "componentDidMount")

      setFnPS   (ComponentWillUpdate  .apply[P, S, B, N])(lc.componentWillUpdate,   "componentWillUpdate")
      setFnPS   (ComponentDidUpdate   .apply[P, S, B, N])(lc.componentDidUpdate,    "componentDidUpdate")
      setFnPS   (ShouldComponentUpdate.apply[P, S, B, N])(lc.shouldComponentUpdate, "shouldComponentUpdate")
      setFnP    (ComponentWillReceiveProps.apply[P, S, B, N])(lc.componentWillReceiveProps, "componentWillReceiveProps")

      if (jsMixins.nonEmpty) {
        val mixins = JArray(jsMixins: _*)
        spec.updateDynamic("mixins")(mixins)
      }

      val spec2 = spec.asInstanceOf[ReactComponentSpec[P, S, B, N]]
      lc.configureSpec.foreach(_(spec2).runNow())
      spec2
    }

    def build: Output = {
      val c = React.createClass(buildSpec)
      val f = React.createFactory(c)
      val r = new ReqProps(f, c, undefined, undefined)
      buildFn(r)
    }
  }
}

