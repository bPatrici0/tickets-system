<template>
  <div class="min-h-screen bg-black p-4">
    <header class="terminal-box mb-6">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl">> Panel Admin<span class="cursor-blink">|</span></h1>
        <button
          @click="handleLogout"
          class="btn-matrix text-sm"
        >
          > Cerrar sesión
        </button>
      </div>
    </header>

    <!-- Estadísticas -->
    <div class="terminal-box p-4">
        <h2 class="text-xl mb-4">> Estadísticas<span class="cursor-blink">|</span></h2>
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
        <h2 class="text-xl mb-4">> Usuarios <span class="text-green-400">({{ users.length }})</span><span class="cursor-blink">|</span></h2>

        <!-- estado de carga -->
        <div v-if="loadingUsers" class="text-green-500 text-center py-4">
          > Cargando usuarios...
        </div>

        <!-- estado -->
        <div v-else-if="users.length === 0" class="text-gray-500 text-center py-4">
          > No hay usuarios registrados
        </div>

        <!-- Lista de usuarios -->
        <div v-else class="space-y-3 max-h-96 overflow-y-auto">
          <div v-for="user in users" :key="user.id" class="p-3 border border-green-500 rounded">
            <div class="flex justify-between items-center mb-2">
              <span class="text-green-300 font-mono">{{ user.email }}</span>
              <label class="switch">
                <input
                  type="checkbox"
                  :checked="user.rol === 'ROLE_ADMIN'"
                  @change="toggleRole(user)"
                  :disabled="updatingUser === user.id"
                >
                <span class="slider round"></span>
              </label>
            </div>
            <div class="flex justify-between items-center text-xs text-green-500">
              <span>Rol: {{ user.rol }}</span>
              <span>ID: {{ user.id }}</span>
            </div>
            <div v-if="updatingUser === user.id" class="text-green-400 text-xs mt-2">
              > Actualizando...
            </div>
          </div>
        </div>

        <button
          @click="fetchUsers"
          class="btn-matrix mt-4 text-sm"
          :disabled="loadingUsers"
        >
          > Actualizar lista
        </button>
      </div>

      <!-- Sección Tickets -->
      <div class="terminal-box p-4">
        <h2 class="text-xl mb-4">> Tickets <span class="text-green-400">({{ tickets.length }})</span><span class="cursor-blink">|</span></h2>

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
               @click="verTicket(ticket.id)">
            <div class="flex justify-between items-center mb-2">
              <span class="text-green-300">#{{ ticket.id }} - {{ ticket.titulo }}</span>
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
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  data() {
    return {
      users: [],
      tickets: [],
      loadingUsers: false,
      loadingTickets: false,
      updatingUser: null
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
    }
  },

  created() {
    const userRole = localStorage.getItem('userRole');
    if (userRole !== 'ROLE_ADMIN') {
      this.$router.push('/tickets');
      return;
    }
    this.fetchUsers();
    this.fetchTickets();
  },

  methods: {
    async fetchUsers() {
      this.loadingUsers = true;
      try {
        const response = await api.get('/admin/users');
        this.users = response.data;
        console.log('Usuarios cargados:', this.users);
      } catch (error) {
        console.error("Error fetching users: ", error);
        alert("Error al cargar usuarios");
      } finally {
        this.loadingUsers = false;
      }
    },

    async fetchTickets() {
      this.loadingTickets = true;
      try {
        const response = await api.get('/tickets');
        this.tickets = response.data;
        console.log('Tickets cargados:', this.tickets);
      } catch (error) {
        console.error("Error fetching tickets: ", error);
        alert("Error al cargar tickets");
      } finally {
        this.loadingTickets = false;
      }
    },

    async toggleRole(user) {
      this.updatingUser = user.id;
      try {
        const newRole = user.rol === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN';

        await api.put(`/admin/users/${user.id}/role`, {
          role: newRole
        });

        // Actualizar localmente
        user.rol = newRole;
        alert(`Rol actualizado a: ${newRole}`);

      } catch (error) {
        console.error("Error updating role: ", error);
        alert("Error al cambiar rol");
      } finally {
        this.updatingUser = null;
      }
    },

    verTicket(ticketId) {
      this.$router.push(`/ticket/${ticketId}`);
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
  @apply border border-green-500 rounded-lg p-4;
}

.btn-matrix {
  @apply bg-green-500/20 text-green-400 border border-green-500 px-3 py-1 hover:bg-green-500/30 transition-colors disabled:opacity-50 disabled:cursor-not-allowed;
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