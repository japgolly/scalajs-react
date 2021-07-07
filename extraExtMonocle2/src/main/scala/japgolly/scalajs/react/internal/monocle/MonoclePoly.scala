package japgolly.scalajs.react.internal.monocle

import japgolly.scalajs.react.internal.monocle.CatsInstances._

// ===================================================================================================================

/** Provide access to the set function on any compatible optic.
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

// ===================================================================================================================

/** Provide access to the modify to Option function on any compatible optic.
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

// ===================================================================================================================

/** Provide access to the modify function on any compatible optic.
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
