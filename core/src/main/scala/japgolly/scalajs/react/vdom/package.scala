package japgolly.scalajs.react

/**
 * Scalatags version = cdf3d4797236a7d31dc413c1c8ba45466ea4a1c6
 */
package object vdom {

  val EmptyTag: TagMod = new TagMod {
    def applyTo(t: Builder) = ()
  }

  trait Tags extends HtmlTags with Extra.Tags
  trait JustTags extends Tags { final def svg = SvgTags }
  object Tags extends JustTags

  trait Attrs extends HtmlAttrs with Extra.Attrs with HtmlStyles
  trait JustAttrs extends Attrs { final def svg = SvgAttrs }
  object Attrs extends JustAttrs

  // If you're wondering why abstract class instead of trait, https://issues.scala-lang.org/browse/SI-4767
  abstract class Base extends Implicits {
    final type ReactTagOf[+N <: TopNode] = japgolly.scalajs.react.vdom.ReactTagOf[N]
    final type ReactTag                  = japgolly.scalajs.react.vdom.ReactTagOf[TopNode]
    final type TagMod                    = japgolly.scalajs.react.vdom.TagMod

    @inline final def TagMod     = japgolly.scalajs.react.vdom.TagMod
    @inline final def EmptyTag   = japgolly.scalajs.react.vdom.EmptyTag
    @inline final def ReactAttr  = japgolly.scalajs.react.vdom.Attr
    @inline final def ReactStyle = japgolly.scalajs.react.vdom.Style
  }

  object all extends Base with Tags with Attrs {
    object svg extends SvgTags with SvgAttrs
    @inline def keyAttr = key
    @inline def refAttr = ref
  }

  object prefix_<^ extends Base {
    @inline def < = Tags
    @inline def ^ = Attrs
  }

  object svg {
    object all extends Base with SvgTags with SvgAttrs

    object prefix_<^ extends Base {
      @inline def < = SvgTags
      @inline def ^ = SvgAttrs
    }
  }
}