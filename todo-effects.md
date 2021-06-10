* Move into core as extensions:
  * CallbackOption
    * keyCodeSwitch
    * keyEventSwitch
    * asEventDefault
  * CallbackTo
    * asEventDefault

* Extend in core:
  * DomUtil

* Document modules

* Merge Effect and SafeEffect
  * Maybe rename
    * {Effect => UnsafeEffect}
    * {SafeEffect => Effect}
    * UnsafeEffect >: Effect
    * UnsafeEffect contains implicit Id



  implicit object callback extends Sync[CallbackTo] {
    @inline override def syncRun[A](f: => CallbackTo[A]): A =
      f.runNow()

    override def syncOption_[A](f: => Option[CallbackTo[A]]): Callback =
      Callback(f.foreach(_.runNow()))
  }
