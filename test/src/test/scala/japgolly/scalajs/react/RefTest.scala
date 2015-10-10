package japgolly.scalajs.react

import japgolly.scalajs.react.Addons.ReactCssTransitionGroup
import utest._
import scala.scalajs.js
import org.scalajs.dom.raw._
import vdom.prefix_<^._
import TestUtil._
import japgolly.scalajs.react.test.ReactTestUtils

object RefTest extends TestSuite {

  trait ReactCssTransitionGroupM extends js.Object

  val Static = ReactComponentB.staticN[HTMLElement]("static", <.h2("nice")).buildU
  val StaticHtml = "<h2>nice</h2>"

  val tests = TestSuite {

    'simple {
      val inputRef = Ref[HTMLInputElement]("r")
      val c = ReactComponentB[Unit]("")
        .render(_ => <.div(<.input(^.value := "cool", ^.ref := inputRef)))
        .buildU
      val m = ReactTestUtils renderIntoDocument c()
      inputRef(m).get.getDOMNode().value mustEqual "cool"
    }

    'toScalaComponentByString {
      val ref = "omg"
      val c = ReactComponentB[Unit]("").render(_ => <.div(Static.withRef(ref)())).buildU
      val m = ReactTestUtils renderIntoDocument c()
      val e = m.refs("omg").get.getDOMNode().asInstanceOf[HTMLElement]
      removeReactDataAttr(e.outerHTML) mustEqual StaticHtml
    }

    'toScalaComponentTypesafe {
      val ref = Ref.to(Static, "rushyyz")
      val c = ReactComponentB[Unit]("").render(_ => <.div(Static.withRef(ref)())).buildU
      val m = ReactTestUtils renderIntoDocument c()
      val e = ref(m).get.getDOMNode()
      removeReactDataAttr(e.outerHTML) mustEqual StaticHtml
    }

    'parameterised {
      val r = Ref.param[Int, TopNode](i => s"ref-$i")
      val C = ReactComponentB[Unit]("").render(_ => <.div(<.p(^.ref := r(1), "One"), <.p(^.ref := r(2), "Two"))).buildU
      val c = ReactTestUtils.renderIntoDocument(C())
      r(1)(c).get.getDOMNode().innerHTML mustEqual "One"
      r(2)(c).get.getDOMNode().innerHTML mustEqual "Two"
      assert(r(3)(c).isEmpty)
    }

    'onOwnedComponenets {
      class WB(t: BackendScope[String,_]) { def getName = t.props.runNow() }
      val W = ReactComponentB[String]("").stateless.backend(new WB(_)).render_C(c => <.div(c)).build

      val innerRef = Ref.to(W, "inner")
      val outerRef = Ref.to(W, "outer")
      val innerWName = "My name is IN"
      val outerWName = "My name is OUT"
      var tested = false
      val C = ReactComponentB[Unit]("")
        .render(P => {
          val inner = W.set(ref = innerRef)(innerWName)
          val outer = W.set(ref = outerRef)(outerWName, inner)
          <.div(outer)
         })
        .componentDidMount(scope => Callback {
          innerRef(scope).get.backend.getName mustEqual innerWName
          outerRef(scope).get.backend.getName mustEqual outerWName
          tested = true
        })
        .buildU
      ReactTestUtils renderIntoDocument C()
      assert(tested) // just in case
    }

    'shouldNotHaveRefsOnUnmountedComponents {
      val C = ReactComponentB[Unit]("child").render(_ => <.div()).buildU
      val P = ReactComponentB[Unit]("parent")
        .render(P => C(<.div(^.ref := "test"))) // div here discarded by C.render
        .componentDidMount(scope => Callback(assert(scope.refs("test").get == null)))
    }

    'refToThirdPartyComponents {
      class RB($: BackendScope[_, _]) {
        def test = Callback {
          val transitionRef = Ref.toJS[ReactCssTransitionGroupM]("addon")($)
          assert(transitionRef.isDefined)
        }
      }
      val C = ReactComponentB[Unit]("C")
        .backend(new RB(_))
        .render(_ => <.div(ReactCssTransitionGroup(name = "testname", ref = "addon")()))
        .componentDidMount(_.backend.test)
        .buildU
      ReactTestUtils renderIntoDocument C()
    }

    // Added in React 0.13
    'passCallback {
      var i: js.UndefOr[HTMLInputElement] = js.undefined
      val PC = ReactComponentB[Unit]("PC")
        .render(_ => <.div(<.input(^.value := "yay", ^.ref[HTMLInputElement](r => i = r.getDOMNode()))))
        .buildU
      ReactTestUtils renderIntoDocument PC()
      assert(i.isDefined)
      assert(i.get.value == "yay")
    }

  }
}
