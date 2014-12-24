package japgolly.scalajs.react.extra

import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.ScalazReact.ChangeFilter
import scalaz.Free

package object router {

  type Renderer[P] = Router[P] => ReactElement

  implicit def routeChangeFilter[P] = ChangeFilter.equal[Location[P]]

  /** Free monad & free functor over route commands. */
  type RouteProg[P, A] = Free.FreeC[({type λ[α] = RouteCmd[P, α]})#λ, A]

  implicit def reactAutoLiftRouteProg[P, A](c: RouteCmd[P, A]): RouteProg[P, A] =
    Free.liftFC[({type λ[α] = RouteCmd[P, α]})#λ, A](c)

  implicit final class ReactRouteProgExt[P, A](val _r: RouteProg[P, A]) extends AnyVal {
    def >>[B](d: RouteProg[P, B]): RouteProg[P, B] = _r flatMap (_ => d)
  }

  implicit final class ReactRouteCmdExt[P, A](val _r: RouteCmd[P, A]) extends AnyVal {
    def >>[B](d: RouteProg[P, B]): RouteProg[P, B] = new ReactRouteProgExt(_r) >> d
  }
}