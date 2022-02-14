package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.vdom.html_<^._
import java.time._
import nyaya.gen._
import nyaya.prop._
import nyaya.test.PropTest._
import sourcecode.Line
import utest._

object ReusabilityTest extends TestSuite {

  object SampleComponent1 {
    case class Picture(id: Long, url: String, title: String)
    case class Props(name: String, age: Option[Int], pic: Picture)

    implicit val picReuse  : Reusability[Picture] = Reusability.by((_: Picture).id)
    implicit val propsReuse: Reusability[Props]   = Reusability.derive[Props]

    var renderCount = 0

    val component = ScalaComponent.builder[Props]("Demo")
      .initialStateFromProps(identity)
      .renderS { (_, *) =>
        renderCount += 1
        <.div(
          <.p("Name: ", *.name),
          <.p("Age: ", *.age.fold("Unknown")(_.toString)),
          <.img(^.src := *.pic.url, ^.title := *.pic.title))
      }
      .configure(Reusability.shouldComponentUpdate)
      .build
  }

  object SampleComponent2 {
    var outerRenderCount = 0
    var innerRenderCount = 0
    type M = Map[Int, String]

    val outerComponent = ScalaComponent.builder[M]("Demo")
      .initialStateFromProps(identity)
      .renderBackend[Backend]
      .build

    class Backend($: BackendScope[M, M]) {
      val updateUser = Reusable.fn((id: Int, data: String) =>
        $.modState(_.updated(id, data)))
      def render(s: M) = {
        // println()
        // println("outer s = " + s)
        // println(s"truth = ${$.props.runNow} | ${$.state.runNow}")
        outerRenderCount += 1
        <.div(
          s.map { case (id, name) =>
            // println(s"- id: $id, name: $name")
            innerComponent.withKey(id)(InnerProps(name, updateUser(id)))
          }.toVdomArray)
      }
    }

    case class InnerProps(name: String, update: String ~=> Callback)
    implicit val propsReuse: Reusability[InnerProps] = Reusability.derive[InnerProps]

    val innerComponent = ScalaComponent.builder[InnerProps]("PersonEditor")
      .render_P { p =>
        // println("inner p = " + p)
        innerRenderCount += 1
        <.input.text(
          ^.value := p.name,
          ^.onChange ==> ((e: ReactEventFromInput) => p.update(e.target.value).asAsyncCallback))
      }
      .configure(Reusability.shouldComponentUpdate)
      .build
  }

  object Recursive {

    sealed trait Item[+A]

    object Item {
      final case class Folder[+Y](name: String, indirect: Vector[Item[Y]], direct: Item[Y]) extends Item[Y]
      final case class Suite[+Z](bms: Vector[BM[Z]]) extends Item[Z]
      final case class Blah1[+B](blah: Option[Blah1[B]]) extends Item[B]
      final case class Blah2[+C](blah: Option[Blah2[C]], i: Int) extends Item[C]
      final case class Blah3(i: Int) extends Item[Nothing]

      final case class BM[+W](value: W)

      implicit def reusabilityB[A: Reusability]: Reusability[BM    [A]] = Reusability.derive
      implicit def reusabilityS[A: Reusability]: Reusability[Suite [A]] = Reusability.derive
      implicit def reusabilityF[A: Reusability]: Reusability[Folder[A]] = Reusability.derive
      implicit def reusability [A: Reusability]: Reusability[Item  [A]] = Reusability.derive
    }
  }

  case class CC0()
  case class CC1(i: Int)
  case class CC2(i: Int, n: String)
  case class CC4(a: Int, b: Int, c: Int, d: Int)

  case class CCT0[A]()
  case class CCT1[A](i: A)
  case class CCT2[A](i: Int, n: A)

  private final case class P[A](aye: A)
  private object P {
    implicit def reusability[A: Reusability]: Reusability[P[A]] = Reusability.derive
  }

  val collectionData = {
    val a = Vector(3,1,2,3,2,1)
    (for (l <- 0 to a.length) yield a.combinations(l).toSet).reduce(_ ++ _)
  }
  def testCollection[F[_]](f: Vector[Int] => F[Int])(implicit r: Reusability[F[Int]], l: Line): Unit = {
    val d = collectionData.map(f)
    for {a <- d; b <- d}
      assertEq(r.test(a, b), a == b)
  }

