package japgolly.scalajs.react.extra

import japgolly.scalajs.react._

/**
 * Installing this will cause logging to occur at React component lifecycle stages.
 *
 * Install in `ScalaComponent.build` via `.configure(LogLifecycle.xxxxxx)`.
 */
object LogLifecycle {

  def custom[P, C <: Children, S, B](logFn: String => ScalaComponent.Lifecycle.Base[P, S, B] => Callback): ScalaComponent.Config[P, C, S, B] =
    in => {
      val log = logFn(in.name)
      in.componentWillMount       (log)
        .componentDidMount        (log)
        .componentWillUnmount     (log)
        .componentWillUpdate      (log)
        .componentDidUpdate       (log)
        .componentWillReceiveProps(log)
    }

  def short[P, C <: Children, S, B]: ScalaComponent.Config[P, C, S, B] =
    custom(componentName => lc =>
      Callback.log(s"[$componentName] ${lc.toString.replaceFirst("\\(.+", "")}"))

  def default[P, C <: Children, S, B]: ScalaComponent.Config[P, C, S, B] =
    custom(componentName => lc =>
      Callback.log(s"[$componentName] $lc"))

  def verbose[P, C <: Children, S, B]: ScalaComponent.Config[P, C, S, B] =
    custom(componentName => lc =>
      Callback.log(s"[$componentName] $lc", lc.raw))

  /** Warning: Consumes and ignores errors. */
  def errors[P, C <: Children, S, B]: ScalaComponent.Config[P, C, S, B] =
    b => {
      val prefix = s"[${b.name}] Error occurred: "
      b.componentDidCatch($ => Callback.log(prefix, $.error, $.info))
    }
}
