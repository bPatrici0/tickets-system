<template>
  <div class="min-h-screen bg-black p-4">
    <header class="terminal-box mb-6">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl">> Admin - Todos los Tickets<span class="cursor-blink">|</span></h1>
        <div>
          <button @click="$router.push('/admin')" class="btn-matrix text-sm mr-2">
            > Volver al Panel
          </button>
          <button @click="handleLogout" class="btn-matrix text-sm">
            > Cerrar Sesión
          </button>
        </div>
      </div>
    </header>

    <!-- Filtros y búsqueda -->
    <div class="terminal-box p-4 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-green-400 text-sm mb-1">Filtrar por estado:</label>
          <select v-model="filtroEstado" class="w-full bg-black border border-green-500 text-green-400 px-3 py-2 rounded">
            <option value="TODOS">Todos los estados</option>
            <option value="ABIERTO">Abiertos</option>
            <option value="EN_PROGRESO">En progreso</option>
            <option value="RESUELTO">Resueltos</option>
          </select>
        </div>

        <div>
          <label class="block text-green-400 text-sm mb-1">Buscar por título:</label>
          <input v-model="busquedaTitulo" type="text" placeholder="Buscar tickets..."
                 class="w-full bg-black border border-green-500 text-green-400 px-3 py-2 rounded">
        </div>

        <div>
          <label class="block text-green-400 text-sm mb-1">Ordenar por:</label>
          <select v-model="orden" class="w-full bg-black border border-green-500 text-green-400 px-3 py-2 rounded">
            <option value="fechaReciente">Más recientes primero</option>
            <option value="fechaAntigua">Más antiguos primero</option>
            <option value="estado">Por estado</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Estadísticas -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
      <div class="terminal-box text-center p-4">
        <div class="text-2xl text-green-400">{{ tickets.length }}</div>
        <div class="text-sm text-green-500">Total Tickets</div>
      </div>
      <div class="terminal-box text-center p-4">
        <div class="text-2xl text-yellow-400">{{ ticketsAbiertos }}</div>
        <div class="text-sm text-green-500">Abiertos</div>
      </div>
      <div class="terminal-box text-center p-4">
        <div class="text-2xl text-blue-400">{{ ticketsEnProgreso }}</div>
        <div class="text-sm text-green-500">En Progreso</div>
      </div>
      <div class="terminal-box text-center p-4">
        <div class="text-2xl text-green-400">{{ ticketsResueltos }}</div>
        <div class="text-sm text-green-500">Resueltos</div>
      </div>
    </div>

    <!-- Lista de tickets -->
    <div class="terminal-box">
      <div v-if="loading" class="text-green-500 text-center py-8">
        > Cargando tickets...
      </div>

      <template v-else-if="ticketsFiltrados.length === 0">
        <div class="text-gray-500 text-center py-8">
            > No hay tickets {{ mensajeFiltro }}
        </div>
      </template>

      <template v-else>
        <!--tickets paginados-->
        <div class="space-y-3">
            <div v-for="ticket in ticketsPaginados" :key="ticket.id"
                class="p-4 border border-green-500 rounded hover:bg-green-900/10 cursor-pointer transition-colors"
                @click="verTicket(ticket.id)">
                <div class="flex justify-between items-start mb-3">
                    <div>
                        <h3 class="text-lg text-green-300 font-mono">#{{ ticket.id }} - {{ ticket.titulo }}</h3>
                        <p class="text-sm text-green-500 mt-1">Por: {{ ticket.usuario?.email || 'Usuario desconocido' }}</p>
                    </div>
                    <span class="px-3 py-1 rounded text-sm font-medium" :class="statusClass(ticket.estado)">
                        {{ ticket.estado }}
                    </span>
                </div>

                <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs text-xs text-green-500">
                    <div>
                        <span class="block">Creado:</span>
                        <span class="text-green-300">{{ formatDate(ticket.fechaCreacion) }}</span>
                    </div>
                    <div>
                        <span class="block">Actualizado:</span>
                        <span class="text-green-300">{{ formatDate(ticket.fechaActualizacion) }}</span>
                    </div>
                    <div>
                        <span class="block">Comentarios:</span>
                        <span class="text-green-300">{{ ticket.cantidadComentarios || 0 }}</span>
                    </div>
                    <div>
                        <span class="block">Prioridad:</span>
                        <span class="text-green-300">{{ ticket.prioridad || 'Media' }}</span>
                    </div>
                </div>

                <div class="mt-3 pt-3 border-t border-green-500/30">
                    <p class="text-sm text-green-400 line-clamp-2">{{ ticket.descripcion }}</p>
                </div>
            </div>
        </div>

        <!--paginacion-->
        <div class="mt-6 flex flex-col sm:flex-row justify-between items-center space-y-4 sm:space-y-0">
            <span class="text-green-500 text-sm">
                Mostrando {{ inicoPagina + 1 }}-{{ finPagina }} de {{ ticketsFiltrados.length }} tickets
            </span>

            <div class="flex items-center space-x-2">
                <button @click="paginaAnterior" class="btn-matrix text-sm" :disabled="paginaActual === 1">
                    ← Anterior
                </button>

                <span class="text-green-400 text-sm">
                    Página {{ paginaActual }} de {{ totalPaginas }}
                </span>

                <button @click="paginaSiguiente" class="btn-matrix text-sm" :disabled="paginaActual === totalPaginas">
                    Siguiente →
                </button>
            </div>

            <div class="flex items-center space-x-2">
                <span class="text-green-400 text-sm">Tickets por página: </span>
                <select v-model="ticketsPorPagina" class="bg-black border border-green-500 text-green-400 px-2 py-1 rounded text-sm">
                    <option value="5">5</option>
                    <option value="10">10</option>
                    <option value="20">20</option>
                    <option value="50">50</option>
                </select>
            </div>
        </div>
      </template>

      <div class="mt-4 flex justify-between items-center">
        <button @click="fetchTickets" class="btn-matrix text-sm" :disabled="loading">
            > Actualizar lista
        </button>
      </div>

      <div v-else class="space-y-3">
        <div v-for="ticket in ticketsFiltrados" :key="ticket.id"
             class="p-4 border border-green-500 rounded hover:bg-green-900/10 cursor-pointer transition-colors"
             @click="verTicket(ticket.id)">

          <div class="flex justify-between items-start mb-3">
            <div>
              <h3 class="text-lg text-green-300 font-mono">#{{ ticket.id }} - {{ ticket.titulo }}</h3>
              <p class="text-sm text-green-500 mt-1">Por: {{ ticket.usuario?.email || 'Usuario desconocido' }}</p>
            </div>
            <span class="px-3 py-1 rounded text-sm font-medium" :class="statusClass(ticket.estado)">
              {{ ticket.estado }}
            </span>
          </div>

          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs text-green-500">
            <div>
              <span class="block">Creado:</span>
              <span class="text-green-300">{{ formatDate(ticket.fechaCreacion) }}</span>
            </div>
            <div>
              <span class="block">Actualizado:</span>
              <span class="text-green-300">{{ formatDate(ticket.fechaActualizacion) }}</span>
            </div>
            <div>
              <span class="block">Comentarios:</span>
              <span class="text-green-300">{{ ticket.cantidadComentarios || 0 }}</span>
            </div>
            <div>
              <span class="block">Prioridad:</span>
              <span class="text-green-300">{{ ticket.prioridad || 'Media' }}</span>
            </div>
          </div>

          <div class="mt-3 pt-3 border-t border-green-500/30">
            <p class="text-sm text-green-400 line-clamp-2">{{ ticket.descripcion }}</p>
          </div>
        </div>
      </div>

      <div class="mt-4 flex justify-between items-center">
        <span class="text-green-500 text-sm">
          Mostrando {{ ticketsFiltrados.length }} de {{ tickets.length }} tickets
        </span>
        <button @click="fetchTickets" class="btn-matrix text-sm" :disabled="loading">
          > Actualizar lista
        </button>
      </div>
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
      paginaActual: 1,
      ticketsPorPagina: 10
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
        if (this.filtroEstado !== 'TODOS') {
            return `con estado ${this.filtroEstado.toLoweCase()}`;
        }
        if (this.busquedaTitulo) {
            return `que coincidadn con la búsqueda`;
        }
        return '';
    },

    totalPaginas() {
        return Math.ceil(this.ticketsFiltrados.length / this.ticketsPorPagina);
    },

    inicioPagina() {
        return (this.paginaActual -1) * this.ticketsPorPagina;
    },

    finPagina() {
        return Math.min(this.inicioPagina + this.ticketsPorPagina, this.ticketsFiltrados.length);
    },

    ticketsPaginados() {
        return this.ticketsFiltrados.slice(this.inicioPagina, this.finPagina);
    },

    mensajeFiltro() {
      if (this.filtroEstado !== 'TODOS') {
        return `con estado ${this.filtroEstado.toLowerCase()}`;
      }
      if (this.busquedaTitulo) {
        return 'que coincidan con la búsqueda';
      }
      return '';
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

                return new Date(year, month, day, hour, minute);
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
    }

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
        console.log('Datos crudos de la API:', response.data);

        this.tickets = response.data || [];

        this.tickets.forEach(ticket => {
          console.log(`Ticket ${ticket.id}:`, {
            fechaCreacion: ticket.fechaCreacion,
            tipo: typeof ticket.fechaCreacion,
            dateObject: this.parseCustomDate(ticket.fechaCreacion),
            timestamp: this.parseCustomDate(ticket.fechaCreacion).getTime()
          });
        });

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

    handleLogout() {
      localStorage.clear();
      this.$router.push('/login');
    }
  }
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.terminal-box {
  @apply border border-green-500 rounded-lg p-4;
}

.btn-matrix {
  @apply bg-green-500/20 text-green-400 border border-green-500 px-3 py-1 rounded hover:bg-green-500/30 transition-colors disabled:opacity-50;
}

.cursor-blink {
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}
</style>