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
* Attributes, styles, tags, modifiers support being wrapped in `Option` or `js.UndefOr`.
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

## 0.6.2 (unreleased)


## 0.6.1 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.6.0...v0.6.1))

##### Core module
* Changed overloaded `classSet` methods into `classSet{,1}{,M}`.
* Styles now given to React in camel case. No more warnings.

##### Test module
* `ComponentOrNode` moved to test module and renamed to `ReactOrDomNode`.
* `ReactTestUtils` now accept plain old `ReactElement`s.
* Added `Sel.findFirstIn`.
* Added `simulateKeyDownUp` and `simulateKeyDownPressUp` to `KeyboardEventData` in the test module.
* In rare circumstances, `Simulation.run` targets can go out of date. Targets are now stored by-name.

# 0.6.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.5.4...v0.6.0))

This release brings scalajs-react in line with React 0.12.
**React version 0.12.0 or later is now required.**

Read about React 0.12 changes here:
*  https://github.com/facebook/react/releases/tag/v0.12.0
*  http://facebook.github.io/react/blog/2014/10/28/react-v0.12.html
*  http://facebook.github.io/react/docs/glossary.html

##### Scala 2.10 is no longer supported.
If this affects you please come to [issue #39](https://github.com/japgolly/scalajs-react/issues/39) to discuss,
or continue to using the 0.5.x series.

##### Other changes in this release:
* Deprecated `ReactOutput` and `VDom` in favour of `ReactElement` or in rare cases, `ReactNode`. (*[glossary](http://facebook.github.io/react/docs/glossary.html)*)
* `.asJsArray: Seq[A] → JArray[A]` renamed to `toJsArray`
* `.toJsArray: Seq[A] → JArray[ReactElement]` is no longer needed.
* Renamed `ComponentSpec` to `ReactComponentSpec`. *(Internal. Extremely unlikely anyone using it directly.)*
* Changed signatures of `ReactS.callback` and brethren from `(c)(a)` to `(a,c)`.
* Renamed `ReactS` methods for consistency and added a few missing ones.

Here are a few commands to ease migration.
```
find -name '*.scala' -exec perl -pi -e 's/(?<!\w)(vdom\.)?ReactOutput(?!\w)/ReactElement/g' {} +
find -name '*.scala' -exec perl -pi -e 's/(?<!\w)VDom(?!\w)/ReactElement/g' {} +
find -name '*.scala' -exec perl -pi -e 's/(?<!\w)asJsArray(?!\w)/toJsArray/g' {} + // careful...
find -name '*.scala' -exec perl -pi -e 's/(?<=[ .]render)Component//g' {} +
```

## 0.5.4 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.5.3...v0.5.4))

* Added `nop` and `_nop` to `ReactS.Fix{,T}`.
* Added `T[A]` to `ReactS.Fix{,T}`.
* Added `ReactS.liftIO` (workaround for Intellij).
* Made `ReactS.>>` lazy.

## 0.5.3 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.5.2...v0.5.3))

* Deprecated and renamed `StateT.liftR` in favour of `liftS`.
* Workaround for Scala's type inference failing with `StateT.liftS` on functions.
  Instead of `f(_).liftS`, `f.liftS` is now available and is confirmed to work in `_runState`.

## 0.5.2 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.5.1...v0.5.2))

* Added `ReactEventI` aliases for the very common case that the underlying node is an `<input>`.
* Added tag attributes:
  * `dangerouslySetInnerHTML`. Usage example: `div(dangerouslySetInnerHtml("<span>"))`.
  * `colspan`
  * `rowspan`
* Added to `ReactS`, `Fix` and `FixT`:
  * `callbackM`
  * `zoom`
  * `zoomU`
* Added `ReactS.FixT`:
  * `applyS`
  * `getsS`
  * `modS`
* Added `StateT` extension `liftR` to `ReactST`
* Deprecated `runState` methods handling `StateT` directly. Use `liftR` first.
* Bump Scala 2.11.2 to 2.11.4.

## 0.5.1 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.5.0...v0.5.1))

* Fixed Scalatags rejecting `VDom`.
* Added `ScalazReact.ReactS.setM`.
* Added `Listenable.install{IO,F}`, added `M[_]` to `Listenable.installS`.
* Added `LogLifecycle` which when applied to a component, logs during each lifecycle callback.

# 0.5.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.4.1...v0.5.0))

##### New features
* Added `Simulation` for composition and abstraction of `ReactTestUtils.Simulate` procedures.
* Added `Sel` for easy DOM lookup in tests. Eg. `Sel(".inner a.active.new") findIn myComponent`.
* Experimental Scala-based component mixins. Three mixins are included.  [See examples](https://github.com/japgolly/scalajs-react/blob/master/example/src/main/scala/japgolly/scalajs/react/example/ExperimentExamples.scala).
  * `OnUnmount` - Automatically run stuff when the component unmounts.
  * `SetInterval` - Same as JS `window.setInterval` but automatically calls `clearInterval` on unmount.
  * `Listenable` - A component is able to receive external data. Automatically registers to receive data on mount, and unregisters to stop on unmount.
* Added `ScalazReact.ReactS.liftR` of type `(S ⇒ ReactST[M,S,A]) ⇒ ReactST[M,S,A]`.
* New method `ReactComponentB.domType` now allows you to specify the type returned by `.getDOMNode()` on your component.

##### Fixes & improvements
* Bugfix for tag attributes with primitive values.
  Eg. `input(tpe := "checkbox", checked := false)` works now.
* `ScalazReact`'s `~~>` and `runState` functions are now lazy.
* Upgrade [Scalatags](https://github.com/lihaoyi/scalatags) to 0.4.2. Fixes sourcemap warnings.

##### API changes
Most people will be unaffected by this. Numerous changes were made to internal types making them more consistent _(see [TYPES.md](https://github.com/japgolly/scalajs-react/blob/master/TYPES.md))_.
* `ReactComponentB` accepts an optional DOM node type, which is propagated to `ReactComponentU` and `ReactComponentM`.
* Just as umounted Scala and JS components are denoted by `ReactComponentU` and `ReactComponentU_` respectively,
  mounted Scala and JS components are now denoted by `ReactComponentM` and `ReactComponentM_`.
* Type changes.
  * `ReactComponentB` innards reworked. Use `ReactComponentB[P,S,B]` in place of `ReactComponentB[P]#B2[S]#B3[B]#B4[C]`.
  * `CompCtor`   → `ReactComponentC`
  * `CompCtorP`  → `ReactComponentC.ReqProps`
  * `CompCtorOP` → `ReactComponentC.DefaultProps`
  * `CompCtorNP` → `ReactComponentC.ConstProps`
  * `ReactComponentM` → `ReactComponentM_`
  * `ComponentConstructor_` → `ReactComponentC_`
  * `ComponentConstructor` → `ReactComponentCU`

##### Deprecated
* The following have been renamed, the old names deprecated:
  * `ReactComponentB.create` → `build`
  * `ReactComponentB.createU` → `buildU`
  * `ReactComponentB.propsAlways` → `propsConst`
  * `Nop` → `EmptyTag`

## 0.4.1 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.4.0...v0.4.1))

* Upgrade to scalatags 0.4.0.
* Component builder supports multiple callbacks.
* JS source maps point to Github.

# 0.4.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.2.0...v0.4.0))
* Major overhaul.
* Modules for Scalaz and testing.

## 0.2.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.1.0...v0.2.0))

## 0.1.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/55a19e7...v0.1.0))

