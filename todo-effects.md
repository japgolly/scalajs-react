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

* Merge Effect and SafeEffect
  * Maybe rename
    * {Effect => UnsafeEffect}
    * {SafeEffect => Effect}
    * UnsafeEffect >: Effect
    * UnsafeEffect contains implicit Id

---------------------------------------------------------------------------------------------------------

```scala

  implicit object callback extends Sync[CallbackTo] {
    @inline override def syncRun[A](f: => CallbackTo[A]): A =
      f.runNow()

    override def syncOption_[A](f: => Option[CallbackTo[A]]): Callback =
      Callback(f.foreach(_.runNow()))
  }

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

```