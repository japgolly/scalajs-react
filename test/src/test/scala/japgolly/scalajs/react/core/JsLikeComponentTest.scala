package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import scala.scalajs.js
import utest._

object JsLikeComponentTest extends TestSuite {

  def RawJs6a = JsComponentEs6PTest.RawComp // nullary ctor
  def RawJs6b = JsComponentEs6STest.RawComp // unary ctor
  def RawJsFn = JsFnComponentTest.RawComp

  def RawForwardRefComp = raw.React.forwardRef((_: js.Object, _: raw.React.ForwardedRef[_]) => null)

  def assertNoError[A](a: => A): Unit = {
    a
    ()
  }

  val o = js.Dynamic.literal("hello" -> 123)

  private def testAssertionFires[A](errFrag: String)(a: => A): Unit =
    if (!TestEnv.fullCI)
      expectErrorContaining(errFrag)(a)

  override def tests = Tests {

    /*
    TODO: https://github.com/lampepfl/dotty/issues/12247
    "js" - {
      "undefined" - testAssertionFires("undefined")(JsComponent[Null, Children.None, Null](js.undefined))
      "string" - testAssertionFires("tring")(JsComponent[Null, Children.None, Null]("what"))
      "num" - testAssertionFires("123")(JsComponent[Null, Children.None, Null](123))
      "obj" - testAssertionFires("hello")(JsComponent[Null, Children.None, Null](o))
      // "fn" - testAssertionFires("a raw JsFnComponent")(JsComponent[Null, Children.None, Null](RawJsFn))
      "es60" - assertNoError(JsComponent[Null, Children.None, Null](RawJs6a))
      "es61" - assertNoError(JsComponent[Null, Children.None, Null](RawJs6b))
      "fwdRef" - assertNoError(JsComponent[js.Object, Children.None, Null](RawForwardRefComp))
    }

    "jsFn" - {
      "undefined" - testAssertionFires("undefined")(JsFnComponent[Null, Children.None](js.undefined))
      "string" - testAssertionFires("tring")(JsFnComponent[Null, Children.None]("what"))
      "num" - testAssertionFires("123")(JsFnComponent[Null, Children.None](123))
      "obj" - testAssertionFires("hello")(JsFnComponent[Null, Children.None](o))
      "fn" - assertNoError(JsFnComponent[Null, Children.None](RawJsFn))
      // "es60" - testAssertionFires("a raw JsComponent")(JsFnComponent[Null, Children.None](RawJs6a))
      // "es61" - testAssertionFires("a raw JsComponent")(JsFnComponent[Null, Children.None](RawJs6b))
    }
    */

    "js" - {
      "undefined" - {def x = JsComponent[Null, Children.None, Null](js.undefined); testAssertionFires("undefined")(x)}
      "string" - {def x = JsComponent[Null, Children.None, Null]("what"); testAssertionFires("tring")(x)}
      "num" - {def x = JsComponent[Null, Children.None, Null](123); testAssertionFires("123")(x)}
      "obj" - {def x = JsComponent[Null, Children.None, Null](o); testAssertionFires("hello")(x)}
      // "fn" - testAssertionFires("a raw JsFnComponent")(JsComponent[Null, Children.None, Null](RawJsFn))
      "es60" - {def x = JsComponent[Null, Children.None, Null](RawJs6a); assertNoError(x)}
      "es61" - {def x = JsComponent[Null, Children.None, Null](RawJs6b); assertNoError(x)}
      "fwdRef" - {def x = JsComponent[js.Object, Children.None, Null](RawForwardRefComp); assertNoError(x)}
    }

    "jsFn" - {
      "undefined" - {def x = JsFnComponent[Null, Children.None](js.undefined); testAssertionFires("undefined")(x)}
      "string" - {def x = JsFnComponent[Null, Children.None]("what"); testAssertionFires("tring")(x)}
      "num" - {def x = JsFnComponent[Null, Children.None](123); testAssertionFires("123")(x)}
      "obj" - {def x = JsFnComponent[Null, Children.None](o); testAssertionFires("hello")(x)}
      "fn" - {def x = JsFnComponent[Null, Children.None](RawJsFn); assertNoError(x)}
      // "es60" - testAssertionFires("a raw JsComponent")(JsFnComponent[Null, Children.None](RawJs6a))
      // "es61" - testAssertionFires("a raw JsComponent")(JsFnComponent[Null, Children.None](RawJs6b))
    }

  }
}
