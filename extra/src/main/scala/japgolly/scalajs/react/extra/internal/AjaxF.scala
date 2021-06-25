package japgolly.scalajs.react.extra.internal

import japgolly.scalajs.react.util.Effect
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.{ProgressEvent, XMLHttpRequest}
import scala.scalajs.js
import scala.util.{Failure, Success}

/** Purely-functional AJAX.
  *
  * For a demo, see
  *   - https://japgolly.github.io/scalajs-react/#examples/ajax-1
  *   - https://japgolly.github.io/scalajs-react/#examples/ajax-2
  */
class AjaxF[F[_], Async[_]](implicit F: Effect.Sync[F], Async: Effect.Async[Async]) {

  def deriveErrorMessage(xhr: XMLHttpRequest): String = {
//    var err = Option(xhr.statusText).map(_.trim).filter(_.nonEmpty) getOrElse xhr.status.toString
//    Option(xhr.responseText).map(_.trim).filter(_.nonEmpty).foreach(r => err = s"$err. Response: $r")
//    err
//    val resp = Option(xhr.responseText).map(_.trim).filter(_.nonEmpty).map("Response: " + _)
    s"[${xhr.status}] Response: ${xhr.responseText}"
  }

  /** Generic HTTP code validation */
  def isStatusSuccessful(status: Int): Boolean =
    (status >= 200 && status < 300) || status == 304

  // ===================================================================================================================
  // Step 1

  def apply(method: String, url: String): Step1 =
    new Step1(xhr => F.delay(xhr.open(method, url, true)))

  def apply(method: String, url: String, user: String, password: String): Step1 =
    new Step1(xhr => F.delay(xhr.open(method, url, true, user, password)))

  def get(url: String): Step1 =
    apply("GET", url)

  def post(url: String): Step1 =
    apply("POST", url)

  private type Ajax[A] = XMLHttpRequest => F[A]

  final class Step1(init: Ajax[Unit]) {
    def and(f: XMLHttpRequest => Unit): Step1 =
      new Step1(xhr => F.chain(init(xhr), F.delay(f(xhr))))

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
        xhr => F.chain(init(xhr), F.delay(xhr.send())),
        None, None, None, None)

