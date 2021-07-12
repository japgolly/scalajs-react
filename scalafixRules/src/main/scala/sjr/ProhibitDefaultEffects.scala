package sjr

import scala.meta._
import scalafix.v1._
import Util._

class ProhibitDefaultEffects extends SemanticRule("ProhibitDefaultEffects") {
  import ProhibitDefaultEffects._

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {

      // case Defn.Def(_, name, _, _, None, body)                 => checkImplicit(name, body)
      // case Defn.Val(_, Pat.Var(name) :: Nil, None, body)       => checkImplicit(name, body)
      // case Defn.Var(_, Pat.Var(name) :: Nil, None, Some(body)) => checkImplicit(name, body)

      case a: Decl.Def   => checkExplicit(a.decltpe)
      case a: Decl.Val   => checkExplicit(a.decltpe)
      case a: Decl.Var   => checkExplicit(a.decltpe)
      case a: Defn.Def   => checkExplicit(a.decltpe)
      case a: Defn.Val   => checkExplicit(a.decltpe)
      case a: Defn.Var   => checkExplicit(a.decltpe)
      case a: Term.Param => checkExplicit(a.decltpe)
    }.asPatch
  }

  // private def checkImplicit(name: Name, body: Term)(implicit doc: SemanticDocument): Patch = {
  // }

  private def checkExplicit(o: Option[Type])(implicit doc: SemanticDocument): Patch =
    o.fold(Patch.empty)(checkExplicit(_))

  private def checkExplicit(t: Type)(implicit doc: SemanticDocument): Patch =
    if (isDefaultEffect(t.symbol))
      Patch.lint(DefaultEffectDetected(t.pos))
    else
      Patch.empty
}

object ProhibitDefaultEffects {
  final case class DefaultEffectDetected(override val position: Position) extends Diagnostic {
    override def message = "This will cause a linking error."
  }
}
