package japgolly.scalajs.react

import japgolly.scalajs.react.util.Effect._
import org.scalajs.dom

object ReactDOM {
  val raw = facade.ReactDOM
  @inline def version = facade.ReactDOM.version

  def flushSync[F[_], A](fa: F[A])(implicit F: Sync[F]): F[A] =
    F.delay(facade.ReactDOM.flushSync(F.toJsFn(fa)))

  @inline def requestFormReset(form: dom.HTMLFormElement): Unit =
    facade.ReactDOM.requestFormReset(form)
}
