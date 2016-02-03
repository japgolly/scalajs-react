package japgolly.scalajs.react.experimental

import japgolly.scalajs.react._
import CompScope._

/**
  * Backend scope with Mapped Properties.
  *
  * So, the same as a normal [[BackendScope]] except a function is applied tp the properties before they are provided.
  */
trait BackendScopeMP[Props, State] {
  def props: CallbackTo[Props]

  private[experimental] def stateAccess: CanSetState[State] with ReadCallback with WriteCallback
}

object BackendScopeMP {
  def apply[P, Q, State](underlying: BackendScope[P, State])(map: P => Q): BackendScopeMP[Q, State] =
    new BackendScopeMP[Q, State] {
      override def props = underlying.props map map
      override def stateAccess = underlying
    }
}
