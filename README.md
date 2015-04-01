scalajs-react
=============

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/japgolly/scalajs-react?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Lifts Facebook's [React](http://facebook.github.io/react/) library into [Scala.js](http://www.scala-js.org/) and endeavours to make it as type-safe and Scala-friendly as possible.

In addition to wrapping React, this provides extra opt-in functionality to support (separately) easier testing, and pure FP.

Additional features not available in React JS itself, are available in the [`extra` module](https://github.com/japgolly/scalajs-react/tree/master/extra).

#### Contents

- [Setup](#setup)
- [Examples](#examples)
- [Differences from React proper](#differences-from-react-proper)
- [MOAR FP! / Scalaz](#moar-fp--scalaz)
- [Testing](#testing)
- [Extensions](#extensions)
- [Gotchas](#gotchas)
- [Alternatives](#alternatives)
- [Extra Features](https://github.com/japgolly/scalajs-react/tree/master/extra) **NEW!**

##### Docs
- [TYPES.md](https://github.com/japgolly/scalajs-react/blob/master/doc/TYPES.md) - Overview of types.
- [Release notes and changelogs](https://github.com/japgolly/scalajs-react/tree/master/doc).

##### Requirements:
* React 0.12
* Scala 2.11
* Scala.JS 0.6.0+


Setup
=====

Firstly, you'll need to add [Scala.js](http://www.scala-js.org) to your project.

Next, add scalajs-react to SBT:
```scala
// Minimal usage
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "0.8.3"

// React itself
//   (react-with-addons.js can be react.js, react.min.js, react-with-addons.min.js)
jsDependencies += "org.webjars" % "react" % "0.13.1" / "react-with-addons.js" commonJSName "React"

// Test support including ReactTestUtils
//   (requires react-with-addons.js instead of just react.js)
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "test" % "0.8.3" % "test"

// Scalaz support
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-scalaz71" % "0.8.3"

// Monocle support
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-monocle" % "0.8.3"

// Extra features (includes Scalaz and Monocle support)
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "0.8.3"
```

Code:
```scala
// The basics
import japgolly.scalajs.react._

// Virtual DOM building
// There are two flavours. In both examples we will build:
//   <a class="google" href="https://www.google.com"><span>GOOGLE!</span></a>

// Method 1 (recommended): Using prefixes < for tags, ^ for attributes.
import japgolly.scalajs.react.vdom.prefix_<^._
val vdom = <.a(
             ^.className := "google",
             ^.href      := "https://www.google.com",
             <.span("GOOGLE!"))

// Method 2: Importing everything without prefix into namespace.
import japgolly.scalajs.react.vdom.all._
val vdom = a(
             className := "google",
             href      := "https://www.google.com",
             span("GOOGLE!"))

// Scalaz support
import japgolly.scalajs.react.ScalazReact._

// Monocle support
import japgolly.scalajs.react.MonocleReact._
```

Examples
========

A number of [examples are demonstrated online here](http://japgolly.github.io/scalajs-react/).

You'll find that nearly all of the [demos in the React doc](http://facebook.github.io/react/) are on display beside their Scala counterparts. If you know Scala and React, you should be up and running in no time.

The source code for the above [lives here](https://github.com/japgolly/scalajs-react/tree/master/gh-pages/src/main/scala/ghpages/examples). To build and play around with locally:

1. Checkout or download this repository.
1. `sbt gh-pages/fastOptJS`
1. Open `gh-pages/index.html` locally.


Differences from React proper
=============================

* Rather than using JSX or `React.DOM.xxx` to build a virtual DOM, a specialised copy of [Scalatags](https://github.com/lihaoyi/scalatags) is used. (See examples.)
* In addition to props and state, if you look at the React samples you'll see that most components need additional functions and in the case of sample #2, state outside of the designated state object (!). In this Scala version, all of that is heaped into an abstract type called `Backend` which you can supply or omit as necessary.
* To keep a collection together when generating the dom, call `.toJsArray`. The only difference I'm aware of is that if the collection is maintained, React will issue warnings if you haven't supplied `key` attributes. Example:
```scala
    table(tbody(
      tr(th("Name"), th("Description"), th("Etcetera")),
      myListOfItems.sortBy(_.name).map(renderItem).toJsArray
    ))
```
* To specify a `key` when creating a React component, instead of merging it into the props, call `.set(key = ...)` before providing the props and children.
```scala
    val Example = ReactComponentB[String]("Eg").render(i => h1(i)).build
    Example.set(key = "key1")("The Prop")
```


MOAR FP / Scalaz
================

Included is a Scalaz module that facilitates a more functional and pure approach to React integration.
This is achieved primarily via state and IO monads. Joyously, this approach makes obsolete the need for a "backend".

State modifications and `setState` callbacks are created via `ReactS`, which is conceptually `WriterT[M, List[Callback], StateT[M, S, A]]`. `ReactS` monads are applied via `runState`. Vanilla `StateT` monads (ie. without callbacks) can be lifted into `ReactS` via `.liftR`. Callbacks take the form of `IO[Unit]` and are hooked into HTML via `~~>`, e.g. `button(onclick ~~> T.runState(blah), "Click Me!")`.

Also included are `runStateF` methods which use a `ChangeFilter` typeclass to compare before and after states at the end of a state monad application, and optionally opt-out of a call to `setState` on a component.

See [ScalazExamples](https://github.com/japgolly/scalajs-react/blob/master/gh-pages/src/main/scala/ghpages/examples/ScalazExample.scala) for a taste.
Take a look at the [ScalazReact module](https://github.com/japgolly/scalajs-react/tree/master/scalaz-7.1/src/main/scala/japgolly/scalajs/react/ScalazReact.scala) for the source.

#### Monocle
A module with a extensions for [Monocle](https://github.com/julien-truffaut/Monocle) also exists under `ext-monocle`.


Testing
=======
[React.addons.TestUtils](http://facebook.github.io/react/docs/test-utils.html) is wrapped in Scala and available as `ReactTestUtils` in the test module (see [Setup](#setup)). Usage is unchanged from JS.

To make event simulation easier, certain event types have dedicated, strongly-typed case classes to wrap event data. For example, JS like
```js
// JavaScript
React.addons.TestUtils.Simulate.change(t, {target: {value: "Hi"}})
```
becomes
```scala
// Scala
ReactTestUtils.Simulate.change(t, ChangeEventData(value = "Hi"))

// Or shorter
ChangeEventData("Hi") simulate t
```

Simulations can also be created and composed without a target, using `Simulation`. Example:
```scala
val a = Simulation.focus
val b = Simulation.change(ChangeEventData(value = "hi"))
val c = Simulation.blur
val s = a andThen b andThen c

// Or shorter
val s = Simulation.focus >> ChangeEventData("hi").simulation >> Simulation.blur

// Or even shorter again, using a convenience method
val s = Simulation.focusChangeBlur("hi")

// Then run it later
s run component
```


DOM lookup is much easier than using `ReactTestUtils` directly by instead using `Sel`.
`Sel` allows you to use a jQuery/CSS-like selector to lookup a DOM element or subset.
Full examples can be [seen here](https://github.com/japgolly/scalajs-react/blob/master/test/src/test/scala/japgolly/scalajs/react/test/SelTest.scala); this is a sample:
```scala
val dom = Sel(".inner a.active.new") findIn myComponent
```


Also included is [DebugJs](https://github.com/japgolly/scalajs-react/blob/master/test/src/main/scala/japgolly/scalajs/react/test/DebugJs.scala), a dumping ground for functionality useful when testing JS. `inspectObject` can be tremendously useful.

#### SBT
In order to test React and use `ReactTestUtils` you will need to make a few changes to SBT.
* Add
```scala
jsDependencies += "org.webjars" % "react" % "0.12.1" % "test"
                  / "react-with-addons.js" commonJSName "React"

requiresDOM := true

test      in Test := (test      in(Test, fastOptStage)).value

testOnly  in Test := (testOnly  in(Test, fastOptStage)).evaluated

testQuick in Test := (testQuick in(Test, fastOptStage)).evaluated
```
* Install PhantomJS.

Extensions
==========

#### Scalatags
* Case of attributes and styles matches React. So unlike vanilla-Scalatags' `onclick` attribute, use `onClick`.
* `attr ==> (SyntheticEvent[_] => _)` - Wires up an event handler.
```scala
    def handleSubmit(e: SyntheticEvent[HTMLInputElement]) = ...
    val html = form(onsubmit ==> handleSubmit)(...)
```
* `attr --> (=> Unit)` - Specify a function as an attribute value.
```scala
    def reset() = T.setState("")
    val html = div(onclick --> reset())("Click to Reset")
```
* `boolean ?= (attr := value)` - Make a condition optional.
```scala
    def hasFocus: Boolean = ...
    val html = div(hasFocus ?= (cls := "focus"))(...)
```
* Attributes, styles, and tags can be wrapped in `Option` or `js.UndefOr` to make them optional.
```scala
    val person: js.UndefOr[Person] = ???
    val name: Option[String] = ???
    val html = div(key := person.map(_.id), value := name)
```
* `EmptyTag` - A virtual DOM building block representing nothing.
```scala
  div(if (allowEdit) editButton else EmptyTag)
```
* Custom tags, attributes and styles.
```scala
  val a = "customAttr" .reactAttr
  val s = "customStyle".reactStyle
  val t = "customTag"  .reactTag

  // <customTag customAttr="hello" style="customStyle:123;">bye</customTag>
  t(a := "hello", s := "123", "bye")
```

#### React
* Where `this.setState(State)` is applicable, you can also run `modState(State => State)`.
* `SyntheticEvent`s have aliases that don't require you to provide the dom type. So instead of `SyntheticKeyboardEvent[xxx]` type alias `ReactKeyboardEvent` can be used.
* The component builder has a `propsDefault` method which takes some default properties and exposes constructor methods that 1) don't require any property specification, and 2) take an `Optional[Props]`.
* The component builder has a `propsAlways` method which provides all component instances with given properties, doesn't allow property specification in the constructor.
* React has a [classSet addon](http://facebook.github.io/react/docs/class-name-manipulation.html)
  for specifying multiple optional class attributes. The same mechanism is applicable with this library is as follows:
```scala
    div(classSet(
      "message"           -> true,
      "message-active"    -> true,
      "message-important" -> props.isImportant,
      "message-read"      -> props.isRead
    ))(props.message)

    // Or for convenience, put all constants in the first arg:
    div(classSet1("message message-active"
      ,"message-important" -> props.isImportant
      ,"message-read"      -> props.isRead
    ))(props.message)
```
* Sometimes you want to allow a function to both get and affect a portion of a component's state. Anywhere that you can call `.setState()` you can also call `focusState()` to return an object that has the same `.setState()`, `.modState()` methods but only operates on a subset of the total state.
```scala
    def incrementCounter(s: ComponentStateFocus[Int]) = s.modState(_ + 1)

    // Then later in a render() method
    val f = T.focusState(_.counter)((a,b) => a.copy(counter = b))
    button(onclick --> incrementCounter(f))("+")
```

##### Refs
Rather than specify references using strings, the `Ref` object can provide some more safety.
* `Ref(name)` will create a reference to both apply to and retrieve a plain DOM node.
* `Ref.to(component, name)` will create a reference to a component so that on retrieval its types are preserved.
* `Ref.param(param => name)` can be used for references to items in a set, with the key being a data entity's ID.
* Because refs are not guaranteed to exist, the return type is wrapped in `js.UndefOr[_]`. A helper method `tryFocus()` has been added to focus the ref if one is returned.
```scala
    val myRef = Ref[HTMLInputElement]("refKey")

    class Backend(T: BackendScope[_, _]) {
      def clearAndFocusInput() = T.setState("", () => myRef(t).tryFocus())
    }
```

Additional features are available in the [`extra` module](https://github.com/japgolly/scalajs-react/tree/master/extra).

Gotchas
=======

* `table(tr(...))` will appear to work fine at first then crash later. React needs `table(tbody(tr(...)))`.
* React doesn't apply invocations of `this.setState` until the end of `render` or the current callback. Calling `.state` after `.setState` will return the original value, ie. `val s1 = x.state; x.setState(s2); x.state == s1 // not s2`.
  If you want to compose state modifications (and you're using Scalaz), take a look at the `ScalazReact` module, specifically `ReactS` and `runState`.


Alternatives
============

#### [xored/scala-js-react](https://github.com/xored/scala-js-react)
Major differences:
- Object-oriented approach.
- Uses XML-literals instead of Scalatags. Resembles JSX very closely.
