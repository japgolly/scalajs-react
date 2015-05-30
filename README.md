scalajs-react
=============

[![Build Status](https://travis-ci.org/japgolly/scalajs-react.svg?branch=master)](https://travis-ci.org/japgolly/scalajs-react)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/japgolly/scalajs-react?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Lifts Facebook's [React](https://facebook.github.io/react/) library into [Scala.js](http://www.scala-js.org/) and endeavours to make it as type-safe and Scala-friendly as possible.

Provides (opt-in) support for pure functional programming, using [Scalaz](https://github.com/scalaz/scalaz) and [Monocle](https://github.com/julien-truffaut/Monocle).

Comes utility modules [`extra`](extra/) and [`test`](test/), helpful for React in Scala(.js), rather than React in JS.
Includes a router, testing utils, performance utils, more.

#### Index

- [Setup & Usage](doc/USAGE.md).
- [Live Examples & Demos](https://japgolly.github.io/scalajs-react/).
- [Summary of Types](doc/TYPES.md).
- [Functional Programming with React](doc/FP.md).
- [`extra`: Utilities not found in React.JS](extra/README.md).
- [Testing](test/README.md).
- [Changelogs](doc/) - [latest](doc/CHANGELOG-0.9.md).

##### Requirements:
* React 0.12
* Scala 2.11
* Scala.JS 0.6.3+
