Port
====

test/src/test/scala-2/japgolly/scalajs/react/core/ReusabilityTest.scala
test/src/test/scala-2/japgolly/scalajs/react/core/ReusableFnTest.scala
test/src/test/scala-2/japgolly/scalajs/react/core/vdom/DevOnlyTest.scala
test/src/test/scala-2/japgolly/scalajs/react/ElisionTest.scala
test/src/test/scala-2/japgolly/scalajs/react/extra/router/DslTest.scala
test/src/test/scala-2/japgolly/scalajs/react/extra/router/Router2Test.scala
test/src/test/scala-2/japgolly/scalajs/react/extra/router/RouterP2Test.scala
test/src/test/scala-2/japgolly/scalajs/react/extra/router/RouterTestHelp.scala
test/src/test/scala-2/japgolly/scalajs/react/extra/router/RouterTest.scala
test/src/test/scala-2/japgolly/scalajs/react/extra/router/SimHistory.scala

ghpages


Later
=====

* Resolve TODOs

* Microlibs!
  * Rename `macro-utils` to `compile-time`
  * Move into it and rename:
    * `CompileTimeTestUtil`
    * `CompileTimeInfo`
    * `InferenceUtil`
    * `NewMacroUtils`
    * `Ops` => `Extensions`
  * Maybe define an export package = `{obj Utils, Extensions._, fail*}`
  * Make erased versions of `<:<` & `=:=` and use here
  * Add inline string fns
  * Add inline string -> int
  * assertContains: make a variant that accepts multiple legal substrs

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
* Test Scala3 Callback ResultGuard Proof
* Test Scala3 Router Macro failure cases (might need to manually report errors if auto-error isn't clear)
* Move out the compile time tests in core/3
* Run the AsyncTests in gh-pages

* Can comp-builder endo-fns be greatly simplified using the new polymorphic fns?

* Ensure we have all the edge-cases for the builder macros tested.
  See f7f158dc8c999dcbd4935e9900f8411f8170af15

* Undo the commit after 12c6a3ee31eaac2b848494c44332f3fc8da30400
  than reverts all the erased stuff

* Re-enable elision tests in bin/ci

* Reusability macros generating boxed code, hopefully fixed on Scala 3 master -- check!

Finally
=======
* Remove `mv_src`
* Remove `reuse`
* Remove `copy3`
* Remove `delete-shit`
