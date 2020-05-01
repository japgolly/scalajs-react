package japgolly.scalajs.react.extra.router

import java.util.UUID
import monocle.Prism
import org.scalajs.dom
import scalaz.Equal
import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.vdom.html_<^._
import MonocleReact._
import ScalazReact._
import TestUtil._
import scala.annotation.nowarn

object Router2Test extends TestSuite {

  sealed trait Module
  case object ModuleRoot extends Module
  case object Module1 extends Module
  case class Module2(i: Int) extends Module
  case class Module3(u: UUID) extends Module

  object Module {
    val routes = RouterConfigDsl[Module].buildRule { dsl =>
      import dsl._

      def moduleRoot(ctl: RouterCtl[Module]) =
        <.div(
          ctl.link(Module1)("Module One"),
          ctl.link(Module2(7))("Module 2.7"),
          ctl.link(Module3(UUID fromString "12345678-1234-1234-1234-123456789012"))("Module 3.12345678-1234-1234-1234-123456789012"))

      (emptyRule
        | staticRoute(root, ModuleRoot) ~> renderR(moduleRoot)
        | staticRoute("one", Module1) ~> render(<.h3("Module #1"))
        | dynamicRouteCT("two" / int.caseClass[Module2]) ~> dynRender(m => <.h3(s"Module #2 @ ${m.i}"))
        | dynamicRouteCT("three" / uuid.caseClass[Module3]) ~> dynRender(m => <.h3(s"Module #3 @ ${m.u}"))
        )
    }
  }

  sealed trait MyPage2
  object MyPage2 {
    case object PublicHome              extends MyPage2
    case object PrivatePage1            extends MyPage2
    case object PrivatePage2            extends MyPage2
    case object PrivatePage3            extends MyPage2
    case object PrivatePage4            extends MyPage2
    case object AccessDenied            extends MyPage2
    case class UserProfilePage(id: Int) extends MyPage2
    case class NestedModule(m: Module)  extends MyPage2
    case object SomethingElse           extends MyPage2
    case class Code1(code: String)      extends MyPage2
    case class Code2(code: String)      extends MyPage2

    case class E(n: En) extends MyPage2
    sealed trait En
    case object E1 extends En
    case object E2 extends En
    @nowarn("cat=unused") def renderE(e: E) = <.div()

    implicit val pageEq: Equal[MyPage2] = Equal.equalA

    var isUserLoggedIn: Boolean = false
    var secret = "apples"

    val userProfilePage =
      ScalaComponent.builder[UserProfilePage]("User profile")
        .render_P(p => <.div(s"Hello user #${p.id}"))
        .build

    case class NavProps(curPage: MyPage2, ctl: RouterCtl[MyPage2])
    val nav = ScalaComponent.builder[NavProps]("NavBar")
      .render_P { i =>
        def item(p: MyPage2, name: String) =
          if (p == i.curPage)
            <.span(name)
          else
            i.ctl.link(p)(name)
        <.div(
          item(PublicHome, "Home"),
          VdomArray(
            item(PrivatePage1, "Private page #1"),
            item(PrivatePage2, "Private page #2"))
            .when(isUserLoggedIn)
        )
      }
      .build

    var innerPageEq: Equal[MyPage2] = null

    val alphaOnly = "^([a-zA-Z]+)$".r
    val code1Prism = Prism[String, Code1](alphaOnly.findFirstIn(_).map(s => Code1(s.toUpperCase)))(_.code)
    val code2Prism = Prism[String, Code2](alphaOnly.findFirstIn(_).map(s => Code2(s.toUpperCase)))(_.code)

