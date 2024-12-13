package japgolly.scalajs.react.hooks

import japgolly.microlibs.types.NaturalComposition
import japgolly.scalajs.react.internal.CoreGeneral.ScalaFnComponent
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{PropsChildren, React, Reusability, Reusable}

final class CustomHook[I, O] private[CustomHook] (val unsafeInit: I => O) extends AnyVal {

  def apply(i: I): CustomHook[Unit, O] =
    CustomHook.unchecked(_ => unsafeInit(i))

  def map[A](f: O => A): CustomHook[I, A] =
    CustomHook.unchecked(i => f(unsafeInit(i)))

  def contramap[A](f: A => I): CustomHook[A, O] =
    CustomHook.unchecked(a => unsafeInit(f(a)))

  def widen[OO >: O]: CustomHook[I, OO] =
    map(o => o)

  def narrow[II <: I]: CustomHook[II, O] =
    contramap[II](i => i)

  // TODO: https://github.com/lampepfl/dotty/issues/12677
  // def ++[I2, O2](next: CustomHook[I2, O2])
  //               (implicit I: NaturalComposition.Split[I, I2], O: NaturalComposition.Merge[O, O2]): CustomHook[I.In, O.Out] =
  def ++[I2, O2, II, OO](next: CustomHook[I2, O2])(implicit
                         I: NaturalComposition.Split.To[II, I, I2],
                         O: NaturalComposition.Merge.To[O, O2, OO]): CustomHook[II, OO] =
    CustomHook.unchecked[I.In, O.Out] { i =>
      val is = I.split(i)
      val o1 = unsafeInit(is._1)
      val o2 = next.unsafeInit(is._2)
      O.merge(o1, o2)
    }

  def toHookResult(implicit ev: I =:= Unit): HookResult[O] =
    HookResult(unsafeInit(ev.flip(())))

  def toHookResult: I => HookResult[O] = 
    (i: I) => HookResult(unsafeInit(i))
}

object CustomHook {

  def apply[I]: Builder.First[I] =
    new Builder.First(_ => ())

  def fromHookResult[O](hook: HookResult[O]): CustomHook[Unit, O] =
    CustomHook.unchecked(_ => hook.hook())

  def fromHookResult[I, O](hook: I => HookResult[O]): CustomHook[I, O] =
    CustomHook.unchecked(i => hook(i).hook())

  /** Provides you with a means to do whatever you want without the static guarantees that the normal DSL provides.
    * It's up to you to ensure you don't vioalte React's hook rules.
    */
  def unchecked[I, O](f: I => O): CustomHook[I, O] =
    new CustomHook(f)

  @inline def delay[O](f: => O): CustomHook[Unit, O] =
    unchecked(_ => f)

  // ===================================================================================================================

  final case class Arg[-Ctx, +I](convert: Ctx => I) extends AnyVal

  trait ArgLowPri {
    implicit def id[A, B >: A]: Arg[A, B] =
        Arg[A, B](a => a)
  }

  object Arg extends ArgLowPri {
    def const[C, I](i: I): Arg[C, I] =
      apply[C, I](_ => i)

    implicit def unit    [Ctx]      : Arg[Ctx, Unit]                       = const(())
    implicit def ctxProps[P]        : Arg[HookCtx.P0[P], P]                = apply((_: HookCtx.P0[P]).props)
    implicit def ctxPropsChildren   : Arg[HookCtx.PC0[Any], PropsChildren] = apply((_: HookCtx.PC0[Any]).propsChildren)
  }

  // ===================================================================================================================

  object Builder {

    final class First[I](init: I => Unit) extends Api.Primary[I, FirstStep[I]] {

      override protected def self(f: I => Any)(implicit step: Step): step.Self =
        step.self(init, f)

      override protected def next[H](f: I => H)(implicit step: Step): step.Next[H] =
        step.next(init, f, "")

      def build: CustomHook[I, Unit] =
        CustomHook.unchecked(init)

      def buildReturning[O](f: I => O): CustomHook[I, O] =
        CustomHook.unchecked[I, O] { i =>
          init(i)
          f(i)
        }
    }

    trait BuildFn[I, +Ctx] {
      def apply[O](f: Ctx => O): CustomHook[I, O]
    }

    final class Subsequent[I, Ctx, CtxFn[_]](displayName: String)(buildFn: BuildFn[I, Ctx]) extends Api.Secondary[Ctx, CtxFn, SubsequentStep[I, Ctx, CtxFn]] {

      override protected def self(f: Ctx => Any)(implicit step: Step): step.Self =
        step.self(buildFn, f, displayName)

      override protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
        step.next[H](buildFn, f, displayName)

      def build: CustomHook[I, Unit] =
        buildReturning(_ => ())

