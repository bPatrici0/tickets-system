<template>
    <div class="min-h-screen bg-black p-4">
        <header class="terminal-box mb-6">
            <div class="flex justify-between items-center">
                <h1 class="text-2xl">
                    > Administración - Ticket #{{ ticket.id }}<span class="cursor-blink">|</span>
                </h1>
                <div>
                    <button @click="$router.push('/admin/tickets')" class="btn-matrix text-sm mr-2">
                        > Volver a Tickets
                    </button>
                    <button @click="handleLogout" class="btn-matrix text-sm">
                        > Cerrar Sesión
                    </button>
                </div>
            </div>
        </header>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <!--panel izquierdo-->
            <div class="terminal-box lg:col-span-2">
                <!--encabezado-->
                <div class="border-b border-green-500 pb-4 mb-4">
                    <div class="flex justify-between items-start mb-4">
                        <h1 class="text-3xl text-green-400">> {{ ticket.titulo }}</h1>
                        <span class="px-3 py-1 rounded text-sm font-bold"
                            :class="statusClass(ticket.estado)">
                            {{ ticket.estado }}
                        </span>
                    </div>

                    <div class="grid grid-cols-2 gap-4 text-sm">
                        <div>
                            <span class="text-green-500">> Creado por: </span>
                            <span class="ml-2">{{ ticket.usuario?.nombre || 'Usuario' }}</span>
                        </div>
                        <div>
                            <span class="text-green-500">> Email: </span>
                            <span class="ml-2">{{ ticket.usuario?.email || 'N/A' }}</span>
                        </div>
                        <div>
                            <span class="text-green-500">> Fecha creacion: </span>
                            <span class="ml-2">{{ formatDate(ticket.fechaActualizacion) }}</span>
                        </div>
                        <div>
                            <span class="text-green-500">> Última Aactualización: </span>
                            <span class="ml-2">{{ formatDate(ticket.fechaActualizacion) }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>