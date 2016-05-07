package japgolly.scalajs.react.test

import scala.scalajs.js
import scala.scalajs.js.{Function1 => JFn1, Object, Array, UndefOr, undefined, Dynamic, native}
import scala.scalajs.js.annotation.JSName
import japgolly.scalajs.react._

/** https://facebook.github.io/react/docs/test-utils.html */
@js.native
@JSName("React.addons.TestUtils")
object ReactTestUtils extends ReactTestUtils

@js.native
trait ReactTestUtils extends Object {

  def Simulate: Simulate = native

  /** Render a component into a detached DOM node in the document. This function requires a DOM. */
  def renderIntoDocument(c: ReactElement): ComponentM = native
  def renderIntoDocument[P,S,B,N <: TopNode](c: ReactComponentU[P,S,B,N]): ReactComponentM[P,S,B,N] = native

  /**
   * Pass a mocked component module to this method to augment it with useful methods that allow it to be used as a dummy
   * React component. Instead of rendering as usual, the component will become a simple &lt;div&gt; (or other tag if
   * mockTagName is provided) containing any provided children.
   */
  def mockComponent(c: ComponentClass, tagName: String = native): Object = native

  /** Returns true if instance is an instance of a React componentClass. */
  def isComponentOfType(instance: ReactElement, c: ComponentClass): Boolean = native

  /** Returns true if instance is a DOM component (such as a &lt;div&gt; or &lt;span&gt;). */
  def isDOMComponent(instance: ReactElement): Boolean = native

  /** Returns true if instance is a composite component (created with React.createClass()) */
  def isCompositeComponent(instance: ReactElement): Boolean = native

  /** The combination of [[isComponentOfType()]] and [[isCompositeComponent()]]. */
  def isCompositeComponentWithType(instance: ReactElement, c: ComponentClass): Boolean = native

  /**
   * Traverse all components in tree and accumulate all components where test(component) is true.
   * This is not that useful on its own, but it's used as a primitive for other test utils.
   */
  def findAllInRenderedTree(tree: ComponentM, test: JFn1[ComponentM, Boolean]): Array[ComponentM] = native

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the class name
   * matching className.
   */
  def scryRenderedDOMComponentsWithClass(tree: ComponentM, className: String): Array[ComponentM] = native

  /**
   * Like [[scryRenderedDOMComponentsWithClass()]] but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  def findRenderedDOMComponentWithClass(tree: ComponentM, className: String): ComponentM = native

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the tag name
   * matching tagName.
   */
  def scryRenderedDOMComponentsWithTag(tree: ComponentM, tagName: String): Array[ComponentM] = native

  /**
   * Like [[scryRenderedDOMComponentsWithTag()]] but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  def findRenderedDOMComponentWithTag(tree: ComponentM, tagName: String): ComponentM = native

  /** Finds all instances of components with type equal to componentClass. */
  def scryRenderedComponentsWithType(tree: ComponentM, c: ComponentClass): Array[ComponentM] = native

  /**
   * Same as [[scryRenderedComponentsWithType()]] but expects there to be one result and returns that one result, or throws
   * exception if there is any other number of matches besides one.
   */
  def findRenderedComponentWithType(tree: ComponentM, c: ComponentClass): ComponentM = native
}

