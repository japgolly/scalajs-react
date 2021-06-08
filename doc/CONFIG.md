# Global Config

* Compile-Time Settings
  * [Limitations](#compile-time-settings-limitations)
  * [Usage](#compile-time-settings-usage)
  * [`.component.names.all`](#componentnamesall)
  * [`.component.names.implicit`](#componentnamesimplicit)
  * [`.config.class` *(Scala 3 only)*](#configclass-scala-3-only)
* Runtime Settings *(development-mode only)*
  * [Usage](#runtime-settings-usage)
  * [`Reusability.disableGloballyInDev()`](#reusabilitydisablegloballyindev)
  * [`ReusabilityOverlay.overrideGloballyInDev()`](#reusabilityoverlayoverridegloballyindev)
  * [Custom `ReusabilityOverride`](#custom-reusabilityoverride)


# Compile-Time Settings: Limitations

There's currently a limitation that components use the compile-time settings available when they're compiled.
Seems like a pretty obvious way to go so why is it a limitation?
When you're writing components in your app (which is what the vast majority of us do), then this isn't a limitation
and it will work the way you expect. On the other hand, if you're writing components in a library, the library author's
settings are used, and downstream users (currently) don't have a way to recompile it with their own settings.
This may (and should) change in the future.
If this is important to you or your organisation, feel free to reach out and sponsor the required work.

# Compile-Time Settings: Usage

Currently, you have to specify compile-time settings to `sbt` directly when you start it.

Examples:

```sh
# Format:
sbt \
 -D<key1>=<value1> \
 -D<key2>=<value2> \
 ...

# Use defaults
sbt

# Clear out all component names
sbt -Djapgolly.scalajs.react.component.names.all=blank
```

# `.component.names.all`

Specifies the transformation to apply to all component display-names, whether automatically/implicitly specified,
or explicitly specified when you create the component.

### Usage:

```
japgolly.scalajs.react.component.names.all=allow|blank
```

| Setting | Outcome |
| -- | -- |
| `allow` (default) | Allow all component names. Doesn't perform any transforms. |
| `blank` | Clear all component names. Transforms all names into `""`. |


# `.component.names.implicit`

Determines how implicitly name components that haven't been created with the explicit specification of an display-name.

### Usage:

```
japgolly.scalajs.react.component.names.implicit=full|short|blank
```

| Setting | Outcome |
| -- | -- |
| `full` (default) | FQCN + field name (except with a trailing `.Comp` or `.Component` removed) |
| `short`  | The last component of the `full` name (above) |
| `blank`  | Empty string. No automatic/implicit component names. |

### Example:

Given the following components:

```scala
package com.example

import japgolly.scalajs.react._

object UsersEditor {
  val Component = ScalaComponent.builder[Unit].renderStatic(<.div).build
}

object Layout {
  val Header = ScalaComponent.builder[Unit].renderStatic(<.div).build
  val Footer = ScalaComponent.builder[Unit].renderStatic(<.div).build
}
```

Outcomes:

| Setting | Name | Name | Name |
| -- | -- | -- | -- |
| `full` (default) | `com.example.UsersEditor` | `com.example.Layout.Header` | `com.example.Layout.Footer` |
| `short` | `UsersEditor` | `Header` | `Footer` |
| `blank` |  |  |  |


# `.config.class` *(Scala 3 only)*

Instructs scalajs-react to use a custom instance of `ScalaJsReactConfig` that you've provided.

### Usage:

Configure as follows where `fqcn` is the fully-qualified classname of your config `object`.

```
japgolly.scalajs.react.config.class=<fqcn>
```

And then create your object in one of the following ways:

```scala
// Recommended method
object Example1 extends ScalaJsReactConfig.Defaults {
  // override settings as desired
}

// Paranoid/full-power method
object Example2 extends ScalaJsReactConfig {

  // `transparent inline` below so that transformations are performed at compile-time
  // and only the results appear in the output JS.

  override transparent inline def automaticComponentName(name: String) =
    ??? // your impl here

  override transparent inline def modifyComponentName(name: String) =
    ??? // your impl here

  override val reusabilityOverride =
    ??? // your impl here
}
```

### Example:

```
japgolly.scalajs.react.config.class=com.example.CustomConfig
```

```scala
package com.example

import japgolly.scalajs.react._

object CustomConfig extends ScalaJsReactConfig.Defaults {
  // `transparent inline` so that the transformation is performed at compile-time
  // and only the result appears in the output JS.
  override transparent inline def automaticComponentName(name: String) =
    name + " (auto)"
}
```


# Runtime Settings: Usage

Runtime settings are designed for use in development-mode only (`fastOptJS`).
They are omitted from production-mode output (`fullOptJS`).

Runtime settings must
1) be specified in your app's `main` method or equivalent entrypoint
2) be evaluated before any affected components are created

Example:

```scala
import japgolly.scalajs.react._
import org.scalajs.dom
import scala.scalajs.js.annotation._

object Main {

  @JSExportTopLevel("main")
  def main() = {

    // Apply runtime settings immediately
    Reusability.disableGloballyInDev()

    // Start app
    val container = dom.document.getElementById("root")
    MyApp().renderIntoDOM(container)
  }
}
```

# `Reusability.disableGloballyInDev()`

*Note: Runtime settings only affect development-mode (`fastOptJS`) and must be applied before any components are created.*

This globally disables `Reusability.shouldComponentUpdate` so that it doesn't nothing.


# `ReusabilityOverlay.overrideGloballyInDev()`

*Note: Runtime settings only affect development-mode (`fastOptJS`) and must be applied before any components are created.*

This makes calls to `Reusability.shouldComponentUpdate` also display a little UI overlay for you to inspect/debug
component updates. You can see a [live example of this in use here](https://japgolly.github.io/scalajs-react/#examples/reusability).


# Custom `ReusabilityOverride`

*Note: Runtime settings only affect development-mode (`fastOptJS`) and must be applied before any components are created.*

You can modify the behaviour of `Reusability.shouldComponentUpdate` to apply any custom logic you like
by creating your own instance of `ScalaJsReactConfig.ReusabilityOverride` and providing to
`ScalaJsReactConfig.Defaults.overrideReusabilityInDev()`.

Example:

```scala
import japgolly.scalajs.react._
import org.scalajs.dom
import scala.scalajs.js.annotation._

object Main {

  private object logComponents extends ScalaJsReactConfig.ReusabilityOverride {
    override def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot] = b => {
      println("Detected component with Reusability.shouldComponentUpdate: " + b.name)
      b
    }
  }

  @JSExportTopLevel("main")
  def main() = {

    // Apply runtime settings immediately
    ScalaJsReactConfig.Defaults.overrideReusabilityInDev(logComponents)

    // Start app
    val container = dom.document.getElementById("root")
    MyApp().renderIntoDOM(container)
  }
}
```
