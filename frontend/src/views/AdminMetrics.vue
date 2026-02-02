<template>
  <div class="min-h-screen bg-black p-4">
    <!-- Header -->
    <header class="terminal-box mb-6">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl text-green-400 font-mono">> MÉTRICAS DE EQUIPO [PERFORMANCE_LOG]<span class="cursor-blink">|</span></h1>
        <button @click="$router.push('/admin')" class="btn-matrix text-sm">
          > VOLVER AL PANEL
        </button>
      </div>
    </header>

    <!-- Main Chart Section -->
    <div class="terminal-box p-6 bg-black/40 backdrop-blur-sm relative overflow-hidden group mb-6">
      <div class="absolute inset-0 bg-green-500/5 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"></div>
      
      <div class="flex items-center justify-between mb-8">
        <h2 class="text-green-400 text-lg font-mono">
          <span class="mr-2">></span> RANKING DE RESOLUCIONES POR TÉCNICO
        </h2>
        <div class="text-[10px] text-green-800 font-mono">USUARIO_ID | RESOLUCIONES_COUNT</div>
      </div>

      <div class="h-[450px] relative">
        <Bar v-if="loaded" :data="chartData" :options="chartOptions" />
        <div v-else class="flex flex-col items-center justify-center h-full space-y-4">
          <div class="terminal-loader"></div>
          <div class="text-green-500 font-mono animate-pulse">...ESCANEANDO_ACTIVIDAD_DE_AUDITORIA...</div>
        </div>
      </div>

      <!-- Detalle en tabla -->
      <div v-if="loaded" class="mt-8 overflow-x-auto">
        <table class="w-full text-left font-mono text-sm border-t border-green-900/50">
          <thead>
            <tr class="text-green-600 border-b border-green-900/30">
              <th class="py-3 px-4">TÉCNICO</th>
              <th class="py-3 px-4 text-center">TICKETS RESUELTOS</th>
              <th class="py-3 px-4 text-right">EFICIENCIA</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in performanceData" :key="index" class="border-b border-green-900/10 hover:bg-green-500/5 transition-colors">
              <td class="py-3 px-4 text-green-400">{{ item.usuario }}</td>
              <td class="py-3 px-4 text-center text-green-300">{{ item.cantidadResoluciones }}</td>
              <td class="py-3 px-4 text-right">
                <div class="w-24 bg-green-900/20 h-2 ml-auto rounded-full overflow-hidden">
                  <div class="bg-green-500 h-full" :style="{ width: getPercentage(item.cantidadResoluciones) + '%' }"></div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Footer Stats -->
    <div v-if="loaded" class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="terminal-box p-4 border-yellow-500/50">
        <div class="text-yellow-500 text-xs mb-1">TOTAL_RESOLUCIONES</div>
        <div class="text-2xl text-yellow-400">{{ totalResoluciones }}</div>
      </div>
      <div class="terminal-box p-4 border-blue-500/50">
        <div class="text-blue-500 text-xs mb-1">TÉCNICO_LÍDER</div>
        <div class="text-2xl text-blue-400">{{ liderTecnico }}</div>
      </div>
      <div class="terminal-box p-4 border-green-500/50">
        <div class="text-green-500 text-xs mb-1">PROMEDIO_POR_TÉCNICO</div>
        <div class="text-2xl text-green-400">{{ promedioResoluciones }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale
} from 'chart.js'
import api from '@/services/api'
import Swal from 'sweetalert2'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

export default {
  name: 'AdminMetrics',
  components: { Bar },
  data() {
    return {
      loaded: false,
      performanceData: [],
      chartOptions: {
        responsive: true,
        maintainAspectRatio: false,
        indexAxis: 'y', // Gráfica horizontal para nombres largos (emails)
        plugins: {
          legend: { display: false },
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
        scales: {
          x: {
            grid: { color: 'rgba(0, 255, 65, 0.1)' },
            ticks: { color: '#00ff41', font: { family: 'monospace' } }
          },
          y: {
            grid: { display: false },
            ticks: { color: '#00ff41', font: { family: 'monospace' } }
          }
        }
      }
    }
  },
  computed: {
    chartData() {
      return {
        labels: this.performanceData.map(item => item.usuario.split('@')[0]), // Mostrar solo el alias para no saturar
        datasets: [{
          label: 'Tickets Resueltos',
          data: this.performanceData.map(item => item.cantidadResoluciones),
          backgroundColor: 'rgba(0, 255, 65, 0.4)',
          borderColor: '#00ff41',
          borderWidth: 1,
          hoverBackgroundColor: 'rgba(0, 255, 65, 0.6)'
        }]
      }
    },
    totalResoluciones() {
      return this.performanceData.reduce((acc, curr) => acc + curr.cantidadResoluciones, 0)
    },
    liderTecnico() {
      if (this.performanceData.length === 0) return 'N/A'
      return this.performanceData[0].usuario.split('@')[0]
    },
    promedioResoluciones() {
      if (this.performanceData.length === 0) return 0
      return (this.totalResoluciones / this.performanceData.length).toFixed(1)
    }
  },
  created() {
    this.fetchPerformanceData()
  },
  methods: {
    async fetchPerformanceData() {
      try {
        const response = await api.get('/auditoria/performance')
        this.performanceData = response.data || []
        this.loaded = true
      } catch (error) {
        console.error("Error fetching performance data:", error)
        Swal.fire({
          title: '> ERROR_DE_SISTEMA',
          text: 'No se pudo acceder a los registros de rendimiento.',
          icon: 'error',
          background: '#000',
          color: '#ff0000',
          customClass: { popup: 'border border-red-500 rounded-none' }
        })
      }
    },
    getPercentage(val) {
      if (!this.totalResoluciones) return 0
      return (val / this.performanceData[0].cantidadResoluciones * 100).toFixed(0)
    }
  }
}
</script>

<style scoped>
.terminal-box {
  border: 1px solid #00FF41;
  box-shadow: 0 0 10px rgba(0, 255, 65, 0.1);
}
.btn-matrix {
  background-color: rgba(0, 255, 65, 0.2);
  color: #00ff41;
  border: 1px solid #00ff41;
  padding: 0.5rem 1rem;
  transition: all 0.3s ease;
  font-family: monospace;
}
.btn-matrix:hover {
  background-color: rgba(0, 255, 65, 0.3);
}
.cursor-blink {
  animation: blink 1s step-end infinite;
}
@keyframes blink { 50% { opacity: 0; } }

.terminal-loader {
  width: 40px;
  height: 40px;
  border: 2px solid #005500;
  border-top: 2px solid #00FF41;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>