@js.native
trait Simulate extends Object {
  def beforeInput      (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def blur             (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def change           (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def click            (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def compositionEnd   (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def compositionStart (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def compositionUpdate(t: ReactOrDomNode, eventData: Object = native): Unit = native
  def contextMenu      (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def copy             (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def cut              (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def doubleClick      (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def drag             (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def dragEnd          (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def dragEnter        (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def dragExit         (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def dragLeave        (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def dragOver         (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def dragStart        (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def drop             (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def error            (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def focus            (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def input            (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def keyDown          (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def keyPress         (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def keyUp            (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def load             (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def mouseDown        (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def mouseEnter       (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def mouseLeave       (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def mouseMove        (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def mouseOut         (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def mouseOver        (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def mouseUp          (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def paste            (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def reset            (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def scroll           (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def select           (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def submit           (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def touchCancel      (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def touchEnd         (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def touchMove        (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def touchStart       (t: ReactOrDomNode, eventData: Object = native): Unit = native
  def wheel            (t: ReactOrDomNode, eventData: Object = native): Unit = native
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// NOTE: Do not use UndefOr for arguments below; undefined causes Phantom-bloody-JS to crash.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

case class ChangeEventData(value           : String  = "",
                           defaultPrevented: Boolean = false) {
  def toJs: Object = {
    val t = Dynamic.literal()
    t.updateDynamic("defaultPrevented")(defaultPrevented)
    t.updateDynamic("value")(value)
    val o = Dynamic.literal("target" -> t)
    o
  }
  def simulate(t: ReactOrDomNode) = ReactTestUtils.Simulate.change(t, this)
  def simulation = Simulation.change(this)
}

case class KeyboardEventData(key             : String  = "",
                             location        : Double  = 0,
                             altKey          : Boolean = false,
                             ctrlKey         : Boolean = false,
                             metaKey         : Boolean = false,
                             shiftKey        : Boolean = false,
                             repeat          : Boolean = false,
                             locale          : String  = "",
                             keyCode         : Int     = 0,
                             charCode        : Int     = 0,
                             which           : Int     = 0,
                             defaultPrevented: Boolean = false) {

  def alt   = copy(altKey   = true)
  def ctrl  = copy(ctrlKey  = true)
  def meta  = copy(metaKey  = true)
  def shift = copy(shiftKey = true)

  def toJs: Object = {
    val o = Dynamic.literal()
    o.updateDynamic("key"             )(key             )
    o.updateDynamic("location"        )(location        )
    o.updateDynamic("altKey"          )(altKey          )
    o.updateDynamic("ctrlKey"         )(ctrlKey         )
    o.updateDynamic("metaKey"         )(metaKey         )
    o.updateDynamic("shiftKey"        )(shiftKey        )
    o.updateDynamic("repeat"          )(repeat          )
    o.updateDynamic("locale"          )(locale          )
    o.updateDynamic("keyCode"         )(keyCode         )
    o.updateDynamic("charCode"        )(charCode        )
    o.updateDynamic("which"           )(which           )
    o.updateDynamic("defaultPrevented")(defaultPrevented)
    o
  }
  def simulateKeyDown       (t: ReactOrDomNode): Unit = ReactTestUtils.Simulate.keyDown (t, this)
  def simulateKeyPress      (t: ReactOrDomNode): Unit = ReactTestUtils.Simulate.keyPress(t, this)
  def simulateKeyUp         (t: ReactOrDomNode): Unit = ReactTestUtils.Simulate.keyUp   (t, this)
  def simulateKeyDownUp     (t: ReactOrDomNode): Unit = {simulateKeyDown(t); simulateKeyUp(t)}
  def simulateKeyDownPressUp(t: ReactOrDomNode): Unit = {simulateKeyDown(t); simulateKeyPress(t); simulateKeyUp(t)}
  def simulationKeyDown        = Simulation.keyDown(this)
  def simulationKeyPress       = Simulation.keyPress(this)
  def simulationKeyUp          = Simulation.keyUp(this)
  def simulationKeyDownUp      = simulationKeyDown >> simulationKeyUp
  def simulationKeyDownPressUp = simulationKeyDown >> simulationKeyPress >> simulationKeyUp
}

case class MouseEventData(screenX         : Double  = 0,
                          screenY         : Double  = 0,
                          clientX         : Double  = 0,
                          clientY         : Double  = 0,
                          pageX           : Double  = 0,
                          pageY           : Double  = 0,
                          altKey          : Boolean = false,
                          ctrlKey         : Boolean = false,
                          metaKey         : Boolean = false,
                          shiftKey        : Boolean = false,
                          button          : Int     = 0,
                          buttons         : Int     = 0,
                          defaultPrevented: Boolean = false) {

  def alt   = copy(altKey   = true)
  def ctrl  = copy(ctrlKey  = true)
  def meta  = copy(metaKey  = true)
  def shift = copy(shiftKey = true)

  def toJs: Object = {
    val o = Dynamic.literal()
    o.updateDynamic("screenX"         )(screenX         )
    o.updateDynamic("screenY"         )(screenY         )
    o.updateDynamic("clientX"         )(clientX         )
    o.updateDynamic("clientY"         )(clientY         )
    o.updateDynamic("pageX"           )(pageX           )
    o.updateDynamic("pageY"           )(pageY           )
    o.updateDynamic("altKey"          )(altKey          )
    o.updateDynamic("ctrlKey"         )(ctrlKey         )
    o.updateDynamic("metaKey"         )(metaKey         )
    o.updateDynamic("shiftKey"        )(shiftKey        )
    o.updateDynamic("button"          )(button          )
    o.updateDynamic("buttons"         )(buttons         )
    o.updateDynamic("defaultPrevented")(defaultPrevented)
    o
  }
  def simulateDrag      (t: ReactOrDomNode) = ReactTestUtils.Simulate.drag      (t, this)
  def simulateDragEnd   (t: ReactOrDomNode) = ReactTestUtils.Simulate.dragEnd   (t, this)
  def simulateDragEnter (t: ReactOrDomNode) = ReactTestUtils.Simulate.dragEnter (t, this)
  def simulateDragExit  (t: ReactOrDomNode) = ReactTestUtils.Simulate.dragExit  (t, this)
  def simulateDragLeave (t: ReactOrDomNode) = ReactTestUtils.Simulate.dragLeave (t, this)
  def simulateDragOver  (t: ReactOrDomNode) = ReactTestUtils.Simulate.dragOver  (t, this)
  def simulateDragStart (t: ReactOrDomNode) = ReactTestUtils.Simulate.dragStart (t, this)
  def simulateDrop      (t: ReactOrDomNode) = ReactTestUtils.Simulate.drop      (t, this)
  def simulateMouseDown (t: ReactOrDomNode) = ReactTestUtils.Simulate.mouseDown (t, this)
  def simulateMouseEnter(t: ReactOrDomNode) = ReactTestUtils.Simulate.mouseEnter(t, this)
  def simulateMouseLeave(t: ReactOrDomNode) = ReactTestUtils.Simulate.mouseLeave(t, this)
  def simulateMouseMove (t: ReactOrDomNode) = ReactTestUtils.Simulate.mouseMove (t, this)
  def simulateMouseOut  (t: ReactOrDomNode) = ReactTestUtils.Simulate.mouseOut  (t, this)
  def simulateMouseOver (t: ReactOrDomNode) = ReactTestUtils.Simulate.mouseOver (t, this)
  def simulateMouseUp   (t: ReactOrDomNode) = ReactTestUtils.Simulate.mouseUp   (t, this)
  def simulateWheel     (t: ReactOrDomNode) = ReactTestUtils.Simulate.wheel     (t, this)
  def simulationDrag       = Simulation.drag      (this)
  def simulationDragEnd    = Simulation.dragEnd   (this)
  def simulationDragEnter  = Simulation.dragEnter (this)
  def simulationDragExit   = Simulation.dragExit  (this)
  def simulationDragLeave  = Simulation.dragLeave (this)
  def simulationDragOver   = Simulation.dragOver  (this)
  def simulationDragStart  = Simulation.dragStart (this)
  def simulationMouseDown  = Simulation.mouseDown (this)
  def simulationMouseEnter = Simulation.mouseEnter(this)
  def simulationMouseLeave = Simulation.mouseLeave(this)
  def simulationMouseMove  = Simulation.mouseMove (this)
  def simulationMouseOut   = Simulation.mouseOut  (this)
  def simulationMouseOver  = Simulation.mouseOver (this)
  def simulationMouseUp    = Simulation.mouseUp   (this)
  def simulationWheel      = Simulation.wheel     (this)
}
