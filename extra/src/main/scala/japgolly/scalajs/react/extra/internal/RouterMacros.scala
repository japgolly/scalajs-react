package japgolly.scalajs.react.extra.internal

import scala.reflect.macros.blackbox.Context
import japgolly.scalajs.react.extra.router.StaticDsl.{Route, RouteB}
import japgolly.scalajs.react.internal.MacroUtils

class RouterMacros (val c: Context) extends MacroUtils {
  import c.universe._

  def quietCaseClass[T: c.WeakTypeTag]: c.Expr[Route[T]] = implCaseClass[Route, T](false)
  def debugCaseClass[T: c.WeakTypeTag]: c.Expr[Route[T]] = implCaseClass[Route, T](true)

  def quietCaseClassB[T: c.WeakTypeTag]: c.Expr[RouteB[T]] = implCaseClass[RouteB, T](false)
  def debugCaseClassB[T: c.WeakTypeTag]: c.Expr[RouteB[T]] = implCaseClass[RouteB, T](true)

  private def implCaseClass[R[_], T: c.WeakTypeTag](debug: Boolean): c.Expr[R[T]] = {
    val T       = caseClassType[T]
    val params  = primaryConstructorParams(T)
    val applyFn = tcApplyFn(T)

    def xmap  = replaceMacroMethod("xmap")
    def const = replaceMacroMethod("const")

    val impl =
      params match {
        case Nil =>
          q"$const[$T]($applyFn())"

        case param :: Nil =>
          val (n, t) = nameAndType(T, param)
          q"$xmap[$T]($applyFn)(_.$n)"

        case _ =>
          var fromTuple = Vector.empty[Tree]
          var toTuple   = Vector.empty[Tree]
          var index     = 0
          for (p <- params) {
            index += 1
            val (n, t) = nameAndType(T, p)
            val tn = TermName("_" + index)
            fromTuple :+= q"t.$tn"
            toTuple   :+= q"c.$n"
          }
          q"$xmap[$T](t => $applyFn(..$fromTuple))(c => (..$toTuple))"
      }

    if (debug) println("\n" + showCode(impl) + "\n")
    c.Expr[R[T]](impl)
  }
}
