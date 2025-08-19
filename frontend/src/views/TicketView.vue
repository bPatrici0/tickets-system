<template>
    <div class="min-h-screen bg-black p-4">
        <header class="terminal-box mb-12">
            <div class="flex justify-between items-center">
                <h1 class="text-5xl">
                    > Ticket No. {{ ticket.id }}<span class="cursor-blink">|</span>
                </h1>
                <button
                    @click="$router.push('/tickets')"
                    class="text-green-500 hover:text-green-400"
                >
                    > Volver a Mis Tickets
                </button>
            </div>
        </header>

        <main class="terminal-box">
            <!--encabezado ticket-->
            <div class="border-b border-green-500 pb-4 mb-4">
                <span class="mb-2" px-100>> Asunto: </span>
                <h1 class="text-4xl text-green-400 mb-2">> {{ ticket.titulo }}</h1>
                <div class="flex flex-wrap gap-4 text-sm">
                    <div>
                        <span class="text-green-500">> Estado: </span>
                        <span class="ml-2 px-2 py-1 rounded"
                            :class="statusClass(ticket.estado)">
                            {{ ticket.estado }}
                        </span>
                    </div>
                    <div>
                        <span class="text-green-500 mb-12">> Creado: </span>
                        <span class="ml-2">{{ formatDate(ticket.fechaCreacion) }}</span>
                    </div>
                </div>
            </div>

            <!--descripcion del problema-->
            <div class="mb-9">
                <h3 class="text-lg mb-2">> Descripción: </h3>
                <div class="bg-black border border-green-500 p-4 text-green-300 whitespace-pre-wrap">
                    {{ ticket.descripcion }}
                </div>
            </div>

            <!--historial-->
            <div class="mb-6" v-if="ticket.comentarios && ticket.comentarios.length > 0">
                <h3 class="text-lg text-green-400 mb-2">> Historial: </h3>
                <div class="space-y-4">
                    <div v-for="comentario in ticket.comentarios" :key="comentario.id"
                        class="border-1-2 border-green-500 pl-3 py-2">
                        <div class="flex justify-between text-sm text-green-500">
                            <span>> {{ comentario.autor || 'Usuario desconocido' }}</span>
                            <span> {{ formatDate(comentario.fechaCreacion) }}</span>
                        </div>
                        <pre class="text-green-300 mt-1 font-mono">{{ comentario.contenido }}</pre>
                    </div>
                </div>
            </div>

            <!--mensaje cuando no hay comentarios-->
            <div v-else class="text-gray-500 italic">
                > No hay comentarios para este ticket!...
            </div>

            <!--formulario para nuevo comentario solo si el ticket no esta resuelto-->
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
                        <span v-if="!isSubmitting">> Agregar comentario</span>
                        <span v-else>> Agregando...</span>
                    </button>
                </form>
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
                comentarios: [],
                polling: null
            },
            nuevoComentario: '',
            isSubmitting: false
        }
    },

    created() {
        this.cargarTicket();
        this.iniciarPolling();
    },

    beforeUnmount() {
        this.detenerPolling();
    },

    methods: {
        iniciarPolling() {
            this.polling = setInterval(() => {
                this.cargarTicket();
            }, 600000); //Actualiza cada 1 minuto.
        },

        detenerPolling() {
            if (this.polling) {
                clearInterval(this.polling);
            }
        },

        async cargarTicket() {
            try {
                const ticketId = this.$route.params.id;
                const response = await api.get(`/tickets/${ticketId}`);

                this.ticket = {
                    ...response.data,
                    comentarios: response.data.comentarios || []
                };

                console.log("Datos completos del ticket: ", this.ticket);

                //this.ticket.comentarios = this.ticket.comentarios?.map(c => ({...c})) || [];

                this.ticket.comentarios.sort((a, b) =>
                    new Date(b.fechaCreacion) - new Date(a.fechaCreacion)
                );
            } catch (error) {
                console.error("Error cargando ticket: ", error);
                alert("Error al cargar el ticket");
                this.ticket.comentarios = [];
                this.$router.push('/tickets');
            }
        },

        formatDate(dateString) {
            if (!dateString) return '[Fecha no disponible]';

            try {
                const date = new Date(dateString);
                return date.toLocaleString('es-MX', {
                    day: '2-digit',
                    month: 'short',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                    hour12: false
                }).replace(/\//g, '-').replace(',', '');
            } catch {
                return '[Fecha inválida]';
            }
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
                if (this.ticket.estado !== 'ABIERTO'){
                    alert("Solo puedes agregar comentarios a tickets ABIERTOS");
                    return;
                }

                if (!this.nuevoComentario.trim()) {
                    alert("El nuevo comentario no puede estar vacio");
                    return;
                }

                const response = await api.post(`/tickets/${this.ticket.id}/comentarios`, {
                    contenido: this.nuevoComentario
                }, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                //verificar la respuesta correctamente
                if (response.data && response.data.comenatarios) {
                    this.ticket.comentarios = response.data.comentarios;
                } else {
                    //si la respuesta no trae los comentarios, recarga el ticket
                    await this.cargarTicket();
                }

                this.nuevoComentario = '';

                //hacer scroll al nuevo comentario
                this.$nextTick(() => {
                    const element = document.querySelector('.history-entry:first-child');
                    if (element) {
                        element.scrollIntoView({ behavior: 'smooth' });
                        //animacion
                        element.classList.add('new-comment');
                    }
                });
            } catch (error) {
                console.error("Error agregando comentario: ", error);
                let errorMsg = "Error al agregar comentario";
                if(error.response) {
                    errorMsg = error.response.data || errorMsg;
                    if(error.response.status === 401) {
                        errorMsg = "No Autorizado - por favor inicia sesión nuevamente";
                        this.$router.push('/login');
                    }
                }
                alert(errorMsg);
            } finally {
                this.isSubmitting = false;
            }
        },

        formatTerminalDate(dateString) {
            const date = new Date(dateString);
            return date.toLocaleString('es-MX', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                hour12: false
            }).replace(/\//g, '-').replace(',', '');
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

/* Estilos para el historial */
.terminal-history {
    display: block !important;
    max-height: 60vh;
    overflow-y: auto;
    scrollbar-width: thin;
    scrollbar-color: #10b981 #000;
}
.history-entry {
    margin-bottom: 1rem;
    padding: 0.5rem;
    border-left: 2px solid #10b981;
}
.output .content {
    white-space: pre-wrap;
    word-break: break-word;
    color: #10b981;
    font-family: monospace;
}
.terminal-history::-webkit-scrollbar {
    width: 8px;
}
.terminal-history::-webkit-scrollbar-track {
    background: #000;
}
.terminal-history::-webkit-scrollbar-thumb {
    background-color: #10b981;
    border-radius: 4px;
}

.command-line {
    @apply text-green-400 mb-1;
}

.prompt {
    @apply text-green-500;
}

.command {
    @apply text-yellow-400;
}

.metadata {
    @apply flex justify-between text-gray-500 text-xs mb-1;
}

.output .content {
    @apply text-green-300 whitespace-pre-wrap ml-4;
    font-family: 'Courier New', monospace;
}

.separator {
    @apply my-3 text-gray-600;
}

.last-entry {
    @apply pb-0;
}

/* Efecto de scroll automático para el último comentario */
.history-entry:last-child {
    animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
    from { opacity: 0.8; transform: translateY(5px); }
    to { opacity: 1; transform: translateY(0); }
}

/*destacar nuevos comentarios*/
.new-comment {
    animation: pulse 2s ease-in-out;
}

@keyframes pulse {
    0% { background-color: rgba(74, 222, 128, 0.1); }
    100% { background-color: transparent; }
}
</style>