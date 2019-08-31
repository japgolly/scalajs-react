package japgolly.scalajs.react.core

import scalajs.js
import scalajs.js.annotation._
import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.ImplicitsFromRaw._

abstract class JsComponentTest extends TestSuite {
  final val H1: raw.React.Element =
    raw.React.createElement("h1", null, "Huge")
}

object JsComponentEs6PTest extends JsComponentTest {
//  @JSName("ES6_P")
//  @js.native
//  object RawComp extends js.Object
  lazy val RawComp = js.eval("ES6_P") // https://github.com/scala-js/scala-js/issues/2800

  @js.native
  trait JsProps extends js.Object {
    val name: String
  }

  def JsProps(name: String): JsProps =
    js.Dynamic.literal("name" -> name).asInstanceOf[JsProps]

  lazy val Component = JsComponent[JsProps, Children.None, Null](RawComp)
  compileError(""" Component() """)

  override def tests = Tests {

    "unspecifiedDisplayName" - {
      def n = ""
      def p = JsProps("Bob")
      "c" - assertEq(Component.displayName, n)
      "u" - assertEq(Component(p).displayName, n)
      "m" - assertEq(ReactTestUtils.withRenderedIntoDocument(Component(p))(_.displayName), n)
    }

    "noChildren" - {
      "main" - {
        val unmounted = Component(JsProps("Bob"))
        assertEq(unmounted.props.name, "Bob")
        assertEq(unmounted.propsChildren.count, 0)
        assertEq(unmounted.propsChildren.isEmpty, true)
        assertEq(unmounted.key, None)
        assertEq(unmounted.ref, None)
        ReactTestUtils.withNewBodyElement { mountNode =>
          val mounted = unmounted.renderIntoDOM(mountNode)
          val n = mounted.getDOMNode.asMounted().asElement()
          assertOuterHTML(n, "<div>Hello Bob</div>")
          // assertEq(mounted.isMounted, yesItsMounted)
          assertEq(mounted.props.name, "Bob")
          assertEq(mounted.propsChildren.count, 0)
          assertEq(mounted.propsChildren.isEmpty, true)
          assertEq(mounted.state, null)
        }
      }

      "withKey" - {
        val unmounted = Component.withKey("hehe")(JsProps("Bob"))
        assertEq(unmounted.props.name, "Bob")
        assertEq(unmounted.propsChildren.count, 0)
        assertEq(unmounted.propsChildren.isEmpty, true)
        assertEq(unmounted.key, Some("hehe": Key))
        assertEq(unmounted.ref, None)
        ReactTestUtils.withNewBodyElement { mountNode =>
          val mounted = unmounted.renderIntoDOM(mountNode)
          val n = mounted.getDOMNode.asMounted().asElement()
          assertOuterHTML(n, "<div>Hello Bob</div>")
          // assertEq(mounted.isMounted, yesItsMounted)
          assertEq(mounted.props.name, "Bob")
          assertEq(mounted.propsChildren.count, 0)
          assertEq(mounted.propsChildren.isEmpty, true)
          assertEq(mounted.state, null)
        }
      }

      "ctorReuse" -
        assert(Component(JsProps("a")) ne Component(JsProps("b")))
    }

    "children" - {
      val C = JsComponent[JsProps, Children.Varargs, Null](RawComp)

      "ctors" - {
        val p = JsProps("x")
        def test(u: JsComponent.Unmounted[JsProps, Null]) = ()
        compileError(""" test(C())         """)
        compileError(""" test(C()())       """)
        compileError(""" test(C()(H1))     """)
        compileError(""" test(C()(H1, H1)) """)
        compileError(""" test(C(p))        """)
        test(C(p)())
        test(C(p)(H1))
        test(C(p)(H1, H1))
      }

      "use" - {
        val unmounted = C(JsProps("X"))(H1)
        assertEq(unmounted.props.name, "X")
        assertEq(unmounted.propsChildren.count, 1)
        assertEq(unmounted.propsChildren.isEmpty, false)
        assertEq(unmounted.key, None)
        assertEq(unmounted.ref, None)
        ReactTestUtils.withNewBodyElement { mountNode =>
          val mounted = unmounted.renderIntoDOM(mountNode)
          val n = mounted.getDOMNode.asMounted().asElement()
          assertOuterHTML(n, "<div>Hello X<h1>Huge</h1></div>")
          // assertEq(mounted.isMounted, yesItsMounted)
          assertEq(mounted.props.name, "X")
          assertEq(mounted.propsChildren.count, 1)
          assertEq(mounted.propsChildren.isEmpty, false)
          assertEq(mounted.state, null)
        }
      }

      "withKey" - {
        ReactTestUtils.withNewBodyElement { mountNode =>
          val n = C.withKey("k")(JsProps("X"))(H1).renderIntoDOM(mountNode).getDOMNode.asMounted().asElement()
          assertOuterHTML(n, "<div>Hello X<h1>Huge</h1></div>")
        }
      }
    }

  }
}


object JsComponentEs6STest extends JsComponentTest {
//  @JSGlobal("ES6_S")
//  @js.native
//  object RawComp extends js.Object
  lazy val RawComp = js.eval("ES6_S") // https://github.com/scala-js/scala-js/issues/2800

  @js.native
  trait JsState extends js.Object {
    val num1: Int
    val num2: Int
  }

