package japgolly.scalajs

import japgolly.scalajs.react.raw.ReactComponent

import scalajs.js
import org.scalajs.dom

/*
Bad approaches
==============
* Building Types via conjunction - too hard to map
* JS + implicit ops - extern JS types can't be changed
* PSBN = annoying. PS usually enough.


[ ] Prevent certain lifecycle methods being called in certain scopes.
[ ] Make easy to add functionality (such as Id/CallbackTo, S zoom, P map).
[ ] All components: Id/Callback.
[ ] All components: S zoom.
[ ] All components: P map.
[ ] Typify PropsChildren.
[ ] Easily facade JS components.
[ ] Easily facade JS ES6 components.
[ ] Create ES6 components in Scala.
*/

package object react {

  type Callback = CallbackTo[Unit]

  /*
  case class CtorLike[In, P, Out](apply: (In, P) => Out) extends AnyVal

  @inline implicit class CtorLikeOps[In, P, Out](ctor: In)(implicit like: CtorLike[In, P, Out]) {
    @inline def apply(p: P): Out =
      like.apply(ctor, p)
    @inline def cmap[P2](f: P2 => P) =
      new MappedCtor[In, P, P2, Out](ctor, f)(like)
  }

  case class ReactComponentU[P <: js.Object](r: raw.ReactComponent[P])

  class MappedCtor[In, P0 <: js.Object, P, Out](val underlying: In, val f: P => P0)(implicit val like: CtorLike[In, P0, Out])
  implicit def likeMappedCtor[In, P0 <: js.Object, P, Out] = CtorLike[MappedCtor[In, P0 , P, Out], P, Out](
    (c, p) => c.like.apply(c.underlying, c.f(p)))

  class JsClassCtor[P <: js.Object](val cls: raw.ReactClass[P])
  implicit def likeJsClassCtor[P <: js.Object] = CtorLike[JsClassCtor[P], P, ReactComponentU[P]](
    (c, p) => ReactComponentU(c.cls(p)))

  class ScalaClassCtor[P](val js: JsClassCtor[Box[P]]) {
//    def backend
  }
  implicit def likeScalaClassCtor[P] = CtorLike[ScalaClassCtor[P], P, ReactComponentU[Box[P]]](
    (c, p) => c.js(Box(p)))

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  abstract class TEST {
    type P <: js.Object
    def p: P

    def jsClassCtor: JsClassCtor[P]
    jsClassCtor(p)

    jsClassCtor.cmap(???).apply(???)

  }
  */

//  class JsClassCtor[P <: js.Object](val raw: Raw.ReactClass[P]) extends AnyVal {
//    def apply(props: P): CompU[P] =
//      new CompU(raw(props))
//  }
//
//  class CompU[P <: js.Object](val raw: Raw.ReactComponent[P]) extends AnyVal {
//
//    def render(container: Raw.ReactDOM.Container): CompM[P] = {
//      val m: Raw.ReactComponent[_] = Raw.ReactDOM.render(raw.render(), container)
//      new CompM(m.asInstanceOf[Raw.ReactComponent[P]])
//    }
//  }
//
//  class CompM[P <: js.Object](val raw: Raw.ReactComponent[P]) extends AnyVal {
//
//  }

  import scalajs.js.|
  def orNullToOption[A](an: A | Null): Option[A] =
    Option(an.asInstanceOf[A])

  type Key = String | Boolean | raw.JsNumber

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
/*
  implicit def toPlainMountedNessNess[P <: js.Object, S <: js.Object]: raw.ReactComponent => CompJs3X.Mounted[P, S] =
    r => new CompJs3X.Mounted[P, S] {
      override val rawInstance = r
    }

  object CompJs3 {
    type Constructor[P <: js.Object, S <: js.Object] = CompJs3X.Constructor[P, S, CompJs3X.Mounted[P, S]]
    type Constructor_NoProps[S <: js.Object] = CompJs3X.Constructor_NoProps[S, CompJs3X.Mounted[Null, S]]

    def Constructor[P <: js.Object, S <: js.Object](r: raw.ReactClass): Constructor[P, S]      = CompJs3X.Constructor(r)
    def Constructor_NoProps[S <: js.Object]        (r: raw.ReactClass): Constructor_NoProps[S] = CompJs3X.Constructor_NoProps(r)
  }

  object CompJs3X {

    trait HasRaw {
      val rawInstance: raw.ReactComponent

      final def rawDyn: js.Dynamic =
        rawInstance.asInstanceOf[js.Dynamic]
    }

    case class Constructor[P <: js.Object, S <: js.Object, M](rawCls: raw.ReactClass) {
      def apply(props: P): Unmounted[P, S, M] =
        new Unmounted(raw.React.createElement(rawCls, props))
    }

    case class Constructor_NoProps[S <: js.Object, M](rawCls: raw.ReactClass) {
      private val instance: Unmounted[Null, S, M] =
        new Constructor(rawCls).apply(null)

      def apply(): Unmounted[Null, S, M] =
        instance
    }

    class Unmounted[P <: js.Object, S <: js.Object, M](val rawElement: raw.ReactComponentElement) {

      def key: Option[Key] =
        orNullToOption(rawElement.key)

      def ref: Option[String] =
        orNullToOption(rawElement.ref)

      def props: P =
        rawElement.props.asInstanceOf[P]

      def children: raw.ReactNodeList =
        rawElement.props.children

      def renderIntoDOM(container: raw.ReactDOM.Container, callback: js.ThisFunction = null)
                       (implicit b: raw.ReactComponent => M): M =
        b(raw.ReactDOM.render(rawElement, container, callback))
    }

    trait Mounted[P <: js.Object, S <: js.Object] extends HasRaw {
      //      def getDefaultProps: Props
      //      def getInitialState: js.Object | Null
      //      def render(): ReactElement

      final def isMounted(): Boolean =
        rawInstance.isMounted()

      final def props: P =
        rawInstance.props.asInstanceOf[P]

      final def children: raw.ReactNodeList =
        rawInstance.props.children

      final def state: S =
        rawInstance.state.asInstanceOf[S]

      final def setState(state: S, callback: Callback = Callback.empty): Unit =
        rawInstance.setState(state, callback.toJsFn)

      final def getDOMNode(): dom.Element =
        raw.ReactDOM.findDOMNode(rawInstance)
    }
  }
  */

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  object CompJs3 {
    type Constructor_NoProps[S <: js.Object] = CompJs3X.Constructor_NoProps[S, Mounted[Null, S]]
    type Constructor[P <: js.Object, S <: js.Object] = CompJs3X.Constructor[P, S, Mounted[P, S]]
    type Unmounted  [P <: js.Object, S <: js.Object] = CompJs3X.Unmounted  [P, S, Mounted[P, S]]
    type Mounted    [P <: js.Object, S <: js.Object] = CompJs3X.Mounted    [P, S, raw.ReactComponent]

    def Constructor[P <: js.Object, S <: js.Object](r: raw.ReactClass): Constructor[P, S] =
      CompJs3X.Constructor(r)(Mounted[P, S])

    def Constructor_NoProps[S <: js.Object](r: raw.ReactClass): Constructor_NoProps[S] =
      CompJs3X.Constructor_NoProps(r)(Mounted[Null, S])

    def Mounted[P <: js.Object, S <: js.Object](r: raw.ReactComponent): Mounted[P, S] =
      CompJs3X.Mounted(r)
  }

  object CompJs3X {

    case class Constructor[P <: js.Object, S <: js.Object, M](rawCls: raw.ReactClass)(m: raw.ReactComponent => M) {
      def mapMounted[MM](f: M => MM): Constructor[P, S, MM] =
        new Constructor(rawCls)(f compose m)

      def apply(props: P): Unmounted[P, S, M] =
        new Unmounted(raw.React.createElement(rawCls, props), m)
    }

    case class Constructor_NoProps[S <: js.Object, M](rawCls: raw.ReactClass)(m: raw.ReactComponent => M) {
      def mapMounted[MM](f: M => MM): Constructor_NoProps[S, MM] =
        new Constructor_NoProps(rawCls)(f compose m)

      private val instance: Unmounted[Null, S, M] =
        new Constructor(rawCls)(m)(null)

      def apply(): Unmounted[Null, S, M] =
        instance
    }

    class Unmounted[P <: js.Object, S <: js.Object, M](val rawElement: raw.ReactComponentElement, m: raw.ReactComponent => M) {

      def key: Option[Key] =
        orNullToOption(rawElement.key)

      def ref: Option[String] =
        orNullToOption(rawElement.ref)

      def props: P =
        rawElement.props.asInstanceOf[P]

      def children: raw.ReactNodeList =
        rawElement.props.children

      def mapMounted[MM](f: M => MM): Unmounted[P, S, MM] =
        new Unmounted(rawElement, f compose m)

      def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): M =
        m(raw.ReactDOM.render(rawElement, container, callback.toJsFn))
    }

    def Mounted[P <: js.Object, S <: js.Object, Raw <: raw.ReactComponent](r: Raw): Mounted[P, S, Raw] =
      new Mounted[P, S, Raw] {
        override val rawInstance = r
      }

    trait Mounted[P <: js.Object, S <: js.Object, Raw <: raw.ReactComponent] {
      val rawInstance: Raw

      def addRawType[T <: js.Object]: Mounted[P, S, Raw with T] =
        this.asInstanceOf[Mounted[P, S, Raw with T]]

      //      def getDefaultProps: Props
      //      def getInitialState: js.Object | Null
      //      def render(): ReactElement

      final def isMounted(): Boolean =
        rawInstance.isMounted()

      final def props: P =
        rawInstance.props.asInstanceOf[P]

      final def children: raw.ReactNodeList =
        rawInstance.props.children

      final def state: S =
        rawInstance.state.asInstanceOf[S]

      final def setState(state: S, callback: Callback = Callback.empty): Unit =
        rawInstance.setState(state, callback.toJsFn)

      final def getDOMNode(): dom.Element =
        raw.ReactDOM.findDOMNode(rawInstance)
    }

  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  object CompScala {

    /**
      * Implicit that automatically determines the type of component to build.
      */
    sealed abstract class BuildResult[P, S] {
      type Out
      val build: CompJs3.Constructor[Box[P], Box[S]] => Out

      final def apply(c: raw.ReactClass): Out =
        build(CompJs3.Constructor(c))
    }

    sealed abstract class BuildResultLowPri {
      /** Default case - Props are required in the component constructor. */
      implicit def buildResultId[P, S]: BuildResult.Aux[P, S, Ctor[P, S]] =
        BuildResult(Ctor(_))
    }

    object BuildResult extends BuildResultLowPri {
      type Aux[P, S, O] = BuildResult[P, S] {type Out = O}

      @inline def apply[P, S, O](f: CompJs3.Constructor[Box[P], Box[S]] => O): Aux[P, S, O] =
        new BuildResult[P, S] {
          override type Out = O
          override val build = f
        }

      /** Special case - When Props = Unit, don't ask for props in the component constructor. */
      implicit def buildResultUnit[S]: BuildResult.Aux[Unit, S, Ctor_NoProps[S]] =
        BuildResult(Ctor_NoProps(_))
    }

    // -------------------------------------------------------------------------------------------------------------------

    def build[P](name: String) = new {
      def initialState[S](s: S) = PS[P, S](name, Box(s))
      def stateless = PS[P, Unit](name, Box.Unit)
    }

    case class PS[P, S](name: String, s: Box[S]) {
      def render(r: Mounted[P, S] => raw.ReactElement)(implicit w: BuildResult[P, S]): Builder[P, S, w.Out] =
        new Builder[P, S, w.Out](name, s, r, w)

      def render_P(r: P => raw.ReactElement)(implicit w: BuildResult[P, S]): Builder[P, S, w.Out] = render($ => r($.props))(w)
      def render_S(r: S => raw.ReactElement)(implicit w: BuildResult[P, S]): Builder[P, S, w.Out] = render($ => r($.state))(w)
    }

    val fieldMounted = "m"

    def mountedFromJs[P, S](rc: raw.ReactComponent): Mounted[P, S] =
      rc.asInstanceOf[js.Dynamic].selectDynamic(fieldMounted).asInstanceOf[Mounted[P, S]]

    case class Builder[P, S, O](name: String, s: Box[S], render: Mounted[P, S] => raw.ReactElement, w: BuildResult.Aux[P, S, O]) {
      def build: O = {

        val spec = js.Dictionary.empty[js.Any]

        for (n <- Option(name))
          spec("displayName") = n

        def withMounted[A](f: Mounted[P, S] => A): js.ThisFunction0[raw.ReactComponent, A] =
          (rc: raw.ReactComponent) =>
            f(mountedFromJs(rc))

//        if (ibf.isDefined)
//          spec(BackendKey) = null

        spec("render") = withMounted(render)

//        @inline def setFnPS[$, A, R](a: ($, P, S) => A)(fn: js.UndefOr[A => CallbackTo[R]], name: String): Unit =
//          fn.foreach { f =>
//            val g = ($: $, p: Box[P], s: Box[S]) =>
//              f(a($, p.v, s.v)).runNow()
//            spec(name) = g: js.ThisFunction
//          }
//
//        @inline def setFnP[$, A, R](a: ($, P) => A)(fn: js.UndefOr[A => CallbackTo[R]], name: String): Unit =
//          fn.foreach { f =>
//            val g = ($: $, p: Box[P], s: Box[S]) =>
//              f(a($, p.v)).runNow()
//            spec(name) = g: js.ThisFunction
//          }
//
//        @inline def setThisFn1[A](fn: js.UndefOr[A => Callback], name: String): Unit =
//          fn.foreach { f =>
//            val g = (a: A) => f(a).runNow()
//            spec(name) = g: js.ThisFunction
//          }
//
//        val renderFn: js.ThisFunction0[js.Any, raw.ReactElement] =
//          (`this`: js.Any) => {
//            // TODO Store an instance of Mounted[P, S] & use everywhere?
//            val m = `this`.asInstanceOf[raw.ReactComponent]
//            val p = m.props.asInstanceOf[Box[P]].a
//            val s = m.state.asInstanceOf[Box[S]].a
//            r(p, s)
//          }

        def getInitialStateFn: js.Function0[Box[S]] = () => s
        spec.update("getInitialState", getInitialStateFn) // TODO I bet this has a perf impact.

        val componentWillMountFn: js.ThisFunction0[raw.ReactComponent, Unit] =
          (rc: raw.ReactComponent) => {
            val m: Mounted[P, S] =
              new Mounted(CompJs3.Mounted[Box[P], Box[S]](rc))
            rc.asInstanceOf[js.Dynamic].updateDynamic(fieldMounted)(m.asInstanceOf[js.Any])
          }
        spec("componentWillMount") = componentWillMountFn


//        def onWillMountFn(f: DuringCallbackU[P, S, B] => Unit): Unit =
//          componentWillMountFn = Some(componentWillMountFn.fold(f)(g => $ => {g($); f($)}))

//        for (initBackend <- ibf)
//          onWillMountFn { $ =>
//            val bs = $.asInstanceOf[BackendScope[P, S]]
//            val backend = initBackend(bs)
//            $.asInstanceOf[Dynamic].updateDynamic(BackendKey)(backend.asInstanceOf[JAny])
//          }

//        for (f <- lc.componentWillMount)
//          onWillMountFn(f(_).runNow())
//        for (f <- componentWillMountFn)
//          spec("componentWillMount") = f: ThisFunction
//
//        val initStateFn: DuringCallbackU[P, S, B] => WrapObj[S] =
//          $ => WrapObj(isf($).runNow())
//        spec("getInitialState") = initStateFn: ThisFunction
//
//        lc.getDefaultProps.flatMap(_.toJsCallback).foreach(spec("getDefaultProps") = _)
//
//        setThisFn1(                                             lc.componentWillUnmount     , "componentWillUnmount")
//        setThisFn1(                                             lc.componentDidMount        , "componentDidMount")
//        setFnPS   (ComponentWillUpdate      .apply[P, S, B, N])(lc.componentWillUpdate      , "componentWillUpdate")
//        setFnPS   (ComponentDidUpdate       .apply[P, S, B, N])(lc.componentDidUpdate       , "componentDidUpdate")
//        setFnPS   (ShouldComponentUpdate    .apply[P, S, B, N])(lc.shouldComponentUpdate    , "shouldComponentUpdate")
//        setFnP    (ComponentWillReceiveProps.apply[P, S, B, N])(lc.componentWillReceiveProps, "componentWillReceiveProps")
//
//        if (jsMixins.nonEmpty)
//          spec("mixins") = JArray(jsMixins: _*)
//
//        val spec2 = spec.asInstanceOf[ReactComponentSpec[P, S, B, N]]
//        lc.configureSpec.foreach(_(spec2).runNow())
//        spec2

        val spec2 = spec.asInstanceOf[raw.ReactComponentSpec]
        val cls = raw.React.createClass(spec2)
        w(cls)
      }
    }

    case class Ctor[P, S](jsInstance: CompJs3.Constructor[Box[P], Box[S]]) {
      def apply(p: P) =
        new Unmounted[P, S](jsInstance(Box(p)))
    }
    case class Ctor_NoProps[S](jsInstance: CompJs3.Constructor[Box[Unit], Box[S]]) {
      private val instance: Unmounted[Unit, S] =
        new Unmounted[Unit, S](jsInstance(Box.Unit))

      def apply() = instance
    }

    class Unmounted[P, S](jsInstance: CompJs3.Unmounted[Box[P], Box[S]]) {
      def key: Option[Key] =
        jsInstance.key

      def ref: Option[String] =
        jsInstance.ref

      def props: P =
        jsInstance.props.a

      def children: raw.ReactNodeList =
        jsInstance.children

      def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): Mounted[P, S] = {
        val rc = raw.ReactDOM.render(jsInstance.rawElement, container, callback.toJsFn)
        mountedFromJs(rc)
      }
    }

    class Mounted[P, S](jsInstance: CompJs3.Mounted[Box[P], Box[S]]) {
      final def isMounted(): Boolean =
        jsInstance.isMounted()

      final def props: P =
        jsInstance.props.a

      final def children: raw.ReactNodeList =
        jsInstance.children

      final def state: S =
        jsInstance.state.a

      final def setState(state: S, callback: Callback = Callback.empty): Unit =
        jsInstance.setState(Box(state), callback)

      final def getDOMNode(): dom.Element =
        jsInstance.getDOMNode()
    }

  }

}
