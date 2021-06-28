package japgolly.scalajs.react.core

import japgolly.scalajs.react._

sealed trait CompilationTest {
  import CompilationTest.*

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
  }

  // ===================================================================================================================
  sealed trait TestComponentBuilder {
    val step1 = ScalaComponent.builder[Int]("")

    step1.renderBackend[B1p]
    step1.renderBackendWithChildren[B1pc]
    step1.renderBackend[B2]
    step1.renderBackend[B3a]
    step1.renderBackend[B3b]

    step1.backend[B1p](_ => new B1p).renderBackend
    step1.backend[B1pc](_ => new B1pc).renderBackendWithChildren
    step1.backend[B2](new B2(_)).renderBackend
    step1.backend[B3a](new B3a(_)).renderBackend
    step1.backend[B3b](new B3b(_)).renderBackend
  }

  // ===================================================================================================================
  // Misc

  PropsChildren(())

  // Ensure that the ScalaJsReactConfig.Defaults trait contains a default value for every config method
  class ScalaJsReactConfigDefaults extends ScalaJsReactConfig.Defaults
}

object CompilationTest {
  import japgolly.scalajs.react.vdom.html_<^.*

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
  class B3b($: BackendScope[Int, Unit])(using i: Imp) {
    def render = VdomNode.cast(123)
  }
}