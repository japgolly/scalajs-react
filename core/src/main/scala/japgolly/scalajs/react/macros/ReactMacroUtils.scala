package japgolly.scalajs.react.macros

import scala.reflect.macros.blackbox.Context

abstract class ReactMacroUtils {
  val c: Context
  import c.universe._

  final def fail(msg: String): Nothing =
    c.abort(c.enclosingPosition, msg)

  final def warn(msg: String): Unit =
    c.warning(c.enclosingPosition, msg)

  final def concreteWeakTypeOf[T: c.WeakTypeTag]: Type = {
    val t = weakTypeOf[T]
    ensureConcrete(t)
    t
  }

  final def ensureConcrete(t: Type): Unit = {
    val sym = t.typeSymbol.asClass
    if (sym.isAbstract)
      fail(s"${sym.name} is abstract which is not allowed.")
    if (sym.isTrait)
      fail(s"${sym.name} is a trait which is not allowed.")
    if (sym.isSynthetic)
      fail(s"${sym.name} is synthetic which is not allowed.")
  }

  final def caseClassType[T: c.WeakTypeTag]: Type = {
    val t = concreteWeakTypeOf[T]
    ensureCaseClass(t)
    t
  }

  final def ensureCaseClass(t: Type): Unit = {
    val sym = t.typeSymbol.asClass
    if (!sym.isCaseClass)
      fail(s"${sym.name} is not a case class.")
  }

  final def primaryConstructorParams(t: Type): List[Symbol] =
    t.decls
      .collectFirst { case m: MethodSymbol if m.isPrimaryConstructor => m }
      .getOrElse(fail("Unable to discern primary constructor."))
      .paramLists
      .headOption
      .getOrElse(fail("Primary constructor missing paramList."))

  final def nameAndType(T: Type, s: Symbol): (TermName, Type) = {
    def paramType(name: TermName): Type =
      T.decl(name).typeSignatureIn(T) match {
        case NullaryMethodType(t) => t
        case t                    => t
      }

    val a = s.asTerm.name
    val A = paramType(a)
    (a, A)
  }

  /**
   * Create code for a function that will call .apply() on a given type's type companion object.
   */
  final def tcApplyFn(t: Type): Select = {
    val sym = t.typeSymbol
    val tc  = sym.companion
    if (tc == NoSymbol)
      fail(s"Companion object not found for $sym")
    val pre = t match {
      case TypeRef(p, _, _) => p
      case x                => fail(s"Don't know how to extract `pre` from ${showRaw(x)}")
    }

    pre match {
      // Path dependent, eg. `t.Literal`
      case SingleType(NoPrefix, path) =>
        Select(Ident(path), tc.asTerm.name)

      // Assume type companion .apply exists
      case _ =>
        Select(Ident(tc), TermName("apply"))
    }
  }

  final def tryInferImplicit(t: Type): Option[Tree] =
    c.inferImplicitValue(t, silent = true) match {
      case EmptyTree => None
      case i         => Some(i)
    }

  final def needInferImplicit(t: Type): Tree =
    tryInferImplicit(t) getOrElse fail(s"Implicit not found: $t")

  final def replaceMacroMethod(newMethod: String) =
    c.macroApplication match {
      case TypeApply(Select(r, _), _) => Select(r, TermName(newMethod))
      case Select(r, _)               => Select(r, TermName(newMethod))
      case x => fail(s"Don't know how to parse macroApplication: ${showRaw(x)}")
    }

  final def readMacroArg_symbol(e: c.Expr[scala.Symbol]): String =
    e match {
      case Expr(Apply(_, List(Literal(Constant(n: String))))) => n
      case _ => fail(s"Expected a symbol, got: ${showRaw(e)}")
    }

  final def excludeNamedParams(exclusions: Seq[String], data: List[(TermName, Type)]): List[(TermName, Type)] =
    if (exclusions.isEmpty)
      data
    else {
      var blacklist = Set.empty[String]
      var bsize = 0
      for (s <- exclusions) {
        if (blacklist contains s)
          fail(s"Duplicate found: $s")
        blacklist += s
        bsize += 1
      }

      def name(x: (TermName, Type)): String =
        x._1.decodedName.toString

      val b = List.newBuilder[(TermName, Type)]
      var excluded = 0
      for (x <- data)
        if (blacklist contains name(x))
          excluded += 1
        else
          b += x

      if (bsize != excluded) {
        val x = blacklist -- data.map(name)
        fail(s"Not found: ${x mkString ", "}")
      }

      b.result()
    }

  final def primaryConstructorParamsExcluding(t: Type, exclusions: Seq[c.Expr[scala.Symbol]]): List[(TermName, Type)] =
    excludeNamedParams(
      exclusions.map(readMacroArg_symbol),
      primaryConstructorParams(t).map(nameAndType(t, _)))
}