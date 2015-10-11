package japgolly.scalajs.react.vdom

import Scalatags._

object SvgAttrs extends SvgAttrs

trait SvgAttrs {

  /**
   * This attribute defines the distance from the origin to the top of accent characters,
   * measured by a distance within the font coordinate system.
   * If the attribute is not specified, the effect is as if the attribute
   * were set to the value of the ascent attribute.
   *
   * Value 	<number>
   *
   * MDN
   */
  final val accentHeight = "accent-height".attr

  /**
   * This attribute controls whether or not the animation is cumulative.
   * It is frequently useful for repeated animations to build upon the previous results,
   * accumulating with each iteration. This attribute said to the animation if the value is added to
   * the previous animated attribute's value on each iteration.
   *
   * Value 	none | sum
   *
   * MDN
   */
  final val accumulate = "accumulate".attr

  /**
   * This attribute controls whether or not the animation is additive.
   * It is frequently useful to define animation as an offset or delta
   * to an attribute's value, rather than as absolute values. This
   * attribute said to the animation if their values are added to the
   * original animated attribute's value.
   *
   * Value 	replace | sum
   *
   * MDN
   */
  final val additive = "additive".attr

  /**
   * The alignment-baseline attribute specifies how an object is aligned
   * with respect to its parent. This property specifies which baseline
   * of this element is to be aligned with the corresponding baseline of
   * the parent. For example, this allows alphabetic baselines in Roman
   * text to stay aligned across font size changes. It defaults to the
   * baseline with the same name as the computed value of the
   * alignment-baseline property. As a presentation attribute, it also
   * can be used as a property directly inside a CSS stylesheet, see css
   * alignment-baseline for further information.
   *
   * Value: 	auto | baseline | before-edge | text-before-edge | middle | central | after-edge |
   * text-after-edge | ideographic | alphabetic | hanging | mathematical | inherit
   *
   * MDN
   */
  final val alignmentBaseline = "alignment-baseline".attr


  /**
   * This attribute defines the maximum unaccented depth of the font
   * within the font coordinate system. If the attribute is not specified,
   * the effect is as if the attribute were set to the vert-origin-y value
   * for the corresponding font.
   *
   * Value 	<number>
   *
   * MDN
   */
  final val ascent = "ascent".attr


  /**
   * This attribute indicates the name of the attribute in the parent element
   * that is going to be changed during an animation.
   *
   * Value 	<attributeName>
   *
   * MDN
   */
  final val attributeName = "attributeName".attr


  /**
   * This attribute specifies the namespace in which the target attribute
   * and its associated values are defined.
   *
   * Value 	CSS | XML | auto
   *
   * MDN
   */
  final val attributeType = "attributeType".attr


  /**
   * The azimuth attribute represent the direction angle for the light
   * source on the XY plane (clockwise), in degrees from the x axis.
   * If the attribute is not specified, then the effect is as if a
   * value of 0 were specified.
   *
   * Value 	<number>
   *
   * MDN
   */
  final val azimuth = "azimuth".attr


  /**
   * The baseFrequency attribute represent The base frequencies parameter
   * for the noise function of the <feturbulence> primitive. If two <number>s
   * are provided, the first number represents a base frequency in the X
   * direction and the second value represents a base frequency in the Y direction.
   * If one number is provided, then that value is used for both X and Y.
   * Negative values are forbidden.
   * If the attribute is not specified, then the effect is as if a value
   * of 0 were specified.
   *
   * Value 	<number-optional-number>
   *
   * MDN
   */
  final val baseFrequency = "baseFrequency".attr


  /**
   * The baseline-shift attribute allows repositioning of the dominant-baseline
   * relative to the dominant-baseline of the parent text content element.
   * The shifted object might be a sub- or superscript.
   * As a presentation attribute, it also can be used as a property directly
   * inside a CSS stylesheet, see css baseline-shift for further information.
   *
   * Value 	auto | baseline | sup | sub | <percentage> | <length> | inherit
   *
   * MDN
   */
  final val baselineShift = "baseline-shift".attr


