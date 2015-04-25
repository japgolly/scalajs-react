package japgolly.scalajs.react

import scala.annotation.elidable
import org.scalajs.dom

package object extra {

  @elidable(elidable.ASSERTION)
  def assertWarn(test: => Boolean, msg: => String): Unit =
    if (!test)
      dom.console.warn(msg)

  type ~=>[A, B] = ReusableFn[A, B]

  @inline implicit class ReactExtrasAnyExt[A](val self: A) extends AnyVal {
    def ~=~(a: A)(implicit r: Reusable[A]): Boolean = r.test(self, a)
    def ~/~(a: A)(implicit r: Reusable[A]): Boolean = !r.test(self, a)
  }

}