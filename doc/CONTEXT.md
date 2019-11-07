# React Context and example

To see why this is useful, you may want to take a glance at: https://reactjs.org/docs/context.html

Basically, React context allows you to have some global data, that's used throughout the application but not necessarily passed from one component to another.
A perfect example is a User object that contains user preferences, name, etc. Here I'll describe an example that though sparse, is a little closer to "real life" than
something contrived. I'm leaving out some details (such as imports) for the sake of brevity, but I do want to show you that this app will be using:

- Ajax to get the user information from a server
- An AppRouter

## Some model objects
```scala
case User(username: String)

case class MyGlobalState(user: Option[User] = None)

object MyGlobalState {
  val ctx: Context[MyGlobalState] = React.createContext(MyGlobalState())
}
```

## The main application
Nothing terribly exciting here
```scala
object MyContextUsingApp {
  @JSExport
  def main(args: Array[String]): Unit = {
    AppCSS.load()

    Content().renderIntoDOM(dom.document.getElementById("content"))
    ()
  }
}
```
## The top level "content"
```scala
object Content {

  import scala.concurrent.ExecutionContext.Implicits.global

  case class State(myGlobalState: MyGlobalState = MyGlobalState())

  class Backend($ : BackendScope[_, State]) {
    def render(s: State): VdomElement =
      MyGlobalState.ctx.provide(s.myGlobalState) {
        <.div(AppRouter.router())
      }

    def refresh(s: State) =
      Ajax.get(s"https://localhost:8000/whoami")
        .and(_.withCredentials = true)
        .send
        .asAsyncCallback
        .flatMap { xhr ⇒
          try {
            import model.ModelPickler._
            import upickle.default._
            val user = Option(read[User](xhr.responseText))
            //This should cause a cascade rendering all the way down the component stack
            $.modState(s => s.copy(myGlobalState = s.myGlobalState.copy(user = user)))  
          } catch {
            case e: InvalidData ⇒
              dom.console.error(e.msg + ":" + e.data)
              throw e
          }
        }
        .toCallback
  }

  private val component = ScalaComponent
    .builder[Unit]("content")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ ⇒ $.backend.refresh($.state))
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()
}
```

## Some class, deep in the component stack
Now, the router may or may not need the context (maybe you want to put the username up at top of your app) but it doesn't matter. Let's assume that the router at
some point calls a page, which wants to display the username. A few things to note:
- The username may not be available at all times, that's reflected by the fact that it's an ```Option[User]``` in the global state.
- When the top level component (named ```Content``` in this example) refreshes after it gets the user from the server, it'll cascade the new user all the way down the hierarchy

```scala
object AboutPage  {
  case class State()
  class Backend($ : BackendScope[_, State]) {
    def render(S: State): VdomElement =
      MyGlobalState.ctx.consume { myGlobalState =>
        <.div(s"Hi there${myGlobalState.user.fold("!")(u => s" ${u.username}!")}")
      }
  }
  private val component = ScalaComponent
    .builder[Unit]("AboutPage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()
}
```

One additional note: Other parts of the documentation suggest that your router should always be your top level component. While in general that is true,
I though that moving the provider to a higher level component delineated the responsibilities better, you are welcome to collapse them into a single 
component. 
