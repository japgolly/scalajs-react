package japgolly.scalajs.react.util

import scala.scalajs.js

final class ConsoleHijack(val config: ConsoleHijack.Config) {
  import ConsoleHijack._
  import ConsoleHijack.Internals._

  def ++(b: ConsoleHijack): ConsoleHijack =
    new ConsoleHijack(config ++ b.config)

  /** Install these hijacks into `console`, execute the given procedure,
    * then restore `console` to the state that it was before this is called.
    */
  def apply[A](a: => A): A = {
    val undo = new js.Array[() => Unit]
    try {
      _install(undo)
      a
    } finally
      undo.foreach(_())
  }

  /** Install these hijacks into `console`.
    *
    * @return A procedure to restore `console` to the state that it was before this is called.
    */
  def install(): () => Unit = {
    val undo = new js.Array[() => Unit]
    _install(undo)
    () => undo.foreach(_())
  }

  private def _install(undo: js.Array[() => Unit]): Unit = {
    val console = js.Dynamic.global.console

    def setHandler(m: Method, h: JsVarargsFn): () => Unit = () => {
      console.updateDynamic(m.name)(h)
    }

    config.foreach { case (method, handler) =>
      val orig = console.selectDynamic(method.name).asInstanceOf[JsVarargsFn]
      undo.push(setHandler(method, orig))
      setHandler(method, mkHandler(handler, orig))()
    }
  }
}

object ConsoleHijack {

  lazy val fatalReactWarnings: ConsoleHijack =
    Error.handleWith { i =>
      if (i.msg.startsWith("Warning: "))
        i.throwException()
      else
        i.fallthrough()
    }

  def apply(cfg: (Method, Handler)*): ConsoleHijack =
    apply(cfg.toMap)

  def apply(cfg: Config): ConsoleHijack =
    new ConsoleHijack(cfg)

  type Config = Map[Method, Handler]

  sealed abstract class Method(final val name: String) {
    final def handleWith(h: Handler): ConsoleHijack =
      ConsoleHijack(this -> h)

    final def throwException =
      handleWith(Handler.throwException)
  }

  case object Debug extends Method("debug")
  case object Error extends Method("error")
  case object Info  extends Method("info")
  case object Log   extends Method("log")
  case object Warn  extends Method("warn")
  case object Trace extends Method("trace")

  final case class Intercept(args: Seq[Any], fallthrough: () => Unit) {
    override def toString = msg

    lazy val msg: String =
      if (args.isEmpty)
        ""
      else {
        @inline def default = args.mkString
        args.head match {
          case fmt: String =>
            try
              Internals.vsprintf(fmt, js.Array(args.tail: _*))
            catch {
              case _: Throwable => default
            }
          case _ => default
        }
      }

    def throwException(): Nothing =
      throw new js.JavaScriptException(msg)
  }

  type Handler = Intercept => Unit

  object Handler {
    @inline def apply(h: Handler): Handler = h

    def throwException: Handler =
      apply(_.throwException())
  }

  private object Internals {

    type VsprintfFn = js.Function2[String, js.Array[Any], String]

