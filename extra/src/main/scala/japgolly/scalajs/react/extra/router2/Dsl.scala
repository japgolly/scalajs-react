package japgolly.scalajs.react.extra.router2

import java.util.regex.{Pattern, Matcher}
import monocle.{Lens, Prism, Iso}
import scala.reflect.ClassTag
import scala.util.matching.Regex
import scalaz.{Equal, \/-, -\/, Monoid}
import scalaz.Isomorphism.<=>
import scalaz.syntax.equal._
import japgolly.scalajs.react.ReactElement
import RouterConfig.Parsed

/**
 * This is not meant to be imported by library-users;
 * [[RouterConfigDsl]] is the entire library-user-facing facade & DSL.
 */
object StaticDsl {

  private val regexEscape1 = """([-()\[\]{}+?*.$\^|,:#<!\\])""".r
  private val regexEscape2 = """\x08""".r

  /**
   * Pattern.quote doesn't work in Scala.JS.
   *
   * http://stackoverflow.com/questions/2593637/how-to-escape-regular-expression-in-javascript
   */
  def regexEscape(s: String): String = {
    var r = s
    r = regexEscape1.replaceAllIn(r, """\\$1""")
    r = regexEscape2.replaceAllIn(r, """\\x08""")
    r
  }

  /**
   * Route builder. Allows you to specify routes like `"user" / int / "display"`.
   * Once complete, [[RouteB]] will become a [[Route]].
   */
  object RouteB {
    trait Composition[A, B] {
      type C
      val ga: C => A
      val gb: C => B
      val gc: (A, B) => C
      def apply(fa: RouteB[A], fb: RouteB[B]): RouteB[C] =
        new RouteB(
          fa.regex + fb.regex,
          fa.matchGroups + fb.matchGroups,
          g => for {a <- fa.parse(g); b <- fb.parse(i => g(i + fa.matchGroups))} yield gc(a, b),
          c => fa.build(ga(c)) + fb.build(gb(c)))
    }

    trait Composition_PriLowest {
      implicit def ***[A, B] = Composition[A, B, (A, B)](_._1, _._2, (_, _))
    }
    trait Composition_PriLow extends Composition_PriLowest {
      implicit def T8[A, B, C, D, E, F, G, H] = Composition[(A, B, C, D, E, F, G), H, (A, B, C, D, E, F, G, H)](r => (r._1, r._2, r._3, r._4, r._5, r._6, r._7), _._8, (l, r) => (l._1, l._2, l._3, l._4, l._5, l._6, l._7, r))
      implicit def T7[A, B, C, D, E, F, G] = Composition[(A, B, C, D, E, F), G, (A, B, C, D, E, F, G)](r => (r._1, r._2, r._3, r._4, r._5, r._6), _._7, (l, r) => (l._1, l._2, l._3, l._4, l._5, l._6, r))
      implicit def T6[A, B, C, D, E, F] = Composition[(A, B, C, D, E), F, (A, B, C, D, E, F)](r => (r._1, r._2, r._3, r._4, r._5), _._6, (l, r) => (l._1, l._2, l._3, l._4, l._5, r))
      implicit def T5[A, B, C, D, E] = Composition[(A, B, C, D), E, (A, B, C, D, E)](r => (r._1, r._2, r._3, r._4), _._5, (l, r) => (l._1, l._2, l._3, l._4, r))
      implicit def T4[A, B, C, D] = Composition[(A, B, C), D, (A, B, C, D)](r => (r._1, r._2, r._3), _._4, (l, r) => (l._1, l._2, l._3, r))
      implicit def T3[A, B, C] = Composition[(A, B), C, (A, B, C)](r => (r._1, r._2), _._3, (l, r) => (l._1, l._2, r))
    }
    trait Composition_PriMed extends Composition_PriLow {
      implicit def _toA[A] = Composition[Unit, A, A](_ => (), identity, (_, a) => a)
      implicit def Ato_[A] = Composition[A, Unit, A](identity, _ => (), (a, _) => a)
    }
    object Composition extends Composition_PriMed {
      implicit def _to_ = Composition[Unit, Unit, Unit](_ => (), _ => (), (_, _) => ())

      type Aux[A, B, O] = Composition[A, B] {type C = O}

