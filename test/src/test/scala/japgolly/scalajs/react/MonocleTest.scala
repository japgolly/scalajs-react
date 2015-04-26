package japgolly.scalajs.react

import monocle._
import scalaz.effect.IO
import utest._
import React._
import ScalazReact._
import MonocleReact._

object MonocleTest extends TestSuite {

  val tests = TestSuite {

    'inference {
      import TestUtil.Inference._
      val lensST: Lens[S, T] = null
      val lensTS: Lens[T, S] = null

      "BackendScope ops"    - test[BackendScope[Unit, S]      ](_ focusStateL lensST).expect[ComponentStateFocus[T]]
      "ComponentScopeM ops" - test[ComponentScopeM[U, S, U, N]](_ focusStateL lensST).expect[ComponentStateFocus[T]]
      "ReactComponentM ops" - test[ReactComponentM[U, S, U, N]](_ focusStateL lensST).expect[ComponentStateFocus[T]]
      "ReactS.zoomL"        - test[ReactST[M, S, A]           ](_ zoomL lensTS      ).expect[ReactST[M, T, A]]
      "c._setStateL"        - test[BackendScope[Unit, S]      ](_ _setStateL lensST ).expect[T => IO[Unit]]
    }
  }
}