package japgolly.scalajs.react.extra.router2

import scalaz.Equal
import org.scalajs.dom
import japgolly.scalajs.react._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.vdom.prefix_<^._
import utest._
import TestUtil._
import TestUtil2._

object Router2Test extends TestSuite {

  sealed trait MyPage2
  object MyPage2 {
    case object PublicHome              extends MyPage2
    case object PrivatePage1            extends MyPage2
    case object PrivatePage2            extends MyPage2
    case class UserProfilePage(id: Int) extends MyPage2
    case object SomethingElse           extends MyPage2

    implicit val pageEq: Equal[MyPage2] = Equal.equalA

    var isUserLoggedIn: Boolean = false
    var secret = "apples"

    val userProfilePage =
      ReactComponentB[UserProfilePage]("User profile")
        .render(p => <.div(s"Hello user #${p.id}"))
        .build

    case class NavProps(curPage: MyPage2, ctl: RouterCtl[MyPage2])
    val nav = ReactComponentB[NavProps]("NavBar")
      .render { i =>
        def item(p: MyPage2, name: String) =
          if (p == i.curPage)
            <.span(name)
          else
            i.ctl.link(p)(name)
        <.div(
          item(PublicHome, "Home"),
          isUserLoggedIn ?= Seq(
            item(PrivatePage1, "Private page #1"),
            item(PrivatePage2, "Private page #2"))
        )
      }
      .build

    val config = RouterConfig.build[MyPage2] { dsl =>
      import dsl._

      val privatePages = (emptyRule
        | dynamicRoute("user" / int.caseclass1(UserProfilePage)(UserProfilePage.unapply)) ~> renderP(userProfilePage(_))
        | staticRoute("private-1", PrivatePage1) ~> render(<.h1("Private #1"))
        | staticRoute("private-2", PrivatePage2) ~> render(<.h1("Private #2: ", secret))
        )
        .addCondition(isUserLoggedIn)(page => redirectToPage(PublicHome)(Redirect.Push))

      (removeTrailingSlashes
        | staticRoute(root, PublicHome) ~> render(<.h1("HOME"))
        | privatePages
        )
        .notFound(redirectToPage(if (isUserLoggedIn) PublicHome else PrivatePage1)(Redirect.Replace))
        .renderWith((ctl, res) =>
          <.div(
            nav(NavProps(res.page, ctl)),
            res.render()))
    }
  }

  // -------------------------------------------------------------------------------------------------------------------

  implicit def str2path(s: String) = Path(s)
  val base = BaseUrl("file:///router2Demo/")
  def html(r: Resolution[_]) = React.renderToStaticMarkup(r.render())

  override val tests = TestSuite {
    import MyPage2._
    val (router, ctl) = Router.componentAndCtl(base, config) // .logToConsole

    val sim = SimHistory(base.abs)
    val r = ReactTestUtils.renderIntoDocument(router())
    def html = r.getDOMNode().outerHTML
    isUserLoggedIn = false

    'addCondition {
      assertContains(html, ">Home</span>") // not at link cos current page
      assertContains(html, "Private page", false) // not logged in

      isUserLoggedIn = true
      r.forceUpdate()
      assertContains(html, ">Home</span>") // not at link cos current page
      assertContains(html, "Private page", true) // logged in

      ctl.setIO(PrivatePage1).unsafePerformIO()
      assertContains(html, ">Home</a>") // link cos not on current page
      assertContains(html, "Private #1")

      isUserLoggedIn = false
      ctl.refreshIO.unsafePerformIO()
      assertContains(html, ">Home</span>") // not at link cos current page
      assertContains(html, "Private page", false) // not logged in
    }

    'notFoundLazyRedirect {
      def r = sim.run(ctl syncToPath Path("what"))
      assertEq(r.page,  PublicHome)
      isUserLoggedIn = true
      assertEq(r.page,  PrivatePage1)
    }

    'lazyRender {
      isUserLoggedIn = true
      ctl.setIO(PrivatePage2).unsafePerformIO()
      assertContains(html, secret)
      secret = "oranges"
      r.forceUpdate()
      assertContains(html, secret)
    }

    'detectErrors {
      var es = config.detectErrorsE(PublicHome, PrivatePage1, PrivatePage2, UserProfilePage(1))
      assertEq(es, Vector.empty)
      es = config.detectErrorsE(SomethingElse)
      assert(es.nonEmpty)
    }
  }
}