package japgolly.scalajs.react.extra

import scala.scalajs.js
import japgolly.scalajs.react._

/**
 * Installing this will cause logging to occur at React component lifecycle stages.
 *
 * Install in `ReactComponentB` via `.configure(LogLifecycle.short)` or `.configure(LogLifecycle.verbose)`.
 */
object LogLifecycle {
  private[this] def header(name: String): String => String =
    h => s"[$name] $h"

  private[this] def fmt(m: String, a: Any) =
    Seq[js.Any](s"\n  $m: $a")

  private[this] def log(m: js.Any, ps: js.Any*) =
    Callback.log(m, ps: _*)

  private[this] def logc(m: js.Any, c: js.Any, ps: js.Any*) =
    log(m + "\n ", c +: ps: _*)

  private[this] def log1(m: String) = (c: js.Any) =>
    logc(m, c)

  private[this] def logP[P <: Product](m: String, c: P => js.Any, extra: P => Seq[js.Any]) = (p: P) =>
    logc(m, c(p), extra(p): _*)

  private[this] def logCWU[P, S](m: String) =
    logP[ComponentWillUpdate[P, S, Any, TopNode]](m, _.$,
      i => fmt("Next props", i.nextProps) ++ fmt("Next state", i.nextState))

  private[this] def logCDU[P, S](m: String) =
    logP[ComponentDidUpdate[P, S, Any, TopNode]](m, _.$,
      i => fmt("Prev props", i.prevProps) ++ fmt("Prev state", i.prevState))

  private[this] def logCWRP[P, S](m: String) =
    logP[ComponentWillReceiveProps[P, S, Any, TopNode]](m, _.$,
      i => fmt("Next props", i.nextProps))

  def short[P, S, B, N <: TopNode] = (rc: ReactComponentB[P, S, B, N]) => {
    val h = header(rc.name)
    rc.componentWillMountCB       (log(h("componentWillMount")))
      .componentDidMountCB        (log(h("componentDidMount")))
      .componentWillUnmountCB     (log(h("componentWillUnmount")))
      .componentWillUpdateCB      (log(h("componentWillUpdate")))
      .componentDidUpdateCB       (log(h("componentDidUpdate")))
      .componentWillReceivePropsCB(log(h("componentWillReceiveProps")))
  }

  def verbose[P, S, B, N <: TopNode] = (rc: ReactComponentB[P, S, B, N]) => {
    val h = header(rc.name)
    rc.componentWillMount       (log1 (h("componentWillMount")))
      .componentDidMount        (log1 (h("componentDidMount")))
      .componentWillUnmount     (log1 (h("componentWillUnmount")))
      .componentWillUpdate      (logCWU[P,S](h("componentWillUpdate")))
      .componentDidUpdate       (logCDU[P,S](h("componentDidUpdate")))
      .componentWillReceiveProps(logCWRP[P,S](h("componentWillReceiveProps")))
  }
}
