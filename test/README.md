Testing
=======

##### Contents
- [Setup](#setup)
- [`React.addons.TestUtils`](#reactaddonstestutils)
- [`Simulate` and `Simulation`](#simulate-and-simulation)
- [`Sel`](#sel)
- [`DebugJs`](#debugjs)

Setup
=====

1. Install PhantomJS.

2. Add the following to SBT:

    ```scala
    // scalajs-react test module
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "test" % "0.9.2" % "test"

    // React JS itself.
    // NOTE: Requires react-with-addons.js instead of just react.js
    jsDependencies +=
      "org.webjars" % "react" % "0.12.2" % "test" / "react-with-addons.js" commonJSName "React"

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

`Sel`
=====
DOM lookup is much easier than using `ReactTestUtils` directly by instead using `Sel`.
`Sel` allows you to use a jQuery/CSS-like selector to lookup a DOM element or subset.
Full examples can be [seen here](src/test/scala/japgolly/scalajs/react/test/SelTest.scala); this is a sample:
```scala
val dom = Sel(".inner a.active.new") findIn myComponent
```

Note: The syntax is quite limited. It supports tags and classes. It does **not** support Ids.
jQuery or Sizzle will do a better job. I don't think I thought of that when I wrote `Sel` :)

`DebugJs`
=========
[DebugJs](src/main/scala/japgolly/scalajs/react/test/DebugJs.scala) is a dumping ground for functionality useful when testing raw JS.

It doesn't have much but `inspectObject` can be tremendously useful.

Example:
```scala
.componentDidMount { $ =>
  val dom = $.getDOMNode()
  println(DebugJs inspectObject dom)
}
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
