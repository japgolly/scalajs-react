import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
const path = require('path')

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      'sjs': path.resolve(__dirname, '../experiment/target/scala-2.13/root-fastopt'),
    },
  },
})
