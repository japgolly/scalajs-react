import React from 'react'
import ReactDOM from 'react-dom'
import './index.css'
import HotReload1 from './HotReload1'
import HotReload2 from './HotReload2'

ReactDOM.render(
  <React.StrictMode>
    <div><HotReload1 /></div>
    <div><HotReload2 /></div>
  </React.StrictMode>,
  document.getElementById('root')
)
