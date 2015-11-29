package japgolly.scalajs.react.vdom

import org.scalajs.dom.{html => *}
import Scalatags._
import NamespaceHtml._

trait HtmlTags {

  // Root Element
  /**
   * Represents the root of an HTML or XHTML document. All other elements must
   * be descendants of this element.
   *
   * MDN
   */
  final val html = "html".tag[*.Html]

  // Document Metadata
  /**
   * Represents a collection of metadata about the document, including links to,
   * or definitions of, scripts and style sheets.
   *
   * MDN
   */
  final val head = "head".tag[*.Head]

  /**
   * Defines the base URL for relative URLs in the page.
   *
   * MDN
   */
  final val base = "base".voidTag[*.Base]
  /**
   * Used to link JavaScript and external CSS with the current HTML document.
   *
   * MDN
   */
  final val link = "link".voidTag[*.Link]
  /**
   * Defines metadata that can't be defined using another HTML element.
   *
   * MDN
   */
  final val meta = "meta".voidTag[*.Meta]


  // Scripting
  /**
   * Defines either an internal script or a link to an external script. The
   * script language is JavaScript.
   *
   * MDN
   */
  final val script = "script".tag[*.Script]


  // Sections
  /**
   * Represents the content of an HTML document. There is only one body
   * element in a document.
   *
   * MDN
   */
  final val body = "body".tag[*.Body]

  /**
   * Heading level 1
   *
   * MDN
   */
  final val h1 = "h1".tag[*.Heading]
  /**
   * Heading level 2
   *
   * MDN
   */
  final val h2 = "h2".tag[*.Heading]
  /**
   * Heading level 3
   *
   * MDN
   */
  final val h3 = "h3".tag[*.Heading]
  /**
   * Heading level 4
   *
   * MDN
   */
  final val h4 = "h4".tag[*.Heading]
  /**
   * Heading level 5
   *
   * MDN
   */
  final val h5 = "h5".tag[*.Heading]
  /**
   * Heading level 6
   *
   * MDN
   */
  final val h6 = "h6".tag[*.Heading]
  /**
   * Defines the header of a page or section. It often contains a logo, the
   * title of the Web site, and a navigational table of content.
   *
   * MDN
   */
  final val header = "header".tag[*.Element]
  /**
   * Defines the footer for a page or section. It often contains a copyright
   * notice, some links to legal information, or addresses to give feedback.
   *
   * MDN
   */
  final val footer = "footer".tag[*.Element]


  // Grouping content
  /**
   * Defines a portion that should be displayed as a paragraph.
   *
   * MDN
   */
  final val p = "p".tag[*.Paragraph]
  /**
   * Represents a thematic break between paragraphs of a section or article or
   * any longer content.
   *
   * MDN
   */
  final val hr = "hr".voidTag[*.HR]
  /**
   * Indicates that its content is preformatted and that this format must be
   * preserved.
   *
   * MDN
   */
  final val pre = "pre".tag[*.Pre]
  /**
   * Represents a content that is quoted from another source.
   *
   * MDN
   */
  final val blockquote = "blockquote".tag[*.Quote]
  /**
   * Defines an ordered list of items.
   *
   * MDN
   */
  final val ol = "ol".tag[*.OList]
  /**
   * Defines an unordered list of items.
   *
   * MDN
   */
  final val ul = "ul".tag[*.UList]
  /**
   * Defines an item of an list.
   *
   * MDN
   */
  final val li = "li".tag[*.LI]
  /**
   * Defines a definition list; a list of terms and their associated definitions.
   *
   * MDN
   */
  final val dl = "dl".tag[*.DList]
  /**
   * Represents a term defined by the next dd
   *
   * MDN
   */
  final val dt = "dt".tag[*.DT]
  /**
   * Represents the definition of the terms immediately listed before it.
   *
   * MDN
   */
  final val dd = "dd".tag[*.DD]
  /**
   * Represents a figure illustrated as part of the document.
   *
   * MDN
   */
  final val figure = "figure".tag[*.Element]
  /**
   * Represents the legend of a figure.
   *
   * MDN
   */
  final val figcaption = "figcaption".tag[*.Element]
  /**
   * Represents a generic container with no special meaning.
   *
   * MDN
   */
  final val div = "div".tag[*.Div]