  /**
   * This attribute defines when an animation should begin.
   * The attribute value is a semicolon separated list of values. The interpretation
   * of a list of start times is detailed in the SMIL specification in "Evaluation
   * of begin and end time lists". Each individual value can be one of the following:
   * <offset-value>, <syncbase-value>, <event-value>, <repeat-value>, <accessKey-value>,
   * <wallclock-sync-value> or the keyword indefinite.
   *
   * Value 	<begin-value-list>
   *
   * MDN
   */
  final val begin = "begin".attr


  /**
   * The bias attribute shifts the range of the filter. After applying the kernelMatrix
   * of the <feconvolvematrix> element to the input image to yield a number and applied
   * the divisor attribute, the bias attribute is added to each component. This allows
   * representation of values that would otherwise be clamped to 0 or 1.
   * If bias is not specified, then the effect is as if a value of 0 were specified.
   *
   * Value 	<number>
   *
   * MDN
   */
  final val bias = "bias".attr


  /**
   * This attribute specifies the interpolation mode for the animation. The default
   * mode is linear, however if the attribute does not support linear interpolation
   * (e.g. for strings), the calcMode attribute is ignored and discrete interpolation is used.
   *
   * Value 	discrete | linear | paced | spline
   *
   * MDN
   */
  final val calcMode = "calcMode".attr


  /**
   * Assigns a class name or set of class names to an element. You may assign the same
   * class name or names to any number of elements. If you specify multiple class names,
   * they must be separated by whitespace characters.
   * The class name of an element has two key roles:
   * -As a style sheet selector, for use when an author wants to assign style
   * information to a set of elements.
   * -For general usage by the browser.
   * The class can be used to style SVG content using CSS.
   *
   * Value 	<list-of-class-names>
   *
   * MDN
   */
  final val `class` = "class".attr


  /**
   * The clip attribute has the same parameter values as defined for the css clip property.
   * Unitless values, which indicate current user coordinates, are permitted on the coordinate
   * values on the <shape>. The value of auto defines a clipping path along the bounds of
   * the viewport created by the given element.
   * As a presentation attribute, it also can be used as a property directly inside a
   * CSS stylesheet, see css clip for further information.
   *
   * Value 	auto | <shape> | inherit
   *
   * MDN
   */
  final val clip = "clip".attr


  /**
   * The clip-path attribute bind the element is applied to with a given <clippath> element
   * As a presentation attribute, it also can be used as a property directly inside a CSS stylesheet
   *
   * Value 	<FuncIRI> | none | inherit
   *
   * MDN
   */
  final val clipPath = "clip-path".attr

  /**
   * The clipPathUnits attribute defines the coordinate system for the contents
   * of the <clippath> element. the clipPathUnits attribute is not specified,
   * then the effect is as if a value of userSpaceOnUse were specified.
   * Note that values defined as a percentage inside the content of the <clippath>
   * are not affected by this attribute. It means that even if you set the value of
   * maskContentUnits to objectBoundingBox, percentage values will be calculated as
   * if the value of the attribute were userSpaceOnUse.
   *
   * Value 	userSpaceOnUse | objectBoundingBox
   *
   * MDN
   */
  final val clipPathUnits = "clipPathUnits".attr

  /**
   * The clip-rule attribute only applies to graphics elements that are contained within a
   * <clippath> element. The clip-rule attribute basically works as the fill-rule attribute,
   * except that it applies to <clippath> definitions.
   *
   * Value 	nonezero | evenodd | inherit
   *
   * MDN
   */
  final val clipRule = "clip-rule".attr

  /**
   * The color attribute is used to provide a potential indirect value (currentColor)
   * for the fill, stroke, stop-color, flood-color and lighting-color attributes.
   * As a presentation attribute, it also can be used as a property directly inside a CSS
   * stylesheet, see css color for further information.
   *
   * Value 	<color> | inherit
   *
   * MDN
   */
  final val color = "color".attr


