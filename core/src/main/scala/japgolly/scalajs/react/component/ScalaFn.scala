package japgolly.scalajs.react.component

import japgolly.scalajs.react.hooks.HookComponentBuilder
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, facade}
import scala.scalajs.js

object ScalaFn {

  type Component[P, CT[-p, +u] <: CtorType[p, u]] = JsFn.ComponentWithRoot[P, CT, Unmounted[P], Box[P], CT, JsFn.Unmounted[Box[P]]]
  type Unmounted[P]                               = JsFn.UnmountedWithRoot[P, Mounted, Box[P]]
  type Mounted                                    = JsFn.Mounted

  private def create[P, C <: Children, CT[-p, +u] <: CtorType[p, u]]
      (render: Box[P] with facade.PropsWithChildren => VdomNode)
      (implicit s: CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] = {

    val jsRender = render.andThen(_.rawNode): js.Function1[Box[P] with facade.PropsWithChildren, facade.React.Node]
    val rawComponent = jsRender.asInstanceOf[facade.React.StatelessFunctionalComponent[Box[P]]]
    JsFn.force[Box[P], C](rawComponent)(s)
      .cmapCtorProps[P](Box(_))
      .mapUnmounted(_.mapUnmountedProps(_.unbox))
  }

  def apply[P](render: P => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None]): Component[P, s.CT] =
    create[P, Children.None, s.CT](b => render(b.unbox))(s)

  def withChildren[P](render: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
    create[P, Children.Varargs, s.CT](b => render(b.unbox, PropsChildren(b.children)))(s)

  def justChildren(render: PropsChildren => VdomNode): Component[Unit, CtorType.Children] =
    create(b => render(PropsChildren(b.children)))

  @inline def withHooks[P] =
    HookComponentBuilder.apply[P]
}
