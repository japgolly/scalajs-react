package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.raw.JsNumber
import japgolly.scalajs.react.vdom.PackageBase._
import scala.scalajs.js
import scala.scalajs.js.|

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
    def activeDescendant = VdomAttr[String]("aria-activedescendant")

    /**
      * Indicates whether assistive technologies will present all, or only parts of, the changed region based on the change notifications defined by the aria-relevant attribute. See related aria-relevant.
      */
    def atomic = VdomAttr[Boolean]("aria-atomic")

    /**
      * Indicates whether user input completion suggestions are provided.
      */
    object autoComplete extends Attr.Generic[String]("aria-autocomplete") {
      /** When a user is providing input, text suggesting one way to complete the provided input may be dynamically inserted after the caret. */
      def inline = this := "inline"

      /** When a user is providing input, an element containing a collection of values that could complete the provided input may be displayed. */
      def list = this := "list"

      /** When a user is providing input, an element containing a collection of values that could complete the provided input may be displayed. If displayed, one value in the collection is automatically selected, and the text needed to complete the automatically selected value appears after the caret in the input. */
      def both = this := "both"

      /** (default) When a user is providing input, an automatic suggestion that attempts to predict how the user intends to complete the input is not displayed. */
      def none = this := "none"
    }

    /**
      * Indicates whether an element, and its subtree, are currently being updated.
      */
    def busy = VdomAttr[Boolean]("aria-busy")

    /**
      * Indicates the current "checked" state of checkboxes, radio buttons, and other widgets. See related aria-pressed and aria-selected.
      */
    object checked extends Attr.Generic[String | Boolean]("aria-checked") {
      def `false` = this := false
      def `true`  = this := true
      def mixed   = this := "mixed"
    }

    /** Defines the total number of columns in a table, grid, or treegrid. See related aria-colindex.
      *
      * If all of the columns are present in the DOM, it is not necessary to set this attribute as the user agent can automatically calculate the total number of columns. However, if only a portion of the columns is present in the DOM at a given moment, this attribute is needed to provide an explicit indication of the number of columns in the full table.
      *
      * Authors MUST set the value of aria-colcount to an integer equal to the number of columns in the full table. If the total number of columns is unknown, authors MUST set the value of aria-colcount to -1 to indicate that the value should not be calculated by the user agent.
      */
    def colCount = VdomAttr[Int]("aria-colcount")

    /** Defines an element's column index or position with respect to the total number of columns within a table, grid, or treegrid. See related aria-colcount and aria-colspan.
      *
      * If all of the columns are present in the DOM, it is not necessary to set this attribute as the user agent can automatically calculate the column index of each cell or gridcell. However, if only a portion of the columns is present in the DOM at a given moment, this attribute is needed to provide an explicit indication of the column of each cell or gridcell with respect to the full table.
      *
      * Authors MUST set the value for aria-colindex to an integer greater than or equal to 1, greater than the aria-colindex value of any previous elements within the same row, and less than or equal to the number of columns in the full table. For a cell or gridcell which spans multiple columns, authors MUST set the value of aria-colindex to the start of the span.
      *
      * If the set of columns which is present in the DOM is contiguous, and if there are no cells which span more than one row or column in that set, then authors MAY place aria-colindex on each row, setting the value to the index of the first column of the set. Otherwise, authors SHOULD place aria-colindex on all of the children or owned elements of each row.
      */
    def colIndex = VdomAttr[Int]("aria-colindex")

    /** Defines the number of columns spanned by a cell or gridcell within a table, grid, or treegrid. See related aria-colindex and aria-rowspan.
      *
      * This attribute is intended for cells and gridcells which are not contained in a native table. When defining the column span of cells or gridcells in a native table, authors SHOULD use the host language's attribute instead of aria-colspan. If aria-colspan is used on an element for which the host language provides an equivalent attribute, user agents MUST ignore the value of aria-colspan and instead expose the value of the host language's attribute to assistive technologies.
      *
      * Authors MUST set the value of aria-colspan to an integer greater than or equal to 1 and less than the value which would cause the cell or gridcell to overlap the next cell or gridcell in the same row.
      */
    def colSpan = VdomAttr[Int]("aria-colspan")

    /**
      * Identifies the element (or elements) whose contents or presence are controlled by the current element. See related aria-owns.
      */
    def controls = VdomAttr("aria-controls")

    /** Indicates the element that represents the current item within a container or set of related elements.
      *
      * The aria-current attribute is an enumerated type. Any value not included in the list of allowed values SHOULD be treated by assistive technologies as if the value true had been provided. If the attribute is not present or its value is an empty string or undefined, the default value of false applies and the aria-current state MUST NOT be exposed by user agents or assistive technologies.
      *
      * The aria-current attribute is used when an element within a set of related elements is visually styled to indicate it is the current item in the set. For example:
      *
      * A page token used to indicate a link within a set of pagination links, where the link is visually styled to represent the currently-displayed page.
      * A step token used to indicate a link within a step indicator for a step-based process, where the link is visually styled to represent the current step.
      * A location token used to indicate the image that is visually highlighted as the current component of a flow chart.
      * A date token used to indicate the current date within a calendar.
      * A time token used to indicate the current time within a timetable.
      * Authors SHOULD only mark one element in a set of elements as current with aria-current.
      *
      * Authors SHOULD NOT use the aria-current attribute as a substitute for aria-selected in widgets where aria-selected has the same meaning. For example, in a tablist, aria-selected is used on a tab to indicate the currently-displayed tabpanel.
      */
    object current extends Attr.Generic[String | Boolean]("aria-current") {
      /** Represents the current page within a set of pages. */
      def page = this := "page"

      /** Represents the current step within a process. */
      def step = this := "step"

      /** Represents the current location within an environment or context. */
      def location = this := "location"

      /** Represents the current date within a collection of dates. */
      def date = this := "date"

      /** Represents the current time within a set of times. */
      def time = this := "time"

      /** Represents the current item within a set. */
      def `true` = this := true

      /** (default) Does not represent the current item within a set. */
      def `false` = this := false
    }

    /**
      * Identifies the element (or elements) that describes the object. See related aria-labelledby.
      */
    def describedBy = VdomAttr("aria-describedby")

    /** Identifies the element that provides a detailed, extended description for the object. See related aria-describedby.
      *
      * The aria-details attribute references a single element that provides more detailed information than would normally be provided by aria-describedby. It enables assistive technologies to make users aware of the availability of an extended description as well as navigate to it. Authors SHOULD ensure the element referenced by aria-details is visible to all users.
      *
      * Unlike elements referenced by aria-describedby, the element referenced by aria-details is not used in either the Accessible Name Computation or the Accessible Description Computation as defined in the Accessible Name and Description specification [accname-aam-1.1]. Thus, the content of an element referenced by aria-details is not flattened to a string when presented to assistive technology users. This makes aria-details particularly useful when converting the information to a string would cause a loss of information or make the extended description more difficult to understand.
      *
      * In some user agents, multiple reference relationships for descriptive information are not supported by the accessibility API. In such cases, if both aria-describedby and aria-details are provided on an element, aria-details takes precedence.
      */
    def details = VdomAttr[String]("aria-details")

    /**
      * Indicates that the element is perceivable but disabled, so it is not editable or otherwise operable. See related aria-hidden and aria-readonly.
      */
    def disabled = VdomAttr[Boolean]("aria-disabled")

    /**
      * Indicates what functions can be performed when the dragged object is released on the drop target. This allows assistive technologies to convey the possible drag options available to users, including whether a pop-up menu of choices is provided by the application. Typically, drop effect functions can only be provided once an object has been grabbed for a drag operation as the drop effect functions available are dependent on the object being dragged.
      */
    def dropEffect = VdomAttr[String]("aria-dropeffect")

    /** Identifies the element that provides an error message for the object. See related aria-invalid and aria-describedby.
      *
      * The aria-errormessage attribute references another element that contains custom error message text. Authors MUST use aria-invalid in conjunction with aria-errormessage. Initially, the object is in a valid state and either has aria-invalid set to false or no aria-invalid attribute, and the element referenced by aria-errormessage is not applicable. If the user enters an invalid value for the object, aria-invalid is set to true to indicate that aria-errormessage is now pertinent. When aria-errormessage is pertinent, authors MUST ensure the content is not hidden and is included in a container that exposes the content to the user as it is expected that the assistive technology user will navigate to the content in order to access it.
      *
      * Authors MAY use live regions for the error message element applying either an aria-live property or using one of the live region roles, for example, alert. A live region scenario is when an error message is displayed to users only after they have provided invalid information. The message describes what is wrong and advises users as to what is required.
      */
    def errorMessage = VdomAttr[String]("aria-errormessage")

    /**
      * Indicates whether the element, or another grouping element it controls, is currently expanded or collapsed.
      */
    def expanded = VdomAttr[Boolean]("aria-expanded")

    /**
      * Identifies the next element (or elements) in an alternate reading order of content which, at the user's discretion, allows assistive technology to override the general default of reading in document source order.
      */
    def flowTo = VdomAttr("aria-flowto")

    /**
      * Indicates an element's "grabbed" state in a drag-and-drop operation.
      */
    def grabbed = VdomAttr[Boolean]("aria-grabbed")

    /** Indicates the availability and type of interactive popup element, such as menu or dialog, that can be triggered by an element.
      *
      * A popup element usually appears as a block of content that is on top of other content. Authors MUST ensure that the role of the element that serves as the container for the popup content is menu, listbox, tree, grid, or dialog, and that the value of aria-haspopup matches the role of the popup container.
      *
      * For the popup element to be keyboard accessible, authors SHOULD ensure that the element that can trigger the popup is focusable, that there is a keyboard mechanism for opening the popup, and that the popup element manages focus of all its descendants as described in Managing Focus.
      *
      * The aria-haspopup property is an enumerated type. User agents MUST treat any value of aria-haspopup that is not included in the list of allowed values, including an empty string, as if the value false had been provided. To provide backward compatibility with ARIA 1.0 content, user agents MUST treat an aria-haspopup value of true as equivalent to a value of menu.
      *
      * Assistive technologies SHOULD NOT expose the aria-haspopup property if it has a value of false.
      */
    object haspopup extends Attr.Generic[String | Boolean]("aria-haspopup") {
      /** (default) Indicates the element does not have a popup. */
      def `false` = this := false

      /** Indicates the popup is a menu. */
      def `true` = this := true

      /** Indicates the popup is a menu. */
      def menu = this := "menu"

      /** Indicates the popup is a listbox. */
      def listbox = this := "listbox"

      /** Indicates the popup is a tree. */
      def tree = this := "tree"

      /** Indicates the popup is a grid. */
      def grid = this := "grid"

      /** Indicates the popup is a dialog. */
      def dialog = this := "dialog"
    }

    /**
      * Indicates that the element and all of its descendants are not visible or perceivable to any user as implemented by the author. See related aria-disabled.
      */
    def hidden = VdomAttr[Boolean]("aria-hidden")

    /** Indicates the entered value does not conform to the format expected by the application. See related aria-errormessage.
      *
      * If the value is computed to be invalid or out-of-range, the application author SHOULD set this attribute to true. User agents SHOULD inform the user of the error. Application authors SHOULD provide suggestions for corrections if they are known.
      *
      * When the user attempts to submit data involving a field for which aria-required is true, authors MAY use the aria-invalid attribute to signal there is an error. However, if the user has not attempted to submit the form, authors SHOULD NOT set the aria-invalid attribute on required widgets simply because the user has not yet entered data.
      *
      * For future expansion, the aria-invalid attribute is an enumerated type. Any value not recognized in the list of allowed values MUST be treated by user agents as if the value true had been provided. If the attribute is not present, or its value is false, or its value is an empty string, the default value of false applies.
      */
    object invalid extends Attr.Generic[String | Boolean]("aria-invalid") {
      /** (default) There are no detected errors in the value. */
      def `false` = this := false

      /** The value entered by the user has failed validation. */
      def `true` = this := true

      /** A grammatical error was detected. */
      def grammar = this := "grammar"

      /** A spelling error was detected. */
      def spelling = this := "spelling"
    }

    /** Indicates keyboard shortcuts that an author has implemented to activate or give focus to an element.
      *
      * The value of the aria-keyshortcuts attribute is a space-delimited list of keyboard shortcuts that can be pressed to activate a command or textbox widget. The keys defined in the shortcuts represent the physical keys pressed and not the actual characters generated. Each keyboard shortcut consists of one or more tokens delimited by the plus sign ("+") representing zero or more modifier keys and exactly one non-modifier key that must be pressed simultaneously to activate the given shortcut.
      *
      * Authors MUST specify modifier keys exactly according to the UI Events KeyboardEvent key Values spec [uievents-key] - for example, "Alt", "Control", "Shift", "Meta", or "AltGraph". Note that Meta corresponds to the Command key, and Alt to the Option key, on Apple computers.
      *
      * The valid names for non-modifier keys are any printable character such as "A", "B", "1", "2", "$", "Plus" for a plus sign, "Space" for the spacebar, or the names of any other non-modifier key specified in the UI Events KeyboardEvent key Values spec [uievents-key] - for example, "Enter", "Tab", "ArrowRight", "PageDown", "Escape", or "F1". The use of "Space" for the spacebar is an exception to the UI Events KeyboardEvent key Values spec [uievents-key] as the space or spacebar key is encoded as ' ' and would be treated as a whitespace character.
      *
      * Authors MUST ensure modifier keys come first when they are part of a keyboard shortcut. Authors MUST ensure that required non-modifier keys come last when they are part of a shortcut. The order of the modifier keys is not otherwise significant, so "Alt+Shift+T" and "Shift+Alt+T" are equivalent, but "T+Shift+Alt" is not valid because all of the modifier keys don't come first, and "Alt" is not valid because it doesn't include at least one non-modifier key.
      *
      * When specifying an alphabetic key, both the uppercase and lowercase variants are considered equivalent: "a" and "A" are the same.
      *
      * When implementing keyboard shortcuts authors should consider the keyboards they intend to support to avoid unintended results. Keyboard designs vary significantly based on the device used and the languages supported. For example, many modifier keys are used in conjunction with other keys to create common punctuation symbols, create number characters, swap keyboard sides on bilingual keyboards to switch languages, and perform a number of other functions.
      *
      * For many supported keyboards, authors can prevent conflicts by avoiding keys other than ASCII letters, as number characters and common punctuation often require modifiers. Here, the keyboard shortcut entered does not equate to the key generated. For example, in French keyboard layouts, the number characters are not available until you press the Control key, so a keyboard shortcut defined as "Control+2" would be ambiguous as this is how one would type the "2" character on a French keyboard.
      *
      * If the character used is determined by a modifier key, the author MUST specify the actual key used to generate the character, that is generated by the key, and not the resulting character. This convention enables the assistive technology to accurately convey what keys must be used to generate the shortcut. For example, on most U.S. English keyboards, the percent sign "%" can be input by pressing Shift+5. The correct way to specify this shortcut is "Shift+5". It is incorrect to specify "%" or "Shift+%". However, note that on some international keyboards the percent sign may be an unmodified key, in which case "%" and "Shift+%" could be correct on those keyboards.
      *
      * If the key that needs to be specified is illegal in the host language or would cause a string to be terminated, authors MUST use the string escaping sequence of the host language to specify it. For example, the double-quote character can be encoded as "Shift+&#39;" in HTML.
      *
      * Examples of valid keyboard shortcuts include:
      *
      *   - "A"
      *   - "Shift+Space"
      *   - "Control+Alt+."
      *   - "Control+Shift+&#39;"
      *   - "Alt+Shift+P Control+F"
      *   - "Meta+C Meta+Shift+C"
      *
      * User agents MUST NOT change keyboard behavior in response to the aria-keyshortcuts attribute. Authors MUST handle scripted keyboard events to process aria-keyshortcuts. The aria-keyshortcuts attribute exposes the existence of these shortcuts so that assistive technologies can communicate this information to users.
      *
      * Authors SHOULD provide a way to expose keyboard shortcuts so that all users may discover them, such as through the use of a tooltip. Authors MUST ensure that aria-keyshortcuts applied to disabled elements are unavailable.
      *
      * Authors SHOULD avoid implementing shortcut keys that inhibit operating system, user agent, or assistive technology functionality. This requires the author to carefully consider both which keys to assign and the contexts and conditions in which the keys are available to the user. For guidance, see the keyboard shortcuts section of the WAI-ARIA Authoring Practices Guide [wai-aria-practices-1.1].
      */
    def keyShortcuts = VdomAttr[String]("aria-keyshortcuts")

    /**
      * Defines a string value that labels the current element. See related aria-labelledby.
      */
    def label = VdomAttr[String]("aria-label")

    /**
      * Identifies the element (or elements) that labels the current element. See related aria-label and aria-describedby.
      */
    def labelledBy = VdomAttr[String]("aria-labelledby")

    /** Defines the hierarchical level of an element within a structure.
      *
      * This can be applied inside trees to tree items, to headings inside a document, to nested grids, nested tablists and to other structural items that may appear inside a container or participate in an ownership hierarchy. The value for aria-level is an integer greater than or equal to 1.
      *
      * Levels increase with depth. If the DOM ancestry does not accurately represent the level, authors SHOULD explicitly define the aria-level attribute.
      *
      * This attribute is applied to elements that act as leaf nodes within the orientation of the set, for example, on elements with role treeitem rather than elements with role group. This means that multiple elements in a set may have the same value for this attribute. Although it would be less repetitive to provide a single value on the container, restricting this to leaf nodes ensures that there is a single way for assistive technologies to use the attribute.
      *
      * If the DOM ancestry accurately represents the level, the user agent can calculate the level of an item from the document structure. This attribute can be used to provide an explicit indication of the level when that is not possible to calculate from the document structure or the aria-owns attribute. User agent support for automatic calculation of level may vary; authors SHOULD test with user agents and assistive technologies to determine whether this attribute is needed. If the author intends for the user agent to calculate the level, the author SHOULD omit this attribute.
      */
    def level = VdomAttr[Int]("aria-level")

    /** Indicates that an element will be updated, and describes the types of updates the user agents, assistive technologies, and user can expect from the live region.
      *
      * The values of this attribute are expressed in degrees of importance. When regions are specified as polite, assistive technologies will notify users of updates but generally do not interrupt the current task, and updates take low priority. When regions are specified as assertive, assistive technologies will immediately notify the user, and could potentially clear the speech queue of previous updates.
      *
      * Politeness levels are essentially an ordering mechanism for updates and serve as a strong suggestion to user agents or assistive technologies. The value may be overridden by user agents, assistive technologies, or the user. For example, if assistive technologies can determine that a change occurred in response to a key press or a mouse click, the assistive technologies may present that change immediately even if the value of the aria-live attribute states otherwise.
      *
      * Since different users have different needs, it is up to the user to tweak his or her assistive technologies' response to a live region with a certain politeness level from the commonly defined baseline. Assistive technologies may choose to implement increasing and decreasing levels of granularity so that the user can exercise control over queues and interruptions.
      *
      * When the property is not set on an object that needs to send updates, the politeness level is the value of the nearest ancestor that sets the aria-live attribute.
      *
      * The aria-live attribute is the primary determination for the order of presentation of changes to live regions. Implementations will also consider the default level of politeness in a role when the aria-live attribute is not set in the ancestor chain (e.g., log changes are polite by default). Items which are assertive will be presented immediately, followed by polite items. User agents or assistive technologies MAY choose to clear queued changes when an assertive change occurs. (e.g., changes in an assertive region may remove all currently queued changes)
      *
      * When live regions are marked as polite, assistive technologies SHOULD announce updates at the next graceful opportunity, such as at the end of speaking the current sentence or when the user pauses typing. When live regions are marked as assertive, assistive technologies SHOULD notify the user immediately. Because an interruption may disorient users or cause them to not complete their current task, authors SHOULD NOT use the assertive value unless the interruption is imperative.
      */
    object live extends Attr.Generic[String]("aria-live") {

      /** (Default) Indicates that updates to the region should not be presented to the user unless the used is currently focused on that region. */
      def off = this := "off"

      /** Indicates that updates to the region have the highest priority and should be presented the user immediately. */
      def assertive = this := "assertive"

      /** Indicates that updates to the region should be presented at the next graceful opportunity, such as at the end of speaking the current sentence or when the user pauses typing. */
      def polite = this := "polite"
    }

    /** Indicates whether an element is modal when displayed.
      *
      * The aria-modal attribute is used to indicate that the presence of a "modal" element precludes usage of other content on the page. For example, when a modal dialog is displayed, it is expected that the user's interaction is limited to the contents of the dialog, until the modal dialog loses focus or is no longer displayed.
      *
      * When a modal element is displayed, assistive technologies SHOULD navigate to the element unless focus has explicitly been set elsewhere. Assistive technologies MAY limit navigation to the modal element's contents. If focus moves to an element outside the modal element, assistive technologies SHOULD NOT limit navigation to the modal element.
      *
      * When a modal element is displayed, authors MUST ensure the interface can be controlled using only descendants of the modal element. In other words, if a modal dialog has a close button, the button should be a descendant of the dialog. When a modal element is displayed, authors SHOULD mark all other contents as inert (such as "inert subtrees" in HTML) if the ability to do so exists in the host language.
      */
    def modal = VdomAttr[Boolean]("aria-modal")

    /**
      * Indicates whether a text box accepts multiple lines of input or only a single line.
      */
    def multiline = VdomAttr[Boolean]("aria-multiline")

    /**
      * Indicates that the user may select more than one item from the current selectable descendants.
      */
    def multiselectable = VdomAttr[Boolean]("aria-multiselectable")

    /**
      * Indicates whether the element and orientation is horizontal or vertical.
      */
    object orientation extends Attr.Generic[String]("aria-orientation") {
      def horizontal = this := "horizontal"
      def vertical   = this := "vertical"
    }

    /**
      * Identifies an element (or elements) in order to define a visual, functional, or contextual parent/child relationship between DOM elements where the DOM hierarchy cannot be used to represent the relationship. See related aria-controls.
      */
    def owns = VdomAttr("aria-owns")

    /** Defines a short hint (a word or short phrase) intended to aid the user with data entry when the control has no value. A hint could be a sample value or a brief description of the expected format.
      *
      * Authors SHOULD NOT use aria-placeholder instead of a label as their purposes are different: The label indicates what kind of information is expected. The placeholder text is a hint about the expected value. See related aria-labelledby and aria-label.
      *
      * Authors SHOULD present this hint to the user by displaying the hint text at any time the control's value is the empty string. This includes cases where the control first receives focus, and when users remove a previously-entered value.
      */
    def placeholder = VdomAttr[String]("aria-placeholder")

    /**
      * Defines an element's number or position in the current set of listitems or treeitems. Not required if all elements in the set are present in the DOM. See related aria-setsize.
      */
    def posInSet = VdomAttr[Int]("aria-posinset")

    /**
      * Indicates the current "pressed" state of toggle buttons. See related aria-checked and aria-selected.
      */
    object pressed extends Attr.Generic[String | Boolean]("aria-pressed") {
      def `false` = this := false
      def `true`  = this := true
      def mixed   = this := "mixed"
    }

    /**
      * Indicates that the element is not editable, but is otherwise operable. See related aria-disabled.
      */
    def readonly = VdomAttr[Boolean]("aria-readonly")

    /** aria-relevant is an optional attribute of live regions. This is a suggestion to assistive technologies, but assistive technologies are not required to present changes of all the relevant types.
      *
      * When aria-relevant is not defined, an element's value is inherited from the nearest ancestor with a defined value. Although the value is a token list, inherited values are not additive; the value provided on a descendant element completely overrides any inherited value from an ancestor element.
      *
      * When text changes are denoted as relevant, user agents MUST monitor any descendant node change that affects the text alternative computation of the live region as if the accessible name were determined from contents (nameFrom: contents). For example, a text change would be triggered if the HTML alt attribute of a contained image changed. However, no change would be triggered if there was a text change to a node outside the live region, even if that node was referenced (via aria-labelledby) by an element contained in the live region.
      */
    object relevant extends Attr.Generic[String]("aria-relevant") {

      /** Element nodes are added to the accessibility tree within the live region. */
      def additions = this := "additions"

      /** Equivalent to the combination of values, "additions text". */
      def additionsText = this := "additions text"

      /** Equivalent to the combination of all values, "additions removals text". */
      def all = this := "all"

      /** Text content, a text alternative, or an element node within the live region is removed from the accessibility tree. */
      def removals = this := "removals"

      /** Text content or a text alternative is added to any descendant in the accessibility tree of the live region. */
      def text = this := "text"
    }

    /**
      * Indicates that user input is required on the element before a form may be submitted.
      */
    def required = VdomAttr[Boolean]("aria-required")

    /** Defines a human-readable, author-localized description for the role of an element.
      *
      * Some assistive technologies, such as screen readers, present the role of an element as part of the user experience. Such assistive technologies typically localize the name of the role, and they may customize it as well. Users of these assistive technologies depend on the presentation of the role name, such as "region," "button," or "slider," for an understanding of the purpose of the element and, if it is a widget, how to interact with it.
      *
      * The aria-roledescription property gives authors the ability to override how assistive technologies localize and express the name of a role. Thus inappropriately using aria-roledescription may inhibit users' ability to understand or interact with an element. Authors SHOULD limit use of aria-roledescription to clarifying the purpose of non-interactive container roles like group or region, or to providing a more specific description of a widget.
      */
    def roleDescription = VdomAttr[String]("aria-roledescription")

    /** Defines the total number of rows in a table, grid, or treegrid. See related aria-rowindex.
      *
      * If all of the rows are present in the DOM, it is not necessary to set this attribute as the user agent can automatically calculate the total number of rows. However, if only a portion of the rows is present in the DOM at a given moment, this attribute is needed to provide an explicit indication of the number of rows in the full table.
      *
      * Authors MUST set the value of aria-rowcount to an integer equal to the number of rows in the full table. If the total number of rows is unknown, authors MUST set the value of aria-rowcount to -1 to indicate that the value should not be calculated by the user agent.
      */
    def rowCount = VdomAttr[String]("aria-rowcount")

    /** Defines an element's row index or position with respect to the total number of rows within a table, grid, or treegrid. See related aria-rowcount and aria-rowspan.
      *
      * If all of the rows are present in the DOM, it is not necessary to set this attribute as the user agent can automatically calculate the index of each row. However, if only a portion of the rows is present in the DOM at a given moment, this attribute is needed to provide an explicit indication of each row's position with respect to the full table.
      *
      * Authors MUST set the value for aria-rowindex to an integer greater than or equal to 1, greater than the aria-rowindex value of any previous rows, and less than or equal to the number of rows in the full table. For a cell or gridcell which spans multiple rows, authors MUST set the value of aria-rowindex to the start of the span.
      *
      * Authors SHOULD place aria-rowindex on each row. Authors MAY also place aria-rowindex on all of the children or owned elements of each row.
      */
    def rowIndex = VdomAttr[Int]("aria-rowindex")

    /** Defines the number of rows spanned by a cell or gridcell within a table, grid, or treegrid. See related aria-rowindex and aria-colspan.
      *
      * This attribute is intended for cells and gridcells which are not contained in a native table. When defining the row span of cells or gridcells in a native table, authors SHOULD use the host language's attribute instead of aria-rowspan. If aria-rowspan is used on an element for which the host language provides an equivalent attribute, user agents MUST ignore the value of aria-rowspan and instead expose the value of the host language's attribute to assistive technologies.
      *
      * Authors MUST set the value of aria-rowspan to an integer greater than or equal to 0 and less than the value which would cause the cell or gridcell to overlap the next cell or gridcell in the same column. Setting the value to 0 indicates that the cell or gridcell is to span all the remaining rows in the row group.
      */
    def rowSpan = VdomAttr[Int]("aria-rowspan")

    /**
      * Indicates the current "selected" state of various widgets. See related aria-checked and aria-pressed.
      */
    def selected = VdomAttr[Boolean]("aria-selected")

    /**
      * Defines the number of items in the current set of listitems or treeitems. Not required if all elements in the set are present in the DOM. See related aria-posinset.
      */
    def setSize = VdomAttr[Int]("aria-setsize")

    /**
      * Indicates if items in a table or grid are sorted in ascending or descending order.
      */
    object sort extends Attr.Generic[String]("aria-sort") {
      /** Items are sorted in ascending order by this column. */
      def ascending = this := "ascending"

      /** Items are sorted in descending order by this column. */
      def descending = this := "descending"

      /** (default) There is no defined sort applied to the column. */
      def none = this := "none"

      /** A sort algorithm other than ascending or descending has been applied. */
      def other = this := "other"
    }

    /**
      * Defines the maximum allowed value for a range widget.
      */
    def valueMax = VdomAttr[JsNumber]("aria-valuemax")

    /**
      * Defines the minimum allowed value for a range widget.
      */
    def valueMin = VdomAttr[JsNumber]("aria-valuemin")

    /**
      * Defines the current value for a range widget. See related aria-valuetext.
      */
    def valueNow = VdomAttr[JsNumber]("aria-valuenow")

    /**
      * Defines the human readable text alternative of aria-valuenow for a range widget.
      */
    def valueText = VdomAttr[String]("aria-valuetext")
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

  final def disablePictureInPicture = VdomAttr[Boolean]("disablePictureInPicture")

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
  final val onChange = Attr.Event.form("onChange")

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

  final def onInput = Attr.Event.form("onInput")

  final def onInputCapture = Attr.Event.form("onInputCapture")

  final def onInvalid = Attr.Event.form("onInvalid")

  final def onInvalidCapture = Attr.Event.form("onInvalidCapture")

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
  final def onReset = Attr.Event.form("onReset")

  final def onResetCapture = Attr.Event.form("onResetCapture")

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
  final def onSubmit = Attr.Event.form("onSubmit")

  final def onSubmitCapture = Attr.Event.form("onSubmitCapture")

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

  /** The pattern attribute specifies a regular expression against which the control's value, or, when the multiple
    * attribute applies and is set, the control's values, are to be checked.
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
    * @see https://www.w3.org/TR/wai-aria-1.1/#role_definitions
    */
  object role extends Attr.Generic[String]("role") {

    /** A type of live region with important, and usually time-sensitive, information. See related alertdialog and status. */
    def alert = this := "alert"

    /** A type of dialog that contains an alert message, where initial focus goes to an element within the dialog. See related alert and dialog. */
    def alertdialog = this := "alertdialog"

    /** A structure containing one or more focusable elements requiring user input, such as keyboard or gesture events, that do not follow a standard interaction pattern supported by a widget role. */
    def application = this := "application"

    /** A section of a page that consists of a composition that forms an independent part of a document, page, or site. */
    def article = this := "article"

    /** A region that contains mostly site-oriented content, rather than page-specific content. */
    def banner = this := "banner"

    /** An input that allows for user-triggered actions when clicked or pressed. See related link. */
    def button = this := "button"

    /** A cell in a tabular container. See related gridcell. */
    def cell = this := "cell"

    /** A checkable input that has three possible values: true, false, or mixed. */
    def checkbox = this := "checkbox"

    /** A cell containing header information for a column. */
    def columnheader = this := "columnheader"

    /** A composite widget containing a single-line textbox and another element, such as a listbox or grid, that can dynamically pop up to help the user set the value of the textbox. */
    def combobox = this := "combobox"

    /** A form of widget that performs an action but does not receive input data. */
    def command = this := "command"

    /** A supporting section of the document, designed to be complementary to the main content at a similar level in the DOM hierarchy, but remains meaningful when separated from the main content. */
    def complementary = this := "complementary"

    /** A widget that may contain navigable descendants or owned children. */
    def composite = this := "composite"

    /** A large perceivable region that contains information about the parent document. */
    def contentinfo = this := "contentinfo"

    /** A definition of a term or concept. See related term. */
    def definition = this := "definition"

    /** A dialog is a descendant window of the primary window of a web application. For HTML pages, the primary application window is the entire web document, i.e., the body element. */
    def dialog = this := "dialog"

    /** A list of references to members of a group, such as a static table of contents. */
    def directory = this := "directory"

    /** An element containing content that assistive technology users may want to browse in a reading mode. */
    def document = this := "document"

    /** A scrollable list of articles where scrolling may cause articles to be added to or removed from either end of the list. */
    def feed = this := "feed"

    /** A perceivable section of content that typically contains a graphical document, images, code snippets, or example text. The parts of a figure MAY be user-navigable. */
    def figure = this := "figure"

    /** A landmark region that contains a collection of items and objects that, as a whole, combine to create a form. See related search. */
    def form = this := "form"

    /** A composite widget containing a collection of one or more rows with one or more cells where some or all cells in the grid are focusable by using methods of two-dimensional navigation, such as directional arrow keys. */
    def grid = this := "grid"

    /** A cell in a grid or treegrid. */
    def gridcell = this := "gridcell"

    /** A set of user interface objects which are not intended to be included in a page summary or table of contents by assistive technologies. */
    def group = this := "group"

    /** A heading for a section of the page. */
    def heading = this := "heading"

    /** A container for a collection of elements that form an image. */
    def img = this := "img"

    /** A generic type of widget that allows user input. */
    def input = this := "input"

    /** A perceivable section containing content that is relevant to a specific, author-specified purpose and sufficiently important that users will likely want to be able to navigate to the section easily and to have it listed in a summary of the page. Such a page summary could be generated dynamically by a user agent or assistive technology. */
    def landmark = this := "landmark"

    /** An interactive reference to an internal or external resource that, when activated, causes the user agent to navigate to that resource. See related button. */
    def link = this := "link"

    /** A section containing listitem elements. See related listbox. */
    def list = this := "list"

    /** A widget that allows the user to select one or more items from a list of choices. See related combobox and list. */
    def listbox = this := "listbox"

    /** A single item in a list or directory. */
    def listitem = this := "listitem"

    /** A type of live region where new information is added in meaningful order and old information may disappear. See related marquee. */
    def log = this := "log"

    /** The main content of a document. */
    def main = this := "main"

    /** A type of live region where non-essential information changes frequently. See related log. */
    def marquee = this := "marquee"

    /** Content that represents a mathematical expression. */
    def math = this := "math"

    /** A type of widget that offers a list of choices to the user. */
    def menu = this := "menu"

    /** A presentation of menu that usually remains visible and is usually presented horizontally. */
    def menubar = this := "menubar"

    /** An option in a set of choices contained by a menu or menubar. */
    def menuitem = this := "menuitem"

    /** A menuitem with a checkable state whose possible values are true, false, or mixed. */
    def menuitemcheckbox = this := "menuitemcheckbox"

    /** A checkable menuitem in a set of elements with the same role, only one of which can be checked at a time. */
    def menuitemradio = this := "menuitemradio"

    /** A collection of navigational elements (usually links) for navigating the document or related documents. */
    def navigation = this := "navigation"

    /** An element whose implicit native role semantics will not be mapped to the accessibility API. See synonym presentation. */
    def none = this := "none"

    /** A section whose content is parenthetic or ancillary to the main content of the resource. */
    def note = this := "note"

    /** A selectable item in a select list. */
    def option = this := "option"

    /** An element whose implicit native role semantics will not be mapped to the accessibility API. See synonym none. */
    def presentation = this := "presentation"

    /** An element that displays the progress status for tasks that take a long time. */
    def progressbar = this := "progressbar"

    /** A checkable input in a group of elements with the same role, only one of which can be checked at a time. */
    def radio = this := "radio"

    /** A group of radio buttons. */
    def radiogroup = this := "radiogroup"

    /** An input representing a range of values that can be set by the user. */
    def range = this := "range"

    /** A perceivable section containing content that is relevant to a specific, author-specified purpose and sufficiently important that users will likely want to be able to navigate to the section easily and to have it listed in a summary of the page. Such a page summary could be generated dynamically by a user agent or assistive technology. */
    def region = this := "region"

    /** The base role from which all other roles in this taxonomy inherit. */
    def roletype = this := "roletype"

    /** A row of cells in a tabular container. */
    def row = this := "row"

    /** A structure containing one or more row elements in a tabular container. */
    def rowgroup = this := "rowgroup"

    /** A cell containing header information for a row in a grid. */
    def rowheader = this := "rowheader"

    /** A graphical object that controls the scrolling of content within a viewing area, regardless of whether the content is fully displayed within the viewing area. */
    def scrollbar = this := "scrollbar"

    /** A landmark region that contains a collection of items and objects that, as a whole, combine to create a search facility. See related form and searchbox. */
    def search = this := "search"

    /** A type of textbox intended for specifying search criteria. See related textbox and search. */
    def searchbox = this := "searchbox"

    /** A renderable structural containment unit in a document or application. */
    def section = this := "section"

    /** A structure that labels or summarizes the topic of its related section. */
    def sectionhead = this := "sectionhead"

    /** A form widget that allows the user to make selections from a set of choices. */
    def select = this := "select"

    /** A divider that separates and distinguishes sections of content or groups of menuitems. */
    def separator = this := "separator"

    /** A user input where the user selects a value from within a given range. */
    def slider = this := "slider"

    /** A form of range that expects the user to select from among discrete choices. */
    def spinbutton = this := "spinbutton"

    /** A type of live region whose content is advisory information for the user but is not important enough to justify an alert, often but not necessarily presented as a status bar. */
    def status = this := "status"

    /** A document structural element. */
    def structure = this := "structure"

    /** A type of checkbox that represents on/off values, as opposed to checked/unchecked values. See related checkbox. */
    def switch = this := "switch"

    /** A grouping label providing a mechanism for selecting the tab content that is to be rendered to the user. */
    def tab = this := "tab"

    /** A section containing data arranged in rows and columns. See related grid. */
    def table = this := "table"

    /** A list of tab elements, which are references to tabpanel elements. */
    def tablist = this := "tablist"

    /** A container for the resources associated with a tab, where each tab is contained in a tablist. */
    def tabpanel = this := "tabpanel"

    /** A word or phrase with a corresponding definition. See related definition. */
    def term = this := "term"

    /** A type of input that allows free-form text as its value. */
    def textbox = this := "textbox"

    /** A type of live region containing a numerical counter which indicates an amount of elapsed time from a start point, or the time remaining until an end point. */
    def timer = this := "timer"

    /** A collection of commonly used function buttons or controls represented in compact visual form. */
    def toolbar = this := "toolbar"

    /** A contextual popup that displays a description for an element. */
    def tooltip = this := "tooltip"

    /** A type of list that may contain sub-level nested groups that can be collapsed and expanded. */
    def tree = this := "tree"

    /** A grid whose rows can be expanded and collapsed in the same manner as for a tree. */
    def treegrid = this := "treegrid"

    /** An option item of a tree. This is an element within a tree that may be expanded or collapsed if it contains a sub-level group of tree item elements. */
    def treeitem = this := "treeitem"

    /** An interactive component of a graphical user interface (GUI). */
    def widget = this := "widget"

    /** A browser or application window. */
    def window = this := "window"

  }

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
  final def spellCheck = VdomAttr[Boolean]("spellCheck")

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

  /** Don't bother with `:=` on this; call `.apply` instead. */
  final def untypedRef = VdomAttr.UntypedRef

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
