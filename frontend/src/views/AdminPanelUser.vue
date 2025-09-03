<template>
    <div class="min-h-screen bg-black p-4">
        <header class="terminal-box mb-6">
            <div class="flex justify-between items-center">
                <h1 class="text-2xl">> Gestion de Usuarios Panel<span>|</span></h1>
                <div>
                    <button @click="$router.push('/admin')" class="btn-matrix text-sm mr-2">
                        > Volver al panel
                    </button>
                    <button @click="handleLogout" class="btn-matrix text-sm">
                        > Cerrar Sesión
                    </button>
                </div>
            </div>
        </header>

        <!--estadisticas-->
        <div class="terminal-box p-4 mb-6">
            <h2 class="text-xl mb-4">> Estadisticas de Usuarios<span class="cursor-blink">|</span></h2>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div class="text-center p-3 border border-green-500 rounded">
                    <div class="text-2xl text-green-400">{{ users.length }}</div>
                    <div class="text-xs text-green-500">Total Usuarios</div>
                </div>
                <div class="text-center p-3 border border-green-500 rounded">
                    <div class="text-2xl text-blue-400">{{ usuariosCount }}</div>
                    <div class="text-xs text-green-500">Usuarios Normales</div>
                </div>
                <div class="text-center p-3 border border-green-500 rounded">
                    <div class="text-2xl text-yellow-400">{{ administradoresCount }}</div>
                    <div class="text-xs text-green-500">Administradores</div>
                </div>
                <div class="text-center p-3 border border-green-500 rounded">
                    <div class="text-2xl text-purple-400">{{ usuariosActivos}}</div>
                    <div class="text-xs text-green-500">Usuarios Activos</div>
                </div>
            </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
            <!--formulario de registro-->
            <div class="terminal-box p-4">
                <h2 class="text-xl mb-4">> Registrar Nuevo Usuario<span>|</span></h2>
                <form @submit.prevent="registerUser" class="space-y-4">
                    <div>
                        <label class="block text-green-400 text-sm mb-1">Nombre Completo</label>
                        <input v-model="newUser.nombre" type="text" required class="w-full bg-black border border-green-500 text-green-400 px-3 py-2 rounded focus:outline-none focus:ring-1 focus:ring-green-500" placeholder="Ej: Erik Rodriguez">
                    </div>

                    <div>
                        <label class="block text-green-400 text-sm mb-1">Email</label>
                        <input v-model="newUser.email" type="email" required class="w-full bg-black border border-green-500 text-green-400 px-3 py-2 rounded focus:outline-none focus:ring-1 focus:ring-green-500" placeholder="Ej: usuario@devops.com">
                    </div>

                    <div>
                        <label class="block text-green-400 text-sm mb-1">Password</label>
                        <input v-model="newUser.password" type="password" required class="w-full bg-black border border-green-500 text-green-400 px-3 py-2 rounded focus:outline-none focus:ring-1 focus:ring-green-500" placeholder="*****">
                    </div>

                    <div>
                        <label class="block text-green-400 text-sm mb-1">Confirmar Password</label>
                        <input v-model="newUser.confirmPassword" type="password" required class="w-full bg-black border border-green-500 text-green-400 px-3 py-2 rounded focus:outline-none focus:ring-1 focus:ring-green-500" placeholder="Repite el password">
                    </div>

                    <div>
                        <label class="block text-green-400 text-sm mb-1">Rol</label>
                        <select v-model="newUser.rol" class="w-full bg-black border border-green-500 text-green-400 px-3 py-2 rounded focus:outline-none focus:ring-1 focus:ring-green-500">
                            <option value="ROLE_USER">Usuario</option>
                            <option value="ROLE_ADMIN">Administrador</option>
                        </select>
                    </div>

                    <div v-if="passwordMismatch" class="text-red-400 text-sm">
                        > Las contraselas no coinciden
                    </div>

                    <button type="submit" :disabled="registering" class="btn-matrix w-full py-2 flex justify-center items-center">
                        <span v-if="!registering">> crear Usuario</span>
                        <span v-else>> Procesando...</span>
                    </button>

                    <div v-if="registerError" class="text-red-400 text-sm mt-2">
                        > Error: {{ registerError }}
                    </div>
                </form>
            </div>

            <!-- lista de usuarios-->
            <div class="terminal-box p-4">
                <h2 class="text-xl mb-4">> Usuarios del Sistema<span class="text-green-400">({{ filteredUsers.length }}/{{ users.length }})</span><span class="cursor-blink">|</span></h2>

                <!--filtros y busqueda-->
                <div class="flex flex-col md:flex-row md:items-center space-y-2 md:space-y-0 md:space-x-4 mb-4">
                    <div class="flex items-center space-x-2">
                        <label class="text-green-400 text-sm">Filtrar: </label>
                        <select v-model="filtroRol" class="bg-black border border-green-500 text-green-400 px-2 py-1 rounded text-sm">
                            <option value="TODOS">Todos</option>
                            <option value="ROLE_ADMIN">Administradores</option>
                            <option value="ROLE_USER">Usuarios</option>
                        </select>
                    </div>

                    <div class="flex items-center space-x-2 flex-grow">
                        <label class="text-green-400 text-sm">Buscar: </label>
                        <input v-model="searchTerm" type="text" class="bg-black border border-green-500 text-green-400 px-2 py-1 rounded text-sm flex-grow" placeholder="Por nombre o email">
                    </div>
                </div>

                <!--estado de carga-->
                <div v-if="loadingUsers" class="text-green-500 text-center py-4">
                    > Cargando usuarios...
                </div>

                <!-- estado sin resultados-->
                <div v-else-if="filteredUsers.length === 0" class="text-gray-500 text-center py-4">
                    > No hay usuarios {{ filtroMensaje }}
                </div>

                <!--tabla de usuarios-->
                <div v-else class="overflow-x-auto">
                    <table class="w-full text-sm">
                        <thead>
                            <tr class="border-b border-green-500">
                                <th class="text-left py-2 text-green-400">ID</th>
                                <th class="text-left py-2 text-green-400">Nombre</th>
                                <th class="text-left py-2 text-green-400">Email</th>
                                <th class="text-left py-2 text-green-400">Rol</th>
                                <th class="text-left py-2 text-green-400">Estado</th>
                                <th class="text-left py-2 text-green-400">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="user in filteredUsers" :key="user.id" class="border-b border-green-500/30 hover:bg-green-900/10">
                                <td class="py-2 text-green-300 font-mono">{{ user.id }}</td>
                                <td class="py-2">{{ user.nombre || 'Sin nombre' }}</td>
                                <td class="py-2 text-green-300"> {{ user.email }}</td>
                                <td class="py-2">
                                    <span :class="user.rol === 'ROLE_ADMIN' ? 'text-yellow-400' : 'text-blue-400'">
                                        {{ user.rol === 'ROLE_ADMIN' ? 'Admin' : 'Usuario' }}
                                    </span>
                                </td>
                                <td class="py-2">
                                    <span :class="user.activo ? 'text-green-400' : 'text-red-400'">
                                        {{ user.activo ? 'Activo' : 'Inactivo' }}
                                    </span>
                                </td>
                                <td class="py-2">
                                    <div class="realtive">
                                        <button @click="toggleMenu(user.id)" class="text-xs px-3 py-1 bg-gray-800 text-green-400 border border-green-500 rounded hover:bg-green-900/20 transition-colors">
                                            Acciones ▼
                                        </button>

                                        <div v-if="activeMenu === user.id" class="absolute top-full right-0 mt-1 w-48 bg-black border border-green-500 rounded shadow-lg z-10">
                                            <div class="py-1">
                                                <button @click="toggleUserStatus(user); activeMenu = null" class="block w-full text-left px-4 py-2 text-sm hover:bg-green-900/30 text-green-400">
                                                    {{ user.activo ? 'Desactivar': Activar }}
                                                </button>
                                                <button @click="toggleUserRole(user); activeMenu = null" class="block w-full text-left px-4 py-2 text-sm hover:bg-green-900/30 text-blue-400">
                                                    🔄 Cabiar Rol
                                                </button>
                                                <button @click="editUser(user); activeMenu = null" class="block w-full text-left px-4 py-2 text-sm hover: bg-green-900/30 text-yellow-400">
                                                    ✏️ Editar
                                                </button>
                                                <button @click="confirmDelete(user); activeMenu = null" class="block w-full text-left px-4 py-2 text-sm hover:bg-green-900-30 text-red-400">
                                                    🗑️ Eliminar
                                                </button>
                                            </div>
                                        </div>
                                    </div>

                                    <div v-if="updatingUser === user.id" class="text-green-400 text-xs mt-1">
                                        > Actualizando...
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <button @click="fetchUsers" class="btn-matrix mt-4 text-sm" :disabled="loadingUsers">
                    > Actualizar lista
                </button>
            </div>
        </div>
    </div>
