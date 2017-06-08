package japgolly.scalajs.react.test

import scala.scalajs.js
import scala.scalajs.js.Dynamic

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// NOTE: Do not use UndefOr for arguments below; undefined causes Phantom-bloody-JS to crash.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

object SimEvent {

  case class Change(value           : String              = "",
                    checked         : js.UndefOr[Boolean] = js.undefined,
                    defaultPrevented: Boolean             = false) {
    def toJs: js.Object = {
      val target = Dynamic.literal(
        "value"            -> value,
        "checked"          -> checked,
        "defaultPrevented" -> defaultPrevented)
      val o = Dynamic.literal("target" -> target)
      o
    }
    def simulate(t: ReactOrDomNode) = Simulate.change(t, this)
    def simulation = Simulation.change(this)
  }

  object Change {
    implicit def autoToJsObject(d: Change): js.Object = d.toJs
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  case class Keyboard(key             : String  = "",
                      location        : Double  = 0,
                      altKey          : Boolean = false,
                      ctrlKey         : Boolean = false,
                      metaKey         : Boolean = false,
                      shiftKey        : Boolean = false,
                      repeat          : Boolean = false,
                      locale          : String  = "",
                      keyCode         : Int     = 0,
                      charCode        : Int     = 0,
                      which           : Int     = 0,
                      defaultPrevented: Boolean = false) {

    def alt   = copy(altKey   = true)
    def ctrl  = copy(ctrlKey  = true)
    def meta  = copy(metaKey  = true)
    def shift = copy(shiftKey = true)

    def desc: String = {
      var s = key
      if (s.isEmpty) s = s"($keyCode)"
      if (shiftKey ) s = "Shift-" + s
      if (altKey   ) s = "Alt-" + s
      if (ctrlKey  ) s = "Ctrl-" + s
      if (metaKey  ) s = "Meta-" + s
      s
    }

    def toJs: js.Object = {
      val o = Dynamic.literal()
      o.updateDynamic("key"             )(key             )
      o.updateDynamic("location"        )(location        )
      o.updateDynamic("altKey"          )(altKey          )
      o.updateDynamic("ctrlKey"         )(ctrlKey         )
      o.updateDynamic("metaKey"         )(metaKey         )
      o.updateDynamic("shiftKey"        )(shiftKey        )
      o.updateDynamic("repeat"          )(repeat          )
      o.updateDynamic("locale"          )(locale          )
      o.updateDynamic("keyCode"         )(keyCode         )
      o.updateDynamic("charCode"        )(charCode        )
      o.updateDynamic("which"           )(which           )
      o.updateDynamic("defaultPrevented")(defaultPrevented)
      o
    }
    def simulateKeyDown       (t: ReactOrDomNode): Unit = Simulate.keyDown (t, this)
    def simulateKeyPress      (t: ReactOrDomNode): Unit = Simulate.keyPress(t, this)
    def simulateKeyUp         (t: ReactOrDomNode): Unit = Simulate.keyUp   (t, this)
    def simulateKeyDownUp     (t: ReactOrDomNode): Unit = {simulateKeyDown(t); simulateKeyUp(t)}
    def simulateKeyDownPressUp(t: ReactOrDomNode): Unit = {simulateKeyDown(t); simulateKeyPress(t); simulateKeyUp(t)}
    def simulationKeyDown        = Simulation.keyDown(this)
    def simulationKeyPress       = Simulation.keyPress(this)
    def simulationKeyUp          = Simulation.keyUp(this)
    def simulationKeyDownUp      = simulationKeyDown >> simulationKeyUp
    def simulationKeyDownPressUp = simulationKeyDown >> simulationKeyPress >> simulationKeyUp
  }

