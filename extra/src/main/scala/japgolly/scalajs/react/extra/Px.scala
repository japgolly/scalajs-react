package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.internal.{LazyVar, PxMacros}

/**
 * A mechanism for caching data with dependencies.
 *
 * This is basically a performance-focused, lightweight implementation of pull-based
 * <a href="http://en.wikipedia.org/wiki/Functional_reactive_programming">FRP</a>,
 * pull-based meaning that in the chain A→B→C, an update to A doesn't affect C until the value of C is requested.
 *
 * What does Px mean? I don't know, I just needed a name and I liked the way @lihaoyi's Rx type name looked in code.
 * You can consider this "Performance eXtension". If this were Java it'd be named
 * `AutoRefreshOnRequestDependentCachedVariable`.
 */
sealed abstract class Px[A] {

  /** Revision of its value. Increments when value changes. */
  def rev: Int

  /** Get the latest value, updating itself if necessary. */
  def value(): A

  /** Get the last used value without updating. */
  def peek: A

  final def valueSince(r: Int): Option[A] = {
    val v = value() // Ensure value and rev are up-to-date
    if (rev != r)
      Some(v)
    else
      None
  }

  final def toCallback: CallbackTo[A] =
    CallbackTo(value())

  def map[B](f: A => B): Px.Derivative[B] =
    new Px.Map(this, f)

  def flatMap[B](f: A => Px[B]): Px.Derivative[B] =
    new Px.FlatMap(this, f)

  /**
   * If this Px contains a function, it can be extracted and the Px dropped from the signature. Every time the function
   * is invoked it will use the latest value of this `Px`, even if you don't explicitly hold a reference to it anymore.
   *
   * Example. From a `Px[Int => String]`, an `Int => String` can be extracted.
   */
  def extract: A =
    macro PxMacros.extract[A]

  // override def toString = value().toString
}

object Px {
  sealed abstract class Root[A](__initValue: () => A) extends Px[A] {
    protected val reusability: Reusability[A]

    private val __value = new LazyVar(__initValue)

    protected final def _updateValue(a: A): Unit =
      __value set a

    protected final def _value(): A =
      __value.get()

    protected var _rev = 0

    override final def rev  = _rev
    override final def peek = _value()

    protected def setMaybe(a: A): Unit =
      if (!reusability.test(_value(), a)) {
        _rev += 1
        _updateValue(a)
      }
  }

  /**
   * A variable in the traditional sense.
   *
   * Doesn't change until you explicitly call `set()`.
   */
  final class Var[A](initialValue: A, protected val reusability: Reusability[A]) extends Root[A](() => initialValue) {
    override def toString = s"Px.Var(rev: $rev, value: $peek)"

    override def value() = _value()

    def set(a: A): Unit =
      setMaybe(a)
  }

  /**
   * The value of a zero-param function.
   *
   * The `M` in `ThunkM` denotes "Manual refresh", meaning that the value will not update until you explicitly call
   * `refresh()`.
   */
  final class ThunkM[A](next: () => A, protected val reusability: Reusability[A]) extends Root[A](next) {
    override def toString = s"Px.ThunkM(rev: $rev, value: $peek)"

    override def value() = _value()

    def refresh(): Unit =
      setMaybe(next())
  }

  /**
   * The value of a zero-param function.
   *
   * The `A` in `ThunkA` denotes "Auto refresh", meaning that the function will be called every time the value is
   * requested, and the value updated if necessary.
   */
  private final class ThunkA[A](next: () => A, protected val reusability: Reusability[A]) extends Root[A](next) {
    override def toString = s"Px.ThunkA(rev: $rev, value: $peek)"

    override def value() = {
      setMaybe(next())
      _value()
    }
  }

  sealed abstract class Derivative[A] extends Px[A] {

