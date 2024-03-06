package japgolly.scalajs.react

import scala.scalajs.js

/** Classes for specifying options for React. */
object ReactOptions {

  /** Options for [[ReactDOM.createRoot()]]. */
  final case class CreateRoot(identifierPrefix                   : js.UndefOr[String     ] = js.undefined,
                              onRecoverableError                 : js.UndefOr[Any => Unit] = js.undefined,
                              unstable_concurrentUpdatesByDefault: js.UndefOr[Boolean    ] = js.undefined,
                              unstable_strictMode                : js.UndefOr[Boolean    ] = js.undefined,
                             ) { self =>
    def raw(): facade.CreateRootOptions = {
      val o = js.Dynamic.literal().asInstanceOf[facade.CreateRootOptions]
      o.identifierPrefix                    = self.identifierPrefix
      o.onRecoverableError                  = self.onRecoverableError
      o.unstable_concurrentUpdatesByDefault = self.unstable_concurrentUpdatesByDefault
      o.unstable_strictMode                 = self.unstable_strictMode
      o
    }
  }

  /** Options for [[ReactDOM.hydrateRoot()]]. */
  final case class HydrateRoot(identifierPrefix                   : js.UndefOr[String     ] = js.undefined,
                               onRecoverableError                 : js.UndefOr[Any => Unit] = js.undefined,
                               unstable_concurrentUpdatesByDefault: js.UndefOr[Boolean    ] = js.undefined,
                               unstable_strictMode                : js.UndefOr[Boolean    ] = js.undefined,
                              ) { self =>
    def raw(): facade.HydrateRootOptions = {
      val o = js.Dynamic.literal().asInstanceOf[facade.HydrateRootOptions]
      o.identifierPrefix                    = self.identifierPrefix
      o.onRecoverableError                  = self.onRecoverableError
      o.unstable_concurrentUpdatesByDefault = self.unstable_concurrentUpdatesByDefault
      o.unstable_strictMode                 = self.unstable_strictMode
      o
    }
  }

}
