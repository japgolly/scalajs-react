Usage
=====

This will attempt to show you how to use React in Scala.

It is expected that you know how React itself works.

#### Contents
- [Setup](#setup)
- [Creating Virtual-DOM](#creating-virtual-dom)
- [Callbacks](#callbacks)
- [Creating Components](#creating-components)
- [Using Components](#using-components)
- [React Extensions](#react-extensions)
- [Gotchas](#gotchas)

Setup
=====

1. Add [Scala.js](http://www.scala-js.org) to your project.

2. Add *scalajs-react* to SBT:

  ```scala
  // core = essentials only. No bells or whistles.
  libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "1.4.2"
  ```

3. Add React to your build.

    How to do this depends on your Scala.JS config and build setup.

    If you're using [scalajs-bundler](https://scalacenter.github.io/scalajs-bundler/),
    add the following SBT settings to get started:

    ```scala
      enablePlugins(ScalaJSPlugin)

      enablePlugins(ScalaJSBundlerPlugin)

      libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "1.4.2"

      npmDependencies in Compile ++= Seq(
        "react" -> "16.7.0",
        "react-dom" -> "16.7.0")
    ```

    If you're using old-school `jsDependencies`, add something akin to:

    ```scala
    // React JS itself (Note the filenames, adjust as needed, eg. to remove addons.)
    jsDependencies ++= Seq(

      "org.webjars.npm" % "react" % "16.7.0"
        /        "umd/react.development.js"
        minified "umd/react.production.min.js"
        commonJSName "React",

      "org.webjars.npm" % "react-dom" % "16.7.0"
        /         "umd/react-dom.development.js"
        minified  "umd/react-dom.production.min.js"
        dependsOn "umd/react.development.js"
        commonJSName "ReactDOM",

      "org.webjars.npm" % "react-dom" % "16.7.0"
        /         "umd/react-dom-server.browser.development.js"
        minified  "umd/react-dom-server.browser.production.min.js"
        dependsOn "umd/react-dom.development.js"
        commonJSName "ReactDOMServer"),
    ```

If you see the error related to `js-tokens` (such as `org.webjars.npm#js-tokens;[3.0.0,4),[4.0.0,5): not found`), then add the following line to `build.sbt`:

```
dependencyOverrides += "org.webjars.npm" % "js-tokens" % "3.0.2"
```

[See here](IDE.md) for tips on configuring your IDE.


Creating Virtual-DOM
====================

See [VDOM.md](VDOM.md).


Callbacks
=========

See [CALLBACK.md](CALLBACK.md).


Creating Components
===================

This is how to create components from Scala.
(For JS components, see [INTEROP.md](INTEROP.md).)

There is a component builder DSL beginning at `ScalaComponent.build`.
You throw types and functions at it, call `build` and when it compiles you will have a React component.

1. The first step is to specify your component's properties type, and a component name.
  ```scala
  import japgolly.scalajs.react._
  import japgolly.scalajs.react.vdom.html_<^._

  object MyComponent {

    case class Props(/* TODO */)

    val myComponent =
      ScalaComponent.builder[Props]("MyComponent")
        |
  }
  ```

2. *(Optional)* If you want a stateful component,
  call one of the methods beginning with `.initialState`.
  Use your IDE to see the methods and the differences in their type signatures.

3. *(Optional)* If you want a backend (explained below) for your component
  (and you do for non-trivial components), call `.backend`.
  If your backend has a `.render` function, instead of `.backend` here you can call `.renderBackend`
  which will use a macro to instantiate your backend, and automatically choose the
  appropriate `.render` function in the next step, bypassing it for you.

4. Choose from one of the many available `render` functions.
   Use your IDE to see the methods and the differences in their type signatures.
   Alternatively, if you (for whatever reason) manually created a backend in the previous step
   and your backend has a `render` function, you can call `.renderBackend` here to have the
   builder automatically select the appropriate `render` function.

5. *(Optional)* Type in the name of one of the React lifecycle hooks (eg. `componentDidMount`)
  to add that hook to your component.

6. Call `.build` and you're done.
   <br>If your props is a singleton type (eg. `Unit`) then the buider automatically
   (i.e. via implicit resolution) provides your component a `CtorType` that doesn't the
   props be specified. (See [TYPES.md](TYPES.md) for more info.)

Example with props:
```scala
val Hello =
  ScalaComponent.builder[String]("Hello")
    .render_P(name => <.div("Hello there ", name))
    .build

// Usage:
Hello("Draconus")
```

Example without props:
```scala
val NoArgs =
  ScalaComponent.builder[Unit]("No args")
    .renderStatic(<.div("Hello!"))
    .build

// Usage:
NoArgs()
```

#### Backends

In addition to props and state, if you look at the React samples you'll see that most components need additional functions
and even, (in the case of React's second example, the timer example), state outside of the designated state object (!).
In plain React with JS, functions which can have access to the component's props and state
(such as helpers fns and event handlers),
are placed within the body of the component class.
In scalajs-react you need another place for such functions as scalajs-react
emphasises type-safety and provides different types for the component's scope at different points in the lifecycle.
Instead they should be placed in some arbitrary class you may provide, called a *backend*.

See the [online timer demo](http://japgolly.github.io/scalajs-react/#examples/timer) for an example.

As mentioned above, for the extremely common case of having a backend class with a `render` method,
the builder comes with a `.renderBackend` method.
It will locate the `render` method, determine what the arguments need (props/state/propsChildren) by examining the
types or the arg names when the types are ambiguous, and create the appropriate function at compile-time.
If can also automate the creation of the backend, see below.

Example before: *(yuk!)*
```scala
type State = Vector[String]

class Backend(bs: BackendScope[Unit, State]) {
  def render: VdomElement = {
    val s = bs.state.runNow()  // yuk!! .runNow() is unsafe
    <.div(
      <.div(s.length, " items found:"),
      <.ol(s.toTagMod(i => <.li(i))))
  }
}

val Example = ScalaComponent.builder[Unit]("Example")
  .initialState(Vector("hello", "world"))
  .backend(new Backend(_))
  .render(_.backend.render)
  .build
```

After:
```scala
class Backend(bs: BackendScope[Unit, State]) {
  def render(s: State): VdomElement = // ← Accept props, state and/or propsChildren as argument
    <.div(
      <.div(s.length, " items found:"),
      <.ol(s.toTagMod(i => <.li(i))))
}

val Example = ScalaComponent.builder[Unit]("Example")
  .initialState(Vector("hello", "world"))
  .renderBackend[Backend]  // ← Use Backend class and backend.render
  .build
```

You can also create a backend yourself and still use `.renderBackend`:
```scala
val Example = ScalaComponent.builder[Unit]("Example")
  .initialState(Vector("hello", "world"))
  .backend(new Backend(_)) // ← Fine! Do it yourself!
  .renderBackend           // ← Use backend.render
  .build
```

Using Components
================

Once you've created a Scala React component, it mostly acts like a typical Scala case class.
To use it, you create an instance.
To create an instance, you call the constructor.

```scala
val NoArgs =
  ScalaComponent.static("No args")(<.div("Hello!"))

val Hello =
  ScalaComponent.builder[String]("Hello")
    .render_P(name => <.div("Hello there ", name))
    .build

// Usage
<.div(
  NoArgs(),
  Hello("John"),
  Hello("Jane"))
```

#### Keys

To add a key to a component instance, call `.withKey(key)` before instantiation.

Examples:

```scala
<.div(
  VdomArray(
  NoArgs.withKey("noargs")(),
  Hello.withKey("john")("John"),
  Hello.withKey("jane")("Jane")))
```

#### Refs

See [REFS.md](REFS.md).

#### Rendering

With React JS you'd call `ReactDOM.render(comp, target, callback?)` to render a component into DOM.

With scalajs-react, (unmounted) components come with a `.renderIntoDOM(target, callback?)` method.

```scala
import org.scalajs.dom.document

NoArgs().renderIntoDOM(document.body)
```

React Extensions
================

* Where `setState(State)` is applicable, you can also run:
  * `modState(State => State)`
  * `modState((State, Props) => State)`
  * `setStateOption(Option[State])`
  * `modStateOption(State => Option[State])`
  * `modStateOption((State, Props) => Option[State])`

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

* Sometimes you want to allow a function to both get and affect a portion of a component's state. Anywhere that you can call `.setState()` you can also call `.zoomState()` to return an object that has the same `.setState()`, `.modState()` methods but only operates on a subset of the total state.

  ```scala
  def incrementCounter(s: StateAccessPure[Int]): Callback =
    s.modState(_ + 1)

  // Then in some other component:
  case class State(name: String, counter: Int)

  def render = {
    val f = $.zoomState(_.counter)(value => _.copy(counter = value))
    button(onclick --> incrementCounter(f), "+")
  }
  ```

  You can cut down on boilerplate by using [Monocle](https://github.com/julien-truffaut/Monocle)
  and the [scalajs-react Monocle extensions](FP.md).
  By doing so, the above snippet will look like this:

  ```scala
  import monocle.macros._

  @Lenses case class State(name: String, counter: Int)

  def render = {
    val f = $ zoomStateL State.counter
    button(onclick --> incrementCounter(f), "+")
  }
  ```

* The `.getDOMNode` callback can sometimes execute when unmounted which is an increasingly annoying bug to track down.
  Since React 16 with its new burn-it-all-down error handling approach, an occurance of this can be fatal.
  In order to properly model the reality of the callback and ensure compile-time safety,
  rather than just getting back a VDOM reference, the return type is an ADT like this:

  ```
                                   ComponentDom
                                    ↑        ↑
                 ComponentDom.Mounted        ComponentDom.Unmounted
                  ↑            ↑
  ComponentDom.Element      ComponentDom.Text
  ```

  Calling `.getDOMNode` from without lifecycle callbacks, returns a `ComponentDom.Mounted`.
  Calling `.getDOMNode` on a mounted component instance or a `BackendScope` now returns `ComponentDom`
  which may or may not be mounted.
  Jump into the `ComponentDom` source to see the available methods but in most cases you'll use one of the following:

  ```scala
  trait ComponentDom {
    def mounted  : Option[ComponentDom.Mounted]
    def toElement: Option[dom.Element]
    def toText   : Option[dom.Text]
  ```

  In unit tests you'll typically use `asMounted().asElement()` or `asMounted().asText()` for inspection.


Gotchas
=======

* `table(tr(...))` will appear to work fine at first then crash later. React needs `table(tbody(tr(...)))`.

* React's `setState` functions are asynchronous; they don't apply invocations of `this.setState` until the end of `render` or the current callback. Calling `.state` after `.setState` will return the initial, original value, i.e.

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

* Since `setState` and `modState` return callbacks, if you need to call them from outside of a component (e.g. by accessing the backend of a mounted component), call `.runNow()` to trigger the change; else the callback will never run.
  See the [Callbacks](#callbacks) section for more detail.
