package japgolly.scalajs.react

import cats.{Monad, ~>}
import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react.ReactMonocle._
import japgolly.scalajs.react.extra._
import monocle._
import scala.annotation.nowarn
import utest._

object MonocleTest extends TestSuite {
  import japgolly.scalajs.react.test.InferenceHelpers._

  implicit val mMonad: Monad[M] with (M ~> CallbackTo) =
    null.asInstanceOf[Monad[M] with (M ~> CallbackTo)]

  case class Poly[A](oa: Option[A])

  object Poly {
    def oa[A] = Lens[Poly[A], Option[A]](_.oa)(oa => _ => Poly(oa))
  }

  @nowarn("cat=unused")
  private class ScopeTest[A](s: StateAccessPure[A]) {
    // Testing:
    // [error] private value s escapes its defining scope as part of type ScopeTest.this.s.WithMappedState[Int]
    // val t = s.zoomStateL(null.asInstanceOf[monocle.Lens[A, Int]])
    // TODO I can't think of how to fix this without crazy typeclasses or f-bounded types
  }

  val tests = Tests {

    "inference" - {
      def lensST: monocle.Lens[S, T] = null
      def lensJST: monocle.Lens[JS, T] = null

      "zoom" - {
      //"RenderScope"       - assertType[Render              ].map(_ zoomStateL lensST ).is_<[StateAccessPure[T]]
        "StateAccessPure"   - assertType[StateAccessPure[S]  ].map(_ zoomStateL lensST ).is_<[StateAccessPure[T]]
        "BackendScope"      - assertType[Backend             ].map(_ zoomStateL lensST ).is_<[StateAccessPure[T]]
        "ScalaMountedCB"    - assertType[ScalaMountedCB      ].map(_ zoomStateL lensST ).is_<[StateAccessPure[T]]
        "StateAccessImpure" - assertType[StateAccessImpure[S]].map(_ zoomStateL lensST ).is_<[StateAccessImpure[T]]
        "JsMounted"         - assertType[JsMounted           ].map(_ zoomStateL lensJST).is_<[StateAccessImpure[T]]
        "ScalaMountedId"    - assertType[ScalaMountedId      ].map(_ zoomStateL lensST ).is_<[StateAccessImpure[T]]
      }

      "poly" - {
        "zoomStateL" - assertType[BackendScope[P, Poly[S]]].map(_ zoomStateL  Poly.oa[S]).is_<[StateAccessPure[Option[S]]]
      }

      "stateSnapshot" - {
        "oneOff"   - assertType[Render].map(StateSnapshot.zoomL(lensST).of(_)).is[StateSnapshot[T]]
        "prepared" - {
          assertType[Render].map { $ =>
            val p = StateSnapshot.withReuse.zoomL(lensST).prepareVia($)
            implicit def rt: Reusability[T] = ???
            p(S)
          }.is[StateSnapshot[T]]
        }
      }

    }
  }
}