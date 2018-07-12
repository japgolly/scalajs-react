package japgolly.scalajs.react

import japgolly.scalajs.react.internal.JsRepr
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import japgolly.scalajs.react.{raw => Raw}
import scala.scalajs.js

object React {
  def raw = Raw.React

  /** Create a new context.
    *
    * If you'd like to retain type information about the JS type used under-the-hood with React,
    * use `React.Context(defaultValue)` instead.
    *
    * @since 1.3.0 / React 16.3.0
    */
  def createContext[A](defaultValue: A)(implicit jsRepr: JsRepr[A]): Context[A] =
    Context(defaultValue)

  /** React Context.
    *
    * See https://reactjs.org/docs/context.html
    *
    * @since 1.3.0 / React 16.3.0
    */
  sealed trait Context[A] { ctx =>

    /** Scala values are converted to JS values and back using this. */
    val jsRepr: JsRepr[A]

    /** The raw JS type that React sees. */
    type RawValue = jsRepr.J

    /** The underlying JS `React.Context`. */
    val raw: Raw.React.Context[RawValue]

    /** Allows Consumers to subscribe to context changes.
      * Accepts a value prop to be passed to Consumers that are descendants of this Provider.
      * One Provider can be connected to many Consumers. Providers can be nested to override values deeper within the
      * tree.
      *
      * Caveat:
      *
      * The React Context API uses `Object.is` to determine equality
      * (see https://reactjs.org/docs/context.html#caveats). This affects
      *
      * scalajs-react will use plain JS values without translation where possible (eg. booleans, ints, strings)
      * in which case, universal equality is used by React (eg. true == true, 123 == 123, "hello" == "hello").
      * For these types of contexts, feel free to call `context.provide(value)(children)` the same way you would in
      * React JS.
      *
      * Non-JS values (eg. case classes) are boxed into JS objects in which case, React will use referential equality
      * meaning that you need to hold on to and reuse the boxed instance. Seeing as you're not doing the boxing
      * yourself, what you should hold on to and reuse is the result of `.provide()` before you apply children.
      * For example, instead of:
      *
      * {{{
      *   class Backend {
      *     def render(p: Props) =
      *       context.provide(X(...))(<.div("hello"))
      *   }
      * }}}
      *
      * you should save and reuse the provide result:
      *
      * {{{
      *   class Backend {
      *     private val contextX = context.provide(X(...))

      *     def render(p: Props) =
      *       contextX(<.div("hello"))
      *   }
      * }}}
      *
      * Doing so will keep React happy and avoid needless re-renders.
      */
    def provide(value: A): Context.Provided[A] = {
      val a = value
      new Context.Provided[A] {
        override val value = a
        override type RawValue = ctx.RawValue
        override val rawValue = jsRepr.toJs(a)

        private type Props = Raw.React.ValueProps[RawValue]
        private val props: Props = new Props {
          override val value: RawValue = rawValue
        }

        override def apply(children: VdomNode*): VdomElement = {
          val e = Raw.React.createElement[Props](raw.Provider, props, children.map(_.rawNode): _*)
          VdomElement(e)
        }
      }
    }

    /** Subscribes to context changes.
      *
      * The value argument passed to the function will be equal to the value prop of the closest Provider for this
      * context above in the tree. If there is no Provider for this context above, the value argument will be equal to
      * the defaultValue that was passed to createContext().
      *
      * All Consumers that are descendants of a Provider will re-render whenever the Provider’s value prop changes.
      * The propagation from Provider to its descendant Consumers is not subject to the shouldComponentUpdate method,
      * so the Consumer is updated even when an ancestor component bails out of the update.
      *
      * Changes are determined by comparing the new and old values using referential equality.
      */
    def consume(f: A => VdomNode): VdomElement = {
      val childFn: js.Function1[RawValue, Raw.React.Node] =
        (rawValue: RawValue) => f(jsRepr.fromJs(rawValue)).rawNode
      val e = Raw.React.createElement(raw.Consumer, null, childFn)
      VdomElement(e)
    }
  }

  object Context {
    type WithRawValue[A, J <: js.Any] = Context[A] { type RawValue = J }

    def apply[A](defaultValue: A)(implicit jsRepr: JsRepr[A]): WithRawValue[A, jsRepr.J] = {
      type R = jsRepr.type
      val _jsRepr: R = jsRepr
      new Context[A] {
        override type RawValue = _jsRepr.J
        override val jsRepr: R = _jsRepr
        override val raw       = Raw.React.createContext(jsRepr.toJs(defaultValue))
        override def toString  = s"Context($defaultValue)"
        override def hashCode  = defaultValue.##
      }
    }

    sealed trait Provided[A] {
      val value: A
      type RawValue <: js.Any
      val rawValue: RawValue
      def apply(children: VdomNode*): VdomElement
    }
  }

}
