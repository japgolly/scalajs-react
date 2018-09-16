package japgolly.scalajs.react.core

import scala.scalajs.js
import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._

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

  override def tests = Tests {

    'js - {
      'undefined - expectErrorContaining("undefined")(JsComponent[Null, Children.None, Null](js.undefined))
      'string - expectErrorContaining("tring")(JsComponent[Null, Children.None, Null]("what"))
      'num - expectErrorContaining("123")(JsComponent[Null, Children.None, Null](123))
      'obj - expectErrorContaining("hello")(JsComponent[Null, Children.None, Null](o))
      // 'fn - expectErrorContaining("a raw JsFnComponent")(JsComponent[Null, Children.None, Null](RawJsFn))
      'es60 - assertNoError(JsComponent[Null, Children.None, Null](RawJs6a))
      'es61 - assertNoError(JsComponent[Null, Children.None, Null](RawJs6b))
      'fwdRef - assertNoError(JsComponent[js.Object, Children.None, Null](RawForwardRefComp))
    }

    'jsFn - {
      'undefined - expectErrorContaining("undefined")(JsFnComponent[Null, Children.None](js.undefined))
      'string - expectErrorContaining("tring")(JsFnComponent[Null, Children.None]("what"))
      'num - expectErrorContaining("123")(JsFnComponent[Null, Children.None](123))
      'obj - expectErrorContaining("hello")(JsFnComponent[Null, Children.None](o))
      'fn - assertNoError(JsFnComponent[Null, Children.None](RawJsFn))
      // 'es60 - expectErrorContaining("a raw JsComponent")(JsFnComponent[Null, Children.None](RawJs6a))
      // 'es61 - expectErrorContaining("a raw JsComponent")(JsFnComponent[Null, Children.None](RawJs6b))
    }

  }
}
