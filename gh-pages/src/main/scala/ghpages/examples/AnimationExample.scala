package japgolly.scalajs.react.example.examples

import org.scalajs.dom.{document, window}

import japgolly.scalajs.react._
import Addons.ReactCssTransitionGroup
import vdom.ReactVDom._
import all._

/**
 * Created by chandrasekharkode on 11/18/14.
 */
object AnimationExample {

  val animationScalaCode = """
                             |  class Backend(T: BackendScope[_, Vector[String]]) {
                             |    def handleAdd(): Unit =
                             |      T.modState(_ :+ window.prompt("Enter some text"))
                             |    def handleRemove(i: Int): Unit =
                             |      T.modState(_.zipWithIndex.filterNot(_._2 == i).map(_._1))
                             |  }
                             |
                             |  val TodoList = ReactComponentB[Unit]("TodoList")
                             |    .initialState(Vector("hello", "world", "click", "me"))
                             |    .backend(new Backend(_))
                             |    .render((_,S,B) =>
                             |    div(
                             |      button(onclick -->  B.handleAdd())("Add Item"),
                             |      ReactCssTransitionGroup("example", component = "h1")(
                             |        S.zipWithIndex.map{case (s,i) =>
                             |          div(key := s, onclick --> B.handleRemove(i))(s)
                             |        }: _*
                             |      )
                             |    )
                             |    ).buildU
                             |
                             |  TodoList() render document.getElementById("scala")
                             |""".stripMargin



  class Backend(T: BackendScope[_, Vector[String]]) {
    def handleAdd(): Unit =
      T.modState(_ :+ window.prompt("Enter some text"))
    def handleRemove(i: Int): Unit =
      T.modState(_.zipWithIndex.filterNot(_._2 == i).map(_._1))
  }

  val AnimatedTodoList = ReactComponentB[Unit]("TodoList")
    .initialState(Vector("hello", "world", "click", "me"))
    .backend(new Backend(_))
    .render((_,S,B) =>
    div(
      button(onclick -->  B.handleAdd())("Add Item"),
      ReactCssTransitionGroup("example", component = "h1")(
        S.zipWithIndex.map{case (s,i) =>
          div(key := s, onclick --> B.handleRemove(i))(s)
        }: _*
      )
    )
    ).buildU

}
