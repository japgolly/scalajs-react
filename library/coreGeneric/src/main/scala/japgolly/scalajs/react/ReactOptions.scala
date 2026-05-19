package japgolly.scalajs.react

import japgolly.scalajs.react.util.Effect.Sync
import scala.scalajs.js

/** Classes for specifying options for React. */
object ReactOptions {

  /** Options for [[ReactDOMClient.createRoot()]]. */
  final case class CreateRoot(identifierPrefix  : js.UndefOr[String]       = js.undefined,
                              onRecoverableError: ReactCaughtError => Unit = null,
                              onCaughtError     : ReactCaughtError => Unit = null,
                              onUncaughtError   : ReactCaughtError => Unit = null,
                             ) { self =>

    def withOnRecoverableError[F[_]](f: ReactCaughtError => F[Unit])(implicit F: Sync[F]): CreateRoot =
      copy(onRecoverableError = e => F.runSync(f(e)))

    def withOnCaughtError[F[_]](f: ReactCaughtError => F[Unit])(implicit F: Sync[F]): CreateRoot =
      copy(onCaughtError = e => F.runSync(f(e)))

    def withOnUncaughtError[F[_]](f: ReactCaughtError => F[Unit])(implicit F: Sync[F]): CreateRoot =
      copy(onUncaughtError = e => F.runSync(f(e)))

    def raw(): facade.CreateRootOptions = {
      val o = js.Dynamic.literal().asInstanceOf[facade.CreateRootOptions]
      o.identifierPrefix   = self.identifierPrefix
      o.onRecoverableError = errorHandler(self.onRecoverableError)
      o.onCaughtError      = errorHandler(self.onCaughtError)
      o.onUncaughtError    = errorHandler(self.onUncaughtError)
      o
    }
  }

  /** Options for [[ReactDOM.hydrateRoot()]]. */
  final case class HydrateRoot(identifierPrefix  : js.UndefOr[String]       = js.undefined,
                               onRecoverableError: ReactCaughtError => Unit = null,
                               onCaughtError     : ReactCaughtError => Unit = null,
                               onUncaughtError   : ReactCaughtError => Unit = null,
                             ) { self =>

    def withOnRecoverableError[F[_]](f: ReactCaughtError => F[Unit])(implicit F: Sync[F]): HydrateRoot =
      copy(onRecoverableError = e => F.runSync(f(e)))

    def withOnCaughtError[F[_]](f: ReactCaughtError => F[Unit])(implicit F: Sync[F]): HydrateRoot =
      copy(onCaughtError = e => F.runSync(f(e)))

    def withOnUncaughtError[F[_]](f: ReactCaughtError => F[Unit])(implicit F: Sync[F]): HydrateRoot =
      copy(onUncaughtError = e => F.runSync(f(e)))

    def raw(): facade.HydrateRootOptions = {
      val o = js.Dynamic.literal().asInstanceOf[facade.HydrateRootOptions]
      o.identifierPrefix   = self.identifierPrefix
      o.onRecoverableError = errorHandler(self.onRecoverableError)
      o.onCaughtError      = errorHandler(self.onCaughtError)
      o.onUncaughtError    = errorHandler(self.onUncaughtError)
      o
    }
  }

  private def errorHandler[A](f: ReactCaughtError => A): js.UndefOr[js.Function2[js.Any, facade.React.ErrorInfo, A]] =
    if (f == null)
      js.undefined
    else
      js.defined((e, i) => f(ReactCaughtError(e, i)))
}
