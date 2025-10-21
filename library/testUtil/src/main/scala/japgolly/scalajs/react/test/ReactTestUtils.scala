package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import japgolly.scalajs.react.hooks.Hooks
import japgolly.scalajs.react.internal.CoreGeneral._
import japgolly.scalajs.react.test.internal.{Resource, WithDsl}
import japgolly.scalajs.react.util.DefaultEffects.{Sync => DS}
import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.{Effect, ImplicitUnit, JsUtil}
import org.scalajs.dom.html.Element
import org.scalajs.dom.{console, document}

object ReactTestUtils extends ReactTestUtils {
  IsReactActEnvironment = true

  private[ReactTestUtils] object Internals {
    val reactDataAttrRegex = """\s+data-react\S*?\s*?=\s*?".*?"""".r
    val reactTextCommentRegex = """<!-- /?react-text[: ].*?-->""".r

    def warnOnError(prefix: String)(a: => Any): Unit =
      try {
        a
        ()
      } catch {
        case t: Throwable =>
          console.warn(s"$prefix: $t")
      }
  } // Internals
}

trait ReactTestUtils extends japgolly.scalajs.react.test.internal.ReactTestUtilExtensions {
  import ReactTestUtils.Internals._

  private val reactRaw = japgolly.scalajs.react.facade.React

  type Unmounted = GenericComponent.Unmounted[_, Unit]

  type CompType = GenericComponent.ComponentRaw {type Raw <: japgolly.scalajs.react.facade.React.ComponentClassUntyped }

  // ===================================================================================================================

  def IsReactActEnvironment(): Boolean =
    JsUtil.global().IS_REACT_ACT_ENVIRONMENT.asInstanceOf[Boolean]

  def IsReactActEnvironment_=(b: Boolean): Unit =
    JsUtil.global().IS_REACT_ACT_ENVIRONMENT = b

  /** When writing UI tests, tasks like rendering, user events, or data fetching can be considered as "units" of
    * interaction with a user interface. React provides a helper called act() that makes sure all updates related to
    * these "units" have been processed and applied to the DOM before you make any assertions:
    *
    * {{{
    *   act {
    *     // render components
    *   }
    *   // make assertions
    * }}}
    *
    * This helps make your tests run closer to what real users would experience when using your application.
    */
  def actSync[A](body: => A): A = {
    var a = Option.empty[A]
    reactRaw.actSync(() => { a = Some(body) })
    a.getOrElse(throw new RuntimeException("React.act didn't seem to complete."))
  }

  /** When writing UI tests, tasks like rendering, user events, or data fetching can be considered as "units" of
    * interaction with a user interface. React provides a helper called act() that makes sure all updates related to
    * these "units" have been processed and applied to the DOM before you make any assertions:
    *
    * {{{
    *   await act(async () => {
    *     // render components
    *   });
    *   // make assertions
    * }}}
    *
    * This helps make your tests run closer to what real users would experience when using your application.
    */
  def act[F[_], A](body: F[A])(implicit F: Async[F]): F[A] = {
    F.flatMap(F.delay(new Hooks.Var(Option.empty[A]))) { ref =>
      def setAsync(a: A): F[Unit] = F.delay(DS.runSync(ref.set(Some(a))))
      val body2 = F.flatMap(body)(setAsync)
      val body3 = F.fromJsPromise(reactRaw.act(F.toJsPromise(body2)))
      F.map(body3)(_ => ref.value.getOrElse(throw new RuntimeException("React.act didn't seem to complete.")))
    }
  }

  @inline def act_[F[_], A](body: => A)(implicit F: Async[F]): F[A] =
    act(F.delay(body))

  def newElement(): Element = {
    val cont = document.createElement("div").domAsHtml
    document.body.appendChild(cont)
    cont
  }

  def newReactRoot(container: japgolly.scalajs.react.facade.ReactDOMClient.RootContainer): TestReactRoot =
    TestReactRoot(container)

  def removeElement(e: Element): Unit =
    warnOnError("Failed to remove element: " + e) {
      document.body.removeChild(e)
    }

  /** Turn `&lt;div data-reactroot=""&gt;hello&lt/div&gt;`
    * into `&lt;div&gt;hello&lt/div&gt;`
    */
  def removeReactInternals(html: String): String = {
    var h = html
    h = reactTextCommentRegex.replaceAllIn(h, "")
    h = reactDataAttrRegex.replaceAllIn(h, "")
    h
  }

  val withElementSync: WithDsl[Element, ImplicitUnit] =
    WithDsl(newElement())(removeElement)

  def withElement[F[_]: Async]: Resource[F, Element] =
    Resource.make(Effect[F].delay(newElement()), e => Effect[F].delay(removeElement(e)))

  val withReactRootSync: WithDsl[TestReactRoot, ImplicitUnit] =
    withElementSync
      .mapResource(e => {
        val stop = ReactTestUtilsConfig.aroundReact.start()
        (e, stop)
      })(_._2())
      .mapResource(x => TestReactRoot(x._1))(_.unmountSync())

  def withReactRoot[F[_]](implicit F: Async[F]): Resource[F, TestReactRoot] =
    withElement[F].flatMap { e =>
      Resource.make_[F, () => Unit](ReactTestUtilsConfig.aroundReact.start(), stop => F.delay(stop())).flatMap { _ =>
        Resource.make_[F, TestReactRoot](TestReactRoot(e), _.unmount[F]())
      }
    }

  def withRenderedSync[A](unmounted: A): WithDsl[TestDomWithRoot, Renderable[A]] =
    WithDsl.apply[TestDomWithRoot, Renderable[A]] { (renderable, cleanup) =>
      val root = withReactRootSync.setup(implicitly, cleanup)
      root.renderSync(unmounted)(renderable)
      root.selectFirstChild()
    }

  def rendered[F[_]: Async, A: Renderable](unmounted: A): Resource[F, TestDomWithRoot] =
    withReactRoot[F].flatMap { root =>
      Resource.eval[F, TestDomWithRoot](
        Effect[F].map(root.render(unmounted))(_ => root.selectFirstChild())
      )
    }

  def withRendered[F[_]: Async, A: Renderable, B](unmounted: A)(f: TestDomWithRoot => F[B]): F[B] =
    rendered(unmounted).use(f)

  def withRendered_[F[_]: Async, A: Renderable, B](unmounted: A)(f: TestDomWithRoot => B): F[B] =
    rendered(unmounted).use_(f)
}
