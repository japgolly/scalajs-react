*I've not documented this side of things as well as I'd like.*

*For now, please just browse the source (it's not massive) and follow the types.*

Built-in Constructs
===================

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


`StateSnapshot[S]`
==================

**This is important!**

Just as we don't like state in our FP code, we shouldn't like stateful components in our scalajs-react FP code.
Stateful components are problematic and rely on runtime guarantees to work as expected; if you're on this page
you likely also want to switch out runtime guarantees for compile-time guarantees.

Enter: `StateSnapshot`!

Detailed doc for `StateSnapshot` doesn't exist yet (pls help!) but I think the best way to see how to handle
state in an FP-manner is to read https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2
which demonstrates the recommended way of handling state in scalajs-react.


Cats & Cats Effects
===================

See [Modules doc](./MODULES.md).


Monocle
=======

See [Modules doc](./MODULES.md).

There's one example online that demonstrates Monocle usage:
[`ExternalVar` example](https://japgolly.github.io/scalajs-react/#examples/external-var).

Note: In my experience, optics are absolutely instrumental to writing a scalajs-react UI app
where components are nearly all stateless and *actually* modular and reusable.
Have the components ask for as little as possible, use `StateSnapshots` instead of actual React state
and use optics to glue all the layers together.
On very large codebases especially, this approach scales very, very well.
