# 0.9.0

* In `ReactComponentB`, `domType` is now specified before lifecycle callbacks
  so that a DOM cast isn't required within the callback bodies.
* Performance Management
  * Detail with examples here: [PERF.md](https://github.com/japgolly/scalajs-react/blob/master/extra/PERF.md)
  * New API: `Addons.Perf` over http://facebook.github.io/react/docs/perf.html.
  * `Reusable` for fast & safe `shouldComponentUpdate` impl.
  * `ReusableFn` for callbacks that can be passed around and work with `shouldComponentUpdate`.
  * `ReusableVal` for specifying a value and its explicit reusability.
  * `ReusableVar` as a version of `ExternalVar` that works with `shouldComponentUpdate`.
  * `Px` for caching.
* Convenience  method `Reusable.component` turns a `P => ReactElement` into a
  stateless component with `shouldComponentUpdate` configured.
* `RoutingRules` args that specify what to render are now lazy and reevaluate on request.
* In `TagMod` composition, constituents are applied in the order they're composed in.
* In `TagMod` composition, avoid allocations composing `EmptyTag`.
* Renamed `ComponentStateFocus` to `CompStateFocus`.
* Changed `CompStateAccess` shape and internals for improved type-inference and usage.
* `TopNode` constraint relaxed from `HTMLElement` to `Element` as React components can render SVG too.
* Added safe `ReactComponentB` methods for side-effecting life-cycle methods.
