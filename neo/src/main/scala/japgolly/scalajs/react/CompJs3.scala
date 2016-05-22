package japgolly.scalajs.react

import org.scalajs.dom
import scalajs.js

object CompJs3 {
//  type Constructor_NoProps[S <: js.Object] = Constructor[Null, S]
//  type Constructor_NoProps[S <: js.Object] = CompJs3X.Constructor_NoProps[S, Mounted[Null, S]]
  type Constructor[P <: js.Object, S <: js.Object] = CompJs3X.Constructor[P, S, Mounted[P, S]]
  type Unmounted  [P <: js.Object, S <: js.Object] = CompJs3X.Unmounted  [P, S, Mounted[P, S]]
  type Mounted    [P <: js.Object, S <: js.Object] = CompJs3X.Mounted    [P, S, raw.ReactComponent]

  def Constructor[P <: js.Object, S <: js.Object](r: raw.ReactClass)
                                                 (implicit direct: CompJs3X.DirectCtor[P, raw.ReactComponentElement]): Constructor[P, S] =
    CompJs3X.Constructor(r)(Mounted[P, S])(direct)

//  def Constructor_NoProps[S <: js.Object](r: raw.ReactClass): Constructor_NoProps[S] =
//    ??? //CompJs3X.Constructor_NoProps(r)(Mounted[Null, S])

  def Mounted[P <: js.Object, S <: js.Object](r: raw.ReactComponent): Mounted[P, S] =
    CompJs3X.Mounted(r)
}

object CompJs3X {

//  type ¬[T] = T => Nothing
//  type ¬¬[T] = ¬[¬[T]]
//  type ∧[T, U] = T with U
//  type ∨[T, U] = ¬[¬[T] ∧ ¬[U]]
//
//  // Type-lambda for context bound
//  type |∨|[T, U] = {
//    type λ[X] = ¬¬[X] <:< (T ∨ U)
//  }

  // Type inequalities
  trait =:!=[A, B]

  def unexpected : Nothing = sys.error("Unexpected invocation")
  implicit def neq[A, B] : A =:!= B = new =:!=[A, B] {}
  implicit def neqAmbig1[A] : A =:!= A = unexpected
  implicit def neqAmbig2[A] : A =:!= A = unexpected


  case class CtorTC_P[A, P, U](apply: (A, P) => U) extends AnyVal
  case class CtorTC__[A, U](apply: A => U) extends AnyVal

  @inline implicit class CtorOps_P[A, P, U](private val self: A)(implicit c: CtorTC_P[A, P, U]) {
    @inline def apply(props: P): U =
      c.apply(self, props)
  }
  @inline implicit class CtorOps__[A, U](private val self: A)(implicit c: CtorTC__[A, U]) {
    @inline def apply(): U =
      c.apply(self)
  }

  implicit def ctorTC_P[P <: js.Object, S <: js.Object, M](implicit ev: P =:!= Null): CtorTC_P[Constructor[P, S, M], P, Unmounted[P, S, M]] =
    CtorTC_P(_ applyDirect _)

  implicit def ctorTC__[S <: js.Object, M]: CtorTC__[Constructor[Null, S, M], Unmounted[Null, S, M]] =
    //CtorTC__(c => new Constructor(c.rawCls)(c.m)(null))
    CtorTC__(_ applyDirect null)


//  case class DirectCtor[P, O](apply: (raw.ReactClass, P) => O) extends AnyVal {
//    def cmap[X](f: X => P): DirectCtor[X, O] = DirectCtor((c, x) => apply(c, f(x)))
//    def map [X](f: O => X): DirectCtor[P, X] = DirectCtor((c, p) => f(apply(c, p)))
//  }

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

    implicit val nullProps: DirectCtor[Null, raw.ReactComponentElement] =
      constProps(null)
  }

  trait DirectCtor_LowPri {
    implicit def askProps[P <: js.Object]/*(implicit ev: P =:!= Null)*/: DirectCtor[P, raw.ReactComponentElement] =
      DirectCtor(raw.React.createElement(_, _))
  }


  case class Constructor[P <: js.Object, S <: js.Object, M](rawCls: raw.ReactClass)
//                                                           (implicit direct: DirectCtor[P, Unmounted[P, S, M]]) {
                                                           (m: raw.ReactComponent => M)
                                                           (implicit direct: DirectCtor[P, raw.ReactComponentElement]) {

    def mapMounted[MM](f: M => MM): Constructor[P, S, MM] =
//      new Constructor(rawCls)(direct.map(_ mapMounted f))
      new Constructor(rawCls)(f compose m)(direct)

    val applyDirect: P => Unmounted[P, S, M] =
      direct.rmap(new Unmounted[P, S, M](_, m))(rawCls)
//      new Unmounted(raw.React.createElement(rawCls, props), m)
  }


  /*
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
    */

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
