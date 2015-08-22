package japgolly.scalajs.react

import monocle._
import scalaz.Functor
import ScalazReact._

object MonocleReact extends MonocleReactExtra {

  @inline implicit def MonocleReactCompStateAccessOps[C, S](c: C)(implicit a: CompStateAccess[C, S]) =
    new MonocleReactCompStateAccessOps[C, S](c)

  final class MonocleReactCompStateAccessOps[C, S](private val c: C) extends AnyVal {
    // This should really be a class param but then we lose the AnyVal
    type CC = CompStateAccess[C, S]

    def zoomL[T](l: Lens[S, T])(implicit C: CC) = new CompStateFocus[T](
      () => l get c.state,
      (t: T, cb: Callback) => c.modState(l set t, cb))

    @inline def _setStateL[L[_, _, _, _], B](l: L[S, S, _, B])(implicit C: CC, L: SetterMonocle[L]): B => Callback =
      c._modState(L set l)
  }

  @inline implicit final class MonocleReactReactSTOps[M[_], S, A](private val s: ReactST[M, S, A]) extends AnyVal {
    @inline def zoomL[T](l: Lens[T, S])(implicit M: Functor[M]): ReactST[M, T, A] =
      ReactS.zoom[M, S, T, A](s, l.get, (a, b) => l.set(b)(a))
  }
}

/**
 * Provide access to the set function on any compatible optic.
 * @tparam O The optic type.
 */
trait SetterMonocle[O[_, _, _, _]] {
  def set[S, B](l: O[S, S, _, B]): B => S => S
}
object SetterMonocle {
  implicit object LensS extends SetterMonocle[PLens] {
    @inline final override def set[S, B](l: PLens[S, S, _, B]): B => S => S = l.set
  }
  implicit object SetterS extends SetterMonocle[PSetter] {
    @inline final override def set[S, B](l: PSetter[S, S, _, B]): B => S => S = l.set
  }
  implicit object OptionalS extends SetterMonocle[POptional] {
    @inline final override def set[S, B](l: POptional[S, S, _, B]): B => S => S = l.set
  }
  implicit object IsoS extends SetterMonocle[PIso] {
    @inline final override def set[S, B](l: PIso[S, S, _, B]): B => S => S = l.set
  }
  implicit object PrismS extends SetterMonocle[PPrism] {
    @inline final override def set[S, B](l: PPrism[S, S, _, B]): B => S => S = l.set
  }
  implicit object TraversalS extends SetterMonocle[PTraversal] {
    @inline final override def set[S, B](l: PTraversal[S, S, _, B]): B => S => S = l.set
  }
}