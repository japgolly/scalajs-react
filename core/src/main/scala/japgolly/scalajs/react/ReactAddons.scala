package japgolly.scalajs.react

import scala.scalajs.js

object ReactAddons {

  def Perf = raw.Perf

  @deprecated("ReactTransitionGroup and ReactCSSTransitionGroup are both deprecated as of React v15.5.0. The recommendation is to use TransitionGroup and CSSTransitionGroup from 'react-transition-group' instead.", "1.0.1")
  lazy val CSSTransitionGroup =
    JsComponent[raw.ReactCSSTransitionGroupProps, Children.Varargs, Null](
      raw.ReactCSSTransitionGroup)

  @inline def CSSTransitionGroupProps(): raw.ReactCSSTransitionGroupProps =
    (new js.Object).asInstanceOf[raw.ReactCSSTransitionGroupProps]

  @inline def CSSTransitionGroupNames(): raw.ReactCSSTransitionGroupNames =
    (new js.Object).asInstanceOf[raw.ReactCSSTransitionGroupNames]
}
