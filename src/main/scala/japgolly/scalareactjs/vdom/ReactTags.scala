package japgolly.scalareactjs.vdom

import scalatags._
import generic.Util

/**
 * Trait that contains the contents of the `Tags` object, so they can be mixed
 * in to other objects if needed.
 */
trait ReactTags extends generic.Tags[VDomBuilder, ReactOutput, ReactFragT]{

  // Root Element
  /**
   * Represents the root of an HTML or XHTML document. All other elements must
   * be descendants of this element.
   *
   *  MDN
   */
  val html = "html".tag[ReactOutput]

  // Document Metadata
  /**
   * Represents a collection of metadata about the document, including links to,
   * or definitions of, scripts and style sheets.
   *
   *  MDN
   */
  val head = "head".tag[ReactOutput]

  /**
   * Defines the base URL for relative URLs in the page.
   *
   *  MDN
   */
  val base = "base".voidTag[ReactOutput]
  /**
   * Used to link JavaScript and external CSS with the current HTML document.
   *
   *  MDN
   */
  val link = "link".voidTag[ReactOutput]
  /**
   * Defines metadata that can't be defined using another HTML element.
   *
   *  MDN
   */
  val meta = "meta".voidTag[ReactOutput]


  // Scripting
  /**
   * Defines either an internal script or a link to an external script. The
   * script language is JavaScript.
   *
   *  MDN
   */
  val script = "script".tag[ReactOutput]


  // Sections
  /**
   * Represents the content of an HTML document. There is only one body
   *   element in a document.
   *
   *  MDN
   */
  val body = "body".tag[ReactOutput]

  /**
   * Heading level 1
   *
   *  MDN
   */
  val h1 = "h1".tag[ReactOutput]
  /**
   * Heading level 2
   *
   *  MDN
   */
  val h2 = "h2".tag[ReactOutput]
  /**
   * Heading level 3
   *
   *  MDN
   */
  val h3 = "h3".tag[ReactOutput]
  /**
   * Heading level 4
   *
   *  MDN
   */
  val h4 = "h4".tag[ReactOutput]
  /**
   * Heading level 5
   *
   *  MDN
   */
  val h5 = "h5".tag[ReactOutput]
  /**
   * Heading level 6
   *
   *  MDN
   */
  val h6 = "h6".tag[ReactOutput]
  /**
   * Defines the header of a page or section. It often contains a logo, the
   * title of the Web site, and a navigational table of content.
   *
   *  MDN
   */
  val header = "header".tag[ReactOutput]
  /**
   * Defines the footer for a page or section. It often contains a copyright
   * notice, some links to legal information, or addresses to give feedback.
   *
   *  MDN
   */
  val footer = "footer".tag[ReactOutput]


  // Grouping content
  /**
   * Defines a portion that should be displayed as a paragraph.
   *
   *  MDN
   */
  val p = "p".tag[ReactOutput]
  /**
   * Represents a thematic break between paragraphs of a section or article or
   * any longer content.
   *
   *  MDN
   */
  val hr = "hr".voidTag[ReactOutput]
  /**
   * Indicates that its content is preformatted and that this format must be
   * preserved.
   *
   *  MDN
   */
  val pre = "pre".tag[ReactOutput]
  /**
   * Represents a content that is quoted from another source.
   *
   *  MDN
   */
  val blockquote = "blockquote".tag[ReactOutput]
  /**
   * Defines an ordered list of items.
   *
   *  MDN
   */
  val ol = "ol".tag[ReactOutput]
  /**
   * Defines an unordered list of items.
   *
   *  MDN
   */
  val ul = "ul".tag[ReactOutput]
  /**
   * Defines an item of an list.
   *
   *  MDN
   */
  val li = "li".tag[ReactOutput]
  /**
   * Defines a definition list; al ist of terms and their associated definitions.
   *
   *  MDN
   */
  val dl = "dl".tag[ReactOutput]
  /**
   * Represents a term defined by the next dd
   *
   *  MDN
   */
  val dt = "dl".tag[ReactOutput]
  /**
   * Represents the definition of the terms immediately listed before it.
   *
   *  MDN
   */
  val dd = "dd".tag[ReactOutput]
  /**
   * Represents a figure illustrated as part of the document.
   *
   *  MDN
   */
  val figure = "figure".tag[ReactOutput]
  /**
   * Represents the legend of a figure.
   *
   *  MDN
   */
  val figcaption = "figcaption".tag[ReactOutput]
  /**
   * Represents a generic container with no special meaning.
   *
   *  MDN
   */
  val div = "div".tag[ReactOutput]

