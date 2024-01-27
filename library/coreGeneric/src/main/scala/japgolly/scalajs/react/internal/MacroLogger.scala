package japgolly.scalajs.react.internal

import sourcecode.Line

// TODO: Move into microlibs
object MacroLogger {

  def apply(): MacroLogger =
    new MacroLogger

  def apply(enabled: Boolean): MacroLogger = {
    val l = new MacroLogger
    l.enabled = enabled
    l
  }
}

class MacroLogger {
  import Console._

  private var enabledStack = List.empty[Boolean]

  var enabled = false

  def pushDisabled(): Unit =
    pushEnabled(false)

  def pushEnabled(e: Boolean = true): Unit = {
    enabledStack ::= enabled
    enabled = e
  }

  def pop(): Unit =
    if (enabledStack.nonEmpty) {
      enabled = enabledStack.head
      enabledStack = enabledStack.tail
    }

  private var _hold = false
  private var _pending = Vector.empty[() => String]

  def hold(): Unit = {
    release()
    _hold = true
  }

  def release(printPending: Boolean = true): Unit = {
    val pending = _pending
    _pending = Vector.empty
    _hold = false
    if (printPending && enabled)
      for (p <- pending)
        _println(p())
  }

  def apply(): Unit =
    if (enabled)
      System.out.println()

  def apply(a: => Any)(implicit l: Line): Unit =
    if (enabled) {
      val text = "" + a
      // for (line <- text.linesIterator) {
      //   System.out.printf("%s[%3d]%s %s\n", CYAN, l.value, RESET, line)
      // }
      val msg = "%s[%3d]%s %s".format(CYAN, l.value, RESET, text.replace("\n", "\n      "))
      _println(msg)
    }

  private def _println(a: => Any): Unit =
    if (enabled) {
      val msg = () => a match {
        case s: String => s
        case _         => "" + a
      }
      if (_hold)
        _pending :+= msg
      else
        System.out.println(msg())
    }

  private def width = 200
  private def sep = "=" * width

  def header(): Unit =
    _println(sep + "\n")

  def footer(): Unit =
    _println("\n" + sep)

  def footer(result: => Any): Unit = {
    apply("Result", result)
    footer()
  }

  def apply(name: => Any, value: => Any)(implicit l: Line): Unit =
    apply(s"$YELLOW$name:$RESET $value")

  def all(name: => Any, values: => Iterable[Any])(implicit l: Line): Unit =
    if (enabled) {
      val vs = values
      if (vs.isEmpty)
        apply(s"$name [0/0]")
      else {
        val total = vs.size
        var i = 0
        for (v <- vs) {
          i += 1
          apply(s"$name [$i/$total]", v)
        }
      }
    }
}