</template>

<script>
import api from '@/services/api'

export default {
    data() {
        return {
            users: [],
            loadingUsers: false,
            updatingUser: null,
            activeMenu: null,
            filtroRol: 'TODOS',
            searchTerm: '',
            newUser: {
                nombre: '',
                email: '',
                password: '',
                confirmPassword: '',
                rol: 'ROLE_USER'
            },
            registering: false,
            registerSuccess: false,
            registerError: '',
            userToDelete: null
        }
    },

    computed: {
        filteredUsers() {
            let filtered = this.users;

            //filtrar por rol
            if (this.filtroRol !== 'TODOS') {
                filtered = filtered.filter(user => user.rol === this.filtroRol);
            }

            //filtrar por termino de busqueda
            if (this.searchTerm) {
                const term = this.searchTerm.toLowerCase();
                filtered = filtered.filter(user =>
                    (user.nombre && user.nombre.toLowerCase().includes(term)) ||
                    user.email.toLowerCase().includes(term)
                );
            }

            return filtered;
        },

        filtroMensaje() {
            switch (this.filtroRol) {
                case 'ROLE_ADMIN': return 'administradores';
                case 'ROLE_USER': return 'usuarios';
                default: return this.searchTerm ? 'que coincidan con la búsqueda' : '';
            }
        },

        usuariosCount() {
            return this.users.filter(user => user.rol === 'ROLE_USER').length;
        },

        administradoresCount() {
            return this.users.filter(user => user.rol === 'ROLE_ADMIN').length;
        },

        usuariosActivos() {
            return this.users.filter(user => user.activo).length;
        },

        passwordMismatch() {
            return this.newUser.password !== this.newUser.confirmPassword &&
                this.newUser.confirmPassword !== '';
        }
    },

    created() {
        const userRole = localStorage.getItem('userRole');
        if (userRole !== 'ROLE_ADMIN') {
            this.$router.push('/tickets');
            return;
        }
        this.fetchUsers();
    },

    methods: {
        async fetchUsers() {
            this.loadingUsers = true;
            try {
                const response = await api.get('/admin/usuarios');
                this.users = response.data || [];
            } catch (error) {
                console.error("Error fetching users: ", error);
                if (error.response?.status === 403) {
                    alert('No tienes permisos de administrador');
                } else if (error.response?.status === 401) {
                    alert('Sesion expirada. Por favor inicia sesión nuevamente!...');
                    this.handleLogout();
                }
                this.users = [];
            } finally {
                this.loadingUsers = false;
            }
        },

        async registerUser() {
            if (this.passwordMismatch) {
                this.registerError = 'Las contraseñas no coinciden';
                return;
            }

            this.registering = true;
            this.registerError = '';
            this.registerSuccess = false;

            try {
                await api.post('/admin/usuarios', {
                    nombre: this.newUser.nombre,
                    email: this.newUser.email,
                    password: this.newUser.password,
                    rol: this.newUser.rol
                });

                this.registerSuccess = true;
                this.newUser = {
                    nombre: '',
                    email: '',
                    password: '',
                    confirmPassword: '',
                    rol: 'ROLE_USER'
                };

                //Recargar la lista de usuarios
                this.fetchUsers();

            } catch (error) {
                console.error("Error registering user: ", error);
                this.registerError = error.response?.data?.message || 'Error al crear el usuario';
            }  finally {
                this.registering = false;
            }
        },

        async toggleUserRole(user) {
            this.updatingUser = user.id;
            try {
                const newRole = user.rol === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN';
                await api.put(`/admin/usuarios/${user.id}/role`, { role: newRole });
                user.rol = newRole;
            } catch (error) {
                console.error("Error updating role: ", error);
                alert("Error al cambiar rol: " + (error.response?.data?.message || error.message));
            } finally {
                this.updatingUser = null;
            }
        },

        async toggleUserStatus(user) {
            this.updatingUser = user.id;
            try {
                const newStatus = !user.activo;
                await api.put(`/admin/usuarios/${user.id}/status`, { activo: newStatus });
                user.activo = newStatus;
            } catch (error) {
                console.error("Error updating status: ", error);
                alert("Error al cambiar estado: " + (error.response?.data?.message || error.message));
            } finally {
                this.updatingUser = null;
            }
        },

        handleLogout() {
            localStorage.clear();
            this.$router.push('/login');
        },

        toggleMenu(userId) {
            this.activeMenu = this.activeMenu === userId ? null : userId;
        },

        closeMenu(event) {
            if (!event.target.closest('.relative')) {
                this.activeMenu = null;
            }
        },

        editUser(user) {
            console.log("Editar usuario:", user);
            alert('Funcion de edicion para ${user.nombre} - proximamente');
        },

        confirmDelete(user) {
            if (confirm('Estas seguro de eliminar a ${user.nombre} (${user.email})? Esta acción no se puede deshacer!...')) {
                this.deleteUser(user);
            }
        },

        async deleteUser(user) {
            this.updatingUser = user.id;
            try {
                await api.delete('/admin/usuarios/${user.id}');
                this.fetchUsers();
                alert('Usuario ${user.nombre} eliminado exitosamente');
            } catch (error) {
                console.error("Error deleting user: ", error);
                alert("Error al eliminar usuario: " + (error.response?.data?.message || error.message));
            } finally {
                this.updatingUser = null;
            }
        }
    },

    mounted() {
        document.addEventListener('click', this.closeMenu);
    },

    beforeUnmount() {
        document.removeEventListener('click', this.closeMenu);
    }
}
</script>

<style scoped>
/* Estilos consistentes con el tema terminal */
.terminal-box {
  border: 1px solid #00FF41;
  border-radius: 0.25rem;
}

.btn-matrix {
  background-color: rgba(0, 255, 65, 0.2);
  color: #00FF41;
  border: 1px solid #00FF41;
  padding: 0.5rem 1rem;
  transition: all 0.2s ease;
}

.btn-matrix:hover:not(:disabled) {
  background-color: rgba(0, 255, 65, 0.3);
}

.btn-matrix:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

input, select {
  background-color: #000;
  border: 1px solid #00FF41;
  color: #00FF41;
  padding: 0.5rem;
  border-radius: 0.25rem;
}

input:focus, select:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(0, 255, 65, 0.2);
}

table {
  border-collapse: collapse;
  width: 100%;
}

th, td {
  padding: 0.5rem;
  text-align: left;
}

.cursor-blink {
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

/* Scrollbar personalizado */
.overflow-x-auto::-webkit-scrollbar {
  height: 6px;
}

.overflow-x-auto::-webkit-scrollbar-track {
  background: #000;
}

.overflow-x-auto::-webkit-scrollbar-thumb {
  background-color: #00FF41;
  border-radius: 3px;
}
</style>