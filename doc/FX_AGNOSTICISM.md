# Creating an Effect-Agnostic Library

*(Note: This is only for library authors. None of this is necessary for end-users writing apps.)*

scalajs-react v2 introduced a new feature called "effect generalisation" that allows users to choose their own types of
effects. (more detail in the [v2 changelog](./changelog/2.0.0.md))

If you're writing a library that handles effects, you can still just extend the `core` module and use `Callback` and
friends directly, but if you want your library to be effect-agnostic then follow this guide.

* [Required sbt changes](#required-sbt-changes)
* [Effects as method parameters](#effects-as-method-parameters)
* [Producing effects from (static) methods](#producing-effects-from-static-methods)
* [Producing effects from classes](#producing-effects-from-classes)
* [Producing effects from traits](#producing-effects-from-traits)
* [Modifying effects](#modifying-effects)
* [Implicit syntax/ops](#implicit-syntaxops)


# Required sbt changes

1. Use different scalajs-react modules. This will remove `Callback` and friends from the classpath and provide a dummy
   effect to code against that will later be replaced by users choosing their effect types.

    ```diff
     libraryDependencies ++= Seq(
    -  "com.github.japgolly.scalajs-react" %%% "core"                % ScalaJsReactVer,
    +  "com.github.japgolly.scalajs-react" %%% "core-generic"        % ScalaJsReactVer,
    +  "com.github.japgolly.scalajs-react" %%% "util-dummy-defaults" % ScalaJsReactVer % Provided,
     )
    ```

2. A trade-off in the design of "effect generalisation" means that us library authors need to be careful not to write
   certain kinds of code because although they'll compile locally, they'll cause linking errors downstream due to
   differences in effect erasure. The rules themselves will be spelt out in the next section but for now, just know
   that there's a scalafix rule for this.

    1. Add to your `project/plugins.sbt`:
        ```scala
        addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.29")
        ```

    1. Create `scalafix.sbt` with:
        ```scala
        ThisBuild / scalafixDependencies       += "com.github.japgolly.scalajs-react" %% "scalafix" % "3.0.0"
        ThisBuild / scalafixScalaBinaryVersion := "2.13"
        ThisBuild / semanticdbEnabled          := true
        ThisBuild / semanticdbVersion          := "4.4.23"

        ThisBuild / scalacOptions ++= {
          if (scalaVersion.value startsWith "2")
            "-Yrangepos" :: Nil
          else
            Nil
        }
        ```

    1. Create `.scalafix.conf` with:
        ```conf
        rules = [
          ExplicitResultTypes, // remove if you want but make sure to add explicit types to methods returning effects
          ScalaJsReactEffectAgnosticism,
        ]
        ```

    1. In your `build.sbt`, add to all modules with generic effects:
        ```scala
        .settings(
          scalafixOnCompile := scalaVersion.value.startsWith("2"), // scalafix for Scala 3 not yet supported
        )
        ```


# Effects as method parameters

1. **Accepting a fire-and-forget callback:**

    ```scala
    def oldWay(c: Callback): Unit =
      c.runNow()
    ```

    ```scala
    import japgolly.scalajs.react.util.Effect.Dispatch

    def newWay[F[_]](f: F[Unit])(implicit x: Dispatch[F]): Unit =
      x.dispatch(f)
    ```

    This approach will not only accept any synchronous effect, but also asynchronous effects too.

1. **Accepting a sync effect:**

    ```scala
    def oldWay[A](c: CallbackTo[A]): A =
      c.runNow()
    ```

    ```scala
    import japgolly.scalajs.react.util.Effect.Sync

    def newWay[F[_], A](f: F[A])(implicit x: Sync[F]): A =
      x.runSync(f)
    ```

1. **Accepting an async effect:**

    ```scala
    def oldWay_run[A](c: AsyncCallback[A]): Unit =
      c.runNow()

    def oldWay_promise[A](c: AsyncCallback[A]): js.Promise[A] =
      c.unsafeToJsPromise()
    ```

    ```scala
    import japgolly.scalajs.react.util.Effect.Async
    import scala.scalajs.js

    def newWay_run[F[_], A](f: F[A])(implicit x: Async[F]): Unit =
      x.dispatch(f)

    def newWay_promise[F[_], A](f: F[A])(implicit x: Async[F]): js.Promise[A] =
      x.toJsPromise(f)()
    ```


# Producing effects from static methods


```scala
def oldWay(n: Int): CallbackTo[Int] =
  CallbackTo(n * n)
```

```scala
import japgolly.scalajs.react.util.Effect.Sync

def newWay[F[_]](n: Int)(implicit x: Sync[F]): F[Int] =
  x.delay(n * n)
```


# Producing effects from classes

*Note: Simply changing `Callback` to the default effect `DefaultEffects.Sync` doesn't work because differences in erasure will lead to Scala.JS linking errors.*

```scala
object OldWay {
  // Friendly constructor (not needed in Scala 3)
  def apply(): OldWay =
    new OldWay
}

class OldWay {
  private var active = false

  val onStart: Callback =
    Callback { active = true }

  val onStop: Callback =
    Callback { active = false }
}
```

becomes...

```scala
import japgolly.scalajs.react.util.DefaultEffects.{Sync => DS} // DS = [D]efault [S]ync effect
import japgolly.scalajs.react.util.Effect.Sync

// Notice the "F" suffix
object NewWayF {
  // Let's capture all mutable state in something that we can pass around
  final class State {
    var active = false
  }
}

// Notice the "F" suffix
class NewWayF[F[_]](state: NewWayF.State)(implicit F: Sync[F]) {

  val onStart: F[Unit] =
    F.delay { state.active = true }

  val onStop: F[Unit] =
    F.delay { state.active = false }

  // Allow users to change the effect type. Notice how we maintain the state.
  def withEffect[G[_]](implicit G: Sync[G]): NewWayF[G] =
    G.subst[F, NewWayF](this)(new NewWayF[G](state))
}

// No more "F" suffix. This uses whatever the user's default effect type is.
class NewWay(state: NewWayF.State) extends NewWayF[DS](state)

object NewWay {
  // Constructor that uses the default effect (and is backwards-source-compatible with OldWay)
  def apply(): NewWay =
    new NewWay(new NewWayF.State)
}
```


# Producing effects from traits

*Note: Simply changing `Callback` to the default effect `DefaultEffects.Sync` doesn't work because differences in erasure will lead to Scala.JS linking errors.*

```scala
trait OldWay {
  protected var active = false

  val onStart: Callback =
    Callback { active = true }

  val onStop: Callback =
    Callback { active = false }
}

// Example usage
object Blah extends OldWay {
  def highFive = 5
}
```

becomes...

```scala
import japgolly.scalajs.react.util.DefaultEffects.{Sync => DS} // DS = [D]efault [S]ync effect
import japgolly.scalajs.react.util.Effect.Sync

// Notice the "F" suffix
object NewWayF {
  // Let's capture all mutable state in something that we can pass around
  final class State {
    var active = false
  }

  def apply[G[_]](s: State)(implicit G: Sync[G]): NewWayF[G] =
    new NewWayF[G] {
      override implicit protected F = G
      override protected def state = s
    }
}

// Notice the "F" suffix
trait NewWayF[F[_]] {

  implicit protected F: Sync[F]
  protected def state: NewWayF.State

  val onStart: F[Unit] =
    F.delay { state.active = true }

  val onStop: F[Unit] =
    F.delay { state.active = false }

  // Allow users to change the effect type. Notice how we maintain the state.
  def withEffect[G[_]](implicit G: Sync[G]): NewWayF[G] =
    G.subst[F, NewWayF](this)(NewWayF[G](state))
}

// No more "F" suffix. This uses whatever the user's default effect type is.
trait NewWay extends NewWayF[DS] {
  override implicit protected F = DS
  override protected lazy val state = new NewWayF.State
}

// Example usage
object Blah extends NewWay {
  def highFive = 5
}
```

# Modifying effects

Sometimes you need to accept an effect, modify it, then return it.

```scala
def oldWay[A](c: CallbackTo[A]): CallbackTo[Option[A]] =
  c.map(Option(_))
    .flatmap(o => Callback { println("Result is " + o); o })
```

becomes...

```scala
import japgolly.scalajs.react.util.Effect.Sync

def newWay[F[_], A](f: F[A])(implicit F: Sync[F]): F[Option[A]] = {
  // no implicit ops lol. This case is rare and I don't want to needlessly add to output JS size
  val fo = F.map(f)(Option(_))
  F.flatmap(fo)(o => F.delay { println("Result is " + o); o })
}
```

This can be even more abstract if you want, no need to restrict it to only synchronous effects:

```scala
import japgolly.scalajs.react.util.Effect.Monad

def newWay[F[_], A](f: F[A])(implicit F: Monad[F]): F[Option[A]] = {
  // no implicit ops in this example so that it doesn't needlessly add to output JS size.
  // implicit ops are exactly available though, see the it's section in this doc.
  val fo = F.map(f)(Option(_))
  F.flatmap(fo)(o => F.delay { println("Result is " + o); o })
}
```


# Implicit syntax/ops

Implicit ops have now been added to make working with general effects easier.

```scala
import japgolly.scalajs.react.util.Effect
import japgolly.scalajs.react.util.syntax._

def example(fi: F[Int])(implicit F: Effect.Sync[F]): F[Int] =
  for {
    i <- fi
    j <- F.delay(123)
  } yield {
    println("Re-running fi = " + fi.runSync())
    i + j
  }
```
