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

                <!--descripcion del problema-->
                <div class="mb-6">
                    <h3 class="text-lg text-green-400 mb-2">> Descripción:</h3>
                    <div class="bg-black border-green-500 p-4 text-green-300 whitespace-pre-wrap">
                        {{ ticket.descripcion }}
                    </div>
                </div>

                <!--historial de comentarios-->
                <div class="mb-6" v-if="ticket.comentarios && ticket.comentarios.length > 0">
                    <h3 class="text-lg text-green-400 mb-4">> Historial: </h3>
                    <div class="space-y-4">
                        <div v-for="comentario in ticket.comentarios" :key="comentario.id"
                            class="border-1-2 border-green-500 pl-4 py-3">
                            <div class="flex justify-between items-center text-sm text-green-500 mb-2">
                                <div>
                                    <span class="font-bold">{{ comentario.autor?.nombre || 'Usuario }}</span>
                                    <span class="text-gray-400 ml-2">({{ comentario.autor?.rol === 'ROLE_ADMIN' ? 'Admin' : 'Usuario' }})</span>
                                    <span>{{ formatDate(comentario.fechaCreacion) }}</span>
                                </div>
                                <pre class="text-green-300 whitespace-pre-wrap">{{ comentario.contenido }}</pre>
                            </div>
                        </div>
                    </div>

                    <!--mensaje cuando no hay comentarios-->
                    <div v-else class="text-gray-500 italic">
                        > No hay comentarios para este ticket...
                    </div>
                </div>

                <!--Panel derecho-->
                <div class="terminal-box">
                    <h2 class="text-xl text-green-400 mb-4">> Acciones de administrador<span class="cursor-blink">|</span></h2>
                </div>

                <!--cambiar estado del ticket-->
                <div class="mb-6">
                    <h3 class="text-lg mb-2">> Cambiar Estado:</h3>
                    <div class="grid grid-cols-1 gap-2">
                        <button @click="cambiarEstado('ABIERTO')"
                            :disabled="ticket.estado === 'ABIERTO' || ticket.estado === 'RESUELTO'"
                            class="btn-status bg-yellow-500/20 text-yellow-400 border-yellow-500">
                            > Marcar como ABIERTO
                        </button>
                        <button @click="cambiarEstado('EN_PROGRESO' || ticket.estado === 'RESUELTO')"
                            :disabled="ticket.estado === 'EN_PROGRESO' || ticket.estado === 'RESUELTO'"
                            class="btn-estatus bg-blue-500/20 text-blue-400 border-blue-500">
                            > Marcar como EN PROGRESO
                        </button>
                        <button @click="cambiarEstado('RESUELTO')"
                            :disabled="ticket.estado === 'RESUELTO'"
                            class="btn-status bg-green-500/20 text-green-400 border-green-500">
                            > Marcar como RESUELTO
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>