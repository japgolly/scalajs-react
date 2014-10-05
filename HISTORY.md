History
=======

### 0.5.0

* Just as umounted Scala and JS components are denoted by `ReactComponentU` and `ReactComponentU_` respectively,
  mounted Scala and JS components are now denoted by `ReactComponentM` and `ReactComponentM_`.
* `ReactComponentB` accepts an optional DOM node type, which is propagated to `ReactComponentU` and `ReactComponentM`.
* Type changes.
  * `ReactComponentB` innards reworked. Use `ReactComponentB[P,S,B]` in place of `ReactComponentB[P]#B2[S]#B3[B]#B4[C]`.
  * `CompCtor`   → `ReactComponentC`
  * `CompCtorP`  → `ReactComponentC.ReqProps`
  * `CompCtorOP` → `ReactComponentC.DefaultProps`
  * `CompCtorNP` → `ReactComponentC.ConstProps`
  * `ReactComponentM` → `ReactComponentM_`
* Renamed and deprecated the old:
  * `ReactComponentB.create` → `build`
  * `ReactComponentB.createU` → `buildU`
  * `ReactComponentB.propsAlways` → `propsConst`
  * `Nop` → `EmptyTag`
* Added `Simulation` for composition and abstraction of `ReactTestUtils.Simulate` procedures.

### 0.4.1 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.4.0...v0.4.1))

* Upgrade to scalatags 0.4.0.
* Component builder supports multiple callbacks.
* JS source maps point to Github.

### 0.4.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.2.0...v0.4.0))
* Major overhaul.
* Modules for Scalaz and testing.

### 0.2.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.1.0...v0.2.0))

### 0.1.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/55a19e7...v0.1.0))

