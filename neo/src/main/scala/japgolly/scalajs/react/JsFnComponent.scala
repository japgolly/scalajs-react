package japgolly.scalajs.react

import scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.{raw => Raw}
import JsFnComponent._

final class JsFnComponent[P <: js.Object, CT[_, _] <: CtorType[_, _]](val raw: Raw.ReactFunctionalComponent,
                                                                      override val ctor: CT[P, Unmounted[P]])
    extends Component[P, CT, Unmounted[P]]

object JsFnComponent {

  def apply[P <: js.Object, C <: ChildrenArg](rc: Raw.ReactFunctionalComponent)
                                             (implicit s: CtorType.Summoner[P, C]): JsFnComponent[P, s.CT] =
    new JsFnComponent[P, s.CT](rc, s.pf.rmap(s.summon(rc))(new Unmounted(_)))

  // ===================================================================================================================

  final class Unmounted[P <: js.Object](val raw: Raw.ReactComponentElement) extends Component.Unmounted[P, Mounted] {

    override def key: Option[Key] =
      jsNullToOption(raw.key)

    override def ref: Option[String] =
      // orNullToOption(raw.ref)
      None

    override def props: P =
      raw.props.asInstanceOf[P]

    override def propsChildren: PropsChildren =
      PropsChildren(raw.props.children)

    override def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty): Unit = {
      val result = Raw.ReactDOM.render(raw, container, callback.toJsFn)

      // Protect against future React change.
      assert(result eq null, "Expected rendered functional component to return null; not " + result)
    }

    // override def mapMounted[M](f: Mounted => M): Unmounted[P, M] =
  }

  type Mounted = Unit
}
