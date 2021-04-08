package japgolly.scalajs.react

import japgolly.scalajs.react.vdom.html_<^.*

class B1 {
  type PropsMate = Int
  // val render = 123L
  // val render: VdomNode = VdomNode.cast(123)
  // def render: VdomNode = VdomNode.cast(123)
  def render(x: PropsMate): VdomNode = 123
  // def render(x: PropsMate) = 123

  // def render(x: PropsMate, pc: PropsChildren): VdomNode = VdomNode.cast(123)
  // def render(p: Int, x: Int): VdomNode = VdomNode.cast(123)
  // def render[A](a: A): VdomNode = VdomNode.cast(123)
}

class B1a()
class B1x(i: Int)

class B2($: BackendScope[Int, Unit]) {
  def render = VdomNode.cast(123)
}

class Imp
// implicit val imp: Imp = new Imp

class B3a($: BackendScope[Int, Unit])(implicit i: Imp) {
  def render = VdomNode.cast(123)
}
class B3b($: BackendScope[Int, Unit])(using i: Imp) {
  def render = VdomNode.cast(123)
}

trait BT
class BP0[A]
class BP1[A](a: A)
