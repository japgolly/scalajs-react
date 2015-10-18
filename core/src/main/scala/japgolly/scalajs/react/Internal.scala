package japgolly.scalajs.react

import scala.scalajs.js._

private[react] object Internal {

  final class FnComposer[R](compose: (=> R, => R) => R) {
    def apply[A](uf: UndefOr[A => R], g: A => R) =
      uf.fold(g)(f => a => compose(f(a), g(a)))

    def apply[A, B](uf: UndefOr[(A, B) => R], g: (A, B) => R) =
      uf.fold(g)(f => (a, b) => compose(f(a, b), g(a, b)))

    def apply[A, B, C](uf: UndefOr[(A, B, C) => R], g: (A, B, C) => R) =
      uf.fold(g)(f => (a, b, c) => compose(f(a, b, c), g(a, b, c)))
  }

  val fcUnit   = new FnComposer[Callback]           (_ >> _)
  val fcEither = new FnComposer[CallbackTo[Boolean]](_ || _)
}
