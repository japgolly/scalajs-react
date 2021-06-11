package japgolly.scalajs.react

import japgolly.scalajs.react.util.Effect.Async
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import scala.scalajs.js

object React {
  @inline def raw: facade.React = facade.React
  @inline def version: String = facade.React.version

  /** Create a new context.
    *
    * If you'd like to retain type information about the JS type used under-the-hood with React,
    * use `React.Context(defaultValue)` instead.
    *
    * @since 1.3.0 / React 16.3.0
    */
  def createContext[A](defaultValue: A): Context[A] =
    Context(defaultValue)

  /** Create a new context with a displayName.
    *
    * If you'd like to retain type information about the JS type used under-the-hood with React,
    * use `React.Context(displayName, defaultValue)` instead.
    *
    * @since 2.0.0 / React 17.0.0
    */
  def createContext[A](displayName: String, defaultValue: A): Context[A] =
    Context(displayName, defaultValue)

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
    val c2 = facade.React.memo(c.raw, r2.test)
    JsComponent.force[Box[P], c.ctor.ChildrenType, Null](c2)
      .cmapCtorProps[P](Box(_))
  }

  val Profiler = feature.Profiler

  /** StrictMode is a tool for highlighting potential problems in an application.
    * Like Fragment, StrictMode does not render any visible UI.
    * It activates additional checks and warnings for its descendants.
    *
    * Strict mode checks are run in development mode only; they do not impact the production build.
    *
    * @since 1.3.0 / React 16.3.0
    */
  def StrictMode(ns: VdomNode*): VdomElement =
    VdomElement(facade.React.createElement(facade.React.StrictMode, null, ns.map(_.rawNode): _*))

  /** Displays a fallback view until an asynchronous view becomes available.
    *
    * See https://reactjs.org/docs/code-splitting.html#suspense
    *
    * @since 1.4.0 / React 16.6.0
    */
  def Suspense[F[_], A](fallback: VdomNode, asyncBody: => F[A])(implicit ev: A => VdomElement, F: Async[F]): VdomElement = {
    type P          = Box[Unit]
    type LazyResult = facade.React.LazyResult[P]

    val lazyFn: () => js.Promise[LazyResult] =
      () => {
        val post: js.Function1[A, LazyResult] = { a =>
          val comp       = ScalaFnComponent[Unit](_ => ev(a))
          val lazyValue  = comp.raw.asInstanceOf[facade.React.LazyResultValue[P]]
          val lazyResult = js.Dynamic.literal(default = lazyValue.asInstanceOf[js.Any]).asInstanceOf[facade.React.LazyResult[P]]
          lazyResult
        }
        val p1 = F.toJsPromise(asyncBody)
        p1().`then`[LazyResult](post)
      }

    val lazyC  = facade.React.`lazy`(lazyFn)
    val lazyE  = facade.React.createElement(lazyC, Box.Unit)

    val suspenseP = js.Dynamic.literal(fallback = fallback.rawNode.asInstanceOf[js.Any]).asInstanceOf[facade.SuspenseProps]
    val suspenseE = facade.React.createElement(facade.Suspense, suspenseP, lazyE)

    VdomElement(suspenseE)
  }
}
