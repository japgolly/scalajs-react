package japgolly.scalajs.react.test

import japgolly.scalajs.react.util.ConsoleHijack

object InitTestEnv {
  def apply(): Unit = ()

  // Treat React warnings as failures
  ConsoleHijack.fatalReactWarnings.install()
}
