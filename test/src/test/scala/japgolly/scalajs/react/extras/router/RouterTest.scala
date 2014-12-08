package japgolly.scalajs.react.extras.router

import japgolly.scalajs.react._, vdom.prefix_<^._
import japgolly.scalajs.react.test._
import org.scalajs.dom._
import scalaz.{-\/, \/-}
import utest._

object RouterTest extends TestSuite {

  sealed trait MyPage
  object MyPage extends Page[MyPage] {
    val root   = Root(rootR)
    val route2 = path("#r2", addBackButton(root, route2R))
    val route3 = path("#r3", addBackButton(root, route3R))

    private def rootR  : Renderer[MyPage] = RootComponent(_)
    private def route2R: Renderer[MyPage] = _ => <.h3("ROUTE #2. Nothing to see here.")
    private def route3R: Renderer[MyPage] = _ => Route3Component("Bob")
  }

  def addBackButton[P](root: Root[P], inner: Renderer[P]): Renderer[P] = router => {
    <.div(
      <.div(router.link(root)("Back", ^.cls := "back")),
      inner(router))
  }

  val RootComponent = ReactComponentB[Router[MyPage]]("Root")
    .render(router =>
      <.div(
        <.h2("Router Demonstration"),
        <.p("This is the root page. Click on a link below to view routes within this page."),
        <.div(router.link(MyPage.route2)("route #2", ^.cls := "r2")),
        <.div(router.link(MyPage.route3)("route #3", ^.cls := "r3")))
    ).build

  val Route3Component = ReactComponentB[String]("R3")
    .render(name =>
      <.div(
        <.h3("ROUTE #3"),
        <.p("Hello ", name, "!")))
    .build

  val C = Router.component(BaseUrl("/routerDemo"), MyPage)

  def assertContains(in: String, subj: String, expect: Boolean): Unit =
    if (in.contains(subj) != expect) {
      println(s"\nHTML: $in\nSubj: $subj\nExpect: $expect\n")
      assert(false)
    }

  override val tests = TestSuite {
    'sim {
      val c = ReactTestUtils.renderIntoDocument(C())

      def testView(routeSuffix: String, v: Route[MyPage]): Unit = {
        assert(window.location.href endsWith routeSuffix)
        val html = c.getDOMNode().outerHTML
        assertContains(html, "Router Demo", v == MyPage.root)
        assertContains(html, """>Back<""",  v != MyPage.root)
        assertContains(html, "ROUTE #2",    v == MyPage.route2)
        assertContains(html, "ROUTE #3",    v == MyPage.route3)
      }
      def assertRoot()   = testView("/routerDemo",    MyPage.root)
      def assertRoute2() = testView("/routerDemo#r2", MyPage.route2)
      def assertRoute3() = testView("/routerDemo#r3", MyPage.route3)

      def click(css: String): Unit = Simulation.click run Sel(css).findIn(c)
      def clickR2()   = click("a.r2")
      def clickR3()   = click("a.r3")
      def clickBack() = click("a.back")

      try {
        assertRoot()
        clickR2()  ; assertRoute2()
        clickBack(); assertRoot()
        clickR3()  ; assertRoute3()
        clickBack(); assertRoot()

      } finally {
        React.unmountComponentAtNode(c.getDOMNode())
      }
    }

    'pure {
      val baseurl = BaseUrl("/blah")
      val r = new Router(baseurl, MyPage.root, MyPage.paths)
      'read {
        'match {
          assert(r.read("http://yaya.com/blah") == \/-(MyPage.root))
          assert(r.read("http://yaya.com/blah#r2") == \/-(MyPage.route2))
        }
        'invalid {
          assert(r.read("http://yaya.com/blah#what") == -\/(ReplaceState(baseurl, MyPage.root)))
        }
      }
    }
  }
}