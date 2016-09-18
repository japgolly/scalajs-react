package japgolly.scalajs.react.vdom

import org.scalajs.dom.{svg => *}
import Scalatags._
import NamespaceSvg._

object SvgTags extends SvgTags

trait SvgTags {
  /**
   * The altGlyph element allows sophisticated selection of the glyphs used to
   * render its child character data.
   *
   * MDN
   */
  final lazy val altGlyph = "altGlyph".tag[*.Element]
  /**
   * The altGlyphDef element defines a substitution representation for glyphs.
   *
   * MDN
   */
  final lazy val altGlyphDef = "altGlyphDef".tag[*.Element]

  /**
   * The altGlyphItem element provides a set of candidates for glyph substitution
   * by the altglyph element.
   *
   * MDN
   */
  final lazy val altGlyphItem = "altGlyphItem".tag[*.Element]
  /**
   * The animate element is put inside a shape element and defines how an
   * attribute of an element changes over the animation
   *
   * MDN
   */
  final lazy val animate = "animate".tag[*.Element]
  /**
   * The animateMotion element causes a referenced element to move along a
   * motion path.
   *
   * MDN
   */
  final lazy val animateMotion = "animateMotion".tag[*.Element]
  /**
   * The animateTransform element animates a transformation attribute on a target
   * element, thereby allowing animations to control translation, scaling,
   * rotation and/or skewing.
   *
   * MDN
   */
  final lazy val animateTransform = "animateTransform".tag[*.Element]
  /**
   * The circle element is an SVG basic shape, used to create circles based on a
   * center point and a radius.
   *
   * MDN
   */
  final lazy val circle = "circle".tag[*.Circle]
  /**
   * The clipping path restricts the region to which paint can be applied.
   * Conceptually, any parts of the drawing that lie outside of the region
   * bounded by the currently active clipping path are not drawn.
   *
   * MDN
   */
  final lazy val clipPathTag = "clipPath".tag[*.ClipPath]
  /**
   * The element allows describing the color profile used for the image.
   *
   * MDN
   */
  final lazy val `color-profile` = "color-profile".tag[*.Element]
  /**
   * The cursor element can be used to define a platform-independent custom
   * cursor. A recommended approach for defining a platform-independent custom
   * cursor is to create a PNG image and define a cursor element that references
   * the PNG image and identifies the exact position within the image which is
   * the pointer position (i.e., the hot spot).
   *
   * MDN
   */
  final lazy val cursorTag = "cursor".tag[*.Element]
  /**
   * SVG allows graphical objects to be defined for later reuse. It is
   * recommended that, wherever possible, referenced elements be defined inside
   * of a defs element. Defining these elements inside of a defs element
   * promotes understandability of the SVG content and thus promotes
   * accessibility. Graphical elements defined in a defs will not be directly
   * rendered. You can use a use element to render those elements wherever you
   * want on the viewport.
   *
   * MDN
   */
  final lazy val defs = "defs".tag[*.Defs]
  /**
   * Each container element or graphics element in an SVG drawing can supply a
   * desc description string where the description is text-only. When the
   * current SVG document fragment is rendered as SVG on visual media, desc
   * elements are not rendered as part of the graphics. Alternate presentations
   * are possible, both visual and aural, which display the desc element but do
   * not display path elements or other graphics elements. The desc element
   * generally improve accessibility of SVG documents
   *
   * MDN
   */
  final lazy val desc = "desc".tag[*.Desc]

  // TODO: Add discard tag (not available in scalajs-dom)

