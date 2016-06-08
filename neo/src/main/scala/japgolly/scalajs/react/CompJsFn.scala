package japgolly.scalajs.react

import scalajs.js
import japgolly.scalajs.react.CompJs3.Summoner

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
                                                   (implicit s: Summoner[P, C, State]): Constructor[P, s.CC] =
    new Constructor[P, s.CC](rc, s.pf.rmap(s.summon(rc))(u => new Unmounted(u.rawElement)))
    // TODO Avoid this rmap ↗

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
