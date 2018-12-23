package japgolly.scalajs.react.extra

import japgolly.scalajs.react.{AsyncCallback, Callback, CallbackKleisli, CallbackTo}
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.{ProgressEvent, XMLHttpRequest}
import scala.scalajs.js

/** Purely-functional AJAX that runs a [[Callback]], and accepts XHR-callbacks as [[Callback]] instances.
  *
  * See https://japgolly.github.io/scalajs-react/#examples/ajax for a demo
  */
object Ajax2 {

  def deriveErrorMessage(xhr: XMLHttpRequest): String = {
//    var err = Option(xhr.statusText).map(_.trim).filter(_.nonEmpty) getOrElse xhr.status.toString
//    Option(xhr.responseText).map(_.trim).filter(_.nonEmpty).foreach(r => err = s"$err. Response: $r")
//    err
//    val resp = Option(xhr.responseText).map(_.trim).filter(_.nonEmpty).map("Response: " + _)
    s"[${xhr.status}] Response: ${xhr.responseText}"
  }

  // ===================================================================================================================
  // Step 1

  def apply(method: String, url: String): Step1 =
    new Step1(CallbackKleisli.lift(_.open(method, url, true)))

  def apply(method: String, url: String, user: String, password: String): Step1 =
    new Step1(CallbackKleisli.lift(_.open(method, url, true, user, password)))

  def get(url: String): Step1 =
    apply("GET", url)

  def post(url: String): Step1 =
    apply("POST", url)

  private type Ajax[A] = CallbackKleisli[XMLHttpRequest, A]

  final class Step1(init: Ajax[Unit]) {
    def and(f: XMLHttpRequest => Unit): Step1 =
      new Step1(init >> CallbackKleisli.lift(f))

    def setRequestHeader(header: String, value: String): Step1 =
      and(_.setRequestHeader(header, value))

    def setRequestContentTypeJson: Step1 =
      setRequestHeader("Content-Type", "application/json")

    def setRequestContentTypeJson(charset: String): Step1 =
      setRequestHeader("Content-Type", "application/json;charset=" + charset)

    def setRequestContentTypeJsonUtf8: Step1 =
      setRequestContentTypeJson("UTF-8")

    def send: Step2 =
      new Step2(
        init >> CallbackKleisli.lift(_.send()),
        None, None, None, None)

    def send(requestBody: js.Any): Step2 =
      new Step2(
        init >> CallbackKleisli.lift(_.send(requestBody)),
        None, None, None, None)
  }

  // ===================================================================================================================
  // Step 2

