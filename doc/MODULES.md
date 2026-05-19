# Modules

And by modules, I mean Maven artifacts.

- [Guide](#guide)
- [Guide with Cats Effect as default effect types](#guide-with-cats-effect-as-default-effect-types)
- [Reference](#reference)


# Guide

These are the directions for using scalajs-react in its default way,
with the opt-in optional features shown for you to accept or remove.

Add to sbt:

```scala
val ScalaJsReactVer = "<latest>"

libraryDependencies ++= Seq(

  // Optional extensions for Cats / Cats Effect
  // (Note: these need to come before "core")
  "com.github.japgolly.scalajs-react" %%% "core-ext-cats"        % ScalaJsReactVer,
  "com.github.japgolly.scalajs-react" %%% "core-ext-cats_effect" % ScalaJsReactVer,

  // Mandatory
  "com.github.japgolly.scalajs-react" %%% "core"                 % ScalaJsReactVer,

  // Optional utils exclusive to scalajs-react
  "com.github.japgolly.scalajs-react" %%% "extra"                % ScalaJsReactVer,

  // Optional extensions to `core` & `extra` for Monocle
  "com.github.japgolly.scalajs-react" %%% "extra-ext-monocle2"   % ScalaJsReactVer,
  "com.github.japgolly.scalajs-react" %%% "extra-ext-monocle3"   % ScalaJsReactVer,

  // For unit tests
  "com.github.japgolly.scalajs-react" %%% "test"                 % ScalaJsReactVer % Test,
)
```

Imports:

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.ReactCats._
import japgolly.scalajs.react.ReactMonocle._

// Unit tests
import japgolly.scalajs.react.test._
```

If you include the `core-ext-cats_effect` module you'll be able to provide `cats.effect.{SyncIO,IO}` instances
directly to scalajs-react in places like component lifecycle callbacks, event handlers, etc.
No additional imports required.


# Guide with Cats Effect as default effect types

These are the instructions to use scalajs-react with `cats.effect.{SyncIO,IO}` as the default effects,
meaning that in all cases where the return type would normally be a `CallbackTo` or an `AsyncCallback`
you'll now get a `SyncIO` and `IO` respectively.

By default, the `Callback` classes won't even be on the classpath.
If you include the `callback` module you'll get them back on the classpath and you'll be able to provide them
directly to scalajs-react in places like component lifecycle callbacks, event handlers, etc.
No additional imports required.

Add to sbt:

```scala
val ScalaJsReactVer = "3.0.0"

libraryDependencies ++= Seq(

  // Optionally include scalajs-react Callback classes
  // (Note: these need to come before "core-bundle-cats_effect")
  "com.github.japgolly.scalajs-react" %%% "callback"                 % ScalaJsReactVer,
  "com.github.japgolly.scalajs-react" %%% "callback-ext-cats"        % ScalaJsReactVer,
  "com.github.japgolly.scalajs-react" %%% "callback-ext-cats_effect" % ScalaJsReactVer,

  // Mandatory
  "com.github.japgolly.scalajs-react" %%% "core-bundle-cats_effect"  % ScalaJsReactVer,

  // Optional utils exclusive to scalajs-react
  "com.github.japgolly.scalajs-react" %%% "extra"                    % ScalaJsReactVer,

  // Optional extensions to `core` & `extra` for Monocle
  "com.github.japgolly.scalajs-react" %%% "extra-ext-monocle2"       % ScalaJsReactVer,
  "com.github.japgolly.scalajs-react" %%% "extra-ext-monocle3"       % ScalaJsReactVer,

  // For unit tests
  "com.github.japgolly.scalajs-react" %%% "test"                     % ScalaJsReactVer % Test,
)
```

Imports:

```scala
import japgolly.scalajs.react._ // automatically includes ReactCats._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.ReactMonocle._

// Unit tests
import japgolly.scalajs.react.test._
```


# Reference

These are all the modules meant for end-user selection.

| Name                       | Description |
|----------------------------|-------------|
| `callback`                 | Lightweight but powerful effects classes:<br> `AsyncCallback`, `Callback(To)`, `CallbackOption` |
| `callback-ext-cats`        | `cats.MonadError` instances for classes in the `callback` module |
| `callback-ext-cats_effect` | For classes in the `callback` module: <ul><li>`cats.effect.{Sync,Async}` instances</li><li>Conversions from/to `cats.effect.{SyncIO,IO}`</li></ul>  |
| `core`                     | Core scalajs-react functionality with `{Callback,AsyncCallback}` as the default effects |
| `core-bundle-cats_effect`  | Core scalajs-react functionality with `cats.effect.{SyncIO,IO}` as the default effects<br>*(Note: `callback` module not included. Add as a separate dependency if required.)* |
| `core-bundle-cb_io`        | Core scalajs-react functionality with `Callback` as the default sync effect and `cats.effect.IO` as the default async effect.<br>*(Note: both `callback` and `core-ext-cats_effect` modules are included.)* |
| `core-ext-cats`            | Extensions to the `core` module for Cats |
| `core-ext-cats_effect`     | Extensions to the `core` module for Cats Effect |
| `extra`                    | Optional utils exclusive to scalajs-react. ([details](./EXTRA.md)) |
| `extra-ext-monocle2`       | Extensions to `core` & `extra` modules for Monocle v2 |
| `extra-ext-monocle3`       | Extensions to `core` & `extra` modules for Monocle v3 |
| `facade`                   | Scala.JS facades for `React`, `ReactDOM`, `ReactDOMServer` |
| `facade-test`              | Scala.JS facades for `ReactTestUtils` |
| `test`                     | Utilities for testing React components |

![main modules](https://rawgit.com/japgolly/scalajs-react/master/doc/modules-main.gv.7563cdfa19f268785faed6501fe5ff66.svg)

There are also other modules that are for internal/technical purposes and are transitive (indirect) dependencies.
This is the full module diagram:

![all modules](https://rawgit.com/japgolly/scalajs-react/master/doc/modules.gv.89d12e3a880d53fc8545744266e9aeb7.svg)
