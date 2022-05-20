package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import japgolly.scalajs.react.facade.SyntheticEvent
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import org.scalajs.dom.{HTMLElement, HTMLInputElement, document}
import scala.annotation.nowarn
import scala.concurrent.Promise
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import sizzle.Sizzle
import utest._

object TestTest extends TestSuite {

  lazy val A = ScalaComponent.builder[Unit]("A").render_C(c => <.p(^.cls := "AA", c)).build
  lazy val B = ScalaComponent.builder[Unit]("B").renderStatic(<.p(^.cls := "BB", "hehehe")).build
  lazy val rab = LegacyReactTestUtils.renderIntoDocument(A(B()))

  val inputRef = Ref[HTMLInputElement]

  lazy val IC = ScalaComponent.builder[Unit]("IC").initialState(true).renderS(($,s) => {
    val ch = (_: ReactEvent) => $.modState(x => !x)
    <.label(
      <.input.checkbox(^.checked := s, ^.onClick ==> ch).withRef(inputRef),
      <.span(s"s = $s")
    )
  }).build

  lazy val IT = ScalaComponent.builder[Unit]("IT").initialState("NIL").renderS(($,s) => {
    val ch = (e: ReactEventFromInput) => $.setState(e.target.value.toUpperCase)
    <.input.text(^.value := s, ^.onChange ==> ch)
  }).build

  class CP {
    var prev = "none"
    def render(p: String) = <.div(s"$prev → $p")
  }
  @nowarn("cat=deprecation")
  val CP = ScalaComponent.builder[String]("asd")
    .backend(_ => new CP)
    .renderBackend
    .componentWillReceiveProps(i => Callback(i.backend.prev = i.currentProps))
    .build

