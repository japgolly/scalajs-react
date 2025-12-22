package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.ImplicitsFromRaw._
import scala.scalajs.js
import utest._

abstract class JsComponentTest extends TestSuite {
  japgolly.scalajs.react.test.InitTestEnv()
  final val H1: facade.React.Element =
    facade.React.createElement("h1", null, "Huge")
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
    }

    "noChildren" - {
      "main" - {
        val unmounted = Component(JsProps("Bob"))
        assertEq(unmounted.props.name, "Bob")
        assertEq(unmounted.propsChildren.count, 0)
        assertEq(unmounted.propsChildren.isEmpty, true)
        assertEq(unmounted.key, None)
        assertEq(unmounted.ref, None)
        ReactTestUtils.withRenderedSync(unmounted) { t =>
          t.outerHTML.assert("<div>Hello Bob</div>")
        }
      }

      "withKey" - {
        val unmounted = Component.withKey("hehe")(JsProps("Bob"))
        assertEq(unmounted.props.name, "Bob")
        assertEq(unmounted.propsChildren.count, 0)
        assertEq(unmounted.propsChildren.isEmpty, true)
        assertEq(unmounted.key, Some("hehe": Key))
        assertEq(unmounted.ref, None)
        ReactTestUtils.withRenderedSync(unmounted) { t =>
          t.outerHTML.assert("<div>Hello Bob</div>")
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
        ReactTestUtils.withRenderedSync(unmounted) { t =>
          t.outerHTML.assert("<div>Hello X<h1>Huge</h1></div>")
        }
      }

      "withKey" - {
        ReactTestUtils.withRenderedSync(C.withKey("k")(JsProps("X"))(H1)) { t  =>
          t.outerHTML.assert("<div>Hello X<h1>Huge</h1></div>")
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
    // def JsState1(num1: Int): JsState =
    //   js.Dynamic.literal("num1" -> num1).asInstanceOf[JsState]
    def JsState(num1: Int, num2: Int): JsState =
      js.Dynamic.literal("num1" -> num1, "num2" -> num2).asInstanceOf[JsState]

    "displayName" - {
      def n = "Statey"
      "c" - assertEq(Component.displayName, n)
      "u" - assertEq(Component().displayName, n)
    }

    "noChildren" - {
      "main" - {
        val unmounted = Component()
        assertEq(unmounted.propsChildren.count, 0)
        assertEq(unmounted.propsChildren.isEmpty, true)
        assertEq(unmounted.key, None)
        assertEq(unmounted.ref, None)
        ReactTestUtils.withRenderedSync(unmounted) { t =>
          val s = JsState(123, 500)
          t.outerHTML.assert(s"<div>State = ${s.num1} + ${s.num2}</div>")
        }
      }

      "ctorReuse" -
        assert(Component() eq Component())

      "withKey" - {
        ReactTestUtils.withRenderedSync(Component.withKey("k")()) { t =>
          t.outerHTML.assert("<div>State = 123 + 500</div>")
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
        ReactTestUtils.withRenderedSync(unmounted) { t =>
          t.outerHTML.assert("<div>State = 123 + 500<h1>Huge</h1></div>")
        }
      }

      "withKey" - {
        ReactTestUtils.withRenderedSync(C.withKey("k")(H1)) { t =>
          t.outerHTML.assert("<div>State = 123 + 500<h1>Huge</h1></div>")
        }
      }
    }

  }
}
