package japgolly.scalajs.react.core

import cats.effect.SyncIO
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import scala.scalajs.js.annotation._

object LinkingTest {

  @JSExportTopLevel("LinkingTest")
  def _test(): Long = {
    var h = 0L

    def test(a: Any): Unit =
      h += a.##

    test(util.Effect.callback)
    test(util.Effect.io)

    test {
      val x = Px.constByNeed(123).toCallback
      test(x: CallbackTo[Int])

      val y = Px.constByNeed(123).toCallback[SyncIO]
      test(y: SyncIO[Int])
    }

    test {
      Px.ManualCollection().refreshCB
    }

    h
  }
}
