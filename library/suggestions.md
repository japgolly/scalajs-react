# Codebase Audit: Suggestions, Improvements & Optimisations

This document catalogues numbered findings from a full-codebase audit of `scalajs-react`, organized into three categories:
- **Section A: Bugs, Race Conditions, Resource Leaks & Edge Cases** (Items 1–26)
- **Section B: Performance & Memory Optimisations** (Items 27–49)
- **Section C: Code Quality, Cross-Version Deduplication, Build & Examples** (Items 50–64)

---

## Section A: Bugs, Race Conditions, Resource Leaks & Edge Cases

### 3. `AsyncCallback.delayMs` leaks JS `setTimeout` timers on cancellation and does not check `s.cancelably` on wake-up
- **Files**:
  - `callback/src/main/scala-2/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 1008–1016)
  - `callback/src/main/scala-3/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 1020–1028)
- **Issue**:
  ```scala
  def delayMs(milliseconds: Double): AsyncCallback[A] =
    if (milliseconds <= 0)
      this
    else
      new AsyncCallback(s => f => Callback {
        timers.setTimeout(milliseconds) {
          underlyingRepr(s)(f).runNow()
        }
      })
  ```
  1. When `s` is cancelled while the timer is pending (for example, `Async[AsyncCallback].sleep(1.hour)` in a Cats Effect `race` or `timeout`), `timers.clearTimeout` is never registered with `s.onCancel`, keeping the JS timer and its captured closures alive in memory until the full timeout expires.
  2. When the timer fires, if `this` does not check `s.cancelably` (e.g. `AsyncCallback.pure(x).delayMs(100)`), `underlyingRepr(s)(f).runNow()` still invokes `f(Success(x))` even after `s` has been cancelled and finalised.
- **Suggestion**:
  Guard scheduling and wake-up with `s.cancelably`, register a `clearTimeout` handler via `s.onCancel`, and call `s.unCancel(token)` when the timer fires:
  ```scala
  def delayMs(milliseconds: Double): AsyncCallback[A] =
    if (milliseconds <= 0)
      this
    else
      new AsyncCallback(s => f => s.cancelably(Callback {
        var handle: timers.SetTimeoutHandle = null
        val token = s.onCancel(AsyncCallback.delay {
          if (handle != null) timers.clearTimeout(handle)
        })
        handle = timers.setTimeout(milliseconds) {
          handle = null
          if (!s.finalised) s.unCancel(token)
          s.cancelably(underlyingRepr(s)(f)).runNow()
        }
      }))
  ```

---

### 4. `AsyncCallback.onCancel` mutates `State` eagerly during `Callback` construction rather than execution
- **Files**:
  - `callback/src/main/scala-2/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 1240–1246)
  - `callback/src/main/scala-3/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 1246–1252)
- **Issue**:
  ```scala
  def onCancel(f: AsyncCallback[Unit]): AsyncCallback[A] =
    new AsyncCallback(s => {
      val i = s.onCancel(f)
      val u = self.underlyingRepr(s)
      val uncancel = Callback(s.unCancel(i))
      g => u(t => Callback.suspend(Callback.unless(s.finalised)(uncancel >> g(t))))
    })
  ```
  `val i = s.onCancel(f)` runs as soon as `underlyingRepr(s)` is applied, before the continuation `g` is supplied and before the returned `Callback` is executed. In combinators like `zipWith` (`this.underlyingRepr(s)(...) >> that.underlyingRepr(s)(...)`), `that`'s `onCancel` handler is registered into `s` before `this` even starts running (and even if `this` synchronously cancels `s` before `that` is reached).
- **Suggestion**:
  Defer `s.onCancel(f)` until the returned `Callback` is actually executed inside `s.cancelably`:
  ```scala
  def onCancel(f: AsyncCallback[Unit]): AsyncCallback[A] =
    new AsyncCallback(s => g => s.cancelably(Callback.suspend {
      val i = s.onCancel(f)
      self.underlyingRepr(s)(t =>
        Callback.suspend(Callback.unless(s.finalised)(Callback(s.unCancel(i)) >> g(t)))
      )
    }))
  ```

---

### 5. `AsyncCallback.when` evaluates `cond` eagerly during `Callback` construction and bypasses `s.cancelably`
- **Files**:
  - `callback/src/main/scala-2/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 867–868)
  - `callback/src/main/scala-3/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 877–878)
- **Issue**:
  ```scala
  def when(cond: => Boolean): AsyncCallback[Option[A]] =
    new AsyncCallback(s => f => if (cond) underlyingRepr(s)(ea => f(ea.map(Some(_)))) else f(Success(None)))
  ```
  Because `if (cond)` is outside the returned `Callback`, in `zipWith` (`this.underlyingRepr(s)(...) >> that.underlyingRepr(s)(...)`), `that.when(cond)` evaluates `cond` *before* `this` executes. Additionally, if `cond` throws an exception, it escapes `underlyingRepr` synchronously instead of failing the `AsyncCallback`, and does not check `s.cancelably`.
- **Suggestion**:
  Evaluate `cond` inside `s.cancelably`:
  ```scala
  def when(cond: => Boolean): AsyncCallback[Option[A]] =
    new AsyncCallback(s => f => s.cancelably(
      CallbackTo(catchAll(cond)).flatMap {
        case Success(true)  => underlyingRepr(s)(ea => f(ea.map(Some(_))))
        case Success(false) => f(Success(None))
        case Failure(t)     => f(Failure(t))
      }
    ))
  ```

---

### 6. `AsyncCallback.traverse` evaluates `f(a)` eagerly without `suspend` and builds $O(N)$ nested `zipWith` state objects
- **Files**:
  - `callback/src/main/scala-2/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 185–201)
  - `callback/src/main/scala-3/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 186–202)
- **Issue**:
  1. `as.indices.iterator.map(i => f(as(i)).map(b => bs(i) = b)).reduce(_.zipWith(_)(discard))` evaluates `f(as(i))` outside `AsyncCallback.suspend`. If `f` throws a synchronous exception on element `k`, it aborts before any of the `0 .. k-1` `AsyncCallback`s are run, unlike `traverse_` (line 253) which wraps `AsyncCallback.suspend(f(a))`.
  2. `.reduce(_.zipWith(_)(discard))` constructs a left-leaning binary tree of $N - 1$ nested `zipWith` combinators, allocating $3(N - 1)$ mutable `Option` variables and $3(N - 1)$ `Callback` closures, and creating an $O(N)$ call stack depth when synchronous tasks complete.
- **Suggestion**:
  Implement `traverse` using the same flat array + latch/counter strategy as `traverse_` with `AsyncCallback.suspend(f(a))`.

---

### 7. `AsyncCallback.firstS` does not check `s.cancelably`
- **Files**:
  - `callback/src/main/scala-2/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 126–135)
  - `callback/src/main/scala-3/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 127–136)
- **Issue**:
  `firstS` (used by `first` and `race`) does not wrap its start or completion callback in `s.cancelably(...)`. If `s` is cancelled before the winner completes, `g(ea)` is still invoked.
- **Suggestion**:
  Wrap both the outer `Callback` and `g(ea)` in `s.cancelably(...)`.

---

### 8. `CallbackCatsEffect.IOToAsyncCallback` hangs outer `uncancelable` block if an `IO` self-cancels via `poll(IO.canceled)` while `s.masks > 0`
- **File**: `callbackExtCatsEffect/src/main/scala/japgolly/scalajs/react/callback/CallbackCatsEffect.scala` (lines 173–186)
- **Issue**:
  In the `s.masks > 0` branch of `IOToAsyncCallback`:
  ```scala
  var res: Either[Throwable, A] = null
  val maskedIO = IO.uncancelable(_ => f0.attempt.map { r => res = r }).flatMap(_ => IO.unit)
  maskedIO.unsafeRunAsyncOutcome {
    case Outcome.Canceled() =>
      s.cancelled = true
      if (res ne null)
        s.cancelably(k(res.toTry)).runNow()
  ```
  If `f0` internally uses `IO.uncancelable(poll => poll(IO.canceled))` (self-cancelling at an inner `poll`), `f0` cancels before `res = r` is reached, so `res` remains `null`. Because `s.masks > 0` in the outer `AsyncCallback`, the outer `AsyncCallback` is in an uncancelable region and expects `k` to be called so it can finish its uncancelable block and decrement `s.masks`, yet `if (res ne null)` prevents `k` from ever being called, hanging the outer `AsyncCallback` forever.

