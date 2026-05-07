import { defineStore } from 'pinia'
import { ref, shallowRef, computed } from 'vue'
import axios from 'axios'
import api from '@/lib/api'
import { FULL_PERMISSIONS, isEmptyMode, isMockMode } from '@/lib/appMode'

// ─── Helpers ─────────────────────────────────────────────────────────────────
function loadJson(key, fallback) {
  if (typeof window === 'undefined') return fallback;
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

function loadString(key, fallback) {
  if (typeof window === 'undefined') return fallback;
  try {
    return localStorage.getItem(key) || fallback;
  } catch {
    return fallback
  }
}

export const useAuthStore = defineStore('auth', () => {
  // ─── State ─────────────────────────────────────────────────────────────────
  // shallowRef: user & permissions tidak perlu deep reactive — hanya di-replace keseluruhan
  const offlineAccessToken = isEmptyMode ? 'empty-access-token' : (isMockMode ? 'mock-access-token' : null)
  const offlineRefreshToken = isEmptyMode ? 'empty-refresh-token' : (isMockMode ? 'mock-refresh-token' : null)
  const offlineUser = isEmptyMode
    ? { id: 'empty-user', username: 'admin', fullname: 'Empty Admin', roles: ['ADMIN'] }
    : (isMockMode ? { id: 'mock-1', username: 'admin', fullname: 'Mock Admin', roles: ['ADMIN'] } : null)

  const accessToken = ref(loadString('access_token', isMockMode ? offlineAccessToken : null))
  const refreshToken = ref(loadString('refresh_token', isMockMode ? offlineRefreshToken : null))
  const user = shallowRef(loadJson('auth_user', (isMockMode || isEmptyMode) ? offlineUser : null))
  const permissions = shallowRef(loadJson('auth_permissions', (isMockMode || isEmptyMode) ? FULL_PERMISSIONS : []))

  // ─── Auto-sync mock data ───────────────────────────────────────────────────
  // Jika dalam mode mock/empty tapi user tidak punya role atau token kosong,
  // paksa gunakan data offline agar dashboard tampil penuh.
  if (isMockMode || isEmptyMode) {
    const hasRoles = user.value?.roles && user.value.roles.length > 0
    if (!hasRoles || !accessToken.value) {
      user.value = offlineUser
      permissions.value = FULL_PERMISSIONS
      if (!accessToken.value) accessToken.value = offlineAccessToken
      if (!refreshToken.value) refreshToken.value = offlineRefreshToken
    }
  }

  const isAuthenticated = computed(() => !!accessToken.value)

  // ─── fetchMe ───────────────────────────────────────────────────────────────
  async function fetchMe() {
    try {
      const res = await api.get('/api/v1/auth/me')
      const me = res.data.data
      // Assign baru agar shallowRef trigger reaktivitas
      user.value = {
        id: me.id,
        username: me.username,
        fullname: me.fullname,
        roles: me.roles ?? [],
      }
      permissions.value = me.permissions ?? []
      localStorage.setItem('auth_user', JSON.stringify(user.value))
      localStorage.setItem('auth_permissions', JSON.stringify(permissions.value))
    } catch (_) {
      // silently ignore (token might just be expired — logout will handle it)
    }
  }

  // ─── login ─────────────────────────────────────────────────────────────────
  async function login(username, password) {
    const res = await api.post('/api/v1/auth/login', { username, password })
    const { accessToken: at, refreshToken: rt } = res.data.data
    accessToken.value = at
    refreshToken.value = rt
    localStorage.setItem('access_token', at)
    localStorage.setItem('refresh_token', rt)
    await fetchMe()
  }

  // ─── logout ────────────────────────────────────────────────────────────────
  async function logout() {
    try {
      if (refreshToken.value) {
        await api.post(`/api/v1/auth/logout?refreshToken=${refreshToken.value}`)
      }
    } catch (_) {
      // ignore logout errors
    } finally {
      accessToken.value = null
      refreshToken.value = null
      user.value = null
      permissions.value = []
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      localStorage.removeItem('auth_user')
      localStorage.removeItem('auth_permissions')
      if (typeof window !== 'undefined') {
        window.location.href = '/login'
      }
    }
  }

  return { accessToken, refreshToken, user, permissions, isAuthenticated, login, logout, fetchMe }
})
