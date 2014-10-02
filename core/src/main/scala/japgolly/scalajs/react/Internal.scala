package japgolly.scalajs.react

import scala.scalajs.js._

private[react] object Internal {

  final class FnResults[R](aa: => R, bb: => R) {
    lazy val a = aa
    lazy val b = bb
  }

  final class FnComposer[R](m: FnResults[R] => R) {
    def apply[A](uf: UndefOr[A => R], g: A => R) =
      uf.fold(g)(f => a => m(new FnResults(f(a), g(a))))

    def apply[A, B](uf: UndefOr[(A, B) => R], g: (A, B) => R) =
      uf.fold(g)(f => (a,b) => m(new FnResults(f(a,b), g(a,b))))

    def apply[A, B, C](uf: UndefOr[(A, B, C) => R], g: (A, B, C) => R) =
      uf.fold(g)(f => (a,b,c) => m(new FnResults(f(a,b,c), g(a,b,c))))
  }

  val fcUnit = new FnComposer[Unit](r => {r.a; r.b})
  val fcEither = new FnComposer[Boolean](r => r.a || r.b)
}
