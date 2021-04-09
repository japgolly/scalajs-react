Port
====

core/src/main/scala-2/japgolly/scalajs/react/React.scala
core/src/main/scala-2/japgolly/scalajs/react/Reusability.scala
core/src/main/scala-2/japgolly/scalajs/react/Reusable.scala
core/src/main/scala-2/japgolly/scalajs/react/ScalaJsReactConfig.scala

core/src/main/scala-2/japgolly/scalajs/react/component/builder/EntryPoint.scala

Soon
====
* RC2 (?)

Later
=====

* Microlibs!
  * Rename `macro-utils` to `compile-time`
  * Move into it and rename:
    * `CompileTimeTestUtil`
    * `CompileTimeInfo`
    * `NewMacroUtils`
    * `Ops` => `Extensions`
  * Maybe define an export package = `{obj Utils, Extensions._, fail*}`
  * Make erased versions of `<:<` & `=:=` and use here

* Remove runtime footprint
  * JsRepr
  * OptionLike for Option
  * Component builder
  * sourcecode

* Ensure everything in scala-3's xxx is covered
  * `react/package.scala`
  * `ReactExtensions`

* Can comp-builder endo-fns be greatly simplified using the new polymorphic fns?

* Ensure we have all the edge-cases for the builder macros tested.
  See f7f158dc8c999dcbd4935e9900f8411f8170af15

Finally
=======
* Remove `mv_src`
* Remove `reuse`
* Remove `copy3`
* Remove `delete-shit`
