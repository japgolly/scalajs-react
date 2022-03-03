package japgolly.scalajs.react.component

import japgolly.scalajs.react.hooks.HookComponentBuilder
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, Reusability, facade}
import scala.scalajs.js

object ScalaFn {

  type Component[P, CT[-p, +u] <: CtorType[p, u]] = JsFn.ComponentWithRoot[P, CT, Unmounted[P], Box[P], CT, JsFn.Unmounted[Box[P]]]
  type Unmounted[P]                               = JsFn.UnmountedWithRoot[P, Mounted, Box[P]]
  type Mounted                                    = JsFn.Mounted

  @inline def withHooks[P] =
    HookComponentBuilder.apply[P]

  // ===================================================================================================================

  inline def apply[P](inline render: P => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None]): Component[P, s.CT] = {
    org.scalajs.dom.console.log("ScalaFn")
    org.scalajs.dom.console.log("ScalaFn")
    create[P, Children.None, s.CT](b => render(b.unbox))(s)
  }

  def withChildren[P](render: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
    create[P, Children.Varargs, s.CT](b => render(b.unbox, PropsChildren(b.children)))(s)

  def justChildren(render: PropsChildren => VdomNode): Component[Unit, CtorType.Children] =
    create(b => render(PropsChildren(b.children)))

  // ===================================================================================================================

  def withReuse[P](render: P => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None], r: Reusability[P]): Component[P, s.CT] =
    withHooks[P].renderWithReuse(render)(s, r)

  def withReuseBy[P, A](reusableInputs: P => A)(render: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None], r: Reusability[A]): Component[P, s.CT] =
    withHooks[P].renderWithReuseBy(reusableInputs)(render)(s, r)

  def withChildrenAndReuse[P](render: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], rp: Reusability[P], rc: Reusability[PropsChildren]): Component[P, s.CT] =
    withHooks[P].withPropsChildren.renderWithReuse(i => render(i.props, i.propsChildren))

  def withChildrenAndReuse[P, A](reusableInputs: (P, PropsChildren) => A)(render: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[A]): Component[P, s.CT] =
    withHooks[P].withPropsChildren.renderWithReuseBy(i => reusableInputs(i.props, i.propsChildren))(render)

  // ===================================================================================================================

  def fromRawBoxed[P, C <: Children, CT[-p, +u] <: CtorType[p, u]]
      (raw: facade.React.StatelessFunctionalComponent[Box[P]])
      (implicit s: CtorType.Summoner.Aux[Box[P], C, CT])
      : Component[P, CT] =
    JsFn.force[Box[P], C](raw)(s)
      .cmapCtorProps[P](Box(_))
      .mapUnmounted(_.mapUnmountedProps(_.unbox))

  // @inline def createRawBoxed[P, C <: Children]
  //     (renderBoxed: js.Function1[Box[P] with facade.PropsWithChildren, VdomNode])
  //     : facade.React.StatelessFunctionalComponent[Box[P]] = {
  //   val rawComponent: js.Function1[Box[P] with facade.PropsWithChildren, facade.React.Node] = { p =>
  //     renderBoxed(p).rawNode
  //   }
  //   rawComponent.asInstanceOf[facade.React.StatelessFunctionalComponent[Box[P]]]
  // }

  inline private def create[P, C <: Children, CT[-p, +u] <: CtorType[p, u]]
      // (inline renderBoxed: js.Function1[Box[P] with facade.PropsWithChildren, VdomNode])
      (inline renderBoxed: Box[P] with facade.PropsWithChildren => VdomNode)
      (implicit s: CtorType.Summoner.Aux[Box[P], C, CT])
      : Component[P, CT] = {

    // val rawComponent = createRawBoxed(renderBoxed)

    type Comp = facade.React.StatelessFunctionalComponent[Box[P]]

    val rawComponent: js.Function1[Box[P] with facade.PropsWithChildren, facade.React.Node] = { p =>
      renderBoxed(p).rawNode
    }

    fromRawBoxed[P, C, CT](rawComponent.asInstanceOf[Comp])(s)
  }
}
