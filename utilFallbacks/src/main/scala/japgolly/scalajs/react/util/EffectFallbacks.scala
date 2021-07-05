package japgolly.scalajs.react.util

import japgolly.scalajs.react.userdefined

// These can be overridden downstream to make implicits globally available without imports.
// It needs to be an abstract class instead of a trait, else the Scala.js linker fails.

abstract class EffectFallbacks  extends EffectFallbacks1
abstract class EffectFallbacks1 extends EffectFallbacks2 // Callback
abstract class EffectFallbacks2 extends EffectFallbacks3 // Cats Effect
abstract class EffectFallbacks3 extends EffectFallbacks4
abstract class EffectFallbacks4 extends userdefined.Effects
