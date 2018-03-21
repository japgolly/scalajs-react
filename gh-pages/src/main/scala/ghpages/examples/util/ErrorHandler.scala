package ghpages.examples.util

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object ErrorHandler {

  type ReactError = raw.React.Error

  def apply(onError: ReactError => CallbackTo[VdomNode]): (=> VdomNode) => VdomElement =
    f => component(Props(() => f, onError))

  def pure(onError: ReactError => VdomNode): (=> VdomNode) => VdomElement =
    apply(onError andThen CallbackTo.pure)

  final case class Props(render: () => VdomNode, onError: ReactError => CallbackTo[VdomNode])

  val component = ScalaComponent.builder[Props]("ErrorHandler")
    .initialState[Option[VdomNode]](None)
    .render_PS((p, s) => s getOrElse p.render())
    .componentWillReceiveProps(_ setState None)
    .componentDidCatch($ => $.props.onError($.error).flatMap(n => $.setState(Some(n))))
    .build
}