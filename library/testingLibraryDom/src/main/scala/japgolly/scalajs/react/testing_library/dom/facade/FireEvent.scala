package japgolly.scalajs.react.testing_library.dom.facade

import org.scalajs.dom.Node
import scala.scalajs.js

@js.native
trait FireEvent extends js.Object {
//def auxClick          (t: Node, eventData: js.Object = js.native): Unit = js.native
//def beforeInput       (t: Node, eventData: js.Object = js.native): Unit = js.native
//def blur              (t: Node, eventData: js.Object = js.native): Unit = js.native
//def change            (t: Node, eventData: js.Object = js.native): Unit = js.native
  def click             (t: Node, eventData: js.Object = js.native): Unit = js.native
  def compositionEnd    (t: Node, eventData: js.Object = js.native): Unit = js.native
  def compositionStart  (t: Node, eventData: js.Object = js.native): Unit = js.native
  def compositionUpdate (t: Node, eventData: js.Object = js.native): Unit = js.native
  def contextMenu       (t: Node, eventData: js.Object = js.native): Unit = js.native
  def copy              (t: Node, eventData: js.Object = js.native): Unit = js.native
  def cut               (t: Node, eventData: js.Object = js.native): Unit = js.native
  def doubleClick       (t: Node, eventData: js.Object = js.native): Unit = js.native
  def dragEnd           (t: Node, eventData: js.Object = js.native): Unit = js.native
  def dragEnter         (t: Node, eventData: js.Object = js.native): Unit = js.native
  def dragExit          (t: Node, eventData: js.Object = js.native): Unit = js.native
  def dragLeave         (t: Node, eventData: js.Object = js.native): Unit = js.native
  def dragOver          (t: Node, eventData: js.Object = js.native): Unit = js.native
  def dragStart         (t: Node, eventData: js.Object = js.native): Unit = js.native
  def drag              (t: Node, eventData: js.Object = js.native): Unit = js.native
  def drop              (t: Node, eventData: js.Object = js.native): Unit = js.native
//def error             (t: Node, eventData: js.Object = js.native): Unit = js.native
//def focus             (t: Node, eventData: js.Object = js.native): Unit = js.native
  def gotPointerCapture (t: Node, eventData: js.Object = js.native): Unit = js.native
  def input             (t: Node, eventData: js.Object = js.native): Unit = js.native
  def keyDown           (t: Node, eventData: js.Object = js.native): Unit = js.native
//def keyPress          (t: Node, eventData: js.Object = js.native): Unit = js.native
  def keyUp             (t: Node, eventData: js.Object = js.native): Unit = js.native
//def load              (t: Node, eventData: js.Object = js.native): Unit = js.native
  def lostPointerCapture(t: Node, eventData: js.Object = js.native): Unit = js.native
  def mouseDown         (t: Node, eventData: js.Object = js.native): Unit = js.native
//def mouseEnter        (t: Node, eventData: js.Object = js.native): Unit = js.native
//def mouseLeave        (t: Node, eventData: js.Object = js.native): Unit = js.native
  def mouseMove         (t: Node, eventData: js.Object = js.native): Unit = js.native
  def mouseOut          (t: Node, eventData: js.Object = js.native): Unit = js.native
  def mouseOver         (t: Node, eventData: js.Object = js.native): Unit = js.native
  def mouseUp           (t: Node, eventData: js.Object = js.native): Unit = js.native
  def paste             (t: Node, eventData: js.Object = js.native): Unit = js.native
  def pointerCancel     (t: Node, eventData: js.Object = js.native): Unit = js.native
  def pointerDown       (t: Node, eventData: js.Object = js.native): Unit = js.native
//def pointerEnter      (t: Node, eventData: js.Object = js.native): Unit = js.native
//def pointerLeave      (t: Node, eventData: js.Object = js.native): Unit = js.native
  def pointerMove       (t: Node, eventData: js.Object = js.native): Unit = js.native
  def pointerOut        (t: Node, eventData: js.Object = js.native): Unit = js.native
  def pointerOver       (t: Node, eventData: js.Object = js.native): Unit = js.native
  def pointerUp         (t: Node, eventData: js.Object = js.native): Unit = js.native
  def reset             (t: Node, eventData: js.Object = js.native): Unit = js.native
  def scroll            (t: Node, eventData: js.Object = js.native): Unit = js.native
//def select            (t: Node, eventData: js.Object = js.native): Unit = js.native
  def submit            (t: Node, eventData: js.Object = js.native): Unit = js.native
  def touchCancel       (t: Node, eventData: js.Object = js.native): Unit = js.native
  def touchEnd          (t: Node, eventData: js.Object = js.native): Unit = js.native
  def touchMove         (t: Node, eventData: js.Object = js.native): Unit = js.native
  def touchStart        (t: Node, eventData: js.Object = js.native): Unit = js.native
  def wheel             (t: Node, eventData: js.Object = js.native): Unit = js.native
}
