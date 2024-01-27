package downstream.mima200

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

// Because `useRefToJsComponent` didn't need the `CT0` arg so I removed it.
object HookUseRefToJsComponent {

  val jsComp = JsComponent.force[Null, Children.None, Null](null)

  val comp = ScalaFnComponent.withHooks[Unit]
    .useRefToJsComponent(jsComp)
    .render { (_, ref) =>
      jsComp.withRef(ref)()
    }
}