  object Keyboard {
    implicit def autoToJsObject(d: Keyboard): js.Object = d.toJs
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  case class Mouse(screenX         : Double  = 0,
                   screenY         : Double  = 0,
                   clientX         : Double  = 0,
                   clientY         : Double  = 0,
                   pageX           : Double  = 0,
                   pageY           : Double  = 0,
                   altKey          : Boolean = false,
                   ctrlKey         : Boolean = false,
                   metaKey         : Boolean = false,
                   shiftKey        : Boolean = false,
                   button          : Int     = 0,
                   buttons         : Int     = 0,
                   defaultPrevented: Boolean = false) {

    def alt   = copy(altKey   = true)
    def ctrl  = copy(ctrlKey  = true)
    def meta  = copy(metaKey  = true)
    def shift = copy(shiftKey = true)

    def toJs: js.Object = {
      val o = Dynamic.literal()
      o.updateDynamic("screenX"         )(screenX         )
      o.updateDynamic("screenY"         )(screenY         )
      o.updateDynamic("clientX"         )(clientX         )
      o.updateDynamic("clientY"         )(clientY         )
      o.updateDynamic("pageX"           )(pageX           )
      o.updateDynamic("pageY"           )(pageY           )
      o.updateDynamic("altKey"          )(altKey          )
      o.updateDynamic("ctrlKey"         )(ctrlKey         )
      o.updateDynamic("metaKey"         )(metaKey         )
      o.updateDynamic("shiftKey"        )(shiftKey        )
      o.updateDynamic("button"          )(button          )
      o.updateDynamic("buttons"         )(buttons         )
      o.updateDynamic("defaultPrevented")(defaultPrevented)
      o
    }
    def simulateDrag      (t: ReactOrDomNode) = Simulate.drag      (t, this)
    def simulateDragEnd   (t: ReactOrDomNode) = Simulate.dragEnd   (t, this)
    def simulateDragEnter (t: ReactOrDomNode) = Simulate.dragEnter (t, this)
    def simulateDragExit  (t: ReactOrDomNode) = Simulate.dragExit  (t, this)
    def simulateDragLeave (t: ReactOrDomNode) = Simulate.dragLeave (t, this)
    def simulateDragOver  (t: ReactOrDomNode) = Simulate.dragOver  (t, this)
    def simulateDragStart (t: ReactOrDomNode) = Simulate.dragStart (t, this)
    def simulateDrop      (t: ReactOrDomNode) = Simulate.drop      (t, this)
    def simulateMouseDown (t: ReactOrDomNode) = Simulate.mouseDown (t, this)
    def simulateMouseEnter(t: ReactOrDomNode) = Simulate.mouseEnter(t, this)
    def simulateMouseLeave(t: ReactOrDomNode) = Simulate.mouseLeave(t, this)
    def simulateMouseMove (t: ReactOrDomNode) = Simulate.mouseMove (t, this)
    def simulateMouseOut  (t: ReactOrDomNode) = Simulate.mouseOut  (t, this)
    def simulateMouseOver (t: ReactOrDomNode) = Simulate.mouseOver (t, this)
    def simulateMouseUp   (t: ReactOrDomNode) = Simulate.mouseUp   (t, this)
    def simulateWheel     (t: ReactOrDomNode) = Simulate.wheel     (t, this)
    def simulationDrag       = Simulation.drag      (this)
    def simulationDragEnd    = Simulation.dragEnd   (this)
    def simulationDragEnter  = Simulation.dragEnter (this)
    def simulationDragExit   = Simulation.dragExit  (this)
    def simulationDragLeave  = Simulation.dragLeave (this)
    def simulationDragOver   = Simulation.dragOver  (this)
    def simulationDragStart  = Simulation.dragStart (this)
    def simulationMouseDown  = Simulation.mouseDown (this)
    def simulationMouseEnter = Simulation.mouseEnter(this)
    def simulationMouseLeave = Simulation.mouseLeave(this)
    def simulationMouseMove  = Simulation.mouseMove (this)
    def simulationMouseOut   = Simulation.mouseOut  (this)
    def simulationMouseOver  = Simulation.mouseOver (this)
    def simulationMouseUp    = Simulation.mouseUp   (this)
    def simulationWheel      = Simulation.wheel     (this)
  }

  object Mouse {
    implicit def autoToJsObject(d: Mouse): js.Object = d.toJs
  }

}