  /**
   * The color-interpolation attribute specifies the color space for gradient interpolations,
   * color animations and alpha compositing.When a child element is blended into a background,
   * the value of the color-interpolation attribute on the child determines the type of
   * blending, not the value of the color-interpolation on the parent. For gradients which
   * make use of the xlink:href attribute to reference another gradient, the gradient uses
   * the color-interpolation attribute value from the gradient element which is directly
   * referenced by the fill or stroke attribute. When animating colors, color interpolation
   * is performed according to the value of the color-interpolation attribute on the element
   * being animated.
   * As a presentation attribute, it also can be used as a property directly inside a CSS
   * stylesheet, see css color-interpolation for further information
   *
   * Value 	auto | sRGB | linearRGB | inherit
   *
   * MDN
   */
  final val colorInterpolation = "color-interpolation".attr


  /**
   * The color-interpolation-filters attribute specifies the color space for imaging operations
   * performed via filter effects. Note that color-interpolation-filters has a different
   * initial value than color-interpolation. color-interpolation-filters has an initial
   * value of linearRGB, whereas color-interpolation has an initial value of sRGB. Thus,
   * in the default case, filter effects operations occur in the linearRGB color space,
   * whereas all other color interpolations occur by default in the sRGB color space.
   * As a presentation attribute, it also can be used as a property directly inside a
   * CSS stylesheet, see css color-interpolation-filters for further information
   *
   * Value 	auto | sRGB | linearRGB | inherit
   *
   * MDN
   */
  final val colorInterpolationFilters = "color-interpolation-filters".attr


  /**
   * The color-profile attribute is used to define which color profile a raster image
   * included through the <image> element should use. As a presentation attribute, it
   * also can be used as a property directly inside a CSS stylesheet, see css color-profile
   * for further information.
   *
   * Value 	auto | sRGB | <name> | <IRI> | inherit
   *
   * MDN
   */
  final val colorProfile = "color-profile".attr


  /**
   * The color-rendering attribute provides a hint to the SVG user agent about how to
   * optimize its color interpolation and compositing operations. color-rendering
   * takes precedence over color-interpolation-filters. For example, assume color-rendering:
   * optimizeSpeed and color-interpolation-filters: linearRGB. In this case, the SVG user
   * agent should perform color operations in a way that optimizes performance, which might
   * mean sacrificing the color interpolation precision as specified by
   * color-interpolation-filters: linearRGB.
   * As a presentation attribute, it also can be used as a property directly inside
   * a CSS stylesheet, see css color-rendering for further information
   *
   * Value 	auto | optimizeSpeed | optimizeQuality | inherit
   *
   * MDN
   */
  final val colorRendering = "color-rendering".attr


  /**
   * The contentScriptType attribute on the <svg> element specifies the default scripting
   * language for the given document fragment.
   * This attribute sets the default scripting language used to process the value strings
   * in event attributes. This language must be used for all instances of script that do not
   * specify their own scripting language. The value content-type specifies a media type,
   * per MIME Part Two: Media Types [RFC2046]. The default value is application/ecmascript
   *
   * Value 	<content-type>
   *
   * MDN
   */
  final val contentScriptType = "contentScriptType".attr


  /**
   * This attribute specifies the style sheet language for the given document fragment.
   * The contentStyleType is specified on the <svg> element. By default, if it's not defined,
   * the value is text/css
   *
   * Value 	<content-type>
   *
   * MDN
   */
  final val contentStyleType = "contentStyleType".attr


  /**
   * The cursor attribute specifies the mouse cursor displayed when the mouse pointer
   * is over an element.This attribute behave exactly like the css cursor property except
   * that if the browser suport the <cursor> element, it should allow to use it with the
   * <funciri> notation. As a presentation attribute, it also can be used as a property
   * directly inside a CSS stylesheet, see css cursor for further information.
   *
   * Value 	 auto | crosshair | default | pointer | move | e-resize |
   * ne-resize | nw-resize | n-resize | se-resize | sw-resize | s-resize | w-resize| text |
   * wait | help | inherit
   *
   * MDN
   */
  final val cursor = "cursor".attr