  final class Step2(begin: Ajax[Unit],
                    onreadystatechange: Option[Ajax[Unit]],
                    ontimeout: Option[Ajax[Unit]],
                    onprogress: Option[CallbackKleisli[(XMLHttpRequest, ProgressEvent), Unit]],
                    onuploadprogress: Option[CallbackKleisli[(XMLHttpRequest, ProgressEvent), Unit]]) {

    private def copy(begin: Ajax[Unit] = begin,
                     onreadystatechange: Option[Ajax[Unit]] = onreadystatechange,
                     ontimeout: Option[Ajax[Unit]] = ontimeout,
                     onprogress: Option[CallbackKleisli[(XMLHttpRequest, ProgressEvent), Unit]] = onprogress,
                     onuploadprogress: Option[CallbackKleisli[(XMLHttpRequest, ProgressEvent), Unit]] = onuploadprogress): Step2 =
      new Step2(
        begin = begin,
        onreadystatechange = onreadystatechange,
        ontimeout = ontimeout,
        onprogress = onprogress,
        onuploadprogress = onuploadprogress)

    def onReadyStateChange(f: XMLHttpRequest => Callback): Step2 =
      _onReadyStateChange(CallbackKleisli(f))

    private def _onReadyStateChange(f: Ajax[Unit]): Step2 =
      copy(onreadystatechange = Some(f <<? onreadystatechange))

    def withTimeout(millis: Double, f: XMLHttpRequest => Callback): Step2 =
      copy(
        begin = begin << CallbackKleisli.lift(_.timeout = millis),
        ontimeout = Some(CallbackKleisli(f) <<? ontimeout))

    // TODO Prevent before withTimeout
    def onTimeout(f: XMLHttpRequest => Callback): Step2 =
      copy(ontimeout = Some(CallbackKleisli(f) <<? ontimeout))

    @deprecated("Use .onDownloadProgress instead", "1.2.1")
    def onProgress(f: (XMLHttpRequest, ProgressEvent) => Callback): Step2 = onDownloadProgress(f)

    def onDownloadProgress(f: (XMLHttpRequest, ProgressEvent) => Callback): Step2 =
      copy(onprogress = Some(CallbackKleisli(f.tupled) <<? onprogress))

    def onUploadProgress(f: (XMLHttpRequest, ProgressEvent) => Callback): Step2 =
      copy(onuploadprogress = Some(CallbackKleisli(f.tupled) <<? onuploadprogress))

    def onComplete: AsyncCallback[XMLHttpRequest] =
      onComplete(_ => true)

    def onComplete(success: XMLHttpRequest => Boolean): AsyncCallback[XMLHttpRequest] = {
      def registerU(k: Ajax[Unit])(set: (XMLHttpRequest, js.Function1[Any, Unit]) => Unit): Ajax[Unit] =
        CallbackKleisli.lift(xhr => set(xhr, Callback.byName(k(xhr)).toJsFn1))

      def register_(cb: Option[Ajax[Unit]])(set: (XMLHttpRequest, js.Function1[Any, Unit]) => Unit): Ajax[Unit] =
        cb match {
          case Some(k) => registerU(k)(set)
          case None    => CallbackKleisli.unit
        }

      def registerE[E](cb: Option[CallbackKleisli[(XMLHttpRequest, E), Unit]])
                      (set: (XMLHttpRequest, js.Function1[E, Unit]) => Unit): Ajax[Unit] =
        cb match {
          case Some(k) => CallbackKleisli.lift(xhr => set(xhr, k.contramap[E]((xhr, _)).toJsFn))
          case None    => CallbackKleisli.unit
        }

      AsyncCallback.first { cc =>

        val fail: Throwable => Callback =
          t => cc(Left(t))

        val main: Ajax[Unit] =
          CallbackKleisli(xhr => cc(
            if (success(xhr))
              Right(xhr)
            else
              Left(AjaxException(xhr))))

        val onreadystatechange: Ajax[Unit] =
          (main <<? this.onreadystatechange).mapCB(_.handleError(fail))

        val onerror: Ajax[Unit] =
          CallbackKleisli(xhr => cc(Left(AjaxException(xhr))))

        val start: Ajax[Unit] =
          registerU(onreadystatechange)(_.onreadystatechange = _) >>
          registerU(onerror)(_.onerror = _) >>
          register_(ontimeout)(_.ontimeout = _) >>
          registerE(onprogress)(_.onprogress = _) >>
          registerE(onuploadprogress)(_.upload.onprogress = _) >>
          begin

        (newXHR >>= start.run).handleError(fail)
      }
    }

//    def onComplete(f: XMLHttpRequest => Callback): Step2 =
//      _onReadyStateChange(CallbackKleisli(f).when_(_.readyState == XMLHttpRequest.DONE))
//
//    def onCompleteHandleStatusFn(success: Int => Boolean)(f: Either[AjaxException, XMLHttpRequest] => Callback): Step2 =
//      _onCompleteHandle(xhr => success(xhr.status))(f)
//
//    def onCompleteHandleStatusIs(success: Int)(f: Either[AjaxException, XMLHttpRequest] => Callback): Step2 =
//      onCompleteHandleStatusFn(_ == success)(f)
//
//    def onCompleteHandle(f: Either[AjaxException, XMLHttpRequest] => Callback): Step2 =
//      onCompleteHandleStatusFn(s => (s >= 200 && s < 300) || s == 304)(f)
  }

  private val newXHR = CallbackTo(new XMLHttpRequest())
}