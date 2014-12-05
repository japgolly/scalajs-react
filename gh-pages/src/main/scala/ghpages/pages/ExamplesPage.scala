package ghpages.pages

import japgolly.scalajs.react._, vdom.ReactVDom._, all._
import ghpages.examples._

object ExamplesPage {

  case class State(index: Int)

  class Backend(t: BackendScope[_, State]) {
    def onMenuClick(newIndex: Int) = t.modState(_.copy(index = newIndex))
  }

  val examplesMenu = ReactComponentB[(List[String], Backend,State)]("examplesMenu")
    .render(P => {
      val (data, b , s) = P
      def element(name: String, index: Int) =
        li(
          if (index == s.index) cls := "list-group-item active" else cls := "list-group-item",
          onclick --> b.onMenuClick(index),
          name)
      div(`class` := "col-md-2",
        ul(`class` := "list-group",
          data.zipWithIndex.map { case (name, index) => element(name, index) }))
  }).build


  val exampleBody = ReactComponentB[String]("examplesBody")
    .render(name => {
    div(`class` := "col-md-10")(
      name match {

        case "HelloReact" => HelloMessageExample.content()

        case "Timer" => TimerExample.content()

        case "Todo" => TodoExample.content()

        case "UsingRefs" => RefsExample.content()

        case "ProductTable" => ProductTableExample.content()

        case "Animation" => AnimationExample.content()

        case "AjaxPictureApp" => PictureAppExample.content()

        case _ => "// TODO"
      }
    )
  }).build


  val component = ReactComponentB[List[String]]("examplesPage")
    .initialState(State(0))
    .backend(new Backend(_))
    .render((P, S, B) =>
      div(`class` := "row",
        examplesMenu((P, B, S)),
        exampleBody(P(S.index)))
    ).build
}
