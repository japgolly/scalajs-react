scalajs-react
=============

[![Build Status](https://travis-ci.org/japgolly/scalajs-react.svg?branch=master)](https://travis-ci.org/japgolly/scalajs-react)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/japgolly/scalajs-react?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Lifts Facebook's [React](https://facebook.github.io/react/) library into [Scala.js](http://www.scala-js.org/) and endeavours to make it as type-safe and Scala-friendly as possible.

Provides (opt-in) support for pure functional programming, using [Scalaz](https://github.com/scalaz/scalaz) and [Monocle](https://github.com/julien-truffaut/Monocle).

Comes utility modules [`extra`](extra/) and [`test`](test/), helpful for React in Scala(.js), rather than React in JS.
Includes a router, testing utils, performance utils, more.

<br>
## Notice: A redesign is currently underway on the [topic/neo](https://github.com/japgolly/scalajs-react/tree/topic/neo) branch.  
### Pop in, have a look around, if you'd like to make some big changes now is the time.
### Also it will largely be backward-compatible so don't be afraid to get started with the latest release if you're a new user.
<br>

##### Contents

- [Usage (+ Setup)](doc/USAGE.md).
  - [The `Callback` class](doc/CALLBACK.md).
- [Live Examples & Demos](https://japgolly.github.io/scalajs-react/).
- [Type Summary](doc/TYPES.md).
- [Functional Programming](doc/FP.md).
- Scala-only Utilities.
  - [Router](doc/ROUTER.md).
  - [Performance Management](doc/PERFORMANCE.md).
  - [Smaller stuff](doc/EXTRA.md).
- [Testing](doc/TESTING.md).
- ScalaDoc: [core](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/core_sjs0.6_2.11/0.11.3) | [extra](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/extra_sjs0.6_2.11/0.11.3) | [scalaz72](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/ext-scalaz72_sjs0.6_2.12/0.11.3) | [monocle](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/ext-monocle_sjs0.6_2.12/0.11.3) | [test](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/test_sjs0.6_2.12/0.11.3)
- [Changelogs](doc/changelog) — [Latest](doc/changelog/0.11.3.md).


##### External Resources

* Templates & Tutorials
  * [chandu0101 / scalajs-react-template](https://github.com/chandu0101/scalajs-react-template)
  * [ochrons / scalajs-spa-tutorial](https://github.com/ochrons/scalajs-spa-tutorial)
  * [TodoMVC example](http://todomvc.com/examples/scalajs-react)
  * [Scala.js and React: Building an Application for the Web](https://scala-bility.blogspot.com/2015/05/scalajs-and-react-building-application.html)

* [Interop With Third-Party Components - chandu0101](https://github.com/chandu0101/scalajs-react-components/blob/master/doc/InteropWithThirdParty.md)

* Libraries
  * [scalajs-benchmark](https://github.com/japgolly/scalajs-benchmark/)
  * [chandu0101 / scalajs-react-components](https://github.com/chandu0101/scalajs-react-components)
  * [payalabs / scalajs-react-mdl](https://github.com/payalabs/scalajs-react-mdl) - (Material Design Lite components)

##### Requirements:
* React 15+
* Scala 2.11 or 2.12.
* Scala.JS 0.6.14+

