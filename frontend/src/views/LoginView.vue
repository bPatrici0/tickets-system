<template>
  <div class="flex items-center justify-center min-h-screen bg-black p-4">
    <div class="w-full max-w-md">
      <!-- Título centrado -->
      <h1 class="text-3xl text-green-500 text-center mb-8 font-mono">
        > Sistema de Tickets<span class="cursor-blink">|</span>
      </h1>

      <!-- Formulario con bordes controlados -->
      <div class="terminal-box">
        <h2 class="text-xl mb-4 text-green-500">> Login</h2>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <!-- Campo Email -->
          <div>
            <label class="block text-green-400 mb-1">> Correo:</label>
            <input
              type="email"
              v-model="email"
              class="w-full bg-black border border-green-500 p-2 text-green-500 focus:outline-none"
              required
            >
          </div>

          <!-- Campo Password -->
          <div>
            <label class="block text-green-400 mb-1">> Password:</label>
            <input
              type="password"
              v-model="password"
              class="w-full bg-black border border-green-500 p-2 text-green-500 focus:outline-none"
              required
            >
          </div>

          <!-- Botón de acción -->
          <button
            type="submit"
            class="btn-matrix w-full py-2 mt-4"
            :disabled="loading"
          >
            <span v-if="!loading">> Login</span>
            <span v-else>> Verificando...</span>
          </button>
        </form>

        <!-- Enlace a registro -->
        <p class="text-center mt-4 text-green-400">
          > ¿No tienes cuenta?
          <router-link
            to="/register"
            class="text-green-300 hover:underline"
          >
            > Regístrate
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  data() {
    return {
      email: '',
      password: '',
      loading: false,
      error: null
    }
  },
  methods: {
    async handleLogin() {
        this.loading = true;
        try {
            // Conexión backend
            const response = await api.post('api/auth/login', {
                email: this.email.trim(),
                password: this.password
            }, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.data) {
                localStorage.setItem('userEmail', response.data.email);
                localStorage.setItem('userRole', response.data.rol);
            }

            //redirigir segun usuario
            if (response.data.rol === "ROLE_ADMIN"){
                this.$router.push('/admin-panel');
            } else {
                this.$router.push('/tickets');
            }
        }
    }
  }
}
</script>

<style scoped>
/* Estilos específicos para esta vista */
.terminal-box {
  @apply border border-green-500 p-6 rounded-lg;
  width: 100%;
}

.btn-matrix {
  @apply bg-green-500 text-black px-4 py-2 rounded hover:bg-green-400 transition-colors;
}

.cursor-blink {
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

/* Elimina el scroll global */
html, body {
  overflow: hidden;
}
</style>