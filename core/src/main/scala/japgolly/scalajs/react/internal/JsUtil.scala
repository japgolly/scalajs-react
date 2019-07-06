package japgolly.scalajs.react.internal

import scala.collection.compat._
import scala.scalajs.js
import scala.scalajs.js.|

object JsUtil {

  object JsSymbol {
    def unapply(a: Any): Option[js.Symbol] =
      js.typeOf(a.asInstanceOf[js.Any]) match {
        case "symbol" => Some(a.asInstanceOf[js.Symbol])
        case _        => None
      }
  }

  def safeToString(a: Any): String =
    try
      a match {
        case JsSymbol(s) => symbolToString(s)
        case _           => a.toString
      }
    catch {
      case _: Throwable => "?"
    }

  def symbolToString(s: js.Symbol): String =
    try
      s.asInstanceOf[js.Dynamic].applyDynamic("toString")().asInstanceOf[String]
    catch {
      case t: Throwable =>
        js.Symbol.keyFor(s).toOption match {
          case Some(k) => s"Symbol($k)"
          case None    => "Symbol(?)"
        }
    }

  def inspectValue(a: Any): String =
    a match {
      case s: String    => js.JSON.stringify(s)
      case o: js.Object => inspectObject(o)
      case () | null    => "" + a
      case JsSymbol(s)  => symbolToString(s)
      case _            => s"${safeToString(a)}: ${js.typeOf(a.asInstanceOf[js.Any])}"
    }

  def objectIterator(o: js.Object): Iterator[(String, js.Any)] = {
    val d = o.asInstanceOf[js.Dynamic]
    js.Object.properties(o).iterator.map { n =>
      val v = (try d.selectDynamic(n) catch { case t: Throwable => safeToString(t) }).asInstanceOf[js.Any]
      n -> v
    }
  }

  def inspectObject(o: js.Object): String = {
    val s = objectIterator(o).toVector.sortBy(_._1)
    if (s.isEmpty)
      "Value has no object properties: " + o
    else {
      val ss = s.map { case (k, v) =>
        (k, js.typeOf(v), safeToString(v).split('\n')(0))
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

  def jsArrayFromTraversable[A](as: IterableOnce[A]): js.Array[A] = {
    val array = new js.Array[A]
    as.iterator.foreach(array push _)
    array
  }

  def jsNullToOption[A](an: A | Null): Option[A] =
    Option(an.asInstanceOf[A])

  def optionToJsNull[A](oa: Option[A]): A | Null =
    oa match {
      case Some(a) => a
      case None    => null
    }
}
