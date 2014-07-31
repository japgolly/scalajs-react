package japgolly.scalajs.react.test

import scala.scalajs.js.{Object, Dynamic, Any => JAny}

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
    val sz = s.size
    val nlen = sz.toString.length
    val klen = s.map(_._1.length).max
    val fmt = s"  [%${nlen}d/$sz] %-${klen}s : %s"
    var i = 0
    s.map { case (k, v) =>
      i = i + 1
      val vv = ("" + v).split('\n')(0)
      fmt.format(i, k, vv)
    }.mkString(s"$o\n", "\n", "")
  }

}
