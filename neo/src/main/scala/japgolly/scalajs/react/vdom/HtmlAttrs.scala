package japgolly.scalajs.react.vdom

import scala.scalajs.js
import japgolly.scalajs.react.raw
import PackageBase._

object HtmlAttrs extends HtmlAttrs
trait HtmlAttrs {

  /**
    * If the value of the type attribute is file, this attribute indicates the
    * types of files that the server accepts; otherwise it is ignored.
    */
  final lazy val accept = ReactAttr("accept")

  final lazy val acceptCharset = ReactAttr("acceptCharset")

  final lazy val accessKey = ReactAttr("accessKey")

  /**
    * The URI of a program that processes the information submitted via the form.
    * This value can be overridden by a formaction attribute on a button or
    * input element.
    */
  final lazy val action = ReactAttr("action")

  final lazy val allowFullScreen = ReactAttr("allowFullScreen")

  final lazy val allowTransparency = ReactAttr("allowTransparency")

  /**
    * This attribute defines the alternative text describing the image. Users
    * will see this displayed if the image URL is wrong, the image is not in one
    * of the supported formats, or until the image is downloaded.
    */
  final lazy val alt = ReactAttr[String]("alt")

  /**
    * ARIA is a set of special accessibility attributes which can be added
    * to any markup, but is especially suited to HTML. The role attribute
    * defines what the general type of object is (such as an article, alert,
    * or slider). Additional ARIA attributes provide other useful properties,
    * such as a description for a form or the current value of a progressbar.
    */
  object aria {

    /**
      * Identifies the currently active descendant of a composite widget.
      */
    final lazy val activeDescendant = ReactAttr("aria-activedescendant")

    /**
      * Indicates whether assistive technologies will present all, or only parts of, the changed region based on the change notifications defined by the aria-relevant attribute. See related aria-relevant.
      */
    final lazy val atomic = ReactAttr("aria-atomic")

    /**
      * Indicates whether user input completion suggestions are provided.
      */
    final lazy val autoComplete = ReactAttr("aria-autocomplete")

    /**
      * Indicates whether an element, and its subtree, are currently being updated.
      */
    final lazy val busy = ReactAttr("aria-busy")

    /**
      * Indicates the current "checked" state of checkboxes, radio buttons, and other widgets. See related aria-pressed and aria-selected.
      */
    final lazy val checked = ReactAttr("aria-checked")

    /**
      * Identifies the element (or elements) whose contents or presence are controlled by the current element. See related aria-owns.
      */
    final lazy val controls = ReactAttr("aria-controls")

    /**
      * Identifies the element (or elements) that describes the object. See related aria-labelledby.
      */
    final lazy val describedBy = ReactAttr("aria-describedby")

    /**
      * Indicates that the element is perceivable but disabled, so it is not editable or otherwise operable. See related aria-hidden and aria-readonly.
      */
    final lazy val disabled = ReactAttr("aria-disabled")

    /**
      * Indicates what functions can be performed when the dragged object is released on the drop target. This allows assistive technologies to convey the possible drag options available to users, including whether a pop-up menu of choices is provided by the application. Typically, drop effect functions can only be provided once an object has been grabbed for a drag operation as the drop effect functions available are dependent on the object being dragged.
      */
    final lazy val dropEffect = ReactAttr("aria-dropeffect")

    /**
      * Indicates whether the element, or another grouping element it controls, is currently expanded or collapsed.
      */
    final lazy val expanded = ReactAttr("aria-expanded")

    /**
      * Identifies the next element (or elements) in an alternate reading order of content which, at the user's discretion, allows assistive technology to override the general default of reading in document source order.
      */
    final lazy val flowTo = ReactAttr("aria-flowto")

    /**
      * Indicates an element's "grabbed" state in a drag-and-drop operation.
      */
    final lazy val grabbed = ReactAttr("aria-grabbed")

    /**
      * Indicates that the element has a popup context menu or sub-level menu.
      */
    final lazy val hasPopup = ReactAttr("aria-haspopup")

    /**
      * Indicates that the element and all of its descendants are not visible or perceivable to any user as implemented by the author. See related aria-disabled.
      */
    final lazy val hidden = ReactAttr("aria-hidden")

    /**
      * Indicates the entered value does not conform to the format expected by the application.
      */
    final lazy val invalid = ReactAttr("aria-invalid")

    /**
      * Defines a string value that labels the current element. See related aria-labelledby.
      */
    final lazy val label = ReactAttr("aria-label")

