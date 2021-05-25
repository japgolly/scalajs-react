TODO
=========================================================================================

* Add useStateSnapshot as custom hook
* Tests
* Add documentation with examples
* Document tradeoffs
  * why not just (HookDsl.useXxx(...) => Unit) calls?
  * why DslMulti instead of Dsl{1,2,...}
* Weave `act` into all the `ReactTestUtils.{with,}render...` stuff?
* Changelog
* gh-pages demo


Old Notes
=========================================================================================

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
