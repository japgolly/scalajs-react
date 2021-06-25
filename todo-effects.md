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

* Add effect trans methods/extensions  (eg. `.to[IO]` and `.to[CallbackTo]`)?
* resolve todos
* Audit all use of DefaultEffects in coreGeneral

* Test coreDef* from downstream-tests
* Add tests with callbacks accepting non-CallbackTo types

* Show `modules.gv.svg` in doc and changelog
  * clarify new imports
  * migration
  * update getting-started/usage instructions

* removal of state-monad extensions
  * update changelog
  * update FP.md

* Fix ScalaDoc links

* At the very end, compare the total diff of the tests - it should be as minimal as possible and if there
  are any mandatory changes, confirm them and add to changelog & migration guide (shouldn't be)

* Remove temp scripts

========================================================================================================================

```s
callback              -- callback                   _sjs1_2.13-2.0.0-SNAPSHOT.pom
callbackExtCats       -- callback-ext-cats          _sjs1_2.13-2.0.0-SNAPSHOT.pom
callbackExtCatsEffect -- callback-ext-cats-effect   _sjs1_2.13-2.0.0-SNAPSHOT.pom
coreDefCallback       -- core                       _sjs1_2.13-2.0.0-SNAPSHOT.pom
coreDefCatsEffect     -- core-cats-effect           _sjs1_2.13-2.0.0-SNAPSHOT.pom
coreExtCats           -- core-ext-cats              _sjs1_2.13-2.0.0-SNAPSHOT.pom
coreExtCatsEffect     -- core-ext-cats-effect       _sjs1_2.13-2.0.0-SNAPSHOT.pom
coreGeneric           -- core-generic               _sjs1_2.13-2.0.0-SNAPSHOT.pom
extra                 -- extra                      _sjs1_2.13-2.0.0-SNAPSHOT.pom
extraExtMonocle2      -- extra-ext-monocle2         _sjs1_2.13-2.0.0-SNAPSHOT.pom
extraExtMonocle3      -- extra-ext-monocle3         _sjs1_2.13-2.0.0-SNAPSHOT.pom
facadeMain            -- facade                     _sjs1_2.13-2.0.0-SNAPSHOT.pom
facadeTest            -- facade-test                _sjs1_2.13-2.0.0-SNAPSHOT.pom
testUtil              -- test                       _sjs1_2.13-2.0.0-SNAPSHOT.pom
util                  -- util                       _sjs1_2.13-2.0.0-SNAPSHOT.pom
utilCatsEffect        -- util-cats-effect           _sjs1_2.13-2.0.0-SNAPSHOT.pom
```