  // Text-level semantics
  /**
   * Represents a hyperlink, linking to another resource.
   *
   *  MDN
   */
  val a = "a".tag[ReactOutput]
  /**
   * Represents emphasized text.
   *
   *  MDN
   */
  val em = "em".tag[ReactOutput]
  /**
   * Represents especially important text.
   *
   *  MDN
   */
  val strong = "strong".tag[ReactOutput]
  /**
   * Represents a side comment; text like a disclaimer or copyright, which is not
   * essential to the comprehension of the document.
   *
   *  MDN
   */
  val small = "small".tag[ReactOutput]
  /**
   * Strikethrough element, used for that is no longer accurate or relevant.
   *
   *  MDN
   */
  val s = "s".tag[ReactOutput]
  /**
   * Represents the title of a work being cited.
   *
   *  MDN
   */
  val cite = "cite".tag[ReactOutput]

  /**
   * Represents computer code.
   *
   *  MDN
   */
  val code = "code".tag[ReactOutput]

  /**
   * Subscript tag[ReactOutput]
   *
   *  MDN
   */
  val sub = "sub".tag[ReactOutput]
  /**
   * Superscript tag.
   *
   *  MDN
   */
  val sup = "sup".tag[ReactOutput]
  /**
   * Italicized text.
   *
   *  MDN
   */
  val i = "i".tag[ReactOutput]
  /**
   * Bold text.
   *
   *  MDN
   */
  val b = "b".tag[ReactOutput]
  /**
   * Underlined text.
   *
   *  MDN
   */
  val u = "u".tag[ReactOutput]

  /**
   * Represents text with no specific meaning. This has to be used when no other
   * text-semantic element conveys an adequate meaning, which, in this case, is
   * often brought by global attributes like class, lang, or dir.
   *
   *  MDN
   */
  val span = "span".tag[ReactOutput]
  /**
   * Represents a line break.
   *
   *  MDN
   */
  val br = "br".voidTag[ReactOutput]
  /**
   * Represents a line break opportunity, that is a suggested point for wrapping
   * text in order to improve readability of text split on several lines.
   *
   *  MDN
   */
  val wbr = "wbr".voidTag[ReactOutput]

  // Edits
  /**
   * Defines an addition to the document.
   *
   *  MDN
   */
  val ins = "ins".tag[ReactOutput]
  /**
   * Defines a removal from the document.
   *
   *  MDN
   */
  val del = "del".tag[ReactOutput]

