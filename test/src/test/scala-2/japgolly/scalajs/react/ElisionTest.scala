/*
package japgolly.scalajs.react

import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import utest._

// Checked with bin/test-elision via Travis CI
object ElisionTest extends TestSuite {

  ScalaJsReactConfig.componentNameModifierAppend(_.stripPrefix("japgolly.scalajs.react."))

  private val Normal =
    ScalaComponent.builder[Int]("COMP_NAMES_ARE_ELIDABLE").render_P(<.div(_)).build

  private val Static =
    ScalaComponent.builder.static("COMP_NAMES_ARE_ELIDABLE")(<.div).build

  private object reusabilityOverride extends ScalaJsReactConfig.DevOnly.ReusabilityOverride {
    override def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot] =
      Reusability.shouldComponentUpdateAnd(_ => Callback.log("REUSABILITY_OVERRIDE_TEST"))
  }

  private object ELIDABLE_AUTO_COMPONENT_NAME {
    val Component =
      ScalaComponent.builder[Unit]
        .stateless
        .noBackend
        .render_(<.div)
        .build
  }

  private val attr =
    VdomAttr.elidable("data-ELIDABLE_VDOM_ATTR")

  override def tests = Tests {
    "normal" - s"[${Normal.raw.displayName}]"
    "static" - s"[${Static.raw.displayName}]"
    "attr"   - ReactDOMServer.renderToStaticMarkup(<.div(attr := 1))

    "reusabilityOverride" - {
      ScalaJsReactConfig.DevOnly.overrideReusability(reusabilityOverride)
      val x = ScalaComponent.builder[Int]("").render_P(<.div(_)).configure(Reusability.shouldComponentUpdate).build
      ScalaJsReactConfig.DevOnly.removeReusabilityOverride()
      x
    }

    "autoName" - {
      var expect = "ELIDABLE_AUTO_X" // split cos we don't want bin/test-elision to detect the string here in the test
      expect = if (TestEnv.fullCI) "" else "ElisionTest." + expect.replaceFirst(".$", "COMPONENT_NAME")
      assertEq(ELIDABLE_AUTO_COMPONENT_NAME.Component.displayName, expect)
    }
  }

}
*/