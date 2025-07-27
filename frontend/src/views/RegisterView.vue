<template>
    <div class="min-h-screen flex-col items-center justify-center bg-black text-green-500 p-4 font-mono">
        <h1 class="text-3x1 mb-8">
            > Sistema de Tickets<span class="cursor-blink">|</span>
        </h1>

        <div class="terminal-box w-full max-w-md">
            <h2 class="text-x1 mb-4">> Registro<span class="cursor-blink">|</span></h2>

            <form @submit.prevent="handleRegister" class="space-y-4">
                <div>
                    <label class="block mb-1">> Nombre: </label>
                    <input type="text" v-model="name" class="w-full bg-black border border-green-500 p-2 text-green-500 focus:outline-none focus:ring-1 focus:ring-green-500" required>
                </div>

                <div>
                    <label class="block mb-1">> Correo:</label>
                    <input type="email" v-model="email" class="w-full bg-black border border-green-500 p-2 text-green-500 focus:outline-none focus:ring-1 focus:ring-green-500" required>
                </div>

                <div>
                    <label class="block mb-1">> Password:</label>
                    <input type="password" v-model="password" class="w-full bg-black border border-green-500 p-2 text-green-500 focus:outline-none focus:ring-1 focus:ring-green-500" required>
                </div>

                <div>
                    <label class="block mb-1">> Confirmar Password:</label>
                    <input type="password" v-model="confirmPassword" class="w-full bg-black border border-green-500 p-2 text-green-500 focus:outline-none focus:ring-1 focus:ring-green-500" required>
                </div>

                <button type="submit" class="btn-matrix mt-4 w-full py-2 hover:bg-green-600 transition-colors" :disable="loading">
                    <span v-if="!loading">> Crear cuenta</span>
                    <span v-else class="flex items-center justify-center">
                        > Procesando<span class="cursor-blink">|</span>
                    </span>
                </button>
            </form>

            <p class="mt-4 text-center">> Ya tienes una cuenta?
                <router-link
                    to="/login"
                    class="text-green-300 hover:underline"
                >
                    > Inicia Sesion
                </router-link>
            </p>
        </div>
    </div>
</template>

<script>
import api from '@/services/api';
export default {
    data() {
        return {
            name: '',
            email: '',
            password: '',
            confirmPassword: '',
            loading: false
        }
    },
    methods: {
        async handleRegister() {
            try {
                const response = await api.post('/auth/registro',{
                    email: this.email.trim(),
                    password: this.password
                }, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                if (response.status === 200) {
                    alert("Registro exitoso!...");
                    this.$router.push('/login');
                }
            } catch (error) {
                console.error("Error en registro: ", error.response);
                alert(error.response?.data || "> Error al registrar...");
            }
        }
    }
}
</script>

<style scoped>
.terminal-box {
  @apply border border-green-500 p-6 rounded-lg shadow-lg shadow-green-500/10;
}

.btn-matrix {
  @apply bg-green-500 text-black font-bold py-2 px-4 rounded;
}

.cursor-blink {
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}
</style>
