package japgolly.scalajs.react.core

import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.test.TestUtil._

object ScalaFnComponentTest extends TestSuite {

  val IntProps = ScalaFnComponent[Int](i => <.code(s"$i² = ${i * i}"))

  final case class Add(x: Int, y: Int)

  val CaseClassProps = ScalaFnComponent[Add] { a =>
    import a._
    <.code(s"$x + $y = ${x + y}")
  }

  val WithChildren = ScalaFnComponent.withChildren[Int]((i, c) => <.div(s"i=$i", c))

  val JustChildren = ScalaFnComponent.justChildren(c => <.h4(c))

  val c1 = <.i("good")
  val c2 = "222"

  override def tests = Tests {
    "int"          - assertRender(IntProps(7),                "<code>7² = 49</code>")
    "caseClass"    - assertRender(CaseClassProps(Add(11, 8)), "<code>11 + 8 = 19</code>")
    "withChild"    - assertRender(WithChildren(3)(c1),        "<div>i=3<i>good</i></div>")
    "withChildren" - assertRender(WithChildren(3)(c1, c2),    "<div>i=3<i>good</i>222</div>")
    "justChild"    - assertRender(JustChildren(c1),           "<h4><i>good</i></h4>")
    "justChildren" - assertRender(JustChildren(c1, c2),       "<h4><i>good</i>222</h4>")

    "memo" - {
      var rendered = 0
      implicit def reusabilityAdd: Reusability[Add] = Reusability.by(_.x)
      val c = React.memo(ScalaFnComponent[Add] { _ =>
        rendered += 1
        <.br
      })
      val w = ScalaComponent.builder[Unit]("").initialState(Add(1, 1)).render_S(c(_)).build
      ReactTestUtils.withRenderedIntoDocument(w()) { m =>
        assert(rendered == 1)
        m.setState(Add(1, 2))
        assert(rendered == 1)
        m.setState(Add(2, 2))
        assert(rendered == 2)
        m.setState(Add(2, 3))
        assert(rendered == 2)
        m.setState(Add(2, 2))
        assert(rendered == 2)
        m.setState(Add(1, 2))
        assert(rendered == 3)
      }
    }
  }
}
