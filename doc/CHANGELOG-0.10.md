# 0.10.0 (unreleased)

This release packs a lot of changes to improve the quality, safety and coding experience.
Many changes are breaking so don't miss the migration commands.
Some manual change in mostly unavoidable as they don't cover as much as usual this time, but they will save you a significant amount.

#### Summary
- [React 0.14](#react-014)
- [Unified the two ways of writing callbacks](#unified-the-two-ways-of-writing-callbacks)
- [`extra` module no longer depends on Scalaz and Monocle](#extra-module-no-longer-depends-on-scalaz-and-monocle)
- [Router2 is now the only router](#router2-is-now-the-only-router)
- [RenderBackend macro](#renderbackend-macro)
- [Better typed input to lifecycle callbacks](#better-typed-input-to-lifecycle-callbacks)
- [Component-scope access type-safety](#component-scope-access-type-safety)
- [Revised `ReactComponentB` method names](#revised-reactcomponentb-method-names)
- [Smaller stuff](#smaller-stuff)
- [Migration commands](#migration-commands)

---

### React 0.14

In this release we jump from React 0.12 to 0.14.

* New in React 0.13
  * Components' `modState` can now be called multiple times in the same render pass without clobbering changes.
    (A fatal issue of NPEs when accessing events in `modState` in React 0.13 has been fixed with React 0.14.)
  * The `ref` attribute now accepts a callback for you to store the ref yourself.
    <br>`^.input(^.ref[HTMLInputElement](myInput = _))`
  * Added `React.findDOMNode(component): Node`.
  * New HTML attributes: `scoped`, `high`, `low`, `optimum`, `min`, `max`, `unselectable`.
  * Removed `ReactTestUtils.isTextComponent`.
  * Removed methods deprecated in React 0.12.

* New in React 0.14
  * `React` singleton split into `React`, `ReactDOM`, `ReactDOMServer`. (Currently all in the `core` module for now; will split later.)
  * References to plain DOM elements (`div`,`input`,etc.) now return the nodes directly without a need to call `getDOMNode()`.
  * Add `FunctionalComponent`, a new type of component which is a thinly-wrapped, pure `P => ReactElement`. [(examples)](../test/src/test/scala/japgolly/scalajs/react/FunctionalComponentTest.scala)
  * React deprecated `α.getDOMNode()` in favour of `ReactDOM.findDOMNode(α)`, likely because `findDOMNode` won't make
    sense in React Native, etc. However in Scala we have the ability to *conditionally* add the `getDOMNode()` method
    onto components. This means we can keep the convenient `getDOMNode()` and in future mirror the conditions that
    React applies (eg. `getDOMNode()` works with the `react-dom` module only).
  * `React.initializeTouchEvents` is no longer necessary and has been removed completely. Touch events now work automatically.
  * New HTML attributes: `capture`, `challenge`, `inputMode`, `is`, `keyParams`, `keyType`, `minLength`, `summary`, `wrap`, `autoSave`, `results`, `security`, `onAbort`, `onCanPlay`, `onCanPlayThrough`, `onDurationChange`, `onEmptied`, `onEncrypted`, `onEnded`, `onError`, `onLoadedData`, `onLoadedMetadata`, `onLoadStart`, `onPause`, `onPlay`, `onPlaying`, `onProgress`, `onRateChange`, `onSeeked`, `onSeeking`, `onStalled`, `onSuspend`, `onTimeUpdate`, `onVolumeChange`, `onWaiting`.
  * New SVG attributes: `xlinkActuate`, `xlinkArcrole`, `xlinkHref`, `xlinkRole`, `xlinkShow`, `xlinkTitle`, `xlinkType`, `xmlBase`, `xmlLang`, `xmlSpace`.

---

### Unified the two ways of writing callbacks

**Problem:**<br>
A gap in the community and library was growing because there were two separate ways of writing callbacks.
An example of which, numerous users using the built-in `Router` experienced confusion and were forced to
discover Scalaz's (and Haskell's) `unsafePerformIO()` without understanding the reasons or benefit.
Another example, writing mixins or shared libraries, one would have to create duplicate methods with differing
type signatures in order to accomodate both styles.
More examples exist, but it should be clear that this please-everyone approach has failed.

(See [#145: Unify `-->` `==>` `~~>`, remove Scalaz `IO`, add `Callback`](https://github.com/japgolly/scalajs-react/issues/145).)

**Solution:**<br>
A new data type `Callback` has been added
(which is actually an alias to the real type `CallbackTo[A]` where `Callback = CallbackTo[Unit]`).
It replaces the existing two competing methods of `() => Unit` and Scalaz `IO[Unit]`.
A basic rule of thumb is if a function performs an effect (changing DOM, using network, changing React state),
it should be wrapped in a `Callback`.

* When creating HTML, `-->` and `==>` only accept `Callback`s.
* `setState()`, `modState()` etc now return `Callback`s.
* Useful methods now moved into core `-->?`, `==>?` which work with optional callbacks.
* Useful methods now moved into core `_setState()` and `_modState()`.
* Component lifecycle methods (like `componentWillMount`) now accept `Callback`.
* `Router` no longer uses Scalaz `IO`.
* Mixins in `extra` no longer have duplicate methods for Scalaz `IO`.

For Scalaz users:<br>
Either use `CallbackTo` instead of `IO`, or else change your `IO`s to `CallbackTo`s when you pass them to React.
* `~~>` is removed. Use `-->` and `==>` instead.
* `{,_}{set,mod}StateIO` methods all removed and in core without the `IO`.
* Isomorphism between `IO` and `CallbackTo` with convenience extension methods `.toCallback` and `.toIO`.
* `OpCallbackIO` has been removed in favour of plain `Callback`.
* New state-monad type-inference convenience: `FixCB[S] = FixT[CallbackTo, S]`

**Example migrations**<br>

Example #1
```scala
// Before
val Example = ReactComponentB[Unit]("Example")
  .initialState(0)
  .render { $ =>

    def clickHandler = $.modState(_ + 1)
    <.div(
      <.div("Button pressed ", $.state, " times."),
      <.button("CLICK ME", ^.onClick --> clickHandler))

  }.buildU

// After
// Surprise! No change needed.
// modState() now returns a Callback which is exactly what --> expects.
```

Example #2<br>
```scala
// Before
import org.scalajs.dom
def clickHandler: Unit = {
  dom.console.log("Updating state.")
  $.modState(_ + 1)
}

// Firstly, there are console helpers on Callback such that
Callback(dom.console.log("Updating state."))
// can be simplified to
Callback.log("Updating state.")

// Callbacks can compose in many different ways.
// The following examples all do the same thing in the same order.

// Method #1: >>
def clickHandler: Callback =
  Callback.log("Updating state.") >>
  $.modState(_ + 1)

// Method #2: <<
def clickHandler: Callback =
  $.modState(_ + 1) << Callback.log("Updating state.")

// Method #3: precedeWith
def clickHandler: Callback =
  $.modState(_ + 1).precedeWith {
    dom.console.log("Updating state.")
  }

// Method #4: runNow()
def clickHandler = Callback {
  dom.console.log("Updating state.")
  $.modState(_ + 1).runNow()
}
```

Example #3: Snippet from https://japgolly.github.io/scalajs-react/#examples/todo<br>
```scala
// Before
def handleSubmit(e: ReactEventI) = {
  e.preventDefault()
  $.modState(s => State(s.items :+ s.text, ""))
}

// React and DOM events gain methods that wrap .preventDefault() & .stopPropagation() in Callback
// .preventDefaultCB and .stopPropagationCB

// After
def handleSubmit(e: ReactEventI) =
  e.preventDefaultCB >>
  $.modState(s => State(s.items :+ s.text, ""))
```

---

### `extra` module no longer depends on Scalaz and Monocle

Thanks to the change above (the addition of `Callback`), `extra` now only depends on `core`.
The built-in Router now relies only on scalajs-react and Scala stdlib.

Scalaz and Monocle are still supported; use the `ext-scalaz` and `ext-monocle` modules.

Methods previously in `extra` that use Scalaz or Monocle (for example, `Reusability.byEqual`, `ReusableVar#setL`)
now require you to import `ScalazReact._` and/or `MonocleReact._`.
After doing so, the methods will appear to be available as if nothing has changed.

---

### Router2 is now the only router

`extra.router` has been removed and `extra.router2` has been renamed to take its place.

If for some reason, you want to use Router v1, don't want to migrate to v2 *and* want to keep up-to-date with scalajs-react,
please copy Router v1 into your own codebase.
Maintaining two routers in scalajs-react is not good.

---

### RenderBackend macro

`.renderBackend` is a fast way for the extremely common case of having a backend class with a render method.
It will locate the `render` method, determine what the arguments need (props/state/propsChildren) by examining the
types or the arg names when the types are ambiguous, and create the appropriate function at compile-time.
If can also automate the creation of the backend, see below.

Before:
```scala
type State = Vector[String]

class Backend($: BackendScope[Unit, State]) {
  def render = {
    val s = $.state
    <.div(
      <.div(s.length, " items found:"),
      <.ol(s.map(i => <.li(i))))
  }
}

val Example = ReactComponentB[Unit]("Example")
  .initialState(Vector("hello", "world"))
  .backend(new Backend(_))
  .render(_.backend.render)
  .buildU
```

After:
```scala
type State = Vector[String]

class Backend($: BackendScope[Unit, Vector[String]]) {
  def render(s: State) =   // ← Accept props, state and/or propsChildren as argument
    <.div(
      <.div(s.length, " items found:"),
      <.ol(s.map(i => <.li(i))))
}

val Example = ReactComponentB[Unit]("Example")
  .initialState(Vector("hello", "world"))
  .renderBackend[Backend]  // ← Use Backend class and backend.render
  .buildU
```

You can also create a backend yourself and still use `.renderBackend`:
```scala
val Example = ReactComponentB[Unit]("Example")
  .initialState(Vector("hello", "world"))
  .backend(new Backend(_)) // ← Fine! Do it yourself!
  .renderBackend           // ← Use backend.render
  .buildU
```

---

### Better typed input to lifecycle callbacks

Component lifecycle callbacks that receive more input than just the component itself, are now provided as case classes
with the additional inputs named.

So instead of `(Component[P,S], P, S) => Callback` and it being unclear whether
the additional `P` is next-props or prev-props, you now get an object that will have either a `.nextProps` or
`.prevProps` depending on which it is, according to the React docs.

This affects the following lifecycle callbacks:
  * `ComponentWillUpdate`
  * `ComponentDidUpdate`
  * `ShouldComponentUpdate`
  * `ComponentWillReceiveProps`


---

### Component-scope access type-safety

This closes an important source of bugs in backends with props/state being used in such a way that they go stale at
runtime. Very easy to make but hard to spot.

Component/Backend scopes' access is now as follows:

| Method | `BackendScope` | During Callback | External Access |
| --- | --- | --- | --- |
| `.props` | `CallbackTo[P]` | `P` | `P` |
| `.state` | `CallbackTo[S]` | `S` | `S` |
| `.setState` | `Callback` | `Callback` | `Unit` |
| `.modState` | `Callback` | `Callback` | `Unit` |
| `.forceUpdate` | `Callback` | `Callback` | `Unit` |

As part of this change, if you were using `CompStateFocus` before, you now choose an appropriate interface from
`CompState`. Eg. instead of `CompStateFocus[S]` you might use `CompState.ReadCallbackOps[S]`.

---

### Revised `ReactComponentB` method names

Firstly, there are no more overloaded `render` methods, nor are they in different locations.
They're now all defined in one place, and have been renamed to represent their type signatures.

Who remembers what mish-mash existed before, in this new world you ask for the types you want by adding suffixes to
the render function name. Conversely, it's now always obvious what's happening by looking at the function name.

**TODO: Add table or examples**

Similarly the `initialState` methods have been revised both
1. To be consistent with the pattern used in `render` methods
2. To optionally accept `Callback`s.

Unlike the `render` methods, this migration can be automated (see below).

---

### Smaller stuff

* Add extension helper methods to DOM nodes and elements:
  * `.domCast[N]` - Same as `asInstanceOf[N]` except with the guarantee that it will only cast DOM to DOM.
  * `.domAsHtml` - Casts to a `HTMLElement`.
  * `.domToHtml` - Checks if the DOM is a `HTMLElement` before casting. Returns an `Option[HTMLElement]`.

* Add `CallbackOption` which is a callback & option monad stack.

  This is more useful than you'd think, check out the online [`CallbackOption` example](https://japgolly.github.io/scalajs-react/#examples/callback-option).

* Changes to `Reusability`:
  * New instance methods:
    * `||`
    * `&&`
  * New object methods:
    * `always`
    * `never`
    * `byRefOr_==`.
    * `byRefOrEqual` (Scalaz module only).
    * `byIterator`
    * `indexedSeq`
    * `double`
    * `float`
  * New implicits for:
    * `java.util.Date`
    * `java.util.UUID`
    * `scalajs.js.Date`
    * `Set[A]`
    * `List[A]`
    * `Vector[A]`
  * Renamed `Reusability.reusableXxx` methods to `Reusability.xxx` for consistency.

* Changes to `Px`
  * `Px` initialisation is now lazy.
    This yields a performance improvement using `Px` `val`s in a backend, and in staple `map` & `flatMap`.
  * Derivative `Px` instances (i.e. those created by `map` or `flatMap`) have a new method: `.reuse` which applies
    filtering to derived results.
  * Added `Px#extract`.
  * Added `Px.cbM` and `Px.cbA` for callbacks.
  * Added `Px.bs` for use with `BackendScope`.

* `SetInterval` was renamed `TimerSupport` and learned:
  * `setIntervalMs`
  * `setTimeout`
  * `setGuaranteedInterval` which
    provides for interval-like behavior but guarantees a minimum interval between the end of the
    previous callback and the start of the next callback. Regular `setInterval` usage invokes the callback at every
    interval using wall clock time, irrespective of how long the callback takes to complete.

* Added `MockRouterCtl` to the `test` module.

* Renamed:
  * `react.vdom.Optional` to `react.OptionLike`.
  * `ComponentScope` to `CompScope`.
  * Refactored the `ComponentScope` fragment traits to have meaningful names and be out of the way.
    This shouldn't affect you but if you directly declared a `ComponentScopeM` somewhere, you'll need to change it
    to `CompScope.DuringCallbackM` if it's being access during a component's lifecycle callback, or just plain
    `ReactComponentM` otherwise.

* Deleted `test.Sel`. Use [Sizzle](http://sizzlejs.com/) or [jQuery](https://jquery.com/).
  For reference, the scalajs-react unit tests were changed to use `Sizzle`. Search the repo for `sizzle` and see how
  little code is required and how is it is.

* `A ~=> B`, given an `A`, can now produce a `ReusableFnA[A, B]` which is effectively a reusable version of
  `(A, () => B)`.

* Case class macros now enforce that inputs are indeed case classes.

* Small improvements to `ReusabilityOverlay`.

* Upgrade scala-js-dom 0.8.{1 ⇒ 2}.

* Added `ReactTagOf` to provide more specific types for scalatags. `ReactTag` is the kept the same as before for compatibility or if you don't need it.  
```scala
val specific: ReactTagOf[html.Div] = div()
val general: ReactTag = specific
```

---

### Migration commands

```sh
# React 0.14
find . -name '*.scala' -exec perl -pi -e 's/(?<=React)([ .]+(?:render|unmountComponentAtNode|findDOMNode))(?!\w)/DOM$1/g' {} +

# extra.{router2 ⇒ router}
find . -name '*.scala' -type f -exec perl -pi -e 's/(?<=extra\.router)2//g' {} +

# Unfortunately the migration to Callback is mostly manual.
# Here are some commands I used to help but they'll only get you halfway there.
find . -name '*.scala' -type f -exec perl -pi -e 's/(?<=forceUpdate)\(\)//g' {} +
find . -name '*.scala' -type f -exec perl -pi -e 's/(?<=tryFocus)\(\)//g' {} +
find . -name '*.scala' -type f -exec perl -pi -e 's/(?<=(set|mod)State)IO//g' {} +
find . -name '*.scala' -type f -exec perl -pi -e 's/FixT\[IO *, */FixCB[/' {} +
find . -name '*.scala' -type f -exec perl -pi -e 's/ReactST\[IO *,/ReactST[CallbackTo,/' {} +

# initialState (do in order)
find . -name '*.scala' -type f -exec perl -pi -e 's/getInitialState|initialStateP/initialState_P/g' {} +
find . -name '*.scala' -type f -exec perl -pi -e 's/initialStateC/getInitialState/g' {} +

# TimerSupport
find . -name '*.scala' -exec perl -pi -e 's/(?<!\w)SetInterval(?!\w)/TimerSupport/g' {} +

# CompScope
find . -name '*.scala' -type f -exec perl -pi -e 's/ComponentScope/CompScope/g' {} +
```
