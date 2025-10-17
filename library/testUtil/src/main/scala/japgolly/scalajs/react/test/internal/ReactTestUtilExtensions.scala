package japgolly.scalajs.react.test.internal

import japgolly.scalajs.react.component.Generic
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.util.Effect.{Id, UnsafeSync}
import org.scalajs.dom

object ReactTestUtilExtensions {

  implicit final class ReactTestExt_MountedSimple[F[_], A[_], P, S](private val m: Generic.MountedSimple[F, A, P, S]) extends AnyVal {
    def outerHtmlScrubbed()(implicit F: UnsafeSync[F]): String =
      F.runSync(m.getDOMNode).asMounted().node match {
        case e: dom.Element => ReactTestUtils.removeReactInternals(e.outerHTML)
        case n              => n.nodeValue
      }

    def showDom()(implicit F: UnsafeSync[F]): String =
      F.runSync(m.getDOMNode).show(ReactTestUtils.removeReactInternals)
  }
}

import ReactTestUtilExtensions._

// =====================================================================================================================

trait ReactTestUtilExtensions1 {

  final implicit def reactTestExtMountedSimple[F[_], A[_], P, S](m: Generic.MountedSimple[F, A, P, S]): ReactTestExt_MountedSimple[F, A, P, S] =
    new ReactTestExt_MountedSimple(m)
}

trait ReactTestUtilExtensions extends ReactTestUtilExtensions1 {

  final implicit def reactTestExtMountedImpure[A[_], P, S](m: Generic.MountedSimple[Id, A, P, S]): ReactTestExt_MountedSimple[Id, A, P, S] =
    new ReactTestExt_MountedSimple(m)
}
