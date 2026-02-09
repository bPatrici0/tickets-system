<template>
  <div class="min-h-screen bg-black p-4">
    <header class="terminal-box mb-6">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl">
          > Gestión de Usuario 
          <span class="text-sm text-green-500/60 font-mono ml-2">[{{ userEmail }}]</span>
          <span class="cursor-blink">|</span>
        </h1>
        <button @click="$router.back()" class="btn-matrix text-sm">
          > Volver
        </button>
      </div>
    </header>

    <div v-if="loading" class="text-green-500 text-center py-10 animate-pulse">
      > Accediendo a base de datos de usuarios...
    </div>

    <div v-else-if="userData" class="max-w-2xl mx-auto">
      <div class="terminal-box p-6 space-y-6">
        <h2 class="text-xl text-green-400 border-b border-green-900 pb-2">
          > Ficha Técnica: #{{ userData.id }}
        </h2>

        <form @submit.prevent="handleUpdate" class="space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-green-500 text-xs mb-1 uppercase tracking-widest">Nombre del Sujeto</label>
              <input 
                v-model="editForm.nombre" 
                type="text" 
                required
                class="w-full bg-black border border-green-500 text-green-400 px-3 py-2 rounded focus:ring-1 focus:ring-green-400 outline-none"
              >
            </div>
            <div>
              <label class="block text-green-500 text-xs mb-1 uppercase tracking-widest">Identificador Email</label>
              <input 
                v-model="editForm.email" 
                type="email" 
                required
                class="w-full bg-black border border-green-500 text-green-400 px-3 py-2 rounded focus:ring-1 focus:ring-green-400 outline-none"
              >
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-green-500 text-xs mb-1 uppercase tracking-widest">Rango en el Sistema</label>
              <select v-model="editForm.rol"
                :disabled="userData.id === 1" 
                class="w-full bg-black border border-green-500 text-green-400 px-3 py-2 rounded outline-none"
                :class="{ 'opacity-50 cursor-not-allowed': userData.id === 1 }">
                <option value="ROLE_USER">Usuario Regular</option>
                <option value="ROLE_ADMIN">Administrador</option>
              </select>
            </div>
            <div>
              <label class="block text-green-500 text-xs mb-1 uppercase tracking-widest">Estado de Conexión</label>
              <select v-model="editForm.activo"
                :disabled="userData.id === 1"
                class="w-full bg-black border border-green-500 text-green-400 px-3 py-2 rounded outline-none"
                :class="[editForm.activo ? 'text-green-400' : 'text-red-500', { 'opacity-50 cursor-not-allowed': userData.id === 1 }]">
                <option :value="true">Activo / Operativo</option>
                <option :value="false">Inactivo / Bloqueado</option>
              </select>
            </div>
          </div>

          <div class="pt-6 border-t border-green-900 flex flex-col md:flex-row gap-3">
            <button 
              type="submit" 
              class="btn-matrix flex-1 py-3 font-bold"
              :disabled="updating"
            >
              {{ updating ? '> Procesando...' : '> Guardar Cambios' }}
            </button>
            <button 
              type="button" 
              @click="resetPassword"
              class="btn-matrix bg-yellow-500/10 text-yellow-400 border-yellow-500 py-3 px-6"
              :disabled="resetting"
            >
              {{ resetting ? '> Reiniciando...' : '> Reiniciar Password' }}
            </button>
          </div>
        </form>

        <div v-if="userData.passwordResetRequired" class="mt-4 p-3 bg-yellow-500/10 border border-yellow-500 text-yellow-500 text-xs text-center animate-pulse">
          ⚠️ ALERTA: Este usuario tiene un cambio de contraseña pendiente.
        </div>
      </div>

      <div class="mt-6 flex justify-between px-2">
        <button v-if="userData.id !== 1" @click="confirmDelete"
          class="text-red-500 text-xs hover:underline uppercase tracking-tighter">
            [!] Eliminar usuario permanentemente 
        </button>
        <div class="text-green-900 text-[10px] font-mono">
          SYS_LOG: READ_ACCESS_GRANTED | USER_ID: {{ userData.id }}
        </div>
      </div>
    </div>

    <div v-else class="text-red-500 text-center py-10">
      > ERROR: Usuario no encontrado en el núcleo.
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import Swal from 'sweetalert2';

