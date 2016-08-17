package japgolly.scalajs.react.vdom

import PackageBase._

object SvgAttrs extends SvgAttrs
trait SvgAttrs {

  /**
    * This attribute defines the distance from the origin to the top of accent characters,
    * measured by a distance within the font coordinate system.
    * If the attribute is not specified, the effect is as if the attribute
    * were set to the value of the ascent attribute.
    *
    * Value 	<number>
    */
  final def accentHeight = ReactAttr("accent-height")

  /**
    * This attribute controls whether or not the animation is cumulative.
    * It is frequently useful for repeated animations to build upon the previous results,
    * accumulating with each iteration. This attribute said to the animation if the value is added to
    * the previous animated attribute's value on each iteration.
    *
    * Value 	none | sum
    */
  final def accumulate = ReactAttr("accumulate")

  /**
    * This attribute controls whether or not the animation is additive.
    * It is frequently useful to define animation as an offset or delta
    * to an attribute's value, rather than as absolute values. This
    * attribute said to the animation if their values are added to the
    * original animated attribute's value.
    *
    * Value 	replace | sum
    */
  final def additive = ReactAttr("additive")

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
    */
  final def alignmentBaseline = ReactAttr("alignment-baseline")

  /**
    * This attribute defines the maximum unaccented depth of the font
    * within the font coordinate system. If the attribute is not specified,
    * the effect is as if the attribute were set to the vert-origin-y value
    * for the corresponding font.
    *
    * Value 	<number>
    */
  final def ascent = ReactAttr("ascent")

  /**
    * This attribute indicates the name of the attribute in the parent element
    * that is going to be changed during an animation.
    *
    * Value 	<attributeName>
    */
  final def attributeName = ReactAttr("attributeName")

  /**
    * This attribute specifies the namespace in which the target attribute
    * and its associated values are defined.
    *
    * Value 	CSS | XML | auto
    */
  final def attributeType = ReactAttr("attributeType")

  /**
    * The azimuth attribute represent the direction angle for the light
    * source on the XY plane (clockwise), in degrees from the x axis.
    * If the attribute is not specified, then the effect is as if a
    * value of 0 were specified.
    *
    * Value 	<number>
    */
  final def azimuth = ReactAttr("azimuth")

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
    */
  final def baseFrequency = ReactAttr("baseFrequency")

  /**
    * The baseline-shift attribute allows repositioning of the dominant-baseline
    * relative to the dominant-baseline of the parent text content element.
    * The shifted object might be a sub- or superscript.
    * As a presentation attribute, it also can be used as a property directly
    * inside a CSS stylesheet, see css baseline-shift for further information.
    *
    * Value 	auto | baseline | sup | sub | <percentage> | <length> | inherit
    */
  final def baselineShift = ReactAttr("baseline-shift")

  /**
    * This attribute defines when an animation should begin.
    * The attribute value is a semicolon separated list of values. The interpretation
    * of a list of start times is detailed in the SMIL specification in "Evaluation
    * of begin and end time lists". Each individual value can be one of the following:
    * <offset-value>, <syncbase-value>, <event-value>, <repeat-value>, <accessKey-value>,
    * <wallclock-sync-value> or the keyword indefinite.
    *
    * Value 	<begin-value-list>
    */
  final def begin = ReactAttr("begin")

  /**
    * The bias attribute shifts the range of the filter. After applying the kernelMatrix
    * of the <feconvolvematrix> element to the input image to yield a number and applied
    * the divisor attribute, the bias attribute is added to each component. This allows
    * representation of values that would otherwise be clamped to 0 or 1.
    * If bias is not specified, then the effect is as if a value of 0 were specified.
    *
    * Value 	<number>
    */
  final def bias = ReactAttr("bias")

  /**
    * This attribute specifies the interpolation mode for the animation. The default
    * mode is linear, however if the attribute does not support linear interpolation
    * (e.g. for strings), the calcMode attribute is ignored and discrete interpolation is used.
    *
    * Value 	discrete | linear | paced | spline
    */
  final def calcMode = ReactAttr("calcMode")

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
    */
  final def `class` = ReactAttr("class")

  /**
    * The clip attribute has the same parameter values as defined for the css clip property.
    * Unitless values, which indicate current user coordinates, are permitted on the coordinate
    * values on the <shape>. The value of auto defines a clipping path along the bounds of
    * the viewport created by the given element.
    * As a presentation attribute, it also can be used as a property directly inside a
    * CSS stylesheet, see css clip for further information.
    *
    * Value 	auto | <shape> | inherit
    */
  final def clip = ReactAttr("clip")

  /**
    * The clip-path attribute bind the element is applied to with a given <clippath> element
    * As a presentation attribute, it also can be used as a property directly inside a CSS stylesheet
    *
    * Value 	<FuncIRI> | none | inherit
    */
  final def clipPath = ReactAttr("clip-path")

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
    */
  final def clipPathUnits = ReactAttr("clipPathUnits")

