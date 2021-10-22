package japgolly.scalajs.react.test.internal

import japgolly.scalajs.react.util.ConsoleHijack

object ReactTestUtilsConfigTypes {

  trait AroundReact {

    def start(): () => Unit

    def apply[A](body: => A): A = {
      val stop = start()
      try
        body
      finally
        stop()
    }
  }

  object AroundReact {

    object id extends AroundReact {
      override def start(): () => Unit = () => ()
      override def apply[A](a: => A): A = a
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
