# Refs

React allows you to obtain references to real DOM objects that your component creates in its VDOM.
See [Refs and the DOM](https://facebook.github.io/react/docs/refs-and-the-dom.html) for details.

This document describes how to do the same within scalajs-react.

- [Refs to VDOM tags](#refs-to-vdom-tags)
- [Refs to Scala components](#refs-to-scala-components)
- [Refs to JS components](#refs-to-js-components)
- [Refs to functional components](#refs-to-functional-components)

## Refs to VDOM tags

1. Create a `Ref` in which to store the DOM reference.
  Its type should be whatever the DOM's type.
  Store this inside a component Backend, as a `private val`.
2. On your [`VdomTag`](TYPES.md), call `.withRef(ref)` and pass it your reference.
3. To access the DOM from callbacks, call `.get` or `.foreach`.

Example (excerpts from [CallbackOption online demo](https://japgolly.github.io/scalajs-react/#examples/callback-option)):
```scala
object CallbackOptionExample {

  class Backend($: BackendScope[Unit, State]) {

    // Create the reference
    private val outerRef = Ref[html.Element]

    // Wire it up from VDOM
    def render(s: State) =
      OuterDiv.withRef(outerRef)(...)

    // Use it
    def init: Callback =
      outerRef.foreach(_.focus())
  }

  val Main = ScalaComponent.builder[Unit]("CallbackOption example")
    .initialState(initState)
    .renderBackend[Backend]
    .componentDidMount(_.backend.init)
    .build
}
```

Here is another example: [Refs online demo](https://japgolly.github.io/scalajs-react/#examples/refs).

## Refs to Scala components

1. Call `Ref.toScalaComponent(comp)` to create a reference. Store this inside a component Backend, as a `private val`.
2. Instead of using the component directly to instantiate it, call `.component` on the ref you created.
3. To access the DOM from callbacks, call `.value` on the ref.

Example:
```scala
val DoubleComp = ScalaComponent.builder[Int]("Doubler")
  .render_P(i => <.p(s"$i + $i = ${i << 1}"))
  .build

class Backend {

  // Create a mutable reference
  private val ref = Ref.toScalaComponent(DoubleComp)

  // Wire it up from VDOM
  def render = <.div(ref.component(123))

  // Use it from a lifecycle callback
  def onMount: Callback =
    ref
      .get
      .map(_.getDOMNode.asElement.outerHTML)
      .flatMapCB(Callback.log("DOM HTML: ", _))
}

val Exapmle = ScalaComponent.builder[Unit]("Example")
  .renderBackend[Backend]
  .componentDidMount(_.backend.onMount)
  .build
```

## Refs to JS components

Same as with Scala components;
just change `Ref.toScalaComponent` to `Ref.toJsComponent`.

## Refs to functional components

This is not supported by React.
As such, there is no mechanism by which to do so from scalajs-react.
