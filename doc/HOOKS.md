# React Hooks with scalajs-react

* Quickstart
* Built-in hooks
* Conventions

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
| `useRef()` | `.useRef[DomType]` |
| `useRef(initialValue)` | `.useRef(initialValue)` |
| `useState(initialState)` <br> `useState(() => initialState)` | `.useState(initialState)` |
| Custom hook with output <br> `const x = useBlah(i)` | `.custom(useBlah(i))`
| Custom hook without output <br> `useBlah(i)` | `.custom_(useBlah(i))`


## New hooks provided by scalajs-react

| Hook | Description |
| ---- | ----------- |
| `.localLazyVal(a)` | Creates a new `lazy val` on each render. |
| `.localVal(a)` | Creates a new `val` on each render. |
| `.localVar(a)` | Creates a new `var` on each render. |
| `.useForceUpdate` | Provides a `Reusable[Callback]` then when invoked, forces a re-render of the component. |
| `.useStateSnapshot(initialState)` <br> *(Requires import japgolly.scalajs.react.extra._)* | Same as `.useState` except you get a `StateSnapshot` (which accepts callbacks on set updates). |
| `.useStateSnapshotWithReuse(initialState)` <br> *(Requires import japgolly.scalajs.react.extra._)* | Same as `.useState` except you get a `StateSnapshot` (which accepts callbacks on set updates) with state `Reusability`. |
| `.useStateWithReuse(initialState)` | Conceptually `useState` + `shouldComponentUpdate`. Same as `useState` except that updates are dropped according to `Reusability`. |

## Hook chaining / dependencies

Sometimes hooks are initialised using props and/or the output of other hooks,
(which scalajs-react refers to as "context").
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

### Hook slots