  // Text-level semantics
  /**
   * Represents a hyperlink, linking to another resource.
   *
   * MDN
   */
  final val a = "a".tag[*.Anchor]
  /**
   * Represents emphasized text.
   *
   * MDN
   */
  final val em = "em".tag[*.Element]
  /**
   * Represents especially important text.
   *
   * MDN
   */
  final val strong = "strong".tag[*.Element]
  /**
   * Represents a side comment; text like a disclaimer or copyright, which is not
   * essential to the comprehension of the document.
   *
   * MDN
   */
  final val small = "small".tag[*.Element]
  /**
   * Strikethrough element, used for that is no longer accurate or relevant.
   *
   * MDN
   */
  final val s = "s".tag[*.Element]
  /**
   * Represents the title of a work being cited.
   *
   * MDN
   */
  final val cite = "cite".tag[*.Element]

  /**
   * Represents computer code.
   *
   * MDN
   */
  final val code = "code".tag[*.Element]

  /**
   * Subscript tag
   *
   * MDN
   */
  final val sub = "sub".tag[*.Element]
  /**
   * Superscript tag.
   *
   * MDN
   */
  final val sup = "sup".tag[*.Element]
  /**
   * Italicized text.
   *
   * MDN
   */
  final val i = "i".tag[*.Element]
  /**
   * Bold text.
   *
   * MDN
   */
  final val b = "b".tag[*.Element]
  /**
   * Underlined text.
   *
   * MDN
   */
  final val u = "u".tag[*.Element]

  /**
   * Represents text with no specific meaning. This has to be used when no other
   * text-semantic element conveys an adequate meaning, which, in this case, is
   * often brought by global attributes like class, lang, or dir.
   *
   * MDN
   */
  final val span = "span".tag[*.Span]
  /**
   * Represents a line break.
   *
   * MDN
   */
  final val br = "br".voidTag[*.BR]
  /**
   * Represents a line break opportunity, that is a suggested point for wrapping
   * text in order to improve readability of text split on several lines.
   *
   * MDN
   */
  final val wbr = "wbr".voidTag[*.Element]

  // Edits
  /**
   * Defines an addition to the document.
   *
   * MDN
   */
  final val ins = "ins".tag[*.Mod]
  /**
   * Defines a remofinal val from the document.
   *
   * MDN
   */
  final val del = "del".tag[*.Mod]

  // Embedded content
  /**
   * Represents an image.
   *
   * MDN
   */
  final val img = "img".voidTag[*.Image]
  /**
   * Represents a nested browsing context, that is an embedded HTML document.
   *
   * MDN
   */
  final val iframe = "iframe".tag[*.IFrame]
  /**
   * Represents a integration point for an external, often non-HTML, application
   * or interactive content.
   *
   * MDN
   */
  final val embed = "embed".voidTag[*.Embed]
  /**
   * Represents an external resource, which is treated as an image, an HTML
   * sub-document, or an external resource to be processed by a plug-in.
   *
   * MDN
   */
  final val `object` = "object".tag[*.Object]
  /**
   * Defines parameters for use by plug-ins invoked by object elements.
   *
   * MDN
   */
  final val param = "param".voidTag[*.Param]
  /**
   * Represents a video, and its associated audio files and captions, with the
   * necessary interface to play it.
   *
   * MDN
   */
  final val video = "video".tag[*.Video]
  /**
   * Represents a sound or an audio stream.
   *
   * MDN
   */
  final val audio = "audio".tag[*.Audio]
  /**
   * Allows the authors to specify alternate media resources for media elements
   * like video or audio
   *
   * MDN
   */
  final val source = "source".voidTag[*.Source]
  /**
   * Allows authors to specify timed text track for media elements like video or
   * audio
   *
   * MDN
   */
  final val track = "track".voidTag[*.Track]
  /**
   * Represents a bitmap area that scripts can use to render graphics like graphs,
   * games or any visual images on the fly.
   *
   * MDN
   */
  final val canvas = "canvas".tag[*.Canvas]
  /**
   * In conjunction with area, defines an image map.
   *
   * MDN
   */
  final val map = "map".tag[*.Map]
  /**
   * In conjunction with map, defines an image map
   *
   * MDN
   */
  final val area = "area".voidTag[*.Area]


