package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.vdom.Exports._
import org.scalajs.dom.{html => H}

object HtmlTags extends HtmlTags
trait HtmlTags {

  /**
    * Represents a hyperlink, linking to another resource.
    */
  object a extends TagOf[H.Anchor]("a", Nil, Namespace.Html) {

    /** A link to open a new window (tab) to a given URL.
      *
      * Like: `<a href="https://google.com" target="_blank" rel="noopener"></a>`
      *
      * @param noopener See https://developers.google.com/web/tools/lighthouse/audits/noopener
      */
    def toNewWindow(href      : String,
                    noopener  : Boolean = true,
                    noreferrer: Boolean = false) = {
      implicit def strAttr: Attr.ValueType[String, Nothing] = Attr.ValueType.string
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
  final def abbr = HtmlTagOf[H.Element]("abbr")

  /**
    * Defines a section containing contact information.
    */
  final def address = HtmlTagOf[H.Element]("address")

  /**
    * In conjunction with map, defines an image map
    */
  final def area: HtmlTagOf[H.Area] = "area".reactTerminalTag

  /**
    * Defines self-contained content that could exist independently of the rest
    * of the content.
    */
  final def article = HtmlTagOf[H.Element]("article")

  /**
    * Defines some content loosely related to the page content. If it is removed,
    * the remaining content still makes sense.
    */
  final def aside = HtmlTagOf[H.Element]("aside")

  /**
    * Represents a sound or an audio stream.
    */
  final def audio = HtmlTagOf[H.Audio]("audio")

  /**
    * Bold text.
    */
  final def b = HtmlTagOf[H.Element]("b")

  /**
    * Defines the base URL for relative URLs in the page.
    */
  final def base: HtmlTagOf[H.Base] = "base".reactTerminalTag

  /**
    * Represents text that must be isolated from its surrounding for bidirectional
    * text formatting. It allows embedding a span of text with a different, or
    * unknown, directionality.
    */
  final def bdi = HtmlTagOf[H.Element]("bdi")

  /**
    * Represents the directionality of its children, in order to explicitly
    * override the Unicode bidirectional algorithm.
    */
  final def bdo = HtmlTagOf[H.Element]("bdo")

  /**
    * Represents a content that is quoted from another source.
    */
  final def blockquote = HtmlTagOf[H.Quote]("blockquote")

  /**
    * Represents the content of an HTML document. There is only one body
    * element in a document.
    */
  final def body = HtmlTagOf[H.Body]("body")

  /**
    * Represents a line break.
    */
  final def br: HtmlTagOf[H.BR] = "br".reactTerminalTag

  final def button = HtmlTagOf[H.Button]("button")

  /**
    * Represents a bitmap area that scripts can use to render graphics like graphs,
    * games or any visual images on the fly.
    */
  final def canvas = HtmlTagOf[H.Canvas]("canvas")

  /**
    * The title of a table.
    */
  final def caption = HtmlTagOf[H.TableCaption]("caption")

  /**
    * Represents the title of a work being cited.
    */
  final def cite = HtmlTagOf[H.Element]("cite")

  /**
    * Represents computer code.
    */
  final def code = HtmlTagOf[H.Element]("code")

  /**
    * A single column.
    */
  final def col: HtmlTagOf[H.TableCol] = "col".reactTerminalTag

  /**
    * A set of columns.
    */
  final def colgroup = HtmlTagOf[H.TableCol]("colgroup")

  /**
    * A command that the user can invoke.
    */
  final def command: HtmlTagOf[H.Element] = "command".reactTerminalTag

  /**
    * Associates to its content a machine-readable equivalent.
    */
  final def data = HtmlTagOf[H.Element]("data")

  /**
    * A set of predefined options for other controls.
    */
  final def datalist = HtmlTagOf[H.DataList]("datalist")

  /**
    * Represents the definition of the terms immediately listed before it.
    */
  final def dd = HtmlTagOf[H.Element]("dd")

  /**
    * Defines a remofinal def from the document.
    */
  final def del = HtmlTagOf[H.Mod]("del")

  /**
    * A widget from which the user can obtain additional information
    * or controls.
    */
  final def details = HtmlTagOf[H.Element]("details")

  /**
    * Represents a term whose definition is contained in its nearest ancestor
    * content.
    */
  final def dfn = HtmlTagOf[H.Element]("dfn")

  /**
    * Represents a generic container with no special meaning.
    */
  final def div = HtmlTagOf[H.Div]("div")

  /**
    * Defines a definition list; a list of terms and their associated definitions.
    */
  final def dl = HtmlTagOf[H.DList]("dl")

  /**
    * Represents a term defined by the next dd
    */
  final def dt = HtmlTagOf[H.Element]("dt")

  /**
    * Represents emphasized text.
    */
  final def em = HtmlTagOf[H.Element]("em")

  /**
    * Represents a integration point for an external, often non-HTML, application
    * or interactive content.
    */
  final def embed: HtmlTagOf[H.Embed] = "embed".reactTerminalTag

  /**
    * A set of fields.
    */
  final def fieldset = HtmlTagOf[H.FieldSet]("fieldset")

  /**
    * Represents the legend of a figure.
    */
  final def figcaption = HtmlTagOf[H.Element]("figcaption")

  /**
    * Represents a figure illustrated as part of the document.
    */
  final def figure = HtmlTagOf[H.Element]("figure")

  /**
    * Defines the footer for a page or section. It often contains a copyright
    * notice, some links to legal information, or addresses to give feedback.
    */
  final def footer = HtmlTagOf[H.Element]("footer")

  /**
    * Represents a form, consisting of controls, that can be submitted to a
    * server for processing.
    */
  final def form = HtmlTagOf[H.Form]("form")

  /**
    * Heading level 1
    */
  final def h1 = HtmlTagOf[H.Heading]("h1")

  /**
    * Heading level 2
    */
  final def h2 = HtmlTagOf[H.Heading]("h2")

  /**
    * Heading level 3
    */
  final def h3 = HtmlTagOf[H.Heading]("h3")

  /**
    * Heading level 4
    */
  final def h4 = HtmlTagOf[H.Heading]("h4")

  /**
    * Heading level 5
    */
  final def h5 = HtmlTagOf[H.Heading]("h5")

  /**
    * Heading level 6
    */
  final def h6 = HtmlTagOf[H.Heading]("h6")

  /**
    * Represents a collection of metadata about the document, including links to,
    * or definitions of, scripts and style sheets.
    */
  final def head = HtmlTagOf[H.Head]("head")

  /**
    * Defines the header of a page or section. It often contains a logo, the
    * title of the Web site, and a navigational table of content.
    */
  final def header = HtmlTagOf[H.Element]("header")

  /**
    * Represents a thematic break between paragraphs of a section or article or
    * any longer content.
    */
  final def hr: HtmlTagOf[H.HR] = "hr".reactTerminalTag

  /**
    * Represents the root of an HTML or XHTML document. All other elements must
    * be descendants of this element.
    */
  final def html = HtmlTagOf[H.Html]("html")

  /**
    * Italicized text.
    */
  final def i = HtmlTagOf[H.Element]("i")

  /**
    * Represents a nested browsing context, that is an embedded HTML document.
    */
  final def iframe = HtmlTagOf[H.IFrame]("iframe")

  /**
    * Represents an image.
    */
  final def img: HtmlTagOf[H.Image] = "img".reactTerminalTag

  /**
    * The HTML element &lt;input&gt; is used to create interactive controls for web-based forms in order to accept data
    * from the user. How an &lt;input&gt; works varies considerably depending on the value of its type attribute.
    */
  object input extends TagOf[H.Input]("input", Nil, Namespace.Html) {

    private[this] val `type` = VdomAttr[String]("type")

    /** Returns a &lt;input type="{t}" /&gt; */
    def withType(t: String): TagOf[H.Input] =
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
  final def ins = HtmlTagOf[H.Mod]("ins")

  /**
    * Represents user input, often from a keyboard, but not necessarily.
    */
  final def kbd = HtmlTagOf[H.Element]("kbd")

  /**
    * A key-pair generator control.
    */
  final def keygen: HtmlTagOf[H.Element] = "keygen".reactTerminalTag

  /**
    * The caption of a single field
    */
  final def label = HtmlTagOf[H.Label]("label")

  /**
    * The caption for a fieldset.
    */
  final def legend = HtmlTagOf[H.Legend]("legend")

  /**
    * Defines an item of an list.
    */
  final def li = HtmlTagOf[H.LI]("li")

  /**
    * Used to link JavaScript and external CSS with the current HTML document.
    */
  final def link: HtmlTagOf[H.Link] = "link".reactTerminalTag

  /**
    * Defines the main or important content in the document. There is only one
    * main element in the document.
    */
  final def main = HtmlTagOf[H.Element]("main")

  /**
    * In conjunction with area, defines an image map.
    */
  final def map = HtmlTagOf[H.Map]("map")

  /**
    * Represents text highlighted for reference purposes, that is for its
    * relevance in another context.
    */
  final def mark = HtmlTagOf[H.Element]("mark")

  /**
    * Defines a mathematical formula.
    */
  final def math = HtmlTagOf[H.Element]("math")

  /**
    * A list of commands
    */
  final def menu = HtmlTagOf[H.Menu]("menu")

  /**
    * Defines metadata that can't be defined using another HTML element.
    */
  final def meta: HtmlTagOf[H.Meta] = "meta".reactTerminalTag

  /**
    * A scalar measurement within a known range.
    */
  final def meter = HtmlTagOf[H.Element]("meter")

  /**
    * Represents a section of a page that links to other pages or to parts within
    * the page: a section with navigation links.
    */
  final def nav = HtmlTagOf[H.Element]("nav")

  /**
    * Defines alternative content to display when the browser doesn't support
    * scripting.
    */
  final def noscript = HtmlTagOf[H.Element]("noscript")

  /**
    * Represents an external resource, which is treated as an image, an HTML
    * sub-document, or an external resource to be processed by a plug-in.
    */
  final def `object` = HtmlTagOf[H.Object]("object")

  /**
    * Defines an ordered list of items.
    */
  final def ol = HtmlTagOf[H.OList]("ol")

  /**
    * A set of options, logically grouped.
    */
  final def optgroup = HtmlTagOf[H.OptGroup]("optgroup")

  /**
    * An option in a select element.
    */
  final def option = HtmlTagOf[H.Option]("option")

  /**
    * The result of a calculation
    */
  final def output = HtmlTagOf[H.Element]("output")

  /**
    * Defines a portion that should be displayed as a paragraph.
    */
  final def p = HtmlTagOf[H.Paragraph]("p")

  /**
    * Defines parameters for use by plug-ins invoked by object elements.
    */
  final def param: HtmlTagOf[H.Param] = "param".reactTerminalTag

  /**
    * Indicates that its content is preformatted and that this format must be
    * preserved.
    */
  final def pre = HtmlTagOf[H.Pre]("pre")

  /**
    * A progress completion bar
    */
  final def progress = HtmlTagOf[H.Progress]("progress")

  /**
    * An inline quotation.
    */
  final def q = HtmlTagOf[H.Quote]("q")

  /**
    * Represents parenthesis around a ruby annotation, used to display the
    * annotation in an alternate way by browsers not supporting the standard
    * display for annotations.
    */
  final def rp = HtmlTagOf[H.Element]("rp")

  /**
    * Represents the text of a ruby annotation.
    */
  final def rt = HtmlTagOf[H.Element]("rt")

  /**
    * Represents content to be marked with ruby annotations, short runs of text
    * presented alongside the text. This is often used in conjunction with East
    * Asian language where the annotations act as a guide for pronunciation, like
    * the Japanese furigana .
    */
  final def ruby = HtmlTagOf[H.Element]("ruby")

  /**
    * Strikethrough element, used for that is no longer accurate or relevant.
    */
  final def s = HtmlTagOf[H.Element]("s")

  /**
    * Represents the output of a program or a computer.
    */
  final def samp = HtmlTagOf[H.Element]("samp")

  /**
    * Defines either an internal script or a link to an external script. The
    * script language is JavaScript.
    */
  final def script = HtmlTagOf[H.Script]("script")

  /**
    * Represents a generic section of a document, i.e., a thematic grouping of
    * content, typically with a heading.
    */
  final def section = HtmlTagOf[H.Element]("section")

  /**
    * A control that allows the user to select one of a set of options.
    */
  final def select = HtmlTagOf[H.Select]("select")

  /**
    * Represents a side comment; text like a disclaimer or copyright, which is not
    * essential to the comprehension of the document.
    */
  final def small = HtmlTagOf[H.Element]("small")

  /**
    * Allows the authors to specify alternate media resources for media elements
    * like video or audio
    */
  final def source: HtmlTagOf[H.Source] = "source".reactTerminalTag

  /**
    * Represents text with no specific meaning. This has to be used when no other
    * text-semantic element conveys an adequate meaning, which, in this case, is
    * often brought by global attributes like class, lang, or dir.
    */
  final def span = HtmlTagOf[H.Span]("span")

  /**
    * Represents especially important text.
    */
  final def strong = HtmlTagOf[H.Element]("strong")

  /**
    * Used to write inline CSS.
    */
  final def styleTag = HtmlTagOf[H.Style]("style")

  /**
    * Subscript tag
    */
  final def sub = HtmlTagOf[H.Element]("sub")

  /**
    * A summary, caption, or legend for a given details.
    */
  final def summary = HtmlTagOf[H.Element]("summary")

  /**
    * Superscript tag.
    */
  final def sup = HtmlTagOf[H.Element]("sup")

  /**
    * Represents data with more than one dimension.
    */
  final def table = HtmlTagOf[H.Table]("table")

  /**
    * The table body.
    */
  final def tbody = HtmlTagOf[H.TableSection]("tbody")

  /**
    * A single cell in a table.
    */
  final def td = HtmlTagOf[H.TableCell]("td")

  /**
    * A multiline text edit control.
    */
  final def textarea = HtmlTagOf[H.TextArea]("textarea")

  /**
    * The table footer.
    */
  final def tfoot = HtmlTagOf[H.TableSection]("tfoot")

  /**
    * A header cell in a table.
    */
  final def th = HtmlTagOf[H.TableCell]("th")

  /**
    * The table headers.
    */
  final def thead = HtmlTagOf[H.TableSection]("thead")

  /**
    * Represents a date and time value; the machine-readable equivalent can be
    * represented in the datetime attribetu
    */
  final def time = HtmlTagOf[H.Element]("time")

  /**
    * Defines the title of the document, shown in a browser's title bar or on the
    * page's tab. It can only contain text and any contained tags are not
    * interpreted.
    */
  final def titleTag = HtmlTagOf[H.Title]("title")

  /**
    * A single row in a table.
    */
  final def tr = HtmlTagOf[H.TableRow]("tr")

  /**
    * Allows authors to specify timed text track for media elements like video or
    * audio
    */
  final def track: HtmlTagOf[H.Track] = "track".reactTerminalTag

  /**
    * Underlined text.
    */
  final def u = HtmlTagOf[H.Element]("u")

  /**
    * Defines an unordered list of items.
    */
  final def ul = HtmlTagOf[H.UList]("ul")

  /**
    * Represents a variable.
    */
  final def `var` = HtmlTagOf[H.Element]("var")

  /**
    * Represents a line break opportunity, that is a suggested point for wrapping
    * text in order to improve readability of text split on several lines.
    */
  final def wbr: HtmlTagOf[H.Element] = "wbr".reactTerminalTag

  /**
    * Represents a video, and its associated audio files and captions, with the
    * necessary interface to play it.
    */
  final def video = HtmlTagOf[H.Video]("video")
}


