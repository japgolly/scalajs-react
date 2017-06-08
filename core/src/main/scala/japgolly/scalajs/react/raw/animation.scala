package japgolly.scalajs.react.raw

import scala.scalajs.js
import scala.scalajs.js.|
import scala.scalajs.js.annotation._

@deprecated("ReactTransitionGroup and ReactCSSTransitionGroup are both deprecated as of React v15.5.0. The recommendation is to use TransitionGroup and CSSTransitionGroup from 'react-transition-group' instead.", "1.0.1")
@JSImport("react-addons-css-transition-group", JSImport.Namespace, "React.addons.CSSTransitionGroup")
@js.native
object ReactCSSTransitionGroup extends js.Object

@js.native
trait ReactCSSTransitionGroupProps extends js.Object {
  var transitionName         : js.UndefOr[String | ReactCSSTransitionGroupNames]
  var transitionAppear       : js.UndefOr[Boolean]
  var transitionEnter        : js.UndefOr[Boolean]
  var transitionLeave        : js.UndefOr[Boolean]
  var transitionAppearTimeout: js.UndefOr[JsNumber]
  var transitionEnterTimeout : js.UndefOr[JsNumber]
  var transitionLeaveTimeout : js.UndefOr[JsNumber]
}

@js.native
trait ReactCSSTransitionGroupNames extends js.Object {
  var appear      : js.UndefOr[String]
  var  enter      : js.UndefOr[String]
  var  leave      : js.UndefOr[String]
  var appearActive: js.UndefOr[String]
  var  enterActive: js.UndefOr[String]
  var  leaveActive: js.UndefOr[String]
}
