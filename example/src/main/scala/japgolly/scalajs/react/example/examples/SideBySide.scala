package japgolly.scalajs.react.example.examples

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.ReactVDom.all._
import org.scalajs.dom
import org.scalajs.dom.Node
import org.scalajs.dom.extensions.PimpedNodeList


/**
  * Created by chandrasekharkode on 11/17/14.
  */
object SideBySide {


   val sideBySideComponent = ReactComponentB[(String, String, Modifier)]("sideBySideExample")
     .render(P => {
         val (jsCode, scalaCode, component) = P
         div(
           div(`class` := "row")(
             div(`class` := "col-md-6")(
               h3("JSX Code"),
               pre(code(jsCode))
             ),
             div(`class` := "col-md-6")(
               h3("Scala Code"),
               pre(code(scalaCode))

             )
           ),
           hr,
           div(
             h3("Demo:"),
             div(`class` := "row text-center")(
               component
             )
           )
         )

       })
     .componentDidMount(_ => {
         applySyntaxHighlight()
       })
     .componentDidUpdate((_,_,_)  => {
         applySyntaxHighlight()
    })
     .build

   def component(jsxCode: String, scalaCode: String, demo: Modifier) = {

     sideBySideComponent((jsxCode, scalaCode, demo))
   }

  def applySyntaxHighlight() = {
    import scala.scalajs.js.Dynamic.{global => g}
    val nodeList = dom.document.querySelectorAll("pre code").toArray
    nodeList.foreach( n => g.hljs.highlightBlock(n))
  }

 }
