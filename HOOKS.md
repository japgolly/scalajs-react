* const [count, setCount] = useState(0)
  Multiple uses from the same render probably legal, maybe common

  const [state, setState] = useState(() => {
    const initialState = someExpensiveComputation(props);
    return initialState;
  });


* useEffect -- componentDidMount, componentDidUpdate, and componentWillUnmount
  useEffect(() => { ??? });
  useEffect(
    () => {
      const subscription = props.source.subscribe();
      return () => { subscription.unsubscribe(); };
    },
    [props.source], // like shouldComponentUpdate for hooks
  );
  - For mount/unmount, pass an empty array ([]) as a second argument

* const value = useContext(MyContext);
  the value returned from React.createContext

* const [state, dispatch] = useReducer(reducer, initialArg?, init?);
  reducer :: (state, action) => newState
  dispatch :: js.Object / js.Any
  The initial state will be set to `init(initialArg)`

* useCallback
  const memoizedCallback = useCallback(
    () => { doSomething(a, b); },
    [a, b],
  );
  useCallback(fn, deps) is equivalent to useMemo(() => fn, deps)

* memoizedValue
  const memoizedValue = useMemo(() => computeExpensiveValue(a, b), [a, b]);
  Might be a good idea to return a `Reusable[Callback]`
  Might also be a good idea to safety a `Reusable[Callback]` to a JS-component expecting stable callbacks (eg. react-table)
  `Reusable[A]`?

* const refContainer = useRef(initialValue?)
  JS version of `Box[A]` with `.current`
  ```
  function TextInputWithFocusButton() {
    const inputEl = useRef(null);
    const onButtonClick = () => {
      // `current` points to the mounted text input element
      inputEl.current.focus();
    };
    return (
      <>
        <input ref={inputEl} type="text" /> <-------------------------------------------------------------------
        <button onClick={onButtonClick}>Focus the input</button>
      </>
    );
  }
  ```

* useImperativeHandle(ref, createHandle, [deps])

* useDebugValue(value, fn?)
  useDebugValue(date, date => date.toDateString())

* custom hooks
  * compose existing hooks

* useLayoutEffect
  same signature as useEffect

-----------------------------------------------------------------

* Weave `act` into all the `ReactTestUtils.{with,}render...` stuff?

* Rules
  * Only call Hooks at the top level. Don’t call Hooks inside loops, conditions, or nested functions.
  * Only call Hooks from React function components.


