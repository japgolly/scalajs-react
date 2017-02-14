package japgolly.scalajs.react

import scala.scalajs.js
import japgolly.scalajs.react.internal._
import CtorType._

sealed trait ChildrenArg
object ChildrenArg {
  sealed trait None       extends ChildrenArg
  sealed trait Varargs    extends ChildrenArg
}

sealed abstract class CtorType[-P, +U] {
  type This[-P, +U] <: CtorType[P, U]

  def applyGeneric(props: P)(children: ArgChild*): U

  def addMod(f: ModFn): This[P, U]

  final def withRawProp(name: String, value: js.Any): This[P, U] =
    addMod(_.asInstanceOf[js.Dynamic].updateDynamic(name)(value))

  final def withKey(k: Key): This[P, U] =
    withRawProp("key", k.asInstanceOf[js.Any])
}

object CtorType {
  type ArgChild    = vdom.ReactNode
  type ArgChildren = Seq[ArgChild]

  type ModFn = js.Object => Unit

  private type MaybeMod = js.UndefOr[Mod]
  @inline private def noMod: MaybeMod = js.undefined

  private final case class Mod(mod: ModFn) extends AnyVal {
    def apply(o: js.Object): js.Object = {
      mod(o)
      o
    }
    def >>(f: ModFn): Mod =
      Mod(o => {mod(o); f(o)})
  }

  private def modAppend(mods: MaybeMod, f: ModFn): MaybeMod =
    mods.fold(Mod(f))(_ >> f)

  // How do I prove this? (without sealing and pattern matching)
  @inline def hackBackToSelf[CT[-p, +u] <: CtorType[p, u], P2, U2](c: CT[_, _])(t: c.This[P2, U2]): CT[P2, U2] =
    t.asInstanceOf[CT[P2, U2]]

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // Types

  final class PropsAndChildren[-P, +U](private[CtorType] val construct: (P, MaybeMod, ArgChildren) => U,
                                       private[CtorType] val mods: MaybeMod) extends CtorType[P, U] {

    override type This[-P, +U] = PropsAndChildren[P, U]

    override def applyGeneric(props: P)(children: ArgChild*): U =
      apply(props)(children: _*)

    def apply(props: P)(children: ArgChild*): U =
      construct(props, mods, children)

    override def addMod(f: ModFn): This[P, U] =
      new PropsAndChildren(construct, modAppend(mods, f))

    def cmapProps[P2](f: P2 => P): PropsAndChildren[P2, U] =
      new PropsAndChildren((p2, m, c) => construct(f(p2), m, c), mods)

    def withChildren(c: ArgChild*): Props[P, U] =
      new Props((p, m) => construct(p, m, c), mods)

    def withProps(p: P): Children[P, U] =
      new Children((m, c) => construct(p, m, c), mods)
  }


  final class Props[-P, +U](private[CtorType] val construct: (P, MaybeMod) => U,
                            private[CtorType] val mods: MaybeMod) extends CtorType[P, U] {

    override type This[-P, +U] = Props[P, U]

    override def applyGeneric(props: P)(children: ArgChild*): U =
      apply(props)

    def apply(props: P): U =
      construct(props, mods)

    override def addMod(f: ModFn): This[P, U] =
      new Props(construct, modAppend(mods, f))

    def cmapProps[P2](f: P2 => P): Props[P2, U] =
      new Props((p2, m) => construct(f(p2), m), mods)

    def withProps(p: P): Void[P, U] =
      new Void[P, U](construct(p, mods), m => construct(p, m), noMod)
  }


  final class Children[-P, +U](private[CtorType] val construct: (MaybeMod, ArgChildren) => U,
                               private[CtorType] val mods: MaybeMod) extends CtorType[P, U] {

    override type This[-P, +U] = Children[P, U]

    override def applyGeneric(props: P)(children: ArgChild*): U =
      apply(children: _*)

    def apply(children: ArgChild*): U =
      construct(mods, children)

    override def addMod(f: ModFn): This[P, U] =
      new Children(construct, modAppend(mods, f))

    def withChildren(c: ArgChild*): Void[P, U] =
      new Void[P, U](construct(mods, c), m => construct(m, c), noMod)
  }


