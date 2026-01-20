<template>
  <div class="min-h-screen bg-black p-3">
    <header class="terminal-box mb-3">
      <div class="flex justify-between items-center">
        <h1 class="text-lg">> Admin - Tickets<span class="cursor-blink">|</span></h1>
        <div>
          <button @click="$router.push('/admin')" class="btn-matrix text-xs mr-2">
            > Panel
          </button>
          <button @click="handleLogout" class="btn-matrix text-xs">
            > Salir
          </button>
        </div>
      </div>
    </header>

    <!-- Filtros compactos -->
    <div class="terminal-box p-2 mb-3">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-2">
        <div>
          <label class="block text-green-400 text-xs mb-1">Estado:</label>
          <select v-model="filtroEstado" class="w-full bg-black border border-green-500 text-green-400 px-2 py-1 rounded text-xs">
            <option value="TODOS">Todos</option>
            <option value="ABIERTO">Abiertos</option>
            <option value="EN_PROGRESO">Progreso</option>
            <option value="RESUELTO">Resueltos</option>
          </select>
        </div>

        <div>
          <label class="block text-green-400 text-xs mb-1">Buscar:</label>
          <input v-model="busquedaTitulo" type="text" placeholder="Buscar..."
                 class="w-full bg-black border border-green-500 text-green-400 px-2 py-1 rounded text-xs">
        </div>

        <div>
          <label class="block text-green-400 text-xs mb-1">Orden:</label>
          <select v-model="orden" class="w-full bg-black border border-green-500 text-green-400 px-2 py-1 rounded text-xs">
            <option value="fechaReciente">Recientes</option>
            <option value="fechaAntigua">Antiguos</option>
            <option value="estado">Estado</option>
          </select>
        </div>

        <div class="flex items-end">
          <button @click="fetchTickets" class="btn-matrix text-xs w-full py-1" :disabled="loading">
            ↻ Actualizar
          </button>
        </div>
      </div>
    </div>

    <!-- Estadísticas ultra compactas -->
    <div class="grid grid-cols-4 gap-1 mb-3">
      <div class="terminal-box text-center p-1">
        <div class="text-base text-green-400">{{ tickets.length }}</div>
        <div class="text-[10px] text-green-500">Total</div>
      </div>
      <div class="terminal-box text-center p-1">
        <div class="text-base text-yellow-400">{{ ticketsAbiertos }}</div>
        <div class="text-[10px] text-green-500">Abiertos</div>
      </div>
      <div class="terminal-box text-center p-1">
        <div class="text-base text-blue-400">{{ ticketsEnProgreso }}</div>
        <div class="text-[10px] text-green-500">Progreso</div>
      </div>
      <div class="terminal-box text-center p-1">
        <div class="text-base text-green-400">{{ ticketsResueltos }}</div>
        <div class="text-[10px] text-green-500">Resueltos</div>
      </div>
    </div>

    <!-- Tabla compacta de tickets -->
    <div class="terminal-box p-2">
      <div v-if="loading" class="text-green-500 text-center py-3 text-sm">
        > Cargando...
      </div>

      <template v-else>
        <div v-if="ticketsFiltrados.length === 0" class="text-gray-500 text-center py-3 text-sm">
          > No hay tickets {{ mensajeFiltro }}
        </div>

        <div v-else>
          <!-- Tabla estilo terminal -->
          <div class="overflow-x-auto">
            <table class="w-full text-xs">
              <thead>
                <tr class="text-green-400 border-b border-green-500">
                  <th class="text-left py-1 px-1">ID</th>
                  <th class="text-left py-1 px-1">Título</th>
                  <th class="text-left py-1 px-1">Usuario</th>
                  <th class="text-left py-1 px-1">Estado</th>
                  <th class="text-left py-1 px-1">Creado</th>
                  <th class="text-left py-1 px-1">Comentarios</th>
                  <th class="text-left py-1 px-1">Prioridad</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="ticket in ticketsPaginados" :key="ticket.id"
                    class="border-b border-green-500/30 hover:bg-green-900/10 cursor-pointer transition-colors"
                    @click="verTicket(ticket.id)">
                  <td class="py-1 px-1 text-green-300 font-mono">#{{ ticket.id }}</td>
                  <td class="py-1 px-1">
                    <div class="text-green-300 truncate max-w-[120px]">{{ ticket.titulo }}</div>
                    <div class="text-green-500 text-[10px] truncate max-w-[120px]">{{ ticket.descripcion }}</div>
                  </td>
                  <td class="py-1 px-1 text-green-400 truncate max-w-[100px]">
                    {{ ticket.creadoPor?.email || ticket.creadoPor?.nombre || 'N/A' }}
                  </td>
                  <td class="py-1 px-1">
                    <span class="px-1 rounded text-[10px] font-medium" :class="statusClass(ticket.estado)">
                      {{ ticket.estado }}
                    </span>
                  </td>
                  <td class="py-1 px-1 text-green-400">{{ formatDateCompact(ticket.fechaCreacion) }}</td>
                  <td class="py-1 px-1 text-green-300 text-center">{{ ticket.cantidadComentarios || 0 }}</td>
                  <td class="py-1 px-1 text-green-400">{{ ticket.prioridad || 'Media' }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Paginación ultra compacta -->
          <div class="mt-3 flex flex-col sm:flex-row justify-between items-center space-y-1 sm:space-y-0">
            <span class="text-green-500 text-xs">
              {{ inicioPagina + 1 }}-{{ finPagina }} de {{ ticketsFiltrados.length }}
            </span>

            <div class="flex items-center space-x-1">
              <button @click="paginaAnterior" class="btn-matrix text-xs px-1 py-0.5" :disabled="paginaActual === 1">
                ←
              </button>

              <span class="text-green-400 text-xs mx-1">
                {{ paginaActual }}/{{ totalPaginas }}
              </span>

              <button @click="paginaSiguiente" class="btn-matrix text-xs px-1 py-0.5" :disabled="paginaActual === totalPaginas">
                →
              </button>
            </div>

            <div class="flex items-center space-x-1">
              <span class="text-green-400 text-xs">Mostrar:</span>
              <select v-model="ticketsPorPagina" class="bg-black border border-green-500 text-green-400 px-1 py-0.5 rounded text-xs">
                <option value="10">10</option>
                <option value="20">20</option>
                <option value="30">30</option>
                <option value="50">50</option>
              </select>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import Swal from 'sweetalert2';

export default {
  data() {
    return {
      tickets: [],
      loading: false,
      filtroEstado: 'TODOS',
      busquedaTitulo: '',
      orden: 'fechaReciente',
      ticketsFiltrados: [],

      // Paginación - más tickets por defecto
      paginaActual: 1,
      ticketsPorPagina: 20 // Más tickets por página
    }
  },

  watch: {
    filtroEstado() {
      this.aplicarFiltrosYOrden();
    },
    busquedaTitulo() {
      this.aplicarFiltrosYOrden();
    },
    orden() {
      this.aplicarFiltrosYOrden();
    },
    tickets() {
      this.aplicarFiltrosYOrden();
    },
    ticketsPorPagina() {
      this.paginaActual = 1;
    }
  },

  computed: {
    ticketsAbiertos() {
      return this.tickets.filter(t => t.estado === 'ABIERTO').length;
    },

    ticketsEnProgreso() {
      return this.tickets.filter(t => t.estado === 'EN_PROGRESO').length;
    },

    ticketsResueltos() {
      return this.tickets.filter(t => t.estado === 'RESUELTO').length;
    },

    mensajeFiltro() {
      if (this.filtroEstado !== 'TODOS') {
        return `con estado ${this.filtroEstado.toLowerCase()}`;
      }
      if (this.busquedaTitulo) {
        return 'que coincidan con la búsqueda';
      }
      return '';
    },

    totalPaginas() {
      return Math.ceil(this.ticketsFiltrados.length / this.ticketsPorPagina);
    },

    inicioPagina() {
      return (this.paginaActual - 1) * this.ticketsPorPagina;
    },

    finPagina() {
      return Math.min(this.inicioPagina + this.ticketsPorPagina, this.ticketsFiltrados.length);
    },

    ticketsPaginados() {
      return this.ticketsFiltrados.slice(this.inicioPagina, this.finPagina);
    }
  },

  created() {
    this.verificarPermisos();
    this.fetchTickets();
  },

  methods: {
    parseCustomDate(dateString) {
      if (!dateString) return new Date();

      if (Array.isArray(dateString)) {
        const [year, month, day, hours, minutes] = dateString;
        return new Date(year, month - 1, day, hours, minutes || 0);
      }

      if (typeof dateString === 'string' && dateString.includes(',')) {
        const parts = dateString.split(',');
        if (parts.length >= 6) {
          const year = parseInt(parts[0]);
          const month = parseInt(parts[1]) - 1;
          const day = parseInt(parts[2]);
          const hour = parseInt(parts[3]);
          const minute = parseInt(parts[4]);
          
          return new Date(year, month, day, hour, minute);
        }
      }

      const date = new Date(dateString);
      // Fallback for some string formats if needed
      if (isNaN(date.getTime()) && typeof dateString === 'string' && dateString.includes('T')) {
         return new Date(dateString.replace('+', '.000+'));
      }
      
      return date;
    },

    aplicarFiltrosYOrden() {
      let filtered = [...this.tickets];

      if (this.filtroEstado !== 'TODOS') {
        filtered = filtered.filter(ticket => ticket.estado === this.filtroEstado);
      }

      if (this.busquedaTitulo) {
        const searchTerm = this.busquedaTitulo.toLowerCase();
        filtered = filtered.filter(ticket =>
          ticket.titulo.toLowerCase().includes(searchTerm) ||
          ticket.descripcion.toLowerCase().includes(searchTerm)
        );
      }

      const ordenEstados = ['ABIERTO', 'EN_PROGRESO', 'RESUELTO'];

      switch (this.orden) {
        case 'fechaReciente':
          filtered.sort((a, b) => this.parseCustomDate(b.fechaCreacion) - this.parseCustomDate(a.fechaCreacion));
          break;
        case 'fechaAntigua':
          filtered.sort((a, b) => this.parseCustomDate(a.fechaCreacion) - this.parseCustomDate(b.fechaCreacion));
          break;
        case 'estado':
          filtered.sort((a, b) => ordenEstados.indexOf(a.estado) - ordenEstados.indexOf(b.estado));
          break;
        default:
          filtered.sort((a, b) => this.parseCustomDate(b.fechaCreacion) - this.parseCustomDate(a.fechaCreacion));
      }

      this.ticketsFiltrados = filtered;
      this.paginaActual = 1;
    },

    paginaAnterior() {
      if (this.paginaActual > 1) {
        this.paginaActual--;
      }
    },

    paginaSiguiente() {
      if (this.paginaActual < this.totalPaginas) {
        this.paginaActual++;
      }
    },

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
      }
    },

    async fetchTickets() {
      this.loading = true;
      try {
        const response = await api.get('/admin/tickets');
        this.tickets = response.data || [];
        this.aplicarFiltrosYOrden();
      } catch (error) {
        console.error("Error fetching tickets:", error);
        Swal.fire({
            title: '> Error de Carga',
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
        this.loading = false;
      }
    },

    verTicket(ticketId) {
      this.$router.push(`/admin/tickets/${ticketId}`);
    },

    formatDateCompact(dateData) {
      if (!dateData) return 'N/A';

      try {
        const date = this.parseCustomDate(dateData);
        if (isNaN(date.getTime())) return 'N/A';

        return date.toLocaleDateString('es-MX', {
          day: '2-digit',
          month: '2-digit',
          year: '2-digit'
        });
      } catch (error) {
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

    handleLogout() {
      localStorage.clear();
      this.$router.push('/login');
    }
  }
}
</script>

<style scoped>
.terminal-box {
  @apply border border-green-500 rounded p-2;
}

.btn-matrix {
  @apply bg-green-500/20 text-green-400 border border-green-500 rounded hover:bg-green-500/30 transition-colors disabled:opacity-50;
}

.cursor-blink {
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

table {
  border-collapse: collapse;
}

th, td {
  border: none;
}
</style>