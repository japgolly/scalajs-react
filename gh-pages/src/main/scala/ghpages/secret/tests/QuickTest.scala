package ghpages.secret.tests

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scala.collection.immutable.TreeMap

object QuickTest {

  sealed trait Status
  object Status {
    case object NotStarted extends Status
    case object InProgress extends Status

    sealed trait Result extends Status {
      def &&(r: => Result): Result
    }
    case object Pass extends Result {
      override def &&(r: => Result) = r
    }
    final case class Fail(error: String) extends Result {
      override def &&(r: => Result) = this
    }
  }

  final case class Test(name: String,
                        body: AsyncCallback[Status.Result],
                        status: Status,
                        runs: Int) {
    def started(expect: Test): Test =
      if (this eq expect)
        copy(status = Status.InProgress, runs = runs + 1)
      else
        this
  }

  type TestSuite = Map[String, AsyncCallback[Status.Result]]

  final case class TestSuiteBuilder() {
    private var m: TestSuite = Map.empty

    def add(name: String)(tf: => AsyncCallback[Status.Result]): this.type = {
      m = m.updated(name, AsyncCallback.byName(tf))
      this
    }

    def result(): TestSuite =
      m
  }


  final case class Props(testSuite: TestSuite, reps: Int = 10)

  final case class State(tests: TreeMap[String, Test]) {
    def mod(name: String, f: Test => Test): State =
      State(tests.updated(name, f(tests(name))))
  }

  private def init(ts: TestSuite): State =
    State(
      (TreeMap.newBuilder[String, Test] ++=
        ts.toVector.map { case (n, t) => n -> Test(n, t, Status.NotStarted, 0) })
        .result())

  final class Backend($: BackendScope[Props, State]) {

    def startTests: Callback = {

      def completeTest(name: String, r: Status.Result, reps: Int): Callback =
        $.modState(
          _.mod(name, _.copy(status = r)),
          $.state.flatMap { s =>
            val t = s.tests(name)
            if (t.runs < reps && t.status == Status.Pass)
              startTest(t, reps)
            else
              Callback.empty
          }
        )

      def startTest(t: Test, reps: Int): Callback =
        t.body
          .attempt
          .map {
            case Right(r) => r
            case Left(e) => Status.Fail(e.getMessage)
          }
          .flatMap(completeTest(t.name, _, reps).asAsyncCallback)
          .toCallback
          .flatMap(_ => $.modState(_.mod(t.name, _.started(t))))

      for {
        p         <- $.props
        s         <- $.state
        notStarted = s.tests.valuesIterator.filter(_.status == Status.NotStarted).toVector
        _         <- Callback.traverse(notStarted)(startTest(_, p.reps))
      } yield ()
    }

    private val tdShared =
      <.td(
        ^.border := "solid 1px #444",
        ^.padding := "0.2em 1ex",
        ^.fontSize := "125%")

    private val tdName = tdShared(^.width := "20vw")
    private val tdRuns = tdShared(^.width := "7ex")
    private val tdResult = tdShared(^.width := "60vw")

    private def renderTest(t: Test): VdomElement = {

      val colour: TagMod = t.status match {
        case Status.NotStarted => ^.color := "#666"
        case Status.InProgress => ^.color := "#007ba8"
        case Status.Pass       => ^.color := "#080"
        case Status.Fail(_)    => ^.color := "#c00"
      }

      val status = t.status match {
        case Status.NotStarted => ""
        case Status.InProgress => "In progress..."
        case Status.Pass       => "Pass"
        case Status.Fail(e)    => e
      }

      <.tr(
        tdName(colour, t.name),
        tdRuns(colour, t.runs),
        tdResult(colour, status))
    }

    def render(s: State): VdomElement = {
      <.table(
        ^.borderCollapse.collapse,
        <.thead(
          <.tr(
            <.th("Name"),
            <.th("Runs"),
            <.th("Result / Status"))),
        <.tbody(
          s.tests.valuesIterator.toTagMod(renderTest)))
    }
  }

  val Component = ScalaComponent.builder[Props]("AsyncTest")
    .initialStateFromProps(p => init(p.testSuite))
    .renderBackend[Backend]
    .componentDidMount(_.backend.startTests)
    .build
}
