import { defineStore } from 'pinia'
import axios from 'axios'
import { useRouter } from 'vue-router'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
  }),
  actions: {
    async login(email, password) {
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        email,
        password
      })
      this.token = null
      localStorage.removeItem('token')
    }
  }
})
