package japgolly.scalajs.react.vdom

import scala.scalajs.js
import PackageBase._

object HtmlAttrs extends HtmlAttrs
trait HtmlAttrs {

  /**
    * If the value of the type attribute is file, this attribute indicates the
    * types of files that the server accepts; otherwise it is ignored.
    */
  final def accept = VdomAttr("accept")

  final def acceptCharset = VdomAttr("acceptCharset")

  final def accessKey = VdomAttr("accessKey")

  /**
    * The URI of a program that processes the information submitted via the form.
    * This value can be overridden by a formaction attribute on a button or
    * input element.
    */
  final def action = VdomAttr("action")

  final def allowFullScreen = VdomAttr[Boolean]("allowFullScreen")

  final def allowTransparency = VdomAttr[Boolean]("allowTransparency")

  /**
    * This attribute defines the alternative text describing the image. Users
    * will see this displayed if the image URL is wrong, the image is not in one
    * of the supported formats, or until the image is downloaded.
    */
  final def alt = VdomAttr[String]("alt")

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
    final def activeDescendant = VdomAttr("aria-activedescendant")

    /**
      * Indicates whether assistive technologies will present all, or only parts of, the changed region based on the change notifications defined by the aria-relevant attribute. See related aria-relevant.
      */
    final def atomic = VdomAttr("aria-atomic")

    /**
      * Indicates whether user input completion suggestions are provided.
      */
    final def autoComplete = VdomAttr("aria-autocomplete")

    /**
      * Indicates whether an element, and its subtree, are currently being updated.
      */
    final def busy = VdomAttr("aria-busy")

    /**
      * Indicates the current "checked" state of checkboxes, radio buttons, and other widgets. See related aria-pressed and aria-selected.
      */
    final def checked = VdomAttr("aria-checked")

    /**
      * Identifies the element (or elements) whose contents or presence are controlled by the current element. See related aria-owns.
      */
    final def controls = VdomAttr("aria-controls")

    /**
      * Identifies the element (or elements) that describes the object. See related aria-labelledby.
      */
    final def describedBy = VdomAttr("aria-describedby")

    /**
      * Indicates that the element is perceivable but disabled, so it is not editable or otherwise operable. See related aria-hidden and aria-readonly.
      */
    final def disabled = VdomAttr("aria-disabled")

    /**
      * Indicates what functions can be performed when the dragged object is released on the drop target. This allows assistive technologies to convey the possible drag options available to users, including whether a pop-up menu of choices is provided by the application. Typically, drop effect functions can only be provided once an object has been grabbed for a drag operation as the drop effect functions available are dependent on the object being dragged.
      */
    final def dropEffect = VdomAttr("aria-dropeffect")

    /**
      * Indicates whether the element, or another grouping element it controls, is currently expanded or collapsed.
      */
    final def expanded = VdomAttr("aria-expanded")

    /**
      * Identifies the next element (or elements) in an alternate reading order of content which, at the user's discretion, allows assistive technology to override the general default of reading in document source order.
      */
    final def flowTo = VdomAttr("aria-flowto")

    /**
      * Indicates an element's "grabbed" state in a drag-and-drop operation.
      */
    final def grabbed = VdomAttr("aria-grabbed")

    /**
      * Indicates that the element has a popup context menu or sub-level menu.
      */
    final def hasPopup = VdomAttr("aria-haspopup")

    /**
      * Indicates that the element and all of its descendants are not visible or perceivable to any user as implemented by the author. See related aria-disabled.
      */
    final def hidden = VdomAttr("aria-hidden")

    /**
      * Indicates the entered value does not conform to the format expected by the application.
      */
    final def invalid = VdomAttr("aria-invalid")

    /**
      * Defines a string value that labels the current element. See related aria-labelledby.
      */
    final def label = VdomAttr("aria-label")

    /**
      * Identifies the element (or elements) that labels the current element. See related aria-label and aria-describedby.
      */
    final def labelledBy = VdomAttr("aria-labelledby")

    /**
      * Defines the hierarchical level of an element within a structure.
      */
    final def level = VdomAttr("aria-level")

    /**
      * Indicates that an element will be updated, and describes the types of updates the user agents, assistive technologies, and user can expect from the live region.
      */
    final def live = VdomAttr("aria-live")

    /**
      * Indicates whether a text box accepts multiple lines of input or only a single line.
      */
    final def multiline = VdomAttr("aria-multiline")

    /**
      * Indicates that the user may select more than one item from the current selectable descendants.
      */
    final def multiselectable = VdomAttr("aria-multiselectable")

    /**
      * Indicates whether the element and orientation is horizontal or vertical.
      */
    final def orientation = VdomAttr("aria-orientation")

    /**
      * Identifies an element (or elements) in order to define a visual, functional, or contextual parent/child relationship between DOM elements where the DOM hierarchy cannot be used to represent the relationship. See related aria-controls.
      */
    final def owns = VdomAttr("aria-owns")

    /**
      * Defines an element's number or position in the current set of listitems or treeitems. Not required if all elements in the set are present in the DOM. See related aria-setsize.
      */
    final def posInSet = VdomAttr("aria-posinset")

    /**
      * Indicates the current "pressed" state of toggle buttons. See related aria-checked and aria-selected.
      */
    final def pressed = VdomAttr("aria-pressed")

    /**
      * Indicates that the element is not editable, but is otherwise operable. See related aria-disabled.
      */
    final def readonly = VdomAttr("aria-readonly")

