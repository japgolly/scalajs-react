package japgolly.scalajs.react.vdom

/**
  * Represents a single XML namespace. This is currently ignored in `scalatags.Text`,
  * but used to create elements with the correct namespace in `scalatags.JsDom`. A
  * [[Namespace]] can be provided implicitly (or explicitly) when creating tags via
  * `"".tag`, with a default of "http://www.w3.org/1999/xhtml" if none is found.
  */
opaque type Namespace = String

object Namespace {

  @inline def apply(uri: String): Namespace =
    uri

  extension (self: Namespace) {
    @inline def uri: String = self
  }

  val Html: Namespace = "http://www.w3.org/1999/xhtml"
  val Svg : Namespace = "http://www.w3.org/2000/svg"
}
