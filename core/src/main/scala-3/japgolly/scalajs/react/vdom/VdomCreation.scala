package japgolly.scalajs.react.vdom

private[vdom] object VdomCreation {

  extension (s: String) {

    // May eventually make use of this
    inline def reactTerminalTag[N <: HtmlTopNode]: HtmlTagOf[N] =
      HtmlTagOf[N](s)
  }

}