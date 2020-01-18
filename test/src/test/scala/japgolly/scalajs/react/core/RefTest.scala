package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.{html, svg}
import scala.scalajs.js
import scala.scalajs.js.annotation._
import utest._

object RefTest extends TestSuite {

  val attr = "data-ah"
  val V = "!"

  private def assertRefUsageR[R](newRef: => R)(renderFn: R => VdomNode, refHtml: R => String)
                                (expectedRefHtml: String, expectedHtml: String => String) = {
    class Backend {
      val ref = newRef
      def render = renderFn(ref)
    }
    val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].build
    ReactTestUtils.withNewBodyElement { mountNode =>
      val mounted = C().renderIntoDOM(mountNode)
      assertRendered(mounted.getDOMNode.asMounted().asHtml(), expectedHtml(expectedRefHtml))
      assertEq(refHtml(mounted.backend.ref), expectedRefHtml)
    }
  }

  private def assertRefUsage[R](renderFn: Ref.Simple[R] => VdomNode, refHtml: R => String)
                               (expectedRefHtml: String, expectedHtml: String => String) =
    assertRefUsageR(Ref[R])(renderFn, r => refHtml(r.get.asCallback.runNow().getOrElse(sys error "Ref = None")))(
      expectedRefHtml, expectedHtml)

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

    def refViaRef(): Unit = {
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

    def refViaComp(): Unit = {
      class Backend {
        val ref = Ref.toScalaComponent(InnerScala.C)
        def render = <.div(InnerScala.C.withRef(ref)(123))
      }
      val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].build
      ReactTestUtils.withNewBodyElement { mountNode =>
        val mounted = C().renderIntoDOM(mountNode)
        assertEq(mounted.backend.ref.unsafeGet().backend.secret, 666)
      }
    }

    def refAndKey(): Unit = {
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

    def refViaRef(): Unit = {
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

    def refViaComp(): Unit = {
      class Backend {
        val ref = Ref.toJsComponent(InnerJs)
        def render = <.div(InnerJs.withRef(ref)())
      }
      val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].build
      ReactTestUtils.withNewBodyElement { mountNode =>
        val mounted = C().renderIntoDOM(mountNode)
        mounted.backend.ref.unsafeGet().raw.inc() // compilation and evaluation without error is test enough
      }
    }

    def refAndKey(): Unit = {
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

  object TestRefForwarding {

    object JsToVdom {

      @JSGlobal("FancyButton")
      @js.native
      private object RawComp extends js.Object

      private val Forwarder = JsForwardRefComponent[Null, Children.Varargs, html.Button](RawComp)

      def nullary() = assertRender(Forwarder(), "<div><button class=\"FancyButton\"></button></div>")

      def children() = assertRender(Forwarder(<.br, <.hr), "<div><button class=\"FancyButton\"><br/><hr/></button></div>")

      def ref() =
        assertRefUsage[html.Button](
          Forwarder.withRef(_)("ok"), _.outerHTML)(
          "<button class=\"FancyButton\">ok</button>", "<div>" + _ + "</div>")

      def wideRef() = assertCompiles(Forwarder.withRef(Ref[html.Element])("ok"))

      def narrowRef() = {
        def X = JsForwardRefComponent[Null, Children.Varargs, html.Element](???)
        compileError(""" X.withRef(Ref[html.Button])("ok") """)
        ()
      }

      def scalaRef() = {
        def ref = Ref.toScalaComponent(TestScala.InnerScala.C)
        compileError(""" Forwarder.withRef(ref)("ok") """)
        ()
      }
    }

    object ScalaToVdom {

      private val Forwarder = React.forwardRef.justChildren[html.Button]((c, r) =>
        <.div(<.button.withRef(r)(^.cls := "fancy", c)))

      def nullary() = assertRender(Forwarder(), "<div><button class=\"fancy\"></button></div>")

      def children() = assertRender(Forwarder(<.br, <.hr), "<div><button class=\"fancy\"><br/><hr/></button></div>")

      def refC() =
        assertRefUsage[html.Button](
          Forwarder.withRef(_)("ok"), _.outerHTML)(
          "<button class=\"fancy\">ok</button>", "<div>" + _ + "</div>")

      def wideRef() = {
        assertCompiles(Forwarder.withRef(Ref[html.Element])("ok"))
        assertCompiles(Forwarder.withRef(Ref[Any])("ok"))
      }

      def narrowRef() = {
        def X = React.forwardRef[String, html.Element]((s, r) => <.div(<.button.withRef(r)(s)))
        compileError(""" X.withRef(Ref[html.Button])("ok") """)
        ()
      }

      def scalaRef() = {
        def ref = Ref.toScalaComponent(TestScala.InnerScala.C)
        compileError(""" Forwarder.withRef(ref)("ok") """)
        ()
      }

      def unmounted() = {
        assertEq(Forwarder().ref, None)
        val ref = Ref[html.Button]
        assertEq(Forwarder.withRef(ref)().ref.map(_.raw), Some(ref.raw))
      }

      private val Forwarder2 = React.forwardRef[Int, html.Span]((i, r) =>
        <.div(<.span.withRef(r)(s"${i}² = ${i * i}")))

      def unary() = assertRender(Forwarder2(3), "<div><span>3² = 9</span></div>")

      def refP() =
        assertRefUsage[html.Span](
          Forwarder2.withRef(_)(3), _.outerHTML)(
          "<span>3² = 9</span>", "<div>" + _ + "</div>")
    }

    object ScalaToScala {
      private class InnerScalaBackend($: BackendScope[Int, Unit]) {
        def gimmeHtmlNow() = $.getDOMNode.runNow().asMounted().asHtml().outerHTML
        def render(p: Int) = <.h1(s"Scala$p")
      }
      private lazy val InnerScala = ScalaComponent.builder[Int]("Scala").renderBackend[InnerScalaBackend].build

      private lazy val Forwarder = React.forwardRef.toScalaComponent(InnerScala)[String]((label, ref) =>
        <.div(label, InnerScala.withRef(ref)(123)))

      def withoutRef() = assertRender(Forwarder("hey"), "<div>hey<h1>Scala123</h1></div>")

      def withRef() = {
        class Backend {
          val ref = Ref.toScalaComponent(InnerScala)
          def render = Forwarder.withRef(ref)("noice")
        }
        val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].build
        ReactTestUtils.withNewBodyElement { mountNode =>
          val mounted = C().renderIntoDOM(mountNode)
          assertRendered(mounted.getDOMNode.asMounted().asHtml(), "<div>noice<h1>Scala123</h1></div>")
          assertEq(mounted.backend.ref.get.asCallback.runNow().map(_.backend.gimmeHtmlNow()), Some("<h1>Scala123</h1>"))
        }
      }

      def withRef2() = {
        class Backend {
          val ref = Ref.toScalaComponent[Int, Unit, InnerScalaBackend]
          def render = Forwarder.withRef(ref)("noice")
        }
        val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].build
        ReactTestUtils.withNewBodyElement { mountNode =>
          val mounted = C().renderIntoDOM(mountNode)
          assertRendered(mounted.getDOMNode.asMounted().asHtml(), "<div>noice<h1>Scala123</h1></div>")
          assertEq(mounted.backend.ref.get.asCallback.runNow().map(_.backend.gimmeHtmlNow()), Some("<h1>Scala123</h1>"))
        }
      }

      def mappedRef() = {
        class Backend {
          val ref = Ref.toScalaComponent(InnerScala).map(_.backend.gimmeHtmlNow())
          def render = Forwarder.withRef(ref)("noice")
        }
        val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].build
        ReactTestUtils.withNewBodyElement { mountNode =>
          val mounted = C().renderIntoDOM(mountNode)
          assertRendered(mounted.getDOMNode.asMounted().asHtml(), "<div>noice<h1>Scala123</h1></div>")
          assertEq(mounted.backend.ref.get.asCallback.runNow(), Some("<h1>Scala123</h1>"))
        }
      }

      def wrongScala() = {
        def Scala2 = ScalaComponent.builder[Int]("Scala2").renderStatic(<.div).build
        def ref = Ref.toScalaComponent(Scala2)
        compileError(""" Forwarder.withRef(ref)("nah mate") """)
        ()
      }

      def vdomRef() = {
        def ref = Ref[html.Button]
        compileError(""" Forwarder.withRef(ref)("nah mate") """)
        ()
      }
    }

    object ScalaToJs {
      val InnerJs = JsComponentEs6STest.Component

      private lazy val Forwarder = React.forwardRef.toJsComponent(InnerJs)[String]((label, ref) =>
        <.div(label, InnerJs.withRef(ref)()))

      def withoutRef() = assertRender(Forwarder("hey"), "<div>hey<div>State = 123 + 500</div></div>")

      def withRef() = {
        class Backend {
          val ref = Ref.toJsComponent(InnerJs)
          def render = Forwarder.withRef(ref)("noice")
        }
        val C = ScalaComponent.builder[Unit]("X").renderBackend[Backend].build
        ReactTestUtils.withNewBodyElement { mountNode =>
          val mounted = C().renderIntoDOM(mountNode)
          assertRendered(mounted.getDOMNode.asMounted().asHtml(), "<div>noice<div>State = 123 + 500</div></div>")
          val r = mounted.backend.ref.get.asCallback.runNow().get
          assertEq(r.getDOMNode.toHtml.map(_.outerHTML), Some("<div>State = 123 + 500</div>"))
          assertEq(r.state.num2, 500)
        }
      }

      def wrongJs() = {
        type P = JsComponentEs6PTest.JsProps
        type S = Null
        type R = JsComponent.RawMounted[P, S]
        def ref = Ref.toJsComponent(JsComponentEs6PTest.Component)
        compileError(""" Forwarder.withRef(ref)("nah mate") """)
        ()
      }

      def vdomRef() = {
        def ref = Ref[html.Button]
        compileError(""" Forwarder.withRef(ref)("nah mate") """)
        ()
      }
    }
  }

  override def tests = Tests {

    "empty" - {
      assertEq[Option[Unit]](Ref[Unit].get.asCallback.runNow(), None)
    }
    "htmlTag" - testHtmlTag()
    "svgTag"  - testSvgTag()
    "scalaComponent" - {
      import TestScala._
      "refViaComp" - refViaComp()
      "refViaRef"  - refViaRef()
      "refAndKey"  - refAndKey()
    }
    "jsComponent" - {
      import TestJs._
      "refViaComp" - refViaComp()
      "refViaRef"  - refViaRef()
      "refAndKey"  - refAndKey()
    }
    "forward" - {
      "jsToVdom" - {
        import TestRefForwarding.JsToVdom._
        "nullary"   - nullary()
        "children"  - children()
        "ref"       - ref()
        "wideRef"   - wideRef()
        "narrowRef" - narrowRef()
        "scalaRef"  - scalaRef()
      }
      "scalaToVdom" - {
        import TestRefForwarding.ScalaToVdom._
        "nullary"   - nullary()
        "unary"     - unary()
        "children"  - children()
        "refP"      - refP()
        "refC"      - refC()
        "wideRef"   - wideRef()
        "narrowRef" - narrowRef()
        "scalaRef"  - scalaRef()
        "unmounted" - unmounted()
      }
      "scalaToScala" - {
        import TestRefForwarding.ScalaToScala._
        "withoutRef" - withoutRef()
        "withRef"    - withRef()
        "withRef2"   - withRef2()
        "mappedRef"  - mappedRef()
        "wrongScala" - wrongScala()
        "vdomRef"    - vdomRef()
      }
      "scalaToJs" - {
        import TestRefForwarding.ScalaToJs._
        "withoutRef" - withoutRef()
        "withRef"    - withRef()
        "wrongJs"    - wrongJs()
        "vdomRef"    - vdomRef()
      }
    }
  }
}
