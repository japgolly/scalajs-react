* Move into core as extensions:
  * CallbackOption
    * keyCodeSwitch
    * keyEventSwitch
    * asEventDefault
  * CallbackTo
    * asEventDefault

* userdefined.(Unsafe)Effect?

* Document modules

* Remove temp scripts

* Add WithAsyncEffect / WithEffectAsync to component.*

* Add WithEffect/WithAsyncEffect or support more than just the default effect type
  * DefaultReusabilityOverlay
  * Hooks.scala
  * Router
  * RouterCtl
  * RouterWithPropsConfig.scala
  * RoutingRule
  * RoutingRules
  * StateSnapshot (*Pure)
  * TriStateCheckbox
and then in testUtil:
  * TestBroadcaster

* Test React.Suspense

* Re-enable cats/monocle/scalaz tests

* Revise all module names (and be consistent with local module dir & sbt names)


./toggle tests/src/test/scala-2/japgolly/scalajs/react/core/ReusabilityTest2.scala.off
./toggle tests/src/test/scala-2/japgolly/scalajs/react/core/ScalaSpecificHooksTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ContextTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/HooksTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ReactDOMServerTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/RefTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ReusableFnTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ScalaBuilderTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ScalaComponentTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ScalaFnComponentTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/vdom/PrefixedTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/vdom/UnprefixedTest.scala.off

./toggle tests/src/test/scala/japgolly/scalajs/react/extra/BroadcasterTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/EventListenerTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/OnUnmountTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/PxTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/router/DslTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/router/Router2Test.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/router/RouterP2Test.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/router/RouterTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/router/RouterTestHelp.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/router/SimHistory.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/StateSnapshotTest.scala.off

./toggle tests/src/test/scala/japgolly/scalajs/react/test/JsEnvUtils.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/test/ReactTestVarTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/test/TestTest.scala.off

./toggle tests/src/test/scala/japgolly/scalajs/react/MiscTest.scala.off

./toggle tests/src/test/scala/japgolly/scalajs/react/CatsTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/MonocleTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/ScalazTest.scala.off
