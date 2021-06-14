* Move into core as extensions:
  * CallbackOption
    * keyCodeSwitch
    * keyEventSwitch
    * asEventDefault
  * CallbackTo
    * asEventDefault

* Extend in core:
  * DomUtil

* userdefined.(Unsafe)Effect?

* Document modules

* Remove temp scripts

* Add WithAsyncEffect / WithEffectAsync to component.*

* Add WithEffect/WithAsyncEffect or support more than just the default effect type
  * DefaultReusabilityOverlay
  * Hooks.scala
  * Router
  * RouterCtl
  * RouterWithPropsConfig.scala
  * RoutingRule
  * RoutingRules
  * StateSnapshot (*Pure)
  * TriStateCheckbox

* Test React.Suspense

---------------------------------------------------------------------------------------------------------

```scala


  implicit val callbackInstance: Effect[CallbackTo] = new Effect[CallbackTo] {
    override def point     [A]   (a: => A)                                 = CallbackTo(a)
    override def pure      [A]   (a: A)                                    = CallbackTo.pure(a)
    override def map       [A, B](a: CallbackTo[A])(f: A => B)             = a map f
    override def flatMap   [A, B](a: CallbackTo[A])(f: A => CallbackTo[B]) = a flatMap f
    override def extract   [A]   (a: => CallbackTo[A])                     = a.toScalaFn
  }

    implicit lazy val endoCallback: Id[CallbackTo]               = Trans.id[CallbackTo]
    implicit lazy val idToCallback: Trans[Effect.Id, CallbackTo] = Trans[Effect.Id, CallbackTo]
    implicit lazy val callbackToId: Trans[CallbackTo, Effect.Id] = Trans[CallbackTo, Effect.Id]

    final protected def async(f: Untyped => F[Unit]): A[Unit] =
      AsyncCallback.viaCallback(cb => F.toCallback(f(cb)))

// Reusability
  def callbackByRef[A]: Reusability[CallbackTo[A]] =
    by((_: CallbackTo[A]).underlyingRepr)(byRef)

  def callbackOptionByRef[A]: Reusability[CallbackOption[A]] =
    by((_: CallbackOption[A]).underlyingRepr)(byRef)

  def callbackKleisliByRef[A, B]: Reusability[CallbackKleisli[A, B]] =
    by((_: CallbackKleisli[A, B]).underlyingRepr)(byRef)

  def asyncCallbackByRef[A]: Reusability[AsyncCallback[A]] =
    by((_: AsyncCallback[A]).underlyingRepr)(byRef)

  lazy val emptyCallback: Reusable[Callback] =
    callbackByRef(Callback.empty)

  implicit lazy val callbackSetIntervalResult: Reusability[Callback.SetIntervalResult] =
    byRef || by(_.handle)

  implicit lazy val callbackSetTimeoutResult: Reusability[Callback.SetTimeoutResult] =
    byRef || by(_.handle)


// Reusable
  def callbackByRef[A](c: CallbackTo[A]): Reusable[CallbackTo[A]] =
    byRefIso(c)(_.underlyingRepr)

  def callbackOptionByRef[A](c: CallbackOption[A]): Reusable[CallbackOption[A]] =
    byRefIso(c)(_.underlyingRepr)

  def callbackKleisliByRef[A, B](c: CallbackKleisli[A, B]): Reusable[CallbackKleisli[A, B]] =
    byRefIso(c)(_.underlyingRepr)

  def asyncCallbackByRef[A](c: AsyncCallback[A]): Reusable[AsyncCallback[A]] =
    byRefIso(c)(_.underlyingRepr)


```