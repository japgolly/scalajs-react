package japgolly.scalajs.react.vdom

import scala.scalajs.js.UndefOr

trait Optional[T[_]] {
  def foreach[A](t: T[A])(f: A => Unit): Unit
  def fold[A, B](t: T[A], b: => B)(f: A => B): B
  def isEmpty[A](t: T[A]): Boolean
}

object Optional {
  implicit val optionInstance: Optional[Option] = new Optional[Option] {
    @inline final override def foreach[A](t: Option[A])(f: (A) => Unit): Unit = t foreach f
    @inline final override def fold[A, B](t: Option[A], b: => B)(f: A => B): B = t.fold(b)(f)
    @inline final override def isEmpty[A](t: Option[A]): Boolean = t.isEmpty
  }

  implicit val jsUndefOrInstance: Optional[UndefOr] = new Optional[UndefOr] {
    @inline final override def foreach[A](t: UndefOr[A])(f: (A) => Unit): Unit = t foreach f
    @inline final override def fold[A, B](t: UndefOr[A], b: => B)(f: A => B): B = t.fold(b)(f)
    @inline final override def isEmpty[A](t: UndefOr[A]): Boolean = t.isEmpty
  }
}