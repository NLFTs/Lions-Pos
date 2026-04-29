import { defineStore } from 'pinia'
import { ref, shallowRef, computed } from 'vue'
import axios from 'axios'
import api from '@/lib/api'
import router from '@/router'

// ─── Helpers ─────────────────────────────────────────────────────────────────
function loadJson(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

export const useAuthStore = defineStore('auth', () => {
  // ─── State ─────────────────────────────────────────────────────────────────
  // shallowRef: user & permissions tidak perlu deep reactive — hanya di-replace keseluruhan
  const accessToken  = ref(localStorage.getItem('access_token')  || null)
  const refreshToken = ref(localStorage.getItem('refresh_token') || null)
  const user         = shallowRef(loadJson('auth_user', null))
  const permissions  = shallowRef(loadJson('auth_permissions', []))

  const isAuthenticated = computed(() => !!accessToken.value)

  // ─── fetchMe ───────────────────────────────────────────────────────────────
  async function fetchMe() {
    try {
      const res = await api.get('/api/v1/auth/me')
      const me = res.data.data
      // Assign baru agar shallowRef trigger reaktivitas
      user.value = {
        id:       me.id,
        username: me.username,
        fullname: me.fullname,
        roles:    me.roles ?? [],
      }
      permissions.value = me.permissions ?? []
      localStorage.setItem('auth_user',        JSON.stringify(user.value))
      localStorage.setItem('auth_permissions', JSON.stringify(permissions.value))
    } catch (_) {
      // silently ignore (token might just be expired — logout will handle it)
    }
  }

  // ─── login ─────────────────────────────────────────────────────────────────
  async function login(username, password) {
    const res = await axios.post('/api/v1/auth/login', { username, password })
    const { accessToken: at, refreshToken: rt } = res.data.data
    accessToken.value  = at
    refreshToken.value = rt
    localStorage.setItem('access_token',  at)
    localStorage.setItem('refresh_token', rt)
    await fetchMe()
  }

  // ─── logout ────────────────────────────────────────────────────────────────
  async function logout() {
    try {
      if (refreshToken.value) {
        await axios.post(`/api/v1/auth/logout?refreshToken=${refreshToken.value}`)
      }
    } catch (_) {
      // ignore logout errors
    } finally {
      accessToken.value  = null
      refreshToken.value = null
      user.value         = null
      permissions.value  = []
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      localStorage.removeItem('auth_user')
      localStorage.removeItem('auth_permissions')
      router.push('/login')
    }
  }

  return { accessToken, refreshToken, user, permissions, isAuthenticated, login, logout, fetchMe }
})
