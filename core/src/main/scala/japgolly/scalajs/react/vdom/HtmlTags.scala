package japgolly.scalajs.react.vdom

import org.scalajs.dom.{html => *}
import Exports._

object HtmlTags extends HtmlTags
trait HtmlTags {

  /**
    * Represents a hyperlink, linking to another resource.
    */
  object a extends TagOf[*.Anchor]("a", Nil, Namespace.Html) {

    /** A link to open a new window (tab) to a given URL.
      *
      * Like: `<a href="https://google.com" target="_blank" rel="noopener"></a>`
      *
      * @param noopener See https://developers.google.com/web/tools/lighthouse/audits/noopener
      */
    def toNewWindow(href      : String,
                    noopener  : Boolean = true,
                    noreferrer: Boolean = false) = {
      implicit def strAttr = Attr.ValueType.string
      val a = this(HtmlAttrs.target.blank, HtmlAttrs.href := href)
      (noopener, noreferrer) match {
        case(true , false) => a(HtmlAttrs.rel := "noopener")
        case(true , true ) => a(HtmlAttrs.rel := "noopener noreferrer")
        case(false, true ) => a(HtmlAttrs.rel := "noreferrer")
        case(false, false) => a
      }
    }
  }

  /**
    * An abbreviation or acronym; the expansion of the abbreviation can be
    * represented in the title attribute.
    */
  final def abbr = HtmlTagOf[*.Element]("abbr")

  /**
    * Defines a section containing contact information.
    */
  final def address = HtmlTagOf[*.Element]("address")

  /**
    * In conjunction with map, defines an image map
    */
  final def area: HtmlTagOf[*.Area] = "area".reactTerminalTag

  /**
    * Defines self-contained content that could exist independently of the rest
    * of the content.
    */
  final def article = HtmlTagOf[*.Element]("article")

  /**
    * Defines some content loosely related to the page content. If it is removed,
    * the remaining content still makes sense.
    */
  final def aside = HtmlTagOf[*.Element]("aside")

  /**
    * Represents a sound or an audio stream.
    */
  final def audio = HtmlTagOf[*.Audio]("audio")

  /**
    * Bold text.
    */
  final def b = HtmlTagOf[*.Element]("b")

  /**
    * Defines the base URL for relative URLs in the page.
    */
  final def base: HtmlTagOf[*.Base] = "base".reactTerminalTag

  /**
    * Represents text that must be isolated from its surrounding for bidirectional
    * text formatting. It allows embedding a span of text with a different, or
    * unknown, directionality.
    */
  final def bdi = HtmlTagOf[*.Element]("bdi")

  /**
    * Represents the directionality of its children, in order to explicitly
    * override the Unicode bidirectional algorithm.
    */
  final def bdo = HtmlTagOf[*.Element]("bdo")

  /**
    * Represents a content that is quoted from another source.
    */
  final def blockquote = HtmlTagOf[*.Quote]("blockquote")

  /**
    * Represents the content of an HTML document. There is only one body
    * element in a document.
    */
  final def body = HtmlTagOf[*.Body]("body")

  /**
    * Represents a line break.
    */
  final def br: HtmlTagOf[*.BR] = "br".reactTerminalTag

  final def button = HtmlTagOf[*.Button]("button")

  /**
    * Represents a bitmap area that scripts can use to render graphics like graphs,
    * games or any visual images on the fly.
    */
  final def canvas = HtmlTagOf[*.Canvas]("canvas")

  /**
    * The title of a table.
    */
  final def caption = HtmlTagOf[*.TableCaption]("caption")

  /**
    * Represents the title of a work being cited.
    */
  final def cite = HtmlTagOf[*.Element]("cite")

  /**
    * Represents computer code.
    */
  final def code = HtmlTagOf[*.Element]("code")

  /**
    * A single column.
    */
  final def col: HtmlTagOf[*.TableCol] = "col".reactTerminalTag

  /**
    * A set of columns.
    */
  final def colgroup = HtmlTagOf[*.TableCol]("colgroup")

  /**
    * A command that the user can invoke.
    */
  final def command: HtmlTagOf[*.Element] = "command".reactTerminalTag

