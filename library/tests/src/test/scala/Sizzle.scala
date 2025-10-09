package sizzle

import scala.scalajs.js
import scala.scalajs.js.annotation._

@JSGlobal
@js.native
object Sizzle extends js.Object {

  type DOM    = org.scalajs.dom.Element
  type Result = js.Array[DOM]

  /*
  Sizzle( String selector[, DOMElement|DOMDocument context[, Array results]] )

  This is the main function for finding elements. It will use querySelectorAll if available.

  returns (Array): all of the elements matching the selector

  Parameters

  selector: A css selector

  context: An element or document to use as the context for finding elements. The default is the current document.

  results: Optionally pass an array or array-like object to which Sizzle will append the elements (for instance, jQuery passes the jQuery object here). An array-like object is defined as an object containing elements with an accurate length property and a push method for adding elements to the object.
   */

  def apply(selector: String,
            context: DOM = js.native,
            results: Result = js.native): Result = js.native

  /*
  Sizzle.matchesSelector( DOMElement element, String selector )

  Uses native matchesSelector if available.

  returns(Boolean): whether the given element matches the selector

  Parameters

  element: A DOMElement against which Sizzle will test the selector

  selector: A css selector
   */

  def matchesSelector(element: DOM, selector: String): Boolean = js.native

  /*
  Sizzle.matches( String selector, Array<DOMElement> elements )

  returns(Array): All the elements in the set that match the given selector.

  Parameters

  selector: A css selector

  elements: An array of DOMElements to filter against the specified selector.
   */
  def matches(selector: String, elements: Result): Result = js.native
}
