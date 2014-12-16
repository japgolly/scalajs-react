package japgolly.scalajs.react.extras.router

import japgolly.scalajs.react._, vdom.prefix_<^._
import japgolly.scalajs.react.test._
import org.scalajs.dom._
import scalaz.effect.IO
import scalaz._
import scalaz.syntax.equal._
import utest._
import TestUtil._

object RouterTest extends TestSuite {

  /* TODO

  private val showR2 = "^id/([^/]+)$".r
  private val number = "^(\\d+)$".r

  parse{ case showR2(idStr) => idStr }.thenMatch {
    case number(s) => val id = s.toLong; render(_ => RE)
    case _         => redirect(root, Redirect.Push)
  }
  val show = dynLink[Long](id => s"show/$id")


  val wow1 = location("wow1", router => router.link(ExamplePage1.blah).render)
//  val wow2 = location("wow2", router => router.link(ExamplePage2.blah).render)
}

object ExamplePage2 extends Page {
  override val notFound = render(_ => RE)
  val blah = location("blah", _ => RE)
}
   */

  // -------------------------------------------------------------------------------------------------------------------

  object MyPage extends Page {

    val root       = rootLocation(RootComponent)
    val hello: Loc = location("/hello", addBackButton(root, HelloComponent))
    val oldHello   = redirection("/hey", hello, Redirect.Replace)

    private val namePathMatch = "^/name/(.+)$".r
    parse { case namePathMatch(n) => n }.location(n => addBackButton(root, NameComponent(n)))
    val name = dynLink[String](n => s"/name/$n")

    override val notFound = redirect(root, Redirect.Replace)
  }

  def addBackButton[P](root: => Location[P], inner: => Renderer[P]): Renderer[P] = router => {
    <.div(
      <.div(router.link(root)("Back", ^.cls := "back")),
      inner(router))
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
        'root { r.parseUrl(base.abs) mustEqual Some(Path("")) }
        //'tslash { r.parseUrl(base / "" abs) mustEqual Some(Path("")) }
        'path { r.parseUrl(base / "hehe" abs) mustEqual Some(Path("/hehe")) }
      }
      'syncToUrl {
        'match_root { r.syncToUrl(base.abs) mustEqual \/-(MyPage.root) }
        'match_path { r.syncToUrl(base / "hello" abs) mustEqual \/-(MyPage.hello) }
        'notFound {
          val s = SimHistory(base / "what" abs)
          val a = s.rune(r.syncToUrl(s.startUrl))
          s.history mustEqual List(MyPage.root.path.abs)
          s.broadcasts mustEqual Vector.empty // this is sync(), not set()
          a mustEqual MyPage.root
        }
        'badbase {
          val google = AbsUrl("https://www.google.com")
          val s = SimHistory(google)
          val a = s.rune(r.syncToUrl(s.startUrl))
          s.history mustEqual List(MyPage.root.path.abs, google)
          s.broadcasts mustEqual Vector.empty // this is sync(), not set()
          a mustEqual MyPage.root
        }
      }
    }
  }
}