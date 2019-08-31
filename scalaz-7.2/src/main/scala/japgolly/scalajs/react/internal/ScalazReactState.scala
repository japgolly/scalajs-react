package japgolly.scalajs.react.internal

import scalaz.{Optional => _, _}
import scalaz.effect.IO
import Scalaz.Id
import Leibniz.===
import japgolly.scalajs.react._
import ScalazReact.{reactCallbackScalazInstance, ScalazReactExt_ReactST}

trait ScalazReactState1 {

  final type ReactST[M[_], S, A] = ScalazReactState.ReactST[M, S, A]
  final type ReactS[S, A] = ScalazReactState.ReactS[S, A]
  final val ReactS = ScalazReactState.ReactS

  final type ChangeFilter[S] = ScalazReactState.ChangeFilter[S]
  final val ChangeFilter = ScalazReactState.ChangeFilter

  import ScalazReactState._

  implicit final def ScalazReactExt_StateAccessId[I, S](i: I)(implicit sa: StateAccessor.ReadWriteImpure[I, S]): Ext_StateAccessRW[Effect.Id, I, S, Effect.Id] = new Ext_StateAccessRW(i)
}

trait ScalazReactState2 extends ScalazReactState1 {
  import ScalazReactState._

  implicit final def ScalazReactExt_StateAccessCB[I, S](i: I)(implicit sa: StateAccessor.ReadWritePure[I, S]): Ext_StateAccessRW[CallbackTo, I, S, CallbackTo] = new Ext_StateAccessRW(i)
}

trait ScalazReactState extends ScalazReactState2 {
  import ScalazReactState._

//  implicit final def ScalazReactExt_StateAccessRIWC[I, S](i: I)(implicit sa: StateAccessor.ReadImpureWritePure[I, S]) =
//    ScalazReactExt_StateAccessCB(i)(sa.withReadEffect)

  implicit final def ScalazReactExt_ReactS[S, A](a: ReactS[S, A]) = new Ext_ReactS(a)
  implicit final def ScalazReactExt_ReactST[M[_], S, A](a: ReactST[M, S, A]) = new Ext_ReactST(a)
  implicit final def ScalazReactExt_StateT[M[_], S, A](a: StateT[M, S, A]) = new Ext_StateT(a)
  implicit final def ScalazReactExt_FnToStateT[I, M[_], S, A](a: I => StateT[M, S, A]) = new Ext_FnToStateT(a)
}

// =====================================================================================================================

object ScalazReactState {
  import ReactS.StateAndCallbacks

  type ReactST[M[_], S, A] = StateT[M, StateAndCallbacks[S], A]
  type ReactS[S, A] = ReactST[Id, S, A]

  /**
   * M prefix indicates M in args.
   * T prefix indicates we want the result lifted into an M.
   */
  object ReactS {
    final def StateAndCallbacks[S](s: S, cb: Callback = Callback.empty) =
      new StateAndCallbacks[S](s, cb)

    final class StateAndCallbacks[S](val state: S, val cb: Callback) {
      def withState(s2: S) = new StateAndCallbacks(s2, cb)
      def addCallback(cb2: Callback) = new StateAndCallbacks(state, cb >> cb2)
      override def toString = s"StateAndCallbacks($state, $cb)"
    }

    def apply    [S,A](f: S => (S, A))        : ReactS[S,A]    = applyM[Id, S, A](f)
    def callback [S,A](a: A, c: Callback)     : ReactS[S,A]    = callbackM[Id, S, A](a, c)
    def callbacks[S,A](a: A, c: S => Callback): ReactS[S,A]    = callbacksM[Id, S, A](a, c)
    def get      [S]                          : ReactS[S,S]    = gets(identityFn[S])
    def mod      [S]  (f: S => S)             : ReactS[S,Unit] = modM[Id, S](f)
    def ret      [S,A](a: A)                  : ReactS[S,A]    = retM[Id, S, A](a)
    def set      [S]  (s: S)                  : ReactS[S,Unit] = mod((_: S) => s)

