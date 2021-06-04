# Compile-Time Config

* `.compname.all`
* `.compname.auto`
* `.config.class`

========================================================================================
# TODO

* Rename "compname"
  * "comp-names" ?
  * "component.names" ?
* Rename ".auto" to ".implicit" ?
* Maybe
  ```
  japgolly.scalajs.react.component.names          = allow|blank
  japgolly.scalajs.react.component.names.implicit = full|short|blank
  ```


* specify how/where to specify these settings
* remove elidable
* document everything
* test everything
* Port and remove ElisionTest
========================================================================================


# `.compname.all`

Specifies the transformation to apply to all component display-names, whether automatically/implicitly specified,
or explicitly specified when you create the component.

### Usage:

```
japgolly.scalajs.react.compname.all=allow|blank
```

| Setting | Outcome |
| -- | -- |
| `allow` (default) | Allow all component names. Doesn't perform any transforms. |
| `blank` | Clear all component names. Transforms all names into `""`. |


# `.compname.auto`

Determines how implicitly name components that haven't been created with the explicit specification of an display-name.

### Usage:

```
japgolly.scalajs.react.compname.auto=full|short|blank
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


# `.config.class`

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

  override transparent inline def reusabilityOverride =
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
