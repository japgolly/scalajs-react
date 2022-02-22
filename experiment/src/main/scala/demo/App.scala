package demo

import japgolly.scalajs.react._
import scala.scalajs.js.annotation._
import scala.scalajs.js

object App {

  @JSImport("@/src/App.js", JSImport.Default)
  @js.native
  val raw: js.Any = js.native

  val comp = JsFnComponent[Null, Children.None](raw)
}
