package japgolly.scalajs.react.internal

/**
  * Provide access to the modify function on any compatible optic.
  *
  * @tparam O The optic type.
  */
trait MonocleModifier[O[_, _, _, _]] {
  def modify[S, T, A, B](l: O[S, T, A, B]): (A => B) => S => T
}

object MonocleModifier {
  // Keep this import here so that Lens etc take priority over .internal
  import monocle._

  implicit object LensM extends MonocleModifier[PLens] {
    @inline final override def modify[S, T, A, B](l: PLens[S, T, A, B]) = l.modify
  }
  implicit object SetterM extends MonocleModifier[PSetter] {
    @inline final override def modify[S, T, A, B](l: PSetter[S, T, A, B]) = l.modify
  }
  implicit object OptionalM extends MonocleModifier[POptional] {
    @inline final override def modify[S, T, A, B](l: POptional[S, T, A, B]) = l.modify
  }
  implicit object IsoM extends MonocleModifier[PIso] {
    @inline final override def modify[S, T, A, B](l: PIso[S, T, A, B]) = l.modify
  }
  implicit object PrismM extends MonocleModifier[PPrism] {
    @inline final override def modify[S, T, A, B](l: PPrism[S, T, A, B]) = l.modify
  }
  implicit object TraversalM extends MonocleModifier[PTraversal] {
    @inline final override def modify[S, T, A, B](l: PTraversal[S, T, A, B]) = l.modify
  }
}
