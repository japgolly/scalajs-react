package japgolly.scalajs.react.vdom

/**
  * Represents a single XML namespace. This is currently ignored in `scalatags.Text`,
  * but used to create elements with the correct namespace in `scalatags.JsDom`. A
  * [[Namespace]] can be provided implicitly (or explicitly) when creating tags via
  * `"".tag`, with a default of "http://www.w3.org/1999/xhtml" if none is found.
  */
final case class Namespace(uri: String) extends AnyVal

object Namespace {

  val Html: Namespace =
    Namespace("http://www.w3.org/1999/xhtml")

  val Svg: Namespace =
    Namespace("http://www.w3.org/2000/svg")
}
