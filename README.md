scalajs-react
=============

[![Build Status](https://travis-ci.org/japgolly/scalajs-react.svg?branch=master)](https://travis-ci.org/japgolly/scalajs-react)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/japgolly/scalajs-react?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Lifts Facebook's [React](https://facebook.github.io/react/) library into [Scala.js](http://www.scala-js.org/) and endeavours to make it as type-safe and Scala-friendly as possible.

Provides (opt-in) support for pure functional programming, with additional modules for
[Scalaz](https://github.com/scalaz/scalaz),
[Cats](https://github.com/typelevel/cats),
and [Monocle](https://github.com/julien-truffaut/Monocle).

Comes utility modules [`extra`](extra/) and [`test`](test/), helpful for React in Scala(.js), rather than React in JS.
Includes a router, testing utils, performance utils, more.

##### Contents

- [Usage & Getting Started](doc/USAGE.md)
  - [VDOM](doc/VDOM.md)
  - [Refs](doc/REFS.md)
  - [The `Callback` class](doc/CALLBACK.md)
  - [IDE support](doc/IDE.md)
- Delving deeper
  - [Types](doc/TYPES.md)
  - [Interoperability](doc/INTEROP.md)
  - [Functional programming](doc/FP.md)
- Scala-only Utilities
  - [Router](doc/ROUTER.md)
  - [Performance Management](doc/PERFORMANCE.md)
  - [Other](doc/EXTRA.md)
- [Testing](doc/TESTING.md)
- [Live Examples & Demos](https://japgolly.github.io/scalajs-react/)
- ScalaDoc: [core](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/core_sjs0.6_2.11/1.0.1) | [extra](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/extra_sjs0.6_2.11/1.0.1) | [scalaz72](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/ext-scalaz72_sjs0.6_2.12/1.0.1) | [monocle](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/ext-monocle_sjs0.6_2.12/1.0.1) | [cats](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/ext-cats_sjs0.6_2.12/1.0.1) | [test](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/test_sjs0.6_2.12/1.0.1)
- [Changelogs](doc/changelog) — [**v1.0.1** (Latest)](doc/changelog/1.0.1.md)


##### External Resources

* Templates & Tutorials
  * [chandu0101 / scalajs-react-template](https://github.com/chandu0101/scalajs-react-template)
  * [ochrons / scalajs-spa-tutorial](https://github.com/ochrons/scalajs-spa-tutorial)
  * [TodoMVC example](http://todomvc.com/examples/scalajs-react)
  * [Scala.js and React: Building an Application for the Web](https://scala-bility.blogspot.com/2015/05/scalajs-and-react-building-application.html)

* Libraries
  * [test-state](https://github.com/japgolly/test-state/) - Integration/Functional/Property testing for scalajs-react.
  * [scalajs-benchmark](https://github.com/japgolly/scalajs-benchmark/)
  * [chandu0101 / scalajs-react-components](https://github.com/chandu0101/scalajs-react-components)
  * [payalabs / scalajs-react-mdl](https://github.com/payalabs/scalajs-react-mdl) - (Material Design Lite components)

##### Requirements:
* React 15.3+
* Scala 2.11 or 2.12.
* Scala.JS 0.6.15+