---

### 9. `EffectCatsEffect.EffectIO.finallyRun` discards suppressed exceptions when both `fa` and `fb` fail
- **File**: `coreExtCatsEffect/src/main/scala/japgolly/scalajs/react/util/EffectCatsEffect.scala` (lines 63–66)
- **Issue**:
  ```scala
  override def finallyRun[A, B](fa: => IO[A], fb: => IO[B]) =
    suspend(fa).attempt.flatMap(ta =>
    suspend(fb).attempt.flatMap(tb =>
    IO.fromEither(if (ta.isRight && tb.isLeft) Left(tb.left.getOrElse(null)) else ta)))
  ```
  1. When both `fa` and `fb` fail, `tb`'s exception is silently dropped without `e1.addSuppressed(e2)`.
  2. When `fa` succeeds (`Right`), `suspend(fb)` is still wrapped in `.attempt` and `ta.isRight && tb.isLeft` is checked, allocating unnecessary `Either` wrappers on the happy path.
- **Suggestion**:
  ```scala
  override def finallyRun[A, B](fa: => IO[A], fb: => IO[B]): IO[A] =
    suspend(fa).attempt.flatMap {
      case r @ Right(_) => suspend(fb) *> IO.fromEither(r)
      case Left(e1) =>
        suspend(fb).attempt.flatMap {
          case Right(_) => IO.raiseError(e1)
          case Left(e2) => if (e1 ne e2) e1.addSuppressed(e2); IO.raiseError(e1)
        }
    }
  ```

---

### 10. `React.Suspense` calls `React.lazy` inside render, unmounting and resetting child state on every parent re-render
- **File**: `coreGeneric/src/main/scala/japgolly/scalajs/react/React.scala` (lines 103–126)
- **Issue**:
  `React.Suspense(fallback, asyncBody)` calls `facade.React.lazy(lazyFn)` every time the `VdomElement` is constructed. In React, `React.lazy` creates a new component type identity (`LazyComponent`) and must be declared outside of render. Calling it during render causes React to see a brand-new component type on every parent re-render, unmounting the previously rendered subtree, destroying its state, and re-running `asyncBody` from scratch.
- **Suggestion**:
  Add a `React.Suspense(fallback: VdomNode, children: VdomNode*): VdomElement` overload that wraps existing children (such as a pre-created `React.lazy` component or React 19 `use()`-suspending component) without creating a new `React.lazy` wrapper on every render.

---

### 11. `Reusability.TemporalImplicitsWithoutTolerance` causes an ambiguous implicit error for `LocalDate`
- **File**: `coreGeneric/src/main/scala/japgolly/scalajs/react/Reusability.scala` (lines 318, 474)
- **Issue**:
  `object Reusability` already defines `implicit def localDate: Reusability[LocalDate]` (line 318). `TemporalImplicitsWithoutTolerance` also defines `implicit def reusabilityLocalDate: Reusability[LocalDate] = localDate` (line 474). Importing `Reusability.TemporalImplicitsWithoutTolerance._` makes `Reusability[LocalDate]` fail to compile due to ambiguous implicits.
- **Suggestion**:
  Remove `reusabilityLocalDate` from `TemporalImplicitsWithoutTolerance` (or make it a non-implicit forwarder if binary/source compatibility requires the method name).

---

### 12. `ViewTransitionBuilder` shares a single mutable JS `props` object across builder calls
- **File**: `coreGeneric/src/main/scala/japgolly/scalajs/react/feature/ViewTransition.scala` (lines 23–135)
- **Issue**:
  `ViewTransitionBuilder` mutates a single `private val props: facade.ViewTransitionProps = (new js.Object).asInstanceOf[facade.ViewTransitionProps]` in place and returns `this`. If a user stores a base configuration in a `val` and reuses it for two elements (e.g., `val base = React.ViewTransition.enter("fade"); base.name("a")(el1); base.name("b")(el2)`), both mutate the same underlying JS props object.
- **Suggestion**:
  Clone the JS object (`js.Object.assign(new js.Object, props)`) on mutation or construct a fresh `ViewTransitionProps` inside `apply(children: VdomNode*)`.

---

### 13. `Hooks.UseStateF.xmap` and `withReusability` reset custom effect types `F[_]` back to `DefaultEffects.Sync`
- **File**: `coreGeneric/src/main/scala/japgolly/scalajs/react/hooks/Hooks.scala` (lines 297–300, 352–380)
- **Issue**:
  On `UseStateF[F[_], S]`, `def xmap[T](f: S => T)(g: T => S): UseState[T]` and `def withReusability: UseState[S]` hardcode the return type to `UseState` (`UseStateF[DefaultEffects.Sync, *]`) instead of `UseStateF[F, *]` (unlike `UseStateWithReuseF.xmap` on line 324, which preserves `F`).
- **Suggestion**:
  Parameterize `UseState.xmap` and `UseState.withReusability` over `F[_]` using `self.F` so they return `UseStateF[F, T]` and `UseStateF[F, S]`.

---

### 14. `Api.Primary.useEffectEvent` and `useActionState` accept parameters by-value instead of by-name
- **File**: `coreGeneric/src/main/scala/japgolly/scalajs/react/hooks/Api.scala` (lines 149–158, 609–640)
- **Issue**:
  1. `final def useEffectEvent[A](callback: A)` (line 151) takes `callback` by-value, evaluating it once at component definition time rather than per render.
  2. `final def useActionState[S, ...](initialState: S, ...)` (lines 609–640) takes `initialState: S` by-value, unlike `useState(initialState: => S)` and `useReducer(..., initialState: => S)` which evaluate `initialState` by-name per component mount.
  3. Additionally, `UseActionStateF[F, S, P]` (`Hooks.scala:533-551`) lacks a zero-arg `dispatch(): F[Unit]` helper when `P = Unit`, even though `UseActionState` has overloads specifically producing `UseActionState[S, Unit]`.
- **Suggestion**:
  Change `callback: A` to `callback: => A` in `useEffectEvent`, change `initialState: S` to `initialState: => S` in `useActionState`, and add `def dispatch()(implicit ev: Unit =:= P): F[Unit] = dispatch(ev(()))` to `UseActionStateF`.

---

### 15. `Px.ThunkA` (`.autoRefresh` and `Derivative.withReuse`) evaluates the underlying thunk twice on its very first `.value()` call
- **File**: `extra/src/main/scala/japgolly/scalajs/react/extra/Px.scala` (lines 79–82, 120–126, 135–136)
- **Issue**:
  ```scala
  private final class ThunkA[A](next: () => A, protected val reusability: Reusability[A]) extends Root[A](next) {
    override def value() = {
      setMaybe(next())
      _value()
    }
  }
  ```
  When a `ThunkA` is created, `__value` is an uninitialized `LazyVar(next)`. On the very first call to `value()`:
  1. `setMaybe(next())` evaluates `next()` (1st evaluation).
  2. Inside `setMaybe`: `if (!reusability.test(_value(), a))` calls `_value()` -> `__value.get()`, which sees `init ne null` and evaluates `next()` a second time (2nd evaluation), then tests `reusability` between the two evaluations!
- **Suggestion**:
  Add `def isInitialized: Boolean = init eq null` to `LazyVar` (and expose `protected final def isInitialized: Boolean` on `Px.Root`), then in `ThunkA.value()`:
  ```scala
  override def value() = {
    if (isInitialized) setMaybe(next())
    _value()
  }
  ```

---

### 16. `Px.FlatMap` revision tracking and lazy initialization inconsistencies
- **File**: `extra/src/main/scala/japgolly/scalajs/react/extra/Px.scala` (lines 187–235)
- **Issue**:
  1. In `Px.FlatMap._init()` (lines 198–203), `val inner = f(a)` is created and `_innerBaseRev = inner.rev` is recorded without calling `inner.value()`. Similarly, in `_updateValueIfChanged()` (lines 212–221), `_inner = f(a)` and `_innerBaseRev = _inner.rev` are recorded before `_inner.value()` is called in `value()`. If `_inner` is an uninitialized `Px.Map` or `Px.FlatMap` whose revision only advances when `.value()` is evaluated, `_innerBaseRev` captures the pre-initialization `rev` (`0`), causing `innerDelta` (`_inner.rev - _innerBaseRev`) to spuriously jump as soon as `_inner.value()` is called!
  2. In `Px.FlatMap.rev` (lines 224–229), `xa.rev` and `_inner.rev` are read without calling `_updateValueIfChanged()`, whereas `Px.Map.rev` (line 169) only returns `_revA()` (which is only updated when `_updateValueIfChanged()` runs). Thus, if `xa` or `_inner` is a `Px.Map`, reading `flatMapPx.rev` before `flatMapPx.value()` sees stale `xa.rev` / `_inner.rev` values.
