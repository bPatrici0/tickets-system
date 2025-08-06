<template>
    <div class="min-h-screen bg-black p-4">
        <header class="terminal-box mb-5">
            <div class="flex justify-between items-center">
                <h1 class="text-4xl">
                    > Ticket # {{ ticket.id }}<span class="cursor-blink">|</span>
                </h1>
                <button
                    @click="$router.push('/tickets')"
                    class="text-green-500 hover:text-green-400"
                >
                    > Volver a Mis Tickets
                </button>
            </div>
        </header>

        <main>
            <!--encabezado ticket-->
            <div class="border-b border-green-500 pb-4 mb-4">
                <h2 class="text-2xl text-green-400 mb-2">> {{ ticket.titulo }}</h2>
                <div class="flex flex-wrap gap-4 text-sm">
                    <div>
                        <span class="text-green-500">> Estado: </span>
                        <span class="ml-2 px-2 py-1 rounded"
                            :class="statusClass(ticket.estado)">
                            {{ ticket.estado }}
                        </span>
                    </div>
                    <div>
                        <span class="text-green-500">> Creado: </span>
                        <span class="ml-2">{{ formatDate(ticket.fechaCreacion) }}</span>
                    </div>
                    <div>
                        <span class="text-green-500">> Por: </span>
                        <span class="ml-2">{{ ticket.usuario }}</span>
                    </div>
                </div>
            </div>

            <!--descripcion del problema-->
            <div class="mb-6">
                <h3 class="text-lg text-green-400 mb-2">> Descripción: </h3>
                <div class="bg-black border border-green-500 p-4 text-green-300 whitespace-pre-wrap">
                    {{ ticket.descripcion }}
                </div>
            </div>

            <!--comentarios-->
            <div class="mb-6" v-if="ticket.comentarios && ticket.comentarios.length > 0">
                <h3 class="text-lg text-green-400 mb-2">> Historial: </h3>
                <div class="space-y-3">
                    <div v-for="comentario in ticket.comentarios" :key="comentario.id"
                        class="border-1-2 border-green-500 pl-3 py-1">
                        <div class="flex justify-between text-sm text-green-500">
                            <span>> {{ comentario.autor }}</span>
                            <span> {{ formatDate(comentario.fechaCreacion) }}</span>
                        </div>
                        <p class="text-green-300 mt-1">{{ comentario.contenido }}</p>
                    </div>
                </div>
            </div>

            <!--formulario para nuevo comentario-->
            <div v-if="ticket.estado !== 'RESUELTO'">
                <h3 class="text-lg text-green-400 mb-2">> Añadir comentario:</h3>
                <form @submit.prevent="agregarComentario">
                    <textarea
                        v-model="nuevoComentario"
                        rows="3"
                        class="w-full bg-black border-green-500 p-2 text-green-500 focus:outline-none mb-2"
                        required
                        placeholder="Escribe tu comentario..."
                    ></textarea>
                    <button
                        type="submit"
                        class="btn-matrix"
                        :disabled="isSubmitting"
                    >
                        <span v-if="!isSubmitting">> Enviar comentario</span>
                        <span v-else>> Enviando...</span>
                    </button>
                </form>
            </div>

            <!--boton para cerrar ticket (solo si esta abierto)-->
            <div class="mt-6" v-if="ticket.estado === 'ABIERTO'">
                <button
                    @click="cerrarTicket"
                    class="btn-matrix bg-green-900/30 hover:bg-green-900/50"
                    :disabled="isSubmitting"
                >
                    > Marcar como resuelto
                </button>
            </div>
        </main>
    </div>
</template>

<script>
import api from '@/services/api';

export default {
    data() {
        return {
            ticket: {
                id: null,
                titulo: '',
                descripcion: '',
                estado: '',
                fechaCreacion: '',
                usuario: '',
                comentarios: []
            },
            nuevoComentario: '',
            isSubmitting: false
        }
    },
    created() {
        this.cargarTicket();
    },
    methods: {
        async cargarTicket() {
            try {
                const ticketId = this.$route.params.id;
                const response = await api.get(`/tickets/${ticketId}`);
                this.ticket = response.data;

                if (!this.ticket.comentarios) {
                    this.ticket.comentarios = [];
                }
            } catch (error) {
                console.error("Error cargando ticket: ", error);
                alert("Error al cargar el ticket");
                this.$router.push('/tickets');
            }
        },

        formatDate(dateString) {
            const options = {
                day: '2-digit',
                month: 'short',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            };
            return new Date(dateString).toLocaleDateString('es-MX', options);
        },

        statusClass(estado) {
            const statusMap = {
                'ABIERTO': 'bg-yellow-500/20 text-yellow-400',
                'EN_PROGRESO': 'bg-blue-500/20 text-blue-400',
                'RESUELTO': 'bg-green-500/20 text-green-400'
            };
            return statusMap[estado] || 'bg-gray-500/20 text-gray-400';
        },

        async agregarComentario() {
            this.isSubmitting = true;
            try {
                const response = await api.post(`/tickets/${this.ticket.id}/comentarios`, {
                    contenido: this.nuevoComentario
                });

                if (response.status === 200 || response.status === 201) {
                    this.nuevoComentario = '';
                    await this.cargarTicket();
                }
            } catch (error) {
                console.error("Error agregando comentario: ", error);
                alert("Error al agregar comentario");
            } finally {
                this.isSubmitting = false;
            }
        },

        async cerrarTicket() {
            if(!confirm("Estas seguro marcar este ticket como resuelto?...")) return;

            this.isSubmitting = true;
            try {
                const response = await api.put(`/tickets/${this.ticket.id}/estado`,{
                    estado: 'RESUELTO'
                });

                if (response.status === 200) {
                    alert("Ticket marcado como resuelto");
                    await this.cargarTicket();
                }
            } catch (error) {
                console.error("Error cerrando ticket: ", error);
                alert("Error al cerrar ticket");
            } finally {
                this.isSubmitting = false;
            }
        }
    }
}
</script>

<style scoped>
.btn-matrix {
    @apply bg-green-500/20 text-green-400 border border-green-500 px-4 py-2 hover:bg-green-500/30 transition-colors;
}

.cursor-blink {
    animation: blink 1s step-end infinite;
}

@keyframes blink {
    from, to { opacity: 1; }
    50% { opacity: 0; }
}

.terminal-box {
    @apply border border-green-500 p-4;
}
</style>