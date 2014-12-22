package japgolly.scalajs.react

import org.scalajs.dom
import japgolly.scalajs.react.vdom.Attr
import japgolly.scalajs.react.vdom.Implicits._
import scala.scalajs.js.{UndefOr, undefined}
import scalaz._
import scalaz.effect.IO
import Scalaz.Id
import Leibniz.===

package vdom {
  import Scalatags._
  
  abstract class ScalazImplicits {


  }
}

object ScalazReact extends vdom.ScalazImplicits {
  // Don't edit this directly. Run sync-scala70

  implicit val IoToIo: IO ~> IO = NaturalTransformation.refl[IO]
  implicit object IdToIo extends (Id ~> IO) {
    override def apply[A](a: Id[A]): IO[A] = IO(a)
  }

  implicit final class SzRExt_Attr(val a: Attr) extends AnyVal {

    def ~~>(io: => IO[Unit]) =
      a --> io.unsafePerformIO()

    def ~~>[N <: dom.Node, E <: SyntheticEvent[N]](eventHandler: E => IO[Unit]) =
      a.==>[N, E](eventHandler(_).unsafePerformIO())
  }

  implicit final class SzRExt_C_M(val u: ComponentScope_M[_]) extends AnyVal {
    def forceUpdateIO = IO(u.forceUpdate())
  }

  implicit final class SzRExt_SEvent[N <: dom.Node](val e: SyntheticEvent[N]) extends AnyVal {
    /**
     * Stops the default action of an element from happening.
     * For example: Prevent a submit button from submitting a form Prevent a link from following the URL
     */
    def preventDefaultIO = IO(e.preventDefault())
    /**
     * Stops the bubbling of an event to parent elements, preventing any parent event handlers from being executed.
     */
    def stopPropagationIO = IO(e.stopPropagation())
  }

  val preventDefaultIO  = (_: SyntheticEvent[dom.Node]).preventDefaultIO
  val stopPropagationIO = (_: SyntheticEvent[dom.Node]).stopPropagationIO

  // ===================================================================================================================
  // State manipulation

  final type OpCallbackIO = UndefOr[IO[Unit]]
  implicit def OpCallbackFromIO(cb: OpCallbackIO): OpCallback = cb.map(f => () => f.unsafePerformIO())

  @inline def StateAndCallbacks[S](s: S, cb: OpCallbackIO = undefined) = new StateAndCallbacks[S](s, cb)
  final class StateAndCallbacks[S](val state: S, val cb: OpCallbackIO) {
    @inline def withState(s2: S) = new StateAndCallbacks(s2, cb)
    @inline def addCallback(cb2: OpCallbackIO) = new StateAndCallbacks(state, appendCallbacks(cb, cb2))
    override def toString = s"StateAndCallbacks($state, $cb)"
  }

  @inline private[this] def appendCallbacks(a: OpCallbackIO, b: OpCallbackIO): OpCallbackIO =
    a.fold(b)(aa => b.fold(aa)(bb => aa.flatMap(_ => bb)))

  final type ReactS[S, A] = ReactST[Id, S, A]
  final type ReactST[M[+_], S, A] = StateT[M, StateAndCallbacks[S], A]

  /**
   * M prefix indicates M in args.
   * T prefix indicates we want the result lifted into an M.
   */
  object ReactS {
    @inline def apply    [S,A](f: S => (S, A))       : ReactS[S,A]    = applyM[Id, S, A](f)
    @inline def callback [S,A](a: A, c: OpCallbackIO): ReactS[S,A]    = callbackM[Id, S, A](a, c)
    @inline def get      [S]                         : ReactS[S,S]    = gets(identity[S])
    @inline def mod      [S]  (f: S => S)            : ReactS[S,Unit] = modM[Id, S](f)
    @inline def ret      [S,A](a: A)                 : ReactS[S,A]    = retM[Id, S, A](a)
    @inline def set      [S]  (s: S)                 : ReactS[S,Unit] = mod((_: S) => s)

