package japgolly.scalajs.react.extras

import japgolly.scalajs.react.ReactComponentB
import org.scalajs.dom.console
import scala.scalajs.js

/**
 * Installing this will cause logging to occur at React component lifecycle stages.
 *
 * Install in `ReactComponentB` via `.configure(LogLifecycle.short)` or `.configure(LogLifecycle.verbose)`.
 */
object LogLifecycle {
  @inline private[this] def fmt(m: String, a: Any) =
    Seq[js.Any](s"\n  $m: $a")

  @inline private[this] def log(m: js.Any, ps: js.Any*) =
    console.log(m, ps: _*)

  @inline private[this] def logc(m: js.Any, c: js.Any, ps: js.Any*) =
    log(m + "\n ", c +: ps: _*)

  @inline private[this] def log1(m: String) = (c: js.Any) =>
    logc(m, c)

  @inline private[this] def logp(m: String) = (c: js.Any, p: Any) =>
    logc(m, c, fmt("Props", p): _*)

  @inline private[this] def logps(m: String) = (c: js.Any, p: Any, s: Any) =>
    logc(m, c, fmt("Props", p) ++ fmt("State", s): _*)

  def short[P, S, B] = (rc: ReactComponentB[P, S, B]) => {
    val name = rc.name
    rc.componentWillMount       (_       => log(s"$name.componentWillMount"))
      .componentDidMount        (_       => log(s"$name.componentDidMount"))
      .componentWillUnmount     (_       => log(s"$name.componentWillUnmount"))
      .componentWillUpdate      ((_,_,_) => log(s"$name.componentWillUpdate"))
      .componentDidUpdate       ((_,_,_) => log(s"$name.componentDidUpdate"))
      .componentWillReceiveProps((_,_)   => log(s"$name.componentWillReceiveProps"))
  }

  def verbose[P, S, B] = (rc: ReactComponentB[P, S, B]) => {
    val name = rc.name
    rc.componentWillMount       (log1 (s"$name.componentWillMount"))
      .componentDidMount        (log1 (s"$name.componentDidMount"))
      .componentWillUnmount     (log1 (s"$name.componentWillUnmount"))
      .componentWillUpdate      (logps(s"$name.componentWillUpdate"))
      .componentDidUpdate       (logps(s"$name.componentDidUpdate"))
      .componentWillReceiveProps(logp (s"$name.componentWillReceiveProps"))
  }
}
