package japgolly.scalajs.react.test

import scala.scalajs.js

sealed abstract class ReactEventType {
  def defaultEventData: js.Object
}

object ReactEventType {

  private def extend(base: ReactEventType, o: js.Object): js.Object =
    js.Object.assign(new js.Object, base.defaultEventData, o)

  case object Animation extends ReactEventType {
    override def defaultEventData: js.Object =
      extend(Basic, js.Dynamic.literal(
        animationName = "test",
        pseudoElement = "",
        elapsedTime   = 0,
      ))
  }

  case object Basic extends ReactEventType {
    override def defaultEventData: js.Object =
      // val currentTarget   : DOMEventTarget = js.native
      // val eventPhase      : Double         = js.native
      // val nativeEvent     : dom.Event      = js.native
      // val target          : DOMEventTarget = js.native
      // val `type`          : String         = js.native
      js.Dynamic.literal(
        bubbles          = false,
        cancelable       = false,
        defaultPrevented = false,
        isTrusted        = false,
        timeStamp        = System.currentTimeMillis().toDouble,
      )
  }

  case object Clipboard extends ReactEventType {
    override def defaultEventData: js.Object =
      // def clipboardData: dom.DataTransfer = js.native
      Basic.defaultEventData
  }

  case object Composition extends ReactEventType {
    override def defaultEventData: js.Object =
      // override val nativeEvent: dom.CompositionEvent = js.native
      extend(Basic, js.Dynamic.literal(
        data = "",
      ))
  }

  case object Drag extends ReactEventType {
    override def defaultEventData: js.Object =
      // override val nativeEvent: dom.DragEvent = js.native
      // val dataTransfer: dom.DataTransfer = js.native
      Mouse.defaultEventData
  }

  case object Focus extends ReactEventType {
    override def defaultEventData: js.Object =
      // override val nativeEvent: dom.FocusEvent = js.native
      // val relatedTarget: dom.EventTarget = js.native
      UI.defaultEventData
  }

  case object Form extends ReactEventType {
    override def defaultEventData: js.Object =
      UI.defaultEventData
  }

  case object Keyboard extends ReactEventType {
    override def defaultEventData: js.Object =
      // override val nativeEvent: dom.KeyboardEvent = js.native
      // def getModifierState(keyArg: String): Boolean = js.native
      extend(UI, js.Dynamic.literal(
        location = 0,
        altKey   = false,
        ctrlKey  = false,
        metaKey  = false,
        shiftKey = false,
        repeat   = false,
        locale   = "",
        key      = "Unidentified",
        charCode = 0,
        keyCode  = 0,
        which    = 0,
      ))
  }

  case object Mouse extends ReactEventType {
    override def defaultEventData: js.Object =
      // override val nativeEvent: dom.MouseEvent = js.native
      // val relatedTarget: dom.EventTarget = js.native
      // def getModifierState(keyArg: String): Boolean = js.native
      extend(UI, js.Dynamic.literal(
        screenX   = 0,
        screenY   = 0,
        clientX   = 0,
        clientY   = 0,
        pageX     = 0,
        pageY     = 0,
        altKey    = false,
        ctrlKey   = false,
        metaKey   = false,
        shiftKey  = false,
        button    = 0, // Main button pressed, usually the left button or the un-initialized state
        buttons   = 1, // Primary button (usually the left button)
        movementX = 0,
        movementY = 0,
      ))
  }

  case object Pointer extends ReactEventType {
    override def defaultEventData: js.Object =
      // override val nativeEvent: dom.PointerEvent = js.native
      extend(Mouse, js.Dynamic.literal(
        pointerId          = System.nanoTime().toDouble,
        width              = 0,
        height             = 0,
        pressure           = 0.5,
        tiltX              = 0,
        tiltY              = 0,
        pointerType        = "",
        isPrimary          = true,
        tangentialPressure = 0,
        twist              = 0,
      ))
  }

  case object Touch extends ReactEventType {
    override def defaultEventData: js.Object =
      // override val nativeEvent: dom.TouchEvent = js.native
      // val touches       : dom.TouchList = js.native
      // val targetTouches : dom.TouchList = js.native
      // val changedTouches: dom.TouchList = js.native
      // def getModifierState(keyArg: String): Boolean = js.native
      extend(UI, js.Dynamic.literal(
        altKey    = false,
        ctrlKey   = false,
        metaKey   = false,
        shiftKey  = false,
      ))
  }

  case object Transition extends ReactEventType {
    override def defaultEventData: js.Object =
      extend(Basic, js.Dynamic.literal(
        propertyName  = "test",
        pseudoElement = "",
        elapsedTime   = 0,
      ))
  }

  case object UI extends ReactEventType {
    override def defaultEventData: js.Object =
      // override val nativeEvent: dom.UIEvent = js.native
      // val view: js.Object = js.native
      extend(Basic, js.Dynamic.literal(
        detail = 0,
      ))
  }

  case object Wheel extends ReactEventType {
    override def defaultEventData: js.Object =
      // override val nativeEvent: dom.WheelEvent = js.native
      extend(Mouse, js.Dynamic.literal(
        deltaX    = 0,
        deltaY    = 0,
        deltaZ    = 0,
        deltaMode = 0, // The delta values are specified in pixels
      ))
  }
}