    /**
      * Identifies the element (or elements) that labels the current element. See related aria-label and aria-describedby.
      */
    final lazy val labelledBy = ReactAttr("aria-labelledby")

    /**
      * Defines the hierarchical level of an element within a structure.
      */
    final lazy val level = ReactAttr("aria-level")

    /**
      * Indicates that an element will be updated, and describes the types of updates the user agents, assistive technologies, and user can expect from the live region.
      */
    final lazy val live = ReactAttr("aria-live")

    /**
      * Indicates whether a text box accepts multiple lines of input or only a single line.
      */
    final lazy val multiline = ReactAttr("aria-multiline")

    /**
      * Indicates that the user may select more than one item from the current selectable descendants.
      */
    final lazy val multiselectable = ReactAttr("aria-multiselectable")

    /**
      * Indicates whether the element and orientation is horizontal or vertical.
      */
    final lazy val orientation = ReactAttr("aria-orientation")

    /**
      * Identifies an element (or elements) in order to define a visual, functional, or contextual parent/child relationship between DOM elements where the DOM hierarchy cannot be used to represent the relationship. See related aria-controls.
      */
    final lazy val owns = ReactAttr("aria-owns")

    /**
      * Defines an element's number or position in the current set of listitems or treeitems. Not required if all elements in the set are present in the DOM. See related aria-setsize.
      */
    final lazy val posInSet = ReactAttr("aria-posinset")

    /**
      * Indicates the current "pressed" state of toggle buttons. See related aria-checked and aria-selected.
      */
    final lazy val pressed = ReactAttr("aria-pressed")

    /**
      * Indicates that the element is not editable, but is otherwise operable. See related aria-disabled.
      */
    final lazy val readonly = ReactAttr("aria-readonly")

    /**
      * Indicates what user agent change notifications (additions, removals, etc.) assistive technologies will receive within a live region. See related aria-atomic.
      */
    final lazy val relevant = ReactAttr("aria-relevant")

    /**
      * Indicates that user input is required on the element before a form may be submitted.
      */
    final lazy val required = ReactAttr("aria-required")

    /**
      * Indicates the current "selected" state of various widgets. See related aria-checked and aria-pressed.
      */
    final lazy val selected = ReactAttr("aria-selected")

    /**
      * Defines the number of items in the current set of listitems or treeitems. Not required if all elements in the set are present in the DOM. See related aria-posinset.
      */
    final lazy val setSize = ReactAttr("aria-setsize")

    /**
      * Indicates if items in a table or grid are sorted in ascending or descending order.
      */
    final lazy val sort = ReactAttr("aria-sort")

    /**
      * Defines the maximum allowed value for a range widget.
      */
    final lazy val valueMax = ReactAttr("aria-valuemax")

    /**
      * Defines the minimum allowed value for a range widget.
      */
    final lazy val valueMin = ReactAttr("aria-valuemin")

    /**
      * Defines the current value for a range widget. See related aria-valuetext.
      */
    final lazy val valueNow = ReactAttr("aria-valuenow")

    /**
      * Defines the human readable text alternative of aria-valuenow for a range widget.
      */
    final lazy val valueText = ReactAttr("aria-valuetext")
  }

  final lazy val async = ReactAttr("async")

  final lazy val autoCapitalize = ReactAttr("autoCapitalize")

  /**
    * This attribute indicates whether the value of the control can be
    * automatically completed by the browser. This attribute is ignored if the
    * value of the type attribute is hidden, checkbox, radio, file, or a button
    * type (button, submit, reset, image).
    *
    * Possible values are "off" and "on"
    */
  final lazy val autoComplete = ReactAttr("autoComplete")

  final lazy val autoCorrect = ReactAttr("autoCorrect")

  /**
    * This Boolean attribute lets you specify that a form control should have
    * input focus when the page loads, unless the user overrides it, for example
    * by typing in a different control. Only one form element in a document can
    * have the autoFocus attribute, which is a Boolean. It cannot be applied if
    * the type attribute is set to hidden (that is, you cannot automatically set
    * focus to a hidden control).
    */
  final lazy val autoFocus = ReactAttr("autoFocus")

  final lazy val autoPlay = ReactAttr("autoPlay")

  final lazy val autoSave = ReactAttr("autoSave")

  /**
    * The capture attribute allows authors to declaratively request use of a media capture mechanism, such as a camera or
    * microphone, from within a file upload control, for capturing media on the spot.
    */
  final lazy val capture = ReactAttr("capture")