    @inline def applyT   [M[+_],S,A](f: S => (S, A))         (implicit M: Applicative[M]): ReactST[M,S,A]    = applyM(s ⇒ M point f(s))
    @inline def callbackT[M[+_],S,A](a: A, c: OpCallbackIO)  (implicit M: Applicative[M]): ReactST[M,S,A]    = callbackM(M point a, c)
    @inline def getT     [M[+_],S]                           (implicit M: Applicative[M]): ReactST[M,S,S]    = get.lift[M]
    @inline def getsT    [M[+_],S,A](f: S => A)              (implicit M: Applicative[M]): ReactST[M,S,A]    = getsM(s ⇒ M point f(s))
    @inline def liftR    [M[+_],S,A](f: S ⇒ ReactST[M, S, A])(implicit M: Monad[M])      : ReactST[M,S,A]    = getT[M,S] flatMap f
    @inline def modT     [M[+_],S]  (f: S => S)              (implicit M: Applicative[M]): ReactST[M,S,Unit] = modM(s ⇒ M point f(s))
    @inline def retM     [M[+_],S,A](ma: M[A])               (implicit M: Functor[M])    : ReactST[M,S,A]    = getsM[M,S,A](_ ⇒ ma)
    @inline def setM     [M[+_],S]  (ms: M[S])               (implicit M: Functor[M])    : ReactST[M,S,Unit] = modM((_: S) ⇒ ms)
    @inline def setT     [M[+_],S]  (s: S)                   (implicit M: Applicative[M]): ReactST[M,S,Unit] = setM(M point s)

