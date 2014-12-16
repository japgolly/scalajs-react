package japgolly.scalajs.react.extras.router

import japgolly.scalajs.react._, vdom.prefix_<^._
import japgolly.scalajs.react.test._
import org.scalajs.dom._
import scalaz._
import scalaz.effect.IO
import utest._
import TestUtil._

object RouterTest extends TestSuite {

  object MyPage extends Page {

    // *************
    // Static Routes
    // *************

    val root       = register(rootLocation(RootComponent))
    val hello: Loc = register(location("/hello", HelloComponent))

    register(redirection("/hey", hello, Redirect.Replace))

    // **************
    // Dynamic Routes
    // **************

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

    // *******
    // General
    // *******

    register(removeTrailingSlashes)

    override protected val notFound = redirect(root, Redirect.Replace)

    override protected def interceptRender(i: InterceptionR): ReactElement =
      if (i.loc == root)
        i.element
      else
        <.div(
          <.div(i.router.link(root)("Back", ^.cls := "back")),
          i.element)
  }

  object MyOtherPage extends Page {
    override val notFound = render(<.h1("404!!"))
    val thebuns = register(location(".buns", <.h1("The Buns!")))
  }

  val RootComponent = ReactComponentB[MyPage.Router]("Root")
    .render(router =>
      <.div(
        <.h2("Router Demonstration"),
        <.p("This is the root page. Click on a link below to view routes within this page."),
        <.div(router.link(MyPage.hello)("The 'hello' route", ^.cls := "hello")),
        <.div(router.link(MyPage.name("bob"))("Name('bob')", ^.cls := "n1")),
        <.div(router.link(MyPage.name("crap"))("Name('crap')", ^.cls := "n2")))
    ).build

  val HelloComponent = ReactComponentB[Unit]("Hello")
    .render(_ => <.h3("Hello there!"))
    .buildU

  val NameComponent = ReactComponentB[String]("Name")
    .render(name => <.h3(s"I believe your name is '$name'."))
    .build

  case class PersonId(value: Long)
  val PersonComponent = ReactComponentB[PersonId]("Person by ID")
    .render(p => <.h3(s"Person #${p.value} Details..."))
    .build

  // -------------------------------------------------------------------------------------------------------------------

  case class SimHistory(startUrl: AbsUrl) {

    var history = List(startUrl)
    var broadcasts = Vector.empty[List[AbsUrl]]

    def run[P, B](prog: RouteProg[P, B]): B = {
      import RouteCmd._
      type Cmd[A]  = RouteCmd[P, A]

      val interpretCmd: Cmd ~> IO = new (Cmd ~> IO) {
        override def apply[A](m: Cmd[A]): IO[A] = m match {
          case PushState(url)     => IO{history = url :: history}
          case ReplaceState(url)  => IO{history = url :: history.tail}
          case BroadcastLocChange => IO{broadcasts :+= history}
          case ReturnLoc(loc)     => IO(loc)
        }
      }

      Free.runFC[Cmd, IO, B](prog)(interpretCmd).unsafePerformIO()
    }

    def rune[P, A](r: RouteProg[P, A] \/ A): A =
      r.fold(run, identity)
  }

  // -------------------------------------------------------------------------------------------------------------------

  override val tests = TestSuite {
    type P = MyPage.type

    'sim {
      val base = BaseUrl("file:///routerDemo")
      val Component = MyPage.router(base)
      val c = ReactTestUtils.renderIntoDocument(Component())
      def html = c.getDOMNode().outerHTML

      def testView(routeSuffix: String, p: ApprovedPath[P]): Unit = {
        window.location.href mustEqual base.+(routeSuffix).value
        val h = html
        assertContains(h, "Router Demo",  p eq MyPage.root)
        assertContains(h, """>Back<""",   p ne MyPage.root)
        assertContains(h, "Hello there",  p eq MyPage.hello)
        assertContains(h, "your name is", p.path.value startsWith "/name")
      }
      def assertRoot()         = testView("",          MyPage.root)
      def assertRouteHello()   = testView("/hello",    MyPage.hello)
      def assertRouteNameBob() = testView("/name/bob", MyPage.name("bob"))

      def click(css: String): Unit = Simulation.click run Sel(css).findIn(c)
      def clickBack()    = click("a.back")
      def clickHello()   = click("a.hello")
      def clickNameBob() = click("a.n1")

      try {
        assertRoot()
        clickHello();   assertRouteHello()
        clickBack();    assertRoot()
        clickNameBob(); assertRouteNameBob()
        clickBack();    assertRoot()

      } finally {
        React.unmountComponentAtNode(c.getDOMNode())
      }
    }

    'pure {
      implicit val base = BaseUrl("http://www.yaya.com/blah")
      val r = MyPage.routingEngine(base)

      'urlParsing {
        'root   { r.parseUrl(base.abs)          mustEqual Some(Path("")) }
        'tslash { r.parseUrl(base / "" abs)     mustEqual Some(Path("/")) }
        'path   { r.parseUrl(base / "hehe" abs) mustEqual Some(Path("/hehe")) }
      }

      'syncToUrl {
        def runh[P](r: Router[P], start: AbsUrl) = {
          val s = SimHistory(start)
          val a = s.rune(r.syncToUrl(s.startUrl))
          s.broadcasts mustEqual Vector.empty // this is sync(), not set()
          (s, a)
        }

        def testh[P](r: Router[P], start: AbsUrl)(expectPrevHistory: AbsUrl => List[AbsUrl], expectLoc: Location[P]): Unit = {
          val (s, a) = runh(r, start)
          s.history.mustEqual(expectLoc.path.abs :: expectPrevHistory(start))
          a mustEqual expectLoc
        }

        'match_root - r.syncToUrl(base.abs)          .mustEqual(\/-(MyPage.root))
        'match_path - r.syncToUrl(base / "hello" abs).mustEqual(\/-(MyPage.hello))
        'notFound_redirect - testh(r, base / "what" abs)(_ => Nil, MyPage.root)
        'notFound_render {
          val abs = base / "what" abs
          val r2 = MyOtherPage.routingEngine(base)
          val (s, a) = runh(r2, abs)
          s.history mustEqual List(abs)
          a.path.value mustEqual "/what"
          React.renderToStaticMarkup(a render r2) mustEqual "<h1>404!!</h1>"
        }
        'badbase - testh(r, AbsUrl("https://www.google.com"))(List(_), MyPage.root)
        'tslash_root - testh(r, base / "" abs)      (_ => Nil, MyPage.root)
        'tslash_path - testh(r, base / "hello/" abs)(_ => Nil, MyPage.hello)
      }

      'linksScoped {
        r.link(MyPage.root)
        assertTypeMismatch(compileError("r.link(MyOtherPage.thebuns)"))
      }
    }
  }
}