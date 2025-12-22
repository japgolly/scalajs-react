package japgolly.scalajs.react

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import utest.TestSuite

abstract class AsyncTestSuite extends TestSuite {
  private val Tag = implicitly[ClassTag[AsyncCallback[Any]]]

  override def utestWrap(path: Seq[String], runBody: => Future[Any])(implicit ec: ExecutionContext): Future[Any] = {
    runBody flatMap {
      case Tag(ac) => ac.unsafeToFuture()
      case other => super.utestWrap(path, Future.successful(other))
    }
  }
}
