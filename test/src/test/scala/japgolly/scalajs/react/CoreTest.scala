package japgolly.scalajs.react

import japgolly.scalajs.react.ReactComponentC.ConstProps
import monocle.macros.Lenses
import utest._
import scala.scalajs.js, js.{Array => JArray}
import org.scalajs.dom.raw._
import vdom.all._
import TestUtil._
import japgolly.scalajs.react.test.{Simulation, DebugJs, ReactTestUtils}
import CompScope._
import CompState._

import scala.scalajs.js.annotation.ScalaJSDefined

object CoreTest extends TestSuite {

  object RCB {
    lazy val CA = ReactComponentB[Unit]("CA").render_C(c => div(c)).buildU
    lazy val CB = ReactComponentB[Unit]("CB").render_C(c => span(c)).buildU
    lazy val H1 = ReactComponentB[String]("H").render_P(p => h1(p)).build

    lazy val SI = ReactComponentB[Unit]("SI")
      .initialState(123)
      .render(T => input(value := T.state.toString))
      .domType[HTMLInputElement]
      .componentDidMount($ => Callback {
        val s: String = ReactDOM.findDOMNode($).value // Look, it knows its DOM node type
      })
      .buildU
  }

  object ES6 {
    @ScalaJSDefined
    class CAC extends ReactComponentNoPropsAndState[HTMLElement] {
      override def render() = div(children)
    }
    lazy val CA = ElementFactory.noProps(js.constructorOf[CAC], classOf[CAC])
    @ScalaJSDefined
    class CBC extends ReactComponentNoPropsAndState[HTMLElement] {
      override def render() = span(children)
    }
    lazy val CB = ElementFactory.noProps(js.constructorOf[CBC], classOf[CBC])
    @ScalaJSDefined
    class H1C extends ReactComponentNoState[String, HTMLElement] {
      override def render() = h1(props)
    }
    lazy val H1 = ElementFactory.requiredProps(js.constructorOf[H1C], classOf[H1C])

    @ScalaJSDefined
    class SIC extends ReactComponentNoProps[Int, HTMLInputElement] {
      override val displayName = "SI2"
      override def initialState() = 123
      def render() = {
        input(value := state.toString)
      }
    }

    lazy val SI = ElementFactory.noProps(js.constructorOf[SIC], classOf[SIC])
  }

  val tagmod  : TagMod       = cls := "ho"
  val reacttag: ReactTag     = span()
  val relement: ReactElement = span()

  @Lenses
  case class StrInt(str: String, int: Int)

  @Lenses
  case class StrIntWrap(strInt: StrInt)

