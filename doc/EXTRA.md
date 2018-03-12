`extra` Utility Module
======================

This describes the smaller utilities in the `extra` module.
Find links to the larger utilities from the [main README](../README.md).

```scala
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "1.2.0"
```

#### Contents

- [`Ajax`](#ajax)
- [`StateSnapshot`](#statesnapshot)
- Component Mixins:
  - [Broadcaster and Listenable](#broadcaster-and-listenable)
  - [EventListener](#eventlistener)
  - [LogLifecycle](#loglifecycle)
  - [OnUnmount](#onunmount)
  - [TimerSupport](#timersupport)

<br>

`Ajax`
======

`japgolly.scalajs.react.extra.Ajax` is a helper for AJAX that is purely-functional
in that it runs a `Callback`, and accepts XHR-callbacks as `Callback` instances.

A live demo with accompanying code is available here:<br>
[https://japgolly.github.io/scalajs-react/#examples/ajax](https://japgolly.github.io/scalajs-react/#examples/ajax)


`StateSnapshot`
===============

Consider:
1. React has unidirectional flow (yay) and pure render methods (yay!).
2. Stateful components are like mutable variables, stateless components are like immutable values.
   As such state should be avoided, externalised and pushed all the way up to the top of the component tree.

How does one write components that appear stateful yet maintain referential-transparency?
By declaring the following in the component's props:
1. `S` - The current state value to use in the view.
2. `(Option[S], Callback) => Callback` - A function that accepts `setState` arguments
  (namely: an optional new state, and a callback for React to execute once the new state has been applied),
  and returns a `Callback`.
  The component calls this to request a new state be recorded and then just assumes that the function does something
  meaningful. This is great because no assumptions about the larger context are encoded; it has just enough to do its job.

`StateSnapshot[S]` encapsulates this `(S, (Option[S], Callback) => Callback)` pattern.
It's called StateSnapshot because it takes a snapshot of state at its current value.
It also supports optional [`Reusability`](PERFORMANCE.md).

Construction:

* `StateSnapshot(s)(setStateFn)` - Provide a current value, and update function manually.
* `StateSnapshot(s).setStateVia($)` - Provide a current value manually, and use the `.setState` on `$`.
* `StateSnapshot.of($)` - Read the current value and the update function from `$` (usually a lifecycle scope).
* `StateSnapshot.zoom(…)` - Zooms into a subset of the total state. For example, you could create a `StateSnapshot[Age]` from `Person`.
  * `StateSnapshot.zoom(…)(s)(setStateFn)`
  * `StateSnapshot.zoom(…)(s).setStateVia($)`
  * `StateSnapshot.zoom(…).of($)`

A live demo with accompanying code is available here:<br>
https://japgolly.github.io/scalajs-react/#examples/state-snapshot


Broadcaster and Listenable
==========================
These help your components listen and react to external events or data changes.

##### Usage
```scala
// A listening component
val component = ScalaComponent.builder[...]
  ...
  .backend(_ => new OnUnmount.Backend)
  ...
  .configure(Listenable.listen(...))
  ...

// A simple broadcaster
object HelloBroadcaster extends Broadcaster[String] {
  def sayHello(): Unit = broadcast("Hello!")
}
```

##### Features
* `Listenable`: When component mounts, it registers itself as a listener.
* `Listenable`: When component unmounts, it unregisters itself as a listener.
* `Broadcaster`: Manages listener registration and unregistration.
* `Broadcaster`: Provides a `protected def broadcast(a: A): Unit` for easy message broadcasting.

EventListener
=============
* Installs event listeners when component is mounted.
* Uninstalls event listeners when component is unmounted.
* By default, listens to the component node's events. Can specify other event targets (eg. `window`, `document`)

A live demo with accompanying code is available here:<br>
https://japgolly.github.io/scalajs-react/#examples/event-listener


LogLifecycle
============
This will cause logging to occur at React component lifecycle stages.

##### Usage
```scala
val component = ScalaComponent.builder[...]
  ...
  .configure(LogLifecycle.short)   // Logs the component name and stage
  .configure(LogLifecycle.default) // Logs the component name, stage and the props/state
  .configure(LogLifecycle.verbose) // Logs everything in the world
  ...
```

##### Example output
```
[DragAndDrop Demo] componentWillMount
  Constructor {props: Object, _owner: Constructor, _lifeCycleState: "MOUNTED", _pendingCallbacks: null, _currentElement: ReactElement…}

[DragAndDrop Demo] componentDidMount
  Constructor {props: Object, _owner: Constructor, _lifeCycleState: "MOUNTED", _pendingCallbacks: null, _currentElement: ReactElement…}

[DragAndDrop Demo] componentWillUpdate
  Constructor {props: Object, _owner: Constructor, _lifeCycleState: "MOUNTED", _pendingCallbacks: null, _currentElement: ReactElement…}
  Props: List(Item(10,Ten), Item(20,Two Zero), Item(30,Firty), Item(40,Thorty), Item(50,Fipty))
  State: ParentState(List(Item(10,Ten), Item(20,Two Zero), Item(30,Firty), Item(40,Thorty), Item(50,Fipty)),Started(Item(50,Fipty)),0)

[DragAndDrop Demo] componentDidUpdate
  Constructor {props: Object, _owner: Constructor, _lifeCycleState: "MOUNTED", _pendingCallbacks: null, _currentElement: ReactElement…}
  Props: List(Item(10,Ten), Item(20,Two Zero), Item(30,Firty), Item(40,Thorty), Item(50,Fipty))
  State: ParentState(List(Item(10,Ten), Item(20,Two Zero), Item(30,Firty), Item(40,Thorty), Item(50,Fipty)),Inactive,0)
```


OnUnmount
=========
Accrues procedures to be run automatically when its component unmounts.

##### Example
```scala
class MyBackend extends OnUnmount {
  def init: Callback =
    Callback.log("Initialising now...") >>
    onUnmount( Callback.log("Component unmounting...") )
}

val eg = ScalaComponent.builder[Unit]("Example")
  .stateless
  .backend(_ => new MyBackend)
  .render(_ => ???)
  .componentWillMount(_.backend.init)
  .configure(OnUnmount.install)
  .build
```

TimerSupport
============
Alternatives to `window.setTimeout`/`window.setInterval` that automatically unregister installed callbacks
when the component unmounts

##### Example
```scala
import scala.concurrent.duration._

class MyBackend extends TimerSupport

val Timer = ScalaComponent.builder[Unit]("Timer")
  .initialState(0L)
  .backend(_ => new MyBackend)
  .render_S(s => <.div("Seconds elapsed: ", s))
  .componentDidMount(c => c.backend.setInterval(c.modState(_ + 1), 1.second))
  .configure(TimerSupport.install)
  .build
```
