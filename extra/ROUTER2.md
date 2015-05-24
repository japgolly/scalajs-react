Router (v2)
===========

Included is a router (in the orbit of Single-Page Applications) that is written entirely in Scala.

The package is `japgolly.scalajs.react.extra.router2`.

#### v2? Why & what's new?

The design of [the v1 Router](https://github.com/japgolly/scalajs-react/blob/master/extra/ROUTER.md)
made certain features very hard to accommodate:
[#96](https://github.com/japgolly/scalajs-react/issues/96),
[#103](https://github.com/japgolly/scalajs-react/issues/103),
[#107](https://github.com/japgolly/scalajs-react/issues/107),
[#100](https://github.com/japgolly/scalajs-react/issues/100),
[#94](https://github.com/japgolly/scalajs-react/issues/94),
[#69](https://github.com/japgolly/scalajs-react/issues/69).

In contrast, the v2 Router has a different design.
Notably it uses a user-provided data representation of your pages to identify routes.
This means that you both read the current route, specify target routes using your own, precise types.
Additionally it also handles state and has a better API such that many usages recommended against previously, are now impossible.

Features
========
* Type-safety.
  * Routes (sitemap) correspond to your own arbitrary type hierarchy.
  * Links to routes are guaranteed to be valid.
  * Routes for different pages or routing rule sets cannot be used in the wrong context.
* Rules
  * Routes to views.
  * Redirection routes.
  * Dynamic routes. (eg. `/person/123`)
  * URL re-writing / translation rules. (eg. can remove trailing slashes from URL.)
  * Choose to redirect or render custom view when route is invalid / not found.
* Route views can be intercepted and modified. (eg. to add page headers, footers, a nav breadcrumb.)
* URL and view are always kept in sync.
* Routes are bookmarkable.
* Uses HTML5 History API.
* Routing logic is deterministic and unit-testable.

Caution
=======

* If you want routes starting with slashes, you will need to configure your server appropriately.
  There's no point having `www.blah.com/foo` have routes like `/bar` if when the server receives a request for `www.blah.com/foo/bar` it doesn't know to use the same endpoint as `www.blah.com/foo`.
  If you don't have that control, begin with a `#` instead, like `#foo`.
* If you use Internet Explorer v0.1 ~ v9, the HTML5 API won't be available. But that's ok, there's no need to code like our homo heidelbergensis ancestors, just download and use a polyfill.

Creating & Using a Router
=========================

You need two things to create a router.

1. A `RouterConfig`, describing your routes and other rules. *(Detail below.)*
2. A `BaseUrl`, describing the URL prefix that is the same for all your routes.

  It needn't be absolute at compile-time, but it needs to be absolute at runtime. `BaseUrl.fromWindowOrigin` will give you the protocol, domain and port at runtime, after which you should append a path if necessary. Example: `BaseUrl.fromWindowOrigin / "my_page"` instead of using `BaseUrl("http://blah.com/my_page")` or `BaseUrl("http://127.0.0.1:8080/my_page")` directly.

Once you have these, pass them to `Router(baseUrl, config)` to receive a `ReactComponent` for your router.
Rendering the router is the same as any other React component; just create an instance and call `render()`.

```scala
  @JSExport
  override def main(): Unit = {
    val router = Router(baseUrl, routerConfig)
    router() render dom.document.body
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

Next, you'll want to call `RouterConfig.build` and use the provided DSL.
```scala
val routerConfig = RouterConfig.build[MyPages] { dsl =>
  import dsl._
  
  // TODO Add routing rules here
  // ( <rule1> | <rule2> | ... | <ruleN> )
  //   .notFound( <action> )
}
```

Routing Rules
-------------

To create a route, specify the route and associate an action with it.

### Actions

Each route can be associated with an action. The following actions are available:

| DSL | Args | Description |
|-----|------|-------------|
| `render` | `ReactElement` | Render something. |
| `renderR` | `RouterCtl => ReactElement` | Render something using a `RouterCtl` *(described below)*. |
| `dynRender` | `Page => ReactElement` | Render something using the current page.<br>* *Dynamic routes only.* |
| `dynRenderR` | `(Page, RouterCtl) => ReactElement` | Render something using  the current page, and a `RouterCtl` *(described below)*.<br>* *Dynamic routes only.* |
| `redirectToPage` | `(Page)`<br>`(implicit Redirect.Method)` | Redirect to a page. |
| `redirectToPath` | `(Path | String)`<br>`(implicit Redirect.Method)` | Redirect to a path (a URL suffix proceding the `BaseUrl`). |

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

##### RouteB creation
* `RouteB[Unit]`
  * Implicit conversion from `String`, like `"user/edit"`, or `root` which is an alias for `""`.
  * Implicit conversion from `Path`, like `root`, which is an alias for `Path.root`.
* `RouteB[Int]` - Use DSL `int`.
* `RouteB[Long]` - Use DSL `long`.
* `RouteB[String]` - Use DSL `string(regex)`, like `string("[a-z0-9]{1,20}")`
* Composition
  * `a ~ b` concatenates `a` to `b`.
    <br>Example: `"abc" ~ "def"` is the same as `"abcdef"`.
  * `a / b` adds `a` to `b` with a literal `/` in between.
    <br>Example: `"abc" / "def"` is the same as `"abc/def"`.
  * The types of each route (except `Unit`) are added together into a tuple.
    <br>Example: `"category" / int / "item" / int` is a `RouteB[(Int, Int)]`.
    <br>Example: `"category" / int / "item" / int ~ "." ~ long` is a `RouteB[(Int, Int, Long)]`.
* Combinators on any `RouteB[A]`
  * `.filter(A => Boolean)` causes the route to ignore parsed values which don't satisfy the given filter.
  * `.option` makes this subject portion of the route optional and turns a `RouteB[A]` into a `RouteB[Option[A]]`. Forms an isomorphism between `None` and an empty path.
  * `.xmap[B](A => B)(B => A)` allows you to map the route type from an `A` to a `B`.
  * <code>.caseclass<sub>n</sub>(C)(C.unapply)</code> maps the route type to a case class *n*, where *n* is the class arity.
* Combinators on `RouteB[Option[A]]`
  * `.withDefault(A)` - Specify a default value. Returns a `RouteB[A]`. Uses `==` to compare `A`s to the given default.
  * `.withDefaultE(A)(Equal[A])` - Specify a default value. Returns a `RouteB[A]`. Uses `scalaz.Equal` to compare `A`s to the given default.
  * `.parseDefault(A)` - Specify a default value when parsing. (Path generation ignores this default.) Returns a `RouteB[A]`.
* Combinators on any `RouteB[Unit]`
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
val r: Route[Product] = ("cat" / int / "item" / int).caseclass2(Product)(Product.unapply)

// "get"     <=> "json"
// "get.zip" <=> "zip"
val r: Route[String] = "get" ~ ("." ~ string("[a-z]+")).option.withDefault("json")
```

### Static Routes

Route syntax: `staticRoute(Route[Unit], Page) ~> <action>`

Redirect syntax: `staticRedirect(Route[Unit]) ~> <redirect>`

A `Route[Unit]` can be just a string like `"user/edit"`, or `root` which is an alias for `""`.

Examples:
```scala
staticRoute(root, Home) ~> render( <.h1("Welcome!") )

staticRoute("#hello", Hello) ~> render(HelloComponent())

staticRedirect("#hey") ~> redirectToPage(Hello)(Redirect.Replace)
```

### Dynamic Routes

Syntax:

1. `<dynamic route> ~> <action>`
2. `<dynamic route> ~> (P => <action>)`

| DSL | Args | Description |
|-----|------|-------------|
| `dynamicRoute` | `[P <: Page](Route[P])(PartialFunction[Page, P])` | A dynamic route using a page subtype: `P`.<br>A partial function must be provided to extract a possible `P` from any given `Page`. |
| `dynamicRouteF` | `[P <: Page](Route[P])(Page => Option[P])` | A dynamic route using a page subtype: `P`.<br>A total function must be provided to extract an `Option[P]` from any `Page`. |
| `dynamicRouteCT` | `[P <: Page](Route[P])` | A dynamic route using a page subtype: `P`.<br>The `CT` suffix here denotes that this method uses a compiler-provded `ClassTag` to identify the page subtype `P`. This is equivalent to `{case p: P => p}`.<br>Note that if this is used, the entire space of `P` is associated with a route - do not add another route over `P`. |
| `dynamicRedirect` | `[A](Route[A])` | A dynamic route not associated with a page. Any `A` extracted by the route may then be used to determine the action. |

Example: `item/<id>`
```scala
case class ItemPage(id: Int) extends MyPage

val itemPage = ReactComponentB[ItemPage]("Item page")
  .render(p => <.div(s"Info for item #${p.id}"))
  .build

dynamicRouteCT("item" / int.caseclass1(ItemPage)(ItemPage.unapply))
  ~> dynRender(itemPage(_))
```

### Putting it all together

To create a `RouterConfig` the syntax is `( <rule1> | ... | <ruleN> ).notFound( <action> )`.

Rules are composed via `|`. There is an `emptyRule` is needed that does nothing.

The `.notFound()` method declares how unmatched routes will be handled.
It takes an `Action` as described above, the same kind that is used in the rules.
You will generally redirect to the root, or render a 404-like page.

Once `.notFound()` is called you will have a `RouterConfig`.
Rules can no longer be added, but different (optional) configuration becomes available.

Example:
```scala
val routerConfig = RouterConfig.build[Page] { dsl =>
  import dsl._

  (emptyRule
  | staticRoute(root,     Home)  ~> render(HomePage.component())
  | staticRoute("#hello", Hello) ~> render(<.div("TODO"))
  | staticRedirect("#hey")       ~> redirectToPage(Hello)(Redirect.Replace)
  ) .notFound(redirectToPage(Home)(Redirect.Replace))
}
```

DSL
* rewritePath
* rewritePathR
* removeTrailingSlashes
* removeLeadingSlashes
* trimSlashes

Rule
* `addCondition`
* `fallback`

Additional configuration
* `logToConsole`
* `renderWith`
* `setPostRender`
* `onPostRender`
* `verify`
* `detectErrors`

`RouterCtl`
===========

baseUrl: BaseUrl
byPath: RouterCtl[Path]
refreshIO: IO[Unit]
pathFor(A): Path
setIO(A): IO[Unit]
urlFor(A): AbsUrl
setEH(A): ReactEvent => IO[Unit]
setOnClick(A): TagMod
link(A): ReactTag
contramap[B](B => A): RouterCtl[B]
narrow[B <: A]: RouterCtl[B]
