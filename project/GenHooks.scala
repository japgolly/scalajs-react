import sbt._

object GenHooks {

  def apply(srcRootDir: File): Unit = {

    val dir = srcRootDir / "japgolly/scalajs/react/hooks"

    println()
    println("Generating hooks boilerplate in: " + dir.getAbsolutePath)

    val hookCtxCtorsP  = List.newBuilder[String]
    val hookCtxCtorsPC = List.newBuilder[String]
    val hookCtxsP      = List.newBuilder[String]
    val hookCtxsPC     = List.newBuilder[String]
    val hookCtxFnsP    = List.newBuilder[String]
    val hookCtxFnsPC   = List.newBuilder[String]
    val dslAtStepsP    = List.newBuilder[String]
    val dslAtStepsPC   = List.newBuilder[String]
    val stepMultisP    = List.newBuilder[String]
    val stepMultisPC   = List.newBuilder[String]

    for (n <- 1 to 21) {
      val preHns       = (1 until n).map("H" + _).mkString(", ")
      val Hns          = (1 to n).map("H" + _).mkString(", ")
      val coHns        = (1 to n).map("+H" + _).mkString(", ")
      val hookParams   = (1 to n).map(i => s"hook$i: H$i").mkString(", ")
      val hookArgs     = (1 to n).map(i => s"hook$i").mkString(", ")
      val ctxParams    = ((1 until n).map(i => s"hook$i: H$i") :+ s"final val hook$n: H$n").mkString(", ")
      val ctxSuperArgs = (1 until n).map(i => s", hook$i").mkString
      val ctxToStr     = (1 to n).map(i => s",\\n  hook$i = !hook$i").mkString

      hookCtxCtorsP += s"  def apply[P, $Hns](props: P, $hookParams): P$n[P, $Hns] =\n    new P$n(props, $hookArgs)"

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

      if (n != 1) {
        val s = n - 1

        val preCtxArgs = (1 until n).map(i => s"ctx$s.hook$i").mkString(", ")

        dslAtStepsP += s"  sealed trait AtStep$s[P, $preHns] { type Next[H$n] = ComponentP.Subsequent[P, HookCtx.P$n[P, $Hns], HookCtxFn.P$n[P, $Hns]#Fn] }"
        stepMultisP +=
          s"""  type AtStep$s[P, $preHns] = To[
             |    P,
             |    HookCtx.P$s[P, $preHns],
             |    HookCtxFn.P$s[P, $preHns]#Fn,
             |    ComponentP.Subsequent.AtStep$s[P, $preHns]#Next]
             |
             |  implicit def atStep$s[P, $preHns]: AtStep$s[P, $preHns] =
             |    new Step.ComponentP.Subsequent[P, HookCtx.P$s[P, $preHns], HookCtxFn.P$s[P, $preHns]#Fn] {
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
               |    new Step.ComponentPC.Subsequent[P, HookCtx.PC$s[P, $preHns], HookCtxFn.PC$s[P, $preHns]#Fn] {
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
         |import HookComponentBuilder._
         |
         |trait ComponentP_SubsequentDsl { self: ComponentP.Subsequent.type =>
         |${dslAtStepsP.result().mkString("\n")}
         |}
         |
         |trait ComponentP_SubsequentSteps { self: Step.ComponentP.Subsequent.type =>
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
         |trait ComponentPC_SubsequentSteps { self: Step.ComponentPC.Subsequent.type =>
         |
         |${stepMultisPC.result().mkString("\n")}
         |}
         |""".stripMargin
    )

    println()
  }
}