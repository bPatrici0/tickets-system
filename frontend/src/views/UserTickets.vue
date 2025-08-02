<template>
    <div class="min-h-screen bg-black p-4">
        <header class="terminal-box mb-5">
            <div class="div1 flex justify-between items-center">
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
        <main class="flex flex-col md:flex-row gap-6">
            <!--formulario-->
            <div class="terminal-box md:w-1/3">
                <h3 class="text-lg mb-3">> Crear Ticket</h3>
                <form @submit.prevent="submitTicket">
                    <div class="mb-8">
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
                        aria-label="Enviar ticket"
                    >
                        <span v-if="!isSubmitting">> Enviar ticket</span>
                        <span v-else>> Enviando...</span>
                    </button>
                </form>
            </div>

            <div class="terminal-box md:w-2/3">
                <h3 class="text-lg mb-3">> Tus Tickets <span class="text-green-400"></span></h3>

                <!--lista de tickets-->
                <div class="space-y-3 max-h-[calc(100vh-200px)] overflow-y-auto pr-2">
                    <table class="w-full border-green-500">
                        <thead>
                            <tr class="bg-black text-green-500 border-b border-green-500">
                                <th class="p-2 text-left">> ID</th>
                                <th class="p-2 text-left">> Asunto</th>
                                <th class="p-2 text-left">> Estado</th>
                                <th class="p-2 text-left">> Fecha</th>
                            </tr>
                        </thead>

                        <tbody>
                            <tr
                                v-for="ticket in sortedTickets"
                                :key="ticket.id"
                                class="border-b border-green-500 hover:bg-green-900/10"
                            >
                                <td class="p-2 text-green-400">#{{ ticket.id }}</td>
                                <td class="p-2 text-green-300">{{ ticket.titulo }}</td>
                                <td class="p-2">
                                    <span class="px-2 py-1 rounded text-xs"
                                        :class="statusClass(ticket.estado)">
                                        {{ ticket.estado }}
                                    </span>
                                </td>
                                <td class="p-2 text-green-400 text-sm">
                                    {{ formatDate(ticket.fechaCreacion) }}
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <div
                        v-for="ticket in sortedTickets"
                        :key="ticket.id"
                        class="border border-green-500 p-3 hover:bg-green-900/10 transition-colors"
                    >
                        <div class="flex justify-between items-start">
                            <div>
                                <h4 class="text-green-400 font-mono">#{{ ticket.id }} > {{ ticket.titulo }}</h4>
                                <p class="text-green-300 text-sm mt-1">{{ ticket.descripcion }}</p>
                            </div>
                            <span class="text-xs px-2 py-1 rounded"
                                :class="{
                                    'bg-yellow-500/20 text-yellow-400': ticket.estado === 'ABIERTO',
                                    'bg-blue-500/20 text-blue-400': ticket.estado === 'EN_PROGRESO',
                                    'bg-green-500/20 text-green-400': ticket.estado === 'RESUELTO'
                                }">
                                {{ ticket.estado }}
                            </span>
                        </div>
                        <div class="flex justify-between items-center mt-2 text-xs text-green-500">
                            <span>Creado: {{ formatDate(ticket.fechaCreacion) }}</span>
                            <button
                                @click="verTicket(ticket.id)"
                                class="text-green-400 hover:text-green-300"
                            >
                                > Ver detalles
                            </button>
                        </div>
                    </div>
                </div>
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
                const response = await api.post('/tickets', {
                    titulo: this.newTicket.titulo,
                    descripcion: this.newTicket.descripcion
                });

                if (response.status === 200 || response.status === 201) {
                    alert('Ticket creado exitosamente!...');
                    this.newTicket = { titulo: '', descripcion: '' };
                }
            } catch (error) {
                console.error("Error completo: ", error);
                let errorMsg = "Error al crear ticket!...";

                if (error.response) {
                    if(error.response.status === 401) {
                        errorMsg = "No autorizado - por favor inicia sesión nuevamente";
                        this.$router.push('/login');
                    } else if (error.response.data?.message) {
                        errorMsg = error.response.data.message;
                    }
                }
                alert(errorMsg);
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