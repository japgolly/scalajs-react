package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.CallbackTo
import japgolly.scalajs.react.extra.router.RouterConfig.Parsed
import scala.reflect.ClassTag

/** A single routing rule. Intended to be composed with other [[RoutingRule]]s.
  * When all rules are composed, this is turned into a [[RoutingRule.WithFallback]] instance.
  *
  * @tparam Page The type of legal pages. Most commonly, a sealed trait that you've created, where all subclasses
  *              represent a page in your SPA.
  */
sealed trait RoutingRule[Page] {

  /** Compose rules. */
  final def |(that: RoutingRule[Page]): RoutingRule[Page] =
    RoutingRule.Or(this, that)

  def xmap[A](f: Page => A)(g: A => Page): RoutingRule[A]

  final def pmap[W](f: Page => W)(pf: PartialFunction[W, Page]): RoutingRule[W] =
    pmapF(f)(pf.lift)

  final def pmapCT[W](f: Page => W)(implicit ct: ClassTag[Page]): RoutingRule[W] =
    pmapF(f)(ct.unapply)

  def pmapF[W](f: Page => W)(g: W => Option[Page]): RoutingRule[W]

  final def widen[W >: Page](pf: PartialFunction[W, Page]): RoutingRule[W] =
    widenF(pf.lift)

  final def widenCT[W >: Page](implicit ct: ClassTag[Page]): RoutingRule[W] =
    widenF(ct.unapply)

  final def widenF[W >: Page](f: W => Option[Page]): RoutingRule[W] =
    pmapF[W](p => p)(f)

  /** See [[autoCorrect()]]. */
  final def autoCorrect: RoutingRule[Page] =
    autoCorrect(SetRouteVia.HistoryReplace)

  /**
    * When a route matches a page, compare its [[Path]] to what the route would generate for the same page and if they
    * differ, redirect to the generated one.
    *
    * Example: If a route matches `/issue/dev-23` and returns a `Page("DEV", 23)` for which the generate path would be
    * `/issue/DEV-23`, this would automatically redirect `/issue/dev-23` to `/issue/DEV-23`, and process
    * `/issue/DEV-23` normally using its associated action.
    */
  final def autoCorrect(redirectVia: SetRouteVia): RoutingRule[Page] =
    RoutingRule.AutoCorrect(this, redirectVia)

