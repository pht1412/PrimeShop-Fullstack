import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  base: '/',
  plugins: [react()],
  define: {
    global: 'window',
  },
  resolve: {
    alias: {
      '@': '/src',
    }
  },
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/ws": {
        target: "http://localhost:8080",
        changeOrigin: true,
        ws: true,
      }
    },
    historyApiFallback: true,
    host: '0.0.0.0',
    port: 5173,
    allowedHosts: ['all', 'primeshop-vnpay.loca.lt'],
  },
  assetsInclude: ['**/*.json']
})
