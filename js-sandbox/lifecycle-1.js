'use strict';

const Root = document.getElementById('root');

function assertEq(...args) {
  var name, actual, expect = null
  if (args.length == 2)
    [actual, expect] = args
  else if (args.length == 3)
    [name, actual, expect] = args
  else
    throw `What? assertEq(${args})`

  const a = JSON.stringify(actual)
  const e = JSON.stringify(expect)
  if (a !== e) {
    console.log("Name: ", name)
    console.log("Actual (val): ", actual)
    console.log("Expect (val): ", expect)
    console.log("Actual (cmp): ", a)
    console.log("Expect (cmp): ", e)
    throw `AssertionFailure${name && ` in ${name}`}: ${a} ≠ ${e}`
  }
}

function assertOuterHTML(mounted, expect) {
  const dom = ReactDOM.findDOMNode(mounted)
  assertEq(dom.outerHTML, expect)
}

// ===================================================================================================

function Props(a, b, c) {
  function minus(that) {
    return Props(a - that.a, b - that.b, c - that.c)
  }

  return { a, b, c, minus }
}

var mountCountA = 0
var mountCountB = 0
var mountCountBeforeMountA = 0
var mountCountBeforeMountB = 0
var willMountCountA = 0
var willMountCountB = 0

function assertMountCount(expect) {
  assertEq("mountCountA", mountCountA, expect)
  assertEq("mountCountB", mountCountB, expect)
  assertEq("willMountCountA", willMountCountA, expect)
  assertEq("willMountCountB", willMountCountB, expect)
  assertEq("mountCountBeforeMountA", mountCountBeforeMountA, 0)
  assertEq("mountCountBeforeMountB", mountCountBeforeMountB, 0)
}

var didUpdates = []
var willUpdates = []
function assertUpdates(...expectedProps) {
  assertEq("willUpdates", willUpdates, expectedProps)
  assertEq("didUpdates", didUpdates, expectedProps)
}

var recievedPropDeltas = []

var willUnmountCount = 0

var err,info = null

class Inner extends React.Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  render() {
    const p = this.props;
    return React.createElement("div", null, `${p.a} ${p.b} ${p.c}`)
  }

  shouldComponentUpdate(next) {
    const cur = this.props;
    return (cur.a != next.a) || (cur.b != next.b)
  }

  componentWillMount() {
    mountCountBeforeMountA += mountCountA
    willMountCountA += 1
    mountCountBeforeMountB += mountCountB
    willMountCountB += 1
  }

  componentDidMount() {
    mountCountA += 1
    mountCountB += 1
  }

  componentWillUpdate(next) {
    const cur = this.props;
    willUpdates = willUpdates.concat(next.minus(cur))
  }

  componentDidUpdate(prev) {
    const cur = this.props;
    didUpdates = didUpdates.concat(cur.minus(prev))
  }

  componentWillUnmount() {
    willUnmountCount += 1
  }

  componentWillReceiveProps(next) {
    const cur = this.props;
    console.log(`receive *** = ${recievedPropDeltas} | ${JSON.stringify(next)} | ${JSON.stringify(cur)}`)
    recievedPropDeltas = recievedPropDeltas.concat(next.minus(cur))
  }
}


class Comp extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  render() {
    if (this.state.hasError)
      return React.createElement("div", null, `Error: ${this.state.error}`)
    else
      return React.createElement(Inner, this.props)
  }

  componentDidCatch(e, i) {
    err = e
    info = i
    console.log("Error: ", err)
    console.log("Error msg: ", e.message)
    console.log("Info: ", info)
    this.setState({error: e.message, hasError: true})
  }
}

// ===========================================================================================================================

function renderComp(props) {
return ReactDOM.render(React.createElement(Comp, props), Root)
}

assertMountCount(0)

var mounted = renderComp(Props(1, 2, 3))
assertMountCount(1)
assertOuterHTML(mounted, "<div>1 2 3</div>")
assertUpdates()

mounted = renderComp(Props(1, 2, 8))
assertOuterHTML(mounted, "<div>1 2 3</div>")
assertUpdates()

mounted = renderComp(Props(1, 5, 8))
assertOuterHTML(mounted, "<div>1 5 8</div>")
assertUpdates(Props(0, 3, 0))

assertEq("willUnmountCount", willUnmountCount, 0)
console.log("OMG 1")
const x = React.createElement(Comp, {a:"e"})
console.log("OMG 2")
mounted = ReactDOM.render(x, Root)
console.log("OMG 3")
assertOuterHTML(mounted, "<div>Error: next.minus is not a function</div>")
assertEq("willUnmountCount", willUnmountCount, 1)
