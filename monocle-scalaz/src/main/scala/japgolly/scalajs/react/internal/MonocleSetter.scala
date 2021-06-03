package japgolly.scalajs.react.internal

/**
  * Provide access to the set function on any compatible optic.
  *
  * @tparam O The optic type.
  */
trait MonocleSetter[O[_, _, _, _]] {
  def set[S, A, B](l: O[S, S, A, B]): B => S => S
}

object MonocleSetter {
  // Keep this import here so that Lens etc take priority over .internal
  import monocle._

  implicit object LensS extends MonocleSetter[PLens] {
    @inline final override def set[S, A, B](l: PLens[S, S, A, B]): B => S => S = l.set
  }
  implicit object SetterS extends MonocleSetter[PSetter] {
    @inline final override def set[S, A, B](l: PSetter[S, S, A, B]): B => S => S = l.set
  }
  implicit object OptionalS extends MonocleSetter[POptional] {
    @inline final override def set[S, A, B](l: POptional[S, S, A, B]): B => S => S = l.set
  }
  implicit object IsoS extends MonocleSetter[PIso] {
    @inline final override def set[S, A, B](l: PIso[S, S, A, B]): B => S => S = l.set
  }
  implicit object PrismS extends MonocleSetter[PPrism] {
    @inline final override def set[S, A, B](l: PPrism[S, S, A, B]): B => S => S = l.set
  }
  implicit object TraversalS extends MonocleSetter[PTraversal] {
    @inline final override def set[S, A, B](l: PTraversal[S, S, A, B]): B => S => S = l.set
  }
}
