package japgolly.scalajs.react

import scala.scalajs.js
import scala.scalajs.js.{Object, UndefOr}

package object test {

  @js.native
  sealed trait ComponentClass extends Object

  @inline final implicit def autoComponentClassFromScalaComponent(c: ReactComponentC[_, _, _, _]): ComponentClass =
    c.reactClass.asInstanceOf[ComponentClass]

  final type ComponentM = ReactComponentM_[TopNode]

  @js.native
  sealed trait ReactOrDomNode extends Object

  @inline final implicit def autoReactOrDomNodeN(n: TopNode): ReactOrDomNode =
    n.asInstanceOf[ReactOrDomNode]
  @inline final implicit def autoReactOrDomNodeU(c: ReactElement): ReactOrDomNode =
    c.asInstanceOf[ReactOrDomNode]
  @inline final implicit def autoReactOrDomNodeM[N <: TopNode](c: ReactComponentM_[N]): ReactOrDomNode =
    autoReactOrDomNodeN(ReactDOM findDOMNode c)

  @inline final implicit def RTUSChangeEventData  (d: ChangeEventData  ): Object = d.toJs
  @inline final implicit def RTUSKeyboardEventData(d: KeyboardEventData): Object = d.toJs
  @inline final implicit def RTUSMouseEventData   (d: MouseEventData   ): Object = d.toJs

  @inline final implicit def autoUnboxRefsInTests[T <: TopNode](r: UndefOr[ReactComponentM_[T]]) = r.get
  @inline final implicit def autoUnboxRefsInTestsC[T <: TopNode](r: UndefOr[ReactComponentM_[T]]): ReactOrDomNode = r.get

//  implicit final class RTUSimulateExt(private val u: Simulate) extends AnyVal {
//    def change(t: ReactOrDomNode, newValue: String) = u.change(t, ChangeEventData(value = newValue))
//  }

  private val reactDataAttrRegex = """\s+data-react\S*?\s*?=\s*?".*?"""".r

  implicit class ReactTestUtilsScalaExt(private val * : ReactTestUtils) extends AnyVal {
    def withRenderedIntoDocument[A](c: ReactElement)(f: ComponentM => A): A =
      _withRenderedIntoDocument(c)(* renderIntoDocument _)(_.getDOMNode(), f)

    def withRenderedIntoDocument[P,S,B,N <: TopNode, A](c: ReactComponentU[P,S,B,N])(f: ReactComponentM[P,S,B,N] => A): A =
      _withRenderedIntoDocument(c)(* renderIntoDocument _)(_.getDOMNode(), f)


    /**
      * Turn `&lt;div data-reactid=".0"&gt;hello&lt/div&gt;`
      * into `&lt;div&gt;hello&lt/div&gt;`
      */
    def removeReactDataAttr(html: String): String =
      reactDataAttrRegex.replaceAllIn(html, "")
  }

  private def _withRenderedIntoDocument[A, B, C](a: A)(f: A => B)(n: B => TopNode, g: B => C): C = {
    val b = f(a)
    try
      g(b)
    finally
      ReactDOM unmountComponentAtNode n(b).parentNode
  }

  @inline implicit final class ReactTestExt_Mounted[N <: TopNode](private val c: CompScope.Mounted[N]) extends AnyVal {
    def outerHtmlWithoutReactDataAttr(): String =
      ReactTestUtils removeReactDataAttr c.getDOMNode().outerHTML
  }
}
