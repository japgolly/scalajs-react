package japgolly.scalajs.react.vdom

object ExportsStatic {

  final class VdomExtString(private val s: String) extends AnyVal {

    // TODO Should this just be ReactAttr(name)
    def reactAttr[A]: Attr[A] =
      new Attr.Generic(s)

    def reactStyle[A]: Attr[A] =
      new Attr.Style.Child(s)

    @inline def reactTag[N <: HtmlTopNode]: HtmlTagOf[N] =
      new HtmlTagOf[N](s)

    // May eventually make use of this
    @inline private[vdom] def reactTerminalTag[N <: HtmlTopNode]: HtmlTagOf[N] =
      new HtmlTagOf[N](s)
  }
}

object Exports extends Exports
abstract class Exports {

  @inline implicit final def VdomExtString(a: String) = new ExportsStatic.VdomExtString(a)

  final type ReactElement = japgolly.scalajs.react.vdom.ReactElement
  final val ReactElement = japgolly.scalajs.react.vdom.ReactElement

  final type ReactNode = japgolly.scalajs.react.vdom.ReactNode
  final val ReactNode = japgolly.scalajs.react.vdom.ReactNode

  final val EmptyTag = japgolly.scalajs.react.vdom.TagMod.Empty

  final type TagMod = japgolly.scalajs.react.vdom.TagMod
  final val TagMod = japgolly.scalajs.react.vdom.TagMod

  // TODO Shouldn't the alias be ReactAttr?
  final type Attr[-U] = japgolly.scalajs.react.vdom.Attr[U]
  final val Attr = japgolly.scalajs.react.vdom.Attr

  final type TagOf[+N <: TopNode] = japgolly.scalajs.react.vdom.TagOf[N]
  final type Tag = TagOf[TopNode]

  final type HtmlTagOf[+N <: HtmlTopNode] = japgolly.scalajs.react.vdom.HtmlTagOf[N]
  final type HtmlTag = HtmlTagOf[HtmlTopNode]

  final type SvgTagOf[+N <: SvgTopNode] = japgolly.scalajs.react.vdom.SvgTagOf[N]
  final type SvgTag = SvgTagOf[SvgTopNode]

  //  final type Style                = japgolly.scalajs.react.vdom.Style
  //  @inline final def Style = japgolly.scalajs.react.vdom.Style
}
