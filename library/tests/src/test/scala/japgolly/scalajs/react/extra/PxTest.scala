package japgolly.scalajs.react.extra

import japgolly.scalajs.react.test.TestUtil._
import scala.annotation.nowarn
import utest._

object PxTest extends TestSuite {
  japgolly.scalajs.react.test.InitTestEnv()

  case class TraceFn[A, B](rawFn: A => B) {
    var calls: Vector[(A, B)] = Vector.empty

    def reset(): Unit = calls = Vector.empty

    def count(): Int = calls.length

    val fn: A => B =
      a => {
        val b = rawFn(a)
        calls :+= ((a, b))
        b
      }
  }

  def addFn(n: Int): TraceFn[Int, Int] =
    TraceFn(_ + n)

  type AddCC = Int => Int

  override def tests = Tests {
    "big" - {
      val xa = Px("a").withReuse.manualUpdate

      var vb = "b"
      val xb = Px(vb).withReuse.autoRefresh

      var vc = "c"
      val xc = Px(vc).withReuse.manualRefresh

      var rab   = 0
      var rbc   = 0
      var rabbc = 0

      val xab   = for {a <- xa; b <- xb} yield {rab += 1; a + b}
      val xbc   = for {b <- xb; c <- xc} yield {rbc += 1; b + c}
      val xabbc = for {ab <- xab; bc <- xbc} yield {rabbc += 1; ab + " " + bc}

      def revs = Map[String, Int](
        "xa"    -> xa.rev,
        "xb"    -> xb.rev,
        "xc"    -> xc.rev,
        "xab"   -> (xab  .rev + 1000 * rab),
        "xbc"   -> (xbc  .rev + 1000 * rbc),
        "xabbc" -> (xabbc.rev + 1000 * rabbc))

      var lastRevs = revs
      def assertChanges(expectedChanges: String*): Unit =  {
        val n = revs
        def f(m: Map[String, Int]) = m.toList.toSet
        val changed = (f(lastRevs) -- f(n)).map(_._1)
        assertEq("assertChanges", changed, expectedChanges.toSet)
        lastRevs = n
      }

      def test(a: String, b: String, c: String)(expectedChanges: String*): Unit = {
        var tests = Vector.empty[() => Unit]
        def t(t: => Unit): Unit = tests :+= (() => t)

        t(assertEq("xa.value()", xa.value(), a))
        t(assertEq("xb.value()", xb.value(), b))
        t(assertEq("xc.value()", xc.value(), c))
        t(assertEq("xab.value()", xab.value(), a + b))
        t(assertEq("xbc.value()", xbc.value(), b + c))
        t(assertEq("xabbc.value()", xabbc.value(), a + b + " " + b + c))

        scala.util.Random.shuffle(tests)
        tests.foreach(_())

        assertChanges(expectedChanges: _*)
      }

      test("a", "b", "c")()

      xa.set("A")
      test("A", "b", "c")("xa", "xab", "xabbc")

      vb = "B"
      test("A", "B", "c")("xb", "xab", "xbc", "xabbc")

      vc = "C"
      test("A", "B", "c")()

      xc.refresh()
      test("A", "B", "C")("xc", "xbc", "xabbc")

      xa.set("A")
      xc.refresh()
      test("A", "B", "C")()
    }

    "map" - {
      val add0 = addFn(0)
      val add5 = addFn(5)
      val px3 = Px(3).withReuse.manualUpdate
      val px = px3 map add0.fn map add5.fn
      def test(res: Int, called0: Int, called5: Int): Unit = {
        val v1 = px.value()
        assertEq((v1, add0.count(), add5.count()), (res, called0, called5))
        val v2 = px.value()
        assertEq((v2, add0.count(), add5.count()), (res, called0, called5))
      }
      test(8, 1, 1)
      px3.set(7); test(12, 2, 2)
      px3.set(7); test(12, 2, 2)
      px3.set(0); test(5, 3, 3)
    }

    "mapReuse" - {
      val even = TraceFn((_: Int) & 254)
      val add5 = addFn(5)
      val px4 = Px(4).withReuse.manualUpdate
      val px = px4.map(even.fn).withReuse map add5.fn
      def test(res: Int, called0: Int, called5: Int): Unit = {
        val v1 = px.value()
        assertEq((v1, even.count(), add5.count()), (res, called0, called5))
        val v2 = px.value()
        assertEq((v2, even.count(), add5.count()), (res, called0, called5))
      }
      test(9, 1, 1)
      px4.set(2); test(7, 2, 2)
      px4.set(3); test(7, 3, 2)
      px4.set(3); test(7, 3, 2)
      px4.set(5); test(9, 4, 3)
    }

    "extract" - {
      "bad" - {
        @nowarn("cat=unused") val px: Px[Int] = Px(3).withReuse.manualUpdate
        compileError("px.extract")
        ()
      }
      "fn0" - {
        var i = () => 30
        val fn = Px(i).withoutReuse.autoRefresh.extract
        assertEq(fn(), 30)
        i = () => 4
        assertEq(fn(), 4)
      }
      "fn1" - {
        var i = 30
        val px = Px(i).withReuse.autoRefresh.map(a => (b: Int) => a - b)
        val fn = px.extract
        assertEq(fn(7), 23)
        i = 20
        assertEq(fn(7), 13)
      }
      "fn2" - {
        var i = 30
        val fn = Px(i).withReuse.autoRefresh.map(a => (b: Int, c: Int) => a - b - c).extract
        assertEq(fn(7, 3), 20)
        i = 20
        assertEq(fn(3, 7), 10)
      }
      "dealias" - {
        val add: AddCC = _ + 8
        val fn = Px(add).withoutReuse.autoRefresh.extract
        assertEq(fn(3), 11)
      }
    }

    "flatMap" - {
      "revCollision" - {
        val inner1 = Px("init").withReuse.manualUpdate
        inner1.set("v1")

        val outer = Px(1).withReuse.manualUpdate

        val flat = outer.flatMap {
          case 1 => inner1
          case 2 => Px.constByValue("v2")
        }

        val downstream = flat.map("hello " + _)

        assertEq(downstream.value(), "hello v1")

        outer.set(2)
        assertEq(downstream.value(), "hello v2")
      }

      "innerUpdates" - {
        val inner = Px("a").withReuse.manualUpdate
        val outer = Px(1).withReuse.manualUpdate
        val flat = outer.flatMap(_ => inner)
        val downstream = flat.map(_ + "!")

        assertEq(downstream.value(), "a!")
        val r0 = flat.rev

        inner.set("b")
        assertEq(downstream.value(), "b!")
        assert(flat.rev > r0)
        val r1 = flat.rev

        inner.set("c")
        assertEq(downstream.value(), "c!")
        assert(flat.rev > r1)
      }

      "bothUpdateInterleaved" - {
        val in1 = Px(10).withReuse.manualUpdate
        val in2 = Px(20).withReuse.manualUpdate
        val outer = Px(1).withReuse.manualUpdate
        val flat = outer.flatMap {
          case 1 => in1
          case 2 => in2
        }
        val downstream = flat.map(_ * 2)

        assertEq(downstream.value(), 20)
        val r0 = flat.rev

        in1.set(11)
        assertEq(downstream.value(), 22)
        assert(flat.rev > r0)
        val r1 = flat.rev

        outer.set(2)
        assertEq(downstream.value(), 40)
        assert(flat.rev > r1)
        val r2 = flat.rev

        in2.set(25)
        assertEq(downstream.value(), 50)
        assert(flat.rev > r2)
        val r3 = flat.rev

        outer.set(1)
        assertEq(downstream.value(), 22)
        assert(flat.rev > r3)
      }

      "sameInnerInstance" - {
        val inner = Px("hello").withReuse.manualUpdate
        val outer = Px(1).withReuse.manualUpdate
        val flat = outer.flatMap(_ => inner)
        val downstream = flat.map(_.toUpperCase)

        assertEq(downstream.value(), "HELLO")
        val r0 = flat.rev

        outer.set(2)
        assert(flat.rev > r0)
        assertEq(downstream.value(), "HELLO")
      }

      "autoRefreshInner" - {
        var x = 1
        val inner = Px(x).withReuse.autoRefresh
        val outer = Px("k").withReuse.manualUpdate
        val flat = outer.flatMap(_ => inner)
        val downstream = flat.map(_ + 10)

        assertEq(downstream.value(), 11)
        val r0 = flat.rev

        x = 5
        assertEq(downstream.value(), 15)
        assert(flat.rev > r0)
      }

      "nestedFlatMap" - {
        val p1 = Px("A").withReuse.manualUpdate
        val p2 = Px("B").withReuse.manualUpdate
        val p3 = Px("C").withReuse.manualUpdate

        val flat = for {
          a <- p1
          b <- p2
          c <- p3
        } yield s"$a-$b-$c"

        val downstream = flat.map("[" + _ + "]")
        assertEq(downstream.value(), "[A-B-C]")

        p2.set("X")
        assertEq(downstream.value(), "[A-X-C]")

        p1.set("Z")
        assertEq(downstream.value(), "[Z-X-C]")

        p3.set("W")
        assertEq(downstream.value(), "[Z-X-W]")
      }

      "peekAndLazy" - {
        var derived = 0
        val outer = Px(1).withReuse.manualUpdate
        val flat = outer.flatMap { x =>
          derived += 1
          Px(s"v$x").withReuse.manualUpdate
        }

        assertEq(derived, 0)
        assertEq(flat.peek, "v1")
        assertEq(derived, 1)

        outer.set(2)
        assertEq(flat.peek, "v1")
        assertEq(flat.value(), "v2")
        assertEq(derived, 2)
      }
    }
  }
}
