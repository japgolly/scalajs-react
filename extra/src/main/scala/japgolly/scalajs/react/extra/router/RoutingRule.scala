package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.extra.router.RouterConfig.Parsed
import japgolly.scalajs.react.util.DefaultEffects.Sync
import scala.reflect.ClassTag

/** A single routing rule. Intended to be composed with other [[RoutingRule]]s.
  * When all rules are composed, this is turned into a [[RoutingRule.WithFallback]] instance.
  *
  * @tparam Page The type of legal pages. Most commonly, a sealed trait that you've created, where all subclasses
  *              represent a page in your SPA.
  */
sealed trait RoutingRule[Page, Props] {

  /** Compose rules. */
  final def |(that: RoutingRule[Page, Props]): RoutingRule[Page, Props] =
    RoutingRule.Or(this, that)

  def xmap[A](f: Page => A)(g: A => Page): RoutingRule[A, Props]

  final def pmap[W](f: Page => W)(pf: PartialFunction[W, Page]): RoutingRule[W, Props] =
    pmapF(f)(pf.lift)

  final def pmapCT[W](f: Page => W)(implicit ct: ClassTag[Page]): RoutingRule[W, Props] =
    pmapF(f)(ct.unapply)

  def pmapF[W](f: Page => W)(g: W => Option[Page]): RoutingRule[W, Props]

  final def widen[W >: Page](pf: PartialFunction[W, Page]): RoutingRule[W, Props] =
    widenF(pf.lift)

  final def widenCT[W >: Page](implicit ct: ClassTag[Page]): RoutingRule[W, Props] =
    widenF(ct.unapply)

  final def widenF[W >: Page](f: W => Option[Page]): RoutingRule[W, Props] =
    pmapF[W](p => p)(f)

  /** See [[autoCorrect()]]. */
  final def autoCorrect: RoutingRule[Page, Props] =
    autoCorrect(SetRouteVia.HistoryReplace)

  /**
    * When a route matches a page, compare its [[Path]] to what the route would generate for the same page and if they
    * differ, redirect to the generated one.
    *
    * Example: If a route matches `/issue/dev-23` and returns a `Page("DEV", 23)` for which the generate path would be
    * `/issue/DEV-23`, this would automatically redirect `/issue/dev-23` to `/issue/DEV-23`, and process
    * `/issue/DEV-23` normally using its associated action.
    */
  final def autoCorrect(redirectVia: SetRouteVia): RoutingRule[Page, Props] =
    RoutingRule.AutoCorrect(this, redirectVia)

