package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object UseEffect {

  val Component = ScalaFnComponent.withHooks[Int]

    .useEffect(Callback.empty)
    .useEffectOnMount(Callback.empty)
    .useLayoutEffect(Callback.empty)
    .useLayoutEffectOnMount(Callback.empty)

    .useEffectBy(Callback.log(_))
    .useEffectOnMountBy(Callback.log(_))
    .useLayoutEffectBy(Callback.log(_))
    .useLayoutEffectOnMountBy(Callback.log(_))

    .unchecked(0)

    .useEffectBy($ => Callback.log($.props))
    .useEffectOnMountBy($ => Callback.log($.props))
    .useLayoutEffectBy($ => Callback.log($.props))
    .useLayoutEffectOnMountBy($ => Callback.log($.props))

    .renderRR { (p, s) =>
      p + s
    }
}