  /**
   * For the <circle> and the <ellipse> element, this attribute define the x-axis coordinate
   * of the center of the element. If the attribute is not specified, the effect is as if a
   * value of "0" were specified.For the <radialgradient> element, this attribute define
   * the x-axis coordinate of the largest (i.e., outermost) circle for the radial gradient.
   * The gradient will be drawn such that the 100% gradient stop is mapped to the perimeter
   * of this largest (i.e., outermost) circle. If the attribute is not specified, the effect
   * is as if a value of 50% were specified
   *
   * Value 	<coordinate>
   *
   * MDN
   */
  final val cx = "cx".attr

  /**
   * For the <circle> and the <ellipse> element, this attribute define the y-axis coordinate
   * of the center of the element. If the attribute is not specified, the effect is as if a
   * value of "0" were specified.For the <radialgradient> element, this attribute define
   * the x-axis coordinate of the largest (i.e., outermost) circle for the radial gradient.
   * The gradient will be drawn such that the 100% gradient stop is mapped to the perimeter
   * of this largest (i.e., outermost) circle. If the attribute is not specified, the effect
   * is as if a value of 50% were specified
   *
   * Value 	<coordinate>
   *
   * MDN
   */
  final val cy = "cy".attr


  /**
   *
   *
   * MDN
   */
  final val d = "d".attr


  /**
   *
   *
   * MDN
   */
  final val diffuseConstant = "diffuseConstant".attr


  /**
   *
   *
   * MDN
   */
  final val direction = "direction".attr


  /**
   *
   *
   * MDN
   */
  final val display = "display".attr


  /**
   *
   *
   * MDN
   */
  final val divisor = "divisor".attr


  /**
   *
   *
   * MDN
   */
  final val dominantBaseline = "dominant-baseline".attr


  /**
   *
   *
   * MDN
   */
  final val dur = "dur".attr


  /**
   *
   *
   * MDN
   */
  final val dx = "dx".attr


  /**
   *
   *
   * MDN
   */
  final val dy = "dy".attr


  /**
   *
   *
   * MDN
   */
  final val edgeMode = "edgeMode".attr


  /**
   *
   *
   * MDN
   */
  final val elevation = "elevation".attr


  /**
   *
   *
   * MDN
   */
  final val end = "end".attr


  /**
   *
   *
   * MDN
   */
  final val externalResourcesRequired = "externalResourcesRequired".attr


  /**
   *
   *
   * MDN
   */
  final val fill = "fill".attr


  /**
   *
   *
   * MDN
   */
  final val fillOpacity = "fillOpacity".attr


  /**
   *
   *
   * MDN
   */
  final val fillRule = "fill-rule".attr


  /**
   *
   *
   * MDN
   */
  final val filter = "filter".attr


  /**
   *
   *
   * MDN
   */
  final val filterRes = "filterRes".attr


  /**
   *
   *
   * MDN
   */
  final val filterUnits = "filterUnits".attr


  /**
   *
   *
   * MDN
   */
  final val floodColor = "flood-color".attr


  /**
   *
   *
   * MDN
   */
  final val floodOpacity = "flood-opacity".attr


  /**
   *
   *
   * MDN
   */
  final val fontFamily = "fontFamily".attr


  /**
   *
   *
   * MDN
   */
  final val fontSize = "fontSize".attr


  /**
   *
   *
   * MDN
   */
  final val fontSizeAdjust = "font-size-adjust".attr


  /**
   *
   *
   * MDN
   */
  final val fontStretch = "font-stretch".attr


  /**
   *
   *
   * MDN
   */
  final val fontVariant = "font-variant".attr


  /**
   *
   *
   * MDN
   */
  final val fontWeight = "font-weight".attr


  /**
   *
   *
   * MDN
   */
  final val from = "from".attr


