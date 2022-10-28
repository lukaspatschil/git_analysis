import { resolve } from 'path'
import { defineConfig } from 'vite'
import packageJson from './package.json';

export default defineConfig({
  build: {
    lib: {
      entry: resolve(__dirname, 'lib/main.js'),
      name: packageJson.name,
      fileName: packageJson.name
    },
    rollupOptions: {
      external: [],
      output: {
        globals: {}
      }
    }
  }
})