    def applyT    [M[_],S,A](f: S => (S, A))         (implicit M: Monad      [M]): ReactST[M,S,A]    = applyM(s => M point f(s))
    def callbackM [M[_],S,A](ma: M[A], c: Callback)  (implicit M: Monad      [M]): ReactST[M,S,A]    = callbacksM(ma, _ => c)
    def callbackT [M[_],S,A](a: A, c: Callback)      (implicit M: Monad      [M]): ReactST[M,S,A]    = callbackM(M point a, c)
    def callbacksT[M[_],S,A](a: A, c: S => Callback) (implicit M: Monad      [M]): ReactST[M,S,A]    = callbacksM(M point a, c)
    def getT      [M[_],S]                           (implicit M: Applicative[M]): ReactST[M,S,S]    = get.lift[M]
    def getsT     [M[_],S,A](f: S => A)              (implicit M: Monad      [M]): ReactST[M,S,A]    = getsM(s => M point f(s))
    def liftR     [M[_],S,A](f: S => ReactST[M, S, A])(implicit M: Monad      [M]): ReactST[M,S,A]    = getT[M,S] flatMap f
    def modT      [M[_],S]  (f: S => S)              (implicit M: Monad      [M]): ReactST[M,S,Unit] = modM(s => M point f(s))
    def retM      [M[_],S,A](ma: M[A])               (implicit M: Monad      [M]): ReactST[M,S,A]    = getsM[M,S,A](_ => ma)
    def setM      [M[_],S]  (ms: M[S])               (implicit M: Monad      [M]): ReactST[M,S,Unit] = modM((_: S) => ms)
    def setT      [M[_],S]  (s: S)                   (implicit M: Monad      [M]): ReactST[M,S,Unit] = setM(M point s)

