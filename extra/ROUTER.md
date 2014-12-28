Included is a router (in the orbit of Single-Page Applications) that is written entirely in Scala.

Features
========
* Type-safety.
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

Tutorial
========

The friendliest way to create a router is to use the `RoutingRules` DSL.
Create an object that extends `RoutingRules` and provide the only mandatory method, `notFound`.

```scala
object MyPage extends RoutingRules {
  override val notFound = render( <.h1("404!!") )
}
```

#### Root view

Now lets wire up a view for the root route.
Assuming you have a React component called `RootComponent` somewhere, add this to your `RoutingRules` object:

```scala
  val root = register(rootLocation(RootComponent))
```

#### Redirect 404 to root view

Instead of showing a 404 when an invalid route is accessed, lets redirect to the root view.
All that's needed is
```scala
  override val notFound = redirect(root, Redirect.Replace)
```

`Redirect.Replace` means that the window URL is replaced with the new URL without the old URL going into history.
Use `Redirect.Push` to store the old URL in browser history when the redirect occurs.

Also, be careful that you don't refer to a `val` that hasn't been initialised yet (ie. a forward reference).
Order your rules appropriately or use lazy vals.

So now we have:
```scala
object MyPage extends RoutingRules {
  val root = register(rootLocation(RootComponent))
  override val notFound = redirect(root, Redirect.Replace)
}
```

#### Adding static routes

Routes can either render a view, or redirect. Here are examples of both.

```scala
  // Wire a route #hello to a view
  val hello = register(location("#hello", HelloComponent))

  // Redirect #hey to #hello
  register(redirection("#hey", hello, Redirect.Replace))
```

#### Links

To create safe links to routes, you'll need access to a `Router` in your component's render function.
`Router` has a method `link` that creates links to valid routes.

The `render()` method in your `RoutingRules` accepts:
* plain old `ReactElement`
* `Router => ReactElement`
* Components with the `Router` as their props type.

Putting this altogether we can have:
```scala
object MyPage extends RoutingRules {
  val root  = register(rootLocation(RootComponent))
  val hello = register(location("#hello", <.h1("Hello!") ))
  override val notFound = redirect(root, Redirect.Replace)
}

val RootComponent = ReactComponentB[MyPage.Router]("Root")
  .render(router =>
    <.div(
      <.h2("Router Demonstration"),
      <.div(router.link(MyPage.root) ("The 'root' route")),
      <.div(router.link(MyPage.hello)("The 'hello' route")))
    ).build
```

Note that the `Router` type is prefixed as `ReactComponentB[MyPage.Router]` and not `ReactComponentB[Router]`.
Routers are not interchangable between routing rule sets.

#### Rendering your Router

Before a `Router` can be created it needs to the base URL, which is the prefix portion of the URL that is the same for all your pages routes.

It needn't be absolute at compile-time, but it needs to be absolute at runtime. `BaseUrl.fromWindowOrigin` will give you the protocol, domain and port at runtime, after which you should append a path if necessary. Example: `BaseUrl.fromWindowOrigin / "my_page"`

Once you have a your `BaseUrl`, call `<RoutingRules>.router(baseUrl)` to get a standard React component of your router.

Note: You can enable console logging by providing a `Router.consoleLogger` to `router()`.

Example:
```scala
val baseUrl   = BaseUrl.fromWindowOrigin / "my_page"
val component = MyPage.router(baseUrl, Router.consoleLogger)
```

#### Dynamic Routes

Use `register( parser {...} ... )` to register dynamic routes.

`parser` takes a partial function that attempts to match a portion of a URL and capture some part of it.
If successful, you can create a dynamic response using the captured part.

Unlike static routes, if you want to create a link to a dynamic page you need to specify an additional function that generates the dynamic route path. Use `.dynLink` in your routing rules for this purpose.

Examples:
```scala
object MyPage extends RoutingRules {
  ...

  // This example matches /name/<anything>

  private val namePathMatch = "^/name/(.+)$".r
  register(parser { case namePathMatch(n) => n }.location(n => NameComponent(n)))
  val name = dynLink[String](n => s"/name/$n")

  // This example matches /person/<number>
  //     and redirects on /person/<not-a-number>

  private val personPathMatch = "^/person/(.+)$".r
  register(parser { case personPathMatch(p) => p }.thenMatch {
    case matchNumber(idStr)     => render(PersonComponent(PersonId(idStr.toLong)))
    case _ /* non-numeric id */ => redirect(root, Redirect.Push)
  })
  val person = dynLink[PersonId](id => s"/person/${id.value}")
}
```

Note that we don't store the results of `register` for dynamic routes. This is why `dynLink` is necessary.

#### View interception

To customise all views rendered by a routing rule set, override the `interceptRender` method and follow the types.

This example adds a back button to all pages except the root:
```scala
override protected def interceptRender(i: InterceptionR): ReactElement =
  if (i.loc == root)
    i.element
  else
    <.div(
      <.div(i.router.link(root)("Back", ^.cls := "back")),
      i.element)
```

#### URL rewriting

`RoutingRules` comes with a built-in (although inactive) URL rewriting rule called `removeTrailingSlashes`.
It can be installed via `register()` and is a good example of how to create dynamic matching rules.

Its implementation is simple:
```scala
def removeTrailingSlashes: DynamicRoute = {
  val regex = "^(.*?)/+$".r
  parser { case regex(p) => p }.redirection(p => (Path(p), Redirect.Replace))
}
```

#### Callback: `onRouteChange`
By default, when a new route is activated the window is scrolled to the top.
This behaviour can be removed or customised by providing a callback in your routing rules called `onRouteChange`.

```scala
onRouteChange { loc =>
  ...
}