package japgolly.scalajs.react.internal

import scala.annotation.targetName
import scala.language.`3.0`
import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*
import japgolly.microlibs.macro_utils.MacroUtils
import japgolly.microlibs.macro_utils.MacroUtils.Ops.*
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

      def inlineTermOf[A](using Type[A])(using q: Quotes)(t: q.reflect.Term): Expr[A] = {
        import quotes.reflect.*
        Inlined(None, Nil, t).asExprOf[A]
      }

      // TODO: add all primatives
      def inlineConstStr(s: String)(using Quotes): Expr[String] =
        inlineConstStrOrNull(s)

      def inlineConstStrOrNull(s: String | Null)(using Quotes): Expr[String | Null] = {
        import quotes.reflect.*
        val const = if s == null then NullConstant() else StringConstant(s)
        Expr.inlineTermOf[String | Null](Literal(const))
      }

      // def productToTuple[P](using m: Mirror.ProductOf[P]): Expr[P => m.MirroredElemTypes] =
      //   1

      // def tupleToProduct[P](using m: Mirror.ProductOf[P]): Expr[m.MirroredElemTypes => P] =
      //   1
    }

    extension [A](self: Type[A]) {
      inline def dealias(using Quotes): Type[A] =
        NewMacroUtils.dealias[A](using self)

      def summonOrError(using Quotes): Expr[A] =
        Expr.summonOrError[A](using self)
    }

    extension (using q: Quotes)(self: q.reflect.Term) {
      def implicitlyConvertTo[B](using Type[B]): Option[q.reflect.Term] = {
        import quotes.reflect.*
        if self.tpe <:< TypeRepr.of[B] then
          Some(self)
        else
          self.tpe.asType match {
            case '[t] =>
              Expr.summon[t => B].map { i =>
                val e = self.asExprOf[t]
                Expr.betaReduce('{ $i($e) }).asTerm
              }
          }
      }

      def implicitlyConvertToOrError[B](using Type[B]): q.reflect.Term =
        self.implicitlyConvertTo[B].getOrElse {
          val a = self.tpe.show
          val msg = s"Can't convert $a to ${Type.show[B]}"
          import quotes.reflect.*
          report.throwError(msg, Position.ofMacroExpansion)
        }
    }

    extension (using q: Quotes)(self: q.reflect.TypeRepr) {
      def asTypeTree: q.reflect.TypeTree =
        q.reflect.TypeTree.of(using self.asType)

      def exists: Boolean = {
        import q.reflect.*
        self <:< TypeRepr.of[AnyRef] || self <:< TypeRepr.of[AnyVal]
      }

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

      /** Oldest returned first */
      def ownerPath: List[q.reflect.Symbol] = {
        import q.reflect.*
        def loop(s: Symbol, acc: List[Symbol]): List[Symbol] =
          if s.isNoSymbol then
            acc
          else
            loop(s.maybeOwner, s :: acc)
        if self.isNoSymbol then
          self :: Nil
        else
          loop(self, Nil)
      }

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

  object CompileTimeString {

    transparent inline def replaceFirst(str: String, regex: String, repl: String): String =
      ${ replaceFirst('str, 'regex, 'repl) }

    def replaceFirst(str: Expr[String], regex: Expr[String], repl: Expr[String])(using Quotes): Expr[String] =
      (str.value, regex.value, repl.value) match {
        case (Some(n), Some(r), Some(p)) => Expr(n.replaceFirst(r, p))
        case _                           => '{ $str.replaceFirst($regex, $repl) }
      }

    transparent inline def replaceAll(str: String, regex: String, repl: String): String =
      ${ replaceAll('str, 'regex, 'repl) }

    def replaceAll(str: Expr[String], regex: Expr[String], repl: Expr[String])(using Quotes): Expr[String] =
      (str.value, regex.value, repl.value) match {
        case (Some(n), Some(r), Some(p)) => Expr(n.replaceAll(r, p))
        case _                           => '{ $str.replaceAll($regex, $repl) }
      }

    transparent inline def trim(str: String): String =
      ${ trim('str) }

    def trim(str: Expr[String])(using Quotes): Expr[String] =
      str.value match {
        case Some(s) => Expr(s.trim)
        case None    => '{ $str.trim }
      }

    transparent inline def toLowerCase(str: String): String =
      ${ toLowerCase('str) }

    def toLowerCase(str: Expr[String])(using Quotes): Expr[String] =
      str.value match {
        case Some(s) => Expr(s.toLowerCase)
        case None    => '{ $str.toLowerCase }
      }

    transparent inline def toUpperCase(str: String): String =
      ${ toUpperCase('str) }

    def toUpperCase(str: Expr[String])(using Quotes): Expr[String] =
      str.value match {
        case Some(s) => Expr(s.toUpperCase)
        case None    => '{ $str.toUpperCase }
      }
  }

  object CompileTimeConfig {

    private def _getOrNull(key: String)(using Quotes): String | Null =
      // TODO: [3] Read compile-time env
      System.getProperty(key, null)

    transparent inline def getOrNull(inline key: String): String | Null =
      ${ getOrNull('key) }

    def getOrNull(key: Expr[String])(using Quotes): Expr[String | Null] = {
      import quotes.reflect.*
      val value = _getOrNull(key.valueOrError)
      Expr.inlineConstStrOrNull(value)
    }

    transparent inline def get(inline key: String): Option[String] =
      inline getOrNull(key) match {
        case null => None
        case v    => Some[v.type](v)
      }

    transparent inline def getTrimLowerCaseNonBlank(inline key: String): Option[String] =
      inline get(key) match {
        case None => None
        case Some(v) => inline CompileTimeString.trim(CompileTimeString.toLowerCase(v)) match {
          case "" => None
          case v2 => Some[v2.type](v2)
        }
      }

    transparent inline def getNonBlank(inline key: String): Option[String] =
      inline get(key) match {
        case None     => None
        case Some("") => None
        case Some(v)  => Some[v.type](v)
      }

    def apply[A: Type](key: String, fallback: Expr[A])(using Quotes): Expr[A] =
      _getOrNull(key) match {
        case null => fallback
        case v => forValue(v) match {
          case Right(expr) => expr
          case Left(err)   => fail(s"Invalid config: $key: '$v'\n$v $err.")
        }
      }

    def forValue[A: Type](value: String)(using Quotes): Either[String, Expr[A]] = {
      import quotes.reflect.*

      val mod = Symbol.requiredModule(value)
      val ref = Ref(mod)
      val tpe = ref.tpe

      if tpe <:< TypeRepr.of[A] then
        Right(Expr.inlineTermOf[A](ref))
      else if !tpe.exists then
        Left("doesn't exist")
      else
        Left("isn't an instance of " + Type.show[A])
    }

  }

  inline def inlineWarn(inline warning: String): Unit =
    ${ warn('warning) }

  private def warn(warning: Expr[String])(using Quotes): Expr[Unit] = {
    import quotes.reflect.*
    report.warning(warning.valueOrError)
    '{()}
  }

  trait Field extends MacroUtils.Field {
    override def toString = name

    val name: String

    final def typeRepr(using q: Quotes): q.reflect.TypeRepr =
      q.reflect.TypeRepr.of(using fieldType)
  }

  def mirrorFields[A: Type](m: Expr[Mirror.Of[A]])(using Quotes): List[Field] = {
    import quotes.reflect.*

    def go[Ls: Type, Ts: Type](idx: Int): List[Field] =
      (Type.of[Ls], Type.of[Ts]) match {
        case ('[l *: ll], '[t *: tt]) =>
          val t = Type.of[t]
          val _name = TypeRepr.of[l] match {
            case ConstantType(StringConstant(n)) => n
          }
          val _idx = idx
          val f: Field = new Field {
            override val idx                = _idx
            override val name               = _name
            override type Name              = l
            override type Type              = t
            override implicit val fieldType = t
          }
          f :: go[ll, tt](idx + 1)

        case ('[EmptyTuple], _) =>
          Nil
      }

    m match {
      case '{ $m: Mirror.ProductOf[A] { type MirroredElemLabels = ls; type MirroredElemTypes = ts }} =>
        go[ls, ts](0)
      case '{ $m: Mirror.SumOf[A] { type MirroredElemLabels = ls; type MirroredElemTypes = ts }} =>
        go[ls, ts](0)
    }
  }

  def show[A](e: Expr[A])(using Quotes): Expr[A] = {
    println(e.show)
    e
  }

  import MacroUtils.FieldLookup
  import MacroUtils.{mapByFieldTypes, needGiven} // TODO: remove

  def withCachedGivensPowerMode[F[_], A](using q: Quotes)
                                        (summonMap: Map[q.reflect.TypeRepr, Expr[F[Any]]])
                                        (use: FieldLookup[F] => Expr[A])
                                        (using Type[F], Type[A]): Expr[A] = {
    import quotes.reflect.*

    val summons = summonMap.toArray
    val terms = summons.iterator.map(_._2.asTerm).toList

    ValDef.let(Symbol.spliceOwner, terms) { refs =>
      val lookupFn: FieldLookup[F] =
        f => {
          def fieldType = TypeRepr.of(using f.fieldType)
          val i = summons.indexWhere(_._1 == fieldType)
          if i < 0 then {
            val t = Type.show[F[f.Type]]
            fail(s"Failed to find given $t in cache")
          }
          refs(i).asExprOf[F[f.Type]]
        }
      use(lookupFn).asTerm
    }.asExprOf[A]
  }

  def withCachedGivens[F[_]: Type, A: Type](fields: IterableOnce[Field])
                                           (use: FieldLookup[F] => Expr[A])
                                           (using Quotes): Expr[A] = {
    val summonMap = mapBySpecifiedFieldTypes(fields)(f => needGiven[F[f.Type]].asFAny)
    withCachedGivensPowerMode(summonMap)(use)
  }

  def withCachedGivens[A: Type, F[_]: Type, B: Type](m: Expr[Mirror.Of[A]])
                                                    (use: FieldLookup[F] => Expr[B])
                                                    (using Quotes): Expr[B] = {
    import quotes.reflect.*

    Expr.summon[Mirror.Of[A]] match {

      case Some('{ $m: Mirror.ProductOf[A] { type MirroredElemTypes = types } }) =>
        withCachedGivens(mirrorFields(m))(use)

      case Some('{ $m: Mirror.SumOf[A] { type MirroredElemTypes = types } }) =>
        val summonMap = mapByFieldTypes[types, Expr[F[Any]]]([t] => (t: Type[t]) ?=> needGiven[F[t]].asFAny)
        withCachedGivensPowerMode(summonMap)(use)

      case _ =>
        fail(s"Mirror not found for ${Type.show[A]}")
    }
  }

  def mapBySpecifiedFieldTypes[B](fields: IterableOnce[Field])(f: Field => B)(using q: Quotes): Map[q.reflect.TypeRepr, B] = {
    import quotes.reflect.*

    var map = Map.empty[TypeRepr, B]

    for (field <- fields.iterator) {
      val t = field.typeRepr
      if !map.contains(t) then {
        val b = f(field)
        map = map.updated(t, b)
      }
    }

    map
  }
}