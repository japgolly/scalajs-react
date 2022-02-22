import org.scalajs.linker.interface.{ModuleInitializer, ModuleSplitStyle}
import Dependencies._

ThisBuild / organization := "com.github.japgolly.scalajs-react-experiment"
ThisBuild / shellPrompt  := ((s: State) => Project.extract(s).currentRef.project + "> ")

def scalacFlags = Seq(
  "-deprecation",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-unchecked",
  "-Wconf:msg=may.not.be.exhaustive:e",            // Make non-exhaustive matches errors instead of warnings
  "-Wconf:msg=Reference.to.uninitialized.value:e", // Make uninitialised value calls errors instead of warnings
  "-Wunused:explicits",                            // Warn if an explicit parameter is unused.
  "-Wunused:implicits",                            // Warn if an implicit parameter is unused.
  "-Wunused:imports",                              // Warn if an import selector is not referenced.
  "-Wunused:locals",                               // Warn if a local definition is unused.
  "-Wunused:nowarn",                               // Warn if a @nowarn annotation does not suppress any warnings.
  "-Wunused:patvars",                              // Warn if a variable bound in a pattern is unused.
  "-Wunused:privates",                             // Warn if a private member is unused.
  "-Xlint:adapted-args",                           // An argument list was modified to match the receiver.
  "-Xlint:constant",                               // Evaluation of a constant arithmetic expression resulted in an error.
  "-Xlint:delayedinit-select",                     // Selecting member of DelayedInit.
  "-Xlint:deprecation",                            // Enable -deprecation and also check @deprecated annotations.
  "-Xlint:eta-zero",                               // Usage `f` of parameterless `def f()` resulted in eta-expansion, not empty application `f()`.
  "-Xlint:implicit-not-found",                     // Check @implicitNotFound and @implicitAmbiguous messages.
  "-Xlint:inaccessible",                           // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                              // A type argument was inferred as Any.
  "-Xlint:missing-interpolator",                   // A string literal appears to be missing an interpolator id.
  "-Xlint:nonlocal-return",                        // A return statement used an exception for flow control.
  "-Xlint:nullary-unit",                           // `def f: Unit` looks like an accessor; add parens to look side-effecting.
  "-Xlint:option-implicit",                        // Option.apply used an implicit view.
  "-Xlint:poly-implicit-overload",                 // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",                         // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                            // In a pattern, a sequence wildcard `_*` should match all of a repeated parameter.
  "-Xlint:valpattern",                             // Enable pattern checks in val definitions.
  "-Xmixin-force-forwarders:false",                // Only generate mixin forwarders required for program correctness.
  "-Yjar-compression-level", "9",                  // compression level to use when writing jar files
  "-Ymacro-annotations",                           // Enable support for macro annotations, formerly in macro paradise.
  "-Ypatmat-exhaust-depth", "off",
)

lazy val root = Project("root", file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion        := Ver.scala,
    scalacOptions      ++= scalacFlags,
    libraryDependencies += Dep.scalaJsReact.value,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= { _.withModuleSplitStyle(ModuleSplitStyle.SmallestModules) },
    scalaJSLinkerConfig ~= { _.withSourceMap(true) },
    Compile / scalaJSModuleInitializers += {
      ModuleInitializer.mainMethod("demo.Main", "main") //.withModuleID("main")
    }
  )
