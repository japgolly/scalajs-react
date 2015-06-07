package japgolly.scalajs.react.extra

import japgolly.scalajs.react.ScalazReact._
import japgolly.scalajs.react._

import scalaz._
import scalaz.effect.IO

final case class ListenableB1[P, S, B <: OnUnmount, N <: TopNode, A]
                             (    c: ReactComponentB[P, S, B, N])
                             (val f: P => Listenable[A]) { outer ⇒
  
  private type Comp = ReactComponentB[P, S, B, N]

  private[this] def handleEvent(g: (ComponentScopeM[P, S, B, N], A) => Unit): Comp =
    c.componentDidMount($ => $.backend onUnmountF f($.props).register(a ⇒ g($, a)))
     .configure(OnUnmount.install)

  private[this] def handleEventIO(g: (ComponentScopeM[P, S, B, N], A) => IO[Unit]): Comp =
    handleEvent(($, a) => g($, a).unsafePerformIO())

  sealed class ListenableB2[In](mapIn: ((ComponentScopeM[P, S, B, N], A)) ⇒ In){
    def handleEvent(g: In => Unit): Comp =
      outer.handleEvent(($, a) ⇒ g(mapIn(($, a))))

    def handleEventIO(g: In => IO[Unit]): Comp =
      handleEvent(in => g(in).unsafePerformIO())

    def handleEventS[M[_]](g: In => ReactST[M, S, Unit])(implicit M: M ~> IO): Comp =
      outer.handleEventIO{case (t, a) => t.runState(g(mapIn(t, a)))}

    def handleEventSF[M[_]](g: In => ReactST[M, S, Unit])(implicit M: M ~> IO, F: ChangeFilter[S]): Comp =
      outer.handleEventIO{case (t, a) => t.runStateF(g(mapIn(t, a)))}
  }

  object ignoringProps   extends ListenableB2[A](c ⇒ c._2)
  object ignoringData    extends ListenableB2[ComponentScopeM[P, S, B, N]](_._1)
  object ignoringNothing extends ListenableB2[(ComponentScopeM[P, S, B, N], A)](identity)

  object ignoringInput /* ListenableB2[Unit](_ => ()) //can not abstract over by-names, so inlining */ {
    def handleEvent(g: ⇒ Unit): Comp =
      outer.handleEvent((_, _) ⇒ g)

    def handleEventIO(g: => IO[Unit]): Comp =
      handleEvent(g.unsafePerformIO())

    def handleEventS[M[_]](g: => ReactST[M, S, Unit])(implicit M: M ~> IO): Comp =
      outer.handleEventIO{case (t, _) => t.runState(g)}

    def handleEventSF[M[_]](g: => ReactST[M, S, Unit])(implicit M: M ~> IO, F: ChangeFilter[S]): Comp =
      outer.handleEventIO{case (t, _) => t.runStateF(g)}
  }
}

trait ListenableBuilderSyntax {
  implicit final class ReactComponentBListener[P, S, B <: OnUnmount, N <: TopNode, C]
                                              (val        c: C)
                                              (implicit ev1: C ⇒ ReactComponentB[P, S, B, N]) {
    def listenToFromProps[A](f: P => Listenable[A]): ListenableB1[P, S, B, N, A] =
      ListenableB1[P, S, B, N, A](c)(f)

    def listenTo[A](f: Listenable[A]): ListenableB1[P, S, B, N, A] =
      listenToFromProps(_ ⇒ f)
  }
}

object listenTo extends ListenableBuilderSyntax
