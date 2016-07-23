package japgolly.scalajs.react.vdom

object ExportsStatic {

  final class VdomExtString(private val s: String) extends AnyVal {

    @inline def reactAttr[A]: ReactAttr[A] =
      ReactAttr(s)

//    @inline def reactStyle: ReactStyle =
//      new ReactStyle.Generic(s)
//
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

  //  final type ReactTagOf[+N <: TopNode] = japgolly.scalajs.react.vdom.ReactTagOf[N]
  //  final type ReactTag                  = japgolly.scalajs.react.vdom.ReactTagOf[TopNode]
  //  final type ReactStyle                = japgolly.scalajs.react.vdom.ReactStyle
  //  @inline final def ReactStyle = japgolly.scalajs.react.vdom.ReactStyle
}

//trait Tags extends HtmlTags with Extra.Tags
//trait JustTags extends Tags { final def svg = SvgTags }
//object Tags extends JustTags
//
//trait Attrs extends HtmlAttrs with Extra.Attrs with HtmlStyles
//trait JustAttrs extends Attrs { final def svg = SvgAttrs }
//object Attrs extends JustAttrs