  /** Modify the path(es) generated and parsed by this rule.
    *
    * @param onCreate Modify paths when generating for a route.
    * @param onParse When parsing a path, transform and optionally reject it.
    */
  def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): RoutingRule[Page, Props]

  /** Add a prefix to the path(es) generated and parsed by this rule. */
  final def prefixPath(prefix: String): RoutingRule[Page, Props] =
    modPath(
      p => Path(prefix + p.value),
      _ removePrefix prefix)

  /** Add a prefix to the path(es) generated and parsed by this rule.
    *
    * Unlike [[prefixPath()]] when the suffix is non-empty, a slash is added between prefix and suffix.
    */
  final def prefixPath_/(prefix: String): RoutingRule[Page, Props] = {
    val pre = Path(prefix)
    modPath(
      p => if (p.isEmpty) pre else pre / p,
      p => if (p.value == prefix) Some(Path.root) else p.removePrefix(prefix + "/"))
  }

  /** Prevent this rule from functioning unless some condition holds.
    * When the condition doesn't hold, an optional fallback action may be performed.
    *
    * @param condition Sync[Unit] that requested page and returns true if the page should be rendered.
    * @param fallback  Response when rule matches but condition doesn't hold.
    *                  If response is `None` it will be as if this rule doesn't exist and will likely end in the
    *                  route-not-found fallback behaviour.
    */
  final def addConditionWithOptionalFallback(condition: Sync[Boolean], fallback: Page => Option[Action[Page, Props]]): RoutingRule[Page, Props] =
    RoutingRule.Conditional(condition, this, fallback)

  /** Prevent this rule from functioning unless some condition holds, passes in the page
    * requested as part of the context.
    * When the condition doesn't hold, an optional fallback action may be performed.
    *
    * @param condition Function that takes the requested page and returns true if the page should be rendered.
    * @param fallback  Response when rule matches but condition doesn't hold.
    *                  If response is `None` it will be as if this rule doesn't exist and will likely end in the
    *                  route-not-found fallback behaviour.
    */
  final def addConditionWithOptionalFallback(condition: Page => Sync[Boolean], fallback: Page => Option[Action[Page, Props]]): RoutingRule[Page, Props] =
    RoutingRule.ConditionalP(condition, this, fallback)

  /** Prevent this rule from functioning unless some condition holds.
    * When the condition doesn't hold, an optional fallback action may be performed.
    *
    * @param condition Sync[Unit] that requested page and returns true if the page should be rendered.
    * @param fallback  Response when rule matches but condition doesn't hold.
    *                  If response is `None` it will be as if this rule doesn't exist and will likely end in the
    *                  route-not-found fallback behaviour.
    */
  final def addConditionWithOptionalFallback(condition: Sync[Boolean], fallback: Option[Action[Page, Props]]): RoutingRule[Page, Props] =
    RoutingRule.Conditional(condition, this, (_: Page) => fallback)

  /** Prevent this rule from functioning unless some condition holds, passes in the page
    * requested as part of the context.
    * When the condition doesn't hold, an optional fallback action may be performed.
    *
    * @param condition Function that takes the requested page and returns true if the page should be rendered.
    * @param fallback  Response when rule matches but condition doesn't hold.
    *                  If response is `None` it will be as if this rule doesn't exist and will likely end in the
    *                  route-not-found fallback behaviour.
    */
  final def addConditionWithOptionalFallback(condition: Page => Sync[Boolean], fallback: Option[Action[Page, Props]]): RoutingRule[Page, Props] =
    addConditionWithOptionalFallback(condition, (_: Page) => fallback)

  /** Prevent this rule from functioning unless some condition holds.
    * When the condition doesn't hold, a fallback action is performed.
    *
    * @param condition Sync[Unit] that requested page and returns true if the page should be rendered.
    * @param fallback  Response when rule matches but condition doesn't hold.
    */
  final def addConditionWithFallback(condition: Sync[Boolean], fallback: Action[Page, Props]): RoutingRule[Page, Props] =
    addConditionWithOptionalFallback(condition, (_: Page) => Some(fallback))

  /** Prevent this rule from functioning unless some condition holds, passes in the page
    * requested as part of the context.
    * When the condition doesn't hold, a fallback action is performed.
    *
    * @param condition Function that takes the requested page and returns true if the page should be rendered.
    * @param fallback  Response when rule matches but condition doesn't hold.
    */
  final def addConditionWithFallback(condition: Page => Sync[Boolean], fallback: Action[Page, Props]): RoutingRule[Page, Props] =
    addConditionWithOptionalFallback(condition, (_: Page) => Some(fallback))

  /** Prevent this rule from functioning unless some condition holds.
    *
    * @param condition Sync[Unit] that requested page and returns true if the page should be rendered.
    */
  final def addCondition(condition: Sync[Boolean]): RoutingRule[Page, Props] =
    addConditionWithOptionalFallback(condition, (_: Page) => None)

  /** Prevent this rule from functioning unless some condition holds, passes in the page
    * requested as part of the context.
    *
    * @param condition Function that takes the requested page and returns true if the page should be rendered.
    */
  final def addCondition(condition: Page => Sync[Boolean]): RoutingRule[Page, Props] =
    addConditionWithOptionalFallback(condition, (_: Page) => None)

  /** Specify behaviour when a `Page` doesn't have an associated `Path` or `Action`. */
  final def fallback(fallbackPath  : Page => Path,
                     fallbackAction: (Path, Page) => Action[Page, Props]): RoutingRule.WithFallback[Page, Props] =
    RoutingRule.WithFallback(this, fallbackPath, fallbackAction)

  /** When a `Page` doesn't have an associated  `Path` or `Action`, throw a runtime error.
    *
    * This is the trade-off for keeping the parsing and generation of known `Page`s in sync - compiler proof of
    * `Page` exhaustiveness is sacrificed.
    *
    * It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
    */
  final def noFallback: RoutingRule.WithFallback[Page, Props] =
    fallback(
      page         => sys error s"Unspecified path for page $page.",
      (path, page) => sys error s"Unspecified action for page $page at $path.")
}

object RoutingRule {

  // ===================================================================================================================

