package japgolly.scalajs.react

import scala.scalajs.js

/** Classes for specifying options for React. */
object ReactOptions {

  /** Options for [[ReactDOMClient.createRoot()]]. */
  final case class CreateRoot(identifierPrefix  : js.UndefOr[String]       = js.undefined,
                              onRecoverableError: ReactCaughtError => Unit = null,
                             ) { self =>
    def raw(): facade.CreateRootOptions = {
      val o = js.Dynamic.literal().asInstanceOf[facade.CreateRootOptions]
      o.identifierPrefix   = self.identifierPrefix
      o.onRecoverableError = errorHandler(self.onRecoverableError)
      o
    }
  }

  /** Options for [[ReactDOM.hydrateRoot()]]. */
  final case class HydrateRoot(identifierPrefix  : js.UndefOr[String]       = js.undefined,
                               onRecoverableError: ReactCaughtError => Unit = null,
                             ) { self =>
    def raw(): facade.HydrateRootOptions = {
      val o = js.Dynamic.literal().asInstanceOf[facade.HydrateRootOptions]
      o.identifierPrefix   = self.identifierPrefix
      o.onRecoverableError = errorHandler(self.onRecoverableError)
      o
    }
  }

  private def errorHandler[A](f: ReactCaughtError => A): js.UndefOr[js.Function2[js.Any, facade.React.ErrorInfo, A]] =
    if (f == null)
      js.undefined
    else
      js.defined((e, i) => f(ReactCaughtError(e, i)))
}
