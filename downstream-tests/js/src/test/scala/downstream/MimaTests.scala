package downstream

import japgolly.microlibs.testutil.TestUtil._
import utest._

object MimaTests extends TestSuite {

  override def tests = Tests {

    "2_0_0" - {
      import mima200._

      "HookUseRef" - HookUseRef.test { ref =>
        val a = ref.value
        val b = ref.map(_ + 1).unsafeGet()
        assertEq(b, a + 1)
      }
    }
  }
}
