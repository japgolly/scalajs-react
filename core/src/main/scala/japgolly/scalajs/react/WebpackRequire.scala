package japgolly.scalajs.react

import scalajs.js
import scalajs.js.annotation.JSImport

/**
  * Scala.JS currently doesn't support annotating facades for both script- and module-style output.
  * The default in scalajs-react remains script-style;
  * users who use module-style and webpack/scalajs-bundler should simply reference the relevant objects below so that
  * webpack 1) imports them, and 2) exposes them correctly.
  */
object WebpackRequire {

  @JSImport("expose-loader?React!react", JSImport.Namespace)
  @js.native
  object React extends js.Any

  @JSImport("expose-loader?ReactDOM!react-dom", JSImport.Namespace)
  @js.native
  object ReactDOM extends js.Any

  @JSImport("expose-loader?ReactDOMServer!react-dom/server", JSImport.Namespace)
  @js.native
  object ReactDOMServer extends js.Any

  object Addons {

    @JSImport("expose-loader?React.addons.CSSTransitionGroup!react-addons-css-transition-group", JSImport.Namespace)
    @js.native
    object CSSTransitionGroup extends js.Any

    @JSImport("expose-loader?React.addons.Perf!react-addons-perf", JSImport.Namespace)
    @js.native
    object Perf extends js.Any

  }

}