- **Suggestion**:
  Initialize `inner.value()` inside `_init()` and `_updateValueIfChanged()` before recording `_innerBaseRev = inner.rev`, and call `_updateValueIfChanged()` inside `FlatMap.rev` and `Map.rev`.

---

### 17. `AjaxF.asAsyncCallback` resolves with `Success(xhr)` (status 0) instead of failing on network error, timeout, or abort
- **File**: `extra/src/main/scala/japgolly/scalajs/react/extra/internal/AjaxF.scala` (lines 118–125, 178–198)
- **Issue**:
  In `asAsyncCallback`:
  - `complete` is wrapped in `onreadystatechange`, which checks `if (xhr.readyState == XMLHttpRequest.DONE)` and calls `cc(Success(xhr))`.
  - `onerror` is registered to call `cc(Failure(AjaxException(xhr)))`.
  - `ontimeout` and `onabort` do not call `cc(Failure(...))` at all!
  In the XMLHttpRequest specification, when a network error, timeout, or abort occurs, `readyState` transitions to `DONE` (4) and fires `onreadystatechange` *before* `onerror`, `ontimeout`, or `onabort` fires! Because `asAsyncCallback` uses `A.first`, `onreadystatechange` wins the race and completes the `AsyncCallback` with `Success(xhr)` (where `xhr.status == 0`), causing `onerror` to be ignored and `ontimeout`/`onabort` to never fail the `AsyncCallback`.
- **Suggestion**:
  In `asAsyncCallback`, also fail `cc` with `Failure(AjaxException(xhr))` in `ontimeout` and `onabort`, and in `onreadystatechange` do not call `cc(Success(xhr))` when `xhr.status == 0` if the request errored/timed out/aborted (or trigger `cc(Success(xhr))` from `onload` rather than `onreadystatechange`).

---

### 18. `Router`: `BaseUrl.fromWindowOrigin` mishandles HTTPS default port and IPv6, and compiles a regex
- **File**: `extra/src/main/scala/japgolly/scalajs/react/extra/router/Urls.scala` (lines 40–46)
- **Issue**:
  ```scala
  def fromWindowOrigin: BaseUrl = {
    val l = dom.window.location
    var url = l.protocol + "//" + l.hostname
    if (!l.port.matches("^(?:80)?$"))
      url += ":" + l.port
    BaseUrl(url)
  }
  ```
  `l.port` in the DOM Location API is already `""` when using the default port for the protocol (`80` for HTTP, `443` for HTTPS). Checking `!l.port.matches("^(?:80)?$")` compiles a regex and wrongly strips `:80` if an HTTPS URL explicitly uses port 80, while `l.hostname` omits IPv6 brackets in legacy environments compared to `l.origin` or `l.host`.
- **Suggestion**:
  Use `dom.window.location.origin` (or `if (l.port.isEmpty) l.protocol + "//" + l.hostname else l.protocol + "//" + l.hostname + ":" + l.port`):
  ```scala
  def fromWindowOrigin: BaseUrl =
    BaseUrl(dom.window.location.origin)
  ```

---

### 19. `Router`: `Dsl.queryToSeq` throws uncaught `URIError` on malformed percent-encoding in the browser URL
- **File**: `extra/src/main/scala/japgolly/scalajs/react/extra/router/Dsl.scala` (lines 422–436)
- **Issue**:
  `decodeURIComponent(str.replace('+', ' '))` throws a `js.JavaScriptException(URIError: URI malformed)` if the browser URL query string contains an incomplete `%` sequence (e.g., `?q=100%`). Because the router parses the window URL on mount, a malformed query string in the address bar crashes the entire `Router` component with an unhandled exception instead of failing route matching (`None`) and triggering the `notFound` handler.
- **Suggestion**:
  Catch `js.JavaScriptException` around `decodeURIComponent` in `queryToSeq` (or return `Option[Seq[(String, String)]]`) so malformed URLs fail route matching gracefully.

---

### 20. `Router`: `Dsl.int` and `Dsl.long` throw `NumberFormatException` on integer overflow instead of returning `None`
- **File**: `extra/src/main/scala/japgolly/scalajs/react/extra/router/Dsl.scala` (lines 281–282)
- **Issue**:
  ```scala
  val int  = new RouteB[Int] ("(-?\\d+)", 1, g => Some(g(0).toInt),  _.toString)
  val long = new RouteB[Long]("(-?\\d+)", 1, g => Some(g(0).toLong), _.toString)
  ```
  Because `(-?\d+)` matches arbitrarily long digit strings (e.g. `/item/99999999999999999999`), `g(0).toInt` and `g(0).toLong` throw an uncaught `NumberFormatException` during route parsing instead of returning `None` (404 / non-match).
- **Suggestion**:
  Use `toIntOption` and `toLongOption`:
  ```scala
  val int  = new RouteB[Int] ("(-?\\d+)", 1, _(0).toIntOption,  _.toString)
  val long = new RouteB[Long]("(-?\\d+)", 1, _(0).toLongOption, _.toString)
  ```

---

### 21. `OnUnmountF.unmount` fails to clear `unmountProcs` if an unmount callback throws an exception
- **File**: `extra/src/main/scala/japgolly/scalajs/react/extra/OnUnmount.scala` (lines 15–16)
- **Issue**:
  ```scala
  final def unmount: F[Unit] =
    F.chain(F.sequence_(unmountProcs), F.delay{ unmountProcs = Nil })
  ```
  If any unmount callback throws, `F.delay { unmountProcs = Nil }` never runs, leaking the list of unmount closures in memory.
- **Suggestion**:
  Clear `unmountProcs` before running the captured list:
  ```scala
  final def unmount: F[Unit] =
    F.suspend {
      val procs = unmountProcs
      unmountProcs = Nil
      F.sequence_(procs)
    }
  ```

---

### 22. `ConsoleHijack.Internals.vsprintf` throws `ReferenceError: window is not defined` in non-browser JS environments and pollutes `window`
- **File**: `util/src/main/scala/japgolly/scalajs/react/util/ConsoleHijack.scala` (lines 123–130)
- **Issue**:
  `vsprintf` evaluates a minified `sprintf.js` snippet ending with `window.vsprintf`. In Node.js/Worker environments without a global `window` object, this throws `ReferenceError`, and in browser environments it leaks `window.sprintf__scalajsreact` and `window.vsprintf__scalajsreact` onto the global `window` object.
- **Suggestion**:
  Return `e` (`vsprintf`) directly from the IIFE inside `js.eval` without referencing `window` or `exports`.

---

### 23. `WithDsl.attemptFuture` in `testUtil` only catches `Exception`, leaking mounted DOM nodes on `AssertionError`
- **File**: `testUtil/src/main/scala/japgolly/scalajs/react/test/internal/WithDsl.scala` (lines 22–23, 72–75)
- **Issue**:
  ```scala
  def attemptFuture[A](f: => Future[A]): Future[A] =
    try f catch { case err: Exception => Future.failed(err) }
  ```
  Test assertion failures throw `java.lang.AssertionError`, which extends `java.lang.Error` (a `Throwable`, not an `Exception`). If a synchronous assertion before the `Future` fails inside `withRendered(...).future { ... }`, `attemptFuture` does not catch `AssertionError`, so `.andThen { case _ => cleanup() }` never runs and the mounted React root / DOM container is leaked into `document.body` for subsequent tests.
- **Suggestion**:
  Catch `Throwable` instead of `Exception`:
  ```scala
  def attemptFuture[A](f: => Future[A]): Future[A] =
    try f catch { case err: Throwable => Future.failed(err) }
  ```

---

### 24. `Resource.flatMap` in `testUtil` stores mutable `aOpt` and `bReleaseOpt` fields on the `Resource` definition rather than per `.use` invocation
- **File**: `testUtil/src/main/scala/japgolly/scalajs/react/test/internal/Resource.scala` (lines 18–39)
- **Issue**:
  `flatMap` allocates `var aOpt: Option[A] = None` and `var bReleaseOpt: Option[B => F[Unit]] = None` outside `acquire`, sharing mutable state across all invocations of a `Resource` instance (such as `ReactTestUtils.withReactRoot`).
