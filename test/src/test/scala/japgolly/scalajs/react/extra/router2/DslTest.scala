package japgolly.scalajs.react.extra.router2

import java.util.UUID

import scalaz.Equal
import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import TestUtil2._

object DslTest extends TestSuite {

  val noPageDsl = new RouterConfigDsl[Nothing]

  sealed trait PageSet
  object PageSet {
    sealed trait PageSubSet extends PageSet
    case object Obj1 extends PageSet
    case object Obj2 extends PageSet
    case class CCi(i: Int) extends PageSubSet
    case class CCl(l: Long) extends PageSet
    case class CCu(u: UUID) extends PageSet

    val dsl = new RouterConfigDsl[PageSet]

    import dsl._
    val routeCCi = "cci" / int.caseclass1(CCi)(CCi.unapply)
    val routeCCl = "ccl" / long.caseclass1(CCl)(CCl.unapply)
    val routeCCu = "ccu" / uuid.caseclass1(CCu)(CCu.unapply)

    def reactTag: ReactTag =
      <.span("tag!")

    def reactElement: ReactElement =
      <.span("el!")

    val compCConst: ReactComponentC.ConstProps[Unit, Unit, Unit, TopNode] =
      ReactComponentB.static("", <.span("static component")).buildU

    val compCReqRouter: ReactComponentC.ReqProps[RouterCtl[PageSet], Unit, Unit, TopNode] =
      ReactComponentB[RouterCtl[PageSet]]("").render(r => r.link(Obj1)).build

    val compCReqPage: ReactComponentC.ReqProps[PageSet, Unit, Unit, TopNode] =
      ReactComponentB[PageSet]("").render(p => <.div(s"Page = $p")).build

    val compCReqPageSub: ReactComponentC.ReqProps[PageSubSet, Unit, Unit, TopNode] =
      ReactComponentB[PageSubSet]("").render(p => <.div(s"Page = $p")).build

    def compU: ReactComponentU[Unit, Unit, Unit, TopNode] =
      compCConst()

    case class CompX(title: String, page: PageSet, router: RouterCtl[PageSet])
    val compX = ReactComponentB[CompX]("X").render(p => <.div()).build
  }


  case class IntStr(i: Int, s: String)
  implicit val intStrEq = Equal.equalA[IntStr]
  implicit val uuidEq = Equal.equalA[UUID]

  override def tests = TestSuite {

    'route {
      import StaticDsl.Route
      import noPageDsl._

      def test[A: Equal](r: Route[A])(toStr: A => String, good: A*)(bad: String*): Unit =
        testM(r)(good.map(a => (toStr(a), a)): _*)(bad: _*)

      def testM[A: Equal](r: Route[A])(good: (String, A)*)(bad: String*): Unit = {
        for (p <- bad)
          assertEq(s"Parse bad Path($p) with $r", r parse Path(p), None)
        for ((s, a) <- good) {
          val p = Path(s)
          assertEq(s"Parse good $p with $r", r parse p, Some(a))
          assertEq(s"Path for ($a) with $r", r pathFor a, p)
        }
      }

      def testS[A: Equal](r: Route[A])(good: A*)(bad: String*): Unit =
        test(r)(_.toString, good: _*)(bad: _*)

      def testU(r: Route[Unit])(good: String)(bad: String*): Unit =
        test(r)(_ => good, ())(bad: _*)

      def longs = List[Long](3, 0, 123123, -3, 8234789632145L)
      val ints  = List[Int](3, 0, 123123, -3)
      def ints2 = for {a <- ints; b <- ints} yield (a,b)
      def ints3 = for {a <- ints; b <- ints; c <- ints} yield (a,b,c)
      def uuids = List[UUID](UUID fromString "12345678-1234-1234-1234-123456789012",
                             UUID fromString "87654321-4321-4321-4321-210987654321",
                             UUID fromString "abcdef12-abcd-ef12-12ab-abcdef123456")

      'root -
        testU(root)("")("/", " ", "ah")

      'lit -
        testU("a/b.c")("a/b.c")("a/b.cc", "aa/b.c", "ab.c", "abc", "a/b..c", "a//b.c", "a/b_c")

      'int -
        testS(int)(ints: _*)("", "a3", "3a", "3/", "/3", "3.5")

      'long -
        testS(long)(longs: _*)("", "a3", "3a", "3/", "/3", "3.5")

      'string -
        testS(string("[0-9a-fA-F]+"))("7", "321", "0BeeF")("", "g", "beam", "-9")

      'uuid -
        testS(uuid)(uuids: _*)("12345678-1234-1234-1234-12345678901", "x12345678-1234-1234-1234-123456789012",
                               "12345678-1234-1234-1234-123456789012/", "/12345678-1234-1234-1234-123456789012",
                               "g2345678-1234-1234-1234-123456789012", "G2345678-1234-1234-1234-123456789012",
                               "12345678-g234-1234-1234-123456789012", "12345678-G234-1234-1234-123456789012",
                               "12345678-1234-1234-1234-g23456789012", "12345678-1234-1234-1234-G23456789012")

      '_to_ -
        testU("a" / "b")("a/b")("", "ab", "a//b", "/a/b", "a/b/")

      'Ato_ -
        test[Int](int / "x")(_.toString + "/x", ints: _*)("x", "/x", "3", "3/", "3x", "/3", "3/xx", "x/3")

      '_toA -
        test[Int]("x" / int)("x/" + _.toString, ints: _*)("x", "x/", "3", "/3", "x3", "3/", "xx/3", "3/x")

      '*** {
        test[(Int, Int)](int / int)(t => t._1 + "/" + t._2, ints2: _*)("3", "123", "3//3")
        test[(String, Int)](string("[a-z]+") ~ int)(t => t._1 + t._2, List("a", "zz", "qwefsda").zip(ints): _*)("d 3", "3d", "d/3", "Z3")
      }

