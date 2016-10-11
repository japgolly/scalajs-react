package japgolly.scalajs.react

import org.scalajs.dom

import scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.{raw => Raw}
import JsComponent._

import scala.scalajs.js.UndefOr
import scala.util.{Failure, Success, Try}

final class JsComponent[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u], M]
    (val raw: Raw.ReactClass, override val ctor: CT[P, Unmounted[P, S, M]])(implicit pf: Profunctor[CT])
    extends Component[P, CT, Unmounted[P, S, M]] {

  def mapMounted[MM](f: M => MM): JsComponent[P, S, CT, MM] =
    new JsComponent(raw, ctor rmap (_ mapMounted f))
}

object JsComponent {

  final class CompStdOps[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u], R <: RawMounted]
      (private val self: JsComponent[P, S, CT, JsComponent.Mounted[P, S, R]]) extends AnyVal {

    def addRawType[T <: js.Object]: JsComponent[P, S, CT, Mounted[P, S, R with T]] =
      self.mapMounted(_.addRawType[T])
  }

  @inline implicit def toJsCompStdOps
      [P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u], R <: RawMounted]
      (c: JsComponent[P, S, CT, Mounted[P, S, R]]): CompStdOps[P, S, CT, R] =
    new CompStdOps(c)

  // ===================================================================================================================

  final class Unmounted[P <: js.Object, S <: js.Object, M]
      (val raw: Raw.ReactComponentElement, m: Raw.ReactComponent => M)
      extends Component.Unmounted[P, M] {

    override val reactElement =
      vdom.ReactElement(raw)

    override def key: Option[Key] =
      jsNullToOption(raw.key)

    override def ref: Option[String] =
      jsNullToOption(raw.ref)

    override def props: P =
      raw.props.asInstanceOf[P]

    override def propsChildren: PropsChildren =
      PropsChildren(raw.props.children)

    override def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty): M =
      m(Raw.ReactDOM.render(raw, container, callback.toJsFn))

    override def mapMounted[MM](f: M => MM): Unmounted[P, S, MM] =
      new Unmounted(raw, f compose m)
  }

  // ===================================================================================================================

  type RawMounted = Raw.ReactComponent

  final class Mounted[P <: js.Object, S <: js.Object, Raw <: RawMounted](val raw: Raw)
      extends Component.Mounted[Effect.Id, P, S] {

    override protected implicit def F = Effect.InstanceId

    override def props: P =
      raw.props.asInstanceOf[P]

    override def propsChildren =
      PropsChildren(raw.props.children)

    override def state: S =
      raw.state.asInstanceOf[S]

    override def setState(state: S, callback: Callback = Callback.empty): Unit =
      raw.setState(state, callback.toJsFn)

    override def modState(mod: S => S, callback: Callback = Callback.empty): Unit =
      raw.modState(mod.asInstanceOf[js.Object => js.Object], callback.toJsFn)

    override def isMounted =
      raw.isMounted()

    override def getDOMNode: dom.Element =
      Raw.ReactDOM.findDOMNode(raw)

    override def forceUpdate(callback: Callback = Callback.empty): Unit =
      raw.forceUpdate(callback.toJsFn)

    def addRawType[T <: js.Object]: Mounted[P, S, Raw with T] =
      this.asInstanceOf[Mounted[P, S, Raw with T]]

//      def getDefaultProps: Props
//      def getInitialState: js.Object | Null
//      def render(): ReactElement

//    override def mapProps[A](f: P => A): Mounted[A, S, Raw] = {
//      val self = this
//      new Mounted[A, S, Raw] {
//        override val raw = self.raw
//        override def props: A = f(self.props)
//      }
//    }
  }

  type UnmountedWithRawType[P <: js.Object, S <: js.Object, T <: js.Object] =
    Unmounted[P, S, MountedWithRawType[P, S, T]]

  type MountedWithRawType[P <: js.Object, S <: js.Object, T <: js.Object] =
    Mounted[P, S, RawMounted with T]

  // ===================================================================================================================

  type Basic[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u]] =
    JsComponent[P, S, CT, BasicMounted[P, S]]

  type BasicUnmounted[P <: js.Object, S <: js.Object] =
    Unmounted[P, S, BasicMounted[P, S]]

  type BasicMounted[P <: js.Object, S <: js.Object] =
    Mounted[P, S, RawMounted]

  def apply[P <: js.Object, C <: ChildrenArg, S <: js.Object](rc: Raw.ReactClass)
                                                             (implicit s: CtorType.Summoner[P, C]): Basic[P, S, s.CT] =
    new JsComponent[P, S, s.CT, BasicMounted[P, S]](rc, s.pf.rmap(s.summon(rc))(BasicUnmounted(_)))(s.pf)

  def BasicUnmounted[P <: js.Object, S <: js.Object](r: Raw.ReactComponentElement): BasicUnmounted[P, S] =
    new Unmounted(r, BasicMounted[P, S])

  def BasicMounted[P <: js.Object, S <: js.Object](r: RawMounted): BasicMounted[P, S] =
    new Mounted(r)

  def byName[P <: js.Object, C <: ChildrenArg, S <: js.Object](name: String)
      (implicit s: CtorType.Summoner[P, C]): Basic[P, S, s.CT] = {

    val reactClass = findInScope(name.split('.').toList) match {
      case Some(constructor : js.Function) => constructor
      case Some(_) => throw new IllegalArgumentException(s"React constructor $name is not a function")
      case None => throw new IllegalArgumentException(s"React constructor $name is not defined")
    }
    apply[P, C, S](reactClass.asInstanceOf[Raw.ReactClass])(s)
  }

  private[this] def findInScope(path: List[String], scope: js.Dynamic = js.Dynamic.global) : Option[js.Dynamic] = {
    path match {
      case Nil => Some(scope)
      case name :: tail =>
        val nextScope = scope.selectDynamic(name).asInstanceOf[js.UndefOr[js.Dynamic]].toOption
        nextScope.flatMap(s => findInScope(tail, s))
    }
  }

}
