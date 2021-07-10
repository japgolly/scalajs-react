const useState = React.useState

function Example() {
  // Declare a new state variable, which we'll call "count"
  const [count, setCount] = useState(0);

  console.log(`State = ${count}`)

  return React.createElement("button"
  // , null,
  // ,{ onClick: () => setCount(s => (s + 1)) },
  ,{ onClick: () => setCount(s => s) }, // returning s is how to abort a modState
  "YOOOOOOOOOOOOOOOOOOOOOOOOOO!")
}

const Root = document.getElementById('root');
ReactDOM.render(React.createElement(Example, null), Root)