  @js.native
  trait JsMethods extends js.Object {
    def inc(): Unit = js.native
  }

  lazy val Component = JsComponent[Null, Children.None, JsState](RawComp).addFacade[JsMethods]

  override def tests = Tests {
    def JsState1(num1: Int): JsState =
      js.Dynamic.literal("num1" -> num1).asInstanceOf[JsState]
    def JsState(num1: Int, num2: Int): JsState =
      js.Dynamic.literal("num1" -> num1, "num2" -> num2).asInstanceOf[JsState]

    "displayName" - {
      def n = "Statey"
      "c" - assertEq(Component.displayName, n)
      "u" - assertEq(Component().displayName, n)
      "m" - assertEq(ReactTestUtils.withRenderedIntoDocument(Component())(_.displayName), n)
    }

    "noChildren" - {
      "main" - {
        def setNum1(n: Int)(s: JsState): JsState = JsState(n, s.num2)
        var callCount = 0
        val incCallCount = Callback(callCount += 1)
        val unmounted = Component()
        assertEq(unmounted.propsChildren.count, 0)
        assertEq(unmounted.propsChildren.isEmpty, true)
        assertEq(unmounted.key, None)
        assertEq(unmounted.ref, None)
        ReactTestUtils.withNewBodyElement { mountNode =>
          val mounted = unmounted.renderIntoDOM(mountNode)
          val n = mounted.getDOMNode.asMounted().asElement()
          var s = JsState(123, 500)
          var cc = 0

          def test(children: Int = 0, incCallCount: Boolean = false): Unit = {
            if (incCallCount) cc += 1
            assertOuterHTML(n, s"<div>State = ${s.num1} + ${s.num2}</div>")
            assertEq((mounted.state.num1, mounted.state.num2), (s.num1, s.num2))
            assertEq("propsChildren.count", mounted.propsChildren.count, children)
            assertEq("propsChildren.isEmpty", mounted.propsChildren.isEmpty, children == 0)
            assertEq("callCount", callCount, cc)
          }

          test()

          s = JsState(50, s.num2)
          mounted.setState(JsState1(50), incCallCount)
          test(incCallCount = true)

          s = JsState(300, 11)
          mounted.setStateOption(Some(s), incCallCount)
          test(incCallCount = true)

          mounted.setStateOption(None, incCallCount)
          test(incCallCount = true)

          s = JsState(88, s.num2)
          mounted.modState(setNum1(88)(_), incCallCount)
          test(incCallCount = true)

          s = JsState(828, s.num2)
          mounted.modStateOption(x => Some(setNum1(828)(x)), incCallCount)
          test(incCallCount = true)

          mounted.modStateOption(_ => None, incCallCount)
          test(incCallCount = true)

          s = JsState(666, 500)
          mounted.setState(s)
          test()

          mounted.raw.inc()
          s = JsState(667, s.num2)
          test()

          val zoomed = mounted.zoomState(_.num2)(n => s => JsState(s.num1, n))
          assertEq(zoomed.state, 500)
          zoomed.modState(_ + 1)
          s = JsState(s.num1, 501)
          test()
        }
      }

      "ctorReuse" -
        assert(Component() eq Component())

      "withKey" - {
        ReactTestUtils.withNewBodyElement { mountNode =>
          val n = Component.withKey("k")().renderIntoDOM(mountNode).getDOMNode.asMounted().asElement()
          assertOuterHTML(n, "<div>State = 123 + 500</div>")
        }
      }
    }

    "children" - {
      val C = JsComponent[Null, Children.Varargs, JsState](RawComp).addFacade[JsMethods]

      "ctors" - {
        def test(u: JsComponent.UnmountedWithFacade[Null, JsState, JsMethods]) = ()
        compileError(""" test(C()())           """)
        compileError(""" test(C()(H1))         """)
        compileError(""" test(C()(H1, H1))     """)
        compileError(""" test(C(null)())       """)
        compileError(""" test(C(null)(H1))     """)
        compileError(""" test(C(null)(H1, H1)) """)
        test(C())
        test(C(H1))
        test(C(H1, H1))
      }

      "use" - {
        val unmounted = C(H1)
        assertEq(unmounted.propsChildren.count, 1)
        assertEq(unmounted.propsChildren.isEmpty, false)
        assertEq(unmounted.key, None)
        assertEq(unmounted.ref, None)
        ReactTestUtils.withNewBodyElement { mountNode =>
          val mounted = unmounted.renderIntoDOM(mountNode)
          val n = mounted.getDOMNode.asMounted().asElement()

          assertOuterHTML(n, "<div>State = 123 + 500<h1>Huge</h1></div>")
          // assertEq(mounted.isMounted, yesItsMounted)
          assertEq(mounted.propsChildren.count, 1)
          assertEq(mounted.propsChildren.isEmpty, false)
          assertEq(mounted.state.num1, 123)
          assertEq(mounted.state.num2, 500)
        }
      }

      "withKey" - {
        ReactTestUtils.withNewBodyElement { mountNode =>
          val n = C.withKey("k")(H1).renderIntoDOM(mountNode).getDOMNode.asMounted().asElement()
          assertOuterHTML(n, "<div>State = 123 + 500<h1>Huge</h1></div>")
        }
      }
    }

  }
}

