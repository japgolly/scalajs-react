package demo

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scala.scalajs.js.annotation._
import scala.scalajs.js

object GlobalVars {

  @JSGlobal
  @js.native
  val globalThis: js.Dynamic = js.native

  // type Comp =  JsFn.ComponentWithRoot[Unit,Nullary,JsFn.UnmountedWithRoot[Unit,Unit,Box[Unit]],Box[Unit],Nullary,JsFn.UnmountedWithRoot[Box[Unit],Unit,Box[Unit]]]
  type Comp = ScalaFnComponent[Unit, CtorType.Nullary]

  // var counter: Comp = null

  def get(): Comp = {
    globalThis.qwe.asInstanceOf[Comp]
  }

  def set(c: Comp): Unit = {
    globalThis.qwe = c.asInstanceOf[js.Any]
  }

}

object CounterV {
  // type Props = Unit
  // type State = Int

  // final class Backend($: BackendScope[Props, State]) {
  //   def render(s: State): VdomNode =
  // }

  def newComponent = {
    val sig = ReactRefresh.sig()
    org.scalajs.dom.console.log("V-Sig: ", sig)
    val Component = ScalaFnComponent.withHooks[Unit]
      .localVal(sig(): Unit)
      .useState(0)
      .render { (_, _, s) =>

        <.button(
          "V-Count xxx: ", s.value,
          ^.onClick --> s.modState(_ + 1),
        )
      }
    sig(Component.raw, "oDgYfYHkD9Wkv4hrAPCkI/ev3YU=")
    ReactRefresh.reg(Component.raw, "CounterV")
    Component
  }

  GlobalVars.set(newComponent)

  def Component = GlobalVars.get()

  org.scalajs.dom.console.log("CounterV.Component: ", Component.raw)
}

// object Counter {
//   type Props = Unit
//   type State = Int

//   final class Backend($: BackendScope[Props, State]) {
//     def render(s: State): VdomNode =
//       <.button(
//         "Count: ", s,
//         ^.onClick --> $.modState(_ + 1),
//       )
//   }

//   val Component = ScalaComponent.builder[Props]
//     .initialState(0)
//     .renderBackend[Backend]
//     .configure(Reusability.shouldComponentUpdate)
//     .build
// }