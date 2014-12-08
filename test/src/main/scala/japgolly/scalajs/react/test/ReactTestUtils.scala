package japgolly.scalajs.react.test

import scala.scalajs.js.{Function1 => JFn1, Object, Array, UndefOr, undefined, Dynamic, Number}
import scala.scalajs.js.annotation.JSName
import japgolly.scalajs.react._

/** http://facebook.github.io/react/docs/test-utils.html */
@JSName("React.addons.TestUtils")
object ReactTestUtils extends Object {

  def Simulate: Simulate = ???

  /** Render a component into a detached DOM node in the document. This function requires a DOM. */
  def renderIntoDocument(c: ReactComponentU_): ComponentM = ???
  def renderIntoDocument[P,S,B,N <: TopNode](c: ReactComponentU[P,S,B,N]): ReactComponentM[P,S,B,N] = ???

  /**
   * Pass a mocked component module to this method to augment it with useful methods that allow it to be used as a dummy
   * React component. Instead of rendering as usual, the component will become a simple &lt;div&gt; (or other tag if
   * mockTagName is provided) containing any provided children.
   */
  def mockComponent(c: ComponentClass, tagName: String = ???): Object = ???

  /** Returns true if instance is an instance of a React componentClass. */
  def isComponentOfType(instance: ReactComponentU_, c: ComponentClass): Boolean = ???

  /** Returns true if instance is a DOM component (such as a &lt;div&gt; or &lt;span&gt;). */
  def isDOMComponent(instance: ReactComponentU_): Boolean = ???

  /** Returns true if instance is a composite component (created with React.createClass()) */
  def isCompositeComponent(instance: ReactComponentU_): Boolean = ???

  /** The combination of isComponentOfType() and isCompositeComponent(). */
  def isCompositeComponentWithType(instance: ReactComponentU_, c: ComponentClass): Boolean = ???

  /** Returns true if instance is a plain text component. */
  def isTextComponent(instance: ReactComponentU_): Boolean = ???

  /**
   * Traverse all components in tree and accumulate all components where test(component) is true.
   * This is not that useful on its own, but it's used as a primitive for other test utils.
   */
  def findAllInRenderedTree(tree: ComponentM, test: JFn1[ComponentM, Boolean]): Array[ComponentM] = ???

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the class name
   * matching className.
   */
  def scryRenderedDOMComponentsWithClass(tree: ComponentM, className: String): Array[ComponentM] = ???

  /**
   * Like scryRenderedDOMComponentsWithClass() but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  def findRenderedDOMComponentWithClass(tree: ComponentM, className: String): ComponentM = ???

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the tag name
   * matching tagName.
   */
  def scryRenderedDOMComponentsWithTag(tree: ComponentM, tagName: String): Array[ComponentM] = ???

  /**
   * Like scryRenderedDOMComponentsWithTag() but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  def findRenderedDOMComponentWithTag(tree: ComponentM, tagName: String): ComponentM = ???

  /** Finds all instances of components with type equal to componentClass. */
  def scryRenderedComponentsWithType(tree: ComponentM, c: ComponentClass): Array[ComponentM] = ???

  /**
   * Same as scryRenderedComponentsWithType() but expects there to be one result and returns that one result, or throws
   * exception if there is any other number of matches besides one.
   */
  def findRenderedComponentWithType(tree: ComponentM, c: ComponentClass): ComponentM = ???
}

