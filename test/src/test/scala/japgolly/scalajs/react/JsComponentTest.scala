package japgolly.scalajs.react

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.{Simulation, ReactTestUtils}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.raw.HTMLElement
import utest.framework.TestSuite
import utest._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

object JsComponentTest extends TestSuite {
  val ref = Ref.toJS[SampleReactComponentM]("ref123")
  val p1 = Ref[HTMLElement]("p1")
  val p2 = Ref[HTMLElement]("p2")

  // TODO Callback: review ↓
  class XxxBackend(scope: BackendScope[Unit, Unit]) {
    def modifyOne(i: Int) = Callback {
      ref(scope).foreach(_.setNum(i))
    }

    def modifyTwo(i: Int) = Callback {
      ref(scope).foreach(c => c.setState(SampleReactComponentState(c.state)(num2 = i)))
    }
  }

  def tests = TestSuite {
    'jsComponentType {
      val component = ReactComponentB[Unit]("S").
        stateless.
        backend(new XxxBackend(_)).
        render(scope =>
        <.div(
          React.createFactory(SampleReactComponent)(SampleReactComponentProperty(ref = ref, propOne = "123")),
          <.p(^.ref := p1, ^.onClick --> scope.backend.modifyOne(10)),
          <.p(^.ref := p2, ^.onClick --> scope.backend.modifyTwo(20))
        )).buildU
      val renderedComponent = ReactTestUtils.renderIntoDocument(component())
      val mountedComponent = ref(renderedComponent)
      assert(mountedComponent.map(ReactDOM.findDOMNode(_).tagName.toLowerCase).toOption == Some("div"))
      assert(mountedComponent.map(ReactDOM findDOMNode _ outerHTML).filter(_ contains "123").isDefined)
      assert(mountedComponent.map(_.props).flatMap(_.propOne).toOption == Some("123"))
      assert(mountedComponent.map(_.getNum()).toOption == Some(0))
      assert(mountedComponent.map(_.state.num).toOption == Some(0))
      assert(mountedComponent.map(_.state.num2).toOption == Some(0))
      mountedComponent.foreach(_.setNum(2))
      assert(mountedComponent.map(_.getNum()).toOption == Some(2))
      assert(mountedComponent.map(_.state.num).toOption == Some(2))
      assert(mountedComponent.map(_.state.num2).toOption == Some(0))
      mountedComponent.map(c => c.setState(SampleReactComponentState(c.state)(num2 = 1)))
      assert(mountedComponent.map(_.getNum()).toOption == Some(2))
      assert(mountedComponent.map(_.state.num).toOption == Some(2))
      assert(mountedComponent.map(_.state.num2).toOption == Some(1))
      mountedComponent.map(c => c.setState(SampleReactComponentState(c.state)(num = 3, num2 = 2)))
      assert(mountedComponent.map(_.getNum()).toOption == Some(3))
      assert(mountedComponent.map(_.state.num).toOption == Some(3))
      assert(mountedComponent.map(_.state.num2).toOption == Some(2))
      Simulation(ReactTestUtils.Simulate.click(_)).run(p1(renderedComponent).get)
      assert(mountedComponent.map(_.getNum()).toOption == Some(10))
      assert(mountedComponent.map(_.state.num).toOption == Some(10))
      assert(mountedComponent.map(_.state.num2).toOption == Some(2))
      Simulation(ReactTestUtils.Simulate.click(_)).run(p2(renderedComponent).get)
      assert(mountedComponent.map(_.getNum()).toOption == Some(10))
      assert(mountedComponent.map(_.state.num).toOption == Some(10))
      assert(mountedComponent.map(_.state.num2).toOption == Some(20))
    }
  }
}

@js.native
trait SampleReactComponentProperty extends js.Object {
  val propOne: js.UndefOr[String] = js.native
}

@js.native
trait SampleReactComponentState extends js.Object {
  val num: js.UndefOr[Int] = js.native
  val num2: js.UndefOr[Int] = js.native
}

object SampleReactComponentProperty {
  def apply(ref: js.UndefOr[String] = js.undefined, propOne: js.UndefOr[String] = js.undefined): SampleReactComponentProperty = {
    val p = js.Dynamic.literal()

    ref.foreach(p.updateDynamic("ref")(_))
    propOne.foreach(p.updateDynamic("propOne")(_))

    p.asInstanceOf[SampleReactComponentProperty]
  }
}

object SampleReactComponentState {
  def apply(prevState: SampleReactComponentState)(
    num: js.UndefOr[Int] = js.undefined,
    num2: js.UndefOr[Int] = js.undefined): SampleReactComponentState = {
    val p = js.Dynamic.literal()

    num.orElse(prevState.num).foreach(p.updateDynamic("num")(_))
    num2.orElse(prevState.num2).foreach(p.updateDynamic("num2")(_))

    p.asInstanceOf[SampleReactComponentState]
  }
}

@JSName("SampleReactComponent")
@js.native
object SampleReactComponent extends JsComponentType[SampleReactComponentProperty, SampleReactComponentState, HTMLElement]

@js.native
trait SampleReactComponentM extends JsComponentM[SampleReactComponentProperty, SampleReactComponentState, HTMLElement] {
  def getNum(): Int = js.native

  def setNum(n: Int): Unit = js.native
}
