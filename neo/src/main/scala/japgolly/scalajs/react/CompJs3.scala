package japgolly.scalajs.react

import org.scalajs.dom
import scalajs.js

object CompJs3 {
  type Constructor[P <: js.Object, C <: ChildrenArg, S <: js.Object] = CompJs3X.Constructor[P, C, S, Mounted[P, S]]
  type Unmounted  [P <: js.Object, S <: js.Object] = CompJs3X.Unmounted  [P, S, Mounted[P, S]]
  type Mounted    [P <: js.Object, S <: js.Object] = CompJs3X.Mounted    [P, S, raw.ReactComponent]

  def Constructor[P <: js.Object, C <: ChildrenArg, S <: js.Object](r: raw.ReactClass)
      (implicit d: CompJs3X.DirectCtor[P, C, raw.ReactComponentElement]): Constructor[P, C, S] =
    new CompJs3X.Constructor(r, d, Mounted[P, S])

  def Mounted[P <: js.Object, S <: js.Object](r: raw.ReactComponent): Mounted[P, S] =
    CompJs3X.Mounted(r)
}

object CompJs3X {

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  abstract class DirectCtor[P, C <: ChildrenArg, O] {
    def apply(cls: raw.ReactClass): (P, ChildrenArgSeq) => O

    def lmap[X](f: X => P): DirectCtor[X, C, O]
    def rmap[X](f: O => X): DirectCtor[P, C, X]

    def dimap[X, Y](f: X => P)(g: O => Y): DirectCtor[X, C, Y] =
      lmap(f).rmap(g)
  }

  object DirectCtor extends DirectCtor_LowPri {
    def apply[P, C <: ChildrenArg, O](run: (raw.ReactClass, P, ChildrenArgSeq) => O): DirectCtor[P, C, O] =
      new DirectCtor[P, C, O] {
        override def apply(c: raw.ReactClass) = run(c, _, _)
        override def lmap[X](f: X => P)       = DirectCtor[X, C, O]((rc, x, c) => run(rc, f(x), c))
        override def rmap[X](f: O => X)       = DirectCtor[P, C, X]((rc, p, c) => f(run(rc, p, c)))
      }

    def const[P, O](run: raw.ReactClass => O): DirectCtor[P, ChildrenArg.None, O] =
      new DirectCtor[P, ChildrenArg.None, O] {
        override def apply(c: raw.ReactClass) = {val i = run(c); (_, _) => i}
        override def lmap[X](f: X => P)       = const[X, O](run)
        override def rmap[X](f: O => X)       = const[P, X](f compose run)
        override def toString                 = "DirectCtor.const"
      }

    type Init[P, C <: ChildrenArg] = DirectCtor[P, C, raw.ReactComponentElement]

    def constProps[P <: js.Object](props: P): Init[P, ChildrenArg.None] =
      const(raw.React.createElement(_, props))

    implicit val PropsNull: Init[Null, ChildrenArg.None] =
      constProps(null)

    implicit val PropsBoxUnit: Init[Box[Unit], ChildrenArg.None] =
      constProps(Box.Unit)
  }

  trait DirectCtor_LowPri {
    import DirectCtor.Init

    // Scala's contravariant implicit search is stupid
    private val AskPropsInstance: Init[js.Object, ChildrenArg.None] =
      DirectCtor((rc, p, _) => raw.React.createElement(rc, p))

    implicit def askProps[P <: js.Object]: Init[P, ChildrenArg.None] =
      AskPropsInstance.asInstanceOf[Init[P, ChildrenArg.None]]

    // Scala's contravariant implicit search is stupid
    private val AskPropsCInstance: Init[js.Object, ChildrenArg.Varargs] =
      DirectCtor((rc, p, c) => raw.React.createElement(rc, p, c: _*))

    implicit def askPropsC[P <: js.Object]: Init[P, ChildrenArg.Varargs] =
      AskPropsCInstance.asInstanceOf[Init[P, ChildrenArg.Varargs]]
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  class Constructor[P <: js.Object, C <: ChildrenArg, S <: js.Object, M](val rawCls: raw.ReactClass,
                                                                         val directCtor: DirectCtor[P, C, raw.ReactComponentElement],
                                                                         wrapMount: raw.ReactComponent => M) {

    def mapMounted[MM](f: M => MM): Constructor[P, C, S, MM] =
      new Constructor(rawCls, directCtor, f compose wrapMount)

    val directCtorU: DirectCtor[P, C, Unmounted[P, S, M]] =
      directCtor.rmap(new Unmounted[P, S, M](_, wrapMount))

    val applyDirect: (P, ChildrenArgSeq) => Unmounted[P, S, M] =
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
