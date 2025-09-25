<template>
  <div class="min-h-screen bg-black p-4">
    <header class="terminal-box mb-4">
      <div class="flex justify-between items-center">
        <h1 class="text-xl">> Admin - Todos los Tickets<span class="cursor-blink">|</span></h1>
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

    <!-- Filtros y búsqueda -->
    <div class="terminal-box p-3 mb-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
        <div>
          <label class="block text-green-400 text-xs mb-1">Filtrar por estado:</label>
          <select v-model="filtroEstado" class="w-full bg-black border border-green-500 text-green-400 px-2 py-1 rounded text-sm">
            <option value="TODOS">Todos</option>
            <option value="ABIERTO">Abiertos</option>
            <option value="EN_PROGRESO">En progreso</option>
            <option value="RESUELTO">Resueltos</option>
          </select>
        </div>

        <div>
          <label class="block text-green-400 text-xs mb-1">Buscar:</label>
          <input v-model="busquedaTitulo" type="text" placeholder="Buscar tickets..."
                 class="w-full bg-black border border-green-500 text-green-400 px-2 py-1 rounded text-sm">
        </div>

        <div>
          <label class="block text-green-400 text-xs mb-1">Ordenar:</label>
          <select v-model="orden" class="w-full bg-black border border-green-500 text-green-400 px-2 py-1 rounded text-sm">
            <option value="fechaReciente">Recientes</option>
            <option value="fechaAntigua">Antiguos</option>
            <option value="estado">Estado</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Estadísticas compactas -->
    <div class="grid grid-cols-4 gap-2 mb-4">
      <div class="terminal-box text-center p-2">
        <div class="text-lg text-green-400">{{ tickets.length }}</div>
        <div class="text-xs text-green-500">Total</div>
      </div>
      <div class="terminal-box text-center p-2">
        <div class="text-lg text-yellow-400">{{ ticketsAbiertos }}</div>
        <div class="text-xs text-green-500">Abiertos</div>
      </div>
      <div class="terminal-box text-center p-2">
        <div class="text-lg text-blue-400">{{ ticketsEnProgreso }}</div>
        <div class="text-xs text-green-500">Progreso</div>
      </div>
      <div class="terminal-box text-center p-2">
        <div class="text-lg text-green-400">{{ ticketsResueltos }}</div>
        <div class="text-xs text-green-500">Resueltos</div>
      </div>
    </div>

    <!-- Lista de tickets compacta -->
    <div class="terminal-box">
      <div v-if="loading" class="text-green-500 text-center py-4">
        > Cargando...
      </div>

      <template v-else>
        <div v-if="ticketsFiltrados.length === 0" class="text-gray-500 text-center py-4">
          > No hay tickets {{ mensajeFiltro }}
        </div>

        <div v-else>
          <!-- Tickets compactos -->
          <div class="space-y-2">
            <div v-for="ticket in ticketsPaginados" :key="ticket.id"
                 class="p-3 border border-green-500 rounded hover:bg-green-900/10 cursor-pointer transition-colors"
                 @click="verTicket(ticket.id)">

              <div class="flex justify-between items-center mb-2">
                <div class="flex-1 min-w-0">
                  <h3 class="text-sm text-green-300 font-mono truncate">#{{ ticket.id }} - {{ ticket.titulo }}</h3>
                  <p class="text-xs text-green-500 truncate">Por: {{ ticket.usuario?.email || 'Usuario desconocido' }}</p>
                </div>
                <span class="px-2 py-1 rounded text-xs font-medium ml-2 flex-shrink-0" :class="statusClass(ticket.estado)">
                  {{ ticket.estado }}
                </span>
              </div>

              <div class="grid grid-cols-4 gap-2 text-xs text-green-500">
                <div class="text-center">
                  <span class="block text-green-400">Creado</span>
                  <span class="text-green-300 text-xs">{{ formatDateCompact(ticket.fechaCreacion) }}</span>
                </div>
                <div class="text-center">
                  <span class="block text-green-400">Actualizado</span>
                  <span class="text-green-300 text-xs">{{ formatDateCompact(ticket.fechaActualizacion) }}</span>
                </div>
                <div class="text-center">
                  <span class="block text-green-400">Comentarios</span>
                  <span class="text-green-300">{{ ticket.cantidadComentarios || 0 }}</span>
                </div>
                <div class="text-center">
                  <span class="block text-green-400">Prioridad</span>
                  <span class="text-green-300 text-xs">{{ ticket.prioridad || 'Media' }}</span>
                </div>
              </div>

              <div class="mt-2 pt-2 border-t border-green-500/30">
                <p class="text-xs text-green-400 line-clamp-1">{{ ticket.descripcion }}</p>
              </div>
            </div>
          </div>

          <!-- Paginación compacta -->
          <div class="mt-4 flex flex-col sm:flex-row justify-between items-center space-y-2 sm:space-y-0">
            <span class="text-green-500 text-xs">
              {{ inicioPagina + 1 }}-{{ finPagina }} de {{ ticketsFiltrados.length }}
            </span>

            <div class="flex items-center space-x-1">
              <button @click="paginaAnterior" class="btn-matrix text-xs px-2 py-1" :disabled="paginaActual === 1">
                ←
              </button>

              <span class="text-green-400 text-xs mx-2">
                {{ paginaActual }}/{{ totalPaginas }}
              </span>

              <button @click="paginaSiguiente" class="btn-matrix text-xs px-2 py-1" :disabled="paginaActual === totalPaginas">
                →
              </button>
            </div>

            <div class="flex items-center space-x-1">
              <span class="text-green-400 text-xs">Por página:</span>
              <select v-model="ticketsPorPagina" class="bg-black border border-green-500 text-green-400 px-1 py-1 rounded text-xs">
                <option value="10">10</option>
                <option value="15">15</option>
                <option value="20">20</option>
                <option value="25">25</option>
              </select>
            </div>
          </div>
        </div>

        <div class="mt-3 flex justify-center">
          <button @click="fetchTickets" class="btn-matrix text-xs px-3 py-1" :disabled="loading">
            ↻ Actualizar
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  data() {
    return {
      tickets: [],
      loading: false,
      filtroEstado: 'TODOS',
      busquedaTitulo: '',
      orden: 'fechaReciente',
      ticketsFiltrados: [],

      // Paginación - más tickets por página
      paginaActual: 1,
      ticketsPorPagina: 15 // Aumentado para mostrar más tickets
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

      if (typeof dateString === 'string' && dateString.includes(',')) {
        const parts = dateString.split(',');
        if (parts.length >= 6) {
          const year = parseInt(parts[0]);
          const month = parseInt(parts[1]) - 1;
          const day = parseInt(parts[2]);
          const hour = parseInt(parts[3]);
          const minute = parseInt(parts[4]);
          const second = parseInt(parts[5]);

          return new Date(year, month, day, hour, minute, second);
        }
      }

      return new Date(dateString);
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
        alert('No tienes permisos de administrador');
        this.$router.push('/tickets');
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
        alert("Error al cargar tickets");
      } finally {
        this.loading = false;
      }
    },

    verTicket(ticketId) {
      this.$router.push(`/admin/tickets/${ticketId}`);
    },

    formatDate(dateData) {
      if (!dateData) return 'N/A';

      try {
        const date = this.parseCustomDate(dateData);
        return date.toLocaleDateString('es-MX', {
          day: '2-digit',
          month: 'short',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
      } catch (error) {
        return 'N/A';
      }
    },

    formatDateCompact(dateData) {
      if (!dateData) return 'N/A';

      try {
        const date = this.parseCustomDate(dateData);
        return date.toLocaleDateString('es-MX', {
          day: '2-digit',
          month: '2-digit'
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
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.terminal-box {
  @apply border border-green-500 rounded-lg p-3;
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
</style>