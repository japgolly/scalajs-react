## 0.7.0 (unreleased)

* Removed deprecated methods marked for removal in 0.7.0.
* Moved `.experiment` into a new module "extras".
* More supported React tags and attributes.

##### Scalatags / `.vdom` major changes
Scalatags has been removed as a dependency.
It is instead now embedded directly and highly customised for React.

Why? What does that mean?

* Less transforms from Scalatags to what React needs.
* Internal runtime checks are now elidable.
* Therefore, improved performance, smaller JS output.
* No more console warnings from React about styles.
* Attributes now match what you do in React JS. (eg. `onClick` instead of `onclick`)
* Fixed bug where numbers weren't convertible to `ReactNode`.
* Attributes, styles, tags, modifiers support being wrapped in `Option`, `js.UndefOr`, or 'scalaz.Maybe'.
* Removed Scalatags' `()` to `Tag` implicit conversion. It seems like a good idea but can cause certain issues
  (esp with type inference) to be silently ignored, and is unsafe. Use `EmptyTag` instead.
* As an unintended consequence, `bool && (attr := value)` changed to `bool ?= (attr := value)`.
* `Tag` is now `ReactTag`. `Modifier` is now `TagMod`.
* `TagMod` is now composable.
* No more arbitrary subsets of tags and attributes, ie. no more additional importing of `tags2._` etc.
* There are now well-organised modules for imports. This...
  1. Fixes bug with conflicting types for `Tag` being imported.
  2. Gives you the ability to opt-out of messy namespace pollution.
  3. Gives you control to import types, implicits, tags, attrs, styles, separately.
  4. Gives you control to build your own modules.

So instead of this:
```scala
import japgolly.scalajs.react.vdom.ReactVDom.{Tag => _, _}
import all._

def eg(key: String, name: String) =
  div(
    cls := "red",
    keyAttr := key,
    "Hello ", name)
```

it is now recommended that you use prefixes to access tags and attributes.
This means that you don't run into confusing error messages and IDE refactoring issues when using names like
`id`, `a`, `key` for your variables.
```scala
import japgolly.scalajs.react.vdom.prefix_<^._

def eg(key: String, name: String) =
  <.div(
    ^.cls := "red",
    ^.key := key,
    "Hello ", name)
```

If you prefer having all tags and attributes in scope without a prefix, then you can still do that.
Just change your two `vdom` imports to this one:
```scala
import japgolly.scalajs.react.vdom.all._
```

##### Migration script
To ease migration, here is a script that perform 98% of the required changes for you.
Remember to check into source control before running and verify the changes after running.

https://gist.github.com/japgolly/c68482dbadb0077f550c
