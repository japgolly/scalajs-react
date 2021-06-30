package sjr

import scala.meta._
import scalafix.v1._

class ProhibitDefaultEffects extends SemanticRule("ProhibitDefaultEffects") {
  import ProhibitDefaultEffects._

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      case a: Decl.Def   => check(a.decltpe)
      case a: Decl.Val   => check(a.decltpe)
      case a: Decl.Var   => check(a.decltpe)
      case a: Defn.Def   => check(a.decltpe)
      case a: Defn.Val   => check(a.decltpe)
      case a: Defn.Var   => check(a.decltpe)
      case a: Term.Param => check(a.decltpe)
    }.asPatch
  }

  private def check(o: Option[Type])(implicit doc: SemanticDocument): Patch =
    o.fold(Patch.empty)(check(_))

  private def check(t: Type)(implicit doc: SemanticDocument): Patch =
    if (t.symbol.value startsWith "japgolly/scalajs/react/util/DefaultEffects")
      Patch.lint(DefaultEffectDetected(t.pos))
    else
      Patch.empty
}

object ProhibitDefaultEffects {
  final case class DefaultEffectDetected(override val position: Position) extends Diagnostic {
    override def message = "This will cause a linking error."
  }
}