      def apply[A, B, O](a: O => A, b: O => B, c: (A, B) => O): Aux[A, B, O] =
        new Composition[A, B] {
          override type C = O
          val ga = a
          val gb = b
          val gc = c
        }
    }

    private val someUnit = Some(())

    def literal(s: String): RouteB[Unit] =
      new RouteB(regexEscape(s), 0, _ => someUnit, _ => s)

    val / = literal("/")
  }

  /**
   * A fragment of a route. Can be composed with other fragments.
   *
   * @param matchGroups The number of matches that `regex` will capture.
   */
  class RouteB[A](val regex: String,
                  val matchGroups: Int,
                  val parse: (Int => String) => Option[A],
                  val build: A => String) extends RouteCommon[A, RouteB] {
    import RouteB.Composition

    override def toString =
      s"RouteB($regex)"

    def ~[B](next: RouteB[B])(implicit c: Composition[A, B]): RouteB[c.C] =
      c(this, next)

    def /[B](next: RouteB[B])(implicit c: Composition[A, B]): RouteB[c.C] =
      this ~ RouteB./ ~ next

    def parseThen(f: Option[A] => Option[A]): RouteB[A] =
      new RouteB(regex, matchGroups, f compose parse, build)

    def xmap[B](b: A => B)(a: B => A): RouteB[B] =
      new RouteB(regex, matchGroups, parse(_) map b, build compose a)

    def option: RouteB[Option[A]] =
      new RouteB[Option[A]](s"($regex)?", matchGroups + 1,
        g => Some(if (g(0) eq null) None else parse(i => g(i + 1))),
        _.fold("")(build))

    final def route: Route[A] =
      new Route(Pattern.compile("^" + regex + "$"), m => parse(i => m.group(i + 1)), a => Path(build(a)))
  }

  class RouteBO[A](val r: RouteB[Option[A]]) extends AnyVal {

    /**
     * Specify a default value when parsing.
     *
     * Note: Unlike [[withDefault()]] path generation will still explicitly include the default value.
     *
     * Eg. If the path is like "/file[.format]" and the default is JSON, "/file" will be read as "/file.json", but
     * when generating a path with JSON this will generate "/file.json" instead of "/file".
     */
    def parseDefault(default: => A): RouteB[A] =
      r.xmap(_ getOrElse default)(Some(_))

    /**
     * Specify a default value.
     *
     * Note: Unlike [[parseDefault()]] this will affect path generation too.
     *
     * Eg. If the path is like "/file[.format]" and the default is JSON, "/file" will be read as "/file.json", and
     * when generating a path with JSON this will generate "/file" instead of "/file.json".
     */
    def withDefault(default: => A): RouteB[A] =
      withDefaultE(default)(Equal.equalA)

    def withDefaultE(default: => A)(implicit e: Equal[A]): RouteB[A] =
      r.xmap(_ getOrElse default)(a => if (e.equal(default, a)) None else Some(a))
  }

  abstract class RouteCommon[A, R[_]] {
    def xmap[B](b: A => B)(a: B => A): R[B]
    def parseThen(f: Option[A] => Option[A]): R[A]

    final def xmapL[B](l: Iso[A, B]): R[B] =
      xmap(l.get)(l.reverseGet)

    final def xmapI[B](i: A <=> B): R[B] =
      xmap(i.to)(i.from)

    final def filter(f: A => Boolean): R[A] =
      parseThen(_ filter f)

    final def mapParsed[B <: A](f: A => B): R[B] =
      xmap(f)(x => x)

    final def mapInput[B >: A](f: B => A): R[B] =
      xmap[B](x => x)(f)

    final def const[B](b: B)(implicit ev: A =:= Unit, ev2: Unit =:= A): R[B] =
      xmap(_ => b)(_ => ())

    final def caseclass1[Z](apply: A => Z)(unapply: Z => Option[A]): R[Z] =
      xmap(apply)(unapply(_).get)

    final def caseclass2[AA, B, Z](apply: (AA, B) => Z)(unapply: Z => Option[(AA, B)])(implicit ev: A =:= (AA, B), ev2: (AA, B) =:= A): R[Z] =
      xmap(apply.tupled compose ev)(z => ev2(unapply(z).get))

    final def caseclass3[AA, B, C, Z](apply: (AA, B, C) => Z)(unapply: Z => Option[(AA, B, C)])(implicit ev: A =:= (AA, B, C), ev2: (AA, B, C) =:= A): R[Z] =
      xmap(apply.tupled compose ev)(z => ev2(unapply(z).get))

