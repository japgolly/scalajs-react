package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import scala.annotation.nowarn

sealed trait CompilationTest {
  import CompilationTest._

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
  sealed trait TestComponentBuilder {
    val step1 = ScalaComponent.builder[Int]("")

    step1.renderBackend[B1p]
    step1.renderBackendWithChildren[B1pc]
    step1.renderBackend[B2]
    step1.renderBackend[B3a]

    step1.backend[B1p](_ => new B1p).renderBackend
    step1.backend[B1pc](_ => new B1pc).renderBackendWithChildren
    step1.backend[B2](new B2(_)).renderBackend
    step1.backend[B3a](new B3a(_)).renderBackend

    step1.renderBackend[B2]
      .configure(Reusability.shouldComponentUpdateAndLog("omg"))
      .configure(Reusability.shouldComponentUpdateAnd(_.log("omg")))
      .configure(ReusabilityOverlay.install)
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
