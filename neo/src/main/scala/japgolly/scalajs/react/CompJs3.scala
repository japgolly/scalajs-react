package japgolly.scalajs.react

import org.scalajs.dom
import scalajs.js

object CompJs3 {
  type Constructor[P <: js.Object, S <: js.Object] = CompJs3X.Constructor[P, S, Mounted[P, S]]
  type Unmounted  [P <: js.Object, S <: js.Object] = CompJs3X.Unmounted  [P, S, Mounted[P, S]]
  type Mounted    [P <: js.Object, S <: js.Object] = CompJs3X.Mounted    [P, S, raw.ReactComponent]

  def Constructor[P <: js.Object, S <: js.Object](r: raw.ReactClass)
                                                 (implicit d: CompJs3X.DirectCtor[P, raw.ReactComponentElement]): Constructor[P, S] =
    new CompJs3X.Constructor(r, d, Mounted[P, S])

  def Mounted[P <: js.Object, S <: js.Object](r: raw.ReactComponent): Mounted[P, S] =
    CompJs3X.Mounted(r)
}

object CompJs3X {

  abstract class DirectCtor[P, O] {
    def apply(cls: raw.ReactClass): P => O

    def lmap[X](f: X => P): DirectCtor[X, O]
    def rmap[X](f: O => X): DirectCtor[P, X]

    def dimap[X, Y](f: X => P)(g: O => Y): DirectCtor[X, Y] =
      lmap(f).rmap(g)
  }

  object DirectCtor extends DirectCtor_LowPri {
    def apply[P, O](run: (raw.ReactClass, P) => O): DirectCtor[P, O] =
      new DirectCtor[P, O] {
        override def apply(c: raw.ReactClass) = run(c, _)
        override def lmap[X](f: X => P)       = DirectCtor[X, O]((c, x) => run(c, f(x)))
        override def rmap[X](f: O => X)       = DirectCtor[P, X]((c, p) => f(run(c, p)))
      }

    def const[P, O](run: raw.ReactClass => O): DirectCtor[P, O] =
      new DirectCtor[P, O] {
        override def apply(c: raw.ReactClass) = {val i = run(c); _ => i}
        override def lmap[X](f: X => P)       = const[X, O](run)
        override def rmap[X](f: O => X)       = const[P, X](f compose run)
        override def toString                 = "DirectCtor.const"
      }

    def constProps[P <: js.Object](props: P): DirectCtor[P, raw.ReactComponentElement] =
      const(raw.React.createElement(_, props))

    implicit val PropsNull: DirectCtor[Null, raw.ReactComponentElement] =
      constProps(null)

    implicit val PropsBoxUnit: DirectCtor[Box[Unit], raw.ReactComponentElement] =
      constProps(Box.Unit)
  }

  trait DirectCtor_LowPri {
    // Scala's contravariant implicit search is stupid
    private val AskPropsInstance: DirectCtor[js.Object, raw.ReactComponentElement] =
      DirectCtor(raw.React.createElement(_, _))

    implicit def askProps[P <: js.Object]: DirectCtor[P, raw.ReactComponentElement] =
      AskPropsInstance.asInstanceOf[DirectCtor[P, raw.ReactComponentElement]]
  }

  class Constructor[P <: js.Object, S <: js.Object, M](val rawCls    : raw.ReactClass,
                                                       val directCtor: DirectCtor[P, raw.ReactComponentElement],
                                                       wrapMount     : raw.ReactComponent => M) {

    def mapMounted[MM](f: M => MM): Constructor[P, S, MM] =
      new Constructor(rawCls, directCtor, f compose wrapMount)

    val directCtorU: DirectCtor[P, Unmounted[P, S, M]] =
      directCtor.rmap(new Unmounted[P, S, M](_, wrapMount))

    val applyDirect: P => Unmounted[P, S, M] =
      directCtorU(rawCls)
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
      extends MountedBase[Effect.Id, P, S] {

    val rawInstance: Raw

    override protected final implicit def F = Effect.InstanceId

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

//    override final def mapProps[A](f: P => A): Mounted[A, S, Raw] = {
//      val self = this
//      new Mounted[A, S, Raw] {
//        override val rawInstance = self.rawInstance
//        override def props: A = f(self.props)
//      }
//    }
  }

}
