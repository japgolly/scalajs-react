* Generalise ReactCallbackExtensions with EffectUtil

* userdefined.(Unsafe)Effect?

* Remove temp scripts

* Add WithEffect/WithAsyncEffect or support more than just the default effect type
  * component.*
  * DefaultReusabilityOverlay
  * Hooks.scala
  * Router
  * RouterCtl
  * RouterWithPropsConfig.scala
  * RoutingRule
  * RoutingRules
  * StateAccess (*Pure)
  * StateSnapshotF
  * TriStateCheckbox
and then in testUtil:
  * TestBroadcaster
  * ReactTestVarF

* Test React.Suspense

* Re-enable cats/monocle/scalaz tests

* Revise all module names (and be consistent with local module dir & sbt names)

* Add tests with callbacks accepting non-CallbackTo types
* Rename Step4 => LastStep

* Export Effects as ReactEffect?

* Include a default IORuntime in coreDefCE?

* Show `modules.gv.svg` in doc and changelog
  * clarify new imports
  * migration
  * update getting-started/usage instructions

* At the very end, compare the total diff of the tests - it should be as minimal as possible and if there
  are any mandatory changes, confirm them and add to changelog & migration guide (shouldn't be)

* removal of state-monad extensions
  * update changelog
  * update FP.md

* Add effect trans methods/extensions  (eg. `.to[IO]` and `.to[CallbackTo]`)?

* Test coreDef* from downstream-tests

* Fix ScalaDoc links

* publishLocal, confirm the export set, confirm the module names

* resolve todos

==============================================================================================================

## Problem
Can't have a provided-scope, overridable DefaultEffects module.
It works until linking in certain circumstances.

Example, `def f: Sync[Unit]` becomes `def f: Any(Ref?)` in abstract (`coreGeneric`),
but then `def f: Trampoline` in specific (`tests`) and fails to link.

```
[error] Referring to non-existent method japgolly.scalajs.react.Ref$Full.get()japgolly.scalajs.react.callback.Trampoline
```

## Rejected Solution: wrapper
Provide a constant (non-AnyVal) wrapper that has the same erasure.
The problem is that will prevent instances of `Callback`/`IO` being returned in general.
Instead it would be `Blah[Callback]` or `Blah[IO]` which would be very annoying.

## Terrible Solution: two separate copies
Absolutely terrible but a potential last resort. It would completely prevent abstraction.

## Potential Solution: parameterise and never return Sync directly
Tried and it works!

==============================================================================================================

```scala
      "maybe" - {
        import ScalazReact._
        import scalaz.Maybe
        def some[A](a: A): Maybe[A] = Maybe.Just(a)
        @nowarn("cat=unused") def none[A](a: A): Maybe[A] = Maybe.empty
        "attr_some"    - test(<.div(^.cls   :=? some("hi")       ), """<div class="hi"></div>""")
        "attr_none"    - test(<.div(^.cls   :=? none("h1")       ), """<div></div>""")
        "style_some"   - test(<.div(^.color :=? some("red")      ), """<div style="color:red"></div>""")
        "style_none"   - test(<.div(^.color :=? none("red")      ), """<div></div>""")
        "tagMod_some"  - test(<.div(some(tagMod     ).whenDefined), """<div class="ho"></div>""")
        "tagMod_none"  - test(<.div(none(tagMod     ).whenDefined), """<div></div>""")
        "tag_some"     - test(<.div(some(vdomTag    ).whenDefined), """<div><span></span></div>""")
        "tag_none"     - test(<.div(none(vdomTag    ).whenDefined), """<div></div>""")
        "element_some" - test(<.div(some(vdomElement).whenDefined), """<div><p></p></div>""")
        "element_none" - test(<.div(none(vdomElement).whenDefined), """<div></div>""")
        "comp_some"    - test(<.div(some(H1("yoo")  ).whenDefined), """<div><h1>yoo</h1></div>""")
        "comp_none"    - test(<.div(none(H1("yoo")  ).whenDefined), """<div></div>""")
        "text_some"    - test(<.div(some("yoo"      ).whenDefined), """<div>yoo</div>""")
        "text_none"    - test(<.div(none("yoo"      ).whenDefined), """<div></div>""")
      }

      "maybe" - {
        import ScalazReact._
        import scalaz.Maybe
        def some[A](a: A): Maybe[A] = Maybe.Just(a)
        @nowarn("cat=unused") def none[A](a: A): Maybe[A] = Maybe.empty
        "attr_some"    - test(div(cls   :=? some("hi")         ), """<div class="hi"></div>""")
        "attr_none"    - test(div(cls   :=? none("h1")         ), """<div></div>""")
        "style_some"   - test(div(color :=? some("red")        ), """<div style="color:red"></div>""")
        "style_none"   - test(div(color :=? none("red")        ), """<div></div>""")
        "tagMod_some"  - test(div(some(tagMod     ).whenDefined), """<div class="ho"></div>""")
        "tagMod_none"  - test(div(none(tagMod     ).whenDefined), """<div></div>""")
        "tag_some"     - test(div(some(vdomTag    ).whenDefined), """<div><span></span></div>""")
        "tag_none"     - test(div(none(vdomTag    ).whenDefined), """<div></div>""")
        "element_some" - test(div(some(vdomElement).whenDefined), """<div><p></p></div>""")
        "element_none" - test(div(none(vdomElement).whenDefined), """<div></div>""")
        "comp_some"    - test(div(some(H1("yoo")  ).whenDefined), """<div><h1>yoo</h1></div>""")
        "comp_none"    - test(div(none(H1("yoo")  ).whenDefined), """<div></div>""")
        "text_some"    - test(div(some("yoo"      ).whenDefined), """<div>yoo</div>""")
        "text_none"    - test(div(none("yoo"      ).whenDefined), """<div></div>""")
      }

import japgolly.scalajs.react.MonocleReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.StateSnapshot.{ModFn, SetFn}
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.vdom.html_<^._
import monocle._
import scala.annotation.nowarn
import utest._

    "rezoom" - testReZoomWithReuse()

  private def testReZoomWithReuse(): Unit = {

    var intRenders = 0
    val IntComp = ScalaComponent.builder[StateSnapshot[Int]]("")
      .render_P { p =>
        intRenders += 1
        <.span(p.value, ^.onClick --> p.modState(_ + 1))
      }
      .configure(Reusability.shouldComponentUpdate)
      .build

    // -----------------------------------------------------------------------------------------------------------------

    var strRenders = 0
    val StrComp = ScalaComponent.builder[StateSnapshot[String]]("")
      .render_P { p =>
        strRenders += 1
        <.strong(p.value, ^.onClick --> p.modState(_ + "!"))
      }
      .configure(Reusability.shouldComponentUpdate)
      .build

    // -----------------------------------------------------------------------------------------------------------------

    final case class X(int: Int, str: String)

    object X {
      val int = Lens[X, Int   ](_.int)(x => _.copy(int = x))
      val str = Lens[X, String](_.str)(x => _.copy(str = x))
      implicit def equal: UnivEq[X] = UnivEq.force
      implicit val reusability: Reusability[X] = Reusability.derive
    }

    // -----------------------------------------------------------------------------------------------------------------

    object Middle {

      var renders = 0

      final case class Props(name: String, ss: StateSnapshot[X]) {
        @inline def render = Comp(this)
      }

      implicit def reusability: Reusability[Props] =
        Reusability.derive

      final class Backend($: BackendScope[Props, Unit]) {

        private val ssIntFn =
          StateSnapshot.withReuse.zoomL(X.int).prepareViaProps($)(_.ss)

        private val ssStrFn =
          StateSnapshot.withReuse.zoomL(X.str).prepareViaProps($)(_.ss)

        def render(p: Props): VdomElement = {
          renders += 1

          val ssI: StateSnapshot[Int] =
            ssIntFn(p.ss.value)

          val ssS: StateSnapshot[String] =
            ssStrFn(p.ss.value)

          <.div(
            <.h3(p.name),
            IntComp(ssI),
            StrComp(ssS))
        }
      }

      val Comp = ScalaComponent.builder[Props]("")
        .renderBackend[Backend]
        .configure(Reusability.shouldComponentUpdate)
        .build
    }

    // -----------------------------------------------------------------------------------------------------------------

    object Top {

      final class Backend($: BackendScope[Unit, X]) {
        private val setStateFn =
          StateSnapshot.withReuse.prepareVia($)

        def render(state: X): VdomElement = {
          val ss = setStateFn(state)
          Middle.Props("Demo", ss).render
        }
      }

      val Comp = ScalaComponent.builder[Unit]("")
        .initialState(X(0, "yo"))
        .renderBackend[Backend]
        .build

    }

    // -----------------------------------------------------------------------------------------------------------------

    def counts() = (Middle.renders, intRenders, strRenders)

    ReactTestUtils.withNewBodyElement { mountNode =>
      val mounted = Top.Comp().renderIntoDOM(mountNode)
      def dom() = mounted.getDOMNode.asMounted().asElement()
      def intDom() = dom().querySelector("span")
      def strDom() = dom().querySelector("strong")
      def values() = mounted.state -> counts()

      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>0</span><strong>yo</strong></div>")
      assertEq(values(), X(0, "yo") -> (1, 1, 1))

      Simulate click intDom()
      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>1</span><strong>yo</strong></div>")
      assertEq(values(), X(1, "yo") -> (2, 2, 1)) // notice that StrComp didn't re-render

      Simulate click strDom()
      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>1</span><strong>yo!</strong></div>")
      assertEq(values(), X(1, "yo!") -> (3, 2, 2)) // notice that IntComp didn't re-render

      Simulate click intDom()
      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>2</span><strong>yo!</strong></div>")
      assertEq(values(), X(2, "yo!") -> (4, 3, 2)) // notice that StrComp didn't re-render

      Simulate click strDom()
      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>2</span><strong>yo!!</strong></div>")
      assertEq(values(), X(2, "yo!!") -> (5, 3, 3)) // notice that IntComp didn't re-render
    }

  } // testReZoomWithReuse

```