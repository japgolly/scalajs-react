package ghpages.pages

import japgolly.scalajs.react._, vdom.prefix_<^._

/**
 * Created by chandrasekharkode on 11/16/14.
 */
object HomePage {

  val component = ReactComponentB.static("Home",
    <.div(
      <.h1(
        <.a(
          ^.color := "#000",
          ^.href  := "https://github.com/japgolly/scalajs-react",
          "scalajs-react")),
      <.section(
        ^.marginTop := "1.6em",
        ^.fontSize  := "115%",
        ^.color     := "#444",
        <.p("Lifts Facebook's ",
          <.a(^.href := "http://facebook.github.io/react", "React"),
          " library into ",
          <.a(^.href := "http://www.scala-js.org", "Scala.js"),
          " and endeavours to make it as type-safe and Scala-friendly as possible."),
        <.p(
          "In addition to wrapping React, this provides extra opt-in functionality to support (separately) easier testing, and pure FP.")),
      <.p(
        ^.marginTop := "3em",
        ^.fontStyle := "italic",
        "Big thanks to ",
        <.a(^.href := "https://twitter.com/chandu0101", "@chandu0101"),
        " for creating these pages."))
  ).buildU
}