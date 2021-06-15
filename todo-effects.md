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


./toggle tests/src/test/scala-2/japgolly/scalajs/react/core/ReusabilityTest2.scala.old
./toggle tests/src/test/scala-2/japgolly/scalajs/react/core/ScalaSpecificHooksTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/CatsTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/AsyncCallbackTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/CallbackTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ContextTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/HooksTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/JsComponentEs6Test.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/JsFnComponentTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/JsLikeComponentTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/RawComponentEs6Test.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ReactDOMServerTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/RefTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ReusabilityTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ReusableFnTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ReusableTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ScalaBuilderTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ScalaComponentTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ScalaFnComponentTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/StackSafety.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/StateAccessorTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/StateAccessTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/vdom/DevOnlyTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/vdom/PrefixedTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/vdom/ReactAttrTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/vdom/UnprefixedTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/core/vdom/VdomTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/BroadcasterTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/EventListenerTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/OnUnmountTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/PxTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/router/DslTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/router/Router2Test.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/router/RouterP2Test.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/router/RouterTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/router/RouterTestHelp.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/router/SimHistory.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/extra/StateSnapshotTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/MiscTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/MonocleTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/ScalazTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/test/InferenceHelpers.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/test/JsEnvUtils.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/test/ReactTestVarTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/test/TestTest.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/test/TestTimer.scala.old
./toggle tests/src/test/scala/japgolly/scalajs/react/test/TestUtil.scala.old
