<template>
  <v-container fluid class="fill-height d-flex justify-center align-center" style="background-color: #000; min-height: 100vh;">
    <v-card width="400" class="pa-6 elevation-10" style="background-color: #111; color: #00ff88; border: 1px solid #00ff88;">
      <v-card-title class="text-h5 mb-4" style="color: #00ff88;">
        Login Terminal
      </v-card-title>
      <v-card-text>
        <v-form
          @submit.prevent="handleLogin"
          class="d-flex flex-column"
          style="width: 100%;"
        >
          <v-text-field
            v-model="email"
            label="Email"
            type="email"
            required
            class="mb-4"
            style="color: #00ff88;"
          />
          <v-text-field
            v-model="password"
            label="Password"
            type="password"
            required
            class="mb-4"
            style="color: #00ff88;"
          />
          <v-btn type="submit"
            block
            elevation="2"
            style="background-color: #00ff88; color: #000; text-align: center;"
          >
            Login
          </v-btn>
        </v-form>
        <v-alert v-if="error" type="error" class="mt-4" style="background-color: #330000; color: #ff4444;">
          {{ error }}
        </v-alert>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const email = ref('')
const password = ref('')
const error = ref('')
const authStore = useAuthStore()
const router = useRouter()

const handleLogin = async () => {
  try {
    await authStore.login(email.value, password.value)
    router.push('/dashboard')
  } catch (err) {
    error.value = 'Credenciales inválidas!...'
  }
}
</script>
