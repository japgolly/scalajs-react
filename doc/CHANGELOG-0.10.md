# 0.10.0 (unreleased)

* `ReactComponentB` learned `.renderBackend`.

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

* Replaced Router v1 with v2.

  `extra.router` has been removed; `extra.router2` has been renamed to take its place.

  If for some reason, you want to use Router v1, don't want to migrate to v2 *and* want to keep up-to-date with scalajs-react,
  please copy Router v1 into your own codebase.
  Maintaining two Routers in scalajs-react is not good.

* [#145](https://github.com/japgolly/scalajs-react/issues/145): Unify `-->` `==>` `~~>`, remove Scalaz `IO`, add `Callback`.

  **Problem:**<br>
  A gap in the community and library was growing because there were two separate ways of writing callbacks.
  An example of which, numerous users using the built-in `Router` experienced confusion and were forced to
  discover Scalaz's (and Haskell's) `unsafePerformIO()` without understanding the reasons or benefit.
  Another example, writing mixins or shared libraries, one would have to create duplicate methods with differing
  type signatures in order to accomodate both styles.
  More examples exist, but it should be clear that this please-everyone approach has failed.

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

* Component/Backend scopes' access is now as follows:

  | Method | `BackendScope` | During Callback | External Access |
  | --- | --- | --- | --- |
  | `.props` | `CallbackTo[P]` | `P` | `P` |
  | `.state` | `CallbackTo[S]` | `S` | `S` |
  | `.setState` | `Callback` | `Callback` | `Unit` |
  | `.modState` | `Callback` | `Callback` | `Unit` |
  | `.forceUpdate` | `Callback` | `Callback` | `Unit` |
  
  This closes an important source of bugs in backends with props/state being used in such a way that they go stale at
  runtime. Very easy to make but hard to spot.
  
  As part of this change, if you were using `CompStateFocus` before, it is now `StateAccessCB` or `StateAccessDirect`.

* The `extra` module no longer depends on Scalaz or Monocle.

  `extra` only depends on `core` now.
  The built-in Router now relies only on scalajs-react (`Callback` was very important for this), and Scala stdlib.
  
  Methods previously in `extra` that use Scalaz or Monocle (for example, `Reusability.byEqual`, `ReusableVar#setL`)
  now require you to import `ScalazReact._` and/or `MonocleReact._`.
  After doing so, the methods will appear to be available as if nothing has changed.

* Revise method names in `ReactComponentB`

  Firstly, there are no more overloaded `render` methods, nor are they in different locations.
  They're now all defined in one place, and have been renamed to represent their type signatures.

  Who remembers what mish-mash existed before, in this new world you ask for the types you want by adding suffixes to
  the render function name. Conversely, it's now always obvious what's happening by looking at the function name.

  **TODO: Add table or examples**

  Similarly the `initialState` methods have been revised both
  1. To be consistent with the pattern used in `render` methods
  2. To optionally accept `Callback`s.

  Unlike the `render` methods, this migration can be automated (see below).

* Smaller stuff:

  * `A ~=> B`, given an `A`, can now produce a `ReusableFnA[A, B]` which is effectively a reusable version of
    `(A, () => B)`.

  * Refactored the `ComponentScope` fragment traits to have meaningful names and be out of the way.
    This shouldn't affect you but if you directly declared a `ComponentScopeM` somewhere, you'll need to change it
    to `ComponentScope.DuringCallbackM` if it's being access during a component's lifecycle callback, or just plain
    `ReactComponentM` otherwise.

  * Small improvements to `ReusabilityOverlay`.

  * Case class macros now enforce that inputs are indeed case classes.

  * Add `SetInterval.setIntervalMs`.

  * New `Reusability` instance methods:
    * `||`
    * `&&`
  * New `Reusability` object methods:
    * `always`
    * `never`
    * `byRefOr_==`.
    * `byRefOrEqual` (Scalaz module only).
    * `byIterator`
    * `indexedSeq`
  * New `Reusability` implicits for:
    * `Set[A]`
    * `List[A]`
    * `Vector[A]`
  * Renamed `Reusability.reusableXxx` methods to just `Reusability.xxx` for consistency.

  * `Px` initialisation is now lazy.
    This yields a performance improvement using `Px` `val`s in a backend, and in staple `map` & `flatMap`.
  * Derivative `Px` instances (i.e. those created by `map` or `flatMap`) have a new method: `.reuse` which applies
    filtering to derived results.
  * Added `Px#extract`.
  * Added `Px.cbM` and `Px.cbA` for callbacks.
  * Added `Px.bs` for use with `BackendScope`.
    
  * `react.vdom.Optional` is now `react.OptionLike`.

<br>
Migration commands:
```sh
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
```
