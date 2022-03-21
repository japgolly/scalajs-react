package sjr

import scalafix.v1._

class ScalaJsReactEffectAgnosticism extends SemanticRule("ScalaJsReactEffectAgnosticism") {

  private[this] val prohibitDefaultEffects = new ProhibitDefaultEffects

  override def fix(implicit doc: SemanticDocument): Patch =
    prohibitDefaultEffects.fix
}