  /**
    * The clip-rule attribute only applies to graphics elements that are contained within a
    * <clippath> element. The clip-rule attribute basically works as the fill-rule attribute,
    * except that it applies to <clippath> definitions.
    *
    * Value 	nonezero | evenodd | inherit
    */
  final def clipRule = ReactAttr("clip-rule")

  /**
    * The color attribute is used to provide a potential indirect value (currentColor)
    * for the fill, stroke, stop-color, flood-color and lighting-color attributes.
    * As a presentation attribute, it also can be used as a property directly inside a CSS
    * stylesheet, see css color for further information.
    *
    * Value 	<color> | inherit
    */
  final def color = ReactAttr("color")

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
    */
  final def colorInterpolation = ReactAttr("color-interpolation")

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
    */
  final def colorInterpolationFilters = ReactAttr("color-interpolation-filters")

  /**
    * The color-profile attribute is used to define which color profile a raster image
    * included through the <image> element should use. As a presentation attribute, it
    * also can be used as a property directly inside a CSS stylesheet, see css color-profile
    * for further information.
    *
    * Value 	auto | sRGB | <name> | <IRI> | inherit
    */
  final def colorProfile = ReactAttr("color-profile")

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
    */
  final def colorRendering = ReactAttr("color-rendering")

  /**
    * The contentScriptType attribute on the <svg> element specifies the default scripting
    * language for the given document fragment.
    * This attribute sets the default scripting language used to process the value strings
    * in event attributes. This language must be used for all instances of script that do not
    * specify their own scripting language. The value content-type specifies a media type,
    * per MIME Part Two: Media Types [RFC2046]. The default value is application/ecmascript
    *
    * Value 	<content-type>
    */
  final def contentScriptType = ReactAttr("contentScriptType")

  /**
    * This attribute specifies the style sheet language for the given document fragment.
    * The contentStyleType is specified on the <svg> element. By default, if it's not defined,
    * the value is text/css
    *
    * Value 	<content-type>
    */
  final def contentStyleType = ReactAttr("contentStyleType")

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
    */
  final def cursor = ReactAttr("cursor")

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
    */
  final def cx = ReactAttr("cx")

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
    */
  final def cy = ReactAttr("cy")

  final def d = ReactAttr("d")

  final def diffuseConstant = ReactAttr("diffuseConstant")

  final def direction = ReactAttr("direction")

  final def display = ReactAttr("display")

  final def divisor = ReactAttr("divisor")

  final def dominantBaseline = ReactAttr("dominant-baseline")

  final def dur = ReactAttr("dur")

  final def dx = ReactAttr("dx")

  final def dy = ReactAttr("dy")

  final def edgeMode = ReactAttr("edgeMode")

  final def elevation = ReactAttr("elevation")

  final def end = ReactAttr("end")

  final def externalResourcesRequired = ReactAttr("externalResourcesRequired")

  final def fill = ReactAttr("fill")

  final def fillOpacity = ReactAttr("fillOpacity")

  final def fillRule = ReactAttr("fill-rule")

  final def filter = ReactAttr("filter")

  final def filterRes = ReactAttr("filterRes")

  final def filterUnits = ReactAttr("filterUnits")

  final def floodColor = ReactAttr("flood-color")

  final def floodOpacity = ReactAttr("flood-opacity")

  final def fontFamily = ReactAttr("fontFamily")

  final def fontSize = ReactAttr("fontSize")

  final def fontSizeAdjust = ReactAttr("font-size-adjust")

  final def fontStretch = ReactAttr("font-stretch")

  final def fontVariant = ReactAttr("font-variant")

  final def fontWeight = ReactAttr("font-weight")

  final def from = ReactAttr("from")

  final def gradientTransform = ReactAttr("gradientTransform")

  final def gradientUnits = ReactAttr("gradientUnits")

  final def height = ReactAttr("height")

  final def imageRendering = ReactAttr("imageRendering")

  final def id = ReactAttr("id")

  final def in = ReactAttr("in")

  final def in2 = ReactAttr("in2")

  final def k1 = ReactAttr("k1")

  final def k2 = ReactAttr("k2")

  final def k3 = ReactAttr("k3")

  final def k4 = ReactAttr("k4")

  final def kernelMatrix = ReactAttr("kernelMatrix")

  final def kernelUnitLength = ReactAttr("kernelUnitLength")

  final def kerning = ReactAttr("kerning")

  final def keySplines = ReactAttr("keySplines")

  final def keyTimes = ReactAttr("keyTimes")

  final def letterSpacing = ReactAttr("letter-spacing")

  final def lightingColor = ReactAttr("lighting-color")

  final def limitingConeAngle = ReactAttr("limitingConeAngle")

  final def local = ReactAttr("local")

  final def markerEnd = ReactAttr("markerEnd")

  final def markerMid = ReactAttr("markerMid")

  final def markerStart = ReactAttr("markerStart")

  final def markerHeight = ReactAttr("markerHeight")

