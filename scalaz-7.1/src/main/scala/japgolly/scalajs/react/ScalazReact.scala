package japgolly.scalajs.react

import scalaz.{Optional => _, _}
import scalaz.effect.IO

object ScalazReact
  extends ScalazReactExtra
     with ScalazReactState
     with ScalazReactInstances {

  final class SzRExt_CallbackOps[A](private val _c: () => A) extends AnyVal {
    @inline private def c = CallbackTo lift _c

    def toIO: IO[A] =
      scalazIoToCallbackIso to c

    def flattenIO[B](implicit ev: A =:= IO[B]): CallbackTo[B] =
      //_c.flatMap(a => ev(a).toCallback)
      c.map(_.unsafePerformIO())
  }

  @inline implicit def SzRExt_CallbackOps[A](c: CallbackTo[A]) =
    new SzRExt_CallbackOps(c.toScalaFn)

  implicit final class SzRExt_CallbackConvertableOps[M[_], A](private val m: M[A]) extends AnyVal {
    def toCallback(implicit t: M ~> CallbackTo): CallbackTo[A] =
      t(m)
  }

  @inline implicit def SzRExt_StateTOps[M[_], S, A](s: StateT[M, S, A]) =
    new ScalazReactState.SzRExt_StateTOps(s)

  @inline implicit def SzRExt__StateTOps[I, M[_], S, A](f: I => StateT[M, S, A]) =
    new ScalazReactState.SzRExt__StateTOps(f)

  @inline implicit def SzRExt_ReactSOps[S, A](r: ReactS[S,A]) =
    new ScalazReactState.SzRExt_ReactSOps(r)

  @inline implicit def SzRExt_ReactSTOps[M[_], S, A](r: ReactST[M,S,A]) =
    new ScalazReactState.SzRExt_ReactSTOps(r)

  // TODO Move into extra
  final case class ChangeFilter[S](allowChange: (S, S) => Boolean) {
    def apply[A](s1: S, s2: S, orElse: => A, change: S => A): A =
      if (allowChange(s1, s2)) change(s2) else orElse
  }
  object ChangeFilter {
    def refl[S] = apply[S](_ != _)
    def reflOn[S, T](f: S => T) = apply[S](f(_) != f(_))
    def equal[S: Equal] = apply[S]((a,b) => !implicitly[Equal[S]].equal(a,b))
    def equalOn[S, T: Equal](f: S => T) = apply[S]((a,b) => !implicitly[Equal[T]].equal(f(a),f(b)))
  }
}