  override def tests = Tests {

    "macros" - {
      def test[A](a: A, b: A, expect: Boolean)(implicit r: Reusability[A], l: Line) =
        assertEq(r.test(a, b), expect)

      "caseClass" - {
        "cc0" - {
          implicit val r: Reusability[CC0] = Reusability.derive[CC0]
          test(CC0(), CC0(), true)
        }
        "cc1" - {
          implicit val r: Reusability[CC1] = Reusability.derive[CC1]
          test(CC1(2), CC1(2), true)
          test(CC1(2), CC1(3), false)
        }
        "cc2" - {
          implicit val r: Reusability[CC2] = Reusability.derive[CC2]
          test(CC2(3,"a"), CC2(3,"a"), true)
          test(CC2(3,"a"), CC2(3,"b"), false)
          test(CC2(3,"a"), CC2(4,"a"), false)
        }

        "cct0" - {
          implicit val r: Reusability[CCT0[Int]] = Reusability.derive[CCT0[Int]]
          test(CCT0[Int](), CCT0[Int](), true)
        }
        "cct1" - {
          implicit val r: Reusability[CCT1[Int]] = Reusability.derive[CCT1[Int]]
          test(CCT1(2), CCT1(2), true)
          test(CCT1(2), CCT1(3), false)
        }
        "cct2" - {
          implicit val r: Reusability[CCT2[String]] = Reusability.derive[CCT2[String]]
          test(CCT2(3,"a"), CCT2(3,"a"), true)
          test(CCT2(3,"a"), CCT2(3,"b"), false)
          test(CCT2(3,"a"), CCT2(4,"a"), false)
        }
        "p" - {
          implicitly[Reusability[P[Int]]]
          ()
        }
        "derivesByRef" - {
          class X
          implicit val x: Reusability[X] = Reusability.never
          val _ = x
          case class Y(x: X)
          implicit val y: Reusability[Y] = Reusability.derive[Y]
          val y1 = Y(new X)
          val y2 = Y(new X)
          test(y1, y2, false)
          test(y1, y1, true)
        }
      }

      "caseClassExcept" - {
        "1/1" - {
          implicit val r: Reusability[CC1] = Reusability.caseClassExcept[CC1]("i")
          test(CC1(2), CC1(2), true)
          test(CC1(2), CC1(3), true)
        }

        "1st of 2" - {
          implicit val r: Reusability[CC2] = Reusability.caseClassExcept[CC2]("i")
          test(CC2(3,"a"), CC2(3,"a"), true)
          test(CC2(3,"a"), CC2(3,"b"), false)
          test(CC2(3,"a"), CC2(4,"a"), true)
        }

        "2nd of 2" - {
          implicit val r: Reusability[CC2] = Reusability.caseClassExcept[CC2]("n")
          test(CC2(3,"a"), CC2(3,"a"), true)
          test(CC2(3,"a"), CC2(3,"b"), true)
          test(CC2(3,"a"), CC2(4,"a"), false)
        }

         "2/4" - {
           implicit val r: Reusability[CC4] = Reusability.caseClassExcept[CC4]("a", "c")
           test(CC4(1, 2, 3, 4), CC4(1, 2, 3, 4), true)
           test(CC4(1, 2, 3, 4), CC4(0, 2, 3, 4), true)
           test(CC4(1, 2, 3, 4), CC4(1, 0, 3, 4), false)
           test(CC4(1, 2, 3, 4), CC4(1, 2, 0, 4), true)
           test(CC4(1, 2, 3, 4), CC4(1, 2, 3, 0), false)
         }

        "notFound" - {
          val e = compileError(""" Reusability.caseClassExcept[CC1]("x") """)
          assertContainsAny(e.msg, "Not found", "doesn't exist")
        }

        "dups" - {
          val e = compileError(""" Reusability.caseClassExcept[CC1]("i", "i") """)
          assertContains(e.msg, "Duplicate")
        }
      }

      "recursive" - {
        import Recursive._, Item._
        def test(a: Item[Int], b: Item[Int]): Unit =
          assertEq(s"$a cmp $b", actual = a ~=~ b, expect = a == b)

        val values = List[Item[Int]](
          Blah1(None),
          Blah2(None, 2),
          Blah2(None, 1),
          Blah2(Some(Blah2(None, 1)), 1),
          Blah2(Some(Blah2(None, 2)), 1),
          Blah2(Some(Blah2(None, 1)), 2),
          Suite(Vector.empty),
          Suite(Vector(BM(1))),
          Suite(Vector(BM(1), BM(2))),
          Suite(Vector(BM(2), BM(1))),
          Folder("hehe", Vector.empty, Blah1(None)),
          Folder("hehe", Vector.empty, Blah2(None, 1)),
          Folder("he!he", Vector.empty, Blah1(None)),
          Folder("hehe", Vector(Blah1(None)), Blah1(None)),
          Folder("hehe", Vector(Blah1(None), Blah1(None)), Blah1(None)),
        )

        for {
          a <- values
          b <- values
        } test(a, b)
      }
    }

    "logNonReusable" - {
      class LogSink {
        var messages = List.empty[String]
        def log(s: String): Unit = messages = messages :+ s
      }
      val logSink = new LogSink

      "nonReusable" - {
        Reusability.never.logNonReusable(log = logSink.log).test(0, 0)
        assertEq(logSink.messages, List("Non-reusability:\n- 0\n- 0"))
      }

      "reusable" - {
        Reusability.always.logNonReusable(log = logSink.log).test(0, 0)
        assertEq(logSink.messages, List.empty)
      }

      "formatting" - {
        Reusability.never.logNonReusable(log = logSink.log, fmt = (_, x, y) => s"$x, $y").test(0, 0)
        assertEq(logSink.messages, List("0, 0"))
      }

      "title" - {
        Reusability.never.logNonReusable(log = logSink.log, title = "Sidebar:").test(0, 0)
        assertEq(logSink.messages, List("Sidebar:\n- 0\n- 0"))
      }

      "show" - {
        Reusability.never[Int].logNonReusable(log = logSink.log, show = v => s"Value is $v").test(0, 0)
        assertEq(logSink.messages, List("Non-reusability:\n- Value is 0\n- Value is 0"))
      }
    }

    "shouldComponentUpdate" - {
      "reusableState" - {
        import SampleComponent1._

        val pic1a = Picture(1, "asdf", "qer")
        val pic1b = Picture(1, "eqwrg", "seafr")
        val pic2  = Picture(2, "asdf", "qer")

        val c = ReactTestUtils renderIntoDocument component(Props("n", None, pic1a))
        def test(expectDelta: Int, s: Props): Unit = {
          val a = renderCount
          c.setState(s)
          assertEq(renderCount, a + expectDelta)
        }
        val (update,ignore) = (1,0)

        test(ignore, Props("n", None, pic1a))
        test(update, Props("!", None, pic1a))
        test(ignore, Props("!", None, pic1a))
        test(ignore, Props("!", None, pic1b))
        test(update, Props("!", None, pic2))
        test(ignore, Props("!", None, pic2))
        test(update, Props("!", Some(3), pic2))
        test(update, Props("!", Some(4), pic2))
        test(ignore, Props("!", Some(4), pic2))
        test(update, Props("!", Some(5), pic2))
      }

      "reusableProps" - {
        import SampleComponent2._
        val data1: M = Map(1 -> "One", 2 -> "Two", 3 -> "Three")
        val data2: M = Map(1 -> "One", 2 -> "Two", 3 -> "33333")
        val c = ReactTestUtils renderIntoDocument outerComponent(data1)
        assertEq((outerRenderCount, innerRenderCount), (1, 3))
        // println()
        // println(">>> c.forceUpdate")
        c.forceUpdate
        assertEq((outerRenderCount, innerRenderCount), (2, 3))
        // println()
        // println("c.state = " + c.state)
        // println(">>> c.setState")
        c.setState(data2)
        // println("c.state = " + c.state)
        assertEq((outerRenderCount, innerRenderCount), (3, 4))
      }
    }

    "uuid" - {
      import java.util.UUID
      val value = UUID.randomUUID.toString

      assert(UUID.fromString(value) ~=~ UUID.fromString(value))
      assert(!(UUID.fromString(value) ~=~ UUID.randomUUID))
    }

    "jsDate" - {
      import scala.scalajs.js.Date
      val now = System.currentTimeMillis
      val date1 = new Date(now.toDouble)
      val date2 = new Date(now.toDouble)

      assert(date1 ~=~ date2)
      assert(!(date1 ~=~ new Date(now.toDouble + 1)))
    }

    "javaDate" - {
      import java.util.Date
      val now = System.currentTimeMillis
      val date1 = new Date(now)
      val date2 = new Date(now)

      assert(date1 ~=~ date2)
      assert(!(date1 ~=~ new Date(now + 1)))
    }

    "doubleWithTolerance" - {
      implicit val r = Reusability.double(0.2)
      assert(1.2.toDouble ~=~ 1.0.toDouble)
      assert(0.8.toDouble ~=~ 1.0.toDouble)

      assert(!(0.7.toDouble ~=~ 1.0.toDouble))
      assert(!(1.3.toDouble ~=~ 1.0.toDouble))
    }

    "floatWithTolerance" - {
      implicit val r = Reusability.float(0.2f)
      assert(0.9f ~=~ 1.0f)
      assert(1.0f ~=~ 1.0f)
      assert(1.1f ~=~ 1.0f)

      assert(!(0.7f ~=~ 1.0f))
      assert(!(1.3f ~=~ 1.0f))
    }

    "option" - {
      def test(vs: Option[Boolean]*) =
        for {a <- vs; b <- vs}
          assert((a ~=~ b) == (a == b))
      test(None, Some(true), Some(false))
    }

    "vector" - testCollection(_.toVector)
    "list"   - testCollection(_.toList)
    "set"    - testCollection(_.toSet)

    "map" - {
      val r = Reusability.map[Int, Int]
      val data = Gen.chooseInt(8).mapTo(Gen.chooseInt(8)).pair
      data mustSatisfy Prop.equal("Reusability matches equality")(r.test.tupled, i => i._1 == i._2)
    }

    "javaDurationWithTolerance" - {
      implicit val r = Reusability.javaDuration(Duration.ofSeconds(1))
      assert(Duration.ofSeconds(8) ~=~ Duration.ofSeconds(8))
      assert(Duration.ofSeconds(9) ~=~ Duration.ofSeconds(8))
      assert(Duration.ofSeconds(8) ~=~ Duration.ofSeconds(9))
      assert(Duration.ofSeconds(9) ~/~ Duration.ofSeconds(7))
      assert(Duration.ofSeconds(7) ~/~ Duration.ofSeconds(9))
    }

    "javaDurationWithoutTolerance" - {
      import Reusability.TemporalImplicitsWithoutTolerance._
      assert(Duration.ofSeconds(8) ~=~ Duration.ofSeconds(8))
      assert(Duration.ofSeconds(9) ~/~ Duration.ofSeconds(8))
      assert(Duration.ofSeconds(8) ~/~ Duration.ofSeconds(9))
      assert(Duration.ofSeconds(9) ~/~ Duration.ofSeconds(7))
      assert(Duration.ofSeconds(7) ~/~ Duration.ofSeconds(9))
    }

    "instantWithTolerance" - {
      implicit val r = Reusability.instant(Duration.ofSeconds(1))
      val now = Instant.now()
      assert(now ~=~ now)
      assert(now ~=~ Instant.ofEpochSecond(now.getEpochSecond, now.getNano))
      assert(now ~=~ now.plusSeconds(1))
      assert(now.plusSeconds(1) ~=~ now)
      assert(now ~/~ now.plusSeconds(2))
      assert(now.plusSeconds(2) ~/~ now)
    }

    "instantWithoutTolerance" - {
      import Reusability.TemporalImplicitsWithoutTolerance._
      val now = Instant.now()
      assert(now ~=~ now)
      assert(now ~=~ Instant.ofEpochSecond(now.getEpochSecond, now.getNano))
      assert(now ~/~ now.plusSeconds(1))
      assert(now.plusSeconds(1) ~/~ now)
      assert(now ~/~ now.plusSeconds(2))
      assert(now.plusSeconds(2) ~/~ now)
    }

    "finiteDurationWithTolerance" - {
      import scala.concurrent.duration._
      implicit val r: Reusability[FiniteDuration] = Reusability.finiteDuration(1.second)
      assert(8.seconds ~=~ 8.seconds)
      assert(9.seconds ~=~ 8.seconds)
      assert(8.seconds ~=~ 9.seconds)
      assert(9.seconds ~/~ 7.seconds)
      assert(7.seconds ~/~ 9.seconds)
    }

    "finiteDurationWithoutTolerance" - {
      import scala.concurrent.duration._
      import Reusability.TemporalImplicitsWithoutTolerance._
      assert(8.seconds ~=~ 8.seconds)
      assert(9.seconds ~/~ 8.seconds)
      assert(8.seconds ~/~ 9.seconds)
      assert(9.seconds ~/~ 7.seconds)
      assert(7.seconds ~/~ 9.seconds)
    }

    "javaBigDecimal" - {
      import java.math.BigDecimal
      implicit val r = Reusability.javaBigDecimal(0.5)
      assert(new BigDecimal(10.6) ~=~ new BigDecimal(10.6))
      assert(new BigDecimal(10.6) ~=~ new BigDecimal(10.1))
      assert(new BigDecimal(10.6) ~/~ new BigDecimal(10.0))
    }

    "scalaBigDecimal" - {
      import scala.math.BigDecimal
      implicit val r: Reusability[BigDecimal] = Reusability.scalaBigDecimal(0.5)
      assert(BigDecimal(10.6) ~=~ BigDecimal(10.6))
      assert(BigDecimal(10.6) ~=~ BigDecimal(10.1))
      assert(BigDecimal(10.6) ~/~ BigDecimal(10.0))
    }

    "bigInteger" - {
      import java.math.BigInteger
      assert(new BigInteger("10") ~=~ new BigInteger("10"))
      assert(new BigInteger("10") ~/~ new BigInteger("11"))
    }

    "bigInt" - {
      import scala.math.BigInt
      assert(BigInt("10") ~=~ BigInt("10"))
      assert(BigInt("10") ~/~ BigInt("11"))
    }
  }
}
