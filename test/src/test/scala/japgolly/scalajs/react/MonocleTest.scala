package japgolly.scalajs.react

import monocle._
import monocle.macros.Lenses
import utest._
import extra._
import ScalazReact._
import MonocleReact._

import scalaz.{Monad, ~>}

object MonocleTest extends TestSuite {
  import japgolly.scalajs.react.test.InferenceUtil._

  implicit val mMonad = null.asInstanceOf[Monad[M] with (M ~> CallbackTo)]

  @Lenses case class Poly[A](oa: Option[A])

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
      //"RenderScope"       - test[Render              ](_ zoomStateL lensST ).expect_<[StateAccessPure[T]]
        "StateAccessPure"   - test[StateAccessPure[S]  ](_ zoomStateL lensST ).expect_<[StateAccessPure[T]]
        "BackendScope"      - test[Backend             ](_ zoomStateL lensST ).expect_<[StateAccessPure[T]]
        "ScalaMountedCB"    - test[ScalaMountedCB      ](_ zoomStateL lensST ).expect_<[StateAccessPure[T]]
        "StateAccessImpure" - test[StateAccessImpure[S]](_ zoomStateL lensST ).expect_<[StateAccessImpure[T]]
        "JsMounted"         - test[JsMounted           ](_ zoomStateL lensJST).expect_<[StateAccessImpure[T]]
        "ScalaMountedId"    - test[ScalaMountedId      ](_ zoomStateL lensST ).expect_<[StateAccessImpure[T]]
        "ReactS"            - test[ReactST[M, S, A]    ](_ zoomL      lensTS ).expect  [ReactST[M, T, A]]
      }

      "setStateFnL" - {
        "RenderScope"       - test[Render              ](_ setStateFnL lensST ).expect[T => Callback]
        "StateAccessPure"   - test[StateAccessPure[S]  ](_ setStateFnL lensST ).expect[T => Callback]
        "BackendScope"      - test[Backend             ](_ setStateFnL lensST ).expect[T => Callback]
        "ScalaMountedCB"    - test[ScalaMountedCB      ](_ setStateFnL lensST ).expect[T => Callback]
        "StateAccessImpure" - test[StateAccessImpure[S]](_ setStateFnL lensST ).expect[T => Unit]
        "JsMounted"         - test[JsMounted           ](_ setStateFnL lensJST).expect[T => Unit]
        "ScalaMountedId"    - test[ScalaMountedId      ](_ setStateFnL lensST ).expect[T => Unit]
      }

      "poly" - {
        "zoomStateL"  - test[BackendScope[P, Poly[S]]](_ zoomStateL  Poly.oa[S]).expect_<[StateAccessPure[Option[S]]]
        "setStateFnL" - test[BackendScope[P, Poly[S]]](_ setStateFnL Poly.oa[S]).expect[Option[S] => Callback]
      }

      "stateSnapshot" - {
        "oneOff"   - test[Render](StateSnapshot.zoomL(lensST).of(_)).expect[StateSnapshot[T]]
        "prepared" - {
          test[Render] { $ =>
            val p = StateSnapshot.withReuse.zoomL(lensST).prepareVia($)
            implicit def rt: Reusability[T] = ???
            p(S)
          }.expect[StateSnapshot[T]]
        }
      }

    }
  }
}