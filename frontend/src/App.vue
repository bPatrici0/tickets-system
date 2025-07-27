<template>
    <div id="app" class="min-h-screen bg-black  text-green-200 font-mono">

        <!--contenedor de vistas-->
        <router-view class="terminal-box" />
    </div>
</template>

<script>
export default {
    name: 'App',
}
</script>