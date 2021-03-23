package japgolly.scalajs.react.internal

import scala.scalajs.js.UndefOr

trait OptionLike[O[_]] {
  def map     [A, B](o: O[A])(f: A => B)         : O[B]
  def fold    [A, B](o: O[A], b: => B)(f: A => B): B
  def foreach [A]   (o: O[A])(f: A => Unit)      : Unit
  def isEmpty [A]   (o: O[A])                    : Boolean
  def toOption[A]   (o: O[A])                    : Option[A]
}

//sealed trait OptionLikeLowPri {
//  Hmmmm....
//  implicit lazy val someInstance: OptionLike[Some] = new OptionLike[Some] {
//    type O[A] = Some[A]
//    def map     [A, B](o: O[A])(f: A => B)         : O[B]    = Some(f(o.value))
//    def fold    [A, B](o: O[A], b: => B)(f: A => B): B       = f(o.value)
//    def foreach [A]   (o: O[A])(f: A => Unit)      : Unit    = f(o.value)
//    def isEmpty [A]   (o: O[A])                    : Boolean = false
//    def toOption[A]   (o: O[A])                    : Some[A] = o
//  }
//}

object OptionLike {
  implicit val optionInstance: OptionLike[Option] = new OptionLike[Option] {
    type O[A] = Option[A]
    def map     [A, B](o: O[A])(f: A => B)         : O[B]      = o map f
    def fold    [A, B](o: O[A], b: => B)(f: A => B): B         = o.fold(b)(f)
    def foreach [A]   (o: O[A])(f: A => Unit)      : Unit      = o foreach f
    def isEmpty [A]   (o: O[A])                    : Boolean   = o.isEmpty
    def toOption[A]   (o: O[A])                    : Option[A] = o
  }

  implicit val jsUndefOrInstance: OptionLike[UndefOr] = new OptionLike[UndefOr] {
    type O[A] = UndefOr[A]
    def map     [A, B](o: O[A])(f: A => B)         : O[B]      = o map f
    def fold    [A, B](o: O[A], b: => B)(f: A => B): B         = o.fold(b)(f)
    def foreach [A]   (o: O[A])(f: A => Unit)      : Unit      = o foreach f
    def isEmpty [A]   (o: O[A])                    : Boolean   = o.isEmpty
    def toOption[A]   (o: O[A])                    : Option[A] = o.toOption
  }
}