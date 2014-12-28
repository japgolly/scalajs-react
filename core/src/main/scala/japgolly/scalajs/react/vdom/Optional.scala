package japgolly.scalajs.react.vdom

import scala.scalajs.js.UndefOr

trait Optional[T[_]] {
  def foreach[A](t: T[A])(f: A => Unit): Unit
  def fold[A, B](t: T[A], f: A => B, b: => B): B
}

object Optional {
  implicit val optionInstance: Optional[Option] = new Optional[Option] {
    @inline final override def foreach[A](t: Option[A])(f: (A) => Unit): Unit = t foreach f
    @inline final override def fold[A, B](t: Option[A], f: A => B, b: => B): B = t.fold(b)(f)
  }

  implicit val jsUndefOrInstance: Optional[UndefOr] = new Optional[UndefOr] {
    @inline final override def foreach[A](t: UndefOr[A])(f: (A) => Unit): Unit = t foreach f
    @inline final override def fold[A, B](t: UndefOr[A], f: A => B, b: => B): B = t.fold(b)(f)
  }
}