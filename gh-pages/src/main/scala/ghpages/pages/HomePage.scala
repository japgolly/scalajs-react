package ghpages.pages

import japgolly.scalajs.react.vdom.ReactVDom.all._

/**
  * Created by chandrasekharkode on 11/16/14.
  */
object HomePage {

   val content = div(
     h1("scalajs-react"),
     p("Lifts Facebook's ",
       a(href := "http://facebook.github.io/react/")("react"),
       " library into ",
       a(href := "http://www.scala-js.org/")("Scala.js "),
       "and endeavours to make it as type-safe and Scala-friendly as possible."
     ),
     p("In addition to wrapping React, this provides extra opt-in functionality to support (separately) easier testing, and pure FP.")
   )
}