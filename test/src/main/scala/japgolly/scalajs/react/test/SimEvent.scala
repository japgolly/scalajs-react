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

    import org.scalajs.dom.ext.{KeyCode, KeyValue}

    def Alt      : Keyboard = apply(key = KeyValue.Alt       , keyCode = KeyCode.Alt)
    def Ctrl     : Keyboard = apply(key = KeyValue.Control   , keyCode = KeyCode.Ctrl)
    def CapsLock : Keyboard = apply(key = KeyValue.CapsLock  , keyCode = KeyCode.CapsLock)
    def Shift    : Keyboard = apply(key = KeyValue.Shift     , keyCode = KeyCode.Shift)

    def Backspace: Keyboard = apply(key = KeyValue.Backspace , keyCode = KeyCode.Backspace)
    def Enter    : Keyboard = apply(key = KeyValue.Enter     , keyCode = KeyCode.Enter)
    def Escape   : Keyboard = apply(key = KeyValue.Escape    , keyCode = KeyCode.Escape)
    def Pause    : Keyboard = apply(key = KeyValue.Pause     , keyCode = KeyCode.Pause)
    def Space    : Keyboard = apply(key = KeyValue.Spacebar  , keyCode = KeyCode.Space)
    def Tab      : Keyboard = apply(key = KeyValue.Tab       , keyCode = KeyCode.Tab)

    def Down     : Keyboard = apply(key = KeyValue.ArrowDown , keyCode = KeyCode.Down)
    def Left     : Keyboard = apply(key = KeyValue.ArrowLeft , keyCode = KeyCode.Left)
    def Right    : Keyboard = apply(key = KeyValue.ArrowRight, keyCode = KeyCode.Right)
    def Up       : Keyboard = apply(key = KeyValue.ArrowUp   , keyCode = KeyCode.Up)

    def Insert   : Keyboard = apply(key = KeyValue.Insert    , keyCode = KeyCode.Insert)
    def Delete   : Keyboard = apply(key = KeyValue.Delete    , keyCode = KeyCode.Delete)
    def Home     : Keyboard = apply(key = KeyValue.Home      , keyCode = KeyCode.Home)
    def End      : Keyboard = apply(key = KeyValue.End       , keyCode = KeyCode.End)
    def PageUp   : Keyboard = apply(key = KeyValue.PageUp    , keyCode = KeyCode.PageUp)
    def PageDown : Keyboard = apply(key = KeyValue.PageDown  , keyCode = KeyCode.PageDown)

    def F1       : Keyboard = apply(key = KeyValue.F1        , keyCode = KeyCode.F1)
    def F2       : Keyboard = apply(key = KeyValue.F2        , keyCode = KeyCode.F2)
    def F3       : Keyboard = apply(key = KeyValue.F3        , keyCode = KeyCode.F3)
    def F4       : Keyboard = apply(key = KeyValue.F4        , keyCode = KeyCode.F4)
    def F5       : Keyboard = apply(key = KeyValue.F5        , keyCode = KeyCode.F5)
    def F6       : Keyboard = apply(key = KeyValue.F6        , keyCode = KeyCode.F6)
    def F7       : Keyboard = apply(key = KeyValue.F7        , keyCode = KeyCode.F7)
    def F8       : Keyboard = apply(key = KeyValue.F8        , keyCode = KeyCode.F8)
    def F9       : Keyboard = apply(key = KeyValue.F9        , keyCode = KeyCode.F9)
    def F10      : Keyboard = apply(key = KeyValue.F10       , keyCode = KeyCode.F10)
    def F11      : Keyboard = apply(key = KeyValue.F11       , keyCode = KeyCode.F11)
    def F12      : Keyboard = apply(key = KeyValue.F12       , keyCode = KeyCode.F12)

    def Num0     : Keyboard = apply(key = "0"                , keyCode = KeyCode.Num0)
    def Num1     : Keyboard = apply(key = "1"                , keyCode = KeyCode.Num1)
    def Num2     : Keyboard = apply(key = "2"                , keyCode = KeyCode.Num2)
    def Num3     : Keyboard = apply(key = "3"                , keyCode = KeyCode.Num3)
    def Num4     : Keyboard = apply(key = "4"                , keyCode = KeyCode.Num4)
    def Num5     : Keyboard = apply(key = "5"                , keyCode = KeyCode.Num5)
    def Num6     : Keyboard = apply(key = "6"                , keyCode = KeyCode.Num6)
    def Num7     : Keyboard = apply(key = "7"                , keyCode = KeyCode.Num7)
    def Num8     : Keyboard = apply(key = "8"                , keyCode = KeyCode.Num8)
    def Num9     : Keyboard = apply(key = "9"                , keyCode = KeyCode.Num9)

    def A        : Keyboard = apply(key = "A"                , keyCode = KeyCode.A)
    def B        : Keyboard = apply(key = "B"                , keyCode = KeyCode.B)
    def C        : Keyboard = apply(key = "C"                , keyCode = KeyCode.C)
    def D        : Keyboard = apply(key = "D"                , keyCode = KeyCode.D)
    def E        : Keyboard = apply(key = "E"                , keyCode = KeyCode.E)
    def F        : Keyboard = apply(key = "F"                , keyCode = KeyCode.F)
    def G        : Keyboard = apply(key = "G"                , keyCode = KeyCode.G)
    def H        : Keyboard = apply(key = "H"                , keyCode = KeyCode.H)
    def I        : Keyboard = apply(key = "I"                , keyCode = KeyCode.I)
    def J        : Keyboard = apply(key = "J"                , keyCode = KeyCode.J)
    def K        : Keyboard = apply(key = "K"                , keyCode = KeyCode.K)
    def L        : Keyboard = apply(key = "L"                , keyCode = KeyCode.L)
    def M        : Keyboard = apply(key = "M"                , keyCode = KeyCode.M)
    def N        : Keyboard = apply(key = "N"                , keyCode = KeyCode.N)
    def O        : Keyboard = apply(key = "O"                , keyCode = KeyCode.O)
    def P        : Keyboard = apply(key = "P"                , keyCode = KeyCode.P)
    def Q        : Keyboard = apply(key = "Q"                , keyCode = KeyCode.Q)
    def R        : Keyboard = apply(key = "R"                , keyCode = KeyCode.R)
    def S        : Keyboard = apply(key = "S"                , keyCode = KeyCode.S)
    def T        : Keyboard = apply(key = "T"                , keyCode = KeyCode.T)
    def U        : Keyboard = apply(key = "U"                , keyCode = KeyCode.U)
    def V        : Keyboard = apply(key = "V"                , keyCode = KeyCode.V)
    def W        : Keyboard = apply(key = "W"                , keyCode = KeyCode.W)
    def X        : Keyboard = apply(key = "X"                , keyCode = KeyCode.X)
    def Y        : Keyboard = apply(key = "Y"                , keyCode = KeyCode.Y)
    def Z        : Keyboard = apply(key = "Z"                , keyCode = KeyCode.Z)

    def a        : Keyboard = apply(key = "a"                , keyCode = KeyCode.A)
    def b        : Keyboard = apply(key = "b"                , keyCode = KeyCode.B)
    def c        : Keyboard = apply(key = "c"                , keyCode = KeyCode.C)
    def d        : Keyboard = apply(key = "d"                , keyCode = KeyCode.D)
    def e        : Keyboard = apply(key = "e"                , keyCode = KeyCode.E)
    def f        : Keyboard = apply(key = "f"                , keyCode = KeyCode.F)
    def g        : Keyboard = apply(key = "g"                , keyCode = KeyCode.G)
    def h        : Keyboard = apply(key = "h"                , keyCode = KeyCode.H)
    def i        : Keyboard = apply(key = "i"                , keyCode = KeyCode.I)
    def j        : Keyboard = apply(key = "j"                , keyCode = KeyCode.J)
    def k        : Keyboard = apply(key = "k"                , keyCode = KeyCode.K)
    def l        : Keyboard = apply(key = "l"                , keyCode = KeyCode.L)
    def m        : Keyboard = apply(key = "m"                , keyCode = KeyCode.M)
    def n        : Keyboard = apply(key = "n"                , keyCode = KeyCode.N)
    def o        : Keyboard = apply(key = "o"                , keyCode = KeyCode.O)
    def p        : Keyboard = apply(key = "p"                , keyCode = KeyCode.P)
    def q        : Keyboard = apply(key = "q"                , keyCode = KeyCode.Q)
    def r        : Keyboard = apply(key = "r"                , keyCode = KeyCode.R)
    def s        : Keyboard = apply(key = "s"                , keyCode = KeyCode.S)
    def t        : Keyboard = apply(key = "t"                , keyCode = KeyCode.T)
    def u        : Keyboard = apply(key = "u"                , keyCode = KeyCode.U)
    def v        : Keyboard = apply(key = "v"                , keyCode = KeyCode.V)
    def w        : Keyboard = apply(key = "w"                , keyCode = KeyCode.W)
    def x        : Keyboard = apply(key = "x"                , keyCode = KeyCode.X)
    def y        : Keyboard = apply(key = "y"                , keyCode = KeyCode.Y)
    def z        : Keyboard = apply(key = "z"                , keyCode = KeyCode.Z)
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
