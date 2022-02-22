package demo

// import japgolly.scalajs.react.facade
import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.annotation.nowarn

@nowarn("cat=unused")
object ReactRefresh {

  @js.native
  @JSGlobal("$RefreshSig$")
  def sig(): Sig = js.native

  @js.native
  trait Sig extends js.Object {
    def apply(): Any = js.native
    def apply(comp: Any, id: String): Any = js.native
  }

  @js.native
  @JSGlobal("$RefreshReg$")
  def reg(comp: Any, id: String): Any = js.native

}
