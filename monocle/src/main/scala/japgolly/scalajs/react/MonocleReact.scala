package japgolly.scalajs.react

import monocle._
import scalaz.Monad
import extra._
import ScalazReact._
import CompState._
import CompScope._

object MonocleReact extends MonocleReactExtra {

  @inline implicit def MonocleReactCompStateOpsDD[$, S]($: $)(implicit ops: $ => ReadDirectWriteDirectOps[S]) =
    new MonocleReactCompStateOps[ReadDirectWriteDirectOps[S], S, Unit](ops($))

  @inline implicit def MonocleReactCompStateOpsDC[$, S]($: $)(implicit ops: $ => ReadDirectWriteCallbackOps[S]) =
    new MonocleReactCompStateOps[ReadDirectWriteCallbackOps[S], S, Callback](ops($))

  @inline implicit def MonocleReactCompStateOpsCC[$, S]($: $)(implicit ops: $ => ReadCallbackWriteCallbackOps[S]) =
    new MonocleReactCompStateOps[ReadCallbackWriteCallbackOps[S], S, Callback](ops($))

  final class MonocleReactCompStateOps[Ops <: WriteOpAux[S, W], S, W](private val $: Ops) extends AnyVal {

    def modStateL[A, B](l: PLens[S, S, A, B])(f: A => B, cb: Callback = Callback.empty): W =
      $.modState(l.modify(f), cb)

    def setStateL[L[_, _, _, _], B](l: L[S, S, _, B])(b: B, cb: Callback = Callback.empty)(implicit L: SetterMonocle[L]): W =
      $.modState(L.set(l)(b), cb)

    def _setStateL[L[_, _, _, _], B](l: L[S, S, _, B], cb: Callback = Callback.empty)(implicit L: SetterMonocle[L]): B => W =
      setStateL(l)(_, cb)
  }

  @inline implicit def MonocleReactCompStateZoomOpsDD[$, S]($: $)(implicit ops: $ => ReadDirectWriteDirectOps[S]) =
    new MonocleReactCompStateZoomOps[ReadDirectWriteDirectOps[S], S](ops($))

  @inline implicit def MonocleReactCompStateZoomOpsDC[$, S]($: $)(implicit ops: $ => ReadDirectWriteCallbackOps[S]) =
    new MonocleReactCompStateZoomOps[ReadDirectWriteCallbackOps[S], S](ops($))

  @inline implicit def MonocleReactCompStateZoomOpsCC[$, S]($: $)(implicit ops: $ => ReadCallbackWriteCallbackOps[S]) =
    new MonocleReactCompStateZoomOps[ReadCallbackWriteCallbackOps[S], S](ops($))

  final class MonocleReactCompStateZoomOps[Ops <: ZoomOps[S], S](private val $: Ops) extends AnyVal {
    def zoomL[T](l: Lens[S, T]): Ops#This[T] =
      $.zoom(l.get)((s, t) => l.set(t)(s))
  }

  @inline implicit final class MonocleReactReactSTOps[M[_], S, A](private val s: ReactST[M, S, A]) extends AnyVal {
    def zoomL[T](l: Lens[T, S])(implicit M: Monad[M]): ReactST[M, T, A] =
      ReactS.zoom[M, S, T, A](s, l.get, (a, b) => l.set(b)(a))
  }

  @inline implicit final class MonocleReactExternalVarObjOps(private val x: ExternalVar.type) extends AnyVal {
    def at[S, A](lens: Lens[S, A])(s: S, $: CompState.WriteAccess[S]): ExternalVar[A] =
      new ExternalVar(lens get s, $ modState lens.set(_))
  }

  @inline implicit final class MonocleReactReusableVarObjOps(private val x: ReusableVar.type) extends AnyVal {
    def at[S, A: Reusability](lens: Lens[S, A])(s: S, $: CompState.WriteAccess[S]): ReusableVar[A] =
      new ReusableVar[A](lens get s, ReusableFn($ modState lens.set(_)))
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