  /**
    * Associates to its content a machine-readable equivalent.
    */
  final def data = HtmlTagOf[*.Element]("data")

  /**
    * A set of predefined options for other controls.
    */
  final def datalist = HtmlTagOf[*.DataList]("datalist")

  /**
    * Represents the definition of the terms immediately listed before it.
    */
  final def dd = HtmlTagOf[*.DD]("dd")

  /**
    * Defines a remofinal def from the document.
    */
  final def del = HtmlTagOf[*.Mod]("del")

  /**
    * A widget from which the user can obtain additional information
    * or controls.
    */
  final def details = HtmlTagOf[*.Element]("details")

  /**
    * Represents a term whose definition is contained in its nearest ancestor
    * content.
    */
  final def dfn = HtmlTagOf[*.Element]("dfn")

  /**
    * Represents a generic container with no special meaning.
    */
  final def div = HtmlTagOf[*.Div]("div")

  /**
    * Defines a definition list; a list of terms and their associated definitions.
    */
  final def dl = HtmlTagOf[*.DList]("dl")

  /**
    * Represents a term defined by the next dd
    */
  final def dt = HtmlTagOf[*.DT]("dt")

  /**
    * Represents emphasized text.
    */
  final def em = HtmlTagOf[*.Element]("em")

  /**
    * Represents a integration point for an external, often non-HTML, application
    * or interactive content.
    */
  final def embed: HtmlTagOf[*.Embed] = "embed".reactTerminalTag

  /**
    * A set of fields.
    */
  final def fieldset = HtmlTagOf[*.FieldSet]("fieldset")

  /**
    * Represents the legend of a figure.
    */
  final def figcaption = HtmlTagOf[*.Element]("figcaption")

  /**
    * Represents a figure illustrated as part of the document.
    */
  final def figure = HtmlTagOf[*.Element]("figure")

  /**
    * Defines the footer for a page or section. It often contains a copyright
    * notice, some links to legal information, or addresses to give feedback.
    */
  final def footer = HtmlTagOf[*.Element]("footer")

  /**
    * Represents a form, consisting of controls, that can be submitted to a
    * server for processing.
    */
  final def form = HtmlTagOf[*.Form]("form")

  /**
    * Heading level 1
    */
  final def h1 = HtmlTagOf[*.Heading]("h1")

  /**
    * Heading level 2
    */
  final def h2 = HtmlTagOf[*.Heading]("h2")

  /**
    * Heading level 3
    */
  final def h3 = HtmlTagOf[*.Heading]("h3")

  /**
    * Heading level 4
    */
  final def h4 = HtmlTagOf[*.Heading]("h4")

  /**
    * Heading level 5
    */
  final def h5 = HtmlTagOf[*.Heading]("h5")

  /**
    * Heading level 6
    */
  final def h6 = HtmlTagOf[*.Heading]("h6")

  /**
    * Represents a collection of metadata about the document, including links to,
    * or definitions of, scripts and style sheets.
    */
  final def head = HtmlTagOf[*.Head]("head")

  /**
    * Defines the header of a page or section. It often contains a logo, the
    * title of the Web site, and a navigational table of content.
    */
  final def header = HtmlTagOf[*.Element]("header")

  /**
    * Represents a thematic break between paragraphs of a section or article or
    * any longer content.
    */
  final def hr: HtmlTagOf[*.HR] = "hr".reactTerminalTag

  /**
    * Represents the root of an HTML or XHTML document. All other elements must
    * be descendants of this element.
    */
  final def html = HtmlTagOf[*.Html]("html")

  /**
    * Italicized text.
    */
  final def i = HtmlTagOf[*.Element]("i")

  /**
    * Represents a nested browsing context, that is an embedded HTML document.
    */
  final def iframe = HtmlTagOf[*.IFrame]("iframe")

  /**
    * Represents an image.
    */
  final def img: HtmlTagOf[*.Image] = "img".reactTerminalTag

  /**
    * The HTML element &lt;input&gt; is used to create interactive controls for web-based forms in order to accept data
    * from the user. How an &lt;input&gt; works varies considerably depending on the value of its type attribute.
    */
  object input extends TagOf[*.Input]("input", Nil, Namespace.Html) {