  /** Modify the path(es) generated and parsed by this rule.
    *
    * @param onCreate Modify paths when generating for a route.
    * @param onParse When parsing a path, transform and optionally reject it.
    */
  def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): RoutingRule[Page]

  /** Add a prefix to the path(es) generated and parsed by this rule. */
  final def prefixPath(prefix: String): RoutingRule[Page] =
    modPath(
      p => Path(prefix + p.value),
      _ removePrefix prefix)

  /** Add a prefix to the path(es) generated and parsed by this rule.
    *
    * Unlike [[prefixPath()]] when the suffix is non-empty, a slash is added between prefix and suffix.
    */
  final def prefixPath_/(prefix: String): RoutingRule[Page] = {
    val pre = Path(prefix)
    modPath(
      p => if (p.isEmpty) pre else pre / p,
      p => if (p.value == prefix) Some(Path.root) else p.removePrefix(prefix + "/"))
  }

  /** Prevent this rule from functioning unless some condition holds.
    * When the condition doesn't hold, an optional fallback action may be performed.
    *
    * @param condition Callback that requested page and returns true if the page should be rendered.
    * @param fallback  Response when rule matches but condition doesn't hold.
    *                  If response is `None` it will be as if this rule doesn't exist and will likely end in the
    *                  route-not-found fallback behaviour.
    */
  final def addConditionWithOptionalFallback(condition: CallbackTo[Boolean], fallback: Page => Option[Action[Page]]): RoutingRule[Page] =
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
  final def addConditionWithOptionalFallback(condition: Page => CallbackTo[Boolean], fallback: Page => Option[Action[Page]]): RoutingRule[Page] =
    RoutingRule.ConditionalP(condition, this, fallback)

  /** Prevent this rule from functioning unless some condition holds.
    * When the condition doesn't hold, an optional fallback action may be performed.
    *
    * @param condition Callback that requested page and returns true if the page should be rendered.
    * @param fallback  Response when rule matches but condition doesn't hold.
    *                  If response is `None` it will be as if this rule doesn't exist and will likely end in the
    *                  route-not-found fallback behaviour.
    */
  final def addConditionWithOptionalFallback(condition: CallbackTo[Boolean], fallback: Option[Action[Page]]): RoutingRule[Page] =
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
  final def addConditionWithOptionalFallback(condition: Page => CallbackTo[Boolean], fallback: Option[Action[Page]]): RoutingRule[Page] =
    addConditionWithOptionalFallback(condition, (_: Page) => fallback)

  /** Prevent this rule from functioning unless some condition holds.
    * When the condition doesn't hold, a fallback action is performed.
    *
    * @param condition Callback that requested page and returns true if the page should be rendered.
    * @param fallback  Response when rule matches but condition doesn't hold.
    */
  final def addConditionWithFallback(condition: CallbackTo[Boolean], fallback: Action[Page]): RoutingRule[Page] =
    addConditionWithOptionalFallback(condition, (_: Page) => Some(fallback))

  /** Prevent this rule from functioning unless some condition holds, passes in the page
    * requested as part of the context.
    * When the condition doesn't hold, a fallback action is performed.
    *
    * @param condition Function that takes the requested page and returns true if the page should be rendered.
    * @param fallback  Response when rule matches but condition doesn't hold.
    */
  final def addConditionWithFallback(condition: Page => CallbackTo[Boolean], fallback: Action[Page]): RoutingRule[Page] =
    addConditionWithOptionalFallback(condition, (_: Page) => Some(fallback))

  /** Prevent this rule from functioning unless some condition holds.
    *
    * @param condition Callback that requested page and returns true if the page should be rendered.
    */
  final def addCondition(condition: CallbackTo[Boolean]): RoutingRule[Page] =
    addConditionWithOptionalFallback(condition, (_: Page) => None)

  /** Prevent this rule from functioning unless some condition holds, passes in the page
    * requested as part of the context.
    *
    * @param condition Function that takes the requested page and returns true if the page should be rendered.
    */
  final def addCondition(condition: Page => CallbackTo[Boolean]): RoutingRule[Page] =
    addConditionWithOptionalFallback(condition, (_: Page) => None)

  /** Specify behaviour when a `Page` doesn't have an associated `Path` or `Action`. */
  final def fallback(fallbackPath  : Page => Path,
                     fallbackAction: (Path, Page) => Action[Page]): RoutingRule.WithFallback[Page] =
    RoutingRule.WithFallback(this, fallbackPath, fallbackAction)

  /** When a `Page` doesn't have an associated  `Path` or `Action`, throw a runtime error.
    *
    * This is the trade-off for keeping the parsing and generation of known `Page`s in sync - compiler proof of
    * `Page` exhaustiveness is sacrificed.
    *
    * It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
    */
  final def noFallback: RoutingRule.WithFallback[Page] =
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
  final case class Atom[Page](parse : Path         => Option[Parsed[Page]],
                              path  : Page         => Option[Path],
                              action: (Path, Page) => Option[Action[Page]]) extends RoutingRule[Page] {

    override def xmap[A](f: Page => A)(g: A => Page): RoutingRule[A] =
      Atom[A](
        p => parse(p).map(_.bimap(_ map f, f)),
        path compose g,
        (u, p) => action(u, g(p)).map(_ map f))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): RoutingRule[W] =
      Atom[W](
        parse(_).map(_.bimap(_ map f, f)),
        g(_).flatMap(path),
        (path, w) => g(w).flatMap(action(path, _)).map(_ map f))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): RoutingRule[Page] =
      Atom(
        onParse(_) flatMap parse,
        path(_) map onCreate,
        action)
  }

  // ===================================================================================================================

  final case class Conditional[Page](condition : CallbackTo[Boolean],
                                     underlying: RoutingRule[Page],
                                     otherwise : Page => Option[Action[Page]]) extends RoutingRule[Page] {

    override def xmap[A](f: Page => A)(g: A => Page): RoutingRule[A] =
      Conditional[A](
        condition,
        underlying.xmap(f)(g),
        a => otherwise(g(a)).map(_.map(f)))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): RoutingRule[W] =
      Conditional[W](
        condition,
        underlying.pmapF(f)(g),
        g(_).flatMap(otherwise(_).map(_.map(f))))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): RoutingRule[Page] =
      copy(underlying = underlying.modPath(onCreate, onParse))
  }

  // ===================================================================================================================

  final case class ConditionalP[Page](condition : Page => CallbackTo[Boolean],
                                      underlying: RoutingRule[Page],
                                      otherwise : Page => Option[Action[Page]]) extends RoutingRule[Page] {

    override def xmap[A](f: Page => A)(g: A => Page): RoutingRule[A] =
      ConditionalP[A](
        condition compose g,
        underlying.xmap(f)(g),
        a => otherwise(g(a)).map(_.map(f)))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): RoutingRule[W] =
      ConditionalP[W](
        g(_).fold(CallbackTo.pure(false))(condition),
        underlying.pmapF(f)(g),
        g(_).flatMap(otherwise(_).map(_.map(f))))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): RoutingRule[Page] =
      copy(underlying = underlying.modPath(onCreate, onParse))
  }

  // ===================================================================================================================

  final case class Or[Page](lhs: RoutingRule[Page], rhs: RoutingRule[Page]) extends RoutingRule[Page] {
    private def mod[A](f: RoutingRule[Page] => RoutingRule[A]): RoutingRule[A] =
      Or(f(lhs), f(rhs))

    override def xmap[A](f: Page => A)(g: A => Page): RoutingRule[A] =
      mod(_.xmap(f)(g))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): RoutingRule[W] =
      mod(_.pmapF(f)(g))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): RoutingRule[Page] =
      mod(_.modPath(onCreate, onParse))
  }

  // ===================================================================================================================

  final case class AutoCorrect[Page](underlying : RoutingRule[Page],
                                     redirectVia: SetRouteVia) extends RoutingRule[Page] {

    override def xmap[A](f: Page => A)(g: A => Page): RoutingRule[A] =
      copy(underlying.xmap(f)(g))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): RoutingRule[W] =
      copy(underlying.pmapF(f)(g))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): RoutingRule[Page] =
      copy(underlying.modPath(onCreate, onParse))
  }

  def parseOnly[Page](parse: Path => Option[Parsed[Page]]) =
    Atom[Page](parse, _ => None, (_, _) => None)

  def empty[P]: RoutingRule[P] =
    Atom(_ => None, _ => None, (_, _) => None)

  // ===================================================================================================================

  /** Exhaustive routing rules. For all `Page`s there are `Path`s and `Action`s. */
  final case class WithFallback[Page](rule          : RoutingRule[Page],
                                      fallbackPath  : Page => Path,
                                      fallbackAction: (Path, Page) => Action[Page]) {

    /** Specify a catch-all response to unmatched/invalid routes. */
    def notFound(whenNotFound: Path => Parsed[Page]): RouterConfig[Page] =
      notFoundDynamic(whenNotFound.andThen(CallbackTo.pure))

    /** Specify a catch-all response to unmatched/invalid routes. */
    def notFoundDynamic(whenNotFound: Path => CallbackTo[Parsed[Page]]): RouterConfig[Page] = {
      val rules = RoutingRules.fromRule(rule, fallbackPath, fallbackAction, whenNotFound)
      RouterConfig.withDefaults(rules)
    }
  }

}
