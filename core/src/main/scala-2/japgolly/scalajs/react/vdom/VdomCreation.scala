package japgolly.scalajs.react.vdom

private[vdom] object VdomCreation {

  @inline final implicit class VdomExtString(private val s: String) extends AnyVal {

    // May eventually make use of this
    @inline def reactTerminalTag[N <: HtmlTopNode]: HtmlTagOf[N] =
      new HtmlTagOf[N](s)
  }

}