    def send(requestBody: js.Any): Step2 =
      new Step2(
        xhr => F.chain(init(xhr), F.delay(xhr.send(requestBody))),
        None, None, None, None)
  }

  // ===================================================================================================================
  // Step 2

  private type OnProgress = (XMLHttpRequest, ProgressEvent) => F[Unit]

  final class Step2(begin             : Ajax[Unit],
                    onreadystatechange: Option[Ajax[Unit]],
                    ontimeout         : Option[Ajax[Unit]],
                    onprogress        : Option[OnProgress],
                    onuploadprogress  : Option[OnProgress]) {

    private def copy(begin             : Ajax[Unit]         = begin,
                     onreadystatechange: Option[Ajax[Unit]] = onreadystatechange,
                     ontimeout         : Option[Ajax[Unit]] = ontimeout,
                     onprogress        : Option[OnProgress] = onprogress,
                     onuploadprogress  : Option[OnProgress] = onuploadprogress): Step2 =
      new Step2(
        begin              = begin,
        onreadystatechange = onreadystatechange,
        ontimeout          = ontimeout,
        onprogress         = onprogress,
        onuploadprogress   = onuploadprogress)

    private def optionalBefore[A, B, C](before: Option[A => F[B]], last: A => F[C]): A => F[C] =
      before.fold(last)(b => (a: A) => F.chain(b(a), last(a)))

    private def optionalBefore[A1, A2, B, C](before: Option[(A1, A2) => F[B]], last: (A1, A2) => F[C]): (A1, A2) => F[C] =
      before.fold(last)(b => (a1: A1, a2: A2) => F.chain(b(a1, a2), last(a1, a2)))

    def withTimeout(millis: Double, f: XMLHttpRequest => F[Unit]): Step2 =
      copy(
        begin     = xhr => F.chain(F.delay(xhr.timeout = millis), begin(xhr)),
        ontimeout = Some(optionalBefore(ontimeout, f)),
      )

    // TODO Prevent before withTimeout
    def onTimeout(f: XMLHttpRequest => F[Unit]): Step2 =
      copy(ontimeout = Some(optionalBefore(ontimeout, f)))

    def onDownloadProgress(f: OnProgress): Step2 =
      copy(onprogress = Some(optionalBefore(onprogress, f)))

    def onUploadProgress(f: OnProgress): Step2 =
      copy(onuploadprogress = Some(optionalBefore(onuploadprogress, f)))

    private def onReadyStateChange(f: Ajax[Unit]): Step2 =
      copy(onreadystatechange = Some(optionalBefore(onreadystatechange, f)))

    private def onCompleteKleisli(f: Ajax[Unit]): Ajax[Unit] =
      xhr => F.when_(xhr.readyState == XMLHttpRequest.DONE)(f(xhr))

    def onComplete(f: XMLHttpRequest => F[Unit]): Step2 =
      onReadyStateChange(onCompleteKleisli(f))

    def validateResponse(isValid: XMLHttpRequest => Boolean): (AjaxException => F[Unit]) => Step2 =
      onFailure => onComplete(xhr =>
        F.unless_(isValid(xhr))(onFailure(AjaxException(xhr))))

    def validateStatus(isValidStatus: Int => Boolean): (AjaxException => F[Unit]) => Step2 =
      validateResponse(xhr => isValidStatus(xhr.status))

    def validateStatusIs(expectedStatus: Int): (AjaxException => F[Unit]) => Step2 =
      validateStatus(_ == expectedStatus)

    def validateStatusIsSuccessful: (AjaxException => F[Unit]) => Step2 =
      validateStatus(isStatusSuccessful)

    private def registerU(k: Ajax[Unit])(set: (XMLHttpRequest, js.Function1[Any, Unit]) => Unit): Ajax[Unit] =
      xhr => F.delay(set(xhr, _ => F.runSync(F.suspend(k(xhr)))))

    private def register_(cb: Option[Ajax[Unit]])(set: (XMLHttpRequest, js.Function1[Any, Unit]) => Unit): Ajax[Unit] =
      cb match {
        case Some(k) => registerU(k)(set)
        case None    => _ => F.empty
      }

    private def registerE[E](cb: Option[(XMLHttpRequest, E) => F[Unit]])
                            (set: (XMLHttpRequest, js.Function1[E, Unit]) => Unit): Ajax[Unit] =
      cb match {
        case Some(k) => xhr => F.delay(set(xhr, e => F.runSync(k(xhr, e))))
        case None    => _ => F.empty
      }

    private def registerSecondaryCallbacks: Ajax[Unit] =
      xhr =>
        F.chain(
          register_(ontimeout)(_.ontimeout = _)(xhr),
          registerE(onprogress)(_.onprogress = _)(xhr),
          registerE(onuploadprogress)(_.upload.onprogress = _)(xhr),
        )

    def asCallback: F[Unit] =
      F.flatMap(newXHR)(xhr =>
        F.chain(
          register_(onreadystatechange)(_.onreadystatechange = _)(xhr),
          registerSecondaryCallbacks(xhr),
          begin(xhr),
        )
      )

    def asAsyncCallback: Async[XMLHttpRequest] =
      Async.first[XMLHttpRequest] { cc =>

        val fail: Throwable => F[Unit] =
          t => F.fromJsFn0(cc(Failure(t)))

        val onreadystatechange: Ajax[Unit] = {
          val complete = onCompleteKleisli(xhr => F.fromJsFn0(cc(Success(xhr))))
          xhr => {
            val main = F.suspend(F.chain(
              this.onreadystatechange.fold(F.empty)(_(xhr)),
              complete(xhr),
            ))
            F.handleError(main)(fail)
          }
        }

        val onerror: Ajax[Unit] =
          xhr => fail(AjaxException(xhr))

        val start: Ajax[Unit] =
          xhr => F.chain(
            registerU(onreadystatechange)(_.onreadystatechange = _)(xhr),
            registerU(onerror)(_.onerror = _)(xhr),
            registerSecondaryCallbacks(xhr),
            begin(xhr),
          )

        val main1 = F.flatMap(newXHR)(start)
        val main2 = F.handleError(main1)(fail)
        F.toJsFn(main2)
      }
  }

  private val newXHR = F.delay(new XMLHttpRequest())
}
