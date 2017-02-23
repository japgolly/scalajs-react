package japgolly.scalajs.react.extra

import japgolly.scalajs.react._

/**
 * Installing this will cause logging to occur at React component lifecycle stages.
 *
 * Install in `ScalaComponent.build` via `.configure(LogLifecycle.short)` or `.configure(LogLifecycle.verbose)`.
 */
object LogLifecycle {

  def short[P, C <: Children, S, B]: ScalaComponentConfig[P, C, S, B] =
    in => {
      val componentName = in.name

      def log(cbName: String): Callback =
        Callback.log(s"[$componentName] $cbName")

      in.componentWillMountConst       (log("componentWillMount"))
        .componentDidMountConst        (log("componentDidMount"))
        .componentWillUnmountConst     (log("componentWillUnmount"))
        .componentWillUpdateConst      (log("componentWillUpdate"))
        .componentDidUpdateConst       (log("componentDidUpdate"))
        .componentWillReceivePropsConst(log("componentWillReceiveProps"))
    }

  def verbose[P, C <: Children, S, B]: ScalaComponentConfig[P, C, S, B] =
    in => {
      val componentName = in.name

      // TODO Test LogLifecycle.verbose
      def log(cbName: String): ScalaComponent.Lifecycle.Base[P, S, B] => Callback =
        b => Callback.log(s"[$componentName] $cbName\n  $b\n  ", b.raw)

      in.componentWillMount       (log("componentWillMount"))
        .componentDidMount        (log("componentDidMount"))
        .componentWillUnmount     (log("componentWillUnmount"))
        .componentWillUpdate      (log("componentWillUpdate"))
        .componentDidUpdate       (log("componentDidUpdate"))
        .componentWillReceiveProps(log("componentWillReceiveProps"))
    }
}
