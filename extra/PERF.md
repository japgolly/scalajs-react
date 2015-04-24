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

##### Usage
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

ReusableFn
==========

ReusableVar
===========
