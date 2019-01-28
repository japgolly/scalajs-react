package japgolly.scalajs.react

import japgolly.scalajs.react.internal.{Box, JsRepr, NotAllowed}
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import japgolly.scalajs.react.{raw => Raw}
import scala.scalajs.js

object React {
  @inline def raw: Raw.React = Raw.React
  @inline def version: String = Raw.React.version

  /** Create a new context.
    *
    * If you'd like to retain type information about the JS type used under-the-hood with React,
    * use `React.Context(defaultValue)` instead.
    *
    * @since 1.3.0 / React 16.3.0
    */
  def createContext[A](defaultValue: A)(implicit jsRepr: JsRepr[A]): Context[A] =
    Context(defaultValue)(jsRepr)

  @deprecated("Use Ref. For details see https://github.com/japgolly/scalajs-react/blob/master/doc/REFS.md", "1.3.0 / React 16.3.0")
  def createRef(notAllowed: NotAllowed) = NotAllowed.body

  type Context[A] = feature.Context[A]
  val  Context    = feature.Context

  val Fragment = feature.ReactFragment

  /** Ref forwarding is an opt-in feature that lets some components take a ref they receive,
    * and pass it further down (in other words, "forward" it) to a child.
    *
    * See https://reactjs.org/docs/forwarding-refs.html
    */
  @inline def forwardRef = component.ReactForwardRef

  /** Class components can bail out from rendering when their input props are the same using shouldComponentUpdate.
    * Now you can do the same with function components by wrapping them in React.memo.
    *
    * @since 1.4.0 / React 16.6.0
    */
  def memo[P, CT[-p, +u] <: CtorType[p, u]](c: ScalaFnComponent[P, CT])
                                           (implicit r: Reusability[P],
                                            s: CtorType.Summoner[Box[P], c.ctor.ChildrenType])
      : GenericComponent[P, s.CT, JsComponent.Unmounted[Box[P], Null]] = {
    val r2 = implicitly[Reusability[Box[P]]]
    val c2 = Raw.React.memo(c.raw, r2.test)
    JsComponent.force[Box[P], c.ctor.ChildrenType, Null](c2)
      .cmapCtorProps[P](Box(_))
  }

  /** StrictMode is a tool for highlighting potential problems in an application.
    * Like Fragment, StrictMode does not render any visible UI.
    * It activates additional checks and warnings for its descendants.
    *
    * Strict mode checks are run in development mode only; they do not impact the production build.
    *
    * @since 1.3.0 / React 16.3.0
    */
  def StrictMode(ns: VdomNode*): VdomElement =
    VdomElement(Raw.React.createElement(Raw.React.StrictMode, null, ns.map(_.rawNode): _*))

  /** Displays a fallback view until an asynchronous view becomes available.
    *
    * See https://reactjs.org/docs/code-splitting.html#suspense
    *
    * @since 1.4.0 / React 16.6.0
    */
  def Suspense[A](fallback: VdomNode, asyncBody: AsyncCallback[A])(implicit ev: A => VdomElement): VdomElement = {
    val lazyBody = asyncBody.map { a =>
      type P         = Box[Unit]
      val comp       = ScalaFnComponent[Unit](_ => ev(a))
      val lazyValue  = comp.raw.asInstanceOf[Raw.React.LazyResultValue[P]]
      val lazyResult = js.Dynamic.literal(default = lazyValue.asInstanceOf[js.Any]).asInstanceOf[Raw.React.LazyResult[P]]
      lazyResult
    }
    val lazyFn = () => lazyBody.unsafeToJsPromise()
    val lazyC  = Raw.React.`lazy`(lazyFn)
    val lazyE  = Raw.React.createElement(lazyC, Box.Unit)

    val suspenseP = js.Dynamic.literal(fallback = fallback.rawNode.asInstanceOf[js.Any]).asInstanceOf[Raw.SuspenseProps]
    val suspenseE = Raw.React.createElement(Raw.Suspense, suspenseP, lazyE)

    VdomElement(suspenseE)
  }
}