  // Tabular data
  /**
   * Represents data with more than one dimension.
   *
   * MDN
   */
  final val table = "table".tag[*.Table]
  /**
   * The title of a table.
   *
   * MDN
   */
  final val caption = "caption".tag[*.TableCaption]
  /**
   * A set of columns.
   *
   * MDN
   */
  final val colgroup = "colgroup".tag[*.TableCol]
  /**
   * A single column.
   *
   * MDN
   */
  final val col = "col".voidTag[*.TableCol]
  /**
   * The table body.
   *
   * MDN
   */
  final val tbody = "tbody".tag[*.TableSection]
  /**
   * The table headers.
   *
   * MDN
   */
  final val thead = "thead".tag[*.TableSection]
  /**
   * The table footer.
   *
   * MDN
   */
  final val tfoot = "tfoot".tag[*.TableSection]
  /**
   * A single row in a table.
   *
   * MDN
   */
  final val tr = "tr".tag[*.TableRow]
  /**
   * A single cell in a table.
   *
   * MDN
   */
  final val td = "td".tag[*.TableCell]
  /**
   * A header cell in a table.
   *
   * MDN
   */
  final val th = "th".tag[*.TableHeaderCell]

  // Forms
  /**
   * Represents a form, consisting of controls, that can be submitted to a
   * server for processing.
   *
   * MDN
   */
  final val form = "form".tag[*.Form]
  /**
   * A set of fields.
   *
   * MDN
   */
  final val fieldset = "fieldset".tag[*.FieldSet]
  /**
   * The caption for a fieldset.
   *
   * MDN
   */
  final val legend = "legend".tag[*.Legend]
  /**
   * The caption of a single field
   *
   * MDN
   */
  final val label = "label".tag[*.Label]
  /**
   * A button
   *
   * MDN
   */
  final val button = "button".tag[*.Button]
  /**
   * A control that allows the user to select one of a set of options.
   *
   * MDN
   */
  final val select = "select".tag[*.Select]
  /**
   * A set of predefined options for other controls.
   *
   * MDN
   */
  final val datalist = "datalist".tag[*.DataList]
  /**
   * A set of options, logically grouped.
   *
   * MDN
   */
  final val optgroup = "optgroup".tag[*.OptGroup]
  /**
   * An option in a select element.
   *
   * MDN
   */
  final val option = "option".tag[*.Option]
  /**
   * A multiline text edit control.
   *
   * MDN
   */
  final val textarea = "textarea".tag[*.TextArea]


  // ==== [japgolly] Here begins Scalatags' Tags2 ====

  /**
   * The HTML element &lt;input&gt; is used to create interactive controls for web-based forms in order to accept data
   * from the user. How an &lt;input&gt; works varies considerably depending on the value of its type attribute.
   */
  object input extends ReactTagOf[*.Input]("input", Nil, implicitly) {
    private[this] val `type` = "type".attr

    /** Returns a &lt;input type="{t}" /&gt; */
    def withType(t: String): ReactTagOf[*.Input] =
      this(`type` := t)

    /** A push button with no default behavior. */
    def button = this withType "button"

    /** A check box. You must use the value attribute to define the value submitted by this item. Use the checked attribute to indicate whether this item is selected. You can also use the indeterminate attribute to indicate that the checkbox is in an indeterminate state (on most platforms, this draws a horizontal line across the checkbox). */
    val checkbox = this withType "checkbox"

    /** [HTML5] A control for specifying a color. A color picker's UI has no required features other than accepting simple colors as text (more info). */
    def color = this withType "color"

    /** [HTML5] A control for entering a date (year, month, and day, with no time). */
    def date = this withType "date"

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

    /** A single-line text field; line-breaks are automatically removed from the input value. */
    val text = this withType "text"

    /** [HTML5] A control for entering a time value with no time zone. */
    def time = this withType "time"

    /** [HTML5] A field for editing a URL. The input value is validated to contain either the empty string or a valid absolute URL before submitting. Line-breaks and leading or trailing whitespace are automatically removed from the input value. You can use attributes such as pattern and maxlength to restrict values entered in the control. The :valid and :invalid CSS pseudo-classes are applied as appropriate. */
    def url = this withType "url"

    /** [HTML5] A control for entering a date consisting of a week-year number and a week number with no time zone. */
    def week = this withType "week"
  }

  // Document Metadata
  /**
   * Defines the title of the document, shown in a browser's title bar or on the
   * page's tab. It can only contain text and any contained tags are not
   * interpreted.
   *
   * MDN
   */
  final val titleTag = "title".tag[*.Title]

  /**
   * Used to write inline CSS.
   *
   * MDN
   */
  final val styleTag = "style".tag[*.Style]
  // Scripting
  /**
   * Defines alternative content to display when the browser doesn't support
   * scripting.
   *
   * MDN
   */
  final val noscript = "noscript".tag[*.Element]