  // Embedded content
  /**
   * Represents an image.
   *
   *  MDN
   */
  val img = "img".voidTag[ReactOutput]
  /**
   * Represents a nested browsing context, that is an embedded HTML document.
   *
   *  MDN
   */
  val iframe = "iframe".tag[ReactOutput]
  /**
   * Represents a integration point for an external, often non-HTML, application
   * or interactive content.
   *
   *  MDN
   */
  val embed = "embed".voidTag[ReactOutput]
  /**
   * Represents an external resource, which is treated as an image, an HTML
   * sub-document, or an external resource to be processed by a plug-in.
   *
   *  MDN
   */
  val `object` = "object".tag[ReactOutput]
  /**
   * Defines parameters for use by plug-ins invoked by object elements.
   *
   *  MDN
   */
  val param = "param".voidTag[ReactOutput]
  /**
   * Represents a video, and its associated audio files and captions, with the
   * necessary interface to play it.
   *
   *  MDN
   */
  val video = "video".tag[ReactOutput]
  /**
   * Represents a sound or an audio stream.
   *
   *  MDN
   */
  val audio = "audio".tag[ReactOutput]
  /**
   * Allows the authors to specify alternate media resources for media elements
   * like video or audio
   *
   *  MDN
   */
  val source = "source".voidTag[ReactOutput]
  /**
   * Allows authors to specify timed text track for media elements like video or
   * audio
   *
   *  MDN
   */
  val track = "track".voidTag[ReactOutput]
  /**
   * Represents a bitmap area that scripts can use to render graphics like graphs,
   * games or any visual images on the fly.
   *
   *  MDN
   */
  val canvas = "canvas".tag[ReactOutput]
  /**
   * In conjunction with area, defines an image map.
   *
   *  MDN
   */
  val map = "map".tag[ReactOutput]
  /**
   * In conjunction with map, defines an image map
   *
   *  MDN
   */
  val area = "area".voidTag[ReactOutput]


  // Tabular data
  /**
   * Represents data with more than one dimension.
   *
   *  MDN
   */
  val table = "table".tag[ReactOutput]
  /**
   * The title of a table.
   *
   *  MDN
   */
  val caption = "caption".tag[ReactOutput]
  /**
   * A set of columns.
   *
   *  MDN
   */
  val colgroup = "colgroup".tag[ReactOutput]
  /**
   * A single column.
   *
   *  MDN
   */
  val col = "col".voidTag[ReactOutput]
  /**
   * The table body.
   *
   *  MDN
   */
  val tbody = "tbody".tag[ReactOutput]
  /**
   * The table headers.
   *
   *  MDN
   */
  val thead = "thead".tag[ReactOutput]
  /**
   * The table footer.
   *
   *  MDN
   */
  val tfoot = "tfoot".tag[ReactOutput]
  /**
   * A single row in a table.
   *
   *  MDN
   */
  val tr = "tr".tag[ReactOutput]
  /**
   * A single cell in a table.
   *
   *  MDN
   */
  val td = "td".tag[ReactOutput]
  /**
   * A header cell in a table.
   *
   *  MDN
   */
  val th = "th".tag[ReactOutput]

  // Forms
  /**
   * Represents a form, consisting of controls, that can be submitted to a
   * server for processing.
   *
   *  MDN
   */
  val form = "form".tag[ReactOutput]
  /**
   * A set of fields.
   *
   *  MDN
   */
  val fieldset = "fieldset".tag[ReactOutput]
  /**
   * The caption for a fieldset.
   *
   *  MDN
   */
  val legend = "legend".tag[ReactOutput]
  /**
   * The caption of a single field
   *
   *  MDN
   */
  val label = "label".tag[ReactOutput]
  /**
   * A typed data field allowing the user to input data.
   *
   *  MDN
   */
  val input = "input".voidTag[ReactOutput]
  /**
   * A button
   *
   *  MDN
   */
  val button = "button".tag[ReactOutput]
  /**
   * A control that allows the user to select one of a set of options.
   *
   *  MDN
   */
  val select = "select".tag[ReactOutput]
  /**
   * A set of predefined options for other controls.
   *
   *  MDN
   */
  val datalist = "datalist".tag[ReactOutput]
  /**
   * A set of options, logically grouped.
   *
   *  MDN
   */
  val optgroup = "optgroup".tag[ReactOutput]
  /**
   * An option in a select element.
   *
   *  MDN
   */
  val option = "option".tag[ReactOutput]
  /**
   * A multiline text edit control.
   *
   *  MDN
   */
  val textarea = "textarea".tag[ReactOutput]
}
