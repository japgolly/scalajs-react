package japgolly.scalajs.react.internal

import cats._
import cats.data.StateT
import cats.implicits._

import japgolly.scalajs.react._
import CatsReact.{reactCallbackCatsInstance, CatsReactExt_ReactST}

/**
  * Created by alonsodomin on 13/03/2017.
  */

trait CatsReactState extends CatsReactState2 {
  import CatsReactState._

  implicit final def CatsReactExt_ReactS[S, A](a: ReactS[S, A]) = new Ext_ReactS(a)
  implicit final def CatsReactExt_ReactST[M[_], S, A](a: ReactST[M, S, A]) = new Ext_ReactST(a)
  implicit final def CatsReactExt_StateT[M[_], S, A](a: StateT[M, S, A]) = new Ext_StateT(a)
  implicit final def CatsReactExt_FnToStateT[I, M[_], S, A](a: I => StateT[M, S, A]) = new Ext_FnToStateT(a)
}

trait CatsReactState2 extends CatsReactState1 {
  import CatsReactState._

  implicit final def CatsReactExt_StateAccessCB[I, S](i: I)(implicit sa: StateAccessor.ReadWritePure[I, S]): Ext_StateAccessRW[CallbackTo, I, S, CallbackTo] = new Ext_StateAccessRW(i)
}

trait CatsReactState1 {
  final type ReactST[F[_], S, A] = CatsReactState.ReactST[F, S, A]
  final type ReactS[S, A] = CatsReactState.ReactS[S, A]
  final val ReactS = CatsReactState.ReactS

  final type ChangeFilter[S] = CatsReactState.ChangeFilter[S]
  final val ChangeFilter = CatsReactState.ChangeFilter

  import CatsReactState._
  implicit final def CatsReactExt_StateAccessId[I, S](i: I)(implicit sa: StateAccessor.ReadWriteImpure[I, S]): Ext_StateAccessRW[Effect.Id, I, S, Effect.Id] = new Ext_StateAccessRW(i)
}

object CatsReactState {
  import ReactS.StateAndCallbacks

  type ReactST[M[_], S, A] = StateT[M, StateAndCallbacks[S], A]
  type ReactS[S, A] = ReactST[Id, S, A]

  object ReactS {
    final def StateAndCallbacks[S](state: S, cb: Callback = Callback.empty) =
      new StateAndCallbacks[S](state, cb)

    final class StateAndCallbacks[S](val state: S, val cb: Callback) {
      def withState(s2: S) = new StateAndCallbacks[S](s2, cb)
      def addCallback(cb2: Callback) = new StateAndCallbacks[S](state, cb >> cb2)
      override def toString = s"StateAndCallbacks($state, $cb)"
    }

    def apply    [S, A](f: S => (S, A))        : ReactS[S, A] = applyM[Id, S, A](f)
    def callback [S, A](a: A, c: Callback)     : ReactS[S, A] = callbacks(a, _ => c)
    def callbacks[S, A](a: A, c: S => Callback): ReactS[S, A] = callbacksM[Id, S, A](a, c)
    def get      [S]                           : ReactS[S, S] = inspect(identityFn)
    def inspect  [S, A](f: S => A)             : ReactS[S, A] = StateT.inspect[Id, StateAndCallbacks[S], A](sc => f(sc.state))
    def mod      [S]   (f: S => S)             : ReactS[S, Unit] = modT[Id, S](f)
    def ret      [S, A](a: A)                  : ReactS[S, A] = retM[Id, S, A](a)
    def set      [S]   (s: S)                  : ReactS[S, Unit] = setT[Id, S](s)

    def applyT    [M[_], S, A](f: S => (S, A))        (implicit M: Applicative[M]): ReactST[M, S, A] = applyM(s => M.pure(f(s)))
    def callbackT [M[_], S, A](a: A, c: Callback)     (implicit M: Applicative[M]): ReactST[M, S, A] = callbacksT(a, _ => c)
    def callbacksT[M[_], S, A](a: A, c: S => Callback)(implicit M: Applicative[M]): ReactST[M, S, A] = callbacksM(M.pure(a), c)
    def getT      [M[_], S]                           (implicit M: Applicative[M]): ReactST[M, S, S] = get.transformF(M.pure)
    def inspectT  [M[_], S, A](f: S => A)             (implicit M: Applicative[M]): ReactST[M, S, A] = inspectM(s => M.pure(f(s)))
    def modT      [M[_], S]   (f: S => S)             (implicit M: Applicative[M]): ReactST[M, S, Unit] = modM(s => M.pure(f(s)))
    def retT      [M[_], S, A](a: A)                  (implicit M: Applicative[M]): ReactST[M, S, A] = StateT[M, StateAndCallbacks[S], A](sc => M.pure(sc -> a))
    def setT      [M[_], S]   (s: S)                  (implicit M: Applicative[M]): ReactST[M, S, Unit] = modT[M, S](_ => s)

