scalajs-react
=============

Lifts Facebook's [React](http://facebook.github.io/react/) library into [Scala.js](http://www.scala-js.org/) and endeavours to make it as type-safe and Scala-compatible as possible.

### Differences from React proper
* Rather than using JSX or `React.DOM.xxx` to build a virtual DOM, use `ReactVDom` which is backed by lihaoyi's excellent [Scalatags](https://github.com/lihaoyi/scalatags) library. (See examples.)
* In addition to props and state, if you look at the React samples you'll see that most components need additional functions and in the case of sample #2, state outside of the designated state object (!). In this Scala version, all of that is heaped into an abstract type called `Backend` which you can supply or omit as necessary.

Examples
========

[Examples are included](https://github.com/japgolly/scalajs-react/tree/master/src/main/scala/japgolly/scalajs/react/example) with this project. If you know Scala and React then that should have you up and running in no time.

If you'd like to see side-by-side comparisons of sample code taken from [http://facebook.github.io/react/](http://facebook.github.io/react/), do this:

1. Checkout or download this repository.
1. `sbt fastOptJS`
1. Open `index.html` locally.

Here are two examples:

### Hello World
This is the first demo on the [React](http://facebook.github.io/react/) homepage.
```scala
val HelloMessage = ReactComponentB[String]("HelloMessage")
  .render(name => div("Hello ", name))
  .create

React.renderComponent(HelloMessage("John"), mountNode)
```

### Live Incrementing Counter
This is the second demo on the [React](http://facebook.github.io/react/) homepage.
In this example a dedicated `State` class is created so as to closer match the React sample. This can actually be omitted with a `Long` being the state type.
```scala
case class State(secondsElapsed: Long)

class Backend {
  var interval: js.UndefOr[Int] = js.undefined
  def tick(scope: ComponentScopeM[_, State, _]): js.Function =
    () => scope.modState(s => State(s.secondsElapsed + 1))
}

val Timer = ReactComponentB[Unit]("Timer")
  .initialState(State(0))
  .backend(_ => new Backend)
  .render((_,S,_) => div("Seconds elapsed: ", S.secondsElapsed))
  .componentDidMount(scope =>
    scope.backend.interval = window.setInterval(scope.backend.tick(scope), 1000))
  .componentWillUnmount(_.backend.interval foreach window.clearInterval)
  .create

React.renderComponent(Timer(()), mountNode)
```
