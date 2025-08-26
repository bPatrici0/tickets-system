<template>
    <div class="min-h-screen bg-black p-4">
        <header class="terminal-box mb-6">
            <div class="flex justify-between items-center">
                <h1 class="text-2xl">> Gestion de Usuarios Panel<span>|</span></h1>
                <div>
                    <button @click="$router.push('/admin')" class="btn-matrix text-sm mr-2">
                        > Volver al panel
                    </button>
                    <button>
                        > Cerrar Sesión
                    </button>
                </div>
            </div>
        </header>
    </div>
</template>