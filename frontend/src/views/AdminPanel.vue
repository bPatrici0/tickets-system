<template>
  <div class="min-h-screen bg-black p-4">
    <header class="terminal-box mb-6">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl">
          > Panel Admin 
          <span class="text-sm text-green-500/60 font-mono ml-2">[{{ userEmail }}]</span>
          <span class="cursor-blink">|</span>
        </h1>
        <div class="relative">
          <button
            @click="toggleMenu"
            class="btn-matrix text-sm"
          >
            > Opciones [{{ showMenu ? '-' : '+' }}]
          </button>
          
          <!-- Menú desplegable -->
          <transition name="menu">
            <div v-if="showMenu" class="absolute right-0 mt-2 w-48 bg-black border border-green-500 rounded shadow-[0_0_15px_rgba(0,255,65,0.3)] z-50 overflow-hidden">
              <button @click="generarReporte" class="w-full text-left px-4 py-2 text-green-400 hover:bg-green-500/20 transition-colors border-b border-green-900/50">
                > Generar Reporte PDF
              </button>
              <button @click="$router.push('/admin/metrics')" class="w-full text-left px-4 py-2 text-blue-400 hover:bg-blue-500/20 transition-colors border-b border-green-900/50">
                > Métricas de Equipo
              </button>
              <button @click="handleLogout" class="w-full text-left px-4 py-2 text-red-500 hover:bg-red-500/10 transition-colors">
                > Cerrar sesión
              </button>
            </div>
          </transition>
        </div>
      </div>
    </header>

    <!-- Admin Dashboard -->
    <AdminDashboard :tickets="tickets" :users="users" />


    <!-- Estadísticas -->
    <div class="terminal-box p-4">
        <h2 class="text-xl mb-4">> Estadísticas<span class="cursor-blink"></span></h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div class="text-center p-3 border border-green-500 rounded">
                <div class="text-2xl text-green-400">{{ users.length }}</div>
                <div class="text-xs text-green-500">Usuarios</div>
            </div>
            <div class="text-center p-3 border border-green-500 rounded">
                <div class="text-2xl text-yellow-400">{{ ticketsAbiertos }}</div>
                <div class="text-xs text-green-500">Abiertos</div>
            </div>
            <div class="text-center p-3 border border-green-500 rounded">
                <div class="text-2xl text-blue-400">{{ ticketsEnProgreso }}</div>
                <div class="text-xs text-green-500">En progreso</div>
            </div>
            <div class="text-center p-3 border border-green-500 rounded">
                <div class="text-2xl text-green-400">{{ ticketsResueltos }}</div>
                <div class="text-xs text-green-500">Resueltos</div>
            </div>
        </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6 py-4">
      <!-- Sección Usuarios -->
      <div class="terminal-box p-4">
        <h2 class="text-xl mb-4 cursor-pointer hover:text-green-300" @click="$router.push('/admin/AdminPanelUser')">
            > Usuarios <span class="text-green-400">({{ filteredUsers.length }})/({{ users.length }})</span><span class="cursor-blink"></span>
        </h2>

        <!--filtro por rol-->
        <div class="flex items-center space-x-2">
            <label class="text-green-400 text-sm">Filtrar: </label>
            <select v-model="filtroRol" class="bg-black border border-green-500 text-green-400 px-2 py-1 rounded text-sm">
                <option value="TODOS">Todos</option>
                <option value="ROLE_ADMIN">Administradores</option>
                <option value="ROLE_USER">Usuarios</option>
            </select>
        </div>

        <!-- estado de carga -->
        <div v-if="loadingUsers" class="text-green-500 text-center py-4">
          > Cargando usuarios...
        </div>

        <!-- estado -->
        <div v-else-if="filteredUsers.length === 0" class="text-gray-500 text-center py-4">
          > No hay usuarios {{ filtroMensaje }}
        </div>

        <!-- Lista de usuarios -->
        <div v-else class="space-y-3 max-h-96 overflow-y-auto">
          <div v-for="user in filteredUsers" :key="user.id" class="p-3 border border-green-500 rounded">
            <div class="flex justify-between items-center mb-2">
                <div>
                    <span class="text-green-300 font-mono">{{ user.email }}</span>
                    <span class="text-xs text-green-500">ID: {{ user.id }}</span>
                </div>
                <label class="switch">
                    <input type="checkbox" :checked="user.rol === 'ROLE_ADMIN'" @change="toggleRole(user)" :disabled="updatingUser === user.id">
                    <span class="slider round"></span>
                </label>
            </div>
            <div class="flex justify-between items-center text-xs">
                <span class="text-green-500">Rol: {{ user.rol }}</span>
                <span :class="user.rol === 'ROLE_ADMIN' ? 'text-yellow-400' : 'text-blue-400'">
                    {{ user.rol === 'ROLE_ADMIN' ? 'Administrador' : 'Usuario' }}
                </span>
            </div>
            <div v-if="updatingUser === user.id" class="text-green-400 text-xs mt-2">
              > Actualizando...
            </div>
          </div>
        </div>

        <button @click="fetchUsers" class="btn-matrix mt-4 text-sm" :disabled="loadingUsers">
          > Actualizar lista
        </button>

        <!--estadisticas de usuarios-->
        <div class="mt-4 p-3 border border-green-500 rounded text-xs text-xs text-green-400">
            <div>> Estadísticas de usuarios: </div>
            <div class="grid grid-cols-2 gap-2 mt-2">
                <div class="text-center p-2 bg-green-500/20 rounded">
                    <div class="text-green-400 font-bold">{{ usuariosCount }}</div>
                    <div>Usuarios</div>
                </div>
                <div class="text-center p-2 bg-yellow-500/20 rounded">
                    <div class="text-yellow-400 font-bold">{{ administradoresCount }}</div>
                    <div>Administradores</div>
                </div>
            </div>
        </div>
      </div>

      <!-- Sección Tickets -->
      <div class="terminal-box p-4">
        <h2 class="text-xl mb-4 cursor-pointer hover:text-green-300" @click="$router.push('/admin/tickets')">
            > Tickets <span class="text-green-400">({{ tickets.length }})</span><span class="cursor-blink"></span>
        </h2>

        <!-- estado de carga -->
        <div v-if="loadingTickets" class="text-green-500 text-center py-4">
          > Cargando tickets...
        </div>

        <!-- estado -->
        <div v-else-if="tickets.length === 0" class="text-gray-500 text-center py-4">
          > No hay tickets
        </div>

        <!-- Lista de tickets -->
        <div v-else class="space-y-3 max-h-96 overflow-y-auto">
          <div v-for="ticket in tickets" :key="ticket.id"
               class="p-3 border border-green-500 rounded hover:bg-green-900/10 cursor-pointer"
               :class="{ 'row-critica': ticket.prioridad === 'CRITICA' }"
               @click="verTicket(ticket.id)">
            <div class="flex justify-between items-center mb-2">
              <span class="text-green-300">
                #{{ ticket.id }} - {{ ticket.titulo }}
                <span v-if="ticket.categoria" :class="['tag-badge', getTagClass(ticket.categoria)]">
                  [{{ ticket.categoria }}]
                </span>
                <span v-if="ticket.prioridad" :class="['prio-badge', getPrioClass(ticket.prioridad)]">
                  {{ ticket.prioridad }}
                </span>
              </span>
              <span class="text-xs px-2 py-1 rounded" :class="statusClass(ticket.estado)">
                {{ ticket.estado }}
              </span>
            </div>
            <div class="text-xs text-green-500 space-y-1">
              <div>Por: {{ ticket.creadoPor?.email || 'Usuario desconocido' }}</div>
              <div>Creado: {{ formatDate(ticket.fechaCreacion) }}</div>
            </div>
          </div>
        </div>

        <button
          @click="fetchTickets"
          class="btn-matrix mt-4 text-sm"
          :disabled="loadingTickets"
        >
          > Actualizar tickets
        </button>
      </div>
    </div>

    <!-- Sección de Auditoría (Línea de Tiempo) -->
    <div class="terminal-box p-4 mt-6">
      <h2 class="text-xl mb-4 text-green-400">> Historial de Operaciones (Auditoría)<span class="cursor-blink"></span></h2>
      
      <div v-if="loadingAudit" class="text-green-500 text-center py-4">> Consultando registros...</div>
      
      <div v-else-if="auditLogs.length === 0" class="text-gray-500 text-center py-4">> No hay registros de actividad reciente</div>
      
      <div v-else class="space-y-2 max-h-60 overflow-y-auto pr-2">
        <div v-for="log in auditLogs" :key="log.id" class="text-xs border-l-2 border-green-900 pl-3 py-1 hover:bg-green-500/5 transition-colors">
          <div class="flex justify-between text-green-500">
            <span>[{{ formatDate(log.fecha) }}]</span>
            <span class="text-green-300 font-bold">{{ log.accion }}</span>
          </div>
          <div class="text-gray-400 mt-1">
            <span class="text-green-600">Usuario:</span> {{ log.usuario }} | 
            <span class="text-green-600">Ticket:</span> #{{ log.ticketId }}
          </div>
          <div v-if="log.valorAnterior || log.valorNuevo" class="text-green-400/70 italic mt-0.5">
            {{ log.valorAnterior || 'NULL' }} >> {{ log.valorNuevo }}
          </div>
          <div v-if="log.detalles" class="text-gray-500 mt-0.5">> {{ log.detalles }}</div>
        </div>
      </div>
      
      <button @click="fetchAuditLogs" class="btn-matrix mt-4 text-sm" :disabled="loadingAudit">
        > Actualizar historial
      </button>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import Swal from 'sweetalert2';