    final def caseclass4[AA, B, C, D, Z](apply: (AA, B, C, D) => Z)(unapply: Z => Option[(AA, B, C, D)])(implicit ev: A =:= (AA, B, C, D), ev2: (AA, B, C, D) =:= A): R[Z] =
      xmap(apply.tupled compose ev)(z => ev2(unapply(z).get))

    final def caseclass5[AA, B, C, D, E, Z](apply: (AA, B, C, D, E) => Z)(unapply: Z => Option[(AA, B, C, D, E)])(implicit ev: A =:= (AA, B, C, D, E), ev2: (AA, B, C, D, E) =:= A): R[Z] =
      xmap(apply.tupled compose ev)(z => ev2(unapply(z).get))

    final def caseclass6[AA, B, C, D, E, F, Z](apply: (AA, B, C, D, E, F) => Z)(unapply: Z => Option[(AA, B, C, D, E, F)])(implicit ev: A =:= (AA, B, C, D, E, F), ev2: (AA, B, C, D, E, F) =:= A): R[Z] =
      xmap(apply.tupled compose ev)(z => ev2(unapply(z).get))

    final def caseclass7[AA, B, C, D, E, F, G, Z](apply: (AA, B, C, D, E, F, G) => Z)(unapply: Z => Option[(AA, B, C, D, E, F, G)])(implicit ev: A =:= (AA, B, C, D, E, F, G), ev2: (AA, B, C, D, E, F, G) =:= A): R[Z] =
      xmap(apply.tupled compose ev)(z => ev2(unapply(z).get))

    final def caseclass8[AA, B, C, D, E, F, G, H, Z](apply: (AA, B, C, D, E, F, G, H) => Z)(unapply: Z => Option[(AA, B, C, D, E, F, G, H)])(implicit ev: A =:= (AA, B, C, D, E, F, G, H), ev2: (AA, B, C, D, E, F, G, H) =:= A): R[Z] =
      xmap(apply.tupled compose ev)(z => ev2(unapply(z).get))
  }

  /**
   * A complete route.
   */
  final class Route[A](pattern: Pattern,
                       parseFn: Matcher => Option[A],
                       buildFn: A => Path) extends RouteCommon[A, Route] {
    override def toString =
      s"Route($pattern)"

    def parseThen(f: Option[A] => Option[A]): Route[A] =
      new Route(pattern, f compose parseFn, buildFn)

    def xmap[B](b: A => B)(a: B => A): Route[B] =
      new Route(pattern, parseFn(_) map b, buildFn compose a)

    def parse(path: Path): Option[A] = {
      val m = pattern.matcher(path.value)
      if (m.matches)
        parseFn(m)
      else
        None
    }

    def pathFor(a: A): Path =
      buildFn(a)
  }

  // ===================================================================================================================

  object Rule {
    def parseOnly[Page](parse: Path => Option[Parsed[Page]]) =
      new Rule[Page](parse, _ => None, _ => None)

    def empty[P]: Rule[P] =
      Rule(_ => None, _ => None, _ => None)

    implicit def monoid[P]: Monoid[Rule[P]] =
      new Monoid[Rule[P]] {
        override def zero = empty
        override def append(a: Rule[P], b: => Rule[P]) = a | b
      }
  }

