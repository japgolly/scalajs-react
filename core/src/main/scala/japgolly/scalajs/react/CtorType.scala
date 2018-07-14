package japgolly.scalajs.react

import scala.scalajs.js
import japgolly.scalajs.react.internal._
import CtorType._

/** Describes how a component uses `.props.children`. */
sealed trait Children
object Children {

  /** The component doesn't use `.props.children`. */
  sealed trait None extends Children

  /** The component uses `.props.children`: 0-n children are acceptable. */
  sealed trait Varargs extends Children
}

sealed abstract class CtorType[-P, +U] {
  type This[-P, +U] <: CtorType[P, U]

  type ChildrenType <: Children

  def applyGeneric(props: P)(children: ChildArg*): U

  // This should really be on Children but I don't want to deal with types -> terms right now
  def liftChildren(r: raw.PropsChildren): ChildrenArgs = {
    import japgolly.scalajs.react.vdom.Implicits._
    (PropsChildren(r): vdom.VdomNode) :: Nil
  }

  def addMod(f: ModFn): This[P, U]

  final def withRawProp(name: String, value: js.Any): This[P, U] =
    addMod(_.asInstanceOf[js.Dynamic].updateDynamic(name)(value))

  final def withKey(k: Key): This[P, U] =
    withRawProp("key", k.asInstanceOf[js.Any])

  final def withKey(k: Long): This[P, U] =
    withKey(k.toString)
}

object CtorType {
  type ChildArg     = vdom.VdomNode
  type ChildrenArgs = Seq[ChildArg]

  type ModFn = js.Object => Unit

  private type MaybeMod = js.UndefOr[Mod]
  @inline private def noMod: MaybeMod = js.undefined

