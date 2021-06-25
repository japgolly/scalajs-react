* userdefined.(Unsafe)Effect?

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

* Add tests with callbacks accepting non-CallbackTo types

* Export Effects as ReactEffect?

* Include a default IORuntime in coreDefCE?

* Show `modules.gv.svg` in doc and changelog
  * clarify new imports
  * migration
  * update getting-started/usage instructions

* At the very end, compare the total diff of the tests - it should be as minimal as possible and if there
  are any mandatory changes, confirm them and add to changelog & migration guide (shouldn't be)

* removal of state-monad extensions
  * update changelog
  * update FP.md

* Add effect trans methods/extensions  (eg. `.to[IO]` and `.to[CallbackTo]`)?

* Test coreDef* from downstream-tests

* Fix ScalaDoc links

* publishLocal, confirm the export set, confirm the module names

* resolve todos
