package japgolly.scalajs.react.userdefined

// This can be overridden downstream to make implicits globally available without imports.
// It needs to be an abstract class instead of a trait, else the Scala.js linker fails.
abstract class Effects
