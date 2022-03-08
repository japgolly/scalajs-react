package japgolly.scalajs.react

import japgolly.microlibs.compiletime._
import japgolly.microlibs.compiletime.MacroEnv._
import japgolly.scalajs.react.hooks.Hooks._
import japgolly.scalajs.react.internal.Box
import org.scalajs.dom._
import scala.annotation.tailrec
import scala.language.`3.0`
import scala.scalajs.js

object RewritePoC {
  // console.log("object RewritePoC")

  inline def start[P] = new Step0[P]

  @inline final class Step0[P] {
    def useState[S](i: => S): Step1[P, UseState[S]] =
      new Step1
  }

  @inline final class Step1[P, A] {
    def useState[S](i: => S): Step2[P, A, UseState[S]] =
      new Step2

    inline def render(inline f: (P, A) => String): JsComp[P] =
      ${ renderMacro[P]('this, 'f) }
  }

  @inline final class Step2[P, A, B] {
    inline def render(inline f: (P, A, B) => String): JsComp[P] =
      ${ renderMacro[P]('this, 'f) }
  }

  type JsComp[P] = js.Function1[P, String]

  // ===================================================================================================================

  import scala.quoted.*

  def renderMacro[P](thiz: Expr[Any], render: Expr[Any])(using qq: Quotes, P: Type[P]): Expr[JsComp[P]] = {
    import quotes.reflect.*

    type InitStep = UntypedValDef.WithQuotes[qq.type]

    case class DslStep(method: String, types: List[TypeTree], args: List[Term]) {
      override def toString = args.mkString(s".$method(", ", ", ")")
    }

    def breakIntoSteps(term: Term): List[DslStep] = {
      var steps = List.empty[DslStep]
      @tailrec
      def go(t: Term): Unit =
        t match {

          // step 1+
          case Apply(TypeApply(Select(inner, method), types), args) =>
            steps ::= DslStep(method, types, args)
            go(inner)

          // step 0
          case Typed(Apply(TypeApply(Select(New(_), _), _), Nil), _) =>
            ()

          case x =>
            throw new RuntimeException(s"Failed to parse step:\n\n$x\n")
        }
      go(term)
      steps
    }

    val steps = breakIntoSteps(thiz.asTerm.underlying)

    def parseStep(i: Init {val q: qq.type}, step: DslStep, stepNo: Int): InitStep = {
      val prefix = "hook" + (stepNo + 1)
      step.method match {
        case "useState" =>
          step.types.head.asType match {
            case '[t] =>
              val arg = step.args.head.asExprOf[t]
              val hook = i.valDef('{ facade.React.useStateFn(() => Box[t]($arg)) }, prefix + "js")
              i.valDef('{ UseState.fromJsBoxed[t](${hook.ref}) }, prefix).untyped
          }
      }
    }

    def withHooks[A: Type](use: List[Term] => Expr[A]): Expr[A] = {
      val init      = Init("hook" + _)
      val initSteps = steps.zipWithIndex.map(parseStep(init, _, _))
      val args      = initSteps.map(_.ref: Term)
      val innerExpr = Expr.betaReduce(use(args))
      init.wrapExpr(innerExpr.asTerm.asExprOf[A])
    }

    val renderApplied: Expr[P] => Expr[String] =
      p => withHooks[String] { hookArgs =>
        val args = p.asTerm :: hookArgs
        val apply = Symbol.requiredClass("scala.Function" + args.size).declaredMethod("apply").head
        Apply(Select(render.asTerm, apply), args).asExprOf[String]
      }

    val result: Expr[JsComp[P]] = '{
      val jsFunction: JsComp[P] = (ppppppppppppppppppppppppppppppppp: P) => {
        org.scalajs.dom.console.log("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        ${ renderApplied('ppppppppppppppppppppppppppppppppp) }
      }
      jsFunction
    }

    // println()
    // println(result.asTerm.show(using Printer.TreeAnsiCode))
    // println()

    result
  }

  // ===================================================================================================================

  // @tailrec
  // def uninline[A](self: Expr[A])(using Quotes, Type[A]): Expr[A] = {
  //   import quotes.reflect.*
  //   self.asTerm match {
  //     case Inlined(_, _, term) => uninline(term.asExprOf[A])
  //     case _                   => self
  //   }
  // }

  // def simplifyExpr[A](self: Expr[A])(using Quotes, Type[A]): Expr[A] = {
  //   import quotes.reflect.*
  //   self.asTerm.simplify.asExprOf[A]
  //   self
  // }

}
