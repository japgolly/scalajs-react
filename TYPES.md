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
but if you don't know or care about the event target type, simply use `ReactEvent` or `ReactEventH` instead.

| React Event Type | Alias for any `Node` | Aliases `Node` to `+HTMLElement` |
| ---- | ---- | ---- |
| `SyntheticEvent[Node]` | `ReactEvent` | `ReactEventH` |
| `SyntheticClipboardEvent[Node]` | `ReactClipboardEvent` | `ReactClipboardEventH` |
| `SyntheticCompositionEvent[Node]` | `ReactCompositionEvent` | `ReactCompositionEventH` |
| `SyntheticDragEvent[Node]` | `ReactDragEvent` | `ReactDragEventH` |
| `SyntheticFocusEvent[Node]` | `ReactFocusEvent` | `ReactFocusEventH` |
| `SyntheticKeyboardEvent[Node]` | `ReactKeyboardEvent` | `ReactKeyboardEventH` |
| `SyntheticMouseEvent[Node]` | `ReactMouseEvent` | `ReactMouseEventH` |
| `SyntheticTouchEvent[Node]` | `ReactTouchEvent` | `ReactTouchEventH` |
| `SyntheticUIEvent[Node]` | `ReactUIEvent` | `ReactUIEventH` |
| `SyntheticWheelEvent[Node]` | `ReactWheelEvent` | `ReactWheelEventH` |

# Other

| Type | Desc |
| ---- | ---- |
| `ComponentStateFocus[T]` | Rather than give functions full access to a components state, you can narrow the state down to a subset and pass that around via this type. |
| `Ref[+N]` | A named reference to an element in a React VDOM. (See [React: More About Refs](http://facebook.github.io/react/docs/more-about-refs.html).) |
| `RefP[I, +N]` | As above but references multiple, related DOM elements and requires a parameter `I` (usually an ID) to disambiguate. |

#### TODO

* `VDom`
* `ComponentOrNode`
* `ReactVDom.Tag`
* `ReactVDom.Modifier`
