import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { setupMocks } from './mock'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  headers: { 'Content-Type': 'application/json' },
})

if (import.meta.env.VITE_MOCK_API === 'true') {
  setupMocks(api)
}

// ─── Helper: decode permissions from JWT payload (no extra lib needed) ────────
function parseJwtPerms(token) {
  if (import.meta.env.VITE_MOCK_API === 'true' && token === 'mock-access-token') {
    return [
      'user.index', 'user.store', 'user.update', 'user.destroy',
      'post.index', 'post.store', 'post.update', 'post.destroy',
      'category.index', 'category.store', 'category.update', 'category.destroy',
      'role.index', 'role.store', 'role.update', 'role.destroy',
      'permission.index', 'permission.store', 'permission.update', 'permission.destroy',
      'module.index', 'module.store', 'module.update', 'module.destroy',
      'log.index'
    ]
  }
  try {
    const payload = JSON.parse(
      atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
    )
    return Array.isArray(payload.perms) ? payload.perms : []
  } catch {
    return []
  }
}

// ─── Request: attach access token ────────────────────────────────────────────
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// ─── Response: handle 401 → refresh token ────────────────────────────────────
let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error)
    else prom.resolve(token)
  })
  failedQueue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return api(originalRequest)
          })
          .catch((err) => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = localStorage.getItem('refresh_token')
      if (!refreshToken) {
        useAuthStore().logout()
        return Promise.reject(error)
      }

      try {
        const res = await axios.post(`/api/v1/auth/refresh?refreshToken=${refreshToken}`)
        const newAccessToken = res.data.data.accessToken

        // ── Sync new token to localStorage + store ──
        localStorage.setItem('access_token', newAccessToken)
        api.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`

        // Decode permissions from new JWT and update store — no extra API call
        const auth = useAuthStore()
        auth.accessToken = newAccessToken
        const newPerms = parseJwtPerms(newAccessToken)
        auth.permissions = newPerms
        localStorage.setItem('auth_permissions', JSON.stringify(newPerms))

        processQueue(null, newAccessToken)
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        return api(originalRequest)
      } catch (err) {
        processQueue(err, null)
        useAuthStore().logout()
        return Promise.reject(err)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default api
