import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), {
    name: 'request-logger',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        console.log(`[Request] ${req.method} ${req.url}`);
        next();
      });
    }
  }],
})