  /**
   * A single routing rule. Intended to be composed with other [[Rule]]s.
   * When all rules are composed, this is turned into a [[Rules]] instance.
   *
   * @param parse  Attempt to parse a given path.
   * @param path   Attempt to determine the path for some page.
   * @param action Attempt to determine the action when a route resolves to some page.
   * @tparam Page  The type of legal pages.
   */
  final case class Rule[Page](parse : Path => Option[Parsed[Page]],
                              path  : Page => Option[Path],
                              action: Page => Option[Action[Page]]) {

    /**
     * Compose rules.
     */
    def |(that: Rule[Page]): Rule[Page] =
      new Rule[Page](
        parse  || that.parse,
        path   || that.path,
        action || that.action)

    def xmap[A](f: Page => A)(g: A => Page): Rule[A] =
      new Rule[A](
        p => parse(p).map(_.bimap(_ map f, f)),
        path compose g,
        a => action(g(a)).map(_ map f))

    def xmapL[A](l: Iso[Page, A]): Rule[A] =
      xmap(l.get)(l.reverseGet)

    def xmapI[A](i: Page <=> A): Rule[A] =
      xmap(i.to)(i.from)

    def pmap[W](f: Page => W)(pf: PartialFunction[W, Page]): Rule[W] =
      pmapF(f)(pf.lift)

    def pmapCT[W](f: Page => W)(implicit ct: ClassTag[Page]): Rule[W] =
      pmapF(f)(ct.unapply)

    def pmapL[W](l: Prism[W, Page]): Rule[W] =
      pmapF(l.reverseGet)(l.getOption)

    def pmapF[W](f: Page => W)(g: W => Option[Page]): Rule[W] =
      new Rule[W](
        parse(_) map (_.bimap(_ map f, f)),
        g(_) flatMap path,
        g(_) flatMap action map (_ map f))

    def widen[W >: Page](pf: PartialFunction[W, Page]): Rule[W] =
      widenF(pf.lift)

    def widenCT[W >: Page](implicit ct: ClassTag[Page]): Rule[W] =
      widenF(ct.unapply)

    def widenF[W >: Page](f: W => Option[Page]): Rule[W] =
      pmapF[W](p => p)(f)

    /**
     * Modify the path(es) generated and parsed by this rule.
     */
    def modPath(add: Path => Path, remove: Path => Option[Path]): Rule[Page] =
      new Rule(
        remove(_) flatMap parse,
        path(_) map add,
        action)

    /**
     * Add a prefix to the path(es) generated and parsed by this rule.
     */
    def prefixPath(prefix: String): Rule[Page] =
      modPath(
        p => Path(prefix + p.value),
        _ removePrefix prefix)

    /**
     * Add a prefix to the path(es) generated and parsed by this rule.
     *
     * Unlike [[prefixPath()]] when the suffix is non-empty, a slash is added between prefix and suffix.
     */
    def prefixPath_/(prefix: String): Rule[Page] = {
      val pre = Path(prefix)
      modPath(
        p => if (p.isEmpty) pre else pre / p,
        p => if (p.value == prefix) Some(Path.root) else p.removePrefix(prefix + "/"))
    }

    /**
     * Prevent this rule from functioning unless some condition holds.
     * When the condition doesn't hold, an alternative action may be performed.
     *
     * @param condUnmet Response when rule matches but condition doesn't hold.
     *                  If response is `None` it will be as if this rule doesn't exist and will likely end in the
     *                  route-not-found fallback behaviour.
     */
    def addCondition(cond: => Boolean)(condUnmet: Page => Option[Action[Page]]): Rule[Page] =
      new Rule[Page](parse, path,
        (if (cond) action else condUnmet)(_))

    /**
     * Specify behaviour when a `Page` doesn't have an associated `Path` or `Action`.
     */
    def fallback(fp: Page => Path, fa: Page => Action[Page]): Rules[Page] =
      new Rules[Page](parse, path | fp, action | fa)

    /**
     * When a `Page` doesn't have an associated  `Path` or `Action`, throw a runtime error.
     *
     * This is the trade-off for keeping the parsing and generation of known `Page`s in sync - compiler proof of
     * `Page` exhaustiveness is sacrificed.
     *
     * It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
     */
    def noFallback: Rules[Page] = {
      def force[A](desc: String): Page => A =
        p => sys error s"Unspecified $desc for page [$p]."
      fallback(force("path"), force("action"))
    }
  }

  object Rules {

    /**
     * Create routing rules all at once, with compiler proof that all `Page`s will have a `Path` and `Action`
     * associated.
     *
     * The trade-off here is that care will need to be taken to ensure that path-parsing aligns with paths
     * generated for pages. It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
     */
    def apply[Page](toPage: Path => Option[Parsed[Page]], fromPage: Page => (Path, Action[Page])) =
      new Rules[Page](toPage, fromPage(_)._1, fromPage(_)._2)
  }

  /**
   * Exhaustive routing rules. For all `Page`s there are `Path`s and `Action`s.
   */
  final case class Rules[Page](parse : Path => Option[Parsed[Page]],
                               path  : Page => Path,
                               action: Page => Action[Page]) {

    /**
     * Specify a catch-all response to unmatched/invalid routes.
     */
    def notFound(f: Path => Parsed[Page]): RouterConfig[Page] =
      RouterConfig.withDefaults(parse | f, path, action)
  }

