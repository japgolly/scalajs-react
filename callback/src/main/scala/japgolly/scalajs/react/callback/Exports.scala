package japgolly.scalajs.react.callback

trait Exports {
  import japgolly.scalajs.react.{callback => C}

  final type AsyncCallback[+A] = C.AsyncCallback[A]
  final val  AsyncCallback     = C.AsyncCallback

  final type Callback = C.Callback
  final val  Callback = C.Callback

  final type CallbackTo[+A] = C.CallbackTo[A]
  final val  CallbackTo     = C.CallbackTo

  final type CallbackKleisli[A, B] = C.CallbackKleisli[A, B]
  final val  CallbackKleisli       = C.CallbackKleisli

  final type CallbackOption[+A] = C.CallbackOption[A]
  final val  CallbackOption     = C.CallbackOption
}