    private[this] val `type` = VdomAttr[String]("type")

    /** Returns a &lt;input type="{t}" /&gt; */
    def withType(t: String): TagOf[*.Input] =
      this(`type`.:=(t)(Attr.ValueType.string))

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

  /**
    * Defines an addition to the document.
    */
  final def ins = HtmlTagOf[*.Mod]("ins")

  /**
    * Represents user input, often from a keyboard, but not necessarily.
    */
  final def kbd = HtmlTagOf[*.Element]("kbd")

  /**
    * A key-pair generator control.
    */
  final def keygen: HtmlTagOf[*.Element] = "keygen".reactTerminalTag

  /**
    * The caption of a single field
    */
  final def label = HtmlTagOf[*.Label]("label")

  /**
    * The caption for a fieldset.
    */
  final def legend = HtmlTagOf[*.Legend]("legend")

  /**
    * Defines an item of an list.
    */
  final def li = HtmlTagOf[*.LI]("li")

  /**
    * Used to link JavaScript and external CSS with the current HTML document.
    */
  final def link: HtmlTagOf[*.Link] = "link".reactTerminalTag

  /**
    * Defines the main or important content in the document. There is only one
    * main element in the document.
    */
  final def main = HtmlTagOf[*.Element]("main")

  /**
    * In conjunction with area, defines an image map.
    */
  final def map = HtmlTagOf[*.Map]("map")

  /**
    * Represents text highlighted for reference purposes, that is for its
    * relevance in another context.
    */
  final def mark = HtmlTagOf[*.Element]("mark")

  /**
    * Defines a mathematical formula.
    */
  final def math = HtmlTagOf[*.Element]("math")

  /**
    * A list of commands
    */
  final def menu = HtmlTagOf[*.Menu]("menu")

  /**
    * Defines metadata that can't be defined using another HTML element.
    */
  final def meta: HtmlTagOf[*.Meta] = "meta".reactTerminalTag

  /**
    * A scalar measurement within a known range.
    */
  final def meter = HtmlTagOf[*.Element]("meter")

  /**
    * Represents a section of a page that links to other pages or to parts within
    * the page: a section with navigation links.
    */
  final def nav = HtmlTagOf[*.Element]("nav")

  /**
    * Defines alternative content to display when the browser doesn't support
    * scripting.
    */
  final def noscript = HtmlTagOf[*.Element]("noscript")

  /**
    * Represents an external resource, which is treated as an image, an HTML
    * sub-document, or an external resource to be processed by a plug-in.
    */
  final def `object` = HtmlTagOf[*.Object]("object")

  /**
    * Defines an ordered list of items.
    */
  final def ol = HtmlTagOf[*.OList]("ol")

  /**
    * A set of options, logically grouped.
    */
  final def optgroup = HtmlTagOf[*.OptGroup]("optgroup")

  /**
    * An option in a select element.
    */
  final def option = HtmlTagOf[*.Option]("option")

  /**
    * The result of a calculation
    */
  final def output = HtmlTagOf[*.Element]("output")

  /**
    * Defines a portion that should be displayed as a paragraph.
    */
  final def p = HtmlTagOf[*.Paragraph]("p")

  /**
    * Defines parameters for use by plug-ins invoked by object elements.
    */
  final def param: HtmlTagOf[*.Param] = "param".reactTerminalTag

  /**
    * Indicates that its content is preformatted and that this format must be
    * preserved.
    */
  final def pre = HtmlTagOf[*.Pre]("pre")

  /**
    * A progress completion bar
    */
  final def progress = HtmlTagOf[*.Progress]("progress")

  /**
    * An inline quotation.
    */
  final def q = HtmlTagOf[*.Quote]("q")

  /**
    * Represents parenthesis around a ruby annotation, used to display the
    * annotation in an alternate way by browsers not supporting the standard
    * display for annotations.
    */
  final def rp = HtmlTagOf[*.Element]("rp")

  /**
    * Represents the text of a ruby annotation.
    */
  final def rt = HtmlTagOf[*.Element]("rt")

  /**
    * Represents content to be marked with ruby annotations, short runs of text
    * presented alongside the text. This is often used in conjunction with East
    * Asian language where the annotations act as a guide for pronunciation, like
    * the Japanese furigana .
    */
  final def ruby = HtmlTagOf[*.Element]("ruby")

