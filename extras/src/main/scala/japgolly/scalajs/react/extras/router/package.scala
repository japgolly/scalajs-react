package japgolly.scalajs.react.extras

import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.ScalazReact.ChangeFilter
import scalaz.Free

package object router {

  @inline implicit def routeChangeFilter[P] =
    ChangeFilter.refl[Route[P]]

  type Renderer[P] = Router[P] => ReactElement

  type RouteProg[P, A] = Free.FreeC[({type λ[α] = RouteCmd[P, α]})#λ, A]

  implicit def autoLiftRouteProg[P, A](c: RouteCmd[P, A]): RouteProg[P, A] =
    Free.liftFC[({type λ[α] = RouteCmd[P, α]})#λ, A](c)

  implicit class RouteProgExt[P, A](val c: RouteProg[P, A]) extends AnyVal {
    def >>[B](d: RouteProg[P, B]): RouteProg[P, B] = c flatMap (_ => d)
  }

  implicit class RouteCmdExt[P, A](val c: RouteCmd[P, A]) extends AnyVal {
    def >>[B](d: RouteProg[P, B]): RouteProg[P, B] = (c: RouteProg[P, A]) >> d
  }

}