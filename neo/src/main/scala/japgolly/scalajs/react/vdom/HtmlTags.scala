package japgolly.scalajs.react.vdom

/*

import org.scalajs.dom.{html => *}
import NamespaceHtml._
import Scalatags._

// TODO Make ReactTags a case class (String) AnyVal then use def instead of lazy val

final class HtmlTag(val name: String) extends AnyVal {

}

trait HtmlTags {

  /**
    * Represents a hyperlink, linking to another resource.
    */
  final val a = "a".tag[*.Anchor]

  /**
    * An abbreviation or acronym; the expansion of the abbreviation can be
    * represented in the title attribute.
    */
  final lazy val abbr = "abbr".tag[*.Element]

  /**
    * Defines a section containing contact information.
    */
  final lazy val address = "address".tag[*.Element]

  /**
    * In conjunction with map, defines an image map
    */
  final lazy val area = "area".voidTag[*.Area]

  /**
    * Defines self-contained content that could exist independently of the rest
    * of the content.
    */
  final lazy val article = "article".tag[*.Element]

  /**
    * Defines some content loosely related to the page content. If it is removed,
    * the remaining content still makes sense.
    */
  final lazy val aside = "aside".tag[*.Element]

  /**
    * Represents a sound or an audio stream.
    */
  final lazy val audio = "audio".tag[*.Audio]

  /**
    * Bold text.
    */
  final lazy val b = "b".tag[*.Element]

  /**
    * Defines the base URL for relative URLs in the page.
    */
  final lazy val base = "base".voidTag[*.Base]

  /**
    * Represents text that must be isolated from its surrounding for bidirectional
    * text formatting. It allows embedding a span of text with a different, or
    * unknown, directionality.
    */
  final lazy val bdi = "bdi".tag[*.Element]

  /**
    * Represents the directionality of its children, in order to explicitly
    * override the Unicode bidirectional algorithm.
    */
  final lazy val bdo = "bdo".tag[*.Element]

  /**
    * Represents a content that is quoted from another source.
    */
  final lazy val blockquote = "blockquote".tag[*.Quote]

  /**
    * Represents the content of an HTML document. There is only one body
    * element in a document.
    */
  final lazy val body = "body".tag[*.Body]

  /**
    * Represents a line break.
    */
  final val br = "br".voidTag[*.BR]

  final val button = "button".tag[*.Button]

  /**
    * Represents a bitmap area that scripts can use to render graphics like graphs,
    * games or any visual images on the fly.
    */
  final lazy val canvas = "canvas".tag[*.Canvas]

  /**
    * The title of a table.
    */
  final lazy val caption = "caption".tag[*.TableCaption]

  /**
    * Represents the title of a work being cited.
    */
  final lazy val cite = "cite".tag[*.Element]

  /**
    * Represents computer code.
    */
  final lazy val code = "code".tag[*.Element]

  /**
    * A single column.
    */
  final lazy val col = "col".voidTag[*.TableCol]

  /**
    * A set of columns.
    */
  final lazy val colgroup = "colgroup".tag[*.TableCol]

  /**
    * A command that the user can invoke.
    */
  final lazy val command = "command".voidTag[*.Element]

  /**
    * Associates to its content a machine-readable equivalent.
    */
  final lazy val data = "data".tag[*.Element]

  /**
    * A set of predefined options for other controls.
    */
  final lazy val datalist = "datalist".tag[*.DataList]

  /**
    * Represents the definition of the terms immediately listed before it.
    */
  final lazy val dd = "dd".tag[*.DD]

  /**
    * Defines a remofinal lazy val from the document.
    */
  final lazy val del = "del".tag[*.Mod]

  /**
    * A widget from which the user can obtain additional information
    * or controls.
    */
  final lazy val details = "details".tag[*.Element]

  /**
    * Represents a term whose definition is contained in its nearest ancestor
    * content.
    */
  final lazy val dfn = "dfn".tag[*.Element]

  /**
    * Represents a generic container with no special meaning.
    */
  final val div = "div".tag[*.Div]

  /**
    * Defines a definition list; a list of terms and their associated definitions.
    */
  final lazy val dl = "dl".tag[*.DList]

  /**
    * Represents a term defined by the next dd
    */
  final lazy val dt = "dt".tag[*.DT]

  /**
    * Represents emphasized text.
    */
  final lazy val em = "em".tag[*.Element]

  /**
    * Represents a integration point for an external, often non-HTML, application
    * or interactive content.
    */
  final lazy val embed = "embed".voidTag[*.Embed]

  /**
    * A set of fields.
    */
  final lazy val fieldset = "fieldset".tag[*.FieldSet]

  /**
    * Represents the legend of a figure.
    */
  final lazy val figcaption = "figcaption".tag[*.Element]

  /**
    * Represents a figure illustrated as part of the document.
    */
  final lazy val figure = "figure".tag[*.Element]

  /**
    * Defines the footer for a page or section. It often contains a copyright
    * notice, some links to legal information, or addresses to give feedback.
    */
  final lazy val footer = "footer".tag[*.Element]

  /**
    * Represents a form, consisting of controls, that can be submitted to a
    * server for processing.
    */
  final lazy val form = "form".tag[*.Form]

  /**
    * Heading level 1
    */
  final lazy val h1 = "h1".tag[*.Heading]

  /**
    * Heading level 2
    */
  final lazy val h2 = "h2".tag[*.Heading]

  /**
    * Heading level 3
    */
  final lazy val h3 = "h3".tag[*.Heading]

  /**
    * Heading level 4
    */
  final lazy val h4 = "h4".tag[*.Heading]

  /**
    * Heading level 5
    */
  final lazy val h5 = "h5".tag[*.Heading]

  /**
    * Heading level 6
    */
  final lazy val h6 = "h6".tag[*.Heading]

  /**
    * Represents a collection of metadata about the document, including links to,
    * or definitions of, scripts and style sheets.
    */
  final lazy val head = "head".tag[*.Head]

  /**
    * Defines the header of a page or section. It often contains a logo, the
    * title of the Web site, and a navigational table of content.
    */
  final lazy val header = "header".tag[*.Element]

  /**
    * Represents a thematic break between paragraphs of a section or article or
    * any longer content.
    */
  final lazy val hr = "hr".voidTag[*.HR]

  /**
    * Represents the root of an HTML or XHTML document. All other elements must
    * be descendants of this element.
    */
  final lazy val html = "html".tag[*.Html]

  /**
    * Italicized text.
    */
  final lazy val i = "i".tag[*.Element]

  /**
    * Represents a nested browsing context, that is an embedded HTML document.
    */
  final lazy val iframe = "iframe".tag[*.IFrame]

  /**
    * Represents an image.
    */
  final val img = "img".voidTag[*.Image]

  /**
    * The HTML element &lt;input&gt; is used to create interactive controls for web-based forms in order to accept data
    * from the user. How an &lt;input&gt; works varies considerably depending on the value of its type attribute.
    */
  object input extends ReactTagOf[*.Input]("input", Nil, implicitly) {

    /** A check box. You must use the value attribute to define the value submitted by this item. Use the checked attribute to indicate whether this item is selected. You can also use the indeterminate attribute to indicate that the checkbox is in an indeterminate state (on most platforms, this draws a horizontal line across the checkbox). */
    lazy val checkbox = this withType "checkbox"

    /** A single-line text field; line-breaks are automatically removed from the input value. */
    val text = this withType "text"
    private[this] val `type` = "type".attr

    /** A push button with no default behavior. */
    def button = this withType "button"

    /** [HTML5] A control for specifying a color. A color picker's UI has no required features other than accepting simple colors as text (more info). */
    def color = this withType "color"

    /** [HTML5] A control for entering a date (year, month, and day, with no time). */
    def date = this withType "date"

    /** Returns a &lt;input type="{t}" /&gt; */
    def withType(t: String): ReactTagOf[*.Input] =
    this (`type`.:=(t)(ReactAttr.ValueType.string))

    /** [HTML5] A control for entering a date and time (hour, minute, second, and fraction of a second) based on UTC time zone. */
    def datetime = this withType "datetime"

    /** [HTML5] A control for entering a date and time, with no time zone. */
    def datetimeLocal = this withType "datetime-local"

    /** [HTML5] A field for editing an e-mail address. The input value is validated to contain either the empty string or a single valid e-mail address before submitting. The :valid and :invalid CSS pseudo-classes are applied as appropriate. */
    def email = this withType "email"

    /** A control that lets the user select a file. Use the accept attribute to define the types of files that the control can select. */
    def file = this withType "file"

    /** A control that is not displayed, but whose value is submitted to the server. */
    def hidden = this withType "hidden"

    /** A graphical submit button. You must use the src attribute to define the source of the image and the alt attribute to define alternative text. You can use the height and width attributes to define the size of the image in pixels. */
    def image = this withType "image"

    /** [HTML5] A control for entering a month and year, with no time zone. */
    def month = this withType "month"

    /** [HTML5] A control for entering a floating point number. */
    def number = this withType "number"

    /** A single-line text field whose value is obscured. Use the maxlength attribute to specify the maximum length of the value that can be entered. */
    def password = this withType "password"

    /** A radio button. You must use the value attribute to define the value submitted by this item. Use the checked attribute to indicate whether this item is selected by default. Radio buttons that have the same value for the name attribute are in the same "radio button group"; only one radio button in a group can be selected at a time. */
    def radio = this withType "radio"

    /** [HTML5] A control for entering a number whose exact value is not important. This type control uses the following default values if the corresponding attributes are not specified: */
    def range = this withType "range"

    /** A button that resets the contents of the form to default values. */
    def reset = this withType "reset"

    /** [HTML5] A single-line text field for entering search strings; line-breaks are automatically removed from the input value. */
    def search = this withType "search"

    /** A button that submits the form. */
    def submit = this withType "submit"

    /** [HTML5] A control for entering a telephone number; line-breaks are automatically removed from the input value, but no other syntax is enforced. You can use attributes such as pattern and maxlength to restrict values entered in the control. The :valid and :invalid CSS pseudo-classes are applied as appropriate. */
    def tel = this withType "tel"

    /** [HTML5] A control for entering a time value with no time zone. */
    def time = this withType "time"

    /** [HTML5] A field for editing a URL. The input value is validated to contain either the empty string or a valid absolute URL before submitting. Line-breaks and leading or trailing whitespace are automatically removed from the input value. You can use attributes such as pattern and maxlength to restrict values entered in the control. The :valid and :invalid CSS pseudo-classes are applied as appropriate. */
    def url = this withType "url"

    /** [HTML5] A control for entering a date consisting of a week-year number and a week number with no time zone. */
    def week = this withType "week"
  }

  /**
    * Defines an addition to the document.
    */
  final lazy val ins = "ins".tag[*.Mod]

  /**
    * Represents user input, often from a keyboard, but not necessarily.
    */
  final lazy val kbd = "kbd".tag[*.Element]

  /**
    * A key-pair generator control.
    */
  final lazy val keygen = "keygen".voidTag[*.Element]

  /**
    * The caption of a single field
    */
  final lazy val label = "label".tag[*.Label]

  /**
    * The caption for a fieldset.
    */
  final lazy val legend = "legend".tag[*.Legend]

  /**
    * Defines an item of an list.
    */
  final val li = "li".tag[*.LI]

  /**
    * Used to link JavaScript and external CSS with the current HTML document.
    */
  final lazy val link = "link".voidTag[*.Link]

  /**
    * Defines the main or important content in the document. There is only one
    * main element in the document.
    */
  final lazy val main = "main".tag[*.Element]

  /**
    * In conjunction with area, defines an image map.
    */
  final lazy val map = "map".tag[*.Map]

  /**
    * Represents text highlighted for reference purposes, that is for its
    * relevance in another context.
    */
  final lazy val mark = "mark".tag[*.Element]

  /**
    * Defines a mathematical formula.
    */
  final lazy val math = "math".tag[*.Element]

  /**
    * A list of commands
    */
  final lazy val menu = "menu".tag[*.Menu]

  /**
    * Defines metadata that can't be defined using another HTML element.
    */
  final lazy val meta = "meta".voidTag[*.Meta]

  /**
    * A scalar measurement within a known range.
    */
  final lazy val meter = "meter".tag[*.Element]

  /**
    * Represents a section of a page that links to other pages or to parts within
    * the page: a section with navigation links.
    */
  final lazy val nav = "nav".tag[*.Element]

  /**
    * Defines alternative content to display when the browser doesn't support
    * scripting.
    */
  final lazy val noscript = "noscript".tag[*.Element]

  /**
    * Represents an external resource, which is treated as an image, an HTML
    * sub-document, or an external resource to be processed by a plug-in.
    */
  final lazy val `object` = "object".tag[*.Object]

  /**
    * Defines an ordered list of items.
    */
  final val ol = "ol".tag[*.OList]

  /**
    * A set of options, logically grouped.
    */
  final lazy val optgroup = "optgroup".tag[*.OptGroup]

  /**
    * An option in a select element.
    */
  final lazy val option = "option".tag[*.Option]

  /**
    * The result of a calculation
    */
  final lazy val output = "output".tag[*.Element]

  /**
    * Defines a portion that should be displayed as a paragraph.
    */
  final val p = "p".tag[*.Paragraph]

  /**
    * Defines parameters for use by plug-ins invoked by object elements.
    */
  final lazy val param = "param".voidTag[*.Param]

  /**
    * Indicates that its content is preformatted and that this format must be
    * preserved.
    */
  final lazy val pre = "pre".tag[*.Pre]

  /**
    * A progress completion bar
    */
  final lazy val progress = "progress".tag[*.Progress]

  /**
    * An inline quotation.
    */
  final lazy val q = "q".tag[*.Quote]

  /**
    * Represents parenthesis around a ruby annotation, used to display the
    * annotation in an alternate way by browsers not supporting the standard
    * display for annotations.
    */
  final lazy val rp = "rp".tag[*.Element]

  /**
    * Represents the text of a ruby annotation.
    */
  final lazy val rt = "rt".tag[*.Element]

  /**
    * Represents content to be marked with ruby annotations, short runs of text
    * presented alongside the text. This is often used in conjunction with East
    * Asian language where the annotations act as a guide for pronunciation, like
    * the Japanese furigana .
    */
  final lazy val ruby = "ruby".tag[*.Element]

  /**
    * Strikethrough element, used for that is no longer accurate or relevant.
    */
  final lazy val s = "s".tag[*.Element]

  /**
    * Represents the output of a program or a computer.
    */
  final lazy val samp = "samp".tag[*.Element]

  /**
    * Defines either an internal script or a link to an external script. The
    * script language is JavaScript.
    */
  final lazy val script = "script".tag[*.Script]

  /**
    * Represents a generic section of a document, i.e., a thematic grouping of
    * content, typically with a heading.
    */
  final lazy val section = "section".tag[*.Element]

  /**
    * A control that allows the user to select one of a set of options.
    */
  final lazy val select = "select".tag[*.Select]

  /**
    * Represents a side comment; text like a disclaimer or copyright, which is not
    * essential to the comprehension of the document.
    */
  final lazy val small = "small".tag[*.Element]

  /**
    * Allows the authors to specify alternate media resources for media elements
    * like video or audio
    */
  final lazy val source = "source".voidTag[*.Source]

  /**
    * Represents text with no specific meaning. This has to be used when no other
    * text-semantic element conveys an adequate meaning, which, in this case, is
    * often brought by global attributes like class, lang, or dir.
    */
  final val span = "span".tag[*.Span]

  /**
    * Represents especially important text.
    */
  final lazy val strong = "strong".tag[*.Element]

  /**
    * Used to write inline CSS.
    */
  final lazy val styleTag = "style".tag[*.Style]

  /**
    * Subscript tag
    */
  final lazy val sub = "sub".tag[*.Element]

  /**
    * A summary, caption, or legend for a given details.
    */
  final lazy val summary = "summary".tag[*.Element]

  /**
    * Superscript tag.
    */
  final lazy val sup = "sup".tag[*.Element]

  /**
    * Represents data with more than one dimension.
    */
  final val table = "table".tag[*.Table]

  /**
    * The table body.
    */
  final val tbody = "tbody".tag[*.TableSection]

  /**
    * A single cell in a table.
    */
  final val td = "td".tag[*.TableCell]

  /**
    * A multiline text edit control.
    */
  final val textarea = "textarea".tag[*.TextArea]

  /**
    * The table footer.
    */
  final lazy val tfoot = "tfoot".tag[*.TableSection]

  /**
    * A header cell in a table.
    */
  final val th = "th".tag[*.TableHeaderCell]

  /**
    * The table headers.
    */
  final lazy val thead = "thead".tag[*.TableSection]

  /**
    * Represents a date and time value; the machine-readable equivalent can be
    * represented in the datetime attribetu
    */
  final lazy val time = "time".tag[*.Element]

  /**
    * Defines the title of the document, shown in a browser's title bar or on the
    * page's tab. It can only contain text and any contained tags are not
    * interpreted.
    */
  final lazy val titleTag = "title".tag[*.Title]

  /**
    * A single row in a table.
    */
  final val tr = "tr".tag[*.TableRow]

  /**
    * Allows authors to specify timed text track for media elements like video or
    * audio
    */
  final lazy val track = "track".voidTag[*.Track]

  /**
    * Underlined text.
    */
  final lazy val u = "u".tag[*.Element]

  /**
    * Defines an unordered list of items.
    */
  final val ul = "ul".tag[*.UList]

  /**
    * Represents a variable.
    */
  final lazy val `var` = "var".tag[*.Element]

  /**
    * Represents a line break opportunity, that is a suggested point for wrapping
    * text in order to improve readability of text split on several lines.
    */
  final lazy val wbr = "wbr".voidTag[*.Element]

  /**
    * Represents a video, and its associated audio files and captions, with the
    * necessary interface to play it.
    */
  final lazy val video = "video".tag[*.Video]
}

 */

