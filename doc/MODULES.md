# Guide

Sbt:

```scala
val ScalaJsReactVer = "2.0.0"

libraryDependencies ++= Seq(

  // Optional extensions for Cats / Cats Effect
  // (Note: these need to come before "core")
  "com.github.japgolly.scalajs-react" %%% "core-ext-cats"        % ScalaJsReactVer,
  "com.github.japgolly.scalajs-react" %%% "core-ext-cats-effect" % ScalaJsReactVer,

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

# Guide with `cats.effect.{SyncIO,IO}` as default effect types

Sbt:

```scala
val ScalaJsReactVer = "2.0.0"

libraryDependencies ++= Seq(

  // Optionally include scalajs-react Callback classes
  // (Note: these need to come before "core-bundle-cats-effect")
  "com.github.japgolly.scalajs-react" %%% "callback"                 % ScalaJsReactVer,
  "com.github.japgolly.scalajs-react" %%% "callback-ext-cats"        % ScalaJsReactVer,
  "com.github.japgolly.scalajs-react" %%% "callback-ext-cats-effect" % ScalaJsReactVer,

  // Mandatory
  "com.github.japgolly.scalajs-react" %%% "core-bundle-cats-effect"  % ScalaJsReactVer,

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
import japgolly.scalajs.react._ // includes ReactCats._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.ReactMonocle._

// Unit tests
import japgolly.scalajs.react.test._
```


# Reference

| Name                       | Description |
|----------------------------|-------------|
| `callback`                 | Lightweight but powerful effects classes:<br> `AsyncCallback`, `Callback(To)`, `CallbackOption` |
| `callback-ext-cats`        | `cats.MonadError` instances for classes in the `callback` module |
| `callback-ext-cats-effect` | For classes in the `callback` module: <ul><li>`cats.effect.{Sync,Async}` instances</li><li>Conversions from/to `cats.effect.{SyncIO,IO}`</li></ul>  |
| `core`                     | Core scalajs-react functionality with `{Callback,AsyncCallback}` as the default effects |
| `core-bundle-cats-effect`  | Core scalajs-react functionality with `cats.effect.{SyncIO,IO}` as the default effects<br>*(Note: `callback` module not included. Add as a separate dependency if required.)* |
| `core-ext-cats`            | Extensions to the `core` module for Cats |
| `core-ext-cats-effect`     | Extensions to the `core` module for Cats Effect |
| `extra`                    | Optional utils exclusive to scalajs-react. ([details](./EXTRA.md)) |
| `extra-ext-monocle2`       | Extensions to `core` & `extra` modules for Monocle v2 |
| `extra-ext-monocle3`       | Extensions to `core` & `extra` modules for Monocle v3 |
| `facade`                   | Scala.JS facades for `React`, `ReactDOM`, `ReactDOMServer` |
| `facade-test`              | Scala.JS facades for `ReactTestUtils` |
| `test`                     | Utilities for testing React components |

![module diagram](https://rawgit.com/japgolly/scalajs-react/master/doc/modules.gv.svg)
