*I've neglected to document this portion as well as I'd like.*

*For now, please just browse the source (it's not massive) and follow the types.*

Callback
========

Included is a type `CallbackTo[A]` which captures effects designated for use in React callbacks.
`Callback` is `CallbackTo[Unit]` with a different companion object, full of different goodies that all return `Unit`.
 <br>*(See also [USAGE.md](USAGE.md).)*

It is roughly equivalent to `IO`/`Task` in Scalaz, Haskell's `IO` monad, etc.

Living in the `core` module with no FP dependencies,
many ops normally provided via typeclasses (eg. `<*`, `>>=`, etc.) are built-in directly.
The Cats & Scalaz modules contain typeclass instances for it.

There's also `CallbackOption` which is a callback & option monad stack.
Check out the online [`CallbackOption` example](https://japgolly.github.io/scalajs-react/#examples/callback-option).

There's also `CallbackKleisli` which is the `ReaderT CallbackTo` monad stack.

Scalaz
======

```scala
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-scalaz72" % "1.2.3"
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
libraryDependencies ++= Seq(
    "com.github.japgolly.scalajs-react" %%% "ext-monocle" % "1.2.3",
    "com.github.julien-truffaut" %%%  "monocle-core"  % "1.5.0",
    "com.github.julien-truffaut" %%%  "monocle-macro" % "1.5.0"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
```

A module with a extensions for [Monocle](https://github.com/julien-truffaut/Monocle) also exists under `ext-monocle`.

There's one example online that demonstrates Monocle usage:
[`ExternalVar` example](https://japgolly.github.io/scalajs-react/#examples/external-var).

Monocle-cats
============

```scala
libraryDependencies ++= Seq(
    "com.github.japgolly.scalajs-react" %%% "ext-monocle-cats" % "1.2.3",
    "com.github.julien-truffaut" %%%  "monocle-core"  % "1.5.0-cats",
    "com.github.julien-truffaut" %%%  "monocle-macro" % "1.5.0-cats"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

Same as the `ext-monocle` module but using the cats variant of monocle

Cats
====

```scala
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-cats" % "1.2.3"
```

There's a Cats module now too. It's pretty much that same as the Scalaz module but without
any `IO` stuff.
