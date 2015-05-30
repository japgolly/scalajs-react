Usage
=====

This will attempt to show you how to use React in Scala.

It is expected that you know how React itself works.

#### Contents
- [Setup](#setup)
- [Creating Virtual-DOM](#creating-virtual-dom)
- [Creating Components](#creating-components)
- [React Extensions](#react-extensions)
- [Differences from React proper](#differences-from-react-proper)
- [Gotchas](#gotchas)

Setup
=====

1. Add [Scala.js](http://www.scala-js.org) to your project.

2. Add *scalajs-react* to SBT:

  ```scala
  // core = essentials only. No bells or whistles.
  libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "0.9.0"

  // React.JS itself
  // Note the JS filename. Can also be react.js, react.min.js, or react-with-addons.min.js.
  jsDependencies +=
    "org.webjars" % "react" % "0.12.2" / "react-with-addons.js" commonJSName "React"
  ```

Creating Virtual-DOM
====================

scalajs-react uses a specialised copy of
[@lihaoyi](https://twitter.com/li_haoyi)'s [Scalatags](https://github.com/lihaoyi/scalatags)
to build virtual DOM.

There are two built-in ways of creating virtual-DOM.

1. **Prefixed (recommended)** - Importing DOM tags and attributes under prefixes is recommended. Apart from essential implicit conversions, only two names are imported: `<` for tags, `^` for attributes.

  ```scala
  import japgolly.scalajs.react.vdom.prefix_<^._

  <.ol(
    ^.id     := "my-list",
    ^.lang   := "en"
    ^.margin := "8px",
    <.li("Item 1"),
    <.li("Item 2"))
  ```

2. **Global** - You can import all DOM tags and attributes into the global namespace. Beware that doing so means that you will run into confusing error messages and IDE refactoring issues when you use names like `id`, `a`, `key` for your variables and parameters.

  ```scala
  import japgolly.scalajs.react.vdom.all._

  ol(
    id     := "my-list",
    lang   := "en"
    margin := "8px",
    li("Item 1"),
    li("Item 2"))
  ```

#### Callbacks

There are two ways of wiring up events to vdom.

1. **`attr ==> handler`** where `handler` is in the shape of `ReactEvent => Unit`, an event handler.
  Event types are described in [TYPES.md](types.md).

  ```scala
  def onTextChange(e: ReactEventI): Unit = {
    println("Value received = " + e.target.value)
  }

  ^.input(
    ^.`type`    := "text",
    ^.value     := currentValue,
    ^.onChange ==> onTextChange)
  ```

2. **`attr --> proc`** where `proc` is in the shape of `(=> Unit)`, a procedure.

  ```scala
  def onButtonPressed: Unit = {
    println("The button was pressed!")
  }

  ^.button(
    ^.onClick --> onButtonPressed,
    "Press me!")
  ```

#### Optional markup

* `boolean ?= markup` - Ignores `markup` unless `boolean` is `true`.

  ```scala
  def hasFocus: Boolean = ???

  <.div(
    hasFocus ?= (^.color := "green"),
    "I'm green when focused.")
  ```

* Attributes, styles, and tags can be wrapped in `Option` or `js.UndefOr` to make them optional.

  ```scala
  val loggedInUser: Option[User] = ???

  ^.div(
    <.h3("Welcome"),
    loggedInUser.map(user =>
      <.a(
        ^.href := user.profileUrl,
        "My Profile")))
  ```

* `EmptyTag` - A virtual DOM building block representing nothing.

  ```scala
  ^.div(if (allowEdit) editButton else EmptyTag)
  ```

#### Custom markup elements

The vdom imports will add string extension methods that allow you to create you own custom tags, attributes and styles.

```scala
val customAttr  = "customAttr" .reactAttr
val customStyle = "customStyle".reactStyle
val customTag   = "customTag"  .reactTag

// Produces: <customTag customAttr="hello" style="customStyle:123;">bye</customTag>
customTag(customAttr := "hello", customStyle := "123", "bye")
```
↳ produces ↴
```html
<customTag customAttr="hello" style="customStyle:123;">bye</customTag>
```


Creating Components
===================

Provided is a component builder DSL called `ReactComponentB`.

You throw types and functions at it, call `build` (or `buildU`) and when it compiles you will have a React component.

You first specify your component's properties type, and a component name.
```scala
ReactComponentB[Props]("MyComponent")
```

Next you keep calling functions on the result until you get to a `build` method.
If your props type is `Unit`, use `buildU` instead to be able to instantiate your component with having to pass `()` as a constructor argument.

For a list of available methods, let your IDE guide you or see the
[source](../core/src/main/scala/japgolly/scalajs/react/ReactComponentB.scala).

The result of the `build` function will be an object that acts like a class.
You must create an instance of it to use it in vdom.

(`ReactComponent` types are described in [TYPES.md](TYPES.md).)

Example:
```scala
// Creation
val Hello = ReactComponentB[String]("Hello <name>")
  .render(name => <.div("Hello ", name))
  .build

// Usage
<.div(
  Hello("John"),
  Hello("Jane"))
```

#### Backends

In addition to props and state, if you look at the React samples you'll see that most components need additional functions and even (in the case of React's second example, the timer example), state outside of the designated state object (!). In this Scala version, all of that can be lumped into some arbitrary class you may provide, called a *backend*.

See the [online timer demo](http://japgolly.github.io/scalajs-react/#examples/timer) for an example.

React Extensions
================

* Where `this.setState(State)` is applicable, you can also run `modState(State => State)`.

* `SyntheticEvent`s have aliases that don't require you to provide the dom type. So instead of `SyntheticKeyboardEvent[xxx]` type alias `ReactKeyboardEvent` can be used.

* The component builder has a `propsDefault` method which takes some default properties and exposes constructor methods that 1) don't require any property specification, and 2) take an `Optional[Props]`.

* The component builder has a `propsAlways` method which provides all component instances with given properties, doesn't allow property specification in the constructor.

* React has a [classSet addon](https://facebook.github.io/react/docs/class-name-manipulation.html)
  for specifying multiple optional class attributes. The same mechanism is applicable with this library is as follows:

  ```scala
  <.div(
    ^.classSet(
      "message"           -> true,
      "message-active"    -> true,
      "message-important" -> props.isImportant,
      "message-read"      -> props.isRead),
    props.message)

  // Or for convenience, put all constants in the first arg:
  <.div(
    ^.classSet1(
      "message message-active",
      "message-important" -> props.isImportant,
      "message-read"      -> props.isRead),
    props.message)
  ```

* Sometimes you want to allow a function to both get and affect a portion of a component's state. Anywhere that you can call `.setState()` you can also call `focusState()` to return an object that has the same `.setState()`, `.modState()` methods but only operates on a subset of the total state.

  ```scala
  def incrementCounter(s: CompStateFocus[Int]): Unit =
    s.modState(_ + 1)

  // Then later in a render() method
  val f = $.focusState(_.counter)((a,b) => a.copy(counter = b))
  button(onclick --> incrementCounter(f), "+")
  ```

  *(Using the Monocle extensions greatly improve this approach.)*

Differences from React proper
=============================

* To keep a collection together when generating the dom, call `.toJsArray`. The only difference I'm aware of is that if the collection is maintained, React will issue warnings if you haven't supplied `key` attributes. Example:

  ```scala
  <.tbody(
    <.tr(
      <.th("Name"),
      <.th("Description"),
      <.th("Etcetera")),
    myListOfItems.sortBy(_.name).map(renderItem).toJsArray
  ```

* To specify a `key` when creating a React component, instead of merging it into the props, call `.set(key = ...)` or `withKey(...)` before providing the props and children.

  ```scala
  val Example = ReactComponentB[String]("Eg").render(i => h1(i)).build
  Example.withKey("key1")("The Prop")
  ```

### Refs
Rather than specify references using strings, the `Ref` object can provide some more safety.

* `Ref(name)` will create a reference to both apply to and retrieve a plain DOM node.

* `Ref.to(component, name)` will create a reference to a component so that on retrieval its types are preserved.

* `Ref.param(param => name)` can be used for references to items in a set, with the key being a data entity's ID.

* Because refs are not guaranteed to exist, the return type is wrapped in `js.UndefOr[_]`. A helper method `tryFocus()` has been added to focus the ref if one is returned.

  ```scala
  val myRef = Ref[HTMLInputElement]("refKey")

  class Backend($: BackendScope[Props, String]) {
    def clearAndFocusInput(): Unit =
     $.setState("", () => myRef(t).tryFocus())
  }
  ```

Gotchas
=======

* `table(tr(...))` will appear to work fine at first then crash later. React needs `table(tbody(tr(...)))`.

* React doesn't apply invocations of `this.setState` until the end of `render` or the current callback. Calling `.state` after `.setState` will return the original value, ie. `val s1 = x.state; x.setState(s2); x.state == s1 // not s2`.
  If you want to compose state modifications (and you're using Scalaz), take a look at the `ScalazReact` module, specifically `ReactS` and `runState`.