  final lazy val cellPadding = ReactAttr("cellPadding")

  final lazy val cellSpacing = ReactAttr("cellSpacing")

  /** &lt;keygen&gt;: A challenge string that is submitted along with the public key. */
  final lazy val challenge = ReactAttr("challenge")

  /**
    * Declares the character encoding of the page or script. Used on meta and
    * script elements.
    */
  final lazy val charset = ReactAttr[String]("charset")

  /**
    * When the value of the type attribute is radio or checkbox, the presence of
    * this Boolean attribute indicates that the control is selected by default;
    * otherwise it is ignored.
    */
  final lazy val checked = ReactAttr[Boolean]("checked")

  final lazy val citeAttr = ReactAttr("cite")

  final lazy val classID = ReactAttr("classID")

  final lazy val colSpan = ReactAttr[Int]("colSpan")

  final def `class`  : Attr[String] = Attr.ClassName
  final def className: Attr[String] = Attr.ClassName
  final def cls      : Attr[String] = Attr.ClassName

  private def classSetImpl(z: TagMod, ps: Seq[(String, Boolean)]): TagMod =
    ps.foldLeft(z)((q, p) =>
      if (p._2)
        q(cls := p._1)
      else
        q)

  final def classSet(ps: (String, Boolean)*): TagMod =
    classSetImpl(EmptyTag, ps)

  final def classSet1(a: String, ps: (String, Boolean)*): TagMod =
    classSetImpl(cls := a, ps)

  final def classSetM(ps: Map[String, Boolean]): TagMod =
    classSetImpl(EmptyTag, ps.toSeq)

  final def classSet1M(a: String, ps: Map[String, Boolean]): TagMod =
    classSetImpl(cls := a, ps.toSeq)

  /**
    * The visible width of the text control, in average character widths. If it
    * is specified, it must be a positive integer. If it is not specified, the
    * default value is 20 (HTML5).
    */
  final lazy val cols = ReactAttr("cols")
  /**
    * This attribute gives the value associated with the http-equiv or name
    * attribute, depending of the context.
    */
  final lazy val contentAttr = ReactAttr("content")

  final lazy val contentEditable = ReactAttr("contentEditable")

  final lazy val contextMenu = ReactAttr("contextMenu")

  final lazy val controls = ReactAttr("controls")

  final lazy val coords = ReactAttr("coords")

  final lazy val crossOrigin = ReactAttr("crossOrigin")

  final lazy val dangerouslySetInnerHtml = ReactAttr[InnerHtmlAttr]("dangerouslySetInnerHTML")

  final lazy val dateTime = ReactAttr("dateTime")

  final lazy val default = ReactAttr("default")

  final lazy val defaultValue = ReactAttr("defaultValue")

  final lazy val defer = ReactAttr("defer")

  final lazy val dir = ReactAttr("dir")

  /**
    * This Boolean attribute indicates that the form control is not available for
    * interaction. In particular, the click event will not be dispatched on
    * disabled controls. Also, a disabled control's value isn't submitted with
    * the form.
    *
    * This attribute is ignored if the value of the type attribute is hidden.
    */
  final lazy val disabled = ReactAttr[Boolean]("disabled")

  final lazy val download = ReactAttr("download")

  final lazy val draggable = ReactAttr[Boolean]("draggable")

  final lazy val encType = ReactAttr("encType")

  /**
    * Describes elements which belongs to this one. Used on labels and output
    * elements.
    */
  final lazy val `for` = ReactAttr("htmlFor")

  final lazy val formAction = ReactAttr("formAction")

  final lazy val formEncType = ReactAttr("formEncType")

  final lazy val formMethod = ReactAttr("formMethod")

  final lazy val formNoValidate = ReactAttr("formNoValidate")

  final lazy val formTarget = ReactAttr("formTarget")

  final lazy val frameBorder = ReactAttr("frameBorder")

  final lazy val headers = ReactAttr("headers")

  /**
    * For use in &lt;meter&gt; tags.
    *
    * @see https://css-tricks.com/html5-meter-element/
    */
  final lazy val high = ReactAttr("high")

  /**
    * This is the single required attribute for anchors defining a hypertext
    * source link. It indicates the link target, either a URL or a URL fragment.
    * A URL fragment is a name preceded by a hash mark (#), which specifies an
    * internal target location (an ID) within the current document. URLs are not
    * restricted to Web (HTTP)-based documents. URLs might use any protocol
    * supported by the browser. For example, file, ftp, and mailto work in most
    * user agents.
    */
  final lazy val href = ReactAttr[String]("href")

