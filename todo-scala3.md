Port
====

core/src/main/scala-2/japgolly/scalajs/react/React.scala
core/src/main/scala-2/japgolly/scalajs/react/ReactDOM.scala
core/src/main/scala-2/japgolly/scalajs/react/ReactDOMServer.scala
core/src/main/scala-2/japgolly/scalajs/react/Ref.scala
core/src/main/scala-2/japgolly/scalajs/react/Reusability.scala
core/src/main/scala-2/japgolly/scalajs/react/Reusable.scala
core/src/main/scala-2/japgolly/scalajs/react/ScalaJsReactConfig.scala
core/src/main/scala-2/japgolly/scalajs/react/StateAccessor.scala
core/src/main/scala-2/japgolly/scalajs/react/UpdateSnapshot.scala

core/src/main/scala-2/japgolly/scalajs/react/internal/CompileTimeInfo.scala

core/src/main/scala-2/japgolly/scalajs/react/component/builder/Builder.scala
core/src/main/scala-2/japgolly/scalajs/react/component/builder/EntryPoint.scala
core/src/main/scala-2/japgolly/scalajs/react/component/builder/Lifecycle.scala
core/src/main/scala-2/japgolly/scalajs/react/component/builder/ViaReactComponent.scala

core/src/main/scala-2/japgolly/scalajs/react/component/Generic.scala
core/src/main/scala-2/japgolly/scalajs/react/component/InspectRaw.scala
core/src/main/scala-2/japgolly/scalajs/react/component/Js.scala
core/src/main/scala-2/japgolly/scalajs/react/component/JsBaseComponentTemplate.scala
core/src/main/scala-2/japgolly/scalajs/react/component/JsFn.scala
core/src/main/scala-2/japgolly/scalajs/react/component/JsForwardRef.scala
core/src/main/scala-2/japgolly/scalajs/react/component/Scala.scala
core/src/main/scala-2/japgolly/scalajs/react/component/ScalaFn.scala
core/src/main/scala-2/japgolly/scalajs/react/component/ScalaForwardRef.scala
core/src/main/scala-2/japgolly/scalajs/react/component/Template.scala

Blocked
=======

* Waiting on
    * https://github.com/scalatest/scalatest/issues/1982
    * https://github.com/typelevel/cats-testkit-scalatest/issues/128
    * https://github.com/typelevel/discipline-scalatest/issues/169
  to
    * Re-enable module: catsEffect
    * Re-enable module: monocle3
    * Re-enable module: monocleCats

* Resolve: `TODO: bm4 currently unavailable with Scala 3`

Finally
=======
* Remove `mv_src`
* Remove `reuse`
* Remove `copy3`
* Remove `delete-shit`

Later
=====
* Replace microlibs' macro-utils with compile-time, make erased versions of `<:<` & `=:=`, and use here
* Remove runtime footprint
  * JsRepr
  * OptionLike for Option
  * Component builder
* Ensure everything in scala-3's xxx is covered
  * `react/package.scala`
  * `ReactExtensions`
* Move `CompileTimeTestUtil` into microlibs
