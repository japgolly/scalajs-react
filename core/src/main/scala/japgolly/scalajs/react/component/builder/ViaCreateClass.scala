package japgolly.scalajs.react.component.builder

import scalajs.js
import japgolly.scalajs.react.{Children, raw}
import japgolly.scalajs.react.component.{Js, Scala}
import japgolly.scalajs.react.internal.Box
import Lifecycle._
import Scala._

/** Uses `React.createClass` to create a component from a builder. */
object ViaCreateClass {

  def apply[P, C <: Children, S, B](builder: Builder.Step4[P, C, S, B]): raw.ReactClass[Box[P], Box[S]] =
    raw.React.createClass(spec(builder))

  type Spec[P, S] = raw.ReactComponentSpec[Box[P], Box[S]]

  def spec[P, C <: Children, S, B](builder: Builder.Step4[P, C, S, B]): Spec[P, S] = {
    type RawComp = raw.ReactComponent[Box[P], Box[S]]
    @inline def castV($: RawComp) = $.asInstanceOf[RawMounted[P, S, B]]

    val spec = js.Object().asInstanceOf[raw.ReactComponentSpec[Box[P], Box[S]]]

    for (n <- Option(builder.name))
      spec.displayName = n

    def withMounted[A](f: RenderScope[P, S, B] => A): js.ThisFunction0[RawComp, A] =
      ($: RawComp) =>
        f(new RenderScope(castV($)))

    spec.render = withMounted(builder.renderFn.andThen(_.rawElement))

    val initStateFn = builder.initStateFn
    spec.getInitialState =
      (($: js.Dynamic) => initStateFn($.props.asInstanceOf[Box[P]])): js.ThisFunction0[js.Dynamic, Box[S]]

    val backendFn = builder.backendFn
    val setup: RawComp => Unit =
      $ => {
        val jMounted : JsMounted    [P, S, B] = Js.mounted[Box[P], Box[S]]($).addFacade[Vars[P, S, B]]
        val sMountedI: MountedImpure[P, S, B] = Scala.mountedRoot(jMounted)
        val sMountedP: MountedPure  [P, S, B] = sMountedI.withEffect
        val backend  : B                      = backendFn(sMountedP)
        jMounted.raw.mountedImpure = sMountedI
        jMounted.raw.mountedPure   = sMountedP
        jMounted.raw.backend       = backend
      }
    spec.componentWillMount = builder.lifecycle.componentWillMount match {
      case None    => setup
      case Some(f) =>
        ($: RawComp) => {
          setup($)
          f(new ComponentWillMount(castV($))).runNow()
        }
    }

    val teardown: RawComp => Unit =
      $ => {
        val vars = castV($)
        vars.mountedImpure = null
        vars.mountedPure   = null
        vars.backend       = null.asInstanceOf[B]
      }
    spec.componentWillUnmount = builder.lifecycle.componentWillUnmount match {
      case None    => teardown
      case Some(f) =>
        ($: RawComp) => {
          f(new ComponentWillUnmount(castV($))).runNow()
          teardown($)
        }
    }

    builder.lifecycle.componentDidMount.foreach(f =>
      spec.componentDidMount = ($: RawComp) =>
        f(new ComponentDidMount(castV($))).runNow())

    builder.lifecycle.componentDidUpdate.foreach(f =>
      spec.componentDidUpdate = ($: RawComp, p: Box[P], s: Box[S]) =>
        f(new ComponentDidUpdate(castV($), p.unbox, s.unbox)).runNow())

    builder.lifecycle.componentWillReceiveProps.foreach(f =>
      spec.componentWillReceiveProps = ($: RawComp, p: Box[P]) =>
        f(new ComponentWillReceiveProps(castV($), p.unbox)).runNow())

    builder.lifecycle.componentWillUpdate.foreach(f =>
      spec.componentWillUpdate = ($: RawComp, p: Box[P], s: Box[S]) =>
        f(new ComponentWillUpdate(castV($), p.unbox, s.unbox)).runNow())

    builder.lifecycle.shouldComponentUpdate.foreach(f =>
      spec.shouldComponentUpdate = ($: RawComp, p: Box[P], s: Box[S]) =>
        f(new ShouldComponentUpdate(castV($), p.unbox, s.unbox)).runNow())

    // if (jsMixins.nonEmpty)
    //   spec("mixins") = JArray(jsMixins: _*)
    // lc.configureSpec.foreach(_(spec2).runNow())

    spec
  }
}
