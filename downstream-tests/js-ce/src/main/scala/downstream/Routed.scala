package downstream

import cats.effect.SyncIO
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._

object Routed {

  sealed trait Module
  case object ModuleRoot extends Module
  case object Module1 extends Module
  case class Module2(i: Int) extends Module

  val routerConfig = RouterConfigDsl[Module].buildConfig { dsl =>
    import dsl._

    def moduleRoot(ctl: RouterCtl[Module]) =
      <.div(
        ctl.link(Module1)("Module One"),
        ctl.link(Module2(7))("Module 2.7"),
      )

    (emptyRule
    | staticRoute(root, ModuleRoot) ~> renderR(moduleRoot)
    | staticRoute("one", Module1) ~> render(<.h3("Module #1"))
    | dynamicRouteCT("two" / int.caseClass[Module2]) ~> dynRender(m => <.h3(s"Module #2 @ ${m.i}"))
    )
      .notFoundDynamic(_ => SyncIO(redirectToPage(ModuleRoot)(SetRouteVia.HistoryReplace)))
      .verify(ModuleRoot, Module1, Module2(2))
  }

  val Component = RouterWithProps(BaseUrl("http://localhost/"), routerConfig)

}
