package japgolly.scalajs.react.component

import japgolly.scalajs.react.vdom.VdomNode

class Delayed[+A](protected[react] val eval: () => A) extends AnyVal {
  def map[B](f: A => B): Delayed[B] =
    Delayed(f(eval()))

  def flatMap[B](f: A => Delayed[B]): Delayed[B] =
    Delayed(f(eval()).eval())
}

object Delayed {
  @inline def apply[A](a: => A): Delayed[A] =
    new Delayed(() => a)

  implicit class DelayedOps[I, O](private val h: I => Delayed[O]) extends AnyVal {
    def contramap[A](f: A => I): A => Delayed[O] = f andThen h
  }

  implicit def autoLift[A](a: => A)(implicit f: A => VdomNode): Delayed[VdomNode] =
    Delayed(f(a))
}

