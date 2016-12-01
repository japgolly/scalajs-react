package japgolly.scalajs.react

import scala.scalajs.js

object ReactAddons {

  def Perf = raw.Perf

  lazy val CSSTransitionGroup =
    JsComponent[raw.ReactCSSTransitionGroupProps, Children.Varargs, Null](
      raw.ReactCSSTransitionGroup)

  @inline def CSSTransitionGroupProps(): raw.ReactCSSTransitionGroupProps =
    (new js.Object).asInstanceOf[raw.ReactCSSTransitionGroupProps]

  @inline def CSSTransitionGroupNames(): raw.ReactCSSTransitionGroupNames =
    (new js.Object).asInstanceOf[raw.ReactCSSTransitionGroupNames]
}
