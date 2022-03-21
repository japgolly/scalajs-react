package japgolly.scalajs.react.test

import japgolly.scalajs.react.test.internal._

object ReactTestUtilsConfig extends ReactTestUtilsConfigTypes {

  object aroundReact extends AroundReact {

    private def default: AroundReact =
      ReactTestUtilsConfigMacros.aroundReact

    private var value: AroundReact =
      default

    def get: AroundReact =
      value

    def set(v: AroundReact): Unit =
      value = v

    override def start(): () => Unit =
      value.start()
  }
}
