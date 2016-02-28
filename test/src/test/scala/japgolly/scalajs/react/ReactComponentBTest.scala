package japgolly.scalajs.react

import utest._
import vdom.prefix_<^._
import TestUtil._

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
    'defaults {
      'auto12 - test[ReactComponentB.P[P]            ](_ render ???).expect[ReactComponentB.PSBR[P, Unit, Unit]            ]
      'auto2  - test[ReactComponentB.PS[P, S]        ](_ render ???).expect[ReactComponentB.PSBR[P, S, Unit]               ]
      'auto45 - test[ReactComponentB.PSBR[P, S, B]   ](_.build     ).expect[ReactComponentC.ReqProps[P, S, B, TopNode]     ]
      'auto5  - test[ReactComponentB[P, S, B, N]     ](_.build     ).expect[ReactComponentC.ReqProps[P, S, B, N]           ]
      'auto4U - test[ReactComponentB.PSBR[Unit, S, B]](_.build    ).expect[ReactComponentC.ConstProps[Unit, S, B, TopNode]]
    }

    'renderBackend {
      import BackendMacroTestData._
      'val                  - assertRender(Val.C(), "<div>hehe1</div>")
      'noArgs               - assertRender(NoArgs.C(), "<div>hehe2</div>")
      'aliasesToSameType    - assertRender(AliasesToSameType.C(7), "<div>4</div>")
      'typeAliasP           - assertRender(TypeAliasP.C(9), "<div>9</div>")
      'typeAliasS           - assertRender(TypeAliasS.C(), "<div>9</div>")
      'typeAliasesInMethod  - assertRender(TypeAliasesInMethod.C(Props(7)), "<div>4</div>")
      'typeAliasesInBackend - assertRender(TypeAliasesInBackend.C(Props(7)), "<div>4</div>")
      'subtypes             - assertRender(Subtypes.C(Vector(1,8)), "<div>9</div>")
      'paramNamesFull       - assertRender(ParamNamesFull.C(7), "<div>4</div>")
      'paramNamesShort      - assertRender(ParamNamesShort.C(7), "<div>4</div>")
      'usePropsChildren     - assertRender(UsePropsChildren.C(<.br), "<div><br/></div>")
      'ambiguousType        - assertContains(compileError("AmbiguousType.x.renderBackend").msg, "what: Int")
    }
  }
}

object BackendMacroTestData {
  case class Props(a: Int)
  case class State(a: Int)

  type PropsInt = Int
  type StateInt = Int

  type PropsAlias = Props
  type StateAlias = State

  object Val {
    class Backend($: BackendScope[Unit, State]) {
      val render = <.div("hehe1")
    }
    val C = ReactComponentB[Unit]("").initialState(State(3)).backend(new Backend(_)).renderBackend.build
  }

  object NoArgs {
    class Backend($: BackendScope[Unit, Unit]) {
      def render = <.div("hehe2")
    }
    val C = ReactComponentB[Unit]("").backend(new Backend(_)).renderBackend.build
  }

  object AliasesToSameType {
    class Backend($: BackendScope[PropsInt, StateInt]) {
      def render(zxc: PropsInt, qwe: StateInt) = <.div(zxc - qwe)
    }
    val C = ReactComponentB[PropsInt]("").initialState[StateInt](3).renderBackend[Backend].build
  }

  object TypeAliasP {
    class Backend($: BackendScope[PropsInt, Unit]) {
      def render(zxc: Int) = <.div(zxc)
    }
    val C = ReactComponentB[PropsInt]("").renderBackend[Backend].build
  }

  object TypeAliasS {
    class Backend($: BackendScope[Unit, StateInt]) {
      def render(zxc: Int) = <.div(zxc)
    }
    val C = ReactComponentB[Unit]("").initialState[StateInt](9).renderBackend[Backend].build
  }

  object TypeAliasesInMethod {
    class Backend($: BackendScope[PropsAlias, StateAlias]) {
      def render(zxc: Props, qwe: State) = <.div(zxc.a - qwe.a)
    }
    val C = ReactComponentB[PropsAlias]("").initialState[StateAlias](State(3)).renderBackend[Backend].build
  }

  object TypeAliasesInBackend {
    class Backend($: BackendScope[Props, State]) {
      def render(qwe: StateAlias, zxc: PropsAlias) = <.div(zxc.a - qwe.a)
    }
    val C = ReactComponentB[Props]("").initialState[State](State(3)).renderBackend[Backend].build
  }

  object Subtypes {
    class Backend($: BackendScope[Vector[Int], Unit]) {
      def render(zxc: Traversable[Int]) = <.div(zxc.sum)
    }
    val C = ReactComponentB[Vector[Int]]("").renderBackend[Backend].build
  }

  object ParamNamesFull {
    class Backend($: BackendScope[Int, Int]) {
      def render(state: Int, props: Int) = <.div(props - state)
    }
    val C = ReactComponentB[Int]("").initialState(3).renderBackend[Backend].build
  }

  object ParamNamesShort {
    class Backend($: BackendScope[Int, Int]) {
      def render(p: Int, s: Int) = <.div(p - s)
    }
    // Confirm pausing works, as used in negative tests
    val pause = ReactComponentB[Int]("").initialState(3).backend(new Backend(_))
    val C = pause.renderBackend.build
  }

  object UsePropsChildren {
    class Backend($: BackendScope[Unit, Unit]) {
      def render(pc: PropsChildren) = <.div(pc)
    }
    val C = ReactComponentB[Unit]("").renderBackend[Backend].build
  }

  object AmbiguousType {
    class Backend($: BackendScope[Int, Int]) {
      def render(what: Int) = <.div(what)
    }
    val x = ReactComponentB[Int]("").initialState(3).backend(new Backend(_))
  }
}