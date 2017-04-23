# Refs

React allows you to obtain references to real DOM objects that your component creates in its VDOM.
See [Refs and the DOM](https://facebook.github.io/react/docs/refs-and-the-dom.html) for details.

This document describes how to do the same within scalajs-react.

- [Refs to VDOM tags](#refs-to-vdom-tags)
- [Refs to Scala components](#refs-to-scala-components)
- [Refs to JS components](#refs-to-js-components)
- [Refs to functional components](#refs-to-functional-components)

## Refs to VDOM tags

1. Create a variable in which to store the DOM reference.
  Its type should be whatever the DOM's type is (wrap it in `Option` for additional safety if desired.)
  The recommended place to store this is inside a component Backend, as a `private var`.
2. On your [`VdomTag`](TYPES.md), call `.ref(vdomTag => Unit)` on it and provide a function that updates your variable.
3. To access the DOM from callbacks, just access the variable.

Example (excerpts from [CallbackOption online demo](https://japgolly.github.io/scalajs-react/#examples/callback-option)):
```scala
object CallbackOptionExample {

  class Backend($: BackendScope[Unit, State]) {

    // Prepare a variable
    private var outerRef: html.Element = _

    // Wire it up from VDOM
    def render(s: State) =
      OuterDiv.ref(outerRef = _)(...)

    // Use it
    def init: Callback =
      Callback(outerRef.focus())
  }

  val Main = ScalaComponent.builder[Unit]("CallbackOption example")
    .initialState(initState)
    .renderBackend[Backend]
    .componentDidMount(_.backend.init)
    .build
}
```


## Refs to Scala components

1. Call `ScalaComponent.mutableRefTo(comp)` to create a reference variable.
  This is mutable and should *not* be shared.
  The recommended place to store this is inside a component Backend, as a `private val`.
2. Instead of using the component directly to instantiate it, call `.component` on the ref you created.
3. To access the DOM from callbacks, call `.value` on the ref.

Example:
```scala
val Double = ScalaComponent.builder[Int]("Doubler")
  .render_P(i => <.p(s"$i + $i = ${i << 1}"))
  .build

class Backend {

  // Create a mutable reference
  private val ref = ScalaComponent.mutableRefTo(Double)

  // Wire it up from VDOM
  def render = <.div(ref.component(123))

  // Use it
  def onMount = Callback.log("DOM HTML: ", ref.value.getDOMNode.outerHTML)
}

val Exapmle = ScalaComponent.builder[Unit]("Example")
  .renderBackend[Backend]
  .componentDidMount(_.backend.onMount)
  .build
```

## Refs to JS components

Same as with Scala components;
just change `ScalaComponent.mutableRefTo` to `JsComponent.mutableRefTo`.

## Refs to functional components

This is not supported by React.
As such, there is no mechanism by which to do so from scalajs-react.
