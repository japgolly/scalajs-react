package japgolly.scalajs.react

import scala.scalajs.js

// Caching later

sealed abstract class Ctor2[-P, +U, F[-p, +u] <: Ctor2[p, u, F]] {
//  final type Props = P
//  final type Unmounted = U
  //def applyGeneric: (P, KeyArg, ChildrenArgSeq) => O

  def lmap[X](f: X => P): F[X, U]
  def rmap[X](f: U => X): F[P, X]
}

object Ctor2 {
  type ArgKey      = js.UndefOr[Key]
  type ArgRef      = js.UndefOr[String]
  type ArgChild    = raw.ReactNodeList
  type ArgChildren = Seq[ArgChild]

//  type Init[-P <: js.Object, C[-p, +u, F[-fp, +fu] <: Ctor2[fp, fu, F]] <: Ctor2[p, u, F]] = C[P, raw.ReactComponentElement, F]

////  def maybeApplyKey(props: js.Object, key: KeyArg): Unit =
////    key.foreach(applyKey(props, _))
////
////  def applyKey(props: js.Object, key: Key): Unit =
////    props.asInstanceOf[js.Dynamic].updateDynamic("key")(key.asInstanceOf[js.Any])

  final case class PropsAndChildren[-P, +U](fn: (ArgKey, ArgRef, P, ArgChildren) => U) extends Ctor2[P, U, PropsAndChildren] {
    override def lmap[X](f: X => P): PropsAndChildren[X, U] = PropsAndChildren((k, r, p, c) => fn(k, r, f(p), c))
    override def rmap[X](f: U => X): PropsAndChildren[P, X] = PropsAndChildren((k, r, p, c) => f(fn(k, r, p, c)))
  }

  final case class Props           [-P, +U](fn: (ArgKey, ArgRef, P             ) => U) extends Ctor2[P, U, Props   ] {
    override def lmap[X](f: X => P): Props[X, U] = Props((k, r, p) => fn(k, r, f(p)))
    override def rmap[X](f: U => X): Props[P, X] = Props((k, r, p) => f(fn(k, r, p)))
  }

  final case class Children        [-P, +U](fn: (ArgKey, ArgRef,    ArgChildren) => U) extends Ctor2[P, U, Children] {
    override def lmap[X](f: X => P): Children[X, U] = Children(fn)
    override def rmap[X](f: U => X): Children[P, X] = Children((k, r, c) => f(fn(k, r, c)))
  }

  final case class Void            [-P, +U](fn: (ArgKey, ArgRef                ) => U, static: U) extends Ctor2[P, U, Void] {
//    override def lmap[X](f: X => P): Void[X, U] = Void(fn, static)
    override def lmap[X](f: X => P): Void[X, U] = this.asInstanceOf[Void[X, U]]
    override def rmap[X](f: U => X): Void[P, X] = Void((k, r) => f(fn(k, r)), f(static))
  }

//
//  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  // Ops
//
//  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//
//  // TODO Need implicit Init[P, C]
//
//
////  trait BaseCtor2[CC <: Ctor2] {
////    val ctor: CC
////  }
//  case class BaseCtor22[+CC <: Ctor2](ctor: CC) {
//    def mapCtor2[CC2 <: Ctor2](f: CC => CC2): BaseCtor22[CC2] = ???
//  }
//  type BaseCtor2[P, U] = BaseCtor22[_ <: Aux[P, U]]
//  type BaseCtor2JS[P <: js.Object, S <: js.Object] = BaseCtor22[_ <: Aux[P, CompJs3.Unmounted[P, S]]]
//
//  trait ImpCtor2[P <: js.Object, C <: ChildrenArg, S <: js.Object] {
//    type CC <: Ctor2
//    def f: raw.ReactClass => CC
//  }
//
//  import CompJs3.{Mounted, Unmounted}
//
//  implicit def impCtor2Full[P <: js.Object, S <: js.Object] = new ImpCtor2[P, ChildrenArg.Varargs, S] {
//    override type CC = PropsAndChildren[P, Unmounted[P, S]]
//    override def f = rc => PropsAndChildren[P, Unmounted[P, S]]((k, r, p, c) =>
//      // TODO use key & ref
//      Unmounted(raw.React.createElement(rc, p, c: _*)))
//  }
//
//  def impCtor2Void[P <: js.Object, S <: js.Object](props: P, newProps: () => P) = new ImpCtor2[P, ChildrenArg.None, S] {
//    override type CC = Void[P, Unmounted[P, S]]
//    override def f = rc => {
//      val static = Unmounted[P, S](raw.React.createElement(rc, props))
////      Void[P, Unmounted[P, S]]((k, r) =>
////        if (k.isEmpty && r.isEmpty)
////          static
////        else {
////          val p = newProps()
////          // TODO use key & ref
////          Unmounted(raw.React.createElement(rc, p))
////        }
////      )
//      Void[P, Unmounted[P, S]]((k, r) => {
//          val p = newProps()
//          // TODO use key & ref
//          Unmounted(raw.React.createElement(rc, p))
//        },
//        static
//      )
//    }
//  }

