package japgolly.scalajs.react.vdom

import scala.scalajs.LinkingInfo.developmentMode

private[vdom] class Style[-U](name: String) extends Attr[U]("style." + name) {
  override def :=[A](a: A)(implicit t: Attr.ValueType[A, U]): TagMod =
    TagMod.fn(b => t.fn(b.addStyle(name, _), a))
}

object Style {

  def apply[A](name: String): Attr[A] =
    new Style[A](name)

  def devOnly[A](name: => String): Attr[A] =
    if (developmentMode)
      apply(name)
    else
      Attr.Dud
}
