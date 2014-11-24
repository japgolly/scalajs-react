# Plain DOM Elements

#### React
A `ReactDOMElement` is a representation of a standard DOM element such as a `<div>`, an `<input>`, etc.

A `ReactElement` is a `ReactDOMElement` or a React component.

A`ReactNode` which is anything usable a child of a `ReactElement`. This can be another `ReactElement`, an array of `ReactElement`s, or plain text or number.

*More detail: http://facebook.github.io/react/docs/glossary.html*

#### Scalatags
[Scalatags](https://github.com/lihaoyi/scalatags) is used for DOM-building type-safety. A DOM element is first constructed via Scalatags, then converted automatically to a `ReactElement`.

There are two types to note:
* `ReactVDom.Tag` - A DOM still being constructed, a soon-to-be `ReactElement`. Additional properties, styles and children can be specified via `tag.apply(Modifier): Tag`.
* `ReactVDom.Modifier` - 0, 1, or *n* properties, styles or children than can be applied to a `Tag`.

Example:
```scala
val tag1   : Tag          = input(cls := "name")
val mod    : Modifier     = value := "Bob"
val tag2   : Tag          = tag1(mod, readonly := true)
val element: ReactElement = tag2
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
| `ReactComponentB[P, S, B]`      | -                      | Scala component builder. |
| `ReactComponentC[P, S, +B, +N]` | `ReactComponentC_`     | A component constructor. |
| `ReactComponentU[P, S, +B, +N]` | `ReactComponentU_`     | An unmounted component.  |
| `ReactComponentM[P, S, +B, +N]` | `ReactComponentM_[+N]` | A mounted component.     |

When building a pure JS React component, a range of functions are provided via `this`.
Similarly, when building a Scala React component, (i.e. using `ReactComponentB`),
you are provided "scope" objects containing what you would expect to find on `this` in JS.
Namely...

| Type | Desc |
| ---- | ---- |
| `ComponentScopeU[P, S, +B]` | An unmounted component's `this` scope. |
| `ComponentScopeWU[P, S, +B]` | A component's `this` scope during `componentWillUpdate`. |
| `ComponentScopeM[P, S, +B]` | A mounted component's `this` scope. |
| `BackendScope[P, S]` | A component's `this` scope as is available to backends. |

# Events

The synthetic event types you read about in the [React docs](http://facebook.github.io/react/docs/events.html)
are typed as shown below.

As type safety is a goal, synthetic events also type the event target,
but if you don't know or care about the event target type, simply use `ReactEvent` instead.

| React Event Type | Alias for any `Node` |
| ---- | ---- | ---- |
| `SyntheticEvent[Node]` | `ReactEvent` |
| `SyntheticClipboardEvent[Node]` | `ReactClipboardEvent` |
| `SyntheticCompositionEvent[Node]` | `ReactCompositionEvent` |
| `SyntheticDragEvent[Node]` | `ReactDragEvent` |
| `SyntheticFocusEvent[Node]` | `ReactFocusEvent` |
| `SyntheticKeyboardEvent[Node]` | `ReactKeyboardEvent` |
| `SyntheticMouseEvent[Node]` | `ReactMouseEvent` |
| `SyntheticTouchEvent[Node]` | `ReactTouchEvent` |
| `SyntheticUIEvent[Node]` | `ReactUIEvent` |
| `SyntheticWheelEvent[Node]` | `ReactWheelEvent` |

One of the suffixes below can be added to any the ReactEvents above, to provide an alias with a more specific node type.

| Suffix | Node |
| ------ | ---- |
| `H` | `HTMLElement` |
| `I` | `HTMLInputElement` |

For example, `ReactDragEventI` is a `ReactDragEvent` over a `HTMLInputElement` (an `<input>`), the same as writing `SyntheticDragEvent[HTMLInputElement]`.

# Other

| Type | Desc |
| ---- | ---- |
| `ComponentStateFocus[T]` | Rather than give functions full access to a components state, you can narrow the state down to a subset and pass that around via this type. |
| `Ref[+N]` | A named reference to an element in a React VDOM. (See [React: More About Refs](http://facebook.github.io/react/docs/more-about-refs.html).) |
| `RefP[I, +N]` | As above but references multiple, related DOM elements and requires a parameter `I` (usually an ID) to disambiguate. |