  /** @param parse  Attempt to parse a given path.
    * @param path   Attempt to determine the path for some page.
    * @param action Attempt to determine the action when a route resolves to some page.
    */
  final case class Atom[Page, Props](parse : Path         => Option[Parsed[Page]],
                                     path  : Page         => Option[Path],
                                     action: (Path, Page) => Option[Action[Page, Props]]) extends RoutingRule[Page, Props] {

    override def xmap[A](f: Page => A)(g: A => Page): RoutingRule[A, Props] =
      Atom[A, Props](
        p => parse(p).map(_.bimap(_ map f, f)),
        path compose g,
        (u, p) => action(u, g(p)).map(_ map f))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): RoutingRule[W, Props] =
      Atom[W, Props](
        parse(_).map(_.bimap(_ map f, f)),
        g(_).flatMap(path),
        (path, w) => g(w).flatMap(action(path, _)).map(_ map f))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): RoutingRule[Page, Props] =
      Atom(
        onParse(_) flatMap parse,
        path(_) map onCreate,
        action)
  }

  // ===================================================================================================================

  final case class Conditional[Page, Props](condition : Sync[Boolean],
                                            underlying: RoutingRule[Page, Props],
                                            otherwise : Page => Option[Action[Page, Props]]) extends RoutingRule[Page, Props] {

    override def xmap[A](f: Page => A)(g: A => Page): RoutingRule[A, Props] =
      Conditional[A, Props](
        condition,
        underlying.xmap(f)(g),
        a => otherwise(g(a)).map(_.map(f)))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): RoutingRule[W, Props] =
      Conditional[W, Props](
        condition,
        underlying.pmapF(f)(g),
        g(_).flatMap(otherwise(_).map(_.map(f))))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): RoutingRule[Page, Props] =
      copy(underlying = underlying.modPath(onCreate, onParse))
  }

  // ===================================================================================================================

  final case class ConditionalP[Page, Props](condition : Page => Sync[Boolean],
                                             underlying: RoutingRule[Page, Props],
                                             otherwise : Page => Option[Action[Page, Props]]) extends RoutingRule[Page, Props] {

    override def xmap[A](f: Page => A)(g: A => Page): RoutingRule[A, Props] =
      ConditionalP[A, Props](
        condition compose g,
        underlying.xmap(f)(g),
        a => otherwise(g(a)).map(_.map(f)))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): RoutingRule[W, Props] =
      ConditionalP[W, Props](
        g(_).fold(Sync.pure(false))(condition),
        underlying.pmapF(f)(g),
        g(_).flatMap(otherwise(_).map(_.map(f))))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): RoutingRule[Page, Props] =
      copy(underlying = underlying.modPath(onCreate, onParse))
  }

  // ===================================================================================================================

  final case class Or[Page, Props](lhs: RoutingRule[Page, Props], rhs: RoutingRule[Page, Props]) extends RoutingRule[Page, Props] {
    private def mod[A](f: RoutingRule[Page, Props] => RoutingRule[A, Props]): RoutingRule[A, Props] =
      Or(f(lhs), f(rhs))

    override def xmap[A](f: Page => A)(g: A => Page): RoutingRule[A, Props] =
      mod(_.xmap(f)(g))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): RoutingRule[W, Props] =
      mod(_.pmapF(f)(g))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): RoutingRule[Page, Props] =
      mod(_.modPath(onCreate, onParse))
  }

  // ===================================================================================================================

  final case class AutoCorrect[Page, Props](underlying : RoutingRule[Page, Props],
                                            redirectVia: SetRouteVia) extends RoutingRule[Page, Props] {

    override def xmap[A](f: Page => A)(g: A => Page): RoutingRule[A, Props] =
      copy(underlying.xmap(f)(g))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): RoutingRule[W, Props] =
      copy(underlying.pmapF(f)(g))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): RoutingRule[Page, Props] =
      copy(underlying.modPath(onCreate, onParse))
  }

  def parseOnly[Page, C](parse: Path => Option[Parsed[Page]]) =
    Atom[Page, C](parse, _ => None, (_, _) => None)

  def empty[P, C]: RoutingRule[P, C] =
    Atom(_ => None, _ => None, (_, _) => None)

  // ===================================================================================================================

  /** Exhaustive routing rules. For all `Page`s there are `Path`s and `Action`s. */
  final case class WithFallback[Page, Props](rule          : RoutingRule[Page, Props],
                                             fallbackPath  : Page => Path,
                                             fallbackAction: (Path, Page) => Action[Page, Props]) {

    /** Specify a catch-all response to unmatched/invalid routes. */
    def notFound(whenNotFound: Path => Parsed[Page]): RouterWithPropsConfig[Page, Props] =
      notFoundDynamic(whenNotFound.andThen(Sync.pure(_)))

    /** Specify a catch-all response to unmatched/invalid routes. */
    def notFoundDynamic(whenNotFound: Path => Sync[Parsed[Page]]): RouterWithPropsConfig[Page, Props] = {
      val rules = RoutingRules.fromRule(rule, fallbackPath, fallbackAction, whenNotFound)
      RouterConfig.withDefaults(rules)
    }
  }

}
