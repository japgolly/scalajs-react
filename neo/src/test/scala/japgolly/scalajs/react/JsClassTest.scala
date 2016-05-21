package japgolly.scalajs.react

import scalajs.js
import utest._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import org.scalajs.dom.document

object JsClassTest extends TestSuite {

  @js.native
  trait JsProps extends js.Object {
    val name: String
  }

  def JsProps(name: String): JsProps =
    js.Dynamic.literal("name" -> name).asInstanceOf[JsProps]

  val RawClass =
    js.Dynamic.global.ComponentClass.asInstanceOf[raw.ReactClass[JsProps]]

//  val Component =
//    new JsClassCtor(RawClass)

  override def tests = TestSuite {
    'render {

      val element = raw.React.createElement(RawClass, JsProps("Bob"))

      val mountNode = newBodyContainer()
      try {
        val mounted = raw.ReactDOM.render(element, mountNode)
        val n = raw.ReactDOM.findDOMNode(mounted)
        try
          assertOuterHTML(n, "<div>Hello Bob</div>")
        finally
          raw.ReactDOM unmountComponentAtNode n.parentNode
      } finally
        document.body.removeChild(mountNode)


    }
  }
}