    lazy val vsprintf: VsprintfFn = {
      // https://github.com/alexei/sprintf.js/blob/master/dist/sprintf.min.js
      val code = """
        !function(){"use strict";var g={not_string:/[^s]/,not_bool:/[^t]/,not_type:/[^T]/,not_primitive:/[^v]/,number:/[diefg]/,numeric_arg:/[bcdiefguxX]/,json:/[j]/,not_json:/[^j]/,text:/^[^\x25]+/,modulo:/^\x25{2}/,placeholder:/^\x25(?:([1-9]\d*)\$|\(([^)]+)\))?(\+)?(0|'[^$])?(-)?(\d+)?(?:\.(\d+))?([b-gijostTuvxX])/,key:/^([a-z_][a-z_\d]*)/i,key_access:/^\.([a-z_][a-z_\d]*)/i,index_access:/^\[(\d+)\]/,sign:/^[+-]/};function y(e){return function(e,t){var r,n,i,s,a,o,p,c,l,u=1,f=e.length,d="";for(n=0;n<f;n++)if("string"==typeof e[n])d+=e[n];else if("object"==typeof e[n]){if((s=e[n]).keys)for(r=t[u],i=0;i<s.keys.length;i++){if(null==r)throw new Error(y('[sprintf] Cannot access property "%s" of undefined value "%s"',s.keys[i],s.keys[i-1]));r=r[s.keys[i]]}else r=s.param_no?t[s.param_no]:t[u++];if(g.not_type.test(s.type)&&g.not_primitive.test(s.type)&&r instanceof Function&&(r=r()),g.numeric_arg.test(s.type)&&"number"!=typeof r&&isNaN(r))throw new TypeError(y("[sprintf] expecting number but found %T",r));switch(g.number.test(s.type)&&(c=0<=r),s.type){case"b":r=parseInt(r,10).toString(2);break;case"c":r=String.fromCharCode(parseInt(r,10));break;case"d":case"i":r=parseInt(r,10);break;case"j":r=JSON.stringify(r,null,s.width?parseInt(s.width):0);break;case"e":r=s.precision?parseFloat(r).toExponential(s.precision):parseFloat(r).toExponential();break;case"f":r=s.precision?parseFloat(r).toFixed(s.precision):parseFloat(r);break;case"g":r=s.precision?String(Number(r.toPrecision(s.precision))):parseFloat(r);break;case"o":r=(parseInt(r,10)>>>0).toString(8);break;case"s":r=String(r),r=s.precision?r.substring(0,s.precision):r;break;case"t":r=String(!!r),r=s.precision?r.substring(0,s.precision):r;break;case"T":r=Object.prototype.toString.call(r).slice(8,-1).toLowerCase(),r=s.precision?r.substring(0,s.precision):r;break;case"u":r=parseInt(r,10)>>>0;break;case"v":r=r.valueOf(),r=s.precision?r.substring(0,s.precision):r;break;case"x":r=(parseInt(r,10)>>>0).toString(16);break;case"X":r=(parseInt(r,10)>>>0).toString(16).toUpperCase()}g.json.test(s.type)?d+=r:(!g.number.test(s.type)||c&&!s.sign?l="":(l=c?"+":"-",r=r.toString().replace(g.sign,"")),o=s.pad_char?"0"===s.pad_char?"0":s.pad_char.charAt(1):" ",p=s.width-(l+r).length,a=s.width&&0<p?o.repeat(p):"",d+=s.align?l+r+a:"0"===o?l+a+r:a+l+r)}return d}(function(e){if(p[e])return p[e];var t,r=e,n=[],i=0;for(;r;){if(null!==(t=g.text.exec(r)))n.push(t[0]);else if(null!==(t=g.modulo.exec(r)))n.push("%");else{if(null===(t=g.placeholder.exec(r)))throw new SyntaxError("[sprintf] unexpected placeholder");if(t[2]){i|=1;var s=[],a=t[2],o=[];if(null===(o=g.key.exec(a)))throw new SyntaxError("[sprintf] failed to parse named argument key");for(s.push(o[1]);""!==(a=a.substring(o[0].length));)if(null!==(o=g.key_access.exec(a)))s.push(o[1]);else{if(null===(o=g.index_access.exec(a)))throw new SyntaxError("[sprintf] failed to parse named argument key");s.push(o[1])}t[2]=s}else i|=2;if(3===i)throw new Error("[sprintf] mixing positional and named placeholders is not (yet) supported");n.push({placeholder:t[0],param_no:t[1],keys:t[2],sign:t[3],pad_char:t[4],align:t[5],width:t[6],precision:t[7],type:t[8]})}r=r.substring(t[0].length)}return p[e]=n}(e),arguments)}function e(e,t){return y.apply(null,[e].concat(t||[]))}var p=Object.create(null);"undefined"!=typeof exports&&(exports.sprintf=y,exports.vsprintf=e),"undefined"!=typeof window&&(window.sprintf=y,window.vsprintf=e,"function"==typeof define&&define.amd&&define(function(){return{sprintf:y,vsprintf:e}}))}();
        window.vsprintf
      """.replace("printf", "printf__scalajsreact")
      js.eval(code).asInstanceOf[VsprintfFn]
    }

    trait JsVarargsFn extends js.Function {
      def apply(args: Any*): Unit
    }

    def mkHandler(f: Handler, orig: JsVarargsFn): JsVarargsFn = { args =>
      val i = Intercept(args, () => orig(args: _*))
      f(i)
    }
  }
}
