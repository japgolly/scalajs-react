'use strict';

const Root = document.getElementById('root');

class Comp extends React.Component {

  onChange(e) {
    e.persist()
    trace("hxahaha2", performance.now(), () => {
      console.log("Event: ", e)
      this.setState({ value: e.target.value })
    })
  }

  onChange(e) {
  }

  onKey(e) {
    e.persist()
    const {key, keyCode, which} = e
    let mods = ""
    if (e.altKey) mods = `${mods}, altKey = true`
    if (e.ctrlKey) mods = `${mods}, ctrlKey = true`
    if (e.metaKey) mods = `${mods}, metaKey = true`
    if (e.shiftKey) mods = `${mods}, shiftKey = true`
    console.log(`def ${key}                : Keyboard = apply(key = "${key}", keyCode = ${keyCode}, which = ${which}${mods})`)
    e.preventDefault()
  }

  render() {
    const input = React.createElement("input", {
      autoFocus: true,
      value: 'press keys here, look in console',
      onChange: this.onChange.bind(this),
      onKeyDown: this.onKey.bind(this),
      style: {width: "100%"},
    })

    return input
  }
}

ReactDOM.render(React.createElement(Comp, null), Root)