  /**
   *
   *
   * MDN
   */
  final val gradientTransform = "gradientTransform".attr


  /**
   *
   *
   * MDN
   */
  final val gradientUnits = "gradientUnits".attr


  /**
   *
   *
   * MDN
   */
  final val height = "height".attr


  /**
   *
   *
   * MDN
   */
  final val imageRendering = "imageRendering".attr

  final val id = "id".attr

  /**
   *
   *
   * MDN
   */
  final val in = "in".attr



  /**
   *
   *
   * MDN
   */
  final val in2 = "in2".attr



  /**
   *
   *
   * MDN
   */
  final val k1 = "k1".attr


  /**
   *
   *
   * MDN
   */
  final val k2 = "k2".attr


  /**
   *
   *
   * MDN
   */
  final val k3 = "k3".attr


  /**
   *
   *
   * MDN
   */
  final val k4 = "k4".attr



  /**
   *
   *
   * MDN
   */
  final val kernelMatrix = "kernelMatrix".attr



  /**
   *
   *
   * MDN
   */
  final val kernelUnitLength = "kernelUnitLength".attr


  /**
   *
   *
   * MDN
   */
  final val kerning = "kerning".attr


  /**
   *
   *
   * MDN
   */
  final val keySplines = "keySplines".attr



  /**
   *
   *
   * MDN
   */
  final val keyTimes = "keyTimes".attr




  /**
   *
   *
   * MDN
   */
  final val letterSpacing = "letter-spacing".attr



  /**
   *
   *
   * MDN
   */
  final val lightingColor = "lighting-color".attr



  /**
   *
   *
   * MDN
   */
  final val limitingConeAngle = "limitingConeAngle".attr



  /**
   *
   *
   * MDN
   */
  final val local = "local".attr



  /**
   *
   *
   * MDN
   */
  final val markerEnd = "markerEnd".attr


  /**
   *
   *
   * MDN
   */
  final val markerMid = "markerMid".attr


  /**
   *
   *
   * MDN
   */
  final val markerStart = "markerStart".attr


  /**
   *
   *
   * MDN
   */
  final val markerHeight = "markerHeight".attr


  /**
   *
   *
   * MDN
   */
  final val markerUnits = "markerUnits".attr


  /**
   *
   *
   * MDN
   */
  final val markerWidth = "markerWidth".attr


  /**
   *
   *
   * MDN
   */
  final val maskContentUnits = "maskContentUnits".attr


  /**
   *
   *
   * MDN
   */
  final val maskUnits = "maskUnits".attr


  /**
   *
   *
   * MDN
   */
  final val mask = "mask".attr



  /**
   *
   *
   * MDN
   */
  final val max = "max".attr



  /**
   *
   *
   * MDN
   */
  final val min = "min".attr


  /**
   *
   *
   * MDN
   */
  final val mode = "mode".attr


  /**
   *
   *
   * MDN
   */
  final val numOctaves = "numOctaves".attr


  final val offset = "offset".attr

  /**
   *
   *
   * MDN
   */
  final val opacity = "opacity".attr



  /**
   *
   *
   * MDN
   */
  final val operator = "operator".attr


  /**
   *
   *
   * MDN
   */
  final val order = "order".attr


  /**
   *
   *
   * MDN
   */
  final val overflow = "overflow".attr



  /**
   *
   *
   * MDN
   */
  final val paintOrder = "paint-order".attr



  /**
   *
   *
   * MDN
   */
  final val pathLength = "pathLength".attr



  /**
   *
   *
   * MDN
   */
  final val patternContentUnits = "patternContentUnits".attr


  /**
   *
   *
   * MDN
   */
  final val patternTransform = "patternTransform".attr



  /**
   *
   *
   * MDN
   */
  final val patternUnits = "patternUnits".attr



  /**
   *
   *
   * MDN
   */
  final val pointerEvents = "pointer-events".attr


  /**
   *
   *
   * MDN
   */
  final val points = "points".attr


