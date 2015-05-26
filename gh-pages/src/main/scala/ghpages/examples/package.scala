package ghpages

import japgolly.scalajs.react._, vdom.prefix_<^._

package object examples {

  def addIntro(top: ReactComponentC.ConstProps[_, _, _, TopNode], f: ReactTag => ReactTag) =
    ReactComponentB[Unit]("").render(_ =>
      <.div(
        f(<.p(^.maxWidth := "84%", ^.marginBottom := "1.5em")),
        top.withKey("top")())
    ).buildU

  def reactJsLink(suffix: String = "") = {
    val url = "https://facebook.github.io/react/" + suffix
    <.a(^.href := url, url)
  }

  def scalaPortOf(name: String) =
    <.span("Scala version of \"", <.em(name), "\" on ", reactJsLink(), ".")

  def scalaPortOfPage(urlSuffix: String) =
    <.span("Scala version of ", reactJsLink(urlSuffix), ".")

}
