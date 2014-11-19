package japgolly.scalajs.react.example.pages

import japgolly.scalajs.react.example.examples.AnimationExample.Backend
import japgolly.scalajs.react.example.examples.AnimationExample._
import japgolly.scalajs.react.example.examples.HelloMessageExample._
import japgolly.scalajs.react.example.examples.PictureAppExample._
import japgolly.scalajs.react.example.examples.ProductTableExample.Backend
import japgolly.scalajs.react.example.examples.ProductTableExample.State
import japgolly.scalajs.react.example.examples.ProductTableExample._
import japgolly.scalajs.react.example.examples.RefsExample.{refsJsxCode, refsScalaCode}
import japgolly.scalajs.react.example.examples.TimerExample.Backend
import japgolly.scalajs.react.example.examples.TimerExample.State
import japgolly.scalajs.react.example.examples.TimerExample._
import japgolly.scalajs.react.example.examples.TodoExample.Backend
import japgolly.scalajs.react.example.examples.TodoExample.State
import japgolly.scalajs.react.example.examples.TodoExample._
import japgolly.scalajs.react.example.examples._
import japgolly.scalajs.react.vdom.ReactVDom.ReactAttrExt
import japgolly.scalajs.react.vdom.ReactVDom.all._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}

/**
 * Created by chandrasekharkode on 11/16/14.
 */
object ExamplesPage {


  case class State(index: Int)

  class Backend(t: BackendScope[_, State]) {
    def onMenuClick(newIndex: Int) = t.modState(_.copy(index = newIndex))
  }

  val examplesMenu = ReactComponentB[(List[String], Backend,State)]("examplesMenu")
    .render(P => {
    val (data, b , s) = P
    def element(name: String, index: Int) = li( if(index==s.index) cls := "list-group-item active" else cls := "list-group-item" , onclick --> b.onMenuClick(index))(name)
    div(`class` := "col-md-2")(
      ul(`class` := "list-group")(
        data.zipWithIndex.map { case (name, index) => element(name, index)}
      )
    )
  }).build


  val hello = ReactComponentB[String]("hellow")
    .render(name => h1(name)).build


  val exampleBody = ReactComponentB[String]("examplesBody")
    .render(name => {
    div(`class` := "col-md-10")(
      name match {

        case "HelloReact" => SideBySide.component(helloJsXCode, helloScalaCode, helloComponent("React"))

        case "Timer" => SideBySide.component(timerJsxCode, timerScalaCode, Timer())

        case "Todo" => SideBySide.component(todoJsxCode, todoScalaCode, TodoApp())

        case "UsingRefs" => SideBySide.component(refsJsxCode, refsScalaCode, RefsExample.App())

        case "ProductTable" => SideBySide.component(tableJsxCode, tableScalaCode, FilterableProductTable(products))

        case "Animation" => SingleSide.component(animationScalaCode, AnimatedTodoList())

        case "AjaxPictureApp" => SideBySide.component(pictureJsxCode, pictureScalaCode , PictureApp())

        case _ => "//TODO"

      }
    )
  }).build


  val component = ReactComponentB[List[String]]("examplesPage")
    .initialState(State(0))
    .backend(new Backend(_))
    .render((P, S, B) => {
    div(`class` := "row")(
      examplesMenu((P, B, S)),
      exampleBody(P(S.index))
    )
  }).build


}
