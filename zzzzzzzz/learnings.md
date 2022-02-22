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
  * scalajs-react can insert the RR code. It should work with anything, not just vite.
  * The babel plugin cannot insert the RR code on the fly.
