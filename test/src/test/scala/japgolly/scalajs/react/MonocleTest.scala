package japgolly.scalajs.react

import monocle._
import monocle.macros.Lenses
import utest._
import ScalazReact._
import MonocleReact._

object MonocleTest extends TestSuite {
  import japgolly.scalajs.react.test.InferenceUtil._

  @Lenses case class Poly[A](oa: Option[A])

  val tests = TestSuite {

    'inference {
      def lensST: Lens[S, T] = null
      def lensTS: Lens[T, S] = null
      def lensJST: Lens[JS, T] = null

      'zoom {
      //'RenderScope       - test[Render              ](_ zoomStateL lensST ).expect_<[StateAccessPure[T]]
        'StateAccessPure   - test[StateAccessPure[S]  ](_ zoomStateL lensST ).expect_<[StateAccessPure[T]]
        'BackendScope      - test[Backend             ](_ zoomStateL lensST ).expect_<[StateAccessPure[T]]
        'ScalaMountedCB    - test[ScalaMountedCB      ](_ zoomStateL lensST ).expect_<[StateAccessPure[T]]
        'StateAccessImpure - test[StateAccessImpure[S]](_ zoomStateL lensST ).expect_<[StateAccessImpure[T]]
        'JsMounted         - test[JsMounted           ](_ zoomStateL lensJST).expect_<[StateAccessImpure[T]]
        'ScalaMountedId    - test[ScalaMountedId      ](_ zoomStateL lensST ).expect_<[StateAccessImpure[T]]
        'ReactS            - test[ReactST[M, S, A]    ](_ zoomL      lensTS ).expect  [ReactST[M, T, A]]
      }

      'setStateFnL {
        'RenderScope       - test[Render              ](_ setStateFnL lensST ).expect[T => Callback]
        'StateAccessPure   - test[StateAccessPure[S]  ](_ setStateFnL lensST ).expect[T => Callback]
        'BackendScope      - test[Backend             ](_ setStateFnL lensST ).expect[T => Callback]
        'ScalaMountedCB    - test[ScalaMountedCB      ](_ setStateFnL lensST ).expect[T => Callback]
        'StateAccessImpure - test[StateAccessImpure[S]](_ setStateFnL lensST ).expect[T => Unit]
        'JsMounted         - test[JsMounted           ](_ setStateFnL lensJST).expect[T => Unit]
        'ScalaMountedId    - test[ScalaMountedId      ](_ setStateFnL lensST ).expect[T => Unit]
      }

      'poly {
        'zoomStateL  - test[BackendScope[P, Poly[S]]](_ zoomStateL  Poly.oa[S]).expect_<[StateAccessPure[Option[S]]]
        'setStateFnL - test[BackendScope[P, Poly[S]]](_ setStateFnL Poly.oa[S]).expect[Option[S] => Callback]
      }

    }
  }
}