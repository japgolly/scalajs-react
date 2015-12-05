package japgolly.scalajs.react

import scalaz.{Optional => _, _}
import scalaz.effect.IO
import Scalaz.Id
import Leibniz.===

private[react] object ScalazReactState {
  import ScalazReact._

  final class ScalazReactStateOps[S, W[_]](private val $: CompState.Access[S], W: CallbackTo ~> W) {
    @inline private implicit def autoToW[A](cb: CallbackTo[A]): W[A] = W(cb)

    private def run[M[_], A, B](st: => ReactST[M, S, A], f: (S, S, A, => Callback) => CallbackTo[B])(implicit M: M ~> CallbackTo, N: Monad[M]): CallbackTo[B] =
      $.state |> (StateAndCallbacks(_)) >>= (s1 =>
        M(st run s1) >>= { x2 =>
          val s2 = x2._1
          val a  = x2._2
          f(s1.state, s2.state, a, $.setState(s2.state, s2.cb))
        }
      )

    def runState[M[_], A](st: => ReactST[M, S, A])(implicit M: M ~> CallbackTo, N: Monad[M]): W[A] =
      run[M, A, A](st, (s1,s2,a,cb) => cb.map(_ => a))

    def _runState[I, M[_], A](f: I => ReactST[M, S, A])(implicit M: M ~> CallbackTo, N: Monad[M]): I => W[A] =
      i => runState(f(i))

    def _runState[I, M[_], A](f: I => ReactST[M, S, A], cb: I => Callback)(implicit M: M ~> CallbackTo, N: Monad[M]): I => W[A] =
      i => runState(f(i) addCallback cb(i))

    def runStateF[M[_], A](st: => ReactST[M, S, A])(implicit M: M ~> CallbackTo, N: Monad[M], F: ChangeFilter[S]): W[A] =
      run[M, A, A](st, (s1,s2,a,cb) => F(s1, s2, CallbackTo pure a, _ => cb.map(_ => a)))

    def _runStateF[I, M[_], A](f: I => ReactST[M, S, A])(implicit M: M ~> CallbackTo, N: Monad[M], F: ChangeFilter[S]): I => W[A] =
      i => runStateF(f(i))

    def _runStateF[I, M[_], A](f: I => ReactST[M, S, A], cb: I => Callback)(implicit M: M ~> CallbackTo, N: Monad[M], F: ChangeFilter[S]): I => W[A] =
      i => runStateF(f(i) addCallback cb(i))

    def modStateF(f: S => S, cb: Callback = Callback.empty)(implicit F: ChangeFilter[S]): W[Unit] =
      $.state >>= (s1 =>
        F(s1, f(s1), Callback.empty, $.setState(_, cb)))

    @inline def _modStateF[I](f: I => S => S, cb: Callback = Callback.empty)(implicit F: ChangeFilter[S]): I => W[Unit] =
      i => modStateF(f(i), cb)
  }

  final class SzRExt_StateTOps[M[_], S, A](private val s: StateT[M, S, A]) extends AnyVal {
    @inline def liftS(implicit M: Monad[M]): ReactST[M, S, A] = ReactS.liftS(s)
  }

  final class SzRExt__StateTOps[I, M[_], S, A](private val f: I => StateT[M, S, A]) extends AnyVal {
    @inline def liftS(implicit M: Monad[M]): I => ReactST[M, S, A] =
      i => new SzRExt_StateTOps(f(i)).liftS
  }

  final class SzRExt_ReactSOps[S, A](private val s: ReactS[S,A]) extends AnyVal {
    // Very common case. Very sick of seeing it highlighted red everywhere in Intellij.
    @inline def liftIO: ReactST[IO, S, A] = s.lift[IO]
    @inline def liftCB: ReactST[CallbackTo, S, A] = s.lift[CallbackTo]
  }

  final class SzRExt_ReactSTOps[M[_], S, A](private val s: ReactST[M,S,A]) extends AnyVal {
    def addCallback(c: Callback)(implicit M: Monad[M]): ReactST[M,S,A] =
      s.flatMap(ReactS.callbackT(_, c))

    def addCallbackS(c: S => Callback)(implicit M: Monad[M]): ReactST[M,S,A] =
      s.flatMap(ReactS.callbacksT(_, c))

    // This shouldn't be needed; it's already in BindSyntax.
    def >>[B](t: => ReactST[M,S,B])(implicit M: Monad[M]): ReactST[M,S,B] =
      s.flatMap(_ => t)

    /** zoom2 because StateT.zoom already exists. 2 because it takes two fns. */
    def zoom2[T](f: T => S, g: (T, S) => T)(implicit M: Monad[M]): ReactST[M, T, A] =
      ReactS.zoom(s, f, g)

    def zoomU[T](implicit M: Monad[M], ev: S === Unit): ReactST[M, T, A] =
      ReactS.zoomU[M, T, A](ev.subst[({type λ[σ] = ReactST[M, σ, A]})#λ](s))
  }
}

// =====================================================================================================================

trait ScalazReactState {

  @inline implicit def ScalazReactStateOpsCB[$, S]($: $)(implicit ops: $ => CompState.WriteAccess[S]) =
    new ScalazReactState.ScalazReactStateOps[S, CallbackTo](ops($).accessCB, ScalazReact.callbackToItself)

  @inline implicit def ScalazReactStateOpsDirect[$, S]($: $)(implicit ops: $ => CompState.AccessD[S]) =
    new ScalazReactState.ScalazReactStateOps[S, Id](ops($).accessCB, ScalazReact.scalazIdToCallbackIso.to)

  @inline def StateAndCallbacks[S](s: S, cb: Callback = Callback.empty) =
    new StateAndCallbacks[S](s, cb)

  final class StateAndCallbacks[S](val state: S, val cb: Callback) {
    @inline def withState(s2: S) = new StateAndCallbacks(s2, cb)
    def addCallback(cb2: Callback) = new StateAndCallbacks(state, appendCallbacks(cb, cb2))
    override def toString = s"StateAndCallbacks($state, $cb)"
  }

  private[this] def appendCallbacks(a: Callback, b: Callback): Callback =
    if (a.isEmpty_?)
      b
    else if (b.isEmpty_?)
      a
    else
      a >> b

  final type ReactS[S, A] = ReactST[Id, S, A]
  final type ReactST[M[_], S, A] = StateT[M, StateAndCallbacks[S], A]

  /**
   * M prefix indicates M in args.
   * T prefix indicates we want the result lifted into an M.
   */
  object ReactS {
    @inline def apply    [S,A](f: S => (S, A))        : ReactS[S,A]    = applyM[Id, S, A](f)
    @inline def callback [S,A](a: A, c: Callback)     : ReactS[S,A]    = callbackM[Id, S, A](a, c)
    @inline def callbacks[S,A](a: A, c: S => Callback): ReactS[S,A]    = callbacksM[Id, S, A](a, c)
    @inline def get      [S]                          : ReactS[S,S]    = gets(identity[S])
    @inline def mod      [S]  (f: S => S)             : ReactS[S,Unit] = modM[Id, S](f)
    @inline def ret      [S,A](a: A)                  : ReactS[S,A]    = retM[Id, S, A](a)
    @inline def set      [S]  (s: S)                  : ReactS[S,Unit] = mod((_: S) => s)

    @inline def applyT    [M[_],S,A](f: S => (S, A))         (implicit M: Monad      [M]): ReactST[M,S,A]    = applyM(s ⇒ M point f(s))
    @inline def callbackM [M[_],S,A](ma: M[A], c: Callback)  (implicit M: Monad      [M]): ReactST[M,S,A]    = callbacksM(ma, _ => c)
    @inline def callbackT [M[_],S,A](a: A, c: Callback)      (implicit M: Monad      [M]): ReactST[M,S,A]    = callbackM(M point a, c)
    @inline def callbacksT[M[_],S,A](a: A, c: S => Callback) (implicit M: Monad      [M]): ReactST[M,S,A]    = callbacksM(M point a, c)
    @inline def getT      [M[_],S]                           (implicit M: Applicative[M]): ReactST[M,S,S]    = get.lift[M]
    @inline def getsT     [M[_],S,A](f: S => A)              (implicit M: Monad      [M]): ReactST[M,S,A]    = getsM(s ⇒ M point f(s))
    @inline def liftR     [M[_],S,A](f: S ⇒ ReactST[M, S, A])(implicit M: Monad      [M]): ReactST[M,S,A]    = getT[M,S] flatMap f
    @inline def modT      [M[_],S]  (f: S => S)              (implicit M: Monad      [M]): ReactST[M,S,Unit] = modM(s ⇒ M point f(s))
    @inline def retM      [M[_],S,A](ma: M[A])               (implicit M: Monad      [M]): ReactST[M,S,A]    = getsM[M,S,A](_ ⇒ ma)
    @inline def setM      [M[_],S]  (ms: M[S])               (implicit M: Monad      [M]): ReactST[M,S,Unit] = modM((_: S) ⇒ ms)
    @inline def setT      [M[_],S]  (s: S)                   (implicit M: Monad      [M]): ReactST[M,S,Unit] = setM(M point s)

    def applyM[M[_], S, A](f: S => M[(S, A)])(implicit F: Monad[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](sc => F.map(f(sc.state))(x => (sc withState x._1, x._2) ))

    def callbacksM[M[_], S, A](ma: M[A], c: S => Callback)(implicit M: Monad[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](s => M.map(ma)(a => (s addCallback c(s.state), a)))

    @inline def gets[S, A](f: S => A): ReactS[S, A] =
      State.gets[StateAndCallbacks[S], A](s => f(s.state))

    def getsM[M[_], S, A](f: S => M[A])(implicit F: Monad[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](sc => F.map(f(sc.state))((sc, _)))

    def liftS[M[_], S, A](t: StateT[M, S, A])(implicit M: Monad[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](sc => M.map(t(sc.state))(sa => (sc withState sa._1, sa._2) ))

    def modM[M[_], S](f: S => M[S])(implicit M: Monad[M]): ReactST[M, S, Unit] =
      StateT[M, StateAndCallbacks[S], Unit](sc => M.map(f(sc.state))(s2 => (sc withState s2,()) ))

    @inline def retT[M[_], S, A](a: A)(implicit M: Monad[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](s => M.point((s, a)))

    def unlift[M[_], S, A](t: ReactST[M, S, A])(implicit M: Monad[M]): StateT[M, S, A] =
      StateT[M, S, A](s => M.map(t(StateAndCallbacks(s)))(sa => (sa._1.state, sa._2) ))

    def zoom[M[_], S, T, A](r: ReactST[M, S, A], f: T => S, g: (T, S) => T)(implicit M: Monad[M]): ReactST[M, T, A] =
      StateT[M, StateAndCallbacks[T], A](tc => {
        val m = r(StateAndCallbacks(f(tc.state), tc.cb))
        M.map(m){ case (sc, a) => (StateAndCallbacks(g(tc.state, sc.state), sc.cb), a) }
      })

    @inline def zoomU[M[_], S, A](r: ReactST[M, Unit, A])(implicit M: Monad[M]): ReactST[M, S, A] =
      zoom[M, Unit, S, A](r, _ => (), (s, _) => s)

    /**
     * M prefix indicates M in args.
     * T prefix indicates we want the result lifted into an M.
     */
    @inline def Fix[S] = new Fix[S]
    final class Fix[S] {
      type T[A] = ReactS[S, A]

      @inline def nop :        ReactS[S,Unit] = ret(())
      @inline def _nop: Any => ReactS[S,Unit] = _ => nop

      @inline def apply    [A]     (f: S => (S, A))           : ReactS[S,A]    = ReactS(f)
      @inline def callback [A]     (a: A, c: Callback)        : ReactS[S,A]    = ReactS.callback(a, c)
      @inline def callbacks[A]     (a: A, c: S => Callback)   : ReactS[S,A]    = ReactS.callbacks(a, c)
      @inline def get                                         : ReactS[S,S]    = ReactS.get
      @inline def gets     [A]     (f: S => A)                : ReactS[S,A]    = ReactS.gets(f)
      @inline def mod              (f: S => S)                : ReactS[S,Unit] = ReactS.mod(f)
      @inline def ret      [A]     (a: A)                     : ReactS[S,A]    = ReactS.ret(a)
      @inline def set              (s: S)                     : ReactS[S,Unit] = ReactS.set(s)
      @inline def zoom     [T,A]   (r: ReactS[T, A])
                                   (f: S => T, g: (S, T) => S): ReactS[S,A]    = ReactS.zoom(r, f, g)
      @inline def zoomU    [A]     (r: ReactS[Unit, A])       : ReactS[S,A]    = ReactS.zoomU(r)

      @inline def applyM    [M[_],A](f: S => M[(S, A)])        (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.applyM(f)
      @inline def applyT    [M[_],A](f: S => (S, A))           (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.applyT(f)
      @inline def callbackM [M[_],A](a: M[A], c: Callback)     (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbackM(a, c)
      @inline def callbackT [M[_],A](a: A, c: Callback)        (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbackT(a, c)
      @inline def callbacksM[M[_],A](a: M[A], c: S => Callback)(implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbacksM(a, c)
      @inline def callbacksT[M[_],A](a: A, c: S => Callback)   (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbacksT(a, c)
      @inline def getT      [M[_]]                             (implicit M: Applicative[M]): ReactST[M,S,S]    = ReactS.getT
      @inline def getsM     [M[_],A](f: S => M[A])             (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.getsM(f)
      @inline def getsT     [M[_],A](f: S => A)                (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.getsT(f)
      @inline def liftR     [M[_],A](f: S => ReactST[M, S, A]) (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.liftR(f)
      @inline def liftS     [M[_],A](t: StateT[M, S, A])       (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.liftS(t)
      @inline def modM      [M[_]]  (f: S => M[S])             (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.modM(f)
      @inline def modT      [M[_]]  (f: S => S)                (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.modT(f)
      @inline def retM      [M[_],A](ma: M[A])                 (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.retM(ma)
      @inline def retT      [M[_],A](a: A)                     (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.retT(a)
      @inline def setM      [M[_]]  (ms: M[S])                 (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.setM(ms)
      @inline def setT      [M[_]]  (s: S)                     (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.setT(s)
      @inline def unlift    [M[_],A](t: ReactST[M, S, A])      (implicit M: Monad      [M]): StateT [M,S,A]    = ReactS.unlift(t)
    }

    /**
     * T prefix indicates we want the result lifted into an M.
     */
    @inline def FixT[M[_], S] = new FixT[M, S]
    @inline def FixCB[S] = new FixT[CallbackTo, S]
    final class FixT[M[_], S] {
      type T[A] = ReactST[M, S, A]

      @inline def nop (implicit M: Monad[M]):        ReactST[M,S,Unit] = retT(())
      @inline def _nop(implicit M: Monad[M]): Any => ReactST[M,S,Unit] = _ => nop

      @inline def apply     [A]  (f: S => M[(S, A)])        (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.applyM(f)
      @inline def applyT    [A]  (f: S => (S, A))           (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.applyT(f)
      @inline def callback  [A]  (a: M[A], c: Callback)     (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbackM(a, c)
      @inline def callbackT [A]  (a: A, c: Callback)        (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbackT(a, c)
      @inline def callbacks [A]  (a: M[A], c: S => Callback)(implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbacksM(a, c)
      @inline def callbacksT[A]  (a: A, c: S => Callback)   (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbacksT(a, c)
      @inline def get                                       (implicit M: Applicative[M]): ReactST[M,S,S]    = ReactS.getT
      @inline def gets      [A]  (f: S => M[A])             (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.getsM(f)
      @inline def getsT     [A]  (f: S => A)                (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.getsT(f)
      @inline def liftR     [A]  (f: S => ReactST[M, S, A]) (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.liftR(f)
      @inline def liftS     [A]  (t: StateT[M, S, A])       (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.liftS(t)
      @inline def mod            (f: S => M[S])             (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.modM(f)
      @inline def modT           (f: S => S)                (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.modT(f)
      @inline def ret       [A]  (ma: M[A])                 (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.retM(ma)
      @inline def retT      [A]  (a: A)                     (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.retT(a)
      @inline def set            (ms: M[S])                 (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.setM(ms)
      @inline def setT           (s: S)                     (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.setT(s)
      @inline def unlift    [A]  (t: ReactST[M, S, A])      (implicit M: Monad      [M]): StateT [M,S,A]    = ReactS.unlift(t)
      @inline def zoom      [T,A](r: ReactST[M, T, A])
                                 (f: S => T, g: (S,T) => S) (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.zoom(r, f, g)
      @inline def zoomU     [A]  (r: ReactST[M, Unit, A])   (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.zoomU(r)
    }
  }
}