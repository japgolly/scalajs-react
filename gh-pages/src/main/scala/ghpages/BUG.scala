package ghpages

import japgolly.scalajs.react._

object BUG {

  def problem = Main // crashes

  val NameChanger =
    ScalaComponent.builder[Int]
      .stateless
      .noBackend
      .render_(???)
      .build

  def noProblem = Main // no crash

  val Main =
    ScalaComponent.builder[Unit]
      .initialState(123)
      .render(_ => NameChanger(123))
      .build
}

// =====================================================================================================================

object BugWithWorkaroundsCommentedOut {

  def problem = Main // crashes

  val NameChanger =
  // val NameChanger: ScalaComponent[Int, Unit, Unit, CtorType.Props] =
    ScalaComponent.builder[Int]
      .stateless
      .noBackend
      .render_(???)
      .build

  def noProblem = Main // no crash

  val Main =
  // val Main: ScalaComponent[Unit, Int, Unit, CtorType.Nullary] =
    ScalaComponent.builder[Unit]
      .initialState(123)
      // .noBackend
      // .initialState[Int](123)
      .render(_ => NameChanger(123))
      // .render(_ => NameChanger.ctor(123))
      .build
}
