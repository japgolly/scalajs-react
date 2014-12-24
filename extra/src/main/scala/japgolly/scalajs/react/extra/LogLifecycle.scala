package japgolly.scalajs.react.extra

import japgolly.scalajs.react.ReactComponentB
import org.scalajs.dom.console
import scala.scalajs.js

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
    console.log(m, ps: _*)

  private[this] def logc(m: js.Any, c: js.Any, ps: js.Any*) =
    log(m + "\n ", c +: ps: _*)

  private[this] def log1(m: String) = (c: js.Any) =>
    logc(m, c)

  private[this] def logp(m: String) = (c: js.Any, p: Any) =>
    logc(m, c, fmt("Props", p): _*)

  private[this] def logps(m: String) = (c: js.Any, p: Any, s: Any) =>
    logc(m, c, fmt("Props", p) ++ fmt("State", s): _*)

  def short[P, S, B] = (rc: ReactComponentB[P, S, B]) => {
    val h = header(rc.name)
    rc.componentWillMount       (_       => log(h("componentWillMount")))
      .componentDidMount        (_       => log(h("componentDidMount")))
      .componentWillUnmount     (_       => log(h("componentWillUnmount")))
      .componentWillUpdate      ((_,_,_) => log(h("componentWillUpdate")))
      .componentDidUpdate       ((_,_,_) => log(h("componentDidUpdate")))
      .componentWillReceiveProps((_,_)   => log(h("componentWillReceiveProps")))
  }

  def verbose[P, S, B] = (rc: ReactComponentB[P, S, B]) => {
    val h = header(rc.name)
    rc.componentWillMount       (log1 (h("componentWillMount")))
      .componentDidMount        (log1 (h("componentDidMount")))
      .componentWillUnmount     (log1 (h("componentWillUnmount")))
      .componentWillUpdate      (logps(h("componentWillUpdate")))
      .componentDidUpdate       (logps(h("componentDidUpdate")))
      .componentWillReceiveProps(logp (h("componentWillReceiveProps")))
  }
}
