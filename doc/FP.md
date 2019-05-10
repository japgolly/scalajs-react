*I've not documented this side of things as well as I'd like.*

*For now, please just browse the source (it's not massive) and follow the types.*

Built-in Constructs
========

When you use scalajs-react, ideally all of your code should be functionally pure,
and very strongly-typed. The library expects it. In order to facilitate it,
be precise as to React's exact requirements,
and have dependency free core (important option for JS size minimisation),
there are a number of constructs provided out-of-the-box and woven in to the foundations.

### Callback(To)

Firstly, there is a type `CallbackTo[A]` that represents a repeatable,
typically side-effecting, synchronous procedure that returns an `A` on successful completion.
It's generally on par with various implementations of the `IO` monad.
It's used in all places that React expects to be provided an impure procedure (a callback).

`Callback` is actually a type alias to `CallbackTo[Unit]` because it's so common.
It also comes with it's own, different companion object, full of different goodies that all return `Unit`.

Many ops that are normally provided via typeclasses (eg. `<*`, `>>=`, etc.) are just built-in directly.
If you use the Cats or Scalaz modules there are typeclass instances for it.
Things like `Traverse F` over `Callback{,To}` etc can be found in the companion objects.

See [CALLBACK.md](CALLBACK.md) for a tutorial.


### AsyncCallback

`AsyncCallback` is a pure asynchronous callback.

You can think of this as being similar to using `Future` - you can use it in for-comprehensions the same way -
except `AsyncCallback` is pure and doesn't need an `ExecutionContext`.

When combining instances, it's good to know which methods are sequential and which are parallel
(or at least concurrent).

The following methods are sequential:
- `>>=` / `flatMap`
- `>>` & `<<`
- `flatTap`

The following methods are effectively parallel:
- `*>` & `<*`
- `race`
- `zip` & `zipWith`
- `AsyncCallback.traverse` et al

In order to actually run this, or get it into a shape in which in can be run, use one of the following:
- `toCallback` <-- most common
- `asCallbackToFuture`
- `asCallbackToJsPromise`
- `unsafeToFuture()`
- `unsafeToJsPromise()`

Like `Callback` et al, many ops that are normally provided via typeclasses are built-in directly.
Things like `Traverse F` over `AsyncCallback` etc can be found in the companion object.

A good example is the [Ajax 2 demo](https://japgolly.github.io/scalajs-react/#examples/ajax-2).

### CallbackOption

`CallbackOption` exists and is a callback & option monad stack aka `A => CallbackTo[Option[B]]`.

Check out the online [`CallbackOption` example](https://japgolly.github.io/scalajs-react/#examples/callback-option).

### CallbackKleisli

`CallbackKleisli` exists and is equivalent to a `Kleisli[CallbackTo, A B]` aka `A => CallbackTo[B]`.


Cats and/or Scalaz
==================

```scala
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-cats"     % "1.4.2"
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-scalaz72" % "1.4.2"
```

There are modules available that integrate Cats and/or Scalaz into scalajs-react,
and scalajs-react into Cats and/or Scalaz.

This mostly consists of:
* FP typeclasses instances for scalajs-react types
* Isomorphisms between scalajs-react types and Cats/Scalaz equivalents
* State monadic component state manipulation

React's `setState` methods are asynchronous meaning that `setState(s) >> getState` often won't return `s`.
This is typically not a problem because the are `modState` functions which you can use to specify an
`S => S` but an alternative approach exists which is to use a state monad.
To that end there exists `ReactS`, which is conceptually `WriterT[M, List[Callback], StateT[M, S, A]]`. `ReactS` monads are applied via `runState`. Vanilla `StateT` monads (ie. without callbacks) can be lifted into `ReactS` via `.liftR`. Callbacks take the form of `IO[Unit]` and are hooked into HTML via `~~>`, e.g. `button(onclick ~~> T.runState(blah), "Click Me!")`.

Also included are `runStateF` methods which use a `ChangeFilter` typeclass to compare before and after states at the end of a state monad application, and optionally opt-out of a call to `setState` on a component.

There's only one example at the moment:
[State monad example](https://japgolly.github.io/scalajs-react/#examples/state-monad).

Personally, I thought I'd be coding in this style everywhere when I first started out but after years
of experience (and library improvement) I find that never *really* need to reach for state monad
transformers; `modState` and simple functional composition always suffices *and* makes the code
much clearer to other team members. But go nuts if you want to :)


Monocle
=======

```scala
libraryDependencies ++= Seq(
    "com.github.japgolly.scalajs-react" %%% "ext-monocle"    % "1.4.2",
    "com.github.julien-truffaut"        %%%  "monocle-core"  % "1.5.0",
    "com.github.julien-truffaut"        %%%  "monocle-macro" % "1.5.0"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
```

A module with a extensions for [Monocle](https://github.com/julien-truffaut/Monocle) also exists under `ext-monocle`.

There's one example online that demonstrates Monocle usage:
[`ExternalVar` example](https://japgolly.github.io/scalajs-react/#examples/external-var).

Note: In my experience, optics are absolutely instrumental to writing a scalajs-react UI app
where components are nearly all stateless and *actually* modular and reusable.
Have the components ask for as little as possible, use `StateSnapshots` instead of actual React state
and use optics to glue all the layers together.
On very large codebases especially, this approach scales very, very well.


### Monocle-cats

There's also the Cats version of Monocle.
It's the same as `ext-monocle` module but uses the Cats variant of Monocle.

```scala
libraryDependencies ++= Seq(
    "com.github.japgolly.scalajs-react" %%% "ext-monocle-cats" % "1.4.2",
    "com.github.julien-truffaut"        %%% "monocle-core"     % "1.5.0-cats",
    "com.github.julien-truffaut"        %%% "monocle-macro"    % "1.5.0-cats"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
```