export default {
  name: 'AdminUserDetail',
  data() {
    return {
      userId: this.$route.params.id,
      userData: null,
      loading: true,
      updating: false,
      resetting: false,
      userEmail: localStorage.getItem('userEmail') || 'Admin',
      editForm: {
        nombre: '',
        email: '',
        rol: '',
        activo: true
      }
    };
  },
  created() {
    this.fetchUser();
  },
  methods: {
    async fetchUser() {
      this.loading = true;
      try {
        const response = await api.get(`/admin/usuarios/${this.userId}`);
        this.userData = response.data;
        this.editForm = {
          nombre: this.userData.nombre,
          email: this.userData.email,
          rol: this.userData.rol,
          activo: this.userData.activo
        };
      } catch (error) {
        console.error('Error fetching user:', error);
        Swal.fire({
          title: '> Error de Acceso',
          text: 'No se pudo recuperar la información del usuario.',
          icon: 'error',
          background: '#000',
          color: '#f00'
        });
      } finally {
        this.loading = false;
      }
    },
    async handleUpdate() {
      this.updating = true;
      try {
        await api.put(`/admin/usuarios/${this.userId}`, this.editForm);
        Swal.fire({
          title: '> Éxito',
          text: 'Ficha de usuario actualizada correctamente.',
          icon: 'success',
          background: '#000',
          color: '#0f0',
          timer: 1500,
          showConfirmButton: false
        });
        this.fetchUser();
      } catch (error) {
        console.error('Error updating user:', error);
        Swal.fire({
          title: '> Error',
          text: 'Fallo al sincronizar cambios con el servidor.',
          icon: 'error',
          background: '#000',
          color: '#f00'
        });
      } finally {
        this.updating = false;
      }
    },
    async resetPassword() {
      const confirm = await Swal.fire({
        title: '> ¿Reiniciar Password?',
        text: 'El sujeto deberá establecer una nueva credencial en la próxima conexión.',
        icon: 'warning',
        showCancelButton: true,
        background: '#000',
        color: '#ffaa00',
        confirmButtonText: '> PROCEDER',
        cancelButtonText: '> CANCELAR',
        confirmButtonColor: '#aa6600'
      });

      if (!confirm.isConfirmed) return;

      this.resetting = true;
      try {
        await api.post(`/admin/usuarios/${this.userId}/reiniciar-password`);
        Swal.fire({
          title: '> Reset Completo',
          text: 'Se ha disparado el protocolo de reinicio.',
          icon: 'success',
          background: '#000',
          color: '#0f0'
        });
        this.fetchUser();
      } catch (error) {
        Swal.fire({
          title: '> Error',
          text: 'No se pudo resetear la contraseña.',
          icon: 'error',
          background: '#000',
          color: '#f00'
        });
      } finally {
        this.resetting = false;
      }
    },
    async confirmDelete() {
      const confirm = await Swal.fire({
        title: '> ¡EXTREMADAMENTE CRÍTICO!',
        text: '¿Confirmas la eliminación total de este registro del sistema?',
        icon: 'warning',
        showCancelButton: true,
        background: '#000',
        color: '#f00',
        confirmButtonText: '> ELIMINAR',
        cancelButtonText: '> ABORTAR',
        confirmButtonColor: '#900'
      });

      if (!confirm.isConfirmed) return;

      try {
        await api.delete(`/admin/usuarios/${this.userId}`);
        Swal.fire({
          title: '> Registro Eliminado',
          text: 'Sujeto purgado del sistema.',
          icon: 'success',
          background: '#000',
          color: '#0f0'
        });
        this.$router.push('/admin');
      } catch (error) {
        Swal.fire({
          title: '> Error de Purga',
          text: 'El sistema denegó la eliminación.',
          icon: 'error',
          background: '#000',
          color: '#f00'
        });
      }
    }
  }
};
</script>

<style scoped>
.terminal-box {
  border: 1px solid #00FF41;
  border-radius: 0.5rem;
}

.btn-matrix {
  background-color: rgba(0, 255, 65, 0.2);
  color: #00ff41;
  border: 1px solid #00ff41;
  transition: all 0.3s ease;
  cursor: pointer;
}

.btn-matrix:hover:not(:disabled) {
  background-color: rgba(0, 255, 65, 0.3);
  box-shadow: 0 0 10px rgba(0, 255, 65, 0.3);
}

.btn-matrix:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.cursor-blink {
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

input::placeholder, select::placeholder {
  color: rgba(0, 255, 65, 0.3);
}
</style>
