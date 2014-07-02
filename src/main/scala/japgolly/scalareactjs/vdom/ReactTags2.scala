package japgolly.scalareactjs.vdom

import scalatags._
import generic.Util

/**
 * Contains HTML tags which are used less frequently. These are generally
 * imported individually as needed.
 */
trait ReactTags2 extends generic.Tags2[VDomBuilder, ReactOutput, ReactFragT]{

  // Document Metadata
  /**
   * Defines the title of the document, shown in a browser's title bar or on the
   * page's tab. It can only contain text and any contained tags are not
   * interpreted.
   *
   * MDN
   */
  val title = "title".tag[ReactOutput]

  /**
   * Used to write inline CSS.
   *
   *  MDN
   */
  val style = "style".tag[ReactOutput]
  // Scripting
  /**
   * Defines alternative content to display when the browser doesn't support
   * scripting.
   *
   *  MDN
   */
  val noscript = "noscript".tag[ReactOutput]

  // Sections
  /**
   * Represents a generic section of a document, i.e., a thematic grouping of
   * content, typically with a heading.
   *
   *  MDN
   */
  val section = "section".tag[ReactOutput]
  /**
   * Represents a section of a page that links to other pages or to parts within
   * the page: a section with navigation links.
   *
   *  MDN
   */
  val nav = "nav".tag[ReactOutput]
  /**
   * Defines self-contained content that could exist independently of the rest
   * of the content.
   *
   *  MDN
   */
  val article = "article".tag[ReactOutput]
  /**
   * Defines some content loosely related to the page content. If it is removed,
   * the remaining content still makes sense.
   *
   *  MDN
   */
  val aside = "aside".tag[ReactOutput]
  /**
   * Defines a section containing contact information.
   *
   *  MDN
   */
  val address = "address".tag[ReactOutput]

  /**
   * Defines the main or important content in the document. There is only one
   * main element in the document.
   *
   *  MDN
   */
  val main = "main".tag[ReactOutput]

  // Text level semantics

  /**
   * An inline quotation.
   *
   *  MDN
   */
  val q = "q".tag[ReactOutput]
  /**
   * Represents a term whose definition is contained in its nearest ancestor
   * content.
   *
   *  MDN
   */
  val dfn = "dfn".tag[ReactOutput]
  /**
   * An abbreviation or acronym; the expansion of the abbreviation can be
   * represented in the title attribute.
   *
   *  MDN
   */
  val abbr = "abbr".tag[ReactOutput]
  /**
   * Associates to its content a machine-readable equivalent.
   *
   *  MDN
   */
  val data = "data".tag[ReactOutput]
  /**
   * Represents a date and time value; the machine-readable equivalent can be
   * represented in the datetime attribetu
   *
   *  MDN
   */
  val time = "time".tag[ReactOutput]
  /**
   * Represents a variable.
   *
   *  MDN
   */
  val `var` = "var".tag[ReactOutput]
  /**
   * Represents the output of a program or a computer.
   *
   *  MDN
   */
  val samp = "samp".tag[ReactOutput]
  /**
   * Represents user input, often from a keyboard, but not necessarily.
   *
   *  MDN
   */
  val kbd = "kbd".tag[ReactOutput]

  /**
   * Defines a mathematical formula.
   *
   *  MDN
   */
  val math = "math".tag[ReactOutput]
  /**
   * Represents text highlighted for reference purposes, that is for its
   * relevance in another context.
   *
   *  MDN
   */
  val mark = "mark".tag[ReactOutput]
  /**
   * Represents content to be marked with ruby annotations, short runs of text
   * presented alongside the text. This is often used in conjunction with East
   * Asian language where the annotations act as a guide for pronunciation, like
   * the Japanese furigana .
   *
   *  MDN
   */
  val ruby = "ruby".tag[ReactOutput]
  /**
   * Represents the text of a ruby annotation.
   *
   *  MDN
   */
  val rt = "rt".tag[ReactOutput]
  /**
   * Represents parenthesis around a ruby annotation, used to display the
   * annotation in an alternate way by browsers not supporting the standard
   * display for annotations.
   *
   *  MDN
   */
  val rp = "rp".tag[ReactOutput]
  /**
   * Represents text that must be isolated from its surrounding for bidirectional
   * text formatting. It allows embedding a span of text with a different, or
   * unknown, directionality.
   *
   *  MDN
   */
  val bdi = "bdi".tag[ReactOutput]
  /**
   * Represents the directionality of its children, in order to explicitly
   * override the Unicode bidirectional algorithm.
   *
   *  MDN
   */
  val bdo = "bdo".tag[ReactOutput]

  // Forms

  /**
   * A key-pair generator control.
   *
   *  MDN
   */
  val keygen = "keygen".voidTag[ReactOutput]
  /**
   * The result of a calculation
   *
   *  MDN
   */
  val output = "output".tag[ReactOutput]
  /**
   * A progress completion bar
   *
   *  MDN
   */
  val progress = "progress".tag[ReactOutput]
  /**
   * A scalar measurement within a known range.
   *
   *  MDN
   */
  val meter = "meter".tag[ReactOutput]


  // Interactive elements
  /**
   * A widget from which the user can obtain additional information
   * or controls.
   *
   *  MDN
   */
  val details = "details".tag[ReactOutput]
  /**
   * A summary, caption, or legend for a given details.
   *
   *  MDN
   */
  val summary = "summary".tag[ReactOutput]
  /**
   * A command that the user can invoke.
   *
   *  MDN
   */
  val command = "command".voidTag[ReactOutput]
  /**
   * A list of commands
   *
   *  MDN
   */
  val menu = "menu".tag[ReactOutput]
}
