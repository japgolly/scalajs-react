package japgolly.scalajs.react

import scala.scalajs.js
import vdom.ReactVDom.all._

object Addons {

  object ReactCssTransitionGroup {
    /** Items in the CSSTransitionGroup need this attribute for animation to work properly. */
    val key = "key".attr
  }

  case class ReactCssTransitionGroup(name: String,
                                     enter: js.UndefOr[Boolean] = js.undefined,
                                     leave: js.UndefOr[Boolean] = js.undefined,
                                     component: js.UndefOr[String] = js.undefined) {
    def toJs: js.Object = {
      val p = js.Dynamic.literal("transitionName" -> name)
      enter.foreach(v => p.updateDynamic("transitionEnter")(v))
      leave.foreach(v => p.updateDynamic("transitionLeave")(v))
      component.foreach(v => p.updateDynamic("component")(React.DOM.selectDynamic(v)))
      p
    }

    def apply(children: ReactNode*): ReactComponentU_ = {
      val f = React.addons.CSSTransitionGroup
      f(toJs, children.toJsArray).asInstanceOf[ReactComponentU_]
    }
  }
}