  /**
   *
   *
   * MDN
   */
  final val pointsAtX = "pointsAtX".attr


  /**
   *
   *
   * MDN
   */
  final val pointsAtY = "pointsAtY".attr


  /**
   *
   *
   * MDN
   */
  final val pointsAtZ = "pointsAtZ".attr


  /**
   *
   *
   * MDN
   */
  final val preserveAlpha = "preserveAlpha".attr



  /**
   *
   *
   * MDN
   */
  final val preserveAspectRatio = "preserveAspectRatio".attr



  /**
   *
   *
   * MDN
   */
  final val primitiveUnits = "primitiveUnits".attr


  /**
   *
   *
   * MDN
   */
  final val r = "r".attr



  /**
   *
   *
   * MDN
   */
  final val radius = "radius".attr


  /**
   *
   *
   * MDN
   */
  final val repeatCount = "repeatCount".attr


  /**
   *
   *
   * MDN
   */
  final val repeatDur = "repeatDur".attr



  /**
   *
   *
   * MDN
   */
  final val requiredFeatures = "requiredFeatures".attr



  /**
   *
   *
   * MDN
   */
  final val restart = "restart".attr



  /**
   *
   *
   * MDN
   */
  final val result = "result".attr



  /**
   *
   *
   * MDN
   */
  final val rx = "rx".attr



  /**
   *
   *
   * MDN
   */
  final val ry = "ry".attr



  /**
   *
   *
   * MDN
   */
  final val scale = "scale".attr



  /**
   *
   *
   * MDN
   */
  final val seed = "seed".attr



  /**
   *
   *
   * MDN
   */
  final val shapeRendering = "shape-rendering".attr



  /**
   *
   *
   * MDN
   */
  final val specularConstant = "specularConstant".attr



  /**
   *
   *
   * MDN
   */
  final val specularExponent = "specularExponent".attr


  /**
   *
   *
   * MDN
   */
  final val spreadMethod = "spreadMethod".attr


  /**
   *
   *
   * MDN
   */
  final val stdDeviation = "stdDeviation".attr



  /**
   *
   *
   * MDN
   */
  final val stitchTiles = "stitchTiles".attr



  /**
   *
   *
   * MDN
   */
  final val stopColor = "stopColor".attr



  /**
   *
   *
   * MDN
   */
  final val stopOpacity = "stopOpacity".attr



  /**
   *
   *
   * MDN
   */
  final val stroke = "stroke".attr


  /**
   *
   *
   * MDN
   */
  final val strokeDasharray= "strokeDasharray".attr


  /**
   *
   *
   * MDN
   */
  final val strokeDashoffset = "stroke-dashoffset".attr


  /**
   *
   *
   * MDN
   */
  final val strokeLinecap = "strokeLinecap".attr


  /**
   *
   *
   * MDN
   */
  final val strokeLinejoin = "stroke-linejoin".attr


  /**
   *
   *
   * MDN
   */
  final val strokeMiterlimit = "stroke-miterlimit".attr


  /**
   *
   *
   * MDN
   */
  final val strokeOpacity = "strokeOpacity".attr


  /**
   *
   *
   * MDN
   */
  final val strokeWidth = "strokeWidth".attr


  /**
   *
   *
   * MDN
   */
  final val style = "style".attr



  /**
   *
   *
   * MDN
   */
  final val surfaceScale = "surfaceScale".attr


  /**
   *
   *
   * MDN
   */
  final val targetX = "targetX".attr


  /**
   *
   *
   * MDN
   */
  final val targetY = "targetY".attr


  /**
   *
   *
   * MDN
   */
  final val textAnchor = "textAnchor".attr


  /**
   *
   *
   * MDN
   */
  final val textDecoration = "text-decoration".attr


  /**
   *
   *
   * MDN
   */
  final val textRendering = "text-rendering".attr


  /**
   *
   *
   * MDN
   */
  final val to = "to".attr


  /*
   *
   *
   * MDN
   */
  final val transform = "transform".attr