    val config = RouterConfigDsl[MyPage2].buildConfig { dsl =>
      import dsl._

      innerPageEq = implicitly[Equal[MyPage2]]

      val privatePages12 = (emptyRule
        | dynamicRouteCT("user" / int.caseClass[UserProfilePage]) ~> dynRender(userProfilePage(_))
        | staticRoute("private-1", PrivatePage1) ~> render(<.h1("Private #1"))
        | staticRoute("private-2", PrivatePage2) ~> render(<.h1("Private #2: ", secret))
        )
        .addConditionWithFallback(CallbackTo(isUserLoggedIn), redirectToPage(PublicHome)(SetRouteVia.HistoryPush))

      val privatePage3 = (emptyRule
        | staticRoute("private-3", PrivatePage3) ~> render(<.h1("Private #3"))
        )
        .addConditionWithFallback(CallbackTo(isUserLoggedIn), redirectToPage(AccessDenied)(SetRouteVia.HistoryReplace))

      val privatePage4 = (emptyRule
        | staticRoute("private-4", PrivatePage4) ~> render(<.h1("Private #4"))
        )
        .addConditionWithFallback(_ => CallbackTo(isUserLoggedIn), redirectToPage(AccessDenied)(SetRouteVia.HistoryReplace))

      val ePages = (emptyRule
        | staticRoute("e/1", E(E1)) ~> render(renderE(E(E1)))
        | staticRoute("e/2", E(E2)) ~> render(renderE(E(E2)))
        )

      val nestedModule =
        Module.routes.prefixPath_/("module").pmap[MyPage2](NestedModule){ case NestedModule(m) => m }

      val code1 = dynamicRouteCT("code1" / remainingPath.pmapL(code1Prism)) ~> dynRender(c => <.div(c.code))
      val code2 = dynamicRouteCT("code2" / remainingPath.pmapL(code2Prism)) ~> dynRender(c => <.div(c.code))

      ( emptyRule // removeTrailingSlashes
      | staticRoute(root, PublicHome) ~> render(<.h1("HOME"))
      | staticRoute("denied", AccessDenied) ~> render(<.h1("AccessDenied"))
      | nestedModule
      | ePages
      | code1
      | privatePages12 // Keep this in between code1 & code2. It tests .addCondition wrt prisms.
      | code2.autoCorrect
      | privatePage3
      | privatePage4
      ) .notFoundDynamic(_ => CallbackTo(redirectToPage(if (isUserLoggedIn) PrivatePage1 else PublicHome)(SetRouteVia.HistoryReplace)))
        .renderWith((ctl, res) =>
          <.div(
            nav(NavProps(res.page, ctl)),
            res.render()))
        .logToConsole
    }
  }

  // -------------------------------------------------------------------------------------------------------------------

  implicit def str2path(s: String) = Path(s)
  def htmlFor(r: Resolution[_]) = ReactDOMServer.renderToStaticMarkup(r.render())