  /**
   * The ellipse element is an SVG basic shape, used to create ellipses based
   * on a center coordinate, and both their x and y radius.
   *
   * Ellipses are unable to specify the exact orientation of the ellipse (if,
   * for example, you wanted to draw an ellipse titled at a 45 degree angle),
   * but can be rotated by using the transform attribute.
   *
   * MDN
   */
  final lazy val ellipse = "ellipse".tag[*.Ellipse]
  /**
   * The feBlend filter composes two objects together ruled by a certain blending
   * mode. This is similar to what is known from image editing software when
   * blending two layers. The mode is defined by the mode attribute.
   *
   * MDN
   */
  final lazy val feBlend = "feBlend".tag[*.FEBlend]
  /**
   * This filter changes colors based on a transformation matrix. Every pixel's
   * color value (represented by an [R,G,B,A] vector) is matrix multiplied to
   * create a new color.
   *
   * MDN
   */
  final lazy val feColorMatrix = "feColorMatrix".tag[*.FEColorMatrix]
  /**
   * The color of each pixel is modified by changing each channel (R, G, B, and
   * A) to the result of what the children fefuncr, fefuncb, fefuncg,
   * and fefunca return.
   *
   * MDN
   */
  final lazy val feComponentTransfer = "feComponentTransfer".tag[*.ComponentTransferFunction]
  /**
   * This filter primitive performs the combination of two input images pixel-wise
   * in image space using one of the Porter-Duff compositing operations: over,
   * in, atop, out, xor. Additionally, a component-wise arithmetic operation
   * (with the result clamped between [0..1]) can be applied.
   *
   * MDN
   */
  final lazy val feComposite = "feComposite".tag[*.FEComposite]
  /**
   * the feConvolveMatrix element applies a matrix convolution filter effect.
   * A convolution combines pixels in the input image with neighboring pixels
   * to produce a resulting image. A wide variety of imaging operations can be
   * achieved through convolutions, including blurring, edge detection,
   * sharpening, embossing and beveling.
   *
   * MDN
   */
  final lazy val feConvolveMatrix = "feConvolveMatrix".tag[*.FEConvolveMatrix]
  /**
   * This filter primitive lights an image using the alpha channel as a bump map.
   * The resulting image, which is an RGBA opaque image, depends on the light
   * color, light position and surface geometry of the input bump map.
   *
   * MDN
   */
  final lazy val feDiffuseLighting = "feDiffuseLighting".tag[*.FEDiffuseLighting]
  /**
   * This filter primitive uses the pixels values from the image from in2 to
   * spatially displace the image from in.
   *
   * MDN
   */
  final lazy val feDisplacementMap = "feDisplacementMap".tag[*.FEDisplacementMap]
  /**
   * This filter primitive define a distant light source that can be used
   * within a lighting filter primitive: fediffuselighting or
   * fespecularlighting.
   *
   * MDN
   */
  final lazy val feDistantLight = "feDistantLight".tag[*.FEDistantLight]
  /**
   * The filter fills the filter subregion with the color and opacity defined by
   * flood-color and flood-opacity.
   *
   * MDN
   */
  final lazy val feFlood = "feFlood".tag[*.FEFlood]
  /**
   * This filter primitive defines the transfer function for the alpha component
   * of the input graphic of its parent fecomponenttransfer element.
   *
   * MDN
   */
  final lazy val feFuncA = "feFuncA".tag[*.FEFuncA]
  /**
   * This filter primitive defines the transfer function for the blue component
   * of the input graphic of its parent fecomponenttransfer element.
   *
   * MDN
   */
  final lazy val feFuncB = "feFuncB".tag[*.FEFuncB]
  /**
   * This filter primitive defines the transfer function for the green component
   * of the input graphic of its parent fecomponenttransfer element.
   *
   * MDN
   */
  final lazy val feFuncG = "feFuncG".tag[*.FEFuncG]
  /**
   * This filter primitive defines the transfer function for the red component
   * of the input graphic of its parent fecomponenttransfer element.
   *
   * MDN
   */
  final lazy val feFuncR = "feFuncR".tag[*.FEFuncR]
  /**
   * The filter blurs the input image by the amount specified in stdDeviation,
   * which defines the bell-curve.
   *
   * MDN
   */
  final lazy val feFaussianBlur = "feFaussianBlur".tag[*.FEGaussianBlur]

  /**
   * The feImage filter fetches image data from an external source and provides
   * the pixel data as output (meaning, if the external source is an SVG image,
   * it is rasterize).
   *
   * MDN
   */
  final lazy val feImage = "feImage".tag[*.FEImage]

  /**
   * The feMerge filter allows filter effects to be applied concurrently
   * instead of sequentially. This is achieved by other filters storing their
   * output via the result attribute and then accessing it in a femergenode
   * child.
   *
   * MDN
   */
  final lazy val feMerge = "feMerge".tag[*.FEMerge]