  // ===================================================================================================================

  final class DynamicRouteB[Page, P <: Page, O](val f: (P => Action[Page]) => O) extends AnyVal {
    def ~>(g: P => Action[Page]): O = f(g)
  }

  final class StaticRouteB[Page, O](val f: (=> Action[Page]) => O) extends AnyVal {
    def ~>(a: => Action[Page]): O = f(a)
  }

  final class StaticRedirectB[Page, O](val f: (=> Redirect[Page]) => O) extends AnyVal {
    def ~>(a: => Redirect[Page]): O = f(a)
  }

  final class DynamicRedirectB[Page, A, O](val f: (A => Redirect[Page]) => O) extends AnyVal {
    def ~>(a: A => Redirect[Page]): O = f(a)
  }
}

// =====================================================================================================================
// =====================================================================================================================

object RouterConfigDsl {
  def apply[Page](implicit pageEq: Equal[Page] = Equal.equalA[Page]) =
    new BuildInterface(pageEq)

  class BuildInterface[Page](pageEq: Equal[Page]) {
    def use[A](f: RouterConfigDsl[Page] => A): A =
      f(new RouterConfigDsl(pageEq))

    def buildConfig(f: RouterConfigDsl[Page] => RouterConfig[Page]): RouterConfig[Page] =
      use(f)

    def buildRule(f: RouterConfigDsl[Page] => StaticDsl.Rule[Page]): StaticDsl.Rule[Page] =
      use(f)
  }
}

/**
 * DSL for creating [[RouterConfig]].
 *
 * Instead creating an instance of this yourself, use [[RouterConfigDsl.apply]].
 */
final class RouterConfigDsl[Page](pageEq: Equal[Page] = Equal.equalA[Page]) {
  import StaticDsl.{Rule => _, Rules => _, _}

  type Action   = japgolly.scalajs.react.extra.router2.Action[Page]
  type Renderer = japgolly.scalajs.react.extra.router2.Renderer[Page]
  type Redirect = japgolly.scalajs.react.extra.router2.Redirect[Page]
  type Parsed   = RouterConfig.Parsed[Page]

  // -------------------------------------------------------------------------------------------------------------------
  // Route DSL

  def root = Path.root
  val int  = new RouteB[Int] ("(-?\\d+)", 1, g => Some(g(0).toInt),  _.toString)
  val long = new RouteB[Long]("(-?\\d+)", 1, g => Some(g(0).toLong), _.toString)
  def string(regex: String) = new RouteB[String](s"($regex)", 1, g => Some(g(0)), identity)

  implicit def _ops_for_routeb_option[A](r: RouteB[Option[A]]) = new RouteBO(r)

  implicit def _auto_routeB_from_str(l: String) = RouteB.literal(l)
  implicit def _auto_routeB_from_path(p: Path) = RouteB.literal(p.value)
  implicit def _auto_route_from_routeB[A, R <% RouteB[A]](r: R) = r.route

  // -------------------------------------------------------------------------------------------------------------------
  // Action DSL

  implicit def _auto_someAction[A <: Action](a: A): Option[A] = Some(a)

  def render[A <% ReactElement](a: => A): Renderer =
    Renderer(_ => a)

  def renderR[A <% ReactElement](g: RouterCtl[Page] => A): Renderer =
    Renderer(g(_))

  def dynRender[P <: Page, A <% ReactElement](g: P => A): P => Renderer =
    p => Renderer(_ => g(p))

  def dynRenderR[P <: Page, A <% ReactElement](g: (P, RouterCtl[Page]) => A): P => Renderer =
    p => Renderer(r => g(p, r))

  def redirectToPage(page: Page)(implicit method: Redirect.Method): RedirectToPage[Page] =
    RedirectToPage[Page](page, method)

  def redirectToPath(path: Path)(implicit method: Redirect.Method): RedirectToPath[Page] =
    RedirectToPath[Page](path, method)

  def redirectToPath(path: String)(implicit method: Redirect.Method): RedirectToPath[Page] =
    redirectToPath(Path(path))

  // -------------------------------------------------------------------------------------------------------------------
  // Rule building DSL

  type Rule = StaticDsl.Rule[Page]
  type Rules = StaticDsl.Rules[Page]
  def Rule = StaticDsl.Rule
  def emptyRule: Rule = Rule.empty

