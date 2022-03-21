package japgolly.scalajs.react.extra.router

/** The means by which the location should be set to a given URL. */
sealed trait SetRouteVia

object SetRouteVia {

  /** The current URL will not be recorded in history. User can't hit ''Back'' button to reach it.
    *
    * Implemented via `replaceState` in the History API.
    *
    * @see https://developer.mozilla.org/en-US/docs/Web/API/History/replaceState
    */
  case object HistoryReplace extends SetRouteVia

  /** The current URL will be recorded in history. User can hit ''Back'' button to reach it.
    *
    * Implemented via `pushState` in the History API.
    *
    * @see https://developer.mozilla.org/en-US/docs/Web/API/History/pushState
    */
  case object HistoryPush extends SetRouteVia

  /** `window.location.href` will be directly set to the new URL.
    *
    * If the new URL is part of the current SPA, the entire SPA will be reloaded.
    *
    * The current URL will be recorded in history. User can hit ''Back'' button to reach it.
    */
  case object WindowLocation extends SetRouteVia
}