package japgolly.scalajs.react

import scala.annotation.elidable
import org.scalajs.dom

package object extra {

  @elidable(elidable.ASSERTION)
  def assertWarn(test: => Boolean, msg: => String): Unit =
    if (!test)
      dom.console.warn(msg)

}