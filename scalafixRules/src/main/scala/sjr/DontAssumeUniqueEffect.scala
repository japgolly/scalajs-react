package sjr

import scala.annotation.tailrec
import scala.meta._
import scalafix.lint.LintSeverity
import scalafix.v1._

class DontAssumeUniqueEffect extends SemanticRule("DontAssumeUniqueEffect") {
  import DontAssumeUniqueEffect._
  import Util._

  override def fix(implicit doc: SemanticDocument): Patch =
    doc.tree.collect {
      case a: Decl.Def => check(a.mods, a.name, a.tparams, a.paramss, a.pos, a.parent)
      case a: Defn.Def => check(a.mods, a.name, a.tparams, a.paramss, a.pos, a.parent)
    }.asPatch

  private def ignorableMethodName(methodName: Term.Name): Boolean = {
    val m = methodName.value
    (
      m.startsWith("with")
      || m.endsWith("F")
      || m == "apply"
      || m == "mapped"
      || m == "subst"
      || m == "to"
    )
  }

  private def ignorableMods(mods: List[Mod]): Boolean =
    mods.exists {
      case _: Mod.Implicit => true
      case _: Mod.Private  => true
      case _               => false
    }

  @tailrec
  private def ignorableParent(parent: Option[Tree]): Boolean =
    parent.orNull match {
      case _: Decl.Type   => true
      case a: Defn.Class  => ignorableMods(a.mods) || ignorableParent(a.parent)
      case _: Defn.Def    => true
      case a: Defn.Object => ignorableMods(a.mods) || ignorableParent(a.parent)
      case a: Defn.Trait  => ignorableMods(a.mods) || ignorableParent(a.parent)
      case _: Defn.Type   => true
      case _: Defn.Val    => true
      case _: Defn.Var    => true
      case a: Template    => ignorableParent(a.parent)
      case _              => false
    }

  private def check(mods      : List[Mod],
                    methodName: Term.Name,
                    tparams   : List[Type.Param],
                    paramss   : List[List[Term.Param]],
                    pos       : Position,
                    parent    : Option[Tree],
                  )(implicit doc: SemanticDocument): Patch = {

    val ignorable = (
      tparams.isEmpty
      || paramss.isEmpty
      || ignorableMethodName(methodName)
      || ignorableMods(mods)
      || ignorableParent(parent)
    )

    if (!ignorable) {
      var types = List.empty[String]
      def addType(t: Name): Unit = {
        val s = t.value
        if (!types.contains(s))
          types ::= s
      }

      for (t <- tparams)
        if (t.tparams.nonEmpty) {

          // F[_]: Sync
          for (c <- t.cbounds)
            if (isEffectTC(c.symbol))
              addType(t.name)

          // (implicit F: Sync[F])
          for {
            ps <- paramss
            p <- ps
            t <- p.decltpe
            n <- effectTCType(t)
          } addType(n)
        }

      // Exclude cases where the F[_] can be inferred by other means
      types = types.filterNot { f =>
        val regex = s"(?:^|.+[, \\[])$f[\\[\\],].*".r

        paramss.exists { ps =>
          ps.exists { p =>
            p.decltpe.exists { t =>
              effectTCType(t).isEmpty && // Not looking for Sync[F]
              regex.matches(t.toString)  // Found usage of F
            }
          }
        }
      }

      if (types.nonEmpty) {
        // println(s"$types) $mods ${isImplicit(mods)} | $paramss")
        return Patch.lint(Found(types, pos))
      }
    }

    Patch.empty
  }
}

object DontAssumeUniqueEffect {
  final case class Found(types: List[String], override val position: Position) extends Diagnostic {
    val descTypes = types.sorted.mkString(", ")
    override def message = "Assumes a single, implicit effect: " + descTypes
    override def severity = LintSeverity.Warning
  }
}
