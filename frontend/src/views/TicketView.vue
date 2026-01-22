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
                <div class="flex flex-wrap gap-6 text-sm">
                    <div>
                        <span class="text-green-500">> Estado: </span>
                        <span class="ml-2 px-2 py-1 rounded"
                            :class="statusClass(ticket.estado)">
                            {{ ticket.estado }}
                        </span>
                    </div>
                    <div>
                        <span class="text-green-500">> Autor: </span>
                        <span class="ml-2">{{ ticket.creadoPor?.nombre || ticket.creadoPor?.email || 'Desconocido' }}</span>
                    </div>
                    <div>
                        <span class="text-green-500">> Creado: </span>
                        <span class="ml-2">{{ formatDate(ticket.fechaCreacion) }}</span>
                    </div>
                    <div>
                        <span class="text-green-500">> Comentarios: </span>
                        <span class="ml-2">{{ ticket.comentarios ? ticket.comentarios.length : 0 }}</span>
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

            <!--historial-->
            <div class="mb-6" v-if="ticket.comentarios && ticket.comentarios.length > 0">
                <h3 class="text-lg text-green-400 mb-6">> Historial: </h3>
                <div class="space-y-8">
                    <div v-for="comentario in ticket.comentarios" :key="comentario.id"
                        class="border-l-2 border-green-500 pl-3 py-2 history-entry">
                        <div class="flex justify-between text-sm text-green-500">
                            <span>> {{ comentario.autor?.nombre || comentario.autor?.email || 'Usuario desconocido' }}</span>
                            <span>{{ formatDate(comentario.fechaCreacion) }}</span>
                        </div>
                        <pre class="text-green-300 mt-1 font-mono whitespace-pre-wrap">{{ comentario.contenido }}</pre>
                    </div>
                </div>
            </div>

            <!--mensaje cuando no hay comentarios-->
            <div v-else class="text-gray-500 italic">
                > No hay comentarios para este ticket!...
            </div>
        </main>
    </div>
</template>

<script>
import api from '@/services/api';
import Swal from 'sweetalert2';
import SocketService from '@/services/SocketService';

