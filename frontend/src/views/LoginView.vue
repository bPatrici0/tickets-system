<template>
  <v-container class="fill-height d-flex justify-center align-center">
    <v-card width="400" class="ma-auto mt-12 pa-6 elevation-10">
      <v-card-title class="text-h5">Login Tickets</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="handleLogin">
          <v-text-field
            v-model="email"
            label="Email"
            type="email"
            required
            class="mb-4"
          ></v-text-field>
          <v-text-field
            v-model="password"
            label="Password"
            type="password"
            required
            class="mb-4"
          ></v-text-field>
          <v-btn type="submit" color="primary" block elevation="2">Login</v-btn>
        </v-form>
        <v-alert v-if="error" type="error" class="mt-4">{{ error }}</v-alert>
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
