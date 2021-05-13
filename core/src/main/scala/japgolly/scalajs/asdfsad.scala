package japgolly.scalajs

import japgolly.scalajs.react.{raw => Raw, _}
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.console
import scala.scalajs.js
import scala.scalajs.js.|

import CustomHook.CustomHook

object UseEffectScala {

  // TODO: Make reusable enough that end-users could reuse in their own (custom) hooks
  private def apply[A](effect: Callback, deps: A)(implicit reuse: Reusability[A]): CustomHook[Unit] =
    $ => {
      val prev = $.useState(deps)
      val effect2 = effect
        .finallyRun(prev.setState(deps))
        .unless_(reuse.test(prev.state, deps))
      $.useEffect(effect2)
    }

   @inline implicit class UseEffectScalaExt(private val $: HooksDsl) extends AnyVal {
    def useEffectScala[A](effect: Callback, deps: A)(implicit reuse: Reusability[A]) =
      apply(effect, deps)(reuse)($)
  }
}