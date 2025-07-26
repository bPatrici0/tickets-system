<template>
    <div class="min-h-screen bg-black p-4">
        <header class="terminal-box mb-6 text-center">
            <h1 class="text-2x1">
                > Sistema de Tickets<span class="cursor-blink">|</span>
            </h1>
            <nav class="mt-4">
                <button
                    @click="handleLogout"
                    class="btn-matrix"
                >
                    > Cerrar sesión
                </button>
            </nav>
        </header>

        <!--contenido-->
        <main class="terminal-box max-w-4x1 mx-auto">
            <h2 class="text-x1 mb-4"> {{ $route.meta.title }}<span class="cursor-blink">|</span></h2>
            <!--formulario-->
        </main>
    </div>
</template>

<script>
export default {
    methods: {
        handleLogout() {
            this.$router.push('/login');
        }
    }
}
</script>