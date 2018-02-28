package japgolly.scalajs.react.extra

import japgolly.scalajs.react.{Callback, CallbackKleisli, CallbackTo}
import org.scalajs.dom.{ProgressEvent, XMLHttpRequest}
import scala.scalajs.js

/** Purely-functional AJAX that runs a [[Callback]], and accepts XHR-callbacks as [[Callback]] instances.
  *
  * See https://japgolly.github.io/scalajs-react/#examples/ajax for a demo
  */
object Ajax {

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

    def setRequestContentTypeJson: Step1 =
      and(_.setRequestHeader("Content-Type", "application/json"))

    def setRequestContentTypeJson(charset: String): Step1 =
      and(_.setRequestHeader("Content-Type", "application/json;charset=" + charset))

    def setRequestContentTypeJsonUtf8: Step1 =
      setRequestContentTypeJson("UTF-8")

    def send: Step2 =
      new Step2(
        init >> CallbackKleisli.lift(_.send()),
        None, None, None)

    def send(requestBody: js.Any): Step2 =
      new Step2(
        init >> CallbackKleisli.lift(_.send(requestBody)),
        None, None, None)
  }

  // ===================================================================================================================
  // Step 2

  final class Step2(begin: Ajax[Unit],
                    onreadystatechange: Option[Ajax[Unit]],
                    ontimeout: Option[Ajax[Unit]],
                    onprogress: Option[CallbackKleisli[(XMLHttpRequest, ProgressEvent), Unit]]) {

    private def copy(begin: Ajax[Unit] = begin,
                     onreadystatechange: Option[Ajax[Unit]] = onreadystatechange,
                     ontimeout: Option[Ajax[Unit]] = ontimeout,
                     onprogress: Option[CallbackKleisli[(XMLHttpRequest, ProgressEvent), Unit]] = onprogress): Step2 =
      new Step2(
        begin = begin,
        onreadystatechange = onreadystatechange,
        ontimeout = ontimeout,
        onprogress = onprogress)

    def onReadyStateChange(f: XMLHttpRequest => Callback): Step2 =
      _onReadyStateChange(CallbackKleisli(f))

    private def _onReadyStateChange(f: Ajax[Unit]): Step2 =
      copy(onreadystatechange = Some(f <<? onreadystatechange))

    def onComplete(f: XMLHttpRequest => Callback): Step2 =
      _onReadyStateChange(CallbackKleisli(f).when_(_.readyState == XMLHttpRequest.DONE))

    def withTimeout(millis: Double, f: XMLHttpRequest => Callback): Step2 =
      copy(
        begin = begin << CallbackKleisli.lift(_.timeout = millis),
        ontimeout = Some(CallbackKleisli(f) <<? ontimeout))

    // TODO Prevent before withTimeout
    def onTimeout(f: XMLHttpRequest => Callback): Step2 =
      copy(ontimeout = Some(CallbackKleisli(f) <<? ontimeout))

    def onProgress(f: (XMLHttpRequest, ProgressEvent) => Callback): Step2 =
      copy(onprogress = Some(CallbackKleisli(f.tupled) <<? onprogress))

    lazy val asCallback: Callback = {
      def register_(cb: Option[Ajax[Unit]])(set: (XMLHttpRequest, js.Function1[Any, Unit]) => Unit): Ajax[Unit] =
        cb match {
          case Some(k) => CallbackKleisli.lift(xhr => set(xhr, Callback.byName(k(xhr)).toJsFn1))
          case None    => CallbackKleisli.unit
        }

      def registerE[E](cb: Option[CallbackKleisli[(XMLHttpRequest, E), Unit]])
                      (set: (XMLHttpRequest, js.Function1[E, Unit]) => Unit): Ajax[Unit] =
        cb match {
          case Some(k) => CallbackKleisli.lift(xhr => set(xhr, k.curry.apply(xhr).toJsFn))
          case None    => CallbackKleisli.unit
        }

      newXHR >>= (
        register_(onreadystatechange)(_.onreadystatechange = _) >>
        register_(ontimeout)(_.ontimeout = _) >>
        registerE(onprogress)(_.onprogress = _) >>
        begin).run
    }
  }

  private val newXHR = CallbackTo(new XMLHttpRequest())
}