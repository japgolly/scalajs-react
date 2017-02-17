scalajs-react [neo]
=============

[![Build Status](https://travis-ci.org/japgolly/scalajs-react.svg?branch=master)](https://travis-ci.org/japgolly/scalajs-react)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/japgolly/scalajs-react?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This branch is where a [redesign](https://github.com/japgolly/scalajs-react/issues/259) is currently taking place.
At the moment, it's all in a new module called [`neo`](neo/src).

The v0.x.y series started as an experiment and grew organically from there.
As such, it has accrued a number of annoyances and obstacles to desired improvements,
that can now only be solved by a redesign.
This begins a v1.x.y series and will begin with **v1.0.0**.

Contributions welcome.

# Done

- Component interfaces that allow any kind (JS, Scala, Clojure) of React component to be used generically.

  This is really awesome because it allows a component to declare access to a subset of any component's state as long as it is a certain type, then satisfy it using Monocle to zoom in and/or transform along the way, even if its a JS component.

  - Allow (any kind of) constructor transforms.
  - Allow (any kind of) props transforms.
  - Allow (any kind of) state transforms.

- Better Constructors
  - Agnostic to underlying implementation of component (JS, Scala, etc.)
  - Don't ask for non-existent or singleton props.
  - Depending on component, either don't ask for children, or ensure children are specified.
  - Allows possibility of even more children type-safety such as requiring exactly one child.
  - Input can be transformed.
  - Output can be transformed.
  - Additional raw React props fields can be configured.

- More transparency. No more hidden magic.
  - A separate `.raw` package that contains the React JS facade (without any Scala niceness).
  - All components expose their raw JS types.
  - All Scala components expose their underlying JS components.
  - It should be trivial to reuse `scalajs-react` components in other React libraries, and vice-versa.

- `JsComponent` - Import React components written in pure JS.
  ([test JS](neo/src/test/resources/component-es3.js) & [test Scala](neo/src/test/scala/japgolly/scalajs/react/JsComponentTest.scala))

  Importing a JS component is now a one-liner.
  ```scala
  val Component = JsComponent[JsProps, Children.None, JsState]("ReactXYZ")
  ```

- Type-safety for JS components that expose ad-hoc methods once mounted.
  You can now specify the JS facade.

- `JsFnComponent` - Import React functional components written in pure JS.
  ([test JS](neo/src/test/resources/component-fn.js) & [test Scala](neo/src/test/scala/japgolly/scalajs/react/JsFnComponentTest.scala))

- `ScalaComponent` - Create React components in Scala.

- `ScalaFnComponent` - Create React functional components in Scala.

- Safe `PropsChildren` type and usage.

- Consistency wrt wrapping typed effects. Eg. `BackendScope.getDOMNode` should be `Callback`/direct just like everything else.

- Virtual DOM major revision.
  - Rewrite and simplify types. Now easier to work with internally. This no longer bears any resemblence to Scalatags and certainly can no longer be considered a fork. Scalatags was tremedously helpful in this journey so if you have a chance, give @lihaoyi a big thanks for his work.
  - Improved efficiency for vdom representation and creation.
  - Add type-safety between attributes/styles and values. Eg `^.disabled := 7` no longer compiles.
  - Event attributes now know which types of events they will generate. Eg `^.onMouseDown` knows to expect a mouse event and won't compile if you pass it a drag event handler.
  - React node array handling is safer, more efficient and has its own type with a nicer interface.
  - No more automatic expansion of `Seq`s. Either use `seq: _*` yourself or turn it into a `ReactArray`.
  - Optional vdom supported when enclosed in `Option` or `js.UndefOr`.
  - All vdom now has `.when(condition)` and `.unless(condition)` when will omit it unless a given condition is met. This replaces the `cond ?= (vdom)` syntax.
  - All vdom composes the same way, call `.apply` on what you have and specify more. This was usually the case but there were a few corner cases that had differences.
  - Easier and clearer access to SVG VDOM.
  - Manually-specified style objects now compose with other style attributes.

  ```
  ReactArray(...)
  Seq(...).toReactArray
  Array(...).toReactArray

  Attr :=? Option(Value)
  Option(Tag | Attr | Component | Value | TagMod)
  (Tag | Attr | Component | Value | TagMod).when(Boolean)
  (Tag | Attr | Component | Value | TagMod).unless(Boolean)
  ```

- Component (and constituent) mapping.
  - Can map props & state (at top-level, and in Unmounted & Mounted too).
  - Can map the constructor type.
  - Can map next stage (i.e. Component→Unmounted and Unmounted→Mounted).
  - Can change effect type in Mounted.

- Refs.
  - Remove String-based refs. React.JS has deprecated these and will remove them.
  - Type-safe refs to HTML/SVG tags that preserve the DOM type.
  - Type-safe refs to Scala components.
  - Type-safe refs to JS components.
  - Prevent refs to functional components.

# Pending

- Easy way to change MountedCB back into Mounted. Same for State/Prop traits if they get added back.

- Revise & integrate the `extra` module.
- Revise & integrate the `test` module.
- Revise & integrate the Scalaz module.
- Revise & integrate the Monocle module.
- Update the `gh-pages` module.
- Update doc.

# Maybe

- Static and dynamic props (for Scala components).
  Probably not as a normal Scala function is all that's really needed.
  There's no big need to avoid creating a new component per staic data.

- Maybe a new means of declaring Scala mixins.

- Component DOM/`getDOMNode` types. Currently none.
  - Having `N <: TopNode` all over the place was annoying.
  - Could add manually like before.
  - Would be nice to auto-detected based on `.render` result. Introduces circular dependency wrt `BackendScope` tho.

- Anything ES6-related should be easy to add now. Please contribute if interested.
  - Facades over ES6-based JS classes. (I tried briefly but didn't get the JS working.)
  - Scala-based ES6-based classes. Because it's important to some people. (Apparently its faster but I'm yet to see any benchmarks or other evidence supporting this.)
  - Once the above works, it would be good to be able to choose a backend type for `ReactComponentB`.

- Add a `Cats` module too? Contribution welcome.

# Release note / migration reminders

Refactored:
  * ExternalVar/ReusableVar -> StateSnapshot
  * ReusableVal/ReusableVal2 -> Reusable
  * ReusableFn(x).{set,mod}State -> ReusableFn.state(x).{set,mod}

* Update in ScalaDoc:
  * ReactComponentB

* Moved into extra:
  * domCast
  * domAsHtml
  * domToHtml

* Removed completely:
  * tryFocus
  * tryTo
  * {set,mod}StateCB
  * CallbackB