- **Suggestion**:
  Define `Resource` directly in terms of `def use[B](f: A => F[B]): F[B]` (using `F.finallyRun`), which makes `map` and `flatMap` completely stateless and re-entrant.

---

### 25. `SimEvent.MouseLike.assign` unconditionally assigns `tgt.defaultPrevented`, and `Simulation` defaults differ from `Simulate`
- **Files**:
  - `testingLibraryDom/src/main/scala/japgolly/scalajs/react/testing_library/dom/SimEvent.scala` (lines 298–301)
  - `testingLibraryDom/src/main/scala/japgolly/scalajs/react/testing_library/dom/Simulation.scala` (lines 36–85)
- **Issue**:
  1. In `SimEvent.MouseLike.assign`:
     ```scala
     tgt.defaultPrevented = defaultPrevented
     if (readOnlyProperties) {
       tgt.defaultPrevented = defaultPrevented
     }
     ```
     Line 298 assigns `tgt.defaultPrevented` unconditionally outside `if (readOnlyProperties)`.
  2. In `Simulation.scala`, `def click = Simulation(Simulate.click(_, SimEvent.Mouse()))` passes `SimEvent.Mouse()` (where `buttons = 0`), whereas `Simulate.click(node)` defaults to `ReactEventType.Mouse.defaultEventData` (where `buttons = 1`). Similarly, `Simulation.keyDown` uses `key = ""` instead of `"Unidentified"`, and `Simulation.pointerDown` uses `pressure = 0, isPrimary = false` instead of `pressure = 0.5, isPrimary = true`.
- **Suggestion**:
  Remove the duplicate line 298 in `SimEvent.scala`, and in `Simulation.scala` call `Simulate.click(_)` (without the second argument) so `Simulation` and `Simulate` use identical default event data.

---

### 26. `ProhibitDefaultEffects` Scalafix rule fails to detect `DefaultEffects` when applied to a type argument (`Type.Apply`)
- **Files**:
  - `scalafixRules/src/main/scala/sjr/ProhibitDefaultEffects.scala` (lines 33–38)
  - `scalafixRules/src/main/scala/sjr/Util.scala` (lines 6–24)
- **Issue**:
  In Scalameta SemanticDB, `t.symbol` on a `Type.Apply(tpe, args)` (such as `DefaultEffects.Sync[Unit]` or `Option[DefaultEffects.Sync[Unit]]`) is `Symbol.None`; the symbol is attached to the inner `Type.Select` / `Type.Name` `tpe`. Thus `checkExplicit(t: Type)` only catches unapplied higher-kinded references like `F[_] = DefaultEffects.Sync` and misses `DefaultEffects.Sync[Unit]`. Also, `Util.isEffectTC`, `Util.effectTCType`, and `Util.isImplicit` in `Util.scala` are unused dead code.
- **Suggestion**:
  Inspect all nested `Type` nodes in `checkExplicit`:
  ```scala
  private def checkExplicit(t: Type)(implicit doc: SemanticDocument): Patch =
    t.collect {
      case sub: Type if isDefaultEffect(sub.symbol) =>
        Patch.lint(DefaultEffectDetected(sub.pos))
    }.asPatch
  ```

---

## Section B: Performance & Memory Optimisations

### 27. `VdomBuilder.ToJs` / `ToRawReactElement` allocates 7 function closures, an unused `styles` `js.Object`, and calls `Object.keys` on every DOM element
- **File**: `coreGeneric/src/main/scala/japgolly/scalajs/react/vdom/VdomBuilder.scala` (lines 10–20, 48–49, 68–168)
- **Issue**:
  Every DOM element created via `<.div(...)`, `<.span(...)`, etc. instantiates `new VdomBuilder.ToRawReactElement()`, which:
  1. Allocates **7 closure objects** (`addAttr`, `addClassName`, `addEventHandler`, `addStyle`, `addStylesObject`, `appendChild`, `setKey` are declared as `val` functions on `VdomBuilder` and overridden with lambdas in `ToJs`).
  2. Eagerly allocates `var styles = new js.Object` and in `addStyleToProps` calls `js.Object.keys(styles).length != 0` (which allocates a JS key array in the JS engine) even when the element has zero inline styles (>90% of DOM elements).
  3. Calls `facade.React.createElement(tag, props, children.toSeq: _*)` (line 166), wrapping `children: js.Array[RawChild]` in a `js.WrappedArray` and unpacking varargs on every element.
- **Suggestion**:
  1. Change the 7 members of `VdomBuilder` from `val` to `def` so `ToJs` implements them as methods with zero closure allocations.
  2. Lazily allocate `styles: js.Object = null` on first `addStyle`/`addStylesObject` call and check `if (styles ne null) setProp("style", styles)` instead of calling `js.Object.keys(styles)`.
  3. Fast-path 0, 1, and 2 children in `ToRawReactElement.build` before falling back to `children.toSeq: _*`.

---

### 28. `TagOf.rawElement` allocates a temporary `js.Array` on every DOM element render
- **File**: `coreGeneric/src/main/scala/japgolly/scalajs/react/vdom/TagOf.scala` (lines 32–48)
- **Issue**:
  `TagOf` stores `modifiers: List[Seq[TagMod]]`. Even when a tag is called once (`<.div(^.cls := "a", "text")`, where `modifiers` is a 1-element list `xs :: Nil`) or zero times (`<.br`, where `modifiers` is `Nil`), `rawElement` allocates `new js.Array[Seq[TagMod]]`, copies `modifiers` into it, and loops over it.
- **Suggestion**:
  Pattern-match `modifiers`:
  ```scala
  modifiers match {
    case Nil       => ()
    case xs :: Nil => xs.foreach(_.applyTo(b))
    case _         => ...
  }
  ```
  This eliminates the `js.Array` allocation in >99% of DOM tags.

---

### 29. `Attr.ValueType.apply` allocates an extra `b.addAttr(name, _)` closure on every attribute assignment
- **Files**:
  - `coreGeneric/src/main/scala-2/japgolly/scalajs/react/vdom/Attr.scala` (lines 243–268)
  - `coreGeneric/src/main/scala-3/japgolly/scalajs/react/vdom/Attr.scala` (lines 259–284)
- **Issue**:
  ```scala
  final class ValueType[-A, +U](val fn: ValueType.Fn[A]) extends AnyVal {
    def apply(name: String, value: A): TagMod =
      TagMod.fn(b => fn(b.addAttr(name, _), value))
  }
  ```
  When `applyTo(b)` runs for any attribute (`^.id := "foo"`, `^.tabIndex := 1`, etc.), `b.addAttr(name, _)` allocates a temporary `Function1[js.Any, Unit]` closure capturing `b` and `name`, passes it to `fn`, which immediately invokes it once.
- **Suggestion**:
  Specialize `ValueType.direct` and `ValueType.string` (or provide a fast-path in `ValueType.apply` when `fn eq ValueType.direct.fn`) to return `TagMod.fn(_.addAttr(name, value.asInstanceOf[js.Any]))` directly without allocating the inner `b.addAttr(name, _)` closure.

---

### 30. `RoutingRulesF.fromRule` has $O(N^2)$ list allocation per URL parse/action lookup and allocates `StaticOrDynamic` objects for every non-matching route
- **File**: `extra/src/main/scala/japgolly/scalajs/react/extra/router/RoutingRulesF.scala` (lines 130–170)
- **Issue**:
  In `RoutingRulesF.fromRule`:
  ```scala
  case r: RoutingRule.Or[Page, Props] =>
    val x = prepareParseFn(r.lhs)
    val y = prepareParseFn(r.rhs)
    p => y(p).reverse_:::(x(p))
  ```
  When $N$ routes are chained with `|` (`r1 | r2 | ... | rN`), `Or` forms a left-leaning tree of depth $N$. On every URL parse or page action lookup:
  1. Every leaf rule allocates a `StaticOrDynamic(Right(None))` and a 1-element `List` even when the route does not match.
  2. At each of the $N - 1$ `Or` nodes, `y(p).reverse_:::(x(p))` copies and prepends the entire accumulated list `x(p)` of size $1, 2, \dots, N-1$, resulting in $\frac{N(N+1)}{2}$ list cell allocations per URL check!
- **Suggestion**:
  Flatten `RoutingRule.Or` chains into an `Array` or `Vector` of leaf rules once inside `fromRule`, and only collect matching routes (`Some`) into the result list, reducing non-matching route allocations to zero and making route resolution $O(N)$ instead of $O(N^2)$.

