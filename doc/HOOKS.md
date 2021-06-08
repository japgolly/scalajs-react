# React Hooks with scalajs-react

* [Design](HOOKS.md#design)
* [Quickstart](HOOKS.md#quickstart)
* [React hooks in scalajs-react](HOOKS.md#react-hooks-in-scalajs-react)
* [New hooks provided by scalajs-react](HOOKS.md#new-hooks-provided-by-scalajs-react)
* [Hook chaining / dependencies / results](HOOKS.md#hook-chaining--dependencies--results)
* [Hooks and PropsChildren](HOOKS.md#hooks-and-propschildren)
* [Custom hooks](HOOKS.md#custom-hooks)
* [Custom hook composition](HOOKS.md#custom-hook-composition)
* [Using third-party JavaScript hooks](HOOKS.md#using-third-party-javascript-hooks)
* [API extensions](HOOKS.md#api-extensions)
* [Escape hatches](HOOKS.md#escape-hatches)


# Design

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

The above JS component can be written in scalajs-react in two
very similar ways.

### Method 1

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

object Example {
  val Component = ScalaFnComponent.withHooks[Unit]

    .useState(0)

    .useEffectBy((_, count) => Callback {
      document.title = s"You clicked ${count.value} times"
    })

    .useState("banana")

    .render((_, count, fruit) =>
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

### Method 2

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

object Example {
  val Component = ScalaFnComponent.withHooks[Unit]

    .useState(0)

    .useEffectBy($ => Callback {
      document.title = s"You clicked ${$.hook1.value} times"
    })

    .useState("banana")

    .render($ =>
      <.div(
        <.p(s"You clicked ${$.hook1.value} times"),
        <.button(
          ^.onClick --> $.hook1.modState(_ + 1),
          "Click me"
        ),
        <.p(s"Your favourite fruit is a ${$.hook2.value}!")
      )
    )
}
```

# React hooks in scalajs-react

| JavaScript | scalajs-react |
| --------- | -------- |
| `useCallback(c)` | `.useCallback(c)` |
| `useCallback(c, [deps])` | `.useCallbackWithDeps(c, (deps))` |
| `useContext(c)` | `.useContext(c)` |
| `useDebugValue(desc)` | `.useDebugValue(desc)` |
| `useDebugValue(a, f)` | `.useDebugValue(f(a))` |
| `useEffect(e)` | `.useEffect(e)` |
| `useEffect(e, [])` | `.useEffectOnMount(e)` |
| `useEffect(e, [deps])` | `.useEffectWithDeps(e, (deps))` |
| `useLayoutEffect(e)` | `.useLayoutEffect(e)` |
| `useLayoutEffect(e, [])` | `.useLayoutEffectOnMount(e)` |
| `useLayoutEffect(e, [deps])` | `.useLayoutEffectWithDeps(e, (deps))` |
| `useMemo(() => a, [deps])` | `.useMemo(a, (deps))` |
| `useReducer(f, s)` | `.useReducer(f, s)` |
| `useReducer(f, a, i)` | `.useReducer(f, i(a))` |
| `useRef()` | `.useRefToAnyVdom` <br> `.useRefToVdom[DomType]` <br> `.useRefToScalaComponent(component)` <br> `.useRefToScalaComponent[P, S, B]` <br> `.useRefToJsComponent(component)` <br> `.useRefToJsComponent[P, S]` <br> `.useRefToJsComponentWithMountedFacade[P, S, F]` |
| `useRef(initialValue)` | `.useRef(initialValue)` |
| `useState(initialState)` <br> `useState(() => initialState)` | `.useState(initialState)` |
| Custom hook <br> `useBlah(i)` | `.custom(useBlah(i))`


# New hooks provided by scalajs-react

| Hook | Description |
| ---- | ----------- |
| `.localLazyVal(a)` | Creates a new `lazy val` on each render. |
| `.localVal(a)` | Creates a new `val` on each render. |
| `.localVar(a)` | Creates a new `var` on each render. |
| `.useForceUpdate` | Provides a `Reusable[Callback]` then when invoked, forces a re-render of the component. |
| `.useStateSnapshot(initialState)` <br> *(Requires import japgolly.scalajs.react.extra._)* | Same as `.useState` except you get a `StateSnapshot` (which accepts callbacks on set updates). |
| `.useStateSnapshotWithReuse(initialState)` <br> *(Requires import japgolly.scalajs.react.extra._)* | Same as `.useState` except you get a `StateSnapshot` (which accepts callbacks on set updates) with state `Reusability`. |
| `.useStateWithReuse(initialState)` | Conceptually `useState` + `shouldComponentUpdate`. Same as `useState` except that updates are dropped according to `Reusability`. |

# Hook chaining / dependencies / results

Sometimes hooks are initialised using props and/or the output of other hooks,
(which scalajs-react refers to as "context").
Each hook that has a return type that's not `Unit`,
becomes available in subsequent contexts.

In order to get access to this context, append a `By` suffix to the hook method
of your choice, and change the arguments to functions that take the context.
There are two ways to do this.

### 1. useXxxxxBy((props, hook1, hook2, ...) => arg)

```scala
val comp = ScalaFnComponent.withHooks[Int]
  .useStateBy(props => props - 1) // initialise state according to props
  .useEffectBy((props, hook1) => Callback.log(s"Props: $props, State: ${hook1.value}"))
```

### 2. useXxxxxBy(ctxObj => arg)

```scala
val comp = ScalaFnComponent.withHooks[Int]
  .useStateBy(props => props - 1) // initialise state according to props
  .useEffectBy(c => Callback.log(s"Props: ${c.props}, State: ${c.hook1.value}"))
```

### Hooks that return `Unit` don't appear in context

```scala
val comp = ScalaFnComponent.withHooks[Int]

  // The result of this hook becomes "hook1"
  .useStateBy(props => props - 1)

  // The result of useEffect is Unit and doesn't appear in context
  .useEffectBy(c => Callback.log(s"Props: ${c.props}, State: ${c.hook1.value}"))

  // The result of this hook becomes "hook2"
  .useState(123)

  .render((props, hook1, hook2) =>
    <.div(
      <.div("State 1 = ", hook1.value),
      <.div("State 2 = ", hook2.value),
    )
  )
```

# Hooks and PropsChildren

In order to get access to `PropsChildren`, call `.withPropsChildren` as the first step in your DSL.
It will then become available...

1) as argument #2 after `props` in multi-arg fns (eg. `.render((props, propsChildren, hook1, hook2, ...) => `)
2) as `.propsChildren` from context objects (eg. `.render($ => $.propsChildren)`)

Example:

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Example {
  final case class Props(name: String)

  val Component = ScalaFnComponent.withHooks[Props]
    .withPropsChildren
    .useState(0)
    .render((props, propsChildren, counter) =>
      <.div(
        <.p(s"Hello ${props.name}."),
        <.p(s"You clicked ${counter.value} times."),
        <.button("Click me", ^.onClick --> counter.modState(_ + 1)),
        <.div(propsChildren)
      )
    )
}
```

# Custom hooks

A custom hook has the type `CustomHook[I, O]` where
`I` is the input type (or `Unit` if your custom hook doesn't take an input),
and `O` is the output type (or `Unit` if your custom hook doesn't return an output),

To create a custom hook, the API is nearly identical to building a component with hooks.

1. Start with `CustomHook[I]` instead of `ScalaFnComponent.withHooks[P]`
2. Complete your hook with `.buildReturning(ctx => O)`, or just `.build` if you don't need to return a value.

Example:

```scala
import japgolly.scalajs.react._
import org.scalajs.dom.document

object ExampleHook {
  val useTitleCounter = CustomHook[Unit]
    .useState(0)
    .useEffectBy((_, count) => Callback {
      document.title = s"You clicked ${count.value} times"
    })
    .buildReturning(_.hook1)
}
```

and to use it:

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Example {
  val Component = ScalaFnComponent.withHooks[Unit]
    .custom(ExampleHook.useTitleCounter) // <--- usage
    .render((_, count) =>
      <.div(
        <.p(s"You clicked ${count.value} times"),
        <.button(
          ^.onClick --> count.modState(_ + 1),
          "Click me")))
}
```

In order to provide the hook directly via `.custom` the input type of the hook must be one of the following...
* `Unit`
* same as the `Props` type
* `PropsChildren`

If the custom hook has any other kind of type, simply provide it to the hook directly.
Example:

```scala
  val someCustomHook: CustomHook[Int, Unit] = ???

  final case class Props(someInt: Int)

  val Component = ScalaFnComponent.withHooks[Props]
    .custom(someCustomHook(123)) // provide a constant Int arg
    .customBy($ => someCustomHook($.props.someInt)) // or use a dynamic value
```

# Custom hook composition

CustomHooks can be composed by calling `++`.

The input/output type of the result will be the "natural" result according to these rules:
* `A ++ Unit` or `Unit ++ A` becomes `A`
* `A ++ A` becomes `A` if it's in the input position
* `A ++ A` becomes `A` if it's in the output position and `A <: scala.Singleton`
* otherwise `A ++ B` becomes `(A, B)`

Examples:

```scala
object Example1 {
  val hook1: CustomHook[Int, Unit] = ???
  val hook2: CustomHook[Int, Unit] = ???
  val hooks: CustomHook[Int, Unit] = hook1 ++ hook2
}

object Example2 {
  val hook1: CustomHook[Unit, Int] = ???
  val hook2: CustomHook[Unit, Int] = ???
  val hooks: CustomHook[Unit, (Int, Int)] = hook1 ++ hook2
}

object Example3 {
  val hook1: CustomHook[Long, Boolean] = ???
  val hook2: CustomHook[String, Int] = ???
  val hooks: CustomHook[(Long, String), (Boolean, Int)] = hook1 ++ hook2
}

object Example4 {
  val hook1: CustomHook[Unit, Boolean] = ???
  val hook2: CustomHook[String, Unit] = ???
  val hooks: CustomHook[String, Boolean] = hook1 ++ hook2
}
```

# Using third-party JavaScript hooks

Using a third-party JavaScript hook is as simple as wrapping it in `CustomHook.unchecked`.

```scala
// Declare your JS facade as normal. Type should be a subtype of js.Function.

// I is the type of the hook's inputs (use a tuple or case class for multiple args)
// O is the type of the hook's output (or Unit if none)
val jsHook = CustomHook.unchecked[I, O](i => JsHookFacade(i))
```

Then to use it, either...

1) simply call `.custom(jsHook)` from your component
2) or create an API extension as shown below

# API extensions

You can also provide your own implicit extensions to the hook API.

Unfortunately it involves a bit of boilerplate. Copy and customise one of the following templates:

### Template 1: Custom hooks that take input and return output

```scala
import japgolly.scalajs.react._

object MyCustomHook {

  // TODO: Replace
  val hook = CustomHook[String]
    .useEffectOnMountBy(name => Callback.log(s"HELLO $name"))
    .buildReturning(name => name)

  object HooksApiExt {
    sealed class Primary[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]) {

      // TODO: Change hook name, input args/type(s), and output type
      final def useMyCustomHook(name: String)(implicit step: Step): step.Next[String] =
        // TODO: Change hook name
        useMyCustomHookBy(_ => name)

      // TODO: Change hook name, input args/type(s), and output type
      final def useMyCustomHookBy(name: Ctx => String)(implicit step: Step): step.Next[String] =
        api.customBy(ctx => hook(name(ctx)))
    }

    final class Secondary[Ctx, CtxFn[_], Step <: HooksApi.SubsequentStep[Ctx, CtxFn]](api: HooksApi.Secondary[Ctx, CtxFn, Step]) extends Primary[Ctx, Step](api) {

      // TODO: Change hook name, input args/type(s), and output type
      def useMyCustomHookBy(name: CtxFn[String])(implicit step: Step): step.Next[String] =
        // TODO: Change hook name, squash each parameter
        // useMyCustomHookBy(step.squash(arg1)(_), step.squash(arg2)(_), ...)
        useMyCustomHookBy(step.squash(name)(_))
    }
  }

  trait HooksApiExt {
    import HooksApiExt._

    // TODO: Change hook name so that it won't conflict with other custom hooks
    implicit def hooksExtMyCustomHook1[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]): Primary[Ctx, Step] =
      new Primary(api)

    // TODO: Change hook name so that it won't conflict with other custom hooks
    implicit def hooksExtMyCustomHook2[Ctx, CtxFn[_], Step <: HooksApi.SubsequentStep[Ctx, CtxFn]](api: HooksApi.Secondary[Ctx, CtxFn, Step]): Secondary[Ctx, CtxFn, Step] =
      new Secondary(api)
  }

  object Implicits extends HooksApiExt
}
```

### Template 2: Custom hooks that take input and don't return output

```scala
import japgolly.scalajs.react._

object MyCustomHook {

  // TODO: Replace
  val hook = CustomHook[String]
    .useEffectOnMountBy(name => Callback.log(s"HELLO $name"))
    .build

  object HooksApiExt {
    sealed class Primary[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]) {

      // TODO: Change hook name, input args/type(s), and output type
      final def useMyCustomHook(name: String)(implicit step: Step): step.Self =
        // TODO: Change hook name
        useMyCustomHookBy(_ => name)

      // TODO: Change hook name, input args/type(s), and output type
      final def useMyCustomHookBy(name: Ctx => String)(implicit step: Step): step.Self =
        api.customBy(ctx => hook(name(ctx)))
    }

    final class Secondary[Ctx, CtxFn[_], Step <: HooksApi.SubsequentStep[Ctx, CtxFn]](api: HooksApi.Secondary[Ctx, CtxFn, Step]) extends Primary[Ctx, Step](api) {

      // TODO: Change hook name, input args/type(s), and output type
      def useMyCustomHookBy(name: CtxFn[String])(implicit step: Step): step.Self =
        // TODO: Change hook name, squash each parameter
        // useMyCustomHookBy(step.squash(arg1)(_), step.squash(arg2)(_), ...)
        useMyCustomHookBy(step.squash(name)(_))
    }
  }

  trait HooksApiExt {
    import HooksApiExt._

    // TODO: Change hook name so that it won't conflict with other custom hooks
    implicit def hooksExtMyCustomHook1[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]): Primary[Ctx, Step] =
      new Primary(api)

    // TODO: Change hook name so that it won't conflict with other custom hooks
    implicit def hooksExtMyCustomHook2[Ctx, CtxFn[_], Step <: HooksApi.SubsequentStep[Ctx, CtxFn]](api: HooksApi.Secondary[Ctx, CtxFn, Step]): Secondary[Ctx, CtxFn, Step] =
      new Secondary(api)
  }

  object Implicits extends HooksApiExt
}
```

### Template 3: Custom hooks that don't take input but return output

```scala
import japgolly.scalajs.react._

object MyCustomHook {

  // TODO: Replace
  val hook = CustomHook[Unit]
    .useEffectOnMount(Callback.log("HELLO!"))
    .buildReturning(_ => 123)

  object HooksApiExt {
    sealed class Primary[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]) {

      // TODO: Change hook name, and output type
      final def useMyCustomHook(implicit step: Step): step.Next[Int] =
        api.custom(hook)
    }
  }

  trait HooksApiExt {
    import HooksApiExt._

    // TODO: Change hook name so that it won't conflict with other custom hooks
    implicit def hooksExtMyCustomHook[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]): Primary[Ctx, Step] =
      new Primary(api)
  }

  object Implicits extends HooksApiExt
}
```

### Template 4: Custom hooks that don't take input or return output

```scala
import japgolly.scalajs.react._

object MyCustomHook {

  // TODO: Replace
  val hook = CustomHook[Unit]
    .useEffectOnMount(Callback.log("HELLO!"))
    .build

  object HooksApiExt {
    sealed class Primary[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]) {

      // TODO: Change hook name
      final def useMyCustomHook(implicit step: Step): step.Self =
        api.custom(hook)
    }
  }

  trait HooksApiExt {
    import HooksApiExt._

    // TODO: Change hook name so that it won't conflict with other custom hooks
    implicit def hooksExtMyCustomHook[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]): Primary[Ctx, Step] =
      new Primary(api)
  }

  object Implicits extends HooksApiExt
}
```

### Usage

By importing `MyCustomHook.Implicits._` users will be able to use your custom hook directly from the hooks API.

Example:

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import MyCustomHook.Implicits._

object Example {
  val Component = ScalaFnComponent.withHooks[Unit]
    .useMyCustomHook // Implicitly available
    .render($ =>
      <.div("MyCustomHook: ", $.hook1)
    )
}
```

# Escape hatches

If you really, really want to work with JS-style imperative hooks, you can!
But it's important to note that the onus is on you to ensure you use hooks correctly without violating React's rules.
If you use the escape hatch, scalajs-react won't be able to check that your code will always work.

In the hooks API, there's `.unchecked(body)` and `.uncheckedBy(ctx => body)` that you can use as an escape hatch,
and create hooks using React directly instead of using scalajs-react's hooks API.

Example:

```scala
package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.facade.{React => ReactJsFacade}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

object Example {
  val Component = ScalaFnComponent.withHooks[Unit]
    .unchecked {
      val count = ReactJsFacade.useState[Int](0)
      ReactJsFacade.useEffect(() => {
        document.title = s"You clicked ${count._1} times"
      })
      count
    }
    .render((_, count) =>
      <.div(
        <.p(s"You clicked ${count._1} times"),
        <.button(
          ^.onClick --> Callback(count._2(count._1 + 1)),
          "Click me"
        ),
      )
    )
}
```
