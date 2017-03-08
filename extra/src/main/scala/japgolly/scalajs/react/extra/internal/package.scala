package japgolly.scalajs.react.extra

import org.scalajs.dom
import scala.annotation.elidable

package object internal {

  @elidable(elidable.ASSERTION)
  def assertWarn(test: => Boolean, msg: => String): Unit =
    if (!test)
      dom.console.warn(msg)

}