    /**
     * In addition to updating when the underlying `Px` changes, this will also check its own result and halt updates
     * if reusable.
     */
    final def withReuse(implicit ev: Reusability[A]): Px[A] =
      Px(value()).withReuse(ev).autoRefresh
  }

  sealed abstract class DerivativeBase[A, B, C](xa: Px[A], derive: A => B) extends Derivative[C] {
    protected type ValRev = (B, Int)

    private val __value = new LazyVar[ValRev](() => {
      val b = derive(xa.value())
      (b, xa.rev)
    })

    private final def __updateValue(b: B): ValRev = {
      val vr = (b, xa.rev)
      __value set vr
      vr
    }

    protected final def _init(): ValRev = __value.get()
    protected final def _value(): B     = _init()._1
    protected final def _revA(): Int    = _init()._2

    protected final def _updateValueIfChanged(): Unit =
      xa.valueSince(_revA()).foreach { a =>
        __updateValue(derive(a))
      }
  }

  /**
   * A value `B` dependent on the value of some `Px[A]`.
   */
  final class Map[A, B](xa: Px[A], f: A => B) extends DerivativeBase[A, B, B](xa, f) {
    override def toString = s"Px.Map(rev: $rev, value: $peek)"

    override def rev  = _revA()
    override def peek = _value()

    override def map[C](g: B => C) =
      new Map(xa, g compose f)

    override def flatMap[C](g: B => Px[C]) =
      new Px.FlatMap(xa, g compose f)

    override def value(): B = {
      _updateValueIfChanged()
      _value()
    }
  }

  /**
   * A `Px[B]` dependent on the value of some `Px[A]`.
   */
  final class FlatMap[A, B](xa: Px[A], f: A => Px[B]) extends DerivativeBase[A, Px[B], B](xa, f) {
    override def toString = s"Px.FlatMap(rev: $rev, value: $peek)"

    override def peek = _value().peek
    override def rev  = {
      val vr = _init()
      vr._1.rev + vr._2
    }

    override def value(): B = {
      _updateValueIfChanged()
      _value().value()
    }
  }

  private final class ConstByValue[A](a: A) extends Px[A] {
    override def toString = s"Px.constByValue($a)"

    override def rev     = 0
    override def peek    = a
    override def value() = a
  }

  private final class ConstByNeed[A](a: => A) extends Px[A] {
    override def toString = s"Px.constByNeed(${if (available) value() else "…"})"
    private[this] var available = false

    override def      rev     = 0
    override lazy val peek    = {val x = a; available = true; x}
    override def      value() = peek
  }

  // ===================================================================================================================

  /** Import this to avoid the need to call `.value()` on your `Px`s. */
  object AutoValue {
    implicit def autoPxValue[A](x: Px[A]): A = x.value()
  }

  /** Refresh multiple [[ThunkM]]s at once. */
  def refresh(xs: ThunkM[_]*): Unit =
    xs.foreach(_.refresh())

  // ===================================================================================================================

  def constByValue[A](a: A): Px[A] =
    new ConstByValue(a)

  def constByNeed[A](a: => A): Px[A] =
    new ConstByNeed(a)

  def apply[A](f: => A): FromThunk[A] =
    new FromThunk(() => f)

  def callback[A](cb: CallbackTo[A]): FromThunk[A] =
    new FromThunk(cb.toScalaFn)

  def props[P](s: GenericComponent.MountedPure[P, _]): FromThunk[P] =
    callback(s.props)

  def state[I, S](i: I)(implicit sa: StateAccessor.ReadPure[I, S]): FromThunk[S] =
    callback(sa.state(i))

  final class FromThunk[A](private val thunk: () => A) extends AnyVal {
    def map[B](f: A => B): FromThunk[B] =
      new FromThunk(() => f(thunk()))

    def withReuse(implicit r: Reusability[A]): FromThunkReusability[A] =
      new FromThunkReusability(thunk, r)

    def withoutReuse: FromThunkReusability[A] =
      new FromThunkReusability(thunk, Reusability.never)
  }

