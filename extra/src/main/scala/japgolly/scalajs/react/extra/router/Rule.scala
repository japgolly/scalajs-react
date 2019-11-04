package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.CallbackTo
import japgolly.scalajs.react.extra.router.RouterConfig.Parsed
import scala.reflect.ClassTag

/** A single routing rule. Intended to be composed with other [[Rule]]s.
  * When all rules are composed, this is turned into a [[Rule.WithFallback]] instance.
  *
  * @tparam Page  The type of legal pages.
  */
sealed trait Rule[Page] {

  /** Compose rules. */
  final def |(that: Rule[Page]): Rule[Page] =
    Rule.Or(this, that)

  def xmap[A](f: Page => A)(g: A => Page): Rule[A]

  final def pmap[W](f: Page => W)(pf: PartialFunction[W, Page]): Rule[W] =
    pmapF(f)(pf.lift)

  final def pmapCT[W](f: Page => W)(implicit ct: ClassTag[Page]): Rule[W] =
    pmapF(f)(ct.unapply)

  def pmapF[W](f: Page => W)(g: W => Option[Page]): Rule[W]

  final def widen[W >: Page](pf: PartialFunction[W, Page]): Rule[W] =
    widenF(pf.lift)

  final def widenCT[W >: Page](implicit ct: ClassTag[Page]): Rule[W] =
    widenF(ct.unapply)

  final def widenF[W >: Page](f: W => Option[Page]): Rule[W] =
    pmapF[W](p => p)(f)

  /** See [[autoCorrect()]]. */
  final def autoCorrect: Rule[Page] =
    autoCorrect(SetRouteVia.HistoryReplace)

  /**
    * When a route matches a page, compare its [[Path]] to what the route would generate for the same page and if they
    * differ, redirect to the generated one.
    *
    * Example: If a route matches `/issue/dev-23` and returns a `Page("DEV", 23)` for which the generate path would be
    * `/issue/DEV-23`, this would automatically redirect `/issue/dev-23` to `/issue/DEV-23`, and process
    * `/issue/DEV-23` normally using its associated action.
    */
  final def autoCorrect(redirectVia: SetRouteVia): Rule[Page] =
    Rule.AutoCorrect(this, redirectVia)

  /** Modify the path(es) generated and parsed by this rule.
    *
    * @param onCreate Modify paths when generating for a route.
    * @param onParse When parsing a path, transform and optionally reject it.
    */
  def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): Rule[Page]

  /** Add a prefix to the path(es) generated and parsed by this rule. */
  final def prefixPath(prefix: String): Rule[Page] =
    modPath(
      p => Path(prefix + p.value),
      _ removePrefix prefix)

  /** Add a prefix to the path(es) generated and parsed by this rule.
    *
    * Unlike [[prefixPath()]] when the suffix is non-empty, a slash is added between prefix and suffix.
    */
  final def prefixPath_/(prefix: String): Rule[Page] = {
    val pre = Path(prefix)
    modPath(
      p => if (p.isEmpty) pre else pre / p,
      p => if (p.value == prefix) Some(Path.root) else p.removePrefix(prefix + "/"))
  }

  /** Prevent this rule from functioning unless some condition holds.
    * When the condition doesn't hold, an alternative action may be performed.
    *
    * @param condUnmet Response when rule matches but condition doesn't hold.
    *                  If response is `None` it will be as if this rule doesn't exist and will likely end in the
    *                  route-not-found fallback behaviour.
    */
  final def addCondition(cond: CallbackTo[Boolean])(condUnmet: Page => Option[Action[Page]]): Rule[Page] =
    addCondition(_ => cond)(condUnmet)

  /** Prevent this rule from functioning unless some condition holds, passes in the page
    * requested as part of the context.
    * When the condition doesn't hold, an alternative action may be performed.
    *
    * @param cond Function that takes the requested page and returns true if the page should be rendered.
    * @param condUnmet Response when rule matches but condition doesn't hold.
    *                  If response is `None` it will be as if this rule doesn't exist and will likely end in the
    *                  route-not-found fallback behaviour.
    */
  final def addCondition(cond: Page => CallbackTo[Boolean])(condUnmet: Page => Option[Action[Page]]): Rule[Page] =
    Rule.Conditional(cond, this, condUnmet)

  /** Specify behaviour when a `Page` doesn't have an associated `Path` or `Action`. */
  final def fallback(fallbackPath  : Page => Path,
                     fallbackAction: (Path, Page) => Action[Page]): Rule.WithFallback[Page] =
    Rule.WithFallback(this, fallbackPath, fallbackAction)

  /** When a `Page` doesn't have an associated  `Path` or `Action`, throw a runtime error.
    *
    * This is the trade-off for keeping the parsing and generation of known `Page`s in sync - compiler proof of
    * `Page` exhaustiveness is sacrificed.
    *
    * It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
    */
  final def noFallback: Rule.WithFallback[Page] =
    fallback(
      page         => sys error s"Unspecified path for page $page.",
      (path, page) => sys error s"Unspecified action for page $page at $path.")
}

