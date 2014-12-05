package japgolly.scalajs.react.example

import japgolly.scalajs.react.example.examples.PictureAppExample
import japgolly.scalajs.react.example.pages.{ExamplesPage, HomePage}
import japgolly.scalajs.react.vdom.ReactVDom._
import japgolly.scalajs.react.vdom.ReactVDom.all._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, _}
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport


/**
 * Created by chandrasekharkode on 11/16/14.
 */
object ReactApp extends JSApp {

  case class State(index: Int)

  case class Backend(t: BackendScope[_, State]) {
    def onMenuClick(newIndex: Int) = t.modState(_.copy(index = newIndex))
  }

  val navMenu = ReactComponentB[(List[String], Backend)]("appMenu")
    .render(P => {
    val (data, b) = P
    def element(name: String, index: Int) = li(`class` := "navbar-brand", onclick --> b.onMenuClick(index))(name)
    div(`class` := "navbar navbar-default")(
      ul(`class` := "navbar-header")(data.zipWithIndex.map { case (name, index) => element(name, index)})
    )
  }).build

  val content = ReactComponentB[String]("homePage")
    .render(_ =>
    div(h1("scalajs-react"),
      p("Lifts Facebook's React library into Scala.js and endeavours to make it as type-safe and Scala-friendly as possible.\n\nIn addition to wrapping React, this provides extra opt-in functionality to support (separately) easier testing, and pure FP.")
    )
    ).build

  val container = ReactComponentB[String]("appMenu")
    .render(P => {
    div(`class` := "container")(
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
    .render((P, S, B) => {
    div(
      navMenu((P, B)),
      container(P(S.index)),
      footer(
       hr,
       span(`class` := "text-center")("Powered By Scala JS React")
      )
    )
  }).build

  def component(data: List[String]) = {
    app(data) render dom.document.body
  }

  @JSExport
  override def main(): Unit = component(List("Home","Examples","Documentation"))

}