  final class FromThunkReusability[A](thunk: () => A, reusability: Reusability[A]) {

    /** Every time [[japgolly.scalajs.react.extra.Px.value()]] is called, the underlying data function is re-evaluated.
      * If a non-reusable change is detected, the value is replaced.
      */
    def autoRefresh: Px[A] =
      new ThunkA(thunk, reusability)

    /** The underlying data function will only be re-evaluated and checked for non-reusable change when
      * [[japgolly.scalajs.react.extra.Px.ThunkM.refresh()]] is called.
      *
      * [[japgolly.scalajs.react.extra.Px.refresh()]] also exists as a convenience to refresh multiple instances at once.
      */
    def manualRefresh: ThunkM[A] =
      new ThunkM(thunk, reusability)

    /** The value is never updated until [[japgolly.scalajs.react.extra.Px.Var.set()]] is called to specify a new value.
      */
    def manualUpdate: Var[A] =
      new Var(thunk(), reusability)
  }

  // Generated by bin/gen-px

  def apply2[A,B,Z](pa:Px[A], pb:Px[B])(z:(A,B)=>Z): Px[Z] =
    for {a<-pa;b<-pb} yield z(a,b)

  def apply3[A,B,C,Z](pa:Px[A], pb:Px[B], pc:Px[C])(z:(A,B,C)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc} yield z(a,b,c)

