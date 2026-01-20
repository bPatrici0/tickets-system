import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import '@/assets/main.css'
import 'sweetalert2/dist/sweetalert2.min.css';

console.log('>>> MAIN.JS CARGADO');
createApp(App)
    .use(router)
    .mount('#app');