  val tests = TestSuite {

    'scalatags {
      @ScalaJSDefined
      class ScalatagsC extends ReactComponentNoState[ReactElement, HTMLElement] {
        override def render() = props
      }
      val Scalatags = ElementFactory.requiredProps(js.constructorOf[ScalatagsC], classOf[ScalatagsC])

      def test(subj: ReactElement, exp: String): Unit = {
        testRCB(subj, exp)
        //testES6(subj, exp)
      }

      def testRCB(subj: ReactElement, exp: String): Unit =
        ReactComponentB[Unit]("tmp").render(_ => subj).buildU.apply() shouldRender exp
      def testES6(subj: ReactElement, exp: String): Unit = {
        Scalatags(subj) shouldRender exp
      }
      def reactNode: ReactNode = RCB.H1("cool")
      def checkbox(check: Boolean) = input(`type` := "checkbox", checked := check)

      'short     - test(div(45: Short),                        "<div>45</div>")
      'byte      - test(div(50: Byte),                         "<div>50</div>")
      'int       - test(div(666),                              "<div>666</div>")
      'long      - test(div(123L),                             "<div>123</div>")
      'double    - test(div(12.3),                             "<div>12.3</div>")
      'string    - test(div("yo"),                             "<div>yo</div>")
      'reactNode - test(div(reactNode),                        "<div><h1>cool</h1></div>")
      'comp      - test(div(RCB.H1("a")),                          "<div><h1>a</h1></div>")
      'seqTag    - test(div(Seq (span(1), span(2))),           "<div><span>1</span><span>2</span></div>")
      'listTag   - test(div(List(span(1), span(2))),           "<div><span>1</span><span>2</span></div>")
      'listComp  - test(div(List(RCB.H1("a"), RCB.H1("b"))),           "<div><h1>a</h1><h1>b</h1></div>")
      'list2jAry - test(div(List(ES6.H1("a"), RCB.H1("b")).toJsArray), "<div><h1>a</h1><h1>b</h1></div>")
      'jAryTag   - test(div(JArray(span(1), span(2))),         "<div><span>1</span><span>2</span></div>")
      'jAryComp  - test(div(JArray(RCB.H1("a"), RCB.H1("b"))),         "<div><h1>a</h1><h1>b</h1></div>")
      'checkboxT - test(checkbox(true),                      """<input type="checkbox" checked=""/>""") // checked="" is new as of React 0.14 but it works
      'checkboxF - test(checkbox(false),                     """<input type="checkbox"/>""")
      'aria      - test(div(aria.label := "ow", "a"),        """<div aria-label="ow">a</div>""")

      'dangerouslySetInnerHtml - test(div(dangerouslySetInnerHtml("<span>")), "<div><span></div>")

      'optional {
        'option {
          'attr_some    - test(div(cls := "hi".some),    """<div class="hi"></div>""")
          'attr_none    - test(div(cls := "hi".none),    """<div></div>""")
          'style_some   - test(div(color := "red".some), """<div style="color:red;"></div>""")
          'style_none   - test(div(color := "red".none), """<div></div>""")
          'tagmod_some  - test(div(tagmod.some),         """<div class="ho"></div>""")
          'tagmod_none  - test(div(tagmod.none),         """<div></div>""")
          'tag_some     - test(div(reacttag.some),       """<div><span></span></div>""")
          'tag_none     - test(div(reacttag.none),       """<div></div>""")
          'element_some - test(div(relement.some),       """<div><span></span></div>""")
          'element_none - test(div(relement.none),       """<div></div>""")
          'comp_some    - test(div(RCB.H1("yoo").some),      """<div><h1>yoo</h1></div>""")
          'comp_none    - test(div(RCB.H1("yoo").none),      """<div></div>""")
        }
        'jsUndefOr {
          'attr_def      - test(div(cls := "hi".jsdef),    """<div class="hi"></div>""")
          'attr_undef    - test(div(cls := "h1".undef),    """<div></div>""")
          'style_def     - test(div(color := "red".jsdef), """<div style="color:red;"></div>""")
          'style_undef   - test(div(color := "red".undef), """<div></div>""")
          'tagmod_def    - test(div(tagmod.jsdef),         """<div class="ho"></div>""")
          'tagmod_undef  - test(div(tagmod.undef),         """<div></div>""")
          'tag_def       - test(div(reacttag.jsdef),       """<div><span></span></div>""")
          'tag_undef     - test(div(reacttag.undef),       """<div></div>""")
          'element_def   - test(div(relement.jsdef),       """<div><span></span></div>""")
          'element_undef - test(div(relement.undef),       """<div></div>""")
          'comp_def      - test(div(RCB.H1("yoo").jsdef),      """<div><h1>yoo</h1></div>""")
          'comp_undef    - test(div(RCB.H1("yoo").undef),      """<div></div>""")
        }
        'maybe {
          import ScalazReact._
          'attr_just     - test(div(cls := "hi".just),        """<div class="hi"></div>""")
          'attr_empty    - test(div(cls := "h1".maybeNot),    """<div></div>""")
          'style_just    - test(div(color := "red".just),     """<div style="color:red;"></div>""")
          'style_empty   - test(div(color := "red".maybeNot), """<div></div>""")
          'tagmod_just   - test(div(tagmod.just),             """<div class="ho"></div>""")
          'tagmod_empty  - test(div(tagmod.maybeNot),         """<div></div>""")
          'tag_just      - test(div(reacttag.just),           """<div><span></span></div>""")
          'tag_empty     - test(div(reacttag.maybeNot),       """<div></div>""")
          'element_just  - test(div(relement.just),           """<div><span></span></div>""")
          'element_empty - test(div(relement.maybeNot),       """<div></div>""")
          'comp_just     - test(div(RCB.H1("yoo").just),          """<div><h1>yoo</h1></div>""")
          'comp_empty    - test(div(RCB.H1("yoo").maybeNot),      """<div></div>""")
        }
      }

      'tagmodComposition {
        val a: TagMod = cls := "hehe"
        val b: TagMod = h3("Good")
        val c = a compose b
        test(div(c), """<div class="hehe"><h3>Good</h3></div>""")
      }

      'combination - test(
        div(cls := "hi", "Str: ", 123, JArray(RCB.H1("a"), RCB.H1("b")), p(cls := "pp")("!")),
        """<div class="hi">Str: 123<h1>a</h1><h1>b</h1><p class="pp">!</p></div>""")

      'styles - test(
        div(backgroundColor := "red", marginTop := "10px", "!"),
        """<div style="background-color:red;margin-top:10px;">!</div>""")

      'noImplicitUnit - assertTypeMismatch(compileError("""val x: TagMod = ()"""))

      'numericStyleUnits {
        'px  - test(div(marginTop := 2.px),  """<div style="margin-top:2px;"></div>""")
        'ex  - test(div(marginTop := 2.ex),  """<div style="margin-top:2ex;"></div>""")
        'em  - test(div(marginTop := 2.em),  """<div style="margin-top:2em;"></div>""")
        'str - assertContains(compileError("""div(marginTop := "hehe".em)""").msg, "not a member of String")
      }

      "?=" - test(
        span(
          true ?= (color := "red"), false ?= (color := "black"),
          true ?= (cls := "great"), false ?= (cls := "saywhat"),
          "ok"),
        """<span class="great" style="color:red;">ok</span>""")

      // Copied from Scalatags
      'copied {

        'attributeChaining - test(
          div(`class` := "thing lol", id := "cow"),
          """<div id="cow" class="thing lol"></div>""")

        'mixingAttributesStylesAndChildren - test(
          div(id := "cow", float.left, p("i am a cow")),
          """<div id="cow" style="float:left;"><p>i am a cow</p></div>""")

        //class/style after attr appends, but attr after class/style overwrites
