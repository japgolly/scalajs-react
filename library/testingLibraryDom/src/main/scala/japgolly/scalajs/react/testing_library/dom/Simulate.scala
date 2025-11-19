package japgolly.scalajs.react.testing_library.dom

import japgolly.scalajs.react.React
import japgolly.scalajs.react.test.{ReactEventType, ReactTestUtils}
import org.scalajs.dom.Node
import scala.scalajs.js

object Simulate {
  import ReactEventType._

  val raw = facade.TestingLibraryDom.fireEvent

  private def mod(e: js.Object, eventType: ReactEventType): js.Object =
    js.Object.assign(
      js.Object(),
      eventType.defaultEventData,
      e)

  private def mod(e: js.Object, eventType: ReactEventType, detail: Int): js.Object =
    js.Object.assign(
      js.Object(),
      eventType.defaultEventData,
      js.Dynamic.literal(detail = detail),
      e)

  private def wrap(f: => Unit): Unit =
    if (React.majorVersion >= 18 && ReactTestUtils.IsReactActEnvironment())
      ReactTestUtils.actSync(f)
    else
      f

//def auxClick          (t: Node, eventData: js.Object = null): Unit = wrap(raw.auxClick          (t, mod(eventData, Mouse, 1)))
//def beforeInput       (t: Node, eventData: js.Object = null): Unit = wrap(raw.beforeInput       (t, mod(eventData, Basic)))
//def blur              (t: Node, eventData: js.Object = null): Unit = wrap(raw.blur              (t, mod(eventData, Focus)))
//def change            (t: Node, eventData: js.Object = null): Unit = wrap(raw.change            (t, mod(eventData, Form)))
  def click             (t: Node, eventData: js.Object = null): Unit = wrap(raw.click             (t, mod(eventData, Mouse, 1)))
  def compositionEnd    (t: Node, eventData: js.Object = null): Unit = wrap(raw.compositionEnd    (t, mod(eventData, Composition)))
  def compositionStart  (t: Node, eventData: js.Object = null): Unit = wrap(raw.compositionStart  (t, mod(eventData, Composition)))
  def compositionUpdate (t: Node, eventData: js.Object = null): Unit = wrap(raw.compositionUpdate (t, mod(eventData, Composition)))
  def contextMenu       (t: Node, eventData: js.Object = null): Unit = wrap(raw.contextMenu       (t, mod(eventData, Mouse)))
  def copy              (t: Node, eventData: js.Object = null): Unit = wrap(raw.copy              (t, mod(eventData, Clipboard)))
  def cut               (t: Node, eventData: js.Object = null): Unit = wrap(raw.cut               (t, mod(eventData, Clipboard)))
  def doubleClick       (t: Node, eventData: js.Object = null): Unit = wrap(raw.doubleClick       (t, mod(eventData, Mouse, 2)))
  def dragEnd           (t: Node, eventData: js.Object = null): Unit = wrap(raw.dragEnd           (t, mod(eventData, Drag)))
  def dragEnter         (t: Node, eventData: js.Object = null): Unit = wrap(raw.dragEnter         (t, mod(eventData, Drag)))
  def dragExit          (t: Node, eventData: js.Object = null): Unit = wrap(raw.dragExit          (t, mod(eventData, Drag)))
  def dragLeave         (t: Node, eventData: js.Object = null): Unit = wrap(raw.dragLeave         (t, mod(eventData, Drag)))
  def dragOver          (t: Node, eventData: js.Object = null): Unit = wrap(raw.dragOver          (t, mod(eventData, Drag)))
  def dragStart         (t: Node, eventData: js.Object = null): Unit = wrap(raw.dragStart         (t, mod(eventData, Drag)))
  def drag              (t: Node, eventData: js.Object = null): Unit = wrap(raw.drag              (t, mod(eventData, Drag)))
  def drop              (t: Node, eventData: js.Object = null): Unit = wrap(raw.drop              (t, mod(eventData, Drag)))
//def error             (t: Node, eventData: js.Object = null): Unit = wrap(raw.error             (t, mod(eventData, Basic)))
//def focus             (t: Node, eventData: js.Object = null): Unit = wrap(raw.focus             (t, mod(eventData, Focus)))
  def gotPointerCapture (t: Node, eventData: js.Object = null): Unit = wrap(raw.gotPointerCapture (t, mod(eventData, Pointer)))
  def input             (t: Node, eventData: js.Object = null): Unit = wrap(raw.input             (t, mod(eventData, Form)))
  def keyDown           (t: Node, eventData: js.Object = null): Unit = wrap(raw.keyDown           (t, mod(eventData, Keyboard)))
//def keyPress          (t: Node, eventData: js.Object = null): Unit = wrap(raw.keyPress          (t, mod(eventData, Keyboard)))
  def keyUp             (t: Node, eventData: js.Object = null): Unit = wrap(raw.keyUp             (t, mod(eventData, Keyboard)))
//def load              (t: Node, eventData: js.Object = null): Unit = wrap(raw.load              (t, mod(eventData, Basic)))
  def lostPointerCapture(t: Node, eventData: js.Object = null): Unit = wrap(raw.lostPointerCapture(t, mod(eventData, Pointer)))
  def mouseDown         (t: Node, eventData: js.Object = null): Unit = wrap(raw.mouseDown         (t, mod(eventData, Mouse, 1)))
//def mouseEnter        (t: Node, eventData: js.Object = null): Unit = wrap(raw.mouseEnter        (t, mod(eventData, Mouse)))
//def mouseLeave        (t: Node, eventData: js.Object = null): Unit = wrap(raw.mouseLeave        (t, mod(eventData, Mouse)))
  def mouseMove         (t: Node, eventData: js.Object = null): Unit = wrap(raw.mouseMove         (t, mod(eventData, Mouse)))
  def mouseOut          (t: Node, eventData: js.Object = null): Unit = wrap(raw.mouseOut          (t, mod(eventData, Mouse)))
  def mouseOver         (t: Node, eventData: js.Object = null): Unit = wrap(raw.mouseOver         (t, mod(eventData, Mouse)))
  def mouseUp           (t: Node, eventData: js.Object = null): Unit = wrap(raw.mouseUp           (t, mod(eventData, Mouse, 1)))
  def paste             (t: Node, eventData: js.Object = null): Unit = wrap(raw.paste             (t, mod(eventData, Clipboard)))
  def pointerCancel     (t: Node, eventData: js.Object = null): Unit = wrap(raw.pointerCancel     (t, mod(eventData, Pointer)))
  def pointerDown       (t: Node, eventData: js.Object = null): Unit = wrap(raw.pointerDown       (t, mod(eventData, Pointer)))
//def pointerEnter      (t: Node, eventData: js.Object = null): Unit = wrap(raw.pointerEnter      (t, mod(eventData, Pointer)))
//def pointerLeave      (t: Node, eventData: js.Object = null): Unit = wrap(raw.pointerLeave      (t, mod(eventData, Pointer)))
  def pointerMove       (t: Node, eventData: js.Object = null): Unit = wrap(raw.pointerMove       (t, mod(eventData, Pointer)))
  def pointerOut        (t: Node, eventData: js.Object = null): Unit = wrap(raw.pointerOut        (t, mod(eventData, Pointer)))
  def pointerOver       (t: Node, eventData: js.Object = null): Unit = wrap(raw.pointerOver       (t, mod(eventData, Pointer)))
  def pointerUp         (t: Node, eventData: js.Object = null): Unit = wrap(raw.pointerUp         (t, mod(eventData, Pointer)))
  def reset             (t: Node, eventData: js.Object = null): Unit = wrap(raw.reset             (t, mod(eventData, Form)))
  def scroll            (t: Node, eventData: js.Object = null): Unit = wrap(raw.scroll            (t, mod(eventData, UI)))
//def select            (t: Node, eventData: js.Object = null): Unit = wrap(raw.select            (t, mod(eventData, Basic)))
  def submit            (t: Node, eventData: js.Object = null): Unit = wrap(raw.submit            (t, mod(eventData, Form)))
  def touchCancel       (t: Node, eventData: js.Object = null): Unit = wrap(raw.touchCancel       (t, mod(eventData, Touch)))
  def touchEnd          (t: Node, eventData: js.Object = null): Unit = wrap(raw.touchEnd          (t, mod(eventData, Touch)))
  def touchMove         (t: Node, eventData: js.Object = null): Unit = wrap(raw.touchMove         (t, mod(eventData, Touch)))
  def touchStart        (t: Node, eventData: js.Object = null): Unit = wrap(raw.touchStart        (t, mod(eventData, Touch)))
  def wheel             (t: Node, eventData: js.Object = null): Unit = wrap(raw.wheel             (t, mod(eventData, Wheel)))
}