  final lazy val hrefLang = ReactAttr("hrefLang")

  final lazy val htmlFor = ReactAttr("htmlFor")

  /**
    * This enumerated attribute defines the pragma that can alter servers and
    * user-agents behavior. The value of the pragma is defined using the content
    * attribute and can be one of the following:
    *
    * - content-language
    * - content-type
    * - default-style
    * - refresh
    * - set-cookie
    */
  final lazy val httpEquiv = ReactAttr("httpEquiv")

  final lazy val icon = ReactAttr("icon")

  /**
    * This attribute defines a unique identifier (ID) which must be unique in
    * the whole document. Its purpose is to identify the element when linking
    * (using a fragment identifier), scripting, or styling (with CSS).
    */
  final lazy val id = ReactAttr("id")

  /**
    * The inputmode attribute tells the browser on devices with dynamic keyboards which keyboard to display. The
    * inputmode attribute applies to the text, search and password input types as well as &lt;textarea&gt;.
    */
  final lazy val inputMode = ReactAttr("inputMode")

  /**
    * http://www.w3.org/TR/2015/CR-SRI-20151112/#the-integrity-attribute
    */
  final lazy val integrity = ReactAttr("integrity")

  final lazy val is = ReactAttr("is")

  final lazy val itemProp = ReactAttr("itemProp")

  final lazy val itemScope = ReactAttr("itemScope")

  final lazy val itemType = ReactAttr("itemType")

  /** React key */
  final val key = ReactAttr.Key

  /** For use in &lt;keygen&gt; */
  final lazy val keyParams = ReactAttr("keyParams")

  /** &lt;keygen&gt;: Specifies the type of key generated. */
  final lazy val keyType = ReactAttr("keyType")

  final lazy val kind = ReactAttr("kind")

  /**
    * This attribute participates in defining the language of the element, the
    * language that non-editable elements are written in or the language that
    * editable elements should be written in. The tag contains one single entry
    * value in the format defines in the Tags for Identifying Languages (BCP47)
    * IETF document. If the tag content is the empty string the language is set
    * to unknown; if the tag content is not valid, regarding to BCP47, it is set
    * to invalid.
    */
  final lazy val lang = ReactAttr[String]("lang")

  final lazy val list = ReactAttr("list")

  final lazy val loop = ReactAttr("loop")

  /**
    * For use in &lt;meter&gt; tags.
    *
    * @see https://css-tricks.com/html5-meter-element/
    */
  final lazy val low = ReactAttr("low")

  final lazy val manifest = ReactAttr("manifest")

  final lazy val marginHeight = ReactAttr("marginHeight")

  final lazy val marginWidth = ReactAttr("marginWidth")

  /**
    * For use in &lt;meter&gt; tags.
    *
    * @see https://css-tricks.com/html5-meter-element/
    */
  final lazy val max = ReactAttr("max")

  final lazy val maxLength = ReactAttr("maxLength")

  /**
    * This attribute specifies the media which the linked resource applies to.
    * Its value must be a media query. This attribute is mainly useful when
    * linking to external stylesheets by allowing the user agent to pick
    * the best adapted one for the device it runs on.
    *
    * @see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/link#attr-media
    */
  final lazy val media = ReactAttr("media")

  final lazy val mediaGroup = ReactAttr("mediaGroup")

  /**
    * The HTTP method that the browser uses to submit the form. Possible values are:
    *
    * - post: Corresponds to the HTTP POST method ; form data are included in the
    * body of the form and sent to the server.
    *
    * - get: Corresponds to the HTTP GET method; form data are appended to the
    * action attribute URI with a '?' as a separator, and the resulting URI is
    * sent to the server. Use this method when the form has no side-effects and
    * contains only ASCII characters.
    *
    * This value can be overridden by a formmethod attribute on a button or
    * input element.
    */
  final lazy val method = ReactAttr("method")

  /**
    * For use in &lt;meter&gt; tags.
    *
    * @see https://css-tricks.com/html5-meter-element/
    */
  final lazy val min = ReactAttr("min")

  final lazy val minLength = ReactAttr("minLength")

  final lazy val multiple = ReactAttr("multiple")

  final lazy val muted = ReactAttr("muted")

