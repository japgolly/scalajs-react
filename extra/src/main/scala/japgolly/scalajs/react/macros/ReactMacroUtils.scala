package japgolly.scalajs.react.macros

import scala.reflect.macros.blackbox.Context

object ReactMacroUtils {

  def fail(c: Context, msg: String): Nothing =
    c.abort(c.enclosingPosition, msg)

  def concreteWeakTypeOf[T: c.WeakTypeTag](c: Context): c.universe.Type = {
    val t = c.universe.weakTypeOf[T]
    ensureConcrete(c)(t)
    t
  }

  def ensureConcrete(c: Context)(t: c.universe.Type): Unit = {
    val sym = t.typeSymbol.asClass
    if (sym.isAbstract)
      fail(c, s"ensureConcrete: [${sym.name}] is abstract which is not allowed.")
    if (sym.isTrait)
      fail(c, s"ensureConcrete: [${sym.name}] is a trait which is not allowed.")
    if (sym.isSynthetic)
      fail(c, s"ensureConcrete: [${sym.name}] is synthetic which is not allowed.")
  }

  def primaryConstructorParams[T: c.WeakTypeTag](c: Context): List[c.universe.Symbol] = {
    import c.universe._
    val T = weakTypeOf[T]
    T.decls
      .collectFirst { case m: MethodSymbol if m.isPrimaryConstructor => m }
      .getOrElse(fail(c, "Unable to discern primary constructor."))
      .paramLists
      .headOption
      .getOrElse(fail(c, "Primary constructor missing paramList."))
  }

  def nameAndType[T: c.WeakTypeTag](c: Context)(s: c.universe.Symbol): (c.universe.TermName, c.universe.Type) = {
    import c.universe._
    val T = weakTypeOf[T]

    def paramType(name: TermName): Type =
      T.decl(name).typeSignatureIn(T) match {
        case NullaryMethodType(t) => t
        case t                    => t
      }

    val a = s.asTerm.name
    val A = paramType(a)
    (a, A)
  }
}