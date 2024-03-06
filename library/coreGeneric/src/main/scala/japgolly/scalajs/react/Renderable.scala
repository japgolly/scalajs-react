package japgolly.scalajs.react

import japgolly.scalajs.react.component.Generic.{UnmountedRaw => Component}
import japgolly.scalajs.react.vdom.VdomNode

/** Typeclass for anything that React can render.
  *
  * @since v2.2.0 / React 18
  */
@inline final case class Renderable[-A](raw: A => facade.React.Node) extends AnyVal {
  @inline def apply(a: A): facade.React.Node =
    raw(a)
}

object Renderable {

  @inline implicit def long: Renderable[Long] =
    Renderable(_.toString)

  @inline implicit def raw[A](implicit ev: A => facade.React.Node): Renderable[A] =
    Renderable(ev)

  @inline implicit def vdom: Renderable[VdomNode] =
    Renderable(_.rawNode)

  @inline implicit def component: Renderable[Component] =
    Renderable(_.raw)
}
