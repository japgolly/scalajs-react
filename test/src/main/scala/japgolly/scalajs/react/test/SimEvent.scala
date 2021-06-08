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

    def assign(tgt: Dynamic, readOnlyProperties: Boolean = false): tgt.type = {
      val innerTgt: Dynamic = {
        (tgt.target: Any) match {
          case null | () =>
            tgt.target = Dynamic.literal()
            tgt.target
          case other =>
            other.asInstanceOf[Dynamic]
        }
      }
      assignTarget(innerTgt)
      if (readOnlyProperties) {
        tgt.defaultPrevented = defaultPrevented
      }
      tgt
    }

    def assignTarget(tgt: Dynamic): tgt.type = {
      tgt.value   = value
      tgt.checked = checked.asInstanceOf[js.Any]
      tgt
    }

    def toJs: js.Object =
      assign(Dynamic.literal(), readOnlyProperties = true)

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
                      code            : String  = "",
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

    def assign(tgt: Dynamic, readOnlyProperties: Boolean = false): tgt.type = {
      tgt.key      = key
      tgt.location = location
      tgt.altKey   = altKey
      tgt.ctrlKey  = ctrlKey
      tgt.metaKey  = metaKey
      tgt.shiftKey = shiftKey
      tgt.repeat   = repeat
      tgt.code     = code
      tgt.locale   = locale
      tgt.keyCode  = keyCode
      tgt.charCode = charCode
      tgt.which    = which
      if (readOnlyProperties) {
        tgt.defaultPrevented = defaultPrevented
      }
      tgt
    }

    def toJs: js.Object =
      assign(Dynamic.literal(), readOnlyProperties = true)

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

    def Alt            : Keyboard = apply(key = "Alt", keyCode = 18, which = 18, altKey = true)
    def ContextMenu    : Keyboard = apply(key = "ContextMenu", keyCode = 93, which = 93)
    def Ctrl           : Keyboard = apply(key = "Control", keyCode = 17, which = 17, ctrlKey = true)
    def MetaLeft       : Keyboard = apply(key = "Meta", keyCode = 91, which = 91) // lack of `metaKey = true` is deliberate here
    def MetaRight      : Keyboard = apply(key = "Meta", keyCode = 92, which = 92) // lack of `metaKey = true` is deliberate here
    def Shift          : Keyboard = apply(key = "Shift", keyCode = 16, which = 16, shiftKey = true)

    def CapsLock       : Keyboard = apply(key = "CapsLock", keyCode = 20, which = 20)
    def NumLock        : Keyboard = apply(key = "NumLock", keyCode = 144, which = 144)
    def ScrollLock     : Keyboard = apply(key = "ScrollLock", keyCode = 145, which = 145)

    def Down           : Keyboard = apply(key = "ArrowDown", keyCode = 40, which = 40)
    def Left           : Keyboard = apply(key = "ArrowLeft", keyCode = 37, which = 37)
    def Right          : Keyboard = apply(key = "ArrowRight", keyCode = 39, which = 39)
    def Up             : Keyboard = apply(key = "ArrowUp", keyCode = 38, which = 38)

    def Insert         : Keyboard = apply(key = "Insert", keyCode = 45, which = 45)
    def Delete         : Keyboard = apply(key = "Delete", keyCode = 46, which = 46)
    def Home           : Keyboard = apply(key = "Home", keyCode = 36, which = 36)
    def End            : Keyboard = apply(key = "End", keyCode = 35, which = 35)
    def PageDown       : Keyboard = apply(key = "PageDown", keyCode = 34, which = 34)
    def PageUp         : Keyboard = apply(key = "PageUp", keyCode = 33, which = 33)

    def F1             : Keyboard = apply(key = "F1", keyCode = 112, which = 112)
    def F2             : Keyboard = apply(key = "F2", keyCode = 113, which = 113)
    def F3             : Keyboard = apply(key = "F3", keyCode = 114, which = 114)
    def F4             : Keyboard = apply(key = "F4", keyCode = 115, which = 115)
    def F5             : Keyboard = apply(key = "F5", keyCode = 116, which = 116)
    def F6             : Keyboard = apply(key = "F6", keyCode = 117, which = 117)
    def F7             : Keyboard = apply(key = "F7", keyCode = 118, which = 118)
    def F8             : Keyboard = apply(key = "F8", keyCode = 119, which = 119)
    def F9             : Keyboard = apply(key = "F9", keyCode = 120, which = 120)
    def F10            : Keyboard = apply(key = "F10", keyCode = 121, which = 121)
    def F11            : Keyboard = apply(key = "F11", keyCode = 122, which = 122)
    def F12            : Keyboard = apply(key = "F12", keyCode = 123, which = 123)

    def Num0           : Keyboard = apply(key = "0", keyCode = 48, which = 48)
    def Num1           : Keyboard = apply(key = "1", keyCode = 49, which = 49)
    def Num2           : Keyboard = apply(key = "2", keyCode = 50, which = 50)
    def Num3           : Keyboard = apply(key = "3", keyCode = 51, which = 51)
    def Num4           : Keyboard = apply(key = "4", keyCode = 52, which = 52)
    def Num5           : Keyboard = apply(key = "5", keyCode = 53, which = 53)
    def Num6           : Keyboard = apply(key = "6", keyCode = 54, which = 54)
    def Num7           : Keyboard = apply(key = "7", keyCode = 55, which = 55)
    def Num8           : Keyboard = apply(key = "8", keyCode = 56, which = 56)
    def Num9           : Keyboard = apply(key = "9", keyCode = 57, which = 57)

    def NumPad0        : Keyboard = apply(key = "0", keyCode = 96, which = 96)
    def NumPad1        : Keyboard = apply(key = "1", keyCode = 97, which = 97)
    def NumPad2        : Keyboard = apply(key = "2", keyCode = 98, which = 98)
    def NumPad3        : Keyboard = apply(key = "3", keyCode = 99, which = 99)
    def NumPad4        : Keyboard = apply(key = "4", keyCode = 100, which = 100)
    def NumPad5        : Keyboard = apply(key = "5", keyCode = 101, which = 101)
    def NumPad6        : Keyboard = apply(key = "6", keyCode = 102, which = 102)
    def NumPad7        : Keyboard = apply(key = "7", keyCode = 103, which = 103)
    def NumPad8        : Keyboard = apply(key = "8", keyCode = 104, which = 104)
    def NumPad9        : Keyboard = apply(key = "9", keyCode = 105, which = 105)

    def A              : Keyboard = apply(key = "A", keyCode = 65, which = 65, shiftKey = true)
    def B              : Keyboard = apply(key = "B", keyCode = 66, which = 66, shiftKey = true)
    def C              : Keyboard = apply(key = "C", keyCode = 67, which = 67, shiftKey = true)
    def D              : Keyboard = apply(key = "D", keyCode = 68, which = 68, shiftKey = true)
    def E              : Keyboard = apply(key = "E", keyCode = 69, which = 69, shiftKey = true)
    def F              : Keyboard = apply(key = "F", keyCode = 70, which = 70, shiftKey = true)
    def G              : Keyboard = apply(key = "G", keyCode = 71, which = 71, shiftKey = true)
    def H              : Keyboard = apply(key = "H", keyCode = 72, which = 72, shiftKey = true)
    def I              : Keyboard = apply(key = "I", keyCode = 73, which = 73, shiftKey = true)
    def J              : Keyboard = apply(key = "J", keyCode = 74, which = 74, shiftKey = true)
    def K              : Keyboard = apply(key = "K", keyCode = 75, which = 75, shiftKey = true)
    def L              : Keyboard = apply(key = "L", keyCode = 76, which = 76, shiftKey = true)
    def M              : Keyboard = apply(key = "M", keyCode = 77, which = 77, shiftKey = true)
    def N              : Keyboard = apply(key = "N", keyCode = 78, which = 78, shiftKey = true)
    def O              : Keyboard = apply(key = "O", keyCode = 79, which = 79, shiftKey = true)
    def P              : Keyboard = apply(key = "P", keyCode = 80, which = 80, shiftKey = true)
    def Q              : Keyboard = apply(key = "Q", keyCode = 81, which = 81, shiftKey = true)
    def R              : Keyboard = apply(key = "R", keyCode = 82, which = 82, shiftKey = true)
    def S              : Keyboard = apply(key = "S", keyCode = 83, which = 83, shiftKey = true)
    def T              : Keyboard = apply(key = "T", keyCode = 84, which = 84, shiftKey = true)
    def U              : Keyboard = apply(key = "U", keyCode = 85, which = 85, shiftKey = true)
    def V              : Keyboard = apply(key = "V", keyCode = 86, which = 86, shiftKey = true)
    def W              : Keyboard = apply(key = "W", keyCode = 87, which = 87, shiftKey = true)
    def X              : Keyboard = apply(key = "X", keyCode = 88, which = 88, shiftKey = true)
    def Y              : Keyboard = apply(key = "Y", keyCode = 89, which = 89, shiftKey = true)
    def Z              : Keyboard = apply(key = "Z", keyCode = 90, which = 90, shiftKey = true)

    def a              : Keyboard = apply(key = "a", keyCode = 65, which = 65)
    def b              : Keyboard = apply(key = "b", keyCode = 66, which = 66)
    def c              : Keyboard = apply(key = "c", keyCode = 67, which = 67)
    def d              : Keyboard = apply(key = "d", keyCode = 68, which = 68)
    def e              : Keyboard = apply(key = "e", keyCode = 69, which = 69)
    def f              : Keyboard = apply(key = "f", keyCode = 70, which = 70)
    def g              : Keyboard = apply(key = "g", keyCode = 71, which = 71)
    def h              : Keyboard = apply(key = "h", keyCode = 72, which = 72)
    def i              : Keyboard = apply(key = "i", keyCode = 73, which = 73)
    def j              : Keyboard = apply(key = "j", keyCode = 74, which = 74)
    def k              : Keyboard = apply(key = "k", keyCode = 75, which = 75)
    def l              : Keyboard = apply(key = "l", keyCode = 76, which = 76)
    def m              : Keyboard = apply(key = "m", keyCode = 77, which = 77)
    def n              : Keyboard = apply(key = "n", keyCode = 78, which = 78)
    def o              : Keyboard = apply(key = "o", keyCode = 79, which = 79)
    def p              : Keyboard = apply(key = "p", keyCode = 80, which = 80)
    def q              : Keyboard = apply(key = "q", keyCode = 81, which = 81)
    def r              : Keyboard = apply(key = "r", keyCode = 82, which = 82)
    def s              : Keyboard = apply(key = "s", keyCode = 83, which = 83)
    def t              : Keyboard = apply(key = "t", keyCode = 84, which = 84)
    def u              : Keyboard = apply(key = "u", keyCode = 85, which = 85)
    def v              : Keyboard = apply(key = "v", keyCode = 86, which = 86)
    def w              : Keyboard = apply(key = "w", keyCode = 87, which = 87)
    def x              : Keyboard = apply(key = "x", keyCode = 88, which = 88)
    def y              : Keyboard = apply(key = "y", keyCode = 89, which = 89)
    def z              : Keyboard = apply(key = "z", keyCode = 90, which = 90)

    def NumPadDivide   : Keyboard = apply(key = "/", keyCode = 111, which = 111)
    def NumPadMultiply : Keyboard = apply(key = "*", keyCode = 106, which = 106)
    def NumPadSubtract : Keyboard = apply(key = "-", keyCode = 109, which = 109)
    def NumPadAdd      : Keyboard = apply(key = "+", keyCode = 107, which = 107)
    def NumPadDot      : Keyboard = apply(key = ".", keyCode = 110, which = 110)

    def Apostrophe     : Keyboard = apply(key = "'", keyCode = 222, which = 222)
    def Backslash      : Keyboard = apply(key = "\\", keyCode = 220, which = 220)
    def Backspace      : Keyboard = apply(key = "Backspace", keyCode = 8, which = 8)
    def Backtick       : Keyboard = apply(key = "`", keyCode = 192, which = 192)
    def BracketLeft    : Keyboard = apply(key = "[", keyCode = 219, which = 219)
    def BracketRight   : Keyboard = apply(key = "]", keyCode = 221, which = 221)
    def Comma          : Keyboard = apply(key = ",", keyCode = 188, which = 188)
    def Enter          : Keyboard = apply(key = "Enter", keyCode = 13, which = 13)
    def Equals         : Keyboard = apply(key = "=", keyCode = 187, which = 187)
    def Escape         : Keyboard = apply(key = "Escape", keyCode = 27, which = 27)
    def Hyphen         : Keyboard = apply(key = "-", keyCode = 189, which = 189)
    def Pause          : Keyboard = apply(key = "Pause", keyCode = 19, which = 19)
    def Period         : Keyboard = apply(key = ".", keyCode = 190, which = 190)
    def Semicolon      : Keyboard = apply(key = ";", keyCode = 186, which = 186)
    def Slash          : Keyboard = apply(key = "/", keyCode = 191, which = 191)
    def Space          : Keyboard = apply(key = " ", keyCode = 32, which = 32)
    def Tab            : Keyboard = apply(key = "Tab", keyCode = 9, which = 9)

    def Ampersand      : Keyboard = apply(key = "&", keyCode = 55, which = 55, shiftKey = true)
    def Asterisk       : Keyboard = apply(key = "*", keyCode = 56, which = 56, shiftKey = true)
    def AtSign         : Keyboard = apply(key = "@", keyCode = 50, which = 50, shiftKey = true)
    def BraceLeft      : Keyboard = apply(key = "{", keyCode = 219, which = 219, shiftKey = true)
    def BraceRight     : Keyboard = apply(key = "}", keyCode = 221, which = 221, shiftKey = true)
    def Caret          : Keyboard = apply(key = "^", keyCode = 54, which = 54, shiftKey = true)
    def Colon          : Keyboard = apply(key = ":", keyCode = 186, which = 186, shiftKey = true)
    def DollarSign     : Keyboard = apply(key = "$", keyCode = 52, which = 52, shiftKey = true)
    def ExclamationMark: Keyboard = apply(key = "!", keyCode = 49, which = 49, shiftKey = true)
    def GreaterThan    : Keyboard = apply(key = ">", keyCode = 190, which = 190, shiftKey = true)
    def LessThan       : Keyboard = apply(key = "<", keyCode = 188, which = 188, shiftKey = true)
    def NumberSign     : Keyboard = apply(key = "#", keyCode = 51, which = 51, shiftKey = true)
    def ParenLeft      : Keyboard = apply(key = "(", keyCode = 57, which = 57, shiftKey = true)
    def ParenRight     : Keyboard = apply(key = ")", keyCode = 48, which = 48, shiftKey = true)
    def PercentSign    : Keyboard = apply(key = "%", keyCode = 53, which = 53, shiftKey = true)
    def Plus           : Keyboard = apply(key = "+", keyCode = 187, which = 187, shiftKey = true)
    def QuestionMark   : Keyboard = apply(key = "?", keyCode = 191, which = 191, shiftKey = true)
    def QuotationMark  : Keyboard = apply(key = "\"", keyCode = 222, which = 222, shiftKey = true)
    def Tilde          : Keyboard = apply(key = "~", keyCode = 192, which = 192, shiftKey = true)
    def Underscore     : Keyboard = apply(key = "_", keyCode = 189, which = 189, shiftKey = true)
    def VerticalBar    : Keyboard = apply(key = "|", keyCode = 220, which = 220, shiftKey = true)

    @inline def Meta = MetaLeft
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  case class Mouse(screenX         : Double  = 0,
                   screenY         : Double  = 0,
                   clientX         : Double  = 0,
                   clientY         : Double  = 0,
                   pageX           : Double  = 0,
                   pageY           : Double  = 0,
                   movementX       : Long    = 0,
                   movementY       : Long    = 0,
                   altKey          : Boolean = false,
                   ctrlKey         : Boolean = false,
                   metaKey         : Boolean = false,
                   shiftKey        : Boolean = false,
                   button          : Int     = 0,
                   buttons         : Int     = 0,
                   defaultPrevented: Boolean = false) extends MouseLike {

    def alt   = copy(altKey   = true)
    def ctrl  = copy(ctrlKey  = true)
    def meta  = copy(metaKey  = true)
    def shift = copy(shiftKey = true)

    def toJs: js.Object =
      assign(Dynamic.literal(), readOnlyProperties = true)
  }

  trait MouseLike {
    val screenX         : Double
    val screenY         : Double
    val clientX         : Double
    val clientY         : Double
    val pageX           : Double
    val pageY           : Double
    val movementX       : Long
    val movementY       : Long
    val altKey          : Boolean
    val ctrlKey         : Boolean
    val metaKey         : Boolean
    val shiftKey        : Boolean
    val button          : Int
    val buttons         : Int
    val defaultPrevented: Boolean

    def toJs: js.Object

    @deprecated("Use assign", "1.7.6")
    def setMouseAttributes(obj: js.Object): Unit =
      assign(obj.asInstanceOf[Dynamic], readOnlyProperties = true)

    def assign(tgt: Dynamic, readOnlyProperties: Boolean = false): tgt.type = {
      tgt.screenX          = screenX
      tgt.screenY          = screenY
      tgt.clientX          = clientX
      tgt.clientY          = clientY
      tgt.pageX            = pageX
      tgt.pageY            = pageY
      tgt.movementX        = movementX.toDouble
      tgt.movementY        = movementY.toDouble
      tgt.altKey           = altKey
      tgt.ctrlKey          = ctrlKey
      tgt.metaKey          = metaKey
      tgt.shiftKey         = shiftKey
      tgt.button           = button
      tgt.buttons          = buttons
      tgt.defaultPrevented = defaultPrevented
      if (readOnlyProperties) {
        tgt.defaultPrevented = defaultPrevented
      }
      tgt
    }

    def simulateAuxClick   (t: ReactOrDomNode) = Simulate.auxClick   (t, toJs)
    def simulateClick      (t: ReactOrDomNode) = Simulate.click      (t, toJs)
    def simulateContextMenu(t: ReactOrDomNode) = Simulate.contextMenu(t, toJs)
    def simulateDoubleClick(t: ReactOrDomNode) = Simulate.doubleClick(t, toJs)
    def simulateDrag       (t: ReactOrDomNode) = Simulate.drag       (t, toJs)
    def simulateDragEnd    (t: ReactOrDomNode) = Simulate.dragEnd    (t, toJs)
    def simulateDragEnter  (t: ReactOrDomNode) = Simulate.dragEnter  (t, toJs)
    def simulateDragExit   (t: ReactOrDomNode) = Simulate.dragExit   (t, toJs)
    def simulateDragLeave  (t: ReactOrDomNode) = Simulate.dragLeave  (t, toJs)
    def simulateDragOver   (t: ReactOrDomNode) = Simulate.dragOver   (t, toJs)
    def simulateDragStart  (t: ReactOrDomNode) = Simulate.dragStart  (t, toJs)
    def simulateDrop       (t: ReactOrDomNode) = Simulate.drop       (t, toJs)
    def simulateMouseDown  (t: ReactOrDomNode) = Simulate.mouseDown  (t, toJs)
    def simulateMouseEnter (t: ReactOrDomNode) = Simulate.mouseEnter (t, toJs)
    def simulateMouseLeave (t: ReactOrDomNode) = Simulate.mouseLeave (t, toJs)
    def simulateMouseMove  (t: ReactOrDomNode) = Simulate.mouseMove  (t, toJs)
    def simulateMouseOut   (t: ReactOrDomNode) = Simulate.mouseOut   (t, toJs)
    def simulateMouseOver  (t: ReactOrDomNode) = Simulate.mouseOver  (t, toJs)
    def simulateMouseUp    (t: ReactOrDomNode) = Simulate.mouseUp    (t, toJs)
    def simulateWheel      (t: ReactOrDomNode) = Simulate.wheel      (t, toJs)

    def simulationAuxClick    = Simulation.auxClick   (toJs)
    def simulationClick       = Simulation.click      (toJs)
    def simulationContextMenu = Simulation.contextMenu(toJs)
    def simulationDoubleClick = Simulation.doubleClick(toJs)
    def simulationDrag        = Simulation.drag       (toJs)
    def simulationDragEnd     = Simulation.dragEnd    (toJs)
    def simulationDragEnter   = Simulation.dragEnter  (toJs)
    def simulationDragExit    = Simulation.dragExit   (toJs)
    def simulationDragLeave   = Simulation.dragLeave  (toJs)
    def simulationDragOver    = Simulation.dragOver   (toJs)
    def simulationDragStart   = Simulation.dragStart  (toJs)
    def simulationDrop        = Simulation.drop       (toJs)
    def simulationMouseDown   = Simulation.mouseDown  (toJs)
    def simulationMouseEnter  = Simulation.mouseEnter (toJs)
    def simulationMouseLeave  = Simulation.mouseLeave (toJs)
    def simulationMouseMove   = Simulation.mouseMove  (toJs)
    def simulationMouseOut    = Simulation.mouseOut   (toJs)
    def simulationMouseOver   = Simulation.mouseOver  (toJs)
    def simulationMouseUp     = Simulation.mouseUp    (toJs)
    def simulationWheel       = Simulation.wheel      (toJs)
  }

  object Mouse {
    implicit def autoToJsObject(d: Mouse): js.Object = d.toJs
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  case class Pointer(screenX           : Double  = 0,
                     screenY           : Double  = 0,
                     clientX           : Double  = 0,
                     clientY           : Double  = 0,
                     pageX             : Double  = 0,
                     pageY             : Double  = 0,
                     movementX         : Long    = 0,
                     movementY         : Long    = 0,
                     altKey            : Boolean = false,
                     ctrlKey           : Boolean = false,
                     metaKey           : Boolean = false,
                     shiftKey          : Boolean = false,
                     button            : Int     = 0,
                     buttons           : Int     = 0,
                     defaultPrevented  : Boolean = false,
                     pointerId         : Double  = 0,
                     width             : Double  = 0,
                     height            : Double  = 0,
                     pressure          : Double  = 0,
                     tiltX             : Double  = 0,
                     tiltY             : Double  = 0,
                     pointerType       : String  = "",
                     isPrimary         : Boolean = false,
                     tangentialPressure: Double  = 0,
                     twist             : Int     = 0) extends MouseLike {

    def alt   = copy(altKey   = true)
    def ctrl  = copy(ctrlKey  = true)
    def meta  = copy(metaKey  = true)
    def shift = copy(shiftKey = true)

    override def assign(tgt: Dynamic, readOnlyProperties: Boolean = false): tgt.type = {
      super.assign(tgt, readOnlyProperties = readOnlyProperties)
      tgt.pointerId          = pointerId
      tgt.width              = width
      tgt.height             = height
      tgt.pressure           = pressure
      tgt.tiltX              = tiltX
      tgt.tiltY              = tiltY
      tgt.pointerType        = pointerType
      tgt.isPrimary          = isPrimary
      tgt.tangentialPressure = tangentialPressure
      tgt.twist              = twist
      tgt
    }

    def toJs: js.Object =
      assign(Dynamic.literal(), readOnlyProperties = true)

    def toMouseEvent: Mouse =
      Mouse(
        screenX          = screenX,
        screenY          = screenY,
        clientX          = clientX,
        clientY          = clientY,
        pageX            = pageX,
        pageY            = pageY,
        movementX        = movementX,
        movementY        = movementY,
        altKey           = altKey,
        ctrlKey          = ctrlKey,
        metaKey          = metaKey,
        shiftKey         = shiftKey,
        button           = button,
        buttons          = buttons,
        defaultPrevented = defaultPrevented)

    def simulateGotPointerCapture (t: ReactOrDomNode) = Simulate.gotPointerCapture (t, toJs)
    def simulateLostPointerCapture(t: ReactOrDomNode) = Simulate.lostPointerCapture(t, toJs)
    def simulatePointerCancel     (t: ReactOrDomNode) = Simulate.pointerCancel     (t, toJs)
    def simulatePointerDown       (t: ReactOrDomNode) = Simulate.pointerDown       (t, toJs)
    def simulatePointerEnter      (t: ReactOrDomNode) = Simulate.pointerEnter      (t, toJs)
    def simulatePointerLeave      (t: ReactOrDomNode) = Simulate.pointerLeave      (t, toJs)
    def simulatePointerMove       (t: ReactOrDomNode) = Simulate.pointerMove       (t, toJs)
    def simulatePointerOut        (t: ReactOrDomNode) = Simulate.pointerOut        (t, toJs)
    def simulatePointerOver       (t: ReactOrDomNode) = Simulate.pointerOver       (t, toJs)
    def simulatePointerUp         (t: ReactOrDomNode) = Simulate.pointerUp         (t, toJs)

    def simulationGotPointerCapture  = Simulation.gotPointerCapture (toJs)
    def simulationLostPointerCapture = Simulation.lostPointerCapture(toJs)
    def simulationPointerCancel      = Simulation.pointerCancel     (toJs)
    def simulationPointerDown        = Simulation.pointerDown       (toJs)
    def simulationPointerEnter       = Simulation.pointerEnter      (toJs)
    def simulationPointerLeave       = Simulation.pointerLeave      (toJs)
    def simulationPointerMove        = Simulation.pointerMove       (toJs)
    def simulationPointerOut         = Simulation.pointerOut        (toJs)
    def simulationPointerOver        = Simulation.pointerOver       (toJs)
    def simulationPointerUp          = Simulation.pointerUp         (toJs)
  }

  object Pointer {
    implicit def autoToJsObject(d: Pointer): js.Object = d.toJs
    implicit def autoToMouse(d: Pointer): Mouse = d.toMouseEvent
  }

}
