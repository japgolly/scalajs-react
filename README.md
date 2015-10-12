scalajs-react
=============

[![Build Status](https://travis-ci.org/japgolly/scalajs-react.svg?branch=master)](https://travis-ci.org/japgolly/scalajs-react)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/japgolly/scalajs-react?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Lifts Facebook's [React](https://facebook.github.io/react/) library into [Scala.js](http://www.scala-js.org/) and endeavours to make it as type-safe and Scala-friendly as possible.

Provides (opt-in) support for pure functional programming, using [Scalaz](https://github.com/scalaz/scalaz) and [Monocle](https://github.com/julien-truffaut/Monocle).

Comes utility modules [`extra`](extra/) and [`test`](test/), helpful for React in Scala(.js), rather than React in JS.
Includes a router, testing utils, performance utils, more.

##### Index

- [Usage (+ Setup)](doc/USAGE.md).
- [Live Examples & Demos](https://japgolly.github.io/scalajs-react/).
- [Type Summary](doc/TYPES.md).
- [Functional Programming](doc/FP.md).
- Scala-only Utilities.
  - [Router](extra/ROUTER.md).
  - [Performance Management](extra/PERF.md).
  - [Smaller stuff](extra/README.md).
- [Testing](test/README.md).
- [Changelogs](doc/) — [Latest](doc/CHANGELOG-0.9.md).


##### External Resources

* Templates & Tutorials
  * [chandu0101 / scalajs-react-template](https://github.com/chandu0101/scalajs-react-template)
  * [ochrons / scalajs-spa-tutorial](https://github.com/ochrons/scalajs-spa-tutorial)
  * [TodoMVC example](http://todomvc.com/examples/scalajs-react)
  * [Scala.js and React: Building an Application for the Web](https://scala-bility.blogspot.com/2015/05/scalajs-and-react-building-application.html)

* [Interop With Third-Party Components - chandu0101](https://github.com/chandu0101/scalajs-react-components/blob/master/doc/InteropWithThirdParty.md)

* Component Libraries
  * [chandu0101 / scalajs-react-components](https://github.com/chandu0101/scalajs-react-components)
  * [payalabs / scalajs-react-mdl](https://github.com/payalabs/scalajs-react-mdl) - (Material Design Lite components)


##### Requirements:
* React 0.12 *(0.13 probably works too, 0.14 will be fully supported when released)*
* Scala 2.11
* Scala.JS 0.6.4+
