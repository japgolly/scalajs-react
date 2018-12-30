package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object AjaxExample2 {

  def content = SingleSide.Content(source, Main)

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START
  import japgolly.scalajs.react.extra.Ajax
  import scala.scalajs.js
  import scala.scalajs.js.JSON
  import scala.scalajs.js.annotation.JSName

  def login: AsyncCallback[Unit] =
    Ajax("POST", "https://reqres.in/api/login")
      .setRequestContentTypeJsonUtf8
      .send("""{ "email": "peter@klaven", "password": "cityslicka" }""")
      .validateStatusIsSuccessful(Callback.error) // Ensure (status >= 200 && status < 300) || status == 304
      .asAsyncCallback
      .void

  @js.native
  trait UserAjaxResponse extends js.Object {
    val data: User
  }

  @js.native
  trait User extends js.Object {
    val id: Int
    @JSName("first_name") val firstName: String
    @JSName("last_name") val lastName: String
    val avatar: String
  }

  def loadUser(userId: Int): AsyncCallback[User] =
    Ajax("GET", s"https://reqres.in/api/users/$userId")
      .setRequestContentTypeJsonUtf8
      .send
      .validateStatusIs(200)(Callback.error)
      .asAsyncCallback
      .map(xhr => JSON.parse(xhr.responseText).asInstanceOf[UserAjaxResponse]) // You'd normally be safer than this
      .map(_.data)                                                             // outside of a quick demo.

  def loadUsers(userIds: List[Int]): AsyncCallback[List[User]] =
    AsyncCallback.traverse(userIds)(loadUser)

  // This composition of AsyncCallbacks is like the composition of Futures, or chaining `then` on JS promises.
  // The first step is an AJAX POST to login.
  // After login completes successfully, we do a number of AJAX GETs in parallel to retrieve user profiles.
  // When all user profiles have been retrieved successfully, we create some DOM to display them.
  def loadAndRenderUsers: AsyncCallback[VdomElement] =
    for {
      _     <- login
      users <- loadUsers(List(1, 3, 4, 7, 10, 11))
    } yield renderUsers(users)

  def renderUsers(users: List[User]): VdomElement = {
    val th = <.th(^.border := "#666 solid 1px", ^.padding := "0.5em 0.8em")
    val td = <.td(^.border := "#666 solid 1px")
    <.table(
      <.thead(
        <.tr(
          th("ID"),
          th("First Name"),
          th("Last Name"),
          th("Avatar"))),
      <.tbody(
        users.toTagMod(user =>
          <.tr(
            td(^.padding := "0.5em", user.id),
            td(^.padding := "0.5em", user.firstName),
            td(^.padding := "0.5em", user.lastName),
            td(<.img(^.src := user.avatar))))))
  }

  def onError(error: Throwable): AsyncCallback[VdomElement] =
    AsyncCallback.point {
      error.printStackTrace()
      <.div(^.color.red, ^.fontSize := "120%",
        <.div("An error occurred."),
        <.div(error.toString))
    }

  val Main = React.Suspense(
    fallback  = <.div(^.color := "#33c", ^.fontSize := "150%", "AJAX in progress. Loading..."),
    asyncBody = loadAndRenderUsers.handleError(onError))

  // EXAMPLE:END
}
