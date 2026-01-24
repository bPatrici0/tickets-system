<template>
    <div id="app" class="min-h-screen bg-black  text-green-200 font-mono">
        <!-- Efecto de lluvia Matrix -->
        <MatrixRain v-if="showRain" :active="rainActive" />

        <!--contenedor de vistas-->
        <router-view v-show="!showRain" class="terminal-box" />
    </div>
</template>

<script>
import SocketService from './services/SocketService';
import MatrixRain from './components/MatrixRain.vue';
import SoundService from './services/SoundService';

export default {
    name: 'App',
    components: {
        MatrixRain
    },
    data() {
        return {
            showRain: false,
            rainActive: false
        };
    },
    mounted() {
        console.log('>>> Sistema de Tickets Iniciado');
        SoundService.init();
        this.checkConnection();
    },
    watch: {
        $route() {
            this.triggerMatrixEffect();
            this.checkConnection();
        }
    },
    methods: {
        checkConnection() {
            const token = localStorage.getItem('token');
            const userEmail = localStorage.getItem('userEmail');
            const userRole = localStorage.getItem('userRole');

            if (token && userEmail && !SocketService.connected) {
                console.log('>>> Iniciando Sockets para:', userEmail);
                SocketService.connect(userEmail, userRole);
            }
        },
        triggerMatrixEffect() {
            this.showRain = true;
            this.rainActive = true;
            
            // Simular carga de datos/transición
            setTimeout(() => {
                this.rainActive = false;
                setTimeout(() => {
                    this.showRain = false;
                }, 800); // Coincide con el fade-out de CSS
            }, 1000);
        }
    }
}
</script>