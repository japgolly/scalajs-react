import React, { useState, useEffect } from 'react'

function App() {
  const [count, setCount] = useState(0)

  useEffect(() => {
    document.title = `You clicked ${count} times`;
  });

  return React.createElement("button", {
    type: "button",
    onClick: () => setCount(count => count + 1)
  }, "count is: ", count);
}

export default App
