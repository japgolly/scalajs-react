import React, { useState } from 'react'

function App() {
  const [count, setCount] = useState(0)

  return React.createElement("button", {
    type: "button",
    onClick: () => setCount(count => count + 1)
  }, "HotReload1 count is: ", count);
}

export default App
