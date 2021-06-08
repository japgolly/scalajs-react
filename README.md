scalajs-react
=============

[![Build Status](https://travis-ci.org/japgolly/scalajs-react.svg?branch=master)](https://travis-ci.org/japgolly/scalajs-react)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/japgolly/scalajs-react?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Lifts Facebook's [React](https://facebook.github.io/react/) library into [Scala.js](http://www.scala-js.org/) and endeavours to make it as type-safe and Scala-friendly as possible.

Provides (opt-in) support for pure functional programming, with additional modules for
[Scalaz](https://github.com/scalaz/scalaz),
[Cats](https://github.com/typelevel/cats),
and [Monocle](https://github.com/julien-truffaut/Monocle) (scalaz or cats based monocle).

Comes with utility modules [`extra`](extra/) and [`test`](test/), helpful for React in Scala(.js), rather than React in JS.
Includes a router, testing utils, performance utils, more.

##### Contents

- [Usage & Getting Started](doc/USAGE.md)
  - [VDOM](doc/VDOM.md)
  - [Hooks](doc/HOOKS.md)
  - [Refs](doc/REFS.md)
  - [IDE support](doc/IDE.md)
  - [The `Callback` class](doc/CALLBACK.md)
  - [React Context](doc/CONTEXT.md)
- Delving deeper
  - [Types](doc/TYPES.md)
  - [Interoperability](doc/INTEROP.md)
  - [Functional programming](doc/FP.md)
  - [Global Config](doc/CONFIG.md)
- Scala-only Utilities
  - [Router](doc/ROUTER.md)
  - [Performance Management](doc/PERFORMANCE.md)
  - [Other](doc/EXTRA.md)
- [Testing](doc/TESTING.md)
- [Live Examples & Demos](https://japgolly.github.io/scalajs-react/)
- ScalaDoc: [core](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/core_sjs0.6_2.12/1.7.7) | [extra](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/extra_sjs0.6_2.12/1.7.7) | [scalaz72](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/ext-scalaz72_sjs0.6_2.12/1.7.7) | [monocle](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/ext-monocle_sjs0.6_2.12/1.7.7) | [monocle-cats](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/ext-monocle-cats_sjs0.6_2.12/1.7.7) | [cats](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/ext-cats_sjs0.6_2.12/1.7.7) | [test](https://www.javadoc.io/doc/com.github.japgolly.scalajs-react/test_sjs0.6_2.12/1.7.7)
- [Changelogs](doc/changelog) — [**v1.7.7** (Latest)](doc/changelog/1.7.7.md)


##### External Resources

* Tutorials / Blogs
  * [Scala, React, and SSR](https://blog.shipreq.com/post/scala_react_and_ssr_part_1)
  * [Scala.js and React: Building an Application for the Web](https://scala-bility.blogspot.com/2015/05/scalajs-and-react-building-application.html)

* Templates
  * [chandu0101 / scalajs-react-template](https://github.com/chandu0101/scalajs-react-template)
  * [ochrons / scalajs-spa-tutorial](https://github.com/ochrons/scalajs-spa-tutorial)
  * [TodoMVC example](http://todomvc.com/examples/scalajs-react)
  * [Scala.js, React and ScalaCSS Boilerplate](https://github.com/shashkovdanil/scalajs-react-boilerplate)
  * ['Intro to React' tutorial transposed into scala.js](https://github.com/MrCurtis/ScalaJSReactTutorial)

* Libraries
  * [test-state](https://github.com/japgolly/test-state/) - Integration/Functional/Property testing for scalajs-react.
  * [scalajs-benchmark](https://github.com/japgolly/scalajs-benchmark/)
  * [chandu0101 / scalajs-react-components](https://github.com/chandu0101/scalajs-react-components)
  * [payalabs / scalajs-react-bridge](https://github.com/payalabs/scalajs-react-bridge) - Boilerplate free use of JS components
  * [payalabs / scalajs-react-mdl](https://github.com/payalabs/scalajs-react-mdl) - (Material Design Lite components)
  * [cquiroz / scalajs-react-virtualized](https://github.com/cquiroz/scalajs-react-virtualized) - Facade for react-virtualized
  * [cquiroz / scalajs-react-clipboard](https://github.com/cquiroz/scalajs-react-clipboard) - Facade for react-copy-to-clipboard
  * [kinoplan / scalajs-react-material-ui](https://github.com/kinoplan/scalajs-react-material-ui) - Material-UI components (+ Full Demo)
  * [diode](https://github.com/suzaku-io/diode) - library for managing application state, influenced by Flux and Elm

* Open Source Projects, which are using [scalajs-react](https://github.com/japgolly/scalajs-react)
  * [Mr. Boilerplate](https://github.com/japgolly/mr.boilerplate) - good example of how to properly manage state in scalajs-react
  * [scala-weather-app](https://github.com/malaman/scala-weather-app) - Yet another weather application, based on Scala.js, scalajs-react and Playframework
  * [scalafiddle-editor](https://github.com/scalafiddle/scalafiddle-editor) - Web user interface for ScalaFiddle [https://scalafiddle.io](https://scalafiddle.io)
  * [scastie](https://github.com/scalacenter/scastie) - An interactive playground for Scala [https://scastie.scala-lang.org](https://scastie.scala-lang.org)

##### Requirements:
* React ≥ 17
* Scala ≥ 2.13
* Scala.JS ≥ 1.5

##### Support:
If you like what I do
—my OSS libraries, my contributions to other OSS libs, [my programming blog](https://japgolly.blogspot.com)—
and you'd like to support me, more content, more lib maintenance, [please become a patron](https://www.patreon.com/japgolly)!
I do all my OSS work unpaid so showing your support will make a big difference.
