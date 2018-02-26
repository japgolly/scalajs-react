package japgolly.scalajs.react.vdom

object ExportsStatic {

  final class VdomExtString(private val s: String) extends AnyVal {

    // May eventually make use of this
    private[vdom] def reactTerminalTag[N <: HtmlTopNode]: HtmlTagOf[N] =
      new HtmlTagOf[N](s)
  }
}

object Exports extends Exports
abstract class Exports {

  private[vdom] implicit final def VdomExtString(a: String): ExportsStatic.VdomExtString =
    new ExportsStatic.VdomExtString(a)

  final type VdomNode = japgolly.scalajs.react.vdom.VdomNode
  final val VdomNode = japgolly.scalajs.react.vdom.VdomNode

  final type VdomElement = japgolly.scalajs.react.vdom.VdomElement
  final val VdomElement = japgolly.scalajs.react.vdom.VdomElement

  final val ReactFragment = japgolly.scalajs.react.vdom.ReactFragment

  final val ReactPortal = japgolly.scalajs.react.vdom.ReactPortal

  final type VdomTagOf[+N <: TopNode] = japgolly.scalajs.react.vdom.TagOf[N]
  final type VdomTag = VdomTagOf[TopNode]

  final type HtmlTagOf[+N <: HtmlTopNode] = japgolly.scalajs.react.vdom.HtmlTagOf[N]
  final val HtmlTagOf = japgolly.scalajs.react.vdom.HtmlTagOf
  final type HtmlTag = HtmlTagOf[HtmlTopNode]
  @inline final def HtmlTag(name: String): HtmlTag = HtmlTagOf(name)

  final type SvgTagOf[+N <: SvgTopNode] = japgolly.scalajs.react.vdom.SvgTagOf[N]
  final val SvgTagOf = japgolly.scalajs.react.vdom.SvgTagOf
  final type SvgTag = SvgTagOf[SvgTopNode]
  @inline final def SvgTag(name: String): SvgTag = SvgTagOf(name)

  /** Tag modification. Apply it to a [[VdomTag]]. */
  final type TagMod = japgolly.scalajs.react.vdom.TagMod
  final val TagMod = japgolly.scalajs.react.vdom.TagMod
  final val EmptyVdom = japgolly.scalajs.react.vdom.VdomNode.empty

  final type VdomAttr[-U] = japgolly.scalajs.react.vdom.Attr[U]
  final val VdomAttr = japgolly.scalajs.react.vdom.Attr

  final val VdomStyle = japgolly.scalajs.react.vdom.Style

  final type VdomArray = japgolly.scalajs.react.vdom.VdomArray
  final val VdomArray = japgolly.scalajs.react.vdom.VdomArray

}