  override val tests = Tests {
    import MyPage2._
    implicit val base = RouterTestHelp.localBaseUrl_/
    val (router, lgc) = Router.componentAndLogic(base, config)
    val ctl = lgc.ctl

    val sim = SimHistory(base.abs)
    val r = ReactTestUtils.renderIntoDocument(router())
    def html = r.getDOMNode.asMounted().asElement().outerHTML
    def currentPage(): Option[MyPage2] = lgc.parseUrl(AbsUrl(dom.window.location.href)).flatMap(config.rules.parse(_).runNow().toOption)
    isUserLoggedIn = false

    def syncNoRedirect(path: Path) = {
      sim.reset(path.abs)
      val c = lgc.syncToPath(path).runNow()
      val r = sim.run(c)
      assertEq("history (latest <-> oldest)", sim.history, path.abs :: Nil)
      r
    }

    def assertSyncRedirects(path: Path, expectTo: Path) = {
      sim.reset(path.abs)
      val r = sim.run(lgc.syncToPath(path).runNow())
      assertEq("currentUrl", sim.currentUrl, expectTo.abs)
      r
    }

    "notFoundLazyRedirect" - {
      def r = sim.run(lgc.syncToPath(Path("what")).runNow())
      assertEq(r.page,  PublicHome)
      isUserLoggedIn = true
      assertEq(r.page,  PrivatePage1)
    }

    "lazyRender" - {
      isUserLoggedIn = true
      ctl.set(PrivatePage2).runNow()
      assertContains(html, secret)
      secret = "oranges"
      r.forceUpdate
      assertContains(html, secret)
    }

    "detectErrors" - {
      var es = config.detectErrors(PublicHome).runNow()
      assertEq(es, Vector.empty)

      isUserLoggedIn = true
      es = config.detectErrors(PublicHome, PrivatePage1, PrivatePage2, UserProfilePage(1)).runNow()
      assertEq(es, Vector.empty)

      es = config.detectErrors(SomethingElse).runNow()
      assert(es.nonEmpty)
    }

    "routesPerNestedPageType" - {
      assertEq("E1", ctl.pathFor(E(E1)).value, "e/1")
      assertEq("E2", ctl.pathFor(E(E2)).value, "e/2")
      val es = config.detectErrors(E(E1), E(E2)).runNow()
      assertEq(es, Vector.empty)
    }

    "pageEquality" -
      assert(innerPageEq eq pageEq)

    "nestedModule" - {
      "detectErrors" - {
        val es = config.detectErrors(NestedModule(ModuleRoot), NestedModule(Module1), NestedModule(Module2(666))).runNow()
        assertEq(es, Vector.empty)
      }
      "origPathNotAvail" - {
        val r = sim.run(lgc.syncToPath(Path("one")).runNow())
        assertEq(r.page,  PublicHome)
        assertEq(sim.currentUrl, Path.root.abs)
      }
      "slashNop" - {
        // prefixPath_/ only adds a / when rhs is empty
        assertSyncRedirects("module/", "")
        ()
      }
      "nestedStaticPath" - {
        val r = syncNoRedirect("module/one")
        assertEq(r.page,  NestedModule(Module1))
        assertContains(htmlFor(r), "Module #1")
      }
      "nestedDynamicPath" - {
        val r = syncNoRedirect("module/two/123")
        assertEq(r.page,  NestedModule(Module2(123)))
        assertContains(htmlFor(r), "Module #2 @ 123")
      }
      "nestedDynamicPathUuid" - {
        val r = syncNoRedirect("module/three/12345678-1234-1234-1234-123456789012")
        assertEq(r.page,  NestedModule(Module3(UUID fromString "12345678-1234-1234-1234-123456789012")))
        assertContains(htmlFor(r), "Module #3 @ 12345678-1234-1234-1234-123456789012")
      }
      "routerLinks" - {
        assertEq(ctl.pathFor(NestedModule(ModuleRoot)).value, "module")
        assertEq(ctl.pathFor(NestedModule(Module1)).value, "module/one")
        assertEq(ctl.pathFor(NestedModule(Module2(666))).value, "module/two/666")
        ctl.set(NestedModule(ModuleRoot)).runNow()
        assertContains(html, "/module/one\"")
        assertContains(html, "/module/two/7\"")
      }
    }

    "onSet" - {
      var i = 0
      val ctl2 = ctl.onSet(Callback(i += 1) >> _)
      isUserLoggedIn = true
      ctl2.set(PrivatePage2).runNow()
      assertEq(currentPage(), Some(PrivatePage2))
      assertContains(html, secret)
      assertEq(i, 1)
    }

    "setRespectRouteCondition" - {
      // Make sure we're not starting on PublicHome cos that's were we expect to be redirected
      ctl.set(NestedModule(ModuleRoot)).runNow()
      assertEq(currentPage(), Some(NestedModule(ModuleRoot)))

      // set without being logged in
      isUserLoggedIn = false
      ctl.set(PrivatePage2).runNow()
      assertEq(currentPage(), Some(PublicHome))
      assert(!html.contains(secret))
    }

    "prism" - {
      "buildUrl" - {
        assertEq(ctl.pathFor(Code1("HEH")).value, "code1/HEH")
      }
      "exact" - {
        val r = syncNoRedirect("code1/OMG")
        assertEq(r.page, Code1("OMG"))
        assertContains(htmlFor(r), "OMG")
      }
      "tolerant" - {
        val r = syncNoRedirect("code1/yay")
        assertEq(r.page, Code1("YAY"))
        assertContains(htmlFor(r), "YAY")
      }
    }

    "prismWithRedirect" - {
      "buildUrl" - {
        assertEq(ctl.pathFor(Code2("HEH")).value, "code2/HEH")
      }
      "exact" - {
        val r = syncNoRedirect("code2/OMG")
        assertEq(r.page, Code2("OMG"))
        assertContains(htmlFor(r), "OMG")
      }
      "redirect" - {
        assertSyncRedirects("code2/yay", "code2/YAY")
        ()
      }
    }

    "addCondition" - {
      "1" - {
        assertContains(html, ">Home</span>") // not at link cos current page
        assertContains(html, "Private page", false) // not logged in

        isUserLoggedIn = true
        r.forceUpdate
        assertContains(html, ">Home</span>") // not at link cos current page
        assertContains(html, "Private page", true) // logged in

        ctl.set(PrivatePage1).runNow()
        assertContains(html, ">Home</a>") // link cos not on current page
        assertContains(html, "Private #1")

        isUserLoggedIn = false
        ctl.refresh.runNow()
        assertContains(html, ">Home</span>") // not at link cos current page
        assertContains(html, "Private page", false) // not logged in
      }

      "3" - {
        isUserLoggedIn = true
        ctl.set(PrivatePage3).runNow()
        assertContains(html, "Private #3")

        isUserLoggedIn = false
        ctl.refresh.runNow()
        assertContains(html, "AccessDenied")
      }

      "4" - {
        isUserLoggedIn = true
        ctl.set(PrivatePage4).runNow()
        assertContains(html, "Private #4")

        isUserLoggedIn = false
        ctl.refresh.runNow()
        assertContains(html, "AccessDenied")
      }

      "overloads" - {
        @nowarn("cat=unused") def test(f: RoutingRule[Int] => Any): Unit = ()

        "c"  - test(_.addCondition(CallbackTo(true)))
        "ca" - test(_.addConditionWithFallback(CallbackTo(true), RedirectToPath[Int](null, null)))
        "co" - test(_.addConditionWithOptionalFallback(CallbackTo(true), None))
        "cf" - test(_.addConditionWithOptionalFallback(CallbackTo(true), _ => None))
        "f"  - test(_.addCondition(i => CallbackTo(i == 0)))
        "fa" - test(_.addConditionWithFallback(i => CallbackTo(i == 0), RedirectToPath[Int](null, null)))
        "fo" - test(_.addConditionWithOptionalFallback(i => CallbackTo(i == 0), None))
        "ff" - test(_.addConditionWithOptionalFallback(i => CallbackTo(i == 0), _ => None))
      }
    }

  }
}
