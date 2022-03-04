package japgolly.scalajs.react.test.reactrefresh

import japgolly.scalajs.react.RewritePoC
import japgolly.scalajs.react.hooks.Hooks._

object RewritePoC12 {

  private var v = 2222

  val Component = RewritePoC.start[String]
    .useState(1111)
    .useState[Int] { v += 1; v }
    .render(renderFn)

  @inline def renderFn(p: String, s1: UseState[Int], s2: UseState[Int]): String =
  // val renderFn: (String, UseState[Int], UseState[Int]) => String = (p, s1, s2) =>
    s"Hello p=$p, s1=${s1.value}, s2=${s2.value} !!"
}
