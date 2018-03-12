# Types

You barely need to know any of these in order to use scalajs-react.
This is here as an optional reference.

- [Components](#components)
  - [Stages](#stages)
  - [Behaviour](#behaviour)
  - [Generic](#generic)
  - [JS components](#js-components)
  - [JS functional components](#js-functional-components)
  - [Scala components](#scala-components)
  - [Scala functional components](#scala-functional-components)
  - [Roots](#roots)
- [State](#state)
  - [`StateAccess`](#stateaccess)
  - [`StateAccessor`](#stateaccessor)
  - [{Set,Mod}State abstractions](#setmodstate-abstractions)
- [Events](#events)
- [VDOM](#vdom)



# Components

Type parameters below are abbreviated as follows:
* `P` = Props
* `CT` = `CtorType`
* `U` = Unmounted
* `M` = Mounted
* `F` = Effect type

### Stages

React components can be thought of as having 3 stages.

1. `Component` - A component at it's top-level. After creation, before use. Input (props and/or children) may be required. Pure.
2. `Unmounted` - An instance of a component that's ready to be rendered. Still pure at this point.
3. `Mounted` - A reference to a component that has been mounted/rendered. Usually live. Impure as it can change as the user interacts with the app. Pure versions available where impurity is encapulated in `CallbackTo[A]`.

Example:
```scala
// Stage 1: Component
val component = ScalaComponent.builder[String]("HelloComponent")
  .render(i => <.div(s"Hello ${i.props}. How are you?"))
  .build

// Stage 2: Unmounted
val unmounted = component("David")

// Stage 3: Mounted
val mounted = unmounted.renderIntoDOM(...)
```

### Behaviour

Some types declare behaviour.

#### `Children`
You are required to specify one of these when creating a JS component.
* `Children.None` - indicates that a component doesn't use `.props.children`.
* `Children.Varargs` - indicates that a component uses `.props.children` and accepts 0-n args.

*(In future it may be useful to add `Children.ExactlyOne` as well.)*

#### `CtorType`
The type of constructor that a component has.
You only need to specify these when you want to explicitly declare the types of your components.
* `CtorType.Props` - Props required. Construct like `component(props)`.
* `CtorType.PropsAndChildren` - Props and children required. Construct like `component(props)(children*)`.
* `CtorType.Children` - Children required. Construct like `component(children*)`.
* `CtorType.Nullary` - Nothing required. Construct like `component()`.

### Generic

* Generic representation of components.
  All components are subtypes of these.
  Useful in library methods that do something with any kind of component.

  ```scala
  GenericComponent[P, CT, U]
  GenericComponent.Unmounted[P, M]
  GenericComponent.Mounted[F, P, S]
  GenericComponent.MountedPure[P, S]
  GenericComponent.MountedImpure[P, S]
  ```

* `_Raw` -
  Access to raw (i.e. JS-world / non-Scala.JS) values.
  Notice there are no type parameters.
  All components are subtypes of these.
  Useful for simple interop that doesn't care about the type of props/state.

  ```scala
  GenericComponent.ComponentRaw
  GenericComponent.UnmountedRaw
  GenericComponent.MountedRaw
  ```


### JS components

* JS components.
  Useful if you want to explicitly declare your component's types.

  ```scala
  JsComponent[P, S, CT]
  JsComponent.Unmounted[P, S]
  JsComponent.Mounted[P, S]
  ```

* `_WithFacade` -
  Variations that accept a facade to the type once it's mounted.
  Useful if you want to explicitly declare your component's types.

  ```scala
  JsComponentWithFacade[P, S, Facade, CT]
  JsComponent.UnmountedWithFacade[P, S, Facade]
  JsComponent.MountedWithFacade[P, S, Facade]
  ```

* `_Simple` -
  They allow you to reference JS components more generically and with less constraints.
  Useful in library methods that do something with any kind of JS component,
  regardless of whether it's been mapped or modified after creation.

  ```scala
  JsComponent.ComponentSimple[P, CT, U]
  JsComponent.UnmountedSimple[P, M]
  JsComponent.MountedSimple[F, P, S, R]
  ```

* Raw types. I don't imagine library users would need to use this but just in case, there is also:

```scala
  JsComponent.RawMounted[P, S] // the type of the raw JS mounted value without additional facades.
  JsComponent.ComponentWithRawType[P, S, R, CT]
  JsComponent.UnmountedWithRawType[P, S, R]
  JsComponent.MountedWithRawType[P, S, R]
  ```


### JS functional components

* Main types.
  Useful if you want to explicitly declare your component's types.

  ```scala
  JsFnComponent[P, CT]
  JsFnComponent.Unmounted[P]
  JsFnComponent.Mounted
  ```

* `_Simple` -
  They allow you to reference JS functional components more generically and with less constraints.
  Useful in library methods that do something with any kind of JS functional component,
  regardless of whether it's been mapped or modified after creation.

  ```scala
  JsFnComponent.ComponentSimple[P, CT, U]
  JsFnComponent.UnmountedSimple[P, M]
  ```


### Scala components

The type parameter `B` below, is the type of the Scala component's *backend*.

* Main types:

```scala
  ScalaComponent[P, S, B, CT]
  ScalaComponent.Unmounted[P, S, B]
  ScalaComponent.Mounted[F, P, S, B]
  ScalaComponent.MountedImpure[P, S, B]
  ScalaComponent.MountedPure[P, S, B]
  BackendScope[P, S]
  ```

* `ScalaComponentConfig[P, Children, S, B]` -
  When creating reusable component features (like mixins), use this as the return type.
  The feature is then applied by calling `.configure(feature)`
  when creating a Scala component.
  Example:
  ```scala
  object OnUnmount {
    def install[P, C <: Children, S, B <: OnUnmount]: ScalaComponentConfig[P, C, S, B] =
      _.componentWillUnmount(_.backend.unmount)
    }
  ```

* JS types.
  Useful if you want to deconstruct Scala components into their underlying JS representations.

  ```scala
  ScalaComponent.JsComponent[P, S, B, CT]
  ScalaComponent.JsUnmounted[P, S, B]
  ScalaComponent.JsMounted[P, S, B]
  ScalaComponent.RawMounted[P, S, B] // the type of the raw JS mounted value
  ScalaComponent.Vars[P, S, B] // the JS facade over the raw JS mounted value
  ```


### Scala functional components

* Main types.
  Useful if you want to explicitly declare your component's types.

  ```scala
  ScalaFnComponent[P, CT]
  ScalaFnComponent.Unmounted[P]
  ScalaFnComponent.Mounted
  ```

### Roots
For all component types, whether they be generic, JS, Scala; functional or standard; each type parameter
(i.e. the props, the state, the result of mounting, etc.) can be changed.
In order to preserve transparency and lineage, the root/original/base component is never forgotten and always accessible by calling `.root`.

You'll likely never use or care about these types unless you're doing library interop.
But if you need them, they're there, and they are as follows:

* `_Root` - this is the type before any mapping or modification occurs. The original, underlying type.

```scala
  GenericComponent.ComponentRoot[P, CT, U]
  GenericComponent.UnmountedRoot[P, M]
  GenericComponent.MountedRoot[F, P, S]
  JsComponent.ComponentRoot[P, CT, U]
  JsComponent.UnmountedRoot[P, M]
  JsComponent.MountedRoot[F, P, S, R]
  JsFnComponent.ComponentRoot[P, CT, U]
  JsFnComponent.UnmountedRoot[P]
  ScalaComponent.MountedRoot[F, P, S, B]
  ```

* `_WithRoot` - this the type that is possibly mapped/modified, and contains a `.root` method to the `_Root` type.

```scala
  GenericComponent.ComponentWithRoot[…]
  GenericComponent.UnmountedWithRoot[…]
  GenericComponent.MountedWithRoot[…]
  JsComponent.ComponentWithRoot[…]
  JsComponent.UnmountedWithRoot[…]
  JsComponent.MountedWithRoot[…]
  JsFnComponent.ComponentWithRoot[…]
  JsFnComponent.UnmountedWithRoot[…]
  ScalaComponent.MountedWithRoot[…]
  ```

# State

There are a number of abstraction over state functionality constituents.
In the following types `F` is the effect type which is nearly always
`CallbackTo` for pure code, and `Id` where `type Id[A] = A` for impure usage.

### `StateAccess`

Read- and write- access to mutable state.
All mounted components extend this.
Useful for passing around R/W access to one component's state (or state subset), to a child component.
Lifecycle scopes (eg. `componentDidUpdate`) do *not* extend this as in some cases, `.state` would be ambiguous.

```scala
StateAccess[F, S]
StateAccessPure[S]
StateAccessImpure[S]
```

### `StateAccessor`

Typeclass for pure and/or impure, read- and/or write-access to mutable state.
Useful in library methods that do something with mutable state.
Unless `StateAccess`, lifecycle scopes (eg. `componentDidUpdate`) are supported via this method.
See [extra/StateSnapshot.scala](../extra/src/main/scala/japgolly/scalajs/react/extra/StateSnapshot.scala) for an example.

```scala
StateAccessor.Read[I, F, S]
StateAccessor.ReadPure[I, S]
StateAccessor.ReadImpure[I, S]
StateAccessor.Write[I, F, S]
StateAccessor.WritePure[I, S]
StateAccessor.WriteImpure[I, S]
StateAccessor.ReadWrite[I, F, F, S]
StateAccessor.ReadWritePure[I, S]
StateAccessor.ReadWriteImpure[I, S]
StateAccessor.ReadImpureWritePure[I, S]
StateAccessor.ReadPureWriteImpure[I, S]
```

### {Set,Mod}State abstractions

Firstly, `StateAccess` extends the following subtypes which can be used in isolation:

```scala
StateAccess.SetState[F, S]
StateAccess.ModState[F, S]
StateAccess.ModStateWithProps[F, P, S]
StateAccess.Write[F, S] // SetState & ModState
StateAccess.WriteWithProps[F, P, S] // Write & ModStateWithProps
```

Secondly, there are the following classes that
* require a single function for construction
* extend Scala functions
* extend an appropriate `StateAccess` trait
* allows call-site to choose the set/mod-state variation they need (eg. `setState(s)` vs `setState(Option(s), cb)`)

```scala
SetStateFn[F, S]
ModStateFn[F, S]
ModStateWithPropsFn[F, P, S]

// And aliases:
SetStateFnPure[S]
SetStateFnImpure[S]
ModStateFnPure[S]
ModStateFnImpure[S]
ModStateWithPropsFnPure[P, S]
ModStateWithPropsFnImpure[P, S]
```

# Events

The synthetic event types you read about in the [React docs](https://facebook.github.io/react/docs/events.html)
are typed as shown below.

The `Node` type param refers to the type of target DOM.

| React Event Type | Alias without typing the target  |
| ---- | ---- |
| `ReactEventFrom[Node]` | `ReactEvent` |
| `ReactClipboardEventFrom[Node]` | `ReactClipboardEvent` |
| `ReactCompositionEventFrom[Node]` | `ReactCompositionEvent` |
| `ReactDragEventFrom[Node]` | `ReactDragEvent` |
| `ReactFocusEventFrom[Node]` | `ReactFocusEvent` |
| `ReactKeyboardEventFrom[Node]` | `ReactKeyboardEvent` |
| `ReactMouseEventFrom[Node]` | `ReactMouseEvent` |
| `ReactTouchEventFrom[Node]` | `ReactTouchEvent` |
| `ReactUIEvent[Node]` | `ReactUIEvent` |
| `ReactWheelEventFrom[Node]` | `ReactWheelEvent` |

You can also append one of these suffixes for convenience:

| Suffix | Node |
| ------ | ---- |
| `FromHtml` | `HTMLElement` |
| `FromInput` | `HTMLInputElement` |
| `FromTextArea` | `HTMLTextAreaElement` |

For example, `ReactDragEventFromInput` is a `ReactDragEvent` over a `HTMLInputElement` (an `<input>`), the same as writing `ReactDragEventFrom[HTMLInputElement]`.

Additionally there's `object ReactMouseEvent` which contains a utility for determining if a mouse event
on a link should open in a new tab.


# VDOM

VDOM types are [described here](VDOM.md#types).
