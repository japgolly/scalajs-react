package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.internal.OptionLike
import japgolly.scalajs.react.vdom.Attr.ValueType
import japgolly.scalajs.react.{Callback, CallbackTo, raw}
import org.scalajs.dom
import scala.annotation.{elidable, implicitNotFound, nowarn}
import scala.compiletime.erasedValue
import scala.language.`3.0`
import scala.scalajs.LinkingInfo.developmentMode
import scala.scalajs.js
import scala.scalajs.js.|

/**
  * @tparam U Underlying type of the value required by this attribute.
  */
abstract class Attr[-U](final val attrName: String) {
  override final def toString = s"VdomAttr{name=$attrName}"

  override def hashCode = attrName.hashCode
  override def equals(any: Any) = any match {
    case that: Attr[_] => this.attrName == that.attrName
    case _             => false
  }

  def :=[A](a: A)(implicit t: ValueType[A, U]): TagMod

  final def :=?[O[_], A](oa: O[A])(implicit O: OptionLike[O], t: ValueType[A, U]): TagMod =
    O.fold(oa, TagMod.empty)(:=(_))
}

// TODO: [3] Make erased class
@implicitNotFound("You are passing a CallbackTo[${A}] to a DOM event handler which is most likely a mistake."
  + "\n  If the result is irrelevant, add `.void`."
  + "\n  If the result is necessary, please raise an issue and use `vdom.DomCallbackResult.force` in the meantime.")
sealed trait DomCallbackResult[A]
object DomCallbackResult {
  erased def force[A]: DomCallbackResult[A] = erasedValue
  erased given unit           : DomCallbackResult[Unit               ] = erasedValue
  erased given boolean        : DomCallbackResult[Boolean            ] = erasedValue
  erased given undefOrBoolean : DomCallbackResult[js.UndefOr[Boolean]] = erasedValue
}

sealed trait InnerHtmlAttr

object Attr {

  inline def apply[A](inline name: String): Attr[A] =
    new Generic[A](name)

  inline def devOnly[A](inline name: String): Attr[A] =
    if (developmentMode)
      apply(name)
    else
      Dud

  def elidable[A](name: => String): Attr[A] = {
    @elidable(scala.annotation.elidable.FINEST)
    def attempt: Attr[A] = new Generic(name)
    val x = attempt
    if (x eq null)
      Dud
    else
      x
  }

  class Generic[-U](attrName: String) extends Attr[U](attrName) {
    override def :=[A](a: A)(implicit t: ValueType[A, U]): TagMod =
      t(attrName, a)
  }

  final class Event[E[+x <: dom.Node] <: raw.SyntheticEvent[x]](name: String)
      extends Attr[js.Function1[E[Nothing], Unit]](name) {

    type Event = E[Nothing]

    override def :=[A](a: A)(implicit t: ValueType[A, js.Function1[E[Nothing], Unit]]): TagMod =
      TagMod.fn(b => t.fn(f => b.addEventHandler(name, f.asInstanceOf[js.Function1[js.Any, Unit]]), a))

    // TODO: [3] Remove `ev: `
    def -->[A](callback: => CallbackTo[A])(using erased ev: DomCallbackResult[A]): TagMod =
      ==>(_ => callback)

    // TODO: [3] Remove `ev: `
    def ==>[A](eventHandler: Event => CallbackTo[A])(using erased ev: DomCallbackResult[A]): TagMod =
      :=(((e: Event) => eventHandler(e).runNow()): js.Function1[E[Nothing], Unit])(ValueType.direct)

    def -->?[O[_]](callback: => O[Callback])(implicit o: OptionLike[O]): TagMod =
      this --> Callback(o.foreach(callback)(_.runNow()))

    def ==>?[O[_]](eventHandler: Event => O[Callback])(implicit o: OptionLike[O]): TagMod =
      ==>(e => Callback(o.foreach(eventHandler(e))(_.runNow())))
  }

  object Event {

    inline def apply[E[+x <: dom.Node] <: raw.SyntheticEvent[x]](name: String): Event[E] =
      new Event(name)

