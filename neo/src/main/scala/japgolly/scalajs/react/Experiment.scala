package experiment

import scala.language.reflectiveCalls

object TestUtil {
  trait X
  trait Y
  trait O
  def xy: X => Y = null
  def yx: Y => X = null

  def test[A] = new {
    def apply[B](f: A => B) = new {
      def expect[C](implicit ev: B =:= C): Unit = ()
      def expect_<[C](implicit ev: B <:< C): Unit = ()
      def expect_>[C](implicit ev: C <:< B): Unit = ()
    }
    def expect_<[C](implicit ev: A <:< C): Unit = ()
    def expect_>[C](implicit ev: C <:< A): Unit = ()
    def usableAs[B](implicit ev: A => B): Unit = ()
  }
}
import TestUtil._

// Implemented so far:
// - Multiple component types
// - Component props exist
// - Component props mappable

/*
Generic
- map → Generic'
- props

Js < Generic
- map → Js'
- props
- rawThing
*/

// =====================================================================================================================
object `ops in typeclasses (shared Mapped)` {
  trait GenOps[P] {
    def props: P
  }
  trait JsOps[P] extends GenOps[P] {
    def rawSomething: P
  }
  trait MapOps[P, P0, U, Ops[x] <: GenOps[x]] {
    def map[A](f: P => A): Mapped[A, P0, U, Ops]
  }

  trait Js[P]

  case class Mapped[P, P0, U, Ops[x] <: GenOps[x]](underlying: U, mapP: P0 => P, ops: Ops[P])
                                                  (implicit val uops: U => Ops[P0])

  implicit def toMappedOps[P, P0, U, Ops[x] <: GenOps[x]](m: Mapped[P, P0, U, Ops]): Ops[P] =
    m.ops

  implicit def toJsOps[P](js: Js[P]): JsOps[P] =
    new JsOps[P] {
      override def props = ???
      override def rawSomething = ???
    }

  implicit def toJsMapOps[P](u: Js[P]): MapOps[P, P, Js[P], JsOps] =
    new MapOps[P, P, Js[P], JsOps] {
      override def map[A](f: P => A) =
        Mapped[A, P, Js[P], JsOps](u, f, _mappedJsOps(f, u))
    }

  implicit def toJsMapOps2[P, P0](u: Mapped[P, P0, Js[P0], JsOps]): MapOps[P, P0, Js[P0], JsOps] =
    new MapOps[P, P0, Js[P0], JsOps] {
      override def map[A](f: P => A) =
        Mapped[A, P0, Js[P0], JsOps](u.underlying, f compose u.mapP, _mappedJsOps(f, u))
    }

  def _mappedJsOps[A, P](f: P => A, m: JsOps[P]): JsOps[A] =
    new JsOps[A] {
      override def props = f(m.props)
      override def rawSomething = f(m.rawSomething)
    }

  test[Js[X]](_.props).expect[X]
  test[Js[X]](_ map xy).expect[Mapped[Y, X, Js[X], JsOps]]
  test[Mapped[X, O, Js[O], JsOps]](_.props).expect[X]
  test[Mapped[X, O, Js[O], JsOps]](_ map xy).expect[Mapped[Y, O, Js[O], JsOps]]

  // Problem 1.
  // implicit toMappedOps cant create ops instances because the ops is polymorphic
  // Ops have to be embedded in the component

  // Problem 2.
  // Generic ops classes can't define map() because there is no generic component representation.
}

// =====================================================================================================================
object Subtyping {

  trait Generic[P] {
    def props: P
    def map[A](f: P => A): Generic[A]
  }

  trait GenericMapped[P, P0] extends Generic[P] {
    val underlying: Generic[P0]
    val mapP: P0 => P
    override final def props = mapP(underlying.props)
    override def map[A](f: P => A): Generic[A] with GenericMapped[A, P0]
  }

  trait JsLike[P, P0] extends Generic[P] {
    def rawThing: P
    override def map[A](f: P => A): JsMapped[A, P0]
  }

  final case class Js[P]() extends JsLike[P, P] {
    def props = ???
    def rawThing = ???
    override def map[A](f: P => A) = JsMapped(this, f)
  }

  case class JsMapped[P, P0](underlying: Js[P0], mapP: P0 => P) extends JsLike[P, P0] with GenericMapped[P, P0] {
    override def rawThing = mapP(underlying.rawThing)
    override def map[A](f: P => A) = JsMapped[A, P0](underlying, f compose mapP)
  }

  test[Js[X]](_.props).expect[X]
  test[Js[X]](_ map xy).expect[JsMapped[Y, X]]
  test[JsMapped[X, O]](_.props).expect[X]
  test[JsMapped[X, O]](_ map xy).expect[JsMapped[Y, O]]
}

// =====================================================================================================================
object `single trait per family, type alias for non-mapped case` {

  type Generic[P] = Generic0[P, P]
  trait Generic0[P, P0] {
    def props: P
    def map[A](f: P => A): Generic0[A, P0]
  }

  type Js[P] = Js0[P, P]
  sealed trait Js0[P, P0] extends Generic0[P, P0] {
    def underlying: Js[P0]
    def rawThing: P

    override def map[A](f: P => A): Js0[A, P0] = {
      val self = this
      new Js0[A, P0] {
        override def underlying = self.underlying
        override def props = f(self.props)
        override def rawThing = f(self.rawThing)
      }
    }
  }

  def js[P]: Js[P] =
    new Js0[P, P] {
      def underlying = this
      def props: P = ???
      def rawThing: P = ???
    }

  test[Js[X]](_.props).expect[X]
  test[Js[X]](_ map xy).expect[Js0[Y, X]]
  test[Js0[X, O]](_.props).expect[X]
  test[Js0[X, O]](_ map xy).expect[Js0[Y, O]]
  test[Generic0[X, O]](_.props).expect[X]
  test[Generic0[X, O]](_ map xy).expect[Generic0[Y, O]]
}

// =====================================================================================================================
// Interface that can map its types whilst providing access to the unmapped instance
// - Easiest is a single class per instance impl that has mapped & unmapped types. One repr for everything.
// - 3 classes per instance (base, unmapped, mapped) is also possible but verbose and makes usage noisy.
// - 1 class per impl + 1 shared Mapped class with ops typeclasses is terrible
//   - it takes a matrix of class/ops to provide ops `implicit def`s.
//   - mapped needs a lot of type complexity to retain unmapped bases.
// Regardless of the strategy, map() cannot (simply) be written once to cover all impl cases. Each impl's map() is impl-specific.
