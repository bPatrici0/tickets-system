<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
    <!-- Estado de tickets -->
    <div class="terminal-box p-4 bg-black/40 backdrop-blur-sm relative overflow-hidden group">
      <div class="absolute inset-0 bg-green-500/5 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"></div>
      <h3 class="text-green-400 text-sm mb-4 font-mono flex items-center">
        <span class="mr-2">></span> ESTADO DE TICKETS
        <span class="ml-auto text-[10px] opacity-50">Estado de Tickets</span>
      </h3>
      <div class="h-64 relative flex justify-center">
        <Doughnut v-if="loaded" :data="statusChartData" :options="chartOptions" />
        <div v-else class="flex items-center justify-center h-full text-green-800 font-mono animate-pulse">
          ...CARGANDO_DATOS...
        </div>
      </div>
    </div>

    <!-- Usuarios por rol -->
    <div class="terminal-box p-4 bg-black/40 backdrop-blur-sm relative overflow-hidden group">
      <div class="absolute inset-0 bg-blue-500/5 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"></div>
      <h3 class="text-blue-400 text-sm mb-4 font-mono flex items-center">
        <span class="mr-2">></span> USUARIOS POR ROL
        <span class="ml-auto text-[10px] opacity-50">Usuarios por rol</span>
      </h3>
      <div class="h-64 relative flex justify-center">
        <Pie v-if="loaded" :data="userChartData" :options="chartOptions" />
        <div v-else class="flex items-center justify-center h-full text-blue-800 font-mono animate-pulse">
          ...CARGANDO_DATOS...
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Doughnut, Pie } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  CategoryScale
} from 'chart.js'

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale)

export default {
  name: 'AdminDashboard',
  components: { Doughnut, Pie },
  props: {
    tickets: {
      type: Array,
      required: true
    },
    users: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      loaded: false,
      chartOptions: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              color: '#00ff41',
              font: {
                family: 'monospace',
                size: 10
              },
              usePointStyle: true,
              padding: 20
            }
          },
          tooltip: {
            backgroundColor: 'rgba(0, 0, 0, 0.9)',
            titleColor: '#00ff41',
            bodyColor: '#00ff41',
            borderColor: '#00ff41',
            borderWidth: 1,
            cornerRadius: 0,
            titleFont: { family: 'monospace' },
            bodyFont: { family: 'monospace' }
          }
        },
        elements: {
          arc: {
            borderWidth: 1,
            borderColor: '#000'
          }
        }
      }
    }
  },
  computed: {
    statusChartData() {
      const stats = {
        ABIERTO: 0,
        EN_PROGRESO: 0,
        RESUELTO: 0
      }
      this.tickets.forEach(ticket => {
        if (stats[ticket.estado] !== undefined) {
          stats[ticket.estado]++
        }
      })

      return {
        labels: ['Abiertos', 'En Progreso', 'Resueltos'],
        datasets: [{
          data: [stats.ABIERTO, stats.EN_PROGRESO, stats.RESUELTO],
          backgroundColor: [
            'rgba(255, 206, 86, 0.6)', // Yellow
            'rgba(54, 162, 235, 0.6)', // Blue
            'rgba(0, 255, 65, 0.6)'    // Green
          ],
          borderColor: [
            'rgba(255, 206, 86, 1)',
            'rgba(54, 162, 235, 1)',
            'rgba(0, 255, 65, 1)'
          ],
          hoverOffset: 15,
          hoverBorderWidth: 2
        }]
      }
    },
    userChartData() {
      const stats = {
        ROLE_USER: 0,
        ROLE_ADMIN: 0
      }
      this.users.forEach(user => {
        if (stats[user.rol] !== undefined) {
          stats[user.rol]++
        }
      })

      return {
        labels: ['Usuarios', 'Admins'],
        datasets: [{
          data: [stats.ROLE_USER, stats.ROLE_ADMIN],
          backgroundColor: [
            'rgba(0, 243, 255, 0.6)', // Cyan
            'rgba(0, 255, 65, 0.6)'   // Green
          ],
          borderColor: [
            'rgba(0, 243, 255, 1)',
            'rgba(0, 255, 65, 1)'
          ],
          hoverOffset: 15
        }]
      }
    }
  },
  watch: {
    tickets: {
      immediate: true,
      handler(val) {
        if (val && val.length > 0) this.loaded = true
      }
    }
  }
}
</script>

<style scoped>
.terminal-box {
  border: 1px solid #00FF41;
  box-shadow: 0 0 10px rgba(0, 255, 65, 0.1);
}
</style>
