package japgolly.scalajs.react.core

import nyaya.gen._
import nyaya.prop._
import nyaya.test.PropTest._
import utest._

import japgolly.scalajs.react._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._

object ReusabilityTest extends TestSuite {

  object SampleComponent1 {
    case class Picture(id: Long, url: String, title: String)
    case class Props(name: String, age: Option[Int], pic: Picture)

    implicit val picReuse   = Reusability.by((_: Picture).id)
    implicit val propsReuse = Reusability.derive[Props]

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

    class Backend($: BackendScope[_, M]) {
      val updateUser = Reusable.fn((id: Int, data: String) =>
        $.modState(_.updated(id, data)))
      def render(s: M) = {
        outerRenderCount += 1
        <.div(
          s.map { case (id, name) =>
            innerComponent.withKey(id)(InnerProps(name, updateUser(id)))
          }.toVdomArray)
      }
    }

    case class InnerProps(name: String, update: String ~=> Callback)
    implicit val propsReuse = Reusability.derive[InnerProps]

    val innerComponent = ScalaComponent.builder[InnerProps]("PersonEditor")
      .renderP { (_, p) =>
        innerRenderCount += 1
        <.input(
          ^.`type` := "text",
          ^.value := p.name,
          ^.onChange ==> ((e: ReactEventFromInput) => p.update(e.target.value)))
      }
      .configure(Reusability.shouldComponentUpdate)
      .build
  }

  case class CC0()
  case class CC1(i: Int)
  case class CC2(i: Int, n: String)
  case class CC4(a: Int, b: Int, c: Int, d: Int)

  case class CCT0[A]()
  case class CCT1[A](i: A)
  case class CCT2[A](i: Int, n: A)

  sealed trait X
  object X {
    case object X1 extends X
    final case class X2() extends X
    sealed abstract class X3 extends X
    final case class X3a(i: Int) extends X3
    case object X3b extends X3
  }

  sealed abstract class Y
  object Y {
    case object Y1 extends Y
    final case class Y2() extends Y
    sealed trait Y3 extends Y
    final case class Y3a(i: Int) extends Y3
    case object Y3b extends Y3
  }

  val collectionData = {
    val a = Vector(3,1,2,3,2,1)
    (for (l <- 0 to a.length) yield a.combinations(l).toSet).reduce(_ ++ _)
  }
  def testCollection[F[_]](f: Vector[Int] => F[Int])(implicit r: Reusability[F[Int]]): Unit = {
    val d = collectionData.map(f)
    for {a <- d; b <- d}
      assertEq(r.test(a, b), a == b)
  }