  /**
    * On form elements (input etc.):
    * Name of the element. For example used by the server to identify the fields
    * in form submits.
    *
    * On the meta tag:
    * This attribute defines the name of a document-level metadata.
    * This document-level metadata name is associated with a value, contained by
    * the content attribute.
    */
  final lazy val name = ReactAttr("name")

  final lazy val noValidate = ReactAttr("noValidate")

  /** For &lt;script&gt; and &lt;style&gt;elements. */
  final lazy val nonce = ReactAttr("nonce")

  final lazy val onAbort = Attr.Event.base("onAbort")

  final lazy val onAnimationEnd = Attr.Event.base("onAnimationEnd")

  final lazy val onAnimationIteration = Attr.Event.base("onAnimationIteration")

  final lazy val onAnimationStart = Attr.Event.base("onAnimationStart")

  final lazy val onBeforeInput = Attr.Event.base("onBeforeInput")

  /**
    * The blur event is raised when an element loses focus.
    */
  final lazy val onBlur = Attr.Event.focus("onBlur")

  final lazy val onCanPlay = Attr.Event.base("onCanPlay")

  final lazy val onCanPlayThrough = Attr.Event.base("onCanPlayThrough")

  /**
    * The change event is fired for input, select, and textarea elements
    * when a change to the element's value is committed by the user.
    */
  final val onChange = Attr.Event.base("onChange")

  /**
    * The click event is raised when the user clicks on an element. The click
    * event will occur after the mousedown and mouseup events.
    */
  final val onClick = Attr.Event.mouse("onClick")

  final lazy val onCompositionEnd = Attr.Event.composition("onCompositionEnd")

  final lazy val onCompositionStart = Attr.Event.composition("onCompositionStart")

  final lazy val onCompositionUpdate = Attr.Event.composition("onCompositionUpdate")

  final lazy val onContextMenu = Attr.Event.base("onContextMenu")

  final lazy val onCopy = Attr.Event.base("onCopy")

  final lazy val onCut = Attr.Event.base("onCut")

  /** React alias for [[onDoubleClick]] */
  final def onDblClick = onDoubleClick

  /**
    * The dblclick event is fired when a pointing device button (usually a
    * mouse button) is clicked twice on a single element.
    */
  final lazy val onDoubleClick = Attr.Event.mouse("onDoubleClick")

  final lazy val onDrag = Attr.Event.drag("onDrag")

  final lazy val onDragEnd = Attr.Event.drag("onDragEnd")

  final lazy val onDragEnter = Attr.Event.drag("onDragEnter")

  final lazy val onDragExit = Attr.Event.drag("onDragExit")

  final lazy val onDragLeave = Attr.Event.drag("onDragLeave")

  final lazy val onDragOver = Attr.Event.drag("onDragOver")

  final lazy val onDragStart = Attr.Event.drag("onDragStart")

  final lazy val onDrop = Attr.Event.base("onDrop")

  final lazy val onDurationChange = Attr.Event.base("onDurationChange")

  final lazy val onEmptied = Attr.Event.base("onEmptied")

  final lazy val onEncrypted = Attr.Event.base("onEncrypted")

  final lazy val onEnded = Attr.Event.base("onEnded")

  /**
    * Type: script code
    *
    * This event is sent to an image element when an error occurs loading the image.
    *
    * https://developer.mozilla.org/en-US/docs/Mozilla/Tech/XUL/image#a-onerror
    */
  final lazy val onError = Attr.Event.base("onError")

  /**
    * The focus event is raised when the user sets focus on the given element.
    */
  final lazy val onFocus = Attr.Event.focus("onFocus")

  final lazy val onInput = Attr.Event.base("onInput")

  final lazy val onInvalid = Attr.Event.base("onInvalid")

  /**
    * The keydown event is raised when the user presses a keyboard key.
    */
  final lazy val onKeyDown = Attr.Event.keyboard("onKeyDown")

  /**
    * The keypress event should be raised when the user presses a key on the keyboard.
    * However, not all browsers fire keypress events for certain keys.
    *
    * Webkit-based browsers (Google Chrome and Safari, for example) do not fire keypress events on the arrow keys.
    * Firefox does not fire keypress events on modifier keys like SHIFT.
    */
  final lazy val onKeyPress = Attr.Event.keyboard("onKeyPress")

  /**
    * The keyup event is raised when the user releases a key that's been pressed.
    */
  final lazy val onKeyUp = Attr.Event.keyboard("onKeyUp")

