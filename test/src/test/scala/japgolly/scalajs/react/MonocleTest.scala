package japgolly.scalajs.react

import monocle._
import utest._
import React._
import ScalazReact._
import MonocleReact._
import ComponentScope._
import CompState._

object MonocleTest extends TestSuite {

  val tests = TestSuite {

    'inference {
      import TestUtil.Inference._
      val lensST: Lens[S, T] = null
      val lensTS: Lens[T, S] = null

      'zoomL {
        'DuringCallbackU - test[DuringCallbackU[P, S, U]   ](_ zoomL lensST).expect[ReadDirectWriteCallbackOps[T]]
        'DuringCallbackM - test[DuringCallbackM[P, S, U, N]](_ zoomL lensST).expect[ReadDirectWriteCallbackOps[T]]
        'BackendScope    - test[BackendScope   [P, S]      ](_ zoomL lensST).expect[ReadCallbackWriteCallbackOps[T]]
        'ReactComponentM - test[ReactComponentM[P, S, U, N]](_ zoomL lensST).expect[ReadDirectWriteDirectOps[T]]
        'ReactS          - test[ReactST[M, S, A]           ](_ zoomL lensTS).expect[ReactST[M, T, A]]
      }

      '_setStateL {
        'DuringCallbackU - test[DuringCallbackU[P, S, U]   ](_ _setStateL lensST).expect[T => Callback]
        'DuringCallbackM - test[DuringCallbackM[P, S, U, N]](_ _setStateL lensST).expect[T => Callback]
        'BackendScope    - test[BackendScope   [P, S]      ](_ _setStateL lensST).expect[T => Callback]
        'ReactComponentM - test[ReactComponentM[P, S, U, N]](_ _setStateL lensST).expect[T => Unit]
      }

    }
  }
}