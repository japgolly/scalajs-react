# Testing

This file describes testing functionality provided by React.JS and scalajs-react.
<br>It is plenty for simple and small unit tests.

For larger and/or complicated tests, **it is highly recommended to use
[Scala Test-State](https://github.com/japgolly/test-state)**.
<br>See [this example](https://github.com/japgolly/test-state/tree/master/example-react)
for how to write tests for real-world scalajs-react applications.

#### Contents

- [Setup](#setup)
- [`ReactTestUtils`](#reacttestutils-since-300)
- [`LegacyReactTestUtils [DEPRECATED IN 3.0.0]`](#legacyreacttestutils-deprecated-in-300)
- [`Simulate` and `Simulation`](#simulate-and-simulation)
- [`Testing props changes`](#testing-props-changes)
- [`ReactTestVar`](#reacttestvar)
- [`Test Scripts`](#test-scripts)
- [Fatal React warnings](#fatal-react-warnings)

# Setup

1. Install PhantomJS.

2. Add the following to SBT:

   ```scala
   // scalajs-react test module
   libraryDependencies += "com.github.japgolly.scalajs-react" %%% "test" % "2.1.3" % Test

   // React JS itself.
   // NOTE: Requires react-with-addons.js instead of just react.js
   jsDependencies +=

     "org.webjars.npm" % "react-dom" % "18.3.1" % Test
       /         "umd/react-dom-test-utils.development.js"
       minified  "umd/react-dom-test-utils.production.min.js"
       dependsOn "umd/react-dom.development.js"
       commonJSName "ReactTestUtils"
   ```

# `ReactTestUtils [SINCE 3.0.0]`

`ReactTestUtils` has been rewritten for scalajs-react v3 and React v18.
What used to be `ReactTestUtils` prior to scalajs-react v3 has been renamed to `LegacyReactTestUtils`.

Read through the following for how to test with `ReactTestUtils`.

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.vdom.html_<^._
import utest._

object TestUtilsDemo extends TestSuite {

  // This is a sample component that we will test
  val Component = ScalaFnComponent[String](props =>
    for {
      count <- useState(0)
    } yield
      <.div(
        <.p(s"Hi $props. You clicked ${count.value} times"),
        <.button("Click me", ^.onClick --> count.modState(_ + 1)),
      )
  )

  override def tests = Tests {

    // First we render the component
    ReactTestUtils.withRenderedSync(Component("Axe")) { t =>

      // We have a variety of ways to test the HTML
      t.outerHTML.assert("<div><p>Hi Axe. You clicked 0 times</p><button>Click me</button></div>")
      t.root.outerHTML.assert("<div><div><p>Hi Axe. You clicked 0 times</p><button>Click me</button></div></div>")
      t.innerHTML.assertContains("You clicked 0 times")

      // Let's click the button
      Simulate.click(t.querySelector("button"))
      t.innerHTML.assertContains("You clicked 1 times")

      // Let's change the props
      t.root.renderSync(Component("Bob"))
      t.innerHTML.assertContains("Hi Bob. You clicked 1 times")
    }

  }
}
```

# `LegacyReactTestUtils [DEPRECATED IN 3.0.0]`

This used to be called `ReactTestUtils` prior to scalajs-react v3 and React v18.

The main bucket of testing utilities lies in `japgolly.scalajs.react.test.LegacyReactTestUtils`.

Half of the methods delegate to React.JS's [React.addons.TestUtils](https://facebook.github.io/react/docs/test-utils.html)
(for which there is a raw facade in `japgolly.scalajs.react.test.raw.ReactAddonsTestUtils` if you're interested).

The other half are new functions added specifically in scalajs-react.

- Rendering into DOM with auto-removal
  - `withRendered[M, A](u: Unmounted[M], intoBody: Boolean)(f: M => A): A`
  - `withRenderedIntoDocument[M, A](u: Unmounted[M])(f: M => A): A`
  - `withRenderedIntoBody[M, A](u: Unmounted[M])(f: M => A): A`
  - `withNewBodyElement[A](use: Element => A): A`
  - `newBodyElement(): Element`
  - `removeNewBodyElement(e: Element): Unit`
  - `renderIntoBody[M, A](u: Unmounted[M]): M`
- Asynchronously rendering into DOM with auto-removal
  - `withRenderedAsync[M, A](u: Unmounted[M], intoBody: Boolean)(f: M => Future[A]): Future[A]`
  - `withRenderedIntoDocumentAsync[M, A](u: Unmounted[M])(f: M => Future[A]): Future[A]`
  - `withRenderedIntoBodyAsync[M, A](u: Unmounted[M])(f: M => Future[A]): Future[A]`
  - `withNewBodyElementAsync[A](use: Element => Future[A]): Future[A]`
- Mounted props modification
  - `replaceProps(component, mounted)(newProps: P): mounted'`
  - `modifyProps(component, mounted)(f: P => P): mounted'`
- Other
  - `removeReactInternals(html: String): String` - Removes internal annotations from HTML that React inserts.

There's only one magic implicit method this time around:
Mounted components get `.outerHtmlScrubbed()` which is shorthand for
`LegacyReactTestUtils.removeReactInternals(m.getDOMNode.outerHTML)`.

# `Simulate` and `Simulation`

To make event simulation easier, certain event types have dedicated, strongly-typed case classes to wrap event data. For example, JS like

```js
// JavaScript
ReactAddons.TestUtils.Simulate.change(t, { target: { value: "Hi" } });
```

becomes

```scala
// Scala
Simulate.change(t, SimEvent.Change(value = "Hi"))

// Or shorter
SimEvent.Change("Hi") simulate t
```

`Simulate` is from React and imperative.
If you'd like more composability and/or purity there's also `Simulation` which
represents action (without a target). It does nothing until `.run` is called and a target is provided.

Example:

```scala
val a = Simulation.focus
val b = Simulation.change(SimEvent.Change(value = "hi"))
val c = Simulation.blur
val s = a andThen b andThen c

// Or shorter
val s = Simulation.focus >> SimEvent.Change("hi").simulation >> Simulation.blur

// Or even shorter again, using a convenience method
val s = Simulation.focusChangeBlur("hi")

// Then run it when you're ready
s run component
```

# Testing props changes

If you're using scalajs-react v3 and React v18, simply call `.render` from your React root. Example:

```scala
ReactTestUtils.withRendered(Carrot.Props("1").render) { t =>
  for {
    _ <- t.root.render(Carrot.Props("1").render)
    _ <- t.root.render(Carrot.Props("2").render)
  } yield ()
}
```

If you're not using scalajs-react v3 and React v18, then
when you want to simulate a parent component re-rendering a child component with different props,
you can test the child directly using `LegacyReactTestUtils.{modify,replace}Props`.

Example of code to test:

```scala
class CP {
  var prev = "none"
  def render(p: String) = <.div(s"$prev → $p")
}
val CP = ScalaComponent.builder[String]("asd")
  .backend(_ => new CP)
  .renderP(_.backend.render(_))
  .componentWillReceiveProps(i => Callback(i.backend.prev = i.currentProps))
  .build
```

Example test case:

```scala
LegacyReactTestUtils.withRenderedIntoDocument(CP("start")) { m =>
  assert(m.outerHtmlScrubbed(), "<div>none → start</div>")

  LegacyReactTestUtils.modifyProps(CP, m)(_ + "ed")
  assert(m.outerHtmlScrubbed(), "<div>start → started</div>")

  LegacyReactTestUtils.replaceProps(CP, m)("done!")
  assert(m.outerHtmlScrubbed(), "<div>started → done!</div>")
}
```

# `ReactTestVar`

A `ReactTestVar[A]` is a wrapper around a `var a: A` that:

- can produce a `StateSnapshot[A]` with or without `Reusability`
- can produce a `StateAccess[A]`
- retains history when modified
- can perform arbitrary actions when modified
- can be reset

It's useful for testing components that accept `StateSnapshot[A]`/`StateAccess[A]` instances
in their props.

#### Example testing `StateSnapshot`

```scala
import utest._
import japgolly.scalajs.react._, vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.test._

object ExampleTest extends TestSuite {

  val NameChanger = ScalaComponent.builder[StateSnapshot[String]]("Name changer")
    .render_P { ss =>
      def updateName = (event: ReactEventFromInput) => ss.setState(event.target.value)
      <.input.text(
        ^.value     := ss.value,
        ^.onChange ==> updateName)
    }
    .build

  override def tests = TestSuite {

    val nameVar = ReactTestVar("guy")
    LegacyReactTestUtils.withRenderedIntoDocument(NameChanger(nameVar.stateSnapshot())) { m =>
      SimEvent.Change("bob").simulate(m)
      assert(nameVar.value() == "bob")
    }

  }
}
```

#### Example testing `StateAccess`

When testing a `StateAccess` make sure to feed updates to the `ReactTestVar` back into the component
via `.forceUpdate`.

```scala
val component: ScalaComponent[StateAccessPure[Int], Unit, Unit] = ...

val testVar = ReactTestVar(1)
LegacyReactTestUtils.withRenderedIntoDocument(component(testVar.stateAccess)) { m =>
  testVar.onUpdate(m.forceUpdate) // Update the component when it changes the state

  assert(m.outerHtmlScrubbed() == "<div>1</div>")
  Simulate.click(m.getDOMNode) // our eample component calls .modState(_ + 1) onClick
  assert(testVar.value() == 2)
  assert(m.outerHtmlScrubbed() == "<div>2</div>")
}
```

# Test Scripts

It's possible to write test scripts like

1. _click this_
2. _verify that_
3. _press the Back button_
4. _type name_
5. _press Enter_

In case you missed the notice at the top of the file, that functionality is provided in a sister library called
[Scala Test-State](https://github.com/japgolly/test-state).

See [this example](https://github.com/japgolly/test-state/tree/master/example-react)
for how to write tests for real-world scalajs-react applications.

# Fatal React warnings

The easiest way to make `LegacyReactTestUtils` to turn React warnings into runtime exceptions,
is via a [config option](./CONFIG.md#testwarningsreact).

Alternatively, you can do any of the following...

- Wrapping a test

  ```scala
  import japgolly.scalajs.react.test.ReactTestUtilsConfig
  ReactTestUtilsConfig.AroundReact.fatalReactWarnings {
    // test code here
  }
  ```

- Installing for all `LegacyReactTestUtils` usage

  ```scala
  import japgolly.scalajs.react.test.ReactTestUtilsConfig
  ReactTestUtilsConfig.aroundReact.set(
    ReactTestUtilsConfig.AroundReact.fatalReactWarnings)
  ```

- Installing outside of test code

  ```scala
  import japgolly.scalajs.react.util.ConsoleHijack
  ConsoleHijack.fatalReactWarnings.install()
  ```

- Wrapping non-test code

  ```scala
  import japgolly.scalajs.react.util.ConsoleHijack
  ConsoleHijack.fatalReactWarnings {
    // code here
  }
  ```
