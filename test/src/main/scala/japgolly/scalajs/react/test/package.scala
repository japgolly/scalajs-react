package japgolly.scalajs.react

import org.scalajs.dom.{Element, document}
import scalajs.js
import scalajs.js.{Object, UndefOr}

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

    /** Renders a component then unmounts and cleans up after use.
      *
      * @param intoBody Whether to use [[renderIntoBody()]] or [[ReactTestUtils.renderIntoDocument()]].
      */
    def withRendered[A](c: ReactElement, intoBody: Boolean)
                       (f: ComponentM => A): A =
      if (intoBody)
        withRenderedIntoBody(c)(f)
      else
        withRenderedIntoDocument(c)(f)

    /** Renders a component then unmounts and cleans up after use.
      *
      * @param intoBody Whether to use [[renderIntoBody()]] or [[ReactTestUtils.renderIntoDocument()]].
      */
    def withRendered[P,S,B,N <: TopNode, A](c: ReactComponentU[P,S,B,N], intoBody: Boolean)
                                           (f: ReactComponentM[P, S, B, N] => A): A =
      if (intoBody)
        withRenderedIntoBody(c)(f)
      else
        withRenderedIntoDocument(c)(f)

    /** Renders a component into detached DOM via [[ReactTestUtils.renderIntoDocument()]],
      * then unmounts and cleans up after use.
      */
    def withRenderedIntoDocument[A](c: ReactElement)(f: ComponentM => A): A =
      _withRenderedIntoDocument(* renderIntoDocument c)(_.getDOMNode(), f)

    /** Renders a component into detached DOM via [[ReactTestUtils.renderIntoDocument()]],
      * then unmounts and cleans up after use.
      */
    def withRenderedIntoDocument[P,S,B,N <: TopNode, A](c: ReactComponentU[P,S,B,N])(f: ReactComponentM[P,S,B,N] => A): A =
      _withRenderedIntoDocument(* renderIntoDocument c)(_.getDOMNode(), f)

    /** Renders a component into the document body via [[ReactDOM.render()]],
      * then unmounts and cleans up after use.
      *
      * Unlike [[ReactTestUtils.renderIntoDocument()]], this allows DOM focus to work.
      */
    def withRenderedIntoBody[A](c: ReactElement)(f: ComponentM => A): A =
      _withRenderedIntoBody(ReactDOM.render(c, _))(_.getDOMNode(), f)

    /** Renders a component into the document body via [[ReactDOM.render()]],
      * then unmounts and cleans up after use.
      *
      * Unlike [[ReactTestUtils.renderIntoDocument()]], this allows DOM focus to work.
      */
    def withRenderedIntoBody[P,S,B,N <: TopNode, A](c: ReactComponentU[P,S,B,N])(f: ReactComponentM[P,S,B,N] => A): A =
      _withRenderedIntoBody(ReactDOM.render(c, _))(_.getDOMNode(), f)

    /** Renders a component into the document body via [[ReactDOM.render()]].
      *
      * Unlike [[ReactTestUtils.renderIntoDocument()]], this allows DOM focus to work.
      */
    def renderIntoBody[A](c: ReactElement)(f: ComponentM => A): ComponentM =
      ReactDOM.render(c, _renderIntoBodyContainer())

    /** Renders a component into the document body via [[ReactDOM.render()]].
      *
      * Unlike [[ReactTestUtils.renderIntoDocument()]], this allows DOM focus to work.
      */
    def renderIntoBody[P,S,B,N <: TopNode, A](c: ReactComponentU[P,S,B,N])(f: ReactComponentM[P,S,B,N] => A): ReactComponentM[P,S,B,N] =
      ReactDOM.render(c, _renderIntoBodyContainer())

    /**
      * Turn `&lt;div data-reactroot=""&gt;hello&lt/div&gt;`
      * into `&lt;div&gt;hello&lt/div&gt;`
      */
    def removeReactDataAttr(html: String): String =
      reactDataAttrRegex.replaceAllIn(html, "")
  }

  private def _withRenderedIntoDocument[A, B](a: A)(n: A => TopNode, use: A => B): B = {
    try
      use(a)
    finally
      ReactDOM unmountComponentAtNode n(a).parentNode
  }

  private def _renderIntoBodyContainer(): Element = {
    val cont = document.createElement("div")
    document.body.appendChild(cont)
    cont
  }

  private def _withRenderedIntoBody[A, B](render: Element => A)(n: A => TopNode, use: A => B): B = {
    val parent = _renderIntoBodyContainer()
    try {
      val a = render(parent)
      try
        use(a)
      finally
        ReactDOM unmountComponentAtNode n(a).parentNode
    } finally
      document.body.removeChild(parent)
  }

  @inline implicit final class ReactTestExt_Mounted[N <: TopNode](private val c: CompScope.Mounted[N]) extends AnyVal {
    def outerHtmlWithoutReactDataAttr(): String =
      ReactTestUtils removeReactDataAttr c.getDOMNode().outerHTML
  }
}
