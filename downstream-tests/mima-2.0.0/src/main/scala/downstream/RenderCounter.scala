package downstream

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object RenderCounter {

  def defaultPrefix: String =
    ""

  def apply(subject: Reusable[Any], prefix: String = defaultPrefix) =
    Component(Props(subject, prefix))

  final case class Props(subject: Reusable[Any], prefix: String = defaultPrefix) {
    @inline def render: VdomElement = Component(this)
  }

  implicit val reusabilityProps: Reusability[Props] =
    Reusability.derive

  final class Backend {
    private var count = 0

    def render(p: Props): VdomNode = {
      count += 1
      <.div(p.prefix, count)
    }
  }

  val Component = ScalaComponent.builder[Props]
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdateAndLog("X"))
    .build
}