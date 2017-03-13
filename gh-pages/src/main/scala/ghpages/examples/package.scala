package ghpages

import japgolly.scalajs.react._, vdom.html_<^._

package object examples {

  def addIntro(top: Key => TagMod, f: VdomTag => VdomTag) =
    ScalaComponent.builder[Unit]("").render(_ =>
      <.div(
        f(<.p(^.maxWidth := "84%", ^.marginBottom := "1.5em")),
        top("top"))
    ).build

  def reactJsLink(suffix: String = "") = {
    val url = "https://facebook.github.io/react/" + suffix
    <.a(^.href := url, url)
  }

  def scalaPortOf(name: String) =
    <.span("Scala version of \"", <.em(name), "\" on ", reactJsLink(), ".")

  def scalaPortOfPage(urlSuffix: String) =
    <.span("Scala version of ", reactJsLink(urlSuffix), ".")

}
