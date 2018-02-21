package japgolly.scalajs.react.component.builder

import scalajs.js
import japgolly.scalajs.react.{Children, raw}
import japgolly.scalajs.react.component.{Js, Scala}
import japgolly.scalajs.react.internal.Box
import Lifecycle._
import Scala._

/** Creates an ES6 class extending `React.Component` to create a component from a builder. */
object ViaReactComponent {

  /*
  The following ES6 JS:

  +---------------------------------------------------------------------------------------------------------------------
  | // ** Start **
  |
  | class MyComponent extends React.Component {
  |
  |     static displayName = 'DisplayName!';
  |
  |     constructor(props) {
  |         console.log("aaaaaaaahhhhhh! 1")
  |         super(props);
  |         this.state = f(props);
  |         console.log("aaaaaaaahhhhhh! 2")
  |     }
  |
  |     clickMe = (e) => {
  |         console.log(this);
  |     }
  |
  |     render() {
  |         return <div onClick = {this.clickMe}></div>
  |     }
  |
  |     componentDidMount() {
  |         this.timerID = setInterval(() => this.tick(), 1000);
  |     }
  | }
  +---------------------------------------------------------------------------------------------------------------------

  after transpilation to ES5 through Babel with presets [es2015, react, stage-2] becomes:

  +---------------------------------------------------------------------------------------------------------------------
  | var _createClass = function() {
  |     function defineProperties(target, props) {
  |         for (var i = 0; i < props.length; i++) {
  |             var descriptor = props[i];
  |             descriptor.enumerable = descriptor.enumerable || false;
  |             descriptor.configurable = true;
  |             if ("value" in descriptor) descriptor.writable = true;
  |             Object.defineProperty(target, descriptor.key, descriptor);
  |         }
  |     }
  |     return function(Constructor, protoProps, staticProps) {
  |         if (protoProps) defineProperties(Constructor.prototype, protoProps);
  |         if (staticProps) defineProperties(Constructor, staticProps);
  |         return Constructor;
  |     };
  | }();
  |
  | function _classCallCheck(instance, Constructor) {
  |     if (!(instance instanceof Constructor)) {
  |         throw new TypeError("Cannot call a class as a function");
  |     }
  | }
  |
  | function _possibleConstructorReturn(self, call) {
  |     if (!self) {
  |         throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
  |     }
  |     return call && (typeof call === "object" || typeof call === "function") ? call : self;
  | }
  |
  | function _inherits(subClass, superClass) {
  |     if (typeof superClass !== "function" && superClass !== null) {
  |         throw new TypeError("Super expression must either be null or a function, not " + typeof superClass);
  |     }
  |     subClass.prototype = Object.create(superClass && superClass.prototype, {
  |         constructor: {
  |             value: subClass,
  |             enumerable: false,
  |             writable: true,
  |             configurable: true
  |         }
  |     });
  |     if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
  | }
  |
  +---------------------------------------------------------------------------------------------------------------------
  | // ** Start **
  |
  | var MyComponent = function(_React$Component) {
  |     _inherits(MyComponent, _React$Component);
  |
  |     function MyComponent(props) {
  |         _classCallCheck(this, MyComponent);
  |
  |         console.log("aaaaaaaahhhhhh! 1");
  |
  |         var _this = _possibleConstructorReturn(this, (MyComponent.__proto__ || Object.getPrototypeOf(MyComponent)).call(this, props));
  |
  |         _this.clickMe = function(e) {
  |             console.log(_this);
  |         };
  |
  |         _this.state = f(props);
  |         console.log("aaaaaaaahhhhhh! 2");
  |         return _this;
  |     }
  |
  |     _createClass(MyComponent, [{
  |         key: "render",
  |         value: function render() {
  |             return React.createElement("div", {
  |                 onClick: this.clickMe
  |             });
  |         }
  |     }, {
  |         key: "componentDidMount",
  |         value: function componentDidMount() {
  |             var _this2 = this;
  |
  |             this.timerID = setInterval(function() {
  |                 return _this2.tick();
  |             }, 1000);
  |         }
  |     }]);
  |
  |     return MyComponent;
  | }(React.Component);
  |
  | MyComponent.displayName = 'DisplayName!';
  +---------------------------------------------------------------------------------------------------------------------

   */

  @js.native
  private trait Method extends js.PropertyDescriptor {
    val key: String
  }

  private def Method(_key: String, _value: js.Any): Method =
    js.Dynamic.literal(key = _key, value = _value).asInstanceOf[Method]

  private def _defineProperties(target: js.Object, props: js.Array[Method]): Unit = {
    for (p <- props) {
      // p.enumerable ||= false
      p.configurable = true
      if (js.Object.hasProperty(p, "value")) p.writable = true
      js.Object.defineProperty(target, p.key, p)
    }
  }

  private def _createClass(c: js.Object, protoProps: js.Array[Method] /*, staticProps: js.Array[Method]*/): Unit = {
    _defineProperties(c.asInstanceOf[js.Dynamic].prototype.asInstanceOf[js.Object], protoProps)
    // _defineProperties(c, staticProps)
  }

  // function _classCallCheck(instance, Constructor) {
  //     if (!(instance instanceof Constructor)) {
  //         throw new TypeError("Cannot call a class as a function");
  //     }
  // }

  private def _possibleConstructorReturn(self: js.Any, call: js.Any): js.Any = {
    // if (!self) {
    //   throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
    // }
    call match {
      case _: js.Object | _: js.Function => call
      case _ => self
    }
  }

  private type GetPrototypeOf = js.Object => js.Object
  private type SetPrototypeOf = js.Function2[js.Object, js.Object, Unit]

