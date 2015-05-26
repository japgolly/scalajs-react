package japgolly.scalajs.react.extra

import org.scalajs.dom
import scalaz.effect.IO
import scalaz.syntax.bind.ToBindOps
import japgolly.scalajs.react._, vdom.prefix_<^._, ScalazReact._
import dom.{document, window}
import dom.html.Element
import ReusabilityOverlay._

object ReusabilityOverlay {
  @inline implicit def autoLiftHtml(n: dom.raw.Node) = n.asInstanceOf[Element]

  val template: ReactElement =
    <.div(
      ^.background   := "rgba(244,244,244,.9)",
      ^.padding      := "1px 2px",
      ^.border       := "solid 1px black",
      ^.borderRadius := "4px",
      ^.position     := "absolute",
      ^.zIndex       := "10000",
      ^.color        := "#444",
      ^.fontWeight   := "bold",
      ^.boxShadow    := "0 2px 6px rgba(0,0,0,0.25)",
      ^.cursor       := "pointer",
      <.span(^.color := "#070"),
      "|",
      <.span(^.color := "#a00"))

  case class Overlay(outer: Element, good: Element, bad: Element)

  type Comp = ComponentScope_M[TopNode]

  def install[P: Reusability, S: Reusability, B, N <: TopNode](overlay: ReusabilityOverlay) =
    Reusability.shouldComponentUpdateAnd[P, S, B, N] { ($, p1, p, s1, s) =>
      val logResult =
        if (p || s) {
          def fmt(update: Boolean, name: String, a: Any, b: Any) =
            if (update)
              s"$name update:\n  [$a] ⇒\n  [$b]."
            else
              ""
          val sep = if (p && s) "\n" else ""
          val reason = fmt(p, "Prop", p1, $.props) + sep + fmt(s, "State", s1, $.state)
          overlay logBad reason
        }
        else
          overlay.logGood
      logResult >> overlay.update($)
    } andThen (_
      .componentDidMountIO($ => overlay.create >> overlay.update($))
      .componentWillUnmountIO(overlay.remove)
    )
}

class ReusabilityOverlay(howManyReasonsToShowOnClick: Int = 10) {
  protected var good = 0
  protected var bad = Vector.empty[String]
  protected def badCount = bad.size
  protected var overlay: Option[Overlay] = None

  val logGood = IO[Unit] {
    good += 1
  }

  def logBad(reason: String) = IO[Unit] {
    bad :+= reason
  }

  val create = IO[Unit] {

    // Create
    val tmp = document.createElement("div")
    document.body.appendChild(tmp)
    React.render(template, tmp)
    val outer = tmp.firstChild
    document.body.replaceChild(outer, tmp)

    // Customise
    outer.addEventListener("click", (_: dom.Event) => onClick.unsafePerformIO())

    // Store
    val good = outer.childNodes(0)
    val bad = outer.childNodes(2)
    overlay = Some(Overlay(outer, good, bad))
  }

  val remove = IO[Unit] {
    overlay.foreach { o =>
      document.body.removeChild(o.outer)
      overlay = None
    }
  }

  def withOverlay(f: Overlay => Unit): IO[Unit] =
    IO(overlay foreach f)

  def update($: Comp) = withOverlay { o =>
    // Update position
    val rect = $.getDOMNode().getBoundingClientRect()
    o.outer.style.top  = (window.pageYOffset + rect.top) + "px"
    o.outer.style.left = rect.left + "px"

    // Update content
    o.good.innerHTML = good.toString
    o.bad.innerHTML = badCount.toString
  }

  val onClick = IO[Unit] {
    if (bad.nonEmpty) {
      var i = howManyReasonsToShowOnClick min badCount
      println(s"Last $i reasons to update:")
      for (r <- bad.takeRight(i)) {
        println(s"#$i: $r")
        i -= 1
      }
    }
    val sum = good + badCount
    if (sum != 0)
      printf("%d/%d (%.0f%%) updates prevented.\n", good, sum, good.toDouble / sum * 100)
  }
}
