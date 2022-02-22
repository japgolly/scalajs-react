import React from 'react'
import ReactDOM from 'react-dom'
import './index.css'

import App from './App'
// import * as x from './App'
// const App = x.App

// function renderer(z) {
//   return React.createElement(z.App, null);
// }

// console.log("App: ", (""+App).length)

ReactDOM.render(
  // <React.StrictMode>
    // <App />
    // renderer(x)
    // React.createElement(x.App, null)
    React.createElement(App, null)
  // </React.StrictMode>
  ,
  document.getElementById('root')
)
