<template>
    <div id="app" class="min-h-screen bg-black p-4 text-green-500 font-mono">
        <!-- header estilo terminal-->
        <header class="terminal-box mb-6">
            <h1 class="text-2x1">
                > Sistema de Tickets<span class="cursor-blink">|</span>
            </h1>
        </header>

        <!--navegacion estilo comandos-->
        <nav class="flex gap-4 mb-8 text-green-400">
            <router-link to="/home" class="hover:text-green-300">
                > Home
            </router-link>
            <router-link to="/tickets" class="hover:text-green-300">
                > tickets
            </router-link>
        </nav>

        <!--contenedor de vistas-->
        <router-view class="terminal-box" />
    </div>
</template>

<script>
export default {
    name: 'App',
}
</script>