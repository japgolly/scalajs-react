package japgolly.scalajs.react

import monocle._
import extra.router.StaticDsl.{RouteCommon, Rule}
import extra._

private[react] object MonocleReactExtra {

  final class ReusableFnEndoOps[E, B](private val s: (E => E) ~=> B) extends AnyVal {
    def endoZoomL[I](l: Lens[E, I]): I ~=> B =
      s contramap l.set
  }

  final class ExternalVarOps[A](private val v: ExternalVar[A]) extends AnyVal {
    def setL[B](l: Lens[A, B]): B => Callback =
      b => v.set(l.set(b)(v.value))

    def modL[B](l: Lens[A, B])(f: B => B): Callback =
      v.set(l.modify(f)(v.value))
  }

  final class ReusableVarOps[A](private val v: ReusableVar[A]) extends AnyVal {
    def setL[B](l: Lens[A, B]): B => Callback =
      b => v.set(l.set(b)(v.value))

    def modL[B](l: Lens[A, B])(f: B => B): Callback =
      v.set(l.modify(f)(v.value))
  }

  final class RouteCommonOps[R[X] <: RouteCommon[R, X], A](private val r: RouteCommon[R, A]) extends AnyVal {
    def pmapL[B](l: Prism[A, B]): R[B] =
      r.pmap(l.getOption)(l.reverseGet)

    def xmapL[B](l: Iso[A, B]): R[B] =
      r.xmap(l.get)(l.reverseGet)
  }

  final class RuleOps[Page](private val r: Rule[Page]) extends AnyVal {
    def xmapL[A](l: Iso[Page, A]): Rule[A] =
      r.xmap(l.get)(l.reverseGet)

    def pmapL[W](l: Prism[W, Page]): Rule[W] =
      r.pmapF(l.reverseGet)(l.getOption)
  }
}

abstract class MonocleReactExtra {
  import MonocleReactExtra._

  @inline implicit def MonocleReactReusableFnEndoOps[E, B](s: (E => E) ~=> B) = new ReusableFnEndoOps(s)

  @inline implicit def MonocleReactExternalVarOps[A](v: ExternalVar[A]) = new ExternalVarOps(v)
  @inline implicit def MonocleReactReusableVarOps[A](v: ReusableVar[A]) = new ReusableVarOps(v)

  @inline implicit def MonocleReactRouteCommonOps[R[X] <: RouteCommon[R, X], A](r: RouteCommon[R, A]) = new RouteCommonOps(r)
  @inline implicit def MonocleReactRuleOps[P](r: Rule[P]) = new RuleOps(r)


  import MonocleReact._
  import CompState._

  @inline implicit def MonocleReactCompStateZoomOpsWC[$, S]($: $)(implicit ops: $ => WriteAccess[S]) =
    new MonocleReactCompStateZoomOps[WriteAccess[S], S, WriteAccess](ops($))

  @inline implicit def MonocleReactCompStateZoomOpsWD[$, S]($: $)(implicit ops: $ => WriteAccessD[S]) =
    new MonocleReactCompStateZoomOps[WriteAccessD[S], S, WriteAccessD](ops($))
}
