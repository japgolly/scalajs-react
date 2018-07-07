package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.{html, svg}
import utest._

object RefTest extends TestSuite {

  val attr = "data-ah"
  val V = "!"

  def testHtmlTag(): Unit = {
    class Backend {
      val input = Ref[html.Input]
      def addDataAttr = input.foreach(_.setAttribute(attr, V))
      def render = <.div(<.input.text(^.defaultValue := "2").withRef(input))
    }
    val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].componentDidMount(_.backend.addDataAttr).build
    ReactTestUtils.withNewBodyElement { mountNode =>
      val mounted = C().renderIntoDOM(mountNode)
      assertEq(mounted.getDOMNode.asMounted().asElement().querySelector("input").getAttribute(attr), V)
    }
  }

  def testSvgTag(): Unit = {
    import japgolly.scalajs.react.vdom.svg_<^._
    class Backend {
      val circle = Ref[svg.Circle]
      def addDataAttr = circle.foreach(_.setAttribute(attr, V))
      def render = <.svg(<.circle().withRef(circle))
    }
    val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].componentDidMount(_.backend.addDataAttr).build
    ReactTestUtils.withNewBodyElement { mountNode =>
      val mounted = C().renderIntoDOM(mountNode)
      assertEq(mounted.getDOMNode.asMounted().asElement().querySelector("circle").getAttribute(attr), V)
    }
  }

  object TestScala {
    object InnerScala {
      class B { def secret = 666 }
      val C = ScalaComponent.builder[Int]("X").backend(_ => new B).render_P(i => <.p(s"Hello $i")).build
    }

    def testRef(): Unit = {
      class Backend {
        val ref = Ref.toScalaComponent(InnerScala.C)
        def render = <.div(ref.component(123))
      }
      val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].build
      ReactTestUtils.withNewBodyElement { mountNode =>
        val mounted = C().renderIntoDOM(mountNode)
        assertEq(mounted.backend.ref.unsafeGet().backend.secret, 666)
      }
    }

    def testRefAndKey(): Unit = {
      class Backend {
        val ref = Ref.toScalaComponent(InnerScala.C)
        def render = <.div(ref.component.withKey(555555555)(123))
      }
      val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].build
      ReactTestUtils.withNewBodyElement { mountNode =>
        val mounted = C().renderIntoDOM(mountNode)
        assertEq(mounted.backend.ref.unsafeGet().backend.secret, 666)
      }
    }
  }

  object TestJs {
    val InnerJs = JsComponentEs6STest.Component

    def testRef(): Unit = {
      class Backend {
        val ref = Ref.toJsComponent(InnerJs)
        def render = <.div(ref.component())
      }
      val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].build
      ReactTestUtils.withNewBodyElement { mountNode =>
        val mounted = C().renderIntoDOM(mountNode)
        mounted.backend.ref.unsafeGet().raw.inc() // compilation and evaluation without error is test enough
      }
    }

    def testRefAndKey(): Unit = {
      class Backend {
        val ref = Ref.toJsComponent(InnerJs)
        def render = <.div(ref.component.withKey(555555555)())
      }
      val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].build
      ReactTestUtils.withNewBodyElement { mountNode =>
        val mounted = C().renderIntoDOM(mountNode)
        mounted.backend.ref.unsafeGet().raw.inc() // compilation and evaluation without error is test enough
      }
    }
  }

  override def tests = Tests {
    'htmlTag - testHtmlTag()
    'svgTag - testSvgTag()
    'scalaComponent - {
      'ref - TestScala.testRef()
      'refAndKey - TestScala.testRefAndKey()
    }
    'jsComponent - {
      'ref - TestJs.testRef()
      'refAndKey - TestJs.testRefAndKey()
    }
  }
}
