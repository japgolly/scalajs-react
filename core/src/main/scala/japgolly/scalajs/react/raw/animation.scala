package japgolly.scalajs.react.raw

import scala.scalajs.js
import scala.scalajs.js.|
import scala.scalajs.js.annotation.JSName

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
