package japgolly.scalajs.react.test.internal

import japgolly.scalajs.react.test.ReactTestUtilsConfig.aroundReact
import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.ImplicitUnit
import japgolly.scalajs.react.util.syntax._
import scala.concurrent.{ExecutionContext, Future}

object WithDsl {

  def apply[A, I](f: (I, Cleanup) => A): WithDsl[A, I] =
    new WithDsl[A, I] {
      override val setup = f
    }

  def apply[A](create: => A)(destroy: A => Unit): WithDsl[A, ImplicitUnit] =
    apply[A, ImplicitUnit] { (_, cleanup) =>
      val a = create
      cleanup.register(destroy(a))
      a
    }

  def aroundReactAsync[F[_], A](body: F[A])(implicit F: Async[F]): F[A] = {
    val start = F.delay {
      val stop = aroundReact.start()
      F.delay(stop())
    }
    F.flatMap(start) { stop =>
      F.finallyRun(body, stop)
    }
  }

  def aroundReactFuture[A](body: => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    val stop = aroundReact.start()
    val f    = body
    f.onComplete { _ => stop() }
    f
  }

  def attemptFuture[A](f: => Future[A]): Future[A] =
    try f catch { case err: Exception => Future.failed(err) }

  class Cleanup {
    private var fns = () => ()

    def register(f: => Unit): Unit = {
      val g = fns
      fns = () => { f; g() }
    }

    def apply(): Unit =
      fns()
  }
}

// =====================================================================================================================

/** DSL for working with managed resources.
  *
  * @tparam A The resource type.
  * @tparam I Type of an implicit value required on resource use.
  *
  * @since 2.2.0
  */
trait WithDsl[A, I] { self =>
  import WithDsl._

  val setup: (I, Cleanup) => A

  protected def init(i: I): (A, Cleanup) = {
    val cleanup = new Cleanup
    val a = setup(i, cleanup)
    (a, cleanup)
  }

  def apply[B](use: A => B)(implicit i: I): B = {
    val (a, cleanup) = init(i)
    try
      use(a)
    finally
      cleanup()
  }

  def async[G[_], B](use: A => G[B])(implicit i: I, G: Async[G]): G[B] =
    aroundReactAsync {
      for {
        x <- G.delay(init(i))
        b <- G.finallyRun(use(x._1), G.delay(x._2()))
      } yield b
    }

  def future[B](use: A => Future[B])(implicit i: I, ec: ExecutionContext): Future[B] =
    aroundReactFuture {
      val (a, cleanup) = init(i)
      attemptFuture(use(a)).andThen { case _ => cleanup() }
    }

  def map[B](f: A => B): WithDsl[B, I] =
    mapFull { (a, _) => f(a) }

  def mapResourse[B](f: A => B)(cleanup: B => Unit): WithDsl[B, I] =
    mapFull { (a, c) =>
      val b = f(a)
      c.register(cleanup(b))
      b
    }

  private def mapFull[B](f: (A, Cleanup) => B): WithDsl[B, I] =
    new WithDsl[B, I] {
      override val setup = (i, cleanup) => {
        val a = self.setup(i, cleanup)
        f(a, cleanup)
      }
    }

  def tap[B](f: A => B): WithDsl[A, I] =
    mapFull { (a, _) => f(a); a }
}
