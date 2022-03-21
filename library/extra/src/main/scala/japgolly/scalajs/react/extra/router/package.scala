package japgolly.scalajs.react.extra

import japgolly.scalajs.react.extra.router.RouterConfig.{Logger, Parsed}
import japgolly.scalajs.react.internal.CoreGeneral._
import japgolly.scalajs.react.util.DefaultEffects.Sync
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.{CtorType, ScalaComponent}

package object router {

  type Action     [Page, -Props] = ActionF[Sync, Page, Props]
  type Renderer   [Page, -Props] = RendererF[Sync, Page, Props]
  type RouterCtl  [A]            = RouterCtlF[Sync, A]
  type RouterLogic[Page, Props]  = RouterLogicF[Sync, Page, Props]

  type RouterWithPropsF [F[_], P, Props] = ScalaComponent              [Props, ResolutionWithProps[P, Props], OnUnmountF[F], CtorType.Props]
  type RouterWithPropsFU[F[_], P, Props] = ScalaComponent.Unmounted    [Props, ResolutionWithProps[P, Props], OnUnmountF[F]]
  type RouterWithPropsFM[F[_], P, Props] = ScalaComponent.MountedImpure[Props, ResolutionWithProps[P, Props], OnUnmountF[F]]

  type RouterWithProps [P, Props] = RouterWithPropsF [Sync, P, Props]
  type RouterWithPropsU[P, Props] = RouterWithPropsFU[Sync, P, Props]
  type RouterWithPropsM[P, Props] = RouterWithPropsFM[Sync, P, Props]

  // START: Compatibility with no-props Router API
  type RouterConfig[P] = RouterWithPropsConfig[P, Unit]
  type Resolution[P]   = ResolutionWithProps[P, Unit]

  type RouterF [F[_], P] = ScalaComponent              [Unit, Resolution[P], OnUnmountF[F], CtorType.Nullary]
  type RouterFU[F[_], P] = ScalaComponent.Unmounted    [Unit, Resolution[P], OnUnmountF[F]]
  type RouterFM[F[_], P] = ScalaComponent.MountedImpure[Unit, Resolution[P], OnUnmountF[F]]

  type Router [P] = RouterF [Sync, P]
  type RouterU[P] = RouterFU[Sync, P]
  type RouterM[P] = RouterFM[Sync, P]
  // END

  private[router] implicit class OptionFnExt[A, B](private val f: A => Option[B]) extends AnyVal {
    def ||(g: A => Option[B]): A => Option[B] = a => f(a) orElse g(a)
    def | (g: A => B)        : A => B         = a => f(a) getOrElse g(a)
  }

  private[router] implicit class CallbackToOptionFnExt[A, B](private val f: A => Sync[Option[B]]) extends AnyVal {
    def ||(g: A => Sync[Option[B]]): A => Sync[Option[B]] =
      a => Sync.flatMap(f(a)) {
        case s@ Some(_) => Sync.pure(s)
        case None       => g(a)
      }
  }

  private[router] implicit class OptionFn2Ext[A, B, C](private val f: (A, B) => Option[C]) extends AnyVal {
    def ||(g: (A, B) => Option[C]): (A, B) => Option[C] = (a, b) => f(a, b) orElse g(a, b)
    def | (g: (A, B) => C)        : (A, B) => C         = (a, b) => f(a, b) getOrElse g(a, b)
  }

  private[router] implicit class CallbackToOptionFn2Ext[A, B, C](private val f: (A, B) => Sync[Option[C]]) extends AnyVal {
    def ||(g: (A, B) => Sync[Option[C]]): (A, B) => Sync[Option[C]] =
      (a, b) => Sync.flatMap(f(a, b)) {
        case s@ Some(_) => Sync.pure(s)
        case None       => g(a, b)
      }
  }

  private[router] implicit class SaneEitherMethods[A, B](private val e: Either[A, B]) extends AnyVal {
    def map[C](f: B => C): Either[A, C] =
      e match {
        case Right(b) => Right(f(b))
        case l: Left[A, B] => l.asInstanceOf[Left[A, Nothing]]
      }

    def bimap[C, D](f: A => C, g: B => D): Either[C, D] =
      e match {
        case Right(b) => Right(g(b))
        case Left(a)  => Left(f(a))
      }
  }

  // =====================================================================================================================

  /** A complete set of routing rules that allow the router to handle every all routes without further input.
    *
    * @tparam Page The type of legal pages. Most commonly, a sealed trait that you've created, where all subclasses
    *              represent a page in your SPA.
    */
  type RoutingRules[Page, Props] = RoutingRulesF[Sync, Page, Props]

  object RoutingRules {

    type Exception = RoutingRulesF.Exception

    @inline def fromRule[Page, Props](rule          : RoutingRule[Page, Props],
                                      fallbackPath  : Page => Path,
                                      fallbackAction: (Path, Page) => Action[Page, Props],
                                      whenNotFound  : Path => Sync[Parsed[Page]],
                                     ): RoutingRules[Page, Props] =
      RoutingRulesF.fromRule(rule, fallbackPath, fallbackAction, whenNotFound)

    /** Create routing rules all at once, with compiler proof that all `Page`s will have a `Path` and `Action`
      * associated.
      *
      * The trade-off here is that care will need to be taken to ensure that path-parsing aligns with paths
      * generated for pages. It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
      */
    @inline def bulk[Page, Props](toPage  : Path => Option[Parsed[Page]],
                                  fromPage: Page => (Path, Action[Page, Props]),
                                  notFound: Path => Parsed[Page],
                                 ): RoutingRules[Page, Props] =
      RoutingRulesF.bulk(toPage, fromPage, notFound)

    /** Create routing rules all at once, with compiler proof that all `Page`s will have a `Path` and `Action`
      * associated.
      *
      * The trade-off here is that care will need to be taken to ensure that path-parsing aligns with paths
      * generated for pages. It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
      */
    @inline def bulkDynamic[Page, Props](toPage  : Path => Sync[Option[Parsed[Page]]],
                                         fromPage: Page => (Path, Sync[Action[Page, Props]]),
                                         notFound: Path => Parsed[Page],
                                        ): RoutingRules[Page, Props] =
      RoutingRulesF.bulkDynamic(toPage, fromPage, notFound)
  }

  // ===================================================================================================================

  type RouterWithPropsConfig[Page, Props] = RouterWithPropsConfigF[Sync, Page, Props]

  @inline def RouterWithPropsConfig[Page, Props](
      rules       : RoutingRules[Page, Props],
      renderFn    : (RouterCtl[Page], ResolutionWithProps[Page, Props]) => Props => VdomElement,
      postRenderFn: (Option[Page], Page, Props) => Sync[Unit],
      logger      : Logger,
     ): RouterWithPropsConfig[Page, Props] =
   RouterWithPropsConfigF(rules, renderFn, postRenderFn, logger)
}