    def applyM[M[_], S, A](f: S => M[(S, A)])(implicit F: Monad[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](sc => F.map(f(sc.state))(x => (sc withState x._1, x._2) ))

    def callbacksM[M[_], S, A](ma: M[A], c: S => Callback)(implicit M: Monad[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](s => M.map(ma)(a => (s addCallback c(s.state), a)))

    def gets[S, A](f: S => A): ReactS[S, A] =
      State.gets[StateAndCallbacks[S], A](s => f(s.state))

    def getsM[M[_], S, A](f: S => M[A])(implicit F: Monad[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](sc => F.map(f(sc.state))((sc, _)))

    def liftS[M[_], S, A](t: StateT[M, S, A])(implicit M: Monad[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](sc => M.map(t(sc.state))(sa => (sc withState sa._1, sa._2) ))

    def modM[M[_], S](f: S => M[S])(implicit M: Monad[M]): ReactST[M, S, Unit] =
      StateT[M, StateAndCallbacks[S], Unit](sc => M.map(f(sc.state))(s2 => (sc withState s2,()) ))

    def retT[M[_], S, A](a: A)(implicit M: Monad[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](s => M.point((s, a)))

    def unlift[M[_], S, A](t: ReactST[M, S, A])(implicit M: Monad[M]): StateT[M, S, A] =
      StateT[M, S, A](s => M.map(t(StateAndCallbacks(s)))(sa => (sa._1.state, sa._2) ))

    def zoom[M[_], S, T, A](r: ReactST[M, S, A], f: T => S, g: (T, S) => T)(implicit M: Monad[M]): ReactST[M, T, A] =
      StateT[M, StateAndCallbacks[T], A](tc => {
        val m = r(StateAndCallbacks(f(tc.state), tc.cb))
        M.map(m){ case (sc, a) => (StateAndCallbacks(g(tc.state, sc.state), sc.cb), a) }
      })

    def zoomU[M[_], S, A](r: ReactST[M, Unit, A])(implicit M: Monad[M]): ReactST[M, S, A] =
      zoom[M, Unit, S, A](r, _ => (), (s, _) => s)

    /**
     * M prefix indicates M in args.
     * T prefix indicates we want the result lifted into an M.
     */
    def Fix[S] = new Fix[S]
    final class Fix[S] {
      type T[A] = ReactS[S, A]

      def nop :        ReactS[S,Unit] = ret(())
      def _nop: Any => ReactS[S,Unit] = _ => nop

      def apply    [A]     (f: S => (S, A))           : ReactS[S,A]    = ReactS(f)
      def callback [A]     (a: A, c: Callback)        : ReactS[S,A]    = ReactS.callback(a, c)
      def callbacks[A]     (a: A, c: S => Callback)   : ReactS[S,A]    = ReactS.callbacks(a, c)
      def get                                         : ReactS[S,S]    = ReactS.get
      def gets     [A]     (f: S => A)                : ReactS[S,A]    = ReactS.gets(f)
      def mod              (f: S => S)                : ReactS[S,Unit] = ReactS.mod(f)
      def ret      [A]     (a: A)                     : ReactS[S,A]    = ReactS.ret(a)
      def set              (s: S)                     : ReactS[S,Unit] = ReactS.set(s)
      def zoom     [T,A]   (r: ReactS[T, A])
                           (f: S => T, g: (S, T) => S): ReactS[S,A]    = ReactS.zoom(r, f, g)
      def zoomU    [A]     (r: ReactS[Unit, A])       : ReactS[S,A]    = ReactS.zoomU(r)

      def applyM    [M[_],A](f: S => M[(S, A)])        (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.applyM(f)
      def applyT    [M[_],A](f: S => (S, A))           (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.applyT(f)
      def callbackM [M[_],A](a: M[A], c: Callback)     (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbackM(a, c)
      def callbackT [M[_],A](a: A, c: Callback)        (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbackT(a, c)
      def callbacksM[M[_],A](a: M[A], c: S => Callback)(implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbacksM(a, c)
      def callbacksT[M[_],A](a: A, c: S => Callback)   (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbacksT(a, c)
      def getT      [M[_]]                             (implicit M: Applicative[M]): ReactST[M,S,S]    = ReactS.getT
      def getsM     [M[_],A](f: S => M[A])             (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.getsM(f)
      def getsT     [M[_],A](f: S => A)                (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.getsT(f)
      def liftR     [M[_],A](f: S => ReactST[M, S, A]) (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.liftR(f)
      def liftS     [M[_],A](t: StateT[M, S, A])       (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.liftS(t)
      def modM      [M[_]]  (f: S => M[S])             (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.modM(f)
      def modT      [M[_]]  (f: S => S)                (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.modT(f)
      def retM      [M[_],A](ma: M[A])                 (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.retM(ma)
      def retT      [M[_],A](a: A)                     (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.retT(a)
      def setM      [M[_]]  (ms: M[S])                 (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.setM(ms)
      def setT      [M[_]]  (s: S)                     (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.setT(s)
      def unlift    [M[_],A](t: ReactST[M, S, A])      (implicit M: Monad      [M]): StateT [M,S,A]    = ReactS.unlift(t)
    }

    /**
     * T prefix indicates we want the result lifted into an M.
     */
    def FixT[M[_], S] = new FixT[M, S]
    def FixCB[S] = new FixT[CallbackTo, S]
    final class FixT[M[_], S] {
      type T[A] = ReactST[M, S, A]

      def nop (implicit M: Monad[M]):        ReactST[M,S,Unit] = retT(())
      def _nop(implicit M: Monad[M]): Any => ReactST[M,S,Unit] = _ => nop

      def apply     [A]  (f: S => M[(S, A)])        (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.applyM(f)
      def applyT    [A]  (f: S => (S, A))           (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.applyT(f)
      def callback  [A]  (a: M[A], c: Callback)     (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbackM(a, c)
      def callbackT [A]  (a: A, c: Callback)        (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbackT(a, c)
      def callbacks [A]  (a: M[A], c: S => Callback)(implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbacksM(a, c)
      def callbacksT[A]  (a: A, c: S => Callback)   (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.callbacksT(a, c)
      def get                                       (implicit M: Applicative[M]): ReactST[M,S,S]    = ReactS.getT
      def gets      [A]  (f: S => M[A])             (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.getsM(f)
      def getsT     [A]  (f: S => A)                (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.getsT(f)
      def liftR     [A]  (f: S => ReactST[M, S, A]) (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.liftR(f)
      def liftS     [A]  (t: StateT[M, S, A])       (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.liftS(t)
      def mod            (f: S => M[S])             (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.modM(f)
      def modT           (f: S => S)                (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.modT(f)
      def ret       [A]  (ma: M[A])                 (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.retM(ma)
      def retT      [A]  (a: A)                     (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.retT(a)
      def set            (ms: M[S])                 (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.setM(ms)
      def setT           (s: S)                     (implicit M: Monad      [M]): ReactST[M,S,Unit] = ReactS.setT(s)
      def unlift    [A]  (t: ReactST[M, S, A])      (implicit M: Monad      [M]): StateT [M,S,A]    = ReactS.unlift(t)
      def zoom      [T,A](r: ReactST[M, T, A])
                         (f: S => T, g: (S,T) => S) (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.zoom(r, f, g)
      def zoomU     [A]  (r: ReactST[M, Unit, A])   (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.zoomU(r)
    }
  }

  final class Ext_StateAccessRW[F[_], SI, S, Out[_]](si: SI)(implicit sa: StateAccessor.ReadWrite[SI, F, F, S], fToCb: Effect.Trans[F, CallbackTo], cbToOut: Effect.Trans[CallbackTo, Out]) {
    implicit private def autoOut[A](a: CallbackTo[A]): Out[A] = cbToOut(a)

    private def stateCB: CallbackTo[S] = fToCb(sa.state(si))

    private def run[M[_], A, B](st: => ReactST[M, S, A], conclude: (S, S, A, => Callback) => CallbackTo[B])
                               (implicit M: Monad[M], trans: M ~> CallbackTo): CallbackTo[B] =
      runM[M, A, B](st, conclude).flatMap(trans(_))

    def runState[M[_], A](st: => ReactST[M, S, A])(implicit M: M ~> CallbackTo, N: Monad[M]): Out[A] =
      run[M, A, A](st, (_, _, a, cb) => cb.map(_ => a))

    def runStateFn[I, M[_], A](f: I => ReactST[M, S, A])(implicit M: M ~> CallbackTo, N: Monad[M]): I => Out[A] =
      i => runState(f(i))

    def runStateFn[I, M[_], A](f: I => ReactST[M, S, A], cb: I => Callback)(implicit M: M ~> CallbackTo, N: Monad[M]): I => Out[A] =
      i => runState(f(i) addCallback cb(i))

    def runStateF[M[_], A](st: => ReactST[M, S, A])(implicit M: M ~> CallbackTo, N: Monad[M], F: ChangeFilter[S]): Out[A] =
      run[M, A, A](st, (s1, s2, a, cb) => F(s1, s2, CallbackTo pure a, _ => cb.map(_ => a)))

    def runStateFnF[I, M[_], A](f: I => ReactST[M, S, A])(implicit M: M ~> CallbackTo, N: Monad[M], F: ChangeFilter[S]): I => Out[A] =
      i => runStateF(f(i))

    private def runM[M[_], A, B](st: => ReactST[M, S, A], conclude: (S, S, A, => Callback) => CallbackTo[B])
                                (implicit M: Monad[M]): CallbackTo[M[B]] =
      stateCB.flatMap { s1 =>
        type SA = (StateAndCallbacks[S], A)
        val runCM: CallbackTo[M[SA]] = CallbackTo(st run ReactS.StateAndCallbacks(s1))
        runCM.flatMap { msa =>
          CallbackTo.liftTraverse[SA, B] { xa =>
            val s2: StateAndCallbacks[S] = xa._1
            val a : A                    = xa._2
            def c : Callback             = fToCb(sa(si).setState(s2.state, s2.cb))
            val cb: CallbackTo[B]        = conclude(s1, s2.state, a, c)
            cb
          }.id.map(M.map(msa))
        }
      }

    def runStateM[M[_], A](st: => ReactST[M, S, A])(implicit M: Monad[M]): Out[M[A]] =
      runM[M, A, A](st, (_, _, a, cb) => cb.map(_ => a))

    def runStateFnM[I, M[_], A](f: I => ReactST[M, S, A])(implicit M: Monad[M]): I => Out[M[A]] =
      i => runStateM[M, A](f(i))

    def modStateF(f: S => S, cb: Callback = Callback.empty)(implicit F: ChangeFilter[S]): Out[Unit] =
      stateCB.flatMap(s1 =>
        F(s1, f(s1), Callback.empty, s => fToCb(sa(si).setState(s, cb))))

    def modStateFnF[I](f: I => S => S, cb: Callback = Callback.empty)(implicit F: ChangeFilter[S]): I => Out[Unit] =
      i => modStateF(f(i), cb)
  }

  final case class ChangeFilter[S](allowChange: (S, S) => Boolean) {
    def apply[A](s1: S, s2: S, orElse: => A, change: S => A): A =
      if (allowChange(s1, s2)) change(s2) else orElse
  }
  object ChangeFilter {
    def refl[S] = apply[S](_ != _)
    def reflOn[S, T](f: S => T) = apply[S](f(_) != f(_))
    def equal[S: Equal] = apply[S]((a,b) => !implicitly[Equal[S]].equal(a,b))
    def equalOn[S, T: Equal](f: S => T) = apply[S]((a,b) => !implicitly[Equal[T]].equal(f(a),f(b)))
  }

  final class Ext_StateT[M[_], S, A](private val s: StateT[M, S, A]) extends AnyVal {
    def liftS(implicit M: Monad[M]): ReactST[M, S, A] = ReactS.liftS(s)
  }

  final class Ext_FnToStateT[I, M[_], S, A](private val f: I => StateT[M, S, A]) extends AnyVal {
    def liftS(implicit M: Monad[M]): I => ReactST[M, S, A] =
      i => ReactS.liftS(f(i))
  }

  final class Ext_ReactS[S, A](private val s: ReactS[S, A]) extends AnyVal {
    // Very common case. Very sick of seeing it highlighted red everywhere in Intellij.
    def liftIO: ReactST[IO, S, A] = s.lift[IO]
    def liftCB: ReactST[CallbackTo, S, A] = s.lift[CallbackTo]
  }

  final class Ext_ReactST[M[_], S, A](private val s: ReactST[M, S, A]) extends AnyVal {
    def addCallback(c: Callback)(implicit M: Monad[M]): ReactST[M, S, A] =
      s.flatMap(ReactS.callbackT(_, c))

    def addCallbackS(c: S => Callback)(implicit M: Monad[M]): ReactST[M, S, A] =
      s.flatMap(ReactS.callbacksT(_, c))

    // This shouldn't be needed; it's already in BindSyntax.
    def >>[B](t: => ReactST[M, S, B])(implicit M: Monad[M]): ReactST[M, S, B] =
      s.flatMap(_ => t)

    /** zoom2 because StateT.zoom already exists. 2 because it takes two fns. */
    def zoom2[T](f: T => S, g: (T, S) => T)(implicit M: Monad[M]): ReactST[M, T, A] =
      ReactS.zoom(s, f, g)

    def zoomU[T](implicit M: Monad[M], ev: S === Unit): ReactST[M, T, A] =
      ReactS.zoomU[M, T, A](ev.subst[({type λ[σ] = ReactST[M, σ, A]})#λ](s))
  }
}