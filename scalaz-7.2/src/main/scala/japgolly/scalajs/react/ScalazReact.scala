package japgolly.scalajs.react

import japgolly.scalajs.react.internal._

trait ScalazReact
  extends ScalazReactExt
     with ScalazReactInstances
     with ScalazReactState

object ScalazReact extends ScalazReact
