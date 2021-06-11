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

---------------------------------------------------------------------------------------------------------

```scala

  implicit val callback: Semigroup[Callback] =
    Semigroup(_ >> _)

  val eitherCB: Semigroup[CallbackTo[Boolean]] =
    Semigroup(_ || _)


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

```