  /**
    * The load event fires at the end of the document loading process. At this
    * point, all of the objects in the document are in the DOM, and all the
    * images and sub-frames have finished loading.
    */
  final lazy val onLoad = Attr.Event.base("onLoad")

  final lazy val onLoadStart = Attr.Event.base("onLoadStart")

  final lazy val onLoadedData = Attr.Event.base("onLoadedData")

  final lazy val onLoadedMetadata = Attr.Event.base("onLoadedMetadata")

  /**
    * The mousedown event is raised when the user presses the mouse button.
    */
  final lazy val onMouseDown = Attr.Event.mouse("onMouseDown")

  /**
    * The mouseenter event is fired when a pointing device (usually a mouse)
    * is moved over the element that has the listener attached.
    */
  final lazy val onMouseEnter = Attr.Event.mouse("onMouseEnter")

  /**
    * The mouseleave event is fired when a pointing device (usually a mouse)
    * is moved off the element that has the listener attached.
    */
  final lazy val onMouseLeave = Attr.Event.mouse("onMouseLeave")

  /**
    * The mousemove event is raised when the user moves the mouse.
    */
  final lazy val onMouseMove = Attr.Event.mouse("onMouseMove")

  /**
    * The mouseout event is raised when the mouse leaves an element (e.g, when
    * the mouse moves off of an image in the web page, the mouseout event is
    * raised for that image element).
    */
  final lazy val onMouseOut = Attr.Event.mouse("onMouseOut")

  /**
    * The mouseover event is raised when the user moves the mouse over a
    * particular element.
    */
  final lazy val onMouseOver = Attr.Event.mouse("onMouseOver")

  /**
    * The mouseup event is raised when the user releases the mouse button.
    */
  final lazy val onMouseUp = Attr.Event.mouse("onMouseUp")

  final lazy val onPaste = Attr.Event.base("onPaste")

  final lazy val onPause = Attr.Event.base("onPause")

  final lazy val onPlay = Attr.Event.base("onPlay")

  final lazy val onPlaying = Attr.Event.base("onPlaying")

  final lazy val onProgress = Attr.Event.base("onProgress")

  final lazy val onRateChange = Attr.Event.base("onRateChange")

  /**
    * The reset event is fired when a form is reset.
    */
  final lazy val onReset = Attr.Event.base("onReset")

  /**
    * Specifies the function to be called when the window is scrolled.
    */
  final lazy val onScroll = Attr.Event.base("onScroll")

  final lazy val onSeeked = Attr.Event.base("onSeeked")

  final lazy val onSeeking = Attr.Event.base("onSeeking")

  /**
    * The select event only fires when text inside a text input or textarea is
    * selected. The event is fired after the text has been selected.
    */
  final lazy val onSelect = Attr.Event.base("onSelect")

  final lazy val onStalled = Attr.Event.base("onStalled")

  /**
    * The submit event is raised when the user clicks a submit button in a form
    * (<input type="submit"/>).
    */
  final lazy val onSubmit = Attr.Event.base("onSubmit")

  final lazy val onSuspend = Attr.Event.base("onSuspend")

  final lazy val onTimeUpdate = Attr.Event.base("onTimeUpdate")

  /**
    * Event indicating that the touch point has been canceled or disrupted.
    *
    * For example, when popup menu is shown.
    */
  final lazy val onTouchCancel = Attr.Event.touch("onTouchCancel")

  /**
    * Event indicating that the touch point does not exist any more.
    *
    * For example, whn you release your finger.
    */
  final lazy val onTouchEnd = Attr.Event.touch("onTouchEnd")

  /**
    * Event indicating that the touch point has moved along the plane.
    */
  final lazy val onTouchMove = Attr.Event.touch("onTouchMove")

  /**
    * Event indicating that the user has touched the plane.
    */
  final lazy val onTouchStart = Attr.Event.touch("onTouchStart")

  final lazy val onTransitionEnd = Attr.Event.base("onTransitionEnd")

  final lazy val onVolumeChange = Attr.Event.base("onVolumeChange")

  final lazy val onWaiting = Attr.Event.base("onWaiting")

  final lazy val onWheel = Attr.Event.wheel("onWheel")

  final lazy val open = ReactAttr("open")

  /**
    * For use in &lt;meter&gt; tags.
    *
    * @see https://css-tricks.com/html5-meter-element/
    */
  final lazy val optimum = ReactAttr("optimum")

