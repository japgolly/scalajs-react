package japgolly.scalajs.react.component.builder

import japgolly.microlibs.compiletime.MacroEnv.*
import japgolly.scalajs.react.{Children, PropsChildren}
import japgolly.scalajs.react.component.builder.Lifecycle.RenderScope
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.vdom.VdomNode
import scala.language.`3.0`
import scala.quoted.*

object ComponentBuilderMacros {

  // These are defined in here instead of in ComponentBuilder to avoid a circular dependency
  type NewBackendFn[P, S, B] = BackendScope[P, S] => B
  type RenderFn    [P, S, B] = RenderScope[P, S, B] => VdomNode

  // ===================================================================================================================

  inline def newBackendFn[P, S, B]: NewBackendFn[P, S, B] =
    ${ newBackendFnImpl[P, S, B] }

  private def newBackendFnImpl[P: Type, S: Type, B: Type](using Quotes): Expr[NewBackendFn[P, S, B]] = {

    type Input = BackendScope[P, S]

    lazy val isBackendScope: (q: Quotes) ?=> q.reflect.TypeRepr => Boolean = {
      def monoName(using q2: Quotes)(t: q2.reflect.TypeRepr): String =
        t.dealias.show.takeWhile(_ != '[')

      val name = {
        import quotes.reflect.*
        monoName(TypeRepr.of[BackendScope[Unit, Unit]])
      }

      assert(name startsWith "japgolly.scalajs.react.")
      t => monoName(t) == name
    }

    def lambdaBody(input: Expr[Input]): Expr[B] = {
      MacroUtils.NewInstance.of[B](
        findTermArg = Some { (valDef, fail) =>
          import quotes.reflect.*
          val t = valDef.tpt.tpe.dealias
          if isBackendScope(t) then
            input.asTerm
          else
            fail()
        }
      )
    }

    '{ input => ${ lambdaBody('input) } }
  }

  // ===================================================================================================================

  def renderBackendFnImpl[P: Type, S: Type, B: Type](allowChildren: Boolean)(using Quotes): Expr[RenderFn[P, S, B]] =
    renderBackendFnImpl[P, S, B](allowChildren, renderParams[P, S, B])

  inline def renderBackendFn[P, S, B]: RenderFn[P, S, B] =
    ${ renderBackendFnImpl[P, S, B](allowChildren = false) }

  inline def renderBackendWithChildrenFn[P, S, B]: RenderFn[P, S, B] =
    ${ renderBackendFnImpl[P, S, B](allowChildren = true) }

  private def renderParams[P: Type, S: Type, B: Type](using Quotes): List[RenderParam[P, S, B, ?]] =
    new RenderParam[P, S, B, P](i => '{$i.props})("p", "props") ::
    new RenderParam[P, S, B, S](i => '{$i.state})("s", "state") ::
    Nil

//  def renderBackendSP[P: Type, Q: Type, S: Type, B: Type]: Tree =
//    _renderBackend[B](
//      new RenderParam[P]("props.static") ()             ::
//      new RenderParam[Q]("props.dynamic")("p", "props") ::
//      new RenderParam[S]("state")        ("s", "state") ::
//      Nil)

  private final class RenderParam[P, S, B, T: Type](val select: Quotes ?=> Expr[RenderScope[P, S, B]] => Expr[T])(_names: String*) {
    def spec    (using q: Quotes): q.reflect.TypeRepr = q.reflect.TypeRepr.of[T]
    def dealised(using q: Quotes): q.reflect.TypeRepr = spec.dealias
    val names                    : Set[String]        = _names.toSet
  }

  private def renderBackendFnImpl[P: Type, S: Type, B: Type](allowChildren: Boolean, rps: List[RenderParam[P, S, B, _]])
                                                            (using Quotes): Expr[RenderFn[P, S, B]] = {
    import quotes.reflect.*

    def assertChildrenTypeMatches(childrenUsed: Boolean): Unit =
      if (childrenUsed != allowChildren)
        fail {
          inline val pc = "PropsChildren"
          inline val rb = "renderBackend"
          inline val rbc = "renderBackendWithChildren"
          if (allowChildren)
            s"Use of $pc not detected. Use $rb instead of $rbc."
          else
            s"Use of $pc detected. Use $rbc instead of $rb."
        }

    type Input = RenderScope[P, S, B]

    def generateArguments(input: Expr[Input], params: List[Symbol]): List[Term] = {
      val PropsChildren = TypeRepr.of[PropsChildren]
      var childrenUsed = false

      def getPropsChildren(): Term = {
        childrenUsed = true
        '{ $input.propsChildren }.asTerm
      }

      def attempt(test: RenderParam[P, S, B, _] => Boolean): Option[Term] = {
        val it = rps.iterator.filter(test)
        if (it.isEmpty)
          None
        else {
          val rp = it.next()
          if (it.nonEmpty)
            None // Avoid ambiguity
          else
            Some(rp.select(input).asTerm)
        }
      }

      inline def tryPropsChildren(t: TypeRepr) = Option.when(t =:= PropsChildren)(getPropsChildren())
      inline def byExactTypeAlias(t: TypeRepr) = attempt(_.spec.typeSymbol == t.typeSymbol)
      inline def byExactType     (t: TypeRepr) = attempt(_.dealised.typeSymbol == t.typeSymbol)
      inline def bySubType       (t: TypeRepr) = attempt(_.dealised <:< t)
      inline def byName          (s: Symbol)   = attempt(_.names contains s.name.toString)

      val args = params.map[Term] { p =>
        val pt = p.needType(s"${Type.show[B]}.render param ${p.name}")
        val ptd = pt.dealias

        // println(s"${Type.of[B]}.render(${p.name}: ${pt.show}) -- ${p.tree}")

        (
          tryPropsChildren(pt) orElse
          byExactTypeAlias(pt) orElse
          byExactType(ptd)     orElse
          bySubType(ptd)       orElse
          byName(p)            getOrElse
          fail {
            var paramType = pt.show
            // Remove the "scala." prefix for types directly under scala.
            // This is primarily to make cross-compiled tests pass but it's also a bit nicer
            // for users too.
            if paramType.startsWith("scala.") && paramType.indexOf('.', 6) == -1 then
              paramType = paramType.drop(6)
            s"Don't know what to feed (${p.name}: $paramType) in ${Type.show[B]}.render"
          }
        )
      }

      assertChildrenTypeMatches(childrenUsed)

      args
    }

    val backendClass = TypeRepr.of[B].typeSymbol

    val renderMethod: Symbol = {
      var candidates = backendClass.memberMethod("render")
      backendClass.memberField("render") match {
        case f if !f.isNoSymbol => candidates ::= f
        case _ =>
      }
      candidates match {
        case Nil      => fail(s"${Type.show[B]} missing a render method.")
        case m :: Nil => m
        case _        => fail(s"${Type.show[B]} contains multiple public render methods.")
      }
    }

    val renderType = renderMethod.needType(s"${Type.show[B]}.render")

    val params: Option[List[Symbol]] = {
      val paramSymss = renderMethod.paramSymss

      if paramSymss.exists(_.exists(_.isTypeParam)) then
        fail(s"${Type.show[B]}.render mustn't contain type parameters.")

      paramSymss match {
        case Nil        => None
        case a :: Nil   => Some(a)
        case _          => fail(s"${Type.show[B]}.render mustn't have more than one set of parameters.")
      }
    }

    def lambdaBody(input: Expr[Input]): Expr[VdomNode] = {
      var render: Term = Select('{ $input.backend }.asTerm, renderMethod)
      params match {
        case None =>
          assertChildrenTypeMatches(false)
        case Some(params) =>
          val args = generateArguments(input, params)
          render = Apply(render, args)
      }
      render = render.implicitlyConvertToOrError[VdomNode]
      render.asExprOf[VdomNode]
    }

    val result: Expr[RenderFn[P, S, B]] = '{ input => ${ lambdaBody('input) } }
    // println(result.show)
    result
  }
}
