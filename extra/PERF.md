Performance Management
======================

Included are several utilities for you to manage the performance of your React app.
The strategy is simple: avoid work where possible.

These utilities help you avoid work in two ways.

1. By making components' `shouldComponentUpdate` fns both easy to create, and accurate (safe). If it compiles, the logic in `shouldComponentUpdate` will be what you expect.
2. By allowing you to cache your own arbitrary data, and build on it in a way such that derivative data is also cached effeciently.

#### Contents

- [`React.addons.Perf`](#reactaddonsperf)
- [`Reusability`](#reusability)
  - [Usage](#usage)
  - [Example](#example)
  - [Monitoring](#monitoring)
- [`ReusableFn`](#reusablefn)
  - [Usage](#usage-1)
  - [Example](#example-1)
  - [Warning](#warning)
  - [Tricks](#tricks)
- [`ReusableVal`](#reusableval)
- [`ReusableVar`](#reusablevar)
- [`Px`](#px)
  - [Initial instances](#initial-instances)
  - [Derivative instances](#derivative-instances)
  - [`Px` doesn't have `Reusability`](#px-doesnt-have-reusability)


`React.addons.Perf`
===================
React addons come with performance tools. See https://facebook.github.io/react/docs/perf.html.

A Scala facade is now available via `japgolly.scalajs.react.Addons.Perf`.


`Reusability`
=============

`Reusability` is a typeclass that tests whether one instance can be used in place of another.
It's used to compare properties and state of a component to avoid unnecessary updates.

Imagine a class with 8 fields - typical equality like `==` would compare all 8 fields (and if
you're not using `scalaz.Equal` you've no way of knowing whether all those 8 fields have correct
`equals` methods defined).
When deciding whether a component needs updating, full equality comparison can be overkill (and slow) -
in many cases it is sufficient to check only the ID field, the update-date, or the revision number.
`Reusability` is designed for you to do just that.

#### Usage
When building your component, pass in `Reusability.shouldComponentUpdate` to your `ReactComponentB.configure`.

It will not compile until it knows how to compare the reusability of your props and state.
Out-of-the-box, it knows how to compare Scala primatives, `String`s, `Option`, `Either`, Scala tuples, `js.UndefOr`,
and Scalaz classes `\/` and `\&/`. For all other types, you'll need to teach it how. Use one of the following methods:

* `Reusability.byRef[A]` uses reference equality (ie. `a eq b`)
* `Reusability.by_==[A]` uses universal equality (ie. `a == b`)
* `Reusability.byEqual[A]` uses a Scalaz `Equal` typeclass
* <code>Reusability.caseclass<sub>n</sub></code> for case classes of your own.
* `Reusability.by(A => B)` to use a subset (`B`) of the subject data (`A`).
* `Reusability.fn((A, B) => Boolean)` to hand-write custom logic.

#### Example
The following component will only re-render when one of the following change:
* `props.name`
* `props.age`
* `props.pic.id`

```scala
  case class Picture(id: Long, url: String, title: String)
  case class Props(name: String, age: Option[Int], pic: Picture)

  implicit val picReuse   = Reusability.by((_: Picture).id)       // ← only check id
  implicit val propsReuse = Reusability.caseclass3(Props.unapply) // ← check all fields

  val component = ReactComponentB[Props]("Demo")
    .stateless
    .render((p, _) =>
      <.div(
        <.p("Name: ", p.name),
        <.p("Age: ", p.age.fold("Unknown")(_.toString)),
        <.img(^.src := p.pic.url, ^.title := p.pic.title))
    )
    .configure(Reusability.shouldComponentUpdate)                 // ← hook into lifecycle
    .build
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


`ReusableFn`
============

In effective usage of React, callbacks are passed around as component properties.
Due to the ease of function creation in Scala it is often the case that functions are created inline and thus
provide no means of determining whether a component can safely skip its update.

`ReusableFn` exists as a solution. It is a wrapper around a function that allows it to be both reused, and curried in a way that allows reuse.

#### Usage

1. Just wrap `ReusableFn` around your function.
2. Store the `ReusableFn` as a `val` somewhere outside of your `render` function, usually in the body of your backend class.
3. Replace the callback (say `A => B`) in components' props, to take a `ReusableFn[A, B]` or the shorthand `A ~=> B`.
4. Treat the `ReusableFn` as you would a normal function, save for one difference: application is curried (or Schönfinkel'ed), and each curried argument must have `Reusability`.

#### Example

In this example `personEditor` will only rerender if `props.name` changes, or the curried `PersonId` in its `props.update` function changes (which it won't - observable from the code).

```scala
type PersonId = Long
type PersonData = String

val topComponent = ReactComponentB[Map[PersonId, PersonData]]("Demo")
  .getInitialState(identity)
  .backend(new Backend(_))
  .render(_.backend.render)
  .build

class Backend($: BackendScope[_, Map[PersonId, PersonData]]) {

  val updateUser = ReusableFn((id: PersonId, data: PersonData) =>         // ← Create a 2-arg fn
    $.modStateIO(map => map.updated(id, data)))

  def render =
    <.div(
      $.state.map { case (id, name) =>
        personEditor.withKey(id)(PersonEditorProps(name, updateUser(id))) // ← Apply 1 arg
      }.toJsArray
    )
}

case class PersonEditorProps(name: String, update: String ~=> IO[Unit])   // ← Notice the ~=>

implicit val propsReuse = Reusability.caseclass2(PersonEditorProps.unapply)

val personEditor = ReactComponentB[PersonEditorProps]("PersonEditor")
  .stateless
  .render((p, _) =>
    <.input(
      ^.`type` := "text",
      ^.value := p.name,
      ^.onChange ~~> ((e: ReactEventI) => p.update(e.target.value))))    // ← Use as normal
  .configure(Reusability.shouldComponentUpdate)                          // ← shouldComponentUpdate like magic
  .build
```

#### WARNING!

**DO NOT** feed the ReusableFn(...) constructor a function directly *derived* from a component's props or state.
Access to props/state on the right-hand side of the function args is ok but if the function itself is a result of the
props/state, the function will forever be based on data that can go stale.

Example:
```scala
@Lenses case class Person(name: String, age: Int)
case class Props(person: ReusableVar[Person], other: Other)

// THIS IS BAD!!
ReusableFn($.props.person setL Props.name)

// It is equivalent to:
val g: String => IO[Unit]  = $.props.person setL Person.name   // ← $.props is evaluated once here
val f: String ~=> IO[Unit] = ReusableFn(g)                     // ← …and never again.
```

Alternatives:

1. Use `ReusableFn.byName`:

   ```scala
   ReusableFn.byName($.props.person setL Person.name)
   ```

2. Create a function with `$` on the right-hand side:

   ```scala
   ReusableFn(str => $.props.person.setL(Person.name)(str))
   ```


#### Tricks

To cater for some common use cases, there are few convenience methods that are useful to know.
For these examples imagine `$` to be your component's scope instance, eg. `BackendScope[_,S]`, `ComponentScopeM[_,S,_,_]` or similar.

1. `ReusableFn($).{set,mod}State{,IO}`.

    You'll find that if you try `ReusableFn($.method)` Scala will fail to infer the correct types.
    Use `ReusableFn($).method` instead to get the types that you expect.

    Example: instead of `ReusableFn($.setState)` use `ReusableFn($).setState` and you will correctly get a `S ~=> Unit`.
 
2. `ReusableFn.endo____`.

    Anytime the input to your `ReusableFn` is an endofunction (`A => A`), additional methods starting with `endo` become available.
    
    Specifically, `ReusableFn($).modState` returns a `(S => S) ~=> Unit` which you will often want to transform.
    These examples would be available on an `(S => S) ~=> U`:

    * `endoCall (S => (A => S)): A ~=> U` - Call a 1-arg function on `S`.
    * `endoCall2(S => ((A, B) => S)): A ~=> B ~=> U` - Call a 2-arg function on `S`.
    * `endoCall3(...): A ~=> B ~=> C ~=> U` - Call a 3-arg function on `S`.
    * `endoZoom((S, A) => S): A ~=> U` - Modify a subset of `S`.
    * `endoZoomL(Lens[S, A]): A ~=> U` - Modify a subset of `S` using a `Lens`.
    * `contramap[A](A => (S => S)): A ~=> U` - Not exclusive to endos, but similarly useful in a different shape.
  
  ```scala
  class Backend($: BackendScope[_, Map[Int, String]]) {

    // Manual long-hand
    val long: Int ~=> (String ~=> IO[Unit]) =
      ReusableFn((id: Int, data: String) => $.modStateIO(map => map.updated(id, data)))

    // Shorter using helpers described above
    val short: Int ~=> (String ~=> IO[Unit]) =
      ReusableFn($).modStateIO.endoCall2(_.updated)
  ```

3. `ReusableFn($ focusStateL lens)`

  Lenses provide an abstraction over read-and-write field access.
  Using Monocle, you can annotate your case classes with `@Lenses` to gain automatic lenses.
  `$ focusStateL lens` will then narrow the scope of its state to the field targeted by the given lens.
  This can then be used with `ReusableFn` as follows:
  
  ```scala
  @Lenses
  case class Person(name: String, age: Int)

  class Backend($: BackendScope[_, Person]) {
    
    val nameSetter: String ~=> Unit =
      ReusableFn($ focusStateL Person.name).setState
  ```


`ReusableVal`
=============

Usually reusability is determined by type (ie. via an implicit `Reusability[A]` available for an `A`).
Instead, a `ReusableVal` promises that whoever provides the value will also explicitly specify the value's reusability.

#### Usage

```scala
// Create and specify the Reusability
val i: ReusableVal[Int] =
  ReusableVal(1027)(Reusability.fn((a,b) => a + 99 < b))

// For convenience, there's ReusableVal.byRef
val e: ReusableVal[ReactElement] =
  ReusableVal.byRef(<.span("Hello"))
```


`ReusableVar`
=============

Just as there is `ExternalVar` that provides a component with safe R/W access to an external variable,
there is also `ReusableVar`.

#### Example
```scala
@Lenses case class State(name: String, desc: String)

val topComponent = ReactComponentB[State]("Demo")
  .getInitialState(identity)
  .backend(new Backend(_))
  .render(_.backend.render)
  .build

class Backend($: BackendScope[State, State]) {
  val setName = ReusableVar.state($ focusStateL State.name)
  val setDesc = ReusableVar.state($ focusStateL State.desc)

  def render =
    <.div(
      stringEditor(setName),
      stringEditor(setDesc))
}

val stringEditor = ReactComponentB[ReusableVar[String]]("StringEditor")
  .stateless
  .render((p, _) =>
    <.input(
      ^.`type` := "text",
      ^.value := p.value,
      ^.onChange ~~> ((e: ReactEventI) => p.set(e.target.value))))
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
`Px` comes in two flavours: those with reusable values, and those without.
If its values are reusable then when its underlying value `A` changes, it will compare the new `A` value to the previous `A` (using `Reusability[A]`) and discard the change if it can.
If its values are reusable, all changes to the underlying value (including duplicates) are accepted.

Create a non-derivative `Px` using one of these:

1. `Px(…)` & `Px.NoReuse(…)` - A variable in the traditional sense.

  Doesn't change until you explicitly call `set()`.

  ```scala
  val num = Px(123)
  num.set(666)
  ```

2. `Px.thunkM(…)` & `Px.NoReuse.thunkM(…)` - The value of a zero-param function.

  The `M` in `ThunkM` denotes "Manual refresh", meaning that the value will not update until you explicitly call `refresh()`.

  ```scala
  case class State(name: String, age: Int)

  class ComponentBackend($: BackendScope[User, State]) {
    val user     = Px.thunkM($.props)
    val stateAge = Px.thunkM($.state.age)

    def render: ReactElement = {
      // Every render cycle, refresh Pxs. Unnecessary changes will be discarded.
      Px.refresh(user, stateAge)

      <.div(
        "Age is ", stateAge.value,
        UserInfoComponent(user),
        SomeOtherComponent(user, stateAge)
      )
    }
  }
  ```

3. `Px.thunkA(…)` & `Px.NoReuse.thunkA(…)` - The value of a zero-param function.

  The `A` in `ThunkA` denotes "Auto refresh", meaning that the function will be called every time the value is requested, and the value updated if necessary.

  ```scala
  // Suppose this is updated by some process that periodically pings the server
  object InternalGlobalState {
    var usersOnline = 0
  }

  class ComponentBackend($: BackendScope[Props, _]) {
    val usersOnline = Px.thunkA(InternalGlobalState.usersOnline)

    // Only updated when the InternalGlobalState changes
    val coolGraphOfUsersOnline: Px[ReactElement] =
      for (u <- usersOnline) yield
        <.div(
          <.h3("Users online: ", u),
          coolgraph(u))

    def render: ReactElement =
      <.div(
        "Hello ", $.props.username,
        coolGraphOfUsersOnline.value())
  }
  ```

4. `Px.const(A)` & `Px.lazyConst(=> A)` - A constant value.

  These `Px`s do not have the ability to change.


#### Derivative instances
Derivative `Px`s are created by:
* calling `.map`
* calling `.flatMap`
* using in a for comprehension
* using <code>Px.apply<sub>n</sub></code>

Example:
```scala
val project     : Px[Project]      = Px.thunkM($.props)
val viewSettings: Px[ViewSettings] = Px.thunkM($.state.viewSettings)

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
  def render: ReactElement =
    Component2(Component2Props(px))
}

// Good
case class Component2Props(count: Int)
class Component1Backend {
  val px: Px[Int] = ...
  def render: ReactElement =
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
  def render: ReactElement =
    Component2(Component2Props(px))  // .value() called implicitly
}
```
