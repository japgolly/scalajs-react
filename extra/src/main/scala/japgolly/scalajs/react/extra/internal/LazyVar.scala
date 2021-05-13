package japgolly.scalajs.react.extra.internal

final class LazyVar[A](initArg: () => A) {

  // Don't prevent GC of initArg or waste mem propagating the ref
  private[this] var init = initArg

  private[this] var value: A = _

  def get(): A = {
    if (init ne null)
      set(init())
    value
  }

  def set(a: A): Unit = {
    value = a
    init = null
  }
}