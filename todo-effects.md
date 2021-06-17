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

* Add WithEffect/WithAsyncEffect or support more than just the default effect type
  * component.*
  * DefaultReusabilityOverlay
  * Hooks.scala
  * Router
  * RouterCtl
  * RouterWithPropsConfig.scala
  * RoutingRule
  * RoutingRules
  * StateAccess (*Pure)
  * StateSnapshotF
  * TriStateCheckbox
and then in testUtil:
  * TestBroadcaster
  * ReactTestVarF

* Test React.Suspense

* Re-enable cats/monocle/scalaz tests

* Revise all module names (and be consistent with local module dir & sbt names)

==============================================================================================================

## Problem
Can't have a provided-scope, overridable DefaultEffects module.
It works until linking in certain circumstances.

Example, `def f: Sync[Unit]` becomes `def f: Any(Ref?)` in abstract (`coreGeneral`),
but then `def f: Trampoline` in specific (`tests`) and fails to link.

```
[error] Referring to non-existent method japgolly.scalajs.react.Ref$Full.get()japgolly.scalajs.react.callback.Trampoline
```

## Rejected Solution: wrapper
Provide a constant (non-AnyVal) wrapper that has the same erasure.
The problem is that will prevent instances of `Callback`/`IO` being returned in general.
Instead it would be `Blah[Callback]` or `Blah[IO]` which would be very annoying.

## Terrible Solution: two separate copies
Absolutely terrible but a potential last resort. It would completely prevent abstraction.

## Potential Solution: parameterise and never return Sync directly
Tried and it works!

==============================================================================================================

./toggle tests/src/test/scala-2/japgolly/scalajs/react/core/ReusabilityTest2.scala.off
./toggle tests/src/test/scala-2/japgolly/scalajs/react/core/ScalaSpecificHooksTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ContextTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/HooksTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/core/ReactDOMServerTest.scala.off
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

./toggle tests/src/test/scala/japgolly/scalajs/react/MiscTest.scala.off

./toggle tests/src/test/scala/japgolly/scalajs/react/CatsTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/MonocleTest.scala.off
./toggle tests/src/test/scala/japgolly/scalajs/react/ScalazTest.scala.off
