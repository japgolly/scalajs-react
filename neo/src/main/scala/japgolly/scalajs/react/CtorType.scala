package japgolly.scalajs.react

import scala.scalajs.js
import CtorType._

sealed abstract class CtorType[-P, +U] {
  def applyGeneric(props: P, key: ArgKey = js.undefined, ref: ArgRef = js.undefined)(children: ArgChild*): U
}

object CtorType {
  type ArgKey      = js.UndefOr[Key]
  type ArgRef      = js.UndefOr[Ref]
  type ArgChild    = raw.ReactNodeList
  type ArgChildren = Seq[ArgChild]

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // Types

  final case class PropsAndChildren[-P, +U](fn: (ArgKey, ArgRef, P, ArgChildren) => U) extends CtorType[P, U] {
    override def applyGeneric(p: P, k: ArgKey = js.undefined, r: ArgRef = js.undefined)(c: ArgChild*): U = fn(k, r, p, c)
  }

  final case class Props[-P, +U](fn: (ArgKey, ArgRef, P) => U) extends CtorType[P, U] {
    override def applyGeneric(p: P, k: ArgKey = js.undefined, r: ArgRef = js.undefined)(c: ArgChild*): U = fn(k, r, p)
  }

  final case class Children[-P, +U](fn: (ArgKey, ArgRef, ArgChildren) => U) extends CtorType[P, U] {
    override def applyGeneric(p: P, k: ArgKey = js.undefined, r: ArgRef = js.undefined)(c: ArgChild*): U = fn(k, r, c)
  }

  final case class Void[-P, +U](fn: (ArgKey, ArgRef) => U, static: U) extends CtorType[P, U] {
    override def applyGeneric(p: P, k: ArgKey = js.undefined, r: ArgRef = js.undefined)(c: ArgChild*): U = fn(k, r)
  }

  def void[P, U](props: P)(newProps: (ArgKey, ArgRef) => P)(fn: P => U): Void[P, U] =
    Void((k, r) => fn(newProps(k, r)), fn(props))

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // Functors

  implicit object ProfunctorF extends Profunctor[PropsAndChildren] {
    override def lmap[A, B, C](ct: PropsAndChildren[A, B])(f: C => A) = {
      val fn = ct.fn
      PropsAndChildren((k, r, p, c) => fn(k, r, f(p), c))
    }
    override def rmap[A, B, C](ct: PropsAndChildren[A, B])(f: B => C) = {
      val fn = ct.fn
      PropsAndChildren((k, r, p, c) => f(fn(k, r, p, c)))
    }
    override def dimap[A, B, C, D](ct: PropsAndChildren[A, B])(f: C => A, g: B => D) = {
      val fn = ct.fn
      PropsAndChildren((k, r, p, c) => g(fn(k, r, f(p), c)))
    }
  }

  implicit object ProfunctorP extends Profunctor[Props] {
    override def lmap[A, B, C](ct: Props[A, B])(f: C => A) = {
      val fn = ct.fn
      Props((k, r, p) => fn(k, r, f(p)))
    }
    override def rmap[A, B, C](ct: Props[A, B])(f: B => C) = {
      val fn = ct.fn
      Props((k, r, p) => f(fn(k, r, p)))
    }
    override def dimap[A, B, C, D](ct: Props[A, B])(f: C => A, g: B => D) = {
      val fn = ct.fn
      Props((k, r, p) => g(fn(k, r, f(p))))
    }
  }

  implicit object ProfunctorC extends Profunctor[Children] {
    override def lmap[A, B, C](ct: Children[A, B])(f: C => A) =
      Children(ct.fn)
    override def rmap[A, B, C](ct: Children[A, B])(f: B => C) = {
      val fn = ct.fn
      Children((k, r, c) => f(fn(k, r, c)))
    }
    override def dimap[A, B, C, D](ct: Children[A, B])(f: C => A, g: B => D) = {
      val fn = ct.fn
      Children((k, r, c) => g(fn(k, r, c)))
    }
  }

  implicit object ProfunctorV extends Profunctor[Void] {
    override def lmap[A, B, C](ct: Void[A, B])(f: C => A) =
      Void(ct.fn, ct.static)
    override def rmap[A, B, C](ct: Void[A, B])(f: B => C) = {
      val fn = ct.fn
      Void((k, r) => f(fn(k, r)), f(ct.static))
    }
    override def dimap[A, B, C, D](ct: Void[A, B])(f: C => A, g: B => D) = {
      val fn = ct.fn
      Void((k, r) => g(fn(k, r)), g(ct.static))
    }
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // Ops

  final class OpsF[P, U](val ctor: PropsAndChildren[P, U]) extends AnyVal {
    @inline def apply(props: P)(children: ArgChild*): U =
      set()(props)(children: _*)

    def set(key: ArgKey = js.undefined, ref: ArgRef = js.undefined)(props: P)(children: ArgChild*): U =
      ctor.fn(key, ref, props, children)
  }

  final class OpsC[P, U](val ctor: Children[P, U]) extends AnyVal {
    @inline def apply(children: ArgChild*): U =
      set()(children: _*)

    def set(key: ArgKey = js.undefined, ref: ArgRef = js.undefined)(children: ArgChild*): U =
      ctor.fn(key, ref, children)
  }

  final class OpsP[P, U](val ctor: Props[P, U]) extends AnyVal {
    @inline def apply(props: P): U =
      set()(props)

    def set(key: ArgKey = js.undefined, ref: ArgRef = js.undefined)(props: P): U =
      ctor.fn(key, ref, props)
  }

  final class OpsV[U](val ctor: Void[_, U]) extends AnyVal {
    @inline def apply(): U =
      ctor.static

    def set(key: ArgKey = js.undefined, ref: ArgRef = js.undefined)(): U =
      if (key.isEmpty && ref.isEmpty)
        ctor.static
      else
        ctor.fn(key, ref)
  }
}

