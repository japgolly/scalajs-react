package japgolly.scalajs.react

import japgolly.scalajs.react.vdom.html_<^._
import utest._

// Checked with bin/test-elision via Travis CI
object ElisionTest extends TestSuite {

  private val Normal =
    ScalaComponent.builder[Int]("COMP_NAMES_ARE_ELIDABLE").render_P(<.div(_)).build

  private val Static =
    ScalaComponent.builder.static("COMP_NAMES_ARE_ELIDABLE")(<.div).build

  private object reusabilityOverride extends ScalaJsReactDevConfig.ReusabilityOverride {
    override def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot] =
      Reusability.shouldComponentUpdateAnd(_ => Callback.log("REUSABILITY_OVERRIDE_TEST"))
  }


  override def tests = Tests {
    "normal" - s"[${Normal.raw.displayName}]"
    "static" - s"[${Static.raw.displayName}]"

    "reusabilityOverride" - {
      ScalaJsReactDevConfig.overrideReusability(reusabilityOverride)
      ScalaComponent.builder[Int]("").render_P(<.div(_)).configure(Reusability.shouldComponentUpdate).build
    }
  }

}
