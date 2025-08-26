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
            </div>
        </div>
    </div>
</template>