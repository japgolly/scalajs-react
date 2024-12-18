# React Hooks with scalajs-react

* [Design](#design)
* [Quickstart](#quickstart)
* [React hooks in scalajs-react](#react-hooks-in-scalajs-react)
* [New hooks provided by scalajs-react](#new-hooks-provided-by-scalajs-react)
* [`shouldComponentUpdate`](#shouldcomponentupdate)
* [Hooks with dependencies](#hooks-with-dependencies)
* [Hooks and PropsChildren](#hooks-and-propschildren)
* [Custom hooks](#custom-hooks)
* [Custom hook composition](#custom-hook-composition)
* [Using third-party JavaScript hooks](#using-third-party-javascript-hooks)
* [API extensions](#api-extensions)
* [Escape hatches](#escape-hatches)


# Design

// TODO REWRITE
...
Alternatively, you can use builder-style (link) which provides extra safety while being more verbose.
...

// TODO REWRITE

One of the core goals of scalajs-react is that if your code compiles, it will
work as expected at runtime. As a consequence, most translation of React JS
looks different in scalajs-react specifically because we want to turn runtime
errors into compile-time errors. Hooks is no different.

The main difference you'll notice is that instead of just creating hooks
imperatively, scalajs-react provides a builder-like DSL. The reason for this
difference is that it allows us to enforce at compile-time, the React JS rule
that the same hooks must always be created, and in the same order, even if they
aren't used in a given render pass. In a plain JS world, the onus is on the user
to have read the documentation and know that they have to avoid many very natural
types of code, else they'll get a runtime error (or worse, no runtime error but
undetected bugs). There is a mitigation in that JS users can use a linter that
uses AST reflection to try to detect when a user is misusing hooks.
In scalajs-react the DSL enforces these rules at compile-time without the need
for an AST-inspecting macro.

If you have a spare 9 hours (!), you can watch the livestreamed coding sessions
([part 1](https://www.youtube.com/watch?v=rDTr9TRFGSA), [part 2](https://www.youtube.com/watch?v=8pMXmk_YM5s))
and see how the design gradually evolved into what it (conceptually) is today.

// TODO REWRITE

# Quickstart

Let's translate this JS component...

```js
import React, { useState, useEffect } from 'react';

function Example() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    document.title = `You clicked ${count} times`;
  });

  const [fruit, setFruit] = useState("banana");

  return (
    <div>
      <p>You clicked {count} times</p>
      <button onClick={() => setCount(count + 1)}>
        Click me
      </button>
      <p>Your favourite fruit is a {fruit}!</p>
    </div>
  );
}
```

The above JS component can be written in scalajs-react in the following way:

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

object Example {
  val Component = ScalaFnComponent[Unit]( props =>
    for {
      count <- useState(0)
      _     <- useEffect(Callback {
                 document.title = s"You clicked ${count.value} times"
               })
      fruit <- useState("banana")
    } yield
      <.div(
        <.p(s"You clicked ${count.value} times"),
        <.button(
          ^.onClick --> count.modState(_ + 1),
          "Click me"
        ),
        <.p(s"Your favourite fruit is a ${fruit.value}!")
      )
  )
}
```

# React hooks in scalajs-react

| JavaScript | scalajs-react |
| --------- | -------- |
| `useCallback(c)` | `useCallback(c)` |
| `useCallback(c, [deps])` | `useCallbackWithDeps((deps))(_ => c)` |
| `useCallback(f([deps]), [deps])` | `useCallbackWithDeps((deps))(f)` |
| `useContext(c)` | `useContext(c)` |
| `useDebugValue(desc)` | `useDebugValue(desc)` |
| `useDebugValue(a, f)` | `useDebugValue(f(a))` |
| `useEffect(e)` | `useEffect(e)` |
| `useEffect(e, [])` | `useEffectOnMount(e)` |
| `useEffect(e, [deps])` | `useEffectWithDeps((deps))(_ => e)` |
| `useEffect(f([deps]), [deps])` | `useEffectWithDeps((deps))(f)` |
| `useLayoutEffect(e)` | `useLayoutEffect(e)` |
| `useLayoutEffect(e, [])` | `useLayoutEffectOnMount(e)` |
| `useLayoutEffect(e, [deps])` | `useLayoutEffectWithDeps((deps))(_ => e)` |
| `useLayoutEffect(f([deps]), [deps])` | `useLayoutEffectWithDeps((deps))(f)` |
| `useMemo(() => a, [deps])` | `useMemo((deps))(_ => a)` |
| `useMemo(() => f([deps]), [deps])` | `useMemo((deps))(f)` |
| `useReducer(f, s)` | `useReducer(f, s)` |
| `useReducer(f, a, i)` | `useReducer(f, i(a))`<br>*(Note: `i(a)` is actually `(=> i(a))` and isn't evaluated immediately)* |
| `useRef()` | `.useRefToAnyVdom` <br> `useRefToVdom[DomType]` <br> `useRefToScalaComponent(component)` <br> `useRefToScalaComponent[P, S, B]` <br> `useRefToJsComponent(component)` <br> `useRefToJsComponent[P, S]` <br> `useRefToJsComponentWithMountedFacade[P, S, F]` |
| `useRef(initialValue)` | `useRef(initialValue)` |
| `useState(initialState)` <br> `useState(() => initialState)` | `useState(initialState)` |
| `useId()` | `useId` | 
| `useTransition` | `useTransition` |
| Custom hook <br> `useBlah(i)` | `useBlah(i)` <br> (`def useBlah(i: I): HookResult[O]`) |

Note: The reason that `[deps]` on the JS side becomes `(deps)` on the Scala side,
is that in JS you'd use an array but in Scala you'd use a tuple.
So `[dep1, dep2]` becomes `(dep1, dep2)`; and `[dep1]` becomes just `dep1` which is the same as
`(dep1)`.


# New hooks provided by scalajs-react

| Hook | Description |
| ---- | ----------- |
| `useForceUpdate` | Provides a `Reusable[Callback]` then when invoked, forces a re-render of the component. |
| `useStateSnapshot(initialState)` <br> *(Requires import japgolly.scalajs.react.extra._)* | Same as `.useState` except you get a `StateSnapshot` (which accepts callbacks on set updates). |
| `useStateSnapshotWithReuse(initialState)` <br> *(Requires import japgolly.scalajs.react.extra._)* | Same as `.useState` except you get a `StateSnapshot` (which accepts callbacks on set updates) with state `Reusability`. |
| `useStateWithReuse(initialState)` | Conceptually `useState` + `shouldComponentUpdate`. Same as `useState` except that updates are dropped according to `Reusability`. |

# `shouldComponentUpdate`

In order to avoid a rerender in the case where the render dependencies are reusable,
you can render in a new component wrapped in `React.memo`, just as you would in JS.

For example, the above component can be rewritten as:
```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

object Example {
  private val ReusableRender = React.memo(
    ScalaFnComponent[(UseState[Int], UseState[String])]{ case (count, fruit) =>
      <.div(
        <.p(s"You clicked ${count.value} times"),
        <.button(
          ^.onClick --> count.modState(_ + 1),
          "Click me"
        ),
        <.p(s"Your favourite fruit is a ${fruit.value}!")
      )
    }
  )

  val Component = ScalaFnComponent[Unit]( props =>
    for {
      count <- useState(0)
      _     <- useEffect(Callback {
                 document.title = s"You clicked ${count.value} times"
               })
      fruit <- useState("banana")
    } yield ReusableRender(count, fruit)
  )
}
```

# Custom hooks

A custom hook is just a function that returns a `HookResult[O]`, where `O` is the output type (or `Unit` if your custom hook doesn't return an output).

To create a custom hook, the API is nearly identical to building a component with hooks, only that you are free to return any value instead of a `VdomNode`.

Example:

```scala
import japgolly.scalajs.react._
import org.scalajs.dom.document

object ExampleHook {
  val useTitleCounter: HookResult[UseState[Int]] =
    for {
      count <- useState(0)
      _     <- useEffect(Callback {
                 document.title = s"You clicked ${count.value} times"
               })
    } yield count
}
```

and to use it:

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Example {
  val Component = ScalaFnComponent[Unit]( _ =>
    ExampleHook.useTitleCounter // <--- usage
      .map( count =>
        <.div(
          <.p(s"You clicked ${count.value} times"),
          <.button(
            ^.onClick --> count.modState(_ + 1),
            "Click me"
          )
        )
      )
  )
}
```

# Using third-party JavaScript hooks (TODO)

Using a third-party JavaScript hook is as simple as wrapping it in `HookResult.fromFunction`.

```scala
// Declare your JS facade as normal as `useJsHookFacade`. Type should be a subtype of js.Function.
val useJsHook = HookResult.fromFunction(i => useJsHookFacade(i))
```


# Interop with builder-style

fromHookResult
toHookResult

# Escape hatches (TODO)

If you really, really want to work with JS-style imperative hooks, you can!
But it's important to note that the onus is on you to ensure you use hooks correctly without violating React's rules.
If you use the escape hatch, scalajs-react won't be able to check that your code will always work.

In the hooks API, there's `.unchecked(body)` and `.uncheckedBy(ctx => body)` that you can use as an escape hatch,
and create hooks using React directly instead of using scalajs-react's hooks API.

Example:

```scala
package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.facade.{React => ReactJs}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

object Example {
  val Component = ScalaFnComponent.withHooks[Unit]
    .unchecked {
      val count = ReactJs.useState[Int](0)
      ReactJs.useEffect(() => {
        document.title = s"You clicked ${count._1} times"
      })
      count
    }
    .render { (_, countHook) =>
      val count    = countHook._1
      val setCount = countHook._2
      <.div(
        <.p(s"You clicked $count times"),
        <.button(
          ^.onClick --> Callback(setCount(count + 1)),
          "Click me"
        ),
      )
    }
}
```