trait Simulate extends Object {
  def beforeInput      (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def blur             (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def change           (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def click            (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def compositionEnd   (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def compositionStart (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def compositionUpdate(t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def contextMenu      (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def copy             (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def cut              (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def doubleClick      (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def drag             (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def dragEnd          (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def dragEnter        (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def dragExit         (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def dragLeave        (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def dragOver         (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def dragStart        (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def drop             (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def error            (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def focus            (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def input            (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def keyDown          (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def keyPress         (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def keyUp            (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def load             (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def mouseDown        (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def mouseEnter       (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def mouseLeave       (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def mouseMove        (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def mouseOut         (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def mouseOver        (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def mouseUp          (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def paste            (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def reset            (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def scroll           (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def select           (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def submit           (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def touchCancel      (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def touchEnd         (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def touchMove        (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def touchStart       (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
  def wheel            (t: ReactOrDomNode, eventData: Object = ???): Unit = ???
}

case class ChangeEventData(value: UndefOr[String] = undefined) {
  def toJs: Object = {
    val t = Dynamic.literal()
    value.foreach(v => t.updateDynamic("value")(v))
    val o = Dynamic.literal("target" -> t)
    o
  }
  def simulate(t: ReactOrDomNode) = ReactTestUtils.Simulate.change(t, this)
  def simulation = Simulation.change(this)
}

case class KeyboardEventData(key:      UndefOr[String]  = undefined,
                             location: UndefOr[Number]  = undefined,
                             altKey:   UndefOr[Boolean] = undefined,
                             ctrlKey:  UndefOr[Boolean] = undefined,
                             metaKey:  UndefOr[Boolean] = undefined,
                             shiftKey: UndefOr[Boolean] = undefined,
                             repeat:   UndefOr[Boolean] = undefined,
                             locale:   UndefOr[String]  = undefined,
                             keyCode:  UndefOr[Int]     = undefined) {
  def toJs: Object = {
    val o = Dynamic.literal()
    key     .foreach(v => o.updateDynamic("key"     )(v))
    location.foreach(v => o.updateDynamic("location")(v))
    altKey  .foreach(v => o.updateDynamic("altKey"  )(v))
    ctrlKey .foreach(v => o.updateDynamic("ctrlKey" )(v))
    metaKey .foreach(v => o.updateDynamic("metaKey" )(v))
    shiftKey.foreach(v => o.updateDynamic("shiftKey")(v))
    repeat  .foreach(v => o.updateDynamic("repeat"  )(v))
    locale  .foreach(v => o.updateDynamic("locale"  )(v))
    keyCode .foreach(v => o.updateDynamic("keyCode" )(v))
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

case class MouseEventData(screenX:  UndefOr[Number]  = undefined,
                          screenY:  UndefOr[Number]  = undefined,
                          clientX:  UndefOr[Number]  = undefined,
                          clientY:  UndefOr[Number]  = undefined,
                          altKey:   UndefOr[Boolean] = undefined,
                          ctrlKey:  UndefOr[Boolean] = undefined,
                          metaKey:  UndefOr[Boolean] = undefined,
                          shiftKey: UndefOr[Boolean] = undefined,
                          buttons:  UndefOr[Number]  = undefined) {
  def toJs: Object = {
    val o = Dynamic.literal()
    screenX .foreach(v => o.updateDynamic("screenX" )(v))
    screenY .foreach(v => o.updateDynamic("screenY" )(v))
    clientX .foreach(v => o.updateDynamic("clientX" )(v))
    clientY .foreach(v => o.updateDynamic("clientY" )(v))
    altKey  .foreach(v => o.updateDynamic("altKey"  )(v))
    ctrlKey .foreach(v => o.updateDynamic("ctrlKey" )(v))
    metaKey .foreach(v => o.updateDynamic("metaKey" )(v))
    shiftKey.foreach(v => o.updateDynamic("shiftKey")(v))
    buttons .foreach(v => o.updateDynamic("buttons" )(v))
    o
  }
  def simulateDrag      (t: ReactOrDomNode) = ReactTestUtils.Simulate.drag      (t, this)
  def simulateDragEnd   (t: ReactOrDomNode) = ReactTestUtils.Simulate.dragEnd   (t, this)
  def simulateDragEnter (t: ReactOrDomNode) = ReactTestUtils.Simulate.dragEnter (t, this)
  def simulateDragExit  (t: ReactOrDomNode) = ReactTestUtils.Simulate.dragExit  (t, this)
  def simulateDragLeave (t: ReactOrDomNode) = ReactTestUtils.Simulate.dragLeave (t, this)
  def simulateDragOver  (t: ReactOrDomNode) = ReactTestUtils.Simulate.dragOver  (t, this)
  def simulateDragStart (t: ReactOrDomNode) = ReactTestUtils.Simulate.dragStart (t, this)
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
