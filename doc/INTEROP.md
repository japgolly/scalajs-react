# Interoperability

Firstly, it's recommended you read [TYPES.md](types.md).

- [Using JS components](#using-js-components)
- [Using JS functional components](#using-js-functional-components)

<br>

## Using JS components

1. Determine your types.
  * **Props** - Create a standard Scala.JS facade for the component's props object if it exists. Use `Null` otherwise.
  * **State** - Create a standard Scala.JS facade for the component's state object if it exists. Use `Null` otherwise.
  * **Children** - Determine whether the component uses `.props.children` and choose either `Children.None` or `Children.Varargs` accordingly.
  * **Mounted Facade** - Create a standard Scala.JS facade for the component's mounted instance if it contains additional API.
2. Create the component by calling `JsComponent[Props, Children, State](x)` where x is:
  * a `String` which is the name of the component.
  * an instance of `js.Dynamic`.
3. *(Optional)* To attach a mounted facade (`F`), append `.addFacade[F]` to your `JsComponent`.

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

  @js.native
  trait Props extends js.Object {
    var isOpened: Boolean = js.native
  }

  def props(isOpened: Boolean): Props = {
    val p = (new js.Object).asInstanceOf[Props]
    p.isOpened = isOpened
    p
  }

  val component = JsComponent[Props, Children.Varargs, Null]("ReactCollapse")
}
```

## Using JS functional components

Pretty much the same as above, just without state and a mounted-facade.

1. Determine your types.
  * **Props** - Create a standard Scala.JS facade for the component's props object if it exists. Use `Null` otherwise.
  * **Children** - Determine whether the component uses `.props.children` and choose either `Children.None` or `Children.Varargs` accordingly.
2. Create the component by calling `JsFnComponent[Props, Children](x)` where x is:
  * a `String` which is the name of the component.
  * an instance of `js.Dynamic`.