  /**
    * A hint to the user of what can be entered in the control. The placeholder
    * text must not contain carriage returns or line-feeds. This attribute
    * applies when the value of the type attribute is text, search, tel, url or
    * email; otherwise it is ignored.
    */
  final lazy val placeholder = ReactAttr("placeholder")

  final lazy val poster = ReactAttr("poster")

  final lazy val preload = ReactAttr("preload")

  final lazy val profile = ReactAttr("profile")

  final lazy val radioGroup = ReactAttr("radioGroup")

  /**
    * This Boolean attribute indicates that the user cannot modify the value of
    * the control. This attribute is ignored if the value of the type attribute
    * is hidden, range, color, checkbox, radio, file, or a button type.
    */
  final lazy val readOnly = ReactAttr[Boolean]("readOnly")

  // final val ref = ReactAttr.Ref

  /**
    * This attribute names a relationship of the linked document to the current
    * document. The attribute must be a space-separated list of the link types
    * values. The most common use of this attribute is to specify a link to an
    * external style sheet: the rel attribute is set to stylesheet, and the href
    * attribute is set to the URL of an external style sheet to format the page.
    *
    */
  final lazy val rel = ReactAttr("rel")

  /**
    * This attribute specifies that the user must fill in a value before
    * submitting a form. It cannot be used when the type attribute is hidden,
    * image, or a button type (submit, reset, or button). The :optional and
    * :required CSS pseudo-classes will be applied to the field as appropriate.
    */
  final lazy val required = ReactAttr[Boolean]("required")

  final lazy val results = ReactAttr("results")

  /** For &lt;ol&gt; elements. */
  final lazy val reversed = ReactAttr("reversed")

  /**
    * The attribute describes the role(s) the current element plays in the
    * context of the document. This can be used, for example,
    * by applications and assistive technologies to determine the purpose of
    * an element. This could allow a user to make informed decisions on which
    * actions may be taken on an element and activate the selected action in a
    * device independent way. It could also be used as a mechanism for
    * annotating portions of a document in a domain specific way (e.g.,
    * a legal term taxonomy). Although the role attribute may be used to add
    * semantics to an element, authors should use elements with inherent
    * semantics, such as p, rather than layering semantics on semantically
    * neutral elements, such as div role="paragraph".
    *
    * @see http://www.w3.org/TR/role-attribute/#s_role_module_attributes
    */
  final lazy val role = ReactAttr("role")

  final lazy val rowSpan = ReactAttr[Int]("rowSpan")

  /**
    * The number of visible text lines for the control.
    */
  final lazy val rows = ReactAttr[Int]("rows")

  final lazy val sandbox = ReactAttr("sandbox")

  final lazy val scope = ReactAttr("scope")

  /**
    * For use in &lt;style&gt; tags.
    *
    * If this attribute is present, then the style applies only to its parent element.
    * If absent, the style applies to the whole document.
    */
  final lazy val scoped = ReactAttr("scoped")

  final lazy val scrolling = ReactAttr("scrolling")

  final lazy val seamless = ReactAttr("seamless")

  final lazy val security = ReactAttr("security")

  final lazy val selected = ReactAttr("selected")

  final lazy val shape = ReactAttr("shape")

  /**
    * The initial size of the control. This value is in pixels unless the value
    * of the type attribute is text or password, in which case, it is an integer
    * number of characters. Starting in HTML5, this attribute applies only when
    * the type attribute is set to text, search, tel, url, email, or password;
    * otherwise it is ignored. In addition, the size must be greater than zero.
    * If you don't specify a size, a default value of 20 is used.
    */
  final lazy val size = ReactAttr[Int]("size")

  final lazy val sizes = ReactAttr("sizes")

  /**
    * This enumerated attribute defines whether the element may be checked for
    * spelling errors.
    */
  final lazy val spellCheck = ReactAttr("spellCheck")

  /**
    * If the value of the type attribute is image, this attribute specifies a URI
    * for the location of an image to display on the graphical submit button;
    * otherwise it is ignored.
    */
  final val src = ReactAttr[String]("src")

  final lazy val srcDoc = ReactAttr("srcDoc")

  final lazy val srcLang = ReactAttr("srcLang")

  final lazy val srcSet = ReactAttr("srcSet")

  final lazy val step = ReactAttr("step")

  /**
    * This attribute contains CSS styling declarations to be applied to the
    * element. Note that it is recommended for styles to be defined in a separate
    * file or files. This attribute and the style element have mainly the
    * purpose of allowing for quick styling, for example for testing purposes.
    */
  final def style: Attr[js.Object] = Attr.Style