  private val (_getPrototypeOf, _setPrototypeOf): (GetPrototypeOf, SetPrototypeOf) = {
    val f = js.Dynamic.global.Object.setPrototypeOf.asInstanceOf[js.UndefOr[SetPrototypeOf]]
    f.toOption match {
      case Some(set) =>
        (js.Object.getPrototypeOf: GetPrototypeOf, set)
      case None =>
        val get: GetPrototypeOf = (_: js.Object).asInstanceOf[js.Dynamic].__proto__.asInstanceOf[js.Object]
        val set: SetPrototypeOf = (_: js.Object).asInstanceOf[js.Dynamic].__proto__ = (_: js.Object)
        (get, set)
    }
  }

  private def _inherits(subClass: js.Object, superClass: js.Object): Unit = {
    // if (typeof superClass !== "function" && superClass !== null) {
    //   throw new TypeError("Super expression must either be null or a function, not " + typeof superClass);
    // }
    subClass.asInstanceOf[js.Dynamic].prototype = js.Object.create(
      // superClass && <-- superClass is always defined as React.Component for us
      superClass.asInstanceOf[js.Dynamic].prototype.asInstanceOf[js.Object],
      js.Dynamic.literal(
        constructor = js.Dynamic.literal(
          value = subClass,
          enumerable = false,
          writable = true,
          configurable = true)
      )
    )
    // if (superClass) <-- superClass is always defined as React.Component for us
    _setPrototypeOf(subClass, superClass)
  }

  private val ReactComponent: js.Object =
    js.constructorOf[raw.React.Component[js.Object, js.Object]].asInstanceOf[js.Object]

  private val isMounted: Method =
    Method("isMounted", (() => js.undefined): js.Function)

  // ===================================================================================================================

  def apply[P, C <: Children, S, B](builder: Builder.Step4[P, C, S, B]): raw.ReactClass[Box[P], Box[S]] = {
    val initStateFn = builder.initStateFn
    val backendFn = builder.backendFn
    val renderFn = builder.renderFn

    type This = RawMounted[P, S, B]
    var MyComponent: js.ThisFunction1[This, Box[P], This] = null

    MyComponent = (`this`: This, props: Box[P]) => {

      val _this: This =
        _possibleConstructorReturn(`this`, _getPrototypeOf(MyComponent).asInstanceOf[js.Dynamic].call(`this`, props))
          .asInstanceOf[This]

      val jMounted: JsMounted[P, S, B] =
        Js.mounted[Box[P], Box[S]](_this).addFacade[Vars[P, S, B]]

      _this.mountedImpure = Scala.mountedRoot(jMounted)
      _this.mountedPure = _this.mountedImpure.withEffect
      _this.backend = backendFn(_this.mountedPure)

      _this.asInstanceOf[js.Dynamic].state = initStateFn(props)

      _this
    }

    _inherits(MyComponent, ReactComponent)

    val methods: js.Array[Method] =
      js.Array(isMounted)

    def add[O](k: String, v: js.Any): Unit = methods.push(Method(k, v))
    def add0[O](k: String, f: This => O): Unit = add(k, f: js.ThisFunction0[This, O])
    def add1[I, O](k: String, f: (This, I) => O): Unit = add(k, f: js.ThisFunction1[This, I, O])
    def add2[I, J, O](k: String, f: (This, I, J) => O): Unit = add(k, f: js.ThisFunction2[This, I, J, O])

    add0("render", _this => renderFn(new RenderScope(_this)).rawElement)

    for (f <- builder.lifecycle.componentWillMount)
      add0("componentWillMount", _this => f(new ComponentWillMount(_this)).runNow())

    for (f <- builder.lifecycle.componentDidMount)
      add0("componentDidMount", _this => f(new ComponentDidMount(_this)).runNow())

    for (f <- builder.lifecycle.componentDidUpdate)
      add2("componentDidUpdate",
        (_this: This, p: Box[P], s: Box[S]) => f(new ComponentDidUpdate(_this, p.unbox, s.unbox)).runNow())

    for (f <- builder.lifecycle.componentWillUpdate)
      add2("componentWillUpdate",
        (_this: This, p: Box[P], s: Box[S]) => f(new ComponentWillUpdate(_this, p.unbox, s.unbox)).runNow())

    for (f <- builder.lifecycle.shouldComponentUpdate)
      add2("shouldComponentUpdate",
        (_this: This, p: Box[P], s: Box[S]) => f(new ShouldComponentUpdate(_this, p.unbox, s.unbox)).runNow())

    for (f <- builder.lifecycle.componentWillReceiveProps)
      add1("componentWillReceiveProps",
        (_this: This, p: Box[P]) => f(new ComponentWillReceiveProps(_this, p.unbox)).runNow())

    // I don't know that this teardown is necessary. It should be fine without...
    // Let's try it. If things go wrong this is the code we'll need ↓
    //
    //val teardown: This => Unit =
    //  _this => {
    //    _this.mountedImpure = null
    //    _this.mountedPure = null
    //    _this.backend = null.asInstanceOf[B]
    //  }
    // add0("componentWillUnmount",
    //   builder.lifecycle.componentWillUnmount match {
    //     case None => teardown
    //     case Some(f) => _this => {
    //       f(new ComponentWillUnmount(_this)).runNow()
    //       teardown(_this)
    //     }
    //   })
    for (f <- builder.lifecycle.componentWillUnmount)
      add0("componentWillUnmount", _this => f(new ComponentWillUnmount(_this)).runNow())

    _createClass(MyComponent, methods)

    for (n <- Option(builder.name))
      MyComponent.asInstanceOf[js.Dynamic].displayName = n

    MyComponent.asInstanceOf[raw.ReactClass[Box[P], Box[S]]]
  }
}
