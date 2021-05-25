package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.PropsChildren

final class CustomHook[-I, +O] private[CustomHook] (val unsafeInit: I => O) {

  def apply(i: I): CustomHook[Unit, O] =
    CustomHook.unchecked(_ => unsafeInit(i))

  def map[A](f: O => A): CustomHook[I, A] =
    CustomHook.unchecked(i => f(unsafeInit(i)))

  def contramap[A](f: A => I): CustomHook[A, O] =
    CustomHook.unchecked(a => unsafeInit(f(a)))

  def ++[I1 <: I, O1 >: O, I2, O2](next: CustomHook[I2, O2])(implicit c: CustomHook.Concat[I1, O1, I2, O2]): CustomHook[c.I, c.O] =
    c.concat(this, next)
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

  // ===================================================================================================================

  trait Concat[I1, O1, I2, O2] {
    type I
    type O
    def concat: (CustomHook[I1, O1], CustomHook[I2, O2]) => CustomHook[I, O]
  }

  trait Concat0 {
    implicit def tuple[I1, O1, I2, O2]: Concat.To[I1, O1, I2, O2, (I1, I2), (O1, O2)] =
      Concat((h1, h2) => unchecked((i: (I1, I2)) => (h1.unsafeInit(i._1), h2.unsafeInit(i._2))))
  }

  trait Concat1 extends Concat0 {
    implicit def sameItupleO[I, O1, O2]: Concat.To[I, O1, I, O2, I, (O1, O2)] =
      Concat((h1, h2) => unchecked((i: I) => (h1.unsafeInit(i), h2.unsafeInit(i))))

    implicit def tupleIunitO[I1, I2]: Concat.To[I1, Unit, I2, Unit, (I1, I2), Unit] =
      Concat((h1, h2) => unchecked((i: (I1, I2)) => {h1.unsafeInit(i._1); h2.unsafeInit(i._2)}))
  }

  trait Concat2 extends Concat1 {
    implicit def sameIunitO[I]: Concat.To[I, Unit, I, Unit, I, Unit] =
      Concat((h1, h2) => unchecked((i: I) => {h1.unsafeInit(i); h2.unsafeInit(i)}))
  }

  object Concat extends Concat2 {
    type To[I1, O1, I2, O2, II, OO] = Concat[I1, O1, I2, O2] { type I = II; type O = OO }

    def apply[I1, O1, I2, O2, II, OO](f: (CustomHook[I1, O1], CustomHook[I2, O2]) => CustomHook[II, OO]): To[I1, O1, I2, O2, II, OO] =
      new Concat[I1, O1, I2, O2] {
        override type I = II
        override type O = OO
        override def concat = f
      }
  }

  /** Provides you with a means to do whatever you want without the static guarantees that the normal DSL provides.
    * It's up to you to ensure you don't vioalte React's hook rules.
    */
  def unchecked[I, O](f: I => O): CustomHook[I, O] =
    new CustomHook(f)

  def builder[I]: Builder.First[I] =
    new Builder.First(_ => ())

  // ===================================================================================================================
  // Builder with Props

  object Builder {

    final class First[I](init: I => Unit) extends Api.Primary[I, FirstStep[I]] {

      override protected def self(f: I => Any)(implicit step: Step): step.Self =
        step.self(init, f)

      override protected def next[H](f: I => H)(implicit step: Step): step.Next[H] =
        step.next(init, f)
    }

    trait BuildFn[-I, +Ctx] {
      def apply[O](f: Ctx => O): CustomHook[I, O]
    }

    final class Subsequent[I, Ctx, CtxFn[_]](buildFn: BuildFn[I, Ctx]) extends Api.Secondary[Ctx, CtxFn, SubsequentStep[I, Ctx, CtxFn]] {

      override protected def self(f: Ctx => Any)(implicit step: Step): step.Self =
        step.self(buildFn, f)

      override protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
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
      override type Self     = First[I]
      override type Next[H1] = Subsequent[I, HookCtx.I1[I, H1], HookCtxFn.P1[I, H1]#Fn]

      def self(initFirst: I => Unit, f: I => Any): Self =
        new First[I](i => {
          initFirst(i)
          f(i)
        })

      def next[H1](initFirst: I => Unit, hook1: I => H1): Next[H1] = {
        type Ctx = HookCtx.I1[I, H1]
        val build: BuildFn[I, Ctx] =
          new BuildFn[I, Ctx] {
            override def apply[O](f: Ctx => O) =
              CustomHook.unchecked[I, O] { i =>
                initFirst(i)
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
      final type Self = Subsequent[I, Ctx, CtxFn]
      final def self: (BuildFn[I, Ctx], Ctx => Any) => Self =
        (buildPrev, initNextHook) => {
          val buildNext: BuildFn[I, Ctx] =
            new BuildFn[I, Ctx] {
              override def apply[O](f: Ctx => O) = {
                buildPrev { ctx =>
                  initNextHook(ctx)
                  f(ctx)
                }
              }
            }
          new Subsequent[I, Ctx, CtxFn](buildNext)
        }
      def next[A]: (BuildFn[I, Ctx], Ctx => A) => Next[A]
    }

    object SubsequentStep extends Custom_SubsequentSteps {
      type To[I, Ctx, CtxFn[_], N[_]] = SubsequentStep[I, Ctx, CtxFn] { type Next[A] = N[A] }
    }
  }

}
