package japgolly.scalajs.react.macros

import scala.reflect.macros.blackbox.Context
import japgolly.scalajs.react.extra.router2.StaticDsl.{Route, RouteB}
import ReactMacroUtils._

object RouterMacros {

  def quietCaseClass[T: c.WeakTypeTag](c: Context): c.Expr[Route[T]] = implCaseClass[Route, T](c, false)
  def debugCaseClass[T: c.WeakTypeTag](c: Context): c.Expr[Route[T]] = implCaseClass[Route, T](c, true)

  def quietCaseClassB[T: c.WeakTypeTag](c: Context): c.Expr[RouteB[T]] = implCaseClass[RouteB, T](c, false)
  def debugCaseClassB[T: c.WeakTypeTag](c: Context): c.Expr[RouteB[T]] = implCaseClass[RouteB, T](c, true)

  private def implCaseClass[R[_], T: c.WeakTypeTag](c: Context, debug: Boolean): c.Expr[R[T]] = {
    import c.universe._

    val T       = concreteWeakTypeOf[T](c)
    val params  = primaryConstructorParams(c)
    val applyFn = tcApplyFn(c)(T)

    def replaceMethod(newMethod: String) =
      c.macroApplication match {
        case TypeApply(Select(r, _), _) => Select(r, TermName(newMethod))
        case x => fail(c, s"Don't know how to parse macroApplication: ${showRaw(x)}")
      }

    def xmap  = replaceMethod("xmap")
    def const = replaceMethod("const")

    val impl =
      params match {
        case Nil =>
          q"$const[$T]($applyFn())"

        case param :: Nil =>
          val (n, t) = nameAndType(c)(param)
          q"$xmap[$T]($applyFn)(_.$n)"

        case _ =>
          var fromTuple = Vector.empty[Tree]
          var toTuple   = Vector.empty[Tree]
          var index     = 0
          for (p <- params) {
            index += 1
            val (n, t) = nameAndType[T](c)(p)
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
