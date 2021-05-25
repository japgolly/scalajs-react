import sbt._

object GenHooks {

  def apply(srcRootDir: File): Unit = {

    val dir = srcRootDir / "japgolly/scalajs/react/hooks"

    println()
    println("Generating hooks boilerplate in: " + dir.getAbsolutePath)

    val hookCtxCtorsI   = List.newBuilder[String]
    val hookCtxCtorsP   = List.newBuilder[String]
    val hookCtxCtorsPC  = List.newBuilder[String]
    val hookCtxsI       = List.newBuilder[String]
    val hookCtxsP       = List.newBuilder[String]
    val hookCtxsPC      = List.newBuilder[String]
    val hookCtxFnsP     = List.newBuilder[String]
    val hookCtxFnsPC    = List.newBuilder[String]
    val dslAtStepsI     = List.newBuilder[String]
    val dslAtStepsP     = List.newBuilder[String]
    val dslAtStepsPC    = List.newBuilder[String]
    val stepMultisI     = List.newBuilder[String]
    val stepMultisP     = List.newBuilder[String]
    val stepMultisPC    = List.newBuilder[String]
    val useCallbackApis = List.newBuilder[String]
    val useCallbackArgs = List.newBuilder[String]

    for (n <- 1 to 22) {
      val As           = (1 to n).map('A' + _ - 1).map(_.toChar).mkString(", ")
      val as           = (1 to n).map('a' + _ - 1).map(_.toChar).mkString(", ")
      val _s           = (1 to n).map(_ => '_').mkString(", ")
      val preHns       = (1 until n).map("H" + _).mkString(", ")
      val Hns          = (1 to n).map("H" + _).mkString(", ")
      val coHns        = (1 to n).map("+H" + _).mkString(", ")
      val hookParams   = (1 to n).map(i => s"hook$i: H$i").mkString(", ")
      val hookArgs     = (1 to n).map(i => s"hook$i").mkString(", ")
      val ctxParams    = ((1 until n).map(i => s"hook$i: H$i") :+ s"final val hook$n: H$n").mkString(", ")
      val ctxSuperArgs = (1 until n).map(i => s", hook$i").mkString
      val ctxToStr     = (1 to n).map(i => s",\\n  hook$i = !hook$i").mkString

      useCallbackArgs +=
        s"""  implicit def c$n[$As]: UseCallbackArg[($As) => Callback] =
           |    UseCallbackArg[($As) => Callback, js.Function$n[$As, Unit]](
           |      f => f(${_s}).runNow())(
           |      z => Reusable.byRef(z).withValue(($as) => Callback(z($as))))
           |""".stripMargin

      useCallbackApis +=
        s"""  /** Returns a memoized callback.
           |    *
           |    * @see https://reactjs.org/docs/hooks-reference.html#usecallback
           |    */
           |  @inline final def useCallback$n[$As](f: ($As) => Callback)(implicit step: Step): step.Next[Reusable[($As) => Callback]] =
           |    useCallbackBy((_: Ctx) => (_: UseCallbackInline)(f))
           |
           |  /** Returns a memoized callback.
           |    *
           |    * Pass an inline callback and dependencies. useCallback will return a memoized version of the callback that only
           |    * changes if one of the dependencies has changed. This is useful when passing callbacks to optimized child
           |    * components that rely on reference equality to prevent unnecessary renders.
           |    *
           |    * @see https://reactjs.org/docs/hooks-reference.html#usecallback
           |    */
           |  @inline final def useCallback$n[$As, Z](f: ($As) => Callback, deps: Z)(implicit r: Reusability[Z], step: Step): step.Next[Reusable[($As) => Callback]] =
           |    useCallbackBy((_: Ctx) => (_: UseCallbackInline)(f, deps))
           |""".stripMargin

      if (n <= 21) {
        hookCtxCtorsI += s"    def apply[I, $Hns](input: I, $hookParams): I$n[I, $Hns] =\n      new I$n(input, $hookArgs)"

        hookCtxCtorsP += s"  def apply[P, $Hns](props: P, $hookParams): P$n[P, $Hns] =\n    new P$n(props, $hookArgs)"

        hookCtxsI +=
          s"""  class I$n[+I, $coHns](input: I, $ctxParams) extends I${n-1}(input$ctxSuperArgs) {
             |    override def toString = s"HookCtx.withInput(\\n  input = !input$ctxToStr)"
             |    def apply$n[A](f: (I, $Hns) => A): A = f(input, $hookArgs)
             |  }
             |""".stripMargin.replace('!', '$')

        hookCtxsP +=
          s"""  class P$n[+P, $coHns](props: P, $ctxParams) extends P${n-1}(props$ctxSuperArgs) {
             |    override def toString = s"HookCtx(\\n  props = !props$ctxToStr)"
             |    def apply$n[A](f: (P, $Hns) => A): A = f(props, $hookArgs)
             |  }
             |""".stripMargin.replace('!', '$')

        hookCtxFnsP += s"  sealed trait P$n[P, $Hns] extends HookCtxFn { override type Fn[A] = (P, $Hns) => A }"

        if (n <= 20) {
          hookCtxCtorsPC += s"    def apply[P, $Hns](props: P, propsChildren: PropsChildren, $hookParams): PC$n[P, $Hns] =\n      new PC$n(props, propsChildren, $hookArgs)"

          hookCtxsPC +=
            s"""  class PC$n[+P, $coHns](props: P, propsChildren: PropsChildren, $ctxParams) extends PC${n-1}(props, propsChildren$ctxSuperArgs) {
               |    override def toString = s"HookCtx.withChildren(\\n  props = !props,\\n  propsChildren = !propsChildren$ctxToStr)"
               |    def apply$n[A](f: (P, PropsChildren, $Hns) => A): A = f(props, propsChildren, $hookArgs)
               |  }
               |""".stripMargin.replace('!', '$')

          hookCtxFnsPC += s"  sealed trait PC$n[P, $Hns] extends HookCtxFn { override type Fn[A] = (P, PropsChildren, $Hns) => A }"
        }
      }

      if (n != 1 && n <= 21) {
        val s = n - 1

        val preCtxArgs = (1 until n).map(i => s"ctx$s.hook$i").mkString(", ")

        dslAtStepsI += s"  sealed trait AtStep$s[I, $preHns] { type Next[H$n] = Custom.Subsequent[I, HookCtx.I$n[I, $Hns], HookCtxFn.P$n[I, $Hns]#Fn] }"
        stepMultisI +=
          s"""  type AtStep$s[I, $preHns] = To[
             |    I,
             |    HookCtx.I$s[I, $preHns],
             |    HookCtxFn.P$s[I, $preHns]#Fn,
             |    Custom.Subsequent.AtStep$s[I, $preHns]#Next]
             |
             |  implicit def atStep$s[I, $preHns]: AtStep$s[I, $preHns] =
             |    new Custom.SubsequentStep[I, HookCtx.I$s[I, $preHns], HookCtxFn.P$s[I, $preHns]#Fn] {
             |      override type Next[H$n] = Custom.Subsequent.AtStep$s[I, $preHns]#Next[H$n]
             |      override def next[H$n] =
             |        (buildPrev, initNextHook) => {
             |          val buildNext: Custom.BuildFn[I, HookCtx.I$n[I, $Hns]] =
             |            new Custom.BuildFn[I, HookCtx.I$n[I, $Hns]] {
             |              override def apply[O](f: HookCtx.I$n[I, $Hns] => O) = {
             |                buildPrev { ctx$s =>
             |                  val h$n = initNextHook(ctx$s)
             |                  val ctx$n = HookCtx.withInput(ctx$s.input, $preCtxArgs, h$n)
             |                  f(ctx$n)
             |                }
             |              }
             |            }
             |          new Custom.Subsequent[I, HookCtx.I$n[I, $Hns], HookCtxFn.P$n[I, $Hns]#Fn](buildNext)
             |        }
             |      override def squash[A] = f => _.apply$s(f)
             |    }
             |""".stripMargin

        dslAtStepsP += s"  sealed trait AtStep$s[P, $preHns] { type Next[H$n] = ComponentP.Subsequent[P, HookCtx.P$n[P, $Hns], HookCtxFn.P$n[P, $Hns]#Fn] }"
        stepMultisP +=
          s"""  type AtStep$s[P, $preHns] = To[
             |    P,
             |    HookCtx.P$s[P, $preHns],
             |    HookCtxFn.P$s[P, $preHns]#Fn,
             |    ComponentP.Subsequent.AtStep$s[P, $preHns]#Next]
             |
             |  implicit def atStep$s[P, $preHns]: AtStep$s[P, $preHns] =
             |    new ComponentP.SubsequentStep[P, HookCtx.P$s[P, $preHns], HookCtxFn.P$s[P, $preHns]#Fn] {
             |      override type Next[H$n] = ComponentP.Subsequent.AtStep$s[P, $preHns]#Next[H$n]
             |      override def next[H$n] =
             |        (renderPrev, initNextHook) => {
             |          val renderNext: ComponentP.RenderFn[P, HookCtx.P$n[P, $Hns]] =
             |            render => renderPrev { ctx$s =>
             |              val h$n = initNextHook(ctx$s)
             |              val ctx$n = HookCtx(ctx$s.props, $preCtxArgs, h$n)
             |              render(ctx$n)
             |            }
             |          new ComponentP.Subsequent[P, HookCtx.P$n[P, $Hns], HookCtxFn.P$n[P, $Hns]#Fn](renderNext)
             |        }
             |      override def squash[A] = f => _.apply$s(f)
             |    }
             |""".stripMargin

        if (n <= 20) {
          dslAtStepsPC += s"  sealed trait AtStep$s[P, $preHns] { type Next[H$n] = ComponentPC.Subsequent[P, HookCtx.PC$n[P, $Hns], HookCtxFn.PC$n[P, $Hns]#Fn] }"
          stepMultisPC +=
            s"""  type AtStep$s[P, $preHns] = To[
               |    P,
               |    HookCtx.PC$s[P, $preHns],
               |    HookCtxFn.PC$s[P, $preHns]#Fn,
               |    ComponentPC.Subsequent.AtStep$s[P, $preHns]#Next]
               |
               |  implicit def atStep$s[P, $preHns]: AtStep$s[P, $preHns] =
               |    new ComponentPC.SubsequentStep[P, HookCtx.PC$s[P, $preHns], HookCtxFn.PC$s[P, $preHns]#Fn] {
               |      override type Next[H$n] = ComponentPC.Subsequent.AtStep$s[P, $preHns]#Next[H$n]
               |      override def next[H$n] =
               |        (renderPrev, initNextHook) => {
               |          val renderNext: ComponentPC.RenderFn[P, HookCtx.PC$n[P, $Hns]] =
               |            render => renderPrev { ctx$s =>
               |              val h$n = initNextHook(ctx$s)
               |              val ctx$n = HookCtx.withChildren(ctx$s.props, ctx$s.propsChildren, $preCtxArgs, h$n)
               |              render(ctx$n)
               |            }
               |          new ComponentPC.Subsequent[P, HookCtx.PC$n[P, $Hns], HookCtxFn.PC$n[P, $Hns]#Fn](renderNext)
               |        }
               |      override def squash[A] = f => _.apply$s(f)
               |    }
               |""".stripMargin
        }
      }
    }

    val header =
      s"""package japgolly.scalajs.react.hooks
         |
         |// DO NOT MANUALLY EDIT
         |// DO NOT MANUALLY EDIT
         |//
         |// THIS IS GENERATED BY RUNNING genHooks IN SBT
         |//
         |// DO NOT MANUALLY EDIT
         |// DO NOT MANUALLY EDIT
         |""".stripMargin.trim

    def save(filename: String)(content: String): Unit = {
      println(s"Generating $filename ...")
      val c = content.trim + "\n"
//      println(c)
      IO.write(dir / filename, c)
    }

    save("HookCtx.scala")(
      s"""$header
         |
         |import japgolly.scalajs.react.PropsChildren
         |
         |object HookCtx {
         |
         |${hookCtxCtorsP.result().mkString("\n\n")}
         |
         |  abstract class P0[+P](final val props: P)
         |
         |${hookCtxsP.result().mkString("\n")}
         |  // ===================================================================================================================
         |
         |  object withChildren {
         |
         |    def apply[P](props: P, propsChildren: PropsChildren): PC0[P] =
         |      new PC0(props, propsChildren)
         |
         |${hookCtxCtorsPC.result().mkString("\n\n")}
         |  }
         |
         |  class PC0[+P](props: P, final val propsChildren: PropsChildren) extends P0(props)
         |
         |${hookCtxsPC.result().mkString("\n")}
         |
         |  // ===================================================================================================================
         |
         |  object withInput {
         |
         |    def apply[I](input: I): I0[I] =
         |      new I0(input)
         |
         |${hookCtxCtorsI.result().mkString("\n\n")}
         |  }
         |
         |  class I0[+I](final val input: I)
         |
         |${hookCtxsI.result().mkString("\n")}
         |}
         |""".stripMargin
    )

    save("HookCtxFn.scala")(
      s"""$header
         |
         |import japgolly.scalajs.react.PropsChildren
         |
         |// Note: these are never instantiated. They're just here to serve as type lambdas in Scala 2.
         |sealed trait HookCtxFn { type Fn[A] }
         |
         |object HookCtxFn {
         |${hookCtxFnsP.result().mkString("\n")}
         |
         |${hookCtxFnsPC.result().mkString("\n")}
         |}
         |""".stripMargin
    )

    save("StepBoilerplate.scala")(
      s"""$header
         |
         |import japgolly.scalajs.react.hooks.CustomHook.{Builder => Custom}
         |import japgolly.scalajs.react.hooks.HookComponentBuilder._
         |
         |trait ComponentP_SubsequentDsl { self: ComponentP.Subsequent.type =>
         |${dslAtStepsP.result().mkString("\n")}
         |}
         |
         |trait ComponentP_SubsequentSteps { self: ComponentP.SubsequentStep.type =>
         |
         |${stepMultisP.result().mkString("\n")}
         |}
         |
         |// =====================================================================================================================
         |
         |trait ComponentPC_SubsequentDsl { self: ComponentPC.Subsequent.type =>
         |${dslAtStepsPC.result().mkString("\n")}
         |}
         |
         |trait ComponentPC_SubsequentSteps { self: ComponentPC.SubsequentStep.type =>
         |
         |${stepMultisPC.result().mkString("\n")}
         |}
         |
         |// =====================================================================================================================
         |
         |trait Custom_SubsequentDsl { self: Custom.Subsequent.type =>
         |${dslAtStepsI.result().mkString("\n")}
         |}
         |
         |trait Custom_SubsequentSteps { self: Custom.SubsequentStep.type =>
         |
         |${stepMultisI.result().mkString("\n")}
         |}
         |""".stripMargin
    )

    save("UseCallbackBoilerplate.scala")(
      s"""$header
         |
         |import japgolly.scalajs.react.hooks.Hooks.UseCallbackArg
         |import japgolly.scalajs.react.{Callback, Reusability, Reusable}
         |import scala.scalajs.js
         |
         |trait UseCallbackExtraApi[Ctx, Step <: Api.Step] { self: Api.Primary[Ctx, Step] =>
         |  import Api.UseCallbackInline
         |
         |${useCallbackApis.result().mkString("\n\n")}
         |}
         |
         |// =====================================================================================================================
         |
         |trait UseCallbackArgInstances {
         |
         |${useCallbackArgs.result().mkString("\n\n")}
         |}
         |""".stripMargin
    )

    println()
  }
}