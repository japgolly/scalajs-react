package japgolly.scalajs.react

import japgolly.scalajs.react.MonocleReact._
import japgolly.scalajs.react.ScalazReact._
import japgolly.scalajs.react.extra._
import monocle._
import scala.annotation.nowarn
import scalaz.{Monad, ~>}
import utest._

object MonocleTest extends TestSuite {
  import japgolly.scalajs.react.test.InferenceUtil._

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
      def lensTS: monocle.Lens[T, S] = null
      def lensJST: monocle.Lens[JS, T] = null

      "zoom" - {
      //"RenderScope"       - assertType[Render              ](_ zoomStateL lensST ).is_<[StateAccessPure[T]]
        "StateAccessPure"   - assertType[StateAccessPure[S]  ](_ zoomStateL lensST ).is_<[StateAccessPure[T]]
        "BackendScope"      - assertType[Backend             ](_ zoomStateL lensST ).is_<[StateAccessPure[T]]
        "ScalaMountedCB"    - assertType[ScalaMountedCB      ](_ zoomStateL lensST ).is_<[StateAccessPure[T]]
        "StateAccessImpure" - assertType[StateAccessImpure[S]](_ zoomStateL lensST ).is_<[StateAccessImpure[T]]
        "JsMounted"         - assertType[JsMounted           ](_ zoomStateL lensJST).is_<[StateAccessImpure[T]]
        "ScalaMountedId"    - assertType[ScalaMountedId      ](_ zoomStateL lensST ).is_<[StateAccessImpure[T]]
        "ReactS"            - assertType[ReactST[M, S, A]    ](_ zoomL      lensTS ).is  [ReactST[M, T, A]]
      }

      "poly" - {
        "zoomStateL" - assertType[BackendScope[P, Poly[S]]](_ zoomStateL  Poly.oa[S]).is_<[StateAccessPure[Option[S]]]
      }

      "stateSnapshot" - {
        "oneOff"   - assertType[Render](StateSnapshot.zoomL(lensST).of(_)).is[StateSnapshot[T]]
        "prepared" - {
          assertType[Render] { $ =>
            val p = StateSnapshot.withReuse.zoomL(lensST).prepareVia($)
            implicit def rt: Reusability[T] = ???
            p(S)
          }.is[StateSnapshot[T]]
        }
      }

    }
  }
}