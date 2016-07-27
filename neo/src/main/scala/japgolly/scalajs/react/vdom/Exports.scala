package japgolly.scalajs.react.vdom

object ExportsStatic {

  final class VdomExtString(private val s: String) extends AnyVal {

    @inline def reactAttr[A]: ReactAttr[A] =
      ReactAttr(s)

//    @inline def reactStyle: ReactStyle =
//      new ReactStyle.Generic(s)

    @inline def reactTag[N <: HtmlTopNode]: HtmlTagOf[N] =
      new HtmlTagOf[N](s)

    // May eventually make use of this
    @inline private[vdom] def reactTerminalTag[N <: HtmlTopNode]: HtmlTagOf[N] =
      new HtmlTagOf[N](s)

//    @inline def reactTag[N <: TopNode]: ReactTagOf[N] =
//      makeAbstractReactTag(s, NamespaceHtml.implicitNamespace)
  }
}

object Exports extends Exports
abstract class Exports {

  @inline implicit final def VdomExtString(a: String) = new ExportsStatic.VdomExtString(a)

  final val EmptyTag = japgolly.scalajs.react.vdom.TagMod.Empty

  final type TagMod = japgolly.scalajs.react.vdom.TagMod
  final val TagMod = japgolly.scalajs.react.vdom.TagMod

  final type ReactAttr[-U] = japgolly.scalajs.react.vdom.ReactAttr[U]
  final val ReactAttr = japgolly.scalajs.react.vdom.ReactAttr

  final type ReactTagOf[+N <: TopNode] = japgolly.scalajs.react.vdom.ReactTagOf[N]
  final type ReactTag = ReactTagOf[TopNode]

  final type ReactHtmlTagOf[+N <: HtmlTopNode] = japgolly.scalajs.react.vdom.HtmlTagOf[N]
  final type ReactHtmlTag = HtmlTagOf[HtmlTopNode]

  final type ReactSvgTagOf[+N <: SvgTopNode] = japgolly.scalajs.react.vdom.SvgTagOf[N]
  final type ReactSvgTag = SvgTagOf[SvgTopNode]

  //  final type ReactStyle                = japgolly.scalajs.react.vdom.ReactStyle
  //  @inline final def ReactStyle = japgolly.scalajs.react.vdom.ReactStyle
}