  // Sections
  /**
   * Represents a generic section of a document, i.e., a thematic grouping of
   * content, typically with a heading.
   *
   * MDN
   */
  final val section = "section".tag[*.Element]
  /**
   * Represents a section of a page that links to other pages or to parts within
   * the page: a section with navigation links.
   *
   * MDN
   */
  final val nav = "nav".tag[*.Element]
  /**
   * Defines self-contained content that could exist independently of the rest
   * of the content.
   *
   * MDN
   */
  final val article = "article".tag[*.Element]
  /**
   * Defines some content loosely related to the page content. If it is removed,
   * the remaining content still makes sense.
   *
   * MDN
   */
  final val aside = "aside".tag[*.Element]
  /**
   * Defines a section containing contact information.
   *
   * MDN
   */
  final val address = "address".tag[*.Element]

  /**
   * Defines the main or important content in the document. There is only one
   * main element in the document.
   *
   * MDN
   */
  final val main = "main".tag[*.Element]

  // Text level semantics

  /**
   * An inline quotation.
   *
   * MDN
   */
  final val q = "q".tag[*.Quote]
  /**
   * Represents a term whose definition is contained in its nearest ancestor
   * content.
   *
   * MDN
   */
  final val dfn = "dfn".tag[*.Element]
  /**
   * An abbreviation or acronym; the expansion of the abbreviation can be
   * represented in the title attribute.
   *
   * MDN
   */
  final val abbr = "abbr".tag[*.Element]
  /**
   * Associates to its content a machine-readable equivalent.
   *
   * MDN
   */
  final val data = "data".tag[*.Element]
  /**
   * Represents a date and time value; the machine-readable equivalent can be
   * represented in the datetime attribetu
   *
   * MDN
   */
  final val time = "time".tag[*.Element]
  /**
   * Represents a variable.
   *
   * MDN
   */
  final val `var` = "var".tag[*.Element]
  /**
   * Represents the output of a program or a computer.
   *
   * MDN
   */
  final val samp = "samp".tag[*.Element]
  /**
   * Represents user input, often from a keyboard, but not necessarily.
   *
   * MDN
   */
  final val kbd = "kbd".tag[*.Element]

  /**
   * Defines a mathematical formula.
   *
   * MDN
   */
  final val math = "math".tag[*.Element]
  /**
   * Represents text highlighted for reference purposes, that is for its
   * relevance in another context.
   *
   * MDN
   */
  final val mark = "mark".tag[*.Element]
  /**
   * Represents content to be marked with ruby annotations, short runs of text
   * presented alongside the text. This is often used in conjunction with East
   * Asian language where the annotations act as a guide for pronunciation, like
   * the Japanese furigana .
   *
   * MDN
   */
  final val ruby = "ruby".tag[*.Element]
  /**
   * Represents the text of a ruby annotation.
   *
   * MDN
   */
  final val rt = "rt".tag[*.Element]
  /**
   * Represents parenthesis around a ruby annotation, used to display the
   * annotation in an alternate way by browsers not supporting the standard
   * display for annotations.
   *
   * MDN
   */
  final val rp = "rp".tag[*.Element]
  /**
   * Represents text that must be isolated from its surrounding for bidirectional
   * text formatting. It allows embedding a span of text with a different, or
   * unknown, directionality.
   *
   * MDN
   */
  final val bdi = "bdi".tag[*.Element]
  /**
   * Represents the directionality of its children, in order to explicitly
   * override the Unicode bidirectional algorithm.
   *
   * MDN
   */
  final val bdo = "bdo".tag[*.Element]

  // Forms

  /**
   * A key-pair generator control.
   *
   * MDN
   */
  final val keygen = "keygen".voidTag[*.Element]
  /**
   * The result of a calculation
   *
   * MDN
   */
  final val output = "output".tag[*.Element]
  /**
   * A progress completion bar
   *
   * MDN
   */
  final val progress = "progress".tag[*.Progress]
  /**
   * A scalar measurement within a known range.
   *
   * MDN
   */
  final val meter = "meter".tag[*.Element]


  // Interactive elements
  /**
   * A widget from which the user can obtain additional information
   * or controls.
   *
   * MDN
   */
  final val details = "details".tag[*.Element]
  /**
   * A summary, caption, or legend for a given details.
   *
   * MDN
   */
  final val summary = "summary".tag[*.Element]
  /**
   * A command that the user can invoke.
   *
   * MDN
   */
  final val command = "command".voidTag[*.Element]
  /**
   * A list of commands
   *
   * MDN
   */
  final val menu = "menu".tag[*.Menu]
}