  /**
   * The feMergeNode takes the result of another filter to be processed by its
   * parent femerge.
   *
   * MDN
   */
  final lazy val feMergeNode = "feMergeNode".tag[*.FEMergeNode]
  /**
   * This filter is used to erode or dilate the input image. It's usefulness
   * lies especially in fattening or thinning effects.
   *
   * MDN
   */
  final lazy val feMorphology = "feMorphology".tag[*.FEMorphology]
  /**
   * The input image as a whole is offset by the values specified in the dx
   * and dy attributes. It's used in creating drop-shadows.
   *
   * MDN
   */
  final lazy val feOffset = "feOffset".tag[*.FEOffset]
  final lazy val fePointLight = "fePointLight".tag[*.FEPointLight]
  /**
   * This filter primitive lights a source graphic using the alpha channel as a
   * bump map. The resulting image is an RGBA image based on the light color.
   * The lighting calculation follows the standard specular component of the
   * Phong lighting model. The resulting image depends on the light color, light
   * position and surface geometry of the input bump map. The result of the
   * lighting calculation is added. The filter primitive assumes that the viewer
   * is at infinity in the z direction.
   *
   * MDN
   */
  final lazy val feSpecularLighting = "feSpecularLighting".tag[*.FESpecularLighting]
  /**
   *
   */
  final lazy val feSpotLight = "feSpotLight".tag[*.FESpotLight]
  /**
   * An input image is tiled and the result used to fill a target. The effect
   * is similar to the one of a pattern.
   *
   * MDN
   */
  final lazy val feTile = "feTile".tag[*.FETile]
  /**
   * This filter primitive creates an image using the Perlin turbulence
   * function. It allows the synthesis of artificial textures like clouds or
   * marble.
   *
   * MDN
   */
  final lazy val feTurbulance = "feTurbulance".tag[*.FETurbulence]
  /**
   * The filter element serves as container for atomic filter operations. It is
   * never rendered directly. A filter is referenced by using the filter
   * attribute on the target SVG element.
   *
   * MDN
   */
  final lazy val filterTag = "filter".tag[*.Filter]
  /**
   * The font element defines a font to be used for text layout.
   *
   * MDN
   */
  final lazy val font = "font".tag[*.Element]
  /**
   * The font-face element corresponds to the CSS @font-face declaration. It
   * defines a font's outer properties.
   *
   * MDN
   */
  final lazy val `font-face` = "font-face".tag[*.Element]
  /**
   * The font-face-format element describes the type of font referenced by its
   * parent font-face-uri.
   *
   * MDN
   */
  final lazy val `font-face-format` = "font-face-format".tag[*.Element]
  /**
   * The font-face-name element points to a locally installed copy of this font,
   * identified by its name.
   *
   * MDN
   */
  final lazy val `font-face-name` = "font-face-name".tag[*.Element]
  /**
   * The font-face-src element corresponds to the src property in CSS @font-face
   * descriptions. It serves as container for font-face-name, pointing to
   * locally installed copies of this font, and font-face-uri, utilizing
   * remotely defined fonts.
   *
   * MDN
   */
  final lazy val `font-face-src` = "font-face-src".tag[*.Element]
  /**
   * The font-face-uri element points to a remote definition of the current font.
   *
   * MDN
   */
  final lazy val `font-face-uri` = "font-face-uri".tag[*.Element]
  /**
   * The foreignObject element allows for inclusion of a foreign XML namespace
   * which has its graphical content drawn by a different user agent. The
   * included foreign graphical content is subject to SVG transformations and
   * compositing.
   *
   * MDN
   */
  final lazy val foreignObject = "foreignObject".tag[*.Element]
  /**
   * The g element is a container used to group objects. Transformations applied
   * to the g element are performed on all of its child elements. Attributes
   * applied are inherited by child elements. In addition, it can be used to
   * define complex objects that can later be referenced with the use element.
   *
   * MDN
   */
  final lazy val g = "g".tag[*.G]
  /**
   * A glyph defines a single glyph in an SVG font.
   *
   * MDN
   */
  final lazy val glyph = "glyph".tag[*.Element]
  /**
   * The glyphRef element provides a single possible glyph to the referencing
   * altglyph substitution.
   *
   * MDN
   */
  final lazy val glyphRef = "glyphRef".tag[*.Element]
  /**
   * The horizontal distance between two glyphs can be fine-tweaked with an
   * hkern Element. This process is known as Kerning.
   *
   * MDN
   */
  final lazy val hkern = "hkern".tag[*.Element]
  /**
   * The SVG Image Element (image) allows a raster image into be included in
   * an SVG document.
   *
   * MDN
   */
  final lazy val image = "image".tag[*.Image]
  /**
   * The line element is an SVG basic shape, used to create a line connecting
   * two points.
   *
   * MDN
   */
  final lazy val line = "line".tag[*.Line]
  /**
   * linearGradient lets authors define linear gradients to fill or stroke
   * graphical elements.
   *
   * MDN
   */
  final lazy val linearGradient = "linearGradient".tag[*.LinearGradient]
  /**
   * The marker element defines the graphics that is to be used for drawing
   * arrowheads or polymarkers on a given path, line, polyline or
   * polygon element.
   *
   * MDN
   */
  final lazy val marker = "marker".tag[*.Marker]
  /**
   * In SVG, you can specify that any other graphics object or g element can
   * be used as an alpha mask for compositing the current object into the
   * background. A mask is defined with the mask element. A mask is
   * used/referenced using the mask property.
   *
   * MDN
   */
  final lazy val maskTag = "mask".tag[*.Mask]
  /**
   * Metadata is structured data about data. Metadata which is included with SVG
   * content should be specified within metadata elements. The contents of the
   * metadata should be elements from other XML namespaces such as RDF, FOAF,
   * etc.
   *
   * MDN
   */
  final lazy val metadata = "metadata".tag[*.Metadata]
  /**
   * The missing-glyph's content is rendered, if for a given character the font
   * doesn't define an appropriate glyph.
   *
   * MDN
   */
  final lazy val `missing-glyph` = "missing-glyph".tag[*.Element]
  /**
   * the mpath sub-element for the animatemotion element provides the ability
   * to reference an external path element as the definition of a motion path.
   *
   * MDN
   */
  final lazy val mpath = "mpath".tag[*.Element]
  /**
   * The path element is the generic element to define a shape. All the basic
   * shapes can be created with a path element.
   */
  final lazy val path = "path".tag[*.Path]
  /**
   * A pattern is used to fill or stroke an object using a pre-defined graphic
   * object which can be replicated ("tiled") at fixed intervals in x and y to
   * cover the areas to be painted. Patterns are defined using the pattern
   * element and then referenced by properties fill and stroke on a given
   * graphics element to indicate that the given element shall be filled or
   * stroked with the referenced pattern.
   *
   * MDN
   */
  final lazy val pattern = "pattern".tag[*.Pattern]
  /**
   * The polygon element defines a closed shape consisting of a set of connected
   * straight line segments.
   *
   * MDN
   */
  final lazy val polygon = "polygon".tag[*.Polygon]
  /**
   * The polyline element is an SVG basic shape, used to create a series of
   * straight lines connecting several points. Typically a polyline is used to
   * create open shapes
   *
   * MDN
   */
  final lazy val polyline = "polyline".tag[*.Polyline]
  /**
   * radialGradient lets authors define radial gradients to fill or stroke
   * graphical elements.
   *
   * MDN
   */
  final lazy val radialGradient = "radialGradient".tag[*.RadialGradient]
  /**
   * The rect element is an SVG basic shape, used to create rectangles based on
   * the position of a corner and their width and height. It may also be used to
   * create rectangles with rounded corners.
   *
   * MDN
   */
  final lazy val rect = "rect".tag[*.RectElement]
  /**
   * A SVG script element is equivalent to the script element in HTML and thus is
   * the place for scripts (e.g., ECMAScript).
   *
   * Any functions defined within any script element have a global scope across* the
   * entire current document.
   *
   * MDN
   */
  final lazy val script = "script".tag[*.Script]
  /**
   * The set element provides a simple means of just setting the value of an
   * attribute for a specified duration. It supports all attribute types,
   * including those that cannot reasonably be interpolated, such as string and
   * boolean values. The set element is non-additive. The additive and
   * accumulate attributes are not allowed, and will be ignored if specified.
   *
   * MDN
   */
  final lazy val set = "set".tag[*.Element]
  /**
   * The ramp of colors to use on a gradient is defined by the stop elements
   * that are child elements to either the lineargradient element or the
   * radialgradient element.
   *
   * MDN
   */
  final lazy val stop = "stop".tag[*.Stop]
  /**
   * When it is not the root element, the svg element can be used to nest a
   * standalone SVG fragment inside the current document (which can be an HTML
   * document). This standalone fragment has its own viewPort and its own
   * coordinate system.
   *
   * MDN
   */
  final lazy val svg = "svg".tag[*.SVG]
  /**
   * The switch element evaluates the requiredFeatures, requiredExtensions and
   * systemLanguage attributes on its direct child elements in order, and then
   * processes and renders the first child for which these attributes evaluate
   * to true. All others will be bypassed and therefore not rendered. If the
   * child element is a container element such as a g, then the entire
   * subtree is either processed/rendered or bypassed/not rendered.
   *
   * MDN
   */
  final lazy val switch = "switch".tag[*.Switch]
  /**
   * The symbol element is used to define graphical template objects which can
   * be instantiated by a use element. The use of symbol elements for
   * graphics that are used multiple times in the same document adds structure
   * and semantics. Documents that are rich in structure may be rendered
   * graphically, as speech, or as braille, and thus promote accessibility.
   * note that a symbol element itself is not rendered. Only instances of a
   * symbol element (i.e., a reference to a symbol by a use element) are
   * rendered.
   *
   * MDN
   */
  final lazy val symbol = "symbol".tag[*.Symbol]
  /**
   * The text element defines a graphics element consisting of text. Note that
   * it is possible to apply a gradient, pattern, clipping path, mask or filter
   * to text.
   *
   * MDN
   */
  final lazy val text = "text".tag[*.Text]
  /**
   * In addition to text drawn in a straight line, SVG also includes the
   * ability to place text along the shape of a path element. To specify that
   * a block of text is to be rendered along the shape of a path, include
   * the given text within a textPath element which includes an xlink:href
   * attribute with a reference to a path element.
   *
   * MDN
   */
  final lazy val textPath = "textPath".tag[*.TextPath]
  /**
   * Each container element or graphics element in an SVG drawing can supply
   * a title description string where the description is text-only. When the
   * current SVG document fragment is rendered as SVG on visual media, title
   * element is not rendered as part of the graphics. However, some user agents
   * may, for example, display the title element as a tooltip. Alternate
   * presentations are possible, both visual and aural, which display the title
   * element but do not display path elements or other graphics elements. The
   * title element generally improve accessibility of SVG documents
   *
   * Generally title element should be the first child element of its parent.
   * Note that those implementations that do use title to display a tooltip often
   * will only do so if the title is indeed the first child element of its parent.
   *
   * MDN
   */
  final lazy val title = "title".tag[*.Title]
  /**
   * The textual content for a text can be either character data directly
   * embedded within the text element or the character data content of a
   * referenced element, where the referencing is specified with a tref element.
   *
   * MDN
   */
  final lazy val tref = "tref".tag[*.Element]
  /**
   * Within a text element, text and font properties and the current text
   * position can be adjusted with absolute or relative coordinate values by
   * including a tspan element.
   *
   * MDN
   */
  final lazy val tspan = "tspan".tag[*.TSpan]
  /**
   * The use element takes nodes from within the SVG document, and duplicates
   * them somewhere else. The effect is the same as if the nodes were deeply
   * cloned into a non-exposed DOM, and then pasted where the use element is,
   * much like anonymous content and XBL. Since the cloned nodes are not exposed,
   * care must be taken when using CSS to style a use element and its hidden
   * descendants. CSS attributes are not guaranteed to be inherited by the
   * hidden, cloned DOM unless you explicitly request it using CSS inheritance.
   *
   * MDN
   */
  final lazy val use = "use".tag[*.Use]
  /**
   * A view is a defined way to view the image, like a zoom level or a detail
   * view.
   *
   * MDN
   */
  final lazy val view = "view".tag[*.View]
  /**
   * The vertical distance between two glyphs in top-to-bottom fonts can be
   * fine-tweaked with an vkern Element. This process is known as Kerning.
   *
   * MDN
   */
  final lazy val vkern = "vkern".tag[*.Element]
}
