package japgolly.scalajs.react.component

import japgolly.scalajs.react.hooks.HookComponentBuilder
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, Reusability, facade}
import scala.scalajs.js
import sourcecode.FullName

object ScalaFn extends DerivedDisplayName {

  type Component[P, CT[-p, +u] <: CtorType[p, u]] = JsFn.ComponentWithRoot[P, CT, Unmounted[P], Box[P], CT, JsFn.Unmounted[Box[P]]]
  type Unmounted[P]                               = JsFn.UnmountedWithRoot[P, Mounted, Box[P]]
  type Mounted                                    = JsFn.Mounted

  private def create[P, C <: Children, CT[-p, +u] <: CtorType[p, u]]
      (displayName: String)
      (render: Box[P] with facade.PropsWithChildren => Delayed[VdomNode])
      (implicit s: CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] = {

    val jsRender = render.andThen(_.eval().rawNode): js.Function1[Box[P] with facade.PropsWithChildren, facade.React.Node]
    val rawComponent = jsRender.asInstanceOf[facade.React.StatelessFunctionalComponent[Box[P]]]
    rawComponent.setDisplayName = displayName
    JsFn.force[Box[P], C](rawComponent)(s)
      .cmapCtorProps[P](Box(_))
      .mapUnmounted(_.mapUnmountedProps(_.unbox))
  }

  @inline def withDisplayName(name: String): DisplayNameApplied =
    new DisplayNameApplied(name)

  @inline def withHooks[P](implicit name: FullName): HookComponentBuilder.ComponentP.First[P] =
    HookComponentBuilder.apply[P](derivedDisplayName)

  // ===================================================================================================================

  def apply[P](render: P => Delayed[VdomNode])(implicit s: CtorType.Summoner[Box[P], Children.None], name: FullName): Component[P, s.CT] =
    create[P, Children.None, s.CT](derivedDisplayName)(b => render(b.unbox))(s)

  def withChildren[P](render: (P, PropsChildren) => Delayed[VdomNode])(implicit s: CtorType.Summoner[Box[P], Children.Varargs], name: FullName): Component[P, s.CT] =
    create[P, Children.Varargs, s.CT](derivedDisplayName)(b => render(b.unbox, PropsChildren(b.children)))(s)

  def justChildren(render: PropsChildren => Delayed[VdomNode])(implicit name: FullName): Component[Unit, CtorType.Children] =
    create(derivedDisplayName)(b => render(PropsChildren(b.children)))

  // ===================================================================================================================

  def withReuse[P](render: P => Delayed[VdomNode])(implicit s: CtorType.Summoner[Box[P], Children.None], r: Reusability[P], name: FullName): Component[P, s.CT] =
    withHooks[P].renderWithReuse(render.andThen(_.eval()))(s, r)

  def withReuseBy[P, A](reusableInputs: P => A)(render: A => Delayed[VdomNode])(implicit s: CtorType.Summoner[Box[P], Children.None], r: Reusability[A], name: FullName): Component[P, s.CT] =
    withHooks[P].renderWithReuseBy(reusableInputs)(render.andThen(_.eval()))(s, r)

  def withChildrenAndReuse[P](render: (P, PropsChildren) => Delayed[VdomNode])(implicit s: CtorType.Summoner[Box[P], Children.Varargs], rp: Reusability[P], rc: Reusability[PropsChildren], name: FullName): Component[P, s.CT] =
    withHooks[P].withPropsChildren.renderWithReuse(i => render(i.props, i.propsChildren).eval())

  def withChildrenAndReuse[P, A](reusableInputs: (P, PropsChildren) => A)(render: A => Delayed[VdomNode])(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[A], name: FullName): Component[P, s.CT] =
    withHooks[P].withPropsChildren.renderWithReuseBy(i => reusableInputs(i.props, i.propsChildren))(render.andThen(_.eval()))

  class DisplayNameApplied private[ScalaFn](displayName: String) {
    @inline def withHooks[P]: HookComponentBuilder.ComponentP.First[P] =
      HookComponentBuilder.apply[P](displayName)

    // ===================================================================================================================

    def apply[P](render: P => Delayed[VdomNode])(implicit s: CtorType.Summoner[Box[P], Children.None]): Component[P, s.CT] =
      create[P, Children.None, s.CT](displayName)(b => render(b.unbox))(s)

    def withChildren[P](render: (P, PropsChildren) => Delayed[VdomNode])(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
      create[P, Children.Varargs, s.CT](displayName)(b => render(b.unbox, PropsChildren(b.children)))(s)

    def justChildren(render: PropsChildren => Delayed[VdomNode]): Component[Unit, CtorType.Children] =
      create(displayName)(b => render(PropsChildren(b.children)))

    // ===================================================================================================================

    def withReuse[P](render: P => Delayed[VdomNode])(implicit s: CtorType.Summoner[Box[P], Children.None], r: Reusability[P]): Component[P, s.CT] =
      withHooks[P].renderWithReuse(render.andThen(_.eval()))(s, r)

    def withReuseBy[P, A](reusableInputs: P => A)(render: A => Delayed[VdomNode])(implicit s: CtorType.Summoner[Box[P], Children.None], r: Reusability[A]): Component[P, s.CT] =
      withHooks[P].renderWithReuseBy(reusableInputs)(render.andThen(_.eval()))(s, r)

    def withChildrenAndReuse[P](render: (P, PropsChildren) => Delayed[VdomNode])(implicit s: CtorType.Summoner[Box[P], Children.Varargs], rp: Reusability[P], rc: Reusability[PropsChildren]): Component[P, s.CT] =
      withHooks[P].withPropsChildren.renderWithReuse(i => render(i.props, i.propsChildren).eval())

    def withChildrenAndReuse[P, A](reusableInputs: (P, PropsChildren) => A)(render: A => Delayed[VdomNode])(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[A]): Component[P, s.CT] =
      withHooks[P].withPropsChildren.renderWithReuseBy(i => reusableInputs(i.props, i.propsChildren))(render.andThen(_.eval()))
  }
}
