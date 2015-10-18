package japgolly.scalajs.react

import scala.scalajs.js

/**
 * A component that takes `props` as an argument and returns the element to render.
 *
 * These components behave just like a React class with only a `render` method defined.
 * Since no component instance is created for a functional component, any ref added to one will evaluate to `null`.
 * Functional components do not have lifecycle methods.
 *
 * "In the future, we’ll also be able to make performance optimizations specific to these components by avoiding
 * unnecessary checks and memory allocations."
 *   - https://facebook.github.io/react/blog/2015/10/07/react-v0.14.html
 *
 * @since React 0.14
 */
@js.native
sealed trait FunctionalComponent[-P] extends js.Any
// type FunctionalComponent[-P] = js.Function1[P, ReactElement]
// ↑ Doesn't work for Scala for two reasons:
// 1) Application returns a `ReactElement` immediately which makes it just a normal function.
// 2) React expects P to be a JavaScript object which Scala values are not.

object FunctionalComponent {

  /**
   * Creates a [[FunctionalComponent]].
   *
   * If `props` is `Unit`, there is also [[ReactComponentB.static]] which also sets `shouldComponentUpdate` to `false`
   * which should be more efficient.
   */
  def apply[P](render: P => ReactElement): FunctionalComponent[P] = {
    val f = (o: WrapObj[P]) => render(o.v)
    val jf = f: js.Function1[WrapObj[P], ReactElement]
    jf.asInstanceOf[FunctionalComponent[P]]
  }

  @inline implicit class Ops[P](private val f: FunctionalComponent[P]) extends AnyVal {
    def apply(props: P) =
      // This is what JSX/Babel does:
      React.createElement(f, WrapObj(props))
  }

  // ===================================================================================================================

  /**
   * A [[FunctionalComponent]] that accepts [[PropsChildren]] in addition to `props`.
   */
  @js.native
  sealed trait WithChildren[-P] extends js.Any

  def withChildren[P](render: (P, PropsChildren) => ReactElement): WithChildren[P] = {
    val f = (o: WrapObj[P]) => render(o.v, o.asInstanceOf[js.Dynamic].children.asInstanceOf[PropsChildren])
    val jf = f: js.Function1[WrapObj[P], ReactElement]
    jf.asInstanceOf[WithChildren[P]]
  }

  @inline implicit class WithChildrenOps[P](private val f: FunctionalComponent.WithChildren[P]) extends AnyVal {
    def apply(props: P, children: ReactNode*) =
      // This is what JSX/Babel does:
      React.createElement(f, WrapObj(props), children: _*)
  }
}
