package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html.Input
import scala.scalajs.js

object UseRef {

  trait JsP extends js.Object
  trait JsS extends js.Object
  trait JsF extends js.Object

  val jsComp = JsComponent.force[Null, Children.None, Null](null)

  val scalaComp = ScalaComponent.builder[Int].render_P(identity).build

  val Component = ScalaFnComponent.withHooks[Int]

    .useRef(100)
    .useRefBy((p, _) => p)
    .useRefBy(_.props)
    .useRefToVdom[Input]
    .useRefToAnyVdom
    .useRefToJsComponent(jsComp)
    .useRefToJsComponent[JsP, JsS]
    .useRefToJsComponentWithMountedFacade[JsP, JsS, JsF]
    .useRefToScalaComponent(scalaComp)
    .useRefToScalaComponent[Int, Long, String]

    .renderRR { _ => 123 }
}