//        'classStyleAttrOverwriting - test(
//          div(cls := "my-class", style := "background-color: red;", float.left, p("i am a cow")),
//          """<div class="my-class" style="background-color:red;float:left;"><p>i am a cow</p></div>""")

        'intSeq - test(
          div(h1("Hello"), for (i <- 0 until 5) yield i),
          """<div><h1>Hello</h1>01234</div>""")

        'stringArray - {
          val strArr = Array("hello")
          test(div("lol".some, 1.some, None: Option[String], h1("Hello"), Array(1, 2, 3), strArr, EmptyTag),
            """<div>lol1<h1>Hello</h1>123hello</div>""")
        }

        'applyChaining - test(
          a(tabIndex := 1, cls := "lol")(href := "boo", alt := "g"),
          """<a tabindex="1" href="boo" alt="g" class="lol"></a>""")
      }

      'customAttr  - test(div("accept".reactAttr := "yay"), """<div accept="yay"></div>""")
      'customStyle - test(div("face".reactStyle := "yay"),  """<div style="face:yay;"></div>""")
      'customTag   - test(("ass".reactTag)("Snicker"),      """<ass>Snicker</ass>""")
    }

    'classSet {
      'allConditional {
        val rcb = ReactComponentB[(Boolean,Boolean)]("C").render_P(p => div(classSet("p1" -> p._1, "p2" -> p._2))("x")).build
        @ScalaJSDefined
        class AllConditionalC extends ReactComponentNoState[(Boolean, Boolean), HTMLElement] {
          override def render() = div(classSet("p1" -> props._1, "p2" -> props._2))("x")
        }
        val es6 = ElementFactory.requiredProps(js.constructorOf[AllConditionalC], classOf[AllConditionalC])
        def test(p: (Boolean, Boolean), exp: String) = {
          rcb(p) shouldRender exp
          //es6(p) shouldRender exp
        }
        test((false, false), """<div>x</div>""")
        test((true,  false), """<div class="p1">x</div>""")
        test((false, true), """<div class="p2">x</div>""")
        test((true,  true), """<div class="p1 p2">x</div>""")
      }
      'hasMandatory {
        val rcb = ReactComponentB[Boolean]("C").render_P(p => div(classSet1("mmm", "ccc" -> p))("x")).build
        @ScalaJSDefined
        class HasMandatoryC extends ReactComponentNoState[Boolean, HTMLElement] {
          override def render() = div(classSet1("mmm", "ccc" -> props))("x")
        }
        val es6 = ElementFactory.requiredProps(js.constructorOf[HasMandatoryC], classOf[HasMandatoryC])
        def test(p: Boolean, exp: String) = {
          rcb(p) shouldRender exp
          //es6(p) shouldRender exp
        }
        test(false, """<div class="mmm">x</div>""")
        test(true, """<div class="mmm ccc">x</div>""")
      }
      'appends {
        val rcb = ReactComponentB[Boolean]("C").render_P(p =>
          div(cls := "neat", classSet1("mmm", "ccc" -> p), cls := "slowclap", "x")).build
        @ScalaJSDefined
        class AppendsC extends ReactComponentNoState[Boolean, HTMLElement] {
          override def render() = div(cls := "neat", classSet1("mmm", "ccc" -> props), cls := "slowclap", "x")
        }
        val es6 = ElementFactory.requiredProps(js.constructorOf[AppendsC], classOf[AppendsC])
        def test(p: Boolean, exp: String) = {
          rcb(p) shouldRender exp
          //es6(p) shouldRender exp
        }
        test(false, """<div class="neat mmm slowclap">x</div>""")
        test(true, """<div class="neat mmm ccc slowclap">x</div>""")
      }
    }

    'props {
      'unit {
        val rcb = ReactComponentB[Unit]("U").render_C(c => h1(c)).buildU
        @ScalaJSDefined
        class UnitC extends ReactComponentNoPropsAndState[HTMLElement] {
          override def render() = h1(children)
        }
        val es6 = ElementFactory.noProps(js.constructorOf[UnitC], classOf[UnitC])
        def test(c: ReactNode, exp: String) = {
          rcb(c) shouldRender exp
          //es6(p) shouldRender exp
        }
        test(div("great"), "<h1><div>great</div></h1>")
      }

      'required {
        val rcb = ReactComponentB[String]("C").render_P(name => div("Hi ", name)).build
        @ScalaJSDefined
        class RequiredC extends ReactComponentNoState[String, HTMLElement] {
          override def render() = div("Hi ", props)
        }
        val es6 = ElementFactory.requiredProps(js.constructorOf[RequiredC], classOf[RequiredC])
        def test(p: String, exp: String) = {
          rcb(p) shouldRender exp
          //es6(p) shouldRender exp
        }
        test("Mate", "<div>Hi Mate</div>")
      }

      val rcb = ReactComponentB[String]("C").render_P(name => div("Hey ", name)).propsDefault("man").build
      @ScalaJSDefined
      class OptionalC extends ReactComponentNoState[String, HTMLElement] {
        override def render() = div("Hi ", props)
      }
      val es6 = ElementFactory.defaultProps(js.constructorOf[OptionalC], classOf[OptionalC])("man")
      def test(p: Option[String], exp: String) = {
        p match {
          case Some(_) =>
            rcb(p) shouldRender exp
            //es6(p) shouldRender exp
          case None =>
            rcb() shouldRender exp
            //es6() shouldRender exp
        }
      }
      'optionalNone {
        test(None, "<div>Hey man</div>")
      }
      'optionalSome {
        test(Some("dude"), "<div>Hey dude</div>")
      }

      'always {
        val rcb = ReactComponentB[String]("C").render_P(name => div("Hi ", name)).propsConst("there").build
        @ScalaJSDefined
        class AlwaysC extends ReactComponentNoState[String, HTMLElement] {
          override def render() = div("Hi ", props)
        }
        val es6 = ElementFactory.constantProps(js.constructorOf[AlwaysC], classOf[AlwaysC])("there")
        def test(exp: String) = {
          rcb() shouldRender exp
          //es6() shouldRender exp
        }
        test("<div>Hi there</div>")
      }
    }

    'builder {
      'configure {
        var called = 0
        val f = (_: ReactComponentB[Unit,Unit,Unit,TopNode]).componentWillMount(_ => Callback(called += 1))
        val c = ReactComponentB[Unit]("X").render(_ => div("")).configure(f, f).buildU
        ReactTestUtils.renderIntoDocument(c())
        assert(called == 2)
      }
    }

    'keys {
      'specifiableThruCtor {
        val k1 = "heasdf"
        val xx = RCB.CA.withKey(k1)()
        val k2 = xx.key
        k2 mustEqual k1
