# Interoperability

Firstly, it's recommended you read [TYPES.md](TYPES.md).

- [Using JS components in Scala](#using-js-components-in-scala)
- [Using JS functional components in Scala](#using-js-functional-components-in-scala)
- [Using JS ref-forwarding components in Scala](#using-js-ref-forwarding-components-in-scala)
- [Using Scala components from JS](#using-scala-components-from-js)
- [Using Scala components from JSX](#using-scala-components-from-jsx)

<br>


## Using JS components in Scala

1. Determine your types.
  * **Props** - Create a standard Scala.JS facade for the component's props object if it exists. Use `Null` otherwise.
  * **State** - Create a standard Scala.JS facade for the component's state object if it exists. Use `Null` otherwise.
  * **Children** - Determine whether the component uses `.props.children` and choose either `Children.None` or `Children.Varargs` accordingly.
  * **Mounted Facade** - Create a standard Scala.JS facade for the component's mounted instance if it contains additional API.
2. Get a reference to the target component. Either:
  ```scala
  @JSName("Blah")                // If you're not using modules
  @JSImport("./blah.js", "Blah") // If you're using modules
  @js.native
  object BlahJs extends js.Object
  ```
  or
  ```scala
  val BlahJs = js.Dynamic.global.Blah // If you're not using modules
  ```
3. Create the component by calling `JsComponent[Props, Children, State](raw)` where `raw`
  is the raw JS value of the component you created in the previous step.
4. *(Optional)* To attach a mounted facade (`F`), append `.addFacade[F]` to your `JsComponent`.

Example:
```scala
import japgolly.scalajs.react._
import scalajs.js

/**
 * Component-wrapper for collapse animation with react-motion for
 * elements with variable (and dynamic) height.
 *
 * https://github.com/nkbt/react-collapse
 */
object ReactCollapse {

  @JSName("ReactCollapse")
  @js.native
  object RawComponent extends js.Object

  @js.native
  trait Measures extends js.Object {
    val height: Double = js.native
    val width: Double = js.native
  }

  type OnMeasure = js.Function1[Measures, Unit]
  type OnRest = js.Function0[Unit]

  @js.native
  trait Props extends js.Object {
    var isOpened: Boolean = js.native
    var onMeasure: OnMeasure = js.native
    var onRest: OnRest = js.native
  }

  def props(isOpened: Boolean,
            onMeasure: Measures => Callback = _ => Callback.empty,
            onRest: Callback = Callback.empty): Props = {
    val p = (new js.Object).asInstanceOf[Props]
    p.isOpened = isOpened
    p.onMeasure = (measures: Measures) => onMeasure(measures).runNow()
    p.onRest = onRest.toJsCallback // or alternatively: () => onRest.runNow()
    p
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)
}
```


## Using JS functional components in Scala

Pretty much the same as above, just without state and a mounted-facade.

1. Determine your types.
  * **Props** - Create a standard Scala.JS facade for the component's props object if it exists. Use `Null` otherwise.
  * **Children** - Determine whether the component uses `.props.children` and choose either `Children.None` or `Children.Varargs` accordingly.
2. Create the component by calling `JsFnComponent[Props, Children](x)` where x is:
  * a `String` which is the name of the component.
  * an instance of `js.Dynamic`.


## Using JS ref-forwarding components in Scala

Pretty much the same as `JsComponent`, just that you also specify the ref type
(i.e. the value of the reference value when the component sets it).

1. Determine your types.
  * **Props** - Create a standard Scala.JS facade for the component's props object if it exists. Use `Null` otherwise.
  * **Children** - Determine whether the component uses `.props.children` and choose either `Children.None` or `Children.Varargs` accordingly.
  * **Ref target** - Determine the type of value that a forwarded reference would provide. Usually this will be some HTML DOM.
2. Create the component by calling `JsForwardRefComponent[Props, Children, Ref](x)` where x is:
  * a `String` which is the name of the component.
  * an instance of `js.Dynamic`.

```scala
import japgolly.scalajs.react._
import org.scalajs.dom.html
import scala.scalajs.js
import scala.scalajs.js.annotation._

object FancyButton {

  @JSGlobal("FancyButton")
  @js.native
  private object RawComp extends js.Object

  val Component = JsForwardRefComponent[Null, Children.Varargs, html.Button](RawComp)
}
```


## Using Scala components from JS

1. Create a JS object representation of your component's props.
2. Export a fn to JS that goes from your JS props object to JS `React.Element`.
3. Call the exported method from JS and use the result directly in your JS vdom.

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scala.scalajs.js
import scala.scalajs.js.annotation._

object Example {

  // Say this is the Scala component you want to share
  val myScalaComponent = ScalaComponent.builder[String]("")
    .render_P(name => <.div("My name is: ", name))
    .build

  // This will be the props object used from JS-land
  trait JsProps extends js.Object {
    val name: String
  }

  @JSExportTopLevel("MyScalaComponent")
  def render(props: JsProps): raw.React.Element =
    myScalaComponent(props.name).rawElement
}
```

```js
import { MyScalaComponent } from 'your-scalajs-output';

class MyJsComponent extends React.Component {
  render() {
    return MyScalaComponent({ name: "Bob Loblaw" });
  }
}
```


## Using Scala components from JSX

JSX is a little different than JS as it gets transpiled from code like
`<Blah msg="abc" />` to `React.createElement(Blah, { msg: "abc" })`.
To make that work nicely we actually want to create a new JS component and expose it.

1. Create a JS object representation of your component's props.
2. Contramap the component's props to your JS object representation and call `.toJsComponent` to create a real JS component.
3. Export the JS value of your new component (by calling `.raw`)
4. Use the exported component directly within JSX.

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scala.scalajs.js
import scala.scalajs.js.annotation._

object Example {

  // Say this is the Scala component you want to share
  val myScalaComponent = ScalaComponent.builder[String]("")
    .render_P(name => <.div("My name is: ", name))
    .build

  // This will be the props object used from JS-land
  trait JsProps extends js.Object {
    val name: String
  }

  @JSExportTopLevel("MyScalaComponent")
  val myJsComponent =
    myScalaComponent
      .cmapCtorProps[JsProps](_.name) // Change props from JS to Scala
      .toJsComponent // Create a new, real JS component
      .raw // Leave the nice Scala wrappers behind and obtain the underlying JS value
}
```

```jsx
import { MyScalaComponent } from 'your-scalajs-output';

class MyJsComponent extends React.Component {
  render() {
    return (
      <MyScalaComponent name="Bob Loblaw" />
    );
  }
}
```