---

### 31. `Urls.PathLike.endWith_/` and `rtrim_/` compile regular expressions on every path operation
- **File**: `extra/src/main/scala/japgolly/scalajs/react/extra/router/Urls.scala` (lines 16–17)
- **Issue**:
  ```scala
  final def endWith_/ : Self = map(_.replaceFirst("/*$", "/"))
  final def rtrim_/   : Self = map(_.replaceFirst("/+$", ""))
  ```
  `String#replaceFirst` compiles a `java.util.regex.Pattern` on every call. Because `PathLike./` calls `endWith_/`, every `baseUrl / "path"` call compiles a regex at runtime.
- **Suggestion**:
  Implement `rtrim_/` and `endWith_/` with a simple character index loop:
  ```scala
  final def rtrim_/ : Self = map { s =>
    var i = s.length
    while (i > 0 && s.charAt(i - 1) == '/') i -= 1
    if (i == s.length) s else s.substring(0, i)
  }
  final def endWith_/ : Self = map { s =>
    if (s.endsWith("/") && !s.endsWith("//")) s
    else {
      var i = s.length
      while (i > 0 && s.charAt(i - 1) == '/') i -= 1
      s.substring(0, i) + "/"
    }
  }
  ```

---

### 32. `Trampoline.map` allocates two closures (`f.andThen(new Pure(_))`) per call
- **File**: `util/src/main/scala/japgolly/scalajs/react/util/Trampoline.scala` (lines 15–16)
- **Issue**:
  ```scala
  final def map[B](f: A => B): Trampoline[B] =
    new FlatMap(this, f.andThen(new Pure(_)))
  ```
  `f.andThen(new Pure(_))` allocates both a `Function1` for `new Pure(_)` and a `Function1$$Lambda` inside `andThen` on every `CallbackTo#map` call.
- **Suggestion**:
  Use a single lambda `a => new Pure(f(a))`:
  ```scala
  final def map[B](f: A => B): Trampoline[B] =
    new FlatMap(this, a => new Pure(f(a)))
  ```

---

### 33. `AsyncCallback.map`, `void`, and `ret` allocate unnecessary `flatMap` and `AsyncCallback.pure` layers
- **Files**:
  - `callback/src/main/scala-2/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 162–163, 679–680, 778–783)
  - `callback/src/main/scala-3/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 163–164, 689–690, 788–793)
- **Issue**:
  1. `map` is implemented as `flatMap(a => AsyncCallback.pure(f(a)))`, which on every completion allocates a `Success(f(a))` and a new `AsyncCallback` instance via `pure`, then unpacks its `underlyingRepr`.
  2. `void` is implemented as `map(_ => ())`, which allocates two closures and a fresh `AsyncCallback.pure(())` on every execution instead of chaining `this >> AsyncCallback.unit`.
  3. On line 163, `val unit: AsyncCallback[Unit] = pure(())` is initialized *before* `private lazy val tryUnit = Try(())` (line 310), allocating duplicate `Success(())` instances.
- **Suggestion**:
  Implement `map` directly over `underlyingRepr(s)`, define `def void: AsyncCallback[Unit] = this >> AsyncCallback.unit`, and initialize `val unit: AsyncCallback[Unit] = const(tryUnit)`.

---

### 34. `SyncPromise` uses an immutable `List` with `:+=` ($O(N^2)$ listener registration) and boxes `Try[A]` in `Option`
- **File**: `callback/src/main/scala/japgolly/scalajs/react/callback/SyncPromise.scala` (lines 7–40)
- **Issue**:
  `callbacks :+= f` appends to the end of an immutable `List`, copying the entire list on each `onComplete` call ($O(N^2)$ when multiple tasks await a `SyncPromise`, `Barrier`, `CountDownLatch`, or `memo()`). Also, `private var result = Option.empty[Try[A]]` allocates an extra `Some` wrapper around `Try[A]`.
- **Suggestion**:
  Store `private var result: Try[A] = null` and `private var callbacks: js.Array[Try[A] => Callback] = null` (lazily allocated only if `onComplete` is called before `complete`, and cleared to `null` upon completion).

---

