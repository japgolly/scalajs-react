package japgolly.scalajs.react

import org.scalajs.dom
import scalajs.js
import japgolly.scalajs.react.internal._

object ScalaComponentB {

  def build[P](name: String) = new {
    def initialState[S](s: S) = PS[P, S](name, Box(s))
    def stateless = PS[P, Unit](name, Box.Unit)
  }

  type NewBackendFn[P, S, B] = BackendScope[P, S] => B
  type RenderFn[P, S, B] = MountedC[P, S, B] => raw.ReactElement

  case class PS[P, S](name: String, s: Box[S]) {
    def backend[B](f: NewBackendFn[P, S, B]) = PSB[P, S, B](name, s, f)
    def noBackend = backend[Unit](_ => ())
  }

  case class PSB[P, S, Backend](name: String, s: Box[S], backendFn: NewBackendFn[P, S, Backend]) {

    def render[C <: ChildrenArg](r: RenderFn[P, S, Backend]): Builder[P, C, S, Backend] =
      new Builder[P, C, S, Backend](name, s, backendFn, r)

    def render_P(r: P => raw.ReactElement): Builder[P, ChildrenArg.None, S, Backend] =
      render[ChildrenArg.None]($ => r($.props.runNow()))

    def render_S(r: S => raw.ReactElement): Builder[P, ChildrenArg.None, S, Backend] =
      render[ChildrenArg.None]($ => r($.state.runNow()))
  }

  case class Builder[P, C <: ChildrenArg, S, Backend](name: String,
                                                      s: Box[S],
                                                      backendFn: NewBackendFn[P, S, Backend],
                                                      render: RenderFn[P, S, Backend]) {

    def build(implicit ctorType: CtorType.Summoner[Box[P], C]): ScalaComponent[P, S, Backend, ctorType.CT] = {

      val spec = js.Dictionary.empty[js.Any]

      for (n <- Option(name))
        spec("displayName") = n

      def withMounted[A](f: MountedC[P, S, Backend] => A): js.ThisFunction0[raw.ReactComponent, A] =
        (rc: raw.ReactComponent) =>
          f(rc.asInstanceOf[Hax[P, S, Backend]].mountedC)

      spec("render") = withMounted(render)

      // val initStateFn: DuringCallbackU[P, S, B] => WrapObj[S] =
      //   $ => WrapObj(isf($).runNow())
      // spec("getInitialState") = initStateFn: ThisFunction
      def getInitialStateFn: js.Function0[Box[S]] = () => s
      spec.update("getInitialState", getInitialStateFn) // TODO I bet this has a perf impact.

      val componentWillMountFn: js.ThisFunction0[raw.ReactComponent, Unit] =
        (rc: raw.ReactComponent) => {
          val jsMounted = JsComponent.BasicMounted[Box[P], Box[S]](rc).addRawType[Hax[P, S, Backend]]
          val sMountedI: MountedI[P, S, Backend] = new ScalaComponent.Mounted(jsMounted)
          val sMountedC: MountedC[P, S, Backend] = new ScalaComponent.Mounted(jsMounted)
          val backend: Backend = backendFn(sMountedC)
          jsMounted.rawInstance.mountedI = sMountedI
          jsMounted.rawInstance.mountedC = sMountedC
          jsMounted.rawInstance.backend = backend
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
      val rc = raw.React.createClass(spec2)

      val jsCtor: JsComp[P, S, Backend, ctorType.CT] =
        JsComponent[Box[P], C, Box[S]](rc)(ctorType).addRawType[Hax[P, S, Backend]](ctorType.pf)

      new ScalaComponent(jsCtor)(ctorType.pf)
    }
  }

  @js.native
  trait Hax[P, S, B] extends js.Object {
    var mountedI: MountedI[P, S, B]
    var mountedC: MountedC[P, S, B]
    var backend: B
  }

  type Unmounted[P, S, B] = Component.Unmounted[P, MountedI[P, S, B]]
  type MountedI[P, S, B] = ScalaComponent.Mounted[Effect.Id, P, S, B]
  type MountedC[P, S, B] = ScalaComponent.Mounted[CallbackTo, P, S, B]
  type BackendScope[P, S] = Component.Mounted[CallbackTo, P, S]

  type JsComp[P, S, B, CT[_, _] <: CtorType[_, _]] =
    JsComponent[Box[P], Box[S], CT, JsComponent.MountedWithRawType[Box[P], Box[S], Hax[P, S, B]]]

  final class ScalaComponent[P, S, B, CT[_, _] <: CtorType[_, _]](val jsInstance: JsComp[P, S, B, CT])
                                                                 (implicit pf: Profunctor[CT])
    extends Component[P, CT, Unmounted[P, S, B]] {

    override val ctor: CT[P, Unmounted[P, S, B]] =
      //jsInstance.ctor.dimap(Box(_), _.mapProps(_.a).mapMounted(_.mapProps(_.a).xmapState(_.a)(Box(_))))
      jsInstance.ctor.dimap(Box(_), _.mapProps(_.a).mapMounted(_.rawInstance.mountedI))
  }

  object ScalaComponent {

    final class Mounted[F[_], P, S, B](val jsInstance: JsComponent.MountedWithRawType[Box[P], Box[S], Hax[P, S, B]])
                                      (implicit override protected val F: Effect[F])
        extends Component.Mounted[F, P, S] {

      def backend: F[B] =
        F point jsInstance.rawInstance.backend

      override def isMounted: F[Boolean] =
        F point jsInstance.isMounted

      override def props: F[P] =
        F point jsInstance.props.a

      override def propsChildren: F[PropsChildren] =
        F point jsInstance.propsChildren

      override def state: F[S] =
        F point jsInstance.state.a

      override def setState(newState: S, callback: Callback = Callback.empty): F[Unit] =
        F point jsInstance.setState(Box(newState), callback)

      override def modState(mod: S => S, callback: Callback = Callback.empty): F[Unit] =
        F point jsInstance.modState(s => Box(mod(s.a)), callback)

      override def getDOMNode: F[dom.Element] =
        F point jsInstance.getDOMNode

      override def forceUpdate(callback: Callback = Callback.empty): F[Unit] =
        F point jsInstance.forceUpdate(callback)
    }
  }

}
