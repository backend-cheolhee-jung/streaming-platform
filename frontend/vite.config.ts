import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const gatewayUrl = process.env.VITE_GATEWAY_URL ?? 'http://localhost:10100'

export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    port: 3000,
    proxy: {
      '/users': { target: gatewayUrl, changeOrigin: true },
      '/streams': { target: gatewayUrl, changeOrigin: true },
      '/payments': { target: gatewayUrl, changeOrigin: true },
    },
  },
})
