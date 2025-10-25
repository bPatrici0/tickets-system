<template>
  <div class="flex items-center justify-center min-h-screen bg-black p-4">
    <div class="w-full max-w-md">
      <!-- Título centrado -->
      <h1 class="text-3xl text-green-500 text-center mb-8 font-mono">
        > Sistema de Tickets<span class="cursor-blink">|</span>
      </h1>

      <div class="terminal-box">
        <h2 class="text-xl mb-4 text-green-500">> Login</h2>

        <form v-if="!requirePasswordChange" @submit.prevent="handleLogin" class="space-y-4">
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
          <div class="mb-4 relative">
            <label class="block text-green-400 mb-1">> Password:</label>
            <div>
                <input
                    :type="showPassword ? 'text' : 'password'"
                    v-model="password"
                    class="w-full bg-black border border-green-500 p-2 text-green-500 focus:outline-none pr-10"
                    required
                >
                <button
                    type="button"
                    class="absolute right-3 top-12 transform -translate-y-1/2 text-green-500 hover:text-green-300 focus:outline-none"
                    @click="showPassword = !showPassword"
                    aria-label="Mostrar/ocultar contraseña"
                >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        class="h-5 w-5"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                    >
                        <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            :d="showPassword ? 'M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21' : 'M15 12a3 3 0 11-6 0 3 3 0 016 0z M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z'"
                        />
                    </svg>
                </button>
            </div>
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

        <div v-else class="space-y-4">
            <div class="bg-yellow-500/20 border border-yellow-500 p-4 rounded">
                <h3 class="text-yellow-400 font-bold mb-2">⚠️ Cambio de contraseña requerido!...</h3>
                <p class="text-yellow-300 text-sm">
                    Por seguridad, debes establecer una nueva contraseña para continuar.
                </p>
            </div>

            <form @submit.prevent="handlePasswordChange">
                <div>
                    <label class="block text-green-400 mb-1">> Nueva Contraseña:</label>
                    <input
                        type="password"
                        v-model="newPasswordData.newPassword"
                        class="w-full bg-black border border-green-500 p-2 text-green-500 focus:outline-none"
                        required
                        palceholder="******"
                        >
                </div>

                <div>
                    <label class="block text-green-400 mb-1">> Confirmar contraseña:</label>
                    <input
                        type="password"
                        v-model="newPasswordData.confirmPassword"
                        class="w-full bg-black border border-green-500 p-2 text-green-500 focus:outline-none"
                        required
                        placeholder="Confirma tu contraseña"
                    >
                </div>

                <div v-if="passwordMismatch" class="text-red-400 text-sm mt-2">
                    > Las contraseñas no coinciden
                </div>

                <button
                    type="submit"
                    class="btn-matrix w-full py-2 mt-4"
                    :disabled="changingPassword"
                >
                    <span v-if="!changingPassword">> Establecer nueva contraseña</span>
                    <span v-else>> Procesando...</span>
                </button>
            </form>
        </div>

        <!-- Enlace a registro -->
        <p v-if="!requiredPasswordChange" class="text-center mt-4 text-green-400">
          > ¿No tienes cuenta?
          <router-link
            to="/register"
            class="text-green-300 hover:underline"
          >
            > Regístrate
          </router-link>
        </p>

        <div v-if="error" class="text-red-400 text-sm mt-4 text-center">
            > {{ error }}
        </div>

        <div v-else class="space-y-4">
            <div class="bg-yellow-500/20 border border-yellow-500 p-4 rounded">
                <h3 class="text-yellow-400 font-bold mb-2">⚠️ Cambio de contraseña requerido</h3>
                <p class="text-yellow-300 text-sm">
                    Por seguridad, debes establecer una nueva contraseña para continuar!...
                </p>
            </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
    name: 'LoginView',
  data() {
    return {
      email: '',
      showPassword: false,
      password: '',
      loading: false,
      error: null,
      requirePasswordChange: false,
      changingPassword: false,
      tempUserEmail: '',
      newPasswordData: {
        newPassword: '',
        confirmPassword: ''
      }
    }
  },
  computed: {
    passwordMismatch() {
        return this.newPasswordData.newPassword !== this.newPasswordData.confirmPassword &&
            this.newPasswordData.confirmPassword !== '';
    }
  },
  methods: {
    async handleLogin() {
        this.loading = true;
        this.error = null;

        try {
            const credentials = btoa(`${this.email.trim()}:${this.password}`);

            const response = await api.post('/auth/login', {
                email: this.email.trim(),
                password: this.password
            }, {
                headers: {
                    'Authorization': `Basic ${credentials}`
                }
            });

            console.log('Login response:', response.data);

            if (response.data.passwordResetRequired) {
                this.requirePasswordChange = true;
                this.tempUserEmail = this.email.trim();
                this.error = null;
            } else {
                localStorage.setItem('userEmail', this.email.trim());
                localStorage.setItem('userRole', response.data.rol);
                localStorage.setItem('userName', response.data.nombre || 'Usuario');
                localStorage.setItem('token', credentials);

                this.redirectByRole(response.data.rol);

                //redirigir segun rol
                if (response.data.rol === "ROLE_ADMIN"){
                    this.$router.push('/admin');
                } else {
                    this.$router.push('/tickets');
                }
            }
        } catch (error) {
            console.error("Error: ", error.response);
            alert(error.response?.data || "Error de conexión");
        } finally {
            this.loading = false;
        }
    },
    async handlePasswordChange() {
        if (this.passwordMismatch) {
            this.error = 'Las constraseñas no coinciden';
            return;
        }

        this.changingPassword = true;
        this.error = null;

        try {
            await api.post('/auth/cambiar-password', {
                email: this.tempUserEmail,
                nuevaPassword: this.newPasswordData.newPassword
            });

            alert('✅ Constraseña cambiada correctamente. Ahora puedes iniciar sesión con tu nueva contraseña.');

            this.requirePasswordChange = false;
            this.newPasswordData = {
                newPassword: '',
                confirmPassword: ''
            };
            this.email = '';
            this.password = '';
        } catch (error) {
            console.error('Password change error:', error);
            this.error = error.response?.data || 'Error al cambiar contraseña';
        } finally {
            this.changingPassword = false;
        }
    },
    redirectByRole(role) {
        if (role === 'ROLE_ADMIN') {
            this.$router.push('/admin');
        } else {
            this.$router.push('/tickets');
        }
    },
    async submitTicket() {
        this.isSubmitting = true;
        try {
            const response = await api.post('/tickets', {
                title: this.newTicket.subject,
                description: this.newTicket.description,
                status: 'ABIERTO'
            });

            if(response.status === 201) {
                alert('Ticket creado exitosamente!...');
                this.newTicket = { subject: '', description: '' };
                //para actulizar lista de tickets
            }
        }catch (error) {
            console.error("Error completo: ", error);
            if (error.response?.status === 401) {
                alert("Tu sesión ha expirado. Por favor inicia sesión nuevamente!...");
                this.$router.push('/login');
            } else {
                alert(error.response?.data?.message || "Error al crear ticket!...");
            }
        } finally {
            this.isSubmitting = false;
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