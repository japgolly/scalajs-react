package japgolly.scalajs.react.internal

import scala.annotation.tailrec
import scalaz.{Optional => _, _}
import scalaz.Isomorphism.<~>
import scalaz.effect.IO
import Scalaz.Id
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._

trait ScalazReactInstances {

  implicit final lazy val reactCallbackScalazInstance: MonadError[CallbackTo, Throwable] with BindRec[CallbackTo] with Distributive[CallbackTo] =
    new MonadError[CallbackTo, Throwable] with BindRec[CallbackTo] with Distributive[CallbackTo] {

      override def point[A](a: => A): CallbackTo[A] =
        CallbackTo(a)

      override def bind[A, B](fa: CallbackTo[A])(f: A => CallbackTo[B]): CallbackTo[B] =
        fa >>= f

      override def map[A, B](fa : CallbackTo[A])(f : A => B): CallbackTo[B] =
        fa map f

      override def tailrecM[A, B](f: A => CallbackTo[A \/ B])(a: A): CallbackTo[B] =
        CallbackTo {
          @tailrec
          def go(a: A): B =
            f(a).runNow() match {
              case -\/(n) => go(n)
              case \/-(b) => b
            }
          go(a)
        }

      override def distributeImpl[G[_], A, B](ga: G[A])(f: A => CallbackTo[B])(implicit G: Functor[G]): CallbackTo[G[B]] =
        CallbackTo.liftTraverse(f).id.map(G.map(ga))

      override def raiseError[A](e: Throwable): CallbackTo[A] =
        CallbackTo(throw e)

      override def handleError[A](fa: CallbackTo[A])(f: Throwable => CallbackTo[A]): CallbackTo[A] =
        fa.attempt.flatMap {
          case Right(a) => CallbackTo pure a
          case Left(t)  => f(t)
        }
    }

  implicit final lazy val reactCallbackOptionScalazInstance: MonadPlus[CallbackOption] with BindRec[CallbackOption] =
    new MonadPlus[CallbackOption] with BindRec[CallbackOption] {
      override def point[A](a: => A): CallbackOption[A] =
        CallbackOption.liftValue(a)

      override def bind[A, B](fa: CallbackOption[A])(f: A => CallbackOption[B]): CallbackOption[B] =
        fa >>= f

      override def map[A, B](fa : CallbackOption[A])(f : A => B): CallbackOption[B] =
        fa map f

      override def tailrecM[A, B](f: A => CallbackOption[A \/ B])(a: A): CallbackOption[B] =
        CallbackOption.liftOption {
          @tailrec
          def go(a: A): Option[B] =
            f(a).asCallback.runNow() match {
              case Some(-\/(n)) => go(n)
              case Some(\/-(b)) => Some(b)
              case None         => None
            }
          go(a)
        }

      override def empty[A]: CallbackOption[A] =
        CallbackOption.fail

      override def plus[A](a: CallbackOption[A], b: => CallbackOption[A]): CallbackOption[A] =
        a orElse b
    }

  implicit final lazy val maybeReactInstance: OptionLike[Maybe] = new OptionLike[Maybe] {
    type O[A] = Maybe[A]
    def map     [A, B](o: O[A])(f: A => B)         : O[B]      = o map f
    def fold    [A, B](o: O[A], b: => B)(f: A => B): B         = o.cata(f, b)
    def foreach [A]   (o: O[A])(f: A => Unit)      : Unit      = o.cata(f, ())
    def isEmpty [A]   (o: O[A])                    : Boolean   = o.isEmpty
    def toOption[A]   (o: O[A])                    : Option[A] = o.toOption
  }

  implicit final lazy val ioReactInstance: Effect[IO] = new Effect[IO] {
    override def point  [A]   (a: => A)                 = IO(a)
    override def pure   [A]   (a: A)                    = IO(a)
    override def map    [A, B](a: IO[A])(f: A => B)     = a map f
    override def flatMap[A, B](a: IO[A])(f: A => IO[B]) = a flatMap f
    override def extract[A]   (a: => IO[A])             = () => a.unsafePerformIO()
  }

  implicit final lazy val effectTransEndoIo       = Effect.Trans.id[IO]
  implicit final lazy val effectTransIdToIo       = Effect.Trans[Effect.Id, IO]
  implicit final lazy val effectTransCallbackToIo = Effect.Trans[CallbackTo, IO]
  implicit final lazy val effectTransIoToId       = Effect.Trans[IO, Effect.Id]
  implicit final lazy val effectTransIoToCallback = Effect.Trans[IO, CallbackTo]

  implicit final lazy val scalazIdToReactCallback: (Scalaz.Id ~> CallbackTo) =
    new (Scalaz.Id ~> CallbackTo) { override def apply[A](a: A) = CallbackTo(a) }

  implicit final lazy val ioToReactCallback: (IO ~> CallbackTo) =
    new (IO ~> CallbackTo) { override def apply[A](a: IO[A]) = CallbackTo(a.unsafePerformIO()) }

  implicit final lazy val reactCallbackToIo: (CallbackTo ~> IO) =
    new (CallbackTo ~> IO) { override def apply[A](a: CallbackTo[A]) = IO(a.runNow()) }

  final lazy val ioToReactCallbackIso: (CallbackTo <~> IO) =
    new (CallbackTo <~> IO) {
      override val from = ioToReactCallback
      override val to = reactCallbackToIo
    }

  implicit final lazy val reactCallbackToItself: (CallbackTo ~> CallbackTo) =
    new (CallbackTo ~> CallbackTo) { override def apply[A](a: CallbackTo[A]) = a }

  implicit final def reusabilityDisjunction[A: Reusability, B: Reusability]: Reusability[A \/ B] =
    Reusability((x, y) =>
      x.fold[Boolean](
        a => y.fold(a ~=~ _, _ => false),
        b => y.fold(_ => false, b ~=~ _)))

  implicit final def reusabilityThese[A: Reusability, B: Reusability]: Reusability[A \&/ B] = {
    import \&/._
    Reusability {
      case (Both(a, b), Both(c, d)) => (a ~=~ c) && (b ~=~ d)
      case (This(a),    This(b))    => a ~=~ b
      case (That(a),    That(b))    => a ~=~ b
      case _ => false
    }
  }

  implicit final def routerEqualBaseUrl: Equal[router.BaseUrl] = Equal.equalA
  implicit final def routerEqualPath   : Equal[router.Path]    = Equal.equalA
  implicit final def routerEqualAbsUrl : Equal[router.AbsUrl]  = Equal.equalA

  implicit final def routerRuleMonoid[P]: Monoid[router.StaticDsl.Rule[P]] = {
    import router.StaticDsl.Rule
    new Monoid[Rule[P]] {
      override def zero = Rule.empty
      override def append(a: Rule[P], b: => Rule[P]) = a | b
    }
  }

}
