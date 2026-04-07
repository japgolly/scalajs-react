package japgolly.scalajs.react

import japgolly.scalajs.react.util.Effect._

object ReactDOM {
  val raw = facade.ReactDOM
  @inline def version = facade.ReactDOM.version

  def flushSync[F[_], A](fa: F[A])(implicit F: Sync[F]): F[A] =
    F.delay(facade.ReactDOM.flushSync(F.toJsFn(fa)))
}
