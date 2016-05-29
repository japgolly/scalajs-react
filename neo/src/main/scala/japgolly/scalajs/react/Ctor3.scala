package japgolly.scalajs.react

import org.scalajs.dom

import scala.scalajs.js

// Caching later

sealed abstract class Ctor3[-P, +U] {

  type This[-p, +u] <: Ctor3[p, u]
//  final type Props = P
//  final type Unmounted = U
  //def applyGeneric: (P, KeyArg, ChildrenArgSeq) => O

  def lmap[X](f: X => P): This[X, U]
  def rmap[X](f: U => X): This[P, X]
}

object Ctor3 {
  type Ref         = String
  type ArgKey      = js.UndefOr[Key]
  type ArgRef      = js.UndefOr[Ref]
  type ArgChild    = raw.ReactNodeList
  type ArgChildren = Seq[ArgChild]

//  type Aux[-P, +U, CC <: Ctor3[P, U]] = CC { type This = CC }

  type Init[-P <: js.Object] = Ctor3[P, raw.ReactComponentElement]

  def maybeApplyKey(props: js.Object, key: ArgKey): Unit =
    key.foreach(applyKey(props, _))

  def applyKey(props: js.Object, key: Key): Unit =
    props.asInstanceOf[js.Dynamic].updateDynamic("key")(key.asInstanceOf[js.Any])

  def maybeApplyRef(props: js.Object, ref: ArgRef): Unit =
    ref.foreach(applyRef(props, _))

  def applyRef(props: js.Object, ref: Ref): Unit =
    props.asInstanceOf[js.Dynamic].updateDynamic("ref")(ref.asInstanceOf[js.Any])

  final case class PropsAndChildren[-P, +U](fn: (ArgKey, ArgRef, P, ArgChildren) => U) extends Ctor3[P, U] {
    type This[-p, +u] = PropsAndChildren[p, u]
    override def lmap[X](f: X => P): PropsAndChildren[X, U] = PropsAndChildren((k, r, p, c) => fn(k, r, f(p), c))
    override def rmap[X](f: U => X): PropsAndChildren[P, X] = PropsAndChildren((k, r, p, c) => f(fn(k, r, p, c)))
  }

  final case class Props[-P, +U](fn: (ArgKey, ArgRef, P) => U) extends Ctor3[P, U] {
    type This[-p, +u] = Props[p, u]
    override def lmap[X](f: X => P): Props[X, U] = Props((k, r, p) => fn(k, r, f(p)))
    override def rmap[X](f: U => X): Props[P, X] = Props((k, r, p) => f(fn(k, r, p)))
  }

  final case class Children[-P, +U](fn: (ArgKey, ArgRef, ArgChildren) => U) extends Ctor3[P, U] {
    type This[-p, +u] = Children[p, u]
    override def lmap[X](f: X => P): Children[X, U] = Children(fn)
    override def rmap[X](f: U => X): Children[P, X] = Children((k, r, c) => f(fn(k, r, c)))
  }

  final case class Void[-P, +U](fn: (ArgKey, ArgRef) => U, static: U) extends Ctor3[P, U] {
    type This[-p, +u] = Void[p, u]
    override def lmap[X](f: X => P): Void[X, U] = Void(fn, static)
    override def rmap[X](f: U => X): Void[P, X] = Void((k, r) => f(fn(k, r)), f(static))
  }

  def void[P, U](props: P)(newProps: (ArgKey, ArgRef) => P)(fn: P => U): Void[P, U] =
    Void((k, r) => fn(newProps(k, r)), fn(props))






  trait BaseCtor3[C <: Ctor3[_, _]] {
    val ctor: C
  }

