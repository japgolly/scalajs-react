scalajs-react Extras
====================

A collection of functionality that provides benefit when using scalajs-react.

- [Router](https://github.com/japgolly/scalajs-react/blob/master/extra/ROUTER.md)
- Component Mixins:
  - [Broadcaster and Listenable](#broadcaster-and-listenable)
  - [ExternalVar](#externalvar)
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


ExternalVar
===========
Provides a component with safe R/W access to an external variable.

A live demo with accompanying code is available on
[http://japgolly.github.io/scalajs-react](http://japgolly.github.io/scalajs-react/).


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
  def init(): Unit = {
    console.log("Initialising now...")
    onUnmount { console.log("Component unmounting...") }
  }
}

val eg = ReactComponentB[Unit]("Example")
  .stateless
  .backend(_ => new MyBackend)
  .render(_ => ???)
  .componentWillMount(_.backend.init())
  .configure(OnUnmount.install)
  .buildU
```

SetInterval
===========
Alternative to `window.setInterval` that automatically unregisters installed callbacks when its component unmounts.

##### Example
```scala
class MyBackend extends SetInterval

val Timer = ReactComponentB[Unit]("Timer")
  .initialState(0L)
  .backend(_ => new MyBackend)
  .render((_,s,_) => div("Seconds elapsed: ", s))
  .componentDidMount(c => c.backend.setInterval(c.modState(_ + 1), 1.second))
  .configure(SetInterval.install)
  .buildU
```