//        val xx2 = ES6.CA.withKey(k1)()
//        val k22 = xx2.key
//        k22 mustEqual k1
      }
    }

    'children {
      'argsToComponents {
        'listOfScalatags {
          def test[P,S,B,N <: TopNode](c: ReactComponentU[P,S,B,N]): Unit = {
            c shouldRender "<div><h1>nice</h1><h2>good</h2></div>"
          }
          test(RCB.CA(List(h1("nice"), h2("good"))))
          //test(ES6.CA(List(h1("nice"), h2("good"))))
        }

        'listOfReactComponents {
          def test[P,S,B,N <: TopNode](c: ReactComponentU[P,S,B,N]): Unit = {
            c shouldRender "<div><span><h1>nice</h1></span><span><h2>good</h2></span></div>"
          }
          test(RCB.CA(List(RCB.CB(h1("nice")), RCB.CB(h2("good")))))
          //test(ES6.CA(List(ES6.CB(h1("nice")), ES6.CB(h2("good")))))
        }
      }

      'rendersGivenChildren {
        'none {
          def test[P,S,B,N <: TopNode](c: ReactComponentU[P,S,B,N]): Unit = {
            c shouldRender "<div></div>"
          }
          test(RCB.CA())
          //test(ES6.CA())
        }
        'one {
          def test[P,S,B,N <: TopNode](c: ReactComponentU[P,S,B,N]): Unit = {
            c shouldRender "<div><h1>yay</h1></div>"
          }
          test(RCB.CA(h1("yay")))
          //test(ES6.CA(h1("yay")))
        }
        'two {
          def test[P,S,B,N <: TopNode](c: ReactComponentU[P,S,B,N]): Unit = {
            c shouldRender "<div><h1>yay</h1><h3>good</h3></div>"
          }
          test(RCB.CA(h1("yay"), h3("good")))
          //test(ES6.CA(h1("yay"), h3("good")))
        }
        'nested {
          def test[P,S,B,N <: TopNode](c: ReactComponentU[P,S,B,N]): Unit = {
            c shouldRender "<div><span><h1>nice</h1></span></div>"
          }
          test(RCB.CA(RCB.CB(h1("nice"))))
          //test(ES6.CA(ES6.CB(h1("nice"))))
        }
      }

      'forEach {
        val C1 = collectorNC[ReactNode]((l, c) => c.forEach(l append _))
        val C2 = collectorNC[(ReactNode, Int)]((l, c) => c.forEach((a, b) => l.append((a, b))))

        'withoutIndex {
          val x = runNC(C1, h1("yay"), h3("good"))
          assert(x.size == 2)
        }

        'withIndex {
          val x = runNC(C2, h1("yay"), h3("good"))
          assert(x.size == 2)
          assert(x.toList.map(_._2) == List(0,1))
        }
      }

      'only {
        val A = collector1C[Option[ReactNode]](_.only)

        'one {
          val r = run1C(A, div("Voyager (AU) is an awesome band"))
          assert(r.isDefined)
        }

        'two {
          val r = run1C(A, div("The Pensive Disarray"), div("is such a good song"))
          assert(r == None)
        }
      }
    }

    'stateFocus {
      // def inc(s: CompStateFocus[Int]) = s.modState(_ * 3)
      case class SI(s: String, i: Int)
      val C = ReactComponentB[SI]("C").initialState_P(p => p).render($ => {
        val f = $.zoom(_.i)((a,b) => a.copy(i = b))
        // inc(f)
        div($.state.s + "/" + (f.state*3))
      }).build
      C(SI("Me",7)) shouldRender "<div>Me/21</div>"
    }

    'mountedStateAccess {
      val c = ReactTestUtils.renderIntoDocument(RCB.SI())
      assert(c.state == 123)
