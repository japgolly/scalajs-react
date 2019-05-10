Performance Management
======================

Included are several utilities for you to manage the performance of your React app.
The strategy is simple: avoid work where possible.

These utilities help you avoid work in two ways.

1. By making components' `shouldComponentUpdate` fns both easy to create, and accurate (safe). If it compiles, the logic in `shouldComponentUpdate` will be what you expect.
2. By allowing you to cache your own arbitrary data, and build on it in a way such that derivative data is also cached efficiently.

`Reusability`/`Reusable` is part of `core` (as of v1.4.0) where as
the other utilities are part of the `extra` module.

```scala
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "1.4.2"
```

### Contents

- [`Reusability`](#reusability)
  - [Usage](#usage)
  - [Example](#example)
  - [Monitoring](#monitoring)
- [`Reusable`](#reusable)
  - [Usage](#usage-1)
  - [Mapping without affecting reusability](#mapping-without-affecting-reusability)
  - [Examples](#examples)
- [Reusable functions](#reusable-functions)
  - [Usage](#usage-2)
  - [Example](#example-2)
  - [Warning](#warning)
  - [Tricks](#tricks)
- [`StateSnapshot` with `Reusability`](#statesnapshot-with-reusability)
- [`Px`](#px)
  - [Initial instances](#initial-instances)
  - [Derivative instances](#derivative-instances)
  - [`Px` doesn't have `Reusability`](#px-doesnt-have-reusability)


`Reusability`
=============

`Reusability` is a typeclass that tests whether one instance can be used in place of another.
It's used to compare properties and state of a component to avoid unnecessary updates.

An implicit instance of `Reusability[A]` means that
reusability can be calculated for the entire type of `A`, all of its values.

Imagine a class with 8 fields - typical equality like `==` would compare all 8 fields (and if
you're not using `scalaz.Equal` you've no way of knowing whether all those 8 fields have correct
`equals` methods defined).
When deciding whether a component needs updating, full equality comparison can be overkill (and slow) -
in many cases it is sufficient to check only the ID field, the update-date, or the revision number.
`Reusability` is designed for you to do just that.

#### Usage
When building your component, pass in `Reusability.shouldComponentUpdate` to `.configure` on the component builder.

It will not compile until it knows how to compare the reusability of your props and state.
Out-of-the-box, it knows how to compare Scala primitives, `String`s, `Option`, `Either`, Scala tuples, `js.UndefOr`,
Scala and JS `Date`s, `UUID`s, `Set`s, `List`s, `Vector`s,
and Scalaz classes `\/` and `\&/`. For all other types, you'll need to teach it how. Use one of the following methods:

* `Reusability((A, B) => Boolean)` to hand-write custom logic.
* `Reusability.by_==` uses universal equality (ie. `a1 == a2`).
* `Reusability.byRef` uses reference equality (ie. `a1 eq a2`).
* `Reusability.byRefOr_==` uses reference equality and if different, tries universal equality.
* `Reusability.derive` for ADTs of your own.
* `Reusability.deriveDebug` as above, but shows you the code that the macro generates.
* `Reusability.caseClassExcept` for case classes of your own where you want to exclude some fields.
* `Reusability.caseClassExceptDebug` as above, but shows you the code that the macro generates.
* `Reusability.by(A => B)` to use a subset (`B`) of the subject data (`A`).
* `Reusability.byIterator` uses an `Iterable`'s iterator to check each element in order.
* `Reusability.indexedSeq` uses `.length` and `.apply(index)` to check each element in order.
* `Reusability.{double,float}` exist and require a tolerance to be specified.
* `Reusability.{always,never,const(bool)}` for a hard-coded reusability decision.

If you're using the Scalaz module, you also gain:
* `Reusability.byEqual` uses a Scalaz `Equal` typeclass.
* `Reusability.byRefOrEqual` uses reference equality and if different, tries using a Scalaz `Equal` typeclass.


#### Example
The following component will only re-render when one of the following change:
* `props.name`
* `props.age`
* `props.pic.id`

```scala
case class Picture(id: Long, url: String, title: String)
case class Props(name: String, age: Option[Int], pic: Picture)

implicit val picReuse   = Reusability.by((_: Picture).id)  // ← only check id
implicit val propsReuse = Reusability.derive[Props]        // ← check all fields

val component = ScalaComponent.builder[Props]("Demo")
  .render_P(p =>
    <.div(
      <.p("Name: ", p.name),
      <.p("Age: ", p.age.fold("Unknown")(_.toString)),
      <.img(^.src := p.pic.url, ^.title := p.pic.title))
  )
  .configure(Reusability.shouldComponentUpdate)                 // ← hook into lifecycle
  .build
```

Alternatively, `picReuse` could be written using `caseClassExcept` as follows.

```scala
// Not natural in this case but demonstrates how to use caseClassExcept
implicit val picReuse = Reusability.caseClassExcept[Picture]('url, 'title)
```

You can peek into reusability calculation by wrapping it with a logger:

```scala
implicit val loggedPicReuse =
    Reusability.caseClassExcept[Picture]('url, 'title).logNonReusable
```

#### Monitoring

There exist two mixins, out-of-the-box, to help you monitor reusability. Use them instead of `shouldComponentUpdate`.

1. `shouldComponentUpdateWithOverlay` - Adds an overlay beside each mounted instance of the component, showing how many updates were prevented and how many were rendered. You can hover over it for some detail, and click it to print more to the JS console. [Live demo](https://japgolly.github.io/scalajs-react/#examples/reusability).
2. `shouldComponentUpdateAndLog` - Logs each callback evaluation to the console.

Usage:
```scala
// No monitoring
.configure(Reusability.shouldComponentUpdate)

// Display stats on screen, clickable for detail
.configure(Reusability.shouldComponentUpdateWithOverlay)

// Log to console
.configure(Reusability.shouldComponentUpdateAndLog("MyComponent"))
```

You can also call `.logNonReusable` on any reusability instance to get a new reusability which emits a warning
about non-reusability to aid quick debugging.


`Reusable`
==========

There are a cases when you cannot universally define reusability for all inhabitants of a type,
but you can for values that you produce. Enter: `Reusable[A]`

`Reusable[A]` is a single value of `A` with reusability.
It promises that whoever provides the value will also specify the value's reusability.
Where as `Reusability[A]` is for ∀a∈A, and `Reusable[A]` is for ∃a∈A.

#### Usage

To create a `Reusable` value, use one of the following methods:

* `Reusable(A)((A, A) => Boolean)` to hand-write custom logic.
* `Reusable.implicitly` when there is an implicit `Reusability` instance available.
* `Reusable.explicitly` when to explicitly provide a `Reusability` instance.
* `Reusable.by_==` uses universal equality (ie. `a1 == a2`).
* `Reusable.byRef` uses reference equality (ie. `a1 eq a2`).
* `Reusable.byRefOr_==` uses reference equality and if different, tries universal equality.
* `Reusable.callback{,Option}ByRef` uses reference equality over `Callback` and `CallbackOption`
* `Reusable.byRefIso` - Compare by reference through an isomorphism
* `Reusable.{always,never,const(bool)}` for a hard-coded reusability decision.

#### Mapping without affecting reusability

A common use case is to have a non-reusable value deterministically derived from a reusable seed value.

`Reusable` instances have a `.map` function that lazily maps the value without affecting its reusability.

Example below.

#### Examples

```scala
// Explicit reusability
val i: Reusable[Int] =
  Reusable(readSensor)((a, b) => Math.abs(a - b) < 100)

// Variable that's reusable each time it's used without an update
var statusVdom: ReusableVal[VdomElement] =
  Reusable.byRef(<.h1("Initialising..."))
// Example update:
def enterReadyState: Unit =
  statusVdom = Reusable.byRef(<.h1("Ready."))

// VDOM reusable based on seed Int
def renderCalculation(n: Int): Reusable[VdomElement] =
  Reusable.implicitly(n).map { n =>
    val result = expensiveCalculation(n)
    <.div("n=$n, result=$result")
  }
```


Reusable functions
==================

In React, components typically pass callbacks to their children via component properties.
Due to the ease of function creation in Scala it is often the case that functions are created inline and thus
provide no means of determining whether a component can safely skip its update.

The solution here is to use `Reusable[A => B]` (shorthand: `A ~=> B`).
It is a function that has been created in such a way that allows it to be both reused, and curried in a way that allows reuse.

#### Usage

1. Just wrap `Reusable.fn` around your function.
2. Store the `Reusable.fn` as a `val` somewhere outside of your `render` function, usually in the body of your backend class.
3. Replace the callback (say `A => B`) in components' props, to take a `A ~=> B`.
4. Treat the `A ~=> B` as you would a normal function.

For three or more arguments the result of `Reusable.fn(…)` is curried (or Schönfinkel'ed!), and each curried argument must have `Reusability`.
Eg. `Reusable.fn((A, B) => C)` returns a `A ~=> (B ~=> C)`.

#### Example

In this example `personEditor` will only rerender if `props.name` changes, or the curried `PersonId` in its `props.update` function changes (which it won't - observable from the code).

```scala
type State = Map[PersonId, PersonData]
type PersonId = Long
type PersonData = String

val topComponent = ScalaComponent.builder[State]("Demo")
  .initialStateFromProps(identity)
  .renderBackend[Backend]
  .build

class Backend(bs: BackendScope[_, State]) {

  val updateUser = Reusable.fn((id: PersonId, data: PersonData) =>         // ← Create a 2-arg fn
    bs.modState(_.updated(id, data)))

  def render(state: State) =
    <.div(
      state.toVdomArray { case (id, name) =>
        personEditor.withKey(id.toString)(PersonEditorProps(name, updateUser(id))) // ← Apply 1 arg
      }
    )
}

case class PersonEditorProps(name: String, update: String ~=> Callback)   // ← Notice the ~=>

implicit val propsReuse = Reusability.derive[PersonEditorProps]

val personEditor = ScalaComponent.builder[PersonEditorProps]("PersonEditor")
  .render_P(p =>
    <.input.text(
      ^.onChange ==> ((e: ReactEventFromInput) => p.update(e.target.value)),
      ^.value := p.name))                                                // ← Use as normal
  .configure(Reusability.shouldComponentUpdate)                          // ← shouldComponentUpdate like magic
  .build
```

#### WARNING!

**DO NOT** feed the `Reusable.fn(...)` constructor a function directly *derived* from a component's props or state.
Access to props/state on the right-hand side of the function args is ok but if the function itself is a result of the
props/state, the function will forever be based on data that can go stale.

Example:
```scala
@Lenses case class Person(name: String, age: Int)
case class Props(person: StateSnapshot[Person], other: Other)

// THIS IS BAD!!
Reusable.fn($.props.runNow().person setStateL Props.name)

// It is equivalent to:
val g: String => Callback  = $.props.runNow().person setStateL Person.name // ← $.props is evaluated once here
val f: String ~=> Callback = Reusable.fn(g)                                // ← …and never again.
```

Alternatives:

Ensure the scope is only used on the right-hand side of the function:

```scala
Reusable.fn(str => $.props.flatMap(_.person.setStateL(Person.name)(str)))
```


#### Tricks

To cater for some common use cases, there are few convenience methods that are useful to know.
For these examples imagine `$` to be your component's scope instance, eg. `BackendScope[P, S]`, `StateAccessPure[S]` or similar.

1. `Reusable.fn.state($).{set,mod}`.

    You'll find that if you try `Reusable.fn($.xxxState)` Scala will fail to infer the correct types.
    Use `Reusable.fn.state($).xxx` instead to get the types that you expect.

    Example: instead of `Reusable.fn($.setState)` use `Reusable.fn.state($).set` and you will correctly get a `S ~=> Callback`.

3. `Reusable.fn.state($ zoomStateL lens)`

  Lenses provide an abstraction over read-and-write field access.
  Using Monocle, you can annotate your case classes with `@Lenses` to gain automatic lenses.
  `$ zoomStateL lens` will then narrow the scope of its state to the field targeted by the given lens.
  This can then be used with `Reusable.fn.state` as follows:

  ```scala
  @Lenses
  case class Person(name: String, age: Int)

  class Backend($: BackendScope[_, Person]) {

    val nameSetter: String ~=> Callback =
      Reusable.fn.state($ zoomStateL Person.name).set
  ```


`StateSnapshot` with `Reusability`
==================================

[`StateSnapshot`](EXTRA.md#statesnapshot) is supports reusability.
Begin with `StateSnapshot.withReuse`.

* `StateSnapshot.withReuse(s)(reusable setStateFn)` - Provide a current value, and update function manually.
* `StateSnapshot.withReuse.prepare(setStateFn)` - Provides a reusable `S => StateSnapshot[S]` with a stable update function. This should be stored in a component backend, as a `val`, and reused with different values of `S`.
* `StateSnapshot.withReuse.prepareVia($)` - As above but gets the update function from `$` which is usually a `BackendScope`.
* `StateSnapshot.withReuse.zoom(…)` - Zooms into a subset of the total state. For example, you could create a `StateSnapshot[Age]` from `Person`.
  * `StateSnapshot.withReuse.zoom(…).prepare(setStateFn)`
  * `StateSnapshot.withReuse.zoom(…).prepareVia($)`


#### Example
```scala
@Lenses case class State(name: String, desc: String)

class Backend(bs: BackendScope[State, State]) {
  val ssName = StateSnapshot.withReuse.zoomL(State.name).prepareVia(bs)
  val ssDesc = StateSnapshot.withReuse.zoomL(State.desc).prepareVia(bs)

  def render(s: State) =
    <.div(
      stringEditor(ssName(s)),
      stringEditor(ssDesc(s)))
}

val topComponent = ScalaComponent.builder[State]("Demo")
  .initialState_P(identity)
  .renderBackend[Backend]
  .build

lazy val stringEditor = ScalaComponent.builder[StateSnapshot[String]]("StringEditor")
  .render_P(p =>
    <.input.text(
      ^.value := p.value,
      ^.onChange ==> ((e: ReactEventFromInput) => p.setState(e.target.value))))
  .configure(Reusability.shouldComponentUpdate)
  .build
```


`Px`
====

`Px` is a mechanism for caching data with dependencies.
It's basically a performance-focused, lightweight implementation of pull-based
[FRP](http://en.wikipedia.org/wiki/Functional_reactive_programming);
pull-based meaning that in the chain A→B→C, an update to A doesn't affect C until the value of C is requested.
Values are only compared when they are set or modified. When data is retrieved, only the revision number (an integer) is compared to determine if an update is required.

**NOTE:** `Px` does not have `Reusability`. Details below.

*What does Px mean? I don't know, I just needed a name and I liked the way @lihaoyi's Rx type name looked in code.
You can consider this "Performance eXtension". If this were Java it'd be named
`AutoRefreshOnRequestDependentCachedVariable`.*

#### Initial instances

![DSL](https://rawgit.com/japgolly/scalajs-react/master/doc/px.gv.svg)

`Px` comes in two flavours: those with reusable values, and those without.
If its values are reusable then when its underlying value `A` changes, it will compare the new `A` value to the previous `A` (using `Reusability[A]`) and discard the change if it can.
If its values are reusable, all changes to the underlying value (including duplicates) are accepted.

Create a non-derivative `Px` using one of these:

1. **Manual Update** - A variable in the traditional sense.

  Doesn't change until you explicitly call `.set()`.

  ```scala
  val num = Px(123).withReuse.manualUpdate
  num.set(666)
  ```

2. **Manual Refresh** - The value of a zero-param function.
  The value will not update until you explicitly call `refresh()`.

  ```scala
  case class State(name: String, age: Int)

  class ComponentBackend($: BackendScope[User, State]) {

    val user     = Px.props($).withReuse.manualRefresh
    val stateAge = Px.state($).map(_.age).withReuse.manualRefresh

    def render: VdomElement = {
      // Every render cycle, refresh Pxs. Unnecessary changes will be discarded.
      // This is a shortcut for:
      //   user.refresh(); stateAge.refresh()
      Px.refresh(user, stateAge)

      <.div(
        "Age is ", stateAge.value(),
        UserInfoComponent(user),
        SomeOtherComponent(user, stateAge))
    }
  }
  ```

3. **Auto Refresh** - The value of a zero-param function.
  The function will be called every time the value is requested, and the value updated if necessary.

  ```scala
  // Suppose this is updated by some process that periodically pings the server
  object InternalGlobalState {
    var usersOnline = 0
  }

  class ComponentBackend($: BackendScope[Props, _]) {
    val usersOnline = Px(InternalGlobalState.usersOnline).withReuse.autoRefresh

    // Only updated when the InternalGlobalState changes
    val coolGraphOfUsersOnline: Px[VdomElement] =
      for (u <- usersOnline) yield
        <.div(
          <.h3("Users online: ", u),
          coolgraph(u))

    def render(p: Props): VdomElement =
      <.div(
        "Hello ", p.username,
        coolGraphOfUsersOnline.value())
  }
  ```

4. **Constants** - `Px.constByValue(A)` & `Px.constByNeed(=> A)` create constant values.

  These `Px`s do not have the ability to change.


#### Derivative instances
Derivative `Px`s are created by:
* calling `.map`
* calling `.flatMap`
* using in a for comprehension
* using <code>Px.apply<sub>n</sub></code>

Example:
```scala
val project     : Px[Project]      = Px.props($).withReuse.manualRefresh
val viewSettings: Px[ViewSettings] = Px.state($).map(_.viewSettings).withReuse.manualRefresh

// Using .map
val columns   : Px[Columns]    = viewSettings.map(_.columns)
val textSearch: Px[TextSearch] = project map TextSearch.apply

// Using Px.applyn
val widgets: Px[Widgets] = Px.apply2(project, textSearch)(Widgets.apply)

// For comprehension
val rows: Px[Rows] =
  for {
    vs <- viewSettings
    p  <- project
    ts <- textSearch
  } yield
    new Rows(vs, p, ts.index)

// column.value()     will only change when viewSettings.refresh() is called and its state changes.
// textSearch.value() will only change when project.refresh() is called and the project changes.
// widgets.value()    will only change when either project or textSearch changes.
// rows.value()       will only change when viewSettings, project or textSearch changes.
```

#### `Px` doesn't have `Reusability`

For `Reusability` to work it needs to compare two immutable values; `Px` is mutable.

If you have `(a: Px[T], b: Px[T])` you might assume that if they are the same by reference equality `(a eq b)` and the revisions line up then they have `Reusability`. No.
A `Px` is useless unless you call `.value()` and it's these values you would need to compare in `shouldComponentUpdate`.
Comparing `a.value()` and `b.value()` will not work because `.value()` always returns the latest value;
you would need to know which value in its history was seen by your component.

In short, do not use `Px` in a component's props or state. Instead of `Px[A]`, just use the `A`.

```scala
// BAD!
case class Component2Props(count: Px[Int])
class Component1Backend {
  val px: Px[Int] = ...
  def render: VdomElement =
    Component2(Component2Props(px))
}

// Good
case class Component2Props(count: Int)
class Component1Backend {
  val px: Px[Int] = ...
  def render: VdomElement =
    Component2(Component2Props(px.value()))
}
```

There's also a convenience import in `Px.AutoValue` that avoids the need to call `.value()` on your `Px`s, if you're into that kind of thing.

```scala
// Also good
import Px.AutoValue._

case class Component2Props(count: Int)
class Component1Backend {
  val px: Px[Int] = ...
  def render: VdomElement =
    Component2(Component2Props(px))  // .value() called implicitly
}
```
