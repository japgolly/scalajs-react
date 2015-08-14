package japgolly.scalajs.react.macros

import scala.reflect.macros.blackbox.Context
import japgolly.scalajs.react.extra.router2.StaticDsl.{Route, RouteB}

class RouterMacros (val c: Context) extends ReactMacroUtils {
  import c.universe._

  def quietCaseClass[T: c.WeakTypeTag]: c.Expr[Route[T]] = implCaseClass[Route, T](false)
  def debugCaseClass[T: c.WeakTypeTag]: c.Expr[Route[T]] = implCaseClass[Route, T](true)

  def quietCaseClassB[T: c.WeakTypeTag]: c.Expr[RouteB[T]] = implCaseClass[RouteB, T](false)
  def debugCaseClassB[T: c.WeakTypeTag]: c.Expr[RouteB[T]] = implCaseClass[RouteB, T](true)

  private def implCaseClass[R[_], T: c.WeakTypeTag](debug: Boolean): c.Expr[R[T]] = {
    val T       = concreteWeakTypeOf[T]
    val params  = primaryConstructorParams(T)
    val applyFn = tcApplyFn(T)

    def replaceMethod(newMethod: String) =
      c.macroApplication match {
        case TypeApply(Select(r, _), _) => Select(r, TermName(newMethod))
        case x => fail(s"Don't know how to parse macroApplication: ${showRaw(x)}")
      }

    def xmap  = replaceMethod("xmap")
    def const = replaceMethod("const")

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

    if (debug) println("\n" + impl + "\n")
    c.Expr[R[T]](impl)
  }
}
