package japgolly.scalajs.react.test

import japgolly.scalajs.react.test.internal._
import japgolly.scalajs.react.util.Effect.Async
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object ReactTestUtilsConfig extends ReactTestUtilsConfigTypes {

  object aroundReact extends AroundReact {

    private def default: AroundReact =
      ReactTestUtilsConfigMacros.aroundReact

    private var value: AroundReact =
      default

    def get: AroundReact =
      value

    def set(v: AroundReact): Unit =
      value = v

    override def start(): () => Unit =
      value.start()

    def async[F[_], A](body: F[A])(implicit F: Async[F]): F[A] = {
      val start = F.delay {
        val stop = ReactTestUtilsConfig.aroundReact.start()
        F.delay(stop())
      }
      F.flatMap(start) { stop =>
        F.finallyRun(body, stop)
      }
    }

    def future[A](body: => Future[A])(implicit ec: ExecutionContext): Future[A] = {
      val stop = ReactTestUtilsConfig.aroundReact.start()
      val f    = body
      f.onComplete { _ => stop() }
      f
    }
  }
}
