package japgolly.scalajs.react

// TODO Document CallbackOption

object CallbackOption {
  private[CallbackOption] val someUnit: Option[Unit] = Some(())

  def apply[A](cb: CallbackTo[Option[A]]): CallbackOption[A] =
    new CallbackOption(cb.toScalaFn)

  def empty: CallbackOption[Unit] =
    pure(())

  def pure[A](a: A): CallbackOption[A] =
    CallbackOption(CallbackTo pure Some(a))

  def liftValue[A](a: => A): CallbackOption[A] =
    CallbackOption(CallbackTo(Some(a)))

  def liftOption[A](oa: => Option[A]): CallbackOption[A] =
    CallbackOption(CallbackTo(oa))

  def liftCallback[A](cb: CallbackTo[A]): CallbackOption[A] =
    CallbackOption(cb map Some.apply)

  def require(condition: => Boolean): CallbackOption[Unit] =
    CallbackOption(CallbackTo(if (condition) someUnit else None))

  def unless(condition: => Boolean): CallbackOption[Unit] =
    require(!condition)

  implicit def toCallback(s: CallbackOption[Unit]): Callback =
    s.toCallback
}

// =====================================================================================================================

/**
 * Callback that can short-circuit along the way when conditions you specify, aren't met.
 *
 * This is meant to be lightweight, and be immediately useful without the typical pain of imports, implicit conversions
 * and extension methods then normally accompany monad transforms in Scala.
 *
 * For a more generic (i.e. beyond Option) or comprehensive monad transformer use Scalaz or similar.
 */
final class CallbackOption[A](private val cbfn: () => Option[A]) extends AnyVal {
  import CallbackOption.someUnit

  def get: CallbackTo[Option[A]] =
    CallbackTo lift cbfn

  def toCallback(implicit ev: A =:= Unit): Callback =
    get.void

  def toCallbackB: CallbackB =
    get.map(_.isDefined)

  def unary_!(implicit ev: A =:= Unit): CallbackOption[Unit] =
    CallbackOption(get.map {
      case None    => someUnit
      case Some(_) => None
    })

  def map[B](f: A => B): CallbackOption[B] =
    CallbackOption(get.map(_ map f))

  def flatMapOption[B](f: A => Option[B]): CallbackOption[B] =
    CallbackOption(get.map(_ flatMap f))

  def flatMap[B](f: A => CallbackOption[B]): CallbackOption[B] =
    CallbackOption(get flatMap {
      case Some(a) => f(a).get
      case None    => CallbackTo(None)
    })

  def filter(condition: A => Boolean): CallbackOption[A] =
    CallbackOption(get.map(_ filter condition))

  def >>[B](next: CallbackOption[B]): CallbackOption[B] =
    flatMap(_ => next)

  def <<[B](prev: CallbackOption[B]): CallbackOption[A] =
    prev >> this

  @inline def *>[B](next: CallbackOption[B]): CallbackOption[B] =
    this >> next

  def <*[B](next: CallbackOption[B]): CallbackOption[A] =
    for {
      a <- this
      _ <- next
    } yield a

  def void: CallbackOption[Unit] =
    map(_ => ())
}
