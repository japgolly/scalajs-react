package japgolly.scalajs.react.extra.router2

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

    val dsl = new RouterConfigDsl[PageSet]

    import dsl._
    val routeCCi = "cci" / int.caseclass1(CCi)(CCi.unapply)
    val routeCCl = "ccl" / long.caseclass1(CCl)(CCl.unapply)

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

    case class CompX(title: String, page: Page, router: RouterCtl[Page])
    val compX = ReactComponentB[CompX]("X").render(p => <.div()).build
  }


  case class IntStr(i: Int, s: String)
  implicit val intStrEq = Equal.equalA[IntStr]

  override def tests = TestSuite {

    'route {
      import StaticDsl.Route
      import noPageDsl._

      def test[A: Equal](r: Route[A])(toStr: A => String, good: A*)(bad: String*): Unit = {
        for (b <- bad)
          assertEq(s"Parse $b", r parse Path(b), None)
        for (g <- good) {
          val s = Path(toStr(g))
          assertEq(s"Parse $s", r parse s, Some(g))
          assertEq(s"Path for $g", r pathFor g, s)
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

      'T456 {
        val s1 = string("[ab]")
        val s2 = string("[cd]")
        val s3 = string("[ef]")
        val s4 = string("[gh]")
        val s5 = string("[ij]")
        val s6 = string("[kl]")
        def s(s: String) = s.toCharArray.toVector.map(_.toString)
        val data = for {a <- s("ab"); b <- s("cd"); c <- s("ef"); d <- s("gh"); e <- s("ij"); f <- s("kl")} yield (a,b,c,d,e,f)
        // ok = acegik
        test(s1~s2~s3~s4~s5~s6)({case (a,b,c,d,e,f) => s"$a$b$c$d$e$f"}, data: _*)("ccegik", "caegik", "a/c/e/g/i/k")
        test(s1/s2/s3/s4/s5/s6)({case (a,b,c,d,e,f) => s"$a/$b/$c/$d/$e/$f"}, data: _*)("c/c/e/g/i/k", "c/a/e/g/i/k", "acegik")
      }

      'filter -
        testS(int.filter(_ > 99))(100, 666, 1000)("99", "0", "-100")

      'xmap -
        test((int / string("[a-z]+")).caseclass2(IntStr)(IntStr.unapply))(v => v.i + "/" + v.s,
          IntStr(0, "yay"), IntStr(100, "cool"))("0/", "/yay", "yar")
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
        dynamicRoute(routeCCi) ~> render  (reactTag)
        dynamicRoute(routeCCi) ~> render  (reactElement)
        dynamicRoute(routeCCi) ~> render  (compCConst())
        dynamicRoute(routeCCi) ~> render  (compU)
        dynamicRoute(routeCCi) ~> renderR (compCReqRouter(_))
        dynamicRoute(routeCCi) ~> renderP (compCReqPage(_))
        dynamicRoute(routeCCi) ~> renderP (compCReqPageSub(_))
        dynamicRoute(routeCCi) ~> renderPR((p, r) => compX(CompX("ah", p, r)))
        dynamicRoute(routeCCi) ~> redirectToPage(Obj2)
        dynamicRoute(routeCCi) ~> redirectToPath(Path.root)
        dynamicRoute(routeCCi) ~> redirectToPath("hehe")
        compileError("""dynamicRoute(routeCCl) ~> renderP (compCReqPageSub(_))""") // CCl ⊄ PageSubSet
        ()
      }

    }

  }
}
