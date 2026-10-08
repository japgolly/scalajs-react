package japgolly.scalajs.react.facade

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.|

/** Lets you animate a component tree with Transitions and Suspense.
  *
  * See https://react.dev/reference/react/ViewTransition
  *
  * @since 4.1.0 / React 19.3
  */
@JSImport("react", "ViewTransition", "React.ViewTransition")
@js.native
object ViewTransition extends js.Any {
  type Class         = String | js.Dictionary[String] | js.Object
  type Callback      = js.Function2[Instance, js.Array[String], Unit | js.Function0[Any]] |
                       js.Function1[Instance, Unit | js.Function0[Any]]
  type Instance      = ViewTransitionInstance
  type Props         = ViewTransitionProps
  type PseudoElement = ViewTransitionPseudoElement
}

@js.native
trait ViewTransitionPseudoElement extends js.Object {
  def animate(keyframes: js.Any, options: js.Any = js.native): js.Any = js.native
  def getAnimations(options: js.Object = js.native): js.Array[js.Any] = js.native
  def getComputedStyle(): dom.CSSStyleDeclaration = js.native
}

@js.native
trait ViewTransitionInstance extends js.Object {
  val name     : String                      = js.native
  val group    : ViewTransitionPseudoElement = js.native
  val imagePair: ViewTransitionPseudoElement = js.native
  val old      : ViewTransitionPseudoElement = js.native
  val `new`    : ViewTransitionPseudoElement = js.native
}

@js.native
trait ViewTransitionProps extends js.Object {
  var children : js.UndefOr[React.Node] = js.native
  var default  : js.UndefOr[ViewTransition.Class] = js.native
  var enter    : js.UndefOr[ViewTransition.Class] = js.native
  var exit     : js.UndefOr[ViewTransition.Class] = js.native
  var name     : js.UndefOr[String] = js.native
  var onEnter  : js.UndefOr[ViewTransition.Callback] = js.native
  var onExit   : js.UndefOr[ViewTransition.Callback] = js.native
  var onShare  : js.UndefOr[ViewTransition.Callback] = js.native
  var onUpdate : js.UndefOr[ViewTransition.Callback] = js.native
  var ref      : js.UndefOr[React.RefFn[ViewTransitionInstance] | React.RefHandle[ViewTransitionInstance | Null] | React.Ref] = js.native
  var share    : js.UndefOr[ViewTransition.Class] = js.native
  var update   : js.UndefOr[ViewTransition.Class] = js.native
}