//      val c2 = ReactTestUtils.renderIntoDocument(ES6.SI())
//      assert(c2.state == 123)
    }

    'builtWithDomType {
      val c = ReactTestUtils.renderIntoDocument(RCB.SI())
      val v = ReactDOM.findDOMNode(c).value // Look, it knows its DOM node type
      assert(v == "123")
//      val c2 = ReactTestUtils.renderIntoDocument(ES6.SI())
//      val v2 = ReactDOM.findDOMNode(c2).value // Look, it knows its DOM node type
//      assert(v2 == "123")
    }

    'selectWithMultipleValues {
      val s = ReactComponentB[Unit]("s")
        .render(T =>
          select(multiple := true, value := js.Array("a", "c"))(
            option(value := "a")("a"),
            option(value := "b")("b"),
            option(value := "c")("c")
          )
        )
        .domType[HTMLSelectElement]
        .buildU

      val c = ReactTestUtils.renderIntoDocument(s())
      val sel = ReactDOM.findDOMNode(c)
      val options = sel.options.asInstanceOf[js.Array[HTMLOptionElement]] // https://github.com/scala-js/scala-js-dom/pull/107
      val selectedOptions = options filter (_.selected) map (_.value)
      assert(selectedOptions.toSet == Set("a", "c"))
    }

    'inference {
      import TestUtil.Inference._
      def st_get: S => T = null
      def st_set: (S, T) => S = null

      "DuringCallbackU ops" - test[DuringCallbackU[P, S, U]   ](_.zoom(st_get)(st_set)).expect[ReadDirectWriteCallbackOps[T]]
      "DuringCallbackM ops" - test[DuringCallbackM[P, S, U, N]](_.zoom(st_get)(st_set)).expect[ReadDirectWriteCallbackOps[T]]
      "BackendScope    ops" - test[BackendScope   [P, S]      ](_.zoom(st_get)(st_set)).expect[ReadCallbackWriteCallbackOps[T]]
      "ReactComponentM ops" - test[ReactComponentM[P, S, U, N]](_.zoom(st_get)(st_set)).expect[ReadDirectWriteDirectOps[T]]

      "DuringCallbackU props" - test[DuringCallbackU[P, S, U]   ](_.props).expect[P]
      "DuringCallbackM props" - test[DuringCallbackM[P, S, U, N]](_.props).expect[P]
      "WillUpdate      props" - test[WillUpdate     [P, S, U, N]](_.props).expect[P]
      "BackendScope    props" - test[BackendScope   [P, S]      ](_.props).expect[CallbackTo[P]]
      "ReactComponentM props" - test[ReactComponentM[P, S, U, N]](_.props).expect[P]

      "DuringCallbackU state" - test[DuringCallbackU[P, S, U]   ](_.state).expect[S]
      "DuringCallbackM state" - test[DuringCallbackM[P, S, U, N]](_.state).expect[S]
      "WillUpdate      state" - test[WillUpdate     [P, S, U, N]](_.state).expect[S]
      "BackendScope    state" - test[BackendScope   [P, S]      ](_.state).expect[CallbackTo[S]]
      "ReactComponentM state" - test[ReactComponentM[P, S, U, N]](_.state).expect[S]

      "DuringCallbackU state" - test[DuringCallbackU[P, S, U]   ](_.zoom(st_get)(st_set).state).expect[T]
      "DuringCallbackM state" - test[DuringCallbackM[P, S, U, N]](_.zoom(st_get)(st_set).state).expect[T]
      "BackendScope    state" - test[BackendScope   [P, S]      ](_.zoom(st_get)(st_set).state).expect[CallbackTo[T]]
      "ReactComponentM state" - test[ReactComponentM[P, S, U, N]](_.zoom(st_get)(st_set).state).expect[T]
    }

    'shouldCorrectlyDetermineIfComponentIsMounted {
      val C = ReactComponentB[Unit]("IsMountedTestComp")
          .render(P => div())
          .componentWillMount(scope => Callback(assert(!scope.isMounted())))
          .componentDidMount(scope => Callback(assert(scope.isMounted())))
          .buildU
      val instance =  ReactTestUtils.renderIntoDocument(C())
      assert(instance.isMounted())
    }

    'findDOMNode {
      val m = ReactTestUtils renderIntoDocument RCB.H1("good")
      val n = ReactDOM.findDOMNode(m)
      removeReactDataAttr(n.outerHTML) mustEqual "<h1>good</h1>"
    }

    // Changed to an extension method that calls ReactDOM.findDOMNode
    'getDOMNode {
      val m = ReactTestUtils renderIntoDocument RCB.H1("good")
      val n = m.getDOMNode()
      removeReactDataAttr(n.outerHTML) mustEqual "<h1>good</h1>"
    }

    'domTypeBeforeCallbacks {
      ReactComponentB[Unit]("").stateless
        .render(_ => canvas())
        .domType[HTMLCanvasElement]
        .componentDidMount($ => Callback(ReactDOM.findDOMNode($).getContext("2d")))
        .buildU
    }

    'multiModState {
      'simple {
        val C = ReactComponentB[Unit]("multiModState")
          .initialState(3)
          .render { $ =>
            val add7 = $.modState(_ + 7)
            val add1 = $.modState(_ + 1)
            button(onClick --> (add1 >> add7))
          }
          .buildU
        val c = ReactTestUtils.renderIntoDocument(C())
        c.state mustEqual 3
        Simulation.click run c
        c.state mustEqual 11
      }
      'zoom {
        val C = ReactComponentB[Unit]("multiModState")
          .initialState(StrInt("yay", 3))
          .render { $ =>
            val $$ = $.zoom(_.int)((a,b) => a.copy(int = b))
            val add7 = $$.modState(_ + 7)
            val add1 = $$.modState(_ + 1)
            button(onClick --> (add1 >> add7))
          }
          .buildU
        val c = ReactTestUtils.renderIntoDocument(C())
        c.state mustEqual StrInt("yay", 3)
        Simulation.click run c
        c.state mustEqual StrInt("yay", 11)
        c.setState(StrInt("oh", 100))
        Simulation.click run c
        c.state mustEqual StrInt("oh", 108)
      }
      'zoomL {
        import MonocleReact._ // TODO Move
        val C = ReactComponentB[Unit]("multiModState")
          .initialState(StrInt("yay", 3))
          .render { $ =>
            val $$ = $ zoomL StrInt.int
            val add7 = $$.modState(_ + 7)
            val add1 = $$.modState(_ + 1)
            button(onClick --> (add1 >> add7))
          }
          .buildU
        val c = ReactTestUtils.renderIntoDocument(C())
        c.state mustEqual StrInt("yay", 3)
        Simulation.click run c
        c.state mustEqual StrInt("yay", 11)
        c.setState(StrInt("oh", 100))
        Simulation.click run c
        c.state mustEqual StrInt("oh", 108)
      }
      'zoomL2 {
        import MonocleReact._ // TODO Move
        val C = ReactComponentB[Unit]("multiModState")
          .initialState(StrIntWrap(StrInt("yay", 3)))
          .render { $ =>
            val $$ = $ zoomL StrIntWrap.strInt zoomL StrInt.int
            val add7 = $$.modState(_ + 7)
            val add1 = $$.modState(_ + 1)
            button(onClick --> (add1 >> add7))
          }
          .buildU
        val c = ReactTestUtils.renderIntoDocument(C())
        c.state mustEqual StrIntWrap(StrInt("yay", 3))
        Simulation.click run c
        c.state mustEqual StrIntWrap(StrInt("yay", 11))
        c.setState(StrIntWrap(StrInt("oh", 100)))
        Simulation.click run c
        c.state mustEqual StrIntWrap(StrInt("oh", 108))
      }
    }

    'domExt {
      import TestUtil.Inference._
      'domCast   - test[Node](_.domCast[HTMLInputElement]).expect[HTMLInputElement]
      'domAsHtml - test[Node](_.domAsHtml).expect[HTMLElement]
      'domToHtml {
        import org.scalajs.dom._
        val input = document.createElement("input")
        input.domToHtml mustEqual Some(input.asInstanceOf[HTMLElement])
      }
    }
  }
}
