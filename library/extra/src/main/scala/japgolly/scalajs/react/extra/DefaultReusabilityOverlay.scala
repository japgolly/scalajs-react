package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.ReusabilityOverlay.Comp
import japgolly.scalajs.react.util.DefaultEffects.{Sync => DS}
import japgolly.scalajs.react.util.DomUtil._
import japgolly.scalajs.react.util.Effect.Sync
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html.Element
import org.scalajs.dom.{CSSStyleDeclaration, Node, console, document, window}
import scala.concurrent.duration._

object DefaultReusabilityOverlay {

  /** When you're in dev-mode (i.e. `fastOptJS`), this overrides [[Reusability.shouldComponentUpdate]] to use overlays. */
  def overrideGloballyInDev(): Unit =
    overrideGloballyInDev(defaults)

  /** When you're in dev-mode (i.e. `fastOptJS`), this overrides [[Reusability.shouldComponentUpdate]] to use overlays. */
  def overrideGloballyInDev[F[_]: Sync](options: Options[F]): Unit =
    ScalaJsReactConfig.Defaults.overrideReusabilityInDev(
      new ScalaJsReactConfig.ReusabilityOverride {
        override def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot] =
          ReusabilityOverlay.install(options)
      }
    )

  lazy val defaults = Options[DS](
    template             = ShowGoodAndBadCounts,
    reasonsToShowOnClick = 10,
    updatePositionEvery  = 500 millis,
    dynamicStyle         = (_,_,_) => (),
    mountHighlighter     = defaultMountHighlighter,
    updateHighlighter    = defaultUpdateHighlighter)

  case class Options[F[_]](template            : Template,
                           reasonsToShowOnClick: Int,
                           updatePositionEvery : FiniteDuration,
                           dynamicStyle        : (Int, Int, Nodes) => Unit,
                           mountHighlighter    : Comp => F[Unit],
                           updateHighlighter   : Comp => F[Unit])

  implicit def apply[F[_]: Sync, P, S, B](options: Options[F]): ScalaComponent.MountedImpure[P, S, B] => ReusabilityOverlay[F] =
    a => {
      val b = a.asInstanceOf[Comp]
      new DefaultReusabilityOverlay(b, options)
    }

  private[DefaultReusabilityOverlay] implicit def autoLiftHtml(n: Node): Element = n.domAsHtml

  trait Template {
    def template: VdomElement
    def good(e: Element): Node
    def bad(e: Element): Node
  }

  val styleAll: TagMod =
    TagMod(^.fontSize := "0.9em", ^.lineHeight := "0.9em")

  val defaultContainer =
    <.div(
      ^.background   := "rgba(248,248,248,.83)",
      ^.padding      := "1px 2px",
      ^.border       := "solid 1px black",
      ^.borderRadius := "4px",
      ^.position     := "absolute",
      ^.zIndex       := "10000",
      ^.color        := "#444",
      ^.fontWeight   := "normal",
      ^.boxShadow    := "0 2px 6px rgba(0,0,0,0.25)",
      ^.cursor       := "pointer")

  object ShowGoodAndBadCounts extends Template {
    override val template: VdomElement =
      defaultContainer(styleAll,
        <.span(styleAll, ^.color := "#070"),
        <.span(styleAll, ^.padding := "0", ^.margin := "0 0.4ex", "-"),
        <.span(styleAll, ^.color := "#900", ^.fontWeight := "bold"))
    override def good(e: Element) = e childNodes 0
    override def bad (e: Element) = e childNodes 2
  }

  object ShowBadCount extends Template {
    override val template: VdomElement =
      defaultContainer(styleAll,
        <.span(styleAll, ^.display := "none"),
        <.span(styleAll, ^.color := "#900", ^.fontWeight := "bold"))
    override def good(e: Element) = e childNodes 0
    override def bad (e: Element) = e childNodes 1
  }

  def highlighter(before: CSSStyleDeclaration => Unit,
                  frame1: CSSStyleDeclaration => Unit,
                  frame2: CSSStyleDeclaration => Unit): Comp => DS[Unit] =
    $ => DS.delay {
      $.getDOMNode.mounted.map(_.node).foreach { n =>
        before(n.style)
        window.requestAnimationFrame{(_: Double) =>
          frame1(n.style)
          window.requestAnimationFrame((_: Double) =>
            frame2(n.style)
          )
        }
      }
    }

  def outlineHighlighter(outlineCss: String) =
    highlighter(
      _.boxSizing = "border-box",
      s => {
        s.transition = "outline 0s"
        s.outline = outlineCss
      },
      s => {
        s.outline = "1px solid rgba(255,255,255,0)"
        s.transition = "outline 800ms ease-out"
      })

  val defaultMountHighlighter = outlineHighlighter("2px solid #1e90ff")

  val defaultUpdateHighlighter = outlineHighlighter("2px solid rgba(200,20,10,1)")

  case class Nodes(outer: Element, good: Element, bad: Element)

}

