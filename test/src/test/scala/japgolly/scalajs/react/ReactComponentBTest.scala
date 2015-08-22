package japgolly.scalajs.react

import utest._

object ReactComponentBTest extends TestSuite {

  import TestUtil.Inference._

  // ======
  // Stages
  // ======
  //
  // 1 = P
  // 2 = PS
  // 3 = PSB
  //     .render() mandatory
  // 4 = PSBR
  // 5 = ReactComponentB
  // 6 = ReactComponentB#Builder

  override def tests = TestSuite {
    'auto12 - test[ReactComponentB.P[P]            ](_ render ???).expect[ReactComponentB.PSBR[P, Unit, Unit]            ]
    'auto2  - test[ReactComponentB.PS[P, S]        ](_ render ???).expect[ReactComponentB.PSBR[P, S, Unit]               ]
    'auto45 - test[ReactComponentB.PSBR[P, S, B]   ](_.build     ).expect[ReactComponentC.ReqProps[P, S, B, TopNode]     ]
    'auto5  - test[ReactComponentB[P, S, B, N]     ](_.build     ).expect[ReactComponentC.ReqProps[P, S, B, N]           ]
    'auto4U - test[ReactComponentB.PSBR[Unit, S, B]](_.buildU    ).expect[ReactComponentC.ConstProps[Unit, S, B, TopNode]]
  }
}
