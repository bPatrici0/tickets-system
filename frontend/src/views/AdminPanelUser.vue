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
                <div class="text-center p-3 border-green-500 rounded">
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
            <div="terminal-box mb-4">
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
                        <span v-else="> Procesando..."></span>
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
                                <th class="text-left py-2 text-green-400"></th>
                            </tr>
                        </thead>
                    </table
                </div>
            </div>
        </div>
    </div>
</template>