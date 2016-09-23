Testing
=======

This file describes testing functionality provided by React.JS and scalajs-react.
<br>It is plenty for simple and small unit tests.

For larger and/or complicated tests, **it is highly recommended to use
[Scala Test-State](https://github.com/japgolly/test-state)**.
<br>See [this example](https://github.com/japgolly/test-state/tree/master/example-react)
for how to write tests for real-world scalajs-react applications.

#### Contents
- [Setup](#setup)
- [`React.addons.TestUtils`](#reactaddonstestutils)
- [`Simulate` and `Simulation`](#simulate-and-simulation)
- [`ComponentTester`](#componenttester)
- [`ReactTestVar`](#reacttestvar)
- [`WithExternalCompStateAccess`](#withexternalcompstateaccess)
- [`DebugJs`](#debugjs)
- [`Test Scripts`](#test-scripts)

Setup
=====

1. Install PhantomJS.

2. Add the following to SBT:

    ```scala
    // scalajs-react test module
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "test" % "0.11.1" % "test"

    // React JS itself.
    // NOTE: Requires react-with-addons.js instead of just react.js
    jsDependencies ++= Seq(

      "org.webjars.bower" % "react" % "15.3.2" % "test"
        /        "react-with-addons.js"
        minified "react-with-addons.min.js"
        commonJSName "React",

      "org.webjars.bower" % "react" % "15.3.2" % "test"
        /         "react-dom.js"
        minified  "react-dom.min.js"
        dependsOn "react-with-addons.js"
        commonJSName "ReactDOM",

      "org.webjars.bower" % "react" % "15.3.2" % "test"
        /         "react-dom-server.js"
        minified  "react-dom-server.min.js"
        dependsOn "react-dom.js"
        commonJSName "ReactDOMServer"),

    // Indicate that unit tests will access the DOM
    requiresDOM := true

    // Compile tests to JS using fast-optimisation
    scalaJSStage in Test := FastOptStage
    ```

3. To [workaround](https://github.com/scala-js/scala-js/issues/1555) a [PhantomJS bug](https://github.com/ariya/phantomjs/issues/13112) that causes tests to crash if they write to stderr, copy [`PhantomJS2Env.scala`](../project/PhantomJS2Env.scala) to your `project` directory and add this to SBT:

    ```scala
    jsEnv in Test := new PhantomJS2Env(scalaJSPhantomJSClassLoader.value)
    ```


`React.addons.TestUtils`
========================
[React.addons.TestUtils](https://facebook.github.io/react/docs/test-utils.html) is wrapped in Scala and available as `ReactTestUtils`. Usage is unchanged from JS.


`Simulate` and `Simulation`
===========================
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


`ComponentTester`
=================

A helper that renders a component into the document so that
  * you can easily change props and/or state.
  * it is unmounted when the test is over.

If you don't need to test props changes, you can actually just use `ReactTestUtils.withRenderedIntoDocument`.
This might accrue more helpful features over time but currently changing props is the only real advantage this has over `withRenderedIntoDocument`.

##### Example:

If you wanted to test a component that has both props and state, a trivial component could be:

```scala
val Example = ReactComponentB[String]("Example")
  .initialState(0)
  .renderPS((_, p, s) => <.div(s" $p:$s "))
  .build
```

you could test it like this:

```scala
ComponentTester(Example)("First props") { tester =>

  // This imports:
  // - component which is the mounted component.
  // - setProps which changes the props and immediately re-renders.
  // - setState which changes the state and immediately re-renders.
  import tester._

  def assertHtml(p: String, s: Int): Unit =
    assert(component.outerHtmlWithoutReactDataAttr() == s"<div> $p:$s </div>")

  assertHtml("First props", 0)

  setState(2)
  assertHtml("First props", 2)

  setProps("Second props")
  assertHtml("Second props", 2)
}
```


`ReactTestVar`
==============

A `ReactTestVar` is a class that can be used to mock the following types in tests:
  * `ExternalVar[A]`
  * `ReusableVar[A]`

Example:
```scala
import japgolly.scalajs.react._, vdom.prefix_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.test._

object ExampleTest extends TestSuite {

  val NameChanger = ReactComponentB[ExternalVar[String]]("Name changer")
    .render_P { evar =>
      def updateName = (event: ReactEventI) => evar.set(event.target.value)
      <.input(
        ^.`type`    := "text",
        ^.value     := evar.value,
        ^.onChange ==> updateName)
    }
    .build

  override def tests = TestSuite {
    val nameVar = ReactTestVar("guy")
    val comp = ReactTestUtils renderIntoDocument NameChanger(nameVar.externalVar())
    ChangeEventData("bob").simulate(comp)
    assert(nameVar.value() == "bob")
  }
}
```


`WithExternalCompStateAccess`
=============================

Allows you to test a component that requires access to some external component state.

##### Example:

Say you have a component like:
```scala
val Example = ReactComponentB[(CompState.WriteAccess[Int], Int)]("I")
  .render_P { case (w, i) =>
    <.div(
      <.div("state = ", <.span(i)),
      <.button("inc", ^.onClick --> w.modState(_ + 1)) // weird here - just an example
    )
  }
  .build
```

You can use `WithExternalCompStateAccess` to write a test like this:
```scala
import japgolly.scalajs.react.test.WithExternalCompStateAccess
import utest._

object ExampleTest extends TestSuite {

  val Parent = WithExternalCompStateAccess[Int](($, i) => Example(($, i)))

  override def tests = TestSuite {
    val c = ReactTestUtils renderIntoDocument Parent(3)
    def state = ReactTestUtils.findRenderedDOMComponentWithTag(c, "span").getDOMNode().innerHTML.toInt
    def button = ReactTestUtils.findRenderedDOMComponentWithTag(c, "button")
    assert(state == 3)
    ReactTestUtils.Simulate click button
    assert(state == 4)
  }
}
```


`DebugJs`
=========
[DebugJs](../test/src/main/scala/japgolly/scalajs/react/test/DebugJs.scala) is a dumping ground for functionality useful when testing raw JS.

It doesn't have much but `inspectObject` can be tremendously useful.

Example:
```scala
.componentDidMount($ => Callback {
  val dom = $.getDOMNode()
  println(DebugJs inspectObject dom)
})
```

Output (truncated):
```
[object HTMLCanvasElement]
  [  1/137] ALLOW_KEYBOARD_INPUT                      : number   = 1
  [  2/137] ATTRIBUTE_NODE                            : number   = 2
  [  3/137] CDATA_SECTION_NODE                        : number   = 4
  [  4/137] COMMENT_NODE                              : number   = 8
  [  5/137] DOCUMENT_FRAGMENT_NODE                    : number   = 11
  [  6/137] DOCUMENT_NODE                             : number   = 9
  [  7/137] DOCUMENT_POSITION_CONTAINED_BY            : number   = 16
  [  8/137] DOCUMENT_POSITION_CONTAINS                : number   = 8
  [  9/137] DOCUMENT_POSITION_DISCONNECTED            : number   = 1
  [ 10/137] DOCUMENT_POSITION_FOLLOWING               : number   = 4
  [ 11/137] DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC : number   = 32
  [ 12/137] DOCUMENT_POSITION_PRECEDING               : number   = 2
  [ 13/137] DOCUMENT_TYPE_NODE                        : number   = 10
  [ 14/137] ELEMENT_NODE                              : number   = 1
  [ 15/137] ENTITY_NODE                               : number   = 6
  [ 16/137] ENTITY_REFERENCE_NODE                     : number   = 5
  [ 17/137] NOTATION_NODE                             : number   = 12
  [ 18/137] PROCESSING_INSTRUCTION_NODE               : number   = 7
  [ 19/137] TEXT_NODE                                 : number   = 3
  [ 20/137] accessKey                                 : string   =
  [ 21/137] addEventListener                          : function = function addEventListener() {
  [ 22/137] appendChild                               : function = function appendChild() {
  [ 23/137] attributes                                : object   = [object NamedNodeMap]
  [ 24/137] baseURI                                   : object   = null
  [ 25/137] blur                                      : function = function blur() {
  [ 26/137] childElementCount                         : number   = 0
  [ 27/137] childNodes                                : object   = [object NodeList]
  [ 28/137] children                                  : object   = [object HTMLCollection]
  [ 29/137] classList                                 : object   =
  [ 30/137] className                                 : string   =
  [ 31/137] click                                     : function = function click() {
  [ 32/137] clientHeight                              : number   = 0
  [ 33/137] clientLeft                                : number   = 0
  [ 34/137] clientTop                                 : number   = 0
  [ 35/137] clientWidth                               : number   = 0
  [ 36/137] cloneNode                                 : function = function cloneNode() {
  [ 37/137] compareDocumentPosition                   : function = function compareDocumentPosition() {
  [ 38/137] contains                                  : function = function contains() {
  [ 39/137] contentEditable                           : string   = inherit
  [ 40/137] dataset                                   : object   = [object DOMStringMap]
  [ 41/137] dir                                       : string   =
  [ 42/137] dispatchEvent                             : function = function dispatchEvent() {
  [ 43/137] draggable                                 : boolean  = false
  [ 44/137] firstChild                                : object   = null
  [ 45/137] firstElementChild                         : object   = null
  [ 46/137] focus                                     : function = function focus() {
  [ 47/137] getAttribute                              : function = function getAttribute() {
  [ 48/137] getAttributeNS                            : function = function getAttributeNS() {
  [ 49/137] getAttributeNode                          : function = function getAttributeNode() {
  [ 50/137] getAttributeNodeNS                        : function = function getAttributeNodeNS() {
  [ 51/137] getBoundingClientRect                     : function = function getBoundingClientRect() {
  [ 52/137] getClientRects                            : function = function getClientRects() {
  [ 53/137] getContext                                : function = function getContext() {
  [ 54/137] getElementsByClassName                    : function = function getElementsByClassName() {
  [ 55/137] getElementsByTagName                      : function = function getElementsByTagName() {
  [ 56/137] getElementsByTagNameNS                    : function = function getElementsByTagNameNS() {
  [ 57/137] hasAttribute                              : function = function hasAttribute() {
  [ 58/137] hasAttributeNS                            : function = function hasAttributeNS() {
  [ 59/137] hasAttributes                             : function = function hasAttributes() {
  [ 60/137] hasChildNodes                             : function = function hasChildNodes() {
  [ 61/137] height                                    : number   = 150
  [ 62/137] hidden                                    : boolean  = false
  [ 63/137] id                                        : string   =
...
```

Test Scripts
============

It's possible to write test scripts like

1. *click this*
2. *verify that*
3. *press the Back button*
4. *type name*
5. *press Enter*

In case you missed the notice at the top of the file, that functionality is provided in a sister library called
[Scala Test-State](https://github.com/japgolly/test-state).

See [this example](https://github.com/japgolly/test-state/tree/master/example-react)
for how to write tests for real-world scalajs-react applications.
