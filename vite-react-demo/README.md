1. Install

  ```sh
  git clone https://github.com/japgolly/scalajs-react.git vite-demo
  cd vite-demo
  git checkout experiment/vite
  cd vite-react-demo
  yarn
  yarn dev
  ```

2. Open browser to http://localhost:3000/
3. Click both buttons a few times
4. Edit `src/HotReload1.js` and modify the `""HotReload1 count is: "` string. You'll see it automatically update the UI without losing the counts on the buttons.
5. Now do the same to `src/HotReload2.js` and you'll see the browser reload the whole page, and the counts go to 0.

The only difference between `src/HotReload{1,2}.js` is that the latter contains a non-default export: `const x = 1; export { x }`
