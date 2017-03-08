package japgolly.scalajs.react.component

import scala.scalajs.js
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, raw}
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.vdom.VdomElement

object ScalaFn {

  type Component[P, CT[-p, +u] <: CtorType[p, u]] = JsFn.ComponentWithRoot[P, CT, Unmounted[P], Box[P], CT, JsFn.Unmounted[Box[P]]]
  type Unmounted[P]                               = JsFn.UnmountedWithRoot[P, Mounted, Box[P]]
  type Mounted                                    = JsFn.Mounted

  private def create[P, C <: Children, CT[-p, +u] <: CtorType[p, u]]
      (render: Box[P] with raw.PropsWithChildren => VdomElement)
      (implicit s: CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] = {

    val jsRender = render.andThen(_.rawElement): js.Function1[Box[P] with raw.PropsWithChildren, raw.ReactElement]
    val rawComponent = jsRender.asInstanceOf[raw.ReactFunctionalComponent]
    JsFn[Box[P], C](rawComponent)(s)
      .cmapCtorProps[P](Box(_))
      .mapUnmounted(_.mapUnmountedProps(_.unbox))
  }

  def apply[P](render: P => VdomElement): Component[P, CtorType.Props] =
    create(b => render(b.unbox))

  def withChildren[P](render: (P, PropsChildren) => VdomElement): Component[P, CtorType.PropsAndChildren] =
    create(b => render(b.unbox, PropsChildren(b.children)))

  def justChildren(render: PropsChildren => VdomElement): Component[Unit, CtorType.Children] =
    create(b => render(PropsChildren(b.children)))
}
