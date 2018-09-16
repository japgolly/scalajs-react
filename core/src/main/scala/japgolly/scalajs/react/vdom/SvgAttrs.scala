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
  final def accentHeight = VdomAttr("accentHeight")

  /**
    * This attribute controls whether or not the animation is cumulative.
    * It is frequently useful for repeated animations to build upon the previous results,
    * accumulating with each iteration. This attribute said to the animation if the value is added to
    * the previous animated attribute's value on each iteration.
    *
    * Value 	none | sum
    */
  final def accumulate = VdomAttr("accumulate")

  /**
    * This attribute controls whether or not the animation is additive.
    * It is frequently useful to define animation as an offset or delta
    * to an attribute's value, rather than as absolute values. This
    * attribute said to the animation if their values are added to the
    * original animated attribute's value.
    *
    * Value 	replace | sum
    */
  final def additive = VdomAttr("additive")

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
  final def alignmentBaseline = VdomAttr("alignmentBaseline")

  /**
    * This attribute defines the maximum unaccented depth of the font
    * within the font coordinate system. If the attribute is not specified,
    * the effect is as if the attribute were set to the vert-origin-y value
    * for the corresponding font.
    *
    * Value 	<number>
    */
  final def ascent = VdomAttr("ascent")

  /**
    * This attribute indicates the name of the attribute in the parent element
    * that is going to be changed during an animation.
    *
    * Value 	<attributeName>
    */
  final def attributeName = VdomAttr("attributeName")

  /**
    * This attribute specifies the namespace in which the target attribute
    * and its associated values are defined.
    *
    * Value 	CSS | XML | auto
    */
  final def attributeType = VdomAttr("attributeType")

  /**
    * The azimuth attribute represent the direction angle for the light
    * source on the XY plane (clockwise), in degrees from the x axis.
    * If the attribute is not specified, then the effect is as if a
    * value of 0 were specified.
    *
    * Value 	<number>
    */
  final def azimuth = VdomAttr("azimuth")

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
  final def baseFrequency = VdomAttr("baseFrequency")

  /**
    * The baseline-shift attribute allows repositioning of the dominant-baseline
    * relative to the dominant-baseline of the parent text content element.
    * The shifted object might be a sub- or superscript.
    * As a presentation attribute, it also can be used as a property directly
    * inside a CSS stylesheet, see css baseline-shift for further information.
    *
    * Value 	auto | baseline | sup | sub | <percentage> | <length> | inherit
    */
  final def baselineShift = VdomAttr("baselineShift")

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
  final def begin = VdomAttr("begin")

  /**
    * The bias attribute shifts the range of the filter. After applying the kernelMatrix
    * of the <feconvolvematrix> element to the input image to yield a number and applied
    * the divisor attribute, the bias attribute is added to each component. This allows
    * representation of values that would otherwise be clamped to 0 or 1.
    * If bias is not specified, then the effect is as if a value of 0 were specified.
    *
    * Value 	<number>
    */
  final def bias = VdomAttr("bias")

  /**
    * This attribute specifies the interpolation mode for the animation. The default
    * mode is linear, however if the attribute does not support linear interpolation
    * (e.g. for strings), the calcMode attribute is ignored and discrete interpolation is used.
    *
    * Value 	discrete | linear | paced | spline
    */
  final def calcMode = VdomAttr("calcMode")

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
  final def `class` = VdomAttr("class")

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
  final def clip = VdomAttr("clip")

  /**
    * The clip-path attribute bind the element is applied to with a given <clippath> element
    * As a presentation attribute, it also can be used as a property directly inside a CSS stylesheet
    *
    * Value 	<FuncIRI> | none | inherit
    */
  final def clipPath = VdomAttr("clipPath")

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
  final def clipPathUnits = VdomAttr("clipPathUnits")

  /**
    * The clip-rule attribute only applies to graphics elements that are contained within a
    * <clippath> element. The clip-rule attribute basically works as the fill-rule attribute,
    * except that it applies to <clippath> definitions.
    *
    * Value 	nonezero | evenodd | inherit
    */
  final def clipRule = VdomAttr("clipRule")

  /**
    * The color attribute is used to provide a potential indirect value (currentColor)
    * for the fill, stroke, stop-color, flood-color and lighting-color attributes.
    * As a presentation attribute, it also can be used as a property directly inside a CSS
    * stylesheet, see css color for further information.
    *
    * Value 	<color> | inherit
    */
  final def color = VdomAttr("color")

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
  final def colorInterpolation = VdomAttr("colorInterpolation")

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
  final def colorInterpolationFilters = VdomAttr("colorInterpolationFilters")

  /**
    * The color-profile attribute is used to define which color profile a raster image
    * included through the <image> element should use. As a presentation attribute, it
    * also can be used as a property directly inside a CSS stylesheet, see css color-profile
    * for further information.
    *
    * Value 	auto | sRGB | <name> | <IRI> | inherit
    */
  final def colorProfile = VdomAttr("colorProfile")

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
  final def colorRendering = VdomAttr("colorRendering")

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
  final def contentScriptType = VdomAttr("contentScriptType")

  /**
    * This attribute specifies the style sheet language for the given document fragment.
    * The contentStyleType is specified on the <svg> element. By default, if it's not defined,
    * the value is text/css
    *
    * Value 	<content-type>
    */
  final def contentStyleType = VdomAttr("contentStyleType")

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
  final def cursor = VdomAttr("cursor")

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
  final def cx = VdomAttr("cx")

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
  final def cy = VdomAttr("cy")

  final def d = VdomAttr("d")

  final def diffuseConstant = VdomAttr("diffuseConstant")

  final def direction = VdomAttr("direction")

  final def display = VdomAttr("display")

  final def divisor = VdomAttr("divisor")

  final def dominantBaseline = VdomAttr("dominantBaseline")

  final def dur = VdomAttr("dur")

  final def dx = VdomAttr("dx")

  final def dy = VdomAttr("dy")

  final def edgeMode = VdomAttr("edgeMode")

  final def elevation = VdomAttr("elevation")

  final def end = VdomAttr("end")

  final def externalResourcesRequired = VdomAttr("externalResourcesRequired")

  final def fill = VdomAttr("fill")

  final def fillOpacity = VdomAttr("fillOpacity")

  final def fillRule = VdomAttr("fillRule")

  final def filter = VdomAttr("filter")

  final def filterRes = VdomAttr("filterRes")

  final def filterUnits = VdomAttr("filterUnits")

  final def floodColor = VdomAttr("floodColor")

  final def floodOpacity = VdomAttr("floodOpacity")

  final def focusable = VdomAttr("focusable")

  final def fontFamily = VdomAttr("fontFamily")

  final def fontSize = VdomAttr("fontSize")

  final def fontSizeAdjust = VdomAttr("fontSizeAdjust")

  final def fontStretch = VdomAttr("fontStretch")

  final def fontVariant = VdomAttr("fontVariant")

  final def fontWeight = VdomAttr("fontWeight")

  final def from = VdomAttr("from")

  final def gradientTransform = VdomAttr("gradientTransform")

  final def gradientUnits = VdomAttr("gradientUnits")

  final def height = VdomAttr("height")

  final def imageRendering = VdomAttr("imageRendering")

  final def id = VdomAttr("id")

  final def in = VdomAttr("in")

  final def in2 = VdomAttr("in2")

  final def k1 = VdomAttr("k1")

  final def k2 = VdomAttr("k2")

  final def k3 = VdomAttr("k3")

  final def k4 = VdomAttr("k4")

  final def kernelMatrix = VdomAttr("kernelMatrix")

  final def kernelUnitLength = VdomAttr("kernelUnitLength")

  final def kerning = VdomAttr("kerning")

  final def keySplines = VdomAttr("keySplines")

  final def keyTimes = VdomAttr("keyTimes")

  final def letterSpacing = VdomAttr("letterSpacing")

  final def lightingColor = VdomAttr("lightingColor")

  final def limitingConeAngle = VdomAttr("limitingConeAngle")

  final def local = VdomAttr("local")

  final def markerEnd = VdomAttr("markerEnd")

  final def markerMid = VdomAttr("markerMid")

  final def markerStart = VdomAttr("markerStart")

  final def markerHeight = VdomAttr("markerHeight")

  final def markerUnits = VdomAttr("markerUnits")

  final def markerWidth = VdomAttr("markerWidth")

  final def maskContentUnits = VdomAttr("maskContentUnits")

  final def maskUnits = VdomAttr("maskUnits")

  final def mask = VdomAttr("mask")

  final def max = VdomAttr("max")

  final def min = VdomAttr("min")

  final def mode = VdomAttr("mode")

  final def numOctaves = VdomAttr("numOctaves")

  final def offset = VdomAttr("offset")

  final def opacity = VdomAttr("opacity")

  final def operator = VdomAttr("operator")

  final def orient = VdomAttr("orient")

  final def order = VdomAttr("order")

  final def overflow = VdomAttr("overflow")

  final def paintOrder = VdomAttr("paintOrder")

  final def pathAttr = VdomAttr("path")

  final def pathLength = VdomAttr("pathLength")

  final def patternContentUnits = VdomAttr("patternContentUnits")

  final def patternTransform = VdomAttr("patternTransform")

  final def patternUnits = VdomAttr("patternUnits")

  final def pointerEvents = VdomAttr("pointerEvents")

  final def points = VdomAttr("points")

  final def pointsAtX = VdomAttr("pointsAtX")

  final def pointsAtY = VdomAttr("pointsAtY")

  final def pointsAtZ = VdomAttr("pointsAtZ")

  final def preserveAlpha = VdomAttr("preserveAlpha")

  final def preserveAspectRatio = VdomAttr("preserveAspectRatio")

  final def primitiveUnits = VdomAttr("primitiveUnits")

  final def r = VdomAttr("r")

  final def radius = VdomAttr("radius")

  final def refX = VdomAttr("refX")

  final def refY = VdomAttr("refY")

  final def repeatCount = VdomAttr("repeatCount")

  final def repeatDur = VdomAttr("repeatDur")

  final def requiredFeatures = VdomAttr("requiredFeatures")

  final def restart = VdomAttr("restart")

  final def result = VdomAttr("result")

  final def rx = VdomAttr("rx")

  final def ry = VdomAttr("ry")

  final def scale = VdomAttr("scale")

  final def seed = VdomAttr("seed")

  final def shapeRendering = VdomAttr("shapeRendering")

  final def specularConstant = VdomAttr("specularConstant")

  final def specularExponent = VdomAttr("specularExponent")

  final def spreadMethod = VdomAttr("spreadMethod")

  final def stdDeviation = VdomAttr("stdDeviation")

  final def stitchTiles = VdomAttr("stitchTiles")

  final def stopColor = VdomAttr("stopColor")

  final def stopOpacity = VdomAttr("stopOpacity")

  final def stroke = VdomAttr("stroke")

  final def strokeDasharray = VdomAttr("strokeDasharray")

  final def strokeDashoffset = VdomAttr("strokeDashoffset")

  final def strokeLinecap = VdomAttr("strokeLinecap")

  final def strokeLinejoin = VdomAttr("strokeLinejoin")

  final def strokeMiterlimit = VdomAttr("strokeMiterlimit")

  final def strokeOpacity = VdomAttr("strokeOpacity")

  final def strokeWidth = VdomAttr("strokeWidth")

  final def style = VdomAttr("style")

  final def surfaceScale = VdomAttr("surfaceScale")

  final def targetX = VdomAttr("targetX")

  final def targetY = VdomAttr("targetY")

  final def textAnchor = VdomAttr("textAnchor")

  final def textDecoration = VdomAttr("textDecoration")

  final def textRendering = VdomAttr("textRendering")

  final def to = VdomAttr("to")

  final def transform = VdomAttr("transform")

  final def `type` = VdomAttr("type")

  final def untypedRef = VdomAttr.Ref

  final def values = VdomAttr("values")

  final def viewBox = VdomAttr("viewBox")

  final def visibility = VdomAttr("visibility")

  final def width = VdomAttr("width")

  final def wordSpacing = VdomAttr("wordSpacing")

  final def writingMode = VdomAttr("writingMode")

  final def x = VdomAttr("x")

  final def x1 = VdomAttr("x1")

  final def x2 = VdomAttr("x2")

  final def xChannelSelector = VdomAttr("xChannelSelector")

  final def xmlns = VdomAttr("xmlns")

  final def y = VdomAttr("y")

  final def y1 = VdomAttr("y1")

  final def y2 = VdomAttr("y2")

  final def yChannelSelector = VdomAttr("yChannelSelector")

  final def z = VdomAttr("z")

  final def xlinkActuate = VdomAttr("xlinkActuate")

  final def xlinkArcrole = VdomAttr("xlinkArcrole")

  final def xlinkHref = VdomAttr("xlinkHref")

  final def xlinkRole = VdomAttr("xlinkRole")

  final def xlinkShow = VdomAttr("xlinkShow")

  final def xlinkTitle = VdomAttr("xlinkTitle")

  final def xlinkType = VdomAttr("xlinkType")

  final def xmlBase = VdomAttr("xmlBase")

  final def xmlLang = VdomAttr("xmlLang")

  final def xmlSpace = VdomAttr("xmlSpace")
}
