package japgolly.scalajs.react.test

import scala.scalajs.js
import js.{Object, Dynamic, Any => JAny}

object DebugJs {

  def objectPropValues(o: Object): Stream[(String, JAny)] = {
    val d = o.asInstanceOf[Dynamic]
    Object.properties(o).toStream.map(n => {
      val v = (try d.selectDynamic(n) catch{case t:Throwable => t.toString}).asInstanceOf[JAny]
      n -> v
    })
  }

  def inspectObject(o: Object): String = {
    val s = objectPropValues(o).sortBy(_._1)
    if (s.isEmpty)
      "Value has no object properties: " + o
    else {
      val ss = s.map { case (k, v) =>
        (k, js.typeOf(v), ("" + v).split('\n')(0))
      }
      val sz = s.size
      val nlen = sz.toString.length
      val klen = ss.map(_._1.length).max
      val tlen = ss.map(_._2.length).max
      val fmt = s"  [%${nlen}d/$sz] %-${klen}s : %-${tlen}s = %s"
      var i = 0
      ss.map { case (k, t, v) =>
        i = i + 1
        fmt.format(i, k, t, v)
      }.mkString(s"$o\n", "\n", "")
    }
  }

}
