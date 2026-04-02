package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtilsConfig._
import japgolly.scalajs.react.util.Effect
import scala.annotation.nowarn
import utest._

object ReactTestUtilsConfigTest extends AsyncTestSuite {

  @nowarn
  private def api[F[_]: Effect](): Unit = {
    // Make sure Scala 2 & 3 have the same API
    AroundReact.id
    AroundReact.fatalReactWarnings
    aroundReact.get
    (aroundReact.set _): (AroundReact => Unit)
    (i: Int) => aroundReact[F, Int](Effect[F].pure(i)): F[Int]
  }

  override def tests = Tests {

    "api" - api[AsyncCallback]()

  }
}
