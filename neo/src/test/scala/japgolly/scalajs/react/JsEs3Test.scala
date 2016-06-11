package japgolly.scalajs.react

import scalajs.js
import utest._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.DebugJs._
import japgolly.scalajs.react.test.TestUtil._

/*
abstract class JsEs3Test extends TestSuite {
  final val H1: raw.ReactElement =
    raw.React.createElement("h1", null, "Huge")
}

object JsEs3PTest extends JsEs3Test {

  @js.native
  trait JsProps extends js.Object {
    val name: String
  }

  def JsProps(name: String): JsProps =
    js.Dynamic.literal("name" -> name).asInstanceOf[JsProps]

  val RawClass = js.Dynamic.global.ES3_P.asInstanceOf[raw.ReactClass]
  val Component = CompJs3.Constructor[JsProps, ChildrenArg.None, Null](RawClass)
  compileError(""" Component() """)

  override def tests = TestSuite {

    'noChildren {
      'main {
        val unmounted = Component(JsProps("Bob"))
        assertEq(unmounted.props.name, "Bob")
        assertEq(unmounted.propsChildren.count, 0)
        assertEq(unmounted.propsChildren.isEmpty, true)
        assertEq(unmounted.key, None)
        assertEq(unmounted.ref, None)
        withBodyContainer { mountNode =>
          val mounted = unmounted.renderIntoDOM(mountNode)
          val n = mounted.getDOMNode
          assertOuterHTML(n, "<div>Hello Bob</div>")
          assertEq(mounted.isMounted, true)
          assertEq(mounted.props.name, "Bob")
          assertEq(mounted.propsChildren.count, 0)
          assertEq(mounted.propsChildren.isEmpty, true)
          assertEq(mounted.state, null)
        }
      }

      'key {
        val unmounted = Component.set(key = "hehe")(JsProps("Bob"))
        assertEq(unmounted.props.name, "Bob")
        assertEq(unmounted.propsChildren.count, 0)
        assertEq(unmounted.propsChildren.isEmpty, true)
        assertEq(unmounted.key, Some("hehe": Key))
        assertEq(unmounted.ref, None)
        withBodyContainer { mountNode =>
          val mounted = unmounted.renderIntoDOM(mountNode)
          val n = mounted.getDOMNode
          assertOuterHTML(n, "<div>Hello Bob</div>")
          assertEq(mounted.isMounted, true)
          assertEq(mounted.props.name, "Bob")
          assertEq(mounted.propsChildren.count, 0)
          assertEq(mounted.propsChildren.isEmpty, true)
          assertEq(mounted.state, null)
        }
      }

      'ctorReuse -
        assert(Component(JsProps("a")) ne Component(JsProps("b")))
    }

    'children {
      val C = CompJs3.Constructor[JsProps, ChildrenArg.Varargs, Null](RawClass)

      'ctors {
        val p = JsProps("x")
        def test(u: CompJs3.Unmounted[JsProps, Null]) = ()
        compileError(""" test(C())         """)
        compileError(""" test(C()())       """)
        compileError(""" test(C()(H1))     """)
        compileError(""" test(C()(H1, H1)) """)
        compileError(""" test(C(p))        """)
        test(C(p)())
        test(C(p)(H1))
        test(C(p)(H1, H1))
      }

      'use {
        val unmounted = C(JsProps("X"))(H1)
        assertEq(unmounted.props.name, "X")
        assertEq(unmounted.propsChildren.count, 1)
        assertEq(unmounted.propsChildren.isEmpty, false)
        assertEq(unmounted.key, None)
        assertEq(unmounted.ref, None)
        withBodyContainer { mountNode =>
          val mounted = unmounted.renderIntoDOM(mountNode)
          val n = mounted.getDOMNode
          assertOuterHTML(n, "<div>Hello X<h1>Huge</h1></div>")
          assertEq(mounted.isMounted, true)
          assertEq(mounted.props.name, "X")
          assertEq(mounted.propsChildren.count, 1)
          assertEq(mounted.propsChildren.isEmpty, false)
          assertEq(mounted.state, null)
        }
      }
    }

  }
}


object JsEs3STest extends JsEs3Test {

  @js.native
  trait JsState extends js.Object {
    val num1: Int
    val num2: Int
  }

  @js.native
  trait JsMethods extends js.Object {
    def inc(): Unit = js.native
  }

  val RawClass = js.Dynamic.global.ES3_S.asInstanceOf[raw.ReactClass]
  val Component = CompJs3.Constructor[Null, ChildrenArg.None, JsState](RawClass).mapMounted(_.addRawType[JsMethods])

  override def tests = TestSuite {
    def JsState1(num1: Int): JsState =
      js.Dynamic.literal("num1" -> num1).asInstanceOf[JsState]
    def JsState(num1: Int, num2: Int): JsState =
      js.Dynamic.literal("num1" -> num1, "num2" -> num2).asInstanceOf[JsState]

    'noChildren {
      'main {
        val unmounted = Component()
        assertEq(unmounted.propsChildren.count, 0)
        assertEq(unmounted.propsChildren.isEmpty, true)
        assertEq(unmounted.key, None)
        assertEq(unmounted.ref, None)
        withBodyContainer { mountNode =>
          val mounted = unmounted.renderIntoDOM(mountNode)
          val n = mounted.getDOMNode

          assertOuterHTML(n, "<div>State = 123 + 500</div>")
          assertEq(mounted.isMounted, true)
          assertEq(mounted.propsChildren.count, 0)
          assertEq(mounted.propsChildren.isEmpty, true)
          assertEq(mounted.state.num1, 123)
          assertEq(mounted.state.num2, 500)

          mounted.setState(JsState1(666))
          assertOuterHTML(n, "<div>State = 666 + 500</div>")
          assertEq(mounted.isMounted, true)
          assertEq(mounted.propsChildren.isEmpty, true)
          assertEq(mounted.state.num1, 666)
          assertEq(mounted.state.num2, 500)

          mounted.rawInstance.inc()
          assertOuterHTML(n, "<div>State = 667 + 500</div>")
          assertEq(mounted.isMounted, true)
          assertEq(mounted.propsChildren.isEmpty, true)
          assertEq(mounted.state.num1, 667)
          assertEq(mounted.state.num2, 500)

          val zoomed = mounted.zoomState(_.num2)((s, n) => JsState(s.num1, n))
          assertEq(zoomed.state, 500)
          zoomed.modState(_ + 1)
          assertOuterHTML(n, "<div>State = 667 + 501</div>")
          assertEq(mounted.isMounted, true)
          assertEq(mounted.propsChildren.isEmpty, true)
          assertEq(mounted.state.num1, 667)
          assertEq(mounted.state.num2, 501)
        }
      }

      'ctorReuse -
        assert(Component() eq Component())
    }

    'children {
      val C = CompJs3.Constructor[Null, ChildrenArg.Varargs, JsState](RawClass).mapMounted(_.addRawType[JsMethods])

      'ctors {
        type M = CompJs3X.Mounted[Null, JsState, raw.ReactComponent with JsMethods]
        def test(u: CompJs3X.Unmounted[Null, JsState, M]) = ()
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

      'use {
        val unmounted = C(H1)
        assertEq(unmounted.propsChildren.count, 1)
        assertEq(unmounted.propsChildren.isEmpty, false)
        assertEq(unmounted.key, None)
        assertEq(unmounted.ref, None)
        withBodyContainer { mountNode =>
          val mounted = unmounted.renderIntoDOM(mountNode)
          val n = mounted.getDOMNode

          assertOuterHTML(n, "<div>State = 123 + 500<h1>Huge</h1></div>")
          assertEq(mounted.isMounted, true)
          assertEq(mounted.propsChildren.count, 1)
          assertEq(mounted.propsChildren.isEmpty, false)
          assertEq(mounted.state.num1, 123)
          assertEq(mounted.state.num2, 500)
        }
      }
    }

  }
}

*/