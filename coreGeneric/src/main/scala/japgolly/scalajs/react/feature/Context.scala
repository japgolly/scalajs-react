package japgolly.scalajs.react.feature

import japgolly.scalajs.react.facade
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import scala.scalajs.js

/** React Context.
  *
  * See https://reactjs.org/docs/context.html
  *
  * @since 1.3.0 / React 16.3.0
  */
sealed trait Context[A] { ctx =>

  /** The underlying JS `React.Context`. */
  val raw: facade.React.Context[A]

  final def displayName: String =
    raw.displayName.getOrElse("")

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

      private type Props = facade.React.ValueProps[A]
      private val props: Props = new Props {
        override val value = a
      }

      override def apply(children: VdomNode*): VdomElement = {
        val e = facade.React.createElement[Props](raw.Provider, props, children.map(_.rawNode): _*)
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
    * All Consumers that are descendants of a Provider will re-render whenever the Provider's value prop changes.
    * The propagation from Provider to its descendant Consumers is not subject to the shouldComponentUpdate method,
    * so the Consumer is updated even when an ancestor component bails out of the update.
    *
    * Changes are determined by comparing the new and old values using referential equality.
    */
  def consume(f: A => VdomNode): VdomElement = {
    val childFn: js.Function1[A, facade.React.Node] =
      (a: A) => f(a).rawNode
    val e = facade.React.createElement(raw.Consumer, null, childFn)
    VdomElement(e)
  }
}

object Context {

  def apply[A](displayName: String, defaultValue: A): Context[A] = {
    val ctx = apply(defaultValue)
    ctx.raw.displayName = displayName
    ctx
  }

  def apply[A](defaultValue: A): Context[A] = {
    new Context[A] {
      override val raw       = facade.React.createContext(defaultValue)
      override def toString  = s"Context($defaultValue)"
      override def hashCode  = defaultValue.##
    }
  }

  sealed trait Provided[A] {
    val value: A
    def apply(children: VdomNode*): VdomElement
  }
}