  /**
    * Strikethrough element, used for that is no longer accurate or relevant.
    */
  final def s = HtmlTagOf[*.Element]("s")

  /**
    * Represents the output of a program or a computer.
    */
  final def samp = HtmlTagOf[*.Element]("samp")

  /**
    * Defines either an internal script or a link to an external script. The
    * script language is JavaScript.
    */
  final def script = HtmlTagOf[*.Script]("script")

  /**
    * Represents a generic section of a document, i.e., a thematic grouping of
    * content, typically with a heading.
    */
  final def section = HtmlTagOf[*.Element]("section")

  /**
    * A control that allows the user to select one of a set of options.
    */
  final def select = HtmlTagOf[*.Select]("select")

  /**
    * Represents a side comment; text like a disclaimer or copyright, which is not
    * essential to the comprehension of the document.
    */
  final def small = HtmlTagOf[*.Element]("small")

  /**
    * Allows the authors to specify alternate media resources for media elements
    * like video or audio
    */
  final def source: HtmlTagOf[*.Source] = "source".reactTerminalTag

  /**
    * Represents text with no specific meaning. This has to be used when no other
    * text-semantic element conveys an adequate meaning, which, in this case, is
    * often brought by global attributes like class, lang, or dir.
    */
  final def span = HtmlTagOf[*.Span]("span")

  /**
    * Represents especially important text.
    */
  final def strong = HtmlTagOf[*.Element]("strong")

  /**
    * Used to write inline CSS.
    */
  final def styleTag = HtmlTagOf[*.Style]("style")

  /**
    * Subscript tag
    */
  final def sub = HtmlTagOf[*.Element]("sub")

  /**
    * A summary, caption, or legend for a given details.
    */
  final def summary = HtmlTagOf[*.Element]("summary")

  /**
    * Superscript tag.
    */
  final def sup = HtmlTagOf[*.Element]("sup")

  /**
    * Represents data with more than one dimension.
    */
  final def table = HtmlTagOf[*.Table]("table")

  /**
    * The table body.
    */
  final def tbody = HtmlTagOf[*.TableSection]("tbody")

  /**
    * A single cell in a table.
    */
  final def td = HtmlTagOf[*.TableCell]("td")

  /**
    * A multiline text edit control.
    */
  final def textarea = HtmlTagOf[*.TextArea]("textarea")

  /**
    * The table footer.
    */
  final def tfoot = HtmlTagOf[*.TableSection]("tfoot")

  /**
    * A header cell in a table.
    */
  final def th = HtmlTagOf[*.TableHeaderCell]("th")

  /**
    * The table headers.
    */
  final def thead = HtmlTagOf[*.TableSection]("thead")

  /**
    * Represents a date and time value; the machine-readable equivalent can be
    * represented in the datetime attribetu
    */
  final def time = HtmlTagOf[*.Element]("time")

  /**
    * Defines the title of the document, shown in a browser's title bar or on the
    * page's tab. It can only contain text and any contained tags are not
    * interpreted.
    */
  final def titleTag = HtmlTagOf[*.Title]("title")

  /**
    * A single row in a table.
    */
  final def tr = HtmlTagOf[*.TableRow]("tr")

  /**
    * Allows authors to specify timed text track for media elements like video or
    * audio
    */
  final def track: HtmlTagOf[*.Track] = "track".reactTerminalTag

  /**
    * Underlined text.
    */
  final def u = HtmlTagOf[*.Element]("u")

  /**
    * Defines an unordered list of items.
    */
  final def ul = HtmlTagOf[*.UList]("ul")

  /**
    * Represents a variable.
    */
  final def `var` = HtmlTagOf[*.Element]("var")

  /**
    * Represents a line break opportunity, that is a suggested point for wrapping
    * text in order to improve readability of text split on several lines.
    */
  final def wbr: HtmlTagOf[*.Element] = "wbr".reactTerminalTag

  /**
    * Represents a video, and its associated audio files and captions, with the
    * necessary interface to play it.
    */
  final def video = HtmlTagOf[*.Video]("video")
}


