package japgolly.scalajs.react.test

import japgolly.scalajs.react.facade._
import japgolly.scalajs.react.util.ConsoleHijack

object InitTestEnv {
  def apply(): Unit = ()

  // Log versions
  println("React version = " + React.version)
  // println("ReactDOM version = " + ReactDOM.version)
  // println("ReactDOMClient version = " + ReactDOMClient.version)
  // println("ReactDOMServer version = " + ReactDOMServer.version)

  // Treat React warnings as failures
  ConsoleHijack.fatalReactWarnings.install()
}