  def apply4[A,B,C,D,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D])(z:(A,B,C,D)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd} yield z(a,b,c,d)

  def apply5[A,B,C,D,E,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E])(z:(A,B,C,D,E)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe} yield z(a,b,c,d,e)

  def apply6[A,B,C,D,E,F,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F])(z:(A,B,C,D,E,F)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf} yield z(a,b,c,d,e,f)

  def apply7[A,B,C,D,E,F,G,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G])(z:(A,B,C,D,E,F,G)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg} yield z(a,b,c,d,e,f,g)

  def apply8[A,B,C,D,E,F,G,H,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H])(z:(A,B,C,D,E,F,G,H)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph} yield z(a,b,c,d,e,f,g,h)

  def apply9[A,B,C,D,E,F,G,H,I,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I])(z:(A,B,C,D,E,F,G,H,I)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi} yield z(a,b,c,d,e,f,g,h,i)

  def apply10[A,B,C,D,E,F,G,H,I,J,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J])(z:(A,B,C,D,E,F,G,H,I,J)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj} yield z(a,b,c,d,e,f,g,h,i,j)

  def apply11[A,B,C,D,E,F,G,H,I,J,K,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J], pk:Px[K])(z:(A,B,C,D,E,F,G,H,I,J,K)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj;k<-pk} yield z(a,b,c,d,e,f,g,h,i,j,k)

  def apply12[A,B,C,D,E,F,G,H,I,J,K,L,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J], pk:Px[K], pl:Px[L])(z:(A,B,C,D,E,F,G,H,I,J,K,L)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj;k<-pk;l<-pl} yield z(a,b,c,d,e,f,g,h,i,j,k,l)

  def apply13[A,B,C,D,E,F,G,H,I,J,K,L,M,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J], pk:Px[K], pl:Px[L], pm:Px[M])(z:(A,B,C,D,E,F,G,H,I,J,K,L,M)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj;k<-pk;l<-pl;m<-pm} yield z(a,b,c,d,e,f,g,h,i,j,k,l,m)

  def apply14[A,B,C,D,E,F,G,H,I,J,K,L,M,N,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J], pk:Px[K], pl:Px[L], pm:Px[M], pn:Px[N])(z:(A,B,C,D,E,F,G,H,I,J,K,L,M,N)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj;k<-pk;l<-pl;m<-pm;n<-pn} yield z(a,b,c,d,e,f,g,h,i,j,k,l,m,n)

  def apply15[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J], pk:Px[K], pl:Px[L], pm:Px[M], pn:Px[N], po:Px[O])(z:(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj;k<-pk;l<-pl;m<-pm;n<-pn;o<-po} yield z(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o)

  def apply16[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J], pk:Px[K], pl:Px[L], pm:Px[M], pn:Px[N], po:Px[O], pp:Px[P])(z:(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj;k<-pk;l<-pl;m<-pm;n<-pn;o<-po;p<-pp} yield z(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p)

  def apply17[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J], pk:Px[K], pl:Px[L], pm:Px[M], pn:Px[N], po:Px[O], pp:Px[P], pq:Px[Q])(z:(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj;k<-pk;l<-pl;m<-pm;n<-pn;o<-po;p<-pp;q<-pq} yield z(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q)

  def apply18[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J], pk:Px[K], pl:Px[L], pm:Px[M], pn:Px[N], po:Px[O], pp:Px[P], pq:Px[Q], pr:Px[R])(z:(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj;k<-pk;l<-pl;m<-pm;n<-pn;o<-po;p<-pp;q<-pq;r<-pr} yield z(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r)

  def apply19[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J], pk:Px[K], pl:Px[L], pm:Px[M], pn:Px[N], po:Px[O], pp:Px[P], pq:Px[Q], pr:Px[R], ps:Px[S])(z:(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj;k<-pk;l<-pl;m<-pm;n<-pn;o<-po;p<-pp;q<-pq;r<-pr;s<-ps} yield z(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s)

  def apply20[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J], pk:Px[K], pl:Px[L], pm:Px[M], pn:Px[N], po:Px[O], pp:Px[P], pq:Px[Q], pr:Px[R], ps:Px[S], pt:Px[T])(z:(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj;k<-pk;l<-pl;m<-pm;n<-pn;o<-po;p<-pp;q<-pq;r<-pr;s<-ps;t<-pt} yield z(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t)

  def apply21[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J], pk:Px[K], pl:Px[L], pm:Px[M], pn:Px[N], po:Px[O], pp:Px[P], pq:Px[Q], pr:Px[R], ps:Px[S], pt:Px[T], pu:Px[U])(z:(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj;k<-pk;l<-pl;m<-pm;n<-pn;o<-po;p<-pp;q<-pq;r<-pr;s<-ps;t<-pt;u<-pu} yield z(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u)

  def apply22[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,Z](pa:Px[A], pb:Px[B], pc:Px[C], pd:Px[D], pe:Px[E], pf:Px[F], pg:Px[G], ph:Px[H], pi:Px[I], pj:Px[J], pk:Px[K], pl:Px[L], pm:Px[M], pn:Px[N], po:Px[O], pp:Px[P], pq:Px[Q], pr:Px[R], ps:Px[S], pt:Px[T], pu:Px[U], pv:Px[V])(z:(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V)=>Z): Px[Z] =
    for {a<-pa;b<-pb;c<-pc;d<-pd;e<-pe;f<-pf;g<-pg;h<-ph;i<-pi;j<-pj;k<-pk;l<-pl;m<-pm;n<-pn;o<-po;p<-pp;q<-pq;r<-pr;s<-ps;t<-pt;u<-pu;v<-pv} yield z(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v)

  // ===================================================================================================================

  trait ManualCollection {
    def add[A](px: Px.ThunkM[A]): px.type
    val refreshCB: Callback

    final def addAll(pxs: Px.ThunkM[_]*): Unit =
      pxs.foreach(add(_))

    final def refresh(): Unit =
      refreshCB.runNow()
  }

  object ManualCollection {
    def apply(initial: Px.ThunkM[_]*): ManualCollection =
      new ManualCollection {

        var pxs = initial.toList

        override def add[A](px: Px.ThunkM[A]): px.type = {
          pxs ::= px
          px
        }

        override val refreshCB: Callback =
          Callback(Px.refresh(pxs: _*))
      }
  }

}