      def buildReturning[O](f: Ctx => O): CustomHook[I, O] =
        buildFn(f)

      def buildReturning[O](f: CtxFn[O])(implicit step: Step): CustomHook[I, O] =
        buildReturning(step.squash(f)(_))
    }

    object Subsequent extends Custom_SubsequentDsl

    final class FirstStep[I] extends Api.AbstractStep {
      override type Self     = First[I]
      override type Next[H1] = Subsequent[I, HookCtx.I1[I, H1], ({ type F[A] = (I, H1) => A})#F]

      def self(initFirst: I => Unit, f: I => Any): Self =
        new First[I](i => {
          initFirst(i)
          f(i)
        })

      def next[H1](initFirst: I => Unit, hook1: I => H1, displayName: String): Next[H1] = {
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
        new Subsequent[I, HookCtx.I1[I, H1], ({ type F[A] = (I, H1) => A})#F](displayName)(build)
      }
    }

    implicit def firstStep[P]: FirstStep[P] =
      new FirstStep

    trait SubsequentStep[I, _Ctx, _CtxFn[_]] extends Api.SubsequentStep[_Ctx, _CtxFn] {
      final type Self = Subsequent[I, Ctx, CtxFn]
      final def self: (BuildFn[I, Ctx], Ctx => Any, String) => Self =
        (buildPrev, initNextHook, displayName) => {
          val buildNext: BuildFn[I, Ctx] =
            new BuildFn[I, Ctx] {
              override def apply[O](f: Ctx => O) = {
                buildPrev { ctx =>
                  initNextHook(ctx)
                  f(ctx)
                }
              }
            }
          new Subsequent[I, Ctx, CtxFn](displayName)(buildNext)
        }
      def next[A]: (BuildFn[I, Ctx], Ctx => A, String) => Next[A]
    }

    object SubsequentStep extends Custom_SubsequentSteps {
      type To[I, Ctx, CtxFn[_], N[_]] = SubsequentStep[I, Ctx, CtxFn] { type Next[A] = N[A] }
    }
  }

  // ===================================================================================================================

  private final case class ReusableDepState[+A](value: A, rev: Int) {
    var skipTest = true
    val asTuple = (value, rev)
  }

  def reusableDeps[D](implicit r: Reusability[D]): CustomHook[() => D, (D, Int)] =
    CustomHook[() => D]
      .useState[Option[ReusableDepState[D]]](None)
      .buildReturning { (getDeps, depsState) =>
        val next = getDeps()
        val nextDepState =
          depsState.value match {

            // React is calling us again after the setState below
            // Don't re-test reusability, we could end up in an infinite-loop (see #1027)
            case Some(s) if s.skipTest =>
              s.skipTest = false
              s

            // Previous result can be reused
            case Some(prevState) if r.test(prevState.value, next) =>
              prevState

            // Not reusable
            case o =>
              val nextRev     = o.fold(1)(_.rev + 1)
              val nextState   = ReusableDepState(next, nextRev)
              val updateState = depsState.setState(Some(nextState)) // this causes a hook re-eval
              DefaultEffects.Sync.runSync(updateState)
              nextState
          }
        nextDepState.asTuple
      }

  def reusableByDeps[D, A](create: (D, Int) => A)(implicit r: Reusability[D]): CustomHook[() => D, Reusable[A]] =
    reusableDeps[D].map { case (d, rev) => Reusable.implicitly(rev).withValue(create(d, rev)) }

  protected[hooks] final case class ReuseComponentProps[A](value: A, render: A => VdomNode)(implicit val reusability: Reusability[A]) {
    def renderVdom: VdomNode = render(value)
  }

  private implicit val reusabilityInstance: Reusability[ReuseComponentProps[Any]] =
    Reusability( (a, b) => a.reusability.test(a.value, b.value) )

  // Component for reuse. Must be stable.
  private lazy val ReuseComponent = React.memo(ScalaFnComponent[ReuseComponentProps[Any]](_.renderVdom))

  def shouldComponentUpdate[D](render: D => VdomNode)(implicit r: Reusability[D]): CustomHook[() => D, VdomNode] = {
    CustomHook[() => D]
      .buildReturning{ deps =>
        ReuseComponent(ReuseComponentProps(deps(), render).asInstanceOf[ReuseComponentProps[Any]] )
      }
    }

  lazy val useForceUpdate: CustomHook[Unit, Reusable[DefaultEffects.Sync[Unit]]] = {
    val inc: Int => Int = _ + 1
    CustomHook[Unit]
      .useState(0)
      .buildReturning($ => {
        val s = $.hook1
        Reusable.implicitly(s.value).withLazyValue(s.modState.map(_(inc)).value)
      })
  }

}
