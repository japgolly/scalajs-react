package japgolly.scalajs.react.test.emissions.util

// We're gonna inherit like it's 1999 baby! 🥳
class TestJs(val name: String, val filename: String, originalContent: String)
    extends MutableVirtualFile(Some(filename), originalContent) {

  def this(name: String, filename: String) =
    this(name, filename, Util.needFileContent(filename))

  trim()
}

object TestJs {

  case class Hack(run: TestJs => Unit) {
    def >>    (next: Hack)          : Hack = Hack { js => run(js); next.run(js) }
    def when  (b: Boolean)          : Hack = when(_ => b)
    def when  (f: TestJs => Boolean): Hack = Hack { js => if (f(js)) run(js) }
    def unless(b: Boolean)          : Hack = unless(_ => b)
    def unless(f: TestJs => Boolean): Hack = when(!f(_))
    def disable                     : Hack = Hack.none

    def runAnon(jsContent: String): TestJs = {
      val js = new TestJs("anon", "anon.js", jsContent)
      run(js)
      js
    }
  }

  object Hack {
    def none: Hack =
      apply(_ => ())

    // Make much more readable for human eyes. (Note: breaks the validity of the JS)
    val humanReadable: Hack = {

      // Lines that simply initialise object singletons
      val objectInit = """^ *(?:\$j_[a-zA-Z0-9_]+\$?\.)?\$m_[a-zA-Z0-9_]+\$\(\); *$""".r

      apply(_
        .modifyLines(_
          .replace("＿", "_")
          .replace("$0024", "$")
          .replace("$002e", "_") // "."
          .replace("$005f", "_")
          .replace("$less$up", "")
          .replace("japgolly_scalajs_react_", "sjr_")
          .replace("japgolly$scalajs$react$", "sjr$")
          .replaceAll("scala_scalajs_runtime_(?=AnonFunction|WrappedVarArgs)", "")
        )
        .filterNot(objectInit)
      )
    }

    // Hacks to apply before comparison, so that tests consitently pass
    val comparisonHacks: Hack =
      apply(_
        .filterNot(_ startsWith "import ")
        .modify(_
          .replace("PropsChildren$", "PropsChildren") // Not sure why SJS sometimes emits one or the other
        )
      )

    val forComparison: Hack =
      humanReadable >> comparisonHacks
  }
}
