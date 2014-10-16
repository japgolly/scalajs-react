package japgolly.scalajs.react.test

import scala.scalajs.js.Array
import ReactTestUtils._

/**
 * Path to a subset of DOM.
 * Much easier and more powerful than what you find in ReactTestUtils.
 * 
 * Example: Sel("div.inner a.active.blue span") find c
 */
sealed abstract class Sel {
  import Sel._

  final def &(s: Sel): Sel =
    this match {
      case Tag(_) | Cls(_) => And(this, s :: Nil)
      case And(h, t)       => And(s, h :: t)
      case Descent(p, c)   => Descent(p, c & s)
      case E               => s
      case ∅               => ∅
    }

  final def >>(s: Sel): Sel = Descent(this, s)

  final def findAll(i: ComponentM): Array[ComponentM] = this match {
    case Tag(n)        => scryRenderedDOMComponentsWithTag(i, n)
    case Cls(n)        => scryRenderedDOMComponentsWithClass(i, n)
    case And(h, t)     => (h.findAll(i) /: t)((q, s) => q intersect s.findAll(i))
    case Descent(p, c) => val r = p findAll i; r flatMap c.findAll filterNot (r contains _)
    case E             => Array(i)
    case ∅             => Array()
  }

  final def findE(i: ComponentM): Either[String, ComponentM] = {
    val a = findAll(i)
    a.length match {
      case 1 => Right(a(0))
      case 0 => Left(s"DOM not found for [$this]")
      case n => Left(s"Too many DOM elements ($n) found for [$this]")
    }
  }

  final def findO(i: ComponentM): Option[ComponentM] =
    findE(i).right.toOption

  final def find(i: ComponentM): ComponentM =
    findE(i).fold(sys.error, identity)
}

object Sel {
  @inline final def id: Sel = E

  case object E extends Sel {
    override def toString = "*"
  }

  case object ∅ extends Sel {
    override def toString = "∅"
  }

  final case class Tag(tag: String) extends Sel {
    override def toString = tag
  }

  final case class Cls(cls: String) extends Sel {
    override def toString = "." + cls
  }

  final case class And(h: Sel, t: List[Sel]) extends Sel {
    override def toString = (h :: t).mkString("(", " & ", ")")
  }

  final case class Descent(p: Sel, c: Sel) extends Sel {
    override def toString = p + " >> " + c
  }

  def apply(css: String): Sel = {
    def lvl(s: String): Sel =
      (id /: s.split("(?=\\.)").filter(_.nonEmpty))((q,a) => q & elem(a))
      // .filter here ↗ due to https://github.com/scala-js/scala-js/issues/1171
    def elem(s: String): Sel =
      if (s.isEmpty) ∅
      else if (s == "*") id
      else if (s.head == '.') Cls(s.tail)
      else Tag(s)
    css.split("\\s+").filter(_.nonEmpty).toList match {
      case Nil => ∅
      case l1 :: ln => (lvl(l1) /: ln)((q, l) => q >> lvl(l))
    }
  }
}