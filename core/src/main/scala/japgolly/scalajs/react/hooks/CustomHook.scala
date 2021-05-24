package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.PropsChildren

final class CustomHook[-I, +O] private[CustomHook] (val unsafeInit: I => O) {
  def apply(i: I): CustomHook[Unit, O] =
    CustomHook.unchecked[Unit, O](_ => unsafeInit(i))
}

object CustomHook {
  final case class Arg[Ctx, I](convert: Ctx => I) extends AnyVal

  object Arg {
    def const[C, I](i: I): Arg[C, I] =
      apply(_ => i)

    implicit def unit    [Ctx]      : Arg[Ctx, Unit]                       = const(())
    implicit def id      [A, B >: A]: Arg[A, B]                            = apply(a => a)
    implicit def ctxProps[P]        : Arg[HookCtx.P0[P], P]                = apply(_.props)
    implicit def ctxPropsChildren   : Arg[HookCtx.PC0[Any], PropsChildren] = apply(_.propsChildren)
  }

  /** Provides you with a means to do whatever you want without the static guarantees that the normal DSL provides.
    * It's up to you to ensure you don't vioalte React's hook rules.
    */
  def unchecked[I, O](f: I => O): CustomHook[I, O] =
    new CustomHook(f)

  def builder[I]: Builder.First[I] =
    new Builder.First

  // ===================================================================================================================
  // Builder with Props

  object Builder {

    final class First[I] extends Api.Primary[I, FirstStep[I]] {

      override protected def next[H](f: I => H)(implicit step: Step): step.Next[H] =
        step(f)
    }

    trait BuildFn[-I, +Ctx] {
      def apply[O](f: Ctx => O): CustomHook[I, O]
    }

    final class Subsequent[I, Ctx, CtxFn[_]](buildFn: BuildFn[I, Ctx]) extends Api.Secondary[Ctx, CtxFn, SubsequentStep[I, Ctx, CtxFn]] {

      protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
        step.next[H](buildFn, f)

      def build: CustomHook[I, Unit] =
        buildReturning(_ => ())

      def buildReturning[O](f: Ctx => O): CustomHook[I, O] =
        buildFn(f)

      def buildReturning[O](f: CtxFn[O])(implicit step: Step): CustomHook[I, O] =
        buildReturning(step.squash(f)(_))
    }

    object Subsequent extends Custom_SubsequentDsl

    final class FirstStep[I] extends Api.Step {
      override type Next[H1] =
        Subsequent[I, HookCtx.I1[I, H1], HookCtxFn.P1[I, H1]#Fn]

      def apply[H1](hook1: I => H1): Next[H1] = {
        type Ctx = HookCtx.I1[I, H1]
        val build: BuildFn[I, Ctx] =
          new BuildFn[I, Ctx] {
            override def apply[O](f: Ctx => O) =
              CustomHook.unchecked[I, O] { i =>
                val h1 = hook1(i)
                val ctx = HookCtx.withInput(i, h1)
                f(ctx)
              }
          }
        new Subsequent[I, HookCtx.I1[I, H1], HookCtxFn.P1[I, H1]#Fn](build)
      }
    }

    implicit def firstStep[P]: FirstStep[P] =
      new FirstStep

    trait SubsequentStep[I, _Ctx, _CtxFn[_]] extends Api.SubsequentStep[_Ctx, _CtxFn] {
      def next[A]: (BuildFn[I, Ctx], Ctx => A) => Next[A]
    }

    object SubsequentStep extends Custom_SubsequentSteps {
      type To[I, Ctx, CtxFn[_], N[_]] = SubsequentStep[I, Ctx, CtxFn] { type Next[A] = N[A] }
    }
  }

}
