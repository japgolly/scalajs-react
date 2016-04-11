package japgolly.scalajs.react.experimental

import scala.scalajs.js
import japgolly.scalajs.react.{BackendScope => NormalBackendScope, _}
import japgolly.scalajs.react.macros.CompBuilderMacros
import CompScope._
import ReactComponentB.BackendKey
import StaticPropComponent.PropPair

// TODO Add: ScalaDoc, GH doc, gh-pages example, doc testing.

/*
object xxxxx extends StaticPropComponent.Template("xxxxx") {
  override protected def configureBackend = new Backend(_, _)
  override protected def configureRender  = _.renderBackend
}

object Example extends StaticPropComponent.BareTemplate {
  override type StaticProps  = Nothing
  override type DynamicProps = Nothing
  override type Backend      = Nothing

  override protected def displayName         = ???
  override protected def configureState      = ???
  override protected def configureBackend    = ???
  override protected def configureRender     = ???
  override protected def staticPropsEquality = ???
}
*/

trait StaticPropComponent {
  type StaticProps
  type DynamicProps
  type ComponentState = Unit
  type Backend
  type Node = TopNode

  final type ComponentProps = PropPair[StaticProps, DynamicProps]
  final type BackendScope = BackendScopeMP[DynamicProps, ComponentState]

  val Component: ReactComponentC.ReqProps[ComponentProps, ComponentState, Backend, Node]

  def apply(sp: StaticProps) =
    (dp: DynamicProps) => Component(PropPair(sp, dp))
}

object StaticPropComponent {
  final case class PropPair[Static, Dynamic](static: Static, dynamic: Dynamic)

  trait BareTemplate extends StaticPropComponent {

    protected def displayName: String
    protected def configureState: ReactComponentB.P[ComponentProps] => ReactComponentB.PS[ComponentProps, ComponentState]
    protected def configureBackend: (StaticProps, BackendScope) => Backend
    protected def configureRender: NeedRender[StaticProps, DynamicProps, ComponentState, Backend, ReactComponentB.PSBR[ComponentProps, ComponentState, Backend]] => ReactComponentB.PSBR[ComponentProps, ComponentState, Backend]

    protected def staticPropsEquality: (StaticProps, StaticProps) => Boolean

    protected def warnStaticPropsChange: (StaticProps, StaticProps) => Callback =
      (a, b) => Callback.warn(s"[$displayName] Static props changed\nfrom $a\n  to $b")

    protected def configure: ReactComponentB[ComponentProps, ComponentState, Backend, Node] => ReactComponentB[ComponentProps, ComponentState, Backend, Node] =
      identity

    final override val Component: ReactComponentC.ReqProps[ComponentProps, ComponentState, Backend, Node] = {
      val eq = staticPropsEquality
      def newBackendScopeMP($: NormalBackendScope[ComponentProps, ComponentState]) =
        BackendScopeMP($)(_.dynamic)

      val a = ReactComponentB[ComponentProps](displayName)
      val b = configureState(a)
      val c = b.backend($ => configureBackend($.props.runNow().static, newBackendScopeMP($)))
      val d = configureRender(new NeedRender(c.render))
      val e = d
        .domType[Node]
        .componentWillReceiveProps { i =>
          val sp1 = i.currentProps.static
          val sp2 = i.nextProps.static
          if (eq(sp1, sp2))
            Callback.empty
          else
            warnStaticPropsChange(sp1, sp2).attempt >>
              Callback {
                val raw = i.$.asInstanceOf[js.Dictionary[js.Any]]
                val bs = newBackendScopeMP(raw.asInstanceOf[NormalBackendScope[ComponentProps, ComponentState]])
                val nb = configureBackend(sp2, bs)
                raw.update(BackendKey, nb.asInstanceOf[js.Any])
              }
        }
      configure(e).propsRequired.build
    }
  }

  /**
    * Where as [[BareTemplate]] makes no assumptions at all, this makes a few to reduce boilerplate in 95%+ of usage.
    */
  abstract class Template(override protected val displayName: String) extends BareTemplate {
    final override type ComponentState = Unit
    final override protected def configureState = _.stateless

    override type StaticProps <: AnyRef
    override protected def staticPropsEquality = _ eq _

    // TODO would be nice to add configure{Backend,Render} here too but that needs a macro
  }

  final class NeedRender[P, Q, S, B, Out] private[experimental](private val g: (DuringCallbackU[PropPair[P, Q], S, B] => ReactElement) => Out) extends AnyVal {

    def render(f: DuringCallbackU[PropPair[P, Q], S, B] => ReactElement): Out =
      g(f)

    def renderPCS(f: (DuringCallbackU[PropPair[P, Q], S, B], Q, PropsChildren, S) => ReactElement): Out =
      render($ => f($, $.props.dynamic, $.propsChildren, $.state))

    def renderPC(f: (DuringCallbackU[PropPair[P, Q], S, B], Q, PropsChildren) => ReactElement): Out =
      render($ => f($, $.props.dynamic, $.propsChildren))

    def renderPS(f: (DuringCallbackU[PropPair[P, Q], S, B], Q, S) => ReactElement): Out =
      render($ => f($, $.props.dynamic, $.state))

    def renderP(f: (DuringCallbackU[PropPair[P, Q], S, B], Q) => ReactElement): Out =
      render($ => f($, $.props.dynamic))

    def renderCS(f: (DuringCallbackU[PropPair[P, Q], S, B], PropsChildren, S) => ReactElement): Out =
      render($ => f($, $.propsChildren, $.state))

    def renderC(f: (DuringCallbackU[PropPair[P, Q], S, B], PropsChildren) => ReactElement): Out =
      render($ => f($, $.propsChildren))

    def renderS(f: (DuringCallbackU[PropPair[P, Q], S, B], S) => ReactElement): Out =
      render($ => f($, $.state))

    def render_P(f: Q => ReactElement): Out =
      render($ => f($.props.dynamic))

    def render_C(f: PropsChildren => ReactElement): Out =
      render($ => f($.propsChildren))

    def render_S(f: S => ReactElement): Out =
      render($ => f($.state))

    /**
      * Use a method named `render` in the backend, automatically populating its arguments with props, state,
      * propsChildren where needed.
      */
    def renderBackend: Out =
      macro CompBuilderMacros.renderBackendSP[P, Q, S, B]
  }
}
