package ghpages.examples

import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._, vdom.prefix_<^._, ScalazReact._

/** This is the same as TodoExample, modified to demonstrate a more functional programming style. */
object ScalazExample {

  def content = SingleSide.Content(source, TodoApp())

  val source =
    """
      |val TodoList = ReactComponentB[List[String]]("TodoList")
      |  .render(props => {
      |    def createItem(itemText: String) = <.li(itemText)
      |    <.ul(props map createItem)
      |  })
      |  .build
      |
      |case class State(items: List[String], text: String)
      |
      |val ST = ReactS.Fix[State]                              // Let's use a helper so that we don't have to specify the
      |                                                        //   state type everywhere.
      |
      |def acceptChange(e: ReactEventI) =
      |  ST.mod(_.copy(text = e.target.value))                 // A pure state modification. State value is provided when run.
      |
      |def handleSubmit(e: ReactEventI) = (
      |  ST.retM(e.preventDefaultIO)                           // Lift an IO effect into a shape that allows composition
      |                                                        //   with state modification.
      |  >>                                                    // Use >> to compose. It's flatMap (>>=) that ignores input.
      |  ST.mod(s => State(s.items :+ s.text, "")).liftIO      // Here we lift a pure state modification into a shape that
      |)                                                       //   allows composition with IO effects.
      |
      |val TodoApp = ReactComponentB[Unit]("TodoApp")
      |  .initialState(State(Nil, ""))
      |  .renderS(($,_,S) =>                                   // Using renderS instead of render to get $ (`this` in JS).
      |    <.div(
      |      <.h3("TODO"),
      |      TodoList(S.items),
      |      <.form(^.onSubmit ~~> $._runState(handleSubmit))( // In Scalaz mode, only use ~~> for callbacks.
      |        <.input(                                        //   ==> and --> are unsafe.
      |          ^.onChange ~~> $._runState(acceptChange),     // runState runs a state monad and applies the result.
      |          ^.value := S.text),                           // _runState takes a function to a state monad.
      |        <.button("Add #", S.items.length + 1)
      |      )
      |    )
      |  ).buildU
      |""".stripMargin

  val TodoList = ReactComponentB[List[String]]("TodoList")
    .render(props => {
      def createItem(itemText: String) = <.li(itemText)
      <.ul(props map createItem)
    })
    .build

  case class State(items: List[String], text: String)

  val ST = ReactS.Fix[State]                              // Let's use a helper so that we don't have to specify the
                                                          //   state type everywhere.

  def acceptChange(e: ReactEventI) =
    ST.mod(_.copy(text = e.target.value))                 // A pure state modification. State value is provided when run.

  def handleSubmit(e: ReactEventI) = (
    ST.retM(e.preventDefaultIO)                           // Lift an IO effect into a shape that allows composition
                                                          //   with state modification.
    >>                                                    // Use >> to compose. It's flatMap (>>=) that ignores input.
    ST.mod(s => State(s.items :+ s.text, "")).liftIO      // Here we lift a pure state modification into a shape that
  )                                                       //   allows composition with IO effects.

  val TodoApp = ReactComponentB[Unit]("TodoApp")
    .initialState(State(Nil, ""))
    .renderS(($,_,S) =>                                   // Using renderS instead of render to get $ (`this` in JS).
      <.div(
        <.h3("TODO"),
        TodoList(S.items),
        <.form(^.onSubmit ~~> $._runState(handleSubmit))( // In Scalaz mode, only use ~~> for callbacks.
          <.input(                                        //   ==> and --> are unsafe.
            ^.onChange ~~> $._runState(acceptChange),     // runState runs a state monad and applies the result.
            ^.value := S.text),                           // _runState takes a function to a state monad.
          <.button("Add #", S.items.length + 1)
        )
      )
    ).buildU
}
