package japgolly.scalajs.react.vdom

import scala.scalajs.js
import japgolly.scalajs.react.{ReactElement, ReactNode, React}

private[vdom] object VDomBuilder  {

  def specialCaseAttrs =
    Set("onBlur", "onChange", "onClick", "onFocus", "onKeyDown", "onKeyPress", "onKeyUp", "onLoad", "onMouseDown"
      , "onMouseMove", "onMouseOut", "onMouseOver", "onMouseUp", "onSelect", "onScroll", "onSubmit", "onReset"
      , "readOnly", "colSpan", "rowSpan")

  def specialNameAttrs =
    Map("class" -> "className", "for" -> "htmlFor")

  val attrTranslations =
    specialCaseAttrs.toList.map(x => x.toLowerCase -> x).toMap ++ specialNameAttrs

  // May as well just copy Scalatags and customise
  // cat ~/crap/ReactStyles.scala | fgrep -- - | egrep -v '^ *\* ' | fgrep -v '= this :=' | perl -pe 's/^.+(".+").+(".+-.+").*$/GOL \2 -> \1,/' | grep GOL | cut -b4- | sort
  val styleTranslations = Map(
    "animation-delay" -> "animationDelay",
    "animation-direction" -> "animationDirection",
    "animation-duration" -> "animationDuration",
    "animation-fill-mode" -> "animationFillMode",
    "animation-iteration-count" -> "animationIterationCount",
    "animation-name" -> "animationName",
    "animation-play-state" -> "animationPlayState",
    "animation-timing-function" -> "animationTimingFunction",
    "backface-visibility" -> "backfaceVisibility",
    "background-attachment" -> "backgroundAttachment",
    "background-clip" -> "backgroundClip",
    "background-color" -> "backgroundColor",
    "background-image" -> "backgroundImage",
    "background-origin" -> "backgroundOrigin",
    "background-position" -> "backgroundPosition",
    "background-repeat" -> "backgroundRepeat",
    "background-size" -> "backgroundSize",
    "border-bottom" -> "borderBottom",
    "border-bottom-color" -> "borderBottomColor",
    "border-bottom-left-radius" -> "borderBottomLeftRadius",
    "border-bottom-right-radius" -> "borderBottomRightRadius",
    "border-bottom-style" -> "borderBottomStyle",
    "border-bottom-width" -> "borderBottomWidth",
    "border-collapse" -> "borderCollapse",
    "border-color" -> "borderColor",
    "border-left" -> "borderLeft",
    "border-left-color" -> "borderLeftColor",
    "border-left-style" -> "borderLeftStyle",
    "border-left-width" -> "borderLeftWidth",
    "border-radius" -> "borderRadius",
    "border-right" -> "borderRight",
    "border-right-color" -> "borderRightColor",
    "border-right-style" -> "bocrderRightStyle",
    "border-right-width" -> "borderRightWidth",
    "border-spacing" -> "borderSpacing",
    "border-style" -> "borderStyle",
    "border-top" -> "borderTop",
    "border-top-color" -> "borderTopColor",
    "border-top-left-radius" -> "borderTopLeftRadius",
    "border-top-right-radius" -> "borderTopRightRadius",
    "border-top-style" -> "borderTopStyle",
    "border-top-width" -> "borderTopWidth",
    "border-width" -> "borderWidth",
    "box-shadow" -> "boxShadow",
    "box-sizing" -> "boxSizing",
    "caption-side" -> "captionSide",
    "column-count" -> "columnCount",
    "column-fill" -> "columnFill",
    "column-gap" -> "columnGap",
    "column-rule-color" -> "columnRuleColor",
    "column-rule" -> "columnRule",
    "column-rule-style" -> "columnRuleStyle",
    "column-rule-width" -> "columnRuleWidth",
    "column-span" -> "columnSpan",
    "column-width" -> "columnWidth",
    "counter-increment" -> "counterIncrement",
    "counter-reset" -> "counterReset",
    "empty-cells" -> "emptyCells",
    "font-family" -> "fontFamily",
    "font-feature-settings" -> "fontFeatureSettings",
    "font-size-adjust" -> "fontSizeAdjust",
    "font-size" -> "fontSize",
    "font-style" -> "fontStyle",
    "font-weight" -> "fontWeight",
    "letter-spacing" -> "letterSpacing",
    "list-style-image" -> "listStyleImage",
    "list-style" -> "listStyle",
    "list-style-position" -> "listStylePosition",
    "list-style-type" -> "listStyleType",
    "margin-bottom" -> "marginBottom",
    "margin-left" -> "marginLeft",
    "margin-right" -> "marginRight",
    "margin-top" -> "marginTop",
    "max-height" -> "maxHeight",
    "max-width" -> "maxWidth",
    "min-height" -> "minHeight",
    "outline-color" -> "outlineColor",
    "outline-style" -> "outlineStyle",
    "outline-width" -> "outlineWidth",
    "overflow-x" -> "overflowX",
    "overflow-y" -> "overflowY",
    "padding-bottom" -> "paddingBottom",
    "padding-left" -> "paddingLeft",
    "padding-right" -> "paddingRight",
    "padding-top" -> "paddingTop",
    "page-break-after" -> "pageBreakAfter",
    "page-break-before" -> "pageBreakBefore",
    "page-break-inside" -> "pageBreakInside",
    "perspective-origin" -> "perspectiveOrigin",
    "pointer-events" -> "pointerEvents",
    "table-layout" -> "tableLayout",
    "text-align-last" -> "textAlignLast",
    "text-align" -> "textAlign",
    "text-decoration" -> "textDecoration",
    "text-indent" -> "textIndent",
    "text-overflow" -> "textOverflow",
    "text-shadow" -> "textShadow",
    "text-transform" -> "textTransform",
    "text-underline-position" -> "textUnderlinePosition",
    "transform-origin" -> "transformOrigin",
    "transform-style" -> "transformStyle",
    "transition-delay" -> "transitionDelay",
    "transition-duration" -> "transitionDuration",
    "transition-property" -> "transitionProperty",
    "transition-timing-function" -> "transitionTimingFunction",
    "unicode-bidi" -> "unicodeBidi",
    "vertical-align" -> "verticalAlign",
    "white-space" -> "whiteSpace",
    "word-break" -> "wordBreak",
    "word-spacing" -> "wordSpacing",
    "word-wrap" -> "wordWrap",
    "z-index" -> "zIndex")
}

private[vdom] final class VDomBuilder {
  import VDomBuilder._

  private[this] var props    = new js.Object
  private[this] var style    = new js.Object
  private[this] var children = new js.Array[ReactFragT]()

  def appendChild(c: ReactFragT): Unit =
    children.push(c)

  def addAttr(k: String, v: js.Any): Unit =
    set(props, attrTranslations.getOrElse(k, k), v)

  def addStyle(k: String, v: String): Unit =
    set(style, styleTranslations.getOrElse(k, k), v)

  @inline private[this] def set(o: js.Object, k: String, v: js.Any): Unit =
    o.asInstanceOf[js.Dynamic].updateDynamic(k)(v)

  @inline private[this] def hasStyle = js.Object.keys(style).length != 0

  def render(tag: String): ReactElement = {
    if (hasStyle) set(props, "style", style)
    React.createElement(tag, props, children: _*)
  }
}
