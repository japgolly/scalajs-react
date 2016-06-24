package japgolly.scalajs.react.internal

/*
In a perfect world we'd use just Monocle publicly but unfortunately
- Monocle depends on Scalaz. Very unfair to push that and the JS size increase onto library users.
- Don't want to introduce dep-hell wrt Monocle or Scalaz.
 */

final class Iso[A, B](val get: A => B, val set: B => A) {
  val mod: (B => B) => A => A =
    set compose _ compose get

  def toLens: Lens[A, B] =
    Lens(get)(b => Function const set(b))
}

object Iso {
  @inline def apply[A, B](get: A => B)(set: B => A): Iso[A, B] =
    new Iso(get, set)

  def id[A]: Iso[A, A] = {
    val i: A => A = a => a
    apply(i)(i)
  }
}

// =====================================================================================================================

final class Lens[A, B](val get: A => B, val set: B => A => A) {

  val mod: (B => B) => A => A =
    f => a => set(f(get(a)))(a)

  def -->[C](next: Lens[B, C]): Lens[A, C] = {
    val nextSet = next.set
    Lens(next.get compose get)(mod compose nextSet)
  }

  def -->[C](next: Iso[B, C]): Lens[A, C] = {
    val nextSet = next.set
    Lens(next.get compose get)(set compose nextSet)
  }
}

object Lens {
//  @inline def apply[A, B](get: A => B)(set: (A, B) => A): Lens[A, B] =
//    new Lens(get, set)

  @inline def apply[A, B](get: A => B)(set: B => A => A): Lens[A, B] =
    new Lens(get, set)

  def id[A]: Lens[A, A] =
    apply[A, A](a => a)(a => _ => a)
}
