package japgolly.scalajs.react

import org.scalajs.dom
import scalajs.js

object CompScala {

  def build[P](name: String) = new {
    def initialState[S](s: S) = PS[P, S](name, Box(s))
    def stateless = PS[P, Unit](name, Box.Unit)
  }

  type NewBackendFn[P, S, B] = BackendScope[P, S] => B

  case class PS[P, S](name: String, s: Box[S]) {
    def backend[B](f: NewBackendFn[P, S, B]) = PSB[P, S, B](name, s, f)
    def noBackend = backend[Unit](_ => ())
  }

  case class PSB[P, S, Backend](name: String, s: Box[S], backendFn: NewBackendFn[P, S, Backend]) {

    def render[C <: ChildrenArg](r: Mounted[CallbackTo, P, S, Backend] => raw.ReactElement): Builder[P, C, S, Backend] =
      new Builder[P, C, S, Backend](name, s, backendFn, r)

    def render_P(r: P => raw.ReactElement): Builder[P, ChildrenArg.None, S, Backend] =
      render[ChildrenArg.None]($ => r($.props.runNow()))

    def render_S(r: S => raw.ReactElement): Builder[P, ChildrenArg.None, S, Backend] =
      render[ChildrenArg.None]($ => r($.state.runNow()))
  }

  val fieldMounted = "m"

  def mountedFromJs[P, S, Backend](rc: raw.ReactComponent): Mounted[CallbackTo, P, S, Backend] =
    rc.asInstanceOf[js.Dynamic].selectDynamic(fieldMounted).asInstanceOf[Mounted[CallbackTo, P, S, Backend]]

  case class Builder[P, C <: ChildrenArg, S, Backend](name: String,
                                                      s: Box[S],
                                                      backendFn: NewBackendFn[P, S, Backend],
                                                      render: Mounted[CallbackTo, P, S, Backend] => raw.ReactElement) {

    def build(implicit directCtor: DirectCtor.Init[Box[P], C]): Ctor[P, C, S, Backend] = {

      val spec = js.Dictionary.empty[js.Any]

      for (n <- Option(name))
        spec("displayName") = n

      def withMounted[A](f: Mounted[CallbackTo, P, S, Backend] => A): js.ThisFunction0[raw.ReactComponent, A] =
        (rc: raw.ReactComponent) =>
          f(mountedFromJs(rc))

      spec("render") = withMounted(render)

      // val initStateFn: DuringCallbackU[P, S, B] => WrapObj[S] =
      //   $ => WrapObj(isf($).runNow())
      // spec("getInitialState") = initStateFn: ThisFunction
      def getInitialStateFn: js.Function0[Box[S]] = () => s
      spec.update("getInitialState", getInitialStateFn) // TODO I bet this has a perf impact.

      val componentWillMountFn: js.ThisFunction0[raw.ReactComponent, Unit] =
        (rc: raw.ReactComponent) => {
          val mjs = CompJs3.Mounted[Box[P], Box[S]](rc)
          val m = new Mounted0[CallbackTo, P, S, Backend](mjs)
          m._backend = backendFn(m.asInstanceOf[BackendScope[P, S]])
          rc.asInstanceOf[js.Dynamic].updateDynamic(fieldMounted)(m.asInstanceOf[js.Any])
        }
      spec("componentWillMount") = componentWillMountFn

//        def onWillMountFn(f: DuringCallbackU[P, S, B] => Unit): Unit =
//          componentWillMountFn = Some(componentWillMountFn.fold(f)(g => $ => {g($); f($)}))
//        for (f <- lc.componentWillMount)
//          onWillMountFn(f(_).runNow())

//        for (f <- componentWillMountFn)
//          spec("componentWillMount") = f: ThisFunction
//
//
//        lc.getDefaultProps.flatMap(_.toJsCallback).foreach(spec("getDefaultProps") = _)
//
//        setThisFn1(                                             lc.componentWillUnmount     , "componentWillUnmount")
//        setThisFn1(                                             lc.componentDidMount        , "componentDidMount")
//        setFnPS   (ComponentWillUpdate      .apply[P, S, B, N])(lc.componentWillUpdate      , "componentWillUpdate")
//        setFnPS   (ComponentDidUpdate       .apply[P, S, B, N])(lc.componentDidUpdate       , "componentDidUpdate")
//        setFnPS   (ShouldComponentUpdate    .apply[P, S, B, N])(lc.shouldComponentUpdate    , "shouldComponentUpdate")
//        setFnP    (ComponentWillReceiveProps.apply[P, S, B, N])(lc.componentWillReceiveProps, "componentWillReceiveProps")
//
//        if (jsMixins.nonEmpty)
//          spec("mixins") = JArray(jsMixins: _*)
//
//        val spec2 = spec.asInstanceOf[ReactComponentSpec[P, S, B, N]]
//        lc.configureSpec.foreach(_(spec2).runNow())
//        spec2

      val spec2 = spec.asInstanceOf[raw.ReactComponentSpec]
      val cls = raw.React.createClass(spec2)
      val jsCtor = CompJs3.Constructor[Box[P], C, Box[S]](cls)(directCtor)
      Ctor(jsCtor)
    }
  }