    inline def animation  (name: String) = apply[raw.SyntheticAnimationEvent  ](name)
    inline def base       (name: String) = apply[raw.SyntheticEvent           ](name)
    inline def clipboard  (name: String) = apply[raw.SyntheticClipboardEvent  ](name)
    inline def composition(name: String) = apply[raw.SyntheticCompositionEvent](name)
    inline def drag       (name: String) = apply[raw.SyntheticDragEvent       ](name)
    inline def focus      (name: String) = apply[raw.SyntheticFocusEvent      ](name)
    inline def form       (name: String) = apply[raw.SyntheticFormEvent       ](name)
    inline def keyboard   (name: String) = apply[raw.SyntheticKeyboardEvent   ](name)
    inline def mouse      (name: String) = apply[raw.SyntheticMouseEvent      ](name)
    inline def pointer    (name: String) = apply[raw.SyntheticPointerEvent    ](name)
    inline def touch      (name: String) = apply[raw.SyntheticTouchEvent      ](name)
    inline def transition (name: String) = apply[raw.SyntheticTransitionEvent ](name)
    inline def ui         (name: String) = apply[raw.SyntheticUIEvent         ](name)
    inline def wheel      (name: String) = apply[raw.SyntheticWheelEvent      ](name)
  }

  private[vdom] object Dud extends Attr[Any]("") {
    override def :=[A](a: A)(implicit t: ValueType[A, Any]) = TagMod.empty
  }

  private[vdom] object ClassName extends Attr[String]("class") {
    override def :=[A](a: A)(implicit t: ValueType[A, String]): TagMod =
      TagMod.fn(b => t.fn(b.addClassName, a))
  }

  private[vdom] object Style extends Attr[js.Object]("style") {
    override def :=[A](a: A)(implicit t: ValueType[A, js.Object]): TagMod =
      TagMod.fn(b => t.fn(b.addStyles, a))
  }

  sealed trait Key
  private[vdom] object Key extends Attr[Key]("key") {
    override def :=[A](a: A)(implicit t: ValueType[A, Key]): TagMod =
      TagMod.fn(b => t.fn(b.setKey, a))
  }

  def ref[N <: TopNode](r: japgolly.scalajs.react.Ref.Set[N]): TagMod =
    ValueType.direct("ref", r.rawSetFn)

  object UntypedRef extends Attr[raw.React.RefFn[TopNode]]("ref") {
    override def :=[A](a: A)(implicit t: ValueType[A, raw.React.RefFn[TopNode]]) =
      t(attrName, a)
    def apply(f: (TopNode | Null) => Unit): TagMod = {
      val jsFn: raw.React.RefFn[TopNode] = f
      :=(jsFn)(ValueType.direct)
    }
  }

//  implicit val ordering: Ordering[ReactAttr[Nothing]] =
//    Ordering.by((_: ReactAttr[Nothing]).name)

  // ===================================================================================================================

  /**
    * Used to specify how to handle a particular type [[A]] when it is used as
    * the value of a [[Attr]] of type [[U]].
    *
    * @tparam A Input type. Type provided by dev.
    * @tparam U Underlying type. Type required by attribute.
    */
  @implicitNotFound("Don't know how to use ${A} with a ${U} attribute.")
  final class ValueType[-A, +U](val fn: ValueType.Fn[A]) extends AnyVal {
    def apply(name: String, value: A): TagMod =
      TagMod.fn(b => fn(b.addAttr(name, _), value))
  }

  object ValueType {
    type Simple[A] = ValueType[A, A]

    type Fn[-A] = (js.Any => Unit, A) => Unit

    inline def apply[A, U](fn: Fn[A]): ValueType[A, U] =
      new ValueType(fn)

    val direct: ValueType[js.Any, Nothing] =
      apply(_(_))

    val string: ValueType[String, Nothing] =
      apply(_(_))

    def byImplicit[A, U](implicit f: A => js.Any): ValueType[A, U] =
      apply((b, a) => b(f(a)))

    @nowarn("cat=unused")
    implicit def byUnion[A, B, C](implicit f: A => (B | C)): ValueType[A, B | C] =
      apply((b, a) => b(a.asInstanceOf[js.Any]))

    implicit lazy val untypedRef: ValueType[japgolly.scalajs.react.Ref.Set[_ <: TopNode], raw.React.RefFn[TopNode]] =
      apply((f, a) => f(a.rawSetFn))

//    def array[A](implicit f: A => js.Any): Simple[js.Array[A]] =
//      map(_ map f)
//
//    def optional[T[_], A](ot: OptionLike[T], vt: ValueType[A, Any]): Simple[T[A]] =
//      apply((b, ta) => ot.foreach(ta)(vt.fn(b, _)))
  }
}
