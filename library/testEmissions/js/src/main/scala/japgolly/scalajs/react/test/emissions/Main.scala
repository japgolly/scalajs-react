package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

object Main {

  def main(): Unit = {
    val app  = Component()
    val cont = dom.document.getElementById("root")
    app.renderIntoDOM(cont)
  }

  private val Component = ScalaFnComponent[Unit] { _ =>
    <.div(
      CustomHooks                .Component(0),
      HooksPrimative             .Component(),
      HooksTrivial               .Component(0),
      HooksWithChildrenCtxFn     .Component(0)(<.div),
      HooksWithChildrenCtxObj    .Component(0)(<.div),
      HooksWithJsFns             .Component(0),
      HooksWithScalaFns          .Component(0),
      JustPropsChildrenViaHookApi.Component(0)(<.div),
      JustPropsViaHookApi        .Component(0),
      UseCallback                .Component(0),
      UseEffect                  .Component(0),
      UseMemo                    .Component(0),
      UseRef                     .Component(0),
      UseStateWithReuse          .Component(0),
    )
  }
}
