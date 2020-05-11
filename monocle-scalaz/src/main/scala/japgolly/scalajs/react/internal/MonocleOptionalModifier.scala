package japgolly.scalajs.react.internal

import japgolly.scalajs.react.MonocleReact.{applicativeOption, functorOption}

/**
  * Provide access to the modify to Option function on any compatible optic.
  *
  * @tparam O The optic type.
  */
trait MonocleOptionalModifier[O[_, _, _, _]] {
  def modifyOption[S, T, A, B](l: O[S, T, A, B])(f: A => Option[B]): S => Option[T]
}

object MonocleOptionalModifier {
  // Keep this import here so that Lens etc take priority over .internal
  import monocle._

  implicit object LensM extends MonocleOptionalModifier[PLens] {
    @inline final override def modifyOption[S, T, A, B](l: PLens[S, T, A, B])(f: A => Option[B]) = l.modifyF(f)
  }
  implicit object OptionalM extends MonocleOptionalModifier[POptional] {
    @inline final override def modifyOption[S, T, A, B](l: POptional[S, T, A, B])(f: A => Option[B]) = l.modifyF(f)
  }
  implicit object IsoM extends MonocleOptionalModifier[PIso] {
    @inline final override def modifyOption[S, T, A, B](l: PIso[S, T, A, B])(f: A => Option[B]) = l.modifyF(f)
  }
  implicit object PrismM extends MonocleOptionalModifier[PPrism] {
    @inline final override def modifyOption[S, T, A, B](l: PPrism[S, T, A, B])(f: A => Option[B]) = l.modifyF(f)
  }
  implicit object TraversalM extends MonocleOptionalModifier[PTraversal] {
    @inline final override def modifyOption[S, T, A, B](l: PTraversal[S, T, A, B])(f: A => Option[B]) = l.modifyF(f)
  }
}
