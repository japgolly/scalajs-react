package japgolly.scalajs.react.facade

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.|

@JSImport("react-dom/client", JSImport.Namespace, "ReactDOM")
@js.native
object ReactDOMClient extends ReactDOMClient

@js.native
trait ReactDOMClient extends js.Object {
  final type HydrationContainer = dom.Element | dom.Document
  final type RootContainer      = dom.Element | dom.DocumentFragment

  /** Create a React root for the supplied container and return the root. The root can be used to render a React element into the DOM with `.render`. */
  final def createRoot(container: RootContainer, options: CreateRootOptions = js.native): RootType = js.native

  /** Same as createRoot(), but is used to hydrate a container whose HTML contents were rendered by ReactDOMServer. React will attempt to attach event listeners to the existing markup. */
  final def hydrateRoot(container: HydrationContainer, element: React.Node, options: HydrateRootOptions = js.native): RootType = js.native
}

@js.native
trait RootType extends js.Object {
  def render(element: React.Node): Unit = js.native
  def unmount(): Unit = js.native
}

@js.native
trait CreateRootOptions extends js.Object {
  var identifierPrefix                   : js.UndefOr[String]
  var onRecoverableError                 : js.UndefOr[Any => Unit]
  var unstable_concurrentUpdatesByDefault: js.UndefOr[Boolean]
  var unstable_strictMode                : js.UndefOr[Boolean]
  // var transitionCallbacks                : js.UndefOr[TransitionTracingCallbacks]
}

@js.native
trait HydrateRootOptions extends js.Object {
  var identifierPrefix                   : js.UndefOr[String]
  var onRecoverableError                 : js.UndefOr[Any => Unit]
  var unstable_concurrentUpdatesByDefault: js.UndefOr[Boolean]
  var unstable_strictMode                : js.UndefOr[Boolean]
  // var hydratedSources                    : js.UndefOr[Array[MutableSource[any]]]
  // var onHydrated                         : js.UndefOr[Comment => Unit]
  // var onDeleted                          : js.UndefOr[Comment => Unit]
}

// @js.native
// trait RecoverableError extends js.Object {
//   val message: String
// }

// @js.native
// trait TransitionTracingCallbacks extends js.Object {
//   var onMarkerComplete      : js.UndefOr[(String, String, Double, Double) => Unit]
//   var onMarkerIncomplete    : js.UndefOr[(String, String, Double, Array[Deletions]) => Unit]
//   var onMarkerProgress      : js.UndefOr[(String, String, Double, Double, Array[HasName]) => Unit]
//   var onTransitionComplete  : js.UndefOr[(String, Double, Double) => Unit]
//   var onTransitionIncomplete: js.UndefOr[(String, Double, Array[Deletions]) => Unit]
//   var onTransitionProgress  : js.UndefOr[(String, Double, Double, Array[HasName]) => Unit]
//   var onTransitionStart     : js.UndefOr[(String, Double) => Unit]
// }

// @js.native
// trait HasName extends js.Object {
//   val name: String
// }

// @js.native
// trait Deletions extends js.Object {
//   val `type`: String
//   val name: js.UndefOr[String]
//   val newName: js.UndefOr[String]
//   val endTime: Double
// }
