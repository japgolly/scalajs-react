package japgolly.scalajs.react.component

import scala.annotation.elidable
import scala.scalajs.js
import japgolly.scalajs.react.internal.JsUtil

object InspectRaw {

  def isComponent(a: js.Any): Boolean =
    a match {
      case _: js.Function => true
      case _              => false
    }

  def invalidComponentDesc(a: js.Any): String =
    a.asInstanceOf[Any] match {
      case s: String    => '"' + s + "\". Strings are no longer supported; either create a facade or use js.Dynamic. See docs for detail."
      case o: js.Object => JsUtil.inspectObject(o)
      case ()           => a.toString
      case _            => s"${a.toString} (type=${js.typeOf(a)})"
    }

  @elidable(elidable.ASSERTION)
  @inline
  def assertIsComponent(aa: => js.Any, name: => String): Unit = {
    val a = aa
    assert(isComponent(a), s"Invalid $name: ${invalidComponentDesc(a)}")
  }

}
