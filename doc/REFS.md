# Refs

React allows you to obtain references to real DOM objects that your component creates in its VDOM.
See [Refs and the DOM](https://facebook.github.io/react/docs/refs-and-the-dom.html) for details.

This document describes how to do the same within scalajs-react.

- [Refs to VDOM tags](#refs-to-vdom-tags)
  - [Typed](#type-safe)
  - [Untyped](#untyped)
- [Refs to Scala components](#refs-to-scala-components)
- [Refs to JS components](#refs-to-js-components)
- [Refs to functional components](#refs-to-functional-components)
- [Forwarding refs](#forwarding-refs)


## Refs to VDOM tags

There are two different types of refs to VDOM.
The names evolved incrementally and are now a bit misleading.
The types are named "typed" and "untyped".
What are the differences?

1. "Typed" refs can only be used with vdom of the same type:
  if you try to associate a typed ref to an `<input>` with a `<select>` you'll get a compilation error.

  "Untyped" refs on the other hand can be associated with any vdom type:
  if you try to associate an untyped ref to an `<input>` with a `<select>` you won't experience any compilation
  or runtime errors, but the association will be silently ignored.
  If the provided vdom is the ref type (or a subtype), then the ref will work as expected.

2. Decoupling of control. With "typed" refs, you create the ref and apply it in the same class.
   With "untyped" refs, you can pass the ref around and have a completely different part of the code wire it
   up to vdom.


### Typed

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

### Untyped

There are two ways to use an untyped ref.

The first is to just pass in a `Dom => Unit` function:

```scala
var refOrNull: html.Input = null

def render =
  <.input(^.untypedRef(refOrNull = _))
```

The second is to create a `Ref` object and wire it up like a normal vdom attribute:

```scala
val ref = Ref.toVdom[html.Input]

def render =
  <.input(^.untypedRef := ref)
```


## Refs to Scala components

1. Create a ref. You've got two ways to do this:
  * Call `Ref.toScalaComponent(C)` where `C` is the target Scala component.
  * Call `Ref.toScalaComponent[P, S, B]` and explicitly specify the expected types. So `P` = props, `S` = state, `B` = backend.
2. Store the ref inside a component Backend, as a `private val`.
3. Use the ref in your render function.
  * `C.withRef(ref)(props)` where `C` is the Scala component.
  * Alternatively, if you specified the component to create the ref, you can avoid respecifying it again and use
    `ref.component(props)`
4. To access the DOM from callbacks, call `.value` on the ref.

Example:
```scala
val DoubleComp = ScalaComponent.builder[Int]("Doubler")
  .render_P(i => <.p(s"$i + $i = ${i << 1}"))
  .build

class Backend {

  // Create a mutable reference
  private val ref = Ref.toScalaComponent(DoubleComp)

  // Wire it up from VDOM
  def render = <.div(DoubleComp.withRef(ref)(123))
               // Alternatively: <.div(ref.component(123))

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

Same as with Scala components; except instead of `Ref.toScalaComponent` use one of these...

* `Ref.toJsComponent(C)` where `C` is a `JsComponent` instance
* `Ref.toJsComponent[P,S]` where `P` = props, `S` = state
* `Ref.toJsComponentWithMountedFacade[P,S,F]` where `P` = props, `S` = state, `F` = facade with additional methods on mounted component


## Refs to functional components

This is not supported by React.
As such, there is no mechanism by which to do so from scalajs-react.


## Forwarding refs

Create a ref-forwarding component by calling `React.forwardRef`,
or wrap a JS one as described in [the interoperability doc](INTEROP.md).

Next, use it as a normal component,
specifying `.withRef` before providing the props to forward a ref,
or just by providing props if you don't want to specify a ref.

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Foo {
  val Component = ScalaComponent.builder[Int]("Foo")
    .render_P(p => <.h1(s"$p = $p !"))
    .build
}

object Bar {
  val Component = React.forwardRef.toScalaComponent(Foo.Component)[String](
    (label, ref) =>
      <.div(label, Foo.Component.withRef(ref)(123))
  )
}
```

