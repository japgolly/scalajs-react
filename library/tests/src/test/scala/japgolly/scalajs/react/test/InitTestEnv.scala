package japgolly.scalajs.react.test

object InitTestEnv {
  def apply(): Unit = ()

  // Treat React warnings as failures
  ReactTestUtilsConfig.aroundReact.set(ReactTestUtilsConfig.AroundReact.fatalReactWarnings)
}
