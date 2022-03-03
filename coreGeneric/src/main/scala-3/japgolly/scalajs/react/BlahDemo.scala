package japgolly.scalajs.react

import scala.language.`3.0`
import japgolly.microlibs.compiletime.MacroEnv._
import japgolly.microlibs.compiletime._
import org.scalajs.dom._
import scala.scalajs.js
import scala.annotation.tailrec

object BlahDemo {
  // console.log("object BlahDemo")

  def start = new Step1

  final class Step1 {
    def useState(i: Int): Step2 =
      new Step2(i)
  }

  type Props = Int
  type JsComp = js.Function1[Props, String]
  type RenderArg = (Props, Int) => String

  final class Step2(val i: Int) {
    inline def render(inline f: RenderArg): JsComp =
      ${ renderMacro('this, 'f) }

    // inline def render(f: Int => String): JsComp =
    //   () => {
    //     console.log("DSL RENDER START")
    //     // val x = facade.React.useStateValue(i)
    //     // f(x._1)
    //     f(i)
    //   }
  }

  import scala.quoted.*

  def renderMacro(thiz: Expr[Step2], render: Expr[RenderArg])(using q1: Quotes): Expr[JsComp] = {
    import quotes.reflect.*
    implicit def ppp: Printer[Tree] = Printer.TreeAnsiCode

    def println(args: Any*) = ()

    println()
    println(">"*120)
    println()

    // Apply(Select(Select(Ident(BlahDemo),start),useState),List(Literal(Constant(123456))))
    println(thiz.asTerm.underlying)

    // japgolly.scalajs.react.BlahDemo.start.useState(123456)
    println(thiz.asTerm.underlying.show)

    val init = Init("hook" + _)
    // var renderArgs = List.empty[Ident]

    def parseDsl(method: String, args: List[Term]) = {
      println()
      println("method: " + method)
      println("  args: " + args)
      method match {
        case "useState" =>
          val arg = args.head.asExpr.asExprOf[Int]
          val vd  = init.valDef('{ facade.React.useStateValue($arg) })
          ///println("vd: " + vd.)
          vd

          // renderArgs :+= .untyped.ref
      }
    }

    val ivd =
    thiz.asTerm.underlying match {
      case Apply(Select(Select(Ident(_),"start"),method), args) =>
        parseDsl(method, args)
    }

    // uninlined render fn
    /*
    Block(
      List(
        DefDef(
          $anonfun,
          List(
            List(
              ValDef(
                i,
                TypeTree[TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),Int)],
                EmptyTree
              )
            )
          ),
          TypeTree[TypeRef(TermRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),Predef),String)],
          Block(
            List(
              Apply(Select(Select(Select(Select(Select(Ident(org),scalajs),dom),package),console),log),List(Literal(Constant(BlahDemo1 render)), Typed(SeqLiteral(List(),TypeTree[TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),Any)]),TypeTree[AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class <repeated>),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),Any)))])))),Apply(Select(Apply(Select(Literal(Constant(Hello )),+),List(Ident(i))),+),List(Literal(Constant( !!))))))),
              Closure(List(),Ident($anonfun),EmptyTree)
            )
    */

    println()
    uninline(render).asTerm match {
      // TODO: Handle multiple statements
      case Block(List(dd@ DefDef(name, _, _, _)), Closure(Ident(name2), _)) if name == name2 =>
        println("YES "*50)
        println()
        println("dd.paramss: " + dd.paramss)
        println()
        println("dd.rhs: " + dd.rhs)
        // println()
        // println(Expr.betaReduce('{ ${dd.asExprOf[RenderArg] }(123456) }))
        println()
      // case _ =>
      //   println("NO "*50)
    }

    // println()
    // println(ivd.valDef)
    // println(init.stmts)
    // println()
    // println(render.show)
    // println(render.asTerm)
    // println("-"*120)
    // // val render1 = '{$render(1)}
    // val render1 = simplifyExpr(uninline(render))
    // println(render1.show)
    // println(render1.asTerm)
    // println("!"*120)
    // println(ivd.use(render(_)).show)
    // println()
    // println(ivd.use(render(_)).asTerm)
    // println("!"*120)

    // val i = renderArgs.head.asExpr.asExprOf[Int]
    // val inner = init.wrapExpr[String] {
    //   '{
    //     $render($i)
    //     null
    //   }
    // }

    // $render($thiz.i)

    val renderApplied = (p: Expr[Props]) =>
      ivd.use(hook => render(p, '{ $hook._1 }))

    // def renderApplied(p: Quotes ?=> Expr[Props])(using Quotes): Expr[String] =
    //   ivd.use(render(p, _))

    // TODO: weird that init.wrap doesn't work

    val result: Expr[JsComp] =

    '{
      val jsFunction: JsComp = (p: Props) => {
        // console.log("DSL RENDER START")
        // val wtf = facade.React.useStateValue(123987654)
        ${ renderApplied('p) }
      }
      jsFunction
    }


    println()
    println(result.asTerm.show)
    println()
    println(result.asTerm.show(using Printer.TreeStructure))

    println()
    println("<"*120)
    println()

    result
  }

  // def mkResult(f: Quotes ?=> (Expr[Props]) => Expr[String])(using q2: Quotes): Expr[JsComp] = '{
  //   val jsFunction = (p: Props) => {
  //     // console.log("DSL RENDER START")
  //     val wtf = facade.React.useStateValue(123987654)
  //     ${ f('p) }
  //   }
  //   jsFunction
  // }

  @tailrec
  def uninline[A](self: Expr[A])(using Quotes, Type[A]): Expr[A] = {
    import quotes.reflect.*
    self.asTerm match {
      case Inlined(_, _, term) => uninline(term.asExprOf[A])
      case _                   => self
    }
  }

  def simplifyExpr[A](self: Expr[A])(using Quotes, Type[A]): Expr[A] = {
    import quotes.reflect.*
    self.asTerm.simplify.asExprOf[A]
    self
  }

}
