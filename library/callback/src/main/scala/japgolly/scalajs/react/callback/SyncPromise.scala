package japgolly.scalajs.react.callback

import scala.util.Try

/** Promise that is synchronous once its value has been set. */
final class SyncPromise[A] {
  private var result    = Option.empty[Try[A]]
  private var callbacks = List.empty[Try[A] => Callback]

  val complete: Try[A] => Callback =
    t =>
      Callback {
        if (result.isEmpty) {
          result = Some(t)
          var firstErr: Throwable = null
          try {
            for (cb <- callbacks)
              try cb(t).runNow()
              catch {
                case e: Throwable =>
                  if (firstErr eq null)
                    firstErr = e
                  else
                    firstErr.addSuppressed(e)
              }
          } finally
            callbacks = Nil
          if (firstErr ne null)
            throw firstErr
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
