import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { setupMocks } from './mock'
import { setupEmptyData } from './empty'
import { FULL_PERMISSIONS, isEmptyMode, isMockMode } from './appMode'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  headers: { 'Content-Type': 'application/json' },
})

if (isEmptyMode) {
  setupEmptyData(api)
} else if (isMockMode) {
  setupMocks(api)
}

// ─── Helper: decode permissions from JWT payload (no extra lib needed) ────────
function parseJwtPerms(token) {
  if ((isMockMode && token === 'mock-access-token') || (isEmptyMode && token === 'empty-access-token')) {
    return FULL_PERMISSIONS
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

    // Skip refresh logic for login/refresh/logout to avoid loops
    const isAuthPath = originalRequest.url.includes('/auth/login') || 
                       originalRequest.url.includes('/auth/refresh') || 
                       originalRequest.url.includes('/auth/logout')

    if (error.response?.status === 401 && !originalRequest._retry && !isAuthPath) {
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
        // Use global axios to avoid interceptors loop, ensure /api/v1 prefix
        const refreshUrl = originalRequest.url.startsWith('/api/v1') 
          ? '/api/v1/auth/refresh' 
          : '/auth/refresh'
        
        const res = await axios.post(`${refreshUrl}?refreshToken=${refreshToken}`)
        const newAccessToken = res.data.data.accessToken

        // Sync new token to localStorage + store
        localStorage.setItem('access_token', newAccessToken)
        
        const auth = useAuthStore()
        auth.accessToken = newAccessToken
        
        // Decode permissions from new JWT
        const newPerms = parseJwtPerms(newAccessToken)
        auth.permissions = newPerms
        localStorage.setItem('auth_permissions', JSON.stringify(newPerms))

        processQueue(null, newAccessToken)
        
        // Update current request
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