  class FullOps[P, U](val ctor: PropsAndChildren[P, U]) extends AnyVal {
    def apply(props: P)(children: ArgChild*): U =
      ctor.fn(???, ???, props, children)
  }
  implicit def toFullOps[P, U](ctor: PropsAndChildren[P, U]): FullOps[P, U] =
    new FullOps(ctor)

  class VoidOps[P, U](val ctor: Void[P, U]) extends AnyVal {
    def apply(key: ArgKey = js.undefined): U =
      if (key.isEmpty)
        ctor.static
      else
        ctor.fn(key, ???)
  }
  implicit def toVoidOps[P, U](ctor: Void[P, U]): VoidOps[P, U] =
    new VoidOps(ctor)

//  def jsCtor2[P <: js.Object, C <: ChildrenArg, S <: js.Object](rawCls: raw.ReactClass)(implicit i: ImpCtor2[P, C, S]): BaseCtor22[i.CC] =
//    BaseCtor22(i f rawCls)
//
//  sealed trait PPP extends js.Object
//  sealed trait SSS extends js.Object
//  def ppp: PPP = ???
//  def TEST1 = jsCtor2[PPP, ChildrenArg.Varargs, SSS](???)
//  def TEST2 = TEST1(ppp)()
//
//  // case class ExampleComp[C <: Ctor2](implicit ctor: Ctor2.Init[P, C]) extends BaseCtor2[]
//
//
////  def Constructor[P <: js.Object, C <: ChildrenArg, S <: js.Object](r: raw.ReactClass)
////      (implicit d: DirectCtor2[P, C, raw.ReactComponentElement]): Constructor[P, C, S] =
////
////    new Constructor(r, d, Mounted[P, S])
////
////  class Constructor[CC <: Ctor2, S <: js.Object, M](val rawCls: raw.ReactClass,
////                                                                         val directCtor2: DirectCtor2.Init[P, C],
////                                                                         wrapMount: raw.ReactComponent => M)
////    extends BaseCtor2[P, C, Unmounted[P, S, M]] {
//
//
//  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  // Ops
//
//  /*
//
//  final case class PropsAndChildren[P, O](fn: (P, KeyArg, ChildrenArgSeq) => O) extends Ctor2[P, ChildrenArg.Varargs, O] {
//    override def applyGeneric = fn
//  }
//
//  final case class Props[P, O](fn: (P, KeyArg) => O) extends Ctor2[P, ChildrenArg.None, O] {
//    override def applyGeneric = (p, k, _) => fn(p, k)
//  }
//
//  final case class Const[P, O](props: P, propsPlus: (KeyArg) => P,
//                               fn: raw.ReactClass => P => O) extends Ctor2[P, ChildrenArg.None, O] {
//
//    def apply: raw.ReactClass => (P, KeyArg, ChildrenArgSeq) => O
//
//
//    override def applyGeneric = rc => {
//      val fn2 = fn(rc)
//      val const = fn2(props, js.undefined)
//      (_, key, _) =>
//        if (key.isEmpty)
//          const
//        else
//          fn2(propsPlus, k)
//    }
//  }
//
//  final case class Children[P, O](props: P, propsPlus: (KeyArg) => P,
//                                  fn: (P, KeyArg, ChildrenArgSeq) => O) extends Ctor2[P, ChildrenArg.Varargs, O] {
//    override def applyGeneric = fn
//  }
//  */
//
}
