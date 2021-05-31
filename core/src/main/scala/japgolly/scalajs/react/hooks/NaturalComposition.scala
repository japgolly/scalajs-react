package japgolly.scalajs.react.hooks

// TODO: Move into microlibs

object NaturalComposition {

  final case class Discardable[A](value: A) extends AnyVal
  implicit val discardableUnit: Discardable[Unit] = Discardable(())

  // ===================================================================================================================

  trait Merge[A, B] {
    type Out
    def merge: (A, B) => Out
  }

  trait Merge0 {
    implicit def fallback[A, B]: Merge.To[A, B, (A, B)] =
      Merge[A, B, (A, B)]((_, _))
  }

  trait Merge1 extends Merge0 {
    implicit def discardRight[A, B: Discardable]: Merge.To[A, B, A] =
      Merge[A, B, A]((a, _) => a)
  }

  trait Merge2 extends Merge1 {
    implicit def discardLeft[A: Discardable, B]: Merge.To[A, B, B] =
      Merge[A, B, B]((_, b) => b)
  }

  object Merge extends Merge2 {
    type To[A, B, O] = Merge[A, B] { type Out = O }

    def apply[A, B, O](f: (A, B) => O): To[A, B, O] =
      new Merge[A, B] {
        override type Out = O
        override def merge = f
      }

    implicit def sameSingleton[A <: Singleton]: To[A, A, A] =
      Merge[A, A, A]((a, _) => a)
  }

  // ===================================================================================================================

  trait Split[A, B] {
    type In
    def split: In => (A, B)
  }

  trait Split0 {
    implicit def fallback[A, B]: Split.To[(A, B), A, B] =
      Split[(A, B), A, B](identity)
  }

  trait Split1 extends Split0 {
    implicit def discardRight[A, B](implicit b: Discardable[B]): Split.To[A, A, B] =
      Split[A, A, B](a => (a, b.value))
  }

  trait Split2 extends Split1 {
    implicit def discardLeft[A, B](implicit a: Discardable[A]): Split.To[B, A, B] =
      Split[B, A, B](b => (a.value, b))
  }

  object Split extends Split2 {
    type To[I, A, B] = Split[A, B] { type In = I }

    def apply[I, A, B](g: I => (A, B)): To[I, A, B] =
      new Split[A, B] {
        override type In = I
        override def split = g
      }

    implicit def same[A]: To[A, A, A] =
      Split[A, A, A](a => (a, a))
  }

}