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
    val dslAtStepsI     = List.newBuilder[String]
    val dslAtStepsP     = List.newBuilder[String]
    val dslAtStepsPC    = List.newBuilder[String]
    val stepMultisI     = List.newBuilder[String]
    val stepMultisP     = List.newBuilder[String]
    val stepMultisPC    = List.newBuilder[String]
    val useCallbackArgs = List.newBuilder[String]

    def hookCtxFn(n: Int, p: String, pCount: Int) = {
      val Hns = (1 to n).map("H" + _).mkString(", ")
      // s"({ type F[A] = ($p, $Hns) => A})#F"
      s"({ type F[A] = js.Function${pCount + n}[$p, $Hns, A] })#F"
    }

    def hookCtxFnI(n: Int) = hookCtxFn(n, "I", 1)
    def hookCtxFnP(n: Int) = hookCtxFn(n, "P", 1)
    def hookCtxFnPC(n: Int) = hookCtxFn(n, "P, PropsChildren", 2)

    // val i12663_pre = s"({ type F[A] = $s[A]})#F" // TODO: https://github.com/lampepfl/dotty/issues/12663
    // val i12663_a = "({ type F[A] = "
    // val i12663_c = "})#F"
    // val i12663_bc = "[A]" + i12663_c

    for (n <- 1 to 5) {
      val As           = (1 to n).map('A' + _ - 1).map(_.toChar).mkString(", ")
      val as           = (1 to n).map('a' + _ - 1).map(_.toChar).mkString(", ")
      val _s           = (1 to n).map(_ => '_').mkString(", ")
      val preHns       = (1 until n).map("H" + _).mkString(", ")
      val Hns          = (1 to n).map("H" + _).mkString(", ")
      val RHns         = (1 to n).map(n => s"H$n: Reusability[H$n]").mkString(", ")
      val RHnTests     = (1 to n).map(n => s"H$n.test(x.hook$n, y.hook$n)").mkString(" && ")
      val coHns        = (1 to n).map("+H" + _).mkString(", ")
      val hookParams   = (1 to n).map(i => s"hook$i: H$i").mkString(", ")
      val hookArgs     = (1 to n).map(i => s"hook$i").mkString(", ")
      val ctxParams    = ((1 until n).map(i => s"hook$i: H$i") :+ s"final val hook$n: H$n").mkString(", ")
      val ctxSuperArgs = (1 until n).map(i => s", hook$i").mkString
      val ctxToStr     = (1 to n).map(i => s",\\n  hook$i = !hook$i").mkString

      useCallbackArgs +=
        s"""  implicit def c$n[$As, Z[_]](implicit Z: Dispatch[Z]): UseCallbackArg[($As) => Z[Unit]] =
           |    UseCallbackArg[($As) => Z[Unit], js.Function$n[$As, Unit]](
           |      z => ($as) => Z.dispatch(z($as)))(
           |      z => Reusable.byRef(z).withValue(($as) => Z.delay(z($as))))
           |""".stripMargin

      if (n <= 21) {
        hookCtxCtorsI += s"    def apply[I, $Hns](input: I, $hookParams): I$n[I, $Hns] =\n      new I$n(input, $hookArgs)"

        hookCtxCtorsP += s"  def apply[P, $Hns](props: P, $hookParams): P$n[P, $Hns] =\n    new P$n(props, $hookArgs)"

        hookCtxsI +=
          s"""  class I$n[+I, $coHns](input: I, $ctxParams) extends I${n-1}(input$ctxSuperArgs) {
             |    override def toString = s"HookCtx.withInput(\\n  input = !input$ctxToStr)"
             |    def apply$n[A](f: (I, $Hns) => A): A = f(input, $hookArgs)
             |  }
             |
             |  implicit def reusabilityI$n[I, $Hns](implicit I: Reusability[I], $RHns): Reusability[I$n[I, $Hns]] =
             |    Reusability((x, y) => I.test(x.input, y.input) && $RHnTests)
             |""".stripMargin.replace('!', '$')

        hookCtxsP +=
          s"""  class P$n[+P, $coHns](props: P, $ctxParams) extends P${n-1}(props$ctxSuperArgs) {
             |    override def toString = s"HookCtx(\\n  props = !props$ctxToStr)"
             |    @inline final def apply$n[A](f: js.Function${n + 1}[P, $Hns, A]): A = f(props, $hookArgs)
             |  }
             |
             |  implicit def reusabilityP$n[P, $Hns](implicit P: Reusability[P], $RHns): Reusability[P$n[P, $Hns]] =
             |    Reusability((x, y) => P.test(x.props, y.props) && $RHnTests)
             |""".stripMargin.replace('!', '$')

        if (n <= 20) {
          hookCtxCtorsPC += s"    def apply[P, $Hns](props: P, propsChildren: PropsChildren, $hookParams): PC$n[P, $Hns] =\n      new PC$n(props, propsChildren, $hookArgs)"

          hookCtxsPC +=
            s"""  class PC$n[+P, $coHns](props: P, propsChildren: PropsChildren, $ctxParams) extends PC${n-1}(props, propsChildren$ctxSuperArgs) {
               |    override def toString = s"HookCtx.withChildren(\\n  props = !props,\\n  propsChildren = !propsChildren$ctxToStr)"
               |    def apply$n[A](f: (P, PropsChildren, $Hns) => A): A = f(props, propsChildren, $hookArgs)
               |  }
               |
               |  implicit def reusabilityPC$n[P, $Hns](implicit P: Reusability[P], PC: Reusability[PropsChildren], $RHns): Reusability[PC$n[P, $Hns]] =
               |    Reusability((x, y) => P.test(x.props, y.props) && PC.test(x.propsChildren, y.propsChildren) && $RHnTests)
               |""".stripMargin.replace('!', '$')
        }
      }

      if (n != 1 && n <= 21) {
        val s = n - 1

        val preCtxArgs = (1 until n).map(i => s"ctx$s.hook$i").mkString(", ")

        dslAtStepsI += s"  sealed trait AtStep$s[I, $preHns] { type Next[H$n] = Custom.Subsequent[I, HookCtx.I$n[I, $Hns], ${hookCtxFnI(n)}] }"
        stepMultisI +=
          s"""  type AtStep$s[I, $preHns] = To[
             |    I,
             |    HookCtx.I$s[I, $preHns],
             |    ${hookCtxFnI(s)},
             |    Custom.Subsequent.AtStep$s[I, $preHns]#Next]
             |
             |  implicit def atStep$s[I, $preHns]: AtStep$s[I, $preHns] =
             |    new Custom.SubsequentStep[I, HookCtx.I$s[I, $preHns], ${hookCtxFnI(s)}] {
             |      override type Next[H$n] = Custom.Subsequent.AtStep$s[I, $preHns]#Next[H$n]
             |      override def next[H$n] =
             |        (buildPrev, initNextHook) => {
             |          val buildNext: Custom.BuildFn[I, HookCtx.I$n[I, $Hns]] =
             |            new Custom.BuildFn[I, HookCtx.I$n[I, $Hns]] {
             |              override def apply[O](f: js.Function1[HookCtx.I$n[I, $Hns], O]) = {
             |                buildPrev { ctx$s =>
             |                  val h$n = initNextHook(ctx$s)
             |                  val ctx$n = HookCtx.withInput(ctx$s.input, $preCtxArgs, h$n)
             |                  f(ctx$n)
             |                }
             |              }
             |            }
             |          new Custom.Subsequent[I, HookCtx.I$n[I, $Hns], ${hookCtxFnI(n)}](buildNext)
             |        }
             |      @inline override def squash[A] = f => _.apply$s(f)
             |    }
             |""".stripMargin

        dslAtStepsP += s"  sealed trait AtStep$s[P, $preHns] { type Next[H$n] = ComponentP.Subsequent[P, HookCtx.P$n[P, $Hns], ${hookCtxFnP(n)}] }"
        stepMultisP +=
          s"""  type AtStep$s[P, $preHns] = To[
             |    P,
             |    HookCtx.P$s[P, $preHns],
             |    ${hookCtxFnP(s)},
             |    ComponentP.Subsequent.AtStep$s[P, $preHns]#Next]
             |
             |  implicit def atStep$s[P, $preHns]: AtStep$s[P, $preHns] =
             |    new ComponentP.SubsequentStep[P, HookCtx.P$s[P, $preHns], ${hookCtxFnP(s)}] {
             |      override type Next[H$n] = ComponentP.Subsequent.AtStep$s[P, $preHns]#Next[H$n]
             |      override def next[H$n] =
             |        (renderPrev, initNextHook) => {
             |          val renderNext: ComponentP.RenderFn[P, HookCtx.P$n[P, $Hns]] =
             |            render => renderPrev { ctx$s =>
             |              val h$n = initNextHook(ctx$s)
             |              val ctx$n = HookCtx(ctx$s.props, $preCtxArgs, h$n)
             |              render(ctx$n)
             |            }
             |          new ComponentP.Subsequent[P, HookCtx.P$n[P, $Hns], ${hookCtxFnP(n)}](renderNext)
             |        }
             |      override def squash[A] = f => _.apply$s(f)
             |    }
             |""".stripMargin

        if (n <= 20) {
          dslAtStepsPC += s"  sealed trait AtStep$s[P, $preHns] { type Next[H$n] = ComponentPC.Subsequent[P, HookCtx.PC$n[P, $Hns], ${hookCtxFnPC(n)}] }"
          stepMultisPC +=
            s"""  type AtStep$s[P, $preHns] = To[
               |    P,
               |    HookCtx.PC$s[P, $preHns],
               |    ${hookCtxFnPC(s)},
               |    ComponentPC.Subsequent.AtStep$s[P, $preHns]#Next]
               |
               |  implicit def atStep$s[P, $preHns]: AtStep$s[P, $preHns] =
               |    new ComponentPC.SubsequentStep[P, HookCtx.PC$s[P, $preHns], ${hookCtxFnPC(s)}] {
               |      override type Next[H$n] = ComponentPC.Subsequent.AtStep$s[P, $preHns]#Next[H$n]
               |      override def next[H$n] =
               |        (renderPrev, initNextHook) => {
               |          val renderNext: ComponentPC.RenderFn[P, HookCtx.PC$n[P, $Hns]] =
               |            render => renderPrev { ctx$s =>
               |              val h$n = initNextHook(ctx$s)
               |              val ctx$n = HookCtx.withChildren(ctx$s.props, ctx$s.propsChildren, $preCtxArgs, h$n)
               |              render(ctx$n)
               |            }
               |          new ComponentPC.Subsequent[P, HookCtx.PC$n[P, $Hns], ${hookCtxFnPC(n)}](renderNext)
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
         |import japgolly.scalajs.react.{PropsChildren, Reusability}
         |import scala.scalajs.js
         |
         |object HookCtx {
         |
         |${hookCtxCtorsP.result().mkString("\n\n")}
         |
         |  abstract class P0[+P](final val props: P)
         |
         |  implicit def reusabilityP0[P](implicit P: Reusability[P]): Reusability[P0[P]] =
         |    Reusability.by(_.props)
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
         |  implicit def reusabilityPC0[P](implicit P: Reusability[P], PC: Reusability[PropsChildren]): Reusability[PC0[P]] =
         |    Reusability((x, y) => P.test(x.props, y.props) && PC.test(x.propsChildren, y.propsChildren))
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
         |  implicit def reusabilityI0[I](implicit I: Reusability[I]): Reusability[I0[I]] =
         |    Reusability.by(_.input)
         |
         |${hookCtxsI.result().mkString("\n")}
         |}
         |""".stripMargin
    )

    save("StepBoilerplate.scala")(
      s"""$header
         |
         |import japgolly.scalajs.react.PropsChildren
         |import japgolly.scalajs.react.hooks.CustomHook.{Builder => Custom}
         |import japgolly.scalajs.react.hooks.HookComponentBuilder._
         |import scala.scalajs.js
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
         |import japgolly.scalajs.react.Reusable
         |import japgolly.scalajs.react.hooks.Hooks.UseCallbackArg
         |import japgolly.scalajs.react.util.Effect._
         |import scala.scalajs.js
         |
         |trait UseCallbackArgInstances {
         |
         |${useCallbackArgs.result().mkString("\n")}
         |}
         |""".stripMargin
    )

    println()
  }
}
