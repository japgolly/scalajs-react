package japgolly.scalajs.react

object InliningTest extends vdom.CssUnitsOps {
  import CompileTimeTestUtil._

  _assertEqualExprs(0.px, "0")
  _assertEqualExprs(0.0.px, "0")
  _assertEqualExprs(1.px, "1px")

  def x(n: Int) = n.px
  assert(x(0) == "0")
  assert(x(1) == "1px")
}
