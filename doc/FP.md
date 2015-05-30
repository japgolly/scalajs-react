*I've neglected to document this portion as well as I'd like.*

*For now, please just browse the source (it's not massive) and follow the types.*

Scalaz
======

```scala
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-scalaz71" % "0.8.4"
```

Included is a Scalaz module that facilitates a more functional and pure approach to React integration.
This is achieved primarily via state and IO monads. Joyously, this approach makes obsolete the need for a "backend".

State modifications and `setState` callbacks are created via `ReactS`, which is conceptually `WriterT[M, List[Callback], StateT[M, S, A]]`. `ReactS` monads are applied via `runState`. Vanilla `StateT` monads (ie. without callbacks) can be lifted into `ReactS` via `.liftR`. Callbacks take the form of `IO[Unit]` and are hooked into HTML via `~~>`, e.g. `button(onclick ~~> T.runState(blah), "Click Me!")`.

Also included are `runStateF` methods which use a `ChangeFilter` typeclass to compare before and after states at the end of a state monad application, and optionally opt-out of a call to `setState` on a component.

There's only one example at the moment:
[State monad example](https://japgolly.github.io/scalajs-react/#examples/state-monad).

Monocle
=======

```scala
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-monocle" % "0.8.4"
```

A module with a extensions for [Monocle](https://github.com/julien-truffaut/Monocle) also exists under `ext-monocle`.

There's one example online that demonstrates Monocle usage:
[`ExternalVar` example](https://japgolly.github.io/scalajs-react/#examples/external-var).

