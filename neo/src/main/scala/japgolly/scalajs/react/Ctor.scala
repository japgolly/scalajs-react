package japgolly.scalajs.react

import scala.scalajs.js

// Caching later

sealed abstract class Ctor { //[P, C <: ChildrenArg, +O] {
  type Props
  type Unmounted
  //def applyGeneric: (P, KeyArg, ChildrenArgSeq) => O
}

object Ctor {
  type ArgKey      = js.UndefOr[Key]
  type ArgRef      = js.UndefOr[String]
  type ArgChild    = raw.ReactNodeList
  type ArgChildren = Seq[ArgChild]

  type Aux[P, U] = Ctor {
    type Props     = P
    type Unmounted = U
  }

  sealed abstract class AuxB[P, U] extends Ctor {
    override final type Props     = P
    override final type Unmounted = U
  }

  type Init[P <: js.Object] = Aux[P, raw.ReactComponentElement]

//  def maybeApplyKey(props: js.Object, key: KeyArg): Unit =
//    key.foreach(applyKey(props, _))
//
//  def applyKey(props: js.Object, key: Key): Unit =
//    props.asInstanceOf[js.Dynamic].updateDynamic("key")(key.asInstanceOf[js.Any])

  final case class PropsAndChildren[P, U](fn: (ArgKey, ArgRef, P, ArgChildren) => U) extends AuxB[P, U]
  final case class Props           [P, U](fn: (ArgKey, ArgRef, P             ) => U) extends AuxB[P, U]
  final case class Children        [P, U](fn: (ArgKey, ArgRef,    ArgChildren) => U) extends AuxB[P, U]

  final case class Void            [P, U](fn: (ArgKey, ArgRef                ) => U, static: U) extends AuxB[P, U] {
    def lmap[X](f: X => P): Void[X, U] = Void(fn, static)
    def rmap[X](f: U => X): Void[P, X] = Void((k, r) => f(fn(k, r)), f(static))
  }


  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // Ops

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  // TODO Need implicit Init[P, C]


//  trait BaseCtor[CC <: Ctor] {
//    val ctor: CC
//  }
  case class BaseCtor2[+CC <: Ctor](ctor: CC) {
    def mapCtor[CC2 <: Ctor](f: CC => CC2): BaseCtor2[CC2] = ???
  }
  type BaseCtor[P, U] = BaseCtor2[_ <: Aux[P, U]]
  type BaseCtorJS[P <: js.Object, S <: js.Object] = BaseCtor2[_ <: Aux[P, CompJs3.Unmounted[P, S]]]

  trait ImpCtor[P <: js.Object, C <: ChildrenArg, S <: js.Object] {
    type CC <: Ctor
    def f: raw.ReactClass => CC
  }

  import CompJs3.{Mounted, Unmounted}

  implicit def impCtorFull[P <: js.Object, S <: js.Object] = new ImpCtor[P, ChildrenArg.Varargs, S] {
    override type CC = PropsAndChildren[P, Unmounted[P, S]]
    override def f = rc => PropsAndChildren[P, Unmounted[P, S]]((k, r, p, c) =>
      // TODO use key & ref
      Unmounted(raw.React.createElement(rc, p, c: _*)))
  }

  def impCtorVoid[P <: js.Object, S <: js.Object](props: P, newProps: () => P) = new ImpCtor[P, ChildrenArg.None, S] {
    override type CC = Void[P, Unmounted[P, S]]
    override def f = rc => {
      val static = Unmounted[P, S](raw.React.createElement(rc, props))
//      Void[P, Unmounted[P, S]]((k, r) =>
//        if (k.isEmpty && r.isEmpty)
//          static
//        else {
//          val p = newProps()
//          // TODO use key & ref
//          Unmounted(raw.React.createElement(rc, p))
//        }
//      )
      Void[P, Unmounted[P, S]]((k, r) => {
          val p = newProps()
          // TODO use key & ref
          Unmounted(raw.React.createElement(rc, p))
        },
        static
      )
    }
  }

  class FullOps[P, U](val ctor: PropsAndChildren[P, U]) extends AnyVal {
    def apply(props: P)(children: ArgChild*): U =
      ctor.fn(???, ???, props, children)
  }
  implicit def toFullOps[P, U](base: BaseCtor2[PropsAndChildren[P, U]]): FullOps[P, U] =
    new FullOps(base.ctor)

  class VoidOps[P, U](val ctor: Void[P, U]) extends AnyVal {
    def apply(key: ArgKey = js.undefined): U =
      if (key.isEmpty)
        ctor.static
      else
        ctor.fn(key, ???)
  }
  implicit def toVoidOps[P, U](base: BaseCtor2[Void[P, U]]): VoidOps[P, U] =
    new VoidOps(base.ctor)

  def jsCtor[P <: js.Object, C <: ChildrenArg, S <: js.Object](rawCls: raw.ReactClass)(implicit i: ImpCtor[P, C, S]): BaseCtor2[i.CC] =
    BaseCtor2(i f rawCls)

  sealed trait PPP extends js.Object
  sealed trait SSS extends js.Object
  def ppp: PPP = ???
  def TEST1 = jsCtor[PPP, ChildrenArg.Varargs, SSS](???)
  def TEST2 = TEST1(ppp)()

  // case class ExampleComp[C <: Ctor](implicit ctor: Ctor.Init[P, C]) extends BaseCtor[]


//  def Constructor[P <: js.Object, C <: ChildrenArg, S <: js.Object](r: raw.ReactClass)
//      (implicit d: DirectCtor[P, C, raw.ReactComponentElement]): Constructor[P, C, S] =
//
//    new Constructor(r, d, Mounted[P, S])
//
//  class Constructor[CC <: Ctor, S <: js.Object, M](val rawCls: raw.ReactClass,
//                                                                         val directCtor: DirectCtor.Init[P, C],
//                                                                         wrapMount: raw.ReactComponent => M)
//    extends BaseCtor[P, C, Unmounted[P, S, M]] {


  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // Ops

  /*

  final case class PropsAndChildren[P, O](fn: (P, KeyArg, ChildrenArgSeq) => O) extends Ctor[P, ChildrenArg.Varargs, O] {
    override def applyGeneric = fn
  }

  final case class Props[P, O](fn: (P, KeyArg) => O) extends Ctor[P, ChildrenArg.None, O] {
    override def applyGeneric = (p, k, _) => fn(p, k)
  }

  final case class Const[P, O](props: P, propsPlus: (KeyArg) => P,
                               fn: raw.ReactClass => P => O) extends Ctor[P, ChildrenArg.None, O] {

    def apply: raw.ReactClass => (P, KeyArg, ChildrenArgSeq) => O


    override def applyGeneric = rc => {
      val fn2 = fn(rc)
      val const = fn2(props, js.undefined)
      (_, key, _) =>
        if (key.isEmpty)
          const
        else
          fn2(propsPlus, k)
    }
  }

  final case class Children[P, O](props: P, propsPlus: (KeyArg) => P,
                                  fn: (P, KeyArg, ChildrenArgSeq) => O) extends Ctor[P, ChildrenArg.Varargs, O] {
    override def applyGeneric = fn
  }
  */

}
