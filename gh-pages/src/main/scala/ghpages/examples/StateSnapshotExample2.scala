package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide

object StateSnapshotExample2 {

  def content = SingleSide.Content(source, Main())

  val source = GhPagesMacros.exampleSource

  def Main = {
    import japgolly.scalajs.react._
    import japgolly.scalajs.react.vdom.html_<^._

    ScalaComponent.static(
      <.div(
        <.p(
          <.code("StateSnapshot"), " is [an immediate value] and [a means of modifying it], that you can pass around.",
          " This is actually the recommended way of using scalajs-react. Have one stateful component that is the root/top component, and have all children components be stateless and request ",
          <.code("StateSnapshot"), "s in their Props. Obviously components should be decoupled and only ask for state that they care about.",
          " This example demonstrates how to do just that. Stateless, decoupled components all the way down."),
        <.br,
        <.div(
          "This demonstrates two ways of accomplishing this:",
          <.ol(
            <.li("Create Reusable lenses and call ", <.code(".withReuse.zoomL(lens)")),
            <.li("Prepare a reusable setter using ", <.code("StateSnapshot.withReuse.zoomL.prepareViaProps")))),
        <.br,
        <.div(
          "This example contains:",
          <.ul(
            <.li(<.code("Data"), " - a case class with an Int field and a String field"),
            <.li(<.code("IntEditor"), " - a reusable stateless component that allows users to modify an Int"),
            <.li(<.code("TextEditor"), " - a reusable stateless component that allows users to modify a String"),
            <.li(<.code("Middle"), " - a medium-level stateless component in your app. Accepts Data, calls IntEditor & TextEditor."),
            <.li(<.code("Top"), " - the top/root stateful component in your app"))),
        <.br,
        <.div(
          "Important points:",
          <.ul(
            <.li("The TextEditor doesn't update when the IntEditor makes a change, and vice-versa"),
            <.li("IntEditor, TextEditor and Middle all only ask for exactly what they need with no knowledge of where the state comes from"),
            <.li("The Top component is free to add more state (eg. for more pages in your SPA) without affecting anything outside of Top"),
            <.li("Components are completely decoupled"),
            <.li("In a real-world project, components typically declare their own state and request it as a ", <.code("StateSnapshot"),
              " with the top-level state often being something like ", <.code("case class GlobalState(page1: Page1.State, page2: Page2.State, ...)")),
            <.li("Following this pattern makes it very easy to share state between SPA pages or components"),
            <.li("Following this pattern makes it very easy to allow users to change back and forth between pages without losing state"),
          )),
        <.div(
          ^.marginTop := "4rem",
          Top.Comp())))
  }

  // EXAMPLE:START
  import japgolly.scalajs.react._
  import japgolly.scalajs.react.vdom.html_<^._
  import japgolly.scalajs.react.MonocleReact._
  import japgolly.scalajs.react.extra._
  import monocle.macros.Lenses

  val IntEditor = ScalaComponent.builder[StateSnapshot[Int]]
    .render_P { stateSnapshot =>
      <.span(
        ^.paddingLeft := "6ex", // leave some space for ReusabilityOverlay
        <.button(
          s"Current value is ${stateSnapshot.value}. Click to increment",
          ^.onClick --> stateSnapshot.modState(_ + 1)))
    }
    .configure(ReusabilityOverlay.install)
    .build

  // -----------------------------------------------------------------------------------------------------------------

  val TextEditor = ScalaComponent.builder[StateSnapshot[String]]
    .render_P { stateSnapshot =>
      <.span(
        ^.paddingLeft := "6ex", // leave some space for ReusabilityOverlay
        <.input.text(
          ^.value     := stateSnapshot.value,
          ^.onChange ==> ((e: ReactEventFromInput) => stateSnapshot.setState(e.target.value))))
    }
    .configure(ReusabilityOverlay.install)
    .build

  // -----------------------------------------------------------------------------------------------------------------

  @Lenses
  final case class Data(int: Int, str: String)

  object Data {
    implicit val reusability: Reusability[Data] = Reusability.derive

    // Here we wrap the lenses in Reusable.byRef so that React can compare setState/modState functions and know when its
    // it's got the same lens as a previous render. This is required to make [Method 1] work with Reusability
    object reusableLens {
      val int = Reusable.byRef(Data.int)
      val str = Reusable.byRef(Data.str)
    }
  }

  // -----------------------------------------------------------------------------------------------------------------

  object Middle {

    final case class Props(name: String, ss: StateSnapshot[Data]) {
      @inline def render = Comp(this)
    }

    implicit def reusability: Reusability[Props] =
      Reusability.derive

    final class Backend($: BackendScope[Props, Unit]) {

      // Method 2: StateSnapshot.withReuse.zoomL.prepareViaProps
      // Notice that we're using a normal lens here instead of a Reusable[lens]
      private val ssStrFn =
        StateSnapshot.withReuse.zoomL(Data.str).prepareViaProps($)(_.ss)

      def render(p: Props): VdomElement = {

        // Method 1: ss.withReuse.zoomStateL
        val ssI: StateSnapshot[Int] =
          p.ss.withReuse.zoomStateL(Data.reusableLens.int)

        // Method 2: StateSnapshot.withReuse.zoomL.prepareViaProps
        val ssS: StateSnapshot[String] =
          ssStrFn(p.ss.value)

        <.div(
          <.h3(p.name),
          <.div("IntEditor: ", IntEditor(ssI)),
          <.div("TextEditor: ", TextEditor(ssS), ^.marginTop := "0.6em"))
      }
    }

    val Comp = ScalaComponent.builder[Props]
      .renderBackend[Backend]
      .configure(Reusability.shouldComponentUpdate)
      .build
  }

  // -----------------------------------------------------------------------------------------------------------------

  object Top {

    final class Backend($: BackendScope[Unit, Data]) {
      private val setStateFn =
        StateSnapshot.withReuse.prepareVia($)

      def render(state: Data): VdomElement = {
        val ss = setStateFn(state)
        Middle.Props("Demo", ss).render
      }
    }

    val Comp = ScalaComponent.builder[Unit]
      .initialState(Data(123, "hello"))
      .renderBackend[Backend]
      .build
  }
  // EXAMPLE:END
}
