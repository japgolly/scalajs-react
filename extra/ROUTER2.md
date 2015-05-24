Router (v2)
===========

Included is a router (in the orbit of Single-Page Applications) that is written entirely in Scala.

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

