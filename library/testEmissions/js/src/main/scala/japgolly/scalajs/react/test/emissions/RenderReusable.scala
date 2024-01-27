package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.hooks.HookCtx
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html.Input

object RenderReusable {

  def all = ScalaFnComponent[PI] { p =>
    <.div(
      P          (p),
      CtxObj_P   (),
      CtxFn_P    (p),
      P_PC       (p)(1),
      CtxObj_P_PC(2),
      CtxFn_P_PC (3),
    )
  }

  val P =
    ScalaFnComponent.withHooks[PI]
      .renderRRReusable(p => Reusable.implicitly(p.toString))

  val CtxObj_P =
    ScalaFnComponent.withHooks[Unit]
      .useRef(100)
      .useState(0)
      .renderRRReusable { $ =>
        val ref = $.hook1
        val s = $.hook2
        Reusable.never(
          <.div(
            ref.value,
            <.button(^.onClick --> ref.mod(_ + 1)),
            <.button(^.onClick --> s.modState(_ + 1)),
          )
        )
      }

  val CtxFn_P =
    ScalaFnComponent.withHooks[PI]
      .useState(20)
      .useCallback(Callback.log("asd"))
      .useForceUpdate
      .renderRRReusable { (p, s, incES, fu) =>
        Reusable.never(
          <.div(
            s"P=$p, S=${s.value}",
            <.button(^.onClick --> s.modState(_ + 1)),
            <.button(^.onClick --> (incES >> fu)),
          )
        )
      }

  val P_PC =
    ScalaFnComponent.withHooks[PI]
      .withPropsChildren
      .renderRRReusable { (p, c) =>
        Reusable.always(<.div(s"P=$p", c))
      }

  val CtxObj_P_PC =
    ScalaFnComponent.withHooks[Unit]
      .withPropsChildren
      .useRefToVdom[Input]
      .useState("x")
      .renderRRReusable { $ =>
        val c        = $.propsChildren
        val inputRef = $.hook1
        val s        = $.hook2
        def onChange(e: ReactEventFromInput): Callback = s.setState(e.target.value)
        Reusable.never(
          <.div(<.input.text.withRef(inputRef)(^.value := s.value, ^.onChange ==> onChange), c)
        )
      }

  val CtxFn_P_PC =
    ScalaFnComponent.withHooks[Unit]
      .withPropsChildren
      .useRefToVdom[Input]
      .useState("x")
      .renderRRReusable { (_, c, inputRef, s) =>
        def onChange(e: ReactEventFromInput): Callback = s.setState(e.target.value)
        Reusable.never(
          <.div(<.input.text.withRef(inputRef)(^.value := s.value, ^.onChange ==> onChange), c)
        )
      }

}
