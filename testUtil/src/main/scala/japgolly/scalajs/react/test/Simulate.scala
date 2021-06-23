package japgolly.scalajs.react.test

import scala.scalajs.js

/** https://reactjs.org/docs/test-utils.html#simulate */
object Simulate {
  import ReactEventType._

  def raw = japgolly.scalajs.react.test.facade.ReactTestUtils.Simulate

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

  def auxClick          (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.auxClick          (t, mod(eventData, Mouse, 1))
  def beforeInput       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.beforeInput       (t, mod(eventData, Basic))
  def blur              (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.blur              (t, mod(eventData, Focus))
  def change            (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.change            (t, mod(eventData, Form))
  def click             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.click             (t, mod(eventData, Mouse, 1))
  def compositionEnd    (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.compositionEnd    (t, mod(eventData, Composition))
  def compositionStart  (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.compositionStart  (t, mod(eventData, Composition))
  def compositionUpdate (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.compositionUpdate (t, mod(eventData, Composition))
  def contextMenu       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.contextMenu       (t, mod(eventData, Mouse))
  def copy              (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.copy              (t, mod(eventData, Clipboard))
  def cut               (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.cut               (t, mod(eventData, Clipboard))
  def doubleClick       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.doubleClick       (t, mod(eventData, Mouse, 2))
  def dragEnd           (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.dragEnd           (t, mod(eventData, Drag))
  def dragEnter         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.dragEnter         (t, mod(eventData, Drag))
  def dragExit          (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.dragExit          (t, mod(eventData, Drag))
  def dragLeave         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.dragLeave         (t, mod(eventData, Drag))
  def dragOver          (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.dragOver          (t, mod(eventData, Drag))
  def dragStart         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.dragStart         (t, mod(eventData, Drag))
  def drag              (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.drag              (t, mod(eventData, Drag))
  def drop              (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.drop              (t, mod(eventData, Drag))
  def error             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.error             (t, mod(eventData, Basic))
  def focus             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.focus             (t, mod(eventData, Focus))
  def gotPointerCapture (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.gotPointerCapture (t, mod(eventData, Pointer))
  def input             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.input             (t, mod(eventData, Form))
  def keyDown           (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.keyDown           (t, mod(eventData, Keyboard))
  def keyPress          (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.keyPress          (t, mod(eventData, Keyboard))
  def keyUp             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.keyUp             (t, mod(eventData, Keyboard))
  def load              (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.load              (t, mod(eventData, Basic))
  def lostPointerCapture(t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.lostPointerCapture(t, mod(eventData, Pointer))
  def mouseDown         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseDown         (t, mod(eventData, Mouse, 1))
  def mouseEnter        (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseEnter        (t, mod(eventData, Mouse))
  def mouseLeave        (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseLeave        (t, mod(eventData, Mouse))
  def mouseMove         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseMove         (t, mod(eventData, Mouse))
  def mouseOut          (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseOut          (t, mod(eventData, Mouse))
  def mouseOver         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseOver         (t, mod(eventData, Mouse))
  def mouseUp           (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseUp           (t, mod(eventData, Mouse, 1))
  def paste             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.paste             (t, mod(eventData, Clipboard))
  def pointerCancel     (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerCancel     (t, mod(eventData, Pointer))
  def pointerDown       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerDown       (t, mod(eventData, Pointer))
  def pointerEnter      (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerEnter      (t, mod(eventData, Pointer))
  def pointerLeave      (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerLeave      (t, mod(eventData, Pointer))
  def pointerMove       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerMove       (t, mod(eventData, Pointer))
  def pointerOut        (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerOut        (t, mod(eventData, Pointer))
  def pointerOver       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerOver       (t, mod(eventData, Pointer))
  def pointerUp         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerUp         (t, mod(eventData, Pointer))
  def reset             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.reset             (t, mod(eventData, Form))
  def scroll            (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.scroll            (t, mod(eventData, UI))
  def select            (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.select            (t, mod(eventData, Basic))
  def submit            (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.submit            (t, mod(eventData, Form))
  def touchCancel       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.touchCancel       (t, mod(eventData, Touch))
  def touchEnd          (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.touchEnd          (t, mod(eventData, Touch))
  def touchMove         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.touchMove         (t, mod(eventData, Touch))
  def touchStart        (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.touchStart        (t, mod(eventData, Touch))
  def wheel             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.wheel             (t, mod(eventData, Wheel))
}