  val tests = Tests {

    "findRenderedDOMComponentWithClass" - {
      val x = LegacyReactTestUtils.findRenderedDOMComponentWithClass(rab, "BB")
      val n = x.getDOMNode.asMounted().asElement()
      assert(n.matchesBy[HTMLElement](_.className == "BB"))
    }

    "findRenderedComponentWithType" - {
      val n = LegacyReactTestUtils.findRenderedComponentWithType(rab, B).getDOMNode.asMounted().asElement()
      assert(n.matchesBy[HTMLElement](_.className == "BB"))
    }

    "renderIntoDocument" - {
      def test(c: GenericComponent.MountedRaw, exp: String): Unit =
        assertOuterHTML(ReactDOM.findDOMNode(c.raw).get.asElement(), exp)

      "plainElement" - {
        val re: VdomElement = <.div("Good")
        val c = LegacyReactTestUtils.renderIntoDocument(re)
        test(c, """<div>Good</div>""")
      }

      "scalaComponent" - {
        val c = LegacyReactTestUtils.renderIntoDocument(B())
        test(c, """<p class="BB">hehehe</p>""")
      }
    }

    "Simulate" - {
      "click" - {
        val c = LegacyReactTestUtils.renderIntoDocument(IC())
        val s = LegacyReactTestUtils.findRenderedDOMComponentWithTag(c, "span")
        val a = s.getDOMNode.asMounted().asElement().innerHTML
        Simulate.click(inputRef.unsafeGet())
        val b = s.getDOMNode.asMounted().asElement().innerHTML
        assert(a != b)
      }

      "eventTypes" - {
        def test[E[+x <: dom.Node] <: SyntheticEvent[x]](eventType: VdomAttr.Event[E], simF: ReactOrDomNode => Unit) = {
          val IDC = ScalaComponent.builder[Unit]("IC").initialState(true).render($ => {
            @nowarn("cat=unused") val ch = (e: E[dom.Node]) => $.modState(x => !x)
            <.label(
              <.input.text(^.value := $.state, eventType ==> ch).withRef(inputRef),
              <.span(s"s = ${$.state}")
            )
          }).build

          val c = LegacyReactTestUtils.renderIntoDocument(IDC())
          val s = LegacyReactTestUtils.findRenderedDOMComponentWithTag(c, "span")

          val a = s.getDOMNode.asMounted().asElement().innerHTML
          simF(inputRef.unsafeGet())
          val b = s.getDOMNode.asMounted().asElement().innerHTML

          assert(a != b)
        }

        "onAuxClick"           - test(^.onAuxClick,           Simulate.auxClick(_))
        "onBeforeInput"        - test(^.onBeforeInput,        Simulate.beforeInput(_))
        "onBlur"               - test(^.onBlur,               Simulate.blur(_))
        "onChange"             - test(^.onChange,             Simulate.change(_))
        "onClick"              - test(^.onClick,              Simulate.click(_))
        "onCompositionEnd"     - test(^.onCompositionEnd,     Simulate.compositionEnd(_))
        "onCompositionStart"   - test(^.onCompositionStart,   Simulate.compositionStart(_))
        "onCompositionUpdate"  - test(^.onCompositionUpdate,  Simulate.compositionUpdate(_))
        "onContextMenu"        - test(^.onContextMenu,        Simulate.contextMenu(_))
        "onCopy"               - test(^.onCopy,               Simulate.copy(_))
        "onCut"                - test(^.onCut,                Simulate.cut(_))
        "onDblClick"           - test(^.onDblClick,           Simulate.doubleClick(_))
        "onDragEnd"            - test(^.onDragEnd,            Simulate.dragEnd(_))
        "onDragEnter"          - test(^.onDragEnter,          Simulate.dragEnter(_))
        "onDragExit"           - test(^.onDragExit,           Simulate.dragExit(_))
        "onDragLeave"          - test(^.onDragLeave,          Simulate.dragLeave(_))
        "onDragOver"           - test(^.onDragOver,           Simulate.dragOver(_))
        "onDragStart"          - test(^.onDragStart,          Simulate.dragStart(_))
        "onDrag"               - test(^.onDrag,               Simulate.drag(_))
        "onDrop"               - test(^.onDrop,               Simulate.drop(_))
        "onError"              - test(^.onError,              Simulate.error(_))
        "onFocus"              - test(^.onFocus,              Simulate.focus(_))
        "onGotPointerCapture"  - test(^.onGotPointerCapture,  Simulate.gotPointerCapture(_))
        "onInput"              - test(^.onInput,              Simulate.input(_))
        "onKeyDown"            - test(^.onKeyDown,            Simulate.keyDown(_))
        "onKeyPress"           - test(^.onKeyPress,           Simulate.keyPress(_))
        "onKeyUp"              - test(^.onKeyUp,              Simulate.keyUp(_))
        "onLoad"               - test(^.onLoad,               Simulate.load(_))
        "onLostPointerCapture" - test(^.onLostPointerCapture, Simulate.lostPointerCapture(_))
        "onMouseDown"          - test(^.onMouseDown,          Simulate.mouseDown(_))
        "onMouseEnter"         - test(^.onMouseEnter,         Simulate.mouseEnter(_))
        "onMouseLeave"         - test(^.onMouseLeave,         Simulate.mouseLeave(_))
        "onMouseMove"          - test(^.onMouseMove,          Simulate.mouseMove(_))
        "onMouseOut"           - test(^.onMouseOut,           Simulate.mouseOut(_))
        "onMouseOver"          - test(^.onMouseOver,          Simulate.mouseOver(_))
        "onMouseUp"            - test(^.onMouseUp,            Simulate.mouseUp(_))
        "onPaste"              - test(^.onPaste,              Simulate.paste(_))
        "onPointerCancel"      - test(^.onPointerCancel,      Simulate.pointerCancel(_))
        "onPointerDown"        - test(^.onPointerDown,        Simulate.pointerDown(_))
        "onPointerEnter"       - test(^.onPointerEnter,       Simulate.pointerEnter(_))
        "onPointerLeave"       - test(^.onPointerLeave,       Simulate.pointerLeave(_))
        "onPointerMove"        - test(^.onPointerMove,        Simulate.pointerMove(_))
        "onPointerOut"         - test(^.onPointerOut,         Simulate.pointerOut(_))
        "onPointerOver"        - test(^.onPointerOver,        Simulate.pointerOver(_))
        "onPointerUp"          - test(^.onPointerUp,          Simulate.pointerUp(_))
        "onReset"              - test(^.onReset,              Simulate.reset(_))
        "onScroll"             - test(^.onScroll,             Simulate.scroll(_))
        "onSelect"             - test(^.onSelect,             Simulate.select(_))
        "onSubmit"             - test(^.onSubmit,             Simulate.submit(_))
        "onTouchCancel"        - test(^.onTouchCancel,        Simulate.touchCancel(_))
        "onTouchEnd"           - test(^.onTouchEnd,           Simulate.touchEnd(_))
        "onTouchMove"          - test(^.onTouchMove,          Simulate.touchMove(_))
        "onTouchStart"         - test(^.onTouchStart,         Simulate.touchStart(_))
        "onWheel"              - test(^.onWheel,              Simulate.wheel(_))
      }

      "eventDefaults" - {
        var ok = false
        val c = ScalaComponent.builder[Unit]("").render_P { _ =>
          def onClick(e: ReactMouseEvent) = {
            // Make sure these don't throw
            e.defaultPrevented
            e.isDefaultPrevented()
            locally(e.screenX)
            locally(e.screenY)
            locally(e.clientX)
            locally(e.clientY)
            locally(e.pageX)
            locally(e.pageY)
            locally(e.altKey)
            locally(e.ctrlKey)
            locally(e.metaKey)
            locally(e.shiftKey)
            locally(e.button)
            locally(e.buttons)
            Callback { ok = true }
          }
          <.div(^.onClick ==> onClick)
        }.build
        LegacyReactTestUtils.withRenderedIntoDocument(c()) { m =>
          Simulate.click(m)
        }
        assertEq(ok, true)
      }

      "change" - {
        val c = LegacyReactTestUtils.renderIntoDocument(IT())
        SimEvent.Change("hehe").simulate(c)
        val t = c.getDOMNode.asMounted().domCast[HTMLInputElement].value
        assertEq(t, "HEHE")
      }

      "focusChangeBlur" - {
        var events = Vector.empty[String]
        val C = ScalaComponent.builder[Unit]("C").initialState("ey").render(T => {
          def e(s: String) = Callback(events :+= s)
          def chg(ev: ReactEventFromInput) =
            e("change") >> T.setState(ev.target.value)
          <.input.text(^.value := T.state, ^.onFocus --> e("focus"), ^.onChange ==> chg, ^.onBlur --> e("blur")).withRef(inputRef)
        }).build
        LegacyReactTestUtils.renderIntoDocument(C())
        Simulation.focusChangeBlur("good") run inputRef.unsafeGet()
        assertEq(events, Vector("focus", "change", "blur"))
        assertEq(inputRef.unsafeGet().value, "good")
      }
      "targetByName" - {
        val c = LegacyReactTestUtils.renderIntoDocument(IC())
        var count = 0
        def tgt = {
          count += 1
          Sizzle("input", c.getDOMNode.asMounted().asElement()).sole()
        }
        Simulation.focusChangeBlur("-") run tgt
        assert(count == 3)
      }
    }

    "withRenderedIntoDocument" - {
      var m: ScalaComponent.MountedImpure[Unit, Boolean, Unit] = null
      LegacyReactTestUtils.withRenderedIntoDocument(IC()) { mm =>
        m = mm
        val n = m.getDOMNode.asMounted().asElement()
        assert(LegacyReactTestUtils.removeReactInternals(n.outerHTML) startsWith "<label><input ")
        // assert(m.isMounted == yesItsMounted)
      }
      // assert(m.isMounted == nopeNotMounted)
    }

    "withRenderedIntoBody" - {
      def inspectBody() = document.body.childElementCount
      val body1 = inspectBody()
      var m: ScalaComponent.MountedImpure[Unit, Boolean, Unit] = null
      LegacyReactTestUtils.withRenderedIntoBody(IC()) { mm =>
        m = mm
        val n = m.getDOMNode.asMounted().asElement()
        assert(LegacyReactTestUtils.removeReactInternals(n.outerHTML) startsWith "<label><input ")
        // assert(m.isMounted == yesItsMounted)

        // Benefits of body over detached
        inputRef.unsafeGet().focus()
        assert(document.activeElement == inputRef.unsafeGet())
        inputRef.unsafeGet().blur()
        assert(document.activeElement != inputRef.unsafeGet())
      }
      val body2 = inspectBody()
      // assert(m.isMounted == nopeNotMounted)
      assert(body1 == body2)
    }

    "withRenderedIntoDocumentFuture" - {
      var m: ScalaComponent.MountedImpure[Unit, Boolean, Unit] = null
      val promise: Promise[Unit] = Promise[Unit]()
      LegacyReactTestUtils.withRenderedIntoDocumentFuture(IC()) { mm =>
        m = mm
        promise.future
      }
      val n = m.getDOMNode.asMounted().asElement()
      assert(LegacyReactTestUtils.removeReactInternals(n.outerHTML) startsWith "<label><input ")
      // assert(m.isMounted == yesItsMounted)

      promise.success(())

      promise.future //.map(_ => assert(m.isMounted == nopeNotMounted))
    }

    "withRenderedIntoBodyAsync" - {
      def inspectBody() = document.body.childElementCount
      val body1 = inspectBody()
      var m: ScalaComponent.MountedImpure[Unit, Boolean, Unit] = null
      val promise: Promise[Unit] = Promise[Unit]()
      val future = LegacyReactTestUtils.withRenderedIntoBodyFuture(IC()) { mm =>
        m = mm
        promise.future
      }
      val n = m.getDOMNode.asMounted().asElement()
      assert(LegacyReactTestUtils.removeReactInternals(n.outerHTML) startsWith "<label><input ")
      // assert(m.isMounted == yesItsMounted)

      // Benefits of body over detached
      inputRef.unsafeGet().focus()
      assert(document.activeElement == inputRef.unsafeGet())
      inputRef.unsafeGet().blur()
      assert(document.activeElement != inputRef.unsafeGet())

      promise.success(())

      future.map { _ =>
        val body2 = inspectBody()
        // assert(m.isMounted == nopeNotMounted)
        assert(body1 == body2)
      }
    }

    "modifyProps" - {
      LegacyReactTestUtils.withRenderedIntoDocument(CP("start")) { m =>
        assertRendered(m.getDOMNode.asMounted().asElement(), "<div>none → start</div>")
        LegacyReactTestUtils.modifyProps(CP, m)(_ + "ed")
        assertRendered(m.getDOMNode.asMounted().asElement(), "<div>start → started</div>")
        LegacyReactTestUtils.replaceProps(CP, m)("done!")
        assertRendered(m.getDOMNode.asMounted().asElement(), "<div>started → done!</div>")
      }
    }

    "removeReactInternals" - {
      val c = ScalaComponent.static("")(<.div(<.br, "hello", <.hr))
      LegacyReactTestUtils.withRenderedIntoDocument(c()) { m =>
        val orig = m.getDOMNode.asMounted().asElement().outerHTML
        val after = LegacyReactTestUtils.removeReactInternals(orig)
        assertEq("<div><br>hello<hr></div>", after)
        s"$orig  →  $after"
      }
    }

    "act" - {
      // Just making sure the facade and types align
      var called = false
      LegacyReactTestUtils.act {
        called = true
      }
      assertEq(called, true)
    }

    // Disabled due to https://github.com/scala-js/scala-js-env-jsdom-nodejs/issues/44
//    "actAsync" - {
//      // Just making sure the facade and types align
//      var called = false
//      LegacyReactTestUtils.actAsync {
//        AsyncCallback.delay {
//          called = true
//        }
//      }.tap { _ =>
//        assertEq(called, true)
//      }.unsafeToFuture()
//    }
  }
}
