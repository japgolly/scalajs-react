package ghpages.pages

import japgolly.scalajs.react._, vdom.html_<^._

/**
 * Created by chandrasekharkode on 11/16/14.
 */
object HomePage {

  private val p =
    <.p(^.margin := "1.3em 0")

  val component = ScalaComponent.static("Home")(
    <.div(
      <.h1(
        <.a(
          ^.color := "#000",
          ^.href  := "https://github.com/japgolly/scalajs-react",
          "scalajs-react")),

      <.section(
        ^.marginTop := "2.2em",
        ^.fontSize  := "115%",
        ^.color     := "#333",

        p(
          "Lifts Facebook's ",
          <.a(^.href := "https://facebook.github.io/react", "React"),
          " library into ",
          <.a(^.href := "http://www.scala-js.org", "Scala.js"),
          " and endeavours to make it as type-safe and Scala-friendly as possible."),

        p(
          "Provides (opt-in) support for pure functional programming, using ",
          <.a(^.href := "https://github.com/typelevel/cats", "Cats"),
          ", ",
          <.a(^.href := "https://github.com/scalaz/scalaz", "Scalaz"),
          " and ",
          <.a(^.href := "https://github.com/julien-truffaut/Monocle", "Monocle"),
          "."),

        p(
          "Comes utility modules helpful for React in Scala(.js), rather than React in JS.",
          "Includes a router, testing utils, performance utils, more."),

      <.p(
        ^.fontSize  := "85%",
        ^.marginTop := "3.3em",
        ^.fontStyle := "italic",
        ^.color     := "#444",
        "Big thanks to ",
        <.a(^.href := "https://twitter.com/chandu0101", "@chandu0101"),
        " for creating these pages."))))
}