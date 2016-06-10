package japgolly.scalajs.react

import org.scalajs.dom
import scalajs.js
import japgolly.scalajs.react.internal._
import JsComponent._

final class JsComponent[P <: js.Object, S <: js.Object, CT[_, _] <: CtorType[_, _], M]
    (val rawCls: raw.ReactClass, override val ctor: CT[P, Unmounted[P, S, M]])
    extends Component[P, CT, Unmounted[P, S, M]] {

  def mapMounted[MM](f: M => MM)(implicit p: Profunctor[CT]): JsComponent[P, S, CT, MM] =
    new JsComponent(rawCls, ctor rmap (_ mapMounted f))
}

object JsComponent {

  // ===================================================================================================================

  final class Unmounted[P <: js.Object, S <: js.Object, M]
      (val rawElement: raw.ReactComponentElement, m: raw.ReactComponent => M)
      extends Component.Unmounted[P, M] {

    override def key: Option[Key] =
      jsNullToOption(rawElement.key)

    override def ref: Option[String] =
      jsNullToOption(rawElement.ref)

    override def props: P =
      rawElement.props.asInstanceOf[P]

    override def propsChildren: PropsChildren =
      PropsChildren(rawElement.props.children)

    override def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): M =
      m(raw.ReactDOM.render(rawElement, container, callback.toJsFn))

    def mapMounted[MM](f: M => MM): Unmounted[P, S, MM] =
      new Unmounted(rawElement, f compose m)
  }

  // ===================================================================================================================

  final class Mounted[P <: js.Object, S <: js.Object, Raw <: raw.ReactComponent](val rawInstance: Raw)
      extends Component.Mounted[Effect.Id, P, S] {

    override protected implicit def F = Effect.InstanceId

    override def props: P =
      rawInstance.props.asInstanceOf[P]

    override def propsChildren =
      PropsChildren(rawInstance.props.children)

    override def state: S =
      rawInstance.state.asInstanceOf[S]

    override def setState(state: S, callback: Callback = Callback.empty): Unit =
      rawInstance.setState(state, callback.toJsFn)

    override def modState(mod: S => S, callback: Callback = Callback.empty): Unit =
      rawInstance.modState(mod.asInstanceOf[js.Object => js.Object], callback.toJsFn)

    override def isMounted =
      rawInstance.isMounted()

    override def getDOMNode: dom.Element =
      raw.ReactDOM.findDOMNode(rawInstance)

    override def forceUpdate(callback: Callback = Callback.empty): Unit =
      rawInstance.forceUpdate(callback.toJsFn)

    def addRawType[T <: js.Object]: Mounted[P, S, Raw with T] =
      this.asInstanceOf[Mounted[P, S, Raw with T]]

//      def getDefaultProps: Props
//      def getInitialState: js.Object | Null
//      def render(): ReactElement

//    override def mapProps[A](f: P => A): Mounted[A, S, Raw] = {
//      val self = this
//      new Mounted[A, S, Raw] {
//        override val rawInstance = self.rawInstance
//        override def props: A = f(self.props)
//      }
//    }
  }

  // ===================================================================================================================

  type Basic[P <: js.Object, S <: js.Object, CT[_, _] <: CtorType[_, _]] =
    JsComponent[P, S, CT, BasicMounted[P, S]]

  type BasicUnmounted[P <: js.Object, S <: js.Object] =
    Unmounted[P, S, BasicMounted[P, S]]

  type BasicMounted[P <: js.Object, S <: js.Object] =
    Mounted[P, S, raw.ReactComponent]

  def apply[P <: js.Object, C <: ChildrenArg, S <: js.Object](rc: raw.ReactClass)
                                                             (implicit s: CtorType.Summoner[P, C]): Basic[P, S, s.CT] =
    new JsComponent[P, S, s.CT, BasicMounted[P, S]](rc, s.pf.rmap(s.summon(rc))(BasicUnmounted(_)))

  def BasicUnmounted[P <: js.Object, S <: js.Object](r: raw.ReactComponentElement): BasicUnmounted[P, S] =
    new Unmounted(r, BasicMounted[P, S])

  def BasicMounted[P <: js.Object, S <: js.Object](r: raw.ReactComponent): BasicMounted[P, S] =
    new Mounted(r)
}
