Performance Management
======================

Included are several utilities for you to manage the performance of your React app.
The strategy is simple: avoid work where possible.

These utilities help you avoid work in two ways.

1. By making components' `shouldComponentUpdate` fns both easy to create, and accurate (safe). If it compiles, the logic in `shouldComponentUpdate` will be what you expect.
2. By allowing you to cache your own arbitrary data, and build on it in a way such that derivative data is also cached effeciently.

Px
==

`Px` is a mechanism for caching data with dependencies.
It's basically a performance-focused, lightweight implementation of pull-based
[FRP](http://en.wikipedia.org/wiki/Functional_reactive_programming);
pull-based meaning that in the chain A→B→C, an update to A doesn't affect C until the value of C is requested.

*What does Px mean? I don't know, I just needed a name and I liked the way @lihaoyi's Rx type name looked in code.
You can consider this "Performance eXtension". If this were Java it'd be named
`AutoRefreshOnRequestDependentCachedVariable`.*

##### Initial instances
`Px` comes in two flavours: reusable and not.
If it's "reusable" then when its underlying value changes, it will compare the new value to the previous one and discard the change if it can.

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

##### Derivative instances
Derivative `Px`s are created by:
* calling `.map`
* calling `.flatMap`
* using in a for comprehension
* using `Px.applyₙ`

Example:
```scala
val project     : Px[Project]      = Px.thunkM($.props)
val viewSettings: Px[ViewSettings] = Px.thunkM($.state.viewSettings)

// Using .map
val columns   : Px[Columns]    = viewSettings.map(_.columns)
val textSearch: Px[TextSearch] = project map TextSearch.apply

// Using Px.applyₙ
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

Reusable
========

`Reusable` is a typeclass that tests whether one instance can be used in place of another.
It is used mostly to compare properties and state of a component to avoid unnecessary updates.

If you imagine a class with 8 fields, equality would compare all 8 fields where as this would typically just compare
the ID field, the update-date, or the revision number.
You might think of this as a very quick version of equality.

##### Usage
When building your component, pass in `Reusable.shouldComponentUpdate` to your `ReactComponentB.configure`.

It will not compile until it knows how to compare the reusability of your props and state.
Out-of-the-box, it knows how to compare Scala primatives, `String`s, `Option`, `Either`, Scala tuples, `js.UndefOr`,
and Scalaz classes `\/` and `\&/`. For all other types, you'll need to teach it how. Use one of the following methods:

* `Reusable.byRef[A]` uses reference equality (ie. `a eq b`)
* `Reusable.by_==[A]` uses universal equality (ie. `a == b`)
* `Reusable.byEqual[A]` uses a Scalaz `Equal` typeclass
* `Reusable.caseclassₙ` for case classes of your own.
* `Reusable.by(A => B)` to use a subset (`B`) of the subject data (`A`).

##### Example
The following component will only re-render when one of the following change:
* `props.name`
* `props.age`
* `props.pic.id`

```scala
  case class Picture(id: Long, url: String, title: String)
  case class Props(name: String, age: Option[Int], pic: Picture)

  implicit val picReuse   = Reusable.by((_: Picture).id)       // ← only check id
  implicit val propsReuse = Reusable.caseclass3(Props.unapply) // ← check all fields

  val component = ReactComponentB[Props]("Demo")
    .stateless
    .render((p, _) =>
      <.div(
        <.p("Name: ", p.name),
        <.p("Age: ", p.age.fold("Unknown")(_.toString)),
        <.img(^.src := p.pic.url, ^.title := p.pic.title))
    )
    .configure(Reusable.shouldComponentUpdate)                 // ← hook into lifecycle
    .build
```


ReusableFn
==========

ReusableVar
===========
