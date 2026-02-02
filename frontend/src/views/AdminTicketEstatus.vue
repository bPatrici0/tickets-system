<template>
    <div class="min-h-screen bg-black p-4">
        <header class="terminal-box mb-6">
            <div class="flex justify-between items-center">
                <h1 class="text-2xl">
                    > Administración - Ticket #{{ ticket.id }}<span class="cursor-blink">|</span>
                </h1>
                <div class="flex items-center space-x-2">
                    <button @click="$router.push('/admin/tickets')" class="btn-matrix text-sm">
                        > Volver a Tickets
                    </button>
                    
                    <div class="relative">
                        <button
                            @click="toggleUtilityMenu"
                            class="btn-matrix text-sm"
                        >
                            > Opciones [{{ showUtilityMenu ? '-' : '+' }}]
                        </button>
                        
                        <!-- Menú desplegable -->
                        <transition name="menu">
                            <div v-if="showUtilityMenu" class="absolute right-0 mt-2 w-48 bg-black border border-green-500 rounded shadow-[0_0_15px_rgba(0,255,65,0.3)] z-50 overflow-hidden">
                                <button @click="generarReporte" class="w-full text-left px-4 py-2 text-green-400 hover:bg-green-500/20 transition-colors border-b border-green-900/50 text-xs">
                                    > Generar Reporte PDF
                                </button>
                                <button @click="handleLogout" class="w-full text-left px-4 py-2 text-red-500 hover:bg-red-500/10 transition-colors text-xs">
                                    > Cerrar sesión
                                </button>
                            </div>
                        </transition>
                    </div>
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
                        <div class="flex items-center space-x-2">
                            <span v-if="ticket.categoria" :class="['tag-badge', getTagClass(ticket.categoria)]">
                                [{{ ticket.categoria }}]
                            </span>
                            <span v-if="ticket.prioridad" :class="['prio-badge', getPrioClass(ticket.prioridad)]">
                                {{ ticket.prioridad }}
                            </span>
                            <span class="px-3 py-1 rounded text-sm font-bold"
                                :class="statusClass(ticket.estado)">
                                {{ ticket.estado }}
                            </span>
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-4 text-sm">
                        <div>
                            <span class="text-green-500">> Creado por: </span>
                            <span class="ml-2">{{ ticket.creadoPor?.nombre || ticket.creadoPor?.email || 'Usuario' }}</span>
                        </div>
                        <div>
                            <span class="text-green-500">> Email: </span>
                            <span class="ml-2">{{ ticket.creadoPor?.email || 'N/A' }}</span>
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
import Swal from 'sweetalert2';
import SoundService from '@/services/SoundService';
import ReportService from '@/services/ReportService';
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
                fechaActualizacion: '',
                usuario: null,
                comentarios: []
            },
            nuevoComentario: '',
            isSubmitting: false,
            loading: false,
            showUtilityMenu: false
        }
    },

    created() {
        this.verificarPermisos();
        this.cargarTicketCompleto();

        // Suscribirse a actualizaciones en tiempo real
        this.liveUpdateHandler = (data) => {
            console.log('>>> AdminTicketEstatus: Actualización detectada via Socket:', data);
            this.cargarTicketCompleto();
        };
        SocketService.on('TICKET_UPDATE', this.liveUpdateHandler);
    },

    beforeUnmount() {
        if (this.liveUpdateHandler) {
            SocketService.off('TICKET_UPDATE', this.liveUpdateHandler);
        }
    },

    methods: {
        verificarPermisos() {
            const userRole = localStorage.getItem('userRole');
            if (userRole !== 'ROLE_ADMIN') {
                Swal.fire({
                    title: '> Acceso Prohibido',
                    text: 'No tienes permisos de administrador',
                    icon: 'error',
                    background: '#000',
                    color: '#ff4444',
                    confirmButtonText: '> OK',
                    confirmButtonColor: '#333',
                    customClass: {
                        popup: 'border border-red-500 rounded-none',
                        title: 'font-mono'
                    }
                }).then(() => {
                    this.$router.push('/tickets');
                });
                return;
            }
        },

        async cargarTicketCompleto() {
            this.loading = true;
            try {
                const ticketId = this.$route.params.id;
                console.log('Ticket ID:', ticketId);

                const [ticketResponse, comentariosResponse] = await Promise.all([
                    api.get(`/admin/tickets/${ticketId}`),
                    api.get(`/tickets/${ticketId}/comentarios`)
                ]);

                console.log('ticket completo:', ticketResponse.data);

                //console.log('Ticket recibido:', ticketResponse.data);
                //console.log('Fecha creación tipo:', typeof ticketResponse.data?.fechaCreacion);
                //console.log('Fecha creacion valor:', ticketResponse.data?.fechaCreacion);

                this.ticket = {
                    ...ticketResponse.data,
                    comentarios: Array.isArray(comentariosResponse.data)
                        ? comentariosResponse.data
                        : []
                };

                console.log('Comentarios recibidos:', this.ticket.comentarios);

                if (this.ticket.comentarios && this.ticket.comentarios.length > 0) {
                    this.ticket.comentarios.forEach((comentario, index) => {
                        console.log(`Comentario ${index} fecha:`, comentario.fechaCreacion);
                    });
                }

                this.ordenarComentarios();

            } catch (error) {
                console.error("Error cargando ticket:", error);
                if (error.response?.status === 403) {
                    Swal.fire({
                        title: '> Error de Permisos',
                        text: 'No tienes permisos para ver este ticket',
                        icon: 'error',
                        background: '#000',
                        color: '#ff4444',
                        confirmButtonText: '> OK',
                        confirmButtonColor: '#333',
                        customClass: {
                            popup: 'border border-red-500 rounded-none'
                        }
                    }).then(() => {
                        this.$router.push('/admin/tickets');
                    });
                } else if (error.response?.status === 404) {
                    Swal.fire({
                        title: '> No Encontrado',
                        text: 'Ticket no encontrado',
                        icon: 'warning',
                        background: '#000',
                        color: '#ffaa00',
                        confirmButtonText: '> OK',
                        confirmButtonColor: '#333',
                        customClass: {
                            popup: 'border border-yellow-500 rounded-none'
                        }
                    }).then(() => {
                        this.$router.push('/admin/tickets');
                    });
                }
            } finally {
                this.loading = false;
            }
        },

        getUsuarioNombre() {
            if (!this.ticket.creadoPor) return 'Usuario desconocido';
            return this.ticket.cradoPor.nombre || this.ticket.creadoPor.email || 'Usuario';
        },

        getUsuarioEmail() {
            if (!this.ticket.usuario) return 'N/A';
            return this.ticket.creadoPor.email || 'N/A';
        },

        getComentarioAutorNombre(comentario) {
            if (!comentario.autor) return 'Usuario';

            return comentario.autor.nombre || comentario.autor.username || 'Usuario';
        },

        getComentarioAutorRol(comentario) {
            if (!comentario.autor) return 'Usuario';

            return comentario.autor.rol === 'ROLE_ADMIN' ? 'Admin' : 'Usuario';
        },

        ordenarComentarios() {
            if (!this.ticket.comentarios || this.ticket.comentarios.length === 0) return;

            this.ticket.comentarios.sort((a, b) => {
                const dateA = this.parseDate(a.fechaCreacion);
                const dateB = this.parseDate(b.fechaCreacion);

                //manejar fechas nulas
                if (!dateA || isNaN(dateA.getTime())) return 1;
                if (!dateB || isNaN(dateB.getTime())) return -1;

                return dateB - dateA; // Más recientes primero
            });
        },

        parseDate(dateData) {
            if (!dateData) {
                console.log('Fecha vacía');
                return null;
            }

            console.log('Parseando fecha:', dateData);

            try {
                if (Array.isArray(dateData)) {
                    console.log('Es un array:', dateData);
                    const [year, month, day, hour, minute] = dateData;
                    const date = new Date(year, month -1, day, hour, minute);
                    console.log('Fecha desde array:', date);
                    return date;
                }

                if (typeof dateData === 'string' && dateData.includes(',')) {
                    console.log('Es un string con comas:', dateData);
                    const parts = dateData.split(',').map(Number);
                    if (parts.length >= 6) {
                        const [year, month, day, hour, minute] = parts;
                        const date = new Date(year, month - 1, day, hour, minute);
                        console.log('Fecha desde String:', date);
                        return date;
                    }
                }

                const fallbackDate = new Date(dateData);
                console.log('Fecha fallback:', fallbackDate);

                if (isNaN(fallbackDate.getTime())) {
                    console.log('Fecha inválida:', dateData);
                    return null;
                }

                return fallbackDate;
            } catch (error) {
                console.error('Error parseando fecha:', error);
                return null;
            }
        },

        formatDate(dateData) {
            if (!dateData) {
                console.log('Fecha vacia');
                return 'N/A';
            }

            try {
                const date = this.parseDate(dateData);

                if (!date || isNaN(date.getTime())) {
                    console.log('Fecha invalida despues de parsear:', dateData);
                    return 'N/A';
                }

                const formatted = date.toLocaleDateString('es-MX', {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                });

                console.log('fecha formateada:', formatted);
                return formatted;
            } catch (error) {
                console.error('Error formateando fecha:', dateData, error);
                return 'N/A';
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

        getTagClass(categoria) {
            if (!categoria) return '';
            const map = {
                'HARDWARE': 'tag-hardware',
                'SOFTWARE': 'tag-software',
                'REDES': 'tag-redes',
                'OTROS': 'tag-otros'
            };
            return map[categoria] || 'tag-otros';
        },

        getPrioClass(prioridad) {
            if (!prioridad) return '';
            const map = {
                'BAJA': 'prio-baja',
                'MEDIA': 'prio-media',
                'ALTA': 'prio-alta',
                'CRITICA': 'prio-critica'
            };
            return map[prioridad] || '';
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
            const result = await Swal.fire({
                title: '> ¿Confirmar Cambio?',
                text: `¿Cambiar estado a ${nuevoEstado}?`,
                icon: 'question',
                showCancelButton: true,
                background: '#000',
                color: '#00ff41',
                confirmButtonText: '> SÍ, CAMBIAR',
                cancelButtonText: '> CANCELAR',
                confirmButtonColor: '#00aa00',
                cancelButtonColor: '#333',
                customClass: {
                    popup: 'border border-green-500 rounded-none',
                    confirmButton: 'font-mono',
                    cancelButton: 'font-mono'
                }
            });

            if (!result.isConfirmed) return;

            SoundService.playClick();
            try {
                await api.put(`/admin/tickets/${this.ticket.id}/estado`, {
                    estado: nuevoEstado
                });

                this.ticket.estado = nuevoEstado;
                this.ticket.fechaActualizacion = new Date().toISOString();

                SoundService.playSuccess();
                Swal.fire({
                    title: '> Estado Actualizado',
                    text: `Estado cambiado a ${nuevoEstado}`,
                    icon: 'success',
                    background: '#000',
                    color: '#00ff41',
                    timer: 1500,
                    showConfirmButton: false,
                    customClass: {
                        popup: 'border border-green-500 rounded-none shadow-[0_0_15px_rgba(0,255,65,0.3)]'
                    }
                });

            } catch (error) {
                SoundService.playError();
                console.error("Error cambiando estado:", error);
                Swal.fire({
                    title: '> Error',
                    text: "Error al cambiar estado: " + (error.response?.data?.message || error.message),
                    icon: 'error',
                    background: '#000',
                    color: '#ff4444',
                    confirmButtonText: '> OK',
                    confirmButtonColor: '#333',
                    customClass: {
                        popup: 'border border-red-500 rounded-none'
                    }
                });
            }
        },

        async agregarComentario() {
            if (this.ticket.estado === 'RESUELTO') {
                Swal.fire({
                    title: '> Ticket Resuelto',
                    text: "No se pueden agregar comentarios a tickets resueltos",
                    icon: 'warning',
                    background: '#000',
                    color: '#ffaa00',
                    confirmButtonText: '> OK',
                    confirmButtonColor: '#333',
                    customClass: {
                        popup: 'border border-yellow-500 rounded-none'
                    }
                });
                return;
            }

            this.isSubmitting = true;
            SoundService.playClick();
            try {
                const response = await api.post(`/tickets/${this.ticket.id}/comentarios`, {
                    contenido: this.nuevoComentario
                });

                // Agregar el nuevo comentario a la lista
                this.ticket.comentarios.unshift(response.data);
                this.ordenarComentarios();
                this.nuevoComentario = '';

                SoundService.playSuccess();
                Swal.fire({
                    title: '> Éxito',
                    text: "Comentario agregado exitosamente",
                    icon: 'success',
                    background: '#000',
                    color: '#00ff41',
                    timer: 1500,
                    showConfirmButton: false,
                    customClass: {
                        popup: 'border border-green-500 rounded-none shadow-[0_0_15px_rgba(0,255,65,0.3)]'
                    }
                });

            } catch (error) {
                SoundService.playError();
                console.error("Error agregando comentario:", error);
                Swal.fire({
                    title: '> Error',
                    text: "Error al agregar comentario: " + (error.response?.data?.message || error.message),
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
        },

        calcularTiempoActivo() {
            if (!this.ticket.fechaCreacion) return 'N/A';

            const creacion = this.parseDate(this.ticket.fechaCreacion);

            if (!creacion || isNaN(creacion.getTime())) {
                return 'Fecha inválida';
            }

            const ahora = new Date();
            const diffMs = ahora - creacion;
            const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
            const diffHours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));

            if (diffDays > 0) {
                return `${diffDays}d ${diffHours}h`;
            } else if (diffHours > 0) {
                return `${diffHours}h ${diffMinutes}m`;
            } else {
                return `${diffMinutes}m`;
            }
        },

        handleLogout() {
            localStorage.clear();
            this.$router.push('/login');
        },

        toggleUtilityMenu() {
            this.showUtilityMenu = !this.showUtilityMenu;
        },

        async generarReporte() {
            this.showUtilityMenu = false;
            Swal.fire({
                title: '> RECOPILANDO LOGS',
                text: 'Interceptando paquetes de datos...',
                allowOutsideClick: false,
                didOpen: () => Swal.showLoading(),
                background: '#000',
                color: '#00ff41',
                customClass: { popup: 'border border-green-500 rounded-none' }
            });

            try {
                const [ticketsRes, usersRes] = await Promise.all([
                    api.get('/admin/tickets'),
                    api.get('/admin/usuarios')
                ]);

                const allTickets = ticketsRes.data || [];
                const users = usersRes.data || [];

                const stats = {
                    totalUsers: users.length,
                    totalAdmins: users.filter(u => u.rol === 'ROLE_ADMIN').length,
                    totalRegularUsers: users.filter(u => u.rol === 'ROLE_USER').length,
                    activeUsers: users.filter(u => u.activo).length,
                    inactiveUsers: users.filter(u => !u.activo).length,
                    openTickets: allTickets.filter(t => t.estado === 'ABIERTO').length,
                    inProgressTickets: allTickets.filter(t => t.estado === 'EN_PROGRESO').length,
                    resolvedTickets: allTickets.filter(t => t.estado === 'RESUELTO').length,
                    totalTickets: allTickets.length
                };

                await ReportService.generateSystemReport(stats, allTickets);

                Swal.fire({
                    title: '> DATA EXFILTRADA',
                    text: 'El reporte de simulación se ha descargado correctamente.',
                    icon: 'success',
                    background: '#000',
                    color: '#00ff41',
                    customClass: { popup: 'border border-green-500 rounded-none shadow-[0_0_15px_rgba(0,255,65,0.3)]' }
                });
            } catch (error) {
                console.error("Error en reporte:", error);
                Swal.fire({
                    title: '> FALLO DE INTERCEPCIÓN',
                    text: 'No se pudo generar el reporte de estado.',
                    icon: 'error',
                    background: '#000',
                    color: '#ff0000',
                    customClass: { popup: 'border border-red-500 rounded-none' }
                });
            }
        }
    }
}
</script>

<style scoped>
.btn-matrix {
    background-color: rgba(0, 255, 65, 0.2);
    color: #4ade80; /* text-green-400 */
    border: 1px solid #22c55e; /* border-green-500 */
    padding: 0.5rem 1rem;
    border-radius: 0.25rem;
    cursor: pointer;
    transition: background-color 0.3s ease;
}
.btn-matrix:hover:not(:disabled) {
    background-color: rgba(34, 197, 94, 0.3); /* hover:bg-green-500/30 */
}
.btn-matrix:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.btn-status {
    padding: 0.5rem 1rem;
    border-radius: 0.25rem;
    border: 1px solid transparent;
    font-size: 0.875rem;
    font-weight: 500;
    transition: background-color 0.3s ease;
    cursor: pointer;
}
.btn-status:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.cursor-blink {
    animation: blink 1s step-end infinite;
}

@keyframes blink {
    from, to { opacity: 1; }
    50% { opacity: 0; }
}

.terminal-box {
    border: 1px solid #22c55e; /* border-green-500 */
    padding: 1.5rem;
    border-radius: 0.5rem;
}

.history-entry {
    transition: all 0.3s ease;
}

.history-entry:hover {
    background-color: rgba(16, 185, 129, 0.05);
}
/* Animaciones del Menú */
.menu-enter-active, .menu-leave-active {
  transition: all 0.3s ease;
}
.menu-enter-from, .menu-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>