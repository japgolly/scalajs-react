package japgolly.scalajs.react.component

import japgolly.scalajs.react.hooks.HookComponentBuilder
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, Reusability, facade}

object ScalaFn {

  type Component[P, CT[-p, +u] <: CtorType[p, u]] = JsFn.ComponentWithRoot[P, CT, Unmounted[P], Box[P], CT, JsFn.Unmounted[Box[P]]]
  type Unmounted[P]                               = JsFn.UnmountedWithRoot[P, Mounted, Box[P]]
  type Mounted                                    = JsFn.Mounted

  def fromBoxed[P, CT[-p, +u] <: CtorType[p, u]](js: JsFn.Component[Box[P], CT]): Component[P, CT] =
    js.cmapCtorProps[P](Box(_)).mapUnmounted(_.mapUnmountedProps(_.unbox))

  @inline def withHooks[P] =
    HookComponentBuilder.apply[P]

  def apply[P](render: P => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None]): Component[P, s.CT] =
    create[P, Children.None, s.CT](b => render(b.unbox))(s)

  def withChildren[P](render: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
    create[P, Children.Varargs, s.CT](b => render(b.unbox, PropsChildren(b.children)))(s)

  def justChildren(render: PropsChildren => VdomNode): Component[Unit, CtorType.Children] =
    create(b => render(PropsChildren(b.children)))

  def withReuse[P](render: P => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None], r: Reusability[P]): Component[P, s.CT] =
    withHooks[P].renderWithReuse(render)(s, r)

  def withReuseBy[P, A](reusableInputs: P => A)(render: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None], r: Reusability[A]): Component[P, s.CT] =
    withHooks[P].renderWithReuseBy(reusableInputs)(render)(s, r)

  def withChildrenAndReuse[P](render: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], rp: Reusability[P], rc: Reusability[PropsChildren]): Component[P, s.CT] =
    withHooks[P].withPropsChildren.renderWithReuse(render)

  def withChildrenAndReuse[P, A](reusableInputs: (P, PropsChildren) => A)(render: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[A]): Component[P, s.CT] =
    withHooks[P].withPropsChildren.renderWithReuseBy(reusableInputs)(render)

  // ===================================================================================================================

  private def create[P, C <: Children, CT[-p, +u] <: CtorType[p, u]]
      (render: Box[P] with facade.PropsWithChildren => VdomNode)
      (implicit s: CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] = {
    val jsRender: JsFn.RawComponent[Box[P]] = render(_).rawNode
    fromBoxed(JsFn.fromJsFn[Box[P], C](jsRender)(s))
  }
}
