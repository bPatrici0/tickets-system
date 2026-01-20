<template>
    <div class="flex items-center justify-center min-h-screen bg-black p-4">
        <div class="w-full max-w-md">
            <h1 class="text-3xl text-green-500 text-center mb-8 font-mono">
                > Registro<span class="cursor-blink">|</span>
            </h1>

            <div class="terminal-box w-full max-w-md">
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

                    <button type="submit" class="btn-matrix mt-4 w-full py-2 hover:bg-green-600 transition-colors" :disabled="loading">
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
    </div>
</template>

<script>
import api from '@/services/api';
import Swal from 'sweetalert2';
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
                    nombre: this.name.trim(),
                    email: this.email.trim(),
                    password: this.password
                }, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                if (response.status === 200) {
                    await Swal.fire({
                        title: '> Registro Exitoso',
                        text: 'Tu cuenta ha sido creada correctamente.',
                        icon: 'success',
                        background: '#000',
                        color: '#00ff41',
                        confirmButtonText: '> Iniciar Sesion',
                        confirmButtonColor: '#00aa00',
                        customClass: {
                            popup: 'border border-green-500 rounded-none',
                            title: 'font-mono',
                            confirmButton: 'font-mono'
                        }
                    });
                    this.$router.push('/login');
                }
            } catch (error) {
                console.error("Error en registro: ", error.response);
                Swal.fire({
                    title: '> Error',
                    text: error.response?.data || "No se pudo crear la cuenta",
                    icon: 'error',
                    background: '#000',
                    color: '#ff4444', 
                    confirmButtonText: '> Reintentar',
                    confirmButtonColor: '#333',
                    customClass: {
                        popup: 'border border-red-500 rounded-none',
                        title: 'font-mono',
                        confirmButton: 'font-mono'
                    }
                });
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
