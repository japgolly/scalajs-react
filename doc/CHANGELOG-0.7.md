# 0.7.2 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.7.1...v0.7.2))

* Added `isMounted` to component scopes.
* Added `Addons.ReactCloneWithProps`.
* Dropped Scalaz 7.0 support. (Scalaz 7.1 still supported.)
* Upgrade Scala.JS to 0.6.0.


# 0.7.1 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.7.0...v0.7.1))

* Support custom tags, attributes and styles via `"string".react{Attr,Style,Tag}`.
* The `class` attribute now gets some special treatment in that it appends rather than overwrites.
  `<.div(^.cls := "a", ^.cls := "b")` is now the same as `<.div(^.cls := "a b")`.


# 0.7.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.6.1...v0.7.0))

To ease migration, here is a script that perform 98% of the required changes for you:
https://gist.github.com/japgolly/c68482dbadb0077f550c

#### Changes:

* Moved `.experiment` into a new module called `extra`.
* Added a `ext-monocle` module with a few extensions for [Monocle](https://github.com/julien-truffaut/Monocle).
* More supported React tags and attributes.
* More ScalazReact extensions:
  * `stateIO`
  * `setStateIO`
  * `modStateIO`
  * `modStateIOF`
  * `_setStateIO`
  * `_modStateIO`
  * `_modStateIOF`
  * Attr operator `~~>?` which is `~~>` but for optional callbacks.
* Removed deprecated methods marked for removal in 0.7.0.
* Deprecated `modStateO`, `modStateU`, `attr runs callback`.

#### Router
New in this release is a router, type-safe and written entirely in Scala.js, for Single-Page Applications.

See [ROUTER.md](https://github.com/japgolly/scalajs-react/blob/v0.7.0/extra/ROUTER.md) for details.

#### Refs
* Refs can now be applied to components from the outside, prior to mounting. ([#44](https://github.com/japgolly/scalajs-react/issues/44))
* Refs can now refer to components and keep their types intact.
* Scala-based ReactComponent constructors now have `.set(key = ?, ref = ?)` with `.withKey(k)` being an alias for `.set(key = k)`.
* Instances (not objects) of `Ref` renamed to `RefSimple`, `RefP` renamed to `RefParam`. Creating refs is unchanged, so `val r = Ref("hehe")` still works.

#### Scalatags / `.vdom` major changes
Scalatags has been removed as a dependency.
It is instead now embedded directly and highly customised for React.

Why? What does that mean?

* Less transforms from Scalatags to what React needs.
* Internal runtime checks are now elidable.
* Therefore, improved performance, smaller JS output.
* No more console warnings from React about styles.
* Attributes now match what you do in React JS. (eg. `onClick` instead of `onclick`)
* Fixed bug where numbers weren't convertible to `ReactNode`.
* Attributes, styles, tags, modifiers support being wrapped in `Option`, `js.UndefOr`, or `scalaz.Maybe`.
* Removed Scalatags' `()` to `Tag` implicit conversion. It seems like a good idea but can cause certain issues
  (esp with type inference) to be silently ignored, and is unsafe. Use `EmptyTag` instead.
* An unintended consequence, `bool && (attr := value)` changed to `bool ?= (attr := value)`.
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

The default prefixes (recommended) is available via `vdom.prefix_<^._` and it provides two prefixes.

1. `<` for tags. _(Example: `<.div`)_
2. `^` for attributes and styles. _(Example: `^.onClick`)_

The above example becomes:
```scala
import japgolly.scalajs.react.vdom.prefix_<^._

def eg(key: String, name: String) =
  <.div(
    ^.cls := "red",
    ^.key := key,
    "Hello ", name)
```

If you prefer having all tags and attributes in scope without a prefix, then you can still do that by using this import:
```scala
import japgolly.scalajs.react.vdom.all._
```