  class OpsPC[P, U](val ctor: PropsAndChildren[P, U]) extends AnyVal {
    def apply(props: P, key: ArgKey = js.undefined, ref: ArgRef = js.undefined)(children: ArgChild*): U =
      ctor.fn(key, ref, props, children)
  }
  class OpsP[P, U](val ctor: Props[P, U]) extends AnyVal {
    def apply(props: P, key: ArgKey = js.undefined, ref: ArgRef = js.undefined): U =
      ctor.fn(key, ref, props)
  }
  class OpsV[U](val ctor: Void[_, U]) extends AnyVal {
    def apply(key: ArgKey = js.undefined, ref: ArgRef = js.undefined): U =
      if (key.isEmpty && ref.isEmpty)
        ctor.static
      else
        ctor.fn(key, ref)
  }
  implicit def toOpsPC[P, U](base: BaseCtor3[PropsAndChildren[P, U]]): OpsPC[P, U] = new OpsPC(base.ctor)
  implicit def toOpsP [P, U](base: BaseCtor3[Props           [P, U]]): OpsP [P, U] = new OpsP (base.ctor)
  implicit def toOpsV [P, U](base: BaseCtor3[Void            [P, U]]): OpsV [   U] = new OpsV (base.ctor)

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  final case class Singleton[A](value: A, mutable: () => A)
  object Singleton {
    implicit val Null = Singleton[Null](null, () => null)
    implicit val Unit = Singleton((), () => ())
    implicit val BoxUnit = Singleton(Box.Unit, () => Box(()))

    sealed trait Not[A]
    @inline implicit def noSingletonFor[A]: Not[A] = null
    implicit def singletonFor1[A: Singleton]: Not[A] = null
    implicit def singletonFor2[A: Singleton]: Not[A] = null
  }

  sealed trait SummonCtor[P <: js.Object, C <: ChildrenArg, S <: js.Object] {
    type CC[-p, +u] <: Ctor3[p, u]
    final type Out = CC[P, CompJs3.Unmounted[P, S]]
    val summon: raw.ReactClass => Out
  }
  object SummonCtor {
    import CompJs3.Unmounted

    def apply[P <: js.Object, C <: ChildrenArg, S <: js.Object, CCC[-p, +u] <: Ctor3[p, u]](f: raw.ReactClass => CCC[P, CompJs3.Unmounted[P, S]]): Aux[P, C, S, CCC] =
      new SummonCtor[P, C, S] { override type CC[-p, +u] = CCC[p, u]; override val summon = f }

    type Aux[P <: js.Object, C <: ChildrenArg, S <: js.Object, CCC[-p, +u] <: Ctor3[p, u]] =
      SummonCtor[P, C, S] {type CC[-p, +u] = CCC[p, u]}

    implicit def summonCtorV[P <: js.Object, S <: js.Object](implicit s: Singleton[P]) =
      apply[P, ChildrenArg.None, S, Void](rc =>
        Ctor3.void[P, Unmounted[P, S]](s.value)(singletonProps(s))(p => Unmounted(raw.React.createElement(rc, p))))

    implicit def summonCtorC[P <: js.Object, S <: js.Object](implicit s: Singleton[P]) =
      apply[P, ChildrenArg.Varargs, S, Children](rc =>
        Children[P, Unmounted[P, S]]((k, r, c) =>
          Unmounted(raw.React.createElement(rc, maybeSingletonProps(s)(k, r), c: _*))))

    implicit def summonCtorPC[P <: js.Object, S <: js.Object](implicit w: Singleton.Not[P]) =
      apply[P, ChildrenArg.Varargs, S, PropsAndChildren](rc =>
        PropsAndChildren[P, Unmounted[P, S]]((k, r, p, c) =>
          Unmounted(raw.React.createElement(rc, applyKR(p)(k, r), c: _*))))

    implicit def summonCtorP[P <: js.Object, S <: js.Object](implicit w: Singleton.Not[P]) =
      apply[P, ChildrenArg.None, S, Props](rc =>
        Props[P, Unmounted[P, S]]((k, r, p) =>
          Unmounted(raw.React.createElement(rc, applyKR(p)(k, r)))))

    def maybeSingletonProps[P <: js.Object](s: Singleton[P])(key: ArgKey, ref: ArgRef): P =
      if (key.isEmpty && ref.isEmpty)
        s.value
      else
        singletonProps(s)(key, ref)

    @inline def singletonProps[P <: js.Object](s: Singleton[P])(key: ArgKey, ref: ArgRef): P =
      applyKR(s.mutable())(key, ref)

