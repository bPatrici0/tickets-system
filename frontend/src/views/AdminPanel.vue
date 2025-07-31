<template>
  <div class="min-h-screen bg-black p-4">
    <header class="terminal-box mb-6 text-center">
      <h1 class="text-2xl">> Panel Admin<span class="cursor-blink">|</span></h1>
    </header>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- Sección Usuarios -->
      <div class="terminal-box p-4">
        <h2 class="text-xl mb-4">> Usuarios<span class="cursor-blink">|</span></h2>
        <div v-for="user in users" :key="user.id" class="mb-4 p-2 border-b border-green-500">
          <div class="flex justify-between items-center">
            <span>{{ user.email }}</span>
            <label class="switch">
              <input
                type="checkbox"
                :checked="user.rol === 'ROLE_ADMIN'"
                @change="toggleRole(user)"
              >
              <span class="slider round"></span>
            </label>
          </div>
          <p class="text-sm text-green-400">Rol actual: {{ user.rol }}</p>
        </div>
      </div>

      <!-- Sección Tickets -->
      <div class="terminal-box p-4">
        <h2 class="text-xl mb-4">> Tickets_<span class="cursor-blink">|</span></h2>
        <!-- Listado de tickets aquí -->
      </div>
      <div>
        <nav class="mt-4">
            <button
                @click="handleLogout"
                class="btn-matrix"
            >
                > Cerrar sesión
            </button>
        </nav>
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
        }
    },

    created() {
        const userRole = localStorage.getItem('userRole');
        if(userRole !== 'ROLE_ADMIN'){
            this.$router.push('/tickets');
        }
    },
    methods: {
        async verifyAdminAccess() {
            const userRole = localStorage.getItem('userRole');
            if (userRole !== 'ADMIN') {
                throw new Error('Acceso denegado');
            }
        },

        async fetchUsers() {
            try {
                const response = await api.get('/admin/users');
                this.users = response.data;
            } catch (error) {
                console.error("Error fetching users: ", error);
            }
        },

        async toggleRole(user) {
            try {
                const newRole = user.rol === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN';
                await api.put('/admin/users/${user.id}/role', { role: newRole });
                await this.fetchUsers();
            } catch (error) {
                console.error("Error updating role: ", error);
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
.terminal-box {
  @apply border border-green-500 rounded-lg;
}

.switch {
  position: relative;
  display: inline-block;
  width: 60px;
  height: 34px;
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
  border-radius: 34px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 26px;
  width: 26px;
  left: 4px;
  bottom: 4px;
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
</style>