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

Adding Routes
=============

