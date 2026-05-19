# Testing

This file describes testing functionality provided by React.JS and scalajs-react.
<br>It is plenty for simple and small unit tests.

For larger and/or complicated tests, **it is highly recommended to use
[Scala Test-State](https://github.com/japgolly/test-state)**.
<br>See [this example](https://github.com/japgolly/test-state/tree/master/example-react)
for how to write tests for real-world scalajs-react applications.

#### Contents

- [Setup](#setup)
- [`ReactTestUtils`](#reacttestutils)
- [`Testing props changes`](#testing-props-changes)
- [`ReactTestVar`](#reacttestvar)
- [`Test Scripts`](#test-scripts)

# Setup

Firstly add this to your `project/plugins.sbt`:

```scala
addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.2")
```

Then get a copy of React v19 in UMD form.
You can download a copy from this repo in `/react-umd/dist`.
Save it as `src/test/resources/react.umd.js`.

You'll likely need a polyfill too.
You can download a copy from this repo in `/library/tests/src/test/resources/polyfill.js`.
Save it as `src/test/resources/polyfill.js`.

Now add the following to your sbt settings:

```scala
enablePlugins(JSDependenciesPlugin)

libraryDependencies ++= Seq(
  "com.github.japgolly.scalajs-react" %%% "test" % "3.0.0" % Test,
  "com.github.japgolly.scalajs-react" %%% "testing_library-dom" % "3.0.0" % Test,
)

jsDependencies ++= Seq(
  (ProvidedJS / "polyfill.js") % Test,
  (ProvidedJS / "react.umd.js" dependsOn "polyfill.js") % Test,
)
```

# `ReactTestUtils`

Read through the following for how to test with `ReactTestUtils`.

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.testing_library.dom.Simulate
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

# Testing props changes

Simply call `.render` from your React root. Example:

```scala
ReactTestUtils.withRendered(Carrot.Props("1").render) { t =>
  for {
    _ <- t.root.render(Carrot.Props("1").render)
    _ <- t.root.render(Carrot.Props("2").render)
  } yield ()
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
