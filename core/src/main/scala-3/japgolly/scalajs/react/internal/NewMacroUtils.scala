package japgolly.scalajs.react.internal

import scala.annotation.targetName
import scala.language.`3.0`
import scala.compiletime.*
import scala.quoted.*
import japgolly.microlibs.macro_utils.MacroUtils.fail

object NewMacroUtils { // TODO: Move into microlibs

  object Extensions {

    extension (unused: Expr.type) {
      def summonOrError[A](using Type[A])(using Quotes): Expr[A] = {
        import quotes.reflect.*
        Implicits.search(TypeRepr.of[A]) match {
          case iss: ImplicitSearchSuccess => iss.tree.asExpr.asInstanceOf[Expr[A]]
          case isf: ImplicitSearchFailure => report.throwError(isf.explanation)
        }
      }
    }

    extension [A](self: Type[A]) {
      inline def dealias(using Quotes): Type[A] =
        NewMacroUtils.dealias[A](using self)

      def summonOrError(using Quotes): Expr[A] =
        Expr.summonOrError[A](using self)
    }

    extension (using q: Quotes)(self: q.reflect.TypeRepr) {
      def asTypeTree: q.reflect.TypeTree =
        q.reflect.TypeTree.of(using self.asType)

      @targetName("summon_TypeRepr")
      def summon: Option[Expr[?]] =
        self.asType match {
          case '[a] => Expr.summon[a]
        }

      @targetName("summonOrError_TypeRepr")
      def summonOrError: Expr[?] =
        self.asType match {
          case '[a] => Type.of[a].summonOrError
        }
    } // TypeRepr

    extension (using q: Quotes)(self: q.reflect.TypeTree) {
      inline def asType: Type[?] =
        self.tpe.asType

      @targetName("summon_TypeTree")
      def summon: Option[Expr[?]] =
        self.tpe.summon

      @targetName("summonOrError_TypeTree")
      def summonOrError: Expr[?] =
        self.tpe.summonOrError
    } // TypeTree

    extension (using q: Quotes)(self: q.reflect.Symbol) {

      def getType: Option[q.reflect.TypeRepr] = {
        import q.reflect.*
        self.tree match {
          case ValDef(_, t, _)    => Some(t.tpe)
          case DefDef(_, _, t, _) => Some(t.tpe)
          case _                  => None
        }
      }

      inline def needType(inline s: String): q.reflect.TypeRepr =
        self.needType(_ => s)

      def needType(f: q.reflect.Symbol => String): q.reflect.TypeRepr =
        self.getType.getOrElse(fail(s"Unable to determine type of ${f(self)}"))
    } // Symbol
  }

  import Extensions.*

  def dealias[A](using Type[A])(using Quotes): Type[A] = {
    import quotes.reflect.*
    TypeRepr.of[A].dealias.asType.asInstanceOf[Type[A]]
  }

  def failNoStack(msg: String): Nothing = {
    val e = new RuntimeException(msg)
    e.setStackTrace(Array.empty)
    throw e
  }

  type FailFn       = () => Nothing
  type TermLookupFn = (q: Quotes) ?=> (q.reflect.ValDef , FailFn) => q.reflect.Term
  type TypeLookupFn = (q: Quotes) ?=> (q.reflect.TypeDef, FailFn) => q.reflect.TypeTree

  def newInstanceOf[A: Type](findTermArg          : Option[TermLookupFn] = None,
                             findTypeArg          : Option[TypeLookupFn] = None,
                             autoPopulateImplicits: Boolean              = true,
                           )(using Quotes): Expr[A] = {

    import quotes.reflect.*

    val A = TypeRepr.of[A].dealias.typeSymbol

    if A.flags.is(Flags.Abstract) then
      fail(s"${Type.show[A]} is abstract. It needs to be a concrete.")

    if A.flags.is(Flags.Trait) then
      fail(s"${Type.show[A]} is a trait. It needs to be a class.")

    val ctor = A.primaryConstructor

    def generateTypeArg(d: TypeDef): TypeTree = {
      val failFn: FailFn = () => fail(s"Don't know how to populate the type parameter ${d.name} in new ${Type.show[A]}[...]")
      findTypeArg match {
        case Some(f) => f(d, failFn)
        case None    => failFn()
      }
    }

    def generateTermArg(d: ValDef, isImplicit: Boolean): Term = {
      val failFn: FailFn = () => {
        if autoPopulateImplicits && isImplicit then {
          println()
          println()
          d.tpt.summonOrError
          println()
          println()
        }
        val pre = if isImplicit then "implicit " else ""
        fail(s"Don't know how to populate the parameter ($pre${d.name}: ${d.tpt.show}) in new ${Type.show[A]}(...)")
      }

      var result = Option.empty[Term]

      if autoPopulateImplicits && isImplicit then
        for (e <- d.tpt.summon)
          result = Some(e.asTerm)

      if result.isEmpty then
        for (f <- findTermArg)
          result = Some(f(d, failFn))

      result getOrElse failFn()
    }

    var typeArgs = List.empty[TypeTree]
    var termArgs = List.empty[List[Term]]

    def generateArgs(clauses: List[ParamClause]): Unit =
      clauses.foreach {
        // TODO: c.isImplicit || c.isGiven
        case c: TermParamClause => termArgs = termArgs ::: c.params.map(generateTermArg(_, isImplicit = c.isImplicit)) :: Nil
        case c: TypeParamClause => typeArgs = typeArgs ::: c.params.map(generateTypeArg)
      }

    // Extract args
    ctor.tree match {
      case DefDef(_, p, _, _) => generateArgs(p)
      case t                  => fail(s"Don't know how to interpret the constructor of ${Type.show[A]}\n$t")
    }

    val result: Term = {
      var classType: TypeTree =
        TypeTree.of[A]

      if findTypeArg.isDefined && typeArgs.nonEmpty then
        classType = Applied(classType, typeArgs)

      if termArgs.isEmpty then
        termArgs = Nil :: Nil // `new X` is translated to `new X()`

      var ast: Term =
        Select(New(classType), ctor)

      if findTypeArg.isDefined && typeArgs.nonEmpty then
        ast = TypeApply(ast, typeArgs)

      for (args <- termArgs)
        ast = Apply(ast, args)

      ast
    }

    result.asExprOf[A]
  }

}