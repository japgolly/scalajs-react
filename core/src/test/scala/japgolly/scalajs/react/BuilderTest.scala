package japgolly.scalajs.react

// import scala.language.`3.0`
import scala.language.implicitConversions

import japgolly.scalajs.react.internal.ComponentBuilderMacros.*
import japgolly.scalajs.react.component.builder.*
import japgolly.scalajs.react.vdom.html_<^.*
// import japgolly.scalajs.react.vdom.html_<^.given

object BuiderTest {
  import Builder.*
  import CompileTimeTestUtil.*

  val step1 = new Step1[Int]("")

  // val i = 1
  // showTasty( i )
  // showTasty( i: Long )

  // val b1 = new B1
  // showTasty( b1.render() )
  // showTasty( (s => s.backend.render): RenderFn[Int, Unit, B1] )

  println("123xc")

  showCode(step1.renderBackend[B1] ) //        Apply( Select(New(Ident(B1 )),<init>), List(         ))
  showCode(step1.renderBackend[B2] ) //        Apply( Select(New(Ident(B2 )),<init>), List(Ident(bs)))
  // showCode(step1.renderBackend[B3a]) // Apply( Apply( Select(New(Ident(B3a)),<init>), List(Ident(bs))) ,List(Ident(imp)) )
  // showCode(step1.renderBackend[B3b]) // Apply( Apply( Select(New(Ident(B3b)),<init>), List(Ident(bs))) ,List(Ident(imp)) )
  // showCode(step1.renderBackend[BT] )
  // step1.renderBackend[B1x]

  // step1.renderBackend[B1]  // List(List())
  // step1.renderBackend[B2]  // List(List($))
  // step1.renderBackend[B3a] // List(List($), List(i))
  // step1.renderBackend[B3b] // List(List($), List(i))
  // step1.renderBackend[BT]

  // val step3 = step1.backend[B1](_ => new B1)
  // showCode(step3.renderBackend)
  // showTasty(step3.renderBackendFn)

  val bs: BackendScope[Int, Unit] = null

  // showTasty(new B1)
  // showTasty(new B1())
  // showTasty(new B1a)
  // showTasty(new B1a())
  // showTasty(new B2(bs))
  // showTasty(new B3a(bs))
  // showTasty(new B3b(bs))

  // showTasty(new BP0[Int])    // Apply( TypeApply(Select(New(AppliedTypeTree(Ident(BP0),List(Ident(Int)))),<init>), List(TypeTree[TypeRef(SCALA,class Int)])), List()                     )
  // showTasty(new BP1(3))      // Apply( TypeApply(Select(New(                Ident(BP1)                  ),<init>), List(TypeTree[TypeRef(SCALA,class Int)])), List(Literal(Constant(3))) )
  // showTasty(new BP1[Int](3)) // Apply( TypeApply(Select(New(AppliedTypeTree(Ident(BP1),List(Ident(Int)))),<init>), List(TypeTree[TypeRef(SCALA,class Int)])), List(Literal(Constant(3))) )
  // //                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  // //                                                                           TypeApply

  // SCALA = TermRef(ThisType(TypeRef()),object scala)
}