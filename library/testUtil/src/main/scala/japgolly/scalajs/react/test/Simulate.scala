package japgolly.scalajs.react.test

import japgolly.scalajs.react.React
import japgolly.scalajs.react.test.facade
import scala.scalajs.js

/** https://reactjs.org/docs/test-utils.html#simulate */
object Simulate {
  import ReactEventType._

  val raw = facade.ReactTestUtils.Simulate

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
    if (React.majorVersion >= 18 || ReactTestUtils2.IsReactActEnvironment())
      ReactTestUtils2.act(f)
    else
      f

  def auxClick          (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.auxClick          (t, mod(eventData, Mouse, 1)))
  def beforeInput       (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.beforeInput       (t, mod(eventData, Basic)))
  def blur              (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.blur              (t, mod(eventData, Focus)))
  def change            (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.change            (t, mod(eventData, Form)))
  def click             (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.click             (t, mod(eventData, Mouse, 1)))
  def compositionEnd    (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.compositionEnd    (t, mod(eventData, Composition)))
  def compositionStart  (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.compositionStart  (t, mod(eventData, Composition)))
  def compositionUpdate (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.compositionUpdate (t, mod(eventData, Composition)))
  def contextMenu       (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.contextMenu       (t, mod(eventData, Mouse)))
  def copy              (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.copy              (t, mod(eventData, Clipboard)))
  def cut               (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.cut               (t, mod(eventData, Clipboard)))
  def doubleClick       (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.doubleClick       (t, mod(eventData, Mouse, 2)))
  def dragEnd           (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.dragEnd           (t, mod(eventData, Drag)))
  def dragEnter         (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.dragEnter         (t, mod(eventData, Drag)))
  def dragExit          (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.dragExit          (t, mod(eventData, Drag)))
  def dragLeave         (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.dragLeave         (t, mod(eventData, Drag)))
  def dragOver          (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.dragOver          (t, mod(eventData, Drag)))
  def dragStart         (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.dragStart         (t, mod(eventData, Drag)))
  def drag              (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.drag              (t, mod(eventData, Drag)))
  def drop              (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.drop              (t, mod(eventData, Drag)))
  def error             (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.error             (t, mod(eventData, Basic)))
  def focus             (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.focus             (t, mod(eventData, Focus)))
  def gotPointerCapture (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.gotPointerCapture (t, mod(eventData, Pointer)))
  def input             (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.input             (t, mod(eventData, Form)))
  def keyDown           (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.keyDown           (t, mod(eventData, Keyboard)))
  def keyPress          (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.keyPress          (t, mod(eventData, Keyboard)))
  def keyUp             (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.keyUp             (t, mod(eventData, Keyboard)))
  def load              (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.load              (t, mod(eventData, Basic)))
  def lostPointerCapture(t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.lostPointerCapture(t, mod(eventData, Pointer)))
  def mouseDown         (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.mouseDown         (t, mod(eventData, Mouse, 1)))
  def mouseEnter        (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.mouseEnter        (t, mod(eventData, Mouse)))
  def mouseLeave        (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.mouseLeave        (t, mod(eventData, Mouse)))
  def mouseMove         (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.mouseMove         (t, mod(eventData, Mouse)))
  def mouseOut          (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.mouseOut          (t, mod(eventData, Mouse)))
  def mouseOver         (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.mouseOver         (t, mod(eventData, Mouse)))
  def mouseUp           (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.mouseUp           (t, mod(eventData, Mouse, 1)))
  def paste             (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.paste             (t, mod(eventData, Clipboard)))
  def pointerCancel     (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.pointerCancel     (t, mod(eventData, Pointer)))
  def pointerDown       (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.pointerDown       (t, mod(eventData, Pointer)))
  def pointerEnter      (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.pointerEnter      (t, mod(eventData, Pointer)))
  def pointerLeave      (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.pointerLeave      (t, mod(eventData, Pointer)))
  def pointerMove       (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.pointerMove       (t, mod(eventData, Pointer)))
  def pointerOut        (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.pointerOut        (t, mod(eventData, Pointer)))
  def pointerOver       (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.pointerOver       (t, mod(eventData, Pointer)))
  def pointerUp         (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.pointerUp         (t, mod(eventData, Pointer)))
  def reset             (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.reset             (t, mod(eventData, Form)))
  def scroll            (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.scroll            (t, mod(eventData, UI)))
  def select            (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.select            (t, mod(eventData, Basic)))
  def submit            (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.submit            (t, mod(eventData, Form)))
  def touchCancel       (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.touchCancel       (t, mod(eventData, Touch)))
  def touchEnd          (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.touchEnd          (t, mod(eventData, Touch)))
  def touchMove         (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.touchMove         (t, mod(eventData, Touch)))
  def touchStart        (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.touchStart        (t, mod(eventData, Touch)))
  def wheel             (t: ReactOrDomNode, eventData: js.Object = null): Unit = wrap(raw.wheel             (t, mod(eventData, Wheel)))
}
