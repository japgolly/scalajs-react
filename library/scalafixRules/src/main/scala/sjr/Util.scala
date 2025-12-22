package sjr

import scala.meta._
import scalafix.v1._

object Util {
  def isDefaultEffect(s: Symbol): Boolean =
    s.value startsWith "japgolly/scalajs/react/util/DefaultEffects"

  def isEffectTC(s: Symbol): Boolean =
    s.value startsWith "japgolly/scalajs/react/util/Effect."

  def effectTCType(tpe: Type)(implicit doc: SemanticDocument): Option[Name] =
    tpe match {
      case Type.Apply(t, (n: Name) :: Nil) => Option.when(isEffectTC(t.symbol))(n)
      case _                               => None
    }

  def isImplicit(mods: List[Mod]): Boolean =
    mods.exists {
      case _: Mod.Implicit => true
      case _               => false
    }
}
