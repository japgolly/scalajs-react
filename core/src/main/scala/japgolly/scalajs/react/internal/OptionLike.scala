package japgolly.scalajs.react.internal

import scala.scalajs.js

trait OptionLike[O[_]] {
  def map     [A, B](o: O[A])(f: A => B)         : O[B]
  def fold    [A, B](o: O[A], b: => B)(f: A => B): B
  def foreach [A]   (o: O[A])(f: A => Unit)      : Unit
  def isEmpty [A]   (o: O[A])                    : Boolean
  def toOption[A]   (o: O[A])                    : Option[A]

  /** This is "unsafe" because it can't represent `Some(())`. Use with care. */
  def unsafeToJs[A](o: O[A]): js.UndefOr[A]
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
    override def map       [A, B](o: O[A])(f: A => B)          = o map f
    override def fold      [A, B](o: O[A], b: => B)(f: A => B) = o.fold(b)(f)
    override def foreach   [A]   (o: O[A])(f: A => Unit)       = o foreach f
    override def isEmpty   [A]   (o: O[A])                     = o.isEmpty
    override def toOption  [A]   (o: O[A])                     = o
    override def unsafeToJs[A]   (o: O[A])                     = if (o.isEmpty) js.undefined else o.get
  }

  implicit val jsUndefOrInstance: OptionLike[js.UndefOr] = new OptionLike[js.UndefOr] {
    type O[A] = js.UndefOr[A]
    override def map       [A, B](o: O[A])(f: A => B)          = o map f
    override def fold      [A, B](o: O[A], b: => B)(f: A => B) = o.fold(b)(f)
    override def foreach   [A]   (o: O[A])(f: A => Unit)       = o foreach f
    override def isEmpty   [A]   (o: O[A])                     = o.isEmpty
    override def toOption  [A]   (o: O[A])                     = o.toOption
    override def unsafeToJs[A]   (o: O[A])                     = o
  }
}