package japgolly.scalajs

import japgolly.scalajs.react.util.DefaultEffects.{Async => DefA, Sync => DefS}
import japgolly.scalajs.react.util.Effect.Id

package object react {

  type Key = facade.React.Key

  type StateAccessPure[S] = StateAccess[DefS, DefA, S]
  type StateAccessImpure[S] = StateAccess[Id, DefA, S]

  type SetStateFnPure[S] = SetStateFn[DefS, DefA, S]
  type SetStateFnImpure[S] = SetStateFn[Id, DefA, S]

  type ModStateFnPure[S] = ModStateFn[DefS, DefA, S]
  type ModStateFnImpure[S] = ModStateFn[Id, DefA, S]

  type ModStateWithPropsFnPure[P, S] = ModStateWithPropsFn[DefS, DefA, P, S]
  type ModStateWithPropsFnImpure[P, S] = ModStateWithPropsFn[Id, DefA, P, S]

}
