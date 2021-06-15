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
