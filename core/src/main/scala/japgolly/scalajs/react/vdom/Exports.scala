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

  final val EmptyVdom: VdomNode            = japgolly.scalajs.react.vdom.VdomNode.empty

  @inline
  final def  HtmlTag(name: String): HtmlTag = HtmlTagOf(name)
  final type HtmlTag                        = HtmlTagOf[HtmlTopNode]
  final type HtmlTagOf[+N <: HtmlTopNode]   = japgolly.scalajs.react.vdom.HtmlTagOf[N]
  final val  HtmlTagOf                      = japgolly.scalajs.react.vdom.HtmlTagOf

  @deprecated("Use React.Fragment", "1.3.0")
  final val ReactFragment                   = japgolly.scalajs.react.feature.ReactFragment

  final val ReactPortal                     = japgolly.scalajs.react.vdom.ReactPortal

  @inline
  final def  SvgTag(name: String): SvgTag   = SvgTagOf(name)
  final type SvgTag                         = SvgTagOf[SvgTopNode]
  final type SvgTagOf[+N <: SvgTopNode]     = japgolly.scalajs.react.vdom.SvgTagOf[N]
  final val  SvgTagOf                       = japgolly.scalajs.react.vdom.SvgTagOf

  /** Tag modifier.
    * Apply it to a [[VdomTag]]. */
  final type TagMod                         = japgolly.scalajs.react.vdom.TagMod
  final val  TagMod                         = japgolly.scalajs.react.vdom.TagMod

  final type VdomAttr[-U]                   = japgolly.scalajs.react.vdom.Attr[U]
  final val  VdomAttr                       = japgolly.scalajs.react.vdom.Attr

  final type VdomArray                      = japgolly.scalajs.react.vdom.VdomArray
  final val  VdomArray                      = japgolly.scalajs.react.vdom.VdomArray

  final type VdomElement                    = japgolly.scalajs.react.vdom.VdomElement
  final val  VdomElement                    = japgolly.scalajs.react.vdom.VdomElement

  final type VdomNode                       = japgolly.scalajs.react.vdom.VdomNode
  final val  VdomNode                       = japgolly.scalajs.react.vdom.VdomNode

  final val  VdomStyle                      = japgolly.scalajs.react.vdom.Style

  final type VdomTag                        = VdomTagOf[TopNode]
  final type VdomTagOf[+N <: TopNode]       = japgolly.scalajs.react.vdom.TagOf[N]

}
