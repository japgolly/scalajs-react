package japgolly.scalajs.react

import utest._
import vdom.html_<^._
import test.TestUtil._
import test.InferenceUtil._

object ScalaComponentBTest extends TestSuite {

  // ======
  // Stages
  // ======
  //
  // 1 = P
  // 2 = PS
  // 3 = PSB
  //     .render() mandatory
  // 4 = PSBR
  // 5 = ScalaComponent.build
  // 6 = ScalaComponent.build#Builder

  override def tests = TestSuite {
//    'defaults {
//      'auto12 - test[ScalaComponent.build.P[P]            ](_ render ???).expect[ScalaComponent.build.PSBR[P, Unit, Unit]            ]
//      'auto2  - test[ScalaComponent.build.PS[P, S]        ](_ render ???).expect[ScalaComponent.build.PSBR[P, S, Unit]               ]
//      'auto45 - test[ScalaComponent.build.PSBR[P, S, B]   ](_.build     ).expect[ReactComponentC.ReqProps[P, S, B, TopNode]     ]
//      'auto5  - test[ScalaComponent.build[P, S, B, N]     ](_.build     ).expect[ReactComponentC.ReqProps[P, S, B, N]           ]
//      'auto4U - test[ScalaComponent.build.PSBR[Unit, S, B]](_.build    ).expect[ReactComponentC.ConstProps[Unit, S, B, TopNode]]
//    }

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
    val C = ScalaComponent.build[Unit]("").initialState(State(3)).backend(new Backend(_)).renderBackend.build
  }

  object NoArgs {
    class Backend($: BackendScope[Unit, Unit]) {
      def render = <.div("hehe2")
    }
    val C = ScalaComponent.build[Unit]("").backend(new Backend(_)).renderBackend.build
  }

  object AliasesToSameType {
    class Backend($: BackendScope[PropsInt, StateInt]) {
      def render(zxc: PropsInt, qwe: StateInt) = <.div(zxc - qwe)
    }
    val C = ScalaComponent.build[PropsInt]("").initialState[StateInt](3).renderBackend[Backend].build
  }

  object TypeAliasP {
    class Backend($: BackendScope[PropsInt, Unit]) {
      def render(zxc: Int) = <.div(zxc)
    }
    val C = ScalaComponent.build[PropsInt]("").renderBackend[Backend].build
  }

  object TypeAliasS {
    class Backend($: BackendScope[Unit, StateInt]) {
      def render(zxc: Int) = <.div(zxc)
    }
    val C = ScalaComponent.build[Unit]("").initialState[StateInt](9).renderBackend[Backend].build
  }

  object TypeAliasesInMethod {
    class Backend($: BackendScope[PropsAlias, StateAlias]) {
      def render(zxc: Props, qwe: State) = <.div(zxc.a - qwe.a)
    }
    val C = ScalaComponent.build[PropsAlias]("").initialState[StateAlias](State(3)).renderBackend[Backend].build
  }

  object TypeAliasesInBackend {
    class Backend($: BackendScope[Props, State]) {
      def render(qwe: StateAlias, zxc: PropsAlias) = <.div(zxc.a - qwe.a)
    }
    val C = ScalaComponent.build[Props]("").initialState[State](State(3)).renderBackend[Backend].build
  }

  object Subtypes {
    class Backend($: BackendScope[Vector[Int], Unit]) {
      def render(zxc: Traversable[Int]) = <.div(zxc.sum)
    }
    val C = ScalaComponent.build[Vector[Int]]("").renderBackend[Backend].build
  }

  object ParamNamesFull {
    class Backend($: BackendScope[Int, Int]) {
      def render(state: Int, props: Int) = <.div(props - state)
    }
    val C = ScalaComponent.build[Int]("").initialState(3).renderBackend[Backend].build
  }

  object ParamNamesShort {
    class Backend($: BackendScope[Int, Int]) {
      def render(p: Int, s: Int) = <.div(p - s)
    }
    // Confirm pausing works, as used in negative tests
    val pause = ScalaComponent.build[Int]("").initialState(3).backend(new Backend(_))
    val C = pause.renderBackend.build
  }

  object UsePropsChildren {
    class Backend($: BackendScope[Unit, Unit]) {
      def render(pc: PropsChildren) = <.div(pc)
    }
    val C = ScalaComponent.build[Unit]("").renderBackendWithChildren[Backend].build
  }

  object AmbiguousType {
    class Backend($: BackendScope[Int, Int]) {
      def render(what: Int) = <.div(what)
    }
    val x = ScalaComponent.build[Int]("").initialState(3).backend(new Backend(_))
  }
}