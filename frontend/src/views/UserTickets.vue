<template>
    <div class="min-h-screen bg-black p-4">
        <header class="terminal-box mb-5">
            <div class="flex justify-between items-center">
                <h1 class="text-4xl">
                    > Mis Tickets<span class="cursor-blink">|</span>
                </h1>

                <!-- Menú de usuario-->
                <div class="relative">
                    <button
                        @click="toggleUserMenu"
                        class="flex items-center space-x-2 text-green-500 hover:text-green-400"
                        aria-label="Menú de usuario"
                    >
                        <span>> {{ userName }}</span>
                        <svg
                            class="w-4 h-4 transition-transform duration-200"
                            :class="{ 'rotate-180': showUserMenu }"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                        </svg>
                    </button>

                    <!--menu desplegable-->
                    <div
                        v-if="showUserMenu"
                        class="absolute right-0 mt-2 w-48 bg-black border border-green-500 rounded-md shadow-lg z-10"
                    >
                        <button
                            @click="handleLogout"
                            class="w-full text-left px-4 py-2 text-green-500 hover:bg-green-900 hover:text-green-300"
                        >
                            > Cerrar sesión
                        </button>
                    </div>
                </div>
            </div>
        </header>

        <!--contenido-->
        <main class="terminal-box max-w-4xl mx-auto">
            <h2 class="text-x1 mb-4"> {{ $route.meta.title }}<span class="cursor-blink">|</span></h2>

            <!--formulario-->
            <div class="mb-8 p-4 border border-green-500 rounded-lg">
                <h3 class="text-1g mb-3">> Crear Nuevo Ticket</h3>
                <form @submit.prevent="submitTicket">
                    <div class="mb-4">
                        <label class="block text-green-400 mb-1">> Asunto: </label>
                        <input
                            v-model="newTicket.titulo"
                            type="text"
                            class="w-full bg-black border border-green-500 p-2 text-green-500 focus:outline-none"
                            required
                            placeholder="Ej. Problema con teclado"
                        >
                    </div>

                    <div class="mb-4">
                        <label class="block text-green-400 mb-1">> Descripción: </label>
                            <textarea
                                v-model="newTicket.descripcion"
                                rows="4"
                                class="w-full bg-black border border-green-500 p-2 text-green-500 focus:outline-none"
                                required
                                placeholder="Describe el problema a detalle..."
                            >
                        </textarea>
                    </div>

                    <button
                        type="submit"
                        class="btn-matrix"
                        :disabled="isSubmitting"
                    >
                        <span v-if="!isSubmitting">> Enviar ticket</span>
                        <span v-else>> Enviando...</span>
                    </button>
                </form>
            </div>

            <div>
                <h3 class="text-lg mb-3">> Tus Tickets</h3>
                <!--lista de tickets-->
            </div>
        </main>
    </div>
</template>

<script>
import api from '@/services/api';

export default {
    data() {
        return {
            newTicket: {
                titulo: '',
                descripcion: ''
            },
            isSubmitting: false,
            showUserMenu: false,
            userName: localStorage.getItem('userEmail') || 'Usuario'
        }
    },
    created() {
        if (localStorage.getItem('userRole') === 'ROLE_ADMIN') {
            this.$router.push('/admin');
        }
    },
    methods: {
        toggleUserMenu() {
            this.showUserMenu = !this.showUserMenu;
        },
        handleLogout() {
            localStorage.clear();
            this.$router.push('/login');
        },
        async submitTicket() {
            this.isSubmitting = true;
            try {
                const { data } = await api.post('/tickets', this.newTicket);
                this.showSuccess('Ticket creado exitosamente');
                this.resetForm();
            } catch (error) {
                this.handleError(error);
            } finally {
                this.isSubmitting = false;
            }
        },
        showSuccess(message) {
            alert(message);
            //usar notificaciones como toast
        },
        resetForm() {
            this.newTicket = { titulo: '', descripcion: '' };
        },
        handleError(error) {
            console.error("Error al crear ticket: ", error);
            const message = error.response?.data?.message || "Error al crear ticket";
            alert(message);

            if (error.response?.status === 401) {
                this.handleLogout();
            }
        }
    }
}
</script>

<style scoped>
/*Animacion del menú*/
.rotate-180 {
    transform: rotate(180deg);
}
.z-10 {
    z-index: 10;
}
button:hover {
    transition: color 0.2s ease;
}

</style>