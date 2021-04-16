package japgolly.scalajs.react

import org.scalajs.dom
import org.scalajs.dom.html

trait ReactExtensions {
  import ReactExtensions._

  @inline final implicit def ReactExt_DomNode(n: dom.raw.Node): ReactExt_DomNode =
    new ReactExt_DomNode(n)

  @inline final implicit def ReactExt_OptionCallback(o: Option[Callback]): ReactExt_OptionCallback =
    new ReactExt_OptionCallback(o)

  // @inline final implicit def ReactExt_ScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]](c: ScalaComponent.Component[P, S, B, CT]): ReactExt_ScalaComponent[P, S, B, CT] =
  //   new ReactExt_ScalaComponent(c)

  @inline final implicit def ReactExtrasExt_Any[A](a: A): ReactExtrasExt_Any[A] =
    new ReactExtrasExt_Any(a)
}

object ReactExtensions {

  /** Extensions to plain old DOM. */
  @inline implicit final class ReactExt_DomNode(private val n: dom.raw.Node) extends AnyVal {

    @inline def domCast[N <: dom.raw.Node]: N =
      n.asInstanceOf[N]

    @inline def domAsHtml: html.Element =
      domCast

    def domToHtml: Option[html.Element] =
      n match {
        case e: html.Element => Some(e)
        case _               => None
      }
  }

  @inline implicit final class ReactExt_OptionCallback(private val o: Option[Callback]) extends AnyVal {
    /** Convenience for `.getOrElse(Callback.empty)` */
    @inline def getOrEmpty: Callback =
       o.getOrElse(Callback.empty)
  }

  // I am NOT happy about this here... but it will do for now.

  // implicit final class ReactExt_ScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]](private val self: ScalaComponent.Component[P, S, B, CT]) extends AnyVal {
  //   def withRef(ref: Ref.Handle[ScalaComponent.RawMounted[P, S, B]]): ScalaComponent.Component[P, S, B, CT] =
  //     self.mapCtorType(ct => CtorType.hackBackToSelf(ct)(ct.withRawProp("ref", ref.raw)))(self.ctorPF)

  //   @deprecated("Use .withOptionalRef", "1.7.0")
  //   def withRef(r: Option[Ref.Handle[ScalaComponent.RawMounted[P, S, B]]]): ScalaComponent.Component[P, S, B, CT] =
  //     withOptionalRef(r)

  //   def withOptionalRef(optionalRef: Option[Ref.Handle[ScalaComponent.RawMounted[P, S, B]]]): ScalaComponent.Component[P, S, B, CT] =
  //     optionalRef match {
  //       case None    => self
  //       case Some(r) => withRef(r)
  //     }
  // }

  implicit final class ReactExtrasExt_Any[A](private val self: A) extends AnyVal {
    @inline def ~=~(a: A)(implicit r: Reusability[A]): Boolean = r.test(self, a)
    @inline def ~/~(a: A)(implicit r: Reusability[A]): Boolean = !r.test(self, a)
  }

}