    def applyM[M[_], S, A](f: S => M[(S, A)])(implicit M: Applicative[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](sc => M.map(f(sc.state))(x => sc.withState(x._1) -> x._2))

    def callbackM[M[_], S, A](fa: M[A], c: Callback)(implicit M: Applicative[M]): ReactST[M, S, A] =
      callbacksM(fa, _ => c)

    def callbacksM[M[_], S, A](fa: M[A], c: S => Callback)(implicit M: Applicative[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](s => M.map(fa)(a => s.addCallback(c(s.state)) -> a))

    def inspectM[M[_], S, A](f: S => M[A])(implicit M: Applicative[M]): ReactST[M, S, A] =
      StateT.inspectF[M, StateAndCallbacks[S], A](sc => f(sc.state))

    def liftS[M[_], S, A](st: StateT[M, S, A])(implicit M: Monad[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](sc => M.map(st.run(sc.state))(sa => sc.withState(sa._1) -> sa._2))
    def liftR[M[_], S, A](f: S => ReactST[M, S, A])(implicit M: Monad[M]): ReactST[M, S, A] = getT[M, S].flatMap(f)

    def modM[M[_], S](f: S => M[S])(implicit M: Applicative[M]): ReactST[M, S, Unit] =
      StateT[M, StateAndCallbacks[S], Unit](sc => M.map(f(sc.state))(s => (sc.withState(s), ())))

    def retM[M[_], S, A](fa: M[A])(implicit M: Applicative[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](sc => M.map(fa)(a => sc -> a))

    def setM[M[_], S](fs: M[S])(implicit M: Applicative[M]): ReactST[M, S, Unit] = modM(_ => fs)

    def unlift[M[_], S, A](st: ReactST[M, S, A])(implicit M: Monad[M]): StateT[M, S, A] =
      StateT[M, S, A](s => M.map(st.run(StateAndCallbacks(s)))(sa => sa._1.state -> sa._2))

    def zoom[M[_], S, T, A](rst: ReactST[M, S, A], f: T => S, g: (T, S) => T)(implicit M: Monad[M]): ReactST[M, T, A] =
      StateT[M, StateAndCallbacks[T], A] { st =>
        val fta = rst.run(StateAndCallbacks(f(st.state), st.cb))
        M.map(fta) { case (sc, a) =>
          StateAndCallbacks(g(st.state, sc.state), sc.cb) -> a
        }
      }

    def zoomU[M[_], S, A](rst: ReactST[M, Unit, A])(implicit M: Monad[M]): ReactST[M, S, A] =
      zoom[M, Unit, S, A](rst, _ => (), (s, _) => s)

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
      def gets     [A]     (f: S => A)                : ReactS[S,A]    = ReactS.inspect(f)
      def mod              (f: S => S)                : ReactS[S,Unit] = ReactS.mod(f)
      def ret      [A]     (a: A)                     : ReactS[S,A]    = ReactS.ret(a)
      def set              (s: S)                     : ReactS[S,Unit] = ReactS.set(s)
      def zoom     [T,A]   (r: ReactS[T, A])
                           (f: S => T, g: (S, T) => S): ReactS[S,A]    = ReactS.zoom(r, f, g)
      def zoomU    [A]     (r: ReactS[Unit, A])       : ReactS[S,A]    = ReactS.zoomU(r)

      def applyM    [M[_],A](f: S => M[(S, A)])        (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.applyM(f)
      def applyT    [M[_],A](f: S => (S, A))           (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.applyT(f)
      def callbackM [M[_],A](a: M[A], c: Callback)     (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.callbackM(a, c)
      def callbackT [M[_],A](a: A, c: Callback)        (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.callbackT(a, c)
      def callbacksM[M[_],A](a: M[A], c: S => Callback)(implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.callbacksM(a, c)
      def callbacksT[M[_],A](a: A, c: S => Callback)   (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.callbacksT(a, c)
      def getT      [M[_]]                             (implicit M: Applicative[M]): ReactST[M,S,S]    = ReactS.getT
      def inspectM  [M[_],A](f: S => M[A])             (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.inspectM(f)
      def inspectT  [M[_],A](f: S => A)                (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.inspectT(f)
      def liftR     [M[_],A](f: S => ReactST[M, S, A]) (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.liftR(f)
      def liftS     [M[_],A](t: StateT[M, S, A])       (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.liftS(t)
      def modM      [M[_]]  (f: S => M[S])             (implicit M: Applicative[M]): ReactST[M,S,Unit] = ReactS.modM(f)
      def modT      [M[_]]  (f: S => S)                (implicit M: Applicative[M]): ReactST[M,S,Unit] = ReactS.modT(f)
      def retM      [M[_],A](ma: M[A])                 (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.retM(ma)
      def retT      [M[_],A](a: A)                     (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.retT(a)
      def setM      [M[_]]  (ms: M[S])                 (implicit M: Applicative[M]): ReactST[M,S,Unit] = ReactS.setM(ms)
      def setT      [M[_]]  (s: S)                     (implicit M: Applicative[M]): ReactST[M,S,Unit] = ReactS.setT(s)
      def unlift    [M[_],A](t: ReactST[M, S, A])      (implicit M: Monad      [M]): StateT [M,S,A]    = ReactS.unlift(t)
    }

    /**
      * T prefix indicates we want the result lifted into an M.
      */
    def FixT[M[_], S] = new FixT[M, S]
    def FixCB[S] = new FixT[CallbackTo, S]
    final class FixT[M[_], S] {
      type T[A] = ReactST[M, S, A]

      def nop (implicit M: Applicative[M]):        ReactST[M,S,Unit] = retT(())
      def _nop(implicit M: Applicative[M]): Any => ReactST[M,S,Unit] = _ => nop

      def apply     [A]  (f: S => M[(S, A)])        (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.applyM(f)
      def applyT    [A]  (f: S => (S, A))           (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.applyT(f)
      def callback  [A]  (a: M[A], c: Callback)     (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.callbackM(a, c)
      def callbackT [A]  (a: A, c: Callback)        (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.callbackT(a, c)
      def callbacks [A]  (a: M[A], c: S => Callback)(implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.callbacksM(a, c)
      def callbacksT[A]  (a: A, c: S => Callback)   (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.callbacksT(a, c)
      def get                                       (implicit M: Applicative[M]): ReactST[M,S,S]    = ReactS.getT
      def inspect   [A]  (f: S => M[A])             (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.inspectM(f)
      def inspectT  [A]  (f: S => A)                (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.inspectT(f)
      def liftR     [A]  (f: S => ReactST[M, S, A]) (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.liftR(f)
      def liftS     [A]  (t: StateT[M, S, A])       (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.liftS(t)
      def mod            (f: S => M[S])             (implicit M: Applicative[M]): ReactST[M,S,Unit] = ReactS.modM(f)
      def modT           (f: S => S)                (implicit M: Applicative[M]): ReactST[M,S,Unit] = ReactS.modT(f)
      def ret       [A]  (ma: M[A])                 (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.retM(ma)
      def retT      [A]  (a: A)                     (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.retT(a)
      def set            (ms: M[S])                 (implicit M: Applicative[M]): ReactST[M,S,Unit] = ReactS.setM(ms)
      def setT           (s: S)                     (implicit M: Applicative[M]): ReactST[M,S,Unit] = ReactS.setT(s)
      def unlift    [A]  (t: ReactST[M, S, A])      (implicit M: Monad      [M]): StateT [M,S,A]    = ReactS.unlift(t)
      def zoom      [T,A](r: ReactST[M, T, A])
                         (f: S => T, g: (S,T) => S) (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.zoom(r, f, g)
      def zoomU     [A]  (r: ReactST[M, Unit, A])   (implicit M: Monad      [M]): ReactST[M,S,A]    = ReactS.zoomU(r)
    }
  }

  final case class ChangeFilter[S](allowChange: (S, S) => Boolean) {
    def apply[A](s1: S, s2: S, orElse: => A, change: S => A): A =
      if (allowChange(s1, s2)) change(s2) else orElse
  }
  object ChangeFilter {
    def refl[S] = apply[S](_ != _)
    def reflOn[S, T](f: S => T) = apply[S](f(_) != f(_))
    def equal[S: Eq] = apply[S]((a,b) => !Eq[S].eqv(a,b))
    def equalOn[S, T: Eq](f: S => T) = apply[S]((a,b) => !Eq[T].eqv(f(a),f(b)))
  }

  final class Ext_StateAccessRW[F[_], SI, S, Out[_]](si: SI)(implicit sa: StateAccessor.ReadWrite[SI, F, F, S], fToCb: Effect.Trans[F, CallbackTo], cbToOut: Effect.Trans[CallbackTo, Out]) {
    implicit private def autoOut[A](a: CallbackTo[A]): Out[A] = cbToOut(a)

    private def stateCB: CallbackTo[S] = fToCb(sa.state(si))

    private def run[M[_], A, B](st: => ReactST[M, S, A], conclude: (S, S, A, => Callback) => CallbackTo[B])
                               (implicit M: FlatMap[M], trans: M ~> CallbackTo): CallbackTo[B] =
      runM[M, A, B](st, conclude).flatMap(trans(_))

    def runState[M[_], A](st: => ReactST[M, S, A])(implicit M: M ~> CallbackTo, N: FlatMap[M]): Out[A] =
      run[M, A, A](st, (_, _, a, cb) => cb.map(_ => a))

    def runStateFn[I, M[_], A](f: I => ReactST[M, S, A])(implicit M: M ~> CallbackTo, N: FlatMap[M]): I => Out[A] =
      i => runState(f(i))

    def runStateFn[I, M[_], A](f: I => ReactST[M, S, A], cb: I => Callback)(implicit M: M ~> CallbackTo, N: Monad[M]): I => Out[A] =
      i => runState(f(i) addCallback cb(i))

    def runStateF[M[_], A](st: => ReactST[M, S, A])(implicit M: M ~> CallbackTo, N: FlatMap[M], F: ChangeFilter[S]): Out[A] =
      run[M, A, A](st, (s1, s2, a, cb) => F(s1, s2, CallbackTo pure a, _ => cb.map(_ => a)))

    def runStateFnF[I, M[_], A](f: I => ReactST[M, S, A])(implicit M: M ~> CallbackTo, N: FlatMap[M], F: ChangeFilter[S]): I => Out[A] =
      i => runStateF(f(i))

    private def runM[M[_], A, B](st: => ReactST[M, S, A], conclude: (S, S, A, => Callback) => CallbackTo[B])
                                (implicit M: FlatMap[M]): CallbackTo[M[B]] =
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

    def runStateM[M[_], A](st: => ReactST[M, S, A])(implicit M: FlatMap[M]): Out[M[A]] =
      runM[M, A, A](st, (_, _, a, cb) => cb.map(_ => a))

    def runStateFnM[I, M[_], A](f: I => ReactST[M, S, A])(implicit M: FlatMap[M]): I => Out[M[A]] =
      i => runStateM[M, A](f(i))

    def modStateF(f: S => S, cb: Callback = Callback.empty)(implicit F: ChangeFilter[S]): Out[Unit] =
      stateCB.flatMap(s1 =>
        F(s1, f(s1), Callback.empty, s => fToCb(sa(si).setState(s, cb))))

    def modStateFnF[I](f: I => S => S, cb: Callback = Callback.empty)(implicit F: ChangeFilter[S]): I => Out[Unit] =
      i => modStateF(f(i), cb)
  }

  final class Ext_StateT[M[_], S, A](private val s: StateT[M, S, A]) extends AnyVal {
    def liftS(implicit F: Monad[M]): ReactST[M, S, A] = ReactS.liftS(s)
  }

  final class Ext_FnToStateT[I, M[_], S, A](private val f: I => StateT[M, S, A]) extends AnyVal {
    def liftS(implicit F: Monad[M]): I => ReactST[M, S, A] =
      i => ReactS.liftS(f(i))
  }

  final class Ext_ReactS[S, A](private val rs: ReactS[S, A]) extends AnyVal {
    def liftCB: ReactST[CallbackTo, S, A] = rs.transformF(CallbackTo.pure)
  }

  final class Ext_ReactST[M[_], S, A](private val rst: ReactST[M, S, A]) extends AnyVal {
    def addCallback(c: Callback)(implicit F: Monad[M]): ReactST[M, S, A] =
      rst.flatMap(ReactS.callbackT(_, c))

    def addCallbackS(f: S => Callback)(implicit F: Monad[M]): ReactST[M, S, A] =
      rst.flatMap(ReactS.callbacksT(_, f))

    // Providing >> & >>= for convenience
    def >>[B](other: => ReactST[M, S, B])(implicit F: Monad[M]): ReactST[M, S, B] =
      rst.flatMap(_ => other)

    def >>=[B](f: A => ReactST[M, S, B])(implicit F: Monad[M]): ReactST[M, S, B] =
      rst.flatMap(f)

    def zoom2[T](f: T => S, g: (T, S) => T)(implicit F: Monad[M]): ReactST[M, T, A] =
      ReactS.zoom(rst, f, g)

    def zoomU[T](implicit F: Monad[M], ev: S =:= Unit): ReactST[M, T, A] =
      ReactS.zoomU(rst.asInstanceOf[ReactST[M, Unit, A]])

  }

}
