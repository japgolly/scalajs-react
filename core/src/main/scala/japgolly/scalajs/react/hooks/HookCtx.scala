package japgolly.scalajs.react.hooks

object HookCtx {

  def apply[P, H1](props: P, hook1: H1): P1[P, H1] =
    new P1(props, hook1)

  def apply[P, H1, H2](props: P, hook1: H1, hook2: H2): P2[P, H1, H2] =
    new P2(props, hook1, hook2)

  def apply[P, H1, H2, H3](props: P, hook1: H1, hook2: H2, hook3: H3): P3[P, H1, H2, H3] =
    new P3(props, hook1, hook2, hook3)

  abstract class P0[+P](final val props: P)

  class P1[+P, +H1](props: P, final val hook1: H1) extends P0(props) {
    override def toString = s"HookCtx(\n  props = $props,\n  hook1 = $hook1)"
    def apply1[A](f: (P, H1) => A): A = f(props, hook1)
  }

  class P2[+P, +H1, +H2](props: P, hook1: H1, final val hook2: H2) extends P1(props, hook1) {
    override def toString = s"HookCtx(\n  props = $props,\n  hook1 = $hook1,\n  hook2 = $hook2)"
    def apply2[A](f: (P, H1, H2) => A): A = f(props, hook1, hook2)
  }

  class P3[+P, +H1, +H2, +H3](props: P, hook1: H1, hook2: H2, final val hook3: H3) extends P2(props, hook1, hook2) {
    override def toString = s"HookCtx(\n  props = $props,\n  hook1 = $hook1,\n  hook2 = $hook2,\n  hook3 = $hook3)"
    def apply3[A](f: (P, H1, H2, H3) => A): A = f(props, hook1, hook2, hook3)
  }

}
