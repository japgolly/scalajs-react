Router
======

Included is a router (in the orbit of Single-Page Applications) that is written entirely in Scala.

The package is `japgolly.scalajs.react.extra.router`.

```scala
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "1.4.2"
```

## Contents

- [Features](#features)
- [Caution](#caution)
- [Creating & Using a Router](#creating--using-a-router)
- [Creating a `RouterConfig`](#creating-a-routerconfig)
  - [Actions](#actions)
  - [`Route[X]`](#routex)
  - [Static routes](#static-routes)
  - [Dynamic routes](#dynamic-routes)
  - [Putting it all together](#putting-it-all-together)
  - [A spot of unsafeness](#a-spot-of-unsafeness)
- [`RouterCtl`](#routerctl)
- Beyond the Basics
  - [URL rewriting rules](#url-rewriting-rules)
  - [Loose routes with auto-correction](#loose-routes-with-auto-correction)
  - [Conditional routes](#conditional-routes)
  - [Rendering with a layout](#rendering-with-a-layout)
  - [Setting page title](#setting-page-title)
  - [Post-render callback](#post-render-callback)
  - [Nested routes (modules)](#nested-routes-modules)
- [Examples](#examples)


Features
========
* Type-safety.
  * Use your own types to uniquely identify routes and their parameters.
  * Link URLs are guaranteed to be valid.
  * Routes for different pages or routing rule sets cannot be used in the wrong context.
* Rules
  * Routes to views.
  * Redirection routes.
  * Dynamic routes. (eg. `/person/123`)
  * Default values in dynamic routes.
  * URL re-writing / translation rules. (eg. can remove trailing slashes from URL.)
  * Choose to redirect or render custom view when route is invalid / not found.
  * Routes can be nested and modularised.
  * Conditions can be applied to an entire route set.
* Router can indicate the current page with precision, faciliating dynamic menus and breadcrumbs even in the presence of complex, dynamic routes.
* Route views can be wrapped in a layout. (eg. to add page headers, footers, a nav breadcrumb.)
* URL and view are always kept in sync.
* Routes are bookmarkable.
* Uses HTML5 History API.
* Callback for route changes.
* Routing logic is easily unit-testable.


Caution
=======

* If you want routes starting with slashes, you will need to configure your server appropriately.
  There's no point having `www.blah.com/foo` have routes like `/bar` if when the server receives a request for `www.blah.com/foo/bar` it doesn't know to use the same endpoint as `www.blah.com/foo`.
  If you don't have that control, begin with a `#` instead, like `#foo`.

* It's a security feature of browsers that if a user enters a different URL, you can't absorb it with your SPA router. Using `window.onbeforeunload` you can only prompt the user to change their mind and keep the current URL. If a user manually enters a URL to move from one part of your SPA to a different part of the SPA, it's going to reload the page... **unless** you use `#` in your URL and they change the portion after the `#`. In such a case you can use the `window.onhashchange` event handler.

* If you use Internet Explorer v0.1 ~ v9, the HTML5 API won't be available. But that's ok, there's no need to code like our homo-heidelbergensis ancestors, just download and use a polyfill.

* These is a small but significant guarantee that this design sacrifices to buy important features. See *[A spot of unsafeness](#a-spot-of-unsafeness)*.


Creating & Using a Router
=========================

You need two things to create a router.

1. A `RouterConfig`, describing your routes and other rules. *(Detail below.)*
2. A `BaseUrl`, describing the URL prefix that is the same for all your routes.

  The `BaseUrl` needn't be absolute at compile-time, but it needs to be absolute at runtime. `BaseUrl.fromWindowOrigin` will give you the protocol, domain and port at runtime, after which you should append a path if necessary. Example: `BaseUrl.fromWindowOrigin / "my_page"` instead of using `BaseUrl("http://blah.com/my_page")` or `BaseUrl("http://127.0.0.1:8080/my_page")` directly.

Once you have these, pass them to `Router(baseUrl, config)` to receive a `ReactComponent` for your router.
Rendering the router is the same as any other React component; just create an instance and call `render()`.

```scala
  @JSExport
  override def main(): Unit = {
    val router = Router(baseUrl, routerConfig)
    router().renderIntoDOM(dom.document.body)
  }
```

Creating a `RouterConfig`
=========================

First, you'll want to create a data representation of your pages. For example:
```scala
sealed trait MyPages
case object Home                      extends MyPages
case object Search                    extends MyPages
case object Products                  extends MyPages
case class ProductInfo(id: ProductId) extends MyPages
```

Next, you'll want to call `RouterConfigDsl.buildConfig` and use the provided DSL.
```scala
import japgolly.scalajs.react.extra.router._

// import japgolly.scalajs.react.vdom.Implicits._
// ↑ Depending on your usage you might also need this
// ↑ If you have VDOM-related compilation errors, add this import.

val routerConfig = RouterConfigDsl[MyPages].buildConfig { dsl =>
  import dsl._

  // TODO Add routing rules here
  // ( <rule1> | <rule2> | ... | <ruleN> )
  //   .notFound( <action> )
}
```

Details of rule & config constituents follow.

### Actions

Each route can be associated with an action. The following actions are available:

| DSL | Args | Description |
|-----|------|-------------|
| `render` | `VdomElement` | Render something. |
| `renderR` | `RouterCtl => VdomElement` | Render something using a [`RouterCtl`](#routerctl). |
| `dynRender` | `Page => VdomElement` | Render something using the current page.<br>* *Dynamic routes only.* |
| `dynRenderR` | `(Page, RouterCtl) => VdomElement` | Render something using  the current page, and a [`RouterCtl`](#routerctl).<br>* *Dynamic routes only.* |
| `redirectToPage` | `(Page)`<br>`(implicit Redirect.Method)` | Redirect to a page. |
| `redirectToPath` | `(Path \| String)`<br>`(implicit Redirect.Method)` | Redirect to a path (a URL suffix proceding the `BaseUrl`). |

In the redirect actions, unless you declare your own redirect method, you'll need to specify one manually. (Eg. `redirectToPage(Home)(Redirect.Push)`).

##### `Redirect.Method`
Two redirect methods are available:

1. `Redirect.Push` - The current URL will be recorded in history. User can hit *Back* button to reach it.
2. `Redirect.Replace` - The current URL will not be recorded in history. User can't hit *Back* button to reach it.

### Route[X]

A `Route[X]` is required as input to the higher-level rule-building functions (`staticRoute()`, `staticRedirect()` etc).
It represents a path that requires an `X` to generate, and provides an `X` when parsed.

To construct a `Route`, the DSL provides a route-builder `RouteB` which composes nicely
and is automatically converted to a finalised `Route` when used.

##### `RouteB` creation and usage

* `RouteB[Unit]`
  * Implicit conversion from `String`, like `"user/edit"`.
  * Implicit conversion from `Path`, like `root` which is an alias for `Path.root`.

* `RouteB[Int]` - Use DSL `int`.

* `RouteB[Long]` - Use DSL `long`.

* `RouteB[String]` from a URL substring - Use DSL `string(regex)`, like `string("[a-z0-9]{1,20}")`
  * Best to use a whitelist of characters, eg. `[a-zA-Z0-9]+`.
  * Do not capture groups; use `[a-z]+` instead of `([a-z]+)`.
  * If you need to group, use non-capturing groups like `(?:bye|hello)` instead of `(bye|hello)`.

* `RouteB[String]` from the remainder of the unmatched URL.
  * `remainingPath` - Captures the (non-empty) remaining portion of the URL path.
  * `remainingPathOrBlank` - Captures the (potentially-empty) remaining portion of the URL path.

* `RouteB[UUID]` - Use DSL `uuid`.

* Composition
  * `a ~ b` concatenates `a` to `b`.
    <br>Example: `"abc" ~ "def"` is the same as `"abcdef"`.
  * `a / b` adds `a` to `b` with a literal `/` in between.
    <br>Example: `"abc" / "def"` is the same as `"abc/def"`.
  * The types of each route (except `Unit`) are added together into a tuple.
    <br>Example: `"grp" / int / "item" / int` is a `RouteB[(Int, Int)]`.
    <br>Example: `"grp" / int / "item" / int ~ "." ~ long` is a `RouteB[(Int, Int, Long)]`.

* Combinators on any `RouteB[A]`
  * `.filter(A => Boolean)` causes the route to ignore parsed values which don't satisfy the given filter.
  * `.option` makes this subject portion of the route optional and turns a `RouteB[A]` into a `RouteB[Option[A]]`. Forms an isomorphism between `None` and an empty path.
  * `.pmap[B](A => Option[B])(B => A)` allows you to attempt to map the route type from an `A` to a `B`, or fail. (prism map)
  * `.xmap[B](A => B)(B => A)` allows you to map the route type from an `A` to a `B`. (exponential map)
  * `.caseClass[A]` maps the route type(s) to a case class.
  * `.caseClassDebug[A]` as above, but shows you the code that the macro generates.
  * If you're using the `monocle` module and `import MonocleReact._` you also gain access to:
    * `.pmapL[B](Prism[A, B])`.
    * `.xmapL[B](Iso[A, B])`.

* Combinators on `RouteB[Option[A]]`
  * `.withDefault(A)` - Specify a default value. Returns a `RouteB[A]`. Uses `==` to compare `A`s to the given default.
  * `.withDefaultE(A)` - Specify a default value. Returns a `RouteB[A]`. Uses `scalaz.Equal` to compare `A`s to the given default.
  * `.parseDefault(A)` - Specify a default value when parsing. (Path generation ignores this default.) Returns a `RouteB[A]`.

* Combinators on `RouteB[Unit]`
  * `.const(A)` changes a `RouteB[Unit]` into a `RouteB[A]` by assigning a constant value (not used in route parsing or generation).

Examples:
```scala
// Static routes
val r: Route[Unit] = root
val r: Route[Unit] = "user/profile"

// "user/3/profile" <=> 3
val r: Route[Int] = "user" / int / "profile"

// "category/bikes/item/17" <=> ("bikes", 17)
val r: Route[(String, Int)] = "category" / string("[a-z0-9]{1,20}") / "item" / int

// "cat/3/item/17" <=> Product(3, 17)
case class Product(category: Int, item: Int)
val r: Route[Product] = ("cat" / int / "item" / int).caseClass[Product]

// "get"     <=> "json"
// "get.zip" <=> "zip"
val r: Route[String] = "get" ~ ("." ~ string("[a-z]+")).option.withDefault("json")

// "category/widgets/item/12345678-1234-1234-1234-123456789012" <=> Item("widgets", 12345678-1234-1234-1234-123456789012
case class Item(category: String, itemId: java.util.UUID)
val r: Route[Item] = ("category" / string("[a-z]+") / item / uuid).caseClass[Item]
```

### Static routes

Syntax:

1. `staticRoute(Route[Unit], Page) ~> <action>`
2. `staticRedirect(Route[Unit]) ~> <redirect>`

`Route[Unit]`, `<action>`, and `<redirect>` are all described above.

Examples:
```scala
staticRoute(root, Home) ~> render( <.h1("Welcome!") )

staticRoute("#hello", Hello) ~> render(HelloComponent())

staticRedirect("#hey") ~> redirectToPage(Hello)(Redirect.Replace)
```

### Dynamic routes

Syntax:

1. `<dynamic route> ~> <action>`
2. `<dynamic route> ~> (P => <action>)`

| DSL | Args | Description |
|-----|------|-------------|
| `dynamicRoute` | `[P <: Page](Route[P])(PartialFunction[Page, P])` | A dynamic route using a page subtype: `P`.<br>A partial function must be provided to extract a possible `P` from any given `Page`. |
| `dynamicRouteF` | `[P <: Page](Route[P])(Page => Option[P])` | A dynamic route using a page subtype: `P`.<br>A total function must be provided to extract an `Option[P]` from any `Page`. |
| `dynamicRouteCT` | `[P <: Page](Route[P])` | A dynamic route using a page subtype: `P`.<br>The `CT` suffix here denotes that this method uses a compiler-provded `ClassTag` to identify the page subtype `P`. This is equivalent to `{case p: P => p}`.<br>Note that if this is used, the entire space of `P` is associated with a route - do not add another route over `P`. |
| `dynamicRedirect` | `[A](Route[A])` | A dynamic path not associated with a page. Any `A` extracted by the route may then be used to determine the redirect target. |

Example: This creates a route in the format of `item/<id>`.
```scala
case class ItemPage(id: Int) extends MyPage

val itemPage = ScalaComponent.builder[ItemPage]("Item page")
  .render(p => <.div(s"Info for item #${p.id}"))
  .build

dynamicRouteCT("item" / int.caseClass[ItemPage])
  ~> dynRender(itemPage(_))
```

### Putting it all together

To create a `RouterConfig` the syntax is `(<rule1> | ... | <ruleN>).notFound(<action>)`.

**Rule composition** is acheived via `|`.
If two rules overlap (eg. can respond to the same URL), the left-hand side of `|` has precedence and wins.

**The `.notFound()` method** declares how unmatched routes will be handled.
It takes an `Action` as described above, the same kind that is used in the rules.
You will generally redirect to the root, or render a 404-like page.

Once `.notFound()` is called you will have a `RouterConfig`.
Rules can no longer be added, but different (optional) configuration becomes available.

**Example:**
```scala
val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
  import dsl._

  (emptyRule
  | staticRoute(root,     Home)  ~> render(HomePage.component())
  | staticRoute("#hello", Hello) ~> render(<.div("TODO"))
  | staticRedirect("#hey")       ~> redirectToPage(Hello)(Redirect.Replace)
  ) .notFound(redirectToPage(Home)(Redirect.Replace))
}
```
(The use of `emptyRule` is just for nice formatting.)

If you'd like to see what's happening, you can call `.logToConsole` on your config and to have the routing engine log what it does to the JS console. Add it anytime between `.notFound()` and its use in creating a `Router`.

### A spot of unsafeness

A tradeoff in safety has been made to purchase many new features in the redesigned v2 Router.
Namely, it's possible to accidently forget a route for a page type.

Consider this example:
```scala
sealed trait MyPages
case object Page1                     extends MyPages
case class  Page2(value: Option[Int]) extends MyPages
case object Page3                     extends MyPages

val config = RouterConfigDsl[MyPages].buildConfig { dsl =>
  import dsl._
  ( emptyRule
  | staticRoute("page1", Page1) ~> render(???)
  | dynamicRouteCT("page2" ~ ("/" ~ int).option.caseClass[Page2]) ~> render(???)
  // oops! We forgot Page3!!
  ).notFound(???)
}
```

To mitigate this, `RouterConfig` comes with a `verify(page1, ... pageN)` method that will confirm that each specified page:

1. Has an associated route.
2. Is itself returned when parsing its associated route.
3. Has an associated action.

If any errors are detected they are displayed both on screen and in the console.
There is also a `detectErrors` method which is more appropriate for unit-testing.

Amending the above example, we get:
```scala
val config = RouterConfigDsl[MyPages].buildConfig { dsl =>
  import dsl._
  ( emptyRule
  | staticRoute("page1", Page1) ~> render(???)
  | dynamicRouteCT("page2" ~ ("/" ~ int).option.caseClass[Page2]) ~> render(???)
  // oops! We still forgot Page3 but it will be detected at run- or test- time
  ).notFound(???)
    .verify(Page1, Page2(None), Page2(Some(123)), Page3)  // ← Ensure pages are configured and valid
}
```

You can also call `.fallback` on your rules immediately before calling `.notFound()`.
This will give you control over what happens when a page isn't configured (if you want to do something other than crashing.)


`RouterCtl`
===========

RouterCtl is a client API to the router.
It allows you to control the current page, create links, determine page URLs, etc.

To use it, pass it in to components that need it via their props.

As `RouterCtl[P]` has a page-type context (the `P`!),
if a component only wants/needs to control a router with a certain subset of pages,
the component can accept a `RouterCtl[PageSubset]` instead of a `RouterCtl[AllPages]`.
A conversion to the former is just a `.contramap` or `.narrow` call away for the parent.

Here a subset of useful methods. Use IDE auto-complete or check the source for the full list.

* `link(Page): VdomTag` - Create a link to a page.

  ```scala
  ctl.link(Specials)("Today's Specials", ^.color := "red")
  ```

* `set___` - Programmatically navigate to route when invoked.
  * `set(Page): Callback` - Return a procedure that will navigate to route.
  * `setEH(Page): ReactEvent => Callback` - Consume an event and set the route.
  * `setOnClick(Page): TagMod` - Set the route when the subject is clicked.<br>Shorthand for `^.onClick ==> ctl.setEH(page)`.

* `refresh: Callback` - Refresh the current route when invoked.

  ```scala
  ^.button("Refresh", ^.onClick --> ctl.refresh)
  ```

* `urlFor(Page): AbsUrl` - Get the absolute URL of a given page.


Beyond the Basics
=================

### URL rewriting rules

The following can create rules that simply rewrite (i.e. live redirect) URLs.
They can be composed with other rules via `|` as usual.

| Method | Args | Description |
|--------|------|-------------|
| `rewritePath` | `PartialFunction[Path, Redirect]` | Run a `Path` through a partial function that results in a redirect. |
| `rewritePathF` | `Path => Option[Redirect]` | Run a `Path` through a total function that optionally results in a redirect. |
| `rewritePathR` | `Pattern, Matcher => Option[Redirect]` | Match a `Path` against regex and if it matches, use the match result to optionally redirect. |

Example: This would remove leading dots.
```scala
rewritePathR("^\\.+(.*)$".r, m => Some(redirectToPath(m group 1)(Redirect.Replace)))
```

A few rules are included out-of-the-box for you to use:
* `removeTrailingSlashes` - uses a replace-state redirect to remove trailing slashes from route URLs.
* `removeLeadingSlashes` - uses a replace-state redirect to remove leading slashes from route URLs.
* `trimSlashes` - uses a replace-state redirect to remove leading and trailing slashes from route URLs.

### Loose routes with auto-correction

There are cases in which you may want to create a route that

1. Loosely matches a URL so that it can handle variations.
2. Has a single appropriate URL that you want to use after variations have been accepted and parsed.

##### Example scenario

You may be creating an issue tracker that has URLs for each ticket like:
```
/issue/DEV-4
/issue/DEV-42
/issue/FRONTEND-23
```

You also want to accept imperfections such as:
```
/issue/DEV-004
/issue/DEV42
/issue/frontend-23
```

When an imperfect URL is parsed you want to auto-correct it like:
```
/issue/DEV-004     → /issue/DEV-4
/issue/DEV42       → /issue/DEV-42
/issue/frontend-23 → /issue/FRONTEND-23
```

When a URL is already perfect, you render a page normally.

##### Instructions

There are two features you need to implement this functionality.

First, create a route as you normally would, then map its type using a prism.
To do so, and then call `.pmap` (or `.pmapL` to use use a [Monocle prism](http://julien-truffaut.github.io/Monocle/api/#monocle.PPrism)).

Second, one you have created you route rule, call `.autoCorrect`.
By default it will do a replace-state to change the URL meaning that only the correct URL will appear in the user's history -
pressing *back* will go back to the page before they entered the imperfect URL.
You can use push-state by using `.autoCorrect(Redirect.Method)` but be warned, unless you're doing something magic/crazy,
when the user hits their *back* button they will request the imperfect URL again which will just redirect them forward negating their *back* action.

##### Example implementation

This is the implementation for the example scenario described above.

```scala
sealed trait Page

case object Home extends Page

case class IssuePage(projectCode: String, number: Int) extends Page {
  def toUrlFrag: String = projectCode + "-" + number
}

val cfg = RouterConfigDsl[Page].buildConfig { dsl =>
  import dsl._

  def homeRoute =
    staticRoute(root, Home) ~> render(<.h1("Home"))

  val urlRegex = """([a-zA-Z]+)-?(\d+)""".r

  def parse(urlFrag: String): Option[IssuePage] =
    urlFrag match {
      case urlRegex(code, num) => Some(IssuePage(code.toUpperCase, num.toInt))
      case _                   => None
    }

  def issueRoute =
    dynamicRouteCT("issue" / remainingPath.pmap(parse)(_.toUrlFrag)) ~>
      dynRender(renderIssuePage) autoCorrect

  def renderIssuePage(p: IssuePage) =
    <.div("Issue = " + p)

  ( homeRoute
  | issueRoute
  ).notFound(redirectToPage(Home)(Redirect.Replace))
}
```

### Conditional routes

When you have a `Rule`, you can call `addCondition` on it to evaluate a condition every time it is used.
When the condition is met, the route is usable; when unmet, a fallback behaviour can be actioned or the router can continue processing other rules as if the conditional one didn't exist.

```scala
/**
 * Prevent this rule from functioning unless some condition holds.
 * When the condition doesn't hold, an alternative action may be performed.
 *
 * @param condUnmet Response when rule matches but condition doesn't hold.
 *                  If response is `None` it will be as if this rule doesn't exist and will likely end
 *                  in the route-not-found fallback behaviour.
 */
def addCondition(cond: CallbackTo[Boolean])(condUnmet: Page => Option[Action[Page]]): Rule[Page]
```

Example:
```scala
def grantPrivateAccess: CallbackB =
  ???

val privatePages = (emptyRule
  | staticRoute("private-1", PrivatePage1) ~> render(???)
  | staticRoute("private-2", PrivatePage2) ~> render(???)
  )
  .addCondition(grantPrivateAccess)(_ => redirectToPage(AccessDenied)(Redirect.Push))
```

### Rendering with a layout

Once you have a `RouterConfig`, you can call `.renderWith` on it to supply your own render function that will be invoked each time a route is rendered. It takes a function in the shape: `(RouterCtl[Page], Resolution[Page]) => VdomElement` where a `Resolution` is:

```scala
/**
 * Result of the router resolving a URL and reaching a conclusion about what to render.
 *
 * @param page Data representation (or command) of what will be drawn.
 * @param render The render function provided by the rules and logic in [[RouterConfig]].
 */
final case class Resolution[P](page: P, render: () => VdomElement)
```

Thus using the given `RouterCtl` and `Resolution` you can wrap the page in a layout, link to other pages, highlight the current page, etc.

See *[Examples](#examples)* for a live demonstration.

### Setting page title

You'll likely want to update your page's title to reflect the current route being shown.
To do so, call one of:
* `.setTitle(Page => String)`
* `.setTitleOption(Page => Option[String])`

Example:
```scala
val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
  import dsl._
  ( staticRoute(root,     Home)  ~> render(???)
  | staticRoute("#about", About) ~> render(???)
  )
    .notFound(???)
    .setTitle(p => s"PAGE = $p | Example App")  // ← available after .notFound()
}
```

### Post-render callback

Each time a route is rendered, the "post-render" callback is invoked.
Out-of-the-box, the default action is to scroll the window to the top.

You can *add* your own actions by calling `.onPostRender` on your `RouterConfig` instance.
You can *set* the entire callback (i.e. override instead of add) using `.setPostRender`.

Both `.onPostRender` and  `.setPostRender` take a single arg: `(Option[Page], Page) => Callback`.
The function is provided the previously-rendered page (or `None` when a router renders its first page),
and the current page.

Example:
```scala
val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
  import dsl._
  ( staticRoute(root,     Home)  ~> render(???)
  | staticRoute("#about", About) ~> render(???)
  )
    .notFound(???)
    .onPostRender((prev, cur) =>                          // ← available after .notFound()
      Callback.log(s"Page changing from $prev to $cur.")) // ← our callback
}
```

*Hey? Why wrap in `Callback`?*

It gives you a guarantee by me and the Scala compiler that whatever you put inside,
will only be executed in a callback. Underlying code won't accidently call it now when *installing* the callback,
and then do nothing when the callback actually executes.
It's not an esoteric concern - those kind of mistakes do happen in real-world code.


### Nested routes (modules)

Routes can be created and used as modules. This is how:

##### The Module

1. Use `RouterConfigDsl[InnerPage].buildRule` to create a (composite) rule instead of a full `RouterConfig`. The content will be your normal routing rules up until, and excluding, the `notFound()` call.
2. Have components that need a `RouterCtl` use a `RouterCtl[InnerPage]` as normal - this will continue to work correctly regardless of whether `InnerPage` is nested or not.

##### Nesting
All of this happens in the method in which you build your `RouterConfig[OuterPage]`.

1. Call one of the `pmap` methods ([prism](https://github.com/julien-truffaut/Monocle/blob/master/core/src/main/scala/monocle/Prism.scala)-map) on the `InnerPage` rule in order to bridge the inner & outer page types.
2. Use `prefixPath` or `prefixPath_/` on the nested routes to mount them with a prefix. (Actually you can use `modPath` if a prefix isn't enough.)
3. Append the result to your routes as per usual.

Example:
```scala
sealed trait Module
object Module {
  case object ModuleRoot   extends Module
  case object ModuleDetail extends Module

  val routes = RouterConfigDsl[Module].buildRule { dsl =>
    import dsl._
    (emptyRule
    | staticRoute(root, ModuleRoot)       ~> render(???)
    | staticRoute("detail", ModuleDetail) ~> render(???)
    )
  }
}

sealed trait Outer
object Outer {
  case object Root           extends Outer
  case object Login          extends Outer
  case class Nest(m: Module) extends Outer

  val config = RouterConfigDsl[Outer].buildConfig { dsl =>
    import dsl._
    (emptyRule
    | staticRoute(root, Root)      ~> render(???)
    | staticRoute("#login", Login) ~> render(???)
    | Module.routes.prefixPath_/("#module").pmap[Outer](Nest){ case Nest(m) => m } // Much nest. Wow.
    ).notFound(???)
  }
}
```

If we imagine `BaseUrl` to be `http://www.example.com/` then the sitemap for `Outer` in this example is:

| URL | Page |
|------|------|
| `http://www.example.com/` | `Root` |
| `http://www.example.com/#login` | `Login` |
| `http://www.example.com/#module` | `Nest(ModuleRoot)` |
| `http://www.example.com/#module/detail` | `Nest(ModuleDetail)` |

Examples
========

The github pages for this project online at https://japgolly.github.io/scalajs-react/
uses this router and demonstrates a number of features.

1. The source begins here: [GhPages.scala](../gh-pages/src/main/scala/ghpages/GhPages.scala)
2. Router logging is enabled so you can read what the router does in the console.

There are also unit tests available in the
[japgolly.scalajs.react.extra.router](../test/src/test/scala/japgolly/scalajs/react/extra/router)
package.

[This](https://github.com/chandu0101/scalajs-react-template) simple example [demonstrates](http://chandu0101.github.io/scalajs-react-template/) routing as well.
