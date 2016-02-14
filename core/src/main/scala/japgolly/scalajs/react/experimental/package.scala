package japgolly.scalajs.react

import CompState._

package object experimental {

  implicit def BackendScopeMPStateOps[P, S]($: BackendScopeMP[P, S]): ReadCallbackWriteCallbackOps[S] =
    $.stateAccess

}
