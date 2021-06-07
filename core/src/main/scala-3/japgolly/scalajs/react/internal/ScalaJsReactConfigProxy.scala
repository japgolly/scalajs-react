package japgolly.scalajs.react.internal

import japgolly.microlibs.compiletime.MacroEnv.*
import japgolly.scalajs.react.ScalaJsReactConfig
import japgolly.scalajs.react.ScalaJsReactConfig.*
import scala.quoted.*

// This is here in .internal because users are expected to import japgolly.scalajs.react._
// and I don't want this included as it's not public API.
object ScalaJsReactConfigProxy {

  transparent inline def automaticComponentName(name: String) = ${ _automaticComponentName('name) }
  transparent inline def modifyComponentName   (name: String) = ${ _modifyComponentName('name) }
  transparent inline def reusabilityOverride                  = ${ _reusabilityOverride }

  private def _automaticComponentName(name: Expr[String])(using Quotes) =
    call[String]("automaticComponentName", (name -> "java.lang.String") :: Nil)

  private def _modifyComponentName(name: Expr[String])(using Quotes) =
    call[String]("modifyComponentName", (name -> "java.lang.String") :: Nil)

  private def _reusabilityOverride(using Quotes) =
    call[ReusabilityOverride]("reusabilityOverride", Nil)

  private def call[A](methodName: String, args: List[(Expr[Any], String | Int)])(using Quotes, Type[A]): Expr[A] = {
    import quotes.reflect.*

    val defaultModule = '{Defaults}.inlined

    def resolve(moduleExpr: Expr[ScalaJsReactConfig]): Expr[A] = {
      val module =
        moduleExpr.asTerm

      val moduleSym =
        module
          .tpe
          .classSymbol
          .getOrElse(fail("Unknown config module: " + module.show))

      def name =
        moduleSym.fullName.stripSuffix("$") + "." + methodName

      val paramSigs =
        args.map(_._2)

      var candidateSyms =
        moduleSym.memberMethod(methodName)

      if (args.isEmpty) {
        val f = moduleSym.memberField(methodName)
        if (!f.isNoSymbol)
          candidateSyms ::= f
      }

      if (candidateSyms.isEmpty)
        fail(s"$name doesn't exist.")

      val methodSym =
        candidateSyms
          .iterator
          .filter(_.signature.paramSigs == paramSigs)
          .nextOption()
          .getOrElse {
            val sigs = candidateSyms.map(_.signature.paramSigs.mkString(", ")).sorted.map(p => s"  - $methodName($p)")
            fail(s"$name doesn't have the right signature. Found:\n" + sigs.mkString("\n"))
          }

      // If we determine that a user's custom config object doesn't override this method and it falls back to the
      // Defaults trait, call the method directly on the Defaults object instead so that it will be properly inlined;
      // otherwise Scala.JS will emit the Defaults trait and forward calls through it which also means that raw
      // arguments make it to the output JS and are transformed at runtime (rather than being transformed at
      // compile-time).
      var forceDefault = false
      if (moduleExpr ne defaultModule) {
        val owner = methodSym.maybeOwner
        if (owner.flags.is(Flags.Trait) && owner.fullName == "japgolly.scalajs.react.ScalaJsReactConfig$.Defaults")
          forceDefault = true
      }

      if (forceDefault)
        resolve(defaultModule)
      else {
        var callTerm: Term =
          Select(module, methodSym)

        if (args.nonEmpty)
          callTerm = Apply(callTerm, args.map(_._1.asTerm))

        Inlined(None, Nil, callTerm).asExprOf[A]
      }
    }

    resolve(
      CompileTimeConfig.quoted.getModule[ScalaJsReactConfig](Expr.inlineConst(KeyConfigClass))
        .getOrElse(defaultModule)
    )

  }
}
