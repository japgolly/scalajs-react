package japgolly.scalajs.react.core

import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test.InferenceUtil._

object ScalaBuilderTest extends TestSuite {

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

  override def tests = Tests {
    "defaults" - {
      import ScalaComponent.builder
      "autoSB" - testExpr(builder[P]("")                     .render_P(???).build).expect[ScalaComponent[P, Unit, Unit, CtorType.Props]]
      "autoB"  - testExpr(builder[P]("").initialState[S](???).render_P(???).build).expect[ScalaComponent[P, S   , Unit, CtorType.Props]]
      "autoS"  - testExpr(builder[P]("").backend[B](???)     .render_P(???).build).expect[ScalaComponent[P, Unit, B   , CtorType.Props]]
    }

    "renderBackend" - {
      import BackendMacroTestData._
      "success" - {
        "val"                  - assertRender(Val.C(), "<div>hehe1</div>")
        "noArgs"               - assertRender(NoArgs.C(), "<div>hehe2</div>")
        "aliasesToSameType"    - assertRender(AliasesToSameType.C(7), "<div>4</div>")
        "typeAliasP"           - assertRender(TypeAliasP.C(9), "<div>9</div>")
        "typeAliasS"           - assertRender(TypeAliasS.C(), "<div>9</div>")
        "typeAliasesInMethod"  - assertRender(TypeAliasesInMethod.C(Props(7)), "<div>4</div>")
        "typeAliasesInBackend" - assertRender(TypeAliasesInBackend.C(Props(7)), "<div>4</div>")
        "subtypes"             - assertRender(Subtypes.C(Vector(1,8)), "<div>9</div>")
        "paramNamesFull"       - assertRender(ParamNamesFull.C(7), "<div>4</div>")
        "paramNamesShort"      - assertRender(ParamNamesShort.C(7), "<div>4</div>")
        "useChildren"          - assertRender(UseChildren.C(<.br), "<div><br/></div>")
        "usePropsAndChildren"  - assertRender(UsePropsAndChildren.C(1)(<.br), "<div>1<br/></div>")
      }

      "failure" - {
        "ambiguousType" - assertContains(
          compileError("AmbiguousType.x.renderBackend").msg, "what: Int")

        "useChildrenWithoutSpecifying" - assertContains(
          compileError("UseChildrenWithoutSpecifying.x.renderBackend").msg,
          "Use renderBackendWithChildren instead")

        "specifyChildrenWithoutUsing" - assertContains(
          compileError("SpecifyChildrenWithoutUsing.x.renderBackendWithChildren").msg,
          "Use renderBackend instead")
      }

    }


    "updateSnapshot" - {
      def start = ScalaComponent.builder[P]("").render_P(???)
      def int = (_: Any) => CallbackTo(2)

      "badAccess" - {
        def test(c: CompileError) = {
          val msg = c.msg.toLowerCase
          assertContains(msg, "getsnapshotbeforeupdate")
          assertContains(msg, "componentdidupdate")
          c.msg
        }
        "SS" - test(compileError("start.getSnapshotBeforeUpdate(int).getSnapshotBeforeUpdate(int)"))
        "US" - test(compileError("start.componentDidUpdate(???).getSnapshotBeforeUpdate(int)"))
      }

      "type" - { // FIXME see https://github.com/scala/bug/issues/11660
        "U" - assertCompiles {
          val aux = start.componentDidUpdate($ => Callback(testExpr($.snapshot).expect[Unit]))
          aux.componentDidUpdate($ => Callback(testExpr($.snapshot).expect[Unit]))
        }

        "SU" - assertCompiles {
          val aux = start
            .getSnapshotBeforeUpdate(int)
            .componentDidUpdate($ => Callback(testExpr($.snapshot).expect[Int]))
          aux.componentDidUpdate($ => Callback(testExpr($.snapshot).expect[Int]))
        }

//        "U" - assertCompiles(start
//          .componentDidUpdate($ => Callback(testExpr($.snapshot).expect[Unit]))
//          .componentDidUpdate($ => Callback(testExpr($.snapshot).expect[Unit])))
//        //FIXME ScalaBuilderTest.scala:81:56: cyclic aliasing or subtyping involving type SnapshotValue
//
//        "SU" - assertCompiles(start
//          .getSnapshotBeforeUpdate(int)
//          .componentDidUpdate($ => Callback(testExpr($.snapshot).expect[Int]))
//          .componentDidUpdate($ => Callback(testExpr($.snapshot).expect[Int])))
//        //FIXME ScalaBuilderTest.scala:86:56: cyclic aliasing or subtyping involving type SnapshotValue
      }
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
    val C = ScalaComponent.builder[Unit]("").initialState(State(3)).backend(new Backend(_)).renderBackend.build
  }

  object NoArgs {
    class Backend($: BackendScope[Unit, Unit]) {
      def render = <.div("hehe2")
    }
    val C = ScalaComponent.builder[Unit]("").backend(new Backend(_)).renderBackend.build
  }

  object AliasesToSameType {
    class Backend($: BackendScope[PropsInt, StateInt]) {
      def render(zxc: PropsInt, qwe: StateInt) = <.div(zxc - qwe)
    }
    val C = ScalaComponent.builder[PropsInt]("").initialState[StateInt](3).renderBackend[Backend].build
  }

  object TypeAliasP {
    class Backend($: BackendScope[PropsInt, Unit]) {
      def render(zxc: Int) = <.div(zxc)
    }
    val C = ScalaComponent.builder[PropsInt]("").renderBackend[Backend].build
  }

  object TypeAliasS {
    class Backend($: BackendScope[Unit, StateInt]) {
      def render(zxc: Int) = <.div(zxc)
    }
    val C = ScalaComponent.builder[Unit]("").initialState[StateInt](9).renderBackend[Backend].build
  }

  object TypeAliasesInMethod {
    class Backend($: BackendScope[PropsAlias, StateAlias]) {
      def render(zxc: Props, qwe: State) = <.div(zxc.a - qwe.a)
    }
    val C = ScalaComponent.builder[PropsAlias]("").initialState[StateAlias](State(3)).renderBackend[Backend].build
  }

  object TypeAliasesInBackend {
    class Backend($: BackendScope[Props, State]) {
      def render(qwe: StateAlias, zxc: PropsAlias) = <.div(zxc.a - qwe.a)
    }
    val C = ScalaComponent.builder[Props]("").initialState[State](State(3)).renderBackend[Backend].build
  }

  object Subtypes {
    class Backend($: BackendScope[Vector[Int], Unit]) {
      def render(zxc: Traversable[Int]) = <.div(zxc.sum)
    }
    val C = ScalaComponent.builder[Vector[Int]]("").renderBackend[Backend].build
  }

  object ParamNamesFull {
    class Backend($: BackendScope[Int, Int]) {
      def render(state: Int, props: Int) = <.div(props - state)
    }
    val C = ScalaComponent.builder[Int]("").initialState(3).renderBackend[Backend].build
  }

  object ParamNamesShort {
    class Backend($: BackendScope[Int, Int]) {
      def render(p: Int, s: Int) = <.div(p - s)
    }
    // Confirm pausing works, as used in negative tests
    val pause = ScalaComponent.builder[Int]("").initialState(3).backend(new Backend(_))
    val C = pause.renderBackend.build
  }

  object UseChildren {
    class Backend($: BackendScope[Unit, Unit]) {
      def render(pc: PropsChildren) = <.div(pc)
    }
    val C = ScalaComponent.builder[Unit]("").renderBackendWithChildren[Backend].build
  }

  object UsePropsAndChildren {
    class Backend($: BackendScope[Int, Unit]) {
      def render(i: Int, pc: PropsChildren) = <.div(i, pc)
    }
    val C = ScalaComponent.builder[Int]("").renderBackendWithChildren[Backend].build
  }

  object UseChildrenWithoutSpecifying {
    class Backend($: BackendScope[Unit, Unit]) {
      def render(pc: PropsChildren) = <.div(pc)
    }
    val x = ScalaComponent.builder[Unit]("").backend(new Backend(_))
  }

  object SpecifyChildrenWithoutUsing {
    class Backend($: BackendScope[Int, Unit]) {
      def render(props: Int) = <.div(props)
    }
    val x = ScalaComponent.builder[Int]("").backend(new Backend(_))
  }

  object AmbiguousType {
    class Backend($: BackendScope[Int, Int]) {
      def render(what: Int) = <.div(what)
    }
    val x = ScalaComponent.builder[Int]("").initialState(3).backend(new Backend(_))
  }
}