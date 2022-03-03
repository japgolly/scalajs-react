Part 1: Hot-reloading
=====================

* This is enough to prevent vite hot loading. SJS has lots of this.

    ```js
    const sigh = 123
    export { sigh };
    ```

* Trying to register with RR via SJS doesn't work because the registration needs to happen when the JS module is loaded, not when the Scala object is initialised

* Adding the vite RR wrapper manually in SJS in the Scala object initialisation, works!

* Hot-reload doesn't update the components because Scala objects don't get reinitialised, thus the re-registration doesn't work.
  * Forcing re-evaluation causes re-registration, but the components on screen don't update (`$n_Ldemo_Counter$ = new $c_Ldemo_Counter$()`)

Next: Render `main.js` onto screen and see if a hot-update of App causes re-eval of anything in `main.js`. It probably won't.
      So then look at the Network tab and see what the hot-update code looks like and how it works. Re-registration doesn't seem to be enough to update the screen. (Or are we missing something in the re-registration code? Some kind of stable id or something?)

* Scala.js limitations
  * `@JSExportTopLevel` doesn't result in execution on-module-load, it just creates a downstream `export`
  * No way for code to declare module initialisation code, has to be done via sbt settings

* Debugging @react-refresh, comparing JS vs SJS:
  * in `computeFullKey()`:
    * JS has `fullKey`, SJS doesn't
    * SJS: `hooks` = `signature.getCustomHooks()` = `[]`
    * SJS: Sets `fullKey` to `ownKey`
  * Above doesn't matter cos `canPreserveStateBetween` ends up returning `true` for both
  * In `scheduleFibersWithFamiliesRecursively()`
    * `var family = resolveFamily(candidateType);`
      * JS: `family: {current: ƒ}`
      * SJS: `family: undefined`

* `typ` is always the content of `jsRender`, `k` & `v` are the content of `Component.raw__sjs_js_Any()`

* Got it working!!
  * Object's class needs to be in its own module
  * Object class module needs eager eval (eg. storing in a global var), Object's get method just needs to return the global var
  * On an update, only the object class's module must be updated, and not the object loader's
  * The babel plugin cannot insert the RR code on the fly. scalajs-react can insert the RR code, and it should work with anything, not just vite.

* RR babel doesn't transform components wrapped in an IIFE.
  i.e. `const App = (this$3 => p => {...})(this)` doesn't work but `const App = p => {...}` does

Solutions:
  1. Object eval changes
    1. Make change to Scala.js
    2. ~~maybe a babel plugin~~ This wont work. Babel can't split and create new files.
  2. Avoid IIFEs. Either a change to Scala.js or write a Babel plugin.
  3. Modify scalajs-react to emit RR code (controllable by flag, on by default when dev-mode & module mode, and tolerant to RR not being present)


Part 2: React Refresh injection
===============================

### How does is RR state calculated?

* what if the useState arg is a function?
  `useState(initialState())` is used by RR and it doesn't realise when the contents of `initialState` change

* `const [count, setCount] = useState(0)` is turned into `useState{[count, setCount](0)}`
* `const temp = useState(0); const [count, setCount] = temp` is turned into `useState{temp(0)}`

* custom hooks?
* class components?

### Can scalajs-react be modified to output code in a way that JS will inject RR?

* Will have to inline all the component builder DSL

* Will probably have to beta-expand the component function.
  `val test: js.Function0[Any] = () => { console.log("OMG") }`
  becomes
  `$t_Ldemo_Counter$__test = ((this$2) => (() => { console.log("OMG") }))(this)`


### Can scalajs-react be modified to inject RR itself?

I guess Quill has a single `quote { .. }` method that analyses the contents. Maybe the inner dsl doesn't even do anything, it's just there for the outer quote method to interpret. hmmmm....


Tomorrow's plan:
  * Remove all of this Inline stuff
  * create v2,v3 versions of Hook stuff (unmodified) and check in
  * start working with Scala v3 version by inlining
  * v2 version can be inlined by making so many methods macros (or not)
  * Test RR with different useState args - should result in different checksums


Potential Solutions:
  * RR injected by Babel
    * Macro on last builder dsl method that converts the entire expression into simple JS (Key seems to be: `(thiz: Expr[this.type]).asTerm.underlying`)
      * Could maybe create a new class, might workaround the SJS module issue
    * Inline enough parts of the builder dsl so that the output is enough for babel to work properly
  * RR injected by SJR
    * Encode RR state in a new type, and use it at compile-time in the last builder dsl method
    * Hash RR state in to a new value, and use it at runtime
