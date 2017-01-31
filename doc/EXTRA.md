`extra` Utility Module
======================

This describes the smaller utilities in the `extra` module.
Find links to the larger utilities from the [main README](../README.md).

```scala
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "0.11.3"
```

#### Contents

- [ExternalVar](#externalvar)
- Component Mixins:
  - [Broadcaster and Listenable](#broadcaster-and-listenable)
  - [EventListener](#eventlistener)
  - [LogLifecycle](#loglifecycle)
  - [OnUnmount](#onunmount)
  - [SetInterval](#setinterval)


Broadcaster and Listenable
==========================
These help your components listen and react to external events or data changes.

##### Usage
```scala
// A listening component
val component = ReactComponentB[...]
  ...
  .backend(_ => new OnUnmount.Backend)
  ...
  .configure(Listenable.install(...))
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

A live demo with accompanying code is available here:

https://japgolly.github.io/scalajs-react/#examples/event-listener


ExternalVar
===========
Provides a component with safe R/W access to an external variable.

A live demo with accompanying code is available here:

https://japgolly.github.io/scalajs-react/#examples/external-var


LogLifecycle
============
This will cause logging to occur at React component lifecycle stages.

##### Usage
```scala
val component = ReactComponentB[...]
  ...
  .configure(LogLifecycle.short)   // Logs the component name and stage
  .configure(LogLifecycle.verbose) // Logs component props and state as well
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

val eg = ReactComponentB[Unit]("Example")
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

val Timer = ReactComponentB[Unit]("Timer")
  .initialState(0L)
  .backend(_ => new MyBackend)
  .render_S(s => <.div("Seconds elapsed: ", s))
  .componentDidMount(c => c.backend.setInterval(c.modState(_ + 1), 1.second))
  .configure(TimerSupport.install)
  .build
```
