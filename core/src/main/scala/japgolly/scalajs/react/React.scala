package japgolly.scalajs.react

import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import japgolly.scalajs.react.{raw => Raw}
import scala.scalajs.js

object React {
  def raw = Raw.React

  def createContext[A](defaultValue: A): Context[A] =
    Context(Raw.React.createContext(Box(defaultValue)))

  /** React Context API.
    *
    * See https://reactjs.org/docs/context.html
    */
  final case class Context[A](raw: Raw.React.Context[Context.RawValue[A]]) {
    type RawValue = Context.RawValue[A]

    /** Allows Consumers to subscribe to context changes.
      * Accepts a value prop to be passed to Consumers that are descendants of this Provider.
      * One Provider can be connected to many Consumers. Providers can be nested to override values deeper within the
      * tree.
      *
      * Caveat:
      *
      * The React Context API "uses reference identity to determine when to re-render"
      * (see https://reactjs.org/docs/context.html#caveats).
      *
      * To handle this properly within scalajs-react, you should not worry about reference identity of the value
      * argument to this function, but instead aim for reference identity of this function's return value.
      *
      * For example, instead of:
      *
      * {{{
      *   class Backend {
      *     def render(p: Props) =
      *       context.provide(X)(<.div("hello"))
      *   }
      * }}}
      *
      * you should save and reuse the provide result:
      *
      * {{{
      *   class Backend {
      *     private val contextX = context.provide(X)

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
        (b: RawValue) => f(b.unbox).rawNode
      val e = Raw.React.createElement(raw.Consumer, null, childFn)
      VdomElement(e)
    }
  }

  object Context {
    type RawValue[+A] = Box[A]

    sealed trait Provided[A] {
      val value: A
      def apply(children: VdomNode*): VdomElement

      final type RawValue = Context.RawValue[A]
      final val rawValue: RawValue = Box(value)
    }
  }

}
