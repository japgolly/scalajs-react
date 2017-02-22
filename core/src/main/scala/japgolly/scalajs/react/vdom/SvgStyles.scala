package japgolly.scalajs.react.vdom

import PackageBase._

object SvgStyles extends SvgStyles
trait SvgStyles {

  /** NOTE: This is a Style because react.js does not support it as an Attribute
    *
    * Sometimes it is of interest to let the outline of an object keep its original width no matter which transforms are
    * applied to it. For example, in a map with a 2px wide line representing roads it is of interest to keep the roads
    * 2px wide even when the user zooms into the map. To achieve this, SVG Tiny 1.2 introduces the 'vector-effect'
    * property. Future versions of the SVG language will allow for more powerful vector effects through this property
    * but this version restricts it to being able to specify the non-scaling stroke behavior
    */
  object vectorEffect extends Style[String]("vectorEffect") {

    /**
      * Specifies that no vector effect shall be applied, i.e. the default rendering behaviour from SVG 1.1 is used which
      * is to first fill the geometry of a shape with a specified paint, then stroke the outline with a specified paint.
      */
    final def none = this := "none"

    /**
      *
      * Modifies the way an object is stroked. Normally stroking involves calculating stroke outline of the shape's
      * path in current user space and filling that outline with the stroke paint (color or gradient).
      * With the non-scaling-stroke vector effect, stroke outline shall be calculated in the "host" coordinate space
      * instead of user coordinate space. More precisely: a user agent establishes a host coordinate space which in
      * SVG Tiny 1.2 is always the same as "screen coordinate space". The stroke outline is calculated in the
      * following manner: first, the shape's path is transformed into the host coordinate space.
      * Stroke outline is calculated in the host coordinate space. The resulting outline is transformed back to the
      * user coordinate system. (Stroke outline is always filled with stroke paint in the current user space).
      * The resulting visual effect of this modification is that stroke width is not dependant on the transformations
      * of the element (including non-uniform scaling and shear transformations) and zoom level.
      **/
    final def nonScalingStroke = this := "non-scaling-stroke"
  }

}
