package ghpages.examples.util

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object ErrorHandler {

  def apply(onError: ReactCaughtError => CallbackTo[VdomNode]): (=> VdomNode) => VdomElement =
    f => component(Props(() => f, onError))

  def pure(onError: ReactCaughtError => VdomNode): (=> VdomNode) => VdomElement =
    apply(onError andThen CallbackTo.pure)

  final case class Props(render: () => VdomNode, onError: ReactCaughtError => CallbackTo[VdomNode])

  val component = ScalaComponent.builder[Props]("ErrorHandler")
    .initialState[Option[VdomNode]](None)
    .render_PS((p, s) => s getOrElse p.render())
    .componentWillReceiveProps(_ setState None)
    .componentDidCatch($ => $.props.onError($.error).flatMap(n => $.setState(Some(n))))
    .build
}