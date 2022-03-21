package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide

object CatsEffectExample {

  // EXAMPLE:START

  import cats.effect._
  import japgolly.scalajs.react._
  import japgolly.scalajs.react.vdom.html_<^._

  // Cats Effect example code
  // ========================

  final case class Logger(log: String => SyncIO[Unit]) {
    def apply[A](name: String, effect: IO[A]): IO[A] =
      for {
        _ <- log(s"[$name] Starting").to[IO]
        a <- effect
        _ <- log(s"[$name] Completed.").to[IO]
      } yield a
  }

  // Hooks example
  // =============

  final case class CounterProps(initialCount: Int, logger: Logger)

  val Counter = ScalaFnComponent.withHooks[CounterProps]
    .useStateBy(_.initialCount)
    .render { (props, state) =>

      val inc: SyncIO[Unit] =
        // Depending on which scalajs-react modules you're using, you'll use one of the following:
        //
        // 1. If you're using "core-ext-cats_effect" and "core", then:
        state.withEffect[SyncIO].modState(_ + 1)
        //
        // 2. If you're using "core-bundle-cats_effect" instead of "core",
        //    then Cats Effect types are the defaults and you'd use:
        // state.modState(_ + 1)

      val incAndLog: IO[Unit] =
        props.logger("counter", inc.to[IO])

      <.div(
        <.div("Counter: ", state.value),
        <.button("Increment", ^.onClick --> incAndLog),
        // Here we supply an IO[Unit] directly ^^^^^^
      )
    }

  // Class Component example
  // =======================

  final class CounterAndLog($: BackendScope[Unit, String]) {

    private val logger =
      // As mentioned above, `.withEffect[SyncIO]` isn't needed when you've chosen Cats Effect as your default effect type
      Logger(str => $.withEffect[SyncIO].modState(_ + "\n" + str))

    private val counter =
      Counter(CounterProps(0, logger))

    def render(state: String): VdomNode = {
      <.div(
        counter,
        <.pre(
          ^.marginTop := 0.5.em,
          ^.width := 40.ex,
          ^.height := 20.em,
          ^.border := "1px solid",
          state,
        )
      )
    }
  }

  val CounterAndLog = ScalaComponent.builder[Unit]
    .initialState("Ready.")
    .renderBackend[CounterAndLog]
    .build

  // What about mounting?
  // ====================
  // Because mounting a component to DOM is something you only do once at the start of an application,
  // there's no effectful support. If you're using `IOApp` or similar, you'd just wrap the mounting
  // line of code in `IO { ... }`.

  // EXAMPLE:END

  def content = SingleSide.Content(source, main())

  lazy val main = addIntro(CounterAndLog.withKey(_)(), _(
    ^.marginBottom := "2em",
    "There are two ways of using ",
    <.a(^.href := "https://typelevel.org/cats-effect", "Cats Effect"),
    " directly with scalajs-react:",
    <.ol(
      <.li("Adding the ", <.code("core-ext-cats_effect"), " module before ", <.code("core"), " in your sbt dependencies. This adds support for Cats Effect but the default effect types (when scalajs-react provides ", <.em("you"), " with effects) still defaults to Callback."),
      <.li("Using the ", <.code("core-bundle-cats_effect"), " module instead of ", <.code("core"), " in your sbt dependencies. This configure scalajs-react to use Cats Effect as the default effect types."),
    ),
    "See the scalajs-react ",
    <.a(^.href := "https://github.com/japgolly/scalajs-react/blob/master/doc/MODULES.md", "Modules Guide"),
    " for more detail.",
  ))

  val source = GhPagesMacros.exampleSource
}
