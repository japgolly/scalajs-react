Callback
========

A callback is a procedure that is:
* meant to be run by an event handler or React lifecycle method (as opposed to on the main thread or in a `render` method).
* repeatable. It can be run more than once.
* Is pure (does nothing) in its construction. If you create a `Callback` but never run it, no action or effects should occur.

Callbacks are represented by:
* `Callback` which doesn't return a result.
* `CallbackTo[A]` which returns an `A`.

Actually `Callback` is `CallbackTo[Unit]` with a different companion object, full of different goodies that all return `Unit`.

You can create callbacks in a number of ways:

* By wrapping your code:

  ```scala
  // This is Callback. It is also a CallbackTo[Unit].
  Callback{ println("Hello! I'll be executed later.") }

  // This is a CallbackTo[Int].
  CallbackTo(123)
  ```

* When your component modifies its state via `.setState` or `.modState`, you are provided a `Callback` for the operation.

  ```scala
  componentScope.modState(_.copy(name = newName)) // returns a Callback
  ```

* Using one of the `Callback` object convenience methods

  ```scala
  // Convenience for calling `dom.console.log`.
  Callback.log("Hello Console reader.")

  // Provides both compile-time and runtime warnings that a callback isn't implemented yet.
  Callback.TODO("AJAX not implemented yet")

  // Return a pure value without doing anything
  CallbackTo.pure(0)
  ```

`Callback` also has all kinds of useful methods and combinators. Examples:
* Join callbacks together with many methods like `map`, `flatMap`, `tap`, `flatTap`, and all the squigglies that
  you may be used to in Haskell and inspired libraries like `*>`, `<*`, `>>`, `<<`, `>>=`, etc.
* `.attempt` to catch any error in the callback and handle it.
* `.async`/`.delay(n)` to run asynchronously and return a `Future`.
* `.logResult` to print the callback result before returning it.
* `.logDuration` to measure and log how long the callback takes.

There are other useful methods not listed here.
<br>Have a brief look through the source:
[Callback.scala](../core/src/main/scala/japgolly/scalajs/react/Callback.scala).

Once you have a `Callback` you can run it manually if you need, by calling `.runNow()`.
It isn't necessary and you shouldn't do it because scalajs-react handles it for you to ensure things run at the right time
on the right threads, but if you ever want to, you can.

#### Fusion via `>>`

The `>>` operator deserves a special mention as it's commonly useful.
It's used to fuse to callbacks together sequentially.
It's like a pure version of `;` which is how you sequence statements imperatively.

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
<br>A summary is available here: https://www.dcc.fc.up.pt/~pbv/aulas/tapf/slides/imperative.html

A direct example of how *this* connects to *that*, is the **Equational reasoning** example.
<br>Say we have a callback that prints "ha" twice to display "haha".

```scala
val haha1 = Callback(print("ha")) >> Callback(print("ha"))
```

because `Callback` is referentially-transparent, and is the representation of an action instead of the result of an action,
we can do reduce duplication by doing this:

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


