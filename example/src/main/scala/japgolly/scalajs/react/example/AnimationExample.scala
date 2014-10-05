package japgolly.scalajs.react.example

import scala.scalajs.js
import org.scalajs.dom.{document, window}

import japgolly.scalajs.react._
import Addons.ReactCssTransitionGroup
import vdom.ReactVDom._
import all._

object AnimationExample extends js.JSApp {

  import ReactCssTransitionGroup.key

  def main(): Unit = {

    class Backend(T: BackendScope[_, Vector[String]]) {
      def handleAdd(): Unit =
        T.modState(_ :+ window.prompt("Enter some text"))
      def handleRemove(i: Int): Unit =
        T.modState(_.zipWithIndex.filterNot(_._2 == i).map(_._1))
    }

    val TodoList = ReactComponentB[Unit]("TodoList")
      .initialState(Vector("hello", "world", "click", "me"))
      .backend(new Backend(_))
      .render((_,S,B) =>
        div(
          button(onclick --> B.handleAdd())("Add Item"),
          ReactCssTransitionGroup("example", component = "h1")(
            S.zipWithIndex.map{case (s,i) =>
              div(key := s, onclick --> B.handleRemove(i))(s)
            }: _*
          )
        )
      ).buildU

    TodoList() render document.getElementById("scala")
  }
}