    /**
      * Indicates what user agent change notifications (additions, removals, etc.) assistive technologies will receive within a live region. See related aria-atomic.
      */
    final def relevant = VdomAttr("aria-relevant")

    /**
      * Indicates that user input is required on the element before a form may be submitted.
      */
    final def required = VdomAttr("aria-required")

    /**
      * Indicates the current "selected" state of various widgets. See related aria-checked and aria-pressed.
      */
    final def selected = VdomAttr("aria-selected")

    /**
      * Defines the number of items in the current set of listitems or treeitems. Not required if all elements in the set are present in the DOM. See related aria-posinset.
      */
    final def setSize = VdomAttr("aria-setsize")

    /**
      * Indicates if items in a table or grid are sorted in ascending or descending order.
      */
    final def sort = VdomAttr("aria-sort")

    /**
      * Defines the maximum allowed value for a range widget.
      */
    final def valueMax = VdomAttr("aria-valuemax")

    /**
      * Defines the minimum allowed value for a range widget.
      */
    final def valueMin = VdomAttr("aria-valuemin")

    /**
      * Defines the current value for a range widget. See related aria-valuetext.
      */
    final def valueNow = VdomAttr("aria-valuenow")

    /**
      * Defines the human readable text alternative of aria-valuenow for a range widget.
      */
    final def valueText = VdomAttr("aria-valuetext")
  }

  final def async = VdomAttr[Boolean]("async")

  final def autoCapitalize = VdomAttr("autoCapitalize")

  /** This attribute indicates whether the value of the control can be
    * automatically completed by the browser. This attribute is ignored if the
    * value of the type attribute is hidden, checkbox, radio, file, or a button
    * type (button, submit, reset, image).
    */
  final object autoComplete extends VdomAttr.Generic("autoComplete") {
    def additionalName      = this := "additional-name"
    def addressLevel1       = this := "address-level1"
    def addressLevel2       = this := "address-level2"
    def addressLevel3       = this := "address-level3"
    def addressLevel4       = this := "address-level4"
    def addressLine1        = this := "address-line1"
    def addressLine2        = this := "address-line2"
    def addressLine3        = this := "address-line3"
    def bday                = this := "bday"
    def bdayDay             = this := "bday-day"
    def bdayMonth           = this := "bday-month"
    def bdayYear            = this := "bday-year"
    def ccAdditionalName    = this := "cc-additional-name"
    def ccCsc               = this := "cc-csc"
    def ccExp               = this := "cc-exp"
    def ccExpMonth          = this := "cc-exp-month"
    def ccExpYear           = this := "cc-exp-year"
    def ccFamilyName        = this := "cc-family-name"
    def ccGivenName         = this := "cc-given-name"
    def ccName              = this := "cc-name"
    def ccNumber            = this := "cc-number"
    def ccType              = this := "cc-type"
    def country             = this := "country"
    def countryName         = this := "country-name"
    def currentPassword     = this := "current-password"
    def email               = this := "email"
    def familyName          = this := "family-name"
    def givenName           = this := "given-name"
    def honorificPrefix     = this := "honorific-prefix"
    def honorificSuffix     = this := "honorific-suffix"
    def impp                = this := "impp"
    def language            = this := "language"
    def name                = this := "name"
    def newPassword         = this := "new-password"
    def nickname            = this := "nickname"
    def off                 = this := "off"
    def on                  = this := "on"
    def oneTimeCode         = this := "one-time-code"
    def organization        = this := "organization"
    def organizationTitle   = this := "organization-title"
    def photo               = this := "photo"
    def postalCode          = this := "postal-code"
    def sex                 = this := "sex"
    def streetAddress       = this := "street-address"
    def tel                 = this := "tel"
    def telAreaCode         = this := "tel-area-code"
    def telCountryCode      = this := "tel-country-code"
    def telExtension        = this := "tel-extension"
    def telLocal            = this := "tel-local"
    def telLocalPrefix      = this := "tel-local-prefix"
    def telLocalSuffix      = this := "tel-local-suffix"
    def telNational         = this := "tel-national"
    def transactionAmount   = this := "transaction-amount"
    def transactionCurrency = this := "transaction-currency"
    def url                 = this := "url"
    def username            = this := "username"
    def usernameEmail       = this := "username email"
  }

  final def autoCorrect = VdomAttr[Boolean]("autoCorrect")

  /**
    * This Boolean attribute lets you specify that a form control should have
    * input focus when the page loads, unless the user overrides it, for example
    * by typing in a different control. Only one form element in a document can
    * have the autoFocus attribute, which is a Boolean. It cannot be applied if
    * the type attribute is set to hidden (that is, you cannot automatically set
    * focus to a hidden control).
    */
  final def autoFocus = VdomAttr[Boolean]("autoFocus")

  final def autoPlay = VdomAttr[Boolean]("autoPlay")

  final def autoSave = VdomAttr[Boolean]("autoSave")

  /**
    * The capture attribute allows authors to declaratively request use of a media capture mechanism, such as a camera or
    * microphone, from within a file upload control, for capturing media on the spot.
    */
  final def capture = VdomAttr("capture")

  final def cellPadding = VdomAttr("cellPadding")

  final def cellSpacing = VdomAttr("cellSpacing")

  /** &lt;keygen&gt;: A challenge string that is submitted along with the public key. */
  final def challenge = VdomAttr("challenge")

  /**
    * Declares the character encoding of the page or script. Used on meta and
    * script elements.
    */
  final def charset = VdomAttr[String]("charset")

  /**
    * When the value of the type attribute is radio or checkbox, the presence of
    * this Boolean attribute indicates that the control is selected by default;
    * otherwise it is ignored.
    */
  final def checked = VdomAttr[Boolean]("checked")

  final def citeAttr = VdomAttr("cite")

  final def classID = VdomAttr("classID")

  final def colSpan = VdomAttr[Int]("colSpan")

  final def `class`  : Attr[String] = Attr.ClassName
  final def className: Attr[String] = Attr.ClassName
  final def cls      : Attr[String] = Attr.ClassName

  private def classSetImpl(z: TagMod, ps: Seq[(String, Boolean)]): TagMod =
    ps.foldLeft(z)((q, p) =>
      if (p._2)
        TagMod(q, cls := p._1)
      else
        q)

  final def classSet(ps: (String, Boolean)*): TagMod =
    classSetImpl(TagMod.empty, ps)

  final def classSet1(a: String, ps: (String, Boolean)*): TagMod =
    classSetImpl(cls := a, ps)

  final def classSetM(ps: Map[String, Boolean]): TagMod =
    classSetImpl(TagMod.empty, ps.toSeq)

  final def classSet1M(a: String, ps: Map[String, Boolean]): TagMod =
    classSetImpl(cls := a, ps.toSeq)

  /**
    * The visible width of the text control, in average character widths. If it
    * is specified, it must be a positive integer. If it is not specified, the
    * default value is 20 (HTML5).
    */
  final def cols = VdomAttr("cols")

  /**
    * This attribute gives the value associated with the http-equiv or name
    * attribute, depending of the context.
    */
  final def contentAttr = VdomAttr("content")

  final def contentEditable = VdomAttr("contentEditable")

  final def contextMenu = VdomAttr("contextMenu")

  final def controls = VdomAttr[Boolean]("controls")

  final def coords = VdomAttr("coords")

  final def crossOrigin = VdomAttr("crossOrigin")

  final def dangerouslySetInnerHtml = VdomAttr[InnerHtmlAttr]("dangerouslySetInnerHTML")

  final def dateTime = VdomAttr("dateTime")

  final def default = VdomAttr[Boolean]("default")

  final def defaultValue = VdomAttr("defaultValue")

  final def defer = VdomAttr[Boolean]("defer")

  final def dir = VdomAttr("dir")

  /**
    * This Boolean attribute indicates that the form control is not available for
    * interaction. In particular, the click event will not be dispatched on
    * disabled controls. Also, a disabled control's value isn't submitted with
    * the form.
    *
    * This attribute is ignored if the value of the type attribute is hidden.
    */
  final def disabled = VdomAttr[Boolean]("disabled")

  final def download = VdomAttr("download")

  final def draggable = VdomAttr[Boolean]("draggable")

  final def encType = VdomAttr("encType")

  /**
    * Describes elements which belongs to this one. Used on labels and output
    * elements.
    */
  final def `for` = VdomAttr("htmlFor")
    
  /**
    * Allows association of an input to a non-ancestoral form.
    */
  final def formId = VdomAttr("form")

  final def formAction = VdomAttr("formAction")

  final def formEncType = VdomAttr("formEncType")

  final def formMethod = VdomAttr("formMethod")

  final def formNoValidate = VdomAttr[Boolean]("formNoValidate")

  final def formTarget = VdomAttr("formTarget")

  final def frameBorder = VdomAttr("frameBorder")

  final def headers = VdomAttr("headers")

  final def hidden = VdomAttr[Boolean]("hidden")

  /**
    * For use in &lt;meter&gt; tags.
    *
    * @see https://css-tricks.com/html5-meter-element/
    */
  final def high = VdomAttr("high")

  /**
    * This is the single required attribute for anchors defining a hypertext
    * source link. It indicates the link target, either a URL or a URL fragment.
    * A URL fragment is a name preceded by a hash mark (#), which specifies an
    * internal target location (an ID) within the current document. URLs are not
    * restricted to Web (HTTP)-based documents. URLs might use any protocol
    * supported by the browser. For example, file, ftp, and mailto work in most
    * user agents.
    */
  final def href = VdomAttr[String]("href")

  final def hrefLang = VdomAttr("hrefLang")

  final def htmlFor = VdomAttr("htmlFor")

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
  final def httpEquiv = VdomAttr("httpEquiv")

  final def icon = VdomAttr("icon")

  /**
    * This attribute defines a unique identifier (ID) which must be unique in
    * the whole document. Its purpose is to identify the element when linking
    * (using a fragment identifier), scripting, or styling (with CSS).
    */
  final def id = VdomAttr("id")

  /**
    * The inputmode attribute tells the browser on devices with dynamic keyboards which keyboard to display. The
    * inputmode attribute applies to the text, search and password input types as well as &lt;textarea&gt;.
    */
  final def inputMode = VdomAttr("inputMode")

  /**
    * http://www.w3.org/TR/2015/CR-SRI-20151112/#the-integrity-attribute
    */
  final def integrity = VdomAttr("integrity")

  final def is = VdomAttr("is")

  final def itemProp = VdomAttr("itemProp")

  final def itemScope = VdomAttr[Boolean]("itemScope")

  final def itemType = VdomAttr("itemType")

  /** React key */
  final val key = VdomAttr.Key

  /** For use in &lt;keygen&gt; */
  final def keyParams = VdomAttr("keyParams")

  /** &lt;keygen&gt;: Specifies the type of key generated. */
  final def keyType = VdomAttr("keyType")

  final def kind = VdomAttr("kind")

  /**
    * This attribute participates in defining the language of the element, the
    * language that non-editable elements are written in or the language that
    * editable elements should be written in. The tag contains one single entry
    * value in the format defines in the Tags for Identifying Languages (BCP47)
    * IETF document. If the tag content is the empty string the language is set
    * to unknown; if the tag content is not valid, regarding to BCP47, it is set
    * to invalid.
    */
  final def lang = VdomAttr[String]("lang")

  final def list = VdomAttr("list")

  final def loop = VdomAttr[Boolean]("loop")

  /**
    * For use in &lt;meter&gt; tags.
    *
    * @see https://css-tricks.com/html5-meter-element/
    */
  final def low = VdomAttr("low")

  final def manifest = VdomAttr("manifest")

  final def marginHeight = VdomAttr("marginHeight")

  final def marginWidth = VdomAttr("marginWidth")

  /**
    * For use in &lt;meter&gt; tags.
    *
    * @see https://css-tricks.com/html5-meter-element/
    */
  final def max = VdomAttr("max")

  final def maxLength = VdomAttr[Int]("maxLength")

  /**
    * This attribute specifies the media which the linked resource applies to.
    * Its value must be a media query. This attribute is mainly useful when
    * linking to external stylesheets by allowing the user agent to pick
    * the best adapted one for the device it runs on.
    *
    * @see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/link#attr-media
    */
  final def media = VdomAttr("media")

  final def mediaGroup = VdomAttr("mediaGroup")

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
  final def method = VdomAttr("method")

  /**
    * For use in &lt;meter&gt; tags.
    *
    * @see https://css-tricks.com/html5-meter-element/
    */
  final def min = VdomAttr("min")

  final def minLength = VdomAttr[Int]("minLength")

  final def multiple = VdomAttr[Boolean]("multiple")

  final def muted = VdomAttr[Boolean]("muted")

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
  final def name = VdomAttr[String]("name")

  final def noModule = VdomAttr[Boolean]("noModule")

  final def noValidate = VdomAttr[Boolean]("noValidate")

  /** For &lt;script&gt; and &lt;style&gt;elements. */
  final def nonce = VdomAttr("nonce")

  /** 'on' attribute for amp tags.
    *
    * The on attribute is used to install event handlers on elements. The events that are supported depend on the element.
    *
    * The value for the syntax is a simple domain specific language of the form:
    *
    * {{{
    *   eventName:targetId[.methodName[(arg1=value, arg2=value)]]
    * }}}
    *
    * @see https://www.ampproject.org/docs/reference/spec#on
    */
  final def on = VdomAttr("on")

  final def onAbort = Attr.Event.base("onAbort")

  final def onAbortCapture = Attr.Event.base("onAbortCapture")

  final def onAnimationEnd = Attr.Event.animation("onAnimationEnd")

  final def onAnimationEndCapture = Attr.Event.animation("onAnimationEndCapture")

  final def onAnimationIteration = Attr.Event.animation("onAnimationIteration")

  final def onAnimationIterationCapture = Attr.Event.animation("onAnimationIterationCapture")

  final def onAnimationStart = Attr.Event.animation("onAnimationStart")

  final def onAnimationStartCapture = Attr.Event.animation("onAnimationStartCapture")

  final def onAuxClick = Attr.Event.mouse("onAuxClick")

  final def onAuxClickCapture = Attr.Event("onAuxClickCapture")

  final def onBeforeInput = Attr.Event.base("onBeforeInput")

  /**
    * The blur event is raised when an element loses focus.
    */
  final def onBlur = Attr.Event.focus("onBlur")

  final def onBlurCapture = Attr.Event.focus("onBlurCapture")

  final def onCanPlay = Attr.Event.base("onCanPlay")

  final def onCanPlayCapture = Attr.Event.base("onCanPlayCapture")

  final def onCanPlayThrough = Attr.Event.base("onCanPlayThrough")

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

  final val onClickCapture = Attr.Event.mouse("onClickCapture")

  final def onCompositionEnd = Attr.Event.composition("onCompositionEnd")

  final def onCompositionStart = Attr.Event.composition("onCompositionStart")

  final def onCompositionUpdate = Attr.Event.composition("onCompositionUpdate")

  final def onContextMenu = Attr.Event.mouse("onContextMenu")

  final def onContextMenuCapture = Attr.Event.mouse("onContextMenuCapture")

  final def onCopy = Attr.Event.clipboard("onCopy")

  final def onCopyCapture = Attr.Event.clipboard("onCopyCapture")

  final def onCut = Attr.Event.clipboard("onCut")

  final def onCutCapture = Attr.Event.clipboard("onCutCapture")

  /** React alias for [[onDoubleClick]] */
  final def onDblClick = onDoubleClick

  /**
    * The dblclick event is fired when a pointing device button (usually a
    * mouse button) is clicked twice on a single element.
    */
  final def onDoubleClick = Attr.Event.mouse("onDoubleClick")

  final def onDoubleClickCapture = Attr.Event.mouse("onDoubleClickCapture")

  final def onDrag = Attr.Event.drag("onDrag")

  final def onDragCapture = Attr.Event.drag("onDragCapture")

  final def onDragEnd = Attr.Event.drag("onDragEnd")

  final def onDragEndCapture = Attr.Event.drag("onDragEndCapture")

  final def onDragEnter = Attr.Event.drag("onDragEnter")

  final def onDragEnterCapture = Attr.Event.drag("onDragEnterCapture")

  final def onDragExit = Attr.Event.drag("onDragExit")

  final def onDragExitCapture = Attr.Event.drag("onDragExitCapture")

  final def onDragLeave = Attr.Event.drag("onDragLeave")

  final def onDragLeaveCapture = Attr.Event.drag("onDragLeaveCapture")

  final def onDragOver = Attr.Event.drag("onDragOver")

  final def onDragOverCapture = Attr.Event.drag("onDragOverCapture")

  final def onDragStart = Attr.Event.drag("onDragStart")

  final def onDragStartCapture = Attr.Event.drag("onDragStartCapture")

  final def onDrop = Attr.Event.drag("onDrop")

  final def onDropCapture = Attr.Event.drag("onDropCapture")

  final def onDurationChange = Attr.Event.base("onDurationChange")

  final def onDurationChangeCapture = Attr.Event.base("onDurationChangeCapture")

  final def onEmptied = Attr.Event.base("onEmptied")

  final def onEmptiedCapture = Attr.Event.base("onEmptiedCapture")

  final def onEncrypted = Attr.Event.base("onEncrypted")

  final def onEncryptedCapture = Attr.Event.base("onEncryptedCapture")

  final def onEnded = Attr.Event.base("onEnded")

  final def onEndedCapture = Attr.Event.base("onEndedCapture")

  /**
    * Type: script code
    *
    * This event is sent to an image element when an error occurs loading the image.
    *
    * https://developer.mozilla.org/en-US/docs/Mozilla/Tech/XUL/image#a-onerror
    */
  final def onError = Attr.Event.base("onError")

  final def onErrorCapture = Attr.Event.base("onErrorCapture")

  /**
    * The focus event is raised when the user sets focus on the given element.
    */
  final def onFocus = Attr.Event.focus("onFocus")

  final def onFocusCapture = Attr.Event.focus("onFocusCapture")

  final def onInput = Attr.Event.base("onInput")

  final def onInputCapture = Attr.Event.base("onInputCapture")

  final def onInvalid = Attr.Event.base("onInvalid")

  final def onInvalidCapture = Attr.Event.base("onInvalidCapture")

  /**
    * The keydown event is raised when the user presses a keyboard key.
    */
  final def onKeyDown = Attr.Event.keyboard("onKeyDown")

  final def onKeyDownCapture = Attr.Event.keyboard("onKeyDownCapture")

  /**
    * The keypress event should be raised when the user presses a key on the keyboard.
    * However, not all browsers fire keypress events for certain keys.
    *
    * Webkit-based browsers (Google Chrome and Safari, for example) do not fire keypress events on the arrow keys.
    * Firefox does not fire keypress events on modifier keys like SHIFT.
    */
  final def onKeyPress = Attr.Event.keyboard("onKeyPress")

  final def onKeyPressCapture = Attr.Event.keyboard("onKeyPressCapture")

  /**
    * The keyup event is raised when the user releases a key that's been pressed.
    */
  final def onKeyUp = Attr.Event.keyboard("onKeyUp")

  final def onKeyUpCapture = Attr.Event.keyboard("onKeyUpCapture")

  /**
    * The load event fires at the end of the document loading process. At this
    * point, all of the objects in the document are in the DOM, and all the
    * images and sub-frames have finished loading.
    */
  final def onLoad = Attr.Event.base("onLoad")

  final def onLoadCapture = Attr.Event.base("onLoadCapture")

  final def onLoadStart = Attr.Event.base("onLoadStart")

  final def onLoadStartCapture = Attr.Event.base("onLoadStartCapture")

  final def onLoadedData = Attr.Event.base("onLoadedData")

  final def onLoadedDataCapture = Attr.Event.base("onLoadedDataCapture")

  final def onLoadedMetadata = Attr.Event.base("onLoadedMetadata")

  final def onLoadedMetadataCapture = Attr.Event.base("onLoadedMetadataCapture")

  /**
    * The mousedown event is raised when the user presses the mouse button.
    */
  final def onMouseDown = Attr.Event.mouse("onMouseDown")

  final def onMouseDownCapture = Attr.Event.mouse("onMouseDownCapture")

  /**
    * The mouseenter event is fired when a pointing device (usually a mouse)
    * is moved over the element that has the listener attached.
    */
  final def onMouseEnter = Attr.Event.mouse("onMouseEnter")

  /**
    * The mouseleave event is fired when a pointing device (usually a mouse)
    * is moved off the element that has the listener attached.
    */
  final def onMouseLeave = Attr.Event.mouse("onMouseLeave")

  /**
    * The mousemove event is raised when the user moves the mouse.
    */
  final def onMouseMove = Attr.Event.mouse("onMouseMove")

  final def onMouseMoveCapture = Attr.Event.mouse("onMouseMoveCapture")

  /**
    * The mouseout event is raised when the mouse leaves an element (e.g, when
    * the mouse moves off of an image in the web page, the mouseout event is
    * raised for that image element).
    */
  final def onMouseOut = Attr.Event.mouse("onMouseOut")

  final def onMouseOutCapture = Attr.Event.mouse("onMouseOutCapture")

  /**
    * The mouseover event is raised when the user moves the mouse over a
    * particular element.
    */
  final def onMouseOver = Attr.Event.mouse("onMouseOver")

  final def onMouseOverCapture = Attr.Event.mouse("onMouseOverCapture")

  /**
    * The mouseup event is raised when the user releases the mouse button.
    */
  final def onMouseUp = Attr.Event.mouse("onMouseUp")

  final def onMouseUpCapture = Attr.Event.mouse("onMouseUpCapture")

  final def onPaste = Attr.Event.clipboard("onPaste")

  final def onPasteCapture = Attr.Event.clipboard("onPasteCapture")

  final def onPause = Attr.Event.base("onPause")

  final def onPauseCapture = Attr.Event.base("onPauseCapture")

  final def onPlay = Attr.Event.base("onPlay")

  final def onPlayCapture = Attr.Event.base("onPlayCapture")

  final def onPlaying = Attr.Event.base("onPlaying")

  final def onPlayingCapture = Attr.Event.base("onPlayingCapture")

  /** A user agent MUST fire a pointer event named gotpointercapture when an element receives pointer capture. This event is fired at the element that is receiving pointer capture. Subsequent events for that pointer will be fired at this element.
    *
    * @since 1.3.0 / React 16.4.0
    */
  final def onGotPointerCapture = Attr.Event.pointer("onGotPointerCapture")

  /** A user agent MUST fire a pointer event named lostpointercapture after pointer capture is released for a pointer. This event MUST be fired prior to any subsequent events for the pointer after capture was released. This event is fired at the element from which pointer capture was removed. Subsequent events for the pointer follow normal hit testing mechanisms (out of scope for this specification) for determining the event target.
    *
    * @since 1.3.0 / React 16.4.0
    */
  final def onLostPointerCapture = Attr.Event.pointer("onLostPointerCapture")

  /** A user agent MUST fire a pointer event named pointercancel in the following circumstances:
    *
    *   - The user agent has determined that a pointer is unlikely to continue to produce events (for example, because of a hardware event).
    *
    *   - After having fired the pointerdown event, if the pointer is subsequently used to manipulate the page viewport (e.g. panning or zooming).
    *     NOTE: User agents can trigger panning or zooming through multiple pointer types (such as touch and pen), and therefore the start of a pan or zoom action may result in the cancellation of various pointers, including pointers with different pointer types.
    *
    *   - Immediately before drag operation starts [HTML], for the pointer that caused the drag operation.
    *     NOTE: If the start of the drag operation is prevented through any means (e.g. through calling preventDefault on the dragstart event) there will be no pointercancel event.
    *
    * After firing the pointercancel event, a user agent MUST also fire a pointer event named pointerout followed by firing a pointer event named pointerleave.
    *
    * NOTE: This section is non-normative. Examples of scenarios in which a user agent might determine that a pointer is unlikely to continue to produce events include:
    *   - A device's screen orientation is changed while a pointer is active.
    *   - The user inputs a greater number of simultaneous pointers than is supported by the device.
    *   - The user agent interprets the input as accidental (for example, the hardware supports palm rejection).
    *   - The user agent interprets the input as a pan or zoom gesture.
    * Methods for changing the device's screen orientation, recognizing accidental input, or using a pointer to manipulate the viewport (e.g. panning or zooming) are out of scope for this specification.
    *
    * @since 1.3.0 / React 16.4.0
    */
  final def onPointerCancel = Attr.Event.pointer("onPointerCancel")

  /** A user agent MUST fire a pointer event named pointerdown when a pointer enters the active buttons state. For mouse, this is when the device transitions from no buttons depressed to at least one button depressed. For touch, this is when physical contact is made with the digitizer. For pen, this is when the pen either makes physical contact with the digitizer without any button depressed, or transitions from no buttons depressed to at least one button depressed while hovering.
    *
    * NOTE: For mouse (or other multi-button pointer devices), this means pointerdown and pointerup are not fired for all of the same circumstances as mousedown and mouseup. See chorded buttons for more information.
    *
    * For input devices that do not support hover, a user agent MUST also fire a pointer event named pointerover followed by a pointer event named pointerenter prior to dispatching the pointerdown event.
    *
    * NOTE: Authors can prevent the firing of certain compatibility mouse events by canceling the pointerdown event (if the isPrimary property is true). This sets the PREVENT MOUSE EVENT flag on the pointer. Note, however, that this does not prevent the mouseover, mouseenter, mouseout, or mouseleave events from firing.
    *
    * @since 1.3.0 / React 16.4.0
    */
  final def onPointerDown = Attr.Event.pointer("onPointerDown")

  /** A user agent MUST fire a pointer event named pointerenter when a pointing device is moved into the hit test boundaries of an element or one of its descendants, including as a result of a pointerdown event from a device that does not support hover (see pointerdown). Note that setPointerCapture or releasePointerCapture might have changed the hit test target and while a pointer is captured it is considered to be always inside the boundaries of the capturing element for the purpose of firing boundary events. This event type is similar to pointerover, but differs in that it does not bubble.
    *
    * @since 1.3.0 / React 16.4.0
    */
  final def onPointerEnter = Attr.Event.pointer("onPointerEnter")

  /** A user agent MUST fire a pointer event named pointerleave when a pointing device is moved out of the hit test boundaries of an element and all of its descendants, including as a result of a pointerup and pointercancel events from a device that does not support hover (see pointerup and pointercancel). Note that setPointerCapture or releasePointerCapture might have changed the hit test target and while a pointer is captured it is considered to be always inside the boundaries of the capturing element for the purpose of firing boundary events. User agents MUST also fire a pointer event named pointerleave when a pen stylus leaves hover range detectable by the digitizer. This event type is similar to pointerout, but differs in that it does not bubble and that it MUST not be fired until the pointing device has left the boundaries of the element and the boundaries of all of its descendants.
    *
    * NOTE: There are similarities between this event type, the mouseleave event described in [UIEVENTS], and the CSS :hover pseudo-class described in [CSS21]. See also the pointerenter event.
    *
    * @since 1.3.0 / React 16.4.0
    */
  final def onPointerLeave = Attr.Event.pointer("onPointerLeave")

  /** A user agent MUST fire a pointer event named pointermove when a pointer changes coordinates. Additionally, when a pointer changes button state, pressure, tangential pressure, tilt, twist, or contact geometry (e.g. width and height) and the circumstances produce no other pointer events defined in this specification then a user agent MUST fire a pointer event named pointermove.
    *
    * @since 1.3.0 / React 16.4.0
    */
  final def onPointerMove = Attr.Event.pointer("onPointerMove")

  /** A user agent MUST fire a pointer event named pointerout when any of the following occurs:
    *
    *   - A pointing device is moved out of the hit test boundaries of an element. Note that setPointerCapture or releasePointerCapture might have changed the hit test target and while a pointer is captured it is considered to be always inside the boundaries of the capturing element for the purpose of firing boundary events.
    *   - After firing the pointerup event for a device that does not support hover (see pointerup).
    *   - After firing the pointercancel event (see pointercancel).
    *   - When a pen stylus leaves the hover range detectable by the digitizer.
    *
    * @since 1.3.0 / React 16.4.0
    */
  final def onPointerOut = Attr.Event.pointer("onPointerOut")

  /** A user agent MUST fire a pointer event named pointerover when a pointing device is moved into the hit test boundaries of an element. Note that setPointerCapture or releasePointerCapture might have changed the hit test target and while a pointer is captured it is considered to be always inside the boundaries of the capturing element for the purpose of firing boundary events. A user agent MUST also fire this event prior to firing a pointerdown event for devices that do not support hover (see pointerdown).
    *
    * @since 1.3.0 / React 16.4.0
    */
  final def onPointerOver = Attr.Event.pointer("onPointerOver")

  /** A user agent MUST fire a pointer event named pointerup when a pointer leaves the active buttons state. For mouse, this is when the device transitions from at least one button depressed to no buttons depressed. For touch, this is when physical contact is removed from the digitizer. For pen, this is when the pen is removed from the physical contact with the digitizer while no button is depressed, or transitions from at least one button depressed to no buttons depressed while hovering.
    *
    * For input devices that do not support hover, a user agent MUST also fire a pointer event named pointerout followed by a pointer event named pointerleave after dispatching the pointerup event.
    *
    * NOTE: For mouse (or other multi-button pointer devices), this means pointerdown and pointerup are not fired for all of the same circumstances as mousedown and mouseup. See chorded buttons for more information.
    *
    * @since 1.3.0 / React 16.4.0
    */
  final def onPointerUp = Attr.Event.pointer("onPointerUp")

  final def onProgress = Attr.Event.base("onProgress")

  final def onProgressCapture = Attr.Event.base("onProgressCapture")

  final def onRateChange = Attr.Event.base("onRateChange")

  final def onRateChangeCapture = Attr.Event.base("onRateChangeCapture")

  /**
    * The reset event is fired when a form is reset.
    */
  final def onReset = Attr.Event.base("onReset")

  final def onResetCapture = Attr.Event.base("onResetCapture")

  /**
    * Specifies the function to be called when the window is scrolled.
    */
  final def onScroll = Attr.Event.ui("onScroll")

  final def onScrollCapture = Attr.Event.ui("onScrollCapture")

  final def onSeeked = Attr.Event.base("onSeeked")

  final def onSeekedCapture = Attr.Event.base("onSeekedCapture")

  final def onSeeking = Attr.Event.base("onSeeking")

  final def onSeekingCapture = Attr.Event.base("onSeekingCapture")

  /**
    * The select event only fires when text inside a text input or textarea is
    * selected. The event is fired after the text has been selected.
    */
  final def onSelect = Attr.Event.base("onSelect")

  final def onStalled = Attr.Event.base("onStalled")

  final def onStalledCapture = Attr.Event.base("onStalledCapture")

  /**
    * The submit event is raised when the user clicks a submit button in a form
    * (<input type="submit"/>).
    */
  final def onSubmit = Attr.Event.base("onSubmit")

  final def onSubmitCapture = Attr.Event.base("onSubmitCapture")

  final def onSuspend = Attr.Event.base("onSuspend")

  final def onSuspendCapture = Attr.Event.base("onSuspendCapture")

  final def onTimeUpdate = Attr.Event.base("onTimeUpdate")

  final def onTimeUpdateCapture = Attr.Event.base("onTimeUpdateCapture")

  /**
    * Event indicating that the touch point has been canceled or disrupted.
    *
    * For example, when popup menu is shown.
    */
  final def onTouchCancel = Attr.Event.touch("onTouchCancel")

  final def onTouchCancelCapture = Attr.Event.touch("onTouchCancelCapture")

  /**
    * Event indicating that the touch point does not exist any more.
    *
    * For example, whn you release your finger.
    */
  final def onTouchEnd = Attr.Event.touch("onTouchEnd")

  final def onTouchEndCapture = Attr.Event.touch("onTouchEndCapture")

  /**
    * Event indicating that the touch point has moved along the plane.
    */
  final def onTouchMove = Attr.Event.touch("onTouchMove")

  final def onTouchMoveCapture = Attr.Event.touch("onTouchMoveCapture")

  /**
    * Event indicating that the user has touched the plane.
    */
  final def onTouchStart = Attr.Event.touch("onTouchStart")

  final def onTouchStartCapture = Attr.Event.touch("onTouchStartCapture")

  final def onTransitionEnd = Attr.Event.transition("onTransitionEnd")

  final def onTransitionEndCapture = Attr.Event.transition("onTransitionEndCapture")

  final def onVolumeChange = Attr.Event.base("onVolumeChange")

  final def onVolumeChangeCapture = Attr.Event.base("onVolumeChangeCapture")

  final def onWaiting = Attr.Event.base("onWaiting")

  final def onWaitingCapture = Attr.Event.base("onWaitingCapture")

  final def onWheel = Attr.Event.wheel("onWheel")

  final def onWheelCapture = Attr.Event.wheel("onWheelCapture")

  final def open = VdomAttr[Boolean]("open")

  /**
    * For use in &lt;meter&gt; tags.
    *
    * @see https://css-tricks.com/html5-meter-element/
    */
  final def optimum = VdomAttr("optimum")

  /** The pattern attribute specifies a regular expression against which the control’s value, or, when the multiple
    * attribute applies and is set, the control’s values, are to be checked.
    *
    * @see https://www.w3.org/TR/html5/sec-forms.html#the-pattern-attribute
    */
  final def pattern = VdomAttr[String]("pattern")

  /**
    * A hint to the user of what can be entered in the control. The placeholder
    * text must not contain carriage returns or line-feeds. This attribute
    * applies when the value of the type attribute is text, search, tel, url or
    * email; otherwise it is ignored.
    */
  final def placeholder = VdomAttr("placeholder")

  final def playsInline = VdomAttr[Boolean]("playsInline")

  final def poster = VdomAttr("poster")

  final def preload = VdomAttr("preload")

  final def profile = VdomAttr("profile")

  final def radioGroup = VdomAttr[String]("radioGroup")

  /**
    * This Boolean attribute indicates that the user cannot modify the value of
    * the control. This attribute is ignored if the value of the type attribute
    * is hidden, range, color, checkbox, radio, file, or a button type.
    */
  final def readOnly = VdomAttr[Boolean]("readOnly")

  /**
    * This attribute names a relationship of the linked document to the current
    * document. The attribute must be a space-separated list of the link types
    * values. The most common use of this attribute is to specify a link to an
    * external style sheet: the rel attribute is set to stylesheet, and the href
    * attribute is set to the URL of an external style sheet to format the page.
    *
    */
  final def rel = VdomAttr("rel")

  /**
    * This attribute specifies that the user must fill in a value before
    * submitting a form. It cannot be used when the type attribute is hidden,
    * image, or a button type (submit, reset, or button). The :optional and
    * :required CSS pseudo-classes will be applied to the field as appropriate.
    */
  final def required = VdomAttr[Boolean]("required")

  final def results = VdomAttr("results")

  /** For &lt;ol&gt; elements. */
  final def reversed = VdomAttr[Boolean]("reversed")

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
  final def role = VdomAttr[String]("role")

  final def rowSpan = VdomAttr[Int]("rowSpan")

  /**
    * The number of visible text lines for the control.
    */
  final def rows = VdomAttr[Int]("rows")

  final def sandbox = VdomAttr("sandbox")

  final def scope = VdomAttr("scope")

  /**
    * For use in &lt;style&gt; tags.
    *
    * If this attribute is present, then the style applies only to its parent element.
    * If absent, the style applies to the whole document.
    */
  final def scoped = VdomAttr[Boolean]("scoped")

  final def scrolling = VdomAttr("scrolling")

  final def seamless = VdomAttr[Boolean]("seamless")

  final def security = VdomAttr("security")

  final def selected = VdomAttr[Boolean]("selected")

  final def shape = VdomAttr("shape")

  /**
    * The initial size of the control. This value is in pixels unless the value
    * of the type attribute is text or password, in which case, it is an integer
    * number of characters. Starting in HTML5, this attribute applies only when
    * the type attribute is set to text, search, tel, url, email, or password;
    * otherwise it is ignored. In addition, the size must be greater than zero.
    * If you don't specify a size, a default value of 20 is used.
    */
  final def size = VdomAttr[Int]("size")

  final def sizes = VdomAttr("sizes")

  /**
    * This enumerated attribute defines whether the element may be checked for
    * spelling errors.
    */
  final def spellCheck = VdomAttr("spellCheck")

  /**
    * If the value of the type attribute is image, this attribute specifies a URI
    * for the location of an image to display on the graphical submit button;
    * otherwise it is ignored.
    */
  final val src = VdomAttr[String]("src")

  final def srcDoc = VdomAttr("srcDoc")

  final def srcLang = VdomAttr("srcLang")

  final def srcSet = VdomAttr("srcSet")

  final def step = VdomAttr("step")

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
  final def summaryAttr = VdomAttr("summary")

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
  final def tabIndex = VdomAttr[Int]("tabIndex")

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
  final val title = VdomAttr[String]("title")

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
  final val `type` = VdomAttr("type")

  /** IE-specific property to prevent user selection */
  final def unselectable = VdomAttr("unselectable")

  final def untypedRef = VdomAttr.Ref

  final def useMap = VdomAttr("useMap")

  /**
    * The initial value of the control. This attribute is optional except when
    * the value of the type attribute is radio or checkbox.
    */
  final val value = VdomAttr("value")

  final def wmode = VdomAttr("wmode")

  /** &lt;textarea&gt;: Indicates whether the text should be wrapped. */
  object wrap extends VdomAttr.Generic("wrap") {
    def soft = this := "soft"
    def hard = this := "hard"
  }

  final def xmlns = VdomAttr("xmlns")
}