  case class Ctor[P, C <: ChildrenArg, S, B](jsInstance: CompJs3.Constructor[Box[P], C, Box[S]])
      extends BaseCtor[P, C, Unmounted[P, S, B]] {

    override val applyDirect: (P, ChildrenArgSeq) => Unmounted[P, S, B] =
      jsInstance.directCtorU
        .lmap[P](Box(_))
        .rmap(new Unmounted[P, S, B](_))
        .apply(jsInstance.rawCls)
  }

  class Unmounted[P, S, B](jsInstance: CompJs3.Unmounted[Box[P], Box[S]]) {
    def key: Option[Key] =
      jsInstance.key

    def ref: Option[String] =
      jsInstance.ref

    def props: P =
      jsInstance.props.a

    def propsChildren: PropsChildren =
      jsInstance.propsChildren

    def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): Mounted[CallbackTo, P, S, B] = {
      val rc = raw.ReactDOM.render(jsInstance.rawElement, container, callback.toJsFn)
      mountedFromJs(rc)
    }
  }

  type BackendScope[P, S] = Mounted[CallbackTo, P, S, Null]

  private[CompScala] final class Mounted0[F[_], P, S, Backend](jsInstance: CompJs3.Mounted[Box[P], Box[S]])(implicit F: Effect[F])
      extends Mounted[F, P, S, Backend](jsInstance)(F) {
    var _backend: Backend = _
    override def backend: Backend = _backend
  }

  class MountedD[F[_], P, S, +Backend](val backend: Backend,
                                       jsInstance: CompJs3.Mounted[Box[P], Box[S]])(implicit F: Effect[F])
      extends Mounted[F, P, S, Backend](jsInstance)(F)

  abstract class Mounted[F[_], P, S, +Backend](jsInstance: CompJs3.Mounted[Box[P], Box[S]])
                                              (override protected final val F: Effect[F]) extends MountedBase[F, P, S] {

//    def direct: Mounted[Effect.Id, P, S, Backend] =
//      new MountedD[Effect.Id, P, S, Backend](backend, jsInstance)

    def backend: Backend

    final def isMounted: F[Boolean] =
      F point jsInstance.isMounted

    final def props: F[P] =
      F point jsInstance.props.a

    final def propsChildren: F[PropsChildren] =
      F point jsInstance.propsChildren

    final def state: F[S] =
      F point jsInstance.state.a

    final def setState(newState: S, callback: Callback = Callback.empty): F[Unit] =
      F point jsInstance.setState(Box(newState), callback)

    final def modState(mod: S => S, callback: Callback = Callback.empty): F[Unit] =
      F point jsInstance.modState(s => Box(mod(s.a)), callback)

    final def getDOMNode: F[dom.Element] =
      F point jsInstance.getDOMNode

    override final def forceUpdate(callback: Callback = Callback.empty): F[Unit] =
      F point jsInstance.forceUpdate(callback)
  }

}
