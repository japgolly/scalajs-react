Port
====

extra/src/main/scala-2/japgolly/scalajs/react/extra/internal/RouterMacros.scala

extra/src/main/scala-2/japgolly/scalajs/react/extra/components/TriStateCheckbox.scala

extra/src/main/scala-2/japgolly/scalajs/react/extra/router/Actions.scala
extra/src/main/scala-2/japgolly/scalajs/react/extra/router/Dsl.scala
extra/src/main/scala-2/japgolly/scalajs/react/extra/router/package.scala
extra/src/main/scala-2/japgolly/scalajs/react/extra/router/ResolutionWithProps.scala
extra/src/main/scala-2/japgolly/scalajs/react/extra/router/Router.scala
extra/src/main/scala-2/japgolly/scalajs/react/extra/router/RouterWithPropsConfig.scala
extra/src/main/scala-2/japgolly/scalajs/react/extra/router/RoutingRule.scala
extra/src/main/scala-2/japgolly/scalajs/react/extra/router/RoutingRules.scala


Soon
====

* RC3

Later
=====

* Resolve TODOs

* Microlibs!
  * Rename `macro-utils` to `compile-time`
  * Move into it and rename:
    * `CompileTimeTestUtil`
    * `CompileTimeInfo`
    * `NewMacroUtils`
    * `Ops` => `Extensions`
  * Maybe define an export package = `{obj Utils, Extensions._, fail*}`
  * Make erased versions of `<:<` & `=:=` and use here
  * Add inline string fns
  * Add inline string -> int

* ScalaJsReactConfig
  * Make scala2 use macros (i.e. bugfix)
  * Add tests that new ver works as expected
  * Sync scala2 version to scala3 version
  * Add a proper doc/guide
  * Read from scalac flags
  * Replace assertWarn in extra
  * Add ReusabilityOverride instance to ReusabilityOverlay that wraps the `install` methods, and simplify ReusabilityOverlayMacros

* Component names
  * Test dynamic component names (will `inline` arg prevent?)
  * Add/review/improve name elision tests

* Remove runtime footprint
  * JsRepr
  * OptionLike for Option
  * Component builder
  * sourcecode

* Test Scala3 Reusability macro newness

* Can comp-builder endo-fns be greatly simplified using the new polymorphic fns?

* Ensure we have all the edge-cases for the builder macros tested.
  See f7f158dc8c999dcbd4935e9900f8411f8170af15

Finally
=======
* Remove `mv_src`
* Remove `reuse`
* Remove `copy3`
* Remove `delete-shit`