object Rule {

  /** @param parse  Attempt to parse a given path.
    * @param path   Attempt to determine the path for some page.
    * @param action Attempt to determine the action when a route resolves to some page.
    */
  final case class Atom[Page](parse        : Path         => Option[Parsed[Page]],
                              path         : Page         => Option[Path],
                              action       : (Path, Page) => Option[Action[Page]]) extends Rule[Page] {

    override def xmap[A](f: Page => A)(g: A => Page): Rule[A] =
      Atom[A](
        p => parse(p).map(_.bimap(_ map f, f)),
        path compose g,
        (u, p) => action(u, g(p)).map(_ map f))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): Rule[W] =
      Atom[W](
        parse(_).map(_.bimap(_ map f, f)),
        g(_).flatMap(path),
        (path, w) => g(w).flatMap(action(path, _)).map(_ map f))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): Rule[Page] =
      Atom(
        onParse(_) flatMap parse,
        path(_) map onCreate,
        action)
  }

  final case class Conditional[Page](condition : Page => CallbackTo[Boolean],
                                     underlying: Rule[Page],
                                     otherwise : Page => Option[Action[Page]]) extends Rule[Page] {

    override def xmap[A](f: Page => A)(g: A => Page): Rule[A] =
      Conditional[A](
        condition compose g,
        underlying.xmap(f)(g),
        a => otherwise(g(a)).map(_.map(f)))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): Rule[W] =
      Conditional[W](
        g(_).fold(CallbackTo.pure(false))(condition),
        underlying.pmapF(f)(g),
        g(_).flatMap(otherwise(_).map(_.map(f))))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): Rule[Page] =
      copy(underlying = underlying.modPath(onCreate, onParse))
  }

  final case class Or[Page](lhs: Rule[Page], rhs: Rule[Page]) extends Rule[Page] {
    private def mod[A](f: Rule[Page] => Rule[A]): Rule[A] =
      Or(f(lhs), f(rhs))

    override def xmap[A](f: Page => A)(g: A => Page): Rule[A] =
      mod(_.xmap(f)(g))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): Rule[W] =
      mod(_.pmapF(f)(g))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): Rule[Page] =
      mod(_.modPath(onCreate, onParse))
  }

  final case class AutoCorrect[Page](underlying: Rule[Page],
                                     redirectVia: SetRouteVia) extends Rule[Page] {

    override def xmap[A](f: Page => A)(g: A => Page): Rule[A] =
      copy(underlying.xmap(f)(g))

    override def pmapF[W](f: Page => W)(g: W => Option[Page]): Rule[W] =
      copy(underlying.pmapF(f)(g))

    override def modPath(onCreate: Path => Path, onParse: Path => Option[Path]): Rule[Page] =
      copy(underlying.modPath(onCreate, onParse))
  }

  def parseOnly[Page](parse: Path => Option[Parsed[Page]]) =
    Atom[Page](parse, _ => None, (_, _) => None)

  def empty[P]: Rule[P] =
    Atom(_ => None, _ => None, (_, _) => None)

  /** Exhaustive routing rules. For all `Page`s there are `Path`s and `Action`s. */
  final case class WithFallback[Page](rule          : Rule[Page],
                                      fallbackPath  : Page => Path,
                                      fallbackAction: (Path, Page) => Action[Page]) {

    /**
      * Specify a catch-all response to unmatched/invalid routes.
      */
    def notFound(f: Path => Parsed[Page]): RouterConfig[Page] = {
      val rules = RoutingRules.fromRule(rule, fallbackPath, fallbackAction, f)
      RouterConfig.withDefaults(rules)
    }
  }

  //    /** Create routing rules all at once, with compiler proof that all `Page`s will have a `Path` and `Action`
  //      * associated.
  //      *
  //      * The trade-off here is that care will need to be taken to ensure that path-parsing aligns with paths
  //      * generated for pages. It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
  //      */
  //    def apply[Page](toPage: Path => Option[Parsed[Page]], fromPage: Page => (Path, Action[Page])) =
  //      new Rules[Page](toPage, fromPage(_)._1, (_, p) => fromPage(p)._2)

}
