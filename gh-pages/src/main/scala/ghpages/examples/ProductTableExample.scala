package ghpages.examples

import ghpages.GhPagesMacros
import japgolly.scalajs.react._, vdom.html_<^._
import ghpages.examples.util.SideBySide

object ProductTableExample {

  def content = SideBySide.Content(jsSource, source, main())

  lazy val main = addIntro(FilterableProductTable withProps PRODUCTS,
    _(scalaPortOfPage("docs/thinking-in-react.html")))

  val jsSource =
    """
      |var ProductCategoryRow = React.createClass({
      |    render: function() {
      |        return (<tr><th colSpan="2">{this.props.category}</th></tr>);
      |    }
      |});
      |
      |var ProductRow = React.createClass({
      |    render: function() {
      |        var name = this.props.product.stocked ?
      |            this.props.product.name :
      |            <span style={{color: 'red'}}>
      |                {this.props.product.name}
      |            </span>;
      |        return (
      |            <tr>
      |                <td>{name}</td>
      |                <td>{this.props.product.price}</td>
      |            </tr>
      |        );
      |    }
      |});
      |
      |var ProductTable = React.createClass({
      |    render: function() {
      |        console.log(this.props);
      |        var rows = [];
      |        var lastCategory = null;
      |        this.props.products.forEach(function(product) {
      |            if (product.name.indexOf(this.props.filterText) === -1 || (!product.stocked && this.props.inStockOnly)) {
      |                return;
      |            }
      |            if (product.category !== lastCategory) {
      |                rows.push(<ProductCategoryRow category={product.category} key={product.category} />);
      |            }
      |            rows.push(<ProductRow product={product} key={product.name} />);
      |            lastCategory = product.category;
      |        }.bind(this));
      |        return (
      |            <table>
      |                <thead>
      |                    <tr>
      |                        <th>Name</th>
      |                        <th>Price</th>
      |                    </tr>
      |                </thead>
      |                <tbody>{rows}</tbody>
      |            </table>
      |        );
      |    }
      |});
      |
      |var SearchBar = React.createClass({
      |    handleChange: function() {
      |        this.props.onUserInput(
      |            this.refs.filterTextInput.value,
      |            this.refs.inStockOnlyInput.checked
      |        );
      |    },
      |    render: function() {
      |        return (
      |            <form>
      |                <input
      |                    type="text"
      |                    placeholder="Search..."
      |                    value={this.props.filterText}
      |                    ref="filterTextInput"
      |                    onChange={this.handleChange}
      |                />
      |                <p>
      |                    <input
      |                        type="checkbox"
      |                        value={this.props.inStockOnly}
      |                        ref="inStockOnlyInput"
      |                        onChange={this.handleChange}
      |                    />
      |                    Only show products in stock
      |                </p>
      |            </form>
      |        );
      |    }
      |});
      |
      |var FilterableProductTable = React.createClass({
      |    getInitialState: function() {
      |        return {
      |            filterText: '',
      |            inStockOnly: false
      |        };
      |    },
      |
      |    handleUserInput: function(filterText, inStockOnly) {
      |        this.setState({
      |            filterText: filterText,
      |            inStockOnly: inStockOnly
      |        });
      |    },
      |
      |    render: function() {
      |        return (
      |            <div>
      |                <SearchBar
      |                    filterText={this.state.filterText}
      |                    inStockOnly={this.state.inStockOnly}
      |                    onUserInput={this.handleUserInput}
      |                />
      |                <ProductTable
      |                    products={this.props.products}
      |                    filterText={this.state.filterText}
      |                    inStockOnly={this.state.inStockOnly}
      |                />
      |            </div>
      |        );
      |    }
      |});
      |
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
      |ReactDOM.render(<FilterableProductTable products={PRODUCTS} />, document.body);
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

  val ProductCategoryRow = ScalaComponent.build[String]("ProductCategoryRow")
    .render_P(category => <.tr(<.th(^.colSpan := 2, category)))
    .build

  val ProductRow = ScalaComponent.build[Product]("ProductRow")
    .render_P(p =>
      <.tr(
        <.td(<.span(!p.stocked ?= ^.color.red, p.name)),
        <.td(p.price))
    )
    .build

  def productFilter(s: State)(p: Product): Boolean =
    p.name.contains(s.filterText) &&
    (!s.inStockOnly || p.stocked)

  val ProductTable = ScalaComponent.build[(List[Product], State)]("ProductTable")
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
          rows))
    }
    .build

  val SearchBar = ScalaComponent.build[(State, Backend)]("SearchBar")
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

  val FilterableProductTable = ScalaComponent.build[List[Product]]("FilterableProductTable")
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
