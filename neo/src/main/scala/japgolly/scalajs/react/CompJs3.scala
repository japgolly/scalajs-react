package japgolly.scalajs.react

import org.scalajs.dom
import scalajs.js

object CompJs3 {
  type Constructor_NoProps[S <: js.Object] = CompJs3X.Constructor_NoProps[S, Mounted[Null, S]]
  type Constructor[P <: js.Object, S <: js.Object] = CompJs3X.Constructor[P, S, Mounted[P, S]]
  type Unmounted  [P <: js.Object, S <: js.Object] = CompJs3X.Unmounted  [P, S, Mounted[P, S]]
  type Mounted    [P <: js.Object, S <: js.Object] = CompJs3X.Mounted    [P, S, raw.ReactComponent]

  def Constructor[P <: js.Object, S <: js.Object](r: raw.ReactClass): Constructor[P, S] =
    CompJs3X.Constructor(r)(Mounted[P, S])

  def Constructor_NoProps[S <: js.Object](r: raw.ReactClass): Constructor_NoProps[S] =
    CompJs3X.Constructor_NoProps(r)(Mounted[Null, S])

  def Mounted[P <: js.Object, S <: js.Object](r: raw.ReactComponent): Mounted[P, S] =
    CompJs3X.Mounted(r)
}

object CompJs3X {

  case class Constructor[P <: js.Object, S <: js.Object, M](rawCls: raw.ReactClass)(m: raw.ReactComponent => M) {
    def mapMounted[MM](f: M => MM): Constructor[P, S, MM] =
      new Constructor(rawCls)(f compose m)

    def apply(props: P): Unmounted[P, S, M] =
      new Unmounted(raw.React.createElement(rawCls, props), m)
  }

  case class Constructor_NoProps[S <: js.Object, M](rawCls: raw.ReactClass)(m: raw.ReactComponent => M) {
    def mapMounted[MM](f: M => MM): Constructor_NoProps[S, MM] =
      new Constructor_NoProps(rawCls)(f compose m)

    private val instance: Unmounted[Null, S, M] =
      new Constructor(rawCls)(m)(null)

    def apply(): Unmounted[Null, S, M] =
      instance
  }

  class Unmounted[P <: js.Object, S <: js.Object, M](val rawElement: raw.ReactComponentElement, m: raw.ReactComponent => M) {

    def key: Option[Key] =
      orNullToOption(rawElement.key)

    def ref: Option[String] =
      orNullToOption(rawElement.ref)

    def props: P =
      rawElement.props.asInstanceOf[P]

    def propsChildren: raw.ReactNodeList =
      rawElement.props.children

    def mapMounted[MM](f: M => MM): Unmounted[P, S, MM] =
      new Unmounted(rawElement, f compose m)

    def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): M =
      m(raw.ReactDOM.render(rawElement, container, callback.toJsFn))
  }

  def Mounted[P <: js.Object, S <: js.Object, Raw <: raw.ReactComponent](r: Raw): Mounted[P, S, Raw] =
    new Mounted[P, S, Raw] {
      override val rawInstance = r
    }

  trait Mounted[P <: js.Object, S <: js.Object, Raw <: raw.ReactComponent]
      extends BaseMounted[Effect.Id, P, S] {
    val rawInstance: Raw

    def addRawType[T <: js.Object]: Mounted[P, S, Raw with T] =
      this.asInstanceOf[Mounted[P, S, Raw with T]]

    //      def getDefaultProps: Props
    //      def getInitialState: js.Object | Null
    //      def render(): ReactElement

    override final def isMounted =
      rawInstance.isMounted()

    override final def props: P =
      rawInstance.props.asInstanceOf[P]

    override final def propsChildren =
      rawInstance.props.children

    override final def state: S =
      rawInstance.state.asInstanceOf[S]

    override final def setState(state: S, callback: Callback = Callback.empty): Unit =
      rawInstance.setState(state, callback.toJsFn)

    override final def modState(mod: S => S, callback: Callback = Callback.empty): Unit =
      rawInstance.modState(mod.asInstanceOf[js.Object => js.Object], callback.toJsFn)

    override final def getDOMNode: dom.Element =
      raw.ReactDOM.findDOMNode(rawInstance)

    override final def forceUpdate(callback: Callback = Callback.empty): Unit =
      rawInstance.forceUpdate(callback.toJsFn)
  }

}
