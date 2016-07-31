package japgolly.scalajs.react.vdom

object ExportsStatic {

  final class VdomExtString(private val s: String) extends AnyVal {

    @deprecated("Use ReactAttr[A](\"name\") instead.", "1.0")
    def reactAttr[A]: Attr[A] =
      Attr[A](s)

    @deprecated("Use ReactAttr.style[A](\"name\") instead.", "1.0")
    def reactStyle[A]: Attr[A] =
      Attr.style[A](s)

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

  final type ReactAttr[-U] = japgolly.scalajs.react.vdom.Attr[U]
  final val ReactAttr = japgolly.scalajs.react.vdom.Attr

  final type ReactTagOf[+N <: TopNode] = japgolly.scalajs.react.vdom.TagOf[N]
  final type ReactTag = ReactTagOf[TopNode]

  final type HtmlTagOf[+N <: HtmlTopNode] = japgolly.scalajs.react.vdom.HtmlTagOf[N]
  final type HtmlTag = HtmlTagOf[HtmlTopNode]

  final type SvgTagOf[+N <: SvgTopNode] = japgolly.scalajs.react.vdom.SvgTagOf[N]
  final type SvgTag = SvgTagOf[SvgTopNode]

  //  final type Style                = japgolly.scalajs.react.vdom.Style
  //  @inline final def Style = japgolly.scalajs.react.vdom.Style
}