class DefaultReusabilityOverlay[F[_]]($: Comp, options: DefaultReusabilityOverlay.Options[F])(implicit F: Sync[F]) extends ReusabilityOverlay[F] with TimerSupportF[F] {
  import DefaultReusabilityOverlay.{Nodes, autoLiftHtml}

  override protected def onUnmountEffect = F

  protected var good = 0
  protected var bad = Vector("Initial mount.")
  protected def badCount = bad.size
  protected var overlay: Option[Nodes] = None

  val onClick = F.delay {
    if (bad.nonEmpty) {
      var i = options.reasonsToShowOnClick min badCount
      console.info(s"Last $i reasons to update:")
      for (r <- bad.takeRight(i)) {
        console.info(s"#-$i: $r")
        i -= 1
      }
    }
    val sum = good + badCount
    if (sum != 0)
      console info "%d/%d (%.0f%%) updates prevented.".format(good, sum, good.toDouble / sum * 100)
  }

  val create = F.delay {

    // Create
    val tmp = document.createElement("div").domAsHtml
    document.body.appendChild(tmp)
    options.template.template.renderIntoDOM(tmp, F.delay {
      val outer = tmp.firstChild
      document.body.replaceChild(outer, tmp)

      // Customise
      outer.addEventListener("click", (_: Any) => F.runSync(onClick))

      // Store
      val good = options.template good outer
      val bad  = options.template bad outer
      overlay  = Some(Nodes(outer, good, bad))
    })

    ()
  }

  def withNodes(f: Nodes => Unit): F[Unit] =
    F.delay(overlay foreach f)

  val updatePosition = withNodes { n =>
    $.getDOMNode.mounted.map(_.node).foreach { d =>
      val ds = d.getBoundingClientRect()
      val ns = n.outer.getBoundingClientRect()

      var y = window.pageYOffset + ds.top
      var x = ds.left

      if (d.tagName == "TABLE") {
        y -= ns.height
        x -= ns.width
      } else if (d.tagName == "TR")
        x -= ns.width

      n.outer.style.top  = y.toString + "px"
      n.outer.style.left = x.toString + "px"
    }
  }

  val updateContent = withNodes { n =>
    val b = badCount
    n.good.innerHTML = good.toString
    n.bad.innerHTML = b.toString
    options.dynamicStyle(good, b, n)
    n.outer.setAttribute("title", "Last update reason:\n" + bad.lastOption.getOrElse("None"))
  }

  val update =
    F.chain(updateContent, updatePosition)

  val highlightUpdate =
    options.updateHighlighter($)

  override val logGood =
    F.chain(F.delay(good += 1), update)

  override def logBad(reason: String) =
    F.chain(F.delay(bad :+= reason), update, highlightUpdate)

  override val onMount =
    F.chain(
      create,
      update,
      options.mountHighlighter($),
      setInterval(updatePosition, options.updatePositionEvery))

  override val onUnmount = {
    val f = F.delay {
      overlay.foreach { o =>
        document.body.removeChild(o.outer)
        overlay = None
      }
    }
    F.chain(super.unmount, f)
  }
}
