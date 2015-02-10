package ghpages.examples

import japgolly.scalajs.react._, vdom.prefix_<^._
import ghpages.examples.util.SideBySide

/** Scala version of example on http://facebook.github.io/react/docs/thinking-in-react.html */
object ProductTableExample {

  def content = SideBySide.Content(jsSource, source, FilterableProductTable(products))

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
      |            this.refs.filterTextInput.getDOMNode().value,
      |            this.refs.inStockOnlyInput.getDOMNode().checked
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
      |React.render(<FilterableProductTable products={PRODUCTS} />, document.body);
      | """.stripMargin

  val source =
    """
      |case class Product(name: String, price: Double, category: String, stocked: Boolean)
      |
      |case class State(filterText: String, inStockOnly: Boolean)
      |
      |class Backend($: BackendScope[_, State])  {
      |  def onTextChange(e: ReactEventI) =
      |    $.modState(_.copy(filterText = e.target.value))
      |  def onCheckBox(e: ReactEvent) =
      |    $.modState(s => s.copy(inStockOnly = !s.inStockOnly))
      |}
      |
      |val ProductCategoryRow = ReactComponentB[String]("ProductCategoryRow")
      |  .render(category => <.tr(<.th(^.colSpan := 2, category)))
      |  .build
      |
      |val ProductRow = ReactComponentB[Product]("ProductRow")
      |  .render(P =>
      |    <.tr(
      |      <.td(<.span(!P.stocked ?= ^.color.red, P.name)),
      |      <.td(P.price))
      |  )
      |  .build
      |
      |def productFilter(s: State)(p: Product): Boolean =
      |  p.name.contains(s.filterText) &&
      |  (!s.inStockOnly || p.stocked)
      |
      |val ProductTable = ReactComponentB[(List[Product], State)]("ProductTable")
      |  .render(P => {
      |    val (products, state) = P
      |    val rows = products.filter(productFilter(state))
      |               .groupBy(_.category).toList
      |               .flatMap{ case (cat, ps) =>
      |                  ProductCategoryRow.withKey(cat)(cat) :: ps.map(p => ProductRow.withKey(p.name)(p))
      |                }
      |    <.table(
      |      <.thead(
      |        <.tr(
      |          <.th("Name"),
      |          <.th("Price"))),
      |      <.tbody(
      |        rows))
      |  })
      |  .build
      |
      |val SearchBar = ReactComponentB[(State, Backend)]("SearchBar")
      |  .render(P => {
      |    val (s, b) = P
      |    <.form(
      |      <.input(
      |        ^.placeholder := "Search Bar ...",
      |        ^.value       := s.filterText,
      |        ^.onChange   ==> b.onTextChange),
      |      <.p(
      |        <.input(
      |          ^.tpe     := "checkbox",
      |          ^.onClick ==> b.onCheckBox,
      |          "Only show products in stock")))
      |  })
      |  .build
      |
      |val FilterableProductTable = ReactComponentB[List[Product]]("FilterableProductTable")
      |  .initialState(State("", false))
      |  .backend(new Backend(_))
      |  .render((P, S, B) =>
      |    <.div(
      |      SearchBar((S,B)),
      |      ProductTable((P,S)))
      |  ).build
      |
      |  val products = List(
      |    Product("FootBall", 49.99, "Sporting Goods", true),
      |    Product("Baseball", 9.99, "Sporting Goods", true),
      |    Product("basketball", 29.99, "Sporting Goods", false),
      |    Product("ipod touch", 99.99, "Electronics", true),
      |    Product("iphone 5", 499.99, "Electronics", true),
      |    Product("Nexus 7", 199.99, "Electronics", true))
      |
      |React.render(FilterableProductTable(products), mountNode)
      | """.stripMargin

  case class Product(name: String, price: Double, category: String, stocked: Boolean)

  case class State(filterText: String, inStockOnly: Boolean)

  class Backend($: BackendScope[_, State])  {
    def onTextChange(e: ReactEventI) =
      $.modState(_.copy(filterText = e.target.value))
    def onCheckBox(e: ReactEvent) =
      $.modState(s => s.copy(inStockOnly = !s.inStockOnly))
  }

  val ProductCategoryRow = ReactComponentB[String]("ProductCategoryRow")
    .render(category => <.tr(<.th(^.colSpan := 2, category)))
    .build

  val ProductRow = ReactComponentB[Product]("ProductRow")
    .render(P =>
      <.tr(
        <.td(<.span(!P.stocked ?= ^.color.red, P.name)),
        <.td(P.price))
    )
    .build

  def productFilter(s: State)(p: Product): Boolean =
    p.name.contains(s.filterText) &&
    (!s.inStockOnly || p.stocked)

  val ProductTable = ReactComponentB[(List[Product], State)]("ProductTable")
    .render(P => {
      val (products, state) = P
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
    })
    .build

  val SearchBar = ReactComponentB[(State, Backend)]("SearchBar")
    .render(P => {
      val (s, b) = P
      <.form(
        <.input(
          ^.placeholder := "Search Bar ...",
          ^.value       := s.filterText,
          ^.onChange   ==> b.onTextChange),
        <.p(
          <.input(
            ^.tpe     := "checkbox",
            ^.onClick ==> b.onCheckBox,
            "Only show products in stock")))
    })
    .build

  val FilterableProductTable = ReactComponentB[List[Product]]("FilterableProductTable")
    .initialState(State("", false))
    .backend(new Backend(_))
    .render((P, S, B) =>
      <.div(
        SearchBar((S,B)),
        ProductTable((P,S)))
    ).build

    val products = List(
      Product("FootBall", 49.99, "Sporting Goods", true),
      Product("Baseball", 9.99, "Sporting Goods", true),
      Product("basketball", 29.99, "Sporting Goods", false),
      Product("ipod touch", 99.99, "Electronics", true),
      Product("iphone 5", 499.99, "Electronics", true),
      Product("Nexus 7", 199.99, "Electronics", true))
}