  final def markerUnits = ReactAttr("markerUnits")

  final def markerWidth = ReactAttr("markerWidth")

  final def maskContentUnits = ReactAttr("maskContentUnits")

  final def maskUnits = ReactAttr("maskUnits")

  final def mask = ReactAttr("mask")

  final def max = ReactAttr("max")

  final def min = ReactAttr("min")

  final def mode = ReactAttr("mode")

  final def numOctaves = ReactAttr("numOctaves")

  final def offset = ReactAttr("offset")

  final def opacity = ReactAttr("opacity")

  final def operator = ReactAttr("operator")

  final def order = ReactAttr("order")

  final def overflow = ReactAttr("overflow")

  final def paintOrder = ReactAttr("paint-order")

  final def pathLength = ReactAttr("pathLength")

  final def patternContentUnits = ReactAttr("patternContentUnits")

  final def patternTransform = ReactAttr("patternTransform")

  final def patternUnits = ReactAttr("patternUnits")

  final def pointerEvents = ReactAttr("pointer-events")

  final def points = ReactAttr("points")

  final def pointsAtX = ReactAttr("pointsAtX")

  final def pointsAtY = ReactAttr("pointsAtY")

  final def pointsAtZ = ReactAttr("pointsAtZ")

  final def preserveAlpha = ReactAttr("preserveAlpha")

  final def preserveAspectRatio = ReactAttr("preserveAspectRatio")

  final def primitiveUnits = ReactAttr("primitiveUnits")

  final def r = ReactAttr("r")

  final def radius = ReactAttr("radius")

  final def repeatCount = ReactAttr("repeatCount")

  final def repeatDur = ReactAttr("repeatDur")

  final def requiredFeatures = ReactAttr("requiredFeatures")

  final def restart = ReactAttr("restart")

  final def result = ReactAttr("result")

  final def rx = ReactAttr("rx")

  final def ry = ReactAttr("ry")

  final def scale = ReactAttr("scale")

  final def seed = ReactAttr("seed")

  final def shapeRendering = ReactAttr("shape-rendering")

  final def specularConstant = ReactAttr("specularConstant")

  final def specularExponent = ReactAttr("specularExponent")

  final def spreadMethod = ReactAttr("spreadMethod")

  final def stdDeviation = ReactAttr("stdDeviation")

  final def stitchTiles = ReactAttr("stitchTiles")

  final def stopColor = ReactAttr("stopColor")

  final def stopOpacity = ReactAttr("stopOpacity")

  final def stroke = ReactAttr("stroke")

  final def strokeDasharray = ReactAttr("strokeDasharray")

  final def strokeDashoffset = ReactAttr("stroke-dashoffset")

  final def strokeLinecap = ReactAttr("strokeLinecap")

  final def strokeLinejoin = ReactAttr("stroke-linejoin")

  final def strokeMiterlimit = ReactAttr("stroke-miterlimit")

  final def strokeOpacity = ReactAttr("strokeOpacity")

  final def strokeWidth = ReactAttr("strokeWidth")

  final def style = ReactAttr("style")

  final def surfaceScale = ReactAttr("surfaceScale")

  final def targetX = ReactAttr("targetX")

  final def targetY = ReactAttr("targetY")

  final def textAnchor = ReactAttr("textAnchor")

  final def textDecoration = ReactAttr("text-decoration")

  final def textRendering = ReactAttr("text-rendering")

  final def to = ReactAttr("to")

  final def transform = ReactAttr("transform")

  final def `type` = ReactAttr("type")

  final def values = ReactAttr("values")

  final def viewBox = ReactAttr("viewBox")

  final def visibility = ReactAttr("visibility")

  final def width = ReactAttr("width")

  final def wordSpacing = ReactAttr("word-spacing")

  final def writingMode = ReactAttr("writing-mode")

  final def x = ReactAttr("x")

  final def x1 = ReactAttr("x1")

  final def x2 = ReactAttr("x2")

  final def xChannelSelector = ReactAttr("xChannelSelector")

  final def xmlns = ReactAttr("xmlns")

  final def y = ReactAttr("y")

  final def y1 = ReactAttr("y1")

  final def y2 = ReactAttr("y2")

  final def yChannelSelector = ReactAttr("yChannelSelector")

  final def z = ReactAttr("z")

  final def xlinkActuate = ReactAttr("xlinkActuate")

  final def xlinkArcrole = ReactAttr("xlinkArcrole")

  final def xlinkHref = ReactAttr("xlinkHref")

  final def xlinkRole = ReactAttr("xlinkRole")

  final def xlinkShow = ReactAttr("xlinkShow")

  final def xlinkTitle = ReactAttr("xlinkTitle")

  final def xlinkType = ReactAttr("xlinkType")

  final def xmlBase = ReactAttr("xmlBase")

  final def xmlLang = ReactAttr("xmlLang")

  final def xmlSpace = ReactAttr("xmlSpace")
}
