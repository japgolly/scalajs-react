
object Polyfills {

  // https://github.com/facebook/react/issues/945#issuecomment-35624393
  def functionBind1 =
    """
      |Function.prototype.bind = function(scope){
      |    var self = this;
      |    return function(){
      |        return self.apply(scope, arguments);
      |    };
      |};
    """.stripMargin

  // https://github.com/facebook/react/issues/945#issuecomment-37329894
  def functionBind2 =
    """
      |//if (!Function.prototype.bind) {
      |  var Empty = function(){};
      |  Function.prototype.bind = function bind(that) { // .length is 1
      |    var target = this;
      |    if (typeof target != "function") {
      |      throw new TypeError("Function.prototype.bind called on incompatible " + target);
      |    }
      |    var args = Array.prototype.slice.call(arguments, 1); // for normal call
      |    var binder = function () {
      |      if (this instanceof bound) {
      |        var result = target.apply(
      |            this,
      |            args.concat(Array.prototype.slice.call(arguments))
      |        );
      |        if (Object(result) === result) {
      |            return result;
      |        }
      |        return this;
      |      } else {
      |        return target.apply(
      |            that,
      |            args.concat(Array.prototype.slice.call(arguments))
      |        );
      |      }
      |    };
      |    var boundLength = Math.max(0, target.length - args.length);
      |    var boundArgs = [];
      |    for (var i = 0; i < boundLength; i++) {
      |      boundArgs.push("$" + i);
      |    }
      |    var bound = Function("binder", "return function(" + boundArgs.join(",") + "){return binder.apply(this,arguments)}")(binder);
      |
      |    if (target.prototype) {
      |      Empty.prototype = target.prototype;
      |      bound.prototype = new Empty();
      |      // Clean up dangling references.
      |      Empty.prototype = null;
      |    }
      |    return bound;
      |  };
      |//}
    """.stripMargin

  // https://github.com/facebook/react/blob/master/src/test/phantomjs-shims.js
  def functionBind3 =
    """
      |(function() {
      |var Ap = Array.prototype;
      |var slice = Ap.slice;
      |var Fp = Function.prototype;
      |
      |//if (!Fp.bind) {
      |  // PhantomJS doesn't support Function.prototype.bind natively, so
      |  // polyfill it whenever this module is required.
      |  Fp.bind = function(context) {
      |    var func = this;
      |    var args = slice.call(arguments, 1);
      |
      |    function bound() {
      |      var invokedAsConstructor = func.prototype && (this instanceof func);
      |      return func.apply(
      |        // Ignore the context parameter when invoking the bound function
      |        // as a constructor. Note that this includes not only constructor
      |        // invocations using the new keyword but also calls to base class
      |        // constructors such as BaseClass.call(this, ...) or super(...).
      |        !invokedAsConstructor && context || this,
      |        args.concat(slice.call(arguments))
      |      );
      |    }
      |
      |    // The bound function must share the .prototype of the unbound
      |    // function so that any object created by one constructor will count
      |    // as an instance of both constructors.
      |    bound.prototype = func.prototype;
      |
      |    return bound;
      |  };
      |//}
      |})();
    """.stripMargin

}