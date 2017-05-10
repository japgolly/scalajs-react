# Interop With Third Party Components 

If you want to use a reactjs component in your scalajs-react project then you must define a wrapper for js component.
  
## Example   
 Let say we have a JS component , Name : AwesomeJSComp , props ..
 ```js
   propTypes: {
     numberOfLines: React.PropTypes.number.isRequired,
     onPress: React.PropTypes.func, // function with zero args
     suppressHighlighting: React.PropTypes.bool,
     testID: React.PropTypes.string,
   }
   
   ```
 To create a wrapper first we must map js types to scala types
 
 ```scala
   numberOfLines: Int,
   onPress: js.UndefOr[() => Unit] = js.undefined,
   suppressHighlighting: js.UndefOr[Bool] = js.undefined,
   testID: js.UndefOr[String] = js.undefined
 ```
 make sure you have js.UndefOr[T]  for non required  fields.we also need a method which converts our scala fields to js.Object
 
 ```scala
  def toJS = {
   val p = js.Dynamic.literal()
   p.updateDynamic("numberOfLines")(numberOfLines)
   onPress.foreach(v => p.updateDynamic("onPress")(v))
   suppressHighlighting.foreach(v => p.updateDynamic("suppressHighlighting")(v))
   testID.foreach(v => p.updateDynamic("testID")(v))
   p
  }
 
 ```
 
 that's it, now we have all required bits , just composing bits is pending.lets do that 
 
 ```scala
 
 case class AwesomeJSCompWrapper( numberOfLines: Int,
                                   onPress: js.UndefOr[() => Unit] = js.undefined,
                                   suppressHighlighting: js.UndefOr[Bool] = js.undefined,
                                   testID: js.UndefOr[String] = js.undefined) {
   def toJS = {
     val p = js.Dynamic.literal()
     p.updateDynamic("numberOfLines")(numberOfLines)
     onPress.foreach(v => p.updateDynamic("onPress")(v))
     suppressHighlighting.foreach(v => p.updateDynamic("suppressHighlighting")(v))
     testID.foreach(v => p.updateDynamic("testID")(v))
     p
    }
    
    def apply(children : ReactNode*) = {
     val f = React.asInstanceOf[js.Dynamic].createFactory(js.Dynamic.Global.AwesomeJSComp) // access real js component , make sure you wrap with createFactory (this is needed from 0.13 onwards)
     f(toJS, children.toJsArray).asInstanceOf[ReactComponentU_]
    }
    
 }
    
 ```   
 
 hola you successfully created wrapper! :) 
 
 To use this add original js comp source to jsDependencies in sbt build file/or what ever build tool you use.
 
 now you can use AwesomeJSCmpWrapper like a normal scalajs-react component
 
 ```scala
 
  val component = ReactComponentB.static("Demo",
     <.div(
      AwesomeJSCmpWrapper(numberOfLines = 3,testID = "id")
     )
   ).buildU
   
  ``` 
  
## Wrapper Generator 

 Manual wrapper creation is easy  but its a tedius task! 
 
 To generate wrapper automatically go to following link and fill required fields then hit Generate Button.
 
http://chandu0101.github.io/reactjs-scalajs/
 
 1) Scala component name text field : 
    Enter you scala wrapper name(Example : AwesomeJSCmpWrapper)
  
 2) Js component text field : 
    Enter full access path to js component (Example :  js.Dynamic.Global.AwesomeJSComp)
 
 3) WithChildren check box :
    By default it is checked ,uncheck this if your js component doesn't allow children
 
 4) JS component props textview : 
    Place your js comp props with ``,`` separated
    
  ```js
         numberOfLines: React.PropTypes.number.isRequired,
         onPress: React.PropTypes.func,
         suppressHighlighting: React.PropTypes.bool,
         testID: React.PropTypes.string,
  ```
     
  Now click on Generate Button :)
  
  
## Refs
  
 Some times we may want to call public(exposed) methods of mounted react component's , we use refs to achieve this
 
 Lets assume that our AwesomeJSComp has public method hideMe()
 
 JS World :
 
 ```js
     // pseudo code 
    <div>
     <AwesomeJSComp ref = "awesomecomp",..props > </AwesomeJSComp>
    </div>
    
    function test() {
     this.refs.awesomecomp.hideMe()
    }
    
 ```
 
 Scala World : 
 
 To achieve same thing in scala world ,add a new field ref to our AwesomeJSCompWrapper and then create a facade 
  for public methods of AwesomeJSComp
  
 ```scala
 trait AwesomeJSCompWrapperM extends js.Object { 
   def hideMe() : Unit = js.native 
   
   ... more public methods
 }
 ```
 scala example :
 ```scala
   class RB(t:BackendScope[_,_]) {
      def test = {
        val awesomeJSRef = Ref.toJS[AwesomeJSCompWrapperM]("awesomescalajs")(t) // get ref
        if(awesomeJSRef.isDefined) awesomeJSRef.get.hideMe()
      }
    }
    val C = ReactComponentB[Unit]("RefsToThirdPartyCompDemo")
      .stateless
      .backend(new RB(_))
      .render((P,S,B) => {
        div(
          AwesomeJSCompWrapper(ref = "awesomescalajs",..props)()
        )
      })
      .buildU
      
 ```
 
##Real World Examples 
 
 https://github.com/japgolly/scalajs-react/blob/master/core/src/main/scala/japgolly/scalajs/react/Addons.scala
 
 https://github.com/wav/material-ui-scalajs-react
  
 https://github.com/chandu0101/scalajs-react-native
 

 
 
 
 
