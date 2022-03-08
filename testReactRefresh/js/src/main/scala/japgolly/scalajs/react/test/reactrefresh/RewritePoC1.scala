package japgolly.scalajs.react.test.reactrefresh

import japgolly.scalajs.react.RewritePoC

object RewritePoC1 {

  // val Component: scalajs.js.Function1[String, Unit] =
  //   org.scalajs.dom.console.log(_)

  val Component = RewritePoC.start
    .useState("omfg")
    .render((p, s) => {
      // org.scalajs.dom.console.log("RewritePoC1 render")
      val i: String = s.value
      s"Hello p=$p, s=$i !!"
    })
}