    def applyKR[P <: js.Object](p: P)(key: ArgKey, ref: ArgRef): P = {
//      p.asInstanceOf[js.Dynamic].updateDynamic("key")(key.asInstanceOf[js.Any])
//      p.asInstanceOf[js.Dynamic].updateDynamic("ref")(ref.asInstanceOf[js.Any])
      maybeApplyKey(p, key)
      maybeApplyRef(p, ref)
      p
    }

  }


//  class SummonCtor[
//      P <: js.Object,
//      C <: ChildrenArg,
//      S <: js.Object,
//      Out <: Ctor3[P, CompJs3.Unmounted[P, S]]
//    ](val summon: raw.ReactClass => Out) extends AnyVal
//  def SummonCtor[P <: js.Object, C <: ChildrenArg, S <: js.Object, Out <: Ctor3[P, CompJs3.Unmounted[P, S]]](f: raw.ReactClass => Out)

  object CompJs3 {
//    type Constructor[P <: js.Object, C <: ChildrenArg, S <: js.Object] = CompJs3X.Constructor[P, C, S, Mounted[P, S]]
    type Unmounted  [P <: js.Object, S <: js.Object] = CompJs3X.Unmounted  [P, S, Mounted[P, S]]
    type Mounted    [P <: js.Object, S <: js.Object] = CompJs3X.Mounted    [P, S, raw.ReactComponent]

    def Constructor[P <: js.Object, C <: ChildrenArg, S <: js.Object](rc: raw.ReactClass)
                                                                     (implicit s: SummonCtor[P, C, S]) =
      new CompJs3X.Constructor2[P, S, s.CC, Mounted[P, S]](rc, s.summon(rc))

//        (implicit d: DirectCtor[P, C, raw.ReactComponentElement]): Constructor[P, C, S] =
//      new CompJs3X.Constructor(r, d, Mounted[P, S])

    def Unmounted[P <: js.Object, S <: js.Object](r: raw.ReactComponentElement): Unmounted[P, S] =
      new CompJs3X.Unmounted(r, Mounted[P, S])

    def Mounted[P <: js.Object, S <: js.Object](r: raw.ReactComponent): Mounted[P, S] =
      CompJs3X.Mounted(r)
  }

  object CompJs3X {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

//    class Constructor1[P <: js.Object, S <: js.Object, M, C <: Ctor3[P, Unmounted[P, S, M]]](val rawCls: raw.ReactClass,
//                                                                                             val ctor: C) {
//
//      def mapMounted[MM](f: M => MM): Constructor1[P, S, MM, ctor.This[P, Unmounted[P, S, MM]]] =
//        new Constructor1(rawCls, ctor rmap (_ mapMounted f))
//    }

    class Constructor2[P <: js.Object, S <: js.Object, C[a, b] <: Ctor3[a, b], M](
          val rawCls: raw.ReactClass,
          val ctor: C[P, Unmounted[P, S, M]])
        extends BaseCtor3[C[P, Unmounted[P, S, M]]] {
      def mapMounted[MM](f: M => MM): Constructor2[P, S, ctor.This, MM] =
        new Constructor2(rawCls, ctor rmap (_ mapMounted f))
    }

//      val directCtorU: DirectCtor[P, C, Unmounted[P, S, M]] =
//        directCtor.rmap(new Unmounted[P, S, M](_, wrapMount))
//
//      override val applyDirect: (P, ChildrenArgSeq) => Unmounted[P, S, M] =
//        directCtorU(rawCls)
//    }

    class Unmounted[P <: js.Object, S <: js.Object, M](val rawElement: raw.ReactComponentElement, m: raw.ReactComponent => M) {

      def key: Option[Key] =
        orNullToOption(rawElement.key)

      def ref: Option[String] =
        orNullToOption(rawElement.ref)

      def props: P =
        rawElement.props.asInstanceOf[P]

      def propsChildren: PropsChildren =
        PropsChildren(rawElement.props.children)

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
        PropsChildren(rawInstance.props.children)

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
}

