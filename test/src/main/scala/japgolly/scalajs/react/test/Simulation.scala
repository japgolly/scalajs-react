package japgolly.scalajs.react.test

import scala.scalajs.js

/**
 * Allows composition and abstraction of `ReactTestUtils.Simulate` procedures.
 */
class Simulation(_run: (() => ReactOrDomNode) => Unit) {

  def run(n: => ReactOrDomNode): Unit =
    _run(() => n)

  def andThen(f: Simulation) =
    new Simulation(n => { _run(n); f.run(n()) })

  @inline final def >>     (f: Simulation) = this andThen f
  @inline final def compose(f: Simulation) = f andThen this

  final def runN(cs: ReactOrDomNode*): Unit =
    cs foreach (run(_))
}

object Simulation {

  def apply(run: (=> ReactOrDomNode) => Unit): Simulation =
    new Simulation(n => run(n()))

  // Don't use default arguments - they force parentheses on to caller.
  // Eg. Simulation.blur >> Simulation.focus becomes Simulation.blur() >> Simulation.focus(). Yuk.

  def auxClick           = Simulation(Simulate.auxClick          (_, SimEvent.Mouse()))
  def beforeInput        = Simulation(Simulate.beforeInput       (_))
  def blur               = Simulation(Simulate.blur              (_))
  def change             = Simulation(Simulate.change            (_))
  def click              = Simulation(Simulate.click             (_, SimEvent.Mouse()))
  def compositionEnd     = Simulation(Simulate.compositionEnd    (_))
  def compositionStart   = Simulation(Simulate.compositionStart  (_))
  def compositionUpdate  = Simulation(Simulate.compositionUpdate (_))
  def contextMenu        = Simulation(Simulate.contextMenu       (_, SimEvent.Mouse()))
  def copy               = Simulation(Simulate.copy              (_))
  def cut                = Simulation(Simulate.cut               (_))
  def doubleClick        = Simulation(Simulate.doubleClick       (_, SimEvent.Mouse()))
  def dragEnd            = Simulation(Simulate.dragEnd           (_, SimEvent.Mouse()))
  def dragEnter          = Simulation(Simulate.dragEnter         (_, SimEvent.Mouse()))
  def dragExit           = Simulation(Simulate.dragExit          (_, SimEvent.Mouse()))
  def dragLeave          = Simulation(Simulate.dragLeave         (_, SimEvent.Mouse()))
  def dragOver           = Simulation(Simulate.dragOver          (_, SimEvent.Mouse()))
  def drag               = Simulation(Simulate.drag              (_, SimEvent.Mouse()))
  def dragStart          = Simulation(Simulate.dragStart         (_, SimEvent.Mouse()))
  def drop               = Simulation(Simulate.drop              (_, SimEvent.Mouse()))
  def error              = Simulation(Simulate.error             (_))
  def focus              = Simulation(Simulate.focus             (_))
  def gotPointerCapture  = Simulation(Simulate.gotPointerCapture (_, SimEvent.Pointer()))
  def input              = Simulation(Simulate.input             (_))
  def keyDown            = Simulation(Simulate.keyDown           (_, SimEvent.Keyboard()))
  def keyPress           = Simulation(Simulate.keyPress          (_, SimEvent.Keyboard()))
  def keyUp              = Simulation(Simulate.keyUp             (_, SimEvent.Keyboard()))
  def load               = Simulation(Simulate.load              (_))
  def lostPointerCapture = Simulation(Simulate.lostPointerCapture(_, SimEvent.Pointer()))
  def mouseDown          = Simulation(Simulate.mouseDown         (_, SimEvent.Mouse()))
  def mouseEnter         = Simulation(Simulate.mouseEnter        (_, SimEvent.Mouse()))
  def mouseLeave         = Simulation(Simulate.mouseLeave        (_, SimEvent.Mouse()))
  def mouseMove          = Simulation(Simulate.mouseMove         (_, SimEvent.Mouse()))
  def mouseOut           = Simulation(Simulate.mouseOut          (_, SimEvent.Mouse()))
  def mouseOver          = Simulation(Simulate.mouseOver         (_, SimEvent.Mouse()))
  def mouseUp            = Simulation(Simulate.mouseUp           (_, SimEvent.Mouse()))
  def paste              = Simulation(Simulate.paste             (_))
  def pointerCancel      = Simulation(Simulate.pointerCancel     (_, SimEvent.Pointer()))
  def pointerDown        = Simulation(Simulate.pointerDown       (_, SimEvent.Pointer()))
  def pointerEnter       = Simulation(Simulate.pointerEnter      (_, SimEvent.Pointer()))
  def pointerLeave       = Simulation(Simulate.pointerLeave      (_, SimEvent.Pointer()))
  def pointerMove        = Simulation(Simulate.pointerMove       (_, SimEvent.Pointer()))
  def pointerOut         = Simulation(Simulate.pointerOut        (_, SimEvent.Pointer()))
  def pointerOver        = Simulation(Simulate.pointerOver       (_, SimEvent.Pointer()))
  def pointerUp          = Simulation(Simulate.pointerUp         (_, SimEvent.Pointer()))
  def reset              = Simulation(Simulate.reset             (_))
  def scroll             = Simulation(Simulate.scroll            (_))
  def select             = Simulation(Simulate.select            (_))
  def submit             = Simulation(Simulate.submit            (_))
  def touchCancel        = Simulation(Simulate.touchCancel       (_))
  def touchEnd           = Simulation(Simulate.touchEnd          (_))
  def touchMove          = Simulation(Simulate.touchMove         (_))
  def touchStart         = Simulation(Simulate.touchStart        (_))
  def wheel              = Simulation(Simulate.wheel             (_))