  implicit def _auto_parsed_from_redirect(r: Redirect): Parsed = -\/(r)
  implicit def _auto_parsed_from_page    (p: Page)    : Parsed = \/-(p)

  implicit def _auto_parsedO_from_parsed [A <% Parsed](p: A)        : Option[Parsed] = Some(p)
  implicit def _auto_parsedO_from_parsedO[A <% Parsed](o: Option[A]): Option[Parsed] = o.map(a => a)

  implicit def _auto_notFound_from_parsed [A <% Parsed](a: A)        : Path => Parsed = _ => a
  implicit def _auto_notFound_from_parsedF[A <% Parsed](f: Path => A): Path => Parsed = f(_)

  implicit def _auto_routeParser_from_parsed  [A <% Parsed](a: A)                : Path => Option[Parsed] = _ => Some(a)
  implicit def _auto_routeParser_from_parsedF [A <% Parsed](f: Path => A)        : Path => Option[Parsed] = p => Some(f(p))
  implicit def _auto_routeParser_from_parsedO [A <% Parsed](o: Option[A])        : Path => Option[Parsed] = _ => o.map(a => a)
  implicit def _auto_routeParser_from_parsedFO[A <% Parsed](f: Path => Option[A]): Path => Option[Parsed] = f(_).map(a => a)

  // allows dynamicRoute ~~> X to not care if X is (Action) or (P => Action)
  implicit def _auto_pToAction_from_action(a: => Action): Page => Action = _ => a

  implicit def _auto_rules_from_rulesB(r: Rule): Rules = r.noFallback

  // Only really aids rewriteRuleR but safe anyway
  implicit def _auto_pattern_from_regex(r: Regex): Pattern = r.pattern

  def staticRoute(r: Route[Unit], page: Page)(implicit e: Equal[Page] = pageEq): StaticRouteB[Page, Rule] = {
    val dyn = dynamicRoute(r const page){ case p if e.equal(page, p) => p }
    new StaticRouteB(a => dyn ~> a)
  }

  def dynamicRoute[P <: Page](r: Route[P])(pf: PartialFunction[Page, P]): DynamicRouteB[Page, P, Rule] =
    dynamicRouteF(r)(pf.lift)

  def dynamicRouteF[P <: Page](r: Route[P])(op: Page => Option[P]): DynamicRouteB[Page, P, Rule] = {
    def onPage[A](f: P => A): Page => Option[A] =
      op(_) map f
    new DynamicRouteB(a => Rule(r.parse, onPage(r.pathFor), onPage(a)))
  }

  def dynamicRouteCT[P <: Page](r: Route[P])(implicit ct: ClassTag[P]): DynamicRouteB[Page, P, Rule] =
    dynamicRouteF(r)(ct.unapply)

  def staticRedirect(r: Route[Unit]): StaticRedirectB[Page, Rule] =
    new StaticRedirectB(a => rewritePath(r.parse(_) map (_ => a)))

  def dynamicRedirect[A](r: Route[A]): DynamicRedirectB[Page, A, Rule] =
    new DynamicRedirectB(f => rewritePath(r.parse(_) map f))

  def rewritePath(f: Path => Option[Redirect]): Rule =
    Rule parseOnly f

  def rewritePathR(r: Pattern, f: Matcher => Option[Redirect]): Rule =
    rewritePath { p =>
      val m = r.matcher(p.value)
      if (m.matches) f(m) else None
    }

  // -------------------------------------------------------------------------------------------------------------------
  // Utilities

  /**
   * A rule that uses a replace-state redirect to remove trailing slashes from route URLs.
   */
  def removeTrailingSlashes: Rule =
    rewritePathR("^(.*?)/+$".r, m => redirectToPath(m group 1)(Redirect.Replace))

  /**
   * A rule that uses a replace-state redirect to remove leading slashes from route URLs.
   */
  def removeLeadingSlashes: Rule =
    rewritePathR("^/+(.*)$".r, m => redirectToPath(m group 1)(Redirect.Replace))

  /**
   * A rule that uses a replace-state redirect to remove leading and trailing slashes from route URLs.
   */
  def trimSlashes: Rule = (
    rewritePathR("^/*(.*?)/+$".r, m => redirectToPath(m group 1)(Redirect.Replace))
    | removeLeadingSlashes)
}