  /*
   *
   *
   * MDN
   */
  final val `type`= "type".attr


  /*
   *
   *
   * MDN
   */
  final val values = "values".attr


  /**
   *
   * NOTE: This is a Style because react.js does not support it as an Attribute
   *
   * Sometimes it is of interest to let the outline of an object keep its original width no matter which transforms are
   * applied to it. For example, in a map with a 2px wide line representing roads it is of interest to keep the roads
   * 2px wide even when the user zooms into the map. To achieve this, SVG Tiny 1.2 introduces the 'vector-effect'
   * property. Future versions of the SVG language will allow for more powerful vector effects through this property
   * but this version restricts it to being able to specify the non-scaling stroke behavior
   *
   * w3.org
   *
   */
  object vectorEffect extends Style("vectorEffect", "vectorEffect") {

    /**
     * Specifies that no vector effect shall be applied, i.e. the default rendering behaviour from SVG 1.1 is used which
     * is to first fill the geometry of a shape with a specified paint, then stroke the outline with a specified paint.
     *
     * w3.org
     *
     */
    @inline final def none = this := "none"

    /**
     *
     *  Modifies the way an object is stroked. Normally stroking involves calculating stroke outline of the shape's
     *  path in current user space and filling that outline with the stroke paint (color or gradient).
     *  With the non-scaling-stroke vector effect, stroke outline shall be calculated in the "host" coordinate space
     *  instead of user coordinate space. More precisely: a user agent establishes a host coordinate space which in
     *  SVG Tiny 1.2 is always the same as "screen coordinate space". The stroke outline is calculated in the
     *  following manner: first, the shape's path is transformed into the host coordinate space.
     *  Stroke outline is calculated in the host coordinate space. The resulting outline is transformed back to the
     *  user coordinate system. (Stroke outline is always filled with stroke paint in the current user space).
     *  The resulting visual effect of this modification is that stroke width is not dependant on the transformations
     *  of the element (including non-uniform scaling and shear transformations) and zoom level.
     *
     *  w3.org
     *
     **/
    @inline final def nonScalingStroke = this := "non-scaling-stroke"

  }


  /**
   *
   *
   * MDN
   */
  final val viewBox = "viewBox".attr


  /*
   *
   *
   * MDN
   */
  final val visibility = "visibility".attr


  /*
   *
   *
   * MDN
   */
  final val width = "width".attr


  /*
   *
   *
   * MDN
   */
  final val wordSpacing = "word-spacing".attr

  /*
   *
   *
   * MDN
   */
  final val writingMode = "writing-mode".attr


  /*
   *
   *
   * MDN
   */
  final val x = "x".attr


  /*
   *
   *
   * MDN
   */
  final val x1 = "x1".attr


  /*
   *
   *
   * MDN
   */
  final val x2 = "x2".attr


  /*
   *
   *
   * MDN
   */
  final val xChannelSelector = "xChannelSelector".attr


  /**
   *
   *
   * MDN
   */
  final val xmlns = "xmlns".attr


  /*
   *
   *
   * MDN
   */
  final val y = "y".attr


  /*
   *
   *
   * MDN
   */
  final val y1 = "y1".attr


  /*
   *
   *
   * MDN
   */
  final val y2 = "y2".attr


  /*
   *
   *
   * MDN
   */
  final val yChannelSelector = "yChannelSelector".attr


  /*
   *
   *
   * MDN
   */
  final val z = "z".attr

  final val xlinkActuate = "xlinkActuate".attr
  final val xlinkArcrole = "xlinkArcrole".attr
  final val xlinkHref = "xlinkHref".attr
  final val xlinkRole = "xlinkRole".attr
  final val xlinkShow = "xlinkShow".attr
  final val xlinkTitle = "xlinkTitle".attr
  final val xlinkType = "xlinkType".attr
  final val xmlBase = "xmlBase".attr
  final val xmlLang = "xmlLang".attr
  final val xmlSpace = "xmlSpace".attr
}
