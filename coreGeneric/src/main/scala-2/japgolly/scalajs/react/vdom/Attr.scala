package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.{DefaultEffects, OptionLike}
import japgolly.scalajs.react.vdom.Attr.ValueType
import japgolly.scalajs.react.{Reusable, facade}
import org.scalajs.dom
import scala.annotation.{elidable, implicitNotFound, nowarn}
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

sealed trait InnerHtmlAttr

object Attr {

  def apply[A](name: String): Attr[A] =
    new Generic[A](name)

  def devOnly[A](name: => String): Attr[A] =
    if (developmentMode)
      new Generic(name)
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

  // ===================================================================================================================

  final case class EventCallback[A](prepare: (=> A) => js.Function0[Unit]) extends AnyVal {
    def runOption[O[_]](callback: O[A])(implicit O: OptionLike[O]): Unit =
      O.foreach(callback)(prepare(_)())
  }

  trait EventCallback1 {
    implicit def reusableDispatch[F[_]](implicit F: Dispatch[F]): EventCallback[Reusable[F[Unit]]] =
      new EventCallback(fa => F.dispatchFn(fa))
  }

  trait EventCallback2 extends EventCallback1 {
    implicit def dispatch[F[_]](implicit F: Dispatch[F]): EventCallback[F[Unit]] =
      new EventCallback(fa => F.dispatchFn(fa))
  }

  trait EventCallback3 extends EventCallback2 {
    implicit lazy val reusableDefaultSync: EventCallback[Reusable[DefaultEffects.Sync[Unit]]] =
      reusableDispatch(DefaultEffects.Sync)
  }

  object EventCallback extends EventCallback3 {
    implicit val defaultSync: EventCallback[DefaultEffects.Sync[Unit]] =
      dispatch(DefaultEffects.Sync)
  }

  type EventHandler[E[+x <: dom.Node] <: facade.SyntheticEvent[x]] =
    js.Function0[Unit] | js.Function1[E[Nothing], Unit]

  final class Event[E[+x <: dom.Node] <: facade.SyntheticEvent[x]](name: String) extends Attr[EventHandler[E]](name) {
    type Event = E[Nothing]

    override def :=[A](a: A)(implicit t: ValueType[A, EventHandler[E]]): TagMod =
      TagMod.fn(b => t.fn(f => b.addEventHandler(name, f.asInstanceOf[js.Function1[js.Any, Unit]]), a))

    def -->[A](callback: => A)(implicit F: EventCallback[A]): TagMod =
      :=(F.prepare(callback))(ValueType.direct)

    def ==>[A](eventHandler: Event => A)(implicit F: EventCallback[A]): TagMod = {
      val f: js.Function1[Event, Unit] = e => F.prepare(eventHandler(e))()
      :=(f)(ValueType.direct)
    }

    def -->?[O[_], A](callback: => O[A])(implicit F: EventCallback[A], O: OptionLike[O]): TagMod =
      this --> DefaultEffects.Sync.delay(F.runOption(callback))

    def ==>?[O[_], A](eventHandler: Event => O[A])(implicit F: EventCallback[A], O: OptionLike[O]): TagMod =
      ==>(e => DefaultEffects.Sync.delay(F.runOption(eventHandler(e))))
  }

  object Event {

    def apply[E[+x <: dom.Node] <: facade.SyntheticEvent[x]](name: String): Event[E] =
      new Event(name)

    @inline def animation  (name: String) = apply[facade.SyntheticAnimationEvent  ](name)
    @inline def base       (name: String) = apply[facade.SyntheticEvent           ](name)
    @inline def clipboard  (name: String) = apply[facade.SyntheticClipboardEvent  ](name)
    @inline def composition(name: String) = apply[facade.SyntheticCompositionEvent](name)
    @inline def drag       (name: String) = apply[facade.SyntheticDragEvent       ](name)
    @inline def focus      (name: String) = apply[facade.SyntheticFocusEvent      ](name)
    @inline def form       (name: String) = apply[facade.SyntheticFormEvent       ](name)
    @inline def keyboard   (name: String) = apply[facade.SyntheticKeyboardEvent   ](name)
    @inline def mouse      (name: String) = apply[facade.SyntheticMouseEvent      ](name)
    @inline def pointer    (name: String) = apply[facade.SyntheticPointerEvent    ](name)
    @inline def touch      (name: String) = apply[facade.SyntheticTouchEvent      ](name)
    @inline def transition (name: String) = apply[facade.SyntheticTransitionEvent ](name)
    @inline def ui         (name: String) = apply[facade.SyntheticUIEvent         ](name)
    @inline def wheel      (name: String) = apply[facade.SyntheticWheelEvent      ](name)
  }

  // ===================================================================================================================

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

  object UntypedRef extends Attr[facade.React.RefFn[TopNode]]("ref") {
    override def :=[A](a: A)(implicit t: ValueType[A, facade.React.RefFn[TopNode]]) =
      t(attrName, a)

    def apply(f: (TopNode | Null) => Unit): TagMod = {
      val jsFn: facade.React.RefFn[TopNode] = f
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

    def apply[A, U](fn: Fn[A]): ValueType[A, U] =
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

    implicit lazy val untypedRef: ValueType[japgolly.scalajs.react.Ref.Set[_ <: TopNode], facade.React.RefFn[TopNode]] =
      apply((f, a) => f(a.rawSetFn))

//    def array[A](implicit f: A => js.Any): Simple[js.Array[A]] =
//      map(_ map f)
//
//    def optional[T[_], A](ot: OptionLike[T], vt: ValueType[A, Any]): Simple[T[A]] =
//      apply((b, ta) => ot.foreach(ta)(vt.fn(b, _)))
  }
}
