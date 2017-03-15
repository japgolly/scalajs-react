package japgolly.scalajs.react.internal

import scala.scalajs.js

object JsUtil {

  def objectIterator(o: js.Object): Iterator[(String, js.Any)] = {
    val d = o.asInstanceOf[js.Dynamic]
    js.Object.properties(o).iterator.map { n =>
      val v = (try d.selectDynamic(n) catch { case t: Throwable => t.toString }).asInstanceOf[js.Any]
      n -> v
    }
  }

  def inspectObject(o: js.Object): String = {
    val s = objectIterator(o).toVector.sortBy(_._1)
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

  def jsArray[A](as: A*): js.Array[A] = {
    val array = new js.Array[A]
    array.push(as: _*)
    array
  }

  def jsArrayFromTraversable[A](as: TraversableOnce[A]): js.Array[A] = {
    val array = new js.Array[A]
    as.foreach(array push _)
    array
  }

  def evalName(name: String): Option[js.Dynamic] =
    js.eval(name).asInstanceOf[js.UndefOr[js.Dynamic]].toOption
}
