package downstream

import japgolly.scalajs.react.util.DefaultEffects._

object ScalafixTest {
  def x: Sync[Int] = Sync.delay(1)
}
