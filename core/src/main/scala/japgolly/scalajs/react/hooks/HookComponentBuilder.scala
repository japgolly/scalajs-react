package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, PropsChildren}
import Hooks._

object HookComponentBuilder {

  case class Component[-P, C <: Children](value: (P, PropsChildren) => VdomNode) // TODO: Temp

  def apply[P]: DslStartP[P] =
    new DslStartP

  // ===================================================================================================================
  // API

  // API 1: X / (Ctx => X)
  trait ApiPrimary[Ctx, _Step <: StepBase] { //extends UseReducer[_Step] {
    final type Step = _Step

    protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H]

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

  // API 2: (H1, H2, ..., Hn) => X
  trait ApiSecondary[Ctx, CtxFn[_], _Step <: StepMulti[Ctx, CtxFn]] extends ApiPrimary[Ctx, _Step] {

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

  final class UseReducerInline {
    @inline def apply[S, A](reducer: (S, A) => S, initialArg: S): UseReducer[S, A] =
      UseReducer.unsafeCreate(reducer, initialArg)

    @inline def apply[I, S, A](reducer: (S, A) => S, initialArg: I, init: I => S): UseReducer[S, A] =
      UseReducer.unsafeCreate(reducer, initialArg, init)
  }

  // ===================================================================================================================
  // [P] Step 1

  final class DslStartP[P] extends ApiPrimary[P, StepFirstP[P]] {

    override protected def next[H](f: P => H)(implicit step: Step): step.Next[H] =
      step(f)

    def withPropsChildren: DslStartPC[P] =
      new DslStartPC

    def render(f: P => VdomNode): Component[P, Children.None] =
      Component((p, _) => f(p))
  }

  // ===================================================================================================================
  // [P] Step 2+

  type RenderFnP[-P, +Ctx] = (Ctx => VdomNode) => Component[P, Children.None]

  final class DslMultiP[P, Ctx, CtxFn[_]](renderFn: RenderFnP[P, Ctx]) extends ApiSecondary[Ctx, CtxFn, StepMultiP[P, Ctx, CtxFn]] {

    protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
      step.next[H](renderFn, f)

    def render(f: Ctx => VdomNode): Component[P, Children.None] =
      renderFn(f)

    def render(f: CtxFn[VdomNode])(implicit step: Step): Component[P, Children.None] =
      render(step.squash(f)(_))
  }

  object DslMultiP extends DslMultiPSteps

  // ===================================================================================================================
  // [PC] Step 1

  final class DslStartPC[P] extends ApiPrimary[HookCtx.PC0[P], StepFirstPC[P]] {

    override protected def next[H](f: HookCtx.PC0[P] => H)(implicit step: Step): step.Next[H] =
      step(f)

    def render(f: HookCtx.PC0[P] => VdomNode): Component[P, Children.Varargs] =
      Component((p, pc) => f(HookCtx.withChildren(p, pc)))

    def render(f: (P, PropsChildren) => VdomNode): Component[P, Children.Varargs] =
      Component(f)
  }

  // ===================================================================================================================
  // [P] Step 2+

  type RenderFnPC[-P, +Ctx] = (Ctx => VdomNode) => Component[P, Children.Varargs]

  final class DslMultiPC[P, Ctx, CtxFn[_]](renderFn: RenderFnPC[P, Ctx]) extends ApiSecondary[Ctx, CtxFn, StepMultiPC[P, Ctx, CtxFn]] {

    protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
      step.next[H](renderFn, f)

    def render(f: Ctx => VdomNode): Component[P, Children.Varargs] =
      renderFn(f)

    def render(f: CtxFn[VdomNode])(implicit step: Step): Component[P, Children.Varargs] =
      render(step.squash(f)(_))
  }

  object DslMultiPC extends DslMultiPCSteps
}
