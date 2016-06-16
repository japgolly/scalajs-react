package japgolly.scalajs.react

import org.scalajs.dom
import scala.{Either => Or}
import scalajs.js
import japgolly.scalajs.react.internal._
import ScalaComponent._

object ScalaComponentB {

  type InitStateFnU[P, S]    = Component.Unmounted[P, Any]
  type InitStateArg[P, S]    = (InitStateFnU[P, S] => S) Or js.Function0[Box[S]]
  type NewBackendFn[P, S, B] = BackendScope[P, S] => B
  type RenderFn    [P, S, B] = MountedC[P, S, B] => raw.ReactElement


  private val InitStateUnit : Nothing Or js.Function0[Box[Unit]] =
    Right(() => Box.Unit)

  def build[P](name: String) = new PPPP[P](name)

  case class PPPP[P](name: String) {

    // getInitialState is how it's named in React
    def getInitialState  [State](f: InitStateFnU[P, State] => State)             = new PS[P, State](name, Left(f))
    def getInitialStateCB[State](f: InitStateFnU[P, State] => CallbackTo[State]) = getInitialState(f.andThen(_.runNow()))

    // More convenient methods that don't need the full CompScope
    def initialState    [State](s: => State              ) = new PS[P, State](name, Right(() => Box(s)))
    def initialStateCB  [State](s: CallbackTo[State]     ) = initialState(s.runNow())
    def initialState_P  [State](f: P => State            ) = getInitialState[State]($ => f($.props))
    def initialStateCB_P[State](f: P => CallbackTo[State]) = getInitialState[State]($ => f($.props).runNow())

    def stateless = PS[P, Unit](name, InitStateUnit)
  }

  case class PS[P, S](name: String, initStateFn: InitStateArg[P, S]) {
    def backend[B](f: NewBackendFn[P, S, B]) = PSB[P, S, B](name, initStateFn, f)
    def noBackend = backend[Unit](_ => ())
  }

  case class PSB[P, S, B](name: String, initStateFn: InitStateArg[P, S], backendFn: NewBackendFn[P, S, B]) {

    def render[C <: ChildrenArg](r: RenderFn[P, S, B]): Builder[P, C, S, B] =
      new Builder[P, C, S, B](name, initStateFn, backendFn, r)

    def render_P(r: P => raw.ReactElement): Builder[P, ChildrenArg.None, S, B] =
      render[ChildrenArg.None]($ => r($.props.runNow()))

    def render_S(r: S => raw.ReactElement): Builder[P, ChildrenArg.None, S, B] =
      render[ChildrenArg.None]($ => r($.state.runNow()))
  }

  case class Builder[P, C <: ChildrenArg, S, B](name       : String,
                                                initStateFn: InitStateArg[P, S],
                                                backendFn  : NewBackendFn[P, S, B],
                                                renderFn   : RenderFn[P, S, B]) {

    def spec: raw.ReactComponentSpec = {
      val spec = js.Dictionary.empty[js.Any]

      for (n <- Option(name))
        spec("displayName") = n

      def withMounted[A](f: MountedC[P, S, B] => A): js.ThisFunction0[raw.ReactComponent, A] =
        (rc: raw.ReactComponent) =>
          f(rc.asInstanceOf[Vars[P, S, B]].mountedC)

      spec("render") = withMounted(renderFn)

      def getInitialStateFn: js.Function =
        initStateFn match {
          case Right(fn0) => fn0
          case Left(fn) => ((rc: raw.ReactComponentElement) => {
            val js = JsComponent.BasicUnmounted[Box[P], Box[S]](rc)
            Box(fn(js.mapProps(_.a)))
          }): js.ThisFunction0[raw.ReactComponentElement, Box[S]]
        }
      spec.update("getInitialState", getInitialStateFn)

      val componentWillMountFn: js.ThisFunction0[raw.ReactComponent, Unit] =
        (rc: raw.ReactComponent) => {
          val jMounted : JsMounted[P, S, B] = JsComponent.BasicMounted[Box[P], Box[S]](rc).addRawType[Vars[P, S, B]]
          val sMountedI: Mounted  [P, S, B] = new ScalaComponent.MountedF(jMounted)
          val sMountedC: MountedC [P, S, B] = new ScalaComponent.MountedF(jMounted)
          val backend  : B                  = backendFn(sMountedC)
          jMounted.raw.mounted  = sMountedI
          jMounted.raw.mountedC = sMountedC
          jMounted.raw.backend  = backend
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
      spec.asInstanceOf[raw.ReactComponentSpec]
    }

    def build(implicit ctorType: CtorType.Summoner[Box[P], C]): ScalaComponent[P, S, B, ctorType.CT] = {
      val rc = raw.React.createClass(spec)
      val jc = JsComponent[Box[P], C, Box[S]](rc)(ctorType).addRawType[Vars[P, S, B]](ctorType.pf)
      new ScalaComponent(jc)(ctorType.pf)
    }
  }
}
