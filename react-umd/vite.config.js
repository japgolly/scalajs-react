import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig(({ mode }) => {
  const isProduction = mode === 'production';

  return {
    plugins: [react()],

    define: {
      'process.env.NODE_ENV': JSON.stringify(isProduction ? 'production' : 'development'),
    },

    build: {
      // Prevent Vite from clearing the dist directory on every build.
      emptyOutDir: false,

      minify: isProduction,

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
        fileName: (format) => isProduction ? `react.${format}.min.js` : `react.${format}.js`,
      },

      rollupOptions: {
        // We do NOT externalize React or ReactDOM.
        // By leaving 'external' empty, we are telling Vite/Rollup
        // to bundle them *into* our library file.
        external: [],
      },
    },
  };
});
