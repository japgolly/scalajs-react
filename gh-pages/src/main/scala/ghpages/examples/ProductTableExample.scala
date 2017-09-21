package ghpages.examples

import ghpages.GhPagesMacros
import japgolly.scalajs.react._, vdom.html_<^._
import ghpages.examples.util.SideBySide

object ProductTableExample {

  def content = SideBySide.Content(jsSource, source, main())

  lazy val main = addIntro(FilterableProductTable.withKey(_)(PRODUCTS),
    _(scalaPortOfPage("docs/thinking-in-react.html")))

  val jsSource =
    """
      |class ProductCategoryRow extends React.Component {
      |  render() {
      |    return (
      |      <tr>
      |        <td style={{fontWeight: 'bold'}}>{this.props.category}</td>
      |      </tr>
      |    );
      |  }
      |}
      |
      |class ProductRow extends React.Component {
      |  render() {
      |    var name = this.props.product.stocked ?
      |      this.props.product.name :
      |      <span style={{color: 'red'}}>
      |        {this.props.product.name}
      |      </span>;
      |    return (
      |      <tr>
      |        <td>{name}</td>
      |        <td>{this.props.product.price}</td>
      |      </tr>
      |    );
      |  }
      |}
      |
      |class ProductTable extends React.Component {
      |  render() {
      |    var rows = [];
      |    var lastCategory = null;
      |    this.props.products.forEach(function(product) {
      |      if (product.name.indexOf(this.props.filterText) === -1 || (!product.stocked && this.props.inStockOnly)) {
      |        return;
      |      }
      |      if (product.category !== lastCategory) {
      |        rows.push(<ProductCategoryRow category={product.category} key={product.category} />)
      |      }
      |      rows.push(<ProductRow product={product} key={product.name}/>);
      |      lastCategory = product.category;
      |    }.bind(this));
      |    return (
      |      <table>
      |        <thead>
      |          <tr>
      |            <th>Name</th>
      |            <th>Price</th>
      |          </tr>
      |        </thead>
      |        <tbody>
      |          {rows}
      |        </tbody>
      |      </table>
      |    );
      |  }
      |}
      |
      |class SearchBar extends React.Component {
      |  constructor(props) {
      |    super(props);
      |    this.handleChange = this.handleChange.bind(this);
      |  }
      |
      |  handleChange() {
      |    this.props.onUserInput(
      |      this.refs.filterTextInput.value,
      |      this.refs.inStockOnlyInput.checked
      |    );
      |  }
      |
      |  render() {
      |    return (
      |        <form>
      |            <input
      |                type="text"
      |                placeholder="Search..."
      |                value={this.props.filterText}
      |                ref="filterTextInput"
      |                onChange={this.handleChange}
      |            />
      |            <p>
      |                <input
      |                    type="checkbox"
      |                    value={this.props.inStockOnly}
      |                    ref="inStockOnlyInput"
      |                    onChange={this.handleChange}
      |                />
      |                Only show products in stock
      |            </p>
      |        </form>
      |    );
      |  }
      |}
      |
      |class FilterableProductTable extends React.Component {
      |  constructor(props) {
      |    super(props);
      |    this.state = {
      |      filterText: '',
      |      inStockOnly: false
      |    };
      |    this.handleUserInput = this.handleUserInput.bind(this);
      |  }
      |
      |  handleUserInput(filterText, inStockOnly) {
      |    this.setState({
      |      filterText: filterText,
      |      inStockOnly: inStockOnly
      |    });
      |  }
      |
      |  render() {
      |    return (
      |      <div>
      |          <SearchBar
      |              filterText={this.state.filterText}
      |              inStockOnly={this.state.inStockOnly}
      |              onUserInput={this.handleUserInput}
      |          />
      |          <ProductTable
      |              products={this.props.products}
      |              filterText={this.state.filterText}
      |              inStockOnly={this.state.inStockOnly}
      |          />
      |      </div>
      |    );
      |  }
      |}
      |
      |var PRODUCTS = [
      |  {category: 'Sporting Goods', price: '$49.99', stocked: true, name: 'Football'},
      |  {category: 'Sporting Goods', price: '$9.99', stocked: true, name: 'Baseball'},
      |  {category: 'Sporting Goods', price: '$29.99', stocked: false, name: 'Basketball'},
      |  {category: 'Electronics', price: '$99.99', stocked: true, name: 'iPod Touch'},
      |  {category: 'Electronics', price: '$399.99', stocked: false, name: 'iPhone 5'},
      |  {category: 'Electronics', price: '$199.99', stocked: true, name: 'Nexus 7'}
      |];
      |
      |ReactDOM.render(<FilterableProductTable product={PRODUCTS} />, document.body);
      | """.stripMargin

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  case class Product(name: String, price: Double, category: String, stocked: Boolean)

  case class State(filterText: String, inStockOnly: Boolean)

  class Backend($: BackendScope[_, State])  {
    def onTextChange(e: ReactEventFromInput) =
      e.extract(_.target.value)(value =>
        $.modState(_.copy(filterText = value)))

    def onCheckBox(e: ReactEvent) =
      $.modState(s => s.copy(inStockOnly = !s.inStockOnly))
  }

  val ProductCategoryRow = ScalaComponent.builder[String]("ProductCategoryRow")
    .render_P(category => <.tr(<.th(^.colSpan := 2, category)))
    .build

  val ProductRow = ScalaComponent.builder[Product]("ProductRow")
    .render_P(p =>
      <.tr(
        <.td(<.span(^.color.red.unless(p.stocked), p.name)),
        <.td(p.price))
    )
    .build

  def productFilter(s: State)(p: Product): Boolean =
    p.name.contains(s.filterText) &&
    (!s.inStockOnly || p.stocked)

  val ProductTable = ScalaComponent.builder[(List[Product], State)]("ProductTable")
    .render_P { case (products, state) =>
      val rows = products.filter(productFilter(state))
                 .groupBy(_.category).toList
                 .flatMap{ case (cat, ps) =>
                    ProductCategoryRow.withKey(cat)(cat) :: ps.map(p => ProductRow.withKey(p.name)(p))
                  }
      <.table(
        <.thead(
          <.tr(
            <.th("Name"),
            <.th("Price"))),
        <.tbody(
          rows.toVdomArray))
    }
    .build

  val SearchBar = ScalaComponent.builder[(State, Backend)]("SearchBar")
    .render_P { case (s, b) =>
      <.form(
        <.input.text(
          ^.placeholder := "Search Bar ...",
          ^.value       := s.filterText,
          ^.onChange   ==> b.onTextChange),
        <.p(
          <.input.checkbox(
            ^.onClick ==> b.onCheckBox),
          "Only show products in stock"))
    }
    .build

  val FilterableProductTable = ScalaComponent.builder[List[Product]]("FilterableProductTable")
    .initialState(State("", false))
    .backend(new Backend(_))
    .renderPS(($, p, s) =>
      <.div(
        SearchBar((s,$.backend)),
        ProductTable((p, s))
      )
    ).build

    val PRODUCTS = List(
      Product("FootBall", 49.99, "Sporting Goods", true),
      Product("Baseball", 9.99, "Sporting Goods", true),
      Product("basketball", 29.99, "Sporting Goods", false),
      Product("ipod touch", 99.99, "Electronics", true),
      Product("iphone 5", 499.99, "Electronics", true),
      Product("Nexus 7", 199.99, "Electronics", true))

  // EXAMPLE:END
}
