package japgolly.scalajs.react.extra.router

import sizzle.Sizzle
import japgolly.scalajs.react._, vdom.prefix_<^._
import japgolly.scalajs.react.test._
import org.scalajs.dom._
import scalaz._
import scalaz.effect.IO
import utest._
import TestUtil._

object RouterTest extends TestSuite {

  sealed trait MyPage
  object MyPage {
    case object Root  extends MyPage
    case object Hello extends MyPage
    case class Greet(name: String) extends MyPage
    case class Person(id: Long) extends MyPage

    val RootComponent = ReactComponentB[RouterCtl[MyPage]]("Root")
      .render_P(r =>
        <.div(
          <.h2("Router Demonstration"),
          <.p("This is the root page. Click on a link below to view routes within this page."),
          <.div(r.link(Hello)("The 'hello' route", ^.cls := "hello")),
          <.div(r.link(Greet("bob"))("Greet('bob')", ^.cls := "n1")),
          <.div(r.link(Greet("crap"))("Greet('crap')", ^.cls := "n2")))
      ).build

    val HelloComponent =
      ReactComponentB.static("Hello", <.h3("Hello there!")).build

    val NameComponent = ReactComponentB[String]("Name")
      .render_P(name => <.h3(s"I believe your name is '$name'."))
      .build

    val PersonComponent = ReactComponentB[Person]("Person by ID")
      .render_P(p => <.h3(s"Person #${p.id} Details..."))
      .build

    val config = RouterConfigDsl[MyPage].buildConfig { dsl =>
      import dsl._
      (removeTrailingSlashes
      | staticRoute(root,     Root)                                 ~> renderR(RootComponent(_))
      | staticRoute("/hello", Hello)                                ~> render (HelloComponent())
      | staticRedirect("/hey")                                      ~> redirectToPage(Hello)(Redirect.Replace)
      | dynamicRouteCT("/name" / string("[a-z]+").caseClass[Greet]) ~> dynRender(g => NameComponent(g.name))
      | dynamicRouteCT("/person" / long.caseClass[Person])          ~> dynRender(PersonComponent(_))
      )
        .notFound(redirectToPage(Root)(Redirect.Replace))
        .renderWith((ctl, res) =>
          if (res.page == Root)
            res.render()
          else
            <.div(
              <.div(ctl.link(Root)("Back", ^.cls := "back")),
              res.render()))
    }
  }

  // -------------------------------------------------------------------------------------------------------------------

  //  object MyOtherPage extends RoutingRules {
  //    override val notFound = render(<.h1("404!!"))
  //    val thebuns = register(location(".buns", <.h1("The Buns!")))
  //  }

  // -------------------------------------------------------------------------------------------------------------------

  override val tests = TestSuite {

    'sim {
      import MyPage.{Root, Hello, Greet, Person}
      val base = BaseUrl("http://localhost/")
      val router = Router(base, MyPage.config.logToConsole)
      val c = ReactTestUtils.renderIntoDocument(router())
      def node = ReactDOM findDOMNode c
      def html = node.outerHTML

      def testView(routeSuffix: String, p: MyPage): Unit = {
        window.location.href mustEqual base.+(routeSuffix).value
        val h = html
        assertContains(h, "Router Demo",  p == Root)
        assertContains(h, """>Back<""",   p != Root)
        assertContains(h, "Hello there",  p == Hello)
        assertContains(h, "your name is", p match {case Greet(_) => true; case _ => false})
      }
      def assertRoot()         = testView("",          Root)
      def assertRouteHello()   = testView("/hello",    Hello)
      def assertRouteNameBob() = testView("/name/bob", Greet("bob"))

      def click(css: String): Unit = Simulation.click run Sizzle(css, node).sole
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
        ReactDOM unmountComponentAtNode node
      }
    }

    'pure {
      implicit val base = BaseUrl("http://www.yaya.com/blah")
      val r = new RouterLogic(base, MyPage.config.logToConsole)

      'urlParsing {
        'root   { r.parseUrl(base.abs)          mustEqual Some(Path("")) }
        'tslash { r.parseUrl(base / "" abs)     mustEqual Some(Path("/")) }
        'path   { r.parseUrl(base / "hehe" abs) mustEqual Some(Path("/hehe")) }
      }

      'syncToUrl {
        def runh[P](r: RouterLogic[P], start: AbsUrl) = {
          val s = SimHistory(start)
          val a = s.run(r.syncToUrl(s.startUrl))
          s.broadcasts mustEqual Vector.empty // this is sync(), not set()
          (s, a)
        }

        def testh[P](r: RouterLogic[P], start: AbsUrl)(expectPrevHistory: AbsUrl => List[AbsUrl], expectPage: P, expectPath: String): Unit = {
          val (s, res) = runh(r, start)
          s.history.mustEqual(Path(expectPath).abs :: expectPrevHistory(start))
          res.page mustEqual expectPage
        }

//        'match_root - r.syncToUrl(base.abs)          .mustEqual(\/-(MyPage.root))
//        'match_path - r.syncToUrl(base / "hello" abs).mustEqual(\/-(MyPage.hello))
        'notFound_redirect - testh(r, base / "what" abs)(_ => Nil, MyPage.Root, "")
//        'notFound_render {
//          val abs = base / "what" abs
//          val r2 = MyOtherPage.routingEngine(base)
//          val (s, a) = runh(r2, abs)
//          s.history mustEqual List(abs)
//          a.path.value mustEqual "/what"
//          ReactDOMServer.renderToStaticMarkup(a render r2) mustEqual "<h1>404!!</h1>"
//        }
        'badbase - testh(r, AbsUrl("https://www.google.com"))(List(_), MyPage.Root, "")
        'tslash_root - testh(r, base / "" abs)      (_ => Nil, MyPage.Root, "")
        'tslash_path - testh(r, base / "hello/" abs)(_ => Nil, MyPage.Hello, "/hello")
      }
    }
  }
}