### 35. `AsyncCallback.State.unCancel` never shrinks `cancelCallbacks`, leaking memory in `tailrec` loops
- **Files**:
  - `callback/src/main/scala-2/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 52–60)
  - `callback/src/main/scala-3/japgolly/scalajs/react/callback/AsyncCallback.scala` (lines 53–61)
- **Issue**:
  ```scala
  def onCancel(f: AsyncCallback[Unit]): Int = {
    cancelCallbacks.addOne(f.underlyingRepr)
    cancelCallbacks.length - 1
  }

  def unCancel(i: Int): Unit =
    cancelCallbacks(i) = ()
  ```
  `unCancel` sets `cancelCallbacks(i) = ()` (`undefined`) without shrinking the array. In a `tailrec` loop where each step registers and unregisters an `onCancel` token (such as `IOToAsyncCallback` or `delayMs`), `s.cancelCallbacks` grows unboundedly with `undefined` entries.
- **Suggestion**:
  Pop trailing `undefined` entries in `unCancel`:
  ```scala
  def unCancel(i: Int): Unit = {
    cancelCallbacks(i) = ()
    while (cancelCallbacks.nonEmpty && cancelCallbacks(cancelCallbacks.length - 1).isEmpty)
      cancelCallbacks.pop()
  }
  ```

---

### 36. `JsUtil.runPromiseAsync` allocates an unnecessary `js.Promise` on every promise completion
- **File**: `util/src/main/scala/japgolly/scalajs/react/util/JsUtil.scala` (lines 125–138)
- **Issue**:
  Inside `runPromiseAsync`, `next(ta)` calls `val (p, pc) = newPromise[Unit]()`, immediately completes it with `pc(Try(complete(ta)()))()`, and returns `p` to `pa.then(...)` (whose return value is ignored). Since `complete(ta)()` returns `Unit` (which satisfies `Unit | js.Thenable[Unit]`), allocating a `new js.Promise` and 3 closures on every single `fromJsPromise` resolution is redundant.
- **Suggestion**:
  Call `complete(Success(a))()` and `complete(Failure(...))()` directly in `ok` and `ko`, and wrap `pa` evaluation in `try/catch` in case evaluating the by-name `pa` throws synchronously.

---

### 37. `EffectCatsEffect.AsyncIO.async_` allocates two `js.Promise` instances instead of using `IO.async` directly
- **File**: `coreExtCatsEffect/src/main/scala/japgolly/scalajs/react/util/EffectCatsEffect.scala` (lines 91–96)
- **Issue**:
  ```scala
  override def async_(onCompletion: Sync.Untyped[Unit] => Sync.Untyped[Unit]): IO[Unit] =
    for {
      p <- IO.delay(JsUtil.newPromise[Unit]())
      _ <- IO.delay(onCompletion(p._2(tryUnit))())
      _ <- fromJsPromise(p._1)
    } yield ()
  ```
  This creates a `js.Promise` via `JsUtil.newPromise` and passes it to `fromJsPromise` (which uses `IO.async` + `runPromiseAsync`), whereas `async` right above it (lines 85–89) uses `IO.async` directly with zero `js.Promise` allocations.
- **Suggestion**:
  ```scala
  private val rightUnit: Either[Throwable, Unit] = Right(())

  override def async_(onCompletion: Sync.Untyped[Unit] => Sync.Untyped[Unit]): IO[Unit] =
    IO.async(f => IO.delay {
      onCompletion(() => f(rightUnit))()
      None
    })
  ```

---

### 38. `RateLimit.fn` boxes `Long` into `js.Array[Long]` even when `maxPerWindow == 1`
- **File**: `callback/src/main/scala/japgolly/scalajs/react/callback/RateLimit.scala` (lines 12–40)
- **Issue**:
  1. In Scala.js, `Long` is emulated as a heap-allocated object (`RuntimeLong`), so storing `Long` in a `js.Array[Long]` boxes every timestamp.
  2. For the default and overwhelmingly common case `maxPerWindow == 1`, allocating a `js.Array[Long]` and calling `shift()` / `push()` on every invocation is unnecessary when a single `var availableAt = Long.MinValue` suffices.
  3. `fn0` wraps `fn[Unit, A](run = _ => run(), ...)` and returns `() => f(())`, adding two extra function hops per call.
- **Suggestion**:
  Fast-path `maxPerWindow == 1` with a single `var availableAt = Long.MinValue` field.

---

### 39. `EffectCallback.callback.traverse_` maps `.void` on every element unnecessarily
- **File**: `callback/src/main/scala/japgolly/scalajs/react/util/EffectCallback.scala` (lines 64–65)
- **Issue**:
  ```scala
  @inline override def traverse_[A, B](as: => Iterable[A])(f: A => CallbackTo[B]) =
    Callback.traverse(as)(f(_).void)
  ```
  `Callback.traverse` (`Callback.scala:102`) already accepts `f: A => CallbackTo[B]` and discards `f(a).runNow()`. Passing `f(_).void` allocates a closure and chains a `Trampoline.FlatMap` (`>> Callback.empty`) for every element in `as`.
- **Suggestion**:
  Change to `Callback.traverse(as)(f)`.

---

### 40. `CallbackOption` methods (`flatMap`, `traverse_`, `pass`, `fail`, `require`, `void`, `unary_!`) allocate unnecessary `Trampoline` and collection wrappers
- **Files**:
  - `callback/src/main/scala-2/japgolly/scalajs/react/callback/CallbackOption.scala` (lines 19–26, 46–47, 96–102, 156–181, 240–241)
  - `callback/src/main/scala-3/japgolly/scalajs/react/callback/CallbackOption.scala` (lines 46–47, 96–102, 135–139, 178–188, 247–248)
- **Issue**:
  1. `CallbackOption.traverse_` and `sequence_` call `traverse(ta)(f).void`, building a full `T[B]` collection via `BuildFrom` only to discard it.
  2. `CallbackOption` is an `AnyVal` wrapping `() => Option[A]`, yet `flatMap`, `flatMapOption`, `require`, and `unary_!` convert to `CallbackTo` (`Trampoline`) and back via `.asCBO`, allocating multiple `Trampoline` nodes and wrapper closures per call instead of operating on `cbfn()` directly.
  3. In Scala 2, `CallbackOption.pass` and `CallbackOption.fail` allocate new `CallbackTo.pure(...).asCBO` instances on every call instead of reusing static `() => someUnit` / `() => None` functions.
- **Suggestion**:
  Implement `CallbackOption` combinators directly over `cbfn: () => Option[A]` and implement `traverse_` with a tail-recursive iterator loop without building `T[B]`.

---

### 41. `CallbackTo.when_` in Scala 2 allocates `Some(runNow())` and chains `.void`
- **File**: `callback/src/main/scala-2/japgolly/scalajs/react/callback/CallbackTo.scala` (lines 429–430)
- **Issue**:
  In Scala 3 (`CallbackTo.scala:464`), `when_` is `CallbackTo(if (cond) runNow())`, whereas in Scala 2 it is `when(cond).void`, which allocates `Some(runNow())` and a `Trampoline.FlatMap` node to discard it.
- **Suggestion**:
  Define `def when_(cond: => Boolean): Callback = CallbackTo(if (cond) { runNow(); () })` in Scala 2 as well (and similarly avoid `Some` + `.void` in `AsyncCallback.when_`).

---

### 42. `Hooks`: Repeated allocation of empty `new js.Array[Any]` for 0-dependency hooks on every render
- **File**: `coreGeneric/src/main/scala/japgolly/scalajs/react/hooks/Hooks.scala` (lines 52, 57, 102, 108, 114, 122, 127, 132, 139)
- **Issue**:
  `UseCallback` and `UseEffect.unsafeCreate*OnMount` allocate a fresh `new js.Array[Any]` on every component render. React never mutates the hook dependency array.
- **Suggestion**:
  Share a single `private val emptyDeps: facade.React.HookDeps = new js.Array[Any](0)` instance across `Hooks`.

---

### 43. `Hooks.UseEffectEvent` allocates and immediately discards a `Reusable[A]` wrapper on every render
- **File**: `coreGeneric/src/main/scala/japgolly/scalajs/react/hooks/Hooks.scala` (lines 717–720)
- **Issue**:
  ```scala
  object UseEffectEvent {
    def apply[A](callback: => A)(implicit a: UseCallbackArg[A]): CustomHook[Unit, A] =
      CustomHook.delay(a.fromJs(facade.React.useEffectEvent(a.toJs(callback))))
  }
  ```
  `a.fromJs(...)` returns `Reusable[A]` (`Reusable.byRef(z).withValue(...)`), which is immediately unwrapped into `A` via `Reusable.autoValue`, discarding the `Reusable` instance on every render.
- **Suggestion**:
  Add a `fromJsRaw: Js => A` field to `UseCallbackArg[A]` so `UseEffectEvent` constructs `A` directly without allocating `Reusable[A]`.

---

### 44. `Template.MountedMapped.withEffect` and `.withAsyncEffect` allocate two wrapper objects on every `$.setState` / `$.modState` call
- **Files**:
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/component/Template.scala` (lines 89–93)
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/component/builder/LifecycleF.scala` (lines 60, 66–87)
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/component/builder/ViaReactComponent.scala` (line 279)
- **Issue**:
  1. `LifecycleF.Base.mountedPure` calls `raw.mountedPure.withEffect(F).withAsyncEffect(A)`. Even when `F` and `A` are identical to the already-configured effects on `raw.mountedPure` (the default case for 99% of calls to `$.setState` or `$.modState`), `Template.MountedMapped.withEffect` and `.withAsyncEffect` unconditionally allocate two new `MountedMapped` objects!
  2. In `ViaReactComponent.scala:279`, `new RenderScope(_this)` is allocated on every render of a class component, even though `RenderScope` is completely stateless and only wraps `_this`.
- **Suggestion**:
  1. In `Template.MountedMapped`, short-circuit when the effect instance is identical:
     ```scala
     override def withEffect[F2[_]](implicit t: UnsafeSync[F2]) =
       if (t eq ft) this.asInstanceOf[WithEffect[F2]] else mapped(mp, ls)(t, at)

     override def withAsyncEffect[A2[_]](implicit t: Async[A2]) =
       if (t eq at) this.asInstanceOf[WithAsyncEffect[A2]] else mapped(mp, ls)(ft, t)
     ```
  2. Cache `RenderScope` on `_this` in `ViaReactComponent`.

---

### 45. `ScalaFn` and `ScalaForwardRef` allocate a `Delayed` (`Trampoline.Delay` + `Function0`) wrapper on every functional component render
- **Files**:
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/component/ScalaFn.scala` (lines 18–26, 37–59)
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/component/ScalaForwardRef.scala` (lines 26–39, 108–114)
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/component/Delayed.scala` (lines 18–26)
- **Issue**:
  Because `ScalaFn` render functions take `P => Delayed[VdomNode]` and use `Delayed.autoLift`, every render of a `ScalaFnComponent` wraps its `VdomNode` result in a `() => A` closure and a `new Trampoline.Delay(...)`, only to immediately call `.eval().rawNode` on the very next line (`ScalaFn.scala:21`).
- **Suggestion**:
  Represent `Delayed[+A]` internally over an unboxed union `A | Trampoline[A]` (where `Delayed.autoLift` is a zero-allocation identity cast for strict values and `Delayed.apply(a: => A)` uses `Trampoline.delay(a)`), eliminating 2 allocations per functional component render.

---

### 46. `Reusability.indexedSeq` and `Reusability.localDate(tolerance)` allocate intermediate objects on every check
- **File**: `coreGeneric/src/main/scala/japgolly/scalajs/react/Reusability.scala` (lines 132–134, 166–167)
- **Issue**:
  1. `Reusability.indexedSeq` uses `x.indices.forall(i => x(i) ~=~ y(i))`, allocating a `Range` and a closure (with boxed `Int` indices) on every comparison of two same-length `IndexedSeq`/`Vector`s.
  2. `Reusability.localDate(tolerance: Duration)` allocates two `java.time.Duration` instances (`Duration.ofDays(a.toEpochDay - b.toEpochDay)` and `.abs`) on every comparison instead of precomputing `val tolDays = tolerance.abs.toDays` once.
- **Suggestion**:
  Use a `while` loop in `Reusability.indexedSeq` and compare `Math.abs(a.toEpochDay - b.toEpochDay) <= tolDays` in `Reusability.localDate(tolerance)`.

---

### 47. `Ref.fromJs` double-allocates `Some` on every `ref.get`, and `.widen` / `.narrow` wrap identity functions
- **Files**:
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/Ref.scala` (lines 26–27, 133–146, 177–181)
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/NonEmptyRef.scala` (lines 77–83)
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/hooks/CustomHook.scala` (lines 20–24)
- **Issue**:
  1. `Ref.fromJs` passes `r = Some(_)` into `Full(raw, identityFn, Some(_))`, where `get` runs `jsNullToOption(raw.current).flatMap(r)` — allocating a `Some` in `jsNullToOption` and immediately unboxing and re-allocating a second `Some` in `.flatMap(Some(_))`.
  2. In `Ref.FullF`, `Ref.ToComponentF`, `NonEmptyRef.FullF`, and `CustomHook`, `.widen` and `.narrow` call `map(o => o)` / `contramap(a => a)`, allocating a new wrapper object and composing an extra identity function call onto every access instead of casting `this.asInstanceOf[...]`.
