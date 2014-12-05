package ghpages

import japgolly.scalajs.react._, vdom.ReactVDom._, all._
import org.scalajs.dom
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import ghpages.pages._

object ReactApp extends JSApp {

  case class State(index: Int)

  case class Backend(t: BackendScope[_, State]) {
    def onMenuClick(newIndex: Int) = t.modState(_.copy(index = newIndex))
  }

  val navMenu = ReactComponentB[(List[String], Backend)]("appMenu")
    .render(P => {
      val (data, b) = P
      def element(name: String, index: Int) =
        li(
          `class` := "navbar-brand",
          onclick --> b.onMenuClick(index),
          name)
      div(`class` := "navbar navbar-default",
        ul(`class` := "navbar-header",
          data.zipWithIndex.map { case (name, index) => element(name, index)}))
    }).build

  val content = ReactComponentB[String]("homePage")
    .render(_ =>
      div(
        h1("scalajs-react"),
        p("Lifts Facebook's React library into Scala.js and endeavours to make it as type-safe and Scala-friendly as possible.\n\nIn addition to wrapping React, this provides extra opt-in functionality to support (separately) easier testing, and pure FP."))
    ).build

  val container = ReactComponentB[String]("appMenu")
    .render(P => {
      div(`class` := "container",
        P match {
          case "Home" => HomePage.content
          case "Examples" => ExamplesPage.component(List("HelloReact", "Timer", "Todo","UsingRefs" , "ProductTable" , "Animation" ,"AjaxPictureApp"))
          case "Documentation" => h3("// TODO ")
        }
      )
    }).build

  val app = ReactComponentB[List[String]]("app")
    .initialState(State(0))
    .backend(new Backend(_))
    .render((P, S, B) =>
      div(
        navMenu((P, B)),
        container(P(S.index)),
        footer(
         hr,
         span(`class` := "text-center")("Powered by scalajs-react")))
    ).build

  def component(data: List[String]) =
    app(data) render dom.document.body

  @JSExport
  override def main(): Unit =
    component(List("Home", "Examples", "Documentation"))
}
