package japgolly.scalajs.react

import scalaz.{Optional => _, _}
import extra._
import router.{BaseUrl, AbsUrl, Path}
import router.StaticDsl.Rule

private[react] object ScalazReactExtra {

  final class ScalazReusability$Ops(private val ε: Reusability.type) extends AnyVal {
    /** Compare using Scalaz equality typeclass. */
    def byEqual[A](implicit e: Equal[A]): Reusability[A] =
      new Reusability(e.equal)
  }

  final class ScalazListenable$Ops(private val ε: Listenable.type) extends AnyVal {
    import Listenable._
    import ScalazReact._

    def installS[P, S, B <: OnUnmount, N <: TopNode, M[_], A](f: P => Listenable[A], g: A => ReactST[M, S, Unit])(implicit M: M ~> CallbackTo) =
      install[P, S, B, N, A](f, $ => a => $.runState(g(a)))
  
    def installSF[P, S, B <: OnUnmount, N <: TopNode, M[_], A](f: P => Listenable[A], g: A => ReactST[M, S, Unit])(implicit M: M ~> CallbackTo, F: ChangeFilter[S]) =
      install[P, S, B, N, A](f, $ => a => $.runStateF(g(a)))
  }
}

trait ScalazReactExtra {
  import ScalazReactExtra._

  implicit def routerEqualBaseUrl: Equal[BaseUrl] = Equal.equalA
  implicit def routerEqualPath   : Equal[Path]    = Equal.equalA
  implicit def routerEqualAbsUrl : Equal[AbsUrl]  = Equal.equalA

  implicit def routerRuleMonoid[P]: Monoid[Rule[P]] =
    new Monoid[Rule[P]] {
      override def zero = Rule.empty
      override def append(a: Rule[P], b: => Rule[P]) = a | b
    }

  @inline implicit def ScalazReusability$Ops(a: Reusability.type) = new ScalazReusability$Ops(a)
  @inline implicit def ScalazListenable$Ops(a: Listenable.type) = new ScalazListenable$Ops(a)

  implicit def reusabilityDisjunction[A: Reusability, B: Reusability]: Reusability[A \/ B] =
    Reusability.fn {
      case (-\/(a), -\/(b)) => a ~=~ b
      case (\/-(a), \/-(b)) => a ~=~ b
      case _ => false
    }

  implicit def reusabilityThese[A: Reusability, B: Reusability]: Reusability[A \&/ B] = {
    import \&/._
    Reusability.fn {
      case (Both(a, b), Both(c, d)) => (a ~=~ c) && (b ~=~ d)
      case (This(a),    This(b))    => a ~=~ b
      case (That(a),    That(b))    => a ~=~ b
      case _ => false
    }
  }
}
