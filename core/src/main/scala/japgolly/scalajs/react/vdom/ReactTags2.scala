package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.ReactElement
import scalatags._
import generic.Util

/**
 * Contains HTML tags which are used less frequently. These are generally
 * imported individually as needed.
 */
trait ReactTags2 extends generic.Tags2[VDomBuilder, ReactElement, ReactFragT]{

  // Document Metadata
  /**
   * Defines the title of the document, shown in a browser's title bar or on the
   * page's tab. It can only contain text and any contained tags are not
   * interpreted.
   *
   * MDN
   */
  val title = "title".tag[ReactElement]

  /**
   * Used to write inline CSS.
   *
   *  MDN
   */
  val style = "style".tag[ReactElement]
  // Scripting
  /**
   * Defines alternative content to display when the browser doesn't support
   * scripting.
   *
   *  MDN
   */
  val noscript = "noscript".tag[ReactElement]

  // Sections
  /**
   * Represents a generic section of a document, i.e., a thematic grouping of
   * content, typically with a heading.
   *
   *  MDN
   */
  val section = "section".tag[ReactElement]
  /**
   * Represents a section of a page that links to other pages or to parts within
   * the page: a section with navigation links.
   *
   *  MDN
   */
  val nav = "nav".tag[ReactElement]
  /**
   * Defines self-contained content that could exist independently of the rest
   * of the content.
   *
   *  MDN
   */
  val article = "article".tag[ReactElement]
  /**
   * Defines some content loosely related to the page content. If it is removed,
   * the remaining content still makes sense.
   *
   *  MDN
   */
  val aside = "aside".tag[ReactElement]
  /**
   * Defines a section containing contact information.
   *
   *  MDN
   */
  val address = "address".tag[ReactElement]

  /**
   * Defines the main or important content in the document. There is only one
   * main element in the document.
   *
   *  MDN
   */
  val main = "main".tag[ReactElement]

  // Text level semantics

  /**
   * An inline quotation.
   *
   *  MDN
   */
  val q = "q".tag[ReactElement]
  /**
   * Represents a term whose definition is contained in its nearest ancestor
   * content.
   *
   *  MDN
   */
  val dfn = "dfn".tag[ReactElement]
  /**
   * An abbreviation or acronym; the expansion of the abbreviation can be
   * represented in the title attribute.
   *
   *  MDN
   */
  val abbr = "abbr".tag[ReactElement]
  /**
   * Associates to its content a machine-readable equivalent.
   *
   *  MDN
   */
  val data = "data".tag[ReactElement]
  /**
   * Represents a date and time value; the machine-readable equivalent can be
   * represented in the datetime attribetu
   *
   *  MDN
   */
  val time = "time".tag[ReactElement]
  /**
   * Represents a variable.
   *
   *  MDN
   */
  val `var` = "var".tag[ReactElement]
  /**
   * Represents the output of a program or a computer.
   *
   *  MDN
   */
  val samp = "samp".tag[ReactElement]
  /**
   * Represents user input, often from a keyboard, but not necessarily.
   *
   *  MDN
   */
  val kbd = "kbd".tag[ReactElement]

  /**
   * Defines a mathematical formula.
   *
   *  MDN
   */
  val math = "math".tag[ReactElement]
  /**
   * Represents text highlighted for reference purposes, that is for its
   * relevance in another context.
   *
   *  MDN
   */
  val mark = "mark".tag[ReactElement]
  /**
   * Represents content to be marked with ruby annotations, short runs of text
   * presented alongside the text. This is often used in conjunction with East
   * Asian language where the annotations act as a guide for pronunciation, like
   * the Japanese furigana .
   *
   *  MDN
   */
  val ruby = "ruby".tag[ReactElement]
  /**
   * Represents the text of a ruby annotation.
   *
   *  MDN
   */
  val rt = "rt".tag[ReactElement]
  /**
   * Represents parenthesis around a ruby annotation, used to display the
   * annotation in an alternate way by browsers not supporting the standard
   * display for annotations.
   *
   *  MDN
   */
  val rp = "rp".tag[ReactElement]
  /**
   * Represents text that must be isolated from its surrounding for bidirectional
   * text formatting. It allows embedding a span of text with a different, or
   * unknown, directionality.
   *
   *  MDN
   */
  val bdi = "bdi".tag[ReactElement]
  /**
   * Represents the directionality of its children, in order to explicitly
   * override the Unicode bidirectional algorithm.
   *
   *  MDN
   */
  val bdo = "bdo".tag[ReactElement]

  // Forms

  /**
   * A key-pair generator control.
   *
   *  MDN
   */
  val keygen = "keygen".voidTag[ReactElement]
  /**
   * The result of a calculation
   *
   *  MDN
   */
  val output = "output".tag[ReactElement]
  /**
   * A progress completion bar
   *
   *  MDN
   */
  val progress = "progress".tag[ReactElement]
  /**
   * A scalar measurement within a known range.
   *
   *  MDN
   */
  val meter = "meter".tag[ReactElement]


  // Interactive elements
  /**
   * A widget from which the user can obtain additional information
   * or controls.
   *
   *  MDN
   */
  val details = "details".tag[ReactElement]
  /**
   * A summary, caption, or legend for a given details.
   *
   *  MDN
   */
  val summary = "summary".tag[ReactElement]
  /**
   * A command that the user can invoke.
   *
   *  MDN
   */
  val command = "command".voidTag[ReactElement]
  /**
   * A list of commands
   *
   *  MDN
   */
  val menu = "menu".tag[ReactElement]
}
