package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.vdom.VdomNode

class HookResult[+A](protected[react] val hook: () => A) extends AnyVal {
  def map[B](f: A => B): HookResult[B] =
    HookResult(f(hook()))

  def flatMap[B](f: A => HookResult[B]): HookResult[B] =
    HookResult(f(hook()).hook())
}

object HookResult {
  @inline def apply[A](a: => A): HookResult[A] =
    new HookResult(() => a)

  implicit class HookOps[I, O](private val h: I => HookResult[O]) extends AnyVal {
    def contramap[A](f: A => I): A => HookResult[O] = f andThen h
  }

  implicit def autoLift[A](a: => A)(implicit f: A => VdomNode): HookResult[VdomNode] =
    HookResult(f(a))

}

