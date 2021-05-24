package japgolly.scalajs.react.hooks

import Hooks._

object Api {

  trait Step {
    type Next[A]
  }

  trait SubsequentStep[_Ctx, _CtxFn[_]] extends Step {
    final type Ctx = _Ctx
    final type CtxFn[A] = _CtxFn[A]
    def squash[A]: CtxFn[A] => (Ctx => A)
  }

  final class Var[A](initialValue: A) {
    var value: A =
      initialValue
  }

  // ===================================================================================================================
  // API 1: X / (Ctx => X)

  trait Primary[Ctx, _Step <: Step] {
    final type Step = _Step

    protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H]

    /** Provides you with a means to do whatever you want without the static guarantees that the normal DSL provides.
      * It's up to you to ensure you don't vioalte React's hook rules.
      */
    final def unchecked[A](f: Ctx => A)(implicit step: Step): step.Next[A] =
      next(f)

    /** Create a new `var` on each render. */
    final def newVar[A](f: Ctx => A)(implicit step: Step): step.Next[Var[A]] =
      next(ctx => new Var(f(ctx)))

    /** Create a new `val` on each render. */
    final def newVal[A](f: Ctx => A)(implicit step: Step): step.Next[A] =
      next(f)

    /** Create a new `lazy val` on each render. */
    final def newLazyVal[A](f: Ctx => A)(implicit step: Step): step.Next[() => A] =
      next { ctx =>
        lazy val a: A = f(ctx)
        val result = () => a // TODO: Report Scala bug
        result
      }

    /** Use a custom hook */
    final def custom[I, O](hook: CustomHook[I, O])(implicit step: Step, a: CustomHook.Arg[Ctx, I]): step.Next[O] =
      next(ctx => hook.unsafeInit(a.convert(ctx)))

    /** Use a custom hook */
    final def custom[O](hook: Ctx => CustomHook[Unit, O])(implicit step: Step): step.Next[O] =
      next(hook(_).unsafeInit(()))

    /** Returns a stateful value, and a function to update it.
      *
      * During the initial render, the returned state is the same as the value passed as the first argument
      * (initialState).
      *
      * During subsequent re-renders, the first value returned by useState will always be the most recent state after
      * applying updates.
      */
    final def useState[S](initialState: Ctx => S)(implicit step: Step): step.Next[UseState[S]] =
      next(ctx => UseState.unsafeCreate(initialState(ctx)))

    /** An alternative to [[useState]]. Accepts a reducer of type `(state, action) => newState`, and returns the
      * current state paired with a dispatch method.
      * (If you’re familiar with Redux, you already know how this works.)
      *
      * useReducer is usually preferable to useState when you have complex state logic that involves multiple
      * sub-values or when the next state depends on the previous one. useReducer also lets you optimize performance
      * for components that trigger deep updates because you can pass dispatch down instead of callbacks.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usereducer
      */
    final def useReducer[S, A](reducer: (S, A) => S, initialArg: S)(implicit step: Step): step.Next[UseReducer[S, A]] =
      next(_ => UseReducer.unsafeCreate(reducer, initialArg))

    /** An alternative to [[useState]]. Accepts a reducer of type `(state, action) => newState`, and returns the
      * current state paired with a dispatch method.
      * (If you’re familiar with Redux, you already know how this works.)
      *
      * useReducer is usually preferable to useState when you have complex state logic that involves multiple
      * sub-values or when the next state depends on the previous one. useReducer also lets you optimize performance
      * for components that trigger deep updates because you can pass dispatch down instead of callbacks.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usereducer
      */
    final def useReducer[I, S, A](reducer: (S, A) => S, initialArg: I, init: I => S)(implicit step: Step): step.Next[UseReducer[S, A]] =
      next(_ => UseReducer.unsafeCreate(reducer, initialArg, init))

    /** An alternative to [[useState]]. Accepts a reducer of type `(state, action) => newState`, and returns the
      * current state paired with a dispatch method.
      * (If you’re familiar with Redux, you already know how this works.)
      *
      * useReducer is usually preferable to useState when you have complex state logic that involves multiple
      * sub-values or when the next state depends on the previous one. useReducer also lets you optimize performance
      * for components that trigger deep updates because you can pass dispatch down instead of callbacks.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usereducer
      */
    final def useReducer[S, A](init: Ctx => UseReducerInline => UseReducer[S, A])(implicit step: Step): step.Next[UseReducer[S, A]] =
      next(init(_)(new UseReducerInline))
  }

  // ===================================================================================================================
  // API 2: (H1, H2, ..., Hn) => X

  trait Secondary[Ctx, CtxFn[_], _Step <: SubsequentStep[Ctx, CtxFn]] extends Primary[Ctx, _Step] {

    /** Provides you with a means to do whatever you want without the static guarantees that the normal DSL provides.
      * It's up to you to ensure you don't vioalte React's hook rules.
      */
    final def unchecked[A](f: CtxFn[A])(implicit step: Step): step.Next[A] =
      unchecked(step.squash(f)(_))

    /** Create a new `var` on each render. */
    final def newVar[A](f: CtxFn[A])(implicit step: Step): step.Next[Var[A]] =
      newVar(step.squash(f)(_))

    /** Create a new `val` on each render. */
    final def newVal[A](f: CtxFn[A])(implicit step: Step): step.Next[A] =
      newVal(step.squash(f)(_))

    /** Create a new `lazy val` on each render. */
    final def newLazyVal[A](f: CtxFn[A])(implicit step: Step): step.Next[() => A] =
      newLazyVal(step.squash(f)(_))

    /** Use a custom hook */
    final def custom[O](hook: CtxFn[CustomHook[Unit, O]])(implicit step: Step): step.Next[O] =
      custom(step.squash(hook)(_))

    /** Returns a stateful value, and a function to update it.
      *
      * During the initial render, the returned state is the same as the value passed as the first argument
      * (initialState).
      *
      * During subsequent re-renders, the first value returned by useState will always be the most recent state after
      * applying updates.
      */
    final def useState[S](initialState: CtxFn[S])(implicit step: Step): step.Next[UseState[S]] =
      useState(step.squash(initialState)(_))

    /** An alternative to [[useState]]. Accepts a reducer of type `(state, action) => newState`, and returns the
      * current state paired with a dispatch method.
      * (If you’re familiar with Redux, you already know how this works.)
      *
      * useReducer is usually preferable to useState when you have complex state logic that involves multiple
      * sub-values or when the next state depends on the previous one. useReducer also lets you optimize performance
      * for components that trigger deep updates because you can pass dispatch down instead of callbacks.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usereducer
      */
    final def useReducer[S, A](init: CtxFn[UseReducerInline => UseReducer[S, A]])(implicit step: Step): step.Next[UseReducer[S, A]] =
      useReducer(step.squash(init)(_))
  }

  // ===================================================================================================================
  // Inline API

  final class UseReducerInline {
    @inline def apply[S, A](reducer: (S, A) => S, initialArg: S): UseReducer[S, A] =
      UseReducer.unsafeCreate(reducer, initialArg)

    @inline def apply[I, S, A](reducer: (S, A) => S, initialArg: I, init: I => S): UseReducer[S, A] =
      UseReducer.unsafeCreate(reducer, initialArg, init)
  }


}
