package japgolly.scalajs.react

import utest._
import vdom.prefix_<^._
import TestUtil._

object FunctionalComponentTest extends TestSuite {

  val IntComp = FunctionalComponent[Int](i => <.code(s"$i² = ${i*i}"))
  val IntExample = ReactComponentB[Unit]("")
    .render(_ => <.div(IntComp(7)))
    .build

  case class AddCmd(x: Int, y: Int)
  val AddComp = FunctionalComponent[AddCmd]{a =>
    import a._
    <.code(s"$x + $y = ${x+y}")
  }
  val AddExample = ReactComponentB[Unit]("")
    .render(_ => <.div(AddComp(AddCmd(11, 8))))
    .build

  val KidsComp = FunctionalComponent.withChildren[Int]((i,pc) => <.div(s"i=$i", pc))
  val KidsExample = ReactComponentB[Unit]("")
    .render(_ => <.div(KidsComp(3, <.i("good"))))
    .build

  override def tests = TestSuite {
    'int          { IntExample() shouldRender "<div><code>7² = 49</code></div>" }
    'caseClass    { AddExample() shouldRender "<div><code>11 + 8 = 19</code></div>" }
    'withChildren { KidsExample() shouldRender "<div><div>i=3<i>good</i></div></div>" }
  }
}
