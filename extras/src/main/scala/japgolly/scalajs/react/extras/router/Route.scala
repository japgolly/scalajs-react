package japgolly.scalajs.react.extras.router

sealed trait Route[P] {
  val path: String
}

case class Root[P](renderer: Renderer[P]) extends Route[P] {
  override final val path = ""
}

case class Path[P](path: String, renderer: Renderer[P]) extends Route[P]

// ---------------------------------------------------------------------------------

trait Page[P] {

  private[this] var _paths = List.empty[Path[P]]
  final def paths = _paths

  val root: Root[P]

  protected def path(path: String, r: Renderer[P]) = {
    val p = Path[P](path, r)
    _paths ::= p
    p
  }
}
