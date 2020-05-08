Callback
========

The `Callback` class encapsulates logic and side-effects that are meant to be *executable by React*, when/if React chooses to execute it. Examples are React responding to the user clicking a button, or React unmounting a component.

There's also this blog post with a great introduction to typed side-effects:
http://typelevel.org/blog/2017/05/02/io-monad-for-cats.html

**WARNING**: *If you're new to scalajs-react and typed effects (or just functional programming in general), then it's important you read this because if you incorrectly mix this with imperative-style code that performs side-effects, you'll likely have runtime bugs where data goes stale and/or changes go undetected.*


<br>

#### Contents
- [Introduction](#introduction)
- [Utilities once you have a `Callback`](#utilities-once-you-have-a-callback)
- [Composition](#composition)
- [Monadic Learning Curve](#monadic-learning-curve)
- [Manual Execution](#manual-execution)
- [Common Mistakes](#common-mistakes)
- [Callbacks and Futures](#callbacks-and-futures)

Introduction
============

A callback is a procedure that is:
* Executable by React when/if it chooses (usually an event handler or React lifecycle method.
* Executed asynchronously by React.
* Repeatable. It can be run more than once.
* Pure (does nothing) when you create an instance. If you create a `Callback` but never run it, no action or effects should occur.

Callbacks are represented by:
* `Callback` which doesn't return a result.
* `CallbackTo[A]` which returns an `A`.

Intuitively, you can think of a `CallbackTo[A]` as `() => A`, and a `Callback` as a `() => Unit`.

Actually `Callback` is `CallbackTo[Unit]` with a different companion object, full of different goodies that all return `Unit`.


Creation
========

There are a number of ways in which to create a callback.

The simplest is just to surround all of your code in `Callback{ ... }` or  `CallbackTo{ ... }`.
Example:
```scala
// This is Callback. It is also a CallbackTo[Unit].
val sayHello = Callback {
  println("Hello, I'll be executed asynchronously.")
  println("Bye!")
}
```

Any impure logic/effects (such as accessing DOM state, AJAX, or global variables), *must be inside* the Callback.
Example:
```scala
object Auth {
  private var _isUserLoggedIn = false

  val isUserLoggedIn: CallbackTo[Boolean] =
    CallbackTo(_isUserLoggedIn)

  // Callback = CallbackTo[Unit] = no result
  val login: Callback =
    Callback(_isUserLoggedIn = true)
}
```

Another way to create a `Callback` is by calling `.setState` or `.modState` on a component.
The result of these methods is already a `Callback` because a component's state is only changed when React responds to some kind of
event; it's an error to sychronously update the state in a component's `render` method, for example.
```scala
myComponent.setState(Person(1001, "Bob")) // returns a Callback
```

There are also convenience methods in the `Callback` object:
```scala
// Convenience for calling `dom.console.log`.
Callback.log("Hello Console reader.")

// Provides both compile-time and runtime warnings that a callback isn't implemented yet.
Callback.TODO("AJAX not implemented yet")

// Return a pure value without doing anything
CallbackTo.pure(0)
```

Utilities once you have a `Callback`
====================================

`Callback` instances come with a bunch of useful utility methods:
* `.attempt` to catch any error in the callback and handle it.
* `.async`/`.delay(n)` to run asynchronously.
* `.logResult` to print the callback result before returning it.
* `.logDuration` to measure and log how long the callback takes.
* `.map` (as you would expect) to transform the result.
* `.when`/`.unless` to add a condition so that sometimes the callback will be ignored. Like an `if` statement.
* More; see: [Callback.scala](../core/src/main/scala/japgolly/scalajs/react/Callback.scala).


Composition
===========

When you want to compose multiple `Callback` instances, there are many ways depending on what specifically you want to do.
* *(Most-common)* `.flatMap` and/or for-comprehensions. Same as using Scala's `Future`.
* *(Most-common)* `>>`. This operator composes callbacks sequentially. i.e. `a >> b >> c` will create a new callback which will execute `a` first, then `b` second, and finally `c` third.
* If you want to compose more than two callbacks, or don't know how many you'll have at runtime, there is `Callback.sequence` and `Callback.traverse`.
* Monadic and applicative ops that you'd expect coming from languages like Haskell are there (`*>`, `<*`, `>>`, `<<`, `>>=`, etc). They're baked in rather than typeclass-provided.

The `>>` operator deserves a special mention as it's commonly useful.
It's used to fuse two callbacks together sequentially.
It's like a pure version of `;` which is how you sequence statements imperatively (i.e. `doThis(); doThat()` becomes `doThis >> doThat`).

```scala
def greet: Callback =
  Callback {
    println("Hello.")
    println("Goodbye.")
  }

// This ↑ is equivalent to this ↓

def greet: Callback = {
  val hello = Callback(println("Hello."))
  val bye   = Callback(println("Goodbye."))
  hello >> bye
}

// which is equivalent to this ↓

def greet: Callback = {
  val hello = Callback(println("Hello."))
  val bye   = Callback(println("Goodbye."))
  hello.flatMap(_ => bye)
}

// and again, equivalent to this ↓

def greet: Callback =
  for {
    _ <- Callback(println("Hello."))
    _ <- Callback(println("Goodbye."))
  } yield ()
}
```

If you're wondering why `>>`, it's a convention used in various other monadic libraries.
I like to read it like a pipeline where the arrows show control flow.
This ↓ shows that `a` is run, then it flows right to run `b`, then flows right to run `c`.

```scala
a >> b >> c
```

Now let me also introduce `>>=` which is an alias for `flatMap`. As we know, `flatMap` takes an argument. The signature in a `CallbackTo[A]` is `def flatMap[B](f: A => CallbackTo[B]): CallbackTo[B]`. Now consider this example:

```scala
a >> b >>= c
```

It still looks like a pipeline but this time there is a `=` symbol popping out which you could imagine spits out a value.
The flow is still intact (`a` to `b` to `c`) except this time we know that `c` takes the output of `b`. If we expand that example into real code, it looks like this:

```scala
val a = Callback(println("Start"))
val b = CallbackTo(300)
val c = (i: Int) => Callback(println("Input = " + i))

val x = a >> b >>= c

x.runNow()
// Start
// Input = 300
```

#### Monadic Learning Curve

Working with `Callback`/`CallbackTo` might seem odd if you're not used to capturing effects with monads, but it's a transferable skill with applications outside of `Callback` itself.

If you'd like to learn more about this approach, see *How to declare an imperative* by Philip Wadler.
<br>A summary is available here: http://www.dcc.fc.up.pt/~pbv/aulas/tapf/handouts/imperative.html

A direct example of how *this* connects to *that*, is the **Equational reasoning** example.
<br>Say we have a callback that prints "ha" twice to display "haha".

```scala
val haha1 = Callback(print("ha")) >> Callback(print("ha"))
```

because `Callback` is referentially-transparent, and is the representation of an action instead of the result of an action,
we can reduce duplication by doing this:

```scala
val ha = Callback(print("ha"))
val haha2 = ha >> ha
```

Because equational reasoning holds, both callbacks are equivalent.
```
scala> haha1.runNow()
haha
scala> haha2.runNow()
haha
```

If you're getting started and find monads hard, remember you can start by just wrapping your imperative code.
So instead of:
```scala
def speak(): Unit = {
  println("Hello!")
  println("Goodbye...")
}
```
you can wrap it like:
```scala
def speak = Callback {
  println("Hello!")
  println("Goodbye...")
}
```

Notice that we change `speak()` into `speak` as it's now pure.
<br>Also be careful to call `.runNow()` on any inner callbacks if you take this approach. If you don't, scalac will just throw it away without executing it which is probably not what you want -- Scala does all kinds of nasty things when `Unit` is involved which is what `;` does between imperative statements.

It's actually recommended that you *not* take this approach and instead, use proper operators to combine callbacks as the compiler will be able to offer help and catch problems.


Manual Execution
================

As is stressed above, `Callback`s are meant to be executed by React at a time and frequency of its choosing.

There are scenarios in which you may want to execute a callback manually:
* Working with an external service (eg. a websocket callback)
* In a unit test

There are two ways to go about this:

1. Execute the `Callback` yourself by calling `.runNow()`.
2. Ask `scalajs-react` for an interface that performs the side-effects directly instead of returning `Callback`s. See https://japgolly.github.io/scalajs-react/#examples/websockets for a demo.

Reminder: You should never do this in a React context.


Common Mistakes
===============

* **Executing instead of composing.**

  Callbacks must do nothing when you first create them; React might never even call them.
  If you call `.runNow()` when you're creating a `Callback` that's a bug because you're forcing a one-time execution during construction.

  If fact, even the `{` in `def increment(): Callback = {` is a code-smell. Either use `(` or nothing at all.

  BROKEN:
  ```scala
  def increment(): Callback = {
    $.modState(_ + 1).runNow()
    Callback.log("Scheduled state increment by 1")
  }
  ```

  FIXED:
  ```scala
  val increment: Callback =
    $.modState(_ + 1) >>
    Callback.log("Scheduled state increment by 1")
  ```

* **Side-effects (especially accessing mutable state) during construction**

  Callbacks must be repeatable.
  If you perform a side-effect during construction then it happens exactly once (instead of repeatedly) and at the wrong time (construction instead of callback execution).
  If during construction, you read mutable state like a global variable then it is read exactly once and at the wrong time too, which will give the impression at runtime that the state is never updated.

  Make sure anything impure is *inside* the callback.

  BROKEN:
  ```scala
  def grantPrivateAccess = {
    val rule: js.Any = global.window.localStorage.getItem("token")
    CallbackTo(rule != null)
  }
  ```

  FIXED:
  ```scala
  val grantPrivateAccess = CallbackTo {
    val rule: js.Any = global.window.localStorage.getItem("token")
    rule != null
  }
  ```

  Even better is to isolate the impurity and make it DRY.
  ```scala
  val getToken: CallbackTo[js.Any] =
    CallbackTo(global.window.localStorage.getItem("token"))

  val grantPrivateAccess: CallbackTo[Boolean] =
    getToken.map(_ != null)
  ```

* **() => Callback**

  Callbacks are already repeatable, and do nothing when you create one.
  There's no need or benefit to adding `() =>`.

  Just pass around `Callback` instances instead. If React never chooses to execute them, they'll never be executed.

  But look, if for some reason you just really, really want to have a side-effect in the *construction* of the callback,
  then wrap the construction in `Callback.byName` and continue to pass around `Callback` instead of `() => Callback`.


Callbacks and Futures
=====================

When working with Scala `Future`s (or JS `Promise`s), `AsyncCallback` should be used.

There are a number of conversions available to convert between `Callback` and `Future`.

| Input                      | Method                 | Output                  |
| -------------------------- | ---------------------- | ----------------------- |
| `=> Future[A]`             | `AsyncCallback.fromFuture(f)`        | `AsyncCallback[A]` |
| `AsyncCallback[A]`            | `cb.unsafeToFuture()`          | `Future[A]`             |
| `CallbackTo[A]`            | `cb.asAsyncCallback`          | `AsyncCallback[A]`             |

If you're looking for ways to block (eg. turning a `Future[A]` or `AsyncCallback[A]` into a `Callback[A]`),
it is not supported by Scala.JS (See [#1996](https://github.com/scala-js/scala-js/issues/1996)).

**NOTE:** It's important that when going from `Future` to `AsyncCallback`, you're aware of when the `Future` is instantiated. You should capture the initiation of the `Future`, not just the resulting value.

```scala
def queryServer: Future[Data] = ???

// This is GOOD because the callback wraps the queryServer *function*, not an instance.
AsyncCallback.fromFuture(queryServer)

// This is BAD because the callback wraps a single instance of queryServer.
// 1) The server will be contacted immediately instead of when the callback executes.
// 2) If the callback is executed more than once, the future and old result will be reused.
val f = queryServer
AsyncCallback.fromFuture(f)
```
