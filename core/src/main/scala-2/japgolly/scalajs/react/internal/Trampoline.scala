package japgolly.scalajs.react.internal

import scala.scalajs.js

sealed trait Trampoline[+A] {
  import Trampoline._

  // This is an inline forward to Trampoline.run so that this (and therefore it's entire graph of sub-trampolines)
  // aren't needlessly referenced, and can be garbage-collected during execution if need be.
  //
  // Does it matter? Am I even right in my analysis of the situation?? What wonderful questions.
  @inline final def run: A =
    Trampoline.run(this)

  final def map[B](f: A => B): Trampoline[B] =
    new FlatMap(this, f.andThen(new Pure(_)))

  final def flatMap[B](f: A => Trampoline[B]): Trampoline[B] =
    new FlatMap(this, f)
}

object Trampoline {

  val unit =
    pure(())

  def pure[A](a: A): Trampoline[A] =
    new Pure(a)

  def delay[A](a: () => A): Trampoline[A] =
    new Delay(a)

  def suspend[A](s: () => Trampoline[A]): Trampoline[A] =
    new Suspend(s)

  private[Trampoline] final class Pure   [+A]   (val value: A)                                       extends Trampoline[A]
  private[Trampoline] final class Delay  [+A]   (val value: () => A)                                 extends Trampoline[A]
  private[Trampoline] final class Suspend[+A]   (val suspension: () => Trampoline[A])                extends Trampoline[A]
  private[Trampoline] final class FlatMap[A, +B](val from: Trampoline[A], val f: A => Trampoline[B]) extends Trampoline[B]

  def run[A](initial: Trampoline[A]): A = {
    var next  = initial.asInstanceOf[Trampoline[Any]]
    val stack = new js.Array[Any => Trampoline[Any]]

    while (true) {
      next match {

        case t: FlatMap[_, _] =>
          stack.push(t.f.asInstanceOf[Any => Trampoline[Any]])
          next = t.from

        case t: Suspend[_] =>
          next = t.suspension()

        case t: Pure[_] =>
          if (stack.isEmpty)
            return t.value.asInstanceOf[A]
          else
            next = stack.pop()(t.value)

        case t: Delay[_] =>
          // Pretty much a copy-paste of above because I'm not sure I trust inlining here and I want everything here to
          // be as fast as possible.
          if (stack.isEmpty)
            return t.value().asInstanceOf[A]
          else
            next = stack.pop()(t.value())
      }
    }
    null.asInstanceOf[A]
  }
}
