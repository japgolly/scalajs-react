package japgolly.scalajs.react.example.examples

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.ReactVDom.Modifier
import japgolly.scalajs.react.vdom.ReactVDom.all._


/**
  * Created by chandrasekharkode on 11/17/14.
  */
object SingleSide {

   val singleSideComponent = ReactComponentB[(String, Modifier)]("singleSideComponent")
     .render(P => {
         val (scalaCode, component) = P
         div(`class` := "row")(
           div(`class` := "col-md-6")(
             pre(code(scalaCode))
           ),
           div(`class` := "col-md-6")(
             component
           )
         )
       })
     .componentDidMount(_ => {
         SideBySide.applySyntaxHighlight()
     })
     .componentDidUpdate((_,_,_)  => {
     SideBySide.applySyntaxHighlight()
      })
     .build

   def component(scalaCode: String, demo: Modifier) = {
     singleSideComponent((scalaCode, demo))
   }
 }
