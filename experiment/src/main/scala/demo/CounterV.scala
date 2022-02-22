package demo

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object CounterV {

  val Component = {
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

  org.scalajs.dom.console.log("CounterV.Component: ", Component.raw)
}
