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
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>