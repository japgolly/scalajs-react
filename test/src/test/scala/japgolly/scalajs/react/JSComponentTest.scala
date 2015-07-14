package japgolly.scalajs.react

import japgolly.scalajs.react.test.ReactTestUtils
import org.scalajs.dom.raw.HTMLElement
import utest.framework.TestSuite
import utest._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

object JSComponentTest extends TestSuite {
  def tests = TestSuite {
    'jsComponentType {
      val ref = Ref.toJS[SampleReactComponentM]("ref123")
      val component = ReactComponentB[Unit]("S").render(_ =>
        React.createFactory(SampleReactComponent)(SampleReactComponentProperty(ref = ref, propOne = "123"))
      ).buildU
      val renderedComponent = ReactTestUtils.renderIntoDocument(component())
      val mountedComponent = ref(renderedComponent)
      assert(mountedComponent.map(_.getDOMNode()).map(_.tagName).map(_.toLowerCase).toOption == Some("div"))
      assert(mountedComponent.map(_.props).flatMap(_.propOne).toOption == Some("123"))
      assert(mountedComponent.map(_.getNum()).toOption == Some(0))
      mountedComponent.foreach(_.setNum(2))
      assert(mountedComponent.map(_.getNum()).toOption == Some(2))
    }
  }
}

trait SampleReactComponentProperty extends js.Object {
  val propOne: js.UndefOr[String] = js.native
}

object SampleReactComponentProperty {
  def apply(ref: js.UndefOr[String] = js.undefined, propOne: js.UndefOr[String] = js.undefined): SampleReactComponentProperty = {
    val p = js.Dynamic.literal()

    ref.foreach(p.updateDynamic("ref")(_))
    propOne.foreach(p.updateDynamic("propOne")(_))

    p.asInstanceOf[SampleReactComponentProperty]
  }
}

@JSName("SampleReactComponent")
object SampleReactComponent extends JSComponentType[SampleReactComponentProperty, HTMLElement]

trait SampleReactComponentM extends JSComponentM[SampleReactComponentProperty, HTMLElement] {
  def getNum(): Int = js.native

  def setNum(n: Int): Unit = js.native
}

