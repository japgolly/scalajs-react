Usage
=====

This will attempt to show you how to use React in Scala.

It is expected that you know how React itself works.

#### Contents
- [Setup](#setup)
- [Creating Virtual-DOM](#creating-virtual-dom)
- [Creating Components](#creating-components)
- [Using Components](#using-components)
- [React Extensions](#react-extensions)
- [Differences from React proper](#differences-from-react-proper)
- [Using JS Components](#using-js-components)
- [Gotchas](#gotchas)

Setup
=====

1. Add [Scala.js](http://www.scala-js.org) to your project.

2. Add *scalajs-react* to SBT:

  ```scala
  // core = essentials only. No bells or whistles.
  libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "0.10.0"

  // React JS itself (Note the filenames, adjust as needed, eg. to remove addons.)
  jsDependencies += "org.webjars.npm" % "react"     % "0.14.0" / "react-with-addons.js" commonJSName "React"    minified "react-with-addons.min.js",
  jsDependencies += "org.webjars.npm" % "react-dom" % "0.14.0" / "react-dom.js"         commonJSName "ReactDOM" minified "react-dom.min.js" dependsOn "react-with-addons.js",
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
    ^.lang   := "en",
    ^.margin := "8px",
    <.li("Item 1"),
    <.li("Item 2"))
  ```

2. **Global** - You can import all DOM tags and attributes into the global namespace. Beware that doing so means that you will run into confusing error messages and IDE refactoring issues when you use names like `id`, `a`, `key` for your variables and parameters.

  ```scala
  import japgolly.scalajs.react.vdom.all._

  ol(
    id     := "my-list",
    lang   := "en",
    margin := "8px",
    li("Item 1"),
    li("Item 2"))
  ```

#### Callbacks

Callbacks are represented by:
* `Callback` which doesn't return a result.
* `CallbackTo[A]` which returns an `A`.

Actually `Callback` is `CallbackTo[Unit]` with a different companion object, full of different goodies that all return `Unit`.

You can create callbacks in a number of ways:

* By wrapping your async code:

  ```scala
  Callback{ println("Hello! I'll be executed later.") }
  ```

* When your component modifies its state via `.setState` or `.modState`, you are provided a `Callback` for the operation.

  ```scala
  $.modState(_.copy(name = newName)) // returns a Callback
  ```

* Using one of the `Callback` object convenience methods

  ```scala
  // Convenience for calling `dom.console.log`.
  Callback.log("Hello Console reader.")

  // Provides both compile-time and runtime warnings that a callback isn't implemented yet.
  Callback.TODO("AJAX not implemented yet")

  // Return a pure value without doing anything
  CallbackTo.pure(0)
  ```

`Callback` also has all kinds of useful methods and combinators. Examples:
* Join callbacks together with many methods like `map`, `flatMap`, `tap`, `flatTap`, and all the squigglies that
  you may be used to in Haskell and inspired libraries like `*>`, `<*`, `>>`, `<<`, `>>=`, etc.
* `.attempt` to catch any error in the callback and handle it.
* `.async`/`.delay(n)` to run asynchronously and return a `Future`.
* `.logResult` to print the callback result before returning it.
* `.logDuration` to measure and log how long the callback takes.

#### Event Handlers

There are two ways of attaching event handlers to your virtual DOM.

1. **`<attribute> --> <procedure>`**

  `<attribute>` is a DOM attribute like `onClick`, `onChange`, etc.<br>
  `<procedure>` is a `Callback` (see above) which doesn't need any input.

  ```scala
  def onButtonPressed: Callback =
    Callback.alert("The button was pressed!")

  <.button(
    ^.onClick --> onButtonPressed,
    "Press me!")
  ```

2. **`<attribute> ==> <handler>`**

  `<attribute>` is a DOM attribute like `onClick`, `onChange`, etc.<br>
  `<handler>` is a `ReactEvent => Callback`.
  `ReactEvent` can be more specific - event types are described in [TYPES.md](TYPES.md).

  ```scala
  def onTextChange(e: ReactEventI): Callback =
    Callback.alert("Value received = " + e.target.value)

  <.input(
    ^.`type`    := "text",
    ^.value     := currentValue,
    ^.onChange ==> onTextChange)
  ```

A helpful way to remember which operator to use is to visualise the arrow stem:
<br>`==>` - The `========` has a gap in the middle - it's a pipe for data to come through meaning it expects `? => Callback`.
<br>`-->` - The `--------` has no gap - it's just a wire to a `Callback`.


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

  <.div(
    <.h3("Welcome"),
    loggedInUser.map(user =>
      <.a(
        ^.href := user.profileUrl,
        "My Profile")))
  ```

* Callbacks can be made optional by adding a `?` to the `-->`/`==>` op, and
  wrapping the callback in `Option` or `js.UndefOr`.

  ```scala
  val currentValue: Option[String] = ???

  def onTextChange(e: ReactEventI): Option[Callback] =
    currentValue.map { before =>
      def after = e.target.value
      Callback.alert(s"Value changed from [$before] to [$after]")
    }

  <.input(
    ^.`type`     := "text",
    ^.value      := currentValue.getOrElse(""),
    ^.onChange ==>? onTextChange)
  ```

* `EmptyTag` - A virtual DOM building block representing nothing.

  ```scala
  <.div(if (allowEdit) editButton else EmptyTag)
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
val NoArgs =
  ReactComponentB[Unit]("No args")
    .render(_ => <.div("Hello!"))
    .buildU

val Hello =
  ReactComponentB[String]("Hello <name>")
    .render(name => <.div("Hello ", name))
    .build
```

#### Backends

In addition to props and state, if you look at the React samples you'll see that most components need additional functions and even (in the case of React's second example, the timer example), state outside of the designated state object (!). In this Scala version, all of that can be lumped into some arbitrary class you may provide, called a *backend*.

See the [online timer demo](http://japgolly.github.io/scalajs-react/#examples/timer) for an example.

For the extremely common case of having a backend class with a render method, `ReactComponentB` comes with a `.renderBackend` method.
It will locate the `render` method, determine what the arguments need (props/state/propsChildren) by examining the
types or the arg names when the types are ambiguous, and create the appropriate function at compile-time.
If can also automate the creation of the backend, see below.

Before:
```scala
type State = Vector[String]

class Backend($: BackendScope[Unit, State]) {
  def render = {
    val s = $.state
    <.div(
      <.div(s.length, " items found:"),
      <.ol(s.map(i => <.li(i))))
  }
}

val Example = ReactComponentB[Unit]("Example")
  .initialState(Vector("hello", "world"))
  .backend(new Backend(_))
  .render(_.backend.render)
  .buildU
```

After:
```scala
class Backend($: BackendScope[Unit, State]) {
  def render(s: State) =   // ← Accept props, state and/or propsChildren as argument
    <.div(
      <.div(s.length, " items found:"),
      <.ol(s.map(i => <.li(i))))
}

val Example = ReactComponentB[Unit]("Example")
  .initialState(Vector("hello", "world"))
  .renderBackend[Backend]  // ← Use Backend class and backend.render
  .buildU
```

You can also create a backend yourself and still use `.renderBackend`:
```scala
val Example = ReactComponentB[Unit]("Example")
  .initialState(Vector("hello", "world"))
  .backend(new Backend(_)) // ← Fine! Do it yourself!
  .renderBackend           // ← Use backend.render
  .buildU
```

Using Components
================

Once you've created a Scala React component, it mostly acts like a typical Scala case class.
To use it, you create an instance.
To create an instance, you call the constructor.

```scala
val NoArgs =
  ReactComponentB[Unit]("No args")
    .render(_ => <.div("Hello!"))
    .buildU

val Hello =
  ReactComponentB[String]("Hello <name>")
    .render(name => <.div("Hello ", name))
    .build

// Usage
<.div(
  NoArgs(),
  Hello("John"),
  Hello("Jane"))
```

Component classes provides other methods:

| Method | Desc |
|--------|------|
| `withKey(js.Any)` | Apply a (React) key to the component you're about to instantiate. |
| `withRef(String | Ref)` | Attach a (React) reference to the component you're about to instantiate. |
| `set(key = ?, ref = ?)` | Alternate means of setting one or both of the above. |
| `reactClass` | The React component (constructor) in pure JS (i.e. without the Scala wrapping). |
| `withProps(=> Props)` | Using the given props fn, return a no-args component. |
| `withDefaultProps(=> Props)` | Using the given props fn, return a component which optionally accepts props in its constructor but also allows instantiation without specifying. |

Examples:

```scala
val Hello2 = Hello.withDefaultProps("Anonymous")

<.div(
  NoArgs.withKey("noargs-1")(),
  NoArgs.withKey("noargs-2")(),
  Hello2(),
  Hello2("Bob"))
```

#### Rendering

To render a component, it's the same as React. Use `React.render` and specify a target in the DOM.

```scala
import org.scalajs.dom.document

React.render(NoArgs(), document.body)
```

React Extensions
================

* Where `setState(State)` is applicable, you can also run `modState(State => State)`.

* `SyntheticEvent`s have numerous aliases that reduce verbosity.
  For example, in place of `SyntheticKeyboardEvent[HTMLInputElement]` you can use `ReactKeyboardEventI`.
  See [TYPES.md](types.md) for details.

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

  *(Using the [Monocle extensions](FP.md) greatly improve this approach.)*

Differences from React proper
=============================

* In React JS you access a component's children via `this.props.children`.
  In Scala, instances of `ComponentScope{U,M,WU}` and `BackendScope` provide a `.propsChildren` method.
  There is also a `.propsDynamic` method as a shortcut to access the children as a `js.Dynamic`.

* To keep a collection together when generating the dom, call `.toJsArray`. The only difference I'm aware of is that if the collection is maintained, React will issue warnings if you haven't supplied `key` attributes. Example:

  ```scala
  <.tbody(
    <.tr(
      <.th("Name"),
      <.th("Description"),
      <.th("Etcetera")),
    myListOfItems.sortBy(_.name).map(renderItem).toJsArray
  ```

* To specify a `key` when creating a React component, instead of merging it into the props,
  apply it to the component class as described in [Using Components](#using-components).

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

Using JS Components
===================

*Added in v0.9.2. Kindly contributed by [@imcharsi](https://github.com/imcharsi) - Thank you!*

First, let's create a simple example with only JavaScript, then convert it into scalajs-react example that uses Scala facades.

Below is `sampleReactComponent.js`.
```js
var SampleReactComponent = React.createClass({
  getInitialState: function() {
    return {num:0,num2:0};
  },
  render: function() {
    return React.createElement("div", null, this.props.propOne);
  },
  getNum:function() {
    return this.state.num;
  },
  setNum:function(n) {
    this.setState({num:n});
  }
});
```

Below is `main.jsx`.
```js
var factory = React.createFactory(SampleReactComponent);
React.render(factory({propOne:"123"}), document.body);
```

First, copy JS Component library file that you want to use into Scala.JS resource directory.

Next, you need an SBT configuration like below.
You may want to see [here](http://www.scala-js.org/doc/sbt/depending.html).
(Feel free to change the `react-with-addons.js` filename according to your own case.)
```scala
jsDependencies += (ProvidedJS / "sampleReactComponent.js" dependsOn "react-with-addons.js")
```

Next, declare some Scala facades for your JS Components. You may want to see [here](http://www.scala-js.org/doc/calling-javascript.html) too.

```scala
trait SampleReactComponentProperty extends js.Object {
  val propOne: js.UndefOr[String] = js.native
}

trait SampleReactComponentState extends js.Object {
  val num: js.UndefOr[Int] = js.native
  val num2: js.UndefOr[Int] = js.native
}

@JSName("SampleReactComponent")
object SampleReactComponent
  extends JsComponentType[SampleReactComponentProperty, SampleReactComponentState, HTMLElement]

trait SampleReactComponentM
    extends JsComponentM[SampleReactComponentProperty, SampleReactComponentState, HTMLElement] {
  def getNum(): Int = js.native
  def setNum(n: Int): Unit = js.native
}
```

As with all Scala.JS facades, you will need some boilerplate code to act as a bridge between the JS and easier-to-use Scala. Below is an example of such boilerplate utility code:
```scala
object SampleReactComponentProperty {
  def apply(ref: js.UndefOr[String] = js.undefined, propOne: js.UndefOr[String] = js.undefined): SampleReactComponentProperty = {
    val p = js.Dynamic.literal()

    ref.foreach(p.updateDynamic("ref")(_))
    propOne.foreach(p.updateDynamic("propOne")(_))

    p.asInstanceOf[SampleReactComponentProperty]
  }
}

object SampleReactComponentState {
  def apply(prevState: SampleReactComponentState)(
    num: js.UndefOr[Int] = js.undefined,
    num2: js.UndefOr[Int] = js.undefined): SampleReactComponentState = {
    val p = js.Dynamic.literal()

    num.orElse(prevState.num).foreach(p.updateDynamic("num")(_))
    num2.orElse(prevState.num2).foreach(p.updateDynamic("num2")(_))

    p.asInstanceOf[SampleReactComponentState]
  }
}
```

Bow let's use the previously-mentioned `main.jsx` in scalajs-react.
In this example we'll wrap it in a Scala component with a Scala-based backend.

```scala
class XxxBackend(scope: BackendScope[Unit, Unit]) {
  def modifyOne(i: Int): Unit = {
    ref(scope).foreach(_.setNum(i))
  }

  def modifyTwo(i: Int): Unit = {
    ref(scope).foreach(c => c.setState(SampleReactComponentState(c.state)(num2 = i)))
  }
  ...
}

val ref = Ref.toJS[SampleReactComponentM]("ref123")

val component = ReactComponentB[Unit]("S").stateless.backend(new XxxBackend(_)).render { scope =>
  val factory = React.createFactory(SampleReactComponent)
  factory(SampleReactComponentProperty(ref = ref, propOne = "123"))
}.buildU

React.render(component(), dom.document.body)
```

From this point on, the usage is the same as with normal scalajs-react components.

**NOTE**: When creating a JS component's state facade, do not use `var`s, or at least do not modify them directly if you do.

For example, don't do this:
```scala
trait SampleReactComponentState extends js.Object {
  var num: js.UndefOr[Int] = js.native     // using var
}

mountedComponent.foreach(_.state.var = 1)  // BAD: don't modify directly
```

Instead, ensure you call `setState`. Example:
```scala
trait SampleReactComponentState extends js.Object {
  val num: js.UndefOr[Int] = js.native   // using val, not var
}

mountedComponent.foreach(c =>            // GOOD: call setState
  c.setState(SampleReactComponentState(c.state)(num2 = 1)))
```

Gotchas
=======

* `table(tr(...))` will appear to work fine at first then crash later. React needs `table(tbody(tr(...)))`.

* React's `setState` is asynchronous; it doesn't apply invocations of `this.setState` until the end of `render` or the current callback. Calling `.state` after `.setState` will return the initial, original value, i.e.

  ```scala
  val s1 = $.state
  val s2 = "new state"
  $.setState(s2)
  $.state == s2 // returns false
  $.state == s1 // returns true
  ```

  If this is a problem you have 2 choices.

  1. Use `modState`.
  2. Refactor your logic so that you only call `setState` once.
  3. Use Scalaz state monads as demonstrated in the online [state monad example](https://japgolly.github.io/scalajs-react/#examples/state-monad).

* Type-inference when creating vdom can break if you call a function whose return type is also infered.

  Example: `Option.getOrElse`.

  If you have an `Option[A]`, the return type of `getOrElse` is not always `A`.
  This is because the `A` in `Option` is covariant, and so instead of `getOrElse(default: => A): A`
  *(which would avoid this vdom type-inference problem)*, it's actually `getOrElse[B >: A](default: => B): B`.

  This confuses Scala:
  ```scala
  def problem(name: Option[String]) =
    <.div(^.title := name.getOrElse("No Name"))
  ```

  Workarounds:
  ```scala
  // Workaround #1: Move the call outside.
  def workaround1(nameOption: Option[String]) = {
    val name = nameOption getOrElse "No Name"
    <.div(^.title := name)
  }

  // Workaround #2: Specify the type manually.
  def workaround2(name: Option[String]) =
    <.div(^.title := name.getOrElse[String]("No Name"))
  ```
