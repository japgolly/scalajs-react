package issue972

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.ReactMonocle._
import japgolly.scalajs.react.vdom.html_<^._
import monocle._
import scala.language.`3.0`
import scala.scalajs.js.annotation._
import utest._

case class InnerStatus(cantRelyOn: Int, intsTheseDays: Int)
given Reusability[InnerStatus] = Reusability.never[InnerStatus]

object Outer {
  case class Status(innerStatus: InnerStatus)
  object Status {
    val innerStatus =
      Lens[Status, InnerStatus](_.innerStatus)(sp => _.copy(innerStatus = sp))
  }

  class Backend(scope: BackendScope[Unit, Status]) {
    val innerStatusStatusSnapshot =
      StateSnapshot.withReuse.zoomL(Status.innerStatus).prepareVia(scope)
    def render(u: Unit, status: Status) =
      Inner(innerStatusStatusSnapshot(status))
  }

  val component = ScalaComponent.builder[Unit]
    .initialState(Status(InnerStatus(12, 13)))
    .renderBackend[Backend]
    .build

  def apply() =
    component()
}

object Inner {
  case class Props(ss: StateSnapshot[InnerStatus])
  class Backend(scope: BackendScope[Props, Unit]) {
    def render(p: Props) =
      <.input(
        ^.onInput ==> { (e: ReactEventFromInput) =>
          p.ss.modState{ s =>
            val result = {
              if
                e.target.valueAsNumber >= 21
              then
                s.intsTheseDays
              else
                e.target.valueAsNumber
            }.toString
            e.target.value = result
            s.copy(intsTheseDays = result.toInt)
          }
        },
      )
  }

  val component = ScalaComponent.builder[Props]
    .stateless
    .renderBackend[Backend]
    .build

  def apply(ss: StateSnapshot[InnerStatus]) =
    component(Props(ss))
}

@JSExportTopLevel(name = "Init")
object Init {
  Outer()
}

// ===================================================================================================================

object Issue972 extends TestSuite {
  override def tests = Tests {
    "render" - assertRender(Outer(), "<input/>")
  }
}