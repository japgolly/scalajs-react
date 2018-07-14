package japgolly.scalajs.react.component

import scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.{Callback, Children, CtorType, PropsChildren, vdom, raw => RAW}

object JsFn extends JsBaseComponentTemplate[RAW.React.StatelessFunctionalComponent] {

  type Component[P <: js.Object, CT[-p, +u] <: CtorType[p, u]] = ComponentRoot[P, CT, Unmounted[P]]
  type Unmounted[P <: js.Object]                               = UnmountedRoot[P]
  type Mounted                                                 = Unit

  def apply[P <: js.Object, C <: Children]
           (raw: js.Any)
           (implicit s: CtorType.Summoner[P, C], where: sourcecode.FullName, line: sourcecode.Line): Component[P, s.CT] = {
    InspectRaw.assertIsComponent(raw, "JsFnComponent", where, line)
    force[P, C](raw)(s)
  }

  def force[P <: js.Object, C <: Children](raw: js.Any)(implicit s: CtorType.Summoner[P, C]): Component[P, s.CT] = {
    val rc = raw.asInstanceOf[RAW.React.StatelessFunctionalComponent[P]]
    componentRoot[P, s.CT, Unmounted[P]](rc, s.pf.rmap(s.summon(rc))(unmountedRoot))(s.pf)
  }

  def fromJsFn[P <: js.Object, C <: Children](render: js.Function1[P with RAW.PropsWithChildren, RAW.React.Element])
                                             (implicit s: CtorType.Summoner[P, C]): Component[P, s.CT] =
    JsFn.force[P, C](render)(s)

  /** Create JS functional components from Scala. */
  object fromScala {
    import japgolly.scalajs.react.vdom._

    type UnusedObject = Box[Unit]

    def generic[P <: js.Object, C <: Children](render: P with RAW.PropsWithChildren => VdomElement)
                                              (implicit s: CtorType.Summoner[P, C]): Component[P, s.CT] =
      fromJsFn[P, C]((p: P with RAW.PropsWithChildren) => render(p).rawElement)(s)

    def const(render: VdomElement)
             (implicit s: CtorType.Summoner[UnusedObject, Children.None]): Component[UnusedObject, s.CT] =
      generic[UnusedObject, Children.None](_ => render)(s)

    def byName(render: => VdomElement)
              (implicit s: CtorType.Summoner[UnusedObject, Children.None]): Component[UnusedObject, s.CT] =
      generic[UnusedObject, Children.None](_ => render)(s)

    def apply[P <: js.Object](render: P => VdomElement)
                             (implicit s: CtorType.Summoner[P, Children.None]): Component[P, s.CT] =
      generic[P, Children.None](render)(s)

    def withChildren[P <: js.Object](render: (P, PropsChildren) => VdomElement)
                                    (implicit s: CtorType.Summoner[P, Children.Varargs]): Component[P, s.CT] =
      generic[P, Children.Varargs](p => render(p, PropsChildren(p.children)))(s)

    def justChildren(render: PropsChildren => VdomElement)
                    (implicit s: CtorType.Summoner[UnusedObject, Children.Varargs]): Component[UnusedObject, s.CT] =
      generic[UnusedObject, Children.Varargs](p => render(PropsChildren(p.children)))(s)
  }

  private def staticDisplayName = "<FnComponent>"

  override protected def rawComponentDisplayName: RAW.React.StatelessFunctionalComponent[_ <: js.Object] => String =
    _ => staticDisplayName

  // ===================================================================================================================

  sealed trait UnmountedSimple[P, M] extends Generic.UnmountedSimple[P, M] {
    override type Raw <: RAW.React.ComponentElement[_ <: js.Object]
    override final def displayName = staticDisplayName

    override def mapUnmountedProps[P2](f: P => P2): UnmountedSimple[P2, M]
    override def mapMounted[M2](f: M => M2): UnmountedSimple[P, M2]

    override final def renderIntoDOM(container: RAW.ReactDOM.Container, callback: Callback = Callback.empty): Mounted = {
      val result = RAW.ReactDOM.render(raw, container, callback.toJsFn)

      // Protect against future React change.
      assert(result eq null, "Expected rendered functional component to return null; not " + result)

      mountRaw(result)
    }
  }

  sealed trait UnmountedWithRoot[P1, M1, P0 <: js.Object]
      extends UnmountedSimple[P1, M1] with Generic.UnmountedWithRoot[P1, M1, P0, Mounted] {
    override final type Raw = RAW.React.ComponentElement[P0]
    override final type Root = UnmountedRoot[P0]
    override def mapUnmountedProps[P2](f: P1 => P2): UnmountedWithRoot[P2, M1, P0]
    override def mapMounted[M2](f: M1 => M2): UnmountedWithRoot[P1, M2, P0]
  }

  type UnmountedRoot[P <: js.Object] = UnmountedWithRoot[P, Mounted, P]

  private val constUnit: Any => Unit = _ => ()

  def unmountedRoot[P <: js.Object](r: RAW.React.ComponentElement[P]): UnmountedRoot[P] =
    new UnmountedRoot[P] {
      override def mapUnmountedProps[P2](f: P => P2) = mappedU(this)(f, identityFn)
      override def mapMounted[M2](f: Mounted => M2) = mappedU(this)(identityFn, f)

      override def root          = this
      override val raw           = r
      override val mountRaw      = constUnit
      override val vdomElement   = vdom.VdomElement(raw)
      override def key           = jsNullToOption(raw.key)
      override def ref           = None // orNullToOption(raw.ref)
      override def props         = raw.props.asInstanceOf[P]
      override def propsChildren = PropsChildren.fromRawProps(raw.props)
    }

  private def mappedU[P2, M2, P1, M1, P0 <: js.Object](from: UnmountedWithRoot[P1, M1, P0])
                                                      (mp: P1 => P2, mm: M1 => M2): UnmountedWithRoot[P2, M2, P0] =
    new UnmountedWithRoot[P2, M2, P0] {
      override def root          = from.root
      override val raw           = from.raw
      override val mountRaw      = mm compose from.mountRaw
      override def vdomElement   = from.vdomElement
      override def key           = from.key
      override def ref           = from.ref
      override def props         = mp(from.props)
      override def propsChildren = from.propsChildren
      override def mapUnmountedProps[P3](f: P2 => P3) = mappedU(from)(f compose mp, mm)
      override def mapMounted[M3](f: M2 => M3) = mappedU(from)(mp, f compose mm)
    }
}
