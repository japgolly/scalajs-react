package japgolly.scalajs.react

import monocle._
import scalaz.Functor
import japgolly.scalajs.react.ScalazReact._

object MonocleReact {

  @inline implicit final def toMonRExtCompStateAccessOps[C[_]: CompStateAccess, S](c: C[S]) = new MonRExt_CompStateAccessOps(c)
  final class MonRExt_CompStateAccessOps[C[_], S](val _c: C[S]) extends AnyVal {
    // CompStateAccess[C] should really be a class param but then we lose the AnyVal
    type CC = CompStateAccess[C]

    @inline def focusStateL[T](l: Lens[S, T])(implicit C: CC) = new ComponentStateFocus[T](
      () => l get _c.state,
      (t: T, cb: OpCallback) => _c.modState(l set t, cb))
  }

  @inline implicit final class MonRExt_ReactSTOps[M[_], S, A](val _r: ReactST[M,S,A]) extends AnyVal {

    @inline def zoomL[T](l: Lens[T, S])(implicit M: Functor[M]): ReactST[M, T, A] =
      ReactS.zoom[M, S, T, A](_r, l.get, (a, b) => l.set(b)(a))
  }

  // Seriously, Scala, get your shit together.
  @inline final implicit def moarScalaHandHoldingMon[P,S](b: BackendScope[P,S]): MonRExt_CompStateAccessOps[ComponentScope_SS, S] = (b: ComponentScope_SS[S])
  @inline final implicit def moarScalaHandHoldingMon[P,S,B](b: ComponentScopeU[P,S,B]): MonRExt_CompStateAccessOps[ComponentScope_SS, S] = (b: ComponentScope_SS[S])
}