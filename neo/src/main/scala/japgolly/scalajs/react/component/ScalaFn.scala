package japgolly.scalajs.react.component

import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.{ChildrenArg, CtorType, PropsChildren, raw}
import scala.scalajs.js

object ScalaFn {

  type Component[P, CT[-p, +u] <: CtorType[p, u]] = Js.Component0[P, CT, Unmounted[P], Box[P], CT, JsFn.Unmounted[Box[P]]]
  type Unmounted[P]                               = JsFn.Unmounted0[P, Mounted, Box[P]]
  type Mounted                                    = Unit

  private def create[P, C <: ChildrenArg, CT[-p, +u] <: CtorType[p, u]]
      (render: Box[P] with raw.PropsWithChildren => raw.ReactElement)
      (implicit s: CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] = {

    val jsRender = render: js.Function1[Box[P] with raw.PropsWithChildren, raw.ReactElement]
    val rawComponent = jsRender.asInstanceOf[raw.ReactFunctionalComponent]
    JsFn[Box[P], C](rawComponent)(s)
      .cmapCtorProps[P](Box(_))
      .mapUnmounted(_.mapUnmountedProps(_.unbox))
  }

  def Props[P](render: P => raw.ReactElement): Component[P, CtorType.Props] =
    create(b => render(b.unbox))

  def PropsAndChildren[P](render: (P, PropsChildren) => raw.ReactElement): Component[P, CtorType.PropsAndChildren] =
    create(b => render(b.unbox, PropsChildren(b.children)))

  def Children(render: PropsChildren => raw.ReactElement): Component[Unit, CtorType.Children] =
    create(b => render(PropsChildren(b.children)))

}