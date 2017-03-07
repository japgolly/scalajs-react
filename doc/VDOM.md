# VDOM

- [Basics](#basics)
- [Event Handlers](#event-handlers)
- [Conditional VDOM](#conditional-vdom)
- [Collections](#collections)
- [Custom VDOM](#custom-vdom)
- [Cheatsheet](#cheatsheet)

## Basics

It's important to understand the [VDOM types](TYPES.md#vdom) too so have a read.

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

  <.input(
    ^.`type`    := "text",
    ^.value     := currentValue,
    ^.onChange ==> onTextChange)
  ```

  If your handler needs additional arguments, use currying so that the args you want to specify are on the left and the event is alone on the right.

  ```scala
  def onTextChange(desc: String)(e: ReactEventFromInput): Callback =
    Callback.alert(s"Value received for ${desc} = ${e.target.value}")

  <.input(
    ^.`type`    := "text",
    ^.value     := currentValue,
    ^.onChange ==> onTextChange("name"))
  ```


## Conditional VDOM

* **when / unless** -
  All VDOM has `.when(condition)` and `.unless(condition)` that can be used to conditionally include/omit VDOM.

  ```scala
  def hasFocus: Boolean = ???

  <.div(
    (^.color := "green").when(hasFocus),
    "I'm green when focused.")
  ```

* **Option / js.UndefOr** -
  Append `.whenDefined`.

  ```scala
  val loggedInUser: Option[User] = ???

  <.div(
    <.h3("Welcome"),
    loggedInUser.map(user =>
      <.a(^.href := user.profileUrl, "My Profile")
    ).whenDefined
  )
  ```

  This doesn't just work for `Option[vdom]`, you can also use it in place of `.map` for improved readability and efficiency.
  The above example then becomes:
  ```scala
  val loggedInUser: Option[User] = ???

  <.div(
    <.h3("Welcome"),
    loggedInUser.whenDefined(user =>
      <.a(^.href := user.profileUrl, "My Profile")
    )
  )
  ```

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
    ^.src  :=  ???,
    ^.href :=  ???,
    ^.alt  :=? altText)
  ```

* **Manual** -
  You can also manully write conditional code by using `EmptyVdom` to represent nothing.

  ```scala
  <.div(if (allowEdit) editButton else EmptyVdom)
  ```

## Collections

You have two options of using collections of VDOM:

1. Use a `VdomArray`. React expects a key on each element. Helps Reacts diff'ing mechanism.
There are various ways to do this:
  * Call `.toVdomArray` on your collection.
  * If you find yourself with `.map(...).toVdomArray`, replace it with just `.toVdomArray(...)` for improved readability and efficiency.
  * Call `VdomArray.empty()` to get an empty array, add to it via `+=` and `++=`, then use the array directly in your VDOM.

2. Flatten the collection into a `TagMod`.
  There are various ways to do this:
  * Call `.toTagMod` on your collection.
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


## Custom VDOM

```scala
val customAttr    = VdomAttr("customAttr")
val customStyle   = VdomStyle("customStyle")
val customHtmlTag = HtmlTag("customTag")

customTag(customAttr := "hello", customStyle := "123", "bye")
```
↳ produces ↴
```html
<customTag customAttr="hello" style="customStyle:123;">bye</customTag>
```

In addition to `HtmlTag(…)`, there is also `SvgTag(…)`, `HtmlTagTo[N](…)`, `SvgTagTo[N](…)`.


## Cheatsheet

| Category | Expressions | Result Type |
|-------|------|-------------|
| Values | Unmounted component <br> `VdomTag` <br> `raw.ReactElement` | `VdomElement` |
| Values | Primatives & `String` <br> `PropsChildren` <br> `VdomArray` <br> `VdomElement` <br> `raw.ReactNode` | `VdomNode` |
| Values | `VdomNode` <br> `EmptyVdom` | `TagMod` |
| Attributes | `vdomAttr := value` <br> `eventHandler --> callback` <br> `eventHandler ==> (event => callback)` | `TagMod` |
| Conditional <br> Values | `tagMod.when(condition)` <br> `tagMod.unless(condition)` <br> `Option[tagMod].whenDefined` | `TagMod` |
| Conditional <br> Attributes | `vdomAttr :=? Option[value]` <br> `eventHandler -->? Option[callback]` <br> `eventHandler ==>? Option[event => callback]` | `TagMod` |
| Composition | `vdomTag(tagMod*)` | `VdomTag` |
| Composition | `TagMod(tagMod*)` <br> `tagMod(tagMod*)` | `TagMod` |
| Collections <br> (keyed array) | `Seq[A].toVdomArray(A => vdomNode)` <br> `Seq[vdomNode].toVdomArray` <br> `VdomArray(vdomNode*)` <br> `VdomArray.empty() += … ++= …` | `VdomArray` |
| Collections <br> (flatten) | `Seq[A].toTagMod(A => tagMod)` <br> `Seq[tagMod].toTagMod` <br> `TagMod(tagMod*)` | `TagMod` |
