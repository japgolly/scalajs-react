package japgolly.scalajs.react

import scalajs.js
import japgolly.scalajs.react.internal._

object CompJsFn {
  /*
  type S = Null

  type Constructor[P <: js.Object, C[a, b] <: CtorType[a, b]] = CompJs3X.Constructor[P, S, C, Null]

  def Constructor[P <: js.Object, C <: ChildrenArg](rc: raw.ReactFunctionalComponent)
                                                   (implicit s: Summoner[P, C, S]): Constructor[P, s.CC] =
    new CompJs3X.Constructor[P, S, s.CC, Mounted[P, S]](rc, s.summon(rc)).mapMounted[Null](x => {
      assert(x.rawInstance eq null, "Expected null; got: " + x.rawInstance)
      null
    })(s.pf)
*/

  type State = Null

  def Constructor[P <: js.Object, C <: ChildrenArg](rc: raw.ReactFunctionalComponent)
                                                   (implicit s: CtorType.Summoner[P, C]): Constructor[P, s.CC] =
    new Constructor[P, s.CC](rc, s.pf.rmap(s.summon(rc))(new Unmounted(_)))

  class Constructor[P <: js.Object, C[a, b] <: CtorType[a, b]](val rawFn: raw.ReactFunctionalComponent, val ctor: C[P, Unmounted[P]])
    extends BaseCtor[P, C, Unmounted[P]] {
//    def mapMounted[MM](f: M => MM)(implicit p: Profunctor[C]): Constructor[P, S, C, MM] =
//      new Constructor(rawCls, ctor rmap (_ mapMounted f))
  }

  final class Unmounted[P <: js.Object](val rawElement: raw.ReactComponentElement) {

    def key: Option[Key] =
      orNullToOption(rawElement.key)

//    def ref: Option[String] =
//      orNullToOption(rawElement.ref)

    def props: P =
      rawElement.props.asInstanceOf[P]

    def propsChildren: PropsChildren =
      PropsChildren(rawElement.props.children)

//    def mapMounted[MM](f: M => MM): Unmounted[P, S, MM] =
//      new Unmounted(rawElement, f compose m)

    def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): Unit = {
      val result = raw.ReactDOM.render(rawElement, container, callback.toJsFn)
      assert(result eq null, "Expected rendered functional component to return null; not " + result)
    }
  }
}


object CompScalaFn {

  private def create[P, C <: ChildrenArg](f: Box[P] with raw.PropsWithChildren => raw.ReactElement)
                                         (implicit s: CtorType.Summoner[Box[P], C]) = {
    val fn1 = f: js.Function1[Box[P] with raw.PropsWithChildren, raw.ReactElement]
    val fn2 = fn1.asInstanceOf[raw.ReactFunctionalComponent]
    val jc = CompJsFn.Constructor[Box[P], C](fn2)(s)
    new Ctor(jc)(s.pf)
  }

  def props[P](render: P => raw.ReactElement) =
    create[P, ChildrenArg.None](b => render(b.a))

  def propsAndChildren[P](render: (P, PropsChildren) => raw.ReactElement) =
    create[P, ChildrenArg.Varargs](b => render(b.a, PropsChildren(b.children)))

  def children(render: PropsChildren => raw.ReactElement) =
    create[Unit, ChildrenArg.Varargs](b => render(PropsChildren(b.children)))

  case class Ctor[P, C[a, b] <: CtorType[a, b]](jsInstance: CompJsFn.Constructor[Box[P], C])
                                               (implicit pf: Profunctor[C])
    extends BaseCtor[P, C, Unmounted[P]] {

    override val ctor: C[P, Unmounted[P]] =
      jsInstance.ctor.dimap(Box(_), new Unmounted(_))
  }

  final class Unmounted[P](val jsInstance: CompJsFn.Unmounted[Box[P]]) {

    def key: Option[Key] =
      jsInstance.key

    def props: P =
      jsInstance.props.a

    def propsChildren: PropsChildren =
      jsInstance.propsChildren

    def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): Unit =
      jsInstance.renderIntoDOM(container, callback)
  }

//  val cp = props[Int](???)
//  cp(23)
//
//  val cpc = propsAndChildren[Int](???)
//  cpc(23)()
//
//  val cc = children(???)
//  cc()
}