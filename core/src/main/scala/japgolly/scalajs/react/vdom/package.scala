package japgolly.scalajs.react

/**
 * Scalatags version = 90f796e6f9f1ee79b5961f4199bd1e56ae8e318d
 */
package object vdom {

  trait Tags extends HtmlTags with Extra.Tags
  trait JustTags extends Tags { final def svg = SvgTags }
  object Tags extends JustTags

  trait Attrs extends HtmlAttrs with Extra.Attrs with HtmlStyles
  trait JustAttrs extends Attrs { final def svg = SvgAttrs }
  object Attrs extends JustAttrs

  // If you're wondering why abstract class instead of trait, https://issues.scala-lang.org/browse/SI-4767
  abstract class Base extends Implicits {
    final type ReactTag = japgolly.scalajs.react.vdom.ReactTag
    final type TagMod   = japgolly.scalajs.react.vdom.TagMod
    @inline final def EmptyTag = japgolly.scalajs.react.vdom.EmptyTag
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