export default {
    data() {
        return {
            ticket: {
                id: null,
                titulo: '',
                descripcion: '',
                estado: '',
                fechaCreacion: '',
                comentarios: []
            },
            nuevoComentario: '',
            isSubmitting: false,
            polling: null
        }
    },

    created() {
        this.cargarTicketCompleto();
        this.iniciarPolling();

        // Suscribirse a actualizaciones en tiempo real
        this.liveUpdateHandler = () => {
            console.log('>>> Recargando detalles del ticket por evento Socket');
            this.cargarTicketCompleto();
        };
        SocketService.on('TICKET_UPDATE', this.liveUpdateHandler);
    },

    beforeUnmount() {
        this.detenerPolling();
        if (this.liveUpdateHandler) {
            SocketService.off('TICKET_UPDATE', this.liveUpdateHandler);
        }
    },

    methods: {
        iniciarPolling() {
            this.polling = setInterval(() => {
                if (this.ticket && this.ticket.id) {
                    this.cargarComentariosSeparado();
                }
            }, 30000); // Actualiza cada 30 segundos.
        },

        detenerPolling() {
            if (this.polling) {
                clearInterval(this.polling);
            }
        },

        async cargarTicketCompleto() {
            try {
                const ticketId = this.$route.params.id;

                const [ticketResponse, comentariosResponse] = await Promise.all([
                    api.get(`/tickets/${ticketId}`),
                    api.get(`/tickets/${ticketId}/comentarios`)
                ]);

                console.log('Fecha ticket (raw):', ticketResponse.data.fechaCreacion);
                this.debugDate(ticketResponse.data.fechaCreacion);

                if (comentariosResponse.data && comentariosResponse.data.length > 0) {
                    console.log('Fecha primer comentario (raw):', comentariosResponse.data[0].fechaCreacion);
                    this.debugDate(comentariosResponse.data[0].fechaCreacion);
                }

                this.ticket = {
                    ...ticketResponse.data,
                    comentarios: Array.isArray(comentariosResponse.data)
                        ? comentariosResponse.data
                        : []
                };

                // Ordenar comentarios (más recientes primero)
                this.ordenarComentarios();

            } catch (error) {
                console.error("Error cargando ticket completo:", error);
                this.ticket.comentarios = [];
            }
        },

        async cargarComentariosSeparado() {
            try {
                const response = await api.get(`/tickets/${this.ticket.id}/comentarios`);
                this.ticket.comentarios = Array.isArray(response.data)
                    ? response.data
                    : [];

                this.ordenarComentarios();
            } catch (error) {
                console.error("Error cargando comentarios:", error);
            }
        },

        ordenarComentarios() {
            if (!this.ticket.comentarios || this.ticket.comentarios.length === 0) return;

            this.ticket.comentarios.sort((a, b) => {
                const getTimestamp = (fechaArray) => {
                    if (!Array.isArray(fechaArray)) return 0;
                    const [year, month, day, hours, minutes] = fechaArray;
                    return new Date(year, month - 1, day, hours, minutes).getTime();
                };

                const timestampA = getTimestamp(a.fechaCreacion);
                const timestampB = getTimestamp(b.fechaCreacion);

                return timestampB - timestampA;
            });

            this.$forceUpdate();
        },

        formatDate(dateStringOrArray) {
            if (!dateStringOrArray) return '[Fecha no disponible]';

            try {
                let date;

                if (Array.isArray(dateStringOrArray)) {
                    const [year, month, day, hours, minutes] = dateStringOrArray;

                    date = new Date(year, month - 1, day, hours, minutes);
                } else if (typeof dateStringOrArray === 'string') {
                    date = new Date(dateStringOrArray);

                    if (isNaN(date.getTime())) {
                        if (dateStringOrArray.includes('T') && dateStringOrArray.includes('+')) {
                            date = new Date(dateStringOrArray.replace('+', '.000+'));
                        } else {
                            date = new Date(dateStringOrArray.replace(' ', 'T'));
                        }
                    }
                } else {
                    date = new Date(dateStringOrArray);
                }

                if (isNaN(date.getTime())) {
                    console.warn('Fecha no reconocida:', dateStringOrArray);
                    return '[Fecha inválida]'; // Devuelve el string original
                }

                return date.toLocaleString('es-MX', {
                    day: '2-digit',
                    month: 'short',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                    hour12: false
                }).replace(/\//g, '-').replace(',', '');

            } catch (error) {
                console.error('Error formateando fecha:', error, dateStringOrArray);
                return '[Error de fecha]';
            }
        },

        debugDate(dateString) {
            console.log('Fecha recibida:', dateString);
            console.log('Nuevo Date():', new Date(dateString));
            console.log('Timestamp:', new Date(dateString).getTime());
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
                if (this.ticket.estado !== 'ABIERTO' && this.ticket.estado !== 'EN_PROGRESO') {
                    Swal.fire({
                        title: '> Error',
                        text: "Solo puedes agregar comentarios a tickets ABIERTOS o EN PROGRESO",
                        icon: 'warning',
                        background: '#000',
                        color: '#ffaa00',
                        confirmButtonText: '> OK',
                        confirmButtonColor: '#333',
                        customClass: {
                            popup: 'border border-yellow-500 rounded-none',
                            title: 'font-mono',
                            confirmButton: 'font-mono'
                        }
                    });
                    return;
                }

                if (!this.nuevoComentario.trim()) {
                    Swal.fire({
                        title: '> Input Vacío',
                        text: "El comentario no puede estar vacío",
                        icon: 'error',
                        background: '#000',
                        color: '#ff4444',
                        confirmButtonText: '> OK',
                        confirmButtonColor: '#333',
                        customClass: {
                            popup: 'border border-red-500 rounded-none',
                            title: 'font-mono',
                            confirmButton: 'font-mono'
                        }
                    });
                    return;
                }

                const response = await api.post(`/tickets/${this.ticket.id}/comentarios`, {
                    contenido: this.nuevoComentario
                });

                console.log("Respuesta al agregar comentario:", response.data);

                if (response.data && response.data.id) {
                    this.ticket.comentarios.unshift(response.data);
                    this.ordenarComentarios();
                } else {
                    await this.cargarComentariosSeparado();
                }

                this.nuevoComentario = '';

                // Scroll al nuevo comentario
                this.$nextTick(() => {
                    const comments = document.querySelectorAll('.border-l-2');
                    if (comments.length > 0) {
                        comments[0].scrollIntoView({ behavior: 'smooth' });
                        comments[0].classList.add('new-comment');
                    }
                });

            } catch (error) {
                console.error("Error agregando comentario:", error);
                Swal.fire({
                    title: '> Error',
                    text: "Error al agregar comentario: " + (error.response?.data || error.message),
                    icon: 'error',
                    background: '#000',
                    color: '#ff4444',
                    confirmButtonText: '> OK',
                    confirmButtonColor: '#333',
                    customClass: {
                        popup: 'border border-red-500 rounded-none'
                    }
                });
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

/* Estilos para el historial */
.history-entry {
    margin-bottom: 1rem;
    padding: 0.5rem;
    border-left: 2px solid #10b981;
}

/* Efecto de scroll automático para el último comentario */
.history-entry:last-child {
    animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
    from { opacity: 0.8; transform: translateY(5px); }
    to { opacity: 1; transform: translateY(0); }
}

/* Destacar nuevos comentarios */
.new-comment {
    animation: pulse 2s ease-in-out;
}

@keyframes pulse {
    0% { background-color: rgba(74, 222, 128, 0.1); }
    100% { background-color: transparent; }
}
</style>