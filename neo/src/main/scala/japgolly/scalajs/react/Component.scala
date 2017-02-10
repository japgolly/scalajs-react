package japgolly.scalajs.react

/*
TODO Delete

  trait HasEffect[F[+_]] {
    def withUntypedEffects(implicit t: Effect.Trans[F, Effect.Id]): HasEffect[Effect.Id] =
      withEffect
  }

  trait Props[F[+_], +P] extends HasEffect[F] {
    def props: F[P]
    def propsChildren: F[PropsChildren]
    def mapProps[X](f: P => X): Props[F, X]
    def withEffect[G[+_]](implicit t: Effect.Trans[F, G]): Props[G, P]
  }

  trait State[F[+_], S] extends HasEffect[F] {
    def state: F[S]
    def setState(newState: S, callback: Callback = Callback.empty): F[Unit]
    def modState(mod: S => S, callback: Callback = Callback.empty): F[Unit]
    def xmapState[X](f: S => X)(g: X => S): State[F, X]
    def zoomState[X](get: S => X)(set: X => S => S): State[F, X]
    def withEffect[G[+_]](implicit t: Effect.Trans[F, G]): State[G, S]
  }
*/