      'T3 -
        test[(Int, Int, Int)](int / int / int)(t => t._1 + "/" + t._2 + "/" + t._3, ints3: _*)("3/3", "3/a/3", "a/3/3", "4/4/a")

      'T45678 {
        val s1 = string("[ab]")
        val s2 = string("[cd]")
        val s3 = string("[ef]")
        val s4 = string("[gh]")
        val s5 = string("[ij]")
        val s6 = string("[kl]")
        val s7 = string("[mn]")
        val s8 = string("[op]")
        def s(s: String) = s.toCharArray.toVector.map(_.toString)
        val data = for {
            a <- s("ab")
            b <- s("cd")
            c <- s("ef")
            d <- s("gh")
            e <- s("ij")
            f <- s("kl")
            g <- s("mn")
            h <- s("op")
          } yield (a, b, c, d, e, f, g, h)
        // ok = acegikmo
        test(s1~s2~s3~s4~s5~s6~s7~s8)({case (a,b,c,d,e,f,g,h) => s"$a$b$c$d$e$f$g$h"}, data: _*)("ccegikmo", "caegikmo", "a/c/e/g/i/k/m/o")
        test(s1/s2/s3/s4/s5/s6/s7/s8)({case (a,b,c,d,e,f,g,h) => s"$a/$b/$c/$d/$e/$f/$g/$h"}, data: _*)("c/c/e/g/i/k/m/o", "c/a/e/g/i/k/m/o", "acegikmo")
      }

      'filter -
        testS(int.filter(_ > 99))(100, 666, 1000)("99", "0", "-100")

      'xmap -
        test((int / string("[a-z]+")).caseclass2(IntStr)(IntStr.unapply))(v => v.i + "/" + v.s,
          IntStr(0, "yay"), IntStr(100, "cool"))("0/", "/yay", "yar")

      'option {
        'basic -
          testM(int.option)("" -> None, "3" -> Some(3))("asd")

        'combo {
          testM("yar:" ~ int.option)("yar:" -> None, "yar:22" -> Some(22))("", "3")
          testM(("yar:" ~ int).option)("" -> None, "yar:7" -> Some(7))("yar:", "3")
          testM("a" / int.option / "b" / int.option / "c")(
            "a//b//c"   -> (None, None),
            "a/3/b//c"  -> (Some(3), None),
            "a//b/3/c"  -> (None, Some(3)),
            "a/6/b/1/c" -> (Some(6), Some(1)))("")
          testM("a" ~ ("/x" / int).option ~ ("/y" / int).option)(
          "a"         -> (None, None),
          "a/x/3"     -> (Some(3), None),
          "a/y/3"     -> (None, Some(3)),
          "a/x/7/y/1" -> (Some(7), Some(1)))("")
        }

        'parseDefault - {
          val r: Route[String] = "data" ~ ("." ~ string("[a-z0-9]+")).option.parseDefault("xz")
          testM(r)("data.zip" -> "zip")("data.")
          assertEq(r parse Path("data.xz"), Some("xz"))
          assertEq(r parse Path("data"),    Some("xz"))
          assertEq(r pathFor "xz", Path("data.xz"))
        }

        'withDefault {
          val r: Route[String] = "data" ~ ("." ~ string("[a-z0-9]+")).option.withDefault("xz")
          testM(r)("data.zip" -> "zip")("data.")
          assertEq(r parse Path("data.xz"), Some("xz"))
          assertEq(r parse Path("data"),    Some("xz"))
          assertEq(r pathFor "xz", Path("data"))
        }
      }
    }

    'rules {
      import PageSet._, dsl._
      implicit def redirectMethod = Redirect.Push

      'staticRoute {
        staticRoute("abc", Obj1) ~> render (reactTag)
        staticRoute("abc", Obj1) ~> render (reactElement)
        staticRoute("abc", Obj1) ~> render (compCConst())
        staticRoute("abc", Obj1) ~> render (compU)
        staticRoute("abc", Obj1) ~> renderR(compCReqRouter(_))

        staticRoute("abc", Obj1) ~> redirectToPage(Obj2)
        staticRoute("abc", Obj1) ~> redirectToPath(Path.root)
        staticRoute("abc", Obj1) ~> redirectToPath("hehe")
        ()
      }

      'dynamicRoute {
        dynamicRouteCT(routeCCi) ~> render (reactTag)
        dynamicRouteCT(routeCCi) ~> render (reactElement)
        dynamicRouteCT(routeCCi) ~> render (compCConst())
        dynamicRouteCT(routeCCi) ~> render (compU)
        dynamicRouteCT(routeCCi) ~> renderR(compCReqRouter(_))

        dynamicRouteCT(routeCCi) ~> dynRender (compCReqPage(_))
        dynamicRouteCT(routeCCi) ~> dynRender (compCReqPageSub(_))
        dynamicRouteCT(routeCCi) ~> dynRenderR((p, r) => compX(CompX("ah", p, r)))

        dynamicRouteCT(routeCCi) ~> redirectToPage(Obj2)
        dynamicRouteCT(routeCCi) ~> redirectToPath(Path.root)
        dynamicRouteCT(routeCCi) ~> redirectToPath("hehe")

        dynamicRouteCT(routeCCu) ~> dynRender(compCReqPage(_))

        compileError("""dynamicRoute(routeCCl) ~> dynRender(compCReqPageSub(_))""") // CCl ⊄ PageSubSet
        ()
      }

    }

  }
}
