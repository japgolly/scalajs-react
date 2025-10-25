import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],

  define: {
    'process.env.NODE_ENV': JSON.stringify('development'),
  },

  build: {
    minify: false,
    sourcemap: false,

    // This is the key section for building a library.
    lib: {
      // The entry file that imports and exports React/ReactDOM.
      entry: path.resolve(__dirname, 'src/main.js'),

      // The name of the global variable that will be exposed in the UMD build.
      // When you include the script, you'll access React via `window.ReactDevTestBundle.React`
      name: 'ReactDevTestBundle',

      formats: ['umd'],

      // The name of the output file.
      fileName: (format) => `react.${format}.js`,
    },

    rollupOptions: {
      // We do NOT externalize React or ReactDOM.
      // By leaving 'external' empty, we are telling Vite/Rollup
      // to bundle them *into* our library file.
      external: [],
    },
  },
});
