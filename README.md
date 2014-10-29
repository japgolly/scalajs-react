scalajs-react [![Build Status](https://travis-ci.org/japgolly/scalajs-react.svg?branch=master)](https://travis-ci.org/japgolly/scalajs-react)
=============

Lifts Facebook's [React](http://facebook.github.io/react/) library into [Scala.js](http://www.scala-js.org/) and endeavours to make it as type-safe and Scala-friendly as possible.

In addition to wrapping React, this provides extra opt-in functionality to support (separately) easier testing, and pure FP.

#### Contents

- [Setup](#setup)
- [Examples](#examples)
- [Differences from React proper](#differences-from-react-proper)
- [MOAR FP! / Scalaz](#moar-fp--scalaz)
- [Testing](#testing)
- [Extensions](#extensions)
- [Gotchas](#gotchas)
- [Alternatives](#alternatives)

##### Docs
- [TYPES.md](https://github.com/japgolly/scalajs-react/blob/master/TYPES.md) - Overview of types.
- [HISTORY.md](https://github.com/japgolly/scalajs-react/blob/master/HISTORY.md) - Release notes and change log.


Setup
=====

SBT
```scala
// Minimal usage
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "0.5.1"

// React itself
//   (react-with-addons.js can be react.js, react.min.js, react-with-addons.min.js)
jsDependencies += "org.webjars" % "react" % "0.11.1" / "react-with-addons.js" commonJSName "React"

// Test support including ReactTestUtils
//   (requires react-with-addons.js instead of just react.js)
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "test" % "0.5.1" % "test"

// Scalaz support
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-scalaz70" % "0.5.1" // or
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-scalaz71" % "0.5.1"
```

Code:
```scala
// Typical usage
import japgolly.scalajs.react._ // React
import vdom.ReactVDom._         // Scalatags → React virtual DOM
import vdom.ReactVDom.all._     // Scalatags html & css (div, h1, textarea, etc.)

// Scalaz support
import japgolly.scalajs.react.ScalazReact._

// Consolidated uber import
import japgolly.scalajs.react._, vdom.ReactVDom._, all._, ScalazReact._
```

You will also need to add [Scala.js](http://www.scala-js.org) to your project.
Version 0.5.4 or later is required.

Examples
========

[Examples are included](https://github.com/japgolly/scalajs-react/tree/master/example/src/main/scala/japgolly/scalajs/react/example) with this project. If you know Scala and React then that should have you up and running in no time.

If you'd like to see side-by-side comparisons of sample code taken from [http://facebook.github.io/react/](http://facebook.github.io/react/), do this:

1. Checkout or download this repository.
1. `sbt fastOptJS`
1. Open `example/side_by_side.html` locally.


Differences from React proper
=============================

* Rather than using JSX or `React.DOM.xxx` to build a virtual DOM, use `ReactVDom` which is backed by lihaoyi's excellent [Scalatags](https://github.com/lihaoyi/scalatags) library. (See examples.)
* In addition to props and state, if you look at the React samples you'll see that most components need additional functions and in the case of sample #2, state outside of the designated state object (!). In this Scala version, all of that is heaped into an abstract type called `Backend` which you can supply or omit as necessary.
* If you want to pass some plain text into a React component (as one of its children), then you need to wrap it in `raw()`. (It's a Scalatags thing.)
* To keep a collection together when generating the dom, call `.toJsArray`. The only difference I'm aware of is that if the collection is maintained, React will issue warnings if you haven't supplied `key` attributes. Example:
```scala
    table(tbody(
      tr(th("Name"), th("Description"), th("Etcetera")),
      myListOfItems.sortBy(_.name).map(renderItem).toJsArray
    ))
```
* To specify a `key` when creating a React component, instead of merging it into the props, call `.withKey()` before providing the props and children.
```scala
    val Example = ReactComponentB[String]("Eg").render(i => h1(i)).build
    Example.withKey("key1")("The Prop")
```


MOAR FP / Scalaz
================

Included is a Scalaz module that facilitates a more functional and pure approach to React integration.
This is achieved primarily via state and IO monads. Joyously, this approach makes obsolete the need for a "backend".

State modifications and `setState` callbacks are created via `ReactS`, which is conceptually `WriterT[M, List[Callback], StateT[M, S, A]]` but caters to Scala's hopeless inability to infer types. They are applied via `runState` or `runStateS` for vanilla `StateT` monads (ie. without callbacks). Callbacks take the form of `IO[Unit]` and are hooked into HTML via `~~>`, e.g. `button(onclick ~~> T.runState(blah), "Click Me!")`.

Also included are `runStateF` methods which use a `ChangeFilter` typeclass to compare before and after states at the end of a state monad application, and optionally opt-out of a call to `setState` on a component.

See [ScalazExamples](https://github.com/japgolly/scalajs-react/tree/master/example/src/main/scala/japgolly/scalajs/react/example/ScalazExamples.scala) for a taste.
Take a look at the [ScalazReact module](https://github.com/japgolly/scalajs-react/tree/master/scalaz-7.1/src/main/scala/japgolly/scalajs/react/ScalazReact.scala) for the source.


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
jsDependencies += "org.webjars" % "react" % "0.11.1" % "test"
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
* `boolean && (attr := value)` - Make a condition optional.
```scala
    def hasFocus: Boolean = ...
    val html = div(hasFocus && (cls := "focus"))(...)
```
* [Extra attributes](https://github.com/japgolly/scalajs-react/blob/master/core/src/main/scala/japgolly/scalajs/react/vdom/ReactVDom.scala#L190-203) not yet found in Scalatags proper.

#### React
* Where `this.setState(State)` is applicable, you can also run `modState(State => State)`.
* `SyntheticEvent`s have aliases that don't require you to provide the dom type. So instead of `SyntheticKeyboardEvent[xxx]` type alias `ReactKeyboardEvent` can be used.
* Because refs are not guaranteed to exist, the return type is wrapped in `js.UndefOr[_]`. A helper method `tryFocus()` has been added to focus the ref if one is returned.
```scala
    val myRef = Ref[HTMLInputElement]("refKey")

    class Backend(T: BackendScope[_, _]) {
      def clearAndFocusInput() = T.setState("", () => myRef(t).tryFocus())
    }
```
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
    div(classSet("message message-active"
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
