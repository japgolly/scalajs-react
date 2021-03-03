package japgolly.scalajs.react.internal

/**
  * Provide access to the replace function on any compatible optic.
  *
  * @tparam O The optic type.
  */
trait MonocleReplacer[O[_, _, _, _]] {
  def replace[S, B](l: O[S, S, _, B]): B => S => S
}

object MonocleReplacer {
  // Keep this import here so that Lens etc take priority over .internal
  import monocle._

  implicit object LensS extends MonocleReplacer[PLens] {
    @inline final override def replace[S, B](l: PLens[S, S, _, B]): B => S => S = l.replace
  }
  implicit object SetterS extends MonocleReplacer[PSetter] {
    @inline final override def replace[S, B](l: PSetter[S, S, _, B]): B => S => S = l.replace
  }
  implicit object OptionalS extends MonocleReplacer[POptional] {
    @inline final override def replace[S, B](l: POptional[S, S, _, B]): B => S => S = l.replace
  }
  implicit object IsoS extends MonocleReplacer[PIso] {
    @inline final override def replace[S, B](l: PIso[S, S, _, B]): B => S => S = l.replace
  }
  implicit object PrismS extends MonocleReplacer[PPrism] {
    @inline final override def replace[S, B](l: PPrism[S, S, _, B]): B => S => S = l.replace
  }
  implicit object TraversalS extends MonocleReplacer[PTraversal] {
    @inline final override def replace[S, B](l: PTraversal[S, S, _, B]): B => S => S = l.replace
  }
}
