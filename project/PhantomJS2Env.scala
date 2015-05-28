import org.scalajs.core.tools.io._
import org.scalajs.jsenv.phantomjs.PhantomJSEnv

// https://github.com/scala-js/scala-js/issues/1555
class PhantomJS2Env(jettyClassLoader: ClassLoader,
                    phantomjsPath: String = "phantomjs",
                    addArgs: Seq[String] = Seq.empty,
                    addEnv: Map[String, String] = Map.empty,
                    override val autoExit: Boolean = true)
    extends PhantomJSEnv(phantomjsPath, addArgs, addEnv, autoExit, jettyClassLoader) {

  override protected def vmName: String = "PhantomJS2"

  private val consoleNuker = new MemVirtualJSFile("consoleNuker.js")
    .withContent("console.error = undefined;")

  override protected def customInitFiles(): Seq[VirtualJSFile] =
    super.customInitFiles() :+ consoleNuker
}
