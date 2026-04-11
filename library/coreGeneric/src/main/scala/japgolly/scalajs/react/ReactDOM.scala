package japgolly.scalajs.react

import japgolly.scalajs.react.util.Effect._
import org.scalajs.dom
import scala.scalajs.js

object ReactDOM {
  val raw = facade.ReactDOM
  @inline def version = facade.ReactDOM.version

  def flushSync[F[_], A](fa: F[A])(implicit F: Sync[F]): F[A] =
    F.delay(facade.ReactDOM.flushSync(F.toJsFn(fa)))

  /** @since 4.0.0 / React v19 */
  @inline def requestFormReset(form: dom.HTMLFormElement): Unit =
    facade.ReactDOM.requestFormReset(form)

  /** @since 4.0.0 / React v19 */
  @inline def prefetchDNS(href: String): Unit =
    facade.ReactDOM.prefetchDNS(href)

  /** @since 4.0.0 / React v19 */
  @inline def preconnect(
    href       : String,
    crossOrigin: js.UndefOr[String] = js.undefined
  ): Unit = {
    val o = js.Dynamic.literal().asInstanceOf[facade.ReactDOM.PreconnectOptions]
    o.crossOrigin = crossOrigin
    facade.ReactDOM.preconnect(href, o)
  }

  /** @since 4.0.0 / React v19 */
  @inline def preload(
    href          : String,
    as            : String,
    crossOrigin   : js.UndefOr[String] = js.undefined,
    fetchPriority : js.UndefOr[String] = js.undefined,
    imageSizes    : js.UndefOr[String] = js.undefined,
    imageSrcSet   : js.UndefOr[String] = js.undefined,
    integrity     : js.UndefOr[String] = js.undefined,
    `type`        : js.UndefOr[String] = js.undefined,
    nonce         : js.UndefOr[String] = js.undefined,
    referrerPolicy: js.UndefOr[String] = js.undefined,
    media         : js.UndefOr[String] = js.undefined,
  ): Unit = {
    val o = js.Dynamic.literal().asInstanceOf[facade.ReactDOM.PreloadOptions]
    o.as             = as
    o.crossOrigin    = crossOrigin
    o.fetchPriority  = fetchPriority
    o.imageSizes     = imageSizes
    o.imageSrcSet    = imageSrcSet
    o.integrity      = integrity
    o.`type`         = `type`
    o.nonce          = nonce
    o.referrerPolicy = referrerPolicy
    o.media          = media
    facade.ReactDOM.preload(href, o)
  }

  /** @since 4.0.0 / React v19 */
  @inline def preinit(
    href         : String,
    as           : String,
    crossOrigin  : js.UndefOr[String] = js.undefined,
    fetchPriority: js.UndefOr[String] = js.undefined,
    precedence   : js.UndefOr[String] = js.undefined,
    integrity    : js.UndefOr[String] = js.undefined,
    nonce        : js.UndefOr[String] = js.undefined,
  ): Unit = {
    val o = js.Dynamic.literal().asInstanceOf[facade.ReactDOM.PreinitOptions]
    o.as            = as
    o.crossOrigin   = crossOrigin
    o.fetchPriority = fetchPriority
    o.precedence    = precedence
    o.integrity     = integrity
    o.nonce         = nonce
    facade.ReactDOM.preinit(href, o)
  }
}
