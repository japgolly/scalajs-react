History
=======

### 0.6.0 (unreleased)

This release brings scalajs-react in line with React 0.12.
**React version 0.12.0 or later is now required.**

Changes in React 0.12 can be read here:
*  https://github.com/facebook/react/releases/tag/v0.12.0
*  http://facebook.github.io/react/blog/2014/10/28/react-v0.12.html
*  http://facebook.github.io/react/docs/glossary.html

In addition to React API changes...
* Deprecated `ReactOutput` and `VDom` in favour of `ReactElement` or in rare cases, `ReactNode`. (*[glossary](http://facebook.github.io/react/docs/glossary.html)*)
* `.asJsArray: Seq[A] → JArray[A]` renamed to `toJsArray`
* `.toJsArray: Seq[A] → JArray[ReactElement]` is no longer needed.
* Renamed `ComponentSpec` to `ReactComponentSpec`. *(Internal. Extremely unlikely anyone using it directly.)*
* Changed signatures of `ReactS.callback` and brethren from `(c)(a)` to `(a,c)`.

Here are a few commands to ease migration.
```
find -name '*.scala' -exec perl -pi -e 's/(?<!\w)(vdom\.)?ReactOutput(?!\w)/ReactElement/g' {} +
find -name '*.scala' -exec perl -pi -e 's/(?<!\w)VDom(?!\w)/ReactElement/g' {} +
find -name '*.scala' -exec perl -pi -e 's/(?<!\w)asJsArray(?!\w)/toJsArray/g' {} + // careful...
find -name '*.scala' -exec perl -pi -e 's/(?<=[ .]render)Component//g' {} +
```

### 0.5.2 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.5.1...v0.5.2))

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

### 0.5.1 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.5.0...v0.5.1))

* Fixed Scalatags rejecting `VDom`.
* Added `ScalazReact.ReactS.setM`.
* Added `Listenable.install{IO,F}`, added `M[_]` to `Listenable.installS`.
* Added `LogLifecycle` which when applied to a component, logs during each lifecycle callback.

### 0.5.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.4.1...v0.5.0))

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

### 0.4.1 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.4.0...v0.4.1))

* Upgrade to scalatags 0.4.0.
* Component builder supports multiple callbacks.
* JS source maps point to Github.

### 0.4.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.2.0...v0.4.0))
* Major overhaul.
* Modules for Scalaz and testing.

### 0.2.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.1.0...v0.2.0))

### 0.1.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/55a19e7...v0.1.0))