  final class Void[-P, +U](private[CtorType] val unmodified: U,
                           private[CtorType] val construct: Mod => U,
                           private[CtorType] val mods: MaybeMod) extends CtorType[P, U] {

    override type This[-P, +U] = Void[P, U]

    override def applyGeneric(props: P)(children: ArgChild*): U =
      apply()

    def apply(): U =
      mods.fold(unmodified)(construct)

    override def addMod(f: ModFn): This[P, U] =
      new Void(unmodified, construct, modAppend(mods, f))
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // Functors

  implicit object ProfunctorF extends Profunctor[PropsAndChildren] {
    override def lmap[A, B, C](ct: PropsAndChildren[A, B])(f: C => A) =
      ct.cmapProps(f)

    override def rmap[A, B, C](ct: PropsAndChildren[A, B])(g: B => C) = {
      val fn = ct.construct
      new PropsAndChildren((p, m, c) => g(fn(p, m, c)), ct.mods)
    }

    override def dimap[A, B, C, D](ct: PropsAndChildren[A, B])(f: C => A, g: B => D) = {
      val fn = ct.construct
      new PropsAndChildren((p, m, c) => g(fn(f(p), m, c)), ct.mods)
    }
  }

  implicit object ProfunctorP extends Profunctor[Props] {
    override def lmap[A, B, C](ct: Props[A, B])(f: C => A) =
      ct.cmapProps(f)

    override def rmap[A, B, C](ct: Props[A, B])(g: B => C) = {
      val fn = ct.construct
      new Props((p, m) => g(fn(p, m)), ct.mods)
    }

    override def dimap[A, B, C, D](ct: Props[A, B])(f: C => A, g: B => D) = {
      val fn = ct.construct
      new Props((p, m) => g(fn(f(p), m)), ct.mods)
    }
  }

  implicit object ProfunctorC extends Profunctor[Children] {
    override def lmap[A, B, C](ct: Children[A, B])(f: C => A) =
      // new Children(ct.construct, ct.mods)
      ct.asInstanceOf[Children[C, B]]

    override def rmap[A, B, C](ct: Children[A, B])(g: B => C) = {
      val fn = ct.construct
      new Children((m, c) => g(fn(m, c)), ct.mods)
    }

    override def dimap[A, B, C, D](ct: Children[A, B])(f: C => A, g: B => D) = {
      val fn = ct.construct
      new Children((m, c) => g(fn(m, c)), ct.mods)
    }
  }

  implicit object ProfunctorV extends Profunctor[Void] {
    override def lmap [A, B, C   ](x: Void[A, B])(f: C => A)            = x.asInstanceOf[Void[C, B]]
    override def rmap [A, B, C   ](x: Void[A, B])(g: B => C)            = new Void(g(x.unmodified), g compose x.construct, x.mods)
    override def dimap[A, B, C, D](x: Void[A, B])(f: C => A, g: B => D) = new Void(g(x.unmodified), g compose x.construct, x.mods)
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // Summoner

  sealed trait Summoner[P <: js.Object, C <: ChildrenArg] {
    type CT[-p, +u] <: CtorType[p, u]
    final type Out = CT[P, raw.ReactComponentElement]
    val summon: raw.ReactCtor => Out
    implicit val pf: Profunctor[CT]
    final def aux: Summoner.Aux[P, C, CT] = this
  }

  object Summoner {
    type Aux[P <: js.Object, C <: ChildrenArg, T[-p, +u] <: CtorType[p, u]] =
      Summoner[P, C] {type CT[-p, +u] = T[p, u]}

    def apply[P <: js.Object, C <: ChildrenArg, T[-p, +u] <: CtorType[p, u]]
        (f: raw.ReactCtor => T[P, raw.ReactComponentElement])
        (implicit p: Profunctor[T]): Aux[P, C, T] =
      new Summoner[P, C] {
        override type CT[-p, +u] = T[p, u]
        override val summon = f
        override implicit val pf = p
      }

    implicit def summonV[P <: js.Object](implicit s: Singleton[P]) =
      Summoner[P, ChildrenArg.None, Void](rc =>
        new Void[P, raw.ReactComponentElement](
          raw.React.createElement(rc, s.value),
          m => raw.React.createElement(rc, m(s.mutable())),
          noMod))

    implicit def summonC[P <: js.Object](implicit s: Singleton[P]) =
      Summoner[P, ChildrenArg.Varargs, Children](rc =>
        new Children[P, raw.ReactComponentElement]((mm, c) => {
          val p = mm.fold[js.Object](s.value)(m => m(s.mutable()))
          raw.React.createElement(rc, p, formatChildren(c): _*)
        }, noMod))

    implicit def summonF[P <: js.Object](implicit w: Singleton.Not[P]) =
      Summoner[P, ChildrenArg.Varargs, PropsAndChildren](rc =>
        new PropsAndChildren[P, raw.ReactComponentElement]((p, mm, c) => {
          val p2 = mm.fold[js.Object](p)(m => m(clone(p)))
          raw.React.createElement(rc, p2, formatChildren(c): _*)
        }, noMod))

    implicit def summonP[P <: js.Object](implicit w: Singleton.Not[P]) =
      Summoner[P, ChildrenArg.None, Props](rc =>
        new Props[P, raw.ReactComponentElement]((p, mm) => {
            val p2 = mm.fold[js.Object](p)(m => m(clone(p)))
            raw.React.createElement(rc, p2)
          }, noMod))

    // This could be used to defensively-copy user-provided props before applying modifications (i.e. setting key/ref).
    // For Scala components, a new Box is created each time props are specified so that's fine to modify directly.
    // For other components, this should be considered.
    private def clone[P <: js.Object](p: P): P =
      p

    def formatChildren(c: ArgChildren): Seq[raw.ReactNodeList] =
      if (c.isEmpty)
        Nil
      else
        c.map(_.rawReactNode: raw.ReactNodeList)
  }
}