import AdminDashboard from '../components/AdminDashboard.vue';
import ReportService from '@/services/ReportService';
import SocketService from '@/services/SocketService';



export default {
  components: {
    AdminDashboard
  },
  data() {

    return {
      users: [],
      tickets: [],
      loadingTickets: false,
      loadingAudit: false,
      updatingUser: null,
      filtroRol: 'TODOS',
      showMenu: false,
      auditLogs: [],
      userEmail: ''
    }
  },

  computed: {
    filteredUsers() {
        if (this.filtroRol === 'TODOS') {
            return this.users;
        }
        return this.users.filter(user => user.rol === this.filtroRol);
    },

    filtroMensaje() {
        switch (this.filtroRol) {
            case 'ROLE_ADMIN': return 'administradores';
            case 'ROLE_USER': return 'usuarios';
            default: return '';
        }
    },

    usuariosCount() {
        return this.users.filter(user => user.rol === 'ROLE_USER').length;
    },

    administradoresCount() {
        return this.users.filter(user => user.rol === 'ROLE_ADMIN').length;
    },

    ticketsAbiertos() {
      return this.tickets.filter(t => t.estado === 'ABIERTO').length;
    },
    ticketsEnProgreso() {
      return this.tickets.filter(t => t.estado === 'EN_PROGRESO').length;
    },
    ticketsResueltos() {
      return this.tickets.filter(t => t.estado === 'RESUELTO').length;
    }
  },

  created() {
    const userRole = localStorage.getItem('userRole');
    this.userEmail = localStorage.getItem('userEmail') || 'Admin';
    console.log('Rol del usuario:', userRole);

    if (userRole !== 'ROLE_ADMIN') {
        this.verificarPermisos(userRole);
        return;
    }

    console.log('Acceso permitido, cargando datos...');
    this.fetchUsers();
    this.fetchTickets();
    this.fetchAuditLogs();

    // Suscribirse a actualizaciones en tiempo real con binding correcto
    this.liveUpdateHandler = this.handleLiveUpdate.bind(this);
    SocketService.on('TICKET_UPDATE', this.liveUpdateHandler);
  },

  beforeUnmount() {
    // Limpiar suscripción para evitar fugas de memoria
    if (this.liveUpdateHandler) {
        SocketService.off('TICKET_UPDATE', this.liveUpdateHandler);
    }
  },

  methods: {
    async fetchUsers() {
      this.loadingUsers = true;
      try {
        console.log('Intentando cargar usuarios...');

        const response = await api.get('/admin/usuarios');

        console.log('Respuesta recibida:', response);
        this.users = response.data || [];
        console.log('Usuarios cargados:', this.users.length);

      } catch (error) {
        console.error("Error fetching users: ", error);

        if (error.response) {
            console.error('Status error:', error.response.status);
            console.error('Data error:', error.response.data);

            if (error.response.status === 403) {
                Swal.fire({
                    title: '> Acceso Denegado',
                    text: 'No tienes permisos de administrador',
                    icon: 'error',
                    background: '#000',
                    color: '#ff4444',
                    confirmButtonText: '> OK',
                    confirmButtonColor: '#333',
                    customClass: {
                        popup: 'border border-red-500 rounded-none'
                    }
                });
            } else if (error.response.status === 401) {
                Swal.fire({
                    title: '> Sesión Expirada',
                    text: 'Sesión expirada. Por favor inicia sesión nuevamente.',
                    icon: 'warning',
                    background: '#000',
                    color: '#ffaa00',
                    confirmButtonText: '> OK',
                    confirmButtonColor: '#333',
                    customClass: {
                        popup: 'border border-yellow-500 rounded-none'
                    }
                }).then(() => {
                    this.handleLogout();
                });
            } else if (error.response.status === 404) {
                Swal.fire({
                    title: '> Error 404',
                    text: 'Endpoint no encontrado. Verifica la configuración del backend!...',
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
        }

        this.users = [];
      } finally {
        this.loadingUsers = false;
      }
    },

    async fetchTickets() {
      this.loadingTickets = true;
      try {
        const response = await api.get('/tickets');
        
        // Ordenar por fecha de creación (Reciente a antiguo)
        const ticketsData = response.data || [];
        this.tickets = ticketsData.sort((a, b) => {
            return this.getTimestamp(b.fechaCreacion) - this.getTimestamp(a.fechaCreacion);
        });

        console.log('Tickets cargados:', this.tickets.length);
      } catch (error) {
        console.error("Error fetching tickets: ", error);
        Swal.fire({
            title: '> Error',
            text: "Error al cargar tickets",
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
        this.loadingTickets = false;
      }
    },

    async fetchAuditLogs() {
      this.loadingAudit = true;
      try {
        const response = await api.get('/auditoria/recientes');
        this.auditLogs = response.data || [];
        console.log('Registros de auditoría cargados:', this.auditLogs.length);
      } catch (error) {
        console.error("Error fetching audit logs:", error);
      } finally {
        this.loadingAudit = false;
      }
    },

    getTimestamp(dateData) {
        if (!dateData) return 0;
        if (Array.isArray(dateData)) {
            const [year, month, day, hours, minutes] = dateData;
            return new Date(year, month - 1, day, hours, minutes).getTime();
        }
        return new Date(dateData).getTime();
    },

    async toggleRole(user) {
      this.updatingUser = user.id;
      try {
        const newRole = user.rol === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN';

        await api.put(`/admin/usuarios/${user.id}/role`, {
          role: newRole
        });

        user.rol = newRole;

        const mensaje = newRole === 'ROLE_ADMIN'
            ? 'Usuario modificado a Administrador'
            : 'Usuario modificado a Usuario normal';
        
        // Success Notification (Matrix Theme)
        Swal.fire({
            title: '> Sistema Actualizado',
            text: mensaje,
            icon: 'success',
            background: '#000',
            color: '#00ff41',
            confirmButtonText: '> OK',
            confirmButtonColor: '#00aa00',
            customClass: {
                popup: 'border border-green-500 rounded-none shadow-[0_0_15px_rgba(0,255,65,0.3)]',
                title: 'font-mono',
                confirmButton: 'font-mono'
            },
            timer: 2000,
            timerProgressBar: true
        });

      } catch (error) {
        console.error("Error updating role: ", error);
        
        Swal.fire({
            title: '> Error',
            text: "Error al cambiar rol: " + (error.response?.data?.message || error.message),
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
      } finally {
        this.updatingUser = null;
      }
    },
    
    verTicket(ticketId) {
      this.$router.push(`/admin/tickets/${ticketId}`);
    },

    verificarPermisos(userRole) {
         if (userRole !== 'ROLE_ADMIN') {
            Swal.fire({
                title: '> Acceso Denegado',
                text: 'No tienes permisos de administrador',
                icon: 'warning',
                background: '#000',
                color: '#ffff00',
                confirmButtonText: '> Volver',
                confirmButtonColor: '#333',
                customClass: {
                    popup: 'border border-yellow-500 rounded-none',
                    title: 'font-mono',
                    confirmButton: 'font-mono'
                }
            }).then(() => {
                this.$router.push('/tickets');
            });
         }
    },

    formatDate(dateData) {
      if (!dateData) return '[Fecha no disponible]';

      try {
        let date;

        if (Array.isArray(dateData)) {
          const [year, month, day, hours, minutes] = dateData;
          date = new Date(year, month - 1, day, hours, minutes);
        } else if (typeof dateData === 'string') {
          date = new Date(dateData);
        } else {
          return '[Formato inválido]';
        }

        if (isNaN(date.getTime())) {
          return '[Fecha inválida]';
        }

        return date.toLocaleString('es-MX', {
          day: '2-digit',
          month: 'short',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
          hour12: false
        }).replace(/,/g, '');

      } catch (error) {
        return '[Error fecha]';
      }
    },

    handleLiveUpdate(data) {
        console.log('>>> Actualización en vivo recibida:', data);
        // Recargar datos suavemente
        this.fetchUsers();
        this.fetchTickets();
        this.fetchAuditLogs();
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

    toggleMenu() {
        this.showMenu = !this.showMenu;
    },

    async generarReporte() {
        this.showMenu = false;
        try {
            const stats = {
                totalUsers: this.users.length,
                totalAdmins: this.administradoresCount,
                totalRegularUsers: this.usuariosCount,
                activeUsers: this.users.filter(u => u.activo).length,
                inactiveUsers: this.users.filter(u => !u.activo).length,
                openTickets: this.ticketsAbiertos,
                inProgressTickets: this.ticketsEnProgreso,
                resolvedTickets: this.ticketsResueltos,
                totalTickets: this.tickets.length
            };
            
            
            // Consultar métricas de rendimiento para el reporte
            let performanceData = [];
            try {
                const perfResponse = await api.get('/auditoria/performance');
                performanceData = perfResponse.data || [];
            } catch (err) {
                console.warn('No se pudieron obtener métricas para el reporte:', err);
            }

            await ReportService.generateSystemReport(stats, this.tickets, this.auditLogs, performanceData);
            
            Swal.fire({
                title: '> REPORTE GENERADO',
                text: 'La simulación de datos se ha exportado correctamente',
                icon: 'success',
                background: '#000',
                color: '#00ff41',
                customClass: {
                    popup: 'border border-green-500 rounded-none shadow-[0_0_15px_rgba(0,255,65,0.3)]'
                }
            });
        } catch (error) {
            console.error('Error generando reporte:', error);
            Swal.fire({
                title: '> ERROR DE SISTEMA',
                text: 'No se pudo generar el reporte. Intente más tarde.',
                icon: 'error',
                background: '#000',
                color: '#ff0000',
                customClass: {
                    popup: 'border border-red-500 rounded-none'
                }
            });
        }
    },

    handleLogout() {
      localStorage.clear();
      this.$router.push('/login');
    }
  }
}
</script>

<style scoped>

/*estilo para el filtro*/
select {
    background-color: #000;
    border: 1px solid #00FF41;
    color: #00FF41;
    padding: 0.25rem 0.5rem;
    border-radius: 0.25;
    outline: none;
}
select:focus {
    border-color: #00FF41;
    box-shadow: 0 0 0 2px rgba(0, 255, 65, 0.2);
}
/*Estilos para las card de usuario*/
.border-green-500 {
    border-color: #00FF41;
}
.bg-green-900\/20 {
    background-color: rgba(0, 255, 65, 0.1);
}
.bg-yellow-500\/20 {
    background-color: rgba(255, 193, 7, 0.1);
}
/*efecto hover para las cards*/
.space-y-3 > div:hover {
    background-color: rgba(0, 255, 255, 0.05);
    /*transform: translateX(2px);*/
    transition: all 0.2s ease;
}

.terminal-box {
  border: 1px solid #00FF41;
  border-radius: 0.5rem;
  padding: 1rem;
}

.btn-matrix {
  background-color: rgba(0, 255, 65, 0.2);
  color: #00ff41;
  border: 1px solid #00ff41;
  padding: 0.25rem 0.75rem;
  transition: all 0.3s ease;
}
.btn-matrix:hover:not(:disabled) {
  background-color: rgba(0, 255, 65, 0.3);
}
.btn-matrix:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Animaciones del Menú */
.menu-enter-active, .menu-leave-active {
  transition: all 0.3s ease;
}
.menu-enter-from, .menu-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #00FF41;
}

input:checked + .slider:before {
  transform: translateX(26px);
}

.cursor-blink {
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

/* Scrollbar personalizado */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #000;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background-color: #00FF41;
  border-radius: 3px;
}
</style>