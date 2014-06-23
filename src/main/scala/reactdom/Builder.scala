package scalatags
package reactdom

import scala.collection.mutable
import scala.scalajs.js
import scalatags.generic.Modifier
import scala.reflect.ClassTag

//import reactdom.{ReactBuilder, ReactOutput, ReactFragT}

///**
// * Object to aggregate the modifiers into one coherent data structure
// * so the final HTML string can be properly generated. It's really
// * gross internally, but bloody fast. Even using pre-built data structures
// * like `mutable.Buffer` slows down the benchmarks considerably. Also
// * exposes more of its internals than it probably should for performance,
// * so even though the stuff isn't private, don't touch it!
// */
//class Builder(var children: Array[Frag] = new Array(4),
//              var attrs: Array[(String, String)] = new Array(4)){
//  final var childIndex = 0
//  final var attrIndex = 0
//  private[this] var styleIndex = -1
//  private[this] def increment[T: ClassTag](arr: Array[T], index: Int) = {
//    if (index >= arr.length){
//      val newArr = new Array[T](children.length * 2)
//      var i = 0
//      while(i < children.length){
//        newArr(i) = arr(i)
//        i += 1
//      }
//      newArr
//    }else{
//      null
//    }
//  }
//  def addChild(c: Frag) = {
//    val newChildren = increment(children, childIndex)
//    if (newChildren != null) children = newChildren
//    children(childIndex) = c
//    childIndex += 1
//  }
//  def addAttr(k: String, v: String) = {
//    (k, styleIndex) match{
//      case ("style", -1) =>
//        val newAttrs = increment(attrs, attrIndex)
//        if (newAttrs!= null) attrs = newAttrs
//        styleIndex = attrIndex
//        attrs(attrIndex) = (k -> v)
//        attrIndex += 1
//      case ("style", n) =>
//        val (oldK, oldV) = attrs(styleIndex)
//        attrs(styleIndex) = (oldK, oldV + v)
//      case _ =>
//        val newAttrs = increment(attrs, attrIndex)
//        if (newAttrs!= null) attrs = newAttrs
//        attrs(attrIndex) = (k -> v)
//        attrIndex += 1
//    }
//  }
//}
//trait Frag extends generic.Frag[Builder, String, String]{
//  def writeTo(strb: StringBuilder): Unit
//  def render: String
//  def applyTo(b: Builder) = b.addChild(this)
//}

class RBuilder {
  private[this] var props = (new js.Object).asInstanceOf[js.Dynamic]
  private[this] var style = (new js.Object).asInstanceOf[js.Dynamic]
  private[this] var children = Vector.empty[js.Any]

  def addAttr(k: String, v: js.Any): Unit =
    props.updateDynamic(k)(v)

  def addStyle(k: String, v: String): Unit = {
    style.updateDynamic(k)(v)
    props.updateDynamic("style")(style)
  }

  def appendChild(c: js.Any): Unit =
    children = children :+ c

  def props2 = props.asInstanceOf[js.Object]
  def children2 = children
}