  override def tests = Tests {

    "macros" - {
      def test[A](a: A, b: A, expect: Boolean)(implicit r: Reusability[A]) =
        assert(r.test(a, b) == expect)

      "caseClass" - {
        "cc0" - {
          implicit val r = Reusability.derive[CC0]
          test(CC0(), CC0(), true)
        }
        "cc1" - {
          implicit val r = Reusability.derive[CC1]
          test(CC1(2), CC1(2), true)
          test(CC1(2), CC1(3), false)
        }
        "cc2" - {
          implicit val r = Reusability.derive[CC2]
          test(CC2(3,"a"), CC2(3,"a"), true)
          test(CC2(3,"a"), CC2(3,"b"), false)
          test(CC2(3,"a"), CC2(4,"a"), false)
        }

        "cct0" - {
          implicit val r = Reusability.derive[CCT0[Int]]
          test(CCT0[Int](), CCT0[Int](), true)
        }
        "cct1" - {
          implicit val r = Reusability.derive[CCT1[Int]]
          test(CCT1(2), CCT1(2), true)
          test(CCT1(2), CCT1(3), false)
        }
        "cct2" - {
          implicit val r = Reusability.derive[CCT2[String]]
          test(CCT2(3,"a"), CCT2(3,"a"), true)
          test(CCT2(3,"a"), CCT2(3,"b"), false)
          test(CCT2(3,"a"), CCT2(4,"a"), false)
        }
      }

      "caseClassExcept" - {
        "1/1" - {
          implicit val r = Reusability.caseClassExcept[CC1]('i)
          test(CC1(2), CC1(2), true)
          test(CC1(2), CC1(3), true)
        }

        "1st of 2" - {
          implicit val r = Reusability.caseClassExcept[CC2]('i)
          test(CC2(3,"a"), CC2(3,"a"), true)
          test(CC2(3,"a"), CC2(3,"b"), false)
          test(CC2(3,"a"), CC2(4,"a"), true)
        }

        "2nd of 2" - {
          implicit val r = Reusability.caseClassExcept[CC2]('n)
          test(CC2(3,"a"), CC2(3,"a"), true)
          test(CC2(3,"a"), CC2(3,"b"), true)
          test(CC2(3,"a"), CC2(4,"a"), false)
        }

         "2/4" - {
           implicit val r = Reusability.caseClassExcept[CC4]('a, 'c)
           test(CC4(1, 2, 3, 4), CC4(1, 2, 3, 4), true)
           test(CC4(1, 2, 3, 4), CC4(0, 2, 3, 4), true)
           test(CC4(1, 2, 3, 4), CC4(1, 0, 3, 4), false)
           test(CC4(1, 2, 3, 4), CC4(1, 2, 0, 4), true)
           test(CC4(1, 2, 3, 4), CC4(1, 2, 3, 0), false)
         }

        "notFound" - {
          val e = compileError(""" Reusability.caseClassExcept[CC1]('x) """)
          assert(e.msg contains "Not found")
        }

        "dups" - {
          val e = compileError(""" Reusability.caseClassExcept[CC1]('i, 'i) """)
          assert(e.msg contains "Duplicate")
        }
      }

      "sealedTrait" - {
        import X._
        'all {
          implicit val r = Reusability.derive[X]
          test[X](X1    , X1    , true)
          test[X](X2()  , X1    , false)
          test[X](X1    , X2()  , false)
          test[X](X2()  , X2()  , true)
          test[X](X3a(1), X3a(1), true)
          test[X](X3a(1), X3a(2), false)
          test[X](X3a(2), X3a(1), false)
          test[X](X3a(2), X3b   , false)
          test[X](X3b   , X3b   , true)
        }
        'reuseMid {
          implicit val r = {
            implicit val x3 = Reusability.always[X3]
            Reusability.derive[X]
          }
          test[X](X1    , X1    , true)
          test[X](X2()  , X1    , false)
          test[X](X1    , X2()  , false)
          test[X](X2()  , X2()  , true)
          test[X](X3a(1), X3a(1), true)
          test[X](X3a(1), X3a(2), true) // magic
          test[X](X3a(2), X3a(1), true) // magic
          test[X](X3a(2), X3b   , true) // magic
          test[X](X3b   , X3b   , true)
        }
      }

      "sealedClass" - {
        import Y._
        'all {
          implicit val r = Reusability.derive[Y]
          test[Y](Y1    , Y1    , true)
          test[Y](Y2()  , Y1    , false)
          test[Y](Y1    , Y2()  , false)
          test[Y](Y2()  , Y2()  , true)
          test[Y](Y3a(1), Y3a(1), true)
          test[Y](Y3a(1), Y3a(2), false)
          test[Y](Y3a(2), Y3a(1), false)
          test[Y](Y3a(2), Y3b   , false)
          test[Y](Y3b   , Y3b   , true)
        }
        'reuseMid {
          implicit val r = {
            implicit val y3 = Reusability.always[Y3]
            Reusability.derive[Y]
          }
          test[Y](Y1    , Y1    , true)
          test[Y](Y2()  , Y1    , false)
          test[Y](Y1    , Y2()  , false)
          test[Y](Y2()  , Y2()  , true)
          test[Y](Y3a(1), Y3a(1), true)
          test[Y](Y3a(1), Y3a(2), true) // magic
          test[Y](Y3a(2), Y3a(1), true) // magic
          test[Y](Y3a(2), Y3b   , true) // magic
          test[Y](Y3b   , Y3b   , true)
        }
      }
    }

    "logNonReusable" - {
      val logSink = new {
        var messages = List.empty[String]
        def log(s: String): Unit = messages = messages :+ s
      }

      "nonReusable" - {
        Reusability.never.logNonReusable(log = logSink.log).test(0, 0)
        assert(logSink.messages == List("Non-reusability:\n- 0\n- 0"))
      }

      "reusable" - {
        Reusability.always.logNonReusable(log = logSink.log).test(0, 0)
        assert(logSink.messages == List.empty)
      }

      "formatting" - {
        Reusability.never.logNonReusable(log = logSink.log, fmt = (_, x, y) => s"$x, $y").test(0, 0)
        assert(logSink.messages == List("0, 0"))
      }

      "title" - {
        Reusability.never.logNonReusable(log = logSink.log, title = "Sidebar:").test(0, 0)
        assert(logSink.messages == List("Sidebar:\n- 0\n- 0"))
      }

      "show" - {
        Reusability.never[Int].logNonReusable(log = logSink.log, show = v => s"Value is $v").test(0, 0)
        assert(logSink.messages == List("Non-reusability:\n- Value is 0\n- Value is 0"))
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
          assert(renderCount == a + expectDelta)
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
        assert(outerRenderCount == 1, innerRenderCount == 3)
        c.forceUpdate
        assert(outerRenderCount == 2, innerRenderCount == 3)
        c.setState(data2)
        assert(outerRenderCount == 3, innerRenderCount == 4)
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
      val date1 = new Date(now)
      val date2 = new Date(now)

      assert(date1 ~=~ date2)
      assert(!(date1 ~=~ new Date(now + 1)))
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

  }
}
