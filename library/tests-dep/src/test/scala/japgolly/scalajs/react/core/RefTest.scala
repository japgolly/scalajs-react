package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import utest._

object RefTest extends TestSuite {

  object TestJs {
    val InnerJs = JsComponentEs6STest.Component

    def refViaRef(): Unit = {
      var backend: Backend = null
      class Backend {
        backend = this
        val ref = Ref.toJsComponent(InnerJs)
        def render = <.div(ref.component())
      }
      val C = ScalaComponent.builder[Unit]("X").backend(_ => new Backend()).render(_.backend.render).build
      ReactTestUtils.withRenderedSync(C()) { _ =>
        backend.ref.unsafeGet().raw.inc() // compilation and evaluation without error is test enough
      }
    }

    def refViaComp(): Unit = {
      var backend: Backend = null
      class Backend {
        backend = this
        val ref = Ref.toJsComponent(InnerJs)
        def render = <.div(InnerJs.withRef(ref)())
      }
      val C = ScalaComponent.builder[Unit]("X").backend(_ => new Backend()).render(_.backend.render).build
      ReactTestUtils.withRenderedSync(C()) { _ =>
        backend.ref.unsafeGet().raw.inc() // compilation and evaluation without error is test enough
      }
    }

    def refAndKey(): Unit = {
      var backend: Backend = null
      class Backend {
        backend = this
        val ref = Ref.toJsComponent(InnerJs)
        def render = <.div(ref.component.withKey(555555555)())
      }
      val C = ScalaComponent.builder[Unit]("X").backend(_ => new Backend()).render(_.backend.render).build
      ReactTestUtils.withRenderedSync(C()) { _ =>
        backend.ref.unsafeGet().raw.inc() // compilation and evaluation without error is test enough
      }
    }
  }

  object TestRefForwarding {

    object ScalaToScala {
      private class InnerScalaBackend($: BackendScope[Int, Unit]) {
        def gimmeHtmlNow() = $.getDOMNode.runNow().asMounted().asHtml().outerHTML
        def render(p: Int) = <.h1(s"Scala$p")
      }

      private lazy val InnerScala = ScalaComponent.builder[Int]("Scala")
        .backend(new InnerScalaBackend(_))
        .renderP(_.backend.render(_))
        .build

      private lazy val Forwarder = React.forwardRef.toScalaComponent(InnerScala)[String]((label, ref) =>
        <.div(label, InnerScala.withOptionalRef(ref)(123)))

      def withRef() = {
        var backend: Backend = null
        class Backend {
          backend = this
          val ref = Ref.toScalaComponent(InnerScala)
          def render = Forwarder.withRef(ref)("noice")
        }
        val C = ScalaComponent.builder[Unit]("X").backend(_ => new Backend()).render(_.backend.render).build
        ReactTestUtils.withRenderedSync(C()) { t =>
          assertRendered(t.root.asElement(), "<div><div>noice<h1>Scala123</h1></div></div>")
          assertEq(backend.ref.get.runNow().map(_.backend.gimmeHtmlNow()), Some("<h1>Scala123</h1>"))
        }
      }

      def withRef2() = {
        var backend: Backend = null
        class Backend {
          backend = this
          val ref = Ref.toScalaComponent[Int, Unit, InnerScalaBackend]
          def render = Forwarder.withRef(ref)("noice")
        }
        val C = ScalaComponent.builder[Unit]("X").backend(_ => new Backend()).render(_.backend.render).build
        ReactTestUtils.withRenderedSync(C()) { t =>
          assertRendered(t.root.asElement(), "<div><div>noice<h1>Scala123</h1></div></div>")
          assertEq(backend.ref.get.runNow().map(_.backend.gimmeHtmlNow()), Some("<h1>Scala123</h1>"))
        }
      }

      def mappedRef() = {
        var backend: Backend = null
        class Backend {
          backend = this
          val ref = Ref.toScalaComponent(InnerScala).map(_.backend.gimmeHtmlNow())
          def render = Forwarder.withRef(ref)("noice")
        }
        val C = ScalaComponent.builder[Unit]("X").backend(_ => new Backend()).render(_.backend.render).build
        ReactTestUtils.withRenderedSync(C()) { t =>
          assertRendered(t.root.asElement(), "<div><div>noice<h1>Scala123</h1></div></div>")
          assertEq(backend.ref.get.runNow(), Some("<h1>Scala123</h1>"))
        }
      }
    }

    object ScalaToJs {
      val InnerJs = JsComponentEs6STest.Component

      private lazy val Forwarder = React.forwardRef.toJsComponent(InnerJs)[String]((label, ref) =>
        <.div(label, InnerJs.withOptionalRef(ref)()))

      def withRef() = {
        var backend: Backend = null
        class Backend {
          backend = this
          val ref = Ref.toJsComponent(InnerJs)
          def render = Forwarder.withRef(ref)("noice")
        }
        val C = ScalaComponent.builder[Unit]("X").backend(_ => new Backend()).render(_.backend.render).build
        ReactTestUtils.withRenderedSync(C()) { t =>
          assertRendered(t.root.asElement(), "<div><div>noice<div>State = 123 + 500</div></div></div>")
          val r = backend.ref.get.runNow().get
          assertEq(r.getDOMNode.toHtml.map(_.outerHTML), Some("<div>State = 123 + 500</div>"))
          assertEq(r.state.num2, 500)
        }
      }
    }
  }

  override def tests = Tests {

    "jsComponent" - {
      import TestJs._
      "refViaComp" - refViaComp()
      "refViaRef"  - refViaRef()
      "refAndKey"  - refAndKey()
    }

    "scalaToScala" - {
      import TestRefForwarding.ScalaToScala._
      "withRef"    - withRef()
      "withRef2"   - withRef2()
      "mappedRef"  - mappedRef()
    }

    "scalaToJs" - {
      import TestRefForwarding.ScalaToJs._
      "withRef"    - withRef()
    }
  }
}
