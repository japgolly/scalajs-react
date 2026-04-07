package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import scala.annotation.nowarn

sealed trait CompilationTest {

  def bool: Boolean

  // ===================================================================================================================
  sealed trait TestAsyncCallback {
    def x: AsyncCallback[Int]

    x.handleError(_ => AsyncCallback.pure(1))
  }

  // ===================================================================================================================
  sealed trait TestCallback {
    def c: Callback
    def i: CallbackTo[Int]

    i.handleError(_ => CallbackTo(1))
    i.when(bool)
    i.unless(bool)
    Callback.when(bool)(c)
    Callback(())
    Callback(123)
    // Callback(Callback.empty)
    CallbackTo(false) && CallbackTo(true)
    !CallbackTo(false)
    i.to[CallbackTo]
  }

  // ===================================================================================================================
  // Misc

  PropsChildren(())
}

@nowarn
object CompilationTest {
  import japgolly.scalajs.react.vdom.html_<^._

  class B1p {
    type PropsMate = Int
    def render(x: PropsMate): VdomNode = 123
  }

  class B1pc {
    type PropsMate = Int
    def render(x: PropsMate, pc: PropsChildren): VdomNode = 123
  }

  // def render(p: Int, x: Int): VdomNode = VdomNode.cast(123)
  // def render[A](a: A): VdomNode = VdomNode.cast(123)

  class B2($: BackendScope[Int, Unit]) {
    def render = VdomNode.cast(123)
  }

  class Imp
  implicit val imp: Imp = new Imp

  class B3a($: BackendScope[Int, Unit])(implicit i: Imp) {
    def render = VdomNode.cast(123)
  }
}