- **Suggestion**:
  Use `this.asInstanceOf[...]` in `.widen` and `.narrow`, and avoid `.flatMap(Some(_))` when `r` is the default `Some` projection in `Ref.fromJs`.

---

### 48. `ReactCats` and `CallbackCatsEffect` typeclass instances do not override `map`
- **Files**:
  - `coreExtCats/src/main/scala/japgolly/scalajs/react/internal/ReactCats.scala` (lines 37–53)
  - `callbackExtCatsEffect/src/main/scala/japgolly/scalajs/react/callback/CallbackCatsEffect.scala` (lines 29–81, 148–150, 196–343)
- **Issue**:
  1. `ReactCats.reactCatsSyncEffectMonadThrow`, `CallbackCatsEffect.syncCallback`, and `CallbackCatsEffect.AsyncAsyncCallback` do not override `map`, causing Cats `Monad.map` to default to `flatMap(fa)(a => pure(f(a)))` instead of delegating to `F.map(fa)(f)` / `fa.map(f)`.
  2. In `CallbackCatsEffect.IOToAsyncCallback` (lines 148–150), `f0.attempt.flatMap(IO.pure)` has a redundant `.flatMap(IO.pure)`.
- **Suggestion**:
  Add `override def map` to all three typeclass instances and remove `.flatMap(IO.pure)` in `IOToAsyncCallback`.

---

### 49. `ReactDOM` preload methods and `ReactOptions` assign explicit `undefined` own-properties onto JS options objects
- **Files**:
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/ReactDOM.scala` (lines 23–78)
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/ReactOptions.scala` (lines 25–32, 51–58)
- **Issue**:
  Assigning `o.prop = js.undefined` in Scala.js defines an enumerable own property `{ prop: undefined }` on the JS object (`"prop" in o === true`). Calling `ReactDOM.preload("...", as = "style")` allocates an object with 9 keys set to `undefined`.
- **Suggestion**:
  Only assign defined `js.UndefOr` / `Option` properties using `.foreach(o.prop = _)`.

---

## Section C: Code Quality, Cross-Version Deduplication, Build & Examples

### 50. Massive duplication (~3,000+ lines of 98% identical code) between `scala-2` and `scala-3` source directories
- **Files**:
  - `util/src/main/scala-2/japgolly/scalajs/react/util/NotAllowed.scala` & `scala-3/.../NotAllowed.scala` (100% identical)
  - `callback/src/main/scala-2/japgolly/scalajs/react/callback/AsyncCallback.scala` (1,248 lines) & `scala-3/.../AsyncCallback.scala` (1,254 lines) (~99% identical)
  - `callback/src/main/scala-2/japgolly/scalajs/react/callback/CallbackTo.scala` (740 lines) & `scala-3/.../CallbackTo.scala` (737 lines) (~95% identical)
  - `callback/src/main/scala-2/japgolly/scalajs/react/callback/CallbackOption.scala` (311 lines) & `scala-3/.../CallbackOption.scala` (318 lines) (~95% identical)
  - `callback/src/main/scala-2/japgolly/scalajs/react/callback/Callback.scala` (212 lines) & `scala-3/.../Callback.scala` (214 lines) (~95% identical)
  - `coreGeneric/src/main/scala-2/japgolly/scalajs/react/component/builder/ComponentBuilder.scala` (502 lines) & `scala-3/.../ComponentBuilder.scala` (508 lines) (~98% identical)
  - `coreGeneric/src/main/scala-2/japgolly/scalajs/react/UpdateSnapshot.scala` & `scala-3/.../UpdateSnapshot.scala` (100% unifiable)
- **Issue**:
  Maintaining two nearly identical copies of `AsyncCallback`, `CallbackTo`, `CallbackOption`, `Callback`, and `ComponentBuilder` doubles maintenance effort and has already caused small divergences (such as `CallbackTo.when_` in item 41 and missing `inline` on `CallbackOption.unless` in Scala 3 line 281).
- **Suggestion**:
  Move `NotAllowed.scala`, `UpdateSnapshot.scala`, `AsyncCallback.scala`, and the shared bodies of `ComponentBuilder.scala`, `CallbackTo.scala`, `CallbackOption.scala`, and `Callback.scala` into `src/main/scala/`, isolating only version-specific macro or inline extensions in small `scala-2` / `scala-3` traits.

---

### 51. 100% duplicate files across `coreBundleCallback` / `coreBundleCBIO` and `extraExtMonocle2` / `extraExtMonocle3`
- **Files**:
  - `coreBundleCallback/src/main/scala/japgolly/scalajs/react/internal/ReactCallbackExtensions.scala` and `coreBundleCBIO/src/main/scala/japgolly/scalajs/react/internal/ReactCallbackExtensions.scala` (117 lines, 100% identical)
  - `extraExtMonocle2/src/main/scala/japgolly/scalajs/react/ReactMonocle.scala` and `extraExtMonocle3/src/main/scala/japgolly/scalajs/react/ReactMonocle.scala` (100% identical)
- **Suggestion**:
  Move `ReactCallbackExtensions.scala` into `callback` (or a shared `unmanagedSourceDirectories` setting in `Build.scala`) and share `ReactMonocle.scala` across both Monocle modules.

---

### 52. Dead code in `coreGeneric/src/main/scala-3/japgolly/scalajs/react/vdom/Attr.scala`: `DomCallbackResult`
- **File**: `coreGeneric/src/main/scala-3/japgolly/scalajs/react/vdom/Attr.scala` (lines 32–42)
- **Issue**:
  `sealed trait DomCallbackResult[A]` and `object DomCallbackResult` are completely unused anywhere in the codebase and do not exist in the Scala 2 counterpart.
- **Suggestion**:
  Remove `DomCallbackResult`.

---

### 53. Obsolete Scala 3.0 / Dotty bug workarounds in `coreGeneric/src/main/scala-3`
- **Files**:
  - `coreGeneric/src/main/scala-3/japgolly/scalajs/react/internal/ValueOfCompat.scala` (workaround for `dotty#12510`, fixed in Scala 3.1.0)
  - `coreGeneric/src/main/scala-3/japgolly/scalajs/react/ScalaComponent.scala` (lines 10–13, workaround for `dotty#12111`, fixed in Scala 3.0.1)
  - `coreGeneric/src/main/scala-3/japgolly/scalajs/react/ReusabilityMacros.scala` (lines 353–371, where `mkTest` calls `Expr.summonLater[Reusability[B]]` even though `rf` was already summoned and passed in as parameter `e`)
  - `project/Lib.scala` (line 64: `"-Wconf:msg=unused:s", // Scala 3.1 doesn't support @nowarn("cat=unused")` — project is now on Scala 3.3.8 which supports `@nowarn("cat=unused")`)
- **Suggestion**:
  Remove `ValueOfCompat` in favour of `scala.ValueOf`, simplify `ScalaComponent.scala` to `export Scala.*`, use `e` directly in `ReusabilityMacros.mkTest`, and enable unused warnings in Scala 3.

---

### 54. Scala 3 `CssUnits` macro fails to constant-fold literals wrapped in outer `Inlined` AST nodes
- **File**: `coreGeneric/src/main/scala-3/japgolly/scalajs/react/vdom/CssUnits.scala` (lines 12–23)
- **Issue**:
  `n.asTerm` is matched against `case Inlined(_, Nil, Literal(c)) =>`. If `n` is a bare `Literal(c)` or wrapped in multiple `Inlined` layers (from another `inline def`), the pattern fails to match and falls back to runtime string concatenation.
- **Suggestion**:
  Match on `n.asTerm.underlyingArgument`:
  ```scala
  n.asTerm.underlyingArgument match {
    case Literal(c) =>
      val v = c.value
      val s = if v == 0 then "0" else v.toString + suffix.valueOrAbort
      Expr.inlineConst(s)
    case _ =>
      '{ combineAtRuntime(${n}.toString, $suffix) }
  }
  ```