  private final case class Mod(mod: ModFn) extends AnyVal {
    def applyAndCast[P <: js.Object](o: js.Object): P = {
      mod(o)
      o.asInstanceOf[P]
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

  final class PropsAndChildren[-P, +U](private[CtorType] val construct: (P, MaybeMod, ChildrenArgs) => U,
                                       private[CtorType] val mods: MaybeMod) extends CtorType[P, U] {

    override type This[-P, +U] = PropsAndChildren[P, U]
    override type ChildrenType = Children.Varargs

    override def applyGeneric(props: P)(children: ChildArg*): U =
      apply(props)(children: _*)

    def apply(props: P)(children: ChildArg*): U =
      construct(props, mods, children)

    override def addMod(f: ModFn): This[P, U] =
      new PropsAndChildren(construct, modAppend(mods, f))

    def cmapProps[P2](f: P2 => P): PropsAndChildren[P2, U] =
      new PropsAndChildren((p2, m, c) => construct(f(p2), m, c), mods)

    def withChildren(c: ChildArg*): Props[P, U] =
      new Props((p, m) => construct(p, m, c), mods)

    def withProps(p: P): Children[P, U] =
      new Children((m, c) => construct(p, m, c), mods)
  }


  final class Props[-P, +U](private[CtorType] val construct: (P, MaybeMod) => U,
                            private[CtorType] val mods: MaybeMod) extends CtorType[P, U] {

    override type This[-P, +U] = Props[P, U]
    override type ChildrenType = Children.None

    override def applyGeneric(props: P)(children: ChildArg*): U =
      apply(props)

    override def liftChildren(r: raw.PropsChildren): ChildrenArgs =
      Nil

    def apply(props: P): U =
      construct(props, mods)

    override def addMod(f: ModFn): This[P, U] =
      new Props(construct, modAppend(mods, f))

    def cmapProps[P2](f: P2 => P): Props[P2, U] =
      new Props((p2, m) => construct(f(p2), m), mods)

    def withProps(p: P): Nullary[P, U] =
      new Nullary[P, U](construct(p, mods), m => construct(p, m), noMod)
  }


  final class Children[-P, +U](private[CtorType] val construct: (MaybeMod, ChildrenArgs) => U,
                               private[CtorType] val mods: MaybeMod) extends CtorType[P, U] {

    override type This[-P, +U] = Children[P, U]
    override type ChildrenType = Children.Varargs

    override def applyGeneric(props: P)(children: ChildArg*): U =
      apply(children: _*)

    def apply(children: ChildArg*): U =
      construct(mods, children)

    override def addMod(f: ModFn): This[P, U] =
      new Children(construct, modAppend(mods, f))

    def withChildren(c: ChildArg*): Nullary[P, U] =
      new Nullary[P, U](construct(mods, c), m => construct(m, c), noMod)
  }


  final class Nullary[-P, +U](private[CtorType] val unmodified: U,
                              private[CtorType] val construct: Mod => U,
                              private[CtorType] val mods: MaybeMod) extends CtorType[P, U] {

    override type This[-P, +U] = Nullary[P, U]
    override type ChildrenType = Children.None

    override def applyGeneric(props: P)(children: ChildArg*): U =
      apply()

    override def liftChildren(r: raw.PropsChildren): ChildrenArgs =
      Nil

    def apply(): U =
      mods.fold(unmodified)(construct)

    override def addMod(f: ModFn): This[P, U] =
      new Nullary(unmodified, construct, modAppend(mods, f))
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

  implicit object ProfunctorN extends Profunctor[Nullary] {
    override def lmap [A, B, C   ](x: Nullary[A, B])(f: C => A)            = x.asInstanceOf[Nullary[C, B]]
    override def rmap [A, B, C   ](x: Nullary[A, B])(g: B => C)            = new Nullary(g(x.unmodified), g compose x.construct, x.mods)
    override def dimap[A, B, C, D](x: Nullary[A, B])(f: C => A, g: B => D) = new Nullary(g(x.unmodified), g compose x.construct, x.mods)
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // Summoner

  import japgolly.scalajs.react.{Children => ChildrenArg}

  sealed trait Summoner[P <: js.Object, C <: ChildrenArg] {
    type CT[-p, +u] <: CtorType[p, u]
    final type Out = CT[P, raw.React.ComponentElement[P]]
    val summon: raw.React.ComponentType[P] => Out
    implicit val pf: Profunctor[CT]
    final def aux: Summoner.Aux[P, C, CT] = this
  }

  object Summoner {
    type Aux[P <: js.Object, C <: ChildrenArg, T[-p, +u] <: CtorType[p, u]] =
      Summoner[P, C] {type CT[-p, +u] = T[p, u]}

    def apply[P <: js.Object, C <: ChildrenArg, T[-p, +u] <: CtorType[p, u]]
        (f: raw.React.ComponentType[P] => T[P, raw.React.ComponentElement[P]])
        (implicit p: Profunctor[T]): Aux[P, C, T] =
      new Summoner[P, C] {
        override type CT[-p, +u] = T[p, u]
        override val summon = f
        override implicit val pf = p
      }

    implicit def summonN[P <: js.Object](implicit s: Singleton[P]) =
      Summoner[P, ChildrenArg.None, Nullary](rc =>
        new Nullary[P, raw.React.ComponentElement[P]](
          raw.React.createElement(rc, s.value),
          m => raw.React.createElement[P](rc, m.applyAndCast[P](s.mutableObj())),
          noMod))

    implicit def summonC[P <: js.Object](implicit s: Singleton[P]) =
      Summoner[P, ChildrenArg.Varargs, Children](rc =>
        new Children[P, raw.React.ComponentElement[P]]((mm, c) => {
          val p = mm.fold(s.value)(_.applyAndCast[P](s.mutableObj()))
          raw.React.createElement[P](rc, p, formatChildren(c): _*)
        }, noMod))

    implicit def summonPC[P <: js.Object](implicit w: Singleton.Not[P]) =
      Summoner[P, ChildrenArg.Varargs, PropsAndChildren](rc =>
        new PropsAndChildren[P, raw.React.ComponentElement[P]]((p, mm, c) => {
          val p2 = mm.fold(p)(_.applyAndCast[P](prepareForMutation(p)))
          raw.React.createElement[P](rc, p2, formatChildren(c): _*)
        }, noMod))

    implicit def summonP[P <: js.Object](implicit w: Singleton.Not[P]) =
      Summoner[P, ChildrenArg.None, Props](rc =>
        new Props[P, raw.React.ComponentElement[P]]((p, mm) => {
            val p2 = mm.fold(p)(_.applyAndCast[P](prepareForMutation(p)))
            raw.React.createElement[P](rc, p2)
          }, noMod))

    // This could be used to defensively-copy user-provided props before applying modifications (i.e. setting key/ref).
    // For Scala components, a new Box is created each time props are specified so that's fine to modify directly.
    // For other components, this should be considered.
    private def prepareForMutation(o: js.Object): js.Object =
      if (o.isInstanceOf[js.Object]) o else new js.Object

    def formatChildren(c: ChildrenArgs): Seq[raw.React.Node] =
      if (c.isEmpty)
        Nil
      else
        c.map(_.rawNode)
  }
}

