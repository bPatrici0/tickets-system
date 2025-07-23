import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [
    vue({
      template: {
        compilerOptions: {
          comments: false // Elimina comentarios de desarrollo
        }
      }
    })
  ],
  server: {
    open: false, // Evita que se abra el navegador automáticamente
    hmr: {
      overlay: false // Desactiva el overlay de errores
    }
  }
});