  /**
    * The value is actually just `summary`. This is named `summaryAttr` in Scala to avoid a conflict with the
    * &lt;summary&gt; tag in [[all]].
    */
  final lazy val summaryAttr = ReactAttr("summary")

  /**
    * This integer attribute indicates if the element can take input focus (is
    * focusable), if it should participate to sequential keyboard navigation, and
    * if so, at what position. It can takes several values:
    *
    * - a negative value means that the element should be focusable, but should
    * not be reachable via sequential keyboard navigation;
    * - 0 means that the element should be focusable and reachable via sequential
    * keyboard navigation, but its relative order is defined by the platform
    * convention;
    * - a positive value which means should be focusable and reachable via
    * sequential keyboard navigation; its relative order is defined by the value
    * of the attribute: the sequential follow the increasing number of the
    * tabIndex. If several elements share the same tabIndex, their relative order
    * follows their relative position in the document).
    *
    * An element with a 0 value, an invalid value, or no tabIndex value should be placed after elements with a positive tabIndex in the sequential keyboard navigation order.
    */
  final lazy val tabIndex = ReactAttr[Int]("tabIndex")

  /**
    * A name or keyword indicating where to display the response that is received
    * after submitting the form. In HTML 4, this is the name of, or a keyword
    * for, a frame. In HTML5, it is a name of, or keyword for, a browsing context
    * (for example, tab, window, or inline frame). The following keywords have
    * special meanings:
    *
    * - _self: Load the response into the same HTML 4 frame (or HTML5 browsing
    * context) as the current one. This value is the default if the attribute
    * is not specified.
    * - _blank: Load the response into a new unnamed HTML 4 window or HTML5
    * browsing context.
    * - _parent: Load the response into the HTML 4 frameset parent of the current
    * frame or HTML5 parent browsing context of the current one. If there is no
    * parent, this option behaves the same way as _self.
    * - _top: HTML 4: Load the response into the full, original window, canceling
    * all other frames. HTML5: Load the response into the top-level browsing
    * context (that is, the browsing context that is an ancestor of the current
    * one, and has no parent). If there is no parent, this option behaves the
    * same way as _self.
    * - iframename: The response is displayed in a named iframe.
    */
  object target extends Attr.Generic[String]("target") {

    /** Load the response into the same HTML 4 frame (or HTML5 browsing
      * context) as the current one. This value is the default if the attribute
      * is not specified.
      */
    def self = this := "_self"

    /** Load the response into a new unnamed HTML 4 window or HTML5 browsing context. */
    def blank = this := "_blank"

    /**
      * Load the response into the HTML 4 frameset parent of the current
      * frame or HTML5 parent browsing context of the current one. If there is no
      * parent, this option behaves the same way as _self.
      */
    def parent = this := "_parent"

    /**
      * HTML 4: Load the response into the full, original window, canceling
      * all other frames. HTML5: Load the response into the top-level browsing
      * context (that is, the browsing context that is an ancestor of the current
      * one, and has no parent). If there is no parent, this option behaves the
      * same way as _self.
      */
    def top = this := "_top"
  }

  /**
    * This attribute contains a text representing advisory information related to
    * the element it belongs too. Such information can typically, but not
    * necessarily, be presented to the user as a tooltip.
    */
  final val title = ReactAttr("title")

  /**
    * Shorthand for the `type` attribute
    */
  final def tpe = `type`

  /**
    * This attribute is used to define the type of the content linked to. The
    * value of the attribute should be a MIME type such as text/html, text/css,
    * and so on. The common use of this attribute is to define the type of style
    * sheet linked and the most common current value is text/css, which indicates
    * a Cascading Style Sheet format. You can use tpe as an alias for this
    * attribute so you don't have to backtick-escape this attribute.
    */
  final val `type` = ReactAttr("type")

  /** IE-specific property to prevent user selection */
  final lazy val unselectable = ReactAttr("unselectable")

  final lazy val useMap = ReactAttr("useMap")

  /**
    * The initial value of the control. This attribute is optional except when
    * the value of the type attribute is radio or checkbox.
    */
  final val value = ReactAttr("value")

  final lazy val wmode = ReactAttr("wmode")

  /** &lt;textarea&gt;: Indicates whether the text should be wrapped. */
  final lazy val wrap = ReactAttr("wrap")

  final lazy val xmlns = ReactAttr("xmlns")
}
