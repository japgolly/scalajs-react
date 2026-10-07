package japgolly.scalajs.react.feature

import japgolly.scalajs.react.{facade, Ref}
import japgolly.scalajs.react.util.Effect.Sync
import japgolly.scalajs.react.vdom._
import scala.scalajs.js
import scala.scalajs.js.|

/** Lets you animate a component tree with Transitions and Suspense.
  *
  * See https://react.dev/reference/react/ViewTransition
  *
  * @since 4.1.0 / React 19.3
  */
object ViewTransition {
  def newBuilder(): ViewTransitionBuilder =
    new ViewTransitionBuilder()

  type Callback[F[_]] = (facade.ViewTransition.Instance, js.Array[String]) => F[F[Unit]]
  type Callback_[F[_]] = (facade.ViewTransition.Instance, js.Array[String]) => F[Unit]
}

class ViewTransitionBuilder {
  import ViewTransition.{Callback, Callback_}

  private val props: facade.ViewTransitionProps =
    (new js.Object).asInstanceOf[facade.ViewTransitionProps]

  def name(value: String): this.type = {
    props.name = value
    this
  }

  def default(value: facade.ViewTransition.Class): this.type = {
    props.default = value
    this
  }

  def enter(value: facade.ViewTransition.Class): this.type = {
    props.enter = value
    this
  }

  def exit(value: facade.ViewTransition.Class): this.type = {
    props.exit = value
    this
  }

  def share(value: facade.ViewTransition.Class): this.type = {
    props.share = value
    this
  }

  def update(value: facade.ViewTransition.Class): this.type = {
    props.update = value
    this
  }

  private def convertCallback_[F[_]](f: Callback_[F])(implicit F: Sync[F]): facade.ViewTransition.Callback = {
    val jsFn: js.Function2[facade.ViewTransition.Instance, js.Array[String], Unit | js.Function0[Any]] =
    (instance, strings) => {
      val fx = f(instance, strings)
      F.runSync(fx)
    }
    jsFn
  }

  /** Callback does not return a cleanup function. */
  def onEnter_[F[_]: Sync](f: Callback_[F]): this.type = {
    props.onEnter = convertCallback_(f)
    this
  }

  /** Callback does not return a cleanup function. */
  def onExit_[F[_]: Sync](f: Callback_[F]): this.type = {
    props.onExit = convertCallback_(f)
    this
  }

  /** Callback does not return a cleanup function. */
  def onShare_[F[_]: Sync](f: Callback_[F]): this.type = {
    props.onShare = convertCallback_(f)
    this
  }

  /** Callback does not return a cleanup function. */
  def onUpdate_[F[_]: Sync](f: Callback_[F]): this.type = {
    props.onUpdate = convertCallback_(f)
    this
  }

  private def convertCallback[F[_]](f: Callback[F])(implicit F: Sync[F]): facade.ViewTransition.Callback = {
    val jsFn: js.Function2[facade.ViewTransition.Instance, js.Array[String], Unit | js.Function0[Any]] =
    (instance, strings) => {
      val fx = f(instance, strings)
      val g = F.runSync(fx)
      F.toJsFn(g): js.Function0[Any]
    }
    jsFn
  }

  /** Here you return a cleanup function. The cleanup function is called when the View Transition finishes, allowing you to cancel or cleanup any animations. */
  def onEnter[F[_]: Sync](f: Callback[F]): this.type = {
    props.onEnter = convertCallback(f)
    this
  }

  /** Here you return a cleanup function. The cleanup function is called when the View Transition finishes, allowing you to cancel or cleanup any animations. */
  def onExit[F[_]: Sync](f: Callback[F]): this.type = {
    props.onExit = convertCallback(f)
    this
  }

  /** Here you return a cleanup function. The cleanup function is called when the View Transition finishes, allowing you to cancel or cleanup any animations. */
  def onShare[F[_]: Sync](f: Callback[F]): this.type = {
    props.onShare = convertCallback(f)
    this
  }

  /** Here you return a cleanup function. The cleanup function is called when the View Transition finishes, allowing you to cancel or cleanup any animations. */
  def onUpdate[F[_]: Sync](f: Callback[F]): this.type = {
    props.onUpdate = convertCallback(f)
    this
  }

  def withRef[F[_]](ref: Ref.HandleF[F, facade.ViewTransition.Instance]): this.type = {
    props.ref = ref.raw
    this
  }

  def apply(children: VdomNode*): VdomElement =
    VdomElement(
      facade.React.createElementViewTransition(facade.React.ViewTransition, props, children.map(_.rawNode): _*))
}
