package japgolly.scalajs.react.hooks

sealed trait HookCtxFn { type Fn[A] }

object HookCtxFn {

  sealed trait P1[P, H1] extends HookCtxFn { override type Fn[A] = (P, H1) => A }
  sealed trait P2[P, H1, H2] extends HookCtxFn { override type Fn[A] = (P, H1, H2) => A }
  sealed trait P3[P, H1, H2, H3] extends HookCtxFn { override type Fn[A] = (P, H1, H2, H3) => A }

}
