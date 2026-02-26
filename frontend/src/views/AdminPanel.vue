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
              <button @click="exportarBBDD" class="w-full text-left px-4 py-2 text-green-400 hover:bg-green-500/20 transition-colors border-b border-green-900/50">
                > Exportar Copia BBDD (.sql)
              </button>
              <button @click="importarBBDD" class="w-full text-left px-4 py-2 text-yellow-400 hover:bg-yellow-500/20 transition-colors border-b border-green-900/50">
                > Importar/Restaurar BBDD (.sql)
              </button>
              <button 
                @click="purgarSistema" 
                :disabled="userEmail !== 'admin@devops.com'"
                :class="userEmail === 'admin@devops.com' 
                  ? 'w-full text-left px-4 py-2 text-red-500 hover:bg-red-500/20 transition-colors border-b border-green-900/50' 
                  : 'w-full text-left px-4 py-2 text-gray-600 cursor-not-allowed border-b border-green-900/50'"
              >
                > Purgar Sistema {{ userEmail !== 'admin@devops.com' ? '[BLOQUEADO]' : '[☠]' }}
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

        <!-- Filtros -->
        <div class="flex flex-col space-y-3 mb-4">
            <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-green-500 font-mono text-xs">> BUSCAR:</span>
                <input 
                    v-model="busquedaUsuario" 
                    type="text" 
                    placeholder="NOMBRE O IDENTIFICADOR..."
                    class="w-full bg-black border border-green-500 text-green-400 pl-24 pr-3 py-2 rounded text-xs outline-none focus:ring-1 focus:ring-green-400"
                >
            </div>
            <div class="flex items-center space-x-2">
                <label class="text-green-400 text-sm font-mono">> CATEGORÍA: </label>
                <select v-model="filtroRol" class="bg-black border border-green-500 text-green-400 px-2 py-1 rounded text-sm outline-none">
                    <option value="TODOS">Todos</option>
                    <option value="ROLE_ADMIN">Administrador</option>
                    <option value="ROLE_USER">Usuario</option>
                </select>
            </div>
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
        <div v-else class="space-y-3 h-[520px] overflow-y-auto mb-4 pr-1">
          <div v-for="user in paginatedUsers" :key="user.id" 
               class="p-3 border border-green-500 rounded hover:bg-green-500/5 cursor-pointer transition-all"
               @click="verUsuario(user.id)">
            <div class="flex justify-between items-center mb-2">
                <div>
                    <span class="text-green-300 font-mono">{{ user.email }}</span>
                    <span class="text-xs text-green-500">ID: {{ user.id }}</span>
                </div>
                <label class="switch" @click.stop>
                    <input type="checkbox" :checked="user.rol === 'ROLE_ADMIN'" @change="toggleRole(user)" :disabled="updatingUser === user.id || user.id === 1">
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
        <!-- Paginación de usuarios -->
        <div v-if="filteredUsers.length > usuariosPorPagina" class="flex justify-between items-center mt-2 px-1">
            <button 
                @click="paginaUsuarios > 1 ? paginaUsuarios-- : null" 
                class="btn-matrix text-[10px] px-2 py-1"
                :disabled="paginaUsuarios === 1"
            >
                « ANTERIOR
            </button>
            <span class="text-[10px] text-green-500 font-mono">
                PÁGINA {{ paginaUsuarios }} DE {{ totalPaginasUsuarios }}
            </span>
            <button 
                @click="paginaUsuarios < totalPaginasUsuarios ? paginaUsuarios++ : null" 
                class="btn-matrix text-[10px] px-2 py-1"
                :disabled="paginaUsuarios === totalPaginasUsuarios"
            >
                SIGUIENTE »
            </button>
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
            > Tickets <span class="text-green-400">({{ filteredTickets.length }})/({{ tickets.length }})</span><span class="cursor-blink"></span>
        </h2>

        <!-- Filtro Búsqueda Tickets -->
        <div class="mb-4">
            <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-green-500 font-mono text-xs">> BUSCAR_ENC:</span>
                <input 
                    v-model="busquedaTicket" 
                    type="text" 
                    placeholder="ID, TÍTULO O EMAIL..."
                    class="w-full bg-black border border-green-500 text-green-400 pl-32 pr-3 py-2 rounded text-xs outline-none focus:ring-1 focus:ring-green-400"
                >
            </div>
        </div>

        <!-- estado de carga -->
        <div v-if="loadingTickets" class="text-green-500 text-center py-4">
          > Cargando tickets...
        </div>

        <!-- estado -->
        <div v-else-if="tickets.length === 0" class="text-gray-500 text-center py-4">
          > No hay tickets
        </div>

        <!-- Lista de tickets -->
        <div v-else class="space-y-3 h-[520px] overflow-y-auto mb-4 pr-1">
          <div v-for="ticket in paginatedTickets" :key="ticket.id"
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

        <!-- Paginación de tickets -->
        <div v-if="filteredTickets.length > ticketsPorPagina" class="flex justify-between items-center mt-2 px-1">
            <button 
                @click="paginaTickets > 1 ? paginaTickets-- : null" 
                class="btn-matrix text-[10px] px-2 py-1"
                :disabled="paginaTickets === 1"
            >
                « ANTERIOR
            </button>
            <span class="text-[10px] text-green-500 font-mono">
                PÁGINA {{ paginaTickets }} DE {{ totalPaginasTickets }}
            </span>
            <button 
                @click="paginaTickets < totalPaginasTickets ? paginaTickets++ : null" 
                class="btn-matrix text-[10px] px-2 py-1"
                :disabled="paginaTickets === totalPaginasTickets"
            >
                SIGUIENTE »
            </button>
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
      userEmail: '',
      paginaUsuarios: 1,
      usuariosPorPagina: 5,
      paginaTickets: 1,
      ticketsPorPagina: 5,
      busquedaUsuario: '',
      busquedaTicket: ''
    }
  },

  watch: {
    filtroRol() {
      this.paginaUsuarios = 1;
    },
    busquedaUsuario() {
      this.paginaUsuarios = 1;
    },
    busquedaTicket() {
      this.paginaTickets = 1;
    }
  },

  computed: {
    filteredUsers() {
        let result = this.users;

        // Filtro por rol
        if (this.filtroRol !== 'TODOS') {
            result = result.filter(user => user.rol === this.filtroRol);
        }

        // Filtro por búsqueda
        if (this.busquedaUsuario.trim()) {
            const query = this.busquedaUsuario.toLowerCase();
            result = result.filter(user => 
                (user.nombre && user.nombre.toLowerCase().includes(query)) || 
                (user.email && user.email.toLowerCase().includes(query))
            );
        }

        return result;
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

    totalPaginasUsuarios() {
        return Math.ceil(this.filteredUsers.length / this.usuariosPorPagina) || 1;
    },

    paginatedUsers() {
        const inicio = (this.paginaUsuarios - 1) * this.usuariosPorPagina;
        return this.filteredUsers.slice(inicio, inicio + this.usuariosPorPagina);
    },

    filteredTickets() {
        if (!this.busquedaTicket.trim()) return this.tickets;
        const query = this.busquedaTicket.toLowerCase();
        return this.tickets.filter(t => 
            t.id.toString().includes(query) ||
            (t.titulo && t.titulo.toLowerCase().includes(query)) ||
            (t.creadoPor?.email && t.creadoPor.email.toLowerCase().includes(query))
        );
    },

    totalPaginasTickets() {
        return Math.ceil(this.filteredTickets.length / this.ticketsPorPagina) || 1;
    },

    paginatedTickets() {
        const inicio = (this.paginaTickets - 1) * this.ticketsPorPagina;
        return this.filteredTickets.slice(inicio, inicio + this.ticketsPorPagina);
    },

    ticketsAbiertos() {
      return this.filteredTickets.filter(t => t.estado === 'ABIERTO').length;
    },
    ticketsEnProgreso() {
      return this.filteredTickets.filter(t => t.estado === 'EN_PROGRESO').length;
    },
    ticketsResueltos() {
      return this.filteredTickets.filter(t => t.estado === 'RESUELTO').length;
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

    async exportarBBDD() {
      this.showOptions = false;

      Swal.fire({
        title: '> INICIANDO BACKUP',
        text: 'Extrayendo datos del núcleo de la base de datos...',
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading(), 
        background: '#000',
        color: '#00ff41',
        customClass: { popup: 'border border-blue-500 rounded-none' }
      });

      try {
        const response = await api.get('/admin/db/export', {
          responseType: 'blob' 
        });

        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `backup_db_${new Date().getTime()}.sql`);
        document.body.appendChild(link);
        link.click();

        Swal.fire({
          title: '> Éxito',
          text: 'Base de datos exportada y descargada correctamente!!!...',
          icon: 'success',
          background: '#000',
          color: '#00ff41',
          customClass: {popup: 'border border-green-500 rounded-none' }
        });
      } catch (error) {
        console.error("Error en exportación:", error);
        Swal.fire({
          title: '> FALLO CRÍTICO',
          text: 'No se pudo completar la exportacion de la base de datos.',
          icon: 'error',
          background: '#000',
          color: '#ff0000',
          customClass: { popup: 'border border-red-500 rounded-none' }
        });
      } 
    },

    async importarBBDD() {
      this.showMenu = false;

      // Primera confirmación
      const confirm1 = await Swal.fire({
        title: '> ADVERTENCIA',
        html: '<p style="color:#ff0;">Esta operación <b>REEMPLAZARÁ</b> todos los datos actuales de la base de datos con los del archivo seleccionado.</p><p style="color:#ff0; margin-top:8px;">Se recomienda <b>exportar una copia de seguridad</b> antes de continuar.</p>',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '> CONTINUAR',
        cancelButtonText: '> CANCELAR',
        background: '#000',
        color: '#ffaa00',
        confirmButtonColor: '#ff8800',
        cancelButtonColor: '#333',
        customClass: { popup: 'border border-yellow-500 rounded-none' }
      });

      if (!confirm1.isConfirmed) return;

      // Selector de archivo
      const fileInput = document.createElement('input');
      fileInput.type = 'file';
      fileInput.accept = '.sql';

      fileInput.onchange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        // Validar extensión
        if (!file.name.toLowerCase().endsWith('.sql')) {
          Swal.fire({
            title: '> ERROR',
            text: 'Solo se aceptan archivos con extensión .sql',
            icon: 'error',
            background: '#000',
            color: '#ff0000',
            customClass: { popup: 'border border-red-500 rounded-none' }
          });
          return;
        }

        // Segunda confirmación con nombre del archivo
        const confirm2 = await Swal.fire({
          title: '> CONFIRMAR RESTAURACIÓN',
          html: `<p style="color:#ff0;">Archivo seleccionado:</p><p style="color:#00ff41; font-family:monospace;"><b>${file.name}</b> (${(file.size / 1024).toFixed(1)} KB)</p><p style="color:#ff0000; margin-top:12px;"><b>¿Estás SEGURO de que deseas restaurar esta base de datos?</b></p>`,
          icon: 'warning',
          showCancelButton: true,
          confirmButtonText: '> SÍ, RESTAURAR',
          cancelButtonText: '> CANCELAR',
          background: '#000',
          color: '#ff4444',
          confirmButtonColor: '#cc0000',
          cancelButtonColor: '#333',
          customClass: { popup: 'border border-red-500 rounded-none' }
        });

        if (!confirm2.isConfirmed) return;

        // Mostrar loading
        Swal.fire({
          title: '> RESTAURANDO BBDD',
          text: 'Inyectando datos al núcleo del sistema... No cierre esta ventana.',
          allowOutsideClick: false,
          didOpen: () => Swal.showLoading(),
          background: '#000',
          color: '#ffaa00',
          customClass: { popup: 'border border-yellow-500 rounded-none' }
        });

        try {
          const formData = new FormData();
          formData.append('file', file);

          await api.post('/admin/db/import', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          });

          Swal.fire({
            title: '> ÉXITO',
            text: 'Base de datos restaurada correctamente. Los datos han sido actualizados.',
            icon: 'success',
            background: '#000',
            color: '#00ff41',
            customClass: { popup: 'border border-green-500 rounded-none' }
          });

          // Recargar datos del panel
          this.fetchUsers();
          this.fetchTickets();
          this.fetchAuditLogs();

        } catch (error) {
          console.error('Error en importación:', error);
          Swal.fire({
            title: '> FALLO CRÍTICO',
            text: 'No se pudo restaurar la base de datos: ' + (error.response?.data || error.message),
            icon: 'error',
            background: '#000',
            color: '#ff0000',
            customClass: { popup: 'border border-red-500 rounded-none' }
          });
        }
      };

      fileInput.click();
    },

    async purgarSistema() {
      this.showMenu = false;

      // Primera confirmación
      const confirm1 = await Swal.fire({
        title: '> ☠️ PURGA TOTAL DEL SISTEMA',
        html: '<p style="color:#ff0000;">Esta operación <b>ELIMINARÁ PERMANENTEMENTE</b> todos los datos del sistema:</p><ul style="color:#ff4444; text-align:left; margin-top:10px;"><li>✘ Todos los tickets</li><li>✘ Todos los comentarios</li><li>✘ Todo el historial de auditoría</li></ul><p style="color:#ff0; margin-top:12px;">Los usuarios del sistema <b>NO</b> serán eliminados.</p>',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '> ENTIENDO, CONTINUAR',
        cancelButtonText: '> CANCELAR',
        background: '#000',
        color: '#ff0000',
        confirmButtonColor: '#cc0000',
        cancelButtonColor: '#333',
        customClass: { popup: 'border border-red-500 rounded-none' }
      });

      if (!confirm1.isConfirmed) return;

      // Segunda confirmación + solicitar contraseña
      const confirm2 = await Swal.fire({
        title: '> VERIFICACIÓN DE IDENTIDAD',
        html: '<p style="color:#ff4444;">Para confirmar la purga, introduce tu contraseña de <b>admin@devops.com</b>:</p>',
        input: 'password',
        inputPlaceholder: 'Contraseña de admin@devops.com',
        inputAttributes: {
          autocomplete: 'off'
        },
        showCancelButton: true,
        confirmButtonText: '> EJECUTAR PURGA',
        cancelButtonText: '> ABORTAR',
        background: '#000',
        color: '#ff0000',
        confirmButtonColor: '#cc0000',
        cancelButtonColor: '#333',
        customClass: { 
          popup: 'border border-red-500 rounded-none',
          input: 'bg-black text-green-400 border-red-500 font-mono'
        },
        inputValidator: (value) => {
          if (!value) return '¡Debes ingresar la contraseña!';
        }
      });

      if (!confirm2.isConfirmed) return;

      // Loading
      Swal.fire({
        title: '> ☠️ PURGANDO SISTEMA...',
        text: 'Eliminando todos los datos del núcleo... No cierre esta ventana.',
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading(),
        background: '#000',
        color: '#ff0000',
        customClass: { popup: 'border border-red-500 rounded-none' }
      });

      try {
        const response = await api.post('/admin/db/purge', {
          email: this.userEmail,
          password: confirm2.value
        });

        Swal.fire({
          title: '> PURGA COMPLETADA',
          text: response.data,
          icon: 'success',
          background: '#000',
          color: '#00ff41',
          customClass: { popup: 'border border-green-500 rounded-none' }
        });

        // Recargar datos del panel
        this.fetchUsers();
        this.fetchTickets();
        this.fetchAuditLogs();

      } catch (error) {
        console.error('Error en purga:', error);
        const errorMsg = error.response?.data || error.message;
        Swal.fire({
          title: '> ERROR',
          text: errorMsg,
          icon: 'error',
          background: '#000',
          color: '#ff0000',
          customClass: { popup: 'border border-red-500 rounded-none' }
        });
      }
    },
    
    verTicket(ticketId) {
      this.$router.push(`/admin/tickets/${ticketId}`);
    },

    verUsuario(userId) {
      this.$router.push(`/admin/usuarios/${userId}`);
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