package japgolly.scalajs.react.extra

import utest._

object PxTest extends TestSuite {

  def assertEq[A](actual: A, expect: A): Unit =
    if (actual != expect)
      throw new java.lang.AssertionError(s"Expect [$actual] to be [$expect].")

  override def tests = TestSuite {

    val xa = Px("a")

    var vb = "b"
    val xb = Px.thunkA(vb)

    var vc = "c"
    val xc = Px.thunkM(vc)

    var rab   = 0
    var rbc   = 0
    var rabbc = 0

    val xab   = for {a <- xa; b <- xb} yield {rab += 1; a + b}
    val xbc   = for {b <- xb; c <- xc} yield {rbc += 1; b + c}
    val xabbc = for {ab <- xab; bc <- xbc} yield {rabbc += 1; ab + " " + bc}

    def revs = Map[Symbol, Int](
      'xa    -> xa.rev,
      'xb    -> xb.rev,
      'xc    -> xc.rev,
      'xab   -> (xab  .rev + 1000 * rab),
      'xbc   -> (xbc  .rev + 1000 * rbc),
      'xabbc -> (xabbc.rev + 1000 * rabbc))

    var lastRevs = revs
    def assertChanges(expectedChanges: Symbol*): Unit =  {
      val n = revs
      def f(m: Map[Symbol, Int]) = m.toList.toSet
      val changed = (f(lastRevs) -- f(n)).map(_._1)
      assertEq(changed, expectedChanges.toSet)
      lastRevs = n
    }

    def test(a: String, b: String, c: String)(expectedChanges: Symbol*): Unit = {
      var tests = Vector.empty[() => Unit]
      def t(t: => Unit): Unit = tests :+= (() => t)

      t(assertEq(xa.value(), a))
      t(assertEq(xb.value(), b))
      t(assertEq(xc.value(), c))
      t(assertEq(xab.value(), a + b))
      t(assertEq(xbc.value(), b + c))
      t(assertEq(xabbc.value(), a + b + " " + b + c))

      scala.util.Random.shuffle(tests)
      tests.foreach(_())

      assertChanges(expectedChanges: _*)
    }

    test("a", "b", "c")()

    xa.set("A")
    test("A", "b", "c")('xa, 'xab, 'xabbc)

    vb = "B"
    test("A", "B", "c")('xb, 'xab, 'xbc, 'xabbc)

    vc = "C"
    test("A", "B", "c")()

    xc.refresh()
    test("A", "B", "C")('xc, 'xbc, 'xabbc)


    xa.set("A")
    xc.refresh()
    test("A", "B", "C")()
  }
}
