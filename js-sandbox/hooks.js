const useState = React.useState

var invocations =

function Example() {
  // Declare a new state variable, which we'll call "count"
  const [count, setCount] = useState(0);

  const useMemo

  console.log(`State = ${count}`)

  return React.createElement("button"
  // , null,
  ,{ onClick: () => setCount(s => (s + 1)) },
  "YOOOOOOOOOOOOOOOOOOOOOOOOOO!")
}

const Root = document.getElementById('root');
ReactDOM.render(React.createElement(Example, null), Root)
