<template>
    <div id="app" class="min-h-screen bg-black  text-green-200 font-mono">

        <!--contenedor de vistas-->
        <router-view class="terminal-box" />
    </div>
</template>

<script>
import SocketService from './services/SocketService';

export default {
    name: 'App',
    mounted() {
        console.log('>>> Sistema de Tickets Iniciado');
        this.checkConnection();
    },
    watch: {
        $route() {
            // Re-verificar conexión en cada cambio de ruta por si el usuario se acaba de loguear
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
        }
    }
}
</script>