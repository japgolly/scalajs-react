package japgolly.scalajs.react

import scalajs.js
import japgolly.scalajs.react.internal._
import ScalaFnComponent._

final class ScalaFnComponent[P, CT[_, _] <: CtorType[_, _]](val js: JsFnComponent[Box[P], CT])
                                                           (implicit pf: Profunctor[CT])
    extends Component[P, CT, Unmounted[P]] {

  override val ctor: CT[P, Unmounted[P]] =
    js.ctor.dimap(Box(_), _.mapProps(_.a))
}

object ScalaFnComponent {

  type Unmounted[P] = Component.Unmounted[P, Mounted]

  type Mounted = Unit

  // ===================================================================================================================

  @inline private def create[P, C <: ChildrenArg]
      (f: Box[P] with raw.PropsWithChildren => raw.ReactElement)
      (implicit s: CtorType.Summoner[Box[P], C]): ScalaFnComponent[P, s.CT] =
    create2[P, C, s.CT](f)(s)

  private def create2[P, C <: ChildrenArg, CT[-p, +u] <: CtorType[p, u]]
      (f: Box[P] with raw.PropsWithChildren => raw.ReactElement)
      (implicit s: CtorType.Summoner.Aux[Box[P], C, CT]): ScalaFnComponent[P, CT] = {
    val fn1 = f: js.Function1[Box[P] with raw.PropsWithChildren, raw.ReactElement]
    val fn2 = fn1.asInstanceOf[raw.ReactFunctionalComponent]
    val jc = JsFnComponent[Box[P], C](fn2)(s)
    new ScalaFnComponent(jc)(s.pf)
  }

  def Props[P](render: P => raw.ReactElement)
              (implicit s: CtorType.Summoner[Box[P], ChildrenArg.None]) =
    create[P, ChildrenArg.None](b => render(b.a))(s)

  def PropsAndChildren[P](render: (P, PropsChildren) => raw.ReactElement)
                         (implicit s: CtorType.Summoner[Box[P], ChildrenArg.Varargs]) =
    create[P, ChildrenArg.Varargs](b => render(b.a, PropsChildren(b.children)))(s)

  def Children(render: PropsChildren => raw.ReactElement): ScalaFnComponent[Unit, CtorType.Children] =
    create2(b => render(PropsChildren(b.children)))

// TODO TEST!
//  val cp = props[Int](???)
//  cp(23)
//
//  val cpc = propsAndChildren[Int](???)
//  cpc(23)()
//
//  val cc = children(???)
//  cc()
}