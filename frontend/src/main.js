import { createApp } from 'vue'
import App from './App.vue'

// Inyección directa de estilos
const app = createApp(App)
document.body.innerHTML = `
  <div id="app" style="background: black; min-height: 100vh;">
    <!-- Espacio para Vue -->
  </div>
`
app.mount('#app')

// Verificación en consola del navegador
console.log('Vue está montado:', !!app._container)
