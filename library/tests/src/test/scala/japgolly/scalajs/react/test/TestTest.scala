package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import japgolly.scalajs.react.facade.SyntheticEvent
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import org.scalajs.dom.document
import scala.annotation.nowarn
import scala.concurrent.Promise
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import utest._

object TestTest extends AsyncTestSuite {

  lazy val A = ScalaComponent.builder[Unit]("A").render_C(c => <.p(^.cls := "AA", c)).build
  lazy val B = ScalaComponent.builder[Unit]("B").renderStatic(<.p(^.cls := "BB", "hehehe")).build
  // lazy val rab = ReactTestUtils2.renderIntoDocument(A(B()))

  val inputRef = Ref[dom.HTMLInputElement]

  lazy val IC = ScalaComponent.builder[Unit]("IC").initialState(true).renderS(($,s) => {
    val ch = (_: ReactEvent) => $.modState(x => !x)
    <.label(
      <.input.checkbox(^.checked := s, ^.readOnly := true, ^.onClick ==> ch).withRef(inputRef),
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

    "withRendered" - {

      "plainElement" - ReactTestUtils2.withRenderedSync(<.div("Good")) { r =>
        r.outerHTML.assert("<div>Good</div>")
        r.innerHTML.assert("Good")
        r.root.outerHTML.assert("<div><div>Good</div></div>")
      }

      "scalaComponent" - ReactTestUtils2.withRenderedSync(B()) { r =>
        r.outerHTML.assert("""<p class="BB">hehehe</p>""")
        r.innerHTML.assert("""hehehe""")
        r.root.outerHTML.assert("""<div><p class="BB">hehehe</p></div>""")
      }
    }

    "Simulate" - {

      "click" - {
        ReactTestUtils2.withRenderedSync(IC()) { r =>
          def s = r.querySelector("span")
          val a = s.innerHTML
          Simulate.click(inputRef.unsafeGet())
          val b = s.innerHTML
          assertNotEq(a, b)
        }
      }

      "eventTypes" - {
        def test[E[+x <: dom.Node] <: SyntheticEvent[x]](eventType: VdomAttr.Event[E], simF: ReactOrDomNode => Unit) = {
          val IDC = ScalaComponent.builder[Unit]("IC").initialState(true).render($ => {
            @nowarn("cat=unused") val ch = (e: E[dom.Node]) => $.modState(x => !x)
            <.label(
              <.input.text(^.value := $.state, ^.readOnly := true, eventType ==> ch).withRef(inputRef),
              <.span(s"s = ${$.state}")
            )
          }).build

          ReactTestUtils2.withRenderedSync(IDC()) { r =>
            def s = r.querySelector("span")
            val a = s.innerHTML
            simF(inputRef.unsafeGet())
            val b = s.innerHTML
            assertNotEq(a, b)
          }
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
        ReactTestUtils2.withRenderedSync(c()) { r =>
          r.node.foreach(Simulate.click(_))
        }
        assertEq(ok, true)
      }

      "change" - ReactTestUtils2.withRenderedSync(IT()) { t =>
        t.node.foreach(SimEvent.Change("hehe").simulate(_))
        assertEq(t.asInput().value, "HEHE")
      }

      "focusChangeBlur" - {
        var events = Vector.empty[String]
        val C = ScalaComponent.builder[Unit]("C").initialState("ey").render(T => {
          def e(s: String) = Callback(events :+= s)
          def chg(ev: ReactEventFromInput) =
            e("change") >> T.setState(ev.target.value)
          <.input.text(^.value := T.state, ^.onFocus --> e("focus"), ^.onChange ==> chg, ^.onBlur --> e("blur")).withRef(inputRef)
        }).build
        ReactTestUtils2.withRenderedSync(C()) { _ =>
          Simulation.focusChangeBlur("good") run inputRef.unsafeGet()
          assertEq(events, Vector("focus", "change", "blur"))
          assertEq(inputRef.unsafeGet().value, "good")
        }
      }

      "targetByName" - ReactTestUtils2.withRenderedSync(IC()) { t =>
        var count = 0
        def tgt = {
          count += 1
          t.select("input").node.get
        }
        Simulation.focusChangeBlur("-") run tgt
        assertEq(count, 3)
      }
    }

    "withRendered" - {
      def inspectBody() = document.body.childElementCount
      val body1 = inspectBody()
      ReactTestUtils2.withRenderedSync(IC()) { t =>
        t.outerHTML.assertStartsWith("<label><input ")
        assertNotEq(body1, inspectBody())

        // Benefits of body over detached
        inputRef.unsafeGet().focus()
        assert(document.activeElement == inputRef.unsafeGet())
        inputRef.unsafeGet().blur()
        assert(document.activeElement != inputRef.unsafeGet())
      }
      assertEq(body1, inspectBody())
    }

    "withRenderedFuture" - {
      var r: TestReactRoot = null
      val promise: Promise[Unit] = Promise[Unit]()
      val future = ReactTestUtils2.withRenderedSync(IC()).future { t =>
        r = t.root
        assertEq(r.isEmpty(), false)
        t.outerHTML.assertStartsWith("<label><input ")
        promise.future
      }

      promise.success(())

      future.map { _ => assertEq(r.isEmpty(), true) }
    }

    "replaceProps" - ReactTestUtils2.withRenderedSync(CP("start")) { d =>
      d.outerHTML.assert("<div>none → start</div>")
      d.root.renderSync(CP("started"))
      d.outerHTML.assert("<div>start → started</div>")
      d.root.renderSync(CP("done!"))
      d.outerHTML.assert("<div>started → done!</div>")
    }

    "removeReactInternals" - {
      val c = ScalaComponent.static("")(<.div(<.br, "hello", <.hr))
      ReactTestUtils2.withRenderedSync(c()) { t =>
        // val orig = t.asHtml().outerHTML
        // val after = ReactTestUtils2.removeReactInternals(orig)
        // assertEq("<div><br>hello<hr></div>", after)
        // s"$orig  →  $after"
        t.outerHTML.assert("<div><br>hello<hr></div>")
      }
    }

    "act" - {
      // Just making sure the facade and types align
      var called = false
      ReactTestUtils2.act_ {
        called = true
      }.map(_ => assertEq(called, true))
    }

    // Disabled due to https://github.com/scala-js/scala-js-env-jsdom-nodejs/issues/44
//    "actAsync" - {
//      // Just making sure the facade and types align
//      var called = false
//      ReactTestUtils2.actAsync {
//        AsyncCallback.delay {
//          called = true
//        }
//      }.tap { _ =>
//        assertEq(called, true)
//      }.unsafeToFuture()
//    }
  }
}
