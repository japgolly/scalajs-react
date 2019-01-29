# VDOM

- [Basics](#basics)
- [Event Handlers](#event-handlers)
- [Conditional VDOM](#conditional-vdom)
- [Collections](#collections)
- [Custom VDOM](#custom-vdom)
- [Types](#types)
- [Cheatsheet](#cheatsheet)

## Basics

There are two ways of creating virtual-DOM.

1. **Prefixed (recommended)** - Importing DOM tags and attributes under prefixes is recommended.
  Tags and tag attributes are namespaced;
  tags under `<` (because `<.div` looks similar to `<div>`),
  and attributes under `^` (because something concise was needed and you usually have many attributes which written on new lines all looks to point up back to the target tag).

  Depending on whether you want HTML or SVG import one of:
  * `import japgolly.scalajs.react.vdom.html_<^._`
  * `import japgolly.scalajs.react.vdom.svg_<^._`

  Example:
  ```scala
  import japgolly.scalajs.react.vdom.html_<^._

  <.ol(
    ^.id     := "my-list",
    ^.lang   := "en",
    ^.margin := 8.px,
    <.li("Item 1"),
    <.li("Item 2"))
  ```

2. **Global** - You can import all DOM tags and attributes into the global namespace. Beware that doing so means that you will run into confusing error messages and IDE refactoring issues when you use names like `id`, `a`, `key` for your variables and parameters.

  ```scala
  import japgolly.scalajs.react.vdom.all._

  ol(
    id     := "my-list",
    lang   := "en",
    margin := 8.px,
    li("Item 1"),
    li("Item 2"))
  ```

## Event Handlers

There are two ways of attaching event handlers to your virtual DOM.

> *A helpful way to remember which operator to use is to visualise the arrow stem:
<br>With `==>` the `========` has a gap in the middle - it's a pipe for data to come through meaning it expects `Event => Callback`.
<br>With `-->` the `--------` has no gap - it's just a wire to a `Callback`, no input required.*

1. **`<attribute> --> <callback>`**

  `<attribute>` is a DOM attribute like `onClick`, `onChange`, etc.<br>
  `<callback>` is a `Callback` (see below) which doesn't need any input.

  ```scala
  def onButtonPressed: Callback =
    Callback.alert("The button was pressed!")

  <.button(
    ^.onClick --> onButtonPressed,
    "Press me!")
  ```

2. **`<attribute> ==> <handler>`**

  `<attribute>` is a DOM attribute like `onClick`, `onChange`, etc.<br>
  `<handler>` is a `Event => Callback`.<br>
  See [event types](TYPES.md#events) for the actual types that events can be.

  ```scala
  def onTextChange(e: ReactEventFromInput): Callback =
    Callback.alert("Value received = " + e.target.value)

  <.input.text(
    ^.value     := currentValue,
    ^.onChange ==> onTextChange)
  ```

  If your handler needs additional arguments, use currying so that the args you want to specify are on the left and the event is alone on the right.

  ```scala
  def onTextChange(desc: String)(e: ReactEventFromInput): Callback =
    Callback.alert(s"Value received for ${desc} = ${e.target.value}")

  <.input.text(
    ^.value     := currentValue,
    ^.onChange ==> onTextChange("name"))
  ```


## Conditional VDOM

* **Optional nodes/elements** -
  Since React 16 (and scalajs-react 1.2.0), optional `VdomNode` and `VdomElement`s can be used directly.

  ```scala
  val loginButton : js.UndefOr[VdomNode] = ???
  val logoutButton:     Option[VdomNode] = ???

  <.div(
    <.a(^.href := "/contact", "Contact Us"),
    loginButton,
    logoutButton)
  ```

* **Optional tag-modifiers** -
  Unlike optional nodes above, optional tag-modifiers (`TagMod`) cannot be used directly;
  React itself doesn't allow you to have a component that renders just `class="btn-large"`, nor would it make sense.
  To work with optional tag-modifiers, append `.whenDefined` to your `Option`/`js.UndefOr`.

  ```scala
  val loggedInUser: Option[User] = ???

  <.div(
    <.h3("Welcome"),
    loggedInUser.map(user =>
      TagMod(
        ^.cls := "user-logged-in",
        <.a(^.href := user.profileUrl, "My Profile")))
      .whenDefined)
  ```

  You can also use `.whenDefined` in place of `.map` for improved readability and efficiency.
  The above example then becomes:
  ```scala
  val loggedInUser: Option[User] = ???

  <.div(
    <.h3("Welcome"),
    loggedInUser.whenDefined(user =>
      TagMod(
        ^.cls := "user-logged-in",
        <.a(^.href := user.profileUrl, "My Profile"))))
  ```

* **when / unless** -
  All VDOM has `.when(condition)` and `.unless(condition)` that can be used to conditionally include/omit VDOM.

  ```scala
  def hasFocus: Boolean = ???

  <.div(
    (^.color := "green").when(hasFocus),
    "I'm green when focused.")
  ```

  It's noteworthy that using this approach will result in VDOM being created *before* it determines whether it will
  be used or thrown away. Performance should be maintained via the approaches described in [PERFORMANCE.md](PERFORMANCE.md) so you shouldn't need to worry about optimisation of VDOM construction at this
  level on impact but you've been informed and it's up to you to evaluate your code and environment.

* **Event handler callbacks** -
  Append `?` to `-->`/`==>` operators, and wrap the callback in `Option` or `js.UndefOr`.

  ```scala
  val currentValue: Option[String] = ???

  def onTextChange(e: ReactEventFromInput): Option[Callback] =
    currentValue.map { before =>
      val after = e.target.value
      Callback.alert(s"Value changed from [$before] to [$after]")
    }

  <.input.text(
    ^.value      := currentValue.getOrElse(""),
    ^.onChange ==>? onTextChange)
  ```

* **Attribute values** -
  Append `?` to the `:=` operator, and wrap the value in `Option` or `js.UndefOr`.

  ```scala
  val altText: Option[String] = ???

  <.img(
    ^.src  :=  "blah",
    ^.href :=  "blah",
    ^.alt  :=? altText)
  ```

* **Manually** -
  You can also write conditional VDOM using manual `if-then-else` expressions.

  For VDOM, use `EmptyVdom`:

  ```scala
  <.div(if (allowEdit) <.button("...") else EmptyVdom)
  ```

  For tag-modifiers, use `TagMod.empty`

  ```scala
  <.div(if (hide) ^.display.none else TagMod.empty)
  ```


## Collections

You have are few ways of using collections of VDOM:

* **VdomArray** - An array of VDOM nodes.

  Properties:

  * Mutable; don't let it escape a local pure function
  * React expects a key on each element (which helps React's diff'ing mechanism)
  * Itself, cannot be assigned a key.

  There are various ways to construct one:
    * Call `.toVdomArray` on your collection.
    * If you find yourself with `.map(...).toVdomArray`, replace it with just `.toVdomArray(...)` for improved readability and efficiency.
    * Call `VdomArray.empty()` to get an empty array, add to it via `+=` and `++=`, then use the array directly in your VDOM.

* **ReactFragment** - A sequence of VDOM nodes. *(new as of React 16)*

  Properties:

  * Immutable
  * Elements may, but needn't have keys
  * Itself, can be assigned a key

  There are two ways to construct one, (both after importing vdom):
    * Call `ReactFragment(...)`
    * Call `ReactFragment.withKey(...)`

* **Flatten a Scala collection into a `TagMod`**.

  There are various ways to do this:
  * Call `.toTagMod` on your collection.
  * Call `.mkTagMod(sep)` on your collection which is like `.mkString(sep)` in Scala stdlib.
  * Call `.mkTagMod(start,sep,end)` on your collection which is like `.mkString(start,sep,end)` in Scala stdlib.
  * If you find yourself with `.map(...).toTagMod`, replace it with just `.toTagMod(...)` for improved readability and efficiency.
  * Create the collection using `TagMod(a, b, c, d)`. You'll need to do this if elements have different types, eg `VdomTags` and rendered components.

Examples:
```scala
def allColumns: List[Column] = ???

def renderColumn(c: Column): VdomElement = ???

// Flat, no keys
<.div( allColumns.map(renderColumn).toTagMod )

// Flat, no keys, more efficient
<.div( allColumns.toTagMod(renderColumn) )

// Array, expects keys
<.div( allColumns.map(renderColumn).toVdomArray )

// Array, expects keys, more efficient
<.div( allColumns.toVdomArray(renderColumn) )
```

Manual array usage:
```scala
val array = VdomArray.empty()

for (d <- someData) {
  val fullLabel = ...
  val vdom = <.div(^.key := fullLabel, ...)
  array += vdom
}

if (someCondition)
  array += footer(...)

<.div(
  <.h1("HELLO!"),
  array)
```

* **Flatten a Scala collection into a `ReactFragment`**.

  There are various ways to do this:
  * Call `.toReactFragment` on your collection.
  * Call `.mkReactFragment(sep)` on your collection which is like `.mkString(sep)` in Scala stdlib.
  * Call `.mkReactFragment(start,sep,end)` on your collection which is like `.mkString(start,sep,end)` in Scala stdlib.


## Custom VDOM

```scala
val customAttr    = VdomAttr("customAttr")
val customStyle   = VdomStyle("customStyle")
val customHtmlTag = HtmlTag("customTag")

customHtmlTag(customAttr := "hello", customStyle := "123", "bye")
```
↳ produces ↴
```html
<customTag customAttr="hello" style="customStyle:123;">bye</customTag>
```

In addition to `HtmlTag(…)`, there is also `SvgTag(…)`, `HtmlTagTo[N](…)`, `SvgTagTo[N](…)`.


## Types

The most important types are probably `TagMod` and `VdomElement`.

| Type | Explaination |
| ---- | ---- |
| `VdomElement` | A single VDOM tag (like `<div>`), or a rendered component. |
| `VdomNode` | A single piece of VDOM. <br> Can be a `VdomElement`, or a piece of text, a number, etc.<br>This is also the result of components' `.render` methods. |
| `VdomArray` | An array of VDOM nodes. <br> This is passed to React as an array which helps Reacts diff'ing mechanism.<br>React also requires that each array element have a key. |
| `VdomAttr` | A tag attribute (including styles). <br> Examples: `href`, `value`, `onClick`, `margin`. |
| `VdomTagOf[Node]` | A HTML or SVG tag of type `Node`. |
| `VdomTag` | A HTML or SVG tag. |
| `HtmlTagOf[Node]` | A HTML tag of type `Node`. |
| `HtmlTag` | A HTML tag. |
| `SvgTagOf[Node]` | An SVG tag of type `Node`. |
| `SvgTag` | An SVG tag. |
| `TagMod` | Tag-Modifier. A modification to a `VdomTag`.<br> It cannot be returned from a component's render function. <br> All of the types here can be a `TagMod` because they can all be used to modify a `VdomTag`. <br> This is ***very useful*** for reuse and abstraction in practice, very useful for separating DOM functionality, asthetics and content. <br> For example, it allows a function to return a child tag, a style and some event handlers which the function caller can then apply to some external tag. |

Examples:
```scala
import japgolly.scalajs.react.vdom.all._

val tag1   : VdomTag     = input(className := "name")
val mod1   : TagMod      = value := "Bob"
val mod2   : TagMod      = TagMod(mod1, `type` := "text", title := "hello!")
val tag2   : VdomTag     = tag1(mod2, readOnly := true)
val element: VdomElement = tag2
// equivalent to
// <input class="name" value="Bob" type="text", title := "hello!" readonly=true />
```


## Cheatsheet

| Category | Expressions | Result Type |
|-------|------|-------------|
| Values | Some component <br> `VdomTag` <br> `raw.ReactElement` | `VdomElement` |
| Values | Numbers <br> `String` <br> `PropsChildren` <br> `VdomArray` <br> `VdomElement` <br> `EmptyVdom` <br> `raw.ReactNode` | `VdomNode` |
| Values | `VdomNode` <br> `TagMod.empty` | `TagMod` |
| Attributes | `vdomAttr := value` <br> `eventHandler --> callback` <br> `eventHandler ==> (event => callback)` | `TagMod` |
| Conditional <br> Values | `Option[vdomNode]` <br> `js.UndefOr[vdomNode]` <br> `EmptyVdom` | `VdomNode` |
| Conditional <br> Values | `tagMod.when(condition)` <br> `tagMod.unless(condition)` <br> `Option[tagMod].whenDefined` <br> `TagMod.empty` | `TagMod` |
| Conditional <br> Attributes | `vdomAttr :=? Option[value]` <br> `eventHandler -->? Option[callback]` <br> `eventHandler ==>? Option[event => callback]` | `TagMod` |
| Composition | `vdomTag(tagMod*)` | `VdomTag` |
| Composition | `TagMod(tagMod*)` | `TagMod` |
| Collections <br> (keyed array) | `Seq[A].toVdomArray(A => vdomNode)` <br> `Seq[vdomNode].toVdomArray` <br> `VdomArray(vdomNode*)` <br> `VdomArray.empty() += … ++= …` | `VdomArray` |
| Collections <br> (fragment) | `ReactFragment(VdomNode*)` | `VdomElement` |
| Collections <br> (flatten) | `Seq[A].mkReactFragment(...)` <br> `Seq[A].toReactFragment(A => vdomNode)` <br> `Seq[vdomNode].toReactFragment` | `ReactFragment` |
| Collections <br> (flatten) | `Seq[A].mkTagMod(...)` <br> `Seq[A].toTagMod(A => tagMod)` <br> `Seq[tagMod].toTagMod` <br> `TagMod(tagMod*)` | `TagMod` |
