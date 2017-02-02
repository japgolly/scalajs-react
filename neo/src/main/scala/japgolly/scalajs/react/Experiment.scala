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
// - Component props exist
// - Component props mappable

// =====================================================================================================================
object `ops in typeclasses` {

  trait Comp[P]

  case class Mapped[P, P0](underlying: Comp[P0], mapP: P0 => P)

  trait Ops[P] {
    def props: P
  }

  trait MapOps[P, P0] {
    def map[A](f: P => A): Mapped[A, P0]
  }

  implicit def opsSimple[P](c: Comp[P]): Ops[P] with MapOps[P, P] =
    new Ops[P] with MapOps[P, P] {
      override def props = ???
      override def map[A](f: P => A): Mapped[A, P] =
        Mapped(c, f)
    }

  implicit def opsMapped[P, P0](c: Mapped[P, P0]): Ops[P] with MapOps[P, P0] =
    new Ops[P] with MapOps[P, P0] {
      override def props = c.mapP(c.underlying.props)
      override def map[A](f: P => A): Mapped[A, P0] =
        Mapped(c.underlying, f compose c.mapP)
    }

  test[Comp[X]](_.props).expect[X]
  test[Comp[X]](_ map xy).expect[Mapped[Y, X]]
  test[Mapped[X, O]](_.props).expect[X]
  test[Mapped[X, O]](_ map xy).expect[Mapped[Y, O]]
}

// =====================================================================================================================
object Subtyping {

  trait Ops[P] {
    def props: P
    def map[A](f: P => A): Ops[A]
  }

  trait Comp[P] extends Ops[P] {
    override def map[A](f: P => A): Mapped[A, P] =
      Mapped(this, f)
    override def props: P =
      ???
  }

  case class Mapped[P, P0](underlying: Comp[P0], mapP: P0 => P) extends Ops[P] {
    override def map[A](f: P => A): Mapped[A, P0] =
      Mapped(underlying, f compose mapP)
    override def props: P =
      mapP(underlying.props)
  }

  test[Comp[X]](_.props).expect[X]
  test[Comp[X]](_ map xy).expect[Mapped[Y, X]]
  test[Mapped[X, O]](_.props).expect[X]
  test[Mapped[X, O]](_ map xy).expect[Mapped[Y, O]]
  test[Ops[X]](_.props).expect[X]
  // test[Ops[X]](_ map xy).expect[Mapped[Y, X]]
}

// =====================================================================================================================
object `single trait type alias` {

  type Simple[P] = Comp[P, P]

  sealed trait Comp[P, P0] {
    def props: P

    def map[A](f: P => A): Comp[A, P0] = {
      val self = this
      new Comp[A, P0] {
        override def underlying = self.underlying
        override def props = f(self.props)
        // override def mapP = f compose self.mapP
      }
    }

    def underlying: Simple[P0]
    // def mapP: P0 => P
  }

  def simple[P]: Simple[P] =
    new Comp[P, P] {
      def props: P = ???
      def underlying = this
    }

  test[Simple[X]](_.props).expect[X]
  test[Simple[X]](_ map xy).expect[Comp[Y, X]]
  test[Comp[X, O]](_.props).expect[X]
  test[Comp[X, O]](_ map xy).expect[Comp[Y, O]]
}

// =====================================================================================================================
// Subtyping problems
// - map() in the base class.
// - map() result type isn't Mapped. Either needs a new type param (P0).
// SingleTrait doesn't need to expose mapP.
