package japgolly.scalajs.react.facade

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.|

/** @since 4.1.0 / React 19.3 */
@js.native
trait FragmentInstance extends js.Object {

  def blur(): Unit = js.native

  def focus(focusOptions: js.UndefOr[dom.FocusOptions] = js.native): Unit = js.native

  def focusLast(focusOptions: js.UndefOr[dom.FocusOptions] = js.native): Unit = js.native

  def observeUsing(observer: dom.IntersectionObserver | dom.ResizeObserver): Unit = js.native

  def unobserveUsing(observer: dom.IntersectionObserver | dom.ResizeObserver): Unit = js.native

  def getClientRects(): js.Array[dom.DOMRect] = js.native

  def getRootNode(getRootNodeOptions: js.UndefOr[GetRootNodeOptions] = js.native): dom.Document | dom.ShadowRoot | FragmentInstance = js.native

  def addEventListener(`type`: String,
                       listener: FragmentInstance.EventListener,
                       optionsOrUseCapture: js.UndefOr[FragmentInstance.EventListenerOptionsOrUseCapture] = js.native): Unit = js.native

  def removeEventListener(`type`: String,
                          listener: FragmentInstance.EventListener,
                          optionsOrUseCapture: js.UndefOr[FragmentInstance.EventListenerOptionsOrUseCapture] = js.native): Unit = js.native

  def dispatchEvent(event: dom.Event): Boolean = js.native

  def scrollIntoView(alignToTop: js.UndefOr[Boolean] = js.native): Unit = js.native
}

object FragmentInstance {
  type EventListener = js.Function1[dom.Event, Any]
  type EventListenerOptionsOrUseCapture = Boolean | dom.EventListenerOptions
}

@js.native
trait GetRootNodeOptions extends js.Object {
  var composed: js.UndefOr[Boolean] = js.native
}

object GetRootNodeOptions {
  def apply(composed: js.UndefOr[Boolean] = js.undefined): GetRootNodeOptions = {
    val o = (new js.Object).asInstanceOf[GetRootNodeOptions]
    composed.foreach(o.composed = _)
    o
  }
}
