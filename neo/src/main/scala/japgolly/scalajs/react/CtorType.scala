package japgolly.scalajs.react

import scala.scalajs.js
import CtorType._

sealed abstract class CtorType[-P, +U] {
  type This[-p, +u] <: CtorType[p, u]

  def applyGeneric(props: P, key: ArgKey = js.undefined, ref: ArgRef = js.undefined)(children: ArgChild*): U

  def lmap[X](f: X => P): This[X, U]
  def rmap[X](f: U => X): This[P, X]
}

object CtorType {
  type ArgKey      = js.UndefOr[Key]
  type ArgRef      = js.UndefOr[Ref]
  type ArgChild    = raw.ReactNodeList
  type ArgChildren = Seq[ArgChild]

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // Types

  final case class PropsAndChildren[-P, +U](fn: (ArgKey, ArgRef, P, ArgChildren) => U) extends CtorType[P, U] {
    override type This[-p, +u] = PropsAndChildren[p, u]
    override def applyGeneric(p: P, k: ArgKey = js.undefined, r: ArgRef = js.undefined)(c: ArgChild*): U = fn(k, r, p, c)
    override def lmap[X](f: X => P): PropsAndChildren[X, U] = PropsAndChildren((k, r, p, c) => fn(k, r, f(p), c))
    override def rmap[X](f: U => X): PropsAndChildren[P, X] = PropsAndChildren((k, r, p, c) => f(fn(k, r, p, c)))
  }

  final case class Props[-P, +U](fn: (ArgKey, ArgRef, P) => U) extends CtorType[P, U] {
    override type This[-p, +u] = Props[p, u]
    override def applyGeneric(p: P, k: ArgKey = js.undefined, r: ArgRef = js.undefined)(c: ArgChild*): U = fn(k, r, p)
    override def lmap[X](f: X => P): Props[X, U] = Props((k, r, p) => fn(k, r, f(p)))
    override def rmap[X](f: U => X): Props[P, X] = Props((k, r, p) => f(fn(k, r, p)))
  }

  final case class Children[-P, +U](fn: (ArgKey, ArgRef, ArgChildren) => U) extends CtorType[P, U] {
    override type This[-p, +u] = Children[p, u]
    override def applyGeneric(p: P, k: ArgKey = js.undefined, r: ArgRef = js.undefined)(c: ArgChild*): U = fn(k, r, c)
    override def lmap[X](f: X => P): Children[X, U] = Children(fn)
    override def rmap[X](f: U => X): Children[P, X] = Children((k, r, c) => f(fn(k, r, c)))
  }

  final case class Void[-P, +U](fn: (ArgKey, ArgRef) => U, static: U) extends CtorType[P, U] {
    override type This[-p, +u] = Void[p, u]
    override def applyGeneric(p: P, k: ArgKey = js.undefined, r: ArgRef = js.undefined)(c: ArgChild*): U = fn(k, r)
    override def lmap[X](f: X => P): Void[X, U] = Void(fn, static)
    override def rmap[X](f: U => X): Void[P, X] = Void((k, r) => f(fn(k, r)), f(static))
  }

  def void[P, U](props: P)(newProps: (ArgKey, ArgRef) => P)(fn: P => U): Void[P, U] =
    Void((k, r) => fn(newProps(k, r)), fn(props))

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

