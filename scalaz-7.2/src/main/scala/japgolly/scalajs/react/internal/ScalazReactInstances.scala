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
        CallbackTo.tailrec(a)(f.andThen(_.map(_.toEither)))

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
        CallbackOption.tailrec(a)(f.andThen(_.map(_.toEither)))

      override def empty[A]: CallbackOption[A] =
        CallbackOption.fail

      override def plus[A](a: CallbackOption[A], b: => CallbackOption[A]): CallbackOption[A] =
        a orElse b
    }

  implicit lazy val reactCallbackKleisliScalazInstance: Arrow[CallbackKleisli] with Choice[CallbackKleisli] =
    new Arrow[CallbackKleisli] with Choice[CallbackKleisli] {
      override def arr[A, B](f: A => B): CallbackKleisli[A, B] =
        CallbackKleisli.lift(f)

      override def choice[A, B, C](f: => CallbackKleisli[A, C], g: => CallbackKleisli[B, C]): CallbackKleisli[A \/ B, C] =
        CallbackKleisli.choice(f, g).contramap(_.toEither)

      override def compose[A, B, C](f: CallbackKleisli[B, C], g: CallbackKleisli[A, B]): CallbackKleisli[A, C] =
        g >=> f

      override def first[A, B, C](f: CallbackKleisli[A, B]): CallbackKleisli[(A, C), (B, C)] =
        f.strongL

      override def second[A, B, C](f: CallbackKleisli[A, B]): CallbackKleisli[(C, A), (C, B)] =
        f.strongR

      override def id[A]: CallbackKleisli[A, A] =
        CallbackKleisli.ask

      override def split[A, B, C, D](f: CallbackKleisli[A, B], g: CallbackKleisli[C, D]): CallbackKleisli[(A, C), (B, D)] =
        CallbackKleisli.split(f, g)

      override def dimap[A, B, C, D](fab: CallbackKleisli[A, B])(f: C => A)(g: B => D) =
        fab.dimap(f, g)

      override def mapfst[A, B, C](f: CallbackKleisli[A, B])(g: C => A): CallbackKleisli[C, B] =
        f.contramap(g)

      override def mapsnd[A, B, C](f: CallbackKleisli[A, B])(g: B => C): CallbackKleisli[A, C] =
        f.map(g)
    }

  implicit def reactCallbackKleisliAScalazInstance[A]: MonadError[CallbackKleisli[A, ?], Throwable] with MonadReader[CallbackKleisli[A, ?], A] with BindRec[CallbackKleisli[A, ?]] with Distributive[CallbackKleisli[A, ?]] =
    new MonadError[CallbackKleisli[A, ?], Throwable] with MonadReader[CallbackKleisli[A, ?], A] with BindRec[CallbackKleisli[A, ?]] with Distributive[CallbackKleisli[A, ?]] {

      override def point[B](b: => B): CallbackKleisli[A, B] =
        CallbackKleisli const reactCallbackScalazInstance.point(b)

      override def bind[B, C](fa: CallbackKleisli[A, B])(f: B => CallbackKleisli[A, C]): CallbackKleisli[A, C] =
        fa >>= f

      override def map[B, C](fa: CallbackKleisli[A, B])(f: B => C): CallbackKleisli[A, C] =
        fa map f

      override def tailrecM[S, B](f: S => CallbackKleisli[A, S \/ B])(s: S): CallbackKleisli[A, B] =
        CallbackKleisli.tailrec(s)(f.andThen(_.map(_.toEither)))

      override def distributeImpl[G[_], B, C](ga: G[B])(f: B => CallbackKleisli[A, C])(implicit G: Functor[G]): CallbackKleisli[A, G[C]] =
        CallbackKleisli.liftTraverse(f).id.map(G.map(ga))

      override def raiseError[B](e: Throwable): CallbackKleisli[A, B] =
        CallbackKleisli const reactCallbackScalazInstance.raiseError(e)

      override def handleError[B](fb: CallbackKleisli[A, B])(f: Throwable => CallbackKleisli[A, B]): CallbackKleisli[A, B] =
        CallbackKleisli(a => reactCallbackScalazInstance.handleError(fb(a))(f.andThen(_(a))))

      override def ask: CallbackKleisli[A, A] =
        CallbackKleisli.ask

      override def local[B](f: A => A)(fa: CallbackKleisli[A, B]): CallbackKleisli[A, B] =
        fa.contramap(f)
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

  implicit final lazy val reactRefFnScalazInstance: Profunctor[Ref.Fn] =
    new Profunctor[Ref.Fn] {
      override def lmap[A, B, C](f: Ref.Fn[A, B])(m: C => A) = f.contramap(m)
      override def rmap[A, B, C](f: Ref.Fn[A, B])(m: B => C) = f.map(m)
    }

  implicit final def reactRefFullScalazInstance[X]: Profunctor[Ref.Full[?, X, ?]] =
    new Profunctor[Ref.Full[?, X, ?]] {
      override def lmap[A, B, C](f: Ref.Full[A, X, B])(m: C => A) = f.contramap(m)
      override def rmap[A, B, C](f: Ref.Full[A, X, B])(m: B => C) = f.map(m)
    }
}
