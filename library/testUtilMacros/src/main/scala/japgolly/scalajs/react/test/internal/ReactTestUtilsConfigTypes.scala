package japgolly.scalajs.react.test.internal

import japgolly.scalajs.react.util.ConsoleHijack
import japgolly.scalajs.react.util.Effect

object ReactTestUtilsConfigTypes {

  trait AroundReact {

    def start(): () => Unit

    def sync[A](body: => A): A = {
      val stop = start()
      try
        body
      finally
        stop()
    }

    def apply[F[_]: Effect, A](body: F[A]): F[A] = {
      Effect[F].flatMap(Effect[F].delay(start()))( stop =>
        Effect[F].finallyRun(body, Effect[F].delay(stop()))
      )
    }
  }

  object AroundReact {

    object id extends AroundReact {
      override def start(): () => Unit = () => ()
      override def sync[A](body: => A): A = body
      override def apply[F[_]: Effect, A](body: F[A]): F[A] = body
    }

    object fatalReactWarnings extends AroundReact {
      val consoleHijack = ConsoleHijack.fatalReactWarnings
      override def start(): () => Unit =
        consoleHijack.install()
    }
  }
}

trait ReactTestUtilsConfigTypes {
  type AroundReact = ReactTestUtilsConfigTypes.AroundReact
  val  AroundReact = ReactTestUtilsConfigTypes.AroundReact
}
