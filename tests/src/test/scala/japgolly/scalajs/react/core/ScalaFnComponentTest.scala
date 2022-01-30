package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import sourcecode.Line
import utest._

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

    "reuse" - {
      var renders = 0
      val F = ScalaFnComponent.withReuse[Int] { p =>
        renders += 1
        <.span(p)
      }
      val C = ScalaComponent.builder[Int].render_P(F(_)).build
      ReactTestUtils.withRenderedIntoBody(C(7)) { (m, p) =>
        def test(expectedRenders: Int, expectedHtml: Int)(implicit q: Line): Unit = {
          val a = (renders, p.innerHTML.trim)
          val e = (expectedRenders, s"<span>$expectedHtml</span>")
          assertEq(a, e)
        }
        test(1, 7)
        ReactTestUtils.replaceProps(C, m)(7)
        test(1, 7)
        ReactTestUtils.replaceProps(C, m)(6)
        test(2, 6)
      }
    }

    // See https://github.com/japgolly/scalajs-react/issues/1027
    "reuseNever" - {
      implicit def reusability: Reusability[Int] = Reusability.never
      var renders = 0
      val F = ScalaFnComponent.withReuse[Int] { p =>
        renders += 1
        <.span(p)
      }
      val C = ScalaComponent.builder[Int].render_P(F(_)).build
      ReactTestUtils.withRenderedIntoBody(C(7)) { (m, p) =>
        def test(expectedRenders: Int, expectedHtml: Int)(implicit q: Line): Unit = {
          val a = (renders, p.innerHTML.trim)
          val e = (expectedRenders, s"<span>$expectedHtml</span>")
          assertEq(a, e)
        }
        test(1, 7)
        ReactTestUtils.replaceProps(C, m)(7)
        test(2, 7)
        ReactTestUtils.replaceProps(C, m)(6)
        test(3, 6)
      }
    }

  }
}
