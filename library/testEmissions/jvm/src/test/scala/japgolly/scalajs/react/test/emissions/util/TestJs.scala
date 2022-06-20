package japgolly.scalajs.react.test.emissions.util

import japgolly.scalajs.react.test.emissions.util.MutableVirtualFile.LineTarget

// We're gonna inherit like it's 1999 baby! 🥳
class TestJs(val name: String, val filename: String, originalContent: String)
    extends MutableVirtualFile(Some(filename), originalContent) {

  def this(name: String, filename: String) =
    this(name, filename, Util.needFileContent(filename))

  def run(h: TestJs.Hack): Unit =
    h.run(this)

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

    def runAnon(jsContent: String): TestJs =
      runAs("", "anon.js", jsContent)

    def runAs(name: String, jsContent: String): TestJs =
      runAs(name, if (name.endsWith(".js")) name else name + ".js", jsContent)

    def runAs(name: String, filename: String, jsContent: String): TestJs = {
      val js = new TestJs(name, filename, jsContent)
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
          .replaceAll("\\$\\d+", "\\$0") // change `this$24` etc into just `this$0`
          .replaceAll("scala_scalajs_runtime_(?=AnonFunction|WrappedVarArgs)", "")
        )
        .filterNot(objectInit)
      )
    }

    def addName(where: LineTarget): Hack =
      apply { js =>
        if (js.name.nonEmpty)
          js.addLine(s"// Name: ${js.name}", where)
      }

    def addSizeDetails(where: LineTarget): Hack =
      apply(js => js.addLine(s"// ${js.descSize()}", where))

    private val comparisonHacks: Hack =
      apply(_
        .filterNot(_ startsWith "import ")
        .modify(_
          // Not sure why SJS sometimes emits one or the other of the following

          .replace("PropsChildren$", "PropsChildren")

          // Scala 3 only for some reason
          .replace("$FirstStep.$", "$First.$")

          // From: $j_sjr_hooks_HookCtx$P3.$ct_Lsjr_hooks_HookCtx$P3__O__O__O__O__(new $j_sjr_hooks_HookCtx$P3.$c_Lsjr_hooks_HookCtx$P3(), props$0, hook1, hook2, hook3);
          //   To: new $j_sjr_hooks_HookCtx$P3.$c_Lsjr_hooks_HookCtx$P3(props$0, hook1, hook2, hook3)
          .replaceAll("""\$j_[a-zA-Z_]+?\$P\d+?\.\$[a-zA-Z_]+?\$P[0-9_O]+?\((new [a-zA-Z0-9$_.]+\()\), """, "$1")
        )
      )

    // Hacks to apply before comparison so that
    //   1) tests consitently pass
    //   2) lots of irrelevant noise is filtered out when presenting failures
    val forComparison: Hack = (
      humanReadable
      >> comparisonHacks
      >> addSizeDetails(LineTarget.End)
      >> addName(LineTarget.End)
    )
  }
}
