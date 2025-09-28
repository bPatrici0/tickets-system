<template>
    <div class="min-h-screen bg-black p-4">
        <header class="terminal-box mb-6">
            <div class="flex justify-between items-center">
                <h1 class="text-2xl">
                    > Administración - Ticket #{{ ticket.id }}<span class="cursor-blink">|</span>
                </h1>
                <div>
                    <button @click="$router.push('/admin/tickets')" class="btn-matrix text-sm mr-2">
                        > Volver a Tickets
                    </button>
                    <button @click="handleLogout" class="btn-matrix text-sm">
                        > Cerrar Sesión
                    </button>
                </div>
            </div>
        </header>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <!-- Panel izquierdo - Información del ticket -->
            <div class="terminal-box lg:col-span-2">
                <!-- Encabezado ticket -->
                <div class="border-b border-green-500 pb-4 mb-4">
                    <div class="flex justify-between items-start mb-4">
                        <h1 class="text-3xl text-green-400">> {{ ticket.titulo }}</h1>
                        <span class="px-3 py-1 rounded text-sm font-bold"
                            :class="statusClass(ticket.estado)">
                            {{ ticket.estado }}
                        </span>
                    </div>

                    <div class="grid grid-cols-2 gap-4 text-sm">
                        <div>
                            <span class="text-green-500">> Creado por: </span>
                            <span class="ml-2">{{ ticket.usuario?.nombre || 'Usuario' }}</span>
                        </div>
                        <div>
                            <span class="text-green-500">> Email: </span>
                            <span class="ml-2">{{ ticket.usuario?.email || 'N/A' }}</span>
                        </div>
                        <div>
                            <span class="text-green-500">> Fecha creación: </span>
                            <span class="ml-2">{{ formatDate(ticket.fechaCreacion) }}</span>
                        </div>
                        <div>
                            <span class="text-green-500">> Última actualización: </span>
                            <span class="ml-2">{{ formatDate(ticket.fechaActualizacion) }}</span>
                        </div>
                    </div>
                </div>

                <!-- Descripción del problema -->
                <div class="mb-6">
                    <h3 class="text-lg text-green-400 mb-2">> Descripción:</h3>
                    <div class="bg-black border border-green-500 p-4 text-green-300 whitespace-pre-wrap">
                        {{ ticket.descripcion }}
                    </div>
                </div>

                <!-- Historial de comentarios -->
                <div class="mb-6" v-if="ticket.comentarios && ticket.comentarios.length > 0">
                    <h3 class="text-lg text-green-400 mb-4">> Historial de Comentarios:</h3>
                    <div class="space-y-4">
                        <div v-for="comentario in ticket.comentarios" :key="comentario.id"
                            class="border-l-2 border-green-500 pl-4 py-3">
                            <div class="flex justify-between items-center text-sm text-green-500 mb-2">
                                <div>
                                    <span class="font-bold">{{ comentario.autor?.nombre || 'Usuario' }}</span>
                                    <span class="text-gray-400 ml-2">({{ comentario.autor?.rol === 'ROLE_ADMIN' ? 'Admin' : 'Usuario' }})</span>
                                </div>
                                <span>{{ formatDate(comentario.fechaCreacion) }}</span>
                            </div>
                            <pre class="text-green-300 whitespace-pre-wrap">{{ comentario.contenido }}</pre>
                        </div>
                    </div>
                </div>

                <!-- Mensaje cuando no hay comentarios -->
                <div v-else class="text-gray-500 italic">
                    > No hay comentarios para este ticket...
                </div>
            </div>

            <!-- Panel derecho - Acciones de administración -->
            <div class="terminal-box">
                <h2 class="text-xl text-green-400 mb-4">> Acciones de Administración<span class="cursor-blink">|</span></h2>

                <!-- Cambiar estado -->
                <div class="mb-6">
                    <h3 class="text-lg mb-2">> Cambiar Estado:</h3>
                    <div class="grid grid-cols-1 gap-2">
                        <button @click="cambiarEstado('ABIERTO')"
                            :disabled="ticket.estado === 'ABIERTO' || ticket.estado === 'RESUELTO'"
                            class="btn-status bg-yellow-500/20 text-yellow-400 border-yellow-500">
                            > Marcar como ABIERTO
                        </button>
                        <button @click="cambiarEstado('EN_PROGRESO')"
                            :disabled="ticket.estado === 'EN_PROGRESO' || ticket.estado === 'RESUELTO'"
                            class="btn-status bg-blue-500/20 text-blue-400 border-blue-500">
                            > Marcar como EN PROGRESO
                        </button>
                        <button @click="cambiarEstado('RESUELTO')"
                            :disabled="ticket.estado === 'RESUELTO'"
                            class="btn-status bg-green-500/20 text-green-400 border-green-500">
                            > Marcar como RESUELTO
                        </button>
                    </div>
                </div>

                <!-- Agregar comentario (solo si no está resuelto) -->
                <div v-if="ticket.estado !== 'RESUELTO'" class="mb-6">
                    <h3 class="text-lg mb-2">> Agregar Comentario:</h3>
                    <form @submit.prevent="agregarComentario">
                        <textarea
                            v-model="nuevoComentario"
                            rows="4"
                            class="w-full bg-black border border-green-500 p-3 text-green-400 focus:outline-none focus:ring-1 focus:ring-green-500 mb-3"
                            required
                            placeholder="Escribe tu comentario como administrador..."
                        ></textarea>
                        <button
                            type="submit"
                            class="btn-matrix w-full"
                            :disabled="isSubmitting"
                        >
                            <span v-if="!isSubmitting">> Agregar Comentario</span>
                            <span v-else>> Agregando...</span>
                        </button>
                    </form>
                </div>

                <!-- Información de estado -->
                <div v-else class="bg-red-500/10 border border-red-500 p-3 text-red-400">
                    <p class="text-sm">> Este ticket está RESUELTO. No se pueden agregar más comentarios.</p>
                </div>

                <!-- Estadísticas -->
                <div class="mt-6 pt-4 border-t border-green-500">
                    <h3 class="text-lg text-green-400 mb-2">> Estadísticas:</h3>
                    <div class="space-y-2 text-sm">
                        <div class="flex justify-between">
                            <span class="text-green-500">> Total comentarios:</span>
                            <span class="text-green-300">{{ ticket.comentarios?.length || 0 }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-green-500">> Estado actual:</span>
                            <span :class="statusTextClass(ticket.estado)">{{ ticket.estado }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-green-500">> Tiempo activo:</span>
                            <span class="text-green-300">{{ calcularTiempoActivo() }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
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
                fechaActualizacion: '',
                usuario: null,
                comentarios: []
            },
            nuevoComentario: '',
            isSubmitting: false,
            loading: false
        }
    },

    created() {
        this.verificarPermisos();
        this.cargarTicketCompleto();
    },

    methods: {
        verificarPermisos() {
            const userRole = localStorage.getItem('userRole');
            if (userRole !== 'ROLE_ADMIN') {
                alert('No tienes permisos de administrador');
                this.$router.push('/tickets');
                return;
            }
        },

        async cargarTicketCompleto() {
            this.loading = true;
            try {
                const ticketId = this.$route.params.id;

                const [ticketResponse, comentariosResponse] = await Promise.all([
                    api.get(`/admin/tickets/${ticketId}`),
                    api.get(`/tickets/${ticketId}/comentarios`)
                ]);

                this.ticket = {
                    ...ticketResponse.data,
                    comentarios: Array.isArray(comentariosResponse.data)
                        ? comentariosResponse.data
                        : []
                };

                this.ordenarComentarios();

            } catch (error) {
                console.error("Error cargando ticket:", error);
                if (error.response?.status === 403) {
                    alert('No tienes permisos para ver este ticket');
                    this.$router.push('/admin/tickets');
                } else if (error.response?.status === 404) {
                    alert('Ticket no encontrado');
                    this.$router.push('/admin/tickets');
                }
            } finally {
                this.loading = false;
            }
        },

        getUsuarioNombre() {
            if (!this.ticket.usuario) return 'Usuario desconocido';

            return this.ticket.usuario.nombre || this.ticket.usuario.username || 'Usuario';
        },

        getUsuarioEmail() {
            if (!this.ticket.usuario) return 'N/A';

            return this.ticket.usuario.email || 'N/A';
        },

        getComentarioAutorNombre(comentario) {
            if (!comentario.autor) return 'Usuario';

            return comentario.autor.nombre || comentario.autor.username || 'Usuario';
        },

        ordenarComentarios() {
            if (!this.ticket.comentarios || this.ticket.comentarios.length === 0) return;

            this.ticket.comentarios.sort((a, b) => {
                const dateA = this.parseDate(a.fechaCreacion);
                const dateB = this.parseDate(b.fechaCreacion);
                return dateB - dateA; // Más recientes primero
            });
        },

        parseDate(dateData) {
            if (Array.isArray(dateData)) {
                const [year, month, day, hours, minutes] = dateData;
                return new Date(year, month - 1, day, hours, minutes);
            }
            return new Date(dateData);
        },

        formatDate(dateData) {
            if (!dateData) return 'N/A';

            const date = this.parseDate(dateData);
            return date.toLocaleDateString('es-MX', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        },

        statusClass(estado) {
            const statusMap = {
                'ABIERTO': 'bg-yellow-500/20 text-yellow-400',
                'EN_PROGRESO': 'bg-blue-500/20 text-blue-400',
                'RESUELTO': 'bg-green-500/20 text-green-400'
            };
            return statusMap[estado] || 'bg-gray-500/20 text-gray-400';
        },

        statusTextClass(estado) {
            const statusMap = {
                'ABIERTO': 'text-yellow-400',
                'EN_PROGRESO': 'text-blue-400',
                'RESUELTO': 'text-green-400'
            };
            return statusMap[estado] || 'text-gray-400';
        },

        async cambiarEstado(nuevoEstado) {
            if (!confirm(`¿Cambiar estado a ${nuevoEstado}?`)) return;

            try {
                await api.put(`/admin/tickets/${this.ticket.id}/estado`, {

                    estado: nuevoEstado
                });

                this.ticket.estado = nuevoEstado;
                this.ticket.fechaActualizacion = new Date().toISOString();

                alert(`Estado cambiado a ${nuevoEstado}`);

            } catch (error) {
                console.error("Error cambiando estado:", error);
                alert("Error al cambiar estado: " + (error.response?.data?.message || error.message));
            }
        },

        async agregarComentario() {
            if (this.ticket.estado === 'RESUELTO') {
                alert("No se pueden agregar comentarios a tickets resueltos");
                return;
            }

            this.isSubmitting = true;
            try {
                const response = await api.post(`/tickets/${this.ticket.id}/comentarios`, {
                    contenido: this.nuevoComentario
                });

                // Agregar el nuevo comentario a la lista
                this.ticket.comentarios.unshift(response.data);
                this.ordenarComentarios();
                this.nuevoComentario = '';

                alert("Comentario agregado exitosamente");

            } catch (error) {
                console.error("Error agregando comentario:", error);
                alert("Error al agregar comentario: " + (error.response?.data?.message || error.message));
            } finally {
                this.isSubmitting = false;
            }
        },

        calcularTiempoActivo() {
            if (!this.ticket.fechaCreacion) return 'N/A';

            const creacion = this.parseDate(this.ticket.fechaCreacion);
            const ahora = new Date();
            const diffMs = ahora - creacion;
            const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
            const diffHours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));

            return `${diffDays}d ${diffHours}h`;
        },

        handleLogout() {
            localStorage.clear();
            this.$router.push('/login');
        }
    }
}
</script>

<style scoped>
.btn-matrix {
    @apply bg-green-500/20 text-green-400 border border-green-500 px-4 py-2 rounded hover:bg-green-500/30 transition-colors disabled:opacity-50 disabled:cursor-not-allowed;
}

.btn-status {
    @apply px-4 py-2 rounded border text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed;
}

.cursor-blink {
    animation: blink 1s step-end infinite;
}

@keyframes blink {
    from, to { opacity: 1; }
    50% { opacity: 0; }
}

.terminal-box {
    @apply border border-green-500 p-6 rounded-lg;
}

.history-entry {
    transition: all 0.3s ease;
}

.history-entry:hover {
    background-color: rgba(16, 185, 129, 0.05);
}
</style>