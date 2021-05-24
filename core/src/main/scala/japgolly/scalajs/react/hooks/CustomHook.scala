package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.PropsChildren

final case class CustomHook[I, O](unsafeInit: I => O) {
  def apply(i: I): CustomHook[Unit, O] =
    CustomHook[Unit, O](_ => unsafeInit(i))
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

}