    def applyM[M[+_], S, A](f: S => M[(S, A)])(implicit F: Functor[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](sc => F.map(f(sc.state))(x => (sc withState x._1, x._2) ))

    def callbackM[M[+_], S, A](ma: M[A], c: OpCallbackIO)(implicit M: Functor[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](s => M.map(ma)(a => (s addCallback c, a)))

    @inline def gets[S, A](f: S => A): ReactS[S, A] =
      State.gets[StateAndCallbacks[S], A](s => f(s.state))

    def getsM[M[+_], S, A](f: S => M[A])(implicit F: Functor[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](sc => F.map(f(sc.state))((sc, _)))

    def liftS[M[+_], S, A](t: StateT[M, S, A])(implicit M: Functor[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](sc => M.map(t(sc.state))(sa => (sc withState sa._1, sa._2) ))

    def modM[M[+_], S](f: S => M[S])(implicit M: Functor[M]): ReactST[M, S, Unit] =
      StateT[M, StateAndCallbacks[S], Unit](sc => M.map(f(sc.state))(s2 => (sc withState s2,()) ))

    @inline def retT[M[+_], S, A](a: A)(implicit M: Applicative[M]): ReactST[M, S, A] =
      StateT[M, StateAndCallbacks[S], A](s => M.point((s, a)))

    def unlift[M[+_], S, A](t: ReactST[M, S, A])(implicit M: Functor[M]): StateT[M, S, A] =
      StateT[M, S, A](s => M.map(t(StateAndCallbacks(s)))(sa => (sa._1.state, sa._2) ))

    def zoom[M[+_], S, T, A](r: ReactST[M, S, A], f: T => S, g: (T, S) => T)(implicit M: Functor[M]): ReactST[M, T, A] =
      StateT[M, StateAndCallbacks[T], A](tc => {
        val m = r(StateAndCallbacks(f(tc.state), tc.cb))
        M.map(m){ case (sc, a) => (StateAndCallbacks(g(tc.state, sc.state), sc.cb), a) }
      })

    @inline def zoomU[M[+_], S, A](r: ReactST[M, Unit, A])(implicit M: Functor[M]): ReactST[M, S, A] =
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
      @inline def callback [A]     (a: A, c: OpCallbackIO)    : ReactS[S,A]    = ReactS.callback(a, c)
      @inline def get                                         : ReactS[S,S]    = ReactS.get
      @inline def gets     [A]     (f: S => A)                : ReactS[S,A]    = ReactS.gets(f)
      @inline def mod              (f: S => S)                : ReactS[S,Unit] = ReactS.mod(f)
      @inline def ret      [A]     (a: A)                     : ReactS[S,A]    = ReactS.ret(a)
      @inline def set              (s: S)                     : ReactS[S,Unit] = ReactS.set(s)
      @inline def zoom     [T,A]   (r: ReactS[T, A])
                                   (f: S => T, g: (S, T) => S): ReactS[S,A]    = ReactS.zoom(r, f, g)
      @inline def zoomU    [A]     (r: ReactS[Unit, A])       : ReactS[S,A]    = ReactS.zoomU(r)

      @inline def applyM   [M[+_],A](f: S => M[(S, A)])       (implicit M: Functor[M])    : ReactST[M,S,A]    = ReactS.applyM(f)
      @inline def applyT   [M[+_],A](f: S => (S, A))          (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.applyT(f)
      @inline def callbackM[M[+_],A](a: M[A], c: OpCallbackIO)(implicit M: Functor[M])    : ReactST[M,S,A]    = ReactS.callbackM(a, c)
      @inline def callbackT[M[+_],A](a: A, c: OpCallbackIO)   (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.callbackT(a, c)
      @inline def getT     [M[+_]]                            (implicit M: Applicative[M]): ReactST[M,S,S]    = ReactS.getT
      @inline def getsM    [M[+_],A](f: S => M[A])            (implicit M: Functor[M])    : ReactST[M,S,A]    = ReactS.getsM(f)
      @inline def getsT    [M[+_],A](f: S => A)               (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.getsT(f)
      @inline def liftR    [M[+_],A](f: S => ReactST[M, S, A])(implicit M: Monad[M])      : ReactST[M,S,A]    = ReactS.liftR(f)
      @inline def liftS    [M[+_],A](t: StateT[M, S, A])      (implicit M: Functor[M])    : ReactST[M,S,A]    = ReactS.liftS(t)
      @inline def modM     [M[+_]]  (f: S => M[S])            (implicit M: Functor[M])    : ReactST[M,S,Unit] = ReactS.modM(f)
      @inline def modT     [M[+_]]  (f: S => S)               (implicit M: Applicative[M]): ReactST[M,S,Unit] = ReactS.modT(f)
      @inline def retM     [M[+_],A](ma: M[A])                (implicit M: Functor[M])    : ReactST[M,S,A]    = ReactS.retM(ma)
      @inline def retT     [M[+_],A](a: A)                    (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.retT(a)
      @inline def setM     [M[+_]]  (ms: M[S])                (implicit M: Functor[M])    : ReactST[M,S,Unit] = ReactS.setM(ms)
      @inline def setT     [M[+_]]  (s: S)                    (implicit M: Applicative[M]): ReactST[M,S,Unit] = ReactS.setT(s)
      @inline def unlift   [M[+_],A](t: ReactST[M, S, A])     (implicit M: Functor[M])    : StateT [M,S,A]    = ReactS.unlift(t)
    }

    /**
     * T prefix indicates we want the result lifted into an M.
     */
    @inline def FixT[M[+_], S] = new FixT[M, S]
    final class FixT[M[+_], S] {
      type T[A] = ReactST[M, S, A]

      @inline def nop (implicit M: Applicative[M]):        ReactST[M,S,Unit] = retT(())
      @inline def _nop(implicit M: Applicative[M]): Any => ReactST[M,S,Unit] = _ => nop

      @inline def apply    [A]  (f: S => M[(S, A)])       (implicit M: Functor[M])    : ReactST[M,S,A]    = ReactS.applyM(f)
      @inline def applyT   [A]  (f: S => (S, A))          (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.applyT(f)
      @inline def callback [A]  (a: M[A], c: OpCallbackIO)(implicit M: Functor[M])    : ReactST[M,S,A]    = ReactS.callbackM(a, c)
      @inline def callbackT[A]  (a: A, c: OpCallbackIO)   (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.callbackT(a, c)
      @inline def get                                     (implicit M: Applicative[M]): ReactST[M,S,S]    = ReactS.getT
      @inline def gets     [A]  (f: S => M[A])            (implicit M: Functor[M])    : ReactST[M,S,A]    = ReactS.getsM(f)
      @inline def getsT    [A]  (f: S => A)               (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.getsT(f)
      @inline def liftR    [A]  (f: S => ReactST[M, S, A])(implicit M: Monad[M])      : ReactST[M,S,A]    = ReactS.liftR(f)
      @inline def liftS    [A]  (t: StateT[M, S, A])      (implicit M: Functor[M])    : ReactST[M,S,A]    = ReactS.liftS(t)
      @inline def mod           (f: S => M[S])            (implicit M: Functor[M])    : ReactST[M,S,Unit] = ReactS.modM(f)
      @inline def modT          (f: S => S)               (implicit M: Applicative[M]): ReactST[M,S,Unit] = ReactS.modT(f)
      @inline def ret      [A]  (ma: M[A])                (implicit M: Functor[M])    : ReactST[M,S,A]    = ReactS.retM(ma)
      @inline def retT     [A]  (a: A)                    (implicit M: Applicative[M]): ReactST[M,S,A]    = ReactS.retT(a)
      @inline def set           (ms: M[S])                (implicit M: Functor[M])    : ReactST[M,S,Unit] = ReactS.setM(ms)
      @inline def setT          (s: S)                    (implicit M: Applicative[M]): ReactST[M,S,Unit] = ReactS.setT(s)
      @inline def unlift   [A]  (t: ReactST[M, S, A])     (implicit M: Functor[M])    : StateT [M,S,A]    = ReactS.unlift(t)
      @inline def zoom     [T,A](r: ReactST[M, T, A])
                                (f: S => T, g: (S,T) => S)(implicit M: Functor[M])    : ReactST[M,S,A]    = ReactS.zoom(r, f, g)
      @inline def zoomU    [A]  (r: ReactST[M, Unit, A])  (implicit M: Functor[M])    : ReactST[M,S,A]    = ReactS.zoomU(r)
    }
  }

  implicit final class SzRExt_StateTOps[M[+_], S, A](val s: StateT[M, S, A]) extends AnyVal {
    @inline def liftS(implicit M: Functor[M]): ReactST[M, S, A] = ReactS.liftS(s)
  }

  implicit final class SzRExt__StateTOps[I, M[+_], S, A](val f: I => StateT[M, S, A]) extends AnyVal {
    @inline def liftS(implicit M: Functor[M]): I => ReactST[M, S, A] = f(_).liftS
  }

  implicit final class SzRExt_ReactSOps[S, A](val s: ReactS[S,A]) extends AnyVal {
    // Very common case. Very sick of seeing it highlighted red everywhere in Intellij.
    def liftIO: ReactST[IO, S, A] = s.lift[IO]
  }

  implicit final class SzRExt_ReactSTOps[M[+_], S, A](val s: ReactST[M,S,A]) extends AnyVal {
    def addCallback(c: OpCallbackIO)(implicit M: Monad[M]): ReactST[M,S,A] =
      s.flatMap(ReactS.callbackT(_, c))

    // This shouldn't be needed; it's already in BindSyntax.
    def >>[B](t: => ReactST[M,S,B])(implicit M: Bind[M]): ReactST[M,S,B] =
      s.flatMap(_ => t)

    /** zoom2 because StateT.zoom already exists. 2 because it takes two fns. */
    def zoom2[T](f: T => S, g: (T, S) => T)(implicit M: Functor[M]): ReactST[M, T, A] =
      ReactS.zoom(s, f, g)

    def zoomU[T](implicit M: Functor[M], ev: S === Unit): ReactST[M, T, A] =
      ReactS.zoomU[M, T, A](ev.subst[({type λ[σ] = ReactST[M, σ, A]})#λ](s))
  }

  implicit final class SzRExt_CompStateAccessOps[C[_], S](val u: C[S]) extends AnyVal {
    type CC = CompStateAccess[C]

    private def run[M[+_], A, B](st: => ReactST[M, S, A], f: (S, S, A, => IO[Unit]) => IO[B])(implicit C: CC, M: M ~> IO): IO[B] =
      IO(StateAndCallbacks(C state u)).flatMap(s1 =>
        M(st run s1).flatMap { case (s2, a) =>
          f(s1.state, s2.state, a, IO(C.setState(u, s2.state, s2.cb)))
        }
      )

    def runState[M[+_], A](st: => ReactST[M, S, A])(implicit C: CC, M: M ~> IO): IO[A] =
      run[M, A, A](st, (s1,s2,a,io) => io.map(_ => a))

    def _runState[I, M[+_], A](f: I => ReactST[M, S, A])(implicit C: CC, M: M ~> IO): I => IO[A] =
      i => runState(f(i))

    def _runState[I, M[+_], A](f: I => ReactST[M, S, A], cb: I => OpCallbackIO)(implicit C: CC, M: M ~> IO, N: Monad[M]): I => IO[A] =
      i => runState(f(i) addCallback cb(i))

    def runStateF[M[+_], A](st: => ReactST[M, S, A])(implicit C: CC, M: M ~> IO, F: ChangeFilter[S]): IO[A] =
      run[M, A, A](st, (s1,s2,a,io) => if (F.allowChange(s1,s2)) io.map(_ => a) else IO(a))

    def _runStateF[I, M[+_], A](f: I => ReactST[M, S, A])(implicit C: CC, M: M ~> IO, F: ChangeFilter[S]): I => IO[A] =
      i => runStateF(f(i))

    def _runStateF[I, M[+_], A](f: I => ReactST[M, S, A], cb: I => OpCallbackIO)(implicit C: CC, M: M ~> IO, N: Monad[M], F: ChangeFilter[S]): I => IO[A] =
      i => runStateF(f(i) addCallback cb(i))
  }

  case class ChangeFilter[S](allowChange: (S, S) => Boolean)
  object ChangeFilter {
    def refl[S] = apply[S](_ != _)
    def reflOn[S, T](f: S => T) = apply[S](f(_) != f(_))
    def equal[S: Equal] = apply[S]((a,b) => !implicitly[Equal[S]].equal(a,b))
    def equalOn[S, T: Equal](f: S => T) = apply[S]((a,b) => !implicitly[Equal[T]].equal(f(a),f(b)))
  }

  // Seriously, Scala, get your shit together.
  @inline final implicit def moarScalaHandHolding[P,S](b: BackendScope[P,S]): SzRExt_CompStateAccessOps[ComponentScope_SS, S] = (b: ComponentScope_SS[S])
  @inline final implicit def moarScalaHandHolding[P,S,B](b: ComponentScopeU[P,S,B]): SzRExt_CompStateAccessOps[ComponentScope_SS, S] = (b: ComponentScope_SS[S])
}