  def auxClick          (eventData: js.Object) = Simulation(Simulate.auxClick          (_, eventData))
  def beforeInput       (eventData: js.Object) = Simulation(Simulate.beforeInput       (_, eventData))
  def blur              (eventData: js.Object) = Simulation(Simulate.blur              (_, eventData))
  def change            (eventData: js.Object) = Simulation(Simulate.change            (_, eventData))
  def click             (eventData: js.Object) = Simulation(Simulate.click             (_, eventData))
  def compositionEnd    (eventData: js.Object) = Simulation(Simulate.compositionEnd    (_, eventData))
  def compositionStart  (eventData: js.Object) = Simulation(Simulate.compositionStart  (_, eventData))
  def compositionUpdate (eventData: js.Object) = Simulation(Simulate.compositionUpdate (_, eventData))
  def contextMenu       (eventData: js.Object) = Simulation(Simulate.contextMenu       (_, eventData))
  def copy              (eventData: js.Object) = Simulation(Simulate.copy              (_, eventData))
  def cut               (eventData: js.Object) = Simulation(Simulate.cut               (_, eventData))
  def doubleClick       (eventData: js.Object) = Simulation(Simulate.doubleClick       (_, eventData))
  def dragEnd           (eventData: js.Object) = Simulation(Simulate.dragEnd           (_, eventData))
  def dragEnter         (eventData: js.Object) = Simulation(Simulate.dragEnter         (_, eventData))
  def drag              (eventData: js.Object) = Simulation(Simulate.drag              (_, eventData))
  def dragExit          (eventData: js.Object) = Simulation(Simulate.dragExit          (_, eventData))
  def dragLeave         (eventData: js.Object) = Simulation(Simulate.dragLeave         (_, eventData))
  def dragOver          (eventData: js.Object) = Simulation(Simulate.dragOver          (_, eventData))
  def dragStart         (eventData: js.Object) = Simulation(Simulate.dragStart         (_, eventData))
  def drop              (eventData: js.Object) = Simulation(Simulate.drop              (_, eventData))
  def error             (eventData: js.Object) = Simulation(Simulate.error             (_, eventData))
  def focus             (eventData: js.Object) = Simulation(Simulate.focus             (_, eventData))
  def gotPointerCapture (eventData: js.Object) = Simulation(Simulate.gotPointerCapture (_, eventData))
  def input             (eventData: js.Object) = Simulation(Simulate.input             (_, eventData))
  def keyDown           (eventData: js.Object) = Simulation(Simulate.keyDown           (_, eventData))
  def keyPress          (eventData: js.Object) = Simulation(Simulate.keyPress          (_, eventData))
  def keyUp             (eventData: js.Object) = Simulation(Simulate.keyUp             (_, eventData))
  def load              (eventData: js.Object) = Simulation(Simulate.load              (_, eventData))
  def lostPointerCapture(eventData: js.Object) = Simulation(Simulate.lostPointerCapture(_, eventData))
  def mouseDown         (eventData: js.Object) = Simulation(Simulate.mouseDown         (_, eventData))
  def mouseEnter        (eventData: js.Object) = Simulation(Simulate.mouseEnter        (_, eventData))
  def mouseLeave        (eventData: js.Object) = Simulation(Simulate.mouseLeave        (_, eventData))
  def mouseMove         (eventData: js.Object) = Simulation(Simulate.mouseMove         (_, eventData))
  def mouseOut          (eventData: js.Object) = Simulation(Simulate.mouseOut          (_, eventData))
  def mouseOver         (eventData: js.Object) = Simulation(Simulate.mouseOver         (_, eventData))
  def mouseUp           (eventData: js.Object) = Simulation(Simulate.mouseUp           (_, eventData))
  def paste             (eventData: js.Object) = Simulation(Simulate.paste             (_, eventData))
  def pointerCancel     (eventData: js.Object) = Simulation(Simulate.pointerCancel     (_, eventData))
  def pointerDown       (eventData: js.Object) = Simulation(Simulate.pointerDown       (_, eventData))
  def pointerEnter      (eventData: js.Object) = Simulation(Simulate.pointerEnter      (_, eventData))
  def pointerLeave      (eventData: js.Object) = Simulation(Simulate.pointerLeave      (_, eventData))
  def pointerMove       (eventData: js.Object) = Simulation(Simulate.pointerMove       (_, eventData))
  def pointerOut        (eventData: js.Object) = Simulation(Simulate.pointerOut        (_, eventData))
  def pointerOver       (eventData: js.Object) = Simulation(Simulate.pointerOver       (_, eventData))
  def pointerUp         (eventData: js.Object) = Simulation(Simulate.pointerUp         (_, eventData))
  def reset             (eventData: js.Object) = Simulation(Simulate.reset             (_, eventData))
  def scroll            (eventData: js.Object) = Simulation(Simulate.scroll            (_, eventData))
  def select            (eventData: js.Object) = Simulation(Simulate.select            (_, eventData))
  def submit            (eventData: js.Object) = Simulation(Simulate.submit            (_, eventData))
  def touchCancel       (eventData: js.Object) = Simulation(Simulate.touchCancel       (_, eventData))
  def touchEnd          (eventData: js.Object) = Simulation(Simulate.touchEnd          (_, eventData))
  def touchMove         (eventData: js.Object) = Simulation(Simulate.touchMove         (_, eventData))
  def touchStart        (eventData: js.Object) = Simulation(Simulate.touchStart        (_, eventData))
  def wheel             (eventData: js.Object) = Simulation(Simulate.wheel             (_, eventData))

  // Helpers for common scenarios

  def focusSimBlur(s: Simulation) =
    focus >> s >> blur

  def focusChangeBlur(newValue: String) =
    focusSimBlur(SimEvent.Change(value = newValue).simulation)
}
