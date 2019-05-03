package japgolly.scalajs.react.internal

import japgolly.scalajs.react.Callback
import scala.util.Try
import japgolly.scalajs.react.CallbackTo

/** Promise that is synchronous once its value has been set. */
final class SyncPromise[A] {
  private var result    = Option.empty[Try[A]]
  private var callbacks = List.empty[Try[A] => Callback]

  val complete: Try[A] => Callback =
    t =>
      Callback {
        if (result.isEmpty) {
          result = Some(t)
          try
            callbacks.foreach(_(t).runNow())
          finally
            callbacks = Nil
        }
      }

  def onComplete: (Try[A] => Callback) => Callback =
    f =>
      Callback {
        result match {
          case Some(t) => f(t).runNow()
          case None    => callbacks :+= f
        }
      }
}

object SyncPromise {
  def apply[A]: CallbackTo[SyncPromise[A]] =
    CallbackTo(new SyncPromise)
}