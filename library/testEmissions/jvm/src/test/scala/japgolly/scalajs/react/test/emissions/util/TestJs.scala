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
  }
}
