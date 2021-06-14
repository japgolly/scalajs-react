package japgolly.scalajs.react.extra

import japgolly.scalajs.react.util.DefaultEffects._
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
object Ajax {

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
    new Step1(xhr => Sync.delay(xhr.open(method, url, true)))

  def apply(method: String, url: String, user: String, password: String): Step1 =
    new Step1(xhr => Sync.delay(xhr.open(method, url, true, user, password)))

  def get(url: String): Step1 =
    apply("GET", url)

  def post(url: String): Step1 =
    apply("POST", url)

  private type Ajax[A] = XMLHttpRequest => Sync[A]

  final class Step1(init: Ajax[Unit]) {
    def and(f: XMLHttpRequest => Unit): Step1 =
      new Step1(xhr => Sync.chain(init(xhr), Sync.delay(f(xhr))))

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
        xhr => Sync.chain(init(xhr), Sync.delay(xhr.send())),
        None, None, None, None)

    def send(requestBody: js.Any): Step2 =
      new Step2(
        xhr => Sync.chain(init(xhr), Sync.delay(xhr.send(requestBody))),
        None, None, None, None)
  }

  // ===================================================================================================================
  // Step 2

  private type OnProgress = (XMLHttpRequest, ProgressEvent) => Sync[Unit]

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

    private def optionalBefore[A, B, C](before: Option[A => Sync[B]], last: A => Sync[C]): A => Sync[C] =
      before.fold(last)(b => (a: A) => Sync.chain(b(a), last(a)))

    private def optionalBefore[A1, A2, B, C](before: Option[(A1, A2) => Sync[B]], last: (A1, A2) => Sync[C]): (A1, A2) => Sync[C] =
      before.fold(last)(b => (a1: A1, a2: A2) => Sync.chain(b(a1, a2), last(a1, a2)))

    def withTimeout(millis: Double, f: XMLHttpRequest => Sync[Unit]): Step2 =
      copy(
        begin     = xhr => Sync.chain(Sync.delay(xhr.timeout = millis), begin(xhr)),
        ontimeout = Some(optionalBefore(ontimeout, f)),
      )

    // TODO Prevent before withTimeout
    def onTimeout(f: XMLHttpRequest => Sync[Unit]): Step2 =
      copy(ontimeout = Some(optionalBefore(ontimeout, f)))

    def onDownloadProgress(f: OnProgress): Step2 =
      copy(onprogress = Some(optionalBefore(onprogress, f)))

    def onUploadProgress(f: OnProgress): Step2 =
      copy(onuploadprogress = Some(optionalBefore(onuploadprogress, f)))

    private def onReadyStateChange(f: Ajax[Unit]): Step2 =
      copy(onreadystatechange = Some(optionalBefore(onreadystatechange, f)))

    private def onCompleteKleisli(f: Ajax[Unit]): Ajax[Unit] =
      xhr => Sync.when_(xhr.readyState == XMLHttpRequest.DONE)(f(xhr))

    def onComplete(f: XMLHttpRequest => Sync[Unit]): Step2 =
      onReadyStateChange(onCompleteKleisli(f))

    def validateResponse(isValid: XMLHttpRequest => Boolean): (AjaxException => Sync[Unit]) => Step2 =
      onFailure => onComplete(xhr =>
        Sync.unless_(isValid(xhr))(onFailure(AjaxException(xhr))))

    def validateStatus(isValidStatus: Int => Boolean): (AjaxException => Sync[Unit]) => Step2 =
      validateResponse(xhr => isValidStatus(xhr.status))

    def validateStatusIs(expectedStatus: Int): (AjaxException => Sync[Unit]) => Step2 =
      validateStatus(_ == expectedStatus)

    def validateStatusIsSuccessful: (AjaxException => Sync[Unit]) => Step2 =
      validateStatus(isStatusSuccessful)

    private def registerU(k: Ajax[Unit])(set: (XMLHttpRequest, js.Function1[Any, Unit]) => Unit): Ajax[Unit] =
      xhr => Sync.delay(set(xhr, _ => Sync.runSync(Sync.suspend(k(xhr)))))

    private def register_(cb: Option[Ajax[Unit]])(set: (XMLHttpRequest, js.Function1[Any, Unit]) => Unit): Ajax[Unit] =
      cb match {
        case Some(k) => registerU(k)(set)
        case None    => _ => Sync.empty
      }

    private def registerE[E](cb: Option[(XMLHttpRequest, E) => Sync[Unit]])
                    (set: (XMLHttpRequest, js.Function1[E, Unit]) => Unit): Ajax[Unit] =
      cb match {
        case Some(k) => xhr => Sync.delay(set(xhr, e => Sync.runSync(k(xhr, e))))
        case None    => _ => Sync.empty
      }

    private def registerSecondaryCallbacks: Ajax[Unit] =
      xhr =>
        Sync.chain(
          register_(ontimeout)(_.ontimeout = _)(xhr),
          registerE(onprogress)(_.onprogress = _)(xhr),
          registerE(onuploadprogress)(_.upload.onprogress = _)(xhr),
        )

    def asCallback: Sync[Unit] =
      Sync.flatMap(newXHR)(xhr =>
        Sync.chain(
          register_(onreadystatechange)(_.onreadystatechange = _)(xhr),
          registerSecondaryCallbacks(xhr),
          begin(xhr),
        )
      )

    def asAsyncCallback: Async[XMLHttpRequest] =
      Async.first[XMLHttpRequest] { cc =>

        val fail: Throwable => Sync[Unit] =
          t => cc(Failure(t))

        val onreadystatechange: Ajax[Unit] =
          xhr =>
            Sync.handleError(
              Sync.chain(
                this.onreadystatechange.fold(Sync.empty)(_(xhr)),
                Sync.fromJsFn0(cc(Success(xhr))),
              )
            )(fail)

        val onerror: Ajax[Unit] =
          xhr => fail(AjaxException(xhr))

        val start: Ajax[Unit] =
          xhr => Sync.chain(
            registerU(onreadystatechange)(_.onreadystatechange = _)(xhr),
            registerU(onerror)(_.onerror = _)(xhr),
            registerSecondaryCallbacks(xhr),
            begin(xhr),
          )

        Sync.handleError(Sync.flatMap(newXHR)(start))(fail)
      }
  }

  private val newXHR = Sync.delay(new XMLHttpRequest())
}
