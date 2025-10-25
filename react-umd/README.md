# What is this?

React v19 stopped shipping UMD builds.
JSDOM doesn't support module-based imports —
[see ticket here](https://github.com/scala-js/scala-js-env-jsdom-nodejs/issues/56) —
so in order to be able to run unit tests we have to generate our own build of React.

# How does it work?

1. Input libraries are specified in `package.json`
2. Our bundle definition is specified in `vite.config.js` and `src/main.js`
3. Run `npm run build` to generate `dist/react.umd.js`
4. Unit tests in Scala.js are configured to load `dist/react.umd.js`
