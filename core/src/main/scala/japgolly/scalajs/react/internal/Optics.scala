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
  def apply[A, B](get: A => B)(set: B => A): Iso[A, B] =
    new Iso(get, set)

  private def _id[A]: Iso[A, A] = {
    val i: A => A = a => a
    apply(i)(i)
  }

  private val idInstance = _id[Any]

  def id[A]: Iso[A, A] =
    idInstance.asInstanceOf[Iso[A, A]]
}

// =====================================================================================================================

final class Lens[A, B](val get: A => B, val set: B => A => A) {

  def setO(o: Option[B]): A => Option[A] =
    o.fold[A => Option[A]](_ => None)(set(_).andThen(Some.apply))

  val mod: (B => B) => A => A =
    f => a => set(f(get(a)))(a)

  def modO(f: B => Option[B]): A => Option[A] =
    a => f(get(a)).map(set(_)(a))

  def -->[C](next: Lens[B, C]): Lens[A, C] =
    Lens(next.get compose get)(mod compose next.set)

  def -->[C](next: Iso[B, C]): Lens[A, C] =
    Lens(next.get compose get)(set compose next.set)
}

object Lens {
  def apply[A, B](get: A => B)(set: B => A => A): Lens[A, B] =
    new Lens(get, set)

  private def _id[A]: Lens[A, A] =
    apply[A, A](a => a)(a => _ => a)

  private val idInstance = _id[Any]

  def id[A]: Lens[A, A] =
    idInstance.asInstanceOf[Lens[A, A]]
}
