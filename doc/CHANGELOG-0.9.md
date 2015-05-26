# 0.9.0

##### Breaking
* On `ReactComponentB`, you now specify `domType` *before* lifecycle callbacks.
  This means that `getDOMNode()` will have the right type in lifecycle callback bodies.
* Relaxed `TopNode`, which is a greatest-upper-bound on component DOM node types, from `HTMLElement` to `Element` as React components can render SVG too.
* During `TagMod` composition, constituents are applied in the order they're composed in.
* Changed `CompStateAccess` shape and internals for improved type-inference and usage. (If you don't use the directly, you won't be affected.)
* Renamed `ComponentStateFocus` to `CompStateFocus`.

##### Non-Breaking
* During `TagMod` composition, avoid allocations composing `EmptyTag`.
* The v1 Router's `RoutingRules` args that specify what to render, are now lazy and reevaluate on request.

##### New
* Added `EventListener`. ([live demo](http://japgolly.github.io/scalajs-react/#examples/event-listener))
* `ReactComponentB` now has `.mixinJS` method; components can now mixin pure-JS React mixins.
  <br>Note: That doesn't mean the mixins will work as expected, however.
  There will be mixins that won't work correctly as they make assumptions that don't hold for Scala.
  If a mixin expects to inspect your props or state, forget about it; Scala-land owns that data.
* `ReactComponentB` now has a `.reRender` method for Scala mixins to customise a component's output.
* More Scalaz `IO` support:
  * `ReactComponentB` now supports `IO`-aware lifecycle callbacks.
    <br>Example: `.componentWillUpdateIO` instead of `.componentWillUpdate`.
  * `SetInterval` leanred `setIntervalIO`.

## Performance Management
A number of new tools and utilities have been introduced for you to manage the performance of your React app.

Detail with examples are here: [extra/PERF.md](https://github.com/japgolly/scalajs-react/blob/master/extra/PERF.md).

* Scala facade for `React.addons.Perf`. See http://facebook.github.io/react/docs/perf.html.
* `Reusability` for fast, easy & safe `shouldComponentUpdate` management.
* `ReusableFn` for stable callbacks that don't sabatage `shouldComponentUpdate` or go stale.
* `ReusableVal` for specifying a value and its explicit reusability. For special/individual cases.
* `ReusableVar` for safe R/W access to a variable, that works with `shouldComponentUpdate`.
  <br>(Basically, `ExternalVar` + `Reusability`.)
* `Px` for fast, dependant caching.

## Router v2

This release comes with a new and improved router.

The design of [the v1 Router](https://github.com/japgolly/scalajs-react/blob/master/extra/ROUTER.md)
made certain features very hard to accommodate:
[#96](https://github.com/japgolly/scalajs-react/issues/96),
[#103](https://github.com/japgolly/scalajs-react/issues/103),
[#107](https://github.com/japgolly/scalajs-react/issues/107),
[#100](https://github.com/japgolly/scalajs-react/issues/100),
[#94](https://github.com/japgolly/scalajs-react/issues/94),
[#69](https://github.com/japgolly/scalajs-react/issues/69).

In contrast, the v2 Router has a different design that:

* Uses a user-provided data representation of your pages to identify routes and their parameters.
* Similarly the Router can now indicate the current page with precision, faciliating dynamic menus and breadcrumbs even in the presence of complex, dynamic routes.
* Routes can be stateful and conditional.
* Routes can be nested and modularised.
* Routes can be manipulated in bulk.
* Has a better API such that usage previously recommended against, is now impossible. Noteworthy is that `Router` is now just a `ReactComponent`, and `RouterCtl` is the client API.

Detail with examples are here: [extra/ROUTER2.md](https://github.com/japgolly/scalajs-react/blob/master/extra/ROUTER2.md).
