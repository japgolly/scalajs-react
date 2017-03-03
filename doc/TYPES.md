# Plain DOM Elements

#### React
A `ReactDOMElement` is a representation of a standard DOM element such as a `<div>`, an `<input>`, etc.

A `VdomElement` is a `ReactDOMElement` or a React component.

A`VdomNode` which is anything usable a child of a `VdomElement`. This can be another `VdomElement`, an array of `VdomElement`s, or plain text or number.

*More detail: https://facebook.github.io/react/docs/glossary.html*

#### Scalatags
A custom version [Scalatags](https://github.com/lihaoyi/scalatags) is used for DOM-building type-safety. A DOM element is first constructed via Scalatags, then converted automatically to a `VdomElement`.

There are two types to note:
* `...react.vdom.VdomTag` - A DOM still being constructed, a soon-to-be `VdomElement`. Additional properties, styles and children can be specified via `.apply(TagMod): VdomTag`.
* `...react.vdom.TagMod` - 0, 1, or *n* properties, styles or children that can be applied to a `VdomTag`.

Example:
```scala
val tag1   : VdomTag     = input(className := "name")
val mod    : TagMod       = value := "Bob"
val tag2   : VdomTag     = tag1(mod, readOnly := true)
val element: VdomElement = tag2
// equivalent to <input class="name" value="Bob" readonly=true />
```

# Components

In type constructors you'll often see `P`, `S`, `B`, `N`.
They are arbitrary types for:
* `P` - Component Properties.
* `S` - Component State.
* `B` - Component Backend.
* `N` - DOM Node. (eg. `HTMLAnchorElement` for `<a>...</a>`)

| Scala Component | JS Component | Desc |
| --------------- | ------------ | ---- |
| `ScalaComponent.build[P, S, B]`      | -                      | Scala component builder. |
| `ReactComponentC[P, S, +B, +N]` | `ReactComponentC_`     | A component constructor. |
| `ReactComponentU[P, S, +B, +N]` | `ReactComponentU_`     | An unmounted component.  |
| `ReactComponentM[P, S, +B, +N]` | `ReactComponentM_[+N]` | A mounted component.     |

When building a pure JS React component, a range of functions are provided via `this`.
Similarly, when building a Scala React component, (i.e. using `ReactComponentB`),
you are provided "scope" objects containing what you would expect to find on `this` in JS.
Namely...

| Type | Desc |
| ---- | ---- |
| `CompScope.DuringCallbackU[P, S, +B]` | An unmounted component's `this` scope. |
| `CompScope.DuringCallbackM[P, S, +B]` | A mounted component's `this` scope. |
| `CompScope.WillUpdate[P, S, +B, +N]` | A component's `this` scope during `componentWillUpdate`. |
| `BackendScope[P, S]` | A component's `this` scope as is available to backends. |

For using JS React Components, you can use follow facade interfaces.

|Type|Desc|
| ---- | ---- |
| `JsComponentType[P, S, +N]`| A JS component class. |
| `JsComponentC[P, S, +N]`| A JS component factory. |
| `JsComponentU[P, S, +N]`| A unmounted JS component.|
| `JsComponentM[P, S, +N]`| A mounted JS component.|

The characters, `P`, `S` and `N`'s means are same to above. Note that there are no `B`.

# Events

The synthetic event types you read about in the [React docs](https://facebook.github.io/react/docs/events.html)
are typed as shown below.

As type safety is a goal, synthetic events also type the event target,
but if you don't know or care about the event target type, simply use `ReactEvent` instead.

| React Event Type | Alias for any `Node` |
| ---- | ---- |
| `ReactEventFrom[Node]` | `ReactEvent` |
| `ReactClipboardEventFrom[Node]` | `ReactClipboardEvent` |
| `ReactCompositionEventFrom[Node]` | `ReactCompositionEvent` |
| `ReactDragEventFrom[Node]` | `ReactDragEvent` |
| `ReactFocusEventFrom[Node]` | `ReactFocusEvent` |
| `ReactKeyboardEventFrom[Node]` | `ReactKeyboardEvent` |
| `ReactMouseEventFrom[Node]` | `ReactMouseEvent` |
| `ReactTouchEventFrom[Node]` | `ReactTouchEvent` |
| `SyntheticUIEvent[Node]` | `ReactUIEvent` |
| `ReactWheelEventFrom[Node]` | `ReactWheelEvent` |

One of the suffixes below can be added to any the ReactEvents above, to provide an alias with a more specific node type.

| Suffix | Node |
| ------ | ---- |
| `H` | `HTMLElement` |
| `I` | `HTMLInputElement` |
| `TA` | `HTMLTextAreaElement` |

For example, `ReactDragEventFromInput` is a `ReactDragEvent` over a `HTMLInputElement` (an `<input>`), the same as writing `ReactDragEventFrom[HTMLInputElement]`.

### Component-scope access

To prevent issues such as
backends' props/state being used in such a way that they go stale at runtime (a suprisingly easy mistake to make),
component/backend scope access is as follows:

| Method | `BackendScope` | During Callback | External Access |
| --- | --- | --- | --- |
| `.props` | `CallbackTo[P]` | `P` | `P` |
| `.state` | `CallbackTo[S]` | `S` | `S` |
| `.setState` | `Callback` | `Callback` | `Unit` |
| `.modState` | `Callback` | `Callback` | `Unit` |
| `.forceUpdate` | `Callback` | `Callback` | `Unit` |

# Other

| Type | Desc |
| ---- | ---- |
| `Ref[+N]` | A named reference to an element in a React VDOM. (See [React: More About Refs](https://facebook.github.io/react/docs/more-about-refs.html).) |
| `RefP[I, +N]` | As above but references multiple, related DOM elements and requires a parameter `I` (usually an ID) to disambiguate. |
