package japgolly.scalajs.react.core

import japgolly.scalajs.react._

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
    c.toKleisli[Int]
    i.when(bool)
    i.unless(bool)
    Callback.when(bool)(c)
  }

  // ===================================================================================================================
  sealed trait TestCallbackKleisli {
    def x: CallbackKleisli[Int, String]

    x.when(_ > 3)
  }

  // ===================================================================================================================
  // Misc

  PropsChildren(())

  // Ensure that the ScalaJsReactConfig.Defaults trait contains a default value for every config method
  class ScalaJsReactConfigDefaults extends ScalaJsReactConfig.Defaults

}