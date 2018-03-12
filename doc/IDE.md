# IDEs

Here are tips to improve the experience of developing scalajs-react based
applications in various IDEs.

It's a new page and a little bereft so please don't hesitate to contribute!


## Intellij

#### New component template

1. Go to File → Settings → Editor → Live Templates → Scala
1. Click the green `+` button in the top right and select *Live Template*
1. At the bottom where it says *No applicable contexts yet.* click Define → Scala → Code
1. In the *Abbreviation* field, type `newcomp` (or whatever you want to use to invoke the template from your code)
1. In the *Description* field, type `New React component`
1. In the *Template text* field, paste the following:

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._

object $NAME$ {

  final case class Props() {
    @inline def render: VdomElement = Component(this)
  }

  //implicit val reusabilityProps: Reusability[Props] =
  //  Reusability.derive

  final class Backend($: BackendScope[Props, Unit]) {
    def render(p: Props): VdomElement =
      <.div
  }

  val Component = ScalaComponent.builder[Props]("$NAME$")
    .renderBackend[Backend]
    //.configure(Reusability.shouldComponentUpdate)
    .build
}
```

Once you're done, when you want to create a new component, create a new file,
type `newcomp` and hit enter when Intellij offers you the template.

#### New component template (with State)

Similar to above, here is a `newcompS` template that creates a new component
with state. As is recommended generally, the component shouldn't be stateful from
React's perspective but referentially-transparent with state externalised to the
top of the page/SPA component tree and provided through the props.

```scala
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._

object $NAME$ {

  final case class Props(state: StateSnapshot[State]) {
    @inline def render: VdomElement = Component(this)
  }

  //implicit val reusabilityProps: Reusability[Props] =
  //  Reusability.derive

  final case class State()

  object State {
    def init: State =
      State()

    //implicit val reusability: Reusability[State] =
    //  Reusability.derive
  }

  final class Backend($: BackendScope[Props, Unit]) {
    def render(p: Props): VdomElement =
      <.div
  }

  val Component = ScalaComponent.builder[Props]("$NAME$")
    .renderBackend[Backend]
    //.configure(Reusability.shouldComponentUpdate)
    .build
}
```
