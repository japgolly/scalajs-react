package japgolly.scalajs.react.test

import scala.scalajs.js

/** https://reactjs.org/docs/test-utils.html#simulate */
object Simulate {

  def raw = japgolly.scalajs.react.test.raw.ReactTestUtils.Simulate

  private val _defaultEventData: js.Object =
    js.Dynamic.literal(
      bubbles          = false,
      cancelable       = false,
      defaultPrevented = false,
      isTrusted        = false,
    )

  def defaultEventData(): js.Object =
    js.Object.assign(js.Object(), _defaultEventData)

  def withDefaultEventData(e: js.Object): js.Object =
    js.Object.assign(js.Object(), _defaultEventData, e)

  @inline private def mod(e: js.Object): js.Object =
    withDefaultEventData(e)

  def auxClick          (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.auxClick          (t, mod(eventData))
  def beforeInput       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.beforeInput       (t, mod(eventData))
  def blur              (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.blur              (t, mod(eventData))
  def change            (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.change            (t, mod(eventData))
  def click             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.click             (t, mod(eventData))
  def compositionEnd    (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.compositionEnd    (t, mod(eventData))
  def compositionStart  (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.compositionStart  (t, mod(eventData))
  def compositionUpdate (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.compositionUpdate (t, mod(eventData))
  def contextMenu       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.contextMenu       (t, mod(eventData))
  def copy              (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.copy              (t, mod(eventData))
  def cut               (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.cut               (t, mod(eventData))
  def doubleClick       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.doubleClick       (t, mod(eventData))
  def dragEnd           (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.dragEnd           (t, mod(eventData))
  def dragEnter         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.dragEnter         (t, mod(eventData))
  def dragExit          (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.dragExit          (t, mod(eventData))
  def dragLeave         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.dragLeave         (t, mod(eventData))
  def dragOver          (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.dragOver          (t, mod(eventData))
  def dragStart         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.dragStart         (t, mod(eventData))
  def drag              (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.drag              (t, mod(eventData))
  def drop              (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.drop              (t, mod(eventData))
  def error             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.error             (t, mod(eventData))
  def focus             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.focus             (t, mod(eventData))
  def gotPointerCapture (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.gotPointerCapture (t, mod(eventData))
  def input             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.input             (t, mod(eventData))
  def keyDown           (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.keyDown           (t, mod(eventData))
  def keyPress          (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.keyPress          (t, mod(eventData))
  def keyUp             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.keyUp             (t, mod(eventData))
  def load              (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.load              (t, mod(eventData))
  def lostPointerCapture(t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.lostPointerCapture(t, mod(eventData))
  def mouseDown         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseDown         (t, mod(eventData))
  def mouseEnter        (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseEnter        (t, mod(eventData))
  def mouseLeave        (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseLeave        (t, mod(eventData))
  def mouseMove         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseMove         (t, mod(eventData))
  def mouseOut          (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseOut          (t, mod(eventData))
  def mouseOver         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseOver         (t, mod(eventData))
  def mouseUp           (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.mouseUp           (t, mod(eventData))
  def paste             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.paste             (t, mod(eventData))
  def pointerCancel     (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerCancel     (t, mod(eventData))
  def pointerDown       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerDown       (t, mod(eventData))
  def pointerEnter      (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerEnter      (t, mod(eventData))
  def pointerLeave      (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerLeave      (t, mod(eventData))
  def pointerMove       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerMove       (t, mod(eventData))
  def pointerOut        (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerOut        (t, mod(eventData))
  def pointerOver       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerOver       (t, mod(eventData))
  def pointerUp         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.pointerUp         (t, mod(eventData))
  def reset             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.reset             (t, mod(eventData))
  def scroll            (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.scroll            (t, mod(eventData))
  def select            (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.select            (t, mod(eventData))
  def submit            (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.submit            (t, mod(eventData))
  def touchCancel       (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.touchCancel       (t, mod(eventData))
  def touchEnd          (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.touchEnd          (t, mod(eventData))
  def touchMove         (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.touchMove         (t, mod(eventData))
  def touchStart        (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.touchStart        (t, mod(eventData))
  def wheel             (t: ReactOrDomNode, eventData: js.Object = null): Unit = raw.wheel             (t, mod(eventData))

}