---

### 55. Scala 3 `PropsChildren` opaque type lacks a meaningful `toString`
- **Files**:
  - `coreGeneric/src/main/scala-2/japgolly/scalajs/react/PropsChildren.scala` (lines 19–20)
  - `coreGeneric/src/main/scala-3/japgolly/scalajs/react/PropsChildren.scala` (lines 6–52)
- **Issue**:
  In Scala 2, `PropsChildren` overrides `toString` as `iterator.mkString("PropsChildren(", ", ", ")")`, which is used by `HookCtx.PC0..PC20.toString`. In Scala 3, `PropsChildren` is an `opaque type`, so string interpolation calls `Object.toString` on the raw JS value (`[object Object]` or `undefined`).
- **Suggestion**:
  Provide an extension or helper method for formatting `PropsChildren` consistently across Scala 2 and Scala 3.

---

### 56. `ViaReactComponent`, `ScalaFn`, and `ReactForwardRef` set `displayName = ""` in production builds, and `DerivedDisplayName` ignores `ScalaJsReactConfig`
- **Files**:
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/component/builder/ViaReactComponent.scala` (lines 343–344)
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/component/ScalaFn.scala` (line 23)
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/component/ScalaForwardRef.scala` (line 116)
  - `coreGeneric/src/main/scala/japgolly/scalajs/react/component/DerivedDisplayName.scala` (lines 6–11)
- **Issue**:
  1. When `ScalaJsReactConfig` strips component names in `fullOptJS`, `builder.name` is `""` (empty string), not `null`. `for (n <- Option(builder.name)) staticProps.add("displayName", n)` still sets `displayName = ""` on every component class.
  2. `DerivedDisplayName` (used by `ScalaFn` and `ReactForwardRef`) does not pass the generated name through `ScalaJsReactConfig.Instance.automaticComponentName`, so `ScalaFn` and `ReactForwardRef` retain full class/package display names in `fullOptJS` even when `ScalaJsReactConfig` is configured to strip automatic component names.
- **Suggestion**:
  Pass `DerivedDisplayName.derivedDisplayName` through `ScalaJsReactConfig.Instance.automaticComponentName`, and only set `displayName` on the raw JS component when `(name ne null) && name.nonEmpty`.

---

### 57. `InspectRaw.assertValidJsComponent` allocates a `List` and has a stray quote in its error message
- **File**: `coreGeneric/src/main/scala/japgolly/scalajs/react/component/InspectRaw.scala` (lines 16, 64)
- **Issue**:
  1. Line 16: `List("$$typeof", "type").exists(js.Object.hasProperty(o, _))` allocates a `List` and closure on every `JsComponent` creation in dev mode.
  2. Line 64 has a trailing stray `"` at the end of the documentation URL: `|See https://github.com/japgolly/scalajs-react/blob/master/doc/INTEROP.md"`.
- **Suggestion**:
  Use `js.Object.hasProperty(o, "$$typeof") || js.Object.hasProperty(o, "type")` and remove the stray quote on line 64.

---

### 58. `RouterCtlF.reusability` does not handle `ModCB` or `AltEffect` wrappers
- **File**: `extra/src/main/scala/japgolly/scalajs/react/extra/router/RouterCtlF.scala` (lines 90–98)
- **Issue**:
  `RouterCtlF.reuseF` handles `Contramap(ac, af)` vs `Contramap(bc, bf)`, but returns `false` for two `AltEffect` or `ModCB` wrappers over reusable underlying `RouterCtlF`s.
- **Suggestion**:
  Add `case (ModCB(ac, af), ModCB(bc, bf)) => (af eq bf) && test(ac, bc)` and `case (AltEffect(ac), AltEffect(bc)) => test(ac, bc)` to `reuseF`.

---

### 59. `TimerSupportF.setGuaranteedIntervalMs` allocates 3 local `lazy val` state machines per timer
- **File**: `extra/src/main/scala/japgolly/scalajs/react/extra/TimerSupport.scala` (lines 43–64)
- **Issue**:
  `setGuaranteedIntervalMs` uses 3 mutually recursive local `lazy val`s (`jsProc`, `step`, `proc`), which in Scala.js compile to heap-allocated lazy-val holder objects per call.
- **Suggestion**:
  Use a simple `var jsProc: js.Function0[Unit] = null` assigned before `RawTimers.setTimeout`.

---

### 60. `BroadcasterF` notifies listeners in reverse (LIFO) registration order and allocates a `Tuple2` per listener
- **File**: `extra/src/main/scala/japgolly/scalajs/react/extra/Broadcaster.scala` (lines 15–28)
- **Issue**:
  `_listeners ::= record` prepends to a `List`, so `broadcast` invokes listeners in reverse registration order, and `val record = (listener, ())` allocates a tuple just to get a fresh object identity.
- **Suggestion**:
  Maintain listeners in registration (FIFO) order (e.g. via `Vector` or a small `js.Array` / wrapper node) without allocating a `Tuple2`.

---

### 61. `ReactTestUtils.removeReactInternals` runs two regex passes on every HTML assertion even when no React markers exist
- **File**: `testUtil/src/main/scala/japgolly/scalajs/react/test/ReactTestUtils.scala` (lines 16–19, 108–113)
- **Issue**:
  React 18/19 `createRoot` no longer emits `<!-- react-text -->` or `data-reactroot`, yet `removeReactInternals` unconditionally runs two regex `.replaceAllIn` passes on every `assertInnerHTML` / `assertOuterHTML` call.
- **Suggestion**:
  Guard the regex calls with `if (html.contains("react-text"))` and `if (html.contains("data-react"))`.

---

### 62. `GhPagesMacros` duplicates helper logic between Scala 2 and Scala 3 and reverses the line list twice
- **Files**:
  - `ghpagesMacros/src/main/scala-2/ghpages/GhPagesMacros.scala` (lines 11–62)
  - `ghpagesMacros/src/main/scala-3/ghpages/GhPagesMacros.scala` (lines 12–60)
- **Issue**:
  `GhPagesMacroImpls` (`trimRight`, `blankLine`, `trimLeftAll`, `splitOnce`, `betweenMarkers`, line trimming) is duplicated verbatim in `scala-2` and `scala-3` instead of residing in `ghpagesMacros/src/main/scala/`.

---

### 63. Broken external URL (`wss://echo.websocket.org`) and $O(N)$ `log.init` in `ghpages` examples
- **Files**:
  - `ghpages/src/main/scala/ghpages/examples/WebSocketsExample.scala` (line 25)
  - `ghpages/src/main/scala/ghpages/examples/TouchExample.scala` (lines 27–31)
- **Issue**:
  1. `WebSocketsExample.scala:25` uses `wss://echo.websocket.org`, which was shut down years ago (modern replacements such as `wss://echo.websocket.events` or `wss://ws.postman-echo.com/raw` can be used).
  2. `TouchExample.scala:30` uses `if (log.size > max) copy(log = log.init) else this` on a `List[String]`, traversing the list twice (`size` and `init`) instead of `copy(log = (name :: log).take(max))`.

---

### 64. Unused build configuration, dead `FT` alias, and unused `scalajs-env-selenium` plugin dependency
- **Files**:
  - `aliases.sbt` (line 6)
  - `project/plugins.sbt` (line 3)
  - `project/InBrowserTesting.scala` (lines 1–92)
  - `project/GenHooks.scala` (lines 35–39, 43)
  - `project/Lib.scala` (lines 143–157)
- **Issue**:
  1. `aliases.sbt:6` defines `addCommandAlias("FT", "project facadeTest")`, but `facadeTest` is not a project in `Build.scala` / `build.sbt`.
  2. `project/InBrowserTesting.scala` is completely commented out (`identity`), yet `project/plugins.sbt` still resolves `"org.scala-js" %% "scalajs-env-selenium" % "1.1.1"` (pulling Selenium and Guava into the sbt build).
  3. `project/GenHooks.scala` has unused variables `i12663_a`, `i12663_c`, `i12663_bc`, and `_s`, and `project/Lib.scala:143-157` has commented-out `useScalaJsBundler` dead code.
- **Suggestion**:
  Remove the dead `FT` alias, unused `scalajs-env-selenium` dependency, `InBrowserTesting.scala` stub, and unused variables in `GenHooks.scala`.
