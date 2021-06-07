Port
====

test/src/test/scala-2/japgolly/scalajs/react/ElisionTest.scala

ghpages


Later
=====

* Resolve TODOs

* Microlibs!
  * Move/Delete `CompileTime{Info,TestUtil}`
  * Make erased versions of `<:<` & `=:=` and use here

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

* Manually run AsyncTest

Finally
=======
* Remove `mv_src`
* Remove `reuse`
* Remove `copy3`
* Remove `delete-shit`
