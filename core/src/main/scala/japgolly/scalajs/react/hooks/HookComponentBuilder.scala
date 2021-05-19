package japgolly.scalajs.react.hooks

import Hooks._
import japgolly.scalajs.react.vdom.VdomNode

object HookComponentBuilder {

  case class Component[P](value: P => VdomNode) // TODO: Temp

  // def apply[P]: HookComponentBuilder[P, TupleHList.Empty] =
  //   new HookComponentBuilder[P, TupleHList.Empty] {}

  def apply[P]: DslP0[P] =
    new DslP0

  final class DslP0[P] {

    // type Next[A] = DslP1[P, A]
    // private def next[H1](hook1: P => H1): Next[H1] = new DslP1(hook1)

    type Next[H1] = DslMulti[P, CtxP1[P, H1], ({type L[A] = (P, H1) => A})#L]
    private def next[H1](hook1: P => H1): Next[H1] = {
      type Ctx = CtxP1[P, H1]
      // type F[A] = (P, H1) => A
      // val renderF: F[VdomNode] => Component[P] =
      //   f => new Component(p => {
      //     val h1 = hook1(p)
      //     f(p, h1)
      //   })
      val renderC: (Ctx => VdomNode) => Component[P] =
        f => new Component(p => {
          val h1 = hook1(p)
          f(CtxP1(p, h1))
        })
      new DslMulti(renderC)
    }

    def useState[S](initialState: P => S): Next[UseState[S]] =
      next(p => UseState.unsafeCreate(initialState(p)))
  }

  final case class CtxP1[P, H1](props: P, hook1: H1)
  final case class CtxP2[P, H1, H2](props: P, hook1: H1, hook2: H2)

  // final class DslP1[P, H1](hook1: P => H1) {
  //   type Ctx = CtxP1[P, H1]
  //   type Next[A] = DslP2[P, H1, A]
  //   private def next[H2](hook2: (P, H1) => H2): Next[H2] = new DslP2(hook1, hook2)
  //   // private def nextC[H2](hook2: Ctx => H2): Next[H2] = nextF((p, h1) => hook2(CtxP1(p, h1)))

  //   def useState[S](initialState: Ctx => S): Next[UseState[S]] = useState((p, h1) => initialState(CtxP1(p, h1)))
  //   def useState[S](initialState: (P, H1) => S): Next[UseState[S]] =
  //     next((p, h1) => UseState.unsafeCreate(initialState(p, h1)))

  //   def render(f: Ctx => VdomNode): Component[P] = render((p, h1) => f(CtxP1(p, h1)))
  //   def render(f: (P, H1) => VdomNode): Component[P] =
  //     new Component(p => {
  //       val h1 = hook1(p)
  //       f(p, h1)
  //     })
  // }

  // final class DslP2[P, H1, H2](hook1: P => H1, hook2: (P, H1) => H2) {
  //   type Ctx = CtxP2[P, H1, H2]

  //   def render(f: Ctx => VdomNode): Component[P] = render((p, h1, h2) => f(CtxP2(p, h1, h2)))
  //   def render(f: (P, H1, H2) => VdomNode): Component[P] =
  //     new Component(p => {
  //       val h1 = hook1(p)
  //       val h2 = hook2(p, h1)
  //       f(p, h1, h2)
  //     })
  // }

  // ==========================================================================================================================

  trait NextMulti[P, Ctx, F[_]] {
    type Next[A]
    def map[A]: F[A] => (Ctx => A)
    def next[A](f: Ctx => A): Next[A]
  }
  object NextMulti {
    type Aux[P, Ctx, F[_], N[_]] = NextMulti[P, Ctx, F] { type Next[A] = N[A] }

    type For1[P, H1] = Aux[
      P,
      CtxP1[P, H1],
      ({type L[A] = (P, H1) => A})#L,
      ({type L[H2] = DslMulti[P, CtxP2[P, H1, H2], ({type F[A] = (P, H1, H2) => A})#F]})#L]

    implicit def for1[P, H1]: For1[P, H1] =
      ???
      // new For1[P, H1] {
      //   override type Next[A]
      //   override def map[A]: F[A] => (Ctx => A)
      //   override def next[A](f: Ctx => A): Next[A]
      // }
  }

  final class DslMulti[P, Ctx, F[_]](
        renderC: (Ctx => VdomNode) => Component[P],
        // renderF: F[VdomNode] => Component[P],
      ) {

    def useState[S](initialState: Ctx => S)(implicit n: NextMulti[P, Ctx, F]): n.Next[UseState[S]] =
      n.next(ctx => UseState.unsafeCreate(initialState(ctx)))

    def useState[S](initialState: F[S])(implicit n: NextMulti[P, Ctx, F]): n.Next[UseState[S]] =
      useState(n.map(initialState)(_))

    def render(f: Ctx => VdomNode): Component[P] =
      renderC(f)

    def render(f: F[VdomNode])(implicit n: NextMulti[P, Ctx, F]): Component[P] =
      render(n.map(f)(_))
      // renderF(f)
  }

}

// Optional: Props

// trait HookComponentBuilder[P, H] {
//   def useState[S](initialState: P => S)(implicit a: TupleHList.Append[H, Hooks.UseState[S]]): HookComponentBuilder[P, a.Result] =
//     new HookComponentBuilder[P, a.Result] {
//     }
// }
