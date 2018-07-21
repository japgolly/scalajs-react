package japgolly.scalajs.react

import japgolly.scalajs.react.internal.JsRepr
import japgolly.scalajs.react.{raw => Raw}

object React {
  def raw = Raw.React

  /** Create a new context.
    *
    * If you'd like to retain type information about the JS type used under-the-hood with React,
    * use `React.Context(defaultValue)` instead.
    *
    * @since 1.3.0 / React 16.3.0
    */
  def createContext[A](defaultValue: A)(implicit jsRepr: JsRepr[A]): Context[A] =
    Context(defaultValue)(jsRepr)

  type Context[A] = feature.Context[A]
  val Context     = feature.Context

  val Fragment